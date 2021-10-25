//challenge: blinking cursor? blinking boxes?
//bug: block tile does not destroy if it's surrounded by tiles
//idea: an actual block tile that can't connect to anything and can only get destroyed by touching the bottom or getting captured
//idea: a tile that can connect to anything but itself

package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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

import static com.badlogic.gdx.graphics.GL20.GL_KEEP;
import static com.badlogic.gdx.graphics.GL20.GL_NOTEQUAL;
import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_REPLACE;
import static com.badlogic.gdx.graphics.GL20.GL_STENCIL_BUFFER_BIT;
import static com.badlogic.gdx.graphics.GL20.GL_ZERO;

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
    private int yoffset = tileSize*2;
    private int xoffset = 0;
    protected Music gameover;
    protected Sound thud;
    protected Sound drop;
    protected Sound danger;
    protected Sound twist;
    protected Sound bust;
    //resources
    //private Array<Vector2> corners = new Array<Vector2>();

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
    public Color[] colors = {Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.PURPLE};
    public int ceiling = 8;
    public Tile[][] field = new Tile[6][ceiling+1];
    public boolean moved = false;
    public int hcolumn = 3;
    public int hrow = 0;

    private int next = rng.nextInt(6);

    private Cubby[] cubbies = new Cubby[field.length];
    private Cubby holding = null;

    float hsize = tileSize * 0.2f;
    float size = tileSize*0.8f;

    //to generate new tiles
    private Tile newTile(Tile t) {
        Tile tile;
        if (t == null) {
            tile = new Tile(Type.valueOf(next));
        } else {
            tile = new Tile(Type.valueOf(t.type));
            while (!tile.sides.equals(t.sides)) {
                tile.rotate(1);
            }
            tile.canMove = false;
        }
        tile.ypos = game.camera.viewportHeight;
        return tile;
    }

    public Arcade(final Main game, final Music music) {
        super(game);

        current = newTile(null);

        shapeRenderer = new ShapeRenderer();

        xoffset = (int)(game.camera.viewportWidth - ((tileSize)*field.length))/2;

        for (int i = 0; i < cubbies.length; i++) {
            cubbies[i] = new Cubby(new Vector2(xoffset + ((tileSize - size) / 2) + tileSize * i, xoffset));
        }

        generator = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));
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
                updateCubbies();
                drawParticles();
                drawReflections();
                drawWalls();
                drawBlack();
                drawField();
                drawCubbies();
            } else {
                game.setScreen(new Death(game, field));
            }

            if (flux > 0) flux = flux - 2;

            if (currentTime - startTime >= waitTime) {
                //if enough time has elapsed
                if (tiles < max) {
                    int floor = findFloor(ccolumn);
                    if (floor <= ceiling) {
                        tiles++;
                        place(current, ccolumn, floor);
                        startTime = System.currentTimeMillis();
                        currentTime = startTime;
                    }
                }
            }

            if (current.landed) {
                ccolumn = (ccolumn + 1) % field.length;
                cubbies[ccolumn].held = false;
                if (holding == cubbies[ccolumn]) {
                    holding.lastMovTime = System.currentTimeMillis();
                    holding = null;
                }
                    type = rng.nextInt(100);
                    if (type < 10)
                        next = 4;
                    else if (type < 25)
                        next = 3;
                    else if (type < 50)
                        next = 2;
                    else if (type < 70)
                        next = 1;
                    else if (type < 90)
                        next = 0;
                    else
                        next = 5;
                    current = newTile(cubbies[ccolumn].tile);
                    cubbies[ccolumn].tile = null;
            } else if (current.coords.x < 0) {
                currentTime = currentTime + (long)speed + ((long)(Gdx.graphics.getDeltaTime()*1000)-(findSpace(ccolumn)/2)-(tiles/15));
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

    private void rotate(Tile tile) {
        if (tile.checked)
            tile.firstAngle = tile.angle;
        tile.lastAngle = tile.angle;
        tile.dir = tile.dir + 1;
        tile.checked = false;
        tile.lastRotTime = System.currentTimeMillis();
        game.twist.play();
    }

    public void processTouching(boolean changed) {
        int x = (int) ((touchPos.x - xoffset) / tileSize);
        int y = (int) ((touchPos.y - yoffset) / tileSize);
        if (y >= ceiling)
            y = ceiling;
        if (changed) {
            ospeed = speed;
            //if touch is within range
            if (y >= 0) {
                Tile tile = getTile(new Vector2(x, y));
                if (tile != null && tile.ypos == y * tileSize) {
                    if (tile.canMove) {
                        rotate(tile);
                        moved = false;
                        //this section only sets holding, make sure it doesn't need a value
                        hrow = y;
                        hcolumn = x;
                        //System.out.println("X OFFSET IS " + offset.x);
                    } else {
                        game.danger.play();
                    }
                }
            } else if (x >= 0 && x < field.length) {
                holding = cubbies[x];
                if (holding.tile == null) {
                    holding.lastChaTime = System.currentTimeMillis();
                    holding.xoffset = touchPos.x - holding.xpos;
                    holding.yoffset = touchPos.y - holding.ypos;
                    holding.held = true;
                } else {
                    rotate(holding.tile);
                    holding = null;
                }
            }
        }
        if (holding != null) {
            holding.tPos.set(touchPos.x - holding.xoffset - (holding.size - size)/2, touchPos.y + size/4);
            if (holding.tPos.y > tileSize) {
                System.out.println("COORDS OF CUBBY ARE " + (int)(holding.xpos - xoffset + holding.size/2)/tileSize + " " + (int)(holding.ypos - yoffset + holding.size/2)/tileSize);
                holding.tile = getTile(new Vector2((int)(holding.xpos - xoffset + holding.size/2)/tileSize, (int)(holding.ypos - yoffset + holding.size/2)/tileSize));
                if (holding.tile != null && !holding.tile.canMove)
                    holding.tile = null;
            } else {
                holding.tile = null;
            }
        }
        speed = speed + 0.1f;
    }

    public void processNotouch(boolean changed) {
        int x = (int) ((touchPos.x - xoffset) / tileSize);
        int y = (int) ((touchPos.y - yoffset) / tileSize);
        if (changed) {
            speed = ospeed;
            if (holding != null) {
                System.out.println("RELEASED.");
                holding.lastMovTime = System.currentTimeMillis();
                holding.held = false;
                if (holding.tile != null) {
                    remove((int)holding.tile.coords.x, (int)holding.tile.coords.y);
                }
                holding = null;
            }
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
                            chcoords.set(ccoords.x + 1, ccoords.y);
                            break;
                        case 2:
                            chcoords.set(ccoords.x, ccoords.y - 1);
                            break;
                        case 3:
                            chcoords.set(ccoords.x - 1, ccoords.y);
                            break;
                        default:
                            break;
                    }
                    System.out.println("CCORDS IS " + ccoords + " AND i IS " + i);
                    System.out.println("CHECKING IF MATCHING TILE IS AT " + chcoords);
                    Tile chtile = getTile(chcoords);
                    if (chtile != null && chtile.velocity == 0 && ((i == 2 && ctile.coords.y == 0 && chtile.sides[i]) || (chtile.sides[(i + 2) % 4]))) {
                        System.out.println("YES, IT CONNECTS");
                        ctile.children[i] = chtile;
                        if (ctile.type < rType && !graph.contains(chtile, true)) {
                            System.out.println("WE HAVEN'T RECORDED THIS TILE YET, ADDING THE TILE WHOSE COORDS ARE " + new Vector2(chtile.xpos, chtile.ypos) + " TO THE STACK");
                            stack.add(chtile);
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
                        if (ctile.type < rType)
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
        if (graph.size > 1) {
            speed = speed + (graph.size)/(15*(speed+1));
            brightness = 0;
            game.bust.play();
            dframes = 4 + (graph.size);
            System.out.println(graph.size + " TILES ARE CONNECTED");
            for (int i = 0; i < graph.size; i++) {
                score = score + (i * 10);
                flux = flux + (score * 10);
                graph.get(i).connected = true;
            }
        }
        for (int i = 0; i < graph.size; i++) {
            if (graph.get(i).type == rType) {
                if (graph.size == 1)
                    graph.get(i).parent = null;
                for (int c = 0; c < graph.get(i).children.length; c++) {
                    System.out.println("CHECKING THIS SIDE...");
                    if (graph.size == 1)
                        graph.get(i).children[c] = null;
                    if (graph.get(i).children[c] != null) {
                        System.out.println("DELETE");
                        graph.get(i).sides[c] = false;
                    }
                }
            }
        }
    }

    private void updateCubbies() {
        //must prepare for the next frame
        int x = xoffset + ((1 + ccolumn) * tileSize) - tileSize / 2;
        int y = 0;
        for (int i = 0; i < field.length; i++) {
            game.batch.setColor(Color.WHITE);
            Cubby cubby = cubbies[i];
            if (!cubby.held) {
                if (cubby.tile != null) {
                    Tile tile = cubby.tile;
                    float time = (System.currentTimeMillis() - tile.lastRotTime) / 250f;
                    if (time <= 1) {
                        tile.angle = new Interpolation.SwingOut(1.5f).apply(tile.lastAngle, tile.firstAngle + tile.dir * 90, time);
                        game.batch.setColor(Color.WHITE.cpy().lerp(colors[tile.type], time));
                    } else {
                        if (!tile.checked) {
                            tile.rotate(tile.dir);
                        }
                    }

                    float psize = cubby.size;

                    time = (System.currentTimeMillis() - cubby.lastMovTime) / 250f;
                    if (i == ccolumn) {
                        psize = tileSize * 0.2f;
                        if (time <= 1) {
                            psize = new Interpolation.SwingOut(1.5f).apply(tileSize, tileSize * 0.2f, time);
                        }
                    }
                }
                double distance = Math.sqrt(Math.pow(cubby.tPos.x - cubby.oPos.x,2)+Math.pow(cubby.tPos.y - cubby.oPos.y,2));
                float time = (System.currentTimeMillis() - cubby.lastMovTime) / 250f;
                cubby.size = size;
                cubby.isize = size / 4;
                cubby.oPos.set(xoffset + ((tileSize - cubby.size)/2) + tileSize * i, xoffset + ((tileSize - cubby.size)/2) - 20);
                cubby.xpos = cubby.oPos.x;
                cubby.ypos = cubby.oPos.y;
                if (time <= 1) {
                    cubby.size = new Interpolation.SwingOut(1.5f).apply(tileSize, size, time);
                    cubby.xpos = new Interpolation.SwingOut(1.5f).apply(cubby.tPos.x, cubby.oPos.x, time);
                    cubby.ypos = new Interpolation.SwingOut(1.5f).apply(cubby.tPos.y, cubby.oPos.y, time);
                }
                if (i == ccolumn) {
                    if (time > 1) {
                        cubby.size = size - size * ((currentTime - startTime) / (float) (waitTime));
                        //cubby.xpos = xoffset + ((tileSize - cubby.size)/2) + tileSize * i;
                        //cubby.ypos = xoffset + ((tileSize - cubby.size)/2) - 20;
                    }
                }
            } else {
                float time = (System.currentTimeMillis() - cubby.lastChaTime) / 250f;
                cubby.size = tileSize;
                cubby.xpos = cubby.tPos.x;
                cubby.ypos = cubby.tPos.y;
                if (time <= 1) {
                    cubby.size = new Interpolation.SwingOut(1.5f).apply(size, tileSize, time);
                }
            }
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
                        tile.velocity = tile.velocity + 1.1f;
                    } else {
                        tile.landed = true;
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
                float time = (System.currentTimeMillis() - tile.lastRotTime) / 250f;
                if (tile.canMove) {
                    if (time <= 1) {
                        tile.angle = new Interpolation.SwingOut(1.5f).apply(tile.lastAngle, tile.firstAngle + tile.dir * 90, time);
                        game.batch.setColor(Color.WHITE.cpy().lerp(colors[tile.type], time));
                    } else {
                        if (!tile.checked) {
                            tile.rotate(tile.dir);
                            checktion(tile);
                        }
                    }
                } else {
                    if (time <= 1) {
                        tile.angle = new Interpolation.SwingOut(1.5f).apply(tile.firstAngle * 90 - 30, tile.firstAngle * 90, time);
                        game.batch.setColor(Color.WHITE.cpy().lerp(colors[tile.type], time));
                    }
                }
            }
            if (holding != null && holding.tile != null && tile == holding.tile)
                game.batch.setColor(Color.WHITE);
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
                if (tile != null && tile.canMove && (dframes <= 0 || tile.connected)) {
                    game.batch.setColor(colors[tile.type]);
                    updateTile(tile);
                    if (tile.connected) {
                        game.batch.setColor(Color.WHITE);
                    }
                    if (tile.type == rType) {
                        game.batch.draw(new TextureRegion(game.pixel), xoffset + tileSize * x, yoffset + tile.ypos, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 0.8f, 0.8f, 0);
                        game.batch.setColor(Color.WHITE);
                        for (int c = 0; c < tile.sides.length; c++) {
                            if (tile.sides[c]) {
                                if (!tile.connected || tile.children[c] != null) {
                                    //weirdness when connecting through the floor
                                    game.batch.draw(new TextureRegion(game.types, game.types.getHeight() * rType, 0, game.types.getHeight(), game.types.getHeight()), xoffset + tileSize * x, yoffset + tile.ypos, tileSize / 2, tileSize / 2, tileSize, tileSize, 1, 1, tile.angle + 90 * c);
                                }
                            }
                        }
                    } else {
                        game.batch.draw(new TextureRegion(game.pixel), xoffset + tileSize * x, yoffset + tile.ypos, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 0.8f, 0.8f, 0);
                        game.batch.setColor(Color.WHITE);
                        game.batch.draw(new TextureRegion(game.types, game.types.getHeight() * tile.type, 0, game.types.getHeight(), game.types.getHeight()), xoffset + tileSize * x, yoffset + tile.ypos, tileSize / 2, tileSize / 2, tileSize, tileSize, 1, 1, tile.angle);
                    }
                    if (tile.connected && dframes == 1) {
                        destroyTile(tile);
                    }
                }
            }
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
            for (int i = 0; i < 25; i++) {
                Tile particle = new Tile(Type.valueOf(tile.type));
                particle.xpos = tileSize * tile.coords.x;
                particle.ypos = tileSize * tile.coords.y;
                particle.angle = rng.nextInt((45 - -45) + 1) + -45;
                particle.opacity = 1;
                dblocks.add(particle);
            }
            remove((int) tile.coords.x, (int) tile.coords.y);
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

    public void drawCubbies() {
        //must prepare for the next frame
        int x = xoffset + ((1 + ccolumn) * tileSize) - tileSize / 2;
        int y = 0;
        for (int i = 0; i < field.length; i++) {
            game.batch.setColor(Color.WHITE);
            Cubby cubby = cubbies[i];

            if (cubby != holding && cubby.tile != null) {
                updateTile(cubby.tile);
                game.batch.setColor(colors[cubby.tile.type]);
                game.batch.draw(new TextureRegion(game.pixel), cubby.xpos, cubby.ypos, (tileSize / 2), (tileSize / 2), cubby.size, cubby.size, 1, 1, 0);
                game.batch.setColor(Color.WHITE);
                game.batch.draw(new TextureRegion(game.types, game.types.getHeight() * cubby.tile.type, 0, game.types.getHeight(), game.types.getHeight()), cubby.xpos, cubby.ypos, tileSize / 2, tileSize / 2, cubby.size, cubby.size, 1, 1, cubby.tile.angle);
            }

            game.batch.draw(new TextureRegion(game.pixel), cubby.xpos + ((size - cubby.size)/2), cubby.ypos + ((size - cubby.size)/2), (tileSize / 2), (tileSize / 2), (cubby.size - cubby.isize)/2, cubby.size, 1, 1, 0);
            game.batch.draw(new TextureRegion(game.pixel), cubby.xpos + ((size - cubby.size)/2), cubby.ypos + cubby.size - (cubby.size - cubby.isize)/2 + ((size - cubby.size)/2), (tileSize / 2), (tileSize / 2), cubby.size, (cubby.size - cubby.isize)/2, 1, 1, 0);
            game.batch.draw(new TextureRegion(game.pixel), cubby.xpos + cubby.size - (cubby.size - cubby.isize)/2 + ((size - cubby.size)/2), cubby.ypos + ((size - cubby.size)/2), (tileSize / 2), (tileSize / 2), (cubby.size - cubby.isize)/2, cubby.size, 1, 1, 0);
            game.batch.draw(new TextureRegion(game.pixel), cubby.xpos + ((size - cubby.size)/2), cubby.ypos + ((size - cubby.size)/2), (tileSize / 2), (tileSize / 2), cubby.size, (cubby.size - cubby.isize)/2, 1, 1, 0);
        }
    }

    private void drawBlack() {
        if (holding != null) {
            game.batch.setColor(Color.BLACK);
            game.batch.draw(new TextureRegion(game.pixel), holding.xpos + ((size - holding.size) / 2), holding.ypos + ((size - holding.size) / 2), (tileSize / 2), (tileSize / 2), holding.size, holding.size, 1, 1, 0);
        }
    }

    private void drawReflections() {
        //draw reflection under field
        for (int x = 0; x < field.length; x++) {
            Tile tile = getTile(new Vector2(x, -1));
            if (tile != null && (dframes <= 0 || tile.connected)) {
                game.batch.setColor(colors[tile.type]);
                if (!tile.canMove)
                    game.batch.setColor(Color.RED);
                updateTile(tile);
                if (tile.connected) {
                    game.batch.setColor(Color.WHITE);
                }
                if (tile.type == rType) {
                    game.batch.draw(new TextureRegion(game.pixel), xoffset + tileSize * x, yoffset - tile.ypos - tileSize, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 0.8f, 0.8f, 0);
                    game.batch.setColor(Color.WHITE);
                    for (int c = 0; c < tile.sides.length; c++) {
                        if (tile.sides[(((c - tile.dir) % 4) + 4) % 4]) {
                            if (!tile.connected || tile.children[c] != null) {
                                //weirdness when connecting through the floor
                                game.batch.draw(new TextureRegion(game.types, game.types.getHeight() * rType, 0, game.types.getHeight(), game.types.getHeight()), xoffset + tileSize * x, yoffset - tile.ypos - tileSize, tileSize / 2, tileSize / 2, tileSize, tileSize, 1, 1, tile.angle + 90 * (c + 2));
                            }
                        }
                    }
                } else {
                    game.batch.draw(new TextureRegion(game.pixel), xoffset + tileSize * x, yoffset - tile.ypos - tileSize, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 0.8f, 0.8f, 0);
                    game.batch.setColor(Color.WHITE);
                    game.batch.draw(new TextureRegion(game.types, game.types.getHeight() * tile.type, 0, game.types.getHeight(), game.types.getHeight()), xoffset + tileSize * x, yoffset - tile.ypos - tileSize, tileSize / 2, tileSize / 2, tileSize, tileSize, 1, 1, 180 + tile.angle);
                }
                if (tile.connected && dframes == 1) {
                    destroyTile(tile);
                }
            }
        }

        //draw side reflections
        int s = -1;
        int e = field.length+1;
        for (int x = s; x < e; x++) {
            for (int y = ceiling; y >= 0; y--) {
                Tile tile = getTile(new Vector2(x, y));
                if (tile != null && (dframes <= 0 || tile.connected)) {
                    game.batch.setColor(colors[tile.type]);
                    if (!tile.canMove)
                        game.batch.setColor(Color.RED);
                    updateTile(tile);
                    if (tile.connected) {
                        game.batch.setColor(Color.WHITE);
                    }
                    if (tile.type == rType) {
                        game.batch.draw(new TextureRegion(game.pixel), xoffset + tileSize * x, yoffset + tile.ypos, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 0.8f, 0.8f, 0);
                        game.batch.setColor(Color.WHITE);
                        for (int c = 0; c < tile.sides.length; c++) {
                            if (tile.sides[(((c - tile.dir) % 4) + 4) % 4]) {
                                if (!tile.connected || tile.children[c] != null) {
                                    //weirdness when connecting through the floor
                                    game.batch.draw(new TextureRegion(game.types, game.types.getHeight() * rType, 0, game.types.getHeight(), game.types.getHeight()), xoffset + tileSize * x, yoffset + tile.ypos, tileSize / 2, tileSize / 2, tileSize, tileSize, 1, 1, tile.angle + 90 * c);
                                }
                            }
                        }
                    } else {
                        game.batch.draw(new TextureRegion(game.pixel), xoffset + tileSize * x, yoffset + tile.ypos, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 0.8f, 0.8f, 0);
                        game.batch.setColor(Color.WHITE);
                        game.batch.draw(new TextureRegion(game.types, game.types.getHeight() * tile.type, 0, game.types.getHeight(), game.types.getHeight()), xoffset + tileSize * x, yoffset + tile.ypos, tileSize / 2, tileSize / 2, tileSize, tileSize, 1, 1, tile.angle);
                    }
                }
            }
        }
    }

    private void drawWalls() {
        if (dframes > 0) {
            dframes = dframes - 1;
        } else {
            //hud

            //game.batch.setColor(Color.WHITE);
            //game.batch.draw(new TextureRegion(square), tileSize / 5, (game.camera.viewportHeight - tileSize) + tileSize / 5, (tileSize / 2), (tileSize / 2), (game.camera.viewportWidth - 2 * tileSize / 5)*(flux/10000f), 3 * tileSize / 5, 1, 1, 0);

            //draw sides of field

            game.batch.setColor(Color.BLACK);
            game.batch.draw(new TextureRegion(game.pixel), 0, 0, (tileSize / 2), (tileSize / 2), game.camera.viewportWidth, yoffset - tileSize / 4, 1, 1, 0);
            game.batch.setColor(Color.WHITE);
            game.batch.draw(new TextureRegion(game.pixel), 0, yoffset - tileSize / 4 - 10, (tileSize / 2), (tileSize / 2), game.camera.viewportWidth, 5, 1, 1, 0);
            for (int x = 0; x < field.length; x++) {
                game.batch.setColor(Color.WHITE);
                game.batch.draw(new TextureRegion(game.pixel), (0.75f * tileSize) + xoffset + (tileSize) * x, yoffset, (tileSize / 2), (tileSize / 2), tileSize / 2, 5, 1, 1, 0);
            }
            game.batch.setColor(Color.WHITE);

            game.batch.draw(new TextureRegion(game.pixel), 0, yoffset, (tileSize / 2), (tileSize / 2), xoffset + tileSize / 4, 5, 1, 1, 0);
            game.batch.draw(new TextureRegion(game.pixel), game.camera.viewportWidth - xoffset, yoffset, (tileSize / 2), (tileSize / 2), xoffset, 5, 1, 1, 0);
            for (int y = 0; y <= ceiling; y++) {
                game.batch.draw(new TextureRegion(game.pixel), xoffset - 5, (0.75f * tileSize) + yoffset + (tileSize) * y, (tileSize / 2), (tileSize / 2), 5, tileSize / 2, 1, 1, 0);
                game.batch.draw(new TextureRegion(game.pixel), game.camera.viewportWidth - xoffset, (0.75f * tileSize) + yoffset + (tileSize) * y, (tileSize / 2), (tileSize / 2), 5, tileSize / 2, 1, 1, 0);
            }
            game.batch.draw(new TextureRegion(game.pixel), xoffset - 5, yoffset, (tileSize / 2), (tileSize / 2), 5, tileSize / 4, 1, 1, 0);
            game.batch.draw(new TextureRegion(game.pixel), game.camera.viewportWidth - xoffset, yoffset, (tileSize / 2), (tileSize / 2), 5, tileSize / 4, 1, 1, 0);

            game.batch.setColor(Color.RED);
            game.batch.draw(new TextureRegion(game.pixel), 0, yoffset + tileSize * (ceiling - 1), (tileSize / 2), (tileSize / 2), game.camera.viewportWidth, 5, 1, 1, 0);
        }
        game.batch.setColor(Color.WHITE);
        game.batch.draw(new TextureRegion(game.pixel), 0, game.camera.viewportHeight - (tileSize + 5), (tileSize / 2), (tileSize / 2), game.camera.viewportWidth, tileSize / 7, 1, 1, 0);
        game.batch.setColor(Color.BLACK);
        game.batch.draw(new TextureRegion(game.pixel), 0, (game.camera.viewportHeight - tileSize), (tileSize / 2), (tileSize / 2), game.camera.viewportWidth, 5 * tileSize / 5, 1, 1, 0);
        game.batch.setColor(Color.WHITE);

        game.header.draw(game.batch, Integer.toString(score), xoffset, tileSize * 11);
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
            if (coords.x >= 0 && coords.x < field.length)
                return field[field.length - 1 - (int)coords.x][0];
            else
                return null;
        }
        if (coords.x < 0)
            return field[field.length - 1][(int)coords.y];
        if (coords.x >= field.length)
            return field[0][(int)coords.y];
        return field[(int)coords.x][(int)coords.y];
    }
}