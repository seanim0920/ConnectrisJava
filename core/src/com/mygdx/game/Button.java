package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Button extends Label {
    protected Rectangle rectangle;

    public Button(String text, BitmapFont font, int border) {
        super(text, font);
        rectangle = new Rectangle((int)xpos, (int)ypos, (int)width, (int)height);
    }

    public boolean isTouched(Vector3 touchPos) {
        if (rectangle.contains(new Vector2(touchPos.x, touchPos.y))) {
            return true;
        } else {
            return false;
        }
    }
}