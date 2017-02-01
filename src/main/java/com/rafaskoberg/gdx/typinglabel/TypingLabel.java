
package com.rafaskoberg.gdx.typinglabel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.StringBuilder;

/** An extension of {@link Label} that progressively shows the text as if it was being typed in real time, and allows the use of
 * tokens in the following format: <tt>{TOKEN=PARAMETER}</tt>. */
public class TypingLabel extends Label {
	///////////////////////
	/// --- Members --- ///
	///////////////////////

	// Collections
	private final ObjectMap<String, String> variables = new ObjectMap<>();
	protected final Array<TokenEntry> tokenEntries = new Array<>();

	// Config
	private Color clearColor = new Color(TypingConfig.DEFAULT_CLEAR_COLOR);
	private TypingListener listener = null;
	boolean forceMarkupColor = TypingConfig.FORCE_COLOR_MARKUP_BY_DEFAULT;

	// Internal state
	private final StringBuilder originalText = new StringBuilder();
	private float textSpeed = TypingConfig.DEFAULT_SPEED_PER_CHAR;
	private float charCooldown = textSpeed;
	private int charIndex = -1;
	private boolean parsed = false;
	private boolean paused = false;
	private boolean ended = false;

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

	/** Skips the char progression to the end, showing the entire label. Useful for when users don't want to wait for too long. */
	public void skipToTheEnd () {
		charIndex += getText().length;
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
		textSpeed = TypingConfig.DEFAULT_SPEED_PER_CHAR;
		charCooldown = textSpeed;
		charIndex = -1;
		parsed = false;
		paused = false;
		ended = false;

		this.setText(newText);

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
		if (!ended && !paused) {
			if ((charCooldown -= delta) < 0.0f) {
				processCharProgression();
			}
		}
	}

	/** Proccess char progression according to current cooldown and process all tokens in the current index. */
	private void processCharProgression () {
		// Keep a counter of how many chars we're processing in this tick.
		int charCounter = 0;

		// Process chars while there's room for it
		while (charCooldown < 0.0f) {
			charIndex++;

			// Invalidate label layout, so the new character is passed to the BitmapFontCache
			invalidate();

			// If char progression is finished, notify listener and abort routine
			if (charIndex > getText().length) {
				if (listener != null) listener.end();
				ended = true;
				return;
			}

			// Get next character and calculate cooldown increment
			int safeIndex = MathUtils.clamp(charIndex - 1, 0, getText().length);
			Character ch = Character.valueOf(getText().charAt(safeIndex));
			float intervalMultiplier = TypingConfig.INTERVAL_MULTIPLIERS_BY_CHAR.get(ch, 1);
			charCooldown += textSpeed * intervalMultiplier;

			// Process tokens according to the current index
			while (tokenEntries.size > 0 && tokenEntries.peek().index == charIndex) {
				TokenEntry entry = tokenEntries.pop();
				switch (entry.token) {
				case WAIT:
					charCooldown += entry.floatValue;
					break;

				case FASTER:
				case FAST:
				case NORMAL:
				case SLOW:
				case SLOWER:
				case SPEED:
					textSpeed = entry.floatValue;
					break;

				case SKIP:
					if (entry.stringValue != null) {
						charIndex += entry.stringValue.length();
					}
					break;

				case EVENT:
					if (this.listener != null) {
						listener.event(entry.stringValue);
					}
					break;

				default:
					break;
				}
			}

			// Increment char counter and break loop if enough chars were processed
			charCounter++;
			int charLimit = TypingConfig.CHAR_LIMIT_PER_FRAME;
			if (charLimit > 0 && charCounter > charLimit) {
				charCooldown = textSpeed;
				break;
			}
		}
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
	public void setFontScale (float fontScaleX, float fontScaleY) {
		super.setFontScale(fontScaleX, fontScaleY);
		this.fontScaleChanged = true;
	}

	public void layout () {
		BitmapFontCache cache = getBitmapFontCache();
		StringBuilder text = getText();
		GlyphLayout layout = super.getGlyphLayout();
		int lineAlign = getLineAlign();
		int labelAlign = getLabelAlign();
		int charIndex = MathUtils.clamp(this.charIndex, 0, text.length);
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
		if (wrap || text.indexOf("\n") != -1) {
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
		} else {
			textWidth = width;
			textHeight = font.getData().capHeight;
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

		layout.setText(font, text, 0, charIndex, Color.WHITE, textWidth, lineAlign, wrap, ellipsis);
		cache.setText(layout, x, y);

		if (fontScaleChanged) font.getData().setScale(oldScaleX, oldScaleY);
	}

}
