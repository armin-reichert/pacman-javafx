/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.livescounter.comp.LivesCounterData;

public class LivesCounter extends GameEntity {

    public LivesCounter() {
        setComponent(LivesCounterData.class, new LivesCounterData());
    }

    public LivesCounterData data() {
        return requireComponent(LivesCounterData.class);
    }
}
