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

public class Death extends Arcade implements Screen {
    FreeTypeFontGenerator generator;
    FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
    BitmapFont font;

    private boolean moved = false;
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

    //game variables
    private int score = 0;
    private int waitTime = 3200;
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

    public Death(final Tile[][] field) {
        this.field = field;

        parameter.size = 150;
        parameter.color = Color.WHITE;
        font = generator.generateFont(parameter); // font size 12 pixels

        gameover.play();
        end = true;
        label = new Label("SCORE", font);
        label2 = new Label(Integer.toString(score), font);
        label3 = new Label("RESTART", font);
        label4 = new Label("MENU", font);
        label.ypos = camera.viewportHeight + (tileSize * 11);
        label2.ypos = camera.viewportHeight + (tileSize * 9);
        label3.ypos = camera.viewportHeight + (tileSize * 7);
        label4.ypos = camera.viewportHeight + (tileSize * 5);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
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
        types.dispose();
        generator.dispose();
        drop.dispose();
        thud.dispose();
        danger.dispose();
        gameover.dispose();
        font.dispose();
    }

    private void drawField() {
        //draw the field
        font.setColor(Color.WHITE);
        font.draw(batch, label.layout, (camera.viewportWidth - label.width) / 2, label.ypos);
        font.draw(batch, label2.layout, (camera.viewportWidth - label2.width) / 2, label2.ypos);
        font.draw(batch, label3.layout, (camera.viewportWidth - label3.width) / 2, label3.ypos);
        font.draw(batch, label4.layout, (camera.viewportWidth - label4.width) / 2, label4.ypos);
        boolean falling = false;
        //draw the field
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y <= ceiling; y++) {
                if (field[x][y] != null) {
                    Tile tile = field[x][y];
                    batch.setColor(colors[tile.type].r, colors[tile.type].g, colors[tile.type].b, 1);
                    int height = (y - step) * tileSize;
                    if (tile.ypos > height) {
                        falling = true;
                        tile.ypos = tile.ypos - tile.velocity;
                        tile.velocity = tile.velocity + 1.9f;
                    } else if (tile.ypos < height) {
                        tile.ypos = height;
                        tile.velocity = 0;
                    }
                    batch.draw(new TextureRegion(pixel), tileSize * x, 100, (tileSize / 2), (tileSize / 2), tileSize, tileSize, 1, 1, tile.angle);
                }
            }
        }
        if (!falling) {
            label.velocity = 0;
            label2.velocity = 0;
            label3.velocity = 0;
            label4.velocity = 0;
            if (step <= ceiling)
                step++;
            else if (Gdx.input.isTouched() && touchPos.y < tileSize * 8)
                setScreen(play);
            else if (Gdx.input.isTouched() && touchPos.y < tileSize * 10)
                setScreen(menu);
        } else {
            label.velocity = label.velocity + 1.5f;
            label.ypos = label.ypos - label.velocity;
            label2.velocity = label2.velocity + 1.5f;
            label2.ypos = label2.ypos - label2.velocity;
            label3.velocity = label3.velocity + 1.5f;
            label3.ypos = label3.ypos - label3.velocity;
            label4.velocity = label4.velocity + 1.5f;
            label4.ypos = label4.ypos - label4.velocity;
        }
    }
}