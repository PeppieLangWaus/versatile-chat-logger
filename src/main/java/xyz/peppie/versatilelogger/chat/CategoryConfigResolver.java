package xyz.peppie.versatilelogger.chat;

import java.util.EnumSet;
import java.util.Set;

import xyz.peppie.versatilelogger.config.VersatileLoggerConfig;
import xyz.peppie.versatilelogger.config.VersatileLoggerConfig.IncludeOptionBasic;
import xyz.peppie.versatilelogger.config.VersatileLoggerConfig.IncludeOptionClanChat;
import xyz.peppie.versatilelogger.config.VersatileLoggerConfig.IncludeOptionFriendsChat;
import xyz.peppie.versatilelogger.config.VersatileLoggerConfig.IncludeOptionGroupChat;
import xyz.peppie.versatilelogger.format.LineIncludeOption;

/**
 * Maps a {@link ChatCategory} to its 5 category-specific config accessors and normalizes
 * whichever of the config-facing include enums applies into the category-agnostic
 * {@link LineIncludeOption} set the formatter consumes. Deliberately a plain switch (no
 * reflection) since AGENTS.md forbids reflection.
 */
public class CategoryConfigResolver
{
	public CategorySettings resolve(VersatileLoggerConfig config, ChatCategory category)
	{
		boolean globalRemote = config.remoteLoggingEnabled();
		String globalUrl = config.remoteUrl();

		switch (category)
		{
			case PUBLIC_CHAT:
				return build(config.publicChatLocalEnabled(), globalRemote && config.publicChatRemoteEnabled(),
					config.publicChatOverrideUrlEnabled(), config.publicChatOverrideUrl(), globalUrl,
					toLineOptions(config.publicChatInclude()));
			case CHANNEL_CHAT:
				return build(config.channelChatLocalEnabled(), globalRemote && config.channelChatRemoteEnabled(),
					config.channelChatOverrideUrlEnabled(), config.channelChatOverrideUrl(), globalUrl,
					toLineOptionsFriendsChat(config.channelChatInclude()));
			case CLAN_CHAT:
				return build(config.clanChatLocalEnabled(), globalRemote && config.clanChatRemoteEnabled(),
					config.clanChatOverrideUrlEnabled(), config.clanChatOverrideUrl(), globalUrl,
					toLineOptionsClanChat(config.clanChatInclude()));
			case GAME_MESSAGES:
				return build(config.gameMessagesLocalEnabled(), globalRemote && config.gameMessagesRemoteEnabled(),
					config.gameMessagesOverrideUrlEnabled(), config.gameMessagesOverrideUrl(), globalUrl,
					toLineOptions(config.gameMessagesInclude()));
			case PRIVATE_MESSAGES:
				return build(config.privateMessagesLocalEnabled(), globalRemote && config.privateMessagesRemoteEnabled(),
					config.privateMessagesOverrideUrlEnabled(), config.privateMessagesOverrideUrl(), globalUrl,
					toLineOptions(config.privateMessagesInclude()));
			case GUEST_CLAN_CHAT:
				return build(config.guestClanChatLocalEnabled(), globalRemote && config.guestClanChatRemoteEnabled(),
					config.guestClanChatOverrideUrlEnabled(), config.guestClanChatOverrideUrl(), globalUrl,
					toLineOptionsClanChat(config.guestClanChatInclude()));
			case GROUP_CHAT:
				return build(config.groupChatLocalEnabled(), globalRemote && config.groupChatRemoteEnabled(),
					config.groupChatOverrideUrlEnabled(), config.groupChatOverrideUrl(), globalUrl,
					toLineOptionsGroupChat(config.groupChatInclude()));
			case COLLECTION_LOG:
				return build(config.collectionLogLocalEnabled(), globalRemote && config.collectionLogRemoteEnabled(),
					config.collectionLogOverrideUrlEnabled(), config.collectionLogOverrideUrl(), globalUrl,
					toLineOptions(config.collectionLogInclude()));
			case HIGH_VALUE_DROP:
				return build(config.highValueDropLocalEnabled(), globalRemote && config.highValueDropRemoteEnabled(),
					config.highValueDropOverrideUrlEnabled(), config.highValueDropOverrideUrl(), globalUrl,
					toLineOptions(config.highValueDropInclude()));
			case PET_DROP:
				return build(config.petDropLocalEnabled(), globalRemote && config.petDropRemoteEnabled(),
					config.petDropOverrideUrlEnabled(), config.petDropOverrideUrl(), globalUrl,
					toLineOptions(config.petDropInclude()));
			case LEVEL_UP:
				return build(config.levelUpLocalEnabled(), globalRemote && config.levelUpRemoteEnabled(),
					config.levelUpOverrideUrlEnabled(), config.levelUpOverrideUrl(), globalUrl,
					toLineOptions(config.levelUpInclude()));
			default:
				throw new IllegalArgumentException("Unhandled category: " + category);
		}
	}

	private static CategorySettings build(boolean localEnabled, boolean remoteEnabled,
		boolean overrideEnabled, String overrideUrl, String globalUrl, Set<LineIncludeOption> include)
	{
		String effectiveUrl = overrideEnabled && overrideUrl != null && !overrideUrl.isBlank()
			? overrideUrl
			: globalUrl;
		return new CategorySettings(localEnabled, remoteEnabled, effectiveUrl, include);
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
