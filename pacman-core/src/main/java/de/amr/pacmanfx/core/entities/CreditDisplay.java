package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;

public class CreditDisplay extends GameEntity implements Renderable {

    public CreditDisplay() {
        setComp(CreditDataComp.class, new CreditDataComp());
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.HUD;
    }

    public CreditDataComp data() {
        return reqComp(CreditDataComp.class);
    }
}
