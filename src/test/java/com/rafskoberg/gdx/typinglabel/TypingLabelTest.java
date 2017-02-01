
package com.rafskoberg.gdx.typinglabel;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.rafaskoberg.gdx.typinglabel.TypingConfig;
import com.rafaskoberg.gdx.typinglabel.TypingLabel;
import com.rafaskoberg.gdx.typinglabel.TypingListener;

public class TypingLabelTest extends ApplicationAdapter {
	Skin skin;
	Stage stage;
	SpriteBatch batch;
	TypingLabel label;
	TypingLabel labelEvent;
	TextButton buttonPause;
	TextButton buttonResume;
	TextButton buttonRestart;
	TextButton buttonSkip;

	@Override
	public void create () {
		adjustTypingConfigs();

		batch = new SpriteBatch();
		skin = new Skin(Gdx.files.internal("resources/uiskin.json"));
		skin.getAtlas().getTextures().iterator().next().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		float scale = 1;
		skin.getFont("default-font").getData().setScale(scale);
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);

		Table table = new Table();
		stage.addActor(table);
		table.setFillParent(true);

		label = createTypingLabel();

		labelEvent = new TypingLabel("", skin);
		labelEvent.setAlignment(Align.left, Align.center);
		labelEvent.pause();
		labelEvent.setVisible(false);

		buttonPause = new TextButton("Pause", skin);
		buttonPause.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				label.pause();
			}
		});

		buttonResume = new TextButton("Resume", skin);
		buttonResume.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				label.resume();
			}
		});

		buttonRestart = new TextButton("Restart", skin);
		buttonRestart.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				label.restart();
			}
		});

		buttonSkip = new TextButton("Skip", skin);
		buttonSkip.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				label.skipToTheEnd();
			}
		});

		table.pad(50f);
		table.debugCell();
		table.add(label).colspan(4).growX();
		table.row();
		table.add(labelEvent).colspan(4).align(Align.center);
		table.row().uniform().expand().growX().space(40).center();
		table.add(buttonPause, buttonResume, buttonRestart, buttonSkip);

		table.pack();
		Table.debugCellColor.set(Color.DARK_GRAY);
	}

	public void adjustTypingConfigs () {
		// Only allow two chars per frame
		TypingConfig.CHAR_LIMIT_PER_FRAME = 2;

		// Change color used by CLEARCOLOR token
		TypingConfig.DEFAULT_CLEAR_COLOR = Color.WHITE;

		// Force bitmap fonts to use color markup
		TypingConfig.FORCE_COLOR_MARKUP_BY_DEFAULT = true;
	}

	public TypingLabel createTypingLabel () {
		// Create text with tokens
		// @off
		final StringBuilder text = new StringBuilder()
			.append("{SLOWER}{COLOR=SCARLET} Welcome,{WAIT} {VAR=title}!")
			.append("{FAST}\n\n")
			.append("{RESET} This is a simple test to show you")
			.append("{COLOR=ROYAL} how to make dialogues {SLOW}fun again!")
			.append("{NORMAL}\n")
			.append("{NORMAL}{CLEARCOLOR} With this library you can control the flow of the text with")
			.append("{COLOR=#ff0000} tokens,{CLEARCOLOR}{WAIT=0.7}")
			.append("{NORMAL}\n")
			.append("{SPEED=2.50}{COLOR=LIME} making the text go really fast{WAIT}")
			.append("{SPEED=0.25}{COLOR=FOREST} or extremely slow.")
			.append("{NORMAL}\n")
			.append("{RESET} You can also wait for a second{WAIT=1} or two{WAIT=2},")
			.append("{COLOR=LIME} just to catch an event in code{EVENT=sample}!{WAIT}")
			.append("{NORMAL}\n\n")
			.append("{COLOR=GOLDENROD}{SLOWER} Imagine the possibilities! =D");
		// @on

		// Create label
		final TypingLabel label = new TypingLabel(text, skin);

		// Set variable replacements for the {VARIABLE} token
		label.setVariable("title", "brave programmer");

		// Finally parse tokens in the label text.
		label.parseTokens();

		// Set an event listener for when the {EVENT} token is reached and for the char progression ends.
		label.setTypingListener(new TypingListener() {
			@Override
			public void event (String event) {
				System.out.println("Event: " + event);

				labelEvent.restart("{FASTER}{COLOR=GRAY}Event:{WAIT=0.1}{COLOR=LIME} " + event);
				labelEvent.clearActions();
				// @off
				labelEvent.addAction(
					sequence(
						visible(true),
						alpha(0),
						alpha(1, 0.25f, Interpolation.pow2In),
						delay(0.5f),
						alpha(0, 2f, Interpolation.pow2)
					)
				);
				// @on
			}

			@Override
			public void end () {
				System.out.println("End");

				// Char progression ended, wait 2 seconds and restart label
				Task autoRestartTask = new Task() {
					@Override
					public void run () {
						label.restart();
					}
				};
				Timer.schedule(autoRestartTask, 3f);
			}
		});

		return label;
	}

	public void update (float delta) {
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
	}

	@Override
	public void render () {
		update(Gdx.graphics.getDeltaTime());

		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.draw();
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose () {
		stage.dispose();
		skin.dispose();
	}

	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "TypingLabel Test";
		config.width = 640;
		config.height = 360;
		config.depth = 16;
		config.fullscreen = false;
		config.resizable = false;
		config.foregroundFPS = 30;
		config.backgroundFPS = 5;

		new LwjglApplication(new TypingLabelTest(), config);
	}

}
