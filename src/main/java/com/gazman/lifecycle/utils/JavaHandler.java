package com.gazman.lifecycle.utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ilya Gazman on 2/23/2018.
 */
public class JavaHandler {

    private ArrayList<Runnable> list;
    private boolean active;
    private static final HashMap<Thread, ArrayList<Runnable>> map = new HashMap<>();

    public void post(Runnable runnable) {

    }

    public void loop() {

        while (active) {

        }
    }

    public void stop() {
        active = false;
    }


}
