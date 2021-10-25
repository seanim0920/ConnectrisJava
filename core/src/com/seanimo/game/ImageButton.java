package com.seanimo.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;

public class ImageButton {
    protected Texture image;
    protected float xpos = 0;
    protected float ypos = 1736;
    protected float width;
    protected float height;

    public ImageButton(Texture image, float xpos, float ypos, int border) {
        this.image = image;
        this.xpos = xpos - border;
        this.ypos = ypos - border;
        width = image.getWidth() + (border*2);
        height = image.getHeight() + (border*2);
    }

    public boolean isTouched(Vector3 touchPos) {
        if (touchPos.x > xpos && touchPos.x < xpos + width) {
            return true;
        } else {
            return false;
        }
    }
}
