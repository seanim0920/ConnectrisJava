package com.mygdx.game;

/**
 * Created by admin on 7/19/2017.
 */

public class Tile {
    //for falling physics
    protected float height = 1736;
    protected float velocity = 0;

    protected int type = 0;
    protected float angle = 0;
    protected int dir = 0;
    protected boolean connected = false;
    protected boolean destroyed = false;
    protected boolean falling = false;
    protected boolean[] sides = new boolean[4]; //contains the sides that are linked from 0 - 3 clockwise starting from the top

    public Tile(int type, int dir) {
        this.dir = dir;
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
        for (int loop = 0; loop < dir; loop++) {
            boolean[] temp = sides.clone();
            for (int i = 0; i < 4; i++) {
                System.out.println("TURNING SIDE " + sides[((i+1)%4)] + " INTO " + temp[i]);
                sides[(i + 1) % 4] = temp[i];
            }
        }
    }
    public void rotate() {
        boolean[] temp = sides.clone();
        dir = (dir + 1) % 4;
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