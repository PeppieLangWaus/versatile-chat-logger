package xyz.peppie.versatilelogger.chat;

import lombok.Value;
import net.runelite.api.ChatMessageType;
import net.runelite.api.MessageNode;

@Value
public class PendingCommandMessage
{
	MessageNode node;
	ChatCategory category;
	ChatMessageType type;
	CategorySettings settings;
	long enqueuedAtMillis;
}
