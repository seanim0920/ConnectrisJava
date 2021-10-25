package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import com.badlogic.gdx.math.Vector3;

public class Button {
    protected Label label;
    protected float xpos = 0;
    protected float ypos = 1736;
    protected float width;
    protected float height;

    public Button(Label label, int border) {
        this.label = label;
        xpos = label.xpos - border;
        ypos = label.ypos - border;
        width = label.width + (border*2);
        height = label.height + (border*2);
    }

    public boolean isTouched(Vector3 touchPos) {
        if (touchPos.x > xpos && touchPos.x < xpos + width) {
            return true;
        } else {
            return false;
        }
    }
}