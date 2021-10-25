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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

public abstract class Unit implements Screen {
    public Main game;
    public BitmapFont header;
    public Texture square;
    public boolean touched = false;
    public Vector3 touchPos = new Vector3();
    public Vector3 oldPos = new Vector3();
    public Tile[][] field = new Tile[7][11];
    public OrthographicCamera camera;
    public boolean moved = false;
    public int hcolumn = 3;
    public int hrow = 0;
    public int tileSize;
    public int tcolumn = 3;
    public int ceiling = field[0].length-1;
    public Random rng = new Random();
    public FreeTypeFontGenerator generator;

    public Tile holding = null;

    public Array<Texture> types = new Array<Texture>();
    public Color[] colors = {Color.YELLOW, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.RED};

    public Unit(final Main game) {
        this.game = game;
        this.square = game.pixel;
        this.camera = game.camera;
        this.types = game.types;
        this.header = game.header;
        this.tileSize = (int)(game.camera.viewportWidth/7);
    }

    public void place(Tile tile, int x, int y) {
        adjustColumn(x, y);
        field[x][y] = tile;
    }

    public Tile remove(int x, int y) {
        Tile tile = field[x][y];
        field[x][y] = null;
        adjustColumn(x, y);
        return tile;
    }

    public void adjustColumn(int x, int y) {
        //takes all the pieces in the column starting from y and shifts them to the bottom starting at y
        if (field[x][y] == null) {
            //this executes when a tile is taken away, will move all the tiles above it downwards 1
            for (int i = y + 1; i <= ceiling; i++) {
                field[x][i - 1] = field[x][i];
                field[x][i] = null;
            }
        } else {
            //execute this before placing a tile, will move all the tiles above it upwards 1
            for (int i = ceiling; i > y; i--) {
                field[x][i] = field[x][i - 1];
                field[x][i - 1] = null;
            }
        }
    }

    public int findFloor(int x) {
        int y = ceiling + 1;
        for (int i = 0; i <= ceiling; i++) {
            //add an offset there so that if the block is overlapping its spot by a certain amount, the floor will go above it
            if (field[x][i] == null) {
                y = i;
                break;
            }
        }
        return y;
    }

    public int findSpace(int x) {
        int i = findFloor(x);
        for (int y = i - 1; y >= 0; y--) {
            if (field[hcolumn][y] != null && field[hcolumn][y].height > ((y) * tileSize) + (tileSize / 2)) {
                i = y;
            }
        }
        return i;
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
        game.batch.draw(new TextureRegion(square), 0, game.camera.viewportHeight - (tileSize + 5), (tileSize / 2), (tileSize / 2), game.camera.viewportWidth, tileSize / 7, 1, 1, 0);
        game.batch.setColor(Color.BLACK);
        game.batch.draw(new TextureRegion(square), 0, (game.camera.viewportHeight - tileSize), (tileSize / 2), (tileSize / 2), game.camera.viewportWidth, 5 * tileSize / 5, 1, 1, 0);
        game.batch.setColor(Color.WHITE);

        drawText();

        game.batch.setColor(1, 1, 1, 1);

        game.batch.end();
    }
}