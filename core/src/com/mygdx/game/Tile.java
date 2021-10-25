package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import java.awt.Color;

/**
 * Created by admin on 7/19/2017.
 */

public class Tile {
    //for falling physics
    protected float xpos = 0;
    protected float height = 1736;
    protected float velocity = 0;

    protected boolean canMove = true;
    protected boolean checked = false;
    protected long lastRotTime;
    protected Vector2 parent = new Vector2(-1,-1);
    protected float opacity = 1;
    protected int type;
    protected float angle = 0;
    protected int dir = 0;
    protected boolean placed = false;
    protected boolean caught = false;
    protected boolean connected = false;
    protected boolean destroyed = false;
    protected boolean falling = false;
    protected boolean[] sides = new boolean[4]; //contains the sides that are linked from 0 - 3 counter-clockwise starting from the top

    public Tile(Type type) {
        switch (type) {
            case p:
                this.type = 4;
                for (int i = 0; i < 4; i++)
                    sides[i] = true;
                break;
            case t:
                this.type = 3;
                for (int i = 1; i < 4; i++)
                    sides[i] = true;
                break;
            case r:
                this.type = 2;
                sides[3] = true;
                sides[2] = true;
                break;
            case i:
                this.type = 0;
                sides[2] = true;
                break;
            case l:
                this.type = 1;
                sides[2] = true;
                sides[0] = true;
                break;
            default:
                break;
        }
        for (int loop = 0; loop < dir; loop++) {
            boolean[] temp = sides.clone();
            for (int i = 0; i < 4; i++) {
                sides[(i + 1) % 4] = temp[i];
            }
        }
    }
    public void rotate(boolean right) {
        lastRotTime = System.currentTimeMillis();
        boolean[] temp = sides.clone();
        if (right) {
            dir = (dir + 1) % 4;
            for (int i = 0; i < 4; i++) {
                sides[(i + 1) % 4] = temp[i];
            }
        } else {
            dir = ((((dir - 1) % 4) + 4) % 4);
            for (int i = 0; i < 4; i++) {
                sides[((((i - 1) % 4) + 4) % 4)] = temp[i];
            }
        }
    }

    public void touched() { //when the player touches this tile
    }

    public void hit() { //when a tile falls on this one
    }

    public void destroy() { //when it's caught in an enclosed loop
        destroyed = true;
    }

    public void tapped() {
    }
}