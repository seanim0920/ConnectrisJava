package com.mygdx.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

class Main extends Game implements ApplicationListener {
	private static Preferences prefs;
	AssetManager manager = new AssetManager();
	SpriteBatch batch;
	BitmapFont header;
    BitmapFont text;
	OrthographicCamera camera;
	int tileSize;

	Screen menu;
	Screen play;

	public Texture pixel;

    boolean touched = false;
    Vector3 touchPos = new Vector3();

	Array<Texture> types = new Array<Texture>();
	Texture pin;

	Music music;
	Sound drop;
	Sound danger;
	Sound twist;
	Sound bust;

	@Override
	public void create() {
		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888); //try RGBA4444 later
		pixmap.setBlending(Pixmap.Blending.None);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		pixel = new Texture(pixmap);
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1080, 1920);
		this.tileSize = 153;
		this.setScreen(new Splash(this));
		prefs = Gdx.app.getPreferences("My Preferences");
		prefs.putBoolean("First", false);
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	public void dispose() {
		// dispose of all the native resources
		batch.dispose();
		manager.dispose();
	}
}