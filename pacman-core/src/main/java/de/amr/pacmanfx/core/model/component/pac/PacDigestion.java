/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.component.pac;

import de.amr.pacmanfx.core.model.component.GameEntityComponent;

public class PacDigestion implements GameEntityComponent {

    public static final byte REST_FOREVER = -1;

    private long restingTicks;
    private long starvingTicks;

    @Override
    public void reset() {
        restingTicks = 0;
        starvingTicks = 0;
    }

    public void setRestingTicks(long restingTicks) {
        this.restingTicks = restingTicks;
    }

    public long restingTicks() {
        return restingTicks;
    }

    public void setStarvingTicks(long starvingTicks) {
        this.starvingTicks = starvingTicks;
    }

    public long starvingTicks() {
        return starvingTicks;
    }

    @Override
    public String toString() {
        return "PacDigestion{" +
            "restingTicks=" + restingTicks +
            ", starvingTicks=" + starvingTicks +
            '}';
    }
}
