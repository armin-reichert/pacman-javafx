package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.entities.bonuspoints.comp.BonusPointsComp;

public class BonusPoints extends GameEntity implements Renderable {

    public BonusPoints(int value) {
        setComp(BonusPointsComp.class, new BonusPointsComp(value));
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.PROPS;
    }

    public BonusPointsComp points() {
        return reqComp(BonusPointsComp.class);
    }
}
