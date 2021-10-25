//challenge: blinking cursor? blinking boxes?
//bug: block tile does not destroy if it's surrounded by tiles
//idea: an actual block tile that can't connect to anything and can only get destroyed by touching the bottom or getting captured
//idea: a tile that can connect to anything but itself

package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

/**
 * Created by admin on 7/27/2017.
 */

public class Arcade extends Unit {
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

    private int rType = 5;

    private float ospeed = 0;
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

    //game variables
    private int score = 0;
    private int waitTime = 4000;
    private long currentTime = System.currentTimeMillis();
    private long startTime = 0;
    private int pScale = 5;
    private int type = rng.nextInt(100); //next piece to drop

    //cursor variables
    protected long lastMovTime;
    protected float lastPos = 0;
    protected float cPos;
    protected ShapeRenderer shapeRenderer;

    //variables for the current falling piece
    private Tile current = null;
    private int ccolumn = 3;

    //variables for input
    long lastTouchTime;

    private boolean paused = false;

    //for destroyed blocks
    private Array<Tile> dblocks = new Array<Tile>();

    public Array<Texture> preview = new Array<Texture>();
    public Color[] colors = {Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED};
    public int ceiling = 8;
    public Tile[][] field = new Tile[6][ceiling+1];
    public boolean moved = false;
    public int hcolumn = 3;
    public int hrow = 0;

    private int next = rng.nextInt(6);

    //to generate new tiles
    private Tile newTile() {
        Tile tile;
        tile = new Tile(Type.valueOf(next));

        //if (current != null)
        //tile.incDir(current.dir, tileSize);

        tile.ypos = game.camera.viewportHeight;
        return tile;
    }

    public Arcade(final Main game, final Music music) {
        super(game);

        current = newTile();

        shapeRenderer = new ShapeRenderer();

        xoffset = (int)(game.camera.viewportWidth - ((tileSize)*field.length))/2;
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
                drawParticles();
                drawField();
            } else {
                game.setScreen(new Death(game, field));
            }

            if (flux > 0) flux = flux - 2;
            currentTime = currentTime + ((long)(Gdx.graphics.getDeltaTime()*1000)-(findSpace(ccolumn)/2)-(tiles/15));

