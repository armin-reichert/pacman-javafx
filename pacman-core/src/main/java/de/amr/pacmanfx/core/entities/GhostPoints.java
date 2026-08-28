package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.ghostpoints.comp.GhostPointsComp;

public class GhostPoints extends GameEntity {

    public GhostPoints(int value) {
        setComp(GhostPointsComp.class, new GhostPointsComp(value));
    }

    public GhostPointsComp value() {
        return reqComp(GhostPointsComp.class);
    }
}
