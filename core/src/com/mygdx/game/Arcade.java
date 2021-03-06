//challenge: blinking cursor? blinking boxes?
//bug: block tile does not destroy if it's surrounded by tiles
//idea: an actual block tile that can't connect to anything and can only get destroyed by touching the bottom or getting captured
//idea: a tile that can connect to anything but itself

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

public class Arcade extends Unit implements Screen {
    FreeTypeFontGenerator generator;
    FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
    BitmapFont font;

    private int dframes = 0;

    private int tiles = 0;
    private int max = 100;
    private int flux = 0;
    private Label label;
    private Label label2;
    private Label label3;
    private Label label4;
    private boolean end = false;
    private int step = 1;
    private float brightness = 1;

    protected Music gameover;
    protected Sound thud;
    protected Sound drop;
    protected Sound danger;
    protected Sound twist;
    protected Sound bust;
    //resources
    //private Array<Vector2> corners = new Array<Vector2>();
    private Texture cursor;
    private Pixmap pixmap;

    //game variables
    private int score = 0;
    private int waitTime = 3200;
    private long currentTime = 0;
    private long startTime = 0;
    private int pScale = 5;
    private int next = 0; //next piece to drop

    //variables for the well
    private int ceiling = 11;

    //variables for the current falling piece
    private Tile current = null;
    private int ccolumn = 3;

    //variables for input
    boolean touched = false;
    long lastTouchTime;

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
        else if (next < 55)
            next = 3;
        else if (next < 70)
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

    public Arcade(final Main game) {
        super(game);
        prepare();

        parameter.size = 150;
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
                    processTouch();
                } else {
                    drawConnected();
                }
            } else {
                game.setScreen(new Death(game, field));
            }

            if (flux > 0) flux = flux - 2;
            currentTime = currentTime + ((long)(Gdx.graphics.getDeltaTime()*1000)-(findSpace(tcolumn)/2));

            if (currentTime - startTime >= waitTime) {
                startTime = System.currentTimeMillis();
                currentTime = startTime;
                //if enough time has elapsed
                if (tiles < max) {
                    int floor = findFloor(ccolumn);
                    if (floor <= ceiling) {
                        tiles++;
                        place(current, ccolumn, floor);
                        current = newTile();
                        ccolumn = ccolumn + 1;
                        ccolumn = ccolumn % 8;
                    }
                }
            }
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

    public void processTouch() {
        if (touched) {
            int x = (int) ((touchPos.x) / tileSize);
            int y = (int) ((touchPos.y) / tileSize);
            //if touch is within range
            if (holding == null) {
                if (field[x][y] != null && field[x][y].height == y * tileSize) {
                    if (field[x][y].type > 0) {
                        lastTouchTime = System.currentTimeMillis();
                        holding = remove(x, y);
                        holding.checked = false;
                        moved = false;
                        twist.play();
                        //this section only sets holding, make sure it doesn't need a value
                        hrow = y;
                        hcolumn = x;
                        //System.out.println("X OFFSET IS " + offset.x);
                    } else if (field[x][y].opacity >= 1) {
                        field[x][y].opacity = 0;
                        danger.play();
                    }
                }
            }
            //for moving current piece side-to-side
            if (x != hcolumn || y != hrow) moved = true;
            tcolumn = x;
        } else {
            if (holding != null && touched) {
                int x = hcolumn;
                int y = hrow;
                if (moved || holding.height/tileSize > y) {
                    y = findSpace(x);
                }
                if (!moved) {
                    holding.rotate();
                    holding.lastRotTime = System.currentTimeMillis();
                }
                place(holding, x, y);
                holding = null;
            }
        }
    }

    private void checktion(int x, int y) {
        if (field[x][y] != null && field[x][y].type > 0) {
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
                boolean open = false;
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
                                            //corners.add(vc);
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
                                        //corners.add(vc);
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

                                open = true;
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
                waitTime = waitTime - 10;
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
                                    game.drop.play();
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
                            danger.play();
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
                                bust.play();
                                tile.xpos = x * tileSize;
                                tile.connected = true;
                                dframes = 10;
                            }
                        }
                    }
                    game.batch.draw(new TextureRegion(types.get(tile.type)), tileSize * x, tile.height, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, tile.angle);
                }
            }
        }

        //hud

        //game.batch.setColor(Color.WHITE);
        //game.batch.draw(new TextureRegion(square), tileSize / 5, (game.camera.viewportHeight - tileSize) + tileSize / 5, (tileSize / 2), (tileSize / 2), (game.camera.viewportWidth - 2 * tileSize / 5)*(flux/10000f), 3 * tileSize / 5, 1, 1, 0);
        font.setColor(Color.WHITE);
        font.draw(game.batch, Integer.toString(score), tileSize / 10 + 5, (int) (game.camera.viewportHeight - tileSize / 5) + 4);
        game.batch.setColor(colors[current.type]);
        float time = 1 - (float) (currentTime - startTime) / (float) waitTime;
        game.batch.draw(cursor, tileSize * ccolumn, tileSize * (ceiling - 0.5f),            /* reposition to draw from half way up from the original sprite position */
                tileSize / 2,
                tileSize / 4,
                (float) (tileSize * time),
                tileSize/2,
                1,
                1,
                0,
                0,
                0,
                (int) (200 * time),
                100, /* only use the texture data from the top of the sprite */
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
                            dblocks.add(remove(x, y));
                            tiles--;
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
        generator = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));

        int unit = (int)Math.floor(200/5);
        int half = (int)Math.floor(200/2);

        //making the cursor
        pixmap = new Pixmap(200, 100, Pixmap.Format.RGBA8888); //try RGBA4444 later
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(Color.WHITE);
        pixmap.fillTriangle(0, 0, half, half, 200, 0);
        cursor = new Texture(pixmap);

        pixmap.dispose();

        field = new Tile[7][ceiling + 1];

        current = newTile();
    }
}