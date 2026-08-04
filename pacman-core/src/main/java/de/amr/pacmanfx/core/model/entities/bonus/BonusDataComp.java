/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.entities.bonus;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;

public record BonusDataComp(int symbolCode, int points) implements GameEntityComponent {

    @Override
    public void reset() {}
}
