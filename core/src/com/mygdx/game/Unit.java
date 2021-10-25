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
    public BitmapFont header;
    public boolean touched = false;
    public Vector3 touchPos = new Vector3();
    public Vector3 oldPos = new Vector3();
    public OrthographicCamera camera;
    public int ceiling = 9;
    public Tile[][] field = new Tile[6][ceiling+1];
    public boolean moved = false;
    public int tcolumn = 3;
    public int hcolumn = 3;
    public int hrow = 0;
    public int tileSize;
    public Random rng = new Random();
    public FreeTypeFontGenerator generator;

    public Texture types;
    public Array<Texture> preview = new Array<Texture>();
    public Color[] colors = {Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED};

    public Unit(final Main game) {
        this.game = game;
        this.camera = game.camera;
        this.types = game.types;
        this.preview = game.preview;
        this.header = game.header;
        this.tileSize = (int)Math.ceil(game.camera.viewportWidth/7);
    }

    public void place(Tile tile, int x, int y) {
        tile.xpos = x * tileSize;
        tile.ypos = y * tileSize;
        tile.coords.set(x, y);
        tile.placed = true;
        field[x][y] = tile;
    }

    public Tile remove(int x, int y) {
        Tile tile = field[x][y];
        field[x][y] = null;
        return tile;
    }

    public void checkTouch() {
        if (camera != null) {
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

    public Tile getTile(Vector2 coords) {
        if (coords.y > ceiling)
            return null;
        if (coords.y < 0) {
            if (coords.x % 2 == 0)
                return field[(int)coords.x + 1][0];
            else
                return field[(int)coords.x - 1][0];
        }
        if (coords.x < 0)
            return field[field.length - 1][(int)coords.y];
        if (coords.x >= field.length)
            return field[0][(int)coords.y];
        return field[(int)coords.x][(int)coords.y];
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
    }
}