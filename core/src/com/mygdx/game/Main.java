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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

class Main extends Game implements ApplicationListener {
	private static Preferences prefs;
	AssetManager manager = new AssetManager();
	SpriteBatch batch;
	BitmapFont text;
	OrthographicCamera camera;
	int tileSize;
	BitmapFont font;

	FreeTypeFontGenerator generator;
	FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

	public Random rng = new Random();

	Screen menu;
	Screen play;

	public Texture pixel;
	public Texture floor;
	public Texture push;
	public Texture turn;

	boolean touchedChanged = false;
	boolean touched = false;
	Vector3 touchPos = new Vector3();
	Vector3 oldPos = new Vector3();

	Texture types;
	Array<Texture> preview = new Array<Texture>();
	Texture pin;

	Music music;
	Sound drop;
	Sound danger;
	Sound twist;
	Sound bust;

	Color[] colors = {Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED};

	@Override
	public void create() {
		generator = new FreeTypeFontGenerator(Gdx.files.internal("font.otf"));
		parameter.size = 120;
		parameter.color = Color.WHITE;
		font = generator.generateFont(parameter); // font size 12 pixels
		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888); //try RGBA4444 later
		pixmap.setBlending(Pixmap.Blending.None);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		pixel = new Texture(pixmap);
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1080, 1920);
		this.tileSize = (int)(camera.viewportWidth/7);
		this.setScreen(new Splash(this));
		prefs = Gdx.app.getPreferences("My Preferences");
		prefs.putBoolean("First", false);
	}

	@Override
	public void render() {
		super.render();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();

		batch.setProjectionMatrix(camera.combined);

		checkTouch();

		batch.begin();

		process();

		batch.setColor(Color.WHITE);
		batch.draw(new TextureRegion(pixel), 0, camera.viewportHeight - (tileSize + 5), (tileSize / 2), (tileSize / 2), camera.viewportWidth, tileSize / 7, 1, 1, 0);
		batch.setColor(Color.BLACK);
		batch.draw(new TextureRegion(pixel), 0, (camera.viewportHeight - tileSize), (tileSize / 2), (tileSize / 2), camera.viewportWidth, 5 * tileSize / 5, 1, 1, 0);
		batch.setColor(Color.WHITE);

		drawText();

		batch.setColor(1, 1, 1, 1);

		batch.end();
	}

	@Override
	public void dispose() {
		// dispose of all the native resources
		batch.dispose();
		manager.dispose();
	}

	public void drawText() {
	}

	public void checkTouch() {
		if (camera != null) {
			if (Gdx.input.isTouched()) {
				touchPos = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
				if (touched) {
					processTouching(true, false);
				} else {
					touched = true;
					processTouching(true, true);
				}
				oldPos = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
			} else {
				if (!touched) {
					processTouching(false, false);
				} else {
					touched = false;
					processTouching(false, true);
				}
			}
		}
	}

	public void process() {
	}

	public void processTouching(boolean touching, boolean changed) {
	}
}