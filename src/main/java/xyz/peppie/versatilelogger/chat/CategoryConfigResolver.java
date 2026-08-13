package xyz.peppie.versatilelogger.chat;

import java.util.EnumSet;
import java.util.Set;

import xyz.peppie.versatilelogger.config.VersatileLoggerConfig;
import xyz.peppie.versatilelogger.config.VersatileLoggerConfig.FormatMode;
import xyz.peppie.versatilelogger.config.VersatileLoggerConfig.IncludeOptionBasic;
import xyz.peppie.versatilelogger.config.VersatileLoggerConfig.IncludeOptionBasicCommand;
import xyz.peppie.versatilelogger.config.VersatileLoggerConfig.IncludeOptionClanChat;
import xyz.peppie.versatilelogger.config.VersatileLoggerConfig.IncludeOptionClanChatCommand;
import xyz.peppie.versatilelogger.config.VersatileLoggerConfig.IncludeOptionFriendsChat;
import xyz.peppie.versatilelogger.config.VersatileLoggerConfig.IncludeOptionGroupChat;
import xyz.peppie.versatilelogger.format.LineIncludeOption;

/**
 * Maps a {@link ChatCategory} to its category-specific config accessors and normalizes whichever
 * of the config-facing include enums applies into the category-agnostic {@link LineIncludeOption}
 * set the formatter (and the plugin's command-output tracking) consumes. Deliberately a plain
 * switch (no reflection) since AGENTS.md forbids reflection.
 */
public class CategoryConfigResolver
{
	public CategorySettings resolve(VersatileLoggerConfig config, ChatCategory category)
	{
		boolean globalRemote = config.remoteLoggingEnabled();
		String globalUrl = config.remoteUrl();
		FormatMode globalFormat = config.formatMode();

		switch (category)
		{
			case PUBLIC_CHAT:
				return build(config.publicChatLocalEnabled(), globalRemote && config.publicChatRemoteEnabled(),
					config.publicChatOverrideUrlEnabled(), config.publicChatOverrideUrl(), globalUrl,
					config.publicChatOverrideFormatEnabled(), config.publicChatOverrideFormat(), globalFormat,
					config.publicChatDetailedTimestamp(), toLineOptionsBasicCommand(config.publicChatInclude()));
			case CHANNEL_CHAT:
				return build(config.channelChatLocalEnabled(), globalRemote && config.channelChatRemoteEnabled(),
					config.channelChatOverrideUrlEnabled(), config.channelChatOverrideUrl(), globalUrl,
					config.channelChatOverrideFormatEnabled(), config.channelChatOverrideFormat(), globalFormat,
					config.channelChatDetailedTimestamp(), toLineOptionsFriendsChat(config.channelChatInclude()));
			case CLAN_CHAT:
				return build(config.clanChatLocalEnabled(), globalRemote && config.clanChatRemoteEnabled(),
					config.clanChatOverrideUrlEnabled(), config.clanChatOverrideUrl(), globalUrl,
					config.clanChatOverrideFormatEnabled(), config.clanChatOverrideFormat(), globalFormat,
					config.clanChatDetailedTimestamp(), toLineOptionsClanChatCommand(config.clanChatInclude()));
			case GAME_MESSAGES:
				return build(config.gameMessagesLocalEnabled(), globalRemote && config.gameMessagesRemoteEnabled(),
					config.gameMessagesOverrideUrlEnabled(), config.gameMessagesOverrideUrl(), globalUrl,
					config.gameMessagesOverrideFormatEnabled(), config.gameMessagesOverrideFormat(), globalFormat,
					config.gameMessagesDetailedTimestamp(), toLineOptions(config.gameMessagesInclude()));
			case PRIVATE_MESSAGES:
				return build(config.privateMessagesLocalEnabled(), globalRemote && config.privateMessagesRemoteEnabled(),
					config.privateMessagesOverrideUrlEnabled(), config.privateMessagesOverrideUrl(), globalUrl,
					config.privateMessagesOverrideFormatEnabled(), config.privateMessagesOverrideFormat(), globalFormat,
					config.privateMessagesDetailedTimestamp(), toLineOptionsBasicCommand(config.privateMessagesInclude()));
			case GUEST_CLAN_CHAT:
				return build(config.guestClanChatLocalEnabled(), globalRemote && config.guestClanChatRemoteEnabled(),
					config.guestClanChatOverrideUrlEnabled(), config.guestClanChatOverrideUrl(), globalUrl,
					config.guestClanChatOverrideFormatEnabled(), config.guestClanChatOverrideFormat(), globalFormat,
					config.guestClanChatDetailedTimestamp(), toLineOptionsClanChat(config.guestClanChatInclude()));
			case GROUP_CHAT:
				return build(config.groupChatLocalEnabled(), globalRemote && config.groupChatRemoteEnabled(),
					config.groupChatOverrideUrlEnabled(), config.groupChatOverrideUrl(), globalUrl,
					config.groupChatOverrideFormatEnabled(), config.groupChatOverrideFormat(), globalFormat,
					config.groupChatDetailedTimestamp(), toLineOptionsGroupChat(config.groupChatInclude()));
			case COLLECTION_LOG:
				return build(config.collectionLogLocalEnabled(), globalRemote && config.collectionLogRemoteEnabled(),
					config.collectionLogOverrideUrlEnabled(), config.collectionLogOverrideUrl(), globalUrl,
					config.collectionLogOverrideFormatEnabled(), config.collectionLogOverrideFormat(), globalFormat,
					config.collectionLogDetailedTimestamp(), toLineOptions(config.collectionLogInclude()));
			case HIGH_VALUE_DROP:
				return build(config.highValueDropLocalEnabled(), globalRemote && config.highValueDropRemoteEnabled(),
					config.highValueDropOverrideUrlEnabled(), config.highValueDropOverrideUrl(), globalUrl,
					config.highValueDropOverrideFormatEnabled(), config.highValueDropOverrideFormat(), globalFormat,
					config.highValueDropDetailedTimestamp(), toLineOptions(config.highValueDropInclude()));
			case PET_DROP:
				return build(config.petDropLocalEnabled(), globalRemote && config.petDropRemoteEnabled(),
					config.petDropOverrideUrlEnabled(), config.petDropOverrideUrl(), globalUrl,
					config.petDropOverrideFormatEnabled(), config.petDropOverrideFormat(), globalFormat,
					config.petDropDetailedTimestamp(), toLineOptions(config.petDropInclude()));
			case LEVEL_UP:
				return build(config.levelUpLocalEnabled(), globalRemote && config.levelUpRemoteEnabled(),
					config.levelUpOverrideUrlEnabled(), config.levelUpOverrideUrl(), globalUrl,
					config.levelUpOverrideFormatEnabled(), config.levelUpOverrideFormat(), globalFormat,
					config.levelUpDetailedTimestamp(), toLineOptions(config.levelUpInclude()));
			default:
				throw new IllegalArgumentException("Unhandled category: " + category);
		}
	}

	private static CategorySettings build(boolean localEnabled, boolean remoteEnabled,
		boolean overrideUrlEnabled, String overrideUrl, String globalUrl,
		boolean overrideFormatEnabled, FormatMode overrideFormat, FormatMode globalFormat,
		boolean detailedTimestamp, Set<LineIncludeOption> include)
	{
		String effectiveUrl = overrideUrlEnabled && overrideUrl != null && !overrideUrl.isBlank()
			? overrideUrl
			: globalUrl;
		FormatMode effectiveFormat = overrideFormatEnabled ? overrideFormat : globalFormat;
		return new CategorySettings(localEnabled, remoteEnabled, effectiveUrl, effectiveFormat, detailedTimestamp, include);
	}

	private static Set<LineIncludeOption> toLineOptions(Set<IncludeOptionBasic> include)
	{
		Set<LineIncludeOption> result = EnumSet.noneOf(LineIncludeOption.class);
		if (include.contains(IncludeOptionBasic.TIMESTAMP))
		{
			result.add(LineIncludeOption.TIMESTAMP);
		}
		if (include.contains(IncludeOptionBasic.ICONS))
		{
			result.add(LineIncludeOption.ICONS);
		}
		return result;
	}

	/**
	 * For public chat and private messages: like {@link #toLineOptions}, plus the
	 * {@code COMMAND_OUTPUT} option (no channel name to map, unlike friends/clan chat).
	 */
	private static Set<LineIncludeOption> toLineOptionsBasicCommand(Set<IncludeOptionBasicCommand> include)
	{
		Set<LineIncludeOption> result = EnumSet.noneOf(LineIncludeOption.class);
		if (include.contains(IncludeOptionBasicCommand.TIMESTAMP))
		{
			result.add(LineIncludeOption.TIMESTAMP);
		}
		if (include.contains(IncludeOptionBasicCommand.ICONS))
		{
			result.add(LineIncludeOption.ICONS);
		}
		if (include.contains(IncludeOptionBasicCommand.COMMAND_OUTPUT))
		{
			result.add(LineIncludeOption.COMMAND_OUTPUT);
		}
		return result;
	}

	private static Set<LineIncludeOption> toLineOptionsFriendsChat(Set<IncludeOptionFriendsChat> include)
	{
		Set<LineIncludeOption> result = EnumSet.noneOf(LineIncludeOption.class);
		if (include.contains(IncludeOptionFriendsChat.TIMESTAMP))
		{
			result.add(LineIncludeOption.TIMESTAMP);
		}
		if (include.contains(IncludeOptionFriendsChat.ICONS))
		{
			result.add(LineIncludeOption.ICONS);
		}
		if (include.contains(IncludeOptionFriendsChat.FRIENDS_CHAT_NAME))
		{
			result.add(LineIncludeOption.NAME);
		}
		if (include.contains(IncludeOptionFriendsChat.COMMAND_OUTPUT))
		{
			result.add(LineIncludeOption.COMMAND_OUTPUT);
		}
		return result;
	}

	private static Set<LineIncludeOption> toLineOptionsClanChat(Set<IncludeOptionClanChat> include)
	{
		Set<LineIncludeOption> result = EnumSet.noneOf(LineIncludeOption.class);
		if (include.contains(IncludeOptionClanChat.TIMESTAMP))
		{
			result.add(LineIncludeOption.TIMESTAMP);
		}
		if (include.contains(IncludeOptionClanChat.ICONS))
		{
			result.add(LineIncludeOption.ICONS);
		}
		if (include.contains(IncludeOptionClanChat.CLAN_CHAT_NAME))
		{
			result.add(LineIncludeOption.NAME);
		}
		return result;
	}

	/**
	 * For clan chat specifically: like {@link #toLineOptionsClanChat}, plus the
	 * {@code COMMAND_OUTPUT} option (unlike guest clan chat, not watched for typed commands).
	 */
	private static Set<LineIncludeOption> toLineOptionsClanChatCommand(Set<IncludeOptionClanChatCommand> include)
	{
		Set<LineIncludeOption> result = EnumSet.noneOf(LineIncludeOption.class);
		if (include.contains(IncludeOptionClanChatCommand.TIMESTAMP))
		{
			result.add(LineIncludeOption.TIMESTAMP);
		}
		if (include.contains(IncludeOptionClanChatCommand.ICONS))
		{
			result.add(LineIncludeOption.ICONS);
		}
		if (include.contains(IncludeOptionClanChatCommand.CLAN_CHAT_NAME))
		{
			result.add(LineIncludeOption.NAME);
		}
		if (include.contains(IncludeOptionClanChatCommand.COMMAND_OUTPUT))
		{
			result.add(LineIncludeOption.COMMAND_OUTPUT);
		}
		return result;
	}

	private static Set<LineIncludeOption> toLineOptionsGroupChat(Set<IncludeOptionGroupChat> include)
	{
		Set<LineIncludeOption> result = EnumSet.noneOf(LineIncludeOption.class);
		if (include.contains(IncludeOptionGroupChat.TIMESTAMP))
		{
			result.add(LineIncludeOption.TIMESTAMP);
		}
		if (include.contains(IncludeOptionGroupChat.ICONS))
		{
			result.add(LineIncludeOption.ICONS);
		}
		if (include.contains(IncludeOptionGroupChat.GROUP_NAME))
		{
			result.add(LineIncludeOption.NAME);
		}
		return result;
	}
}
