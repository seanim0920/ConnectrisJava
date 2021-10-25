package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
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
    public Tile[][] field = new Tile[7][12];
    public OrthographicCamera camera;
    public boolean moved = false;
    public int hcolumn = 3;
    public int hrow = 0;
    public int tileSize;
    public int tcolumn = 3;
    public int ceiling = 11;
    public Random rng = new Random();
    public FreeTypeFontGenerator generator;

    public Tile holding = null;

    public Color[] colors = {Color.YELLOW, Color.GREEN, Color.CYAN, new Color(0.25f,0.25f,1,1), Color.MAGENTA, Color.RED};

    public Unit(final Main game) {
        this.game = game;
        this.tileSize = (int)(game.camera.viewportWidth/7);
    }

    public void place(Tile tile, int x, int y) {
        tile.xpos = x * tileSize;
        tile.ypos = y * tileSize;
        tile.coords.set(x, y);
        tile.placed = true;
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
            if (field[hcolumn][y] != null && field[hcolumn][y].ypos > ((y) * tileSize) + (tileSize / 2)) {
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

    public void processTouch() {
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
    }
}