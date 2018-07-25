package ru.mrbrikster.chatty.dependencies;

import java.util.HashMap;

public class DependencyPool {

    private HashMap<Class, Object> dependencies = new HashMap<Class, Object>();

    @SuppressWarnings("all")
    public <T> T getDependency(Class<T> clazz) {
        return (T) dependencies.get(clazz);
    }

    public <T> void putDependency(T dependency) {
        dependencies.put(dependency.getClass(), dependency);
    }

    public <T> void removeDependency(Class<T> clazz) {
        dependencies.remove(clazz);
    }

    public <T> void removeDependency(T dependency) {
        dependencies.remove(dependency.getClass());
    }

    public <T> void removeAll() {
        dependencies.clear();
    }

}
