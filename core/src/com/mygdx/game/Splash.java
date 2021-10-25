package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

class Load extends Thread {
    public void run(final Main game) {
        game.manager.load("images/b.png", Texture.class);
        game.manager.load("images/i.png", Texture.class);
        game.manager.load("images/l.png", Texture.class);
        game.manager.load("images/r.png", Texture.class);
        game.manager.load("images/t.png", Texture.class);
        game.manager.load("images/p.png", Texture.class);
        game.manager.load("images/pin.png", Texture.class);
        game.manager.load("drop.wav", Sound.class);
        game.manager.load("bust.wav", Sound.class);
        game.manager.load("twist.wav", Sound.class);
        game.manager.load("danger.wav", Sound.class);
        game.manager.load("music.mp3", Music.class);
        game.manager.finishLoading();
        game.types.add(game.manager.get("images/i.png", Texture.class));
        game.types.add(game.manager.get("images/l.png", Texture.class));
        game.types.add(game.manager.get("images/r.png", Texture.class));
        game.types.add(game.manager.get("images/t.png", Texture.class));
        game.types.add(game.manager.get("images/p.png", Texture.class));
        game.types.add(game.manager.get("images/b.png", Texture.class));
        game.pin = game.manager.get("images/pin.png", Texture.class);
        game.drop = game.manager.get("drop.wav", Sound.class);
        game.bust = game.manager.get("bust.wav", Sound.class);
        game.twist = game.manager.get("twist.wav", Sound.class);
        game.danger = game.manager.get("danger.wav", Sound.class);
        game.music = game.manager.get("music.mp3", Music.class);
        game.menu = new Menu(game);
        game.play = new Arcade(game, game.manager.get("music.mp3", Music.class));
    }
}

public class Splash implements Screen {
    private final Main game;
    private int x = 0;
    private int y = 0;
    private FreeTypeFontGenerator generator;
    private FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
    private BitmapFont header;

    public Splash(final Main game) {
        this.game = game;
        generator = new FreeTypeFontGenerator(Gdx.files.internal("font.otf"));
        parameter.color = Color.WHITE;
        parameter.size = 120;
        header = generator.generateFont(parameter);
        game.header = header;
        y = game.tileSize * 12;
        new Load().run(game);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.camera.update();

        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin();

        game.batch.setColor(Color.WHITE);
        game.batch.draw(new TextureRegion(game.pixel), 0, game.camera.viewportHeight - (game.tileSize + 5), (game.tileSize / 2), (game.tileSize / 2), game.camera.viewportWidth, game.tileSize / 7, 1, 1, 0);
        game.batch.setColor(Color.BLACK);
        game.batch.draw(new TextureRegion(game.pixel), 0, (game.camera.viewportHeight - game.tileSize), (game.tileSize / 2), (game.tileSize / 2), game.camera.viewportWidth, 5 * game.tileSize / 5, 1, 1, 0);
        game.batch.setColor(Color.WHITE);

        header.draw(game.batch, "CONNECTRIS", x, y);

        game.batch.setColor(1, 1, 1, 1);

        game.batch.end();

        x = (x + 1) % (int)game.camera.viewportWidth;

        if (game.manager.update())
            game.setScreen(game.menu);
    }

    @Override
    public void resize(int x, int y) {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void show() {

    }

    @Override
    public void dispose() {

    }
}
