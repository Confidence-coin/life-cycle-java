package com.gazman.lifecycle.utils.handler;

import java.util.LinkedList;

/**
 * Created by Ilya Gazman on 2/23/2018.
 */
public class Looper {

    private boolean active;
    private LinkedList<Runnable> jobs = new LinkedList<>();

    public void loop() {
        active = true;
        while (active) {
            synchronized (this) {
                if (jobs.isEmpty()) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        active = false;
                        return;
                    }
                    continue;
                }
                while (true) {
                    Runnable job;
                    synchronized (this) {
                        if (jobs.isEmpty()) {
                            break;
                        }
                        job = jobs.remove();
                    }
                    job.run();
                }
            }
        }
    }

    public synchronized void addJob(Runnable job) {
        jobs.add(job);
        notifyAll();
    }

    public synchronized void stop() {
        active = false;
        notifyAll();
    }
}
