package de.amr.pacmanfx.tengenmspacman.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.tengenmspacman.entities.gameoptionsdisplay.GameOptionsComp;

public class GameOptionsDisplay extends GameEntity {

    public GameOptionsDisplay() {
        setComp(GameOptionsComp.class, new GameOptionsComp());
    }

    public GameOptionsComp options() {
        return reqComp(GameOptionsComp.class);
    }
}
