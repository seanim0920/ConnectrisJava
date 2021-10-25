//challenge: blinking cursor? blinking boxes?

package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
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
    private int max = 100;
    private int flux = 0;
    private boolean end = false;
    private int step = 1;
    private float brightness = 1;

    private Music gameover;
    private Sound thud;
    private Sound drop;
    private Sound twist;
    private Sound bust;
    private Random rng = new Random();
    //resources
    private Array<Vector2> corners = new Array<Vector2>();
    private Pixmap pixmap;
    private Array<Texture> types = new Array<Texture>();
    private Color[] colors = {Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, new Color(0.25f,0.25f,1,1), Color.MAGENTA};
    private Texture square;
    private Texture cursor;

    //game variables
    private int score = 0;
    private int waitTime = 1200;
    private long currentTime = 0;
    private long startTime = 0;
    private int tileSize = 154;
    private int pScale = 5;
    private int next = 0; //next piece to drop

    //variables for the well
    private int ceiling = 11;
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
    Vector3 touchPos = new Vector3();

    private boolean paused = false;

    //for destroyed blocks
    private Array<Tile> dblocks = new Array<Tile>();

    //to generate new tiles
    private Tile newTile() {
        int type = next;
        next = rng.nextInt(100);
        if (next < 10)
            next = 5;
        else if (next < 25)
            next = 4;
        else if (next < 50)
            next = 3;
        else if (next < 75)
            next = 2;
        else if (next < 90)
            next = 1;
        else
            next = 0;

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

            if (flux > 0) flux = flux - 2;
            currentTime = currentTime + ((long)(Gdx.graphics.getDeltaTime()*1000)-(findSpace(tcolumn)));

            if (currentTime - startTime >= waitTime) {
                startTime = System.currentTimeMillis();
                currentTime = startTime;
                //if enough time has elapsed
                if (tiles < max) {
                    if (findFloor(tcolumn) <= ceiling) {
                        tiles++;
                        place(current, tcolumn, findFloor(tcolumn));
                        current = newTile();
                    } else {
                        end = true;
                    }
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
            touchPos = game.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            int x = (int) ((touchPos.x) / tileSize);
            int y = (int) ((touchPos.y) / tileSize);
            //if touch is within range
            if (!touched) {
                if (field[x][y] != null && field[x][y].height == y * tileSize) {
                    touched = true;
                    lastTouchTime = System.currentTimeMillis();
                    holding = remove(x, y);
                    holding.checked = false;
                    moved = false;
                    twist.play();
                    holding.rotate();
                    holding.lastRotTime = System.currentTimeMillis();
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
            touched = true;
        } else {
            if (holding != null && touched) {
                int x = hcolumn;
                int y = hrow;
                if (moved || holding.height/tileSize > y) {
                    y = findSpace(x);
                }
                place(holding, x, y);
                holding = null;
            }
            touched = false;
        }
    }

    private void checktion(int x, int y) {
        if (field[x][y] != null) {
            Array<Tile> caught = new Array<Tile>();
            Array<Tile> visited = new Array<Tile>();
            Array<Vector2> stack = new Array<Vector2>();
            stack.add(new Vector2(x, y));
            visited.add(field[x][y]);
            while (stack.size > 0) {
                Vector2 coord = stack.pop();
                int x0 = (int) coord.x;
                int y0 = (int) coord.y;
                //System.out.println("CHECKING TILE AT " + x0 + ", " + y0);
                boolean open = true;
                if (field[x0][y0] != null) {
                    field[x0][y0].checked = true;
                    for (int i = 0; i < 4; i++) {
                        if (field[x0][y0].sides[i]) {
                            //System.out.println("SIDE " + i + " IS OPEN");
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
                            //System.out.println("IS THERE A TILE AT " + x1 + ", " + y1 + "? ");
                            if (x1 >= 0 && x1 < 7 && y1 >= 0 && y1 <= ceiling && field[x1][y1] != null && field[x1][y1].height == y1 * tileSize && field[x1][y1].angle % 90 == 0 && field[x1][y1].sides[(i + 2) % 4]) {
                                if (field[x0][y0].type != 0 || field[x1][y1].type != 0) { //why does this work???
                                    open = false;
                                    //System.out.println("YES");
                                    if (!visited.contains(field[x1][y1], true)) {
                                        //System.out.println("WE HAVEN'T RECORDED THIS TILE YET, ADDING TO THE STACK");
                                        stack.add(new Vector2(x1, y1));
                                        field[x1][y1].xpos = x1 * tileSize;
                                        visited.add(field[x1][y1]);
                                        field[x1][y1].parent = new Vector2(x0, y0);
                                    } else if (field[x0][y0].parent.x != x1 && field[x0][y0].parent.y != y1) {
                                        System.out.println("FOUND A LOOP, PARENT OF " + x0 + ", " + y0 + " IS " + field[x0][y0].parent + "BUT IT'S ALSO CONNECTED TO " + x1 + ", " + y1);
                                        Tile c = field[x0][y0];
                                        Vector2 vch = new Vector2(x1, y1);
                                        Vector2 vc = new Vector2(x0, y0);
                                        Vector2 vp = c.parent;
                                        while (c.parent.x >= 0 && c.parent.y >= 0 && field[(int) c.parent.x][(int) c.parent.y] != null) {
                                            System.out.println("CHECKING IF " + c.parent.x + ", " + c.parent.y + " IS A CORNER");
                                            vp = c.parent;
                                            if (c.type == 0) {
                                                caught.clear();
                                                break;
                                            } else if (((vch.x - vc.x != vc.x - vp.x) || vch.y - vc.y != vc.y - vp.y)) {
                                                System.out.println("FOUND A CORNER");
                                                corners.add(vc);
                                                for (int col = 0; col <= vc.x; col++) {
                                                    for (int row = 0; row <= vc.y; row++) {
                                                        if (field[col][row] != null) {
                                                            if (!caught.contains(field[col][row], true)) {
                                                                field[col][row].xpos = col * tileSize;
                                                                caught.add(field[col][row]);
                                                            } else
                                                                caught.removeValue(field[col][row], true);
                                                        }
                                                    }
                                                }
                                            }
                                            c = field[(int) c.parent.x][(int) c.parent.y];
                                            vch = vc;
                                            vc = vp;
                                        }
                                        vp = new Vector2(x1, y1);
                                        System.out.println("CHECKING IF " + c.parent.x + ", " + c.parent.y + " IS A CORNER");
                                        if (c.type == 0) {
                                            caught.clear();
                                            break;
                                        } else if (((vch.x - vc.x != vc.x - vp.x) || vch.y - vc.y != vc.y - vp.y)) {
                                            System.out.println("FOUND A CORNER");
                                            corners.add(vc);
                                            for (int col = 0; col < vc.x; col++) {
                                                for (int row = 0; row < vc.y; row++) {
                                                    if (field[col][row] != null && !visited.contains(field[col][row], true)) {
                                                        if (!caught.contains(field[col][row], true))
                                                            caught.add(field[col][row]);
                                                        else
                                                            caught.removeValue(field[col][row], true);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else { //clear everything, the path is not valid
                                /*
                                if (x1 < 0 || x1 >= 7 || y1 < 0 || y1 > ceiling)
                                    System.out.println("ONE SIDE IS OUT OF BOUNDS");
                                else if (field[x1][y1] == null)
                                    System.out.println("ONE SIDE IS NOT CONNECTED TO ANYTHING");
                                else if (field[x1][y1].height != y1 * tileSize)
                                    System.out.println("IT'S CONNECTED TO A PIECE BUT THAT PIECE IS STILL MOVING");
                                else if (field[x1][y1].angle % 90 != 0)
                                    System.out.println("IT'S CONNECTED TO A PIECE BUT THAT PIECE IS STILL SPINNING");
                                else if (!field[x1][y1].sides[(i + 2) % 4])
                                    System.out.println("IT'S CONNECTED TO A PIECE BUT THAT PIECE ISN'T CONNECTED TO THIS ONE");
                                else if (field[x1][y1].type == 0)
                                    System.out.println("IT'S CONNECTED TO A BLOCK");
                                else
                                    System.out.println("UNKNOWN ERROR");
                                */

                                if (field[x0][y0].type > 0) {
                                    open = true;
                                    break;
                                } else if (!open)
                                    break;
                            }
                        }
                    }
                    //System.out.println("FINISHED CHECKING TILE AT " + x0 + ", " + y0);
                }
                if (open) {
                    stack.clear();
                    caught.clear();
                    for (int i = 0; i < visited.size; i++) {
                        visited.get(i).parent = new Vector2(-1,-1);
                    }
                    visited.clear();
                    break;
                }
            }
            if (visited.size > 0) {
                brightness = 0;
                bust.play();
                dframes = 10;
                System.out.println(caught.size + " TILES ARE CAUGHT");
                System.out.println(visited.size + " TILES ARE CONNECTED");
            }
            for (int i = 0; i < visited.size; i++) {
                if (caught.contains(visited.get(i), true)) caught.removeValue(visited.get(i), true);
                if (visited.get(i).type > 0) {
                    score = score + (i * 10);
                    flux = flux + (score * 10);
                }
                waitTime--;
                visited.get(i).connected = true;
            }
            for (int i = 0; i < caught.size; i++) {
                System.out.println((caught.get(i).xpos / tileSize) + ", " + (caught.get(i).height / tileSize) + " IS CAUGHT");
                score = score + (i * 20);
                flux = flux + (score * 20);
                caught.get(i).caught = true;
            }
        }
    }

    private void drawHolding() {
        //draw the piece being held
        if (holding != null) {
            game.batch.setColor(colors[holding.type].r, colors[holding.type].g, colors[holding.type].b, 0.5f*brightness);
            float time = (float) (System.currentTimeMillis() - holding.lastRotTime) / 200;
            if (time <= 1) {
                holding.angle = new Interpolation.SwingOut(1.5f).apply((holding.dir - 1) * 90, holding.dir * 90, time);
            } else {
                holding.angle = holding.dir * 90;
            }
            if (touched) {
                if (findFloor(tcolumn) <= ceiling) hcolumn = tcolumn;
                game.batch.draw(new TextureRegion(types.get(holding.type)), tileSize * hcolumn, holding.height, (float) Math.ceil(tileSize / 2), (float) Math.ceil(tileSize / 2), tileSize, tileSize, 1, 1, holding.angle);
                int row = findSpace(hcolumn);
                holding.height = row * tileSize;
                if (row == ceiling) {
                    place(holding, hcolumn, row);
                    holding = null;
                }
            }
        }
    }

    private void drawField() {
        //draw the field
        if (brightness < 1)
            brightness = brightness + 0.05f;
        for (int x = 0; x < 7; x++) {
            for (int y = ceiling; y >= 0; y--) {
                //if (corners.contains(new Vector2(x, y), false))
                    //game.batch.draw(new TextureRegion(square), tileSize * x, tileSize * y, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 0.2f, 0.2f, 0);
                if (field[x][y] != null) {
                    Tile tile = field[x][y];
                    game.batch.setColor(1,0,0,1-tile.opacity);
                    game.batch.draw(new TextureRegion(square), tileSize * x, tileSize * y, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, 0);
                    if (tile.opacity < 1)
                        tile.opacity = tile.opacity + 0.05f;
                    game.batch.setColor(colors[tile.type].r, colors[tile.type].g, colors[tile.type].b, brightness);
                    if (tile.height != y * tileSize) {
                        tile.checked = false;
                        if (tile.height > y * tileSize) {
                            if (tile.height - tile.velocity > y * tileSize) {
                                tile.height = tile.height - tile.velocity;
                                tile.velocity = tile.velocity + 1.9f;
                            } else {
                                tile.height = y * tileSize;
                                tile.velocity = 0;
                                if (y == findFloor(x) - 1)
                                    drop.play();
                            }
                        } else {
                            tile.height = tile.height + tile.velocity;
                            if (tile.height < (y * tileSize)) {
                                tile.velocity = tile.velocity + 1.9f;
                            } else {
                                tile.height = y * tileSize;
                                tile.velocity = 0;
                            }
                        }
                    } else if (!tile.checked) {
                        if (y == ceiling) {
                            for (int i = 0; i <= ceiling; i++) {
                                if (field[x][i] != null) {
                                    field[x][i].opacity = 0;
                                }
                            }
                        }
                        float time = (float) (System.currentTimeMillis() - tile.lastRotTime) / 200;
                        if (time <= 1) {
                            tile.checked = false;
                            tile.angle = new Interpolation.SwingOut(1.5f).apply((tile.dir - 1) * 90, tile.dir * 90, time);
                        } else if (!tile.checked) {
                            tile.angle = tile.dir * 90;
                            checktion(x, y);
                            if (tile.type == 0 && y == 0) {
                                tile.xpos = x * tileSize;
                                tile.caught = true;
                                dframes = 10;
                            }
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
        float time = 1 - (float) (currentTime - startTime) / (float) waitTime;
        game.batch.draw(cursor, tileSize * tcolumn, tileSize * (ceiling - 0.5f),            /* reposition to draw from half way up from the original sprite position */
                tileSize / 2,
                tileSize / 4,
                (float) (tileSize * time),
                tileSize/2,
                1,
                1,
                0,
                0,
                0,
                (int) (tileSize * time),
                tileSize, /* only use the texture data from the top of the sprite */
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
                    if (field[x][y].connected) {
                        //System.out.println("TILE AT " + x + ", " + y + " IS CONNECTED");
                        game.batch.draw(new TextureRegion(types.get(field[x][y].type)), tileSize * x, tileSize * y, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, field[x][y].angle);
                        if (dframes <= 1) {
                            field[x][y].opacity = 1;
                            if (field[x][y].type > 0) {
                                dblocks.add(remove(x, y));
                                tiles--;
                            }
                            else
                                field[x][y].connected = false;
                            //System.out.println("ADDED TO DBLOCKS");
                        }
                    } else if (field[x][y].caught) {
                        game.batch.draw(new TextureRegion(square), tileSize * x, tileSize * y, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, 0);
                        if (dframes <= 1) {
                            field[x][y].opacity = 1;
                            dblocks.add(remove(x, y));
                            tiles--;
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

    private int findSpace(int x) {
        int i = findFloor(x);
        for (int y = i - 1; y >= 0; y--) {
            if (field[hcolumn][y] != null && field[hcolumn][y].height > ((y) * tileSize) + (tileSize / 2)) {
                i = y;
            }
        }
        return i;
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
        for (int i = 0; i < dblocks.size; i++) {
            Tile block = dblocks.get(i);
            if (block.xpos < -tileSize || block.xpos > 1080 + tileSize || block.height < -tileSize || block.opacity <= 0) {
                dblocks.removeIndex(i);
            } else {
                block.opacity = block.opacity - 0.01f;
                game.batch.setColor(1, 1, 1, block.opacity);
                if (block.connected) {
                    block.height = block.height - block.velocity;
                    block.velocity = block.velocity + block.height / tileSize;
                    block.xpos = block.xpos + ((rng.nextInt(2) - 1) * block.velocity) + (block.xpos / tileSize) - 3;
                    if (block.angle <= 0) {
                        block.angle = block.angle + 1;
                    } else {
                        block.angle = block.angle - 1;
                    }
                    game.batch.draw(new TextureRegion(square), block.xpos, block.height, tileSize / 2, tileSize / 2, tileSize / pScale, tileSize / pScale, 1, 1, block.angle);
                }
            }
        }
    }

    private void prepare() {
        gameover = Gdx.audio.newMusic(Gdx.files.internal("gameover.wav"));
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
        pixmap.fillTriangle(0, 0, half, half, 200, 0);
        cursor = new Texture(pixmap);

        //making the "bare" tile
        pixmap = new Pixmap(200, 200, Pixmap.Format.RGBA8888); //try RGBA4444 later
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        square = new Texture(pixmap);

        //making the "box" tile
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixmap.setColor(Color.CLEAR);
        pixmap.fillRectangle(unit,unit,3*unit,3*unit);
        pixmap.setColor(Color.WHITE);
        pixmap.fillTriangle(unit,2*unit,(int)(unit*1.5),half,unit,3*unit); //left
        pixmap.fillTriangle(4*unit,2*unit,(int)(unit*3.5),half,4*unit,3*unit); //right
        pixmap.fillTriangle(2*unit,unit,half,(int)(unit*1.5),3*unit,unit); //top
        pixmap.fillTriangle(2*unit,4*unit,half,(int)(3.5*unit),3*unit,4*unit); //bottom
        pixmap.setColor(Color.CLEAR);
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