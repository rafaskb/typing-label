
package com.rafaskoberg.gdx.typinglabel.effects;

import com.rafaskoberg.gdx.typinglabel.Effect;
import com.rafaskoberg.gdx.typinglabel.TypingGlyph;
import com.rafaskoberg.gdx.typinglabel.TypingLabel;
import com.rafaskoberg.gdx.typinglabel.utils.SimplexNoise;

/** Moves the text in a wind pattern. */
public class WindEffect extends Effect {
    private static final float DEFAULT_SPACING   = 10f;
    private static final float DEFAULT_DISTANCE  = 0.33f;
    private static final float DEFAULT_INTENSITY = 0.375f;
    private static final float DISTANCE_X_RATIO  = 1.5f;
    private static final float DISTANCE_Y_RATIO  = 1.0f;
    private static final float IDEAL_DELTA       = 1 / 60f;

    private SimplexNoise noise        = new SimplexNoise(6, 0, 1f);
    private float        noiseCursorX = 0;
    private float        noiseCursorY = 0;

    private float distanceX = 1; // How much of their line height glyphs should move in the X axis
    private float distanceY = 1; // How much of their line height glyphs should move in the Y axis
    private float spacing   = 1; // How much space there should be between waves
    private float intensity = 1; // How strong the wind should be

    public WindEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance X
        if(params.length > 0) {
            this.distanceX = paramAsFloat(params[0], 1);
        }

        // Distance Y
        if(params.length > 1) {
            this.distanceY = paramAsFloat(params[1], 1);
        }

        // Spacing
        if(params.length > 2) {
            this.spacing = paramAsFloat(params[2], 1);
        }

        // Intensity
        if(params.length > 3) {
            this.intensity = paramAsFloat(params[3], 1);
        }

        // Duration
        if(params.length > 4) {
            this.duration = paramAsFloat(params[4], -1);
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Update noise cursor
        float deltaFactor = delta / IDEAL_DELTA;
        noiseCursorX += 0.1f * intensity * DEFAULT_INTENSITY * deltaFactor;
        noiseCursorY += 0.1f * intensity * DEFAULT_INTENSITY * deltaFactor;
    }

    @Override
    protected void onApply(TypingGlyph glyph, int localIndex, float delta) {
        // Calculate progress
        float progressModifier = (1f / intensity) * DEFAULT_INTENSITY;
        float normalSpacing = (1f / spacing) * DEFAULT_SPACING;
        float progressOffset = localIndex / normalSpacing;
        float progress = calculateProgress(progressModifier, progressOffset);

        // Calculate noise
        float indexOffset = localIndex * 0.05f * spacing;
        float noiseX = noise.getNoise(noiseCursorX + indexOffset, 0);
        float noiseY = noise.getNoise(0, noiseCursorY + indexOffset);

        // Calculate offset
        float lineHeight = getLineHeight();
        float x = lineHeight * noiseX * progress * distanceX * DISTANCE_X_RATIO * DEFAULT_DISTANCE;
        float y = lineHeight * noiseY * progress * distanceY * DISTANCE_Y_RATIO * DEFAULT_DISTANCE;

        // Calculate fadeout
        float fadeout = calculateFadeout();
        x *= fadeout;
        y *= fadeout;

        // Add flag effect to X offset
        x = Math.abs(x) * -Math.signum(distanceX);

        // Apply changes
        glyph.xoffset += x;
        glyph.yoffset += y;
    }

}
