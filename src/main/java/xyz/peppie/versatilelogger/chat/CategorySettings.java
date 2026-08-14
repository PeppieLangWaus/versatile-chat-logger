package xyz.peppie.versatilelogger.chat;

import java.util.Set;
import lombok.Value;
import xyz.peppie.versatilelogger.config.VersatileLoggerConfig.FormatMode;
import xyz.peppie.versatilelogger.format.LineIncludeOption;

/**
 * Resolved, ready-to-use settings for one {@link ChatCategory} for the current config state.
 * {@code remoteEnabled} already accounts for the global remote-logging master switch, so
 * consumers never need to re-check it. {@code effectiveFormatMode} likewise already accounts for
 * the category's own format override, falling back to the global {@code formatMode} when the
 * category doesn't override it. {@code detailedTimestamp} and whether typed chat commands are
 * watched for a resolution (via {@link LineIncludeOption#COMMAND_OUTPUT} in {@code include}) are
 * both per-category settings, unlike the two above.
 */
@Value
public class CategorySettings
{
	boolean localEnabled;
	boolean remoteEnabled;
	String effectiveRemoteUrl;
	FormatMode effectiveFormatMode;
	boolean detailedTimestamp;
	Set<LineIncludeOption> include;
}
