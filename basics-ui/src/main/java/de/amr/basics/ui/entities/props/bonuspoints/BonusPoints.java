package de.amr.basics.ui.entities.props.bonuspoints;

import de.amr.basics.ecs.GameEntity;

public class BonusPoints extends GameEntity {

    public BonusPoints(int value) {
        setComponent(BonusPointsComp.class, new BonusPointsComp(value));
    }

    public BonusPointsComp points() {
        return assertComponent(BonusPointsComp.class);
    }
}
