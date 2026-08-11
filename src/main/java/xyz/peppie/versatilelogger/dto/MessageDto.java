package xyz.peppie.versatilelogger.dto;

import lombok.Value;

@Value
public class MessageDto
{
	int id;
	int timestamp;
	String type;
	String text;
}
