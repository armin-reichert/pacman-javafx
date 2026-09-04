/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.QuerySet;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.ui.action.core.GameAppContext;

public class SceneWithoutLevel extends GameScene {

    private final QuerySet<GameEntity> entities = new QuerySet<>();

    public SceneWithoutLevel(GameAppContext app) {
        super(app);
    }

    public QuerySet<GameEntity> entities() {
        return entities;
    }
}
