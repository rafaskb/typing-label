
package com.rafaskoberg.gdx.typinglabel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.rafaskoberg.gdx.typinglabel.effects.BlinkEffect;
import com.rafaskoberg.gdx.typinglabel.effects.EaseEffect;
import com.rafaskoberg.gdx.typinglabel.effects.FadeEffect;
import com.rafaskoberg.gdx.typinglabel.effects.GradientEffect;
import com.rafaskoberg.gdx.typinglabel.effects.HangEffect;
import com.rafaskoberg.gdx.typinglabel.effects.JumpEffect;
import com.rafaskoberg.gdx.typinglabel.effects.RainbowEffect;
import com.rafaskoberg.gdx.typinglabel.effects.ShakeEffect;
import com.rafaskoberg.gdx.typinglabel.effects.SickEffect;
import com.rafaskoberg.gdx.typinglabel.effects.SlideEffect;
import com.rafaskoberg.gdx.typinglabel.effects.WaveEffect;
import com.rafaskoberg.gdx.typinglabel.effects.WindEffect;

/** Configuration class that easily allows the user to fine tune the library's functionality. */
public class TypingConfig {

    /**
     * Whether or not <a href="https://github.com/libgdx/libgdx/wiki/Color-Markup-Language">LibGDX's Color Markup
     * Language</a> should be enabled when parsing a {@link TypingLabel}. Note that this library doesn't truly handle
     * colors, but simply convert them to the markup format. If markup is disabled, color tokens will be ignored.
     */
    public static boolean FORCE_COLOR_MARKUP_BY_DEFAULT = true;

    /** Default time in seconds that an empty {@code WAIT} token should wait for. Default value is {@code 0.250}. */
    public static float DEFAULT_WAIT_VALUE = 0.250f;

    /** Time in seconds that takes for each char to appear in the default speed. Default value is {@code 0.035}. */
    public static float DEFAULT_SPEED_PER_CHAR = 0.035f;

    /**
     * Minimum value for the {@code SPEED} token. This value divides {@link #DEFAULT_SPEED_PER_CHAR} to calculate the
     * final speed. Keep it above zero. Default value is {@code 0.001}.
     */
    public static float MIN_SPEED_MODIFIER = 0.001f;

    /**
     * Maximum value for the {@code SPEED} token. This value divides {@link #DEFAULT_SPEED_PER_CHAR} to calculate the
     * final speed. Default value is {@code 100}.
     */
    public static float MAX_SPEED_MODIFIER = 100.0f;

    /**
     * Defines how many chars can appear per frame. Use a value less than {@code 1} to disable this limit. Default value
     * is {@code -1}.
     */
    public static int CHAR_LIMIT_PER_FRAME = -1;

    /** Default color for the {@code CLEARCOLOR} token. Can be overriden by {@link TypingLabel#getClearColor()}. */
    public static Color DEFAULT_CLEAR_COLOR = new Color(Color.WHITE);

    /** Characters used to start and end tokens. Defaults to {@link TokenDelimiter#CURLY_BRACKETS}.*/
    public static TokenDelimiter TOKEN_DELIMITER = TokenDelimiter.CURLY_BRACKETS;

    /**
     * Returns a map of characters and their respective interval multipliers, of which the interval to the next char
     * should be multiplied for.
     */
    public static ObjectFloatMap<Character> INTERVAL_MULTIPLIERS_BY_CHAR = new ObjectFloatMap<Character>();

    /** Map of global variables that affect all {@link TypingLabel} instances at once. */
    public static final ObjectMap<String, String> GLOBAL_VARS = new ObjectMap<>();

    /** Map of start tokens and their effect classes. Internal use only. */
    static final ObjectMap<String, Class<? extends Effect>> EFFECT_START_TOKENS = new ObjectMap<>();

    /** Map of end tokens and their effect classes. Internal use only. */
    static final ObjectMap<String, Class<? extends Effect>> EFFECT_END_TOKENS = new ObjectMap<>();

    /** Whether or not effect tokens are dirty and need to be recalculated. */
    static boolean dirtyEffectMaps = true;

    /**
     * Registers a new effect to TypingLabel.
     *
     * @param startTokenName Name of the token that starts the effect, such as WAVE.
     * @param endTokenName   Name of the token that ends the effect, such as ENDWAVE.
     * @param effectClass    Class of the effect, such as WaveEffect.class.
     */
    public static void registerEffect(String startTokenName, String endTokenName, Class<? extends Effect> effectClass) {
        EFFECT_START_TOKENS.put(startTokenName.toUpperCase(), effectClass);
        EFFECT_END_TOKENS.put(endTokenName.toUpperCase(), effectClass);
        dirtyEffectMaps = true;
    }

    /**
     * Unregisters an effect from TypingLabel.
     *
     * @param startTokenName Name of the token that starts the effect, such as WAVE.
     * @param endTokenName   Name of the token that ends the effect, such as ENDWAVE.
     */
    public static void unregisterEffect(String startTokenName, String endTokenName) {
        EFFECT_START_TOKENS.remove(startTokenName.toUpperCase());
        EFFECT_END_TOKENS.remove(endTokenName.toUpperCase());
    }

    static {
        // Generate default char intervals
        INTERVAL_MULTIPLIERS_BY_CHAR.put(' ', 0.0f);
        INTERVAL_MULTIPLIERS_BY_CHAR.put(':', 1.5f);
        INTERVAL_MULTIPLIERS_BY_CHAR.put(',', 2.5f);
        INTERVAL_MULTIPLIERS_BY_CHAR.put('.', 2.5f);
        INTERVAL_MULTIPLIERS_BY_CHAR.put('!', 5.0f);
        INTERVAL_MULTIPLIERS_BY_CHAR.put('?', 5.0f);
        INTERVAL_MULTIPLIERS_BY_CHAR.put('\n', 20f);

        // Register default tokens
        registerEffect("EASE", "ENDEASE", EaseEffect.class);
        registerEffect("HANG", "ENDHANG", HangEffect.class);
        registerEffect("JUMP", "ENDJUMP", JumpEffect.class);
        registerEffect("SHAKE", "ENDSHAKE", ShakeEffect.class);
        registerEffect("SICK", "ENDSICK", SickEffect.class);
        registerEffect("SLIDE", "ENDSLIDE", SlideEffect.class);
        registerEffect("WAVE", "ENDWAVE", WaveEffect.class);
        registerEffect("WIND", "ENDWIND", WindEffect.class);
        registerEffect("RAINBOW", "ENDRAINBOW", RainbowEffect.class);
        registerEffect("GRADIENT", "ENDGRADIENT", GradientEffect.class);
        registerEffect("FADE", "ENDFADE", FadeEffect.class);
        registerEffect("BLINK", "ENDBLINK", BlinkEffect.class);
    }

}
