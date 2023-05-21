package com.qendolin.betterclouds.clouds;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL33.*;

public class GlTimer implements AutoCloseable {

    private int query;
    private int queryWaiting;
    private boolean first = true;
    private int accumFrames = 0;
    private List<Double> history = new ArrayList<>();

    public GlTimer() {
        query = glGenQueries();
        queryWaiting = glGenQueries();
    }

    public void start() {
        glBeginQuery(GL_TIME_ELAPSED, query);
    }

    public void stop() {
        glEndQuery(GL_TIME_ELAPSED);

        int done = queryWaiting;
        queryWaiting = query;
        query = done;

        if(first) {
            first = false;
            return;
        }

        long ns = glGetQueryObjectui64(done, GL_QUERY_RESULT);
        history.add(ns / 1e6);
        accumFrames++;
    }

    public List<Double> get() {
        return history;
    }

    public int frames() {
        return accumFrames;
    }

    public void reset() {
        history = new ArrayList<>();
        accumFrames = 0;
    }

    public void close() {
        glDeleteQueries(new int[] {query, queryWaiting});
    }
}
