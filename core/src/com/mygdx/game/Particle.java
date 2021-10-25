package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import java.util.Random;

import java.util.ArrayList;

/**
 * Created by admin on 7/19/2017.
 */

public class Particle implements Pool.Poolable {
    //we'll store the sides that can be connected, angle and special characteristics of this tile here
    private Random rng = new Random();
    protected Color color;
    protected Vector2 position;
    protected float angle;
    protected float rspeed;
    protected Vector2 velocity;

    public Particle() {
    }

    public void blast(Color color, Vector2 position, Vector2 velocity) {
        this.color = new Color(color);
        this.position = position;
        this.rspeed = rng.nextInt(201)/100 - 1;
        this.velocity = velocity;
    }

    @Override
    public void reset() {
        color = new Color(Color.WHITE);
        position.set(0,0);
        velocity.set(0,0);
        rspeed = 0;
        angle = 0;
    }
}