package com.rafaskoberg.gdx.typinglabel;

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
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class TypingLabelTest extends ApplicationAdapter {
    Skin        skin;
    Stage       stage;
    SpriteBatch batch;
    TypingLabel label;
    TypingLabel labelEvent;
    TextButton  buttonPause;
    TextButton  buttonResume;
    TextButton  buttonRestart;
    TextButton  buttonRebuild;
    TextButton  buttonSkip;

    @Override
    public void create() {
        // Adjust typing config
        adjustTypingConfigs();

        // Initiate batch, skin, and stage
        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        skin.getAtlas().getTextures().iterator().next().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        float scale = 1;
        skin.getFont("default-font").getData().setScale(scale);
        stage = new Stage(new ScreenViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        // Create root table
        final Table table = new Table();
        stage.addActor(table);
        table.setFillParent(true);

        // Create main TypingLabel instance
        final String filename = "default.txt";
        label = createTypingLabel(filename);

        // Create TypingLabel to show events
        labelEvent = new TypingLabel("", skin);
        labelEvent.setAlignment(Align.left, Align.center);
        labelEvent.pause();
        labelEvent.setVisible(false);

        // Pause button
        buttonPause = new TextButton("Pause", skin);
        buttonPause.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                label.pause();
            }
        });

        // Resume button
        buttonResume = new TextButton("Resume", skin);
        buttonResume.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                label.resume();
            }
        });

        // Restart button
        buttonRestart = new TextButton("Restart", skin);
        buttonRestart.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                label.restart();
            }
        });

        // Rebuild button
        buttonRebuild = new TextButton("Rebuild", skin);
        buttonRebuild.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                adjustTypingConfigs();
                Cell<TypingLabel> labelCell = table.getCell(label);
                label = createTypingLabel(filename);
                labelCell.setActor(label);
            }
        });

        // Skip button
        buttonSkip = new TextButton("Skip", skin);
        buttonSkip.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                label.skipToTheEnd();
            }
        });

        // Populate table
        table.pad(50f);
        table.add(label).colspan(5).growX();
        table.row();
        table.add(labelEvent).colspan(5).align(Align.center);
        table.row().uniform().expand().growX().space(40).center();
        table.add(buttonPause, buttonResume, buttonRestart, buttonSkip, buttonRebuild);
        table.pack();
    }

    public void adjustTypingConfigs() {
        // Only allow two chars per frame
        TypingConfig.CHAR_LIMIT_PER_FRAME = 2;

        // Change color used by CLEARCOLOR token
        TypingConfig.DEFAULT_CLEAR_COLOR = Color.WHITE;

        // Force bitmap fonts to use color markup
        TypingConfig.FORCE_COLOR_MARKUP_BY_DEFAULT = true;

        // Get token constants
        final char cOpen = TypingConfig.TOKEN_DELIMITER.open;
        final char cClose = TypingConfig.TOKEN_DELIMITER.close;

        // Create FIRE_WIND token as a global variable
        String fireWindToken = "{FASTER}{GRADIENT=ORANGE;DB6600;-0.5;5}{SLOWER}{WIND=2;4;0.5;0.5}".replace('{', cOpen).replace('}', cClose);
        TypingConfig.GLOBAL_VARS.put("FIRE_WIND", fireWindToken);
    }

    /**
     * Creates a TypingLabel instance with the default text file.
     */
    public TypingLabel createTypingLabel() {
        return createTypingLabel("default.txt");
    }

    /**
     * Creates a TypingLabel instance from the specified file.
     *
     * @param filename Name of the file under resources/text to be loaded. Must include the extension.
     */
    public TypingLabel createTypingLabel(String filename) {
        // Get token constants
        final char cOpen = TypingConfig.TOKEN_DELIMITER.open;
        final char cClose = TypingConfig.TOKEN_DELIMITER.close;

        // Get text
        String text = Gdx.files.internal("text/" + filename).readString();

        // Create label
        final TypingLabel label = new TypingLabel(text, skin);

        // Set default token
        String defaultToken = "{EASE}{FADE=0;1;0.33}";
        defaultToken = defaultToken.replace('{', cOpen).replace('}', cClose);
        label.setDefaultToken(defaultToken);

        // Make the label wrap to new lines, respecting the table's layout.
        label.setWrap(true);

        // Set variable replacements for the {VAR} and {IF} tokens
        label.setVariable("title", "curious human");
        label.setVariable("gender", "f");

        // Set an event listener for when the {EVENT} token is reached and for the char progression ends.
        label.setTypingListener(new TypingAdapter() {
            @Override
            public void event(String event) {
                System.out.println("Event: " + event);

                String eventLabelPrefix = "{FADE}{SLIDE=2;1;1}{FASTER}{COLOR=GRAY}Event:{WAIT=0.1}{COLOR=LIME} ";
                eventLabelPrefix = eventLabelPrefix.replace('{', cOpen).replace('}', cClose);
                labelEvent.restart(eventLabelPrefix + event);
                labelEvent.clearActions();
                labelEvent.addAction(
                    sequence(
                        visible(true),
                        alpha(0),
                        alpha(1, 0.25f, Interpolation.pow2In),
                        delay(0.5f),
                        alpha(0, 2f, Interpolation.pow2)
                    )
                );
            }

            @Override
            public void end() {
                System.out.println("End");
            }
        });

        // Finally parse tokens in the label text.
        label.parseTokens();

        return label;
    }

    public void update(float delta) {
        stage.act(delta);
    }

    @Override
    public void render() {
        update(Gdx.graphics.getDeltaTime());

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "TypingLabel Test";
        config.width = 720;
        config.height = 405;
        config.depth = 16;
        config.fullscreen = false;
        config.resizable = false;
        config.foregroundFPS = 60;
        config.backgroundFPS = 60;
        config.forceExit = false;

        new LwjglApplication(new TypingLabelTest(), config);
    }

}
