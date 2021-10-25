package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Label;

public class Menu extends Main implements Screen {
    public Main game;
    Label title;
    Label button;
    BitmapFont outline;
    FreeTypeFontGenerator generator;
    FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
    long startTime = System.currentTimeMillis();
    private boolean held = false;

    public Menu() {
        generator = new FreeTypeFontGenerator(Gdx.files.internal("font.otf"));
        parameter.color = Color.WHITE;
        parameter.size = 75;
        font = generator.generateFont(parameter); // font size 12 pixels

        title = new Label("CONNECTRIS", font);
        title.ypos = camera.viewportHeight;
        title.xpos = title.center(camera);
        parameter.borderColor = Color.WHITE;
        parameter.color = Color.BLACK;
        parameter.borderWidth = 5;
        outline = generator.generateFont(parameter);
    }

    public void drawText() {
        button.font.draw(batch, button.layout, button.xpos, button.ypos);
        title.font.draw(batch, title.layout, title.xpos, title.ypos);
        float time = (float) (System.currentTimeMillis() - title.lastTime) / 300;
    }

    public void process() {
        button = new Label("PLAY", font);
        button.ypos = tileSize * 6;
        button.xpos = button.center(camera);
        button.rectangle.x = (int)button.xpos;
        button.rectangle.y = (int)button.ypos;
    }

    public void processTouching(boolean changed) {
    }

    public void processNotouch(boolean changed) {
        if (changed && button.rectangle.contains(touchPos.x, touchPos.y)) {
            setScreen(play);
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
    public void render(float delta) {

    }

    @Override
    public void show() {
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