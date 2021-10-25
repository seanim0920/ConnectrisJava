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

    private Array<Vector2> corners = new Array<Vector2>();
    private int dframes = 0;

    private float distance = 0;
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
    private int hrow = 3;
    private int hcolumn = 3;
    private int yoffset = 200;
    private int xoffset = 0;

    private boolean held = false;
    protected Music gameover;
    protected Sound thud;
    protected Sound drop;
    protected Sound danger;
    protected Sound twist;
    protected Sound bust;
    //resources
    private Texture cursor;
    private Texture dot;
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

        tile.height = game.camera.viewportHeight;
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

    public void processTouching(boolean changed) {
        int x = (int) ((touchPos.x) / tileSize);
        int y = (int) ((touchPos.y - yoffset) / tileSize);
        if (y >= ceiling)
            y = ceiling;
        if (touchPos.y >= yoffset) {
            holding = field[x][y];
            hrow = x;
            hcolumn = y;
        } else if (!changed) {
            distance = distance + (touchPos.x - oldPos.x);
        }
        if (x > field.length)
            x = field.length;
        tcolumn = x;
    }

    public void processNotouch(boolean changed) {
        if (changed) {
            if (holding != null) {
                holding.rotate();
                if (holding.canMove) {
                    lastTouchTime = System.currentTimeMillis();
                    holding.checked = false;
                    game.twist.play();
                    //System.out.println("X OFFSET IS " + offset.x);
                } else if (holding.opacity >= 1) {
                    holding.opacity = 0;
                    game.twist.play();
                }
            }
            holding = null;
        }
        if (Math.abs(distance) <= 5) {
            if (distance % tileSize != 0) {
                if ((Math.abs(distance) > Math.abs(distance / tileSize) * tileSize && Math.abs(distance + (touchPos.x - oldPos.x)) > Math.abs(distance / tileSize) * tileSize)) {
                    distance = distance + (touchPos.x - oldPos.x);
                } else {
                    //do field manipulation stuff
                    distance = 0;
                    game.drop.play();
                }
            }
        } else {
            distance = distance - (Math.abs(distance)*(3/distance));
        }
    }

    private void checktion(int x, int y) {
        Vector2 start = new Vector2(-1,-1);
        Vector2 end = new Vector2(-1,-1);
        boolean loop = false;
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
                                    end = new Vector2(x0, y0);
                                    start = new Vector2(x1, y1);
                                    loop = true;
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
                if (loop) {
                    //System.out.println("FOUND A LOOP, PARENT OF " + x0 + ", " + y0 + " IS " + field[x0][y0].parent + "BUT IT'S ALSO CONNECTED TO " + x1 + ", " + y1);
                    Tile c = field[(int)end.x][(int)end.y];
                    while (c.parent.x >= 0 && c.parent.y >= 0 && field[(int)c.parent.x][(int)c.parent.y].parent != start) {
                        c = field[(int)c.parent.x][(int)c.parent.y];
                    }
                    Vector2 vch = c.parent;
                    Vector2 vc = start;
                    Vector2 vp = end;
                    c = field[(int)start.x][(int)start.y];
                    while (field[(int) c.parent.x][(int) c.parent.y] != null && c.parent != start) {
                        System.out.println("CHECKING IF " + c.parent.x + ", " + c.parent.y + " IS A CORNER");
                        if (((vch.x - vc.x != vc.x - vp.x) || vch.y - vc.y != vc.y - vp.y)) {
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
                        vp = c.parent;
                    }
                }
                brightness = 0;
                game.bust.play();
                dframes = 10;
                System.out.println(caught.size + " TILES ARE CAUGHT");
                System.out.println(visited.size + " TILES ARE CONNECTED");
            }
            for (int i = 0; i < visited.size; i++) {
                if (caught.contains(visited.get(i), true))
                    caught.removeValue(visited.get(i), true);
                if (visited.get(i).canMove) {
                    score = score + (i * 10);
                    flux = flux + (score * 10);
                } else {
                    score = score + (i * 20);
                    flux = flux + (score * 20);
                }
                visited.get(i).connected = true;
            }
            for (int i = 0; i < caught.size; i++) {
                System.out.println((caught.get(i).xpos / tileSize) + ", " + (caught.get(i).height / tileSize) + " IS CAUGHT");
                score = score + (i * 25);
                flux = flux + (score * 25);
                caught.get(i).caught = true;
            }
        }
    }

    public void drawText() {
        header.draw(game.batch, Integer.toString(score), 0, tileSize * 12);
    }

    private void drawField() {
        //draw the field
        if (brightness < 1)
            brightness = brightness + 0.05f;
        int s = 0;
        if (distance > 0)
            s = (int)-Math.ceil((int)(distance / tileSize));
        int e = field.length;
        if (distance < 0)
            e = field.length + (int)Math.ceil((int)(distance / tileSize));
        for (int x = s; x < e; x++) {
            for (int y = ceiling; y >= 0; y--) {
                if (corners.contains(new Vector2(x, y), false))
                    game.batch.draw(new TextureRegion(square), tileSize * x, tileSize * y, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 0.2f, 0.2f, 0);
                Tile tile;
                tile = field[x%7][y];
                if (tile != null) {
                    game.batch.setColor(1, 0, 0, 1 - tile.opacity);
                    game.batch.draw(new TextureRegion(square), tileSize * x, tileSize * y, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, 0);
                    if (tile.opacity < 1)
                        tile.opacity = tile.opacity + 0.05f;
                    game.batch.setColor(colors[tile.type].r, colors[tile.type].g, colors[tile.type].b, brightness);
                    if (!tile.canMove)
                        game.batch.setColor(colors[colors.length - 1].r, colors[colors.length - 1].g, colors[colors.length - 1].b, brightness);
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
                            if (!tile.canMove && y == 0) {
                                game.bust.play();
                                tile.xpos = x * tileSize;
                                tile.connected = true;
                                dframes = 10;
                            }
                        }
                    }
                    if (tile == holding) {
                        game.batch.setColor(Color.WHITE);
                    }
                    game.batch.draw(new TextureRegion(types.get(tile.type)), distance + xoffset + tileSize * x, yoffset + tile.height, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, tile.angle);
                }
            }
        }

        //hud

        //game.batch.setColor(Color.WHITE);
        //game.batch.draw(new TextureRegion(square), tileSize / 5, (game.camera.viewportHeight - tileSize) + tileSize / 5, (tileSize / 2), (tileSize / 2), (game.camera.viewportWidth - 2 * tileSize / 5)*(flux/10000f), 3 * tileSize / 5, 1, 1, 0);
        font.setColor(Color.WHITE);
        font.draw(game.batch, Integer.toString(score), tileSize / 10 + 5, (int) (game.camera.viewportHeight - tileSize / 5) + 4);
        game.batch.setColor(colors[current.type]);
        if (!current.canMove)
            game.batch.setColor(colors[colors.length-1]);
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

        //controls
        game.batch.setColor(Color.WHITE);
        game.batch.draw(new TextureRegion(square), 0, yoffset-5, (tileSize / 2), (tileSize / 2), game.camera.viewportWidth, 5, 1, 1, 0);

        for (int x = 0; x < field.length; x++) {
            game.batch.draw(new TextureRegion(game.pin), (distance % tileSize) + tileSize*x + 15, 0, (tileSize / 2), (tileSize / 2), 4*(tileSize/5), tileSize, 1, 1, 0);
        }
        if (distance > 0) {
            game.batch.draw(new TextureRegion(game.pin), (distance % tileSize) + tileSize*(-1) + 15, 0, (tileSize / 2), (tileSize / 2), 4*(tileSize/5), tileSize, 1, 1, 0);
        } else {
            game.batch.draw(new TextureRegion(game.pin), (distance % tileSize) + tileSize*(field.length) + 15, 0, (tileSize / 2), (tileSize / 2), 4*(tileSize/5), tileSize, 1, 1, 0);
        }

        if (Math.abs(distance) > tileSize) {
            if (distance < 0 && findFloor(0) > 0) {
                Tile[] temp = new Tile[findFloor(0)];
                for (int y = findFloor(0); y >= 0; y--) {
                    temp[y] = remove(0, y);
                }
                for (int x = 1; x < field.length; x++) {
                    for (int y = ceiling; y >= 0; y--) {
                        place(remove(x-1, y), x, y);
                    }
                }
                for (int y = temp.length; y >= 0; y--) {
                    place(temp[y], 0, y);
                }
                distance = distance + tileSize;
            }
            else if (distance > 0 && findFloor(field.length-1) > 0) {
                Tile[] temp = new Tile[findFloor(field.length-1)];
                for (int y = findFloor(field.length-1); y >= 0; y--) {
                    temp[y] = remove(0, y);
                }
                for (int x = 0; x < field.length-1; x++) {
                    for (int y = ceiling; y >= 0; y--) {
                        place(remove(x+1, y), x, y);
                    }
                }
                for (int y = temp.length; y >= 0; y--) {
                    place(temp[y], 0, y);
                }
                distance = distance - tileSize;
            }
        }
    }

    private void drawConnected() {
        game.batch.setColor(Color.WHITE);
        dframes = dframes - 1;
        for (int x = 0; x < field.length; x++) {
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
        int unit = (int)Math.floor(200/5);
        int half = (int)Math.floor(200/2);

        //making the cursor
        pixmap = new Pixmap(200, 100, Pixmap.Format.RGBA8888); //try RGBA4444 later
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(Color.WHITE);
        pixmap.fillTriangle(0, 0, half, half, 200, 0);
        cursor = new Texture(pixmap);

        pixmap.dispose();

        pixmap = new Pixmap(200, 200, Pixmap.Format.RGBA8888); //try RGBA4444 later
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(Color.WHITE);
        pixmap.fillCircle(100,100,100);
        dot = new Texture(pixmap);

        pixmap.dispose();

        current = newTile();
    }
}