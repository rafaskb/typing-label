
package com.rafaskoberg.gdx.typinglabel.effects;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntFloatMap;
import com.rafaskoberg.gdx.typinglabel.Effect;
import com.rafaskoberg.gdx.typinglabel.TypingGlyph;
import com.rafaskoberg.gdx.typinglabel.TypingLabel;

/** Hangs the text in midair and suddenly drops it. Doesn't repeat itself. */
public class HangEffect extends Effect {
    private static final float DEFAULT_DISTANCE  = 0.7f;
    private static final float DEFAULT_INTENSITY = 1.5f;

    private float distance  = 1; // How much of their height they should move
    private float intensity = 1; // How fast the glyphs should move

    private IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public HangEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance
        if(params.length > 0) {
            this.distance = paramAsFloat(params[0], 1);
        }

        // Intensity
        if(params.length > 1) {
            this.intensity = paramAsFloat(params[1], 1);
        }
    }

    @Override
    protected void onApply(TypingGlyph glyph, int localIndex, float delta) {
        // Calculate real intensity
        float realIntensity = intensity * 1f * DEFAULT_INTENSITY;

        // Calculate progress
        float timePassed = timePassedByGlyphIndex.getAndIncrement(localIndex, 0, delta);
        float progress = MathUtils.clamp(timePassed / realIntensity, 0, 1);

        // Calculate offset
        float interpolation;
        float split = 0.7f;
        if(progress < split) {
            interpolation = Interpolation.pow3Out.apply(0, 1, progress / split);
        } else {
            interpolation = Interpolation.swing.apply(1, 0, (progress - split) / (1f - split));
        }
        float distanceFactor = Interpolation.linear.apply(1.0f, 1.5f, progress);
        float y = getLineHeight() * distance * distanceFactor * interpolation * DEFAULT_DISTANCE;

        // Calculate fadeout
        float fadeout = calculateFadeout();
        y *= fadeout;

        // Apply changes
        glyph.yoffset += y;
    }

}
