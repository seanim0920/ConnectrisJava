package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

public abstract class Unit implements Screen {
    public Main game;
    public Texture square;
    public boolean touched = false;
    public Vector3 touchPos = new Vector3();
    public Tile[][] field = new Tile[7][12];
    public OrthographicCamera camera;
    public boolean moved = false;
    public int hcolumn = 3;
    public int hrow = 0;
    public int tileSize;
    public int tcolumn = 3;
    public Random rng = new Random();

    public Tile holding = null;

    public Array<Texture> types = new Array<Texture>();
    public Color[] colors = {Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, new Color(0.25f,0.25f,1,1), Color.MAGENTA};

    public Unit(final Main game) {
        this.game = game;
        this.square = game.square;
        this.camera = game.camera;
        this.types = game.types;
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
            for (int i = y + 1; i <= 5; i++) {
                field[x][i - 1] = field[x][i];
                field[x][i] = null;
            }
        } else {
            //execute this before placing a tile, will move all the tiles above it upwards 1
            for (int i = 5; i > y; i--) {
                field[x][i] = field[x][i - 1];
                field[x][i - 1] = null;
            }
        }
    }

    public int findFloor(int x) {
        int y = 12;
        for (int i = 0; i <= 11; i++) {
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
                touched = true;
            } else {
                touched = false;
            }
        }
    }

    public void processTouch() {
    }

    public void process() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.camera.update();

        game.batch.setProjectionMatrix(game.camera.combined);

        checkTouch();
        processTouch();

        game.batch.begin();

        game.batch.setColor(Color.WHITE);
        game.batch.draw(new TextureRegion(square), 0, game.camera.viewportHeight - (tileSize + 5), (tileSize / 2), (tileSize / 2), game.camera.viewportWidth, tileSize / 7, 1, 1, 0);
        game.batch.setColor(Color.BLACK);
        game.batch.draw(new TextureRegion(square), tileSize / 5, (game.camera.viewportHeight - tileSize), (tileSize / 2), (tileSize / 2), game.camera.viewportWidth - 2 * tileSize / 5, 5 * tileSize / 5, 1, 1, 0);

        process();

        game.batch.setColor(1, 1, 1, 1);

        game.batch.end();
    }
}