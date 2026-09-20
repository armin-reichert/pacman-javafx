package de.amr.pacmanfx.core.props.ghostpoints;

import de.amr.pacmanfx.core.ecs.GameEntity;

public class GhostPoints extends GameEntity {

    public GhostPoints(int value) {
        setComp(GhostPointsComp.class, new GhostPointsComp(value));
    }

    public GhostPointsComp points() {
        return reqComp(GhostPointsComp.class);
    }
}
