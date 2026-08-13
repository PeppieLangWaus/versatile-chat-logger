package xyz.peppie.versatilelogger.dto;

import lombok.Value;

@Value
public class MessageDto
{
	int id;
	int timestamp;
	String type;
	String text;
	/**
	 * {@code true} when this is a follow-up resend of a message already sent once with the same
	 * {@code id} - e.g. a chat command (like {@code !log}) whose output resolved after it was
	 * first logged. {@code false} for the original send.
	 */
	boolean edited;
}
