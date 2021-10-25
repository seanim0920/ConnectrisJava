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

    private float speed = 0;
    private int yoffset = 200 + tileSize;
    private int xoffset = 0;
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

    //variables for the current falling piece
    private Tile current = null;
    private int ccolumn = 3;

    //variables for input
    long lastTouchTime;

    private boolean paused = false;

    //for destroyed blocks
    private Array<Tile> dblocks = new Array<Tile>();

    //to generate new tiles
    private Tile newTile() {
        int type = next;
        Tile tile;
        next = rng.nextInt(100);
        if (next < 15)
            tile = new Tile(Type.p);
        else if (next < 35)
            tile = new Tile(Type.t);
        else if (next < 60)
            tile = new Tile(Type.r);
        else if (next < 85)
            tile = new Tile(Type.l);
        else
            tile = new Tile(Type.i);
        if (current != null)
            tile.dir = current.dir;

        tile.ypos = game.camera.viewportHeight;
        if (rng.nextInt(10) == 1)
            tile.canMove = false;
        return tile;
    }

    public Arcade(final Main game, final Music music) {
        super(game);
        prepare();

        generator = new FreeTypeFontGenerator(Gdx.files.internal("font.otf"));
        parameter.size = 150;
        parameter.color = Color.WHITE;
        font = generator.generateFont(parameter); // font size 12 pixels
        music.play();
    }

    @Override
    public void show() {

    }

    public void process() {
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
                game.setScreen(new Death(game, field));
            }

            if (flux > 0) flux = flux - 2;
            currentTime = currentTime + ((long)(Gdx.graphics.getDeltaTime()*1000)-(findSpace(tcolumn)/2));

            if (currentTime - startTime >= waitTime) {
                waitTime--;
                startTime = System.currentTimeMillis();
                currentTime = startTime;
                //if enough time has elapsed
                if (tiles < max) {
                    int floor = findFloor(ccolumn);
                    if (floor <= ceiling) {
                        tiles++;
                        place(current, ccolumn, floor);
                        current = newTile();
                        ccolumn = (ccolumn + 1) % 7;
                    }
                }
            }
        } else {
            drawPause();
        }
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
        if (dframes <= 0) {
            if (touched) {
                int x = (int) ((touchPos.x) / tileSize);
                int y = (int) ((touchPos.y) / tileSize);
                //if touch is within range
                if (holding == null) {
                    if (field[x][y] != null && field[x][y].ypos == y * tileSize) {
                        if (field[x][y].type > 0) {
                            lastTouchTime = System.currentTimeMillis();
                            holding = remove(x, y);
                            holding.checked = false;
                            moved = false;
                            game.twist.play();
                            //this section only sets holding, make sure it doesn't need a value
                            hrow = y;
                            hcolumn = x;
                            //System.out.println("X OFFSET IS " + offset.x);
                        } else if (field[x][y].opacity >= 1) {
                            field[x][y].opacity = 0;
                            game.danger.play();
                        }
                    }
                }
                //for moving current piece side-to-side
                if (x != hcolumn || y != hrow) moved = true;
                tcolumn = x;
            } else {
                if (holding != null) {
                    int x = hcolumn;
                    int y = hrow;
                    if (moved || holding.ypos / tileSize > y) {
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
    }

    private void checktion(Tile tile) {
        boolean containsLoop = false;
        //use BFS to see if the graph is connected
        Array<Tile> caught = new Array<Tile>();
        Array<Tile> graph = new Array<Tile>();
        Array<Tile> stack = new Array<Tile>();
        stack.add(tile);
        graph.add(tile);
        while (stack.size > 0) {
            Tile ctile = stack.pop();
            Vector2 ccoords = ctile.coords;
            System.out.println("CHECKING TILE AT " + ccoords.x + ", " + ccoords.y + ", WHOSE COORDINATES ARE " + new Vector2(ctile.xpos, ctile.ypos));
            boolean open = false;
            for (int i = 0; i < 4; i++) {
                if (ctile.sides[i]) {
                    System.out.println("SIDE " + i + " IS OPEN");
                    Vector2 chcoords = new Vector2();
                    switch (i) {
                        case 0:
                            chcoords.set(ccoords.x, ccoords.y + 1);
                            break;
                        case 1:
                            chcoords.set(ccoords.x - 1, ccoords.y);
                            break;
                        case 2:
                            chcoords.set(ccoords.x, ccoords.y - 1);
                            break;
                        case 3:
                            chcoords.set(ccoords.x + 1, ccoords.y);
                            break;
                        default:
                            break;
                    }
                    System.out.println("CCORDS IS " + ccoords + " AND i IS " + i);
                    System.out.println("CHECKING IF MATCHING TILE IS AT " + chcoords);
                    Tile chtile = getTile(chcoords);
                    if (chtile != null && chtile.placed && chtile.angle % 90 == 0 && ((i == 2 && ctile.coords.y == 0 && chtile.sides[i]) || (chtile.sides[(i + 2) % 4]))) {
                        System.out.println("YES, IT CONNECTS");
                        ctile.children[i] = chtile;
                        if (!graph.contains(chtile, true)) {
                            System.out.println("WE HAVEN'T RECORDED THIS TILE YET, ADDING THE TILE WHOSE COORDS ARE " + new Vector2(chtile.xpos, chtile.ypos) + " TO THE STACK");
                            stack.add(chtile);
                            graph.add(chtile);
                            chtile.parent = ctile;
                        } else if (chtile != ctile.parent) {//this isn't the same tile)
                            System.out.println("FOUND A LOOP");
                            containsLoop = true;
                        } else {
                            System.out.println("THIS IS JUST THE TILE'S PARENT, IGNORE IT");
                        }
                    } else { //clear everything, the path is not valid
                        if (chtile == null)
                            System.out.println("CHTILE IS NULL");
                            //else if (field[x1][y1].ypos != y1 * tileSize)
                            //System.out.println("IT'S CONNECTED TO A PIECE BUT THAT PIECE IS STILL MOVING");
                            //else if (field[x1][y1].angle % 90 != 0)
                            //System.out.println("IT'S CONNECTED TO A PIECE BUT THAT PIECE IS STILL SPINNING");
                        else if (!((i == 2 && ctile.coords.y == 0 && chtile.sides[i]) || ((chtile.sides[(i + 2) % 4]))))
                            System.out.println("NOT CONNECTED TO THIS ONE");
                            //else if (field[x1][y1].type == 0)
                            //System.out.println("IT'S CONNECTED TO A BLOCK");
                        else
                            System.out.println("UNKNOWN ERROR");

                        System.out.println("NO IT DOESNT");
                        open = true;
                        break;
                    }
                }
            }
            //System.out.println("FINISHED CHECKING TILE AT " + x0 + ", " + y0);
            if (open) {
                stack.clear();
                caught.clear();
                for (int i = 0; i < graph.size; i++) {
                    graph.get(i).parent = null;
                    for (int c = 0; c < graph.get(i).children.length; c++)
                        graph.get(i).children[c] = null;
                }
                graph.clear();
                break;
            }
        }
        if (graph.size > 0) {
            speed = speed + (graph.size)/(15*speed);
            brightness = 0;
            game.bust.play();
            dframes = 10;
            System.out.println(caught.size + " TILES ARE CAUGHT");
            System.out.println(graph.size + " TILES ARE CONNECTED");
        }
        for (int i = 0; i < graph.size; i++) {
            if (caught.contains(graph.get(i), true))
                caught.removeValue(graph.get(i), true);
            score = score + (i * 10);
            flux = flux + (score * 10);
            graph.get(i).connected = true;
        }
        for (int i = 0; i < caught.size; i++) {
            System.out.println((caught.get(i).xpos / tileSize) + ", " + (caught.get(i).ypos / tileSize) + " IS CAUGHT");
            score = score + (i * 25);
            flux = flux + (score * 25);
            caught.get(i).caught = true;
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
                game.batch.draw(new TextureRegion(types.get(holding.type)), tileSize * hcolumn, holding.ypos, (float) Math.ceil(tileSize / 2), (float) Math.ceil(tileSize / 2), tileSize, tileSize, 1, 1, holding.angle);
                int row = findSpace(hcolumn);
                holding.ypos = row * tileSize;
                if (row == ceiling) {
                    place(holding, hcolumn, row);
                    holding = null;
                }
            }
        }
    }

    private void updateTile(int x, int y) {
        //if (corners.contains(new Vector2(x, y), false))
        //game.batch.draw(new TextureRegion(square), tileSize * x, tileSize * y, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 0.2f, 0.2f, 0);
        Tile tile = getTile(new Vector2(x, y));
        if (tile != null) {
            if (tile.opacity < 1)
                tile.opacity = tile.opacity + 0.05f;
            game.batch.setColor(colors[tile.type].r, colors[tile.type].g, colors[tile.type].b, brightness);
            if (tile.ypos != tile.coords.y * tileSize) {
                tile.checked = false;
                if (tile.ypos > tile.coords.y * tileSize) {
                    if (tile.ypos - tile.velocity > tile.coords.y * tileSize) {
                        tile.ypos = tile.ypos - tile.velocity;
                        tile.velocity = tile.velocity + 1.9f;
                    } else {
                        tile.ypos = y * tileSize;
                        tile.velocity = 0;
                        if (tile.coords.y == findFloor((int)tile.coords.x) - 1)
                            game.drop.play();
                    }
                } else {
                    tile.ypos = tile.ypos + tile.velocity;
                    if (tile.ypos < (tile.coords.y * tileSize)) {
                        tile.velocity = tile.velocity + 1.9f;
                    } else {
                        tile.ypos = tile.coords.y * tileSize;
                        tile.velocity = 0;
                    }
                }
            } else {
                if (tile.coords.y == ceiling) {
                    game.danger.play();
                    for (int i = 0; i <= ceiling; i++) {
                        if (field[(int)tile.coords.x][i] != null) {
                            field[(int)tile.coords.x][i].opacity = 0;
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
                        game.bust.play();
                        tile.xpos = x * tileSize;
                        tile.connected = true;
                        dframes = 10;
                    }
                }
            }
            game.batch.setColor(colors[tile.type]);
            game.batch.draw(new TextureRegion(game.pixel), xoffset + tileSize * x, yoffset + tileSize * y, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, 0);
            game.batch.setColor(Color.WHITE);
            game.batch.draw(new TextureRegion(types, types.getHeight() * tile.type, 0, types.getHeight(), types.getHeight()), (((tile.dir + 1) % 4) / 2) + xoffset + tileSize * x, ((tile.dir) / 2) + 5 + yoffset + tile.ypos, tileSize / 2, tileSize / 2, tileSize, tileSize, 1, 1, tile.angle);
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
                    if (tile.ypos != y * tileSize) {
                        tile.checked = false;
                        if (tile.ypos > y * tileSize) {
                            if (tile.ypos - tile.velocity > y * tileSize) {
                                tile.ypos = tile.ypos - tile.velocity;
                                tile.velocity = tile.velocity + 1.9f;
                            } else {
                                tile.ypos = y * tileSize;
                                tile.velocity = 0;
                                if (y == findFloor(x) - 1)
                                    game.drop.play();
                            }
                        } else {
                            tile.ypos = tile.ypos + tile.velocity;
                            if (tile.ypos < (y * tileSize)) {
                                tile.velocity = tile.velocity + 1.9f;
                            } else {
                                tile.ypos = y * tileSize;
                                tile.velocity = 0;
                            }
                        }
                    } else if (!tile.checked) {
                        if (y == ceiling) {
                            game.danger.play();
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
                                game.bust.play();
                                tile.xpos = x * tileSize;
                                tile.connected = true;
                                dframes = 10;
                            }
                        }
                    }
                    game.batch.draw(new TextureRegion(types.get(tile.type)), tileSize * x, tile.ypos, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, tile.angle);
                }
            }
        }

        //hud

        //game.batch.setColor(Color.WHITE);
        //game.batch.draw(new TextureRegion(square), tileSize / 5, (game.camera.viewportHeight - tileSize) + tileSize / 5, (tileSize / 2), (tileSize / 2), (game.camera.viewportWidth - 2 * tileSize / 5)*(flux/10000f), 3 * tileSize / 5, 1, 1, 0);
        font.setColor(Color.WHITE);
        font.draw(game.batch, Integer.toString(score), tileSize / 10 + 5, (int) (game.camera.viewportHeight - tileSize / 5) + 4);
        game.batch.setColor(colors[current.type]);
        float time = (float) (currentTime - startTime) / (float) waitTime;
        game.batch.draw(cursor, tileSize * ccolumn, (tileSize * ((ceiling - 0.5f) + time)),            /* reposition to draw from half way up from the original sprite position */
                tileSize / 2,
                tileSize / 4,
                tileSize,
                tileSize/2,
                1,
                1,
                0,
                0,
                0,
                200,
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
            if (block.xpos < -tileSize || block.xpos > 1080 + tileSize || block.ypos < -tileSize || block.opacity <= 0) {
                dblocks.removeIndex(i);
            } else {
                block.opacity = block.opacity - 0.01f;
                game.batch.setColor(1, 1, 1, block.opacity);
                if (block.connected) {
                    block.ypos = block.ypos - block.velocity;
                    block.velocity = block.velocity + block.ypos / tileSize;
                    block.xpos = block.xpos + ((rng.nextInt(2) - 1) * block.velocity) + (block.xpos / tileSize) - 3;
                    if (block.angle <= 0) {
                        block.angle = block.angle + 1;
                    } else {
                        block.angle = block.angle - 1;
                    }
                    game.batch.draw(new TextureRegion(square), block.xpos, block.ypos, tileSize / 2, tileSize / 2, tileSize / pScale, tileSize / pScale, 1, 1, block.angle);
                }
            }
        }
    }

    private void prepare() {
        int unit = (int)Math.floor(200/5);
        int half = (int)Math.floor(200/2);

        //making the cursor
        pixmap = new Pixmap(200, 100, Pixmap.Format.RGBA8888); //try RGBA4444 later
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(Color.WHITE);
        pixmap.fillTriangle(0, 0, half, half, 200, 0);
        cursor = new Texture(pixmap);

        pixmap.dispose();

        current = newTile();
    }
}