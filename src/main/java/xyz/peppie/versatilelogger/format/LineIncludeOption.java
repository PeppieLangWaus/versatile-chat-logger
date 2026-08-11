package xyz.peppie.versatilelogger.format;

/**
 * Category-agnostic include options consumed by {@link MessageFormatter}. Decouples the
 * formatter from the three distinct config-facing "include" enums (which only exist as separate
 * types so RuneLite's multi-select config UI shows each category the right set of options).
 */
public enum LineIncludeOption
{
	TIMESTAMP,
	ICONS,
	NAME
}
