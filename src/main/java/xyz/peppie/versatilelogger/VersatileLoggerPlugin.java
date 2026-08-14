package xyz.peppie.versatilelogger;

import com.google.inject.Provides;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MessageNode;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import xyz.peppie.versatilelogger.chat.CategoryConfigResolver;
import xyz.peppie.versatilelogger.chat.CategorySettings;
import xyz.peppie.versatilelogger.chat.ChatCategory;
import xyz.peppie.versatilelogger.chat.ChatMessageRouter;
import xyz.peppie.versatilelogger.chat.ClanContextResolver;
import xyz.peppie.versatilelogger.chat.PendingCommandMessage;
import xyz.peppie.versatilelogger.config.VersatileLoggerConfig;
import xyz.peppie.versatilelogger.config.VersatileLoggerConfig.FormatMode;
import xyz.peppie.versatilelogger.dto.ClanChatDto;
import xyz.peppie.versatilelogger.dto.FriendsChatDto;
import xyz.peppie.versatilelogger.dto.FullMessagePayload;
import xyz.peppie.versatilelogger.dto.MessageDto;
import xyz.peppie.versatilelogger.dto.UserDto;
import xyz.peppie.versatilelogger.format.LineIncludeOption;
import xyz.peppie.versatilelogger.format.MessageFormatter;
import xyz.peppie.versatilelogger.io.local.LocalLogWriter;
import xyz.peppie.versatilelogger.io.local.LogRetentionManager;
import xyz.peppie.versatilelogger.io.remote.RemoteLogSender;

@Slf4j
@PluginDescriptor(
	name = "Versatile Logger",
	description = "Highly configurable logging tool: logs each chat/message category independently to local files and/or a remote HTTP endpoint (e.g. a Discord webhook).",
	tags = {"chat", "logging", "webhook", "discord"}
)
public class VersatileLoggerPlugin extends Plugin
{
	private static final long PENDING_COMMAND_TIMEOUT_MS = 8_000;

	private static final int MAX_PENDING_COMMANDS = 25;

	@Inject
	private Client client;

	@Inject
	private VersatileLoggerConfig config;

	@Inject
	private ClanContextResolver clanContextResolver;

	@Inject
	private RemoteLogSender remoteLogSender;

	@Inject
	private LocalLogWriter localLogWriter;

	@Inject
	private LogRetentionManager logRetentionManager;

	private final ChatMessageRouter router = new ChatMessageRouter();
	private final CategoryConfigResolver categoryConfigResolver = new CategoryConfigResolver();
	private final MessageFormatter messageFormatter = new MessageFormatter();

	private final Map<MessageNode, PendingCommandMessage> pendingCommandMessages = new LinkedHashMap<>();

	private ScheduledExecutorService fileExecutor;
	private ScheduledFuture<?> retentionTask;
	private String activeAccountName;

	@Override
	protected void startUp()
	{
		fileExecutor = Executors.newSingleThreadScheduledExecutor();
		localLogWriter.startUp(fileExecutor);
		remoteLogSender.startUp();

		fileExecutor.execute(() -> logRetentionManager.run(LocalLogWriter.PLUGIN_ROOT, config.retentionDays()));
		retentionTask = fileExecutor.scheduleAtFixedRate(
			() -> logRetentionManager.run(LocalLogWriter.PLUGIN_ROOT, config.retentionDays()),
			24, 24, TimeUnit.HOURS);

		// Plugin may be enabled while already logged in.
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			beginSessionIfNeeded();
		}

