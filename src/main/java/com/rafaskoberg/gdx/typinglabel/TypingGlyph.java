package com.rafaskoberg.gdx.typinglabel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.GlyphLayout.GlyphRun;

/** Extension of {@link Glyph} with additional data exposed to the user. */
public class TypingGlyph extends Glyph {

    /** The color of the run this glyph belongs to, as an ABGR8888 int. */
    public int runColor = 0xFFFFFFFF;

    /** Internal index associated with this glyph. Internal use only. Defaults to -1. */
    int internalIndex = -1;

    /** Color of this glyph. If set to null, the run's color will be used. Defaults to null. */
    public Color color = null;

}
