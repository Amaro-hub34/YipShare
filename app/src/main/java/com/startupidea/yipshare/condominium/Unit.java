package com.startupidea.yipshare.condominium;

public final class Unit {

    private final String id;
    private final String name;

    public Unit(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
