
package com.rafaskoberg.gdx.typinglabel.effects;

import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.IntArray;
import com.rafaskoberg.gdx.typinglabel.TypingLabel;

/** Drips the text in a random pattern. */
public class SickEffect extends Effect {
	private static final float DEFAULT_FREQUENCY = 50f;
	private static final float DEFAULT_DISTANCE = .125f;
	private static final float DEFAULT_INTENSITY = 1f;

	public float distance = 1; // How far the glyphs should move
	public float frequency = 1; // How frequently the wave pattern repeats
	public float intensity = 1; // How fast the glyphs should move

    private IntArray indices = new IntArray();

	public SickEffect(TypingLabel label) {
		super(label);
	}

	@Override
	protected void onApply (Glyph glyph, int localIndex) {

		// Calculate progress
		float progressModifier = (1f / intensity) * DEFAULT_INTENSITY;
		float normalFrequency = (1f / frequency) * DEFAULT_FREQUENCY;
		float progressOffset = localIndex / normalFrequency;
		float progress = calculateProgress(progressModifier, -progressOffset, false);

		if(progress < .01f && Math.random() > .25f && !indices.contains(localIndex))
			indices.add(localIndex);
		if(progress > .95f)
			indices.removeValue(localIndex);

		if(!indices.contains(localIndex) &&
				!indices.contains(localIndex - 1) &&
				!indices.contains(localIndex - 2) &&
				!indices.contains(localIndex + 2) &&
				!indices.contains(localIndex + 1))
			return;

		// Calculate offset
		float interpolation = 0;
		float split = 0.5f;
		if (progress < split) {
			interpolation = Interpolation.pow2Out.apply(0, 1, progress / split);
		} else {
			interpolation = Interpolation.pow2In.apply(1, 0, (progress - split) / (1f - split));
		}
		float y = getLineHeight() * distance * interpolation * DEFAULT_DISTANCE;

		if(indices.contains(localIndex))
			y *= 2.15f;
		if(indices.contains(localIndex - 1) || indices.contains(localIndex + 1))
			y *= 1.35f;

		// Calculate fadeout
		float fadeout = calculateFadeout();
		y *= fadeout;

		// Apply changes
		glyph.yoffset -= y;
	}

}
