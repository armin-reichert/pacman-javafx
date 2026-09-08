/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs.comp;

public enum RenderingLayer {
    BACKGROUND (0),
    SCENE      (5),
    WORLD      (10),
    MESSAGE    (20),
    FOOD       (30),
    ACTORS     (40),
    PROPS      (50),
    HUD        (60),
    OVERLAY    (80),
    DEBUG      (90);

    private final int z;

    RenderingLayer(int z){
        this.z = z;
    }

    public int z() {
        return z;
    }
}
