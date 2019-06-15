
package com.rafaskoberg.gdx.typinglabel.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntFloatMap;
import com.rafaskoberg.gdx.typinglabel.Effect;
import com.rafaskoberg.gdx.typinglabel.TypingGlyph;
import com.rafaskoberg.gdx.typinglabel.TypingLabel;

/** Fades the text's color from between colors or alphas. Doesn't repeat itself. */
public class FadeEffect extends Effect {
    private Color color1       = null; // First color of the effect.
    private Color color2       = null; // Second color of the effect.
    private float alpha1       = 1; // First alpha of the effect, in case a color isn't provided.
    private float alpha2       = 1; // Second alpha of the effect, in case a color isn't provided.
    private float fadeDuration = 1; // Duration of the fade effect

    private IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public FadeEffect(TypingLabel label, String[] params) {
        super(label);

        // Color 1 or Alpha 1
        if(params.length > 0) {
            this.color1 = paramAsColor(params[0]);
            if(this.color1 == null) {
                alpha1 = paramAsFloat(params[0], 1);
            }
        }

        // Color 2 or Alpha 2
        if(params.length > 1) {
            this.color2 = paramAsColor(params[1]);
            if(this.color2 == null) {
                alpha2 = paramAsFloat(params[1], 1);
            }
        }

        // Fade duration
        if(params.length > 2) {
            this.fadeDuration = paramAsFloat(params[2], 1);
        }
    }

    @Override
    protected void onApply(TypingGlyph glyph, int localIndex, float delta) {
        // Calculate progress
        float timePassed = timePassedByGlyphIndex.getAndIncrement(localIndex, 0, delta);
        float progress = timePassed / fadeDuration;
        if(progress < 0 || progress > 1) {
            return;
        }

        // Create glyph color if necessary
        if(glyph.color == null) {
            glyph.color = new Color(glyph.run.color);
        }

        // Calculate initial color
        if(this.color1 == null) {
            glyph.color.a = MathUtils.lerp(glyph.color.a, this.alpha1, 1f - progress);
        } else {
            glyph.color.lerp(this.color1, 1f - progress);
        }

        // Calculate final color
        if(this.color2 == null) {
            glyph.color.a = MathUtils.lerp(glyph.color.a, this.alpha2, progress);
        } else {
            glyph.color.lerp(this.color2, progress);
        }
    }

}
