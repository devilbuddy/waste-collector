package com.dg.ssrl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dg.ssrl.Components.*;

public class Scheduler {

    private AtomicInteger lockCount = new AtomicInteger(0);

    private List<Actor> queue = new ArrayList<Actor>();

    public void clear() {
        queue.clear();
    }

    public void addActor(Actor actor) {
        queue.add(actor);
    }

    public void removeActor(Actor actor) {
        queue.remove(actor);
    }

    public void lock() {
        lockCount.incrementAndGet();
    }

    public void unlock() {
        int c = lockCount.decrementAndGet();
        if (c < 0) {
            throw new RuntimeException("Can't unlock already unlocked scheduler");
        }
    }

    public void update(World world) {


        if (queue.size() == 0 || lockCount.get() > 0) {
            return;
        }


        int iterations = 0;
        while (iterations < 5) {
            Actor actor = queue.remove(0);
            if (actor.tick()) {
                if (actor.act(world)) {
                    actor.reset();
                    queue.add(actor);
                } else {
                    queue.add(0, actor);
                    break;
                }
            } else {
                queue.add(actor);
            }
            iterations++;
        }


    }

}
