/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.hud.livescounter;

import de.amr.basics.ecs.GameEntity;

public class LivesCounter extends GameEntity {

    public LivesCounter() {
        setComp(LivesCounterDataComp.class, new LivesCounterDataComp());
    }

    public LivesCounterDataComp data() {
        return reqComp(LivesCounterDataComp.class);
    }
}
