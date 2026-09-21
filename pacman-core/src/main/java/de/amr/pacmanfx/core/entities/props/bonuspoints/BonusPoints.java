package de.amr.pacmanfx.core.entities.props.bonuspoints;

import de.amr.basics.ui.ecs.GameEntity;

public class BonusPoints extends GameEntity {

    public BonusPoints(int value) {
        setComp(BonusPointsComp.class, new BonusPointsComp(value));
    }

    public BonusPointsComp points() {
        return reqComp(BonusPointsComp.class);
    }
}
