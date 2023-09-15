package com.qendolin.betterclouds.gui;

public final class Bounds {
    private int x;
    private int y;
    private int width;
    private int height;

    public Bounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
    }

    public Bounds(Bounds other) {
        this.x = other.x;
        this.y = other.y;
        this.width = other.width;
        this.height = other.height;
    }

    public int x() {
        return x;
    }

    public Bounds setX(int x) {
        this.x = x;
        return this;
    }

    public int y() {
        return y;
    }

    public Bounds setY(int y) {
        this.y = y;
        return this;
    }

    public int width() {
        return width;
    }

    public int limitX() {
        return x + width;
    }

    public Bounds setWidth(int width) {
        this.width = width;
        return this;
    }

    public int height() {
        return height;
    }

    public int limitY() {
        return y + height;
    }

    public Bounds setHeight(int height) {
        this.height = height;
        return this;
    }

    public Bounds setSize(int width, int height) {
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
        return this;
    }

    public Bounds expand(int dx, int dy) {
        x -= dx;
        y -= dy;
        width += Math.max(0, dx*2);
        height += Math.max(0, dy*2);
        return this;
    }

    public Bounds expand(int up, int right, int down, int left) {
        x -= left;
        y -= up;
        width += Math.max(0, left + right);
        height += Math.max(0, up + down);
        return this;
    }

    public Bounds offset(int dx, int dy) {
        x += dx;
        y += dy;
        return this;
    }

    public Bounds inset(int dx, int dy) {
        x += dx;
        y += dy;
        width = Math.max(0, width-dx*2);
        height = Math.max(0, height-dy*2);
        return this;
    }

    public Bounds inset(int up, int right, int down, int left) {
        x += left;
        y += up;
        width = Math.max(0, width - left - right);
        height = Math.max(0, height - up - down);
        return this;
    }

    public boolean overlaps(int x, int y) {
        return x >= this.x && y >= this.y && x <= limitX() && y <= limitY();
    }

    public boolean overlaps(double x, double y) {
        return x >= this.x && y >= this.y && x <= limitX() && y <= limitY();
    }

    public int centerX() {
        return x + width/2;
    }

    public int centerY() {
        return y + height/2;
    }
}
