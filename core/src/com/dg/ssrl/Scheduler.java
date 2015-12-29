package com.dg.ssrl;

import java.util.ArrayList;
import java.util.List;

import static com.dg.ssrl.Components.*;

public class Scheduler {

    private List<Actor> queue = new ArrayList<Actor>();

    public void clear() {
        queue.clear();
    }

    public void addActor(Actor actor) {
        queue.add(actor);
    }

    public void update(World world) {
        if (queue.size() == 0) {
            return;
        }
        Actor toAct = queue.get(0);
        boolean acted = toAct.act(world);
        if (acted) {
            queue.add(queue.remove(0));
        }
    }
}
