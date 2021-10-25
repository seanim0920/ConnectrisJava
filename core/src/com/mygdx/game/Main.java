package com.mygdx.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

class Main extends Game implements ApplicationListener {
	SpriteBatch batch;
	BitmapFont font;
	OrthographicCamera camera;

	@Override
	public void create() {
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1080, 1920);

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