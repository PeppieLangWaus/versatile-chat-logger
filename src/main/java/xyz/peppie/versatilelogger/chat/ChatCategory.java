package xyz.peppie.versatilelogger.chat;

import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;
import net.runelite.api.ChatMessageType;

@Getter
public enum ChatCategory
{
	PUBLIC_CHAT("Public chat", "public-chat",
		EnumSet.of(ChatMessageType.PUBLICCHAT, ChatMessageType.MODCHAT)),
	CHANNEL_CHAT("Channel chat (friends chat)", "channel-chat",
		EnumSet.of(ChatMessageType.FRIENDSCHAT)),
	CLAN_CHAT("Clan chat", "clan-chat",
		EnumSet.of(ChatMessageType.CLAN_CHAT)),
	GAME_MESSAGES("Game messages", "game-messages",
		EnumSet.of(ChatMessageType.GAMEMESSAGE, ChatMessageType.ENGINE, ChatMessageType.WELCOME,
			ChatMessageType.BROADCAST, ChatMessageType.DIDYOUKNOW, ChatMessageType.FRIENDSCHATNOTIFICATION,
			ChatMessageType.CLAN_MESSAGE, ChatMessageType.CLAN_GUEST_MESSAGE)),
	PRIVATE_MESSAGES("Private messages", "private-messages",
		EnumSet.of(ChatMessageType.PRIVATECHAT, ChatMessageType.PRIVATECHATOUT)),
	GUEST_CLAN_CHAT("Guest clan chat", "guest-clan-chat",
		EnumSet.of(ChatMessageType.CLAN_GUEST_CHAT)),
	GROUP_CHAT("Group chat (Group Ironman)", "group-chat",
		EnumSet.of(ChatMessageType.CLAN_GIM_CHAT, ChatMessageType.CLAN_GIM_FORM_GROUP,
			ChatMessageType.CLAN_GIM_GROUP_WITH, ChatMessageType.CLAN_GIM_MESSAGE)),
	COLLECTION_LOG("Collection log unlock", "collection-log", EnumSet.noneOf(ChatMessageType.class)),
	HIGH_VALUE_DROP("High value drop", "high-value-drop", EnumSet.noneOf(ChatMessageType.class)),
	PET_DROP("Pet drops", "pet-drop", EnumSet.noneOf(ChatMessageType.class)),
	LEVEL_UP("Level up", "level-up", EnumSet.of(ChatMessageType.LEVELUPMESSAGE));

	private final String displayName;
	private final String folderSlug;
	private final Set<ChatMessageType> types;

	ChatCategory(String displayName, String folderSlug, Set<ChatMessageType> types)
	{
		this.displayName = displayName;
		this.folderSlug = folderSlug;
		this.types = types;
	}
}
