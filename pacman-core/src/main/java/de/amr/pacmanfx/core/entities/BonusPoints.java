package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingComp;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.entities.bonuspoints.comp.BonusPointsComp;

public class BonusPoints extends GameEntity {

    public BonusPoints(int value) {
        setComp(BonusPointsComp.class, new BonusPointsComp(value));
        setComp(RenderingComp.class, new RenderingComp(RenderingLayer.PROPS));
    }

    public BonusPointsComp points() {
        return reqComp(BonusPointsComp.class);
    }
}
