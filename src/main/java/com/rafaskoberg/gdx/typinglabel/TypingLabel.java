
package com.rafaskoberg.gdx.typinglabel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.GlyphLayout.GlyphRun;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.StringBuilder;
import com.rafaskoberg.gdx.typinglabel.effects.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;

/** An extension of {@link Label} that progressively shows the text as if it was being typed in real time, and allows the use of
 * tokens in the following format: <tt>{TOKEN=PARAMETER}</tt>. */
public class TypingLabel extends Label {
	///////////////////////
	/// --- Members --- ///
	///////////////////////

	// Collections
	private final ObjectMap<String, String> variables = new ObjectMap<String, String>();
	protected final Array<TokenEntry> tokenEntries = new Array<TokenEntry>();

	// Config
	private Color clearColor = new Color(TypingConfig.DEFAULT_CLEAR_COLOR);
	private TypingListener listener = null;
	boolean forceMarkupColor = TypingConfig.FORCE_COLOR_MARKUP_BY_DEFAULT;

	// Internal state
	private final StringBuilder originalText = new StringBuilder();
	private final Array<Glyph> glyphCache = new Array<Glyph>();
	private final IntArray glyphRunCapacities = new IntArray();
	private final IntArray offsetCache = new IntArray();
	private final Array<Effect> activeEffects = new Array<Effect>();
	private float textSpeed = TypingConfig.DEFAULT_SPEED_PER_CHAR;
	private float charCooldown = textSpeed;
	private int rawCharIndex = -2; // All chars, including color codes
	private int glyphCharIndex = -1; // Only renderable chars, excludes color codes
	private int glyphCharCompensation = 0;
	private int cachedGlyphCharIndex = -1; // Last glyphCharIndex sent to the cache
	private float lastLayoutX = 0, lastLayoutY = 0;
	private boolean parsed = false;
	private boolean paused = false;
	private boolean ended = false;
	private boolean skipping = false;
	private boolean ignoringEvents = false;
	private boolean ignoringEffects = false;

	// Superclass mirroring
	boolean wrap;
	String ellipsis;
	float lastPrefHeight;
	boolean fontScaleChanged = false;

	////////////////////////////
	/// --- Constructors --- ///
	////////////////////////////

	public TypingLabel (CharSequence text, LabelStyle style) {
		super(text, style);
		saveOriginalText();
	}

	public TypingLabel (CharSequence text, Skin skin, String fontName, Color color) {
		super(text, skin, fontName, color);
		saveOriginalText();
	}

	public TypingLabel (CharSequence text, Skin skin, String fontName, String colorName) {
		super(text, skin, fontName, colorName);
		saveOriginalText();
	}

	public TypingLabel (CharSequence text, Skin skin, String styleName) {
		super(text, skin, styleName);
		saveOriginalText();
	}

	public TypingLabel (CharSequence text, Skin skin) {
		super(text, skin);
		saveOriginalText();
	}

	/////////////////////////////
	/// --- Text Handling --- ///
	/////////////////////////////

	/** Modifies the text of this label. If the char progression is already running, it's highly recommended to use
	 * {@link #restart(CharSequence)} instead. */
	@Override
	public void setText (CharSequence newText) {
		this.setText(newText, true);
	}

	/** Sets the text of this label.
	 * @param modifyOriginalText Flag determining if the original text should be modified as well. If {@code false}, only the
	 *           display text is changed while the original text is untouched.
	 * @see #restart(CharSequence) */
	protected void setText (CharSequence newText, boolean modifyOriginalText) {
		super.setText(newText);
		if (modifyOriginalText) saveOriginalText();
	}

	/** Similar to {@link #getText()}, but returns the original text with all the tokens unchanged. */
	public StringBuilder getOriginalText () {
		return originalText;
	}

	/** Copies the content of {@link #getText()} to the {@link StringBuilder} containing the original text with all tokens
	 * unchanged. */
	protected void saveOriginalText () {
		originalText.setLength(0);
		originalText.insert(0, this.getText());
		originalText.trimToSize();
	}

	/** Restores the original text with all tokens unchanged to this label. Make sure to call {@link #parseTokens()} to parse the
	 * tokens again. */
	protected void restoreOriginalText () {
		super.setText(originalText);
		this.parsed = false;
	}

	////////////////////////////
	/// --- External API --- ///
	////////////////////////////

	/** Returns the {@link TypingListener} associated with this label. May be {@code null}. */
	public TypingListener getTypingListener () {
		return listener;
	}

	/** Sets the {@link TypingListener} associated with this label, or {@code null} to remove the current one. */
	public void setTypingListener (TypingListener listener) {
		this.listener = listener;
	}

	/** Returns a {@link Color} instance with the color to be used on {@code CLEARCOLOR} tokens. Modify this instance to change the
	 * token color. Default value is specified by {@link TypingConfig}.
	 * @see TypingConfig#DEFAULT_CLEAR_COLOR */
	public Color getClearColor () {
		return clearColor;
	}

	/** Sets whether or not this instance should enable markup color by force.
	 * @see TypingConfig#FORCE_COLOR_MARKUP_BY_DEFAULT */
	public void setForceMarkupColor (boolean forceMarkupColor) {
		this.forceMarkupColor = forceMarkupColor;
	}

	/** Parses all tokens of this label. Use this after setting the text and any variables that should be replaced. */
	public void parseTokens () {
		Parser.parseTokens(this);
		parsed = true;
	}

	/** Skips the char progression to the end, showing the entire label. Useful for when users don't want to wait for too long.
	 * Ignores all subsequent events by default. */
	public void skipToTheEnd () {
		skipToTheEnd(true);
	}

	/** Skips the char progression to the end, showing the entire label. Useful for when users don't want to wait for too long.
	 * @param ignoreEvents If {@code true}, skipped events won't be reported to the listener. */
	public void skipToTheEnd (boolean ignoreEvents) {
		skipToTheEnd(ignoreEvents, false);
	}

	/** Skips the char progression to the end, showing the entire label. Useful for when users don't want to wait for too long.
	 * @param ignoreEvents If {@code true}, skipped events won't be reported to the listener.
	 * @param ignoreEffects If {@code true}, all text effects will be instantly cancelled. */
	public void skipToTheEnd (boolean ignoreEvents, boolean ignoreEffects) {
		skipping = true;
		ignoringEvents = ignoreEvents;
		ignoringEffects = ignoreEffects;
	}

	/** Cancels calls to {@link #skipToTheEnd()}. Useful if you need to restore the label's normal behavior at some event after
	 * skipping. */
	public void cancelSkipping () {
		if (skipping) {
			skipping = false;
			ignoringEvents = false;
			ignoringEffects = false;
		}
	}

	/** Returns whether or not this label is paused. */
	public boolean isPaused () {
		return paused;
	}

	/** Pauses this label's character progression. */
	public void pause () {
		paused = true;
	}

	/** Resumes this label's character progression. */
	public void resume () {
		paused = false;
	}

	/** Returns whether or not this label's char progression has ended. */
	public boolean hasEnded () {
		return ended;
	}

	/** Restarts this label with the original text and starts the char progression right away. All tokens are automatically
	 * parsed. */
	public void restart () {
		restart(getOriginalText());
	}

	/** Restarts this label with the given text and starts the char progression right away. All tokens are automatically parsed. */
	public void restart (CharSequence newText) {
		// Reset cache collections
		GlyphUtils.freeAll(glyphCache);
		glyphCache.clear();
		glyphRunCapacities.clear();
		offsetCache.clear();
		activeEffects.clear();

		// Reset state
		textSpeed = TypingConfig.DEFAULT_SPEED_PER_CHAR;
		charCooldown = textSpeed;
		rawCharIndex = -2;
		glyphCharIndex = -1;
		glyphCharCompensation = 0;
		cachedGlyphCharIndex = -1;
		lastLayoutX = 0;
		lastLayoutY = 0;
		parsed = false;
		paused = false;
		ended = false;
		skipping = false;
		ignoringEvents = false;
		ignoringEffects = false;

		// Set new text
		this.setText(newText);
		invalidate();

		// Parse tokens
		tokenEntries.clear();
		parseTokens();
	}

	/** Returns an {@link ObjectMap} with all the variable names and their respective replacement values. */
	public ObjectMap<String, String> getVariables () {
		return variables;
	}

	/** Registers a variable and its respective replacement value to this label. */
	public void setVariable (String var, String value) {
		variables.put(var.toUpperCase(), value);
	}

	/** Registers a set of variables and their respective replacement values to this label. */
	public void setVariables (ObjectMap<String, String> variableMap) {
		this.variables.clear();
		for (Entry<String, String> entry : variableMap.entries()) {
			this.variables.put(entry.key.toUpperCase(), entry.value);
		}
	}

	/** Registers a set of variables and their respective replacement values to this label. */
	public void setVariables (java.util.Map<String, String> variableMap) {
		this.variables.clear();
		for (java.util.Map.Entry<String, String> entry : variableMap.entrySet()) {
			this.variables.put(entry.getKey().toUpperCase(), entry.getValue());
		}
	}

	/** Removes all variables from this label. */
	public void clearVariables () {
		this.variables.clear();
	}

	//////////////////////////////////
	/// --- Core Functionality --- ///
	//////////////////////////////////

	@Override
	public void act (float delta) {
		super.act(delta);

		// Force token parsing
		if (!parsed) {
			parseTokens();
		}

		// Update cooldown and process char progression
		if (skipping || (!ended && !paused)) {
			if (skipping || (charCooldown -= delta) < 0.0f) {
				processCharProgression();
			}
		}

		// Restore glyph offsets
		if (activeEffects.size > 0) {
			for (int i = 0; i < glyphCache.size; i++) {
				Glyph glyph = glyphCache.get(i);
				glyph.xoffset = offsetCache.get(i * 2);
				glyph.yoffset = offsetCache.get(i * 2 + 1);
			}
		}

		// Apply effects
		if (!ignoringEffects) {
			for (int i = activeEffects.size - 1; i >= 0; i--) {
				Effect effect = activeEffects.get(i);
				effect.update(delta);
				int start = effect.indexStart;
				int end = effect.indexEnd >= 0 ? effect.indexEnd : glyphCharIndex;

				// If effect is finished, remove it
				if (effect.isFinished()) {
					activeEffects.removeIndex(i);
					continue;
				}

				// Apply effect to glyph
				for (int j = Math.max(0, start); j <= glyphCharIndex && j <= end && j < glyphCache.size; j++) {
					Glyph glyph = glyphCache.get(j);
					effect.apply(glyph, j);
				}
			}
		}
	}

