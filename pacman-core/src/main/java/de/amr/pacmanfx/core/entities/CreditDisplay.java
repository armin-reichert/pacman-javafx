package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;

public class CreditDisplay extends GameEntity {

    public CreditDisplay() {
        setComp(CreditDataComp.class, new CreditDataComp());
    }

    public CreditDataComp data() {
        return reqComp(CreditDataComp.class);
    }
}
