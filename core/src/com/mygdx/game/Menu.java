package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Menu extends Unit implements Screen {
    Label title;
    Label button;
    BitmapFont outline;
    FreeTypeFontGenerator generator;
    FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
    long startTime = System.currentTimeMillis();
    Tile[] tiles = new Tile[4];
    int dframes = 0;
    Array<Tile> dblocks = new Array<Tile>();
    private boolean held = false;

    public Menu(final Main game) {
        super(game);
        generator = new FreeTypeFontGenerator(Gdx.files.internal("font.otf"));
        parameter.color = Color.WHITE;
        parameter.size = 75;

        title = new Label("CONNECTRIS", header);
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
        float time = (float) (System.currentTimeMillis() - title.lastTime) / 300;
    }

    public void process() {
        button = new Label("PLAY", header);
        button.ypos = tileSize * 6;
        button.xpos = button.center(game.camera);
        button.rectangle.x = (int)button.xpos;
        button.rectangle.y = (int)button.ypos;
    }

    public void processTouching(boolean changed) {
        if (button.rectangle.contains(touchPos.x, touchPos.y)) {
            button.font = outline;
        } else {
            button.font = title.font;
        }
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
        header.dispose();
	}
}