package xyz.peppie.versatilelogger.config;

import java.util.EnumSet;
import java.util.Set;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(VersatileLoggerConfig.GROUP)
public interface VersatileLoggerConfig extends Config
{
	String GROUP = "versatile-logger";

	enum FormatMode
	{
		IN_GAME_MESSAGE,
		FULL
	}

	enum IncludeOptionBasic
	{
		TIMESTAMP,
		ICONS
	}

	/**
	 * Like {@link IncludeOptionBasic}, but for the two categories a player can type a chat
	 * command into with no channel name of its own to show (public chat, private messages).
	 */
	enum IncludeOptionBasicCommand
	{
		TIMESTAMP,
		ICONS,
		COMMAND_OUTPUT
	}

	enum IncludeOptionFriendsChat
	{
		TIMESTAMP,
		ICONS,
		FRIENDS_CHAT_NAME,
		COMMAND_OUTPUT
	}

	enum IncludeOptionClanChat
	{
		TIMESTAMP,
		ICONS,
		CLAN_CHAT_NAME
	}

	/**
	 * Like {@link IncludeOptionClanChat}, but for clan chat specifically, which (unlike guest
	 * clan chat) is a category this plugin watches typed chat commands in.
	 */
	enum IncludeOptionClanChatCommand
	{
		TIMESTAMP,
		ICONS,
		CLAN_CHAT_NAME,
		COMMAND_OUTPUT
	}

	enum IncludeOptionGroupChat
	{
		TIMESTAMP,
		ICONS,
		GROUP_NAME
	}

	// ==== General ====

	@ConfigItem(
		keyName = "retentionDays",
		name = "Log retention (days)",
		description = "How many days of local logs to keep before they're automatically deleted. Set to 0 to keep logs forever.",
		position = 0
	)
	default int retentionDays()
	{
		return 30;
	}

	@ConfigItem(
		keyName = "perCategoryLocalFiles",
		name = "Split local logs per category",
		description = "When on, each category gets its own log file per session. When off, all locally-enabled categories are interleaved into one file per session.",
		position = 1
	)
	default boolean perCategoryLocalFiles()
	{
		return false;
	}

	// ==== Remote Settings ====

	@ConfigSection(
		name = "Remote Settings",
		description = "Global settings for sending logged messages to a remote HTTP endpoint.",
		position = 10,
		closedByDefault = false
	)
	String REMOTE_SETTINGS_SECTION = "remoteSettings";

	@ConfigItem(
		keyName = "remoteLoggingEnabled",
		name = "Remote logging",
		description = "Master switch for remote logging. Must be on for any category's remote logging to take effect, regardless of that category's own remote toggle.",
		section = REMOTE_SETTINGS_SECTION,
		position = 11,
		warning = "This feature submits your IP address to a 3rd-party server not controlled or verified by RuneLite developers"
	)
	default boolean remoteLoggingEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "formatMode",
		name = "Format",
		description = "\"In-game message\" sends a plain-text line matching what you'd see in the chatbox (compatible with a Discord webhook URL pasted directly). \"Full\" sends a structured JSON object with all available message/user/clan/friends-chat details.",
		section = REMOTE_SETTINGS_SECTION,
		position = 12
	)
	default FormatMode formatMode()
	{
		return FormatMode.IN_GAME_MESSAGE;
	}

	@ConfigItem(
		keyName = "remoteUrl",
		name = "Remote URL",
		description = "The default remote URL messages are sent to, unless a category overrides it.",
		section = REMOTE_SETTINGS_SECTION,
		position = 13
	)
	default String remoteUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "authToken",
		name = "Authorization token",
		description = "If set, sent as \"Authorization: Bearer <token>\" on every remote request. Left blank, no Authorization header is sent at all.",
		section = REMOTE_SETTINGS_SECTION,
		position = 14,
		secret = true
	)
	default String authToken()
	{
		return "";
	}

	// ==== Public chat ====

	@ConfigSection(
		name = "Public chat",
		description = "Settings for public chat messages.",
		position = 20,
		closedByDefault = true
	)
	String PUBLIC_CHAT_SECTION = "publicChat";

	@ConfigItem(
		keyName = "publicChatLocalEnabled",
		name = "Local logging",
		description = "Log public chat to disk locally.",
		section = PUBLIC_CHAT_SECTION,
		position = 21
	)
	default boolean publicChatLocalEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "publicChatRemoteEnabled",
		name = "Remote logging",
		description = "Send public chat to the remote endpoint. Only takes effect if the global Remote Logging switch above is also on.",
		section = PUBLIC_CHAT_SECTION,
		position = 22
	)
	default boolean publicChatRemoteEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "publicChatOverrideUrlEnabled",
		name = "Override remote URL",
		description = "Use a different remote URL for public chat instead of the global Remote URL.",
		section = PUBLIC_CHAT_SECTION,
		position = 23
	)
	default boolean publicChatOverrideUrlEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "publicChatOverrideUrl",
		name = "Remote URL",
		description = "The remote URL to use for public chat when the override above is enabled.",
		section = PUBLIC_CHAT_SECTION,
		position = 24
	)
	default String publicChatOverrideUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "publicChatOverrideFormatEnabled",
		name = "Override format",
		description = "Use a different remote format for public chat instead of the global Format setting.",
		section = PUBLIC_CHAT_SECTION,
		position = 25
	)
	default boolean publicChatOverrideFormatEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "publicChatOverrideFormat",
		name = "Format",
		description = "The remote format to use for public chat when the override above is enabled.",
		section = PUBLIC_CHAT_SECTION,
		position = 26
	)
	default FormatMode publicChatOverrideFormat()
	{
		return FormatMode.IN_GAME_MESSAGE;
	}

	@ConfigItem(
		keyName = "publicChatInclude",
		name = "Include",
		description = "Which parts of each public chat message to include when logging/sending. Command output additionally sends/logs a follow-up once a typed chat command (e.g. !log) resolves.",
		section = PUBLIC_CHAT_SECTION,
		position = 27
	)
	default Set<IncludeOptionBasicCommand> publicChatInclude()
	{
		return EnumSet.allOf(IncludeOptionBasicCommand.class);
	}

	@ConfigItem(
		keyName = "publicChatDetailedTimestamp",
		name = "Detailed timestamp",
		description = "Adds seconds to this category's logged timestamps (HH:mm:ss instead of HH:mm). Only has an effect when Timestamp is included above.",
		section = PUBLIC_CHAT_SECTION,
		position = 28
	)
	default boolean publicChatDetailedTimestamp()
	{
		return false;
	}

	// ==== Channel chat (friends chat) ====

	@ConfigSection(
		name = "Channel chat (friends chat)",
		description = "Settings for friends chat messages.",
		position = 30,
		closedByDefault = true
	)
	String CHANNEL_CHAT_SECTION = "channelChat";

	@ConfigItem(
		keyName = "channelChatLocalEnabled",
		name = "Local logging",
		description = "Log friends chat to disk locally.",
		section = CHANNEL_CHAT_SECTION,
		position = 31
	)
	default boolean channelChatLocalEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "channelChatRemoteEnabled",
		name = "Remote logging",
		description = "Send friends chat to the remote endpoint. Only takes effect if the global Remote Logging switch above is also on.",
		section = CHANNEL_CHAT_SECTION,
		position = 32
	)
	default boolean channelChatRemoteEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "channelChatOverrideUrlEnabled",
		name = "Override remote URL",
		description = "Use a different remote URL for friends chat instead of the global Remote URL.",
		section = CHANNEL_CHAT_SECTION,
		position = 33
	)
	default boolean channelChatOverrideUrlEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "channelChatOverrideUrl",
		name = "Remote URL",
		description = "The remote URL to use for friends chat when the override above is enabled.",
		section = CHANNEL_CHAT_SECTION,
		position = 34
	)
	default String channelChatOverrideUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "channelChatOverrideFormatEnabled",
		name = "Override format",
		description = "Use a different remote format for friends chat instead of the global Format setting.",
		section = CHANNEL_CHAT_SECTION,
		position = 35
	)
	default boolean channelChatOverrideFormatEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "channelChatOverrideFormat",
		name = "Format",
		description = "The remote format to use for friends chat when the override above is enabled.",
		section = CHANNEL_CHAT_SECTION,
		position = 36
	)
	default FormatMode channelChatOverrideFormat()
	{
		return FormatMode.IN_GAME_MESSAGE;
	}

	@ConfigItem(
		keyName = "channelChatInclude",
		name = "Include",
		description = "Which parts of each friends chat message to include when logging/sending. Command output additionally sends/logs a follow-up once a typed chat command (e.g. !log) resolves.",
		section = CHANNEL_CHAT_SECTION,
		position = 37
	)
	default Set<IncludeOptionFriendsChat> channelChatInclude()
	{
		return EnumSet.allOf(IncludeOptionFriendsChat.class);
	}

	@ConfigItem(
		keyName = "channelChatDetailedTimestamp",
		name = "Detailed timestamp",
		description = "Adds seconds to this category's logged timestamps (HH:mm:ss instead of HH:mm). Only has an effect when Timestamp is included above.",
		section = CHANNEL_CHAT_SECTION,
		position = 38
	)
	default boolean channelChatDetailedTimestamp()
	{
		return false;
	}

	// ==== Clan chat ====

	@ConfigSection(
		name = "Clan chat",
		description = "Settings for clan chat messages.",
		position = 40,
		closedByDefault = true
	)
	String CLAN_CHAT_SECTION = "clanChat";

	@ConfigItem(
		keyName = "clanChatLocalEnabled",
		name = "Local logging",
		description = "Log clan chat to disk locally.",
		section = CLAN_CHAT_SECTION,
		position = 41
	)
	default boolean clanChatLocalEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "clanChatRemoteEnabled",
		name = "Remote logging",
		description = "Send clan chat to the remote endpoint. Only takes effect if the global Remote Logging switch above is also on.",
		section = CLAN_CHAT_SECTION,
		position = 42
	)
	default boolean clanChatRemoteEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "clanChatOverrideUrlEnabled",
		name = "Override remote URL",
		description = "Use a different remote URL for clan chat instead of the global Remote URL.",
		section = CLAN_CHAT_SECTION,
		position = 43
	)
	default boolean clanChatOverrideUrlEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "clanChatOverrideUrl",
		name = "Remote URL",
		description = "The remote URL to use for clan chat when the override above is enabled.",
		section = CLAN_CHAT_SECTION,
		position = 44
	)
	default String clanChatOverrideUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "clanChatOverrideFormatEnabled",
		name = "Override format",
		description = "Use a different remote format for clan chat instead of the global Format setting.",
		section = CLAN_CHAT_SECTION,
		position = 45
	)
	default boolean clanChatOverrideFormatEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "clanChatOverrideFormat",
		name = "Format",
		description = "The remote format to use for clan chat when the override above is enabled.",
		section = CLAN_CHAT_SECTION,
		position = 46
	)
	default FormatMode clanChatOverrideFormat()
	{
		return FormatMode.IN_GAME_MESSAGE;
	}

	@ConfigItem(
		keyName = "clanChatInclude",
		name = "Include",
		description = "Which parts of each clan chat message to include when logging/sending. Command output additionally sends/logs a follow-up once a typed chat command (e.g. !log) resolves.",
		section = CLAN_CHAT_SECTION,
		position = 47
	)
	default Set<IncludeOptionClanChatCommand> clanChatInclude()
	{
		return EnumSet.allOf(IncludeOptionClanChatCommand.class);
	}

	@ConfigItem(
		keyName = "clanChatDetailedTimestamp",
		name = "Detailed timestamp",
		description = "Adds seconds to this category's logged timestamps (HH:mm:ss instead of HH:mm). Only has an effect when Timestamp is included above.",
		section = CLAN_CHAT_SECTION,
		position = 48
	)
	default boolean clanChatDetailedTimestamp()
	{
		return false;
	}

	// ==== Game messages ====

	@ConfigSection(
		name = "Game messages",
		description = "Settings for generic game/system messages.",
		position = 50,
		closedByDefault = true
	)
	String GAME_MESSAGES_SECTION = "gameMessages";

	@ConfigItem(
		keyName = "gameMessagesLocalEnabled",
		name = "Local logging",
		description = "Log game messages to disk locally.",
		section = GAME_MESSAGES_SECTION,
		position = 51
	)
	default boolean gameMessagesLocalEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "gameMessagesRemoteEnabled",
		name = "Remote logging",
		description = "Send game messages to the remote endpoint. Only takes effect if the global Remote Logging switch above is also on.",
		section = GAME_MESSAGES_SECTION,
		position = 52
	)
	default boolean gameMessagesRemoteEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "gameMessagesOverrideUrlEnabled",
		name = "Override remote URL",
		description = "Use a different remote URL for game messages instead of the global Remote URL.",
		section = GAME_MESSAGES_SECTION,
		position = 53
	)
	default boolean gameMessagesOverrideUrlEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "gameMessagesOverrideUrl",
		name = "Remote URL",
		description = "The remote URL to use for game messages when the override above is enabled.",
		section = GAME_MESSAGES_SECTION,
		position = 54
	)
	default String gameMessagesOverrideUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "gameMessagesOverrideFormatEnabled",
		name = "Override format",
		description = "Use a different remote format for game messages instead of the global Format setting.",
		section = GAME_MESSAGES_SECTION,
		position = 55
	)
	default boolean gameMessagesOverrideFormatEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "gameMessagesOverrideFormat",
		name = "Format",
		description = "The remote format to use for game messages when the override above is enabled.",
		section = GAME_MESSAGES_SECTION,
		position = 56
	)
	default FormatMode gameMessagesOverrideFormat()
	{
		return FormatMode.IN_GAME_MESSAGE;
	}

	@ConfigItem(
		keyName = "gameMessagesInclude",
		name = "Include",
		description = "Which parts of each game message to include when logging/sending.",
		section = GAME_MESSAGES_SECTION,
		position = 57
	)
	default Set<IncludeOptionBasic> gameMessagesInclude()
	{
		return EnumSet.allOf(IncludeOptionBasic.class);
	}

	@ConfigItem(
		keyName = "gameMessagesDetailedTimestamp",
		name = "Detailed timestamp",
		description = "Adds seconds to this category's logged timestamps (HH:mm:ss instead of HH:mm). Only has an effect when Timestamp is included above.",
		section = GAME_MESSAGES_SECTION,
		position = 58
	)
	default boolean gameMessagesDetailedTimestamp()
	{
		return false;
	}

	// ==== Private messages ====

	@ConfigSection(
		name = "Private messages",
		description = "Settings for private messages, both sent and received.",
		position = 60,
		closedByDefault = true
	)
	String PRIVATE_MESSAGES_SECTION = "privateMessages";

	@ConfigItem(
		keyName = "privateMessagesLocalEnabled",
		name = "Local logging",
		description = "Log private messages to disk locally.",
		section = PRIVATE_MESSAGES_SECTION,
		position = 61
	)
	default boolean privateMessagesLocalEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "privateMessagesRemoteEnabled",
		name = "Remote logging",
		description = "Send private messages to the remote endpoint. Only takes effect if the global Remote Logging switch above is also on.",
		section = PRIVATE_MESSAGES_SECTION,
		position = 62
	)
	default boolean privateMessagesRemoteEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "privateMessagesOverrideUrlEnabled",
		name = "Override remote URL",
		description = "Use a different remote URL for private messages instead of the global Remote URL.",
		section = PRIVATE_MESSAGES_SECTION,
		position = 63
	)
	default boolean privateMessagesOverrideUrlEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "privateMessagesOverrideUrl",
		name = "Remote URL",
		description = "The remote URL to use for private messages when the override above is enabled.",
		section = PRIVATE_MESSAGES_SECTION,
		position = 64
	)
	default String privateMessagesOverrideUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "privateMessagesOverrideFormatEnabled",
		name = "Override format",
		description = "Use a different remote format for private messages instead of the global Format setting.",
		section = PRIVATE_MESSAGES_SECTION,
		position = 65
	)
	default boolean privateMessagesOverrideFormatEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "privateMessagesOverrideFormat",
		name = "Format",
		description = "The remote format to use for private messages when the override above is enabled.",
		section = PRIVATE_MESSAGES_SECTION,
		position = 66
	)
	default FormatMode privateMessagesOverrideFormat()
	{
		return FormatMode.IN_GAME_MESSAGE;
	}

	@ConfigItem(
		keyName = "privateMessagesInclude",
		name = "Include",
		description = "Which parts of each private message to include when logging/sending. Command output additionally sends/logs a follow-up once a typed chat command (e.g. !log) resolves.",
		section = PRIVATE_MESSAGES_SECTION,
		position = 67
	)
	default Set<IncludeOptionBasicCommand> privateMessagesInclude()
	{
		return EnumSet.allOf(IncludeOptionBasicCommand.class);
	}

	@ConfigItem(
		keyName = "privateMessagesDetailedTimestamp",
		name = "Detailed timestamp",
		description = "Adds seconds to this category's logged timestamps (HH:mm:ss instead of HH:mm). Only has an effect when Timestamp is included above.",
		section = PRIVATE_MESSAGES_SECTION,
		position = 68
	)
	default boolean privateMessagesDetailedTimestamp()
	{
		return false;
	}

	// ==== Guest clan chat ====

	@ConfigSection(
		name = "Guest clan chat",
		description = "Settings for guest clan chat messages.",
		position = 70,
		closedByDefault = true
	)
	String GUEST_CLAN_CHAT_SECTION = "guestClanChat";

	@ConfigItem(
		keyName = "guestClanChatLocalEnabled",
		name = "Local logging",
		description = "Log guest clan chat to disk locally.",
		section = GUEST_CLAN_CHAT_SECTION,
		position = 71
	)
	default boolean guestClanChatLocalEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "guestClanChatRemoteEnabled",
		name = "Remote logging",
		description = "Send guest clan chat to the remote endpoint. Only takes effect if the global Remote Logging switch above is also on.",
		section = GUEST_CLAN_CHAT_SECTION,
		position = 72
	)
	default boolean guestClanChatRemoteEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "guestClanChatOverrideUrlEnabled",
		name = "Override remote URL",
		description = "Use a different remote URL for guest clan chat instead of the global Remote URL.",
		section = GUEST_CLAN_CHAT_SECTION,
		position = 73
	)
	default boolean guestClanChatOverrideUrlEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "guestClanChatOverrideUrl",
		name = "Remote URL",
		description = "The remote URL to use for guest clan chat when the override above is enabled.",
		section = GUEST_CLAN_CHAT_SECTION,
		position = 74
	)
	default String guestClanChatOverrideUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "guestClanChatOverrideFormatEnabled",
		name = "Override format",
		description = "Use a different remote format for guest clan chat instead of the global Format setting.",
		section = GUEST_CLAN_CHAT_SECTION,
		position = 75
	)
	default boolean guestClanChatOverrideFormatEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "guestClanChatOverrideFormat",
		name = "Format",
		description = "The remote format to use for guest clan chat when the override above is enabled.",
		section = GUEST_CLAN_CHAT_SECTION,
		position = 76
	)
	default FormatMode guestClanChatOverrideFormat()
	{
		return FormatMode.IN_GAME_MESSAGE;
	}

	@ConfigItem(
		keyName = "guestClanChatInclude",
		name = "Include",
		description = "Which parts of each guest clan chat message to include when logging/sending.",
		section = GUEST_CLAN_CHAT_SECTION,
		position = 77
	)
	default Set<IncludeOptionClanChat> guestClanChatInclude()
	{
		return EnumSet.allOf(IncludeOptionClanChat.class);
	}

	@ConfigItem(
		keyName = "guestClanChatDetailedTimestamp",
		name = "Detailed timestamp",
		description = "Adds seconds to this category's logged timestamps (HH:mm:ss instead of HH:mm). Only has an effect when Timestamp is included above.",
		section = GUEST_CLAN_CHAT_SECTION,
		position = 78
	)
	default boolean guestClanChatDetailedTimestamp()
	{
		return false;
	}

	// ==== Group chat (Group Ironman) ====

	@ConfigSection(
		name = "Group chat (Group Ironman)",
		description = "Settings for Group Ironman group chat messages.",
		position = 80,
		closedByDefault = true
	)
	String GROUP_CHAT_SECTION = "groupChat";

	@ConfigItem(
		keyName = "groupChatLocalEnabled",
		name = "Local logging",
		description = "Log group ironman chat to disk locally.",
		section = GROUP_CHAT_SECTION,
		position = 81
	)
	default boolean groupChatLocalEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "groupChatRemoteEnabled",
		name = "Remote logging",
		description = "Send group ironman chat to the remote endpoint. Only takes effect if the global Remote Logging switch above is also on.",
		section = GROUP_CHAT_SECTION,
		position = 82
	)
	default boolean groupChatRemoteEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "groupChatOverrideUrlEnabled",
		name = "Override remote URL",
		description = "Use a different remote URL for group ironman chat instead of the global Remote URL.",
		section = GROUP_CHAT_SECTION,
		position = 83
	)
	default boolean groupChatOverrideUrlEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "groupChatOverrideUrl",
		name = "Remote URL",
		description = "The remote URL to use for group ironman chat when the override above is enabled.",
		section = GROUP_CHAT_SECTION,
		position = 84
	)
	default String groupChatOverrideUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "groupChatOverrideFormatEnabled",
		name = "Override format",
		description = "Use a different remote format for group ironman chat instead of the global Format setting.",
		section = GROUP_CHAT_SECTION,
		position = 85
	)
	default boolean groupChatOverrideFormatEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "groupChatOverrideFormat",
		name = "Format",
		description = "The remote format to use for group ironman chat when the override above is enabled.",
		section = GROUP_CHAT_SECTION,
		position = 86
	)
	default FormatMode groupChatOverrideFormat()
	{
		return FormatMode.IN_GAME_MESSAGE;
	}

	@ConfigItem(
		keyName = "groupChatInclude",
		name = "Include",
		description = "Which parts of each group ironman chat message to include when logging/sending.",
		section = GROUP_CHAT_SECTION,
		position = 87
	)
	default Set<IncludeOptionGroupChat> groupChatInclude()
	{
		return EnumSet.allOf(IncludeOptionGroupChat.class);
	}

	@ConfigItem(
		keyName = "groupChatDetailedTimestamp",
		name = "Detailed timestamp",
		description = "Adds seconds to this category's logged timestamps (HH:mm:ss instead of HH:mm). Only has an effect when Timestamp is included above.",
		section = GROUP_CHAT_SECTION,
		position = 88
	)
	default boolean groupChatDetailedTimestamp()
	{
		return false;
	}

	// ==== Collection log unlock ====

	@ConfigSection(
		name = "Collection log unlock",
		description = "Settings for collection log unlock messages.",
		position = 90,
		closedByDefault = true
	)
	String COLLECTION_LOG_SECTION = "collectionLog";

	@ConfigItem(
		keyName = "collectionLogLocalEnabled",
		name = "Local logging",
		description = "Log collection log unlocks to disk locally.",
		section = COLLECTION_LOG_SECTION,
		position = 91
	)
	default boolean collectionLogLocalEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "collectionLogRemoteEnabled",
		name = "Remote logging",
		description = "Send collection log unlocks to the remote endpoint. Only takes effect if the global Remote Logging switch above is also on.",
		section = COLLECTION_LOG_SECTION,
		position = 92
	)
	default boolean collectionLogRemoteEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "collectionLogOverrideUrlEnabled",
		name = "Override remote URL",
		description = "Use a different remote URL for collection log unlocks instead of the global Remote URL.",
		section = COLLECTION_LOG_SECTION,
		position = 93
	)
	default boolean collectionLogOverrideUrlEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "collectionLogOverrideUrl",
		name = "Remote URL",
		description = "The remote URL to use for collection log unlocks when the override above is enabled.",
		section = COLLECTION_LOG_SECTION,
		position = 94
	)
	default String collectionLogOverrideUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "collectionLogOverrideFormatEnabled",
		name = "Override format",
		description = "Use a different remote format for collection log unlocks instead of the global Format setting.",
		section = COLLECTION_LOG_SECTION,
		position = 95
	)
	default boolean collectionLogOverrideFormatEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "collectionLogOverrideFormat",
		name = "Format",
		description = "The remote format to use for collection log unlocks when the override above is enabled.",
		section = COLLECTION_LOG_SECTION,
		position = 96
	)
	default FormatMode collectionLogOverrideFormat()
	{
		return FormatMode.IN_GAME_MESSAGE;
	}

	@ConfigItem(
		keyName = "collectionLogInclude",
		name = "Include",
		description = "Which parts of each collection log unlock message to include when logging/sending.",
		section = COLLECTION_LOG_SECTION,
		position = 97
	)
	default Set<IncludeOptionBasic> collectionLogInclude()
	{
		return EnumSet.allOf(IncludeOptionBasic.class);
	}

	@ConfigItem(
		keyName = "collectionLogDetailedTimestamp",
		name = "Detailed timestamp",
		description = "Adds seconds to this category's logged timestamps (HH:mm:ss instead of HH:mm). Only has an effect when Timestamp is included above.",
		section = COLLECTION_LOG_SECTION,
		position = 98
	)
	default boolean collectionLogDetailedTimestamp()
	{
		return false;
	}

	// ==== High value drop ====

	@ConfigSection(
		name = "High value drop",
		description = "Settings for valuable drop messages.",
		position = 100,
		closedByDefault = true
	)
	String HIGH_VALUE_DROP_SECTION = "highValueDrop";

	@ConfigItem(
		keyName = "highValueDropLocalEnabled",
		name = "Local logging",
		description = "Log high value drops to disk locally.",
		section = HIGH_VALUE_DROP_SECTION,
		position = 101
	)
	default boolean highValueDropLocalEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highValueDropRemoteEnabled",
		name = "Remote logging",
		description = "Send high value drops to the remote endpoint. Only takes effect if the global Remote Logging switch above is also on.",
		section = HIGH_VALUE_DROP_SECTION,
		position = 102
	)
	default boolean highValueDropRemoteEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highValueDropOverrideUrlEnabled",
		name = "Override remote URL",
		description = "Use a different remote URL for high value drops instead of the global Remote URL.",
		section = HIGH_VALUE_DROP_SECTION,
		position = 103
	)
	default boolean highValueDropOverrideUrlEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highValueDropOverrideUrl",
		name = "Remote URL",
		description = "The remote URL to use for high value drops when the override above is enabled.",
		section = HIGH_VALUE_DROP_SECTION,
		position = 104
	)
	default String highValueDropOverrideUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "highValueDropOverrideFormatEnabled",
		name = "Override format",
		description = "Use a different remote format for high value drops instead of the global Format setting.",
		section = HIGH_VALUE_DROP_SECTION,
		position = 105
	)
	default boolean highValueDropOverrideFormatEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highValueDropOverrideFormat",
		name = "Format",
		description = "The remote format to use for high value drops when the override above is enabled.",
		section = HIGH_VALUE_DROP_SECTION,
		position = 106
	)
	default FormatMode highValueDropOverrideFormat()
	{
		return FormatMode.IN_GAME_MESSAGE;
	}

	@ConfigItem(
		keyName = "highValueDropInclude",
		name = "Include",
		description = "Which parts of each high value drop message to include when logging/sending.",
		section = HIGH_VALUE_DROP_SECTION,
		position = 107
	)
	default Set<IncludeOptionBasic> highValueDropInclude()
	{
		return EnumSet.allOf(IncludeOptionBasic.class);
	}

	@ConfigItem(
		keyName = "highValueDropDetailedTimestamp",
		name = "Detailed timestamp",
		description = "Adds seconds to this category's logged timestamps (HH:mm:ss instead of HH:mm). Only has an effect when Timestamp is included above.",
		section = HIGH_VALUE_DROP_SECTION,
		position = 108
	)
	default boolean highValueDropDetailedTimestamp()
	{
		return false;
	}

	// ==== Pet drops ====

	@ConfigSection(
		name = "Pet drops",
		description = "Settings for pet drop messages.",
		position = 110,
		closedByDefault = true
	)
	String PET_DROP_SECTION = "petDrop";

	@ConfigItem(
		keyName = "petDropLocalEnabled",
		name = "Local logging",
		description = "Log pet drops to disk locally.",
		section = PET_DROP_SECTION,
		position = 111
	)
	default boolean petDropLocalEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "petDropRemoteEnabled",
		name = "Remote logging",
		description = "Send pet drops to the remote endpoint. Only takes effect if the global Remote Logging switch above is also on.",
		section = PET_DROP_SECTION,
		position = 112
	)
	default boolean petDropRemoteEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "petDropOverrideUrlEnabled",
		name = "Override remote URL",
		description = "Use a different remote URL for pet drops instead of the global Remote URL.",
		section = PET_DROP_SECTION,
		position = 113
	)
	default boolean petDropOverrideUrlEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "petDropOverrideUrl",
		name = "Remote URL",
		description = "The remote URL to use for pet drops when the override above is enabled.",
		section = PET_DROP_SECTION,
		position = 114
	)
	default String petDropOverrideUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "petDropOverrideFormatEnabled",
		name = "Override format",
		description = "Use a different remote format for pet drops instead of the global Format setting.",
		section = PET_DROP_SECTION,
		position = 115
	)
	default boolean petDropOverrideFormatEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "petDropOverrideFormat",
		name = "Format",
		description = "The remote format to use for pet drops when the override above is enabled.",
		section = PET_DROP_SECTION,
		position = 116
	)
	default FormatMode petDropOverrideFormat()
	{
		return FormatMode.IN_GAME_MESSAGE;
	}

	@ConfigItem(
		keyName = "petDropInclude",
		name = "Include",
		description = "Which parts of each pet drop message to include when logging/sending.",
		section = PET_DROP_SECTION,
		position = 117
	)
	default Set<IncludeOptionBasic> petDropInclude()
	{
		return EnumSet.allOf(IncludeOptionBasic.class);
	}

	@ConfigItem(
		keyName = "petDropDetailedTimestamp",
		name = "Detailed timestamp",
		description = "Adds seconds to this category's logged timestamps (HH:mm:ss instead of HH:mm). Only has an effect when Timestamp is included above.",
		section = PET_DROP_SECTION,
		position = 118
	)
	default boolean petDropDetailedTimestamp()
	{
		return false;
	}

	// ==== Level up ====

	@ConfigSection(
		name = "Level up",
		description = "Settings for level-up messages.",
		position = 120,
		closedByDefault = true
	)
	String LEVEL_UP_SECTION = "levelUp";

	@ConfigItem(
		keyName = "levelUpLocalEnabled",
		name = "Local logging",
		description = "Log level-ups to disk locally.",
		section = LEVEL_UP_SECTION,
		position = 121
	)
	default boolean levelUpLocalEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "levelUpRemoteEnabled",
		name = "Remote logging",
		description = "Send level-ups to the remote endpoint. Only takes effect if the global Remote Logging switch above is also on.",
		section = LEVEL_UP_SECTION,
		position = 122
	)
	default boolean levelUpRemoteEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "levelUpOverrideUrlEnabled",
		name = "Override remote URL",
		description = "Use a different remote URL for level-ups instead of the global Remote URL.",
		section = LEVEL_UP_SECTION,
		position = 123
	)
	default boolean levelUpOverrideUrlEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "levelUpOverrideUrl",
		name = "Remote URL",
		description = "The remote URL to use for level-ups when the override above is enabled.",
		section = LEVEL_UP_SECTION,
		position = 124
	)
	default String levelUpOverrideUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "levelUpOverrideFormatEnabled",
		name = "Override format",
		description = "Use a different remote format for level-ups instead of the global Format setting.",
		section = LEVEL_UP_SECTION,
		position = 125
	)
	default boolean levelUpOverrideFormatEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "levelUpOverrideFormat",
		name = "Format",
		description = "The remote format to use for level-ups when the override above is enabled.",
		section = LEVEL_UP_SECTION,
		position = 126
	)
	default FormatMode levelUpOverrideFormat()
	{
		return FormatMode.IN_GAME_MESSAGE;
	}

	@ConfigItem(
		keyName = "levelUpInclude",
		name = "Include",
		description = "Which parts of each level-up message to include when logging/sending.",
		section = LEVEL_UP_SECTION,
		position = 127
	)
	default Set<IncludeOptionBasic> levelUpInclude()
	{
		return EnumSet.allOf(IncludeOptionBasic.class);
	}

	@ConfigItem(
		keyName = "levelUpDetailedTimestamp",
		name = "Detailed timestamp",
		description = "Adds seconds to this category's logged timestamps (HH:mm:ss instead of HH:mm). Only has an effect when Timestamp is included above.",
		section = LEVEL_UP_SECTION,
		position = 128
	)
	default boolean levelUpDetailedTimestamp()
	{
		return false;
	}
}
