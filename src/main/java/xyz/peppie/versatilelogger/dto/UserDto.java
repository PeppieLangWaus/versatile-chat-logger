package xyz.peppie.versatilelogger.dto;

import lombok.Value;

@Value
public class UserDto
{
	String name;
	int type;
	ClanRankDto clanRank;
	String friendsChatRank;
}
