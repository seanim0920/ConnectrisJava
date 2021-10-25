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

    private Array<Tile> corners = new Array<Tile>();
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
    private int hrow = 3;
    private int hcolumn = 3;
    private int yoffset = 200 + tileSize;
    private int xoffset = 0;
    private float speed = 1; //increases with each new tile destroyed
    private float ospeed = speed;
    private ImageButton down;
    private ImageButton rturn;
    private ImageButton lturn;

    private boolean moved = true;
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
            tile.incDir(current.dir, tileSize);

        tile.ypos = game.camera.viewportHeight;
        return tile;
    }

    public Arcade(final Main game, final Music music) {
        super(game);
        prepare();

        xoffset = (int)(game.camera.viewportWidth - ((tileSize)*field.length))/2;

        generator = new FreeTypeFontGenerator(Gdx.files.internal("font.otf"));
        parameter.size = tileSize - 5;
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
                //DEATH SCREEN
                //game.setScreen(new Death(game, field));
            }

            if (flux > 0) flux = flux - 2;
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
        game.batch.draw(new TextureRegion(game.pixel), 0, game.camera.viewportHeight - tileSize, (tileSize / 2), (tileSize / 2), game.camera.viewportWidth, tileSize, 1, 1, 0);
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
        int x = (int) ((touchPos.x - xoffset) / tileSize);
        int y = (int) ((touchPos.y - yoffset) / tileSize);
        if (y >= ceiling)
            y = ceiling;
        if (changed) {
            ospeed = speed;
            if (tcolumn != x) {
                moved = true;
            } else {
                moved = false;
            }
        }
        if (x >= 0 && x < field.length) {
            speed = speed + 0.1f;
            if (findFloor(x) <= current.ypos / tileSize)
                tcolumn = x;
        }
    }

    public void processNotouch(boolean changed) {
        int x = (int) ((touchPos.x - xoffset) / tileSize);
        int y = (int) ((touchPos.y - yoffset) / tileSize);
        if (changed) {
                if (x < 0) {
                    current.rotate(false);
                } else if (x >= field.length) {
                    current.rotate(true);
                }
                moved = false;
                speed = ospeed;
        }
    }

    public void drawText() {
        header.draw(game.batch, Integer.toString(score), 0, tileSize * 11);
    }

    private void drawField() {
        //draw the field
        if (brightness < 1)
            brightness = brightness + 0.05f;
        int s = -1;
        int e = field.length+1;
        for (int x = s; x < e; x++) {
            for (int y = ceiling; y >= 0; y--) {
                Tile tile;
                tile = field[((x%field.length)+field.length)%field.length][y];
                if (tile != null) {
                    game.batch.setColor(Color.WHITE);
                    if (tile.ypos != yoffset + y * tileSize + 5) {
                        tile.checked = false;
                        if (tile.ypos > y * tileSize) {
                            if (tile.ypos - tile.velocity > y * tileSize) {
                                tile.ypos = tile.ypos - tile.velocity;
                                tile.velocity = tile.velocity + 1.9f;
                            } else {
                                tile.ypos = y * tileSize;
                                tile.velocity = 0;
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
                        float time = (float) (System.currentTimeMillis() - tile.lastRotTime) / 200;
                        if (time <= 1) {
                            tile.checked = false;
                            tile.angle = new Interpolation.SwingOut(1.5f).apply((tile.dir - 1) * 90, tile.dir * 90, time);
                        } else {
                            tile.angle = tile.dir * 90;
                        }
                    }
                    if (tile == holding) {
                        game.batch.setColor(Color.WHITE);
                    }
                    game.batch.draw(new TextureRegion(types.get(tile.type)), (((tile.dir + 1) % 4) / 2) + xoffset + tileSize * x, ((tile.dir) / 2) + 5 + yoffset + tile.ypos, tileSize / 2, tileSize / 2, tileSize, tileSize, 1, 1, tile.angle);
                }
            }
        }

        current.angle = current.dir * 90;

        game.batch.draw(new TextureRegion(types.get(current.type)), (((current.dir + 1) % 4) / 2) + xoffset + tileSize * tcolumn, ((current.dir) / 2) + 5 + yoffset + current.ypos, tileSize / 2, tileSize / 2, tileSize, tileSize, 1, 1, current.angle);
        game.batch.setColor(colors[current.type]);
        game.batch.draw(new TextureRegion(game.pixel), 0, yoffset + current.ypos, (tileSize / 2), (tileSize / 2), game.camera.viewportWidth, 5, 1, 1, 0);
        current.ypos = current.ypos - speed;

        if (current.ypos <= findSpace(tcolumn) * tileSize) {
            place(current, tcolumn, findSpace(tcolumn));
            checktion(current);
            current = newTile();
        }

        for (int i = 0; i < corners.size; i++) {
            game.batch.draw(new TextureRegion(preview.get(corners.get(i).type)), xoffset + tileSize * corners.get(i).coords.x, 5 + yoffset + corners.get(i).ypos, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, corners.get(i).angle);
        }

        //hud

        //game.batch.setColor(Color.WHITE);
        //game.batch.draw(new TextureRegion(square), tileSize / 5, (game.camera.viewportHeight - tileSize) + tileSize / 5, (tileSize / 2), (tileSize / 2), (game.camera.viewportWidth - 2 * tileSize / 5)*(flux/10000f), 3 * tileSize / 5, 1, 1, 0);
        font.setColor(Color.WHITE);
        game.batch.setColor(1, 1, 1, 1);

        //controls
        game.batch.setColor(colors[current.type]);
        game.batch.draw(new TextureRegion(game.push), xoffset + tileSize*tcolumn, 0, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, 0);
        game.batch.setColor(Color.WHITE);

        for (int x = 0; x < field.length/2; x++) {
            game.batch.draw(new TextureRegion(game.floor), xoffset + tileSize * 2 * x, tileSize + 50, (tileSize / 2), (tileSize / 2), tileSize * 2, tileSize, 1, 1, 0);
        }
        for (int x = 0; x < field.length; x++) {
            if (findFloor(x) <= current.ypos / tileSize)
                game.batch.draw(new TextureRegion(game.pin), xoffset + tileSize * x, 0, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, 0);
            game.batch.draw(new TextureRegion(game.pixel), (0.75f*tileSize) + xoffset + (tileSize) * x, yoffset, (tileSize / 2), (tileSize / 2), tileSize/2, 5, 1, 1, 0);
        }
        game.batch.draw(new TextureRegion(game.pixel), 0, yoffset, (tileSize / 2), (tileSize / 2), xoffset + tileSize / 4, 5, 1, 1, 0);
        game.batch.draw(new TextureRegion(game.pixel), game.camera.viewportWidth - xoffset, yoffset, (tileSize / 2), (tileSize / 2), xoffset, 5, 1, 1, 0);
        for (int y = 0; y <= ceiling; y++) {
            game.batch.draw(new TextureRegion(game.pixel), xoffset - 5, 4+(0.75f*tileSize) + yoffset + (tileSize) * y, (tileSize / 2), (tileSize / 2), 5, tileSize/2, 1, 1, 0);
            game.batch.draw(new TextureRegion(game.pixel), game.camera.viewportWidth - xoffset, 4+(0.75f*tileSize) + yoffset + (tileSize) * y, (tileSize / 2), (tileSize / 2), 5, tileSize/2, 1, 1, 0);
        }
        //game.batch.draw(new TextureRegion(game.pixel), xoffset-5, yoffset, (tileSize / 2), (tileSize / 2), 5, game.camera.viewportHeight, 1, 1, 0);
        //game.batch.draw(new TextureRegion(game.pixel), game.camera.viewportWidth-xoffset, yoffset, (tileSize / 2), (tileSize / 2), 5, game.camera.viewportHeight, 1, 1, 0);

        game.batch.draw(game.turn, 0, tileSize-15, tileSize/2, tileSize/2, tileSize, tileSize, 1, 1, 0, 0, 0, game.turn.getWidth(), game.turn.getHeight(), true, false);
        game.batch.draw(game.turn, game.camera.viewportWidth-tileSize, tileSize-15, tileSize/2, tileSize/2, tileSize, tileSize, 1, 1, 0, 0, 0, game.turn.getWidth(), game.turn.getHeight(), false, false);
        game.batch.setColor(Color.RED);
        game.batch.draw(new TextureRegion(game.pixel), 0, yoffset + (5 + tileSize) * ceiling, (tileSize / 2), (tileSize / 2), game.camera.viewportWidth, 5, 1, 1, 0);
    }

    private void drawConnected() {
        game.batch.setColor(Color.WHITE);
        dframes = dframes - 1;
        for (int x = 0; x < field.length; x++) {
            for (int y = 0; y < ceiling; y++) {
                if (field[x][y] != null) {
                    if (field[x][y].connected) {
                        //System.out.println("TILE AT " + x + ", " + y + " IS CONNECTED");
                        game.batch.draw(new TextureRegion(types.get(field[x][y].type)), xoffset + tileSize * x, yoffset + tileSize * y, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, field[x][y].angle);
                        if (dframes <= 1) {
                            field[x][y].opacity = 1;
                            dblocks.add(remove(x, y));
                            tiles--;
                            //System.out.println("ADDED TO DBLOCKS");
                        }
                    } else if (field[x][y].caught) {
                        game.batch.draw(new TextureRegion(game.pixel), xoffset + tileSize * x, yoffset + tileSize * y, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, 0);
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
                    game.batch.draw(new TextureRegion(game.pixel), block.xpos, block.ypos, tileSize / 2, tileSize / 2, tileSize / pScale, tileSize / pScale, 1, 1, block.angle);
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
            ctile.checked = true;
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
                }
                graph.clear();
                break;
            }
        }
        if (graph.size > 0) {
            speed = speed + (graph.size)/15f;
            if (containsLoop) {
                System.out.println("LOOKING FOR THE LOOP");
                //Use DFS to find all the loops
                Array<Tile> visited = new Array<Tile>();
                stack.clear();
                stack.add(tile);
                visited.add(tile);
                while (stack.size > 0) {
                    Tile ctile = stack.pop();
                    Vector2 ccoords = ctile.coords;
                    System.out.println("CHECKING TILE AT " + ccoords.x + ", " + ccoords.y);
                    for (int i = 0; i < ctile.children.length; i++) {
                        Tile chtile = ctile.children[i];
                        if (chtile != null && !visited.contains(chtile, true)) {
                            stack.add(chtile);
                            visited.add(chtile);
                            if (chtile.parent != ctile) {
                                //then we've found a loop. add all the tiles in the loop to the loop array
                                Array<Tile> loop = new Array<Tile>();
                                Tile ptile = chtile;
                                while (!visited.contains(ptile, true)) {
                                    loop.add(ptile);
                                    ptile = ptile.parent;
                                }
                                loop.add(chtile.parent);
                                ptile = ctile;
                                int index = loop.size;
                                while (!loop.contains(ptile, true)); {
                                    loop.insert(index, ptile);
                                    ptile = ptile.parent;
                                }
                                //loop array should contain all the tiles in the loop now.
                                for (int t = 0; t < loop.size; t++) {
                                    System.out.println("CHECKING IF " + loop.get(t).coords + " IS A CORNER");
                                    Vector2 cdir = loop.get((((t - 1) % loop.size) + loop.size) % loop.size).coords.sub(loop.get(t).coords);
                                    Vector2 pdir = loop.get(t).coords.sub(loop.get((t + 1) % loop.size).coords);
                                    if (((cdir.x != pdir.x) || cdir.y != pdir.y)) {
                                        System.out.println("LOOKS LIKE IT IS A CORNER");
                                        corners.add(loop.get(t));
                                        for (int col = 0; col <= loop.get(i).coords.x; col++) {
                                            for (int row = 0; row <= loop.get(i).coords.y; row++) {
                                                if (field[col][row] != null) {
                                                    if (!caught.contains(field[col][row], true)) {
                                                        caught.add(field[col][row]);
                                                    } else
                                                        caught.removeValue(field[col][row], true);
                                                }
                                            }
                                        }
                                    }
                                }
                                loop.clear();
                            }
                        }
                    }
                    //System.out.println("FINISHED CHECKING TILE AT " + x0 + ", " + y0);
                }
            }
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
}