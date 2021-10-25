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

import java.util.Random;

/**
 * Created by admin on 7/27/2017.
 */

public class Menu implements Screen {
    final Main game;

    FreeTypeFontGenerator generator;
    FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
    BitmapFont font;

    private int dframes = 0;

    private boolean moved = false;
    private int tiles = 0;
    private int max = 500;
    private int flux = 0;
    private boolean end = false;
    private int step = 1;

    private Sound gameover;
    private Sound thud;
    private Sound drop;
    private Sound twist;
    private Sound bust;
    private Random rng = new Random();
    //resources
    private Pixmap pixmap;
    private Array<Texture> types = new Array<Texture>();
    private Color[] colors = {Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA};
    private Texture square;
    private Texture cursor;

    //game variables
    private int score = 0;
    private int waitTime = 1000;
    private long currentTime = 0;
    private long startTime = 0;
    private int tileSize = 154;
    private int pScale = 5;
    private int next = 0; //next piece to drop

    //variables for the well
    private int ceiling = 12;
    private Tile[][] field;

    private Tile holding = null;
    private Vector2 offset = new Vector2();

    //variables for the current falling piece
    private Tile current = null;
    private int tcolumn = 3;
    private int hcolumn = 3;
    private int hrow = 0;

    //variables for input
    boolean touched = false;
    long lastTouchTime;
    long lastRotTime;
    Vector3 touchPos = new Vector3();

    private boolean paused = false;

    //for destroyed blocks
    private Array<Tile> dblocks = new Array<Tile>();

    //to generate new tiles
    private Tile newTile() {
        int type = next;
        next = rng.nextInt(types.size);
        //System.out.println("THE NEXT PIECE IS OF TYPE " + next);
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

        parameter.size = tileSize;
        parameter.color = Color.WHITE;
        font = generator.generateFont(parameter); // font size 12 pixels
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.camera.update();

        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin();

        game.batch.setColor(1, 1, 1, 1);

        if (Gdx.input.isTouched()) {
            touchPos = game.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        }

        if (!paused) {
            if (!end) {
                if (dframes <= 0) {
                    drawParticles();
                    drawHolding();
                    drawField();
                } else {
                    drawConnected();
                }
            } else {
                drawEnd();
            }

            if (flux > 0) flux = flux - 1;
            currentTime = currentTime + ((long)(Gdx.graphics.getDeltaTime()*1000)-(findFloor(tcolumn)+1));

            if (currentTime - startTime >= waitTime) {
                startTime = System.currentTimeMillis();
                currentTime = startTime;
                //if enough time has elapsed
                if (tiles < max) {
                    tiles++;
                    place(current, tcolumn, findFloor(tcolumn));
                    current = newTile();
                }
            }

            checkTouch();
        } else {
            drawPause();
        }

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        paused = false;
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
        generator.dispose(); // don't forget to dispose to avoid memory leaks!
    }

    private void drawPause() {
        game.batch.setColor(Color.WHITE);
        game.batch.draw(new TextureRegion(square), 0, game.camera.viewportHeight - tileSize, (tileSize / 2), (tileSize / 2), game.camera.viewportWidth, tileSize, 1, 1, 0);
        font.setColor(Color.BLACK);
        font.draw(game.batch, "PAUSED", tileSize/10 + 4, (int)(game.camera.viewportHeight - tileSize/5) + 4);

        font.setColor(Color.WHITE);
        font.draw(game.batch, "RESTART", tileSize/10, (int)(game.camera.viewportHeight - (3*tileSize)));

        font.setColor(Color.WHITE);
        font.draw(game.batch, "RESUME", tileSize/10, (int)(game.camera.viewportHeight - (5*tileSize)));

        font.setColor(Color.WHITE);
        font.draw(game.batch, "QUIT", tileSize/10, (int)(game.camera.viewportHeight - (7*tileSize)));

        if (touchPos.x > tileSize/10) resume();
        //restart
        //resume
        //quit
    }

    private void checkTouch() {
        if (Gdx.input.isTouched()) {
            int x = (int) ((touchPos.x) / tileSize);
            int y = (int) ((touchPos.y) / tileSize);
            //if touch is within range
            if (!touched) {
                if (field[x][y] != null && field[x][y].height == y * tileSize) {
                    touched = true;
                    lastTouchTime = System.currentTimeMillis();
                    if (holding != null) {
                        holding.angle = holding.dir * 90;
                        holding = null;
                    }
                    holding = remove(x, y);
                    moved = false;
                    twist.play();
                    holding.rotate();
                    lastRotTime = System.currentTimeMillis();
                    //this section only sets holding, make sure it doesn't need a value
                    offset.set((x * tileSize) - touchPos.x, (y * tileSize) - touchPos.y);
                    hrow = y;
                    hcolumn = x;
                    //System.out.println("X OFFSET IS " + offset.x);
                }
            }
            //for moving current piece side-to-side
            if (x != hcolumn || y != hrow) moved = true;
            tcolumn = x;
        } else {
            if (holding != null && touched) {
                int x = hcolumn;
                int y = hrow;
                if (moved || holding.height/tileSize > hrow) {
                    x = tcolumn;
                    y = findFloor(tcolumn);
                }
                place(holding, x, y);
                if (holding.angle % 90 == 0) {
                    checktion(x, y);
                }
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
            Tile previous = field[x][y];
            Tile loop = null;
            while (stack.size > 0) {
                Vector2 coord = stack.pop();
                int x0 = (int) coord.x;
                int y0 = (int) coord.y;
                System.out.println("CHECKING TILE AT " + x0 + ", " + y0);
                boolean open = false;
                if (field[x0][y0] != null) {
                    for (int i = 0; i < 4; i++) {
                        if (field[x0][y0].sides[i]) {
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
                            System.out.println("IS THERE A TILE AT " + x1 + ", " + y1 + "? ");
                            if (x1 >= 0 && x1 < 7 && y1 >= 0 && y1 <= ceiling && field[x1][y1] != null && field[x1][y1].height == y1 * tileSize && field[x1][y1].sides[(i + 2) % 4]) {
                                //System.out.println("YES");
                                if (!visited.contains(field[x1][y1], true)) {
                                    //System.out.println("WE HAVEN'T RECORDED THIS TILE YET, ADDING TO THE STACK");
                                    stack.add(new Vector2(x1, y1));
                                    visited.add(field[x1][y1]);
                                } else if (field[x1][y1] != previous) {
                                    loop = field[x0][y0];
                                }
                            } else { //clear everything, the path is not valid
                                //System.out.println("NO, NO PATH");
                                stack.clear();
                                open = true;
                            }
                        }
                    }
                    previous = field[x0][y0];
                    //System.out.println("FINISHED CHECKING TILE AT " + x0 + ", " + y0);
                }
                if (open) {
                    visited.clear();
                    break;
                }
            }
            if (visited.size > 0) {
                bust.play();
                dframes = 10;
            }
            System.out.println(visited.size + " TILES ARE CONNECTED");
            for (int i = 0; i < visited.size; i++) {
                score = score + 10;
                flux = flux + 100;
                tiles--;
                visited.get(i).xpos = x * tileSize;
                visited.get(i).connected = true;
            }
            visited.clear();
        }
    }

    private void drawHolding() {
        //draw the piece being held
        if (holding != null) {
            game.batch.setColor(colors[holding.type].r, colors[holding.type].g, colors[holding.type].b, 0.5f*holding.transparency);
            if (touched) {
                int row = findFloor(hcolumn);
                if (row > 0 && field[hcolumn][row - 1] != null && field[hcolumn][row - 1].height > ((row - 1) * tileSize) + (tileSize / 2)) {
                    row = row - 1;
                }
                if (moved) {
                    holding.height = row * tileSize;
                } else if (field[hcolumn][hrow] != null && field[hcolumn][hrow].height < (hrow * tileSize) + (tileSize / 2)) {
                    holding.height = holding.height + holding.velocity;
                    if (holding.height < (row * tileSize)) {
                        holding.velocity = (float) (((row * tileSize) - (hrow * tileSize)) / Math.sqrt(tileSize / 1.9f));
                    } else {
                        holding.velocity = 0;
                        holding.height = row * tileSize;
                        moved = true;
                    }
                }
            }
            int y = (int) ((holding.height) / tileSize);
            if (System.currentTimeMillis() - lastRotTime < 600) {
                float time = (float) (System.currentTimeMillis() - lastRotTime) / 200;
                if (time <= 1) {
                    holding.angle = new Interpolation.SwingOut(1.5f).apply((holding.dir - 1) * 90, holding.dir * 90, time);
                } else {
                    holding.angle = holding.dir * 90;
                    if (!touched) {
                        holding = null;
                        checktion(hcolumn, y);
                    }
                }
            } else {
                lastRotTime = System.currentTimeMillis();
                twist.play();
                holding.rotate();
            }
            if (touched) {
                if (findFloor(tcolumn) < ceiling) hcolumn = tcolumn;
                game.batch.draw(new TextureRegion(types.get(holding.type)), tileSize * hcolumn, holding.height, (float) Math.ceil(tileSize / 2), (float) Math.ceil(tileSize / 2), tileSize, tileSize, 1, 1, holding.angle);
            }
        }
    }

    private void drawField() {
        //draw the field
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < ceiling; y++) {
                if (field[x][y] != null) {
                    Tile tile = field[x][y];
                    if (tile.transparency < 1) tile.transparency = tile.transparency + 0.05f;
                    game.batch.setColor(colors[tile.type].r, colors[tile.type].g, colors[tile.type].b, tile.transparency);
                    if (tile.height > y * tileSize) {
                        tile.height = tile.height - tile.velocity;
                        if (tile.height > (y * tileSize)) {
                            tile.velocity = tile.velocity + 1.9f;
                        } else {
                            tile.height = y * tileSize;
                            drop.play();
                            tile.velocity = 0;
                            if (y >= ceiling) {
                                end = true;
                                gameover.play();
                            }
                            else
                                checktion(x, y);
                        }
                    } else if (tile.height < (y * tileSize)) {
                        tile.height = tile.height + tile.velocity;
                        if (tile.height < (y * tileSize)) {
                            tile.velocity = tile.velocity + 1.9f;
                        } else {
                            tile.height = y * tileSize;
                            tile.velocity = 0;
                            checktion(x, y);
                        }
                    }
                    game.batch.draw(new TextureRegion(types.get(tile.type)), tileSize * x, tile.height, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, tile.angle);
                }
            }
        }

        //hud

        game.batch.setColor(Color.WHITE);
        game.batch.draw(new TextureRegion(square), 0, game.camera.viewportHeight - tileSize, (tileSize / 2), (tileSize / 2), game.camera.viewportWidth, tileSize, 1, 1, 0);
        game.batch.setColor(Color.BLACK);
        game.batch.draw(new TextureRegion(square), tileSize / 5, (game.camera.viewportHeight - tileSize) + tileSize / 5, (tileSize / 2), (tileSize / 2), game.camera.viewportWidth - 2 * tileSize / 5, 3 * tileSize / 5, 1, 1, 0);
        game.batch.setColor(Color.WHITE);
        game.batch.draw(new TextureRegion(square), tileSize / 5, (game.camera.viewportHeight - tileSize) + tileSize / 5, (tileSize / 2), (tileSize / 2), (game.camera.viewportWidth - 2 * tileSize / 5)*(flux/10000f), 3 * tileSize / 5, 1, 1, 0);
        font.setColor(Color.WHITE);
        font.draw(game.batch, Integer.toString(score), tileSize / 10 + 4, (int) (game.camera.viewportHeight - tileSize / 5) + 4);
        game.batch.setColor(colors[current.type]);
        startTime = startTime;
        float time = 1 - (float) (currentTime - startTime) / (float) waitTime;
        game.batch.draw(cursor, tileSize * tcolumn, tileSize * (ceiling - 1.5f),            /* reposition to draw from half way up from the original sprite position */
                tileSize / 2,
                tileSize / 4,
                (float) (tileSize * time),
                tileSize / 2,
                1,
                1,
                0,
                0,
                0,
                (int) (tileSize * time),
                tileSize / 2, /* only use the texture data from the top of the sprite */
                false,
                false);
        game.batch.setColor(1, 1, 1, 1);
    }

    private void drawConnected() {
        game.batch.setColor(Color.WHITE);
        dframes = dframes - 1;
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < ceiling; y++) {
                if (field[x][y] != null) {
                    field[x][y].transparency = 0;
                    if (field[x][y].connected) {
                        System.out.println("TILE AT " + x + ", " + y + " IS CONNECTED");
                        game.batch.draw(new TextureRegion(types.get(field[x][y].type)), tileSize * x, tileSize * y, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, field[x][y].angle);
                        if (dframes <= 1) {
                            field[x][y].transparency = 1;
                            dblocks.add(remove(x, y));
                            System.out.println("ADDED TO DBLOCKS");
                        }
                    }
                }
            }
        }
    }

    private void drawEnd() {
        boolean falling = false;
        //draw the field
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < ceiling; y++) {
                if (field[x][y] != null) {
                    Tile tile = field[x][y];
                    game.batch.setColor(colors[tile.type].r, colors[tile.type].g, colors[tile.type].b, 1);
                    int height = (y - step) * tileSize;
                    if (tile.height > height) {
                        falling = true;
                        tile.height = tile.height - tile.velocity;
                        tile.velocity = tile.velocity + 1.9f;
                    } else if (tile.height < height) {
                        tile.height = height;
                        tile.velocity = 0;
                    }
                    game.batch.draw(new TextureRegion(types.get(tile.type)), tileSize * x, tile.height, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, tile.angle);
                }
            }
        }
        if (!falling) {
            if (step > ceiling)
                game.setScreen(new Menu(game));
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

    private void place(Tile tile, int x, int y) {
        adjustColumn(x, y);
        field[x][y] = tile;
        System.out.println("SOMETHING EXISTS HERE");
    }

    private Tile remove(int x, int y) {
        Tile tile = field[x][y];
        field[x][y] = null;
        adjustColumn(x, y);
        return tile;
    }

    private void drawParticles() {
        //show exploding pieces
        for (int i = 0; i < dblocks.size; i++) {
            Tile block = dblocks.get(i);
            if (block.xpos < -tileSize || block.xpos > 1080 + tileSize || block.height < -tileSize || block.transparency <= 0) {
                dblocks.removeIndex(i);
            } else {
                block.transparency = block.transparency - 0.01f;
                block.height = block.height - block.velocity;
                block.velocity = block.velocity + block.height/tileSize;
                block.xpos = block.xpos + ((rng.nextInt(2) - 1) * block.velocity) + (block.xpos/tileSize) - 3;
                if (block.angle <= 0) {
                    block.angle = block.angle + 1;
                } else {
                    block.angle = block.angle - 1;
                }
                game.batch.setColor(1, 1, 1, block.transparency);
                game.batch.draw(new TextureRegion(square), block.xpos, block.height, tileSize / 2, tileSize / 2, tileSize / pScale, tileSize / pScale, 1, 1, block.angle);
            }
        }
    }

    private void prepare() {
        gameover = Gdx.audio.newSound(Gdx.files.internal("gameover.wav"));
        thud = Gdx.audio.newSound(Gdx.files.internal("thud.wav"));
        drop = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        twist = Gdx.audio.newSound(Gdx.files.internal("twist.wav"));
        bust = Gdx.audio.newSound(Gdx.files.internal("bust.wav"));
        generator = new FreeTypeFontGenerator(Gdx.files.internal("arcade.TTF"));

        int unit = (int)Math.floor(200/5);
        int half = (int)Math.floor(200/2);

        //making the cursor
        pixmap = new Pixmap(200, 100, Pixmap.Format.RGBA8888); //try RGBA4444 later
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(Color.WHITE);
        pixmap.fillTriangle(0, 0, half, 100, 200, 0);
        cursor = new Texture(pixmap);

        //making the "bare" tile
        pixmap = new Pixmap(200, 200, Pixmap.Format.RGBA8888); //try RGBA4444 later
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        square = new Texture(pixmap);

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
        pixmap.fillTriangle(200,2*unit,(int)(unit*4.5f),half,200,3*unit); //right
        pixmap.fillTriangle(2*unit,0,half,unit/2,3*unit,0); //top
        pixmap.fillTriangle(2*unit,200,half,(int)(4.5f*unit),3*unit,200); //bottom
        types.add(new Texture(pixmap));

        //making the "i" tile
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixmap.setColor(Color.CLEAR);
        pixmap.fillRectangle(unit,unit,3*unit,3*unit);
        pixmap.fillRectangle(2*unit,0,unit,half);
        types.add(new Texture(pixmap));

        //making the "l" tile
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixmap.setColor(Color.CLEAR);
        pixmap.fillRectangle(unit,unit,3*unit,3*unit);
        pixmap.fillRectangle(0,2*unit,200,unit);
        types.add(new Texture(pixmap));

        //making the "r" tile
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixmap.setColor(Color.CLEAR);
        pixmap.fillRectangle(unit,unit,3*unit,3*unit);
        pixmap.fillRectangle(2*unit,0,unit,half);
        pixmap.fillRectangle(half,2*unit,half,unit);
        types.add(new Texture(pixmap));

        //making the "t" tile
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixmap.setColor(Color.CLEAR);
        pixmap.fillRectangle(unit,unit,3*unit,3*unit);
        pixmap.fillRectangle(2*unit,0,unit,200);
        pixmap.fillRectangle(half,2*unit,half,unit);
        types.add(new Texture(pixmap));

        //making the "plus" tile
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixmap.setColor(Color.CLEAR);
        pixmap.fillRectangle(unit,unit,3*unit,3*unit);
        pixmap.fillRectangle(2*unit,0,unit,200);
        pixmap.fillRectangle(0,2*unit,200,unit);
        types.add(new Texture(pixmap));

        field = new Tile[7][ceiling + 1];

        current = newTile();
    }
}
