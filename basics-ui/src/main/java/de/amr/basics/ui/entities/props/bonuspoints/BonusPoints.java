package de.amr.basics.ui.entities.props.bonuspoints;

import de.amr.basics.ecs.GameEntity;

public class BonusPoints extends GameEntity {

    public BonusPoints(int value) {
        setComp(BonusPointsComp.class, new BonusPointsComp(value));
    }

    public BonusPointsComp points() {
        return reqComp(BonusPointsComp.class);
    }
}
