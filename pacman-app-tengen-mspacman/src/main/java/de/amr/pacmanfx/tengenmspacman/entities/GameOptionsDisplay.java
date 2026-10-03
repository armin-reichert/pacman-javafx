package de.amr.pacmanfx.tengenmspacman.entities;

import de.amr.basics.ecs.GameEntity;
import de.amr.pacmanfx.tengenmspacman.entities.gameoptionsdisplay.GameOptionsDataComp;

public class GameOptionsDisplay extends GameEntity {

    public GameOptionsDisplay() {
        setComponent(GameOptionsDataComp.class, new GameOptionsDataComp());
    }

    public GameOptionsDataComp options() {
        return assertComponent(GameOptionsDataComp.class);
    }
}
