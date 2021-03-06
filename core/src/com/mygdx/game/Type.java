package com.mygdx.game;

public enum Type {
    i, l, r, t, p, a;

    public static Type valueOf(int type) {
        switch (type) {
            case 0:
                return i;
            case 1:
                return l;
            case 2:
                return r;
            case 3:
                return t;
            case 4:
                return p;
            default:
                return a;
        }
    }
}
