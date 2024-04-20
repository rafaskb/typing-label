
package com.rafaskoberg.gdx.typinglabel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Constructor;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import regexodus.Matcher;
import regexodus.Pattern;
import regexodus.REFlags;

/** Utility class to parse tokens from a {@link TypingLabel}. */
class Parser {
    private static TokenDelimiter CURRENT_DELIMITER    = TypingConfig.TOKEN_DELIMITER;
    private static Pattern        PATTERN_TOKEN_STRIP  = compileTokenPattern();
    private static Pattern        PATTERN_MARKUP_STRIP = Pattern.compile("(\\[{2})|(\\[#?\\w*(\\[|\\])?)");

    private static final Pattern PATTERN_COLOR_HEX_NO_HASH = Pattern.compile("[A-F0-9]{6}");

    private static final String[] BOOLEAN_TRUE = {"true", "yes", "t", "y", "on", "1"};
    private static final int      INDEX_TOKEN  = 1;
    private static final int      INDEX_PARAM  = 2;

    private static String RESET_REPLACEMENT;

    /** Parses all tokens from the given {@link TypingLabel}. */
    static void parseTokens(TypingLabel label) {
        // Detect if token delimiter has changed
        boolean hasDelimiterChanged = CURRENT_DELIMITER != TypingConfig.TOKEN_DELIMITER;
        if(hasDelimiterChanged) {
            CURRENT_DELIMITER = TypingConfig.TOKEN_DELIMITER;
        }

        // Compile patterns if necessary
        if(PATTERN_TOKEN_STRIP == null || TypingConfig.dirtyEffectMaps || hasDelimiterChanged) {
            PATTERN_TOKEN_STRIP = compileTokenPattern();
        }
        if(RESET_REPLACEMENT == null || TypingConfig.dirtyEffectMaps || hasDelimiterChanged) {
            RESET_REPLACEMENT = getResetReplacement();
        }

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

        // Create string builder
        StringBuilder sb = new StringBuilder(text.length());
        Matcher m = PATTERN_TOKEN_STRIP.matcher(text);
        int matcherIndexOffset = 0;

        // Iterate through matches
        while(true) {
            // Reset StringBuilder and matcher
            sb.setLength(0);
            m.setTarget(text);
            m.setPosition(matcherIndexOffset);

            // Make sure there's at least one regex match
            if(!m.find()) break;

            // Get token and parameter
            final InternalToken internalToken = InternalToken.fromName(m.group(INDEX_TOKEN));
            final String param = m.group(INDEX_PARAM);

            // If token couldn't be parsed, move one index forward to continue the search
            if(internalToken == null) {
                matcherIndexOffset++;
                continue;
            }

            // Process tokens and handle replacement
            String replacement = "";
            switch(internalToken) {
                case COLOR:
                    if(hasMarkup) replacement = stringToColorMarkup(param);
                    break;
                case ENDCOLOR:
                case CLEARCOLOR:
                    if(hasMarkup) replacement = "[#" + label.getClearColor().toString() + "]";
                    break;
                case VAR:
                    replacement = null;

                    // Try to replace variable through listeners.
                    for(TypingListener listener : label.getTypingListeners()) {
                        replacement = listener.replaceVariable(param);
                        if(replacement != null) break;
                    }

                    // If replacement is null, get value from maps.
                    if(replacement == null) {
                        replacement = label.getVariables().get(param.toUpperCase());
                    }

                    // If replacement is still null, get value from global scope
                    if(replacement == null) {
                        replacement = TypingConfig.GLOBAL_VARS.get(param.toUpperCase());
                    }

                    // Make sure we're not inserting "null" to the text.
                    if(replacement == null) replacement = param.toUpperCase();
                    break;
                case IF:
                    // Process token
                    replacement = processIfToken(label, param);

                    // Make sure we're not inserting "null" to the text.
                    if(replacement == null) replacement = param.toUpperCase();

                    break;
                case RESET:
                    replacement = RESET_REPLACEMENT + label.getDefaultToken();
                    break;
                default:
                    // We don't want to process this token now. Move one index forward to continue the search
                    matcherIndexOffset++;
                    continue;
            }

            // Update text with replacement
            m.setPosition(m.start());
            text = m.replaceFirst(replacement);
        }

        // Set new text
        label.setText(text, false, false);
    }

    private static String processIfToken(TypingLabel label, String paramsString) {
        // Split params
        final String[] params = paramsString == null ? new String[0] : paramsString.split(";");
        final String variable = params.length > 0 ? params[0] : null;

        // Ensure our params are valid
        if(params.length <= 1 || variable == null) {
            return null;
        }

        /*
            Get variable's value
         */
        String variableValue = null;

        // Try to get value through listener.
        for(TypingListener listener : label.getTypingListeners()) {
            variableValue = listener.replaceVariable(variable);
            if(variableValue != null) break;
        }

        // If value is null, get it from maps.
        if(variableValue == null) {
            variableValue = label.getVariables().get(variable.toUpperCase());
        }

        // If value is still null, get it from global scope
        if(variableValue == null) {
            variableValue = TypingConfig.GLOBAL_VARS.get(variable.toUpperCase());
        }

        // Ensure variable is never null
        if(variableValue == null) {
            variableValue = "";
        }

        // Iterate through params and try to find a match
        String defaultValue = null;
        for(int i = 1, n = params.length; i < n; i++) {
            String[] subParams = params[i].split("=", 2);
            String key = subParams[0];
            String value = subParams[subParams.length - 1];
            boolean isKeyValid = subParams.length > 1 && !key.isEmpty();

            // If key isn't valid, it must be a default value. Store it and carry on
            if(!isKeyValid) {
                defaultValue = value;
                break;
            }

            // Compare variable's value with key
            if(variableValue.equalsIgnoreCase(key)) {
                return value;
            }
        }

        // Try to return any default values captured during the iteration
        if(defaultValue != null) {
            return defaultValue;
        }

        // If we got this far, no values matched our variable.
        // Return the variable itself, which might be useful for debugging.
        return variable;
    }

    /** Parses regular tokens that don't need replacement and register their indexes in the {@link TypingLabel}. */
    private static void parseRegularTokens(TypingLabel label) {
        // Get text
        CharSequence text = label.getText();

        // Create matcher and StringBuilder
        Matcher m = PATTERN_TOKEN_STRIP.matcher(text);
        StringBuilder sb = new StringBuilder(text.length());
        int matcherIndexOffset = 0;

        // Iterate through matches
        while(true) {
            // Reset matcher and StringBuilder
            m.setTarget(text);
            sb.setLength(0);
            m.setPosition(matcherIndexOffset);

            // Make sure there's at least one regex match
            if(!m.find()) break;

            // Get token name and category
            String tokenName = m.group(INDEX_TOKEN).toUpperCase();
            TokenCategory tokenCategory = null;
            InternalToken tmpToken = InternalToken.fromName(tokenName);
            if(tmpToken == null) {
                if(TypingConfig.EFFECT_START_TOKENS.containsKey(tokenName)) {
                    tokenCategory = TokenCategory.EFFECT_START;
                } else if(TypingConfig.EFFECT_END_TOKENS.containsKey(tokenName)) {
                    tokenCategory = TokenCategory.EFFECT_END;
                }
            } else {
                tokenCategory = tmpToken.category;
            }

            // Get token, param and index of where the token begins
            int groupCount = m.groupCount();
            final String paramsString = groupCount == INDEX_PARAM ? m.group(INDEX_PARAM) : null;
            final String[] params = paramsString == null ? new String[0] : paramsString.split(";");
            final String firstParam = params.length > 0 ? params[0] : null;
            final int index = m.start(0);
            int indexOffset = 0;

            // If token couldn't be parsed, move one index forward to continue the search
            if(tokenCategory == null) {
                matcherIndexOffset++;
                continue;
            }

            // Process tokens
            float floatValue = 0;
            String stringValue = null;
            Effect effect = null;

            switch(tokenCategory) {
                case WAIT: {
                    floatValue = stringToFloat(firstParam, TypingConfig.DEFAULT_WAIT_VALUE);
                    break;
                }
                case EVENT: {
                    stringValue = paramsString;
                    indexOffset = -1;
                    break;
                }
                case SPEED: {
                    switch(tokenName) {
                        case "SPEED":
                            float minModifier = TypingConfig.MIN_SPEED_MODIFIER;
                            float maxModifier = TypingConfig.MAX_SPEED_MODIFIER;
                            float modifier = MathUtils.clamp(stringToFloat(firstParam, 1), minModifier, maxModifier);
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / modifier;
                            break;
                        case "SLOWER":
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / 0.500f;
                            break;
                        case "SLOW":
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / 0.667f;
                            break;
                        case "NORMAL":
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR;
                            break;
                        case "FAST":
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / 2.000f;
                            break;
                        case "FASTER":
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / 4.000f;
                            break;
                    }
                    break;
                }
                case EFFECT_START: {
                    Class<? extends Effect> clazz = TypingConfig.EFFECT_START_TOKENS.get(tokenName.toUpperCase());
                    try {
                        if(clazz != null) {
                            Constructor constructor = ClassReflection.getConstructors(clazz)[0];
                            int constructorParamCount = constructor.getParameterTypes().length;
                            if(constructorParamCount >= 2) {
                                effect = (Effect) constructor.newInstance(label, params);
                            } else {
                                effect = (Effect) constructor.newInstance(label);
                            }
                        }
                    } catch(ReflectionException e) {
                        String message = "Failed to initialize " + tokenName + " effect token. Make sure the associated class (" + clazz + ") has only one constructor with TypingLabel as first parameter and optionally String[] as second.";
                        throw new IllegalStateException(message, e);
                    }
                    break;
                }
                case EFFECT_END: {
                    break;
                }
            }

            // Register token
            TokenEntry entry = new TokenEntry(tokenName, tokenCategory, index + indexOffset, floatValue, stringValue);
            entry.effect = effect;
            label.tokenEntries.add(entry);

            // Set new text without tokens
            m.setPosition(0);
            text = m.replaceFirst("");
        }

        // Update label text
        label.setText(text, false, false);
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
            label.tokenEntries.add(new TokenEntry("SKIP", TokenCategory.SKIP, index, 0, tag));
        }
    }

    /** Returns a float value parsed from the given String, or the default value if the string couldn't be parsed. */
    static float stringToFloat(String str, float defaultValue) {
        if(str != null) {
            try {
                return Float.parseFloat(str);
            } catch(Exception e) {
            }
        }
        return defaultValue;
    }

    /** Returns a boolean value parsed from the given String, or the default value if the string couldn't be parsed. */
    static boolean stringToBoolean(String str) {
        if(str != null) {
            for(String booleanTrue : BOOLEAN_TRUE) {
                if(booleanTrue.equalsIgnoreCase(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Parses a color from the given string. Returns null if the color couldn't be parsed. */
    static Color stringToColor(String str) {
        if(str != null) {

            // Try to parse named color
            Color namedColor = Colors.get(str.toUpperCase());
            if(namedColor != null) {
                return new Color(namedColor);
            }

            // Try to parse hex
            if(str.length() >= 6) {
                try {
                    return Color.valueOf(str);
                } catch(NumberFormatException ignored) {
                }
            }
        }

        return null;
    }

    /** Encloses the given string in brackets to work as a regular color markup tag. */
    private static String stringToColorMarkup(String str) {
        if(str != null) {
            // Upper case
            str = str.toUpperCase();

            // If color isn't registered by name, try to parse it as an hex code.
            Color namedColor = Colors.get(str);
            if(namedColor == null) {
                boolean isHexWithoutHashChar = str.length() >= 6 && PATTERN_COLOR_HEX_NO_HASH.matches(str);
                if(isHexWithoutHashChar) {
                    str = "#" + str;
                }
            }
        }

        // Return color code
        return "[" + str + "]";
    }

    /**
     * Returns a compiled {@link Pattern} that groups the token name in the first group and the params in an optional second one. Case
     * insensitive.
     */
    private static Pattern compileTokenPattern() {
        StringBuilder sb = new StringBuilder();
        sb.append("\\").append(CURRENT_DELIMITER.open).append("(");
        Array<String> tokens = new Array<>();
        TypingConfig.EFFECT_START_TOKENS.keys().toArray(tokens);
        TypingConfig.EFFECT_END_TOKENS.keys().toArray(tokens);
        for(InternalToken token : InternalToken.values()) {
            tokens.add(token.name);
        }
        for(int i = 0; i < tokens.size; i++) {
            sb.append(tokens.get(i));
            if((i + 1) < tokens.size) sb.append('|');
        }
        sb.append(")(?:=([;:?=^_ #-'*-.\\.\\w]+))?\\").append(CURRENT_DELIMITER.close);
        return Pattern.compile(sb.toString(), REFlags.IGNORE_CASE);
    }

    /** Returns the replacement string intended to be used on {RESET} tokens. */
    private static String getResetReplacement() {
        Array<String> tokens = new Array<>();
        TypingConfig.EFFECT_END_TOKENS.keys().toArray(tokens);
        tokens.add("CLEARCOLOR");
        tokens.add("NORMAL");

        StringBuilder sb = new StringBuilder();
        for(String token : tokens) {
            sb.append(CURRENT_DELIMITER.open).append(token).append(CURRENT_DELIMITER.close);
        }
        return sb.toString();
    }

}
