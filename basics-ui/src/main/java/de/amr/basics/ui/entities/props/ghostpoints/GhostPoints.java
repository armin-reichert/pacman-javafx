package de.amr.basics.ui.entities.props.ghostpoints;

import de.amr.basics.ui.ecs.GameEntity;

public class GhostPoints extends GameEntity {

    public GhostPoints(int value) {
        setComp(GhostPointsComp.class, new GhostPointsComp(value));
    }

    public GhostPointsComp points() {
        return reqComp(GhostPointsComp.class);
    }
}
