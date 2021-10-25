package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

public abstract class Unit implements Screen {
    public Main game;
    public boolean touched = false;
    public Vector3 touchPos = new Vector3();
    public Vector3 oldPos = new Vector3();
    public int tileSize;
    public Random rng = new Random();
    public FreeTypeFontGenerator generator;

    public BitmapFont header;

    public Texture types;

    public Tile holding = null;

    public Unit(final Main game) {
        this.game = game;
        this.tileSize = game.tileSize;
    }

    public void checkTouch() {
        if (game.camera != null) {
            if (Gdx.input.isTouched()) {
                touchPos = game.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
                if (touched) {
                    processTouching(false);
                } else {
                    touched = true;
                    processTouching(true);
                }
                oldPos = game.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            } else {
                if (!touched) {
                    processNotouch(false);
                } else {
                    touched = false;
                    processNotouch(true);
                }
            }
        }
    }

    public void processTouching(boolean changed) {
    }

    public void processNotouch(boolean changed) {
    }

    public void process() {
    }

    public void drawText() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.camera.update();

        game.batch.setProjectionMatrix(game.camera.combined);

        checkTouch();

        game.batch.begin();

        process();

        game.batch.setColor(Color.WHITE);
        game.batch.draw(new TextureRegion(game.pixel), 0, game.camera.viewportHeight - (tileSize + 5), (tileSize / 2), (tileSize / 2), game.camera.viewportWidth, tileSize / 7, 1, 1, 0);
        game.batch.setColor(Color.BLACK);
        game.batch.draw(new TextureRegion(game.pixel), 0, (game.camera.viewportHeight - tileSize), (tileSize / 2), (tileSize / 2), game.camera.viewportWidth, 5 * tileSize / 5, 1, 1, 0);
        game.batch.setColor(Color.WHITE);

        drawText();

        game.batch.setColor(1, 1, 1, 1);

        game.batch.end();

        drawShape();
    }

    public void drawShape() {

    }
}