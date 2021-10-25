package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Label;

public class Menu extends Unit {
    Label title;
    Label button;
    BitmapFont outline;
    FreeTypeFontGenerator generator;
    FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
    long startTime = System.currentTimeMillis();
    private boolean held = false;

    public Menu(final Main game) {
        super(game);
        generator = new FreeTypeFontGenerator(Gdx.files.internal("font.otf"));
        parameter.color = Color.WHITE;
        parameter.size = 75;

        title = new Label("CONNECTRIS", game.header);
        title.ypos = game.camera.viewportHeight;
        title.xpos = title.center(game.camera);
        parameter.borderColor = Color.WHITE;
        parameter.color = Color.BLACK;
        parameter.borderWidth = 5;
        outline = generator.generateFont(parameter);
    }

    @Override
    public void show() {

    }

    public void drawText() {
        button.font.draw(game.batch, button.layout, button.xpos, button.ypos);
        title.font.draw(game.batch, title.layout, title.xpos, title.ypos);
    }

    public void process() {
        button = new Label("PLAY", game.header);
        button.ypos = tileSize * 6;
        button.xpos = button.center(game.camera);
        button.rectangle.x = (int)button.xpos;
        button.rectangle.y = (int)button.ypos;
    }

    public void processTouching(boolean changed) {
    }

    public void processNotouch(boolean changed) {
        if (changed && button.rectangle.contains(touchPos.x, touchPos.y)) {
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