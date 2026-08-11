package xyz.peppie.versatilelogger.chat;

import java.util.Optional;
import net.runelite.api.ChatMessageType;

/**
 * Single source of truth for mapping an incoming chat message to at most one {@link ChatCategory}.
 * Pattern-based categories always take priority over the generic GAME_MESSAGES bucket, so a
 * message is never counted under two categories at once.
 */
public class ChatMessageRouter
{
	public Optional<ChatCategory> categorize(ChatMessageType type, String message)
	{
		if (type == ChatMessageType.GAMEMESSAGE)
		{
			Optional<ChatCategory> patternMatch = GameMessagePatterns.classify(message);
			if (patternMatch.isPresent())
			{
				return patternMatch;
			}
		}

		for (ChatCategory category : ChatCategory.values())
		{
			if (category.getTypes().contains(type))
			{
				return Optional.of(category);
			}
		}

		return Optional.empty();
	}
}
