package com.dg.ssrl;

import java.util.HashMap;

/**
 * Created by magnus on 2015-09-13.
 */
public class Entity {

    interface Component {}

    public static class Position implements Component {
        public int x;
        public int y;

        public void set(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void translate(Direction direction) {
            x += direction.dx;
            y += direction.dy;
        }

    }

    private HashMap<Class<? extends Component>, Component> components = new HashMap<Class<? extends Component>, Component>();

    public final int id;

    public Entity(int id) {
        this.id = id;
    }

    public <T> T getComponent(Class<T> clazz) {
        T component = (T)components.get(clazz);
        return component;
    }

    public void addComponent(Component component) {
        components.put(component.getClass(), component);
    }
}
