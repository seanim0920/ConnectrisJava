package com.mygdx.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public enum Global {
    INSTANCE;

    AssetManager manager = new AssetManager();
    SpriteBatch batch;
    BitmapFont header;
    BitmapFont text;
    OrthographicCamera camera;
    int tileSize;

    Screen menu;
    Screen play;

    public Texture pixel;
    public Texture push;
    public Texture turn;

    boolean touched = false;
    Vector3 touchPos = new Vector3();

    Array<Texture> types = new Array<Texture>();
    Array<Texture> preview = new Array<Texture>();
    Texture pin;

    Music music;
    Sound drop;
    Sound danger;
    Sound twist;
    Sound bust;
}