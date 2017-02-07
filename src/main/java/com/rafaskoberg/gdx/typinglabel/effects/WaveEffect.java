
package com.rafaskoberg.gdx.typinglabel.effects;

import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.math.Interpolation;
import com.rafaskoberg.gdx.typinglabel.TypingLabel;

/** Moves the text vertically in a sine wave pattern. */
public class WaveEffect extends Effect {
	private static final float DEFAULT_FREQUENCY = 15f;
	private static final float DEFAULT_DISTANCE = 0.33f;
	private static final float DEFAULT_INTENSITY = 0.5f;

	public float distance = 1; // How much of their height they should move
	public float frequency = 1; // How frequently the wave pattern repeats
	public float intensity = 1; // How fast the glyphs should move

	public WaveEffect (TypingLabel label) {
		super(label);
	}

	@Override
	protected void onApply (Glyph glyph, int localIndex) {
		// Calculate progress
		float progressModifier = (1f / intensity) * DEFAULT_INTENSITY;
		float normalFrequency = (1f / frequency) * DEFAULT_FREQUENCY;
		float progressOffset = localIndex / normalFrequency;
		float progress = calculateProgress(progressModifier, progressOffset);

		// Calculate offset
		float y = getLineHeight() * distance * Interpolation.sine.apply(-1, 1, progress) * DEFAULT_DISTANCE;

		// Calculate fadeout
		float fadeout = calculateFadeout();
		y *= fadeout;

		// Apply changes
		glyph.yoffset += y;
	}

}
