package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Label;

public class Menu extends Unit {
    Label title;
    Button button;
    BitmapFont outline;
    FreeTypeFontGenerator generator;
    FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
    long startTime = System.currentTimeMillis();
    long lastMovTime = System.currentTimeMillis();
    float ypos = 0;
    private boolean held = false;

    public Menu(final Main game) {
        super(game);
        generator = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));
        parameter.color = Color.WHITE;
        parameter.size = 75;

        title = new Label("connectris", game.header);
        title.ypos = -tileSize;
        title.xpos = title.center(game.camera);
        parameter.borderColor = Color.WHITE;
        parameter.color = Color.BLACK;
        parameter.borderWidth = 5;
        outline = generator.generateFont(parameter);
        button = new Button("PLAY", game.header, 0);
        button.ypos = -tileSize;
        button.xpos = button.center(game.camera);
        lastMovTime = System.currentTimeMillis() + 250;
        button.lastMovTime = System.currentTimeMillis() + 500;
    }

    @Override
    public void show() {
        title.lastMovTime = System.currentTimeMillis();
        startTime = System.currentTimeMillis();
    }

    public void drawText() {
    }

    public void process() {
        button.font.draw(game.batch, button.layout, button.xpos, button.ypos);
        title.font.draw(game.batch, title.layout, title.xpos, title.ypos);

        float time = (System.currentTimeMillis() - title.lastMovTime) / 500f;
        title.ypos = tileSize * 11;
        if (time <= 1) {
            title.ypos = Interpolation.sine.apply(-tileSize, tileSize * 11, time);
        }

        if (System.currentTimeMillis() - startTime >= 250) {
                time = (System.currentTimeMillis() - lastMovTime) / 500f;
                ypos = tileSize * 10;
                if (time <= 1) {
                    ypos = Interpolation.sine.apply(-tileSize * 2, tileSize * 10, time);
                }
        }

        game.batch.draw(new TextureRegion(game.pixel), 0, ypos, (tileSize / 2), (tileSize / 2), game.camera.viewportWidth, 5, 1, 1, 0);

        if (System.currentTimeMillis() - startTime >= 500) {
                time = (System.currentTimeMillis() - button.lastMovTime) / 500f;
                button.ypos = tileSize * 6;
                if (time <= 1) {
                    button.ypos = Interpolation.sine.apply(-tileSize * 6, tileSize * 6, time);
                }
        }

        button.rectangle.x = (int)button.xpos;
        button.rectangle.y = (int)button.ypos;
    }

    public void processTouching(boolean changed) {
    }

    public void processNotouch(boolean changed) {
        if (changed && button.isTouched(touchPos)) {
            System.out.println("EY, TIS SHOLKD SWITHC");
            game.setScreen(game.play);
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        generator.dispose();
        outline.dispose();
    }
}