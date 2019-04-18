
package com.rafaskoberg.gdx.typinglabel.effects;

import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.IntFloatMap;
import com.rafaskoberg.gdx.typinglabel.TypingLabel;

/** Moves the text vertically easing it into the final position. Doesn't repeat itself. */
public class EaseEffect extends Effect {
    private static final float DEFAULT_DISTANCE  = 0.15f;
    private static final float DEFAULT_INTENSITY = 0.075f;

    public float distance  = 1; // How much of their height they should move
    public float intensity = 1; // How fast the glyphs should move

    private IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public EaseEffect(TypingLabel label) {
        super(label);
    }

    @Override
    protected void onApply(Glyph glyph, int localIndex, float delta) {
        // Calculate progress
        float timePassed = timePassedByGlyphIndex.getAndIncrement(localIndex, 0, delta);
        float progress = timePassed / (intensity * DEFAULT_INTENSITY);
        if(progress < 0 || progress > 1) {
            return;
        }

        // Calculate offset
        float interpolation = Interpolation.pow2InInverse.apply(1, 0, progress);
        float y = getLineHeight() * distance * interpolation * DEFAULT_DISTANCE;

        // Apply changes
        glyph.yoffset += y;
    }

}
