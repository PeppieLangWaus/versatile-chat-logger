package xyz.peppie.versatilelogger.chat;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.FriendsChatManager;
import net.runelite.api.FriendsChatMember;
import net.runelite.api.MessageNode;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.clan.ClanChannelMember;
import net.runelite.api.clan.ClanRank;
import net.runelite.api.clan.ClanSettings;
import net.runelite.api.clan.ClanTitle;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.util.Text;
import xyz.peppie.versatilelogger.dto.ClanChatDto;
import xyz.peppie.versatilelogger.dto.ClanRankDto;
import xyz.peppie.versatilelogger.dto.FriendsChatDto;
import xyz.peppie.versatilelogger.dto.MessageDto;
import xyz.peppie.versatilelogger.dto.UserDto;
import xyz.peppie.versatilelogger.format.MessageFormatter;

@Singleton
public class ClanContextResolver
{
	private final Client client;

	@Inject
	ClanContextResolver(Client client)
	{
		this.client = client;
	}

	public MessageDto buildMessage(MessageNode node, boolean edited)
	{
		return new MessageDto(node.getId(), node.getTimestamp(), node.getType().name(),
			MessageFormatter.resolvedValue(node), edited);
	}

	public UserDto buildUser(MessageNode node)
	{
		String senderName = node.getName();
		int ironmanType = client.getVarbitValue(VarbitID.IRONMAN);
		ClanRankDto clanRank = resolveClanRank(senderName);
		String friendsChatRank = resolveFriendsChatRank(senderName);
		return new UserDto(senderName, ironmanType, clanRank, friendsChatRank);
	}

	public ClanChatDto buildClanChat(ChatMessageType type)
	{
		if (type == ChatMessageType.CLAN_CHAT)
		{
			ClanChannel channel = client.getClanChannel();
			return channel == null ? null : new ClanChatDto(channel.getName());
		}
		if (type == ChatMessageType.CLAN_GUEST_CHAT)
		{
			ClanChannel channel = client.getGuestClanChannel();
			return channel == null ? null : new ClanChatDto(channel.getName());
		}
		return null;
	}

	public FriendsChatDto buildFriendsChat(ChatMessageType type)
	{
		if (type != ChatMessageType.FRIENDSCHAT)
		{
			return null;
		}
		FriendsChatManager manager = client.getFriendsChatManager();
		return manager == null ? null : new FriendsChatDto(manager.getName(), manager.getOwner());
	}

	private ClanRankDto resolveClanRank(String senderName)
	{
		if (senderName == null || senderName.isBlank())
		{
			return null;
		}
		String sanitized = Text.sanitize(senderName);

		ClanChannel primary = client.getClanChannel();
		ClanChannelMember member = primary == null ? null : primary.findMember(sanitized);
		if (member != null)
		{
			return toClanRankDto(member.getRank(), client.getClanSettings());
		}

		ClanChannel guest = client.getGuestClanChannel();
		member = guest == null ? null : guest.findMember(sanitized);
		if (member != null)
		{
			return new ClanRankDto(member.getRank().getRank(), null);
		}

		return null;
	}

	private static ClanRankDto toClanRankDto(ClanRank rank, ClanSettings settings)
	{
		String title = null;
		if (settings != null)
		{
			ClanTitle clanTitle = settings.titleForRank(rank);
			title = clanTitle == null ? null : clanTitle.getName();
		}
		return new ClanRankDto(rank.getRank(), title);
	}

	private String resolveFriendsChatRank(String senderName)
	{
		if (senderName == null || senderName.isBlank())
		{
			return null;
		}
		FriendsChatManager manager = client.getFriendsChatManager();
		if (manager == null)
		{
			return null;
		}
		FriendsChatMember member = manager.findByName(Text.sanitize(senderName));
		return member == null ? null : member.getRank().name();
	}
}
