package com.dg.ssrl;

import java.util.HashMap;

public class Entity {
    public interface Component {}

    private HashMap<Class<? extends Component>, Component> components = new HashMap<Class<? extends Component>, Component>();

    public final int id;
    public boolean alive = true;

    public Entity(int id) {
        this.id = id;
    }

    public <T> T getComponent(Class<T> clazz) {
        return (T)components.get(clazz);
    }

    public void addComponent(Component component) {
        components.put(component.getClass(), component);
    }

    @Override
    public String toString() {
        return "Entity{" +
                "components=" + components +
                ", id=" + id +
                '}';
    }
}
