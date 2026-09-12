/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes.playscene;


import de.amr.basics.InfoMap;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rendering.Renderable;

public class GameLevelRenderable implements Renderable {

    private final InfoMap renderInfo;
    private final GameLevel level;

    public GameLevelRenderable(InfoMap renderInfo, GameLevel level) {
        this.renderInfo = renderInfo;
        this.level = level;
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.SCENE;
    }

    public GameLevel level() {
        return level;
    }

    public InfoMap renderInfo() {
        return renderInfo;
    }
}
