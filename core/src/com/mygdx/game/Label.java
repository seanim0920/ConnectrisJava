package com.mygdx.game;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;

public class Label extends Object {
    //for falling physics
    protected BitmapFont font;
    protected GlyphLayout layout;
    protected float width;
    protected float height;
    protected String text;

    protected float opacity = 1;

    public Label(String text, BitmapFont font) {
        this.text = text;
        this.layout = new GlyphLayout(font, text);
        this.width = layout.width;
        this.height = layout.height;
        this.font = font;
    }

    public float center(OrthographicCamera camera) {
        return ((camera.viewportWidth - width) / 2);
    }
}