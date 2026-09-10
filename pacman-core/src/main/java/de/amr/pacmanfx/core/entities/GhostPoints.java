package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.entities.ghostpoints.comp.GhostPointsComp;

public class GhostPoints extends GameEntity implements Renderable {

    public GhostPoints(int value) {
        setComp(GhostPointsComp.class, new GhostPointsComp(value));
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.PROPS;
    }

    public GhostPointsComp points() {
        return reqComp(GhostPointsComp.class);
    }
}
