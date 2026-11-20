package com.startupidea.yipshare.condominium;

public record Unit(String id, String name) {

    @Override
    public String toString() {
        return name;
    }
}
