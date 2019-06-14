
package com.rafaskoberg.gdx.typinglabel.effects;

import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.FloatArray;
import com.rafaskoberg.gdx.typinglabel.Effect;
import com.rafaskoberg.gdx.typinglabel.TypingGlyph;
import com.rafaskoberg.gdx.typinglabel.TypingLabel;

/** Shakes the text in a random pattern. */
public class ShakeEffect extends Effect {
    private static final float DEFAULT_DISTANCE  = 0.12f;
    private static final float DEFAULT_INTENSITY = 0.5f;

    private final FloatArray lastOffsets = new FloatArray();

    private float distance  = 1; // How far the glyphs should move
    private float intensity = 1; // How fast the glyphs should move

    public ShakeEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance
        if(params.length > 0) {
            this.distance = paramAsFloat(params[0], 1);
        }

        // Intensity
        if(params.length > 1) {
            this.intensity = paramAsFloat(params[1], 1);
        }

        // Duration
        if(params.length > 2) {
            this.duration = paramAsFloat(params[2], -1);
        }
    }

    @Override
    protected void onApply(TypingGlyph glyph, int localIndex, float delta) {
        // Make sure we can hold enough entries for the current index
        if(localIndex >= lastOffsets.size / 2) {
            lastOffsets.setSize(lastOffsets.size + 16);
        }

        // Get last offsets
        float lastX = lastOffsets.get(localIndex * 2);
        float lastY = lastOffsets.get(localIndex * 2 + 1);

        // Calculate new offsets
        float x = getLineHeight() * distance * MathUtils.random(-1, 1) * DEFAULT_DISTANCE;
        float y = getLineHeight() * distance * MathUtils.random(-1, 1) * DEFAULT_DISTANCE;

        // Apply intensity
        float normalIntensity = MathUtils.clamp(intensity * DEFAULT_INTENSITY, 0, 1);
        x = Interpolation.linear.apply(lastX, x, normalIntensity);
        y = Interpolation.linear.apply(lastY, y, normalIntensity);

        // Apply fadeout
        float fadeout = calculateFadeout();
        x *= fadeout;
        y *= fadeout;
        x = Math.round(x);
        y = Math.round(y);

        // Store offsets for the next tick
        lastOffsets.set(localIndex * 2, x);
        lastOffsets.set(localIndex * 2 + 1, y);

        // Apply changes
        glyph.xoffset += x;
        glyph.yoffset += y;
    }

}