	/** Proccess char progression according to current cooldown and process all tokens in the current index. */
	private void processCharProgression () {
		// Keep a counter of how many chars we're processing in this tick.
		int charCounter = 0;

		// Process chars while there's room for it
		while (skipping || charCooldown < 0.0f) {
			// Apply compensation to glyph index, if any
			if (glyphCharCompensation > 0) {
				glyphCharIndex++;
				glyphCharCompensation--;

				// Increment cooldown and wait for it
				charCooldown += textSpeed;
				continue;
			}

			// Increase raw char index
			rawCharIndex++;

			// Get next character and calculate cooldown increment
			int safeIndex = MathUtils.clamp(rawCharIndex - 1, 0, getText().length - 1);
			char primitiveChar = '\u0000'; // Null character by default
			if (getText().length > 0) {
				primitiveChar = getText().charAt(safeIndex);
				Character ch = Character.valueOf(primitiveChar);
				float intervalMultiplier = TypingConfig.INTERVAL_MULTIPLIERS_BY_CHAR.get(ch, 1);
				charCooldown += textSpeed * intervalMultiplier;
			}

			// If char progression is finished, or if text is empty, notify listener and abort routine
			int textLen = getText().length;
			if (textLen == 0 || rawCharIndex >= textLen) {
				if (!ended) {
					ended = true;
					skipping = false;
					if (listener != null) listener.end();
				}
				return;
			}

			// Increase glyph char index for all characters, except new lines.
			if (rawCharIndex >= 0 && primitiveChar != '\n') glyphCharIndex++;

			// Process tokens according to the current index
			while (tokenEntries.size > 0 && tokenEntries.peek().index == rawCharIndex) {
				TokenEntry entry = tokenEntries.pop();
				Token token = entry.token;

				// Speed
				if (token.isSpeed()) {
					textSpeed = entry.floatValue;
					continue;
				}

				// Wait
				if (token == Token.WAIT) {
					glyphCharIndex--;
					glyphCharCompensation++;
					charCooldown += entry.floatValue;
					continue;
				}

				// Skip
				if (token == Token.SKIP) {
					if (entry.stringValue != null) {
						rawCharIndex += entry.stringValue.length();
					}
					continue;
				}

				// Event
				if (token == Token.EVENT) {
					if (this.listener != null && !ignoringEvents) {
						listener.event(entry.stringValue);
					}
					continue;
				}

				// Effects
				if (token.isEffect()) {
					// Get effect class
					Class<? extends Effect> effectClass = null;
					switch (token) {
					case SHAKE:
					case ENDSHAKE:
						effectClass = ShakeEffect.class;
						break;
					case SICK:
					case ENDSICK:
						effectClass = SickEffect.class;
						break;
					case WAVE:
					case ENDWAVE:
						effectClass = WaveEffect.class;
						break;
					case JUMP:
					case ENDJUMP:
						effectClass = JumpEffect.class;
						break;
					default:
						break;
					}

					// End all effects of the same type
					for (int i = 0; i < activeEffects.size; i++) {
						Effect effect = activeEffects.get(i);
						if (effect.indexEnd < 0) {
							if (ClassReflection.isAssignableFrom(effectClass.getClass(), effect.getClass())) {
								effect.indexEnd = glyphCharIndex - 1;
							}
						}
					}

					// Create new effect if necessary
					if (token.isEffectStart()) {
						entry.effect.indexStart = glyphCharIndex;
						activeEffects.add(entry.effect);
					}

					continue;
				}
			}

			// Notify listener about char progression
			if (listener != null) {
				if (rawCharIndex > 0) {
					int nextIndex = MathUtils.clamp(rawCharIndex, 0, getText().length - 1);
					char nextPrimitiveChar = getText().charAt(nextIndex);
					Character nextChar = Character.valueOf(nextPrimitiveChar);
					listener.onChar(nextChar);
				}
			}

			// Increment char counter and break loop if enough chars were processed
			charCounter++;
			int charLimit = TypingConfig.CHAR_LIMIT_PER_FRAME;
			if (!skipping && charLimit > 0 && charCounter > charLimit) {
				charCooldown = textSpeed;
				break;
			}
		}
	}

	@Override
	public boolean remove () {
		GlyphUtils.freeAll(glyphCache);
		glyphCache.clear();
		return super.remove();
	}

	////////////////////////////////////
	/// --- Superclass Mirroring --- ///
	////////////////////////////////////

	@Override
	public BitmapFontCache getBitmapFontCache () {
		return super.getBitmapFontCache();
	}

	@Override
	public void setEllipsis (String ellipsis) {
		// Mimics superclass but keeps an accessible reference
		super.setEllipsis(ellipsis);
		this.ellipsis = ellipsis;
	}

	@Override
	public void setEllipsis (boolean ellipsis) {
		// Mimics superclass but keeps an accessible reference
		super.setEllipsis(ellipsis);
		if (ellipsis)
			this.ellipsis = "...";
		else
			this.ellipsis = null;
	}

	@Override
	public void setWrap (boolean wrap) {
		// Mimics superclass but keeps an accessible reference
		super.setWrap(wrap);
		this.wrap = wrap;
	}

