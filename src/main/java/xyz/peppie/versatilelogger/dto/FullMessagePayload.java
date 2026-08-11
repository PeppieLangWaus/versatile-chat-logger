package xyz.peppie.versatilelogger.dto;

import lombok.Value;

@Value
public class FullMessagePayload
{
	MessageDto message;
	UserDto user;
	ClanChatDto clanChat;
	FriendsChatDto friendsChat;
}
