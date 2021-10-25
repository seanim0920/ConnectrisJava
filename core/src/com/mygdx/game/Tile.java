package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by admin on 7/19/2017.
 */

public class Tile {
    //we'll store the sides that can be connected, angle and special characteristics of this tile here
    protected int type = 0;
    protected int angle = 0;
    protected boolean rotating = false;
    protected boolean connected = false;
    protected boolean destroyed = false;
    protected boolean[] sides = new boolean[4]; //contains the sides that are linked from 0 - 3 clockwise starting from the top

    public Tile(int type, int angle) {
        this.angle = angle;
        this.type = type;
        switch (type) {
            case 6:
                sides[1] = true;
            case 5:
                sides[2] = true;
            case 4:
                sides[3] = true;
            case 2:
                sides[0] = true;
                break;
            case 3:
                sides[1] = true;
                sides[3] = true;
                break;
            default:
                break;
        }
    }
    public void rotate() {
        angle = (angle + 1) % 4;
        boolean[] temp = sides.clone();
        for (int i = 0; i < 4; i++) {
            System.out.println("TURNING SIDE " + sides[((i+1)%4)] + " INTO " + temp[i]);
            sides[(i + 1) % 4] = temp[i];
        }
    }

    public void touched() { //when the player touches this tile
    }

    public void hit() { //when a tile falls on this one
    }

    public void connect() { //when it's caught in an enclosed loop
        connected = true;
    }

    public void disconnect() { //when it's caught in an enclosed loop
        connected = false;
    }

    public void destroy() { //when it's caught in an enclosed loop
        destroyed = true;
    }

    public void tapped() {
    }
}