package de.amr.basics.ui.entities.props.ghostpoints;

import de.amr.basics.ecs.GameEntity;

public class GhostPoints extends GameEntity {

    public GhostPoints(int value) {
        setComponent(GhostPointsComp.class, new GhostPointsComp(value));
    }

    public GhostPointsComp points() {
        return assertComponent(GhostPointsComp.class);
    }
}
