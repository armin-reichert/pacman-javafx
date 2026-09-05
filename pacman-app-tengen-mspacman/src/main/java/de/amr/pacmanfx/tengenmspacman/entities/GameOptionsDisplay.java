package de.amr.pacmanfx.tengenmspacman.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.tengenmspacman.entities.gameoptionsdisplay.GameOptionsDataComp;

public class GameOptionsDisplay extends GameEntity {

    public GameOptionsDisplay() {
        setComp(GameOptionsDataComp.class, new GameOptionsDataComp());
    }

    public GameOptionsDataComp options() {
        return reqComp(GameOptionsDataComp.class);
    }
}
