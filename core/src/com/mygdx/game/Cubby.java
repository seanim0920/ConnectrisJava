package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;

public class Cubby extends Object {
    protected float xoffset = 0;
    protected float yoffset = 0;
    protected float size = 0;
    protected Tile tile = null;
    protected long lastChaTime = 0;
    protected boolean held = false;
    protected Vector2 oPos = new Vector2();
    protected Vector2 tPos = new Vector2();

    public Cubby(Vector2 oPos) {
        this.oPos = oPos;
    }
}