            if (currentTime - startTime >= waitTime) {
                waitTime = waitTime - (int)speed;
                startTime = System.currentTimeMillis();
                currentTime = startTime;
                //if enough time has elapsed
                if (tiles < max) {
                    int floor = findFloor(ccolumn);
                    if (floor <= ceiling) {
                        tiles++;
                        place(current, ccolumn, floor);
                        ccolumn = (ccolumn + 1) % field.length;
                        cPos = ccolumn * tileSize;
                        type = rng.nextInt(100);
                        if (type < 10)
                            next = 4;
                        else if (type < 25)
                            next = 3;
                        else if (type < 50)
                            next = 2;
                        else if (type < 70)
                            next = 1;
                        else if (type < 85)
                            next = 0;
                        else
                            next = 5;
                        current = newTile();
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
            //if touch is within range
            Tile tile = getTile(new Vector2(x, y));
            if (tile != null && tile.ypos == y * tileSize) {
                if (tile.canMove) {
                    tile.rotate();
                    lastTouchTime = System.currentTimeMillis();
                    holding = tile;
                    moved = false;
                    game.twist.play();
                    //this section only sets holding, make sure it doesn't need a value
                    hrow = y;
                    hcolumn = x;
                    //System.out.println("X OFFSET IS " + offset.x);
                } else {
                    game.danger.play();
                }
            }
        }

        speed = speed + 0.1f;
    }

    public void processNotouch(boolean changed) {
        int x = (int) ((touchPos.x - xoffset) / tileSize);
        int y = (int) ((touchPos.y - yoffset) / tileSize);
        if (changed) {
            speed = ospeed;
        }
    }

    private void checktion(Tile tile) {
        tile.checked = true;
        //use BFS to see if the graph is connected
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
                    if (chtile != null && chtile.velocity == 0 && chtile.angle % 90 == 0 && ((i == 2 && ctile.coords.y == 0 && chtile.sides[i]) || (chtile.sides[(i + 2) % 4]))) {
                        System.out.println("YES, IT CONNECTS");
                        ctile.children[i] = chtile;
                        if (!graph.contains(chtile, true)) {
                            System.out.println("WE HAVEN'T RECORDED THIS TILE YET, ADDING THE TILE WHOSE COORDS ARE " + new Vector2(chtile.xpos, chtile.ypos) + " TO THE STACK");
                            if (chtile.type < rType)
                                stack.add(chtile);
                            else
                                chtile.children[(i + 2) % 4] = ctile;
                            graph.add(chtile);
                            chtile.parent = ctile;
                        } else if (chtile != ctile.parent) {//this isn't the same tile)
                            System.out.println("FOUND A LOOP");
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
            speed = speed + (graph.size)/(15*(speed+1));
            brightness = 0;
            game.bust.play();
            dframes = 5 + (2 * graph.size);
            System.out.println(graph.size + " TILES ARE CONNECTED");
        }
        for (int i = 0; i < graph.size; i++) {
            if (graph.get(i).type == rType) {
                for (int c = 0; c < graph.get(i).children.length; c++) {
                    System.out.println("CHECKING THIS SIDE...");
                    if (graph.get(i).children[c] != null) {
                        System.out.println("DELETE");
                        graph.get(i).sides[c] = false;
                    }
                }
            }
            score = score + (i * 10);
            flux = flux + (score * 10);
            graph.get(i).connected = true;
        }
    }

    private void updateTile(Tile tile) {
        //if (corners.contains(new Vector2(x, y), false))
        //game.batch.draw(new TextureRegion(square), tileSize * x, tileSize * y, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 0.2f, 0.2f, 0);
        if (tile != null) {
            int x = (int)tile.coords.x;
            int y = (int)tile.coords.y;
            if (tile.ypos != tile.coords.y * tileSize) {
                if (tile.ypos > tile.coords.y * tileSize) {
                    if (tile.ypos - tile.velocity > tile.coords.y * tileSize) {
                        tile.ypos = tile.ypos - tile.velocity;
                        tile.velocity = tile.velocity + 1.1f + (speed/2);
                    } else {
                        tile.ypos = y * tileSize;
                        tile.velocity = 0;
                        if (tile.coords.y == findFloor((int)tile.coords.x) - 1)
                            game.drop.play();
                        checktion(tile);
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
                    tile.angle = new Interpolation.SwingOut(1.5f).apply((tile.dir - 1) * 90, tile.dir * 90, time);
                } else {
                    if (!tile.checked)
                        checktion(tile);
                    tile.angle = tile.dir * 90;
                }
            }
        }
    }

    private void drawField() {
        //draw the field
        if (brightness < 1)
            brightness = brightness + 0.05f;
        int s = -1;
        int e = field.length+1;
        for (int x = s; x < e; x++) {
            for (int y = ceiling; y >= 0; y--) {
                Tile tile = getTile(new Vector2(x, y));
                updateTile(tile);
                if (tile != null) {
                    game.batch.setColor(colors[tile.type]);
                    if (tile.connected) {
                        game.batch.setColor(Color.WHITE);
                    }
                    game.batch.draw(new TextureRegion(game.pixel), xoffset + tileSize * x, yoffset + tile.ypos, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 0.8f, 0.8f, 0);
                    game.batch.setColor(Color.WHITE);
                    if (tile.type == rType) {
                        for (int c = 0; c < tile.sides.length; c++) {
                            if (tile.sides[c]) {
                                if (!tile.connected || tile.children[c] != null)
                                    game.batch.draw(new TextureRegion(game.types, game.types.getHeight() * rType, 0, game.types.getHeight(), game.types.getHeight()), xoffset + tileSize * x, yoffset + tile.ypos, tileSize / 2, tileSize / 2, tileSize, tileSize, 1, 1, 90 * c);
                            }
                        }
                    } else {
                        game.batch.draw(new TextureRegion(game.types, game.types.getHeight() * tile.type, 0, game.types.getHeight(), game.types.getHeight()), xoffset + tileSize * x, yoffset + tile.ypos, tileSize / 2, tileSize / 2, tileSize, tileSize, 1, 1, tile.angle);
                    }
                    if (tile.connected && dframes == 1) {
                        destroyTile(tile);
                    }
                }
            }
        }

        if (dframes > 0) {
            dframes = dframes - 1;
        }

        //hud

        //game.batch.setColor(Color.WHITE);
        //game.batch.draw(new TextureRegion(square), tileSize / 5, (game.camera.viewportHeight - tileSize) + tileSize / 5, (tileSize / 2), (tileSize / 2), (game.camera.viewportWidth - 2 * tileSize / 5)*(flux/10000f), 3 * tileSize / 5, 1, 1, 0);
        font.setColor(Color.WHITE);
        game.batch.setColor(1, 1, 1, 1);

        for (int x = 0; x < field.length; x++) {
            game.batch.setColor(Color.WHITE);
            game.batch.draw(new TextureRegion(game.pin), xoffset + tileSize * x, 0, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, 0);
            game.batch.setColor(Color.WHITE);
            game.batch.draw(new TextureRegion(game.pixel), (0.75f*tileSize) + xoffset + (tileSize) * x, yoffset, (tileSize / 2), (tileSize / 2), tileSize/2, 5, 1, 1, 0);
        }
        game.batch.setColor(Color.WHITE);

        for (int x = 0; x < field.length/2; x++) {
            game.batch.draw(new TextureRegion(game.floor), xoffset + tileSize * 2 * x, tileSize + 50, (tileSize / 2), (tileSize / 2), tileSize * 2, tileSize, 1, 1, 0);
        }
        game.batch.draw(new TextureRegion(game.pixel), 0, yoffset, (tileSize / 2), (tileSize / 2), xoffset + tileSize / 4, 5, 1, 1, 0);
        game.batch.draw(new TextureRegion(game.pixel), game.camera.viewportWidth - xoffset, yoffset, (tileSize / 2), (tileSize / 2), xoffset, 5, 1, 1, 0);
        for (int y = 0; y <= ceiling; y++) {
            game.batch.draw(new TextureRegion(game.pixel), xoffset - 5, (0.75f*tileSize) + yoffset + (tileSize) * y, (tileSize / 2), (tileSize / 2), 5, tileSize/2, 1, 1, 0);
            game.batch.draw(new TextureRegion(game.pixel), game.camera.viewportWidth - xoffset, (0.75f*tileSize) + yoffset + (tileSize) * y, (tileSize / 2), (tileSize / 2), 5, tileSize/2, 1, 1, 0);
        }
        game.batch.draw(new TextureRegion(game.pixel), xoffset-5, yoffset, (tileSize / 2), (tileSize / 2), 5, tileSize / 4, 1, 1, 0);
        game.batch.draw(new TextureRegion(game.pixel), game.camera.viewportWidth-xoffset, yoffset, (tileSize / 2), (tileSize / 2), 5, tileSize / 4, 1, 1, 0);

        game.batch.setColor(Color.RED);
        game.batch.draw(new TextureRegion(game.pixel), 0, yoffset + tileSize * (ceiling - 1), (tileSize / 2), (tileSize / 2), game.camera.viewportWidth, 5, 1, 1, 0);

        float time = (float) (System.currentTimeMillis() - lastMovTime) / (300 / (1+speed));
        cPos = Interpolation.sineOut.apply(lastPos, ccolumn * tileSize, time);
        if (time > 1) {
            cPos = tileSize * ccolumn;
        }
    }

    private void destroyTile(Tile tile) {
        tile.opacity = 1;
        boolean destroy = true;
        if (tile.type == rType) {
            for (int c = 0; c < tile.sides.length; c++)
                if (tile.sides[c])
                    destroy = false;
        }
        if (destroy) {
            remove((int) tile.coords.x, (int) tile.coords.y);
            for (int i = 0; i < 25; i++) {
                Tile particle = new Tile(Type.valueOf(tile.type));
                particle.xpos = tileSize * tile.coords.x;
                particle.ypos = tileSize * tile.coords.y;
                particle.angle = rng.nextInt((45 - -45) + 1) + -45;
                particle.opacity = 1;
                dblocks.add(particle);
            }
            tiles--;
        } else {
            tile.connected = false;
        }
        //System.out.println("ADDED TO DBLOCKS");
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
                        block.angle = block.angle - 1;
                    } else {
                        block.angle = block.angle + 1;
                    }
                    game.batch.draw(new TextureRegion(game.pixel), block.xpos, block.ypos, tileSize / 2, tileSize / 2, tileSize / pScale, tileSize / pScale, 1, 1, block.angle);
                }
            }
        }
    }

    public void drawShape() {
        //must prepare for the next frame
        shapeRenderer.setProjectionMatrix(game.camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.arc(xoffset + ((1 + ccolumn) * tileSize) - tileSize / 2, tileSize / 2, tileSize / 2, 90, 360 - 360 * ((currentTime - startTime) / (float) (waitTime)));
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.circle(xoffset + ((1 + ccolumn) * tileSize) - tileSize / 2, tileSize / 2, tileSize / 2 - 5);
        //game.batch.draw(new TextureRegion(game.push), xoffset + cPos, 0, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, 0);
        shapeRenderer.end();
    }

    private void place(Tile tile, int x, int y) {
        tile.coords.set(x, y);
        adjustColumn(x, y);
        field[x][y] = tile;
    }

    private Tile remove(int x, int y) {
        Tile tile = field[x][y];
        field[x][y] = null;
        adjustColumn(x, y);
        return tile;
    }

    private void adjustColumn(int x, int y) {
        //takes all the pieces in the column starting from y and shifts them to the bottom starting at y
        if (field[x][y] == null) {
            //this executes when a tile is taken away, will move all the tiles above it downwards 1
            for (int i = y + 1; i <= ceiling; i++) {
                if (field[x][i] != null)
                    field[x][i].coords.y = i - 1;
                field[x][i - 1] = field[x][i];
                field[x][i] = null;
            }
        } else {
            //execute this before placing a tile, will move all the tiles above it upwards 1
            for (int i = ceiling; i > y; i--) {
                if (field[x][i - 1] != null)
                    field[x][i - 1].coords.y = i;
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

    private Tile getTile(Vector2 coords) {
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
}