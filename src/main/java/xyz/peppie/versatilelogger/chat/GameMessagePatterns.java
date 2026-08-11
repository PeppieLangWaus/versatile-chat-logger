package xyz.peppie.versatilelogger.chat;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Text patterns used to detect the three pattern-based sub-categories of
 * {@link net.runelite.api.ChatMessageType#GAMEMESSAGE}. Sourced from RuneLite's own
 * ScreenshotPlugin so detection stays consistent with core RuneLite behavior.
 */
final class GameMessagePatterns
{
	private static final String COLLECTION_LOG_TEXT = "New item added to your collection log: ";

	private static final Pattern VALUABLE_DROP_PATTERN =
		Pattern.compile(".*Valuable drop: ([^<>]+?\\(((?:\\d+,?)+) coins\\))(?:</col>)?");

	private static final List<String> PET_MESSAGES = List.of(
		"You have a funny feeling like you're being followed",
		"You feel something weird sneaking into your backpack",
		"You have a funny feeling like you would have been followed"
	);

	private GameMessagePatterns() {	}

	/**
	 * Classifies a GAMEMESSAGE-typed message into one of the pattern-based categories, if it
	 * matches. Checked in priority order so a message only ever matches at most one pattern.
	 */
	static Optional<ChatCategory> classify(String message)
	{
		if (message == null)
		{
			return Optional.empty();
		}

		if (message.contains(COLLECTION_LOG_TEXT))
		{
			return Optional.of(ChatCategory.COLLECTION_LOG);
		}

		if (VALUABLE_DROP_PATTERN.matcher(message).matches())
		{
			return Optional.of(ChatCategory.HIGH_VALUE_DROP);
		}

		for (String petMessage : PET_MESSAGES)
		{
			if (message.contains(petMessage))
			{
				return Optional.of(ChatCategory.PET_DROP);
			}
		}

		return Optional.empty();
	}
}
