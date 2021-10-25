package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;

import javax.sound.midi.SysexMessage;

import static java.lang.Math.PI;
import static java.lang.Math.abs;

/**
 * Created by admin on 7/27/2017.
 */

public class Menu implements Screen {
    final Main game;

    private Random rng = new Random();
    //resources
    private Pixmap pixmap;
    private Array<Texture> types = new Array<Texture>();
    private Color[] colors = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA};
    private Texture tile;
    private Array<Sound> sounds = new Array<Sound>();
    private Texture[] buttons = new Texture[5];

    //game variables
    private int waitTime = 1000;
    private long startInterval;
    private int tileSize = 151;
    private int pScale = 5;
    private int next = rng.nextInt(7); //next piece to drop
    private int previous = 0; //previous piece that was dropped

    //variables for the well
    private int gap = 75;
    private int ceiling = 11;
    private Tile[][] field;

    //variables for the current falling piece
    private boolean fell = false;
    private Tile falling;
    private int row = 0;
    private int column = 3;
    private int ncolumn = 3;
    private int height = gap + tileSize * ceiling;
    private double counter = 0;
    private int velocity = 0;
    private int stop = 0;

    //variables for input
    boolean touched = false;
    boolean rotated = false;
    long lastTouchTime;
    Vector3 oldPos = new Vector3(0,0,0);
    Vector3 touchPos = new Vector3();

    //for debugging
    private int paused = 0;

    //for destroyed blocks
    private Array<Array<Particle>> dblocks = new Array<Array<Particle>>();

    //for paths
    private Array<Array<Tile>> paths = new Array<Array<Tile>>();

    private final Pool<Particle> particlePool = new Pool<Particle>() {
        @Override
        protected Particle newObject() {
            return new Particle();
        }
    };

    //to generate new tiles
    private Tile newTile() {
        int type = next;
        type = 2;
        previous = type;
        next = rng.nextInt(7);
        if (falling != null)
            return new Tile(type, falling.angle);
        else
            return new Tile(next, 0);
    }

    public Menu(final Main game) {
        this.game = game;
        prepare();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (paused < 2) {
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            game.camera.update();

            game.batch.setProjectionMatrix(game.camera.combined);

            game.batch.begin();

            game.batch.setColor(1, 1, 1, 1);

            //gotta draw the tiles here
            for (int x = 0; x < 7; x++) {
                for (int y = 0; y < ceiling + 1; y++) {
                    //preview row
                    if (y >= ceiling) {
                        //game.batch.draw(new TextureRegion(tile), tileSize * x + tileSize / 5, gap + tileSize * y + tileSize / 5, tileSize / 2, tileSize / 2, 3 * tileSize / 5, 3 * tileSize / 5, 1, 1, 0);
                        if (x == column) {
                            if (fell)
                                game.batch.draw(new TextureRegion(types.get(next)), (((falling.angle + 1) % 4) / 2) + tileSize * x, ((falling.angle) / 2) + gap + tileSize * y, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, 90 * falling.angle);
                            else
                                game.batch.draw(new TextureRegion(types.get(falling.type)), (((falling.angle + 1) % 4) / 2) + tileSize * x, ((falling.angle) / 2) + gap + tileSize * y, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, 90 * falling.angle);
                        }
                    } else {
                        //playing field
                        if (field[x][y] != null) {
                            if (field[x][y].connected) {
                                game.batch.draw(new TextureRegion(tile), tileSize * x, tileSize * y, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, 0);
                            }
                            game.batch.draw(new TextureRegion(types.get(field[x][y].type)), (((field[x][y].angle + 1) % 4) / 2) + tileSize * x, ((field[x][y].angle) / 2) + tileSize * y, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, 90 * field[x][y].angle);
                            if (field[x][y].destroyed) {
                                Array<Particle> block = new Array<Particle>(25);
                                dblocks.add(block);
                                for (int c = 0; c < pScale; c++) {
                                    for (int r = 0; r < pScale; r++) {
                                        Particle p = particlePool.obtain();
                                        p.blast(Color.WHITE, new Vector2((tileSize * x) + (tileSize / pScale + c), (tileSize * y) + (tileSize / pScale + r)), new Vector2(c - ((pScale - 1) / 2), 2 + -1 * (r - (pScale - 1) / 2)).scl(tileSize / 10));
                                        block.add(p);
                                    }
                                }
                                field[x][y] = null;
                            }
                        }
                    }
                }
            }

            //falling tile
            if (fell) {
                height = height - velocity;
                velocity = velocity + 2;
                game.batch.draw(new TextureRegion(types.get(falling.type)), (((falling.angle + 1) % 4) / 2) + tileSize * ncolumn, ((falling.angle) / 2) + height, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, 90 * falling.angle);
                if (counter <= 0) {
                    if (falling.type < 2 && row <= 0) {
                        Array<Particle> block = new Array<Particle>(pScale ^ 2);
                        dblocks.add(block);
                        for (int c = 0; c < pScale; c++) {
                            for (int r = 0; r < pScale; r++) {
                                Particle p = particlePool.obtain();
                                if (c < pScale / 5 || c > 3 * (pScale / 5) || r < pScale / 5 || r > 3 * (pScale / 5)) {
                                    p.blast(Color.WHITE, new Vector2((tileSize * ncolumn) + (c * tileSize / pScale), (r * (tileSize / pScale))), new Vector2(c - ((pScale - 1) / 2), 5 + r).scl(tileSize / 20));
                                } else {
                                    p.blast(colors[falling.type], new Vector2((tileSize * ncolumn) + (c * tileSize / pScale), (r * (tileSize / pScale))), new Vector2(c - ((pScale - 1) / 2), 5 + r).scl(tileSize / 20));
                                }
                                block.add(p);
                            }
                        }
                    } else {
                        field[ncolumn][row] = new Tile(falling.type, falling.angle);
                        checktion(ncolumn, row);
                    }
                    falling = newTile();
                    fell = false;
                    height = gap + tileSize * ceiling;
                    velocity = 0;
                }
                counter = counter - 1;
            }

            //show exploding pieces
            for (int b = 0; b < dblocks.size; b++) {
                Array<Particle> block = dblocks.get(b);
                if (block.size < 1)
                    dblocks.removeIndex(b);
                for (int i = 0; i < block.size; i++) {
                    Particle p = block.get(i);
                    if (p.position.x < 0 || p.position.x > 1080 || p.position.y < 0) {
                        block.removeIndex(i);
                        particlePool.free(p);
                    } else {
                        game.batch.setColor(Color.WHITE);
                        if (p.color == Color.WHITE)
                            game.batch.draw(new TextureRegion(tile), p.position.x, p.position.y, tileSize / 2, tileSize / 2, tileSize / pScale, tileSize / pScale, 1, 1, p.angle);
                        else {
                            game.batch.setColor(p.color);
                            game.batch.draw(new TextureRegion(tile), p.position.x, p.position.y, tileSize / 2, tileSize / 2, tileSize / pScale, tileSize / pScale, 1, 1, p.angle);
                        }
                        p.angle = p.angle + p.rspeed;
                        p.position.set(p.position.add(p.velocity));
                        p.velocity.set(p.velocity.x * 0.99f, p.velocity.y - 1);
                    }
                }
            }

            game.batch.end();

            //if enough time has elapsed
            if (System.currentTimeMillis() - startInterval >= waitTime && stop <= 2) {
                stop++;
                //drop the current piece
                startInterval = System.currentTimeMillis();
                fell = true;
                ncolumn = column;
                sounds.get(0).play();
                for (int y = 0; y < ceiling; y++) {
                    if (field[column][y] == null) {
                        row = y;
                        counter = Math.sqrt((double) (2 * ((tileSize * (ceiling - row)) / 2)));
                        break;
                    }
                }
                if (counter <= 0) {
                    System.out.println("GAME OVER");
                }
            }

            if (Gdx.input.isTouched()) {
                rotated = false;
                touchPos = game.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
                //if touch is within range
                if (!touched) {
                    touched = true;
                    lastTouchTime = System.currentTimeMillis();
                    oldPos = touchPos.cpy();
                }
                //for moving current piece side-to-side
                column = (int) ((touchPos.x) / tileSize);
            } else {
                touched = false;
                if (!rotated && System.currentTimeMillis() - lastTouchTime < 90) {
                    //to prevent simultaneous
                    rotated = true;
                    if ((int) ((touchPos.y) / tileSize) < ceiling && field[(int) ((touchPos.x) / tileSize)][(int) ((touchPos.y) / tileSize)] != null) {
                        field[(int) ((touchPos.x) / tileSize)][(int) ((touchPos.y) / tileSize)].rotate();
                        if (field[(int) ((touchPos.x) / tileSize)][(int) ((touchPos.y) / tileSize)].type > 1 && field[(int) ((touchPos.x) / tileSize)][(int) ((touchPos.y) / tileSize)].type < 6) {
                            for (int i = 0; i < paths.size; i++) {
                                if (paths.get(i).contains(field[(int) ((touchPos.x) / tileSize)][(int) ((touchPos.y) / tileSize)], false)) {
                                    for (int e = 0; e < paths.get(i).size; e++) {
                                        paths.get(i).get(e).disconnect();
                                    }
                                    paths.removeIndex(i);
                                }
                            }
                            checktion((int) ((touchPos.x) / tileSize), (int) ((touchPos.y) / tileSize));
                        }
                        sounds.get(1).play();
                    }
                }
            }
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
        pixmap.dispose();
        tile.dispose();
        for (int i = 0; i < types.size; i++) {
            types.get(i).dispose();
        }
        for (int i = 0; i < sounds.size; i++) {
            sounds.get(i).dispose();
        }
    }

    private void checktion(int x, int y) {
        System.out.println("CHECKING PATH");
        Array<Tile> path = new Array<Tile>();
        Array<Vector2> stack = new Array<Vector2>();
        stack.add(new Vector2(x, y));
        path.add(field[x][y]);
        while (stack.size > 0) {
            Vector2 coord = stack.pop();
            int x0 = (int)coord.x;
            int y0 = (int)coord.y;
            for (int i = 0; i < 4; i++) {
                if (field[x0][y0] != null && field[x0][y0].sides[i]) {
                    int x1 = 0;
                    int y1 = 0;
                    switch (i) {
                        case 0:
                            x1 = x0;
                            y1 = y0 + 1;
                            break;
                        case 2:
                            x1 = x0;
                            y1 = y0 - 1;
                            break;
                        case 1:
                            y1 = y0;
                            x1 = x0 + 1;
                            break;
                        case 3:
                            y1 = y0;
                            x1 = x0 - 1;
                            break;
                        default:
                            break;
                    }
                    if (x1 >= 0 && x1 <= 7 && y1 >= 0 && y1 <= ceiling) {
                        if (field[x1][y1] != null && field[x1][y1].sides[(i + 2) % 4]) {
                            if (!path.contains(field[x1][y1], false)) {
                                stack.add(new Vector2(x1, y1));
                                path.add(field[x1][y1]);
                            }
                        } else {
                            stack.clear();
                            path.clear();
                            break;
                        }
                    }
                }
            }
        }
        if (path.size > 0) {
            for (int i = 0; i < path.size; i++) {
                path.get(i).connect();
            }
            paths.add(path);
        }
    }

    private void prepare() {
        sounds.add(Gdx.audio.newSound(Gdx.files.internal("descend.wav")));
        sounds.add(Gdx.audio.newSound(Gdx.files.internal("flip.wav")));
        sounds.add(Gdx.audio.newSound(Gdx.files.internal("land.wav")));

        /*
        pixmap = new Pixmap(75, 75, Pixmap.Format.RGB888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(Color.CLEAR);
        pixmap.fillRectangle(0,25,75,25);
        pixmap.fillRectangle(25,0,25,75);
        */

        //making the "bare" tile
        pixmap = new Pixmap(tileSize, tileSize, Pixmap.Format.RGBA8888); //try RGBA4444 later
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        //pixmap.fillRectangle(tileSize/10,tileSize/10,tileSize - tileSize/5,tileSize - tileSize/5);
        tile = new Texture(pixmap);

        //making the "box" tile
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixmap.setColor(Color.RED);
        pixmap.fillRectangle(tileSize/5,tileSize/5,tileSize - 2*tileSize/5,tileSize - 2*tileSize/5);
        types.add(new Texture(pixmap));

        //making the "dot" tile
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixmap.setColor(Color.ORANGE);
        pixmap.fillRectangle(tileSize/5,tileSize/5,tileSize - 2*tileSize/5,tileSize - 2*tileSize/5);
        pixmap.setColor(Color.CLEAR);
        pixmap.fillRectangle(tileSize/2 - tileSize/10,tileSize/2 - tileSize/10,tileSize/5 + 1, tileSize/5 + 1);
        types.add(new Texture(pixmap));

        //making the "i" tile
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixmap.setColor(Color.YELLOW);
        pixmap.fillRectangle(tileSize/5,tileSize/5,tileSize - 2*tileSize/5,tileSize - 2*tileSize/5);
        pixmap.setColor(Color.CLEAR);
        pixmap.fillRectangle(tileSize/2 - tileSize/10,tileSize/2 - tileSize/10,tileSize/5 + 1, tileSize/5 + 1);
        pixmap.fillRectangle(tileSize/2-tileSize/10,0,tileSize/5 + 1,tileSize/2 + 1);
        types.add(new Texture(pixmap));

        //making the "l" tile
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixmap.setColor(Color.GREEN);
        pixmap.fillRectangle(tileSize/5,tileSize/5,tileSize - 2*tileSize/5,tileSize - 2*tileSize/5);
        pixmap.setColor(Color.CLEAR);
        pixmap.fillRectangle(0,tileSize/2-tileSize/10,tileSize,tileSize/5 + 1);
        types.add(new Texture(pixmap));

        //making the "r" tile
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixmap.setColor(Color.CYAN);
        pixmap.fillRectangle(tileSize/5,tileSize/5,tileSize - 2*tileSize/5,tileSize - 2*tileSize/5);
        pixmap.setColor(Color.CLEAR);
        pixmap.fillRectangle(tileSize/2 - tileSize/10,tileSize/2 - tileSize/10,tileSize/5 + 1, tileSize/5 + 1);
        pixmap.fillRectangle(tileSize/2 - tileSize/10,0,tileSize/5 + 1,tileSize/2 + 1);
        pixmap.fillRectangle(tileSize/2,tileSize/2-tileSize/10,tileSize/2 + 1,tileSize/5 + 1);
        types.add(new Texture(pixmap));

        //making the "t" tile
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixmap.setColor(0.2f,0.2f,1,1);
        pixmap.fillRectangle(tileSize/5,tileSize/5,tileSize - 2*tileSize/5,tileSize - 2*tileSize/5);
        pixmap.setColor(Color.CLEAR);
        pixmap.fillRectangle(tileSize/2-tileSize/10,0,tileSize/5 + 1,tileSize);
        pixmap.fillRectangle(tileSize/2,tileSize/2-tileSize/10,tileSize/2 + 1,tileSize/5 + 1);
        types.add(new Texture(pixmap));

        //making the "plus" tile
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixmap.setColor(Color.MAGENTA);
        pixmap.fillRectangle(tileSize/5,tileSize/5,tileSize - 2*tileSize/5,tileSize - 2*tileSize/5);
        pixmap.setColor(Color.CLEAR);
        pixmap.fillRectangle(tileSize/2-tileSize/10,0,tileSize/5 + 1,tileSize);
        pixmap.fillRectangle(0,tileSize/2-tileSize/10,tileSize,tileSize/5 + 1);
        types.add(new Texture(pixmap));

        field = new Tile[7][ceiling];

        falling = newTile();
    }
}
