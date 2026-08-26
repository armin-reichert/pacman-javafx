/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.bonus.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;

public record BonusDataComp(int symbolCode, int points) implements GameEntityComp {
}
