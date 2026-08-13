package xyz.peppie.versatilelogger.chat;

import lombok.Value;
import net.runelite.api.ChatMessageType;
import net.runelite.api.MessageNode;

/**
 * A chat message that looked like an unresolved command (e.g. {@code !log}, {@code !pets}) when
 * it was first logged. RuneLite's {@code ChatCommandManager} - and plugins built on it, like
 * RuneProfile's {@code !log} - resolve a command by overwriting the same {@link MessageNode} in
 * place some time later, sometimes after a network round trip, rather than firing a new event.
 * Holding onto the node lets the plugin notice that rewrite and send a follow-up update, instead
 * of delaying the original message until the command resolves.
 */
@Value
public class PendingCommandMessage
{
	MessageNode node;
	ChatCategory category;
	ChatMessageType type;
	CategorySettings settings;
	long enqueuedAtMillis;
}
