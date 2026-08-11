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

/**
 * Resolves the clan/friends-chat context needed for the "Full" remote format. Every method here
 * reads from {@link Client} and must only be called on the client thread — which is already true
 * for every call site, since they're all invoked synchronously from inside the
 * {@code @Subscribe onChatMessage} handler.
 */
@Singleton
public class ClanContextResolver
{
	private final Client client;

	@Inject
	ClanContextResolver(Client client)
	{
		this.client = client;
	}

	/**
	 * {@code text} is the raw, unfiltered {@code MessageNode.getValue()} (icon/formatting tags
	 * intact) — "Full" mode is meant to send every property as-is, unlike "In-game message" mode
	 * which respects each category's icon-filtering include option.
	 */
	public MessageDto buildMessage(MessageNode node)
	{
		return new MessageDto(node.getId(), node.getTimestamp(), node.getType().name(), node.getValue());
	}

	public UserDto buildUser(MessageNode node)
	{
		String senderName = node.getName();
		int ironmanType = client.getVarbitValue(VarbitID.IRONMAN);
		ClanRankDto clanRank = resolveClanRank(senderName);
		String friendsChatRank = resolveFriendsChatRank(senderName);
		return new UserDto(senderName, ironmanType, clanRank, friendsChatRank);
	}

	/**
	 * Attached for CLAN_CHAT and CLAN_GUEST_CHAT only (per the user's own scoping decision) —
	 * GIM chat is a separate "group chat" concept and never gets a clanChat object.
	 */
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
			// No client API exposes the guest clan's numeric id from a ClanChannel, so a title
			// can't be resolved here for guest-clan members; the numeric rank is still returned.
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

	/**
	 * Resolves the message sender's own friends-chat rank (not the local player's), so this
	 * field is meaningful regardless of who sent the message.
	 */
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
