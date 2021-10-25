package com.mygdx.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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
	SpriteBatch batch;
	BitmapFont header;
    BitmapFont text;
	OrthographicCamera camera;

	public Texture square;

    boolean touched = false;
    Vector3 touchPos = new Vector3();

	Array<Texture> types = new Array<Texture>();

	Music gameover;
	Sound drop;
	Sound danger;
	Sound twist;
	Sound bust;

	@Override
	public void create() {
		drop = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		bust = Gdx.audio.newSound(Gdx.files.internal("bust.wav"));
		twist = Gdx.audio.newSound(Gdx.files.internal("twist.wav"));

		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1080, 1920);

		int unit = (int)Math.floor(200/5);
		int half = (int)Math.floor(200/2);

		Pixmap pixmap = new Pixmap(200, 200, Pixmap.Format.RGBA8888); //try RGBA4444 later
		pixmap.setBlending(Pixmap.Blending.None);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		square = new Texture(pixmap);

		//making the "box" tile
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		pixmap.setColor(Color.CLEAR);
		pixmap.fillRectangle(unit,unit,3*unit,3*unit);
		types.add(new Texture(pixmap));

		int radius = unit * 2;

		//making the "i" tile
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		pixmap.setColor(Color.CLEAR);
		pixmap.fillRectangle(unit,unit,3*unit,3*unit);
		pixmap.fillRectangle(2*unit,0,unit,3*unit);
		types.add(new Texture(pixmap));

		//making the "l" tile
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		pixmap.setColor(Color.CLEAR);
		pixmap.fillRectangle(unit,unit,3*unit,3*unit);
		pixmap.fillRectangle(0,2*unit,200,unit);
		types.add(new Texture(pixmap));

		//making the "r" tile
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		pixmap.setColor(Color.CLEAR);
		pixmap.fillRectangle(unit,unit,3*unit,3*unit);
		pixmap.fillRectangle(2*unit,0,unit,half);
		pixmap.fillRectangle(half,2*unit,half,unit);
		types.add(new Texture(pixmap));

		//making the "t" tile
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		pixmap.setColor(Color.CLEAR);
		pixmap.fillRectangle(unit,unit,3*unit,3*unit);
		pixmap.fillRectangle(2*unit,0,unit,200);
		pixmap.fillRectangle(half,2*unit,half,unit);
		types.add(new Texture(pixmap));

		//making the "plus" tile
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		pixmap.setColor(Color.CLEAR);
		pixmap.fillRectangle(unit,unit,3*unit,3*unit);
		pixmap.fillRectangle(2*unit,0,unit,200);
		pixmap.fillRectangle(0,2*unit,200,unit);
		types.add(new Texture(pixmap));
		pixmap.dispose();

		this.setScreen(new Menu(this));
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	public void dispose() {
		// dispose of all the native resources
		batch.dispose();
	}
}