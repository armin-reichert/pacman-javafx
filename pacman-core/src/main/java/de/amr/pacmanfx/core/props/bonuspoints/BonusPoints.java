package de.amr.pacmanfx.core.props.bonuspoints;

import de.amr.pacmanfx.core.ecs.GameEntity;

public class BonusPoints extends GameEntity {

    public BonusPoints(int value) {
        setComp(BonusPointsComp.class, new BonusPointsComp(value));
    }

    public BonusPointsComp points() {
        return reqComp(BonusPointsComp.class);
    }
}