		log.info("Versatile Chat Logger started");
	}

	@Override
	protected void shutDown()
	{
		if (retentionTask != null)
		{
			retentionTask.cancel(false);
			retentionTask = null;
		}

		localLogWriter.endSession();
		activeAccountName = null;
		pendingCommandMessages.clear();
		remoteLogSender.shutDown();

		if (fileExecutor != null)
		{
			fileExecutor.shutdownNow();
			fileExecutor = null;
		}

		log.info("Versatile Chat Logger stopped");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOGGED_IN)
		{
			beginSessionIfNeeded();
		}
		else if (state == GameState.LOGIN_SCREEN || state == GameState.CONNECTION_LOST)
		{
			if (activeAccountName != null)
			{
				localLogWriter.endSession();
				activeAccountName = null;
			}
			pendingCommandMessages.clear();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			beginSessionIfNeeded();
		}
		processPendingCommandMessages();
	}

	private void beginSessionIfNeeded()
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null || localPlayer.getName() == null)
		{
			return;
		}

		String name = localPlayer.getName();
		if (name.equals(activeAccountName))
		{
			return;
		}

		localLogWriter.beginSession(name, config.perCategoryLocalFiles());
		activeAccountName = name;
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		Optional<ChatCategory> maybeCategory = router.categorize(chatMessage.getType(), chatMessage.getMessage());
		if (maybeCategory.isEmpty())
		{
			return;
		}
		ChatCategory category = maybeCategory.get();

		MessageNode node = chatMessage.getMessageNode();
		if (node == null)
		{
			return;
		}

		CategorySettings settings = categoryConfigResolver.resolve(config, category);
		if (!settings.isLocalEnabled() && !settings.isRemoteEnabled())
		{
			return;
		}

		logMessage(node, category, chatMessage.getType(), settings, false);

		if (settings.getInclude().contains(LineIncludeOption.COMMAND_OUTPUT) && looksLikeUnresolvedCommand(node))
		{
			trackPendingCommand(node, category, chatMessage.getType(), settings);
		}
	}

	private static boolean looksLikeUnresolvedCommand(MessageNode node)
	{
		if (node.getRuneLiteFormatMessage() != null)
		{
			return false;
		}
		String value = node.getValue();
		return value != null && value.trim().startsWith("!");
	}

	private void trackPendingCommand(MessageNode node, ChatCategory category, ChatMessageType type, CategorySettings settings)
	{
		if (pendingCommandMessages.size() >= MAX_PENDING_COMMANDS)
		{
			Iterator<MessageNode> oldest = pendingCommandMessages.keySet().iterator();
			if (oldest.hasNext())
			{
				oldest.next();
				oldest.remove();
			}
		}
		pendingCommandMessages.put(node,
			new PendingCommandMessage(node, category, type, settings, System.currentTimeMillis()));
	}

	private void processPendingCommandMessages()
	{
		if (pendingCommandMessages.isEmpty())
		{
			return;
		}

		long now = System.currentTimeMillis();
		Iterator<PendingCommandMessage> iterator = pendingCommandMessages.values().iterator();
		while (iterator.hasNext())
		{
			PendingCommandMessage pending = iterator.next();
			MessageNode node = pending.getNode();

			if (node.getRuneLiteFormatMessage() != null)
			{
				logMessage(node, pending.getCategory(), pending.getType(), pending.getSettings(), true);
				iterator.remove();
			}
			else if (now - pending.getEnqueuedAtMillis() > PENDING_COMMAND_TIMEOUT_MS)
			{
				iterator.remove();
			}
		}
	}

	private void logMessage(MessageNode node, ChatCategory category, ChatMessageType type, CategorySettings settings, boolean edited)
	{
		String line = messageFormatter.buildLine(node, settings.getInclude(), settings.isDetailedTimestamp());

		if (settings.isLocalEnabled())
		{
			localLogWriter.writeLine(category, edited ? "[updated] " + line : line);
		}

		if (settings.isRemoteEnabled())
		{
			sendRemote(node, type, settings, line, edited);
		}
	}

	private void sendRemote(MessageNode node, ChatMessageType type, CategorySettings settings, String line, boolean edited)
	{
		String url = settings.getEffectiveRemoteUrl();
		if (url == null || url.isBlank())
		{
			return;
		}
		String authToken = config.authToken();

		if (settings.getEffectiveFormatMode() == FormatMode.FULL)
		{
			MessageDto messageDto = clanContextResolver.buildMessage(node, edited);
			UserDto userDto = clanContextResolver.buildUser(node);
			ClanChatDto clanChatDto = clanContextResolver.buildClanChat(type);
			FriendsChatDto friendsChatDto = clanContextResolver.buildFriendsChat(type);
			FullMessagePayload payload = new FullMessagePayload(messageDto, userDto, clanChatDto, friendsChatDto);
			remoteLogSender.sendFull(url, authToken, payload);
		}
		else
		{
			remoteLogSender.sendInGameMessage(url, authToken, edited ? "(edited) " + line : line);
		}
	}

	@Provides
	VersatileLoggerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(VersatileLoggerConfig.class);
	}
}
