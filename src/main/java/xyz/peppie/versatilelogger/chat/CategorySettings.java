package xyz.peppie.versatilelogger.chat;

import java.util.Set;
import lombok.Value;
import xyz.peppie.versatilelogger.format.LineIncludeOption;

/**
 * Resolved, ready-to-use settings for one {@link ChatCategory} for the current config state.
 * {@code remoteEnabled} already accounts for the global remote-logging master switch, so
 * consumers never need to re-check it.
 */
@Value
public class CategorySettings
{
	boolean localEnabled;
	boolean remoteEnabled;
	String effectiveRemoteUrl;
	Set<LineIncludeOption> include;
}
