package com.rafaskoberg.gdx.typinglabel;

import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.g2d.GlyphLayout.GlyphRun;

/** Extension of {@link Glyph} with additional data exposed to the user. */
public class TypingGlyph extends Glyph {
    public GlyphRun run;
}
