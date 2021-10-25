package com.mygdx.game;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class Label {
    //for falling physics
    protected GlyphLayout layout;
    protected float width;
    protected float height;
    protected String text;
    protected float xpos = 0;
    protected float ypos = 1736;
    protected float velocity = 0;
    protected long lastTime;

    protected float opacity = 1;

    public Label(String text, BitmapFont font) {
        this.text = text;
        this.layout = new GlyphLayout(font, text);
        this.width = layout.width;
        this.height = layout.height;
    }

    public float center(OrthographicCamera camera) {
        return ((camera.viewportWidth - width) / 2);
    }
}
