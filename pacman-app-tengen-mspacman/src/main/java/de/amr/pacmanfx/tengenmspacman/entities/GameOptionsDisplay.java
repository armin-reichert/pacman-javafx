/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.entities;

import de.amr.basics.ecs.GameEntity;
import de.amr.pacmanfx.tengenmspacman.entities.gameoptionsdisplay.GameOptionsComp;

public class GameOptionsDisplay extends GameEntity {

    public GameOptionsDisplay() {
        setComponent(GameOptionsComp.class, new GameOptionsComp());
    }

    public GameOptionsComp options() {
        return assertComponent(GameOptionsComp.class);
    }
}
