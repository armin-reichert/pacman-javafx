/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.entities.bonus;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;

public class BonusDataComp implements GameEntityComponent {

    private final int symbolCode;

    private final int points;

    public BonusDataComp(int symbolCode, int points) {
        this.symbolCode = symbolCode;
        this.points = points;
    }

    public int symbolCode() {
        return symbolCode;
    }

    public int points() {
        return points;
    }

    @Override
    public void reset() {}
}
