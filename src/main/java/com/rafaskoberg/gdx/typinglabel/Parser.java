
package com.rafaskoberg.gdx.typinglabel;

import com.badlogic.gdx.math.MathUtils;
import com.rafaskoberg.gdx.typinglabel.effects.EaseEffect;
import com.rafaskoberg.gdx.typinglabel.effects.Effect;
import com.rafaskoberg.gdx.typinglabel.effects.JumpEffect;
import com.rafaskoberg.gdx.typinglabel.effects.ShakeEffect;
import com.rafaskoberg.gdx.typinglabel.effects.SickEffect;
import com.rafaskoberg.gdx.typinglabel.effects.WaveEffect;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Utility class to parse tokens from a {@link TypingLabel}. */
class Parser {
    private static final Pattern PATTERN_TOKEN_STRIP  = compileTokenPattern();
    private static final Pattern PATTERN_MARKUP_STRIP = Pattern.compile("(\\[{2})|(\\[#?\\w*(\\[|\\])?)");
    private static final String  RESET_REPLACEMENT    = getResetReplacement();

    public static final int INDEX_TOKEN = 1;
    public static final int INDEX_PARAM = 2;

    /** Parses all tokens from the given {@link TypingLabel}. */
    static void parseTokens(TypingLabel label) {
        // Adjust and check markup color
        if(label.forceMarkupColor) label.getBitmapFontCache().getFont().getData().markupEnabled = true;

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
    private static void parseReplacements(TypingLabel label) {
        // Get text
        CharSequence text = label.getText();
        boolean hasMarkup = label.getBitmapFontCache().getFont().getData().markupEnabled;

        // Create buffer
        StringBuffer buf = new StringBuffer(text.length());
        Matcher m = PATTERN_TOKEN_STRIP.matcher(text);
        int matcherIndexOffset = 0;

        // Iterate through matches
        while(true) {
            // Reset buffer and matcher
            buf.setLength(0);
            m.reset(text);

            // Make sure there's at least one regex match
            if(!m.find(matcherIndexOffset)) break;

            // Get token and parameter
            final Token token = Token.fromName(m.group(INDEX_TOKEN));
            final String param = m.groupCount() == INDEX_PARAM ? m.group(INDEX_PARAM) : null;

            // If token couldn't be parsed, move one index forward to continue the search
            if(token == null) {
                matcherIndexOffset++;
                continue;
            }

            // Process tokens and handle replacement
            String replacement = "";
            switch(token) {
                case COLOR:
                    if(hasMarkup) replacement = stringToColorMarkup(param);
                    break;
                case CLEARCOLOR:
                    if(hasMarkup) replacement = "[#" + label.getClearColor().toString() + "]";
                    break;
                case VAR:
                    String variable = param;
                    replacement = null;

                    // Try to replace variable through listener.
                    if(label.getTypingListener() != null) {
                        replacement = label.getTypingListener().replaceVariable(variable);
                    }

                    // If replacement is null, get value from maps.
                    if(replacement == null) {
                        variable = variable.toUpperCase();
                        replacement = label.getVariables().get(variable, variable);
                    }

                    // Make sure we're not inserting "null" to the text.
                    if(replacement == null) replacement = "";
                    break;
                case RESET:
                    replacement = RESET_REPLACEMENT + label.getDefaultToken();
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
    private static void parseRegularTokens(TypingLabel label) {
        // Get text
        CharSequence text = label.getText();

        // Create matcher and buffer
        Matcher m = PATTERN_TOKEN_STRIP.matcher(text);
        StringBuffer buf = new StringBuffer(text.length());
        int matcherIndexOffset = 0;

        // Iterate through matches
        while(true) {
            // Reset matcher and buffer
            m.reset(text);
            buf.setLength(0);

            // Make sure there's at least one regex match
            if(!m.find(matcherIndexOffset)) break;

            // Get token, param and index of where the token begins
            final Token token = Token.fromName(m.group(INDEX_TOKEN));
            final String paramsString = m.groupCount() == INDEX_PARAM ? m.group(INDEX_PARAM) : null;
            final String[] params = paramsString == null ? new String[0] : paramsString.split(";");
            final String firstParam = params.length > 0 ? params[0] : null;
            final int index = m.start(0);
            int indexOffset = 0;

            // If token couldn't be parsed, move one index forward to continue the search
            if(token == null) {
                matcherIndexOffset++;
                continue;
            }

            // Process tokens
            float floatValue = 0;
            String stringValue = null;
            Effect effect = null;
            switch(token) {
                case WAIT:
                    floatValue = stringToFloat(firstParam, TypingConfig.DEFAULT_WAIT_VALUE);
                    break;

                case SPEED:
                    float minModifier = TypingConfig.MIN_SPEED_MODIFIER;
                    float maxModifier = TypingConfig.MAX_SPEED_MODIFIER;
                    float modifier = MathUtils.clamp(stringToFloat(firstParam, 1), minModifier, maxModifier);
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
                    stringValue = paramsString;
                    indexOffset = -1;
                    break;

                case SHAKE:
                    // distance;intensity;duration
                    effect = new ShakeEffect(label);
                    if(params.length > 0) {
                        ((ShakeEffect) effect).distance = stringToFloat(params[0], 1);
                    }
                    if(params.length > 1) {
                        ((ShakeEffect) effect).intensity = stringToFloat(params[1], 1);
                    }
                    if(params.length > 2) {
                        ((ShakeEffect) effect).duration = stringToFloat(params[2], -1);
                    }
                    break;
                case ENDSHAKE:
                    break;

                case SICK:
                    // distance;intensity;duration
                    effect = new SickEffect(label);
                    if(params.length > 0) {
                        ((SickEffect) effect).distance = stringToFloat(params[0], 1);
                    }
                    if(params.length > 1) {
                        ((SickEffect) effect).intensity = stringToFloat(params[1], 1);
                    }
                    if(params.length > 2) {
                        ((SickEffect) effect).duration = stringToFloat(params[2], -1);
                    }
                    break;
                case ENDSICK:
                    break;

                case WAVE:
                    // distance;frequency;intensity;duration
                    effect = new WaveEffect(label);
                    if(params.length > 0) {
                        ((WaveEffect) effect).distance = stringToFloat(params[0], 1);
                    }
                    if(params.length > 1) {
                        ((WaveEffect) effect).frequency = stringToFloat(params[1], 1);
                    }
                    if(params.length > 2) {
                        ((WaveEffect) effect).intensity = stringToFloat(params[2], 1);
                    }
                    if(params.length > 3) {
                        ((WaveEffect) effect).duration = stringToFloat(params[3], -1);
                    }
                    break;
                case ENDWAVE:
                    break;

                case EASE:
                    // distance;intensity
                    effect = new EaseEffect(label);
                    if(params.length > 0) {
                        ((EaseEffect) effect).distance = stringToFloat(params[0], 1);
                    }
                    if(params.length > 1) {
                        ((EaseEffect) effect).intensity = stringToFloat(params[1], 1);
                    }
                    break;
                case ENDEASE:
                    break;

                case JUMP:
                    // distance;frequency;intensity;duration
                    effect = new JumpEffect(label);
                    if(params.length > 0) {
                        ((JumpEffect) effect).distance = stringToFloat(params[0], 1);
                    }
                    if(params.length > 1) {
                        ((JumpEffect) effect).frequency = stringToFloat(params[1], 1);
                    }
                    if(params.length > 2) {
                        ((JumpEffect) effect).intensity = stringToFloat(params[2], 1);
                    }
                    if(params.length > 3) {
                        ((JumpEffect) effect).duration = stringToFloat(params[3], -1);
                    }
                    break;
                case ENDJUMP:
                    break;

                default:
                    // We don't want to process this token now. Move one index forward to continue the search
                    matcherIndexOffset++;
                    continue;
            }

            // Register token
            TokenEntry entry = new TokenEntry(token, index + indexOffset, floatValue, stringValue);
            entry.effect = effect;
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
    private static void parseColorMarkups(TypingLabel label) {
        // Get text
        final CharSequence text = label.getText();

        // Iterate through matches and register skip tokens
        Matcher m = PATTERN_MARKUP_STRIP.matcher(text);
        while(m.find()) {
            final String tag = m.group(0);
            final int index = m.start(0);
            label.tokenEntries.add(new TokenEntry(Token.SKIP, index, 0, tag));
        }
    }

    /** Returns a float value parsed from the given String, or the default value if the string couldn't be parsed. */
    private static float stringToFloat(String str, float defaultValue) {
        if(str != null) {
            try {
                return Float.parseFloat(str);
            } catch(Exception e) {
            }
        }
        return defaultValue;
    }

    /** Encloses the given string in brackets to work as a regular color markup tag. */
    private static String stringToColorMarkup(String str) {
        if(str != null) str = str.toUpperCase();
        return "[" + str + "]";
    }

    /**
     * Returns a compiled {@link Pattern} that groups the token name in the first group and the params in an optional
     * second one. Case insensitive.
     */
    private static Pattern compileTokenPattern() {
        StringBuilder sb = new StringBuilder();
        sb.append("\\{(");
        Token[] tokens = Token.values();
        for(int i = 0; i < tokens.length; i++) {
            sb.append(tokens[i]);
            if((i + 1) < tokens.length) sb.append('|');
        }
        sb.append(")(?:=([;#-_ \\.\\w]+))?\\}");
        return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
    }

    /** Returns the replacement string intended to be used on {RESET} tokens. */
    private static String getResetReplacement() {
        Token[] tokens = {Token.CLEARCOLOR, Token.NORMAL, Token.ENDJUMP, Token.ENDSHAKE, Token.ENDSICK, Token.ENDWAVE, Token.ENDEASE};

        StringBuilder sb = new StringBuilder();
        for(Token token : tokens) {
            sb.append('{').append(token.name).append('}');
        }
        return sb.toString();
    }

}
