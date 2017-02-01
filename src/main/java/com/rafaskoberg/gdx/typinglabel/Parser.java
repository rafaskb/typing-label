
package com.rafaskoberg.gdx.typinglabel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.math.MathUtils;

/** Utility class to parse tokens from a {@link TypingLabel}. */
class Parser {
	private static final Pattern PATTERN_TOKEN_STRIP = compileTokenPattern();
	private static final Pattern PATTERN_MARKUP_STRIP = Pattern.compile("(\\[{2})|(\\[#?\\w*(\\[|\\])?)");
	private static final String RESET_REPLACEMENT = "{" + Token.CLEARCOLOR.name + "}{" + Token.NORMAL.name + "}";

	public static final int INDEX_TOKEN = 1;
	public static final int INDEX_PARAM = 2;

	/** Parses all tokens from the given {@link TypingLabel}. */
	static void parseTokens (TypingLabel label) {
		// Adjust and check markup color
		if (label.forceMarkupColor) label.getBitmapFontCache().getFont().getData().markupEnabled = true;

		// Remove any previous entries
		label.tokenEntries.clear();

		// Parse all tokens with text replacements, namely color and var.
		parseReplacements(label);

		// Parse all regular tokens and properly register them
		parseRegularTokens(label);

		// Parse color markups and register SKIP tokens
		parseColorMarkups(label);

		// Sort token entries
		label.tokenEntries.sort();
		label.tokenEntries.reverse();
	}

	/** Parse tokens that only replace text, such as colors and variables. */
	private static void parseReplacements (TypingLabel label) {
		// Get text
		CharSequence text = label.getText();
		boolean hasMarkup = label.getBitmapFontCache().getFont().getData().markupEnabled;

		// Create buffer
		StringBuffer buf = new StringBuffer(text.length());
		Matcher m = PATTERN_TOKEN_STRIP.matcher(text);
		int matcherIndexOffset = 0;

		// Iterate through matches
		while (true) {
			// Reset buffer and matcher
			buf.setLength(0);
			m.reset(text);

			// Make sure there's at least one regex match
			if (!m.find(matcherIndexOffset)) break;

			// Get token and parameter
			final Token token = Token.fromName(m.group(INDEX_TOKEN));
			final String param = m.groupCount() == INDEX_PARAM ? m.group(INDEX_PARAM) : null;

			// If token couldn't be parsed, move one index forward to continue the search
			if (token == null) {
				matcherIndexOffset++;
				continue;
			}

			// Process tokens and handle replacement
			String replacement = "";
			switch (token) {
			case COLOR:
				if (hasMarkup) replacement = stringToColorMarkup(param);
				break;
			case CLEARCOLOR:
				if (hasMarkup) replacement = "[#" + label.getClearColor().toString() + "]";
				break;
			case VAR:
				String variable = param.toUpperCase();
				replacement = label.getVariables().get(variable, variable);
				break;
			case RESET:
				replacement = RESET_REPLACEMENT;
				break;
			default:
				// We don't want to process this token now. Move one index forward to continue the search
				matcherIndexOffset++;
				continue;
			}

			// Remove token from string
			m.appendReplacement(buf, Matcher.quoteReplacement(replacement));
			m.appendTail(buf);

			// Update text with replacement
			text = buf.toString();
		}

		// Set new text
		label.setText(text, false);
	}

	/** Parses regular tokens that don't need replacement and register their indexes in the {@link TypingLabel}. */
	private static void parseRegularTokens (TypingLabel label) {
		// Get text
		CharSequence text = label.getText();

		// Create matcher and buffer
		Matcher m = PATTERN_TOKEN_STRIP.matcher(text);
		StringBuffer buf = new StringBuffer(text.length());
		int matcherIndexOffset = 0;

		// Iterate through matches
		while (true) {
			// Reset matcher and buffer
			m.reset(text);
			buf.setLength(0);

			// Make sure there's at least one regex match
			if (!m.find(matcherIndexOffset)) break;

			// Get token, param and index of where the token begins
			final Token token = Token.fromName(m.group(INDEX_TOKEN));
			final String param = m.groupCount() == INDEX_PARAM ? m.group(INDEX_PARAM) : null;
			final int index = m.start(0);

			// If token couldn't be parsed, move one index forward to continue the search
			if (token == null) {
				matcherIndexOffset++;
				continue;
			}

			// Process tokens
			float floatValue = 0;
			String stringValue = null;
			switch (token) {
			case WAIT:
				floatValue = stringToFloat(param, TypingConfig.DEFAULT_WAIT_VALUE);
				break;

			case SPEED:
				float minModifier = TypingConfig.MIN_SPEED_MODIFIER;
				float maxModifier = TypingConfig.MAX_SPEED_MODIFIER;
				float modifier = MathUtils.clamp(stringToFloat(param, 1), minModifier, maxModifier);
				floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / modifier;
				break;
			case SLOWER:
				floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / 0.500f;
				break;
			case SLOW:
				floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / 0.667f;
				break;
			case NORMAL:
				floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR;
				break;
			case FAST:
				floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / 2.000f;
				break;
			case FASTER:
				floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / 4.000f;
				break;

			case EVENT:
				stringValue = param;
				break;

			default:
				// We don't want to process this token now. Move one index forward to continue the search
				matcherIndexOffset++;
				continue;
			}

			// Register token
			TokenEntry entry = new TokenEntry(token, index, floatValue, stringValue);
			label.tokenEntries.add(entry);

			// Remove token from string
			m.appendReplacement(buf, Matcher.quoteReplacement(""));
			m.appendTail(buf);

			// Set new text without tokens
			text = buf.toString();
		}

		// Update label text
		label.setText(text, false);
	}

	/** Parse color markup tags and register SKIP tokens. */
	private static void parseColorMarkups (TypingLabel label) {
		// Get text
		final CharSequence text = label.getText();

		// Iterate through matches and register skip tokens
		Matcher m = PATTERN_MARKUP_STRIP.matcher(text);
		while (m.find()) {
			final String tag = m.group(0);
			final int index = m.start(0);
			label.tokenEntries.add(new TokenEntry(Token.SKIP, index, 0, tag));
		}
	}

	/** Returns a float value parsed from the given String, or the default value if the string couldn't be parsed. */
	private static float stringToFloat (String str, float defaultValue) {
		if (str != null) {
			try {
				return Float.parseFloat(str);
			} catch (Exception e) {
			}
		}
		return defaultValue;
	}

	/** Encloses the given string in brackets to work as a regular color markup tag. */
	private static String stringToColorMarkup (String str) {
		if (str != null) str = str.toUpperCase();
		return "[" + str + "]";
	}

	/** Returns a compiled {@link Pattern} that groups the token name in the first group and the params in an optional second one.
	 * Case insensitive. */
	private static Pattern compileTokenPattern () {
		StringBuilder sb = new StringBuilder();
		sb.append("\\{(");
		Token[] tokens = Token.values();
		for (int i = 0; i < tokens.length; i++) {
			sb.append(tokens[i]);
			if ((i + 1) < tokens.length) sb.append('|');
		}
		sb.append(")(?:=([#_ \\.\\w]+))?\\}");
		return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
	}

}
