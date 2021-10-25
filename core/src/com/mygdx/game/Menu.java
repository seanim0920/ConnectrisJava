//challenge: blinking cursor? blinking boxes?

package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import java.util.Random;
import java.util.TimerTask;

/**
 * Created by admin on 7/27/2017.
 */

public class Menu implements Screen {
    final Main game;

    FreeTypeFontGenerator generator;
    BitmapFont font;

    private TimerTask drop;
    private int tiles = 0;
    private int max = 200;
    private boolean end = false;
    private int step = 1;

    private Random rng = new Random();
    //resources
    private Pixmap pixmap;
    private Array<Texture> types = new Array<Texture>();
    private Color[] colors = {Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA};
    private Texture square;
    private Texture empty;
    private Texture cursor;
    private Array<Sound> sounds = new Array<Sound>();
    private Texture[] buttons = new Texture[5];

    //game variables
    private int score = 0;
    private int waitTime = 2000;
    private long startTime = 0;
    private int tileSize = 153;
    private int pScale = 5;
    private int next = rng.nextInt(7); //next piece to drop
    private int previous = 0; //previous piece that was dropped

    //variables for the well
    private int gap = 75;
    private int ceiling = 10;
    private Tile[][] field;

    private Tile holding = null;
    private Vector2 offset = new Vector2();

    //variables for the current falling piece
    private Tile current = null;
    private int tcolumn = 3;
    private int trow = 0;

    //variables for input
    boolean touched = false;
    boolean rotated = false;
    long lastTouchTime;
    long lastRotTime;
    Vector3 oldPos = new Vector3(0,0,0);
    Vector3 touchPos = new Vector3();

    //for debugging
    private int paused = 0;

    //for destroyed blocks
    private Array<Array<Particle>> dblocks = new Array<Array<Particle>>();

    private final Pool<Particle> particlePool = new Pool<Particle>() {
        @Override
        protected Particle newObject() {
            return new Particle();
        }
    };

    //to generate new tiles
    private Tile newTile() {
        int type = next;
        previous = type;
        next = rng.nextInt(types.size);
        Tile tile;
        if (current != null)
            tile = new Tile(type, current.dir);
        else
            tile = new Tile(next, 0);
        tile.height = game.camera.viewportHeight;
        return tile;
    }

    public Menu(final Main game) {
        this.game = game;
        prepare();

        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = tileSize;
        parameter.color = Color.WHITE;
        font = generator.generateFont(parameter); // font size 12 pixels
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

            drawParticles();

            if (!end) {
                drawField();
            }
            else {
                drawEnd();
            }

            game.batch.end();

            if (System.currentTimeMillis() - startTime >= waitTime) {
                startTime = System.currentTimeMillis();
                //if enough time has elapsed
                if (tiles < max) {
                    tiles++;
                    if (findFloor(tcolumn) < ceiling + 1) {
                        place(current, tcolumn, findFloor(tcolumn));
                    } else {
                        end = true;
                    }
                    current = newTile();
                }
            }

            checkTouch();
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
        square.dispose();
        for (int i = 0; i < types.size; i++) {
            types.get(i).dispose();
        }
        for (int i = 0; i < sounds.size; i++) {
            sounds.get(i).dispose();
        }
        generator.dispose(); // don't forget to dispose to avoid memory leaks!
    }

    private void checkTouch() {
        if (Gdx.input.isTouched()) {
            //rotated = false;
            touchPos = game.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            int x = (int) ((touchPos.x) / tileSize);
            int y = (int) ((touchPos.y) / tileSize);
            //if touch is within range
            if (!touched) {
                touched = true;
                lastTouchTime = System.currentTimeMillis();
                oldPos = touchPos.cpy();
                if (holding == null && field[x][y] != null && field[x][y].height == y * tileSize) {
                    holding = remove(x, y);
                    holding.rotate();
                    lastRotTime = System.currentTimeMillis();
                    //this section only sets holding, make sure it doesn't need a value
                    offset.set((x * tileSize) - touchPos.x,(y * tileSize) - touchPos.y);
                    //System.out.println("X OFFSET IS " + offset.x);
                }
            }
            //for moving current piece side-to-side
            tcolumn = x;
            trow = y;
        } else {
            if (holding != null && touched) {
                int x = (int) ((touchPos.x) / tileSize);
                int y = (int) ((holding.height) / tileSize);
                place(holding, x, y);
                checktion(x, y);
            }
            touched = false;
        }
    }

    private void checktion(int x, int y) {
        if (field[x][y] != null && field[x][y].type > 0) {
            Array<Tile> visited = new Array<Tile>();
            Array<Vector2> stack = new Array<Vector2>();
            stack.add(new Vector2(x, y));
            visited.add(field[x][y]);
            while (stack.size > 0) {
                Vector2 coord = stack.pop();
                int x0 = (int) coord.x;
                int y0 = (int) coord.y;
                System.out.println("CHECKING TILE AT " + x0 + ", " + y0);
                boolean open = false;
                for (int i = 0; i < 4; i++) {
                    if (field[x0][y0] != null && field[x0][y0].sides[i]) {
                        System.out.println("SIDE " + i + " IS OPEN");
                        int x1 = 0;
                        int y1 = 0;
                        switch (i) {
                            case 0:
                                x1 = x0;
                                y1 = y0 + 1;
                                break;
                            case 1:
                                y1 = y0;
                                x1 = x0 - 1;
                                break;
                            case 2:
                                x1 = x0;
                                y1 = y0 - 1;
                                break;
                            case 3:
                                y1 = y0;
                                x1 = x0 + 1;
                                break;
                            default:
                                break;
                        }
                        if (x1 >= 0 && x1 < 7 && y1 >= 0 && y1 <= ceiling && field[x1][y1] != null && field[x1][y1].sides[(i + 2) % 4]) {
                            if (!visited.contains(field[x1][y1], true)) {
                                stack.add(new Vector2(x1, y1));
                                visited.add(field[x1][y1]);
                            } else {
                                stack.clear();
                            }
                        } else { //clear everything, the path is not valid
                            stack.clear();
                            open = true;
                        }
                    }
                }
                if (open) {
                    visited.clear();
                    break;
                }
            }
            for (int i = 0; i < visited.size; i++) {
                visited.get(i).connected = true;
            }
        }
    }

    private void drawField() {
        //draw the possible spots to drop it on
        if (holding != null) {
            game.batch.setColor(1, 1, 1, 0.5f);
            if (touched) {
                int y = (int) ((holding.height) / tileSize);
                if ((field[tcolumn][y] != null && field[tcolumn][y].height < (y * tileSize) + tileSize/2) || trow > y)
                    y = findFloor(tcolumn);
                if ((y * tileSize) > holding.height) {
                    holding.height = holding.height + holding.velocity;
                    holding.velocity = holding.velocity + 2.9f;
                }
                else {
                    holding.velocity = 0;
                    holding.height = y * tileSize;
                }
                game.batch.draw(new TextureRegion(types.get(holding.type)), (((holding.dir + 1) % 4) / 2) + tileSize * tcolumn, ((holding.dir) / 2) + holding.height, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, holding.angle);
            }
            //System.out.println("Current angle is: " + holding.angle + " trying to get from " + holding.dir * 90 + " to " + (holding.dir + 1) * 90);
            if (System.currentTimeMillis() - lastRotTime < 900) {
                float time = (float) (System.currentTimeMillis() - lastRotTime) / 200;
                //System.out.println("Current time is: " + time);
                if (time <= 1) {
                    holding.angle = new Interpolation.SwingOut(1.5f).apply((holding.dir - 1) * 90, holding.dir * 90, time);
                } else {
                    holding.angle = holding.dir * 90;
                    if (!touched)
                        holding = null;
                }
            } else {
                lastRotTime = System.currentTimeMillis();
                holding.rotate();
            }
        }

        game.batch.setColor(Color.WHITE);

        //draw the field
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < ceiling; y++) {
                if (field[x][y] != null) {
                    Tile tile = field[x][y];
                    if (tile.height > y * tileSize) {
                        tile.height = tile.height - tile.velocity;
                        tile.velocity = tile.velocity + 1.9f;
                    } else if (tile.height < y * tileSize) {
                        tile.height = y * tileSize;
                        tile.velocity = 0;
                        checktion(x, y);
                    }
                    if (tile.connected) {
                        destroy(x, y);
                        game.batch.draw(new TextureRegion(square), tileSize * x, tileSize * y, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, 0);
                    }
                    game.batch.draw(new TextureRegion(types.get(tile.type)), (((tile.dir + 1) % 4) / 2) + tileSize * x, ((tile.dir) / 2) + tile.height, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, tile.angle);
                }
            }
        }

        game.batch.setColor(Color.BLACK);
        game.batch.draw(new TextureRegion(square), 0, tileSize * (ceiling + 1), (tileSize / 2), (tileSize / 2), game.camera.viewportWidth, 500, 1, 1, 0);
        game.batch.setColor(Color.WHITE);
        font.draw(game.batch, Integer.toString(score), tileSize/10, (int)(game.camera.viewportHeight - tileSize/5));
        game.batch.draw(new TextureRegion(square), 0, tileSize * (ceiling + 1), (tileSize / 2), (tileSize / 2), game.camera.viewportWidth, tileSize/5, 1, 1, 0);
        game.batch.setColor(colors[current.type]);
        float time = 1 - (float)(System.currentTimeMillis() - startTime) / (float)waitTime;
        game.batch.draw(cursor, tileSize * tcolumn, tileSize * ceiling,            /* reposition to draw from half way up from the original sprite position */
                tileSize / 2,
                tileSize / 4,
                (float)(tileSize * time),
                tileSize / 2,
                1,
                1,
                0,
                0,
                0,
                (int)(tileSize * time),
                tileSize / 2, /* only use the texture data from the top of the sprite */
                false,
                false);
        game.batch.setColor(1,1,1,1);
    }

    private void drawEnd() {
        font.draw(game.batch, "GAME OVER", 0, 0);
        boolean falling = false;
        //draw the field
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < ceiling; y++) {
                if (field[x][y] != null) {
                    Tile tile = field[x][y];
                    int height = (y - step) * tileSize;
                    if (tile.height > height) {
                        falling = true;
                        tile.height = tile.height - tile.velocity;
                        tile.velocity = tile.velocity + 1.9f;
                    } else if (tile.height < height) {
                        tile.height = height;
                        tile.velocity = 0;
                    }
                    game.batch.draw(new TextureRegion(types.get(tile.type)), (((tile.dir + 1) % 4) / 2) + tileSize * x, ((tile.dir) / 2) + tile.height, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, tile.angle);
                }
            }
        }
        if (!falling) {
            step++;
        }
    }

    private void adjustColumn(int x, int y) {
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

    private int findFloor(int x) {
        int y = 0;
        for (int i = 0; i <= ceiling; i++) {
            //add an offset there so that if the block is overlapping its spot by a certain amount, the floor will go above it
            if (field[x][i] == null) {
                y = i;
                break;
            }
        }
        return y;
    }

    private void destroy(int x, int y) {
        score = score + 10;
        tiles--;
        Array<Particle> block = new Array<Particle>(25);
        dblocks.add(block);
        for (int c = 0; c < pScale; c++) {
            for (int r = 0; r < pScale; r++) {
                Particle p = particlePool.obtain();
                p.blast(Color.WHITE, new Vector2((tileSize * x) + (tileSize / pScale + c), (tileSize * y) + (tileSize / pScale + r)), new Vector2(c - ((pScale - 1) / 2), 2 + -1 * (r - (pScale - 1) / 2)).scl(tileSize / 10));
                block.add(p);
            }
        }
        remove(x, y);
    }

    private void place(Tile tile, int x, int y) {
        adjustColumn(x, y);
        field[x][y] = tile;
    }

    private Tile remove(int x, int y) {
        Tile tile = field[x][y];
        field[x][y] = null;
        adjustColumn(x, y);
        return tile;
    }

    private void drawParticles() {
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
                        game.batch.draw(new TextureRegion(square), p.position.x, p.position.y, tileSize / 2, tileSize / 2, tileSize / pScale, tileSize / pScale, 1, 1, p.angle);
                    else {
                        game.batch.setColor(p.color);
                        game.batch.draw(new TextureRegion(square), p.position.x, p.position.y, tileSize / 2, tileSize / 2, tileSize / pScale, tileSize / pScale, 1, 1, p.angle);
                    }
                    p.angle = p.angle + p.rspeed;
                    p.position.set(p.position.add(p.velocity));
                    p.velocity.set(p.velocity.x * 0.99f, p.velocity.y - 1);
                }
            }
        }
    }

    private void prepare() {
        generator = new FreeTypeFontGenerator(Gdx.files.internal("arcade.TTF"));

        //making the cursor
        pixmap = new Pixmap(tileSize, tileSize/2, Pixmap.Format.RGBA8888); //try RGBA4444 later
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(Color.WHITE);
        pixmap.fillTriangle(0, 0, tileSize/2, tileSize/2, tileSize, 0);
        cursor = new Texture(pixmap);

        //making the "empty" tile
        pixmap = new Pixmap(tileSize, tileSize, Pixmap.Format.RGBA8888); //try RGBA4444 later
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixmap.setColor(Color.BLACK);
        pixmap.fillRectangle(tileSize/5,tileSize/5,tileSize - 2*tileSize/5,tileSize - 2*tileSize/5);
        pixmap.fillRectangle(0,tileSize/2-tileSize/10,tileSize,tileSize/5 + 1);
        pixmap.fillRectangle(tileSize/2-tileSize/10,0,tileSize/5 + 1,tileSize);
        //pixmap.fillRectangle(tileSize/10,tileSize/10,tileSize - tileSize/5,tileSize - tileSize/5);
        empty = new Texture(pixmap);

        //making the "bare" tile
        pixmap = new Pixmap(tileSize, tileSize, Pixmap.Format.RGBA8888); //try RGBA4444 later
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        //pixmap.fillRectangle(tileSize/10,tileSize/10,tileSize - tileSize/5,tileSize - tileSize/5);
        square = new Texture(pixmap);

        int unit = (int)Math.floor(tileSize/5);
        int half = (int)Math.floor(tileSize/2);

        //making the "box" tile
        pixmap.setColor(Color.RED);
        pixmap.fill();
        pixmap.setColor(Color.BLACK);
        pixmap.fillRectangle(unit,unit,3*unit,3*unit);
        pixmap.setColor(Color.RED);
        pixmap.fillTriangle(unit,2*unit,(int)(unit*1.5),half,unit,3*unit); //left
        pixmap.fillTriangle(4*unit,2*unit,(int)(unit*3.5),half,4*unit,3*unit); //right
        pixmap.fillTriangle(2*unit,unit,half,(int)(unit*1.5),3*unit,unit); //top
        pixmap.fillTriangle(2*unit,4*unit,half,(int)(3.5*unit),3*unit,4*unit); //bottom
        pixmap.setColor(Color.BLACK);
        pixmap.fillTriangle(0,2*unit,unit/2,half,0,3*unit); //left
        pixmap.fillTriangle(tileSize,2*unit,(int)(unit*4.5),half,tileSize,3*unit); //right
        pixmap.fillTriangle(2*unit,0,half,unit/2,3*unit,0); //top
        pixmap.fillTriangle(2*unit,tileSize,half,(int)(4.5*unit),3*unit,tileSize); //bottom
        types.add(new Texture(pixmap));

        //making the "i" tile
        pixmap.setColor(Color.YELLOW);
        pixmap.fill();
        pixmap.setColor(Color.CLEAR);
        pixmap.fillRectangle(unit,unit,3*unit,3*unit);
        pixmap.fillRectangle(2*unit,0,unit + 1,half + 1);
        types.add(new Texture(pixmap));

        //making the "l" tile
        pixmap.setColor(Color.GREEN);
        pixmap.fill();
        pixmap.setColor(Color.CLEAR);
        pixmap.fillRectangle(unit,unit,3*unit,3*unit);
        pixmap.fillRectangle(0,2*unit,tileSize,unit + 1);
        types.add(new Texture(pixmap));

        //making the "r" tile
        pixmap.setColor(Color.CYAN);
        pixmap.fill();
        pixmap.setColor(Color.CLEAR);
        pixmap.fillRectangle(unit,unit,3*unit,3*unit);
        pixmap.fillRectangle(2*unit,0,unit + 1,half + 1);
        pixmap.fillRectangle(half,2*unit,half + 1,unit + 1);
        types.add(new Texture(pixmap));

        //making the "t" tile
        pixmap.setColor(Color.BLUE);
        pixmap.fill();
        pixmap.setColor(Color.CLEAR);
        pixmap.fillRectangle(unit,unit,3*unit,3*unit);
        pixmap.fillRectangle(2*unit,0,unit + 1,tileSize);
        pixmap.fillRectangle(half,2*unit,half + 1,unit + 1);
        types.add(new Texture(pixmap));

        //making the "plus" tile
        pixmap.setColor(Color.MAGENTA);
        pixmap.fill();
        pixmap.setColor(Color.CLEAR);
        pixmap.fillRectangle(tileSize/5,tileSize/5,tileSize - 2*tileSize/5,tileSize - 2*tileSize/5);
        pixmap.fillRectangle(tileSize/2-tileSize/10,0,tileSize/5 + 1,tileSize);
        pixmap.fillRectangle(0,tileSize/2-tileSize/10,tileSize,tileSize/5 + 1);
        types.add(new Texture(pixmap));

        field = new Tile[7][ceiling + 1];

        current = newTile();
    }
}
