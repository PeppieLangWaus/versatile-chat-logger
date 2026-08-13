package xyz.peppie.versatilelogger.format;

/**
 * Category-agnostic include options. Decouples callers from the several distinct config-facing
 * "include" enums (which only exist as separate types so RuneLite's multi-select config UI shows
 * each category the right set of options). {@link MessageFormatter#buildLine} consumes
 * {@code TIMESTAMP}, {@code ICONS} and {@code NAME}; {@code COMMAND_OUTPUT} is read directly by
 * the plugin to decide whether a category's typed chat commands are watched for a resolution.
 */
public enum LineIncludeOption
{
	TIMESTAMP,
	ICONS,
	NAME,
	COMMAND_OUTPUT
}
