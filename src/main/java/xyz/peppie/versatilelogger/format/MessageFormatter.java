package xyz.peppie.versatilelogger.format;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import net.runelite.api.MessageNode;
import net.runelite.client.util.Text;

/**
 * Builds the single plain-text line representation of a message, shared by local file logging
 * and the "In-game message" remote format so the two representations can never drift.
 */
public class MessageFormatter
{
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
		.withZone(ZoneId.systemDefault());
	private static final DateTimeFormatter TIME_FORMAT_DETAILED = DateTimeFormatter.ofPattern("HH:mm:ss")
		.withZone(ZoneId.systemDefault());

	public String buildLine(MessageNode node, Set<LineIncludeOption> include, boolean detailedTimestamp)
	{
		boolean showIcons = include.contains(LineIncludeOption.ICONS);
		StringBuilder line = new StringBuilder();

		if (include.contains(LineIncludeOption.TIMESTAMP))
		{
			DateTimeFormatter timeFormat = detailedTimestamp ? TIME_FORMAT_DETAILED : TIME_FORMAT;
			line.append('[')
				.append(timeFormat.format(Instant.ofEpochSecond(node.getTimestamp())))
				.append("] ");
		}

		if (include.contains(LineIncludeOption.NAME))
		{
			String channelName = applyIconFilter(node.getSender(), showIcons);
			if (!channelName.isBlank())
			{
				line.append('(').append(channelName).append(") ");
			}
		}

		String speaker = applyIconFilter(node.getName(), showIcons);
		if (!speaker.isBlank())
		{
			line.append(speaker).append(": ");
		}

		line.append(applyIconFilter(resolvedValue(node), showIcons));

		return line.toString();
	}

	/**
	 * Chat commands (e.g. {@code !log}, {@code !pets}) aren't resolved by editing
	 * {@link MessageNode#getValue()} or by firing a new event - RuneLite's {@code
	 * ChatCommandManager}, and third-party plugins built on it such as RuneProfile, resolve them
	 * by overwriting {@link MessageNode#getRuneLiteFormatMessage()} on the same node, sometimes
	 * after a network round trip. Preferring it here - matching what the client itself renders -
	 * means a caller that re-reads a node after resolution automatically picks up the output
	 * instead of the raw command text.
	 */
	public static String resolvedValue(MessageNode node)
	{
		String formatted = node.getRuneLiteFormatMessage();
		return formatted != null ? formatted : node.getValue();
	}

	/**
	 * When icons are included, the raw value is left as-is (embedded {@code <img=NN>}/formatting
	 * tags intact, matching what the game client itself would render). When excluded, all tags
	 * are stripped via {@link Text#removeTags(String)}.
	 */
	private static String applyIconFilter(String raw, boolean showIcons)
	{
		String value = raw == null ? "" : raw;
		return showIcons ? value : Text.removeTags(value);
	}
}