	@Override
	public void setFontScale (float fontScale) {
		super.setFontScale(fontScale);
		this.fontScaleChanged = true;
	}

	@Override
	public void setFontScale (float fontScaleX, float fontScaleY) {
		super.setFontScale(fontScaleX, fontScaleY);
		this.fontScaleChanged = true;
	}

	@Override
	public void setFontScaleX (float fontScaleX) {
		super.setFontScaleX(fontScaleX);
		this.fontScaleChanged = true;
	}

	@Override
	public void setFontScaleY (float fontScaleY) {
		super.setFontScaleY(fontScaleY);
		this.fontScaleChanged = true;
	}

	@Override
	public void layout () {
		// --- SUPERCLASS IMPLEMENTATION (but with accessible getters instead) ---
		BitmapFontCache cache = getBitmapFontCache();
		StringBuilder text = getText();
		GlyphLayout layout = super.getGlyphLayout();
		int lineAlign = getLineAlign();
		int labelAlign = getLabelAlign();
		LabelStyle style = getStyle();

		BitmapFont font = cache.getFont();
		float oldScaleX = font.getScaleX();
		float oldScaleY = font.getScaleY();
		if (fontScaleChanged) font.getData().setScale(getFontScaleX(), getFontScaleY());

		boolean wrap = this.wrap && ellipsis == null;
		if (wrap) {
			float prefHeight = getPrefHeight();
			if (prefHeight != lastPrefHeight) {
				lastPrefHeight = prefHeight;
				invalidateHierarchy();
			}
		}

		float width = getWidth(), height = getHeight();
		Drawable background = style.background;
		float x = 0, y = 0;
		if (background != null) {
			x = background.getLeftWidth();
			y = background.getBottomHeight();
			width -= background.getLeftWidth() + background.getRightWidth();
			height -= background.getBottomHeight() + background.getTopHeight();
		}

		float textWidth, textHeight;
		// if (wrap || text.indexOf("\n") != -1)
		{
			// If the text can span multiple lines, determine the text's actual size so it can be aligned within the label.
			layout.setText(font, text, 0, text.length, Color.WHITE, width, lineAlign, wrap, ellipsis);
			textWidth = layout.width;
			textHeight = layout.height;

			if ((labelAlign & Align.left) == 0) {
				if ((labelAlign & Align.right) != 0)
					x += width - textWidth;
				else
					x += (width - textWidth) / 2;
			}
			// } else {
			// textWidth = width;
			// textHeight = font.getData().capHeight;
		}

		if ((labelAlign & Align.top) != 0) {
			y += cache.getFont().isFlipped() ? 0 : height - textHeight;
			y += style.font.getDescent();
		} else if ((labelAlign & Align.bottom) != 0) {
			y += cache.getFont().isFlipped() ? height - textHeight : 0;
			y -= style.font.getDescent();
		} else {
			y += (height - textHeight) / 2;
		}
		if (!cache.getFont().isFlipped()) y += textHeight;

		// Don't set the layout or cache now, since we progressively update both over time.
		// layout.setText(font, text, 0, text.length, Color.WHITE, textWidth, lineAlign, wrap, ellipsis);
		// cache.setText(layout, x, y);
		if (fontScaleChanged) font.getData().setScale(oldScaleX, oldScaleY);

		// --- END OF SUPERCLASS IMPLEMENTATION ---

		// Store coordinates passed to BitmapFontCache
		lastLayoutX = x;
		lastLayoutY = y;

		// Perform cache layout operation, where the magic happens
		GlyphUtils.freeAll(glyphCache);
		glyphCache.clear();
		layoutCache();
	}

	/** Reallocate glyph clones according to the updated {@link GlyphLayout}. This should only be called when the text or the
	 * layout changes. */
	private void layoutCache () {
		BitmapFontCache cache = getBitmapFontCache();
		GlyphLayout layout = super.getGlyphLayout();
		Array<GlyphRun> runs = layout.runs;

		// Store GlyphRun sizes and count how many glyphs we have
		int glyphCount = 0;
		glyphRunCapacities.setSize(runs.size);
		for (int i = 0; i < runs.size; i++) {
			Array<Glyph> glyphs = runs.get(i).glyphs;
			glyphRunCapacities.set(i, glyphs.size);
			glyphCount += glyphs.size;
		}

		// Make sure our cache array can hold all glyphs
		if (glyphCache.size < glyphCount) {
			glyphCache.setSize(glyphCount);
			offsetCache.setSize(glyphCount * 2);
		}

		// Clone original glyphs with independent instances
		int index = -1;
		for (int i = 0; i < runs.size; i++) {
			Array<Glyph> glyphs = runs.get(i).glyphs;
			for (int j = 0; j < glyphs.size; j++) {
				index++;

				// Get original glyph
				Glyph original = glyphs.get(j);

				// Get clone glyph
				Glyph clone = null;
				if (index < glyphCache.size) {
					clone = glyphCache.get(index);
				}
				if (clone == null) {
					clone = GlyphUtils.obtain();
					glyphCache.set(index, clone);
				}
				GlyphUtils.clone(original, clone);
				clone.width *= getFontScaleX();
				clone.height *= getFontScaleY();
				clone.xoffset *= getFontScaleX();
				clone.yoffset *= getFontScaleY();

				// Store offset data
				offsetCache.set(index * 2, clone.xoffset);
				offsetCache.set(index * 2 + 1, clone.yoffset);

				// Replace glyph in original array
				glyphs.set(j, clone);
			}
		}

		// Remove exceeding glyphs from original array
		int glyphCountdown = glyphCharIndex;
		for (int i = 0; i < runs.size; i++) {
			Array<Glyph> glyphs = runs.get(i).glyphs;
			if (glyphs.size < glyphCountdown) {
				glyphCountdown -= glyphs.size;
				continue;
			}

			for (int j = 0; j < glyphs.size; j++) {
				if (glyphCountdown < 0) {
					glyphs.removeRange(j, glyphs.size - 1);
					break;
				}
				glyphCountdown--;
			}
		}

		// Pass new layout with custom glyphs to BitmapFontCache
		cache.setText(layout, lastLayoutX, lastLayoutY);
	}

	/** Adds cached glyphs to the active BitmapFontCache as the char index progresses. */
	private void addMissingGlyphs () {
		// Add additional glyphs to layout array, if any
		int glyphLeft = glyphCharIndex - cachedGlyphCharIndex;
		if (glyphLeft < 1) return;

		// Get runs
		GlyphLayout layout = super.getGlyphLayout();
		Array<GlyphRun> runs = layout.runs;

		// Iterate through GlyphRuns to find the next glyph spot
		int glyphCount = 0;
		for (int runIndex = 0; runIndex < glyphRunCapacities.size; runIndex++) {
			int runCapacity = glyphRunCapacities.get(runIndex);
			if ((glyphCount + runCapacity) < cachedGlyphCharIndex) {
				glyphCount += runCapacity;
				continue;
			}

			// Get run and increase glyphCount up to its current size
			Array<Glyph> glyphs = runs.get(runIndex).glyphs;
			glyphCount += glyphs.size;

			// Next glyphs go here
			while (glyphLeft > 0) {

				// Skip run if this one is full
				int runSize = glyphs.size;
				if (runCapacity == runSize) {
					break;
				}

				// Put new glyph to this run
				cachedGlyphCharIndex++;
				glyphCount++;
				glyphLeft--;
				glyphs.add(glyphCache.get(cachedGlyphCharIndex));
			}
		}
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		super.validate();
		addMissingGlyphs();

		// Update cache with new glyphs
		// Check if we have special effects, otherwise only run this when necessary
		getBitmapFontCache().setText(getGlyphLayout(), lastLayoutX, lastLayoutY);

		super.draw(batch, parentAlpha);
	}

}
