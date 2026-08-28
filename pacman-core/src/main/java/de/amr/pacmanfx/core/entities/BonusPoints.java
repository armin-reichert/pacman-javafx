package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.bonuspoints.comp.BonusPointsComp;

public class BonusPoints extends GameEntity {

    public BonusPoints(int value) {
        setComp(BonusPointsComp.class, new BonusPointsComp(value));
    }

    public BonusPointsComp points() {
        return reqComp(BonusPointsComp.class);
    }
}
