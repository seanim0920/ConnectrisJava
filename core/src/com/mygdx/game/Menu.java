package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Menu extends Unit implements Screen {
    Label title;
	BitmapFont header;
    BitmapFont text;
    FreeTypeFontGenerator generator;
    FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
    long startTime = System.currentTimeMillis();
    Tile[] tiles = new Tile[4];
    Sprite[] letters = new Sprite[4];
    int dframes = 0;
    Array<Tile> dblocks = new Array<Tile>();

    public Menu(final Main game) {
        super(game);
        generator = new FreeTypeFontGenerator(Gdx.files.internal("font.otf"));
        parameter.color = Color.WHITE;
        parameter.size = 120;
        header = generator.generateFont(parameter);
        parameter.size = 75;
        text = generator.generateFont(parameter);

        title = new Label("CONNECTRIS", header);
        title.ypos = tileSize * 13;
        title.xpos = title.center(game.camera);
        title.lastTime = System.currentTimeMillis();

        BitmapFont.Glyph glyph = text.getData().getGlyph('P');
        Sprite s = new Sprite(text.getRegion().getTexture(),glyph.srcX,glyph.srcY,glyph.width, glyph.height);
        s.flip(true, true);
        s.setOrigin(glyph.width/2, glyph.height/2);
        letters[0] = s;

        glyph = text.getData().getGlyph('L');
        s = new Sprite(text.getRegion().getTexture(),glyph.srcX,glyph.srcY,glyph.width, glyph.height);
        s.flip(false, true);
        s.setOrigin(glyph.width/2, glyph.height/2);
        letters[1] = s;

        glyph = text.getData().getGlyph('A');
        s = new Sprite(text.getRegion().getTexture(),glyph.srcX,glyph.srcY,glyph.width, glyph.height);
        s.flip(false, true);
        s.setOrigin(glyph.width/2, glyph.height/2);
        letters[2] = s;

        glyph = text.getData().getGlyph('Y');
        s = new Sprite(text.getRegion().getTexture(),glyph.srcX,glyph.srcY,glyph.width, glyph.height);
        s.flip(false, true);
        s.setOrigin(glyph.width/2, glyph.height/2);
        letters[3] = s;
	}

    @Override
    public void show() {

    }

    public void drawText() {
        header.draw(game.batch, title.layout, title.xpos, title.ypos);
        float time = (float) (System.currentTimeMillis() - title.lastTime) / 300;
        if (time <= 1) {
            title.ypos = new Interpolation.SwingOut(1.5f).apply(tileSize * 13, tileSize * 12, time);
        } else {
            title.ypos = tileSize * 12;
        }
    }

    public void process() {
        if (dframes <= 0) {
            drawHolding();
            drawField();
            game.batch.setColor(Color.WHITE);
            for (int i = 0; i < 4; i++){
                if (tiles[i] != null)
                    game.batch.draw(new TextureRegion(letters[i]), tiles[i].xpos + tileSize/4, tiles[i].height + tileSize/4, (tileSize / 4), (tileSize / 4), tileSize/2, tileSize/2, 1, 1, tiles[i].angle);
            }
        } else {
            drawConnected();
        }

        if (System.currentTimeMillis() - startTime >= 300) {
            startTime = System.currentTimeMillis();
            if (tiles[0] == null) {
                Tile tile = new Tile(Type.r);
                tile.height = game.camera.viewportHeight + tileSize;
                tile.xpos = 0 * tileSize;
                field[0][0] = tile;
                tiles[0] = tile;
            } else if (tiles[3] == null) {
                Tile tile = new Tile(Type.r);
                tile.height = game.camera.viewportHeight + tileSize;
                tile.xpos = 1 * tileSize;
                field[1][0] = tile;
                tiles[3] = tile;
            } else if (tiles[2] == null) {
                Tile tile = new Tile(Type.r);
                tile.height = game.camera.viewportHeight + tileSize;
                tile.xpos = 3 * tileSize;
                field[3][0] = tile;
                tiles[2] = tile;
            } else if (tiles[1] == null) {
                Tile tile = new Tile(Type.r);
                tile.height = game.camera.viewportHeight + tileSize;
                tile.xpos = 5 * tileSize;
                field[5][0] = tile;
                tiles[1] = tile;
            }
        }
    }

    private void drawField() {
        //draw the field
        for (int x = 0; x < 7; x++) {
            for (int y = 5; y >= 0; y--) {
                //if (corners.contains(new Vector2(x, y), false))
                //game.batch.draw(new TextureRegion(square), tileSize * x, tileSize * y, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 0.2f, 0.2f, 0);
                if (field[x][y] != null) {
                    Tile tile = field[x][y];
                    game.batch.setColor(1, 0, 0, 1 - tile.opacity);
                    game.batch.draw(new TextureRegion(square), tileSize * x, tileSize * y, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, 0);
                    game.batch.setColor(colors[tile.type].r, colors[tile.type].g, colors[tile.type].b, 1);
                    if (tile.height != y * tileSize) {
                        tile.checked = false;
                        if (tile.height > y * tileSize) {
                            if (tile.height - tile.velocity > y * tileSize) {
                                tile.height = tile.height - tile.velocity;
                                tile.velocity = tile.velocity + 1.9f;
                            } else {
                                tile.height = y * tileSize;
                                tile.velocity = 0;
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
                            }
                        }
                    }
                    game.batch.draw(new TextureRegion(types.get(tile.type)), tileSize * x, tile.height, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, tile.angle);
                }
            }
        }
    }

    private void drawConnected() {
        game.batch.setColor(Color.WHITE);
        dframes = dframes - 1;
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 6; y++) {
                if (field[x][y] != null) {
                    if (field[x][y].connected) {
                        //System.out.println("TILE AT " + x + ", " + y + " IS CONNECTED");
                        game.batch.draw(new TextureRegion(types.get(field[x][y].type)), tileSize * x, tileSize * y, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, field[x][y].angle);
                        if (dframes <= 1) {
                            field[x][y].opacity = 1;
                            game.setScreen(game.play);
                            //System.out.println("ADDED TO DBLOCKS");
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
                    game.batch.draw(new TextureRegion(square), block.xpos, block.height, tileSize / 2, tileSize / 2, tileSize / 5, tileSize / 5, 1, 1, block.angle);
                }
            }
        }
    }

    private void drawHolding() {
        //draw the piece being held
        if (holding != null) {
            game.batch.setColor(colors[holding.type].r, colors[holding.type].g, colors[holding.type].b, 0.5f);
            float time = (float) (System.currentTimeMillis() - holding.lastRotTime) / 200;
            if (time <= 1) {
                holding.angle = new Interpolation.SwingOut(1.5f).apply((holding.dir - 1) * 90, holding.dir * 90, time);
            } else {
                holding.angle = holding.dir * 90;
            }
            if (touched) {
                if (findFloor(tcolumn) <= 11) hcolumn = tcolumn;
                holding.xpos = tileSize * hcolumn;
                game.batch.draw(new TextureRegion(types.get(holding.type)), tileSize * hcolumn, holding.height, (float) Math.ceil(tileSize / 2), (float) Math.ceil(tileSize / 2), tileSize, tileSize, 1, 1, holding.angle);
                int row = findSpace(hcolumn);
                holding.height = row * tileSize;
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
                            if (x1 >= 0 && x1 < 7 && y1 >= 0 && y1 <= 5 && field[x1][y1] != null && field[x1][y1].height == y1 * tileSize && field[x1][y1].angle % 90 == 0 && field[x1][y1].sides[(i + 2) % 4]) {
                                //System.out.println("YES");
                                if (!visited.contains(field[x1][y1], true)) {
                                    //System.out.println("WE HAVEN'T RECORDED THIS TILE YET, ADDING TO THE STACK");
                                    stack.add(new Vector2(x1, y1));
                                    field[x1][y1].xpos = x1 * tileSize;
                                    visited.add(field[x1][y1]);
                                    field[x1][y1].parent = new Vector2(x0, y0);
                                }
                            } else {
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
                dframes = 10;
                game.bust.play();
                System.out.println(caught.size + " TILES ARE CAUGHT");
                System.out.println(visited.size + " TILES ARE CONNECTED");
            }
            for (int i = 0; i < visited.size; i++) {
                if (caught.contains(visited.get(i), true)) caught.removeValue(visited.get(i), true);
                visited.get(i).connected = true;
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
        generator.dispose();
        text.dispose();
        header.dispose();
	}

    public void processTouch() {
        if (dframes <= 0) {
            if (touched) {
                int x = (int) ((touchPos.x) / tileSize);
                int y = (int) ((touchPos.y) / tileSize);
                //if touch is within range
                if (holding == null) {
                    if (field[x][y] != null && field[x][y].height == y * tileSize) {
                        if (field[x][y].type > 0) {
                            holding = remove(x, y);
                            holding.checked = false;
                            moved = false;
                            game.twist.play();
                            //this section only sets holding, make sure it doesn't need a value
                            hrow = y;
                            hcolumn = x;
                            //System.out.println("X OFFSET IS " + offset.x);
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
                    if (moved || holding.height / tileSize > y) {
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
}