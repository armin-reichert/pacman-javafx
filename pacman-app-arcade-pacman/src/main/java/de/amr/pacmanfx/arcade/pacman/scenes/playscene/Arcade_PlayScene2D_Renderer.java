/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes.playscene;

import de.amr.basics.InfoMap;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;
import static java.util.Objects.requireNonNull;

/**
 * Renders the game level in a 2D play scene for the Arcade Pac-Man games. The XXL games use a generic map renderer that does not need
 * any graphics.
 */
public class Arcade_PlayScene2D_Renderer extends BaseRenderer {

    private final Renderer levelRenderer;

    public Arcade_PlayScene2D_Renderer(GameScene gameScene, Canvas canvas, Renderer levelRenderer) {
        super(canvas);
        requireNonNull(gameScene);

        this.levelRenderer = requireNonNull(levelRenderer);
        levelRenderer.scalingProperty().bind(scalingProperty());
        levelRenderer.backgroundColorProperty().bind(backgroundColorProperty());

        setDebugInfoRenderer(createDefaultSceneDebugRenderer(gameScene, canvas));
    }

    @Override
    public void render(Renderable r, long tick) {
        requireNonNull(r);
        // This is the "record" type matching pattern
        if (r instanceof GameLevelRenderable(GameLevel level, InfoMap renderInfo)) {
            levelRenderer.info().putAll(renderInfo);
            levelRenderer.render(level, tick);
        }
    }
}