/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.score.comp.ScoreDataComp;

public class Score extends GameEntity {

    public Score() {
        setComponent(ScoreDataComp.class, new ScoreDataComp());
    }

    public ScoreDataComp data() {
        return requireComponent(ScoreDataComp.class);
    }
}