package de.amr.pacmanfx.core.ecs.comp;

public enum RenderingLayer {
    BACKGROUND (0),
    WORLD      (10),
    MESSAGE    (20),
    FOOD       (30),
    ACTORS     (40),
    PROPS      (50),
    OVERLAY    (60),
    DEBUG      (70);

    private final int layer;

    RenderingLayer(int layer){
        this.layer = layer;
    }

    public int layer() {
        return layer;
    }
}
