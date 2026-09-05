/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.InfoMap;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.CommonRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.GameSceneRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;
import static java.util.Objects.requireNonNull;

/**
 * Renders the 2D play scene for the Arcade Pac-Man games. The XXL games use a generic map renderer that does not need
 * any graphics.
 */
public class Arcade_PlayScene2D_Renderer extends GameSceneRenderer implements SpriteRenderer {

    private final SpriteSheet spriteSheet;

    private final BaseRenderer levelRenderer;

    public Arcade_PlayScene2D_Renderer(GameScene gameScene, ActorSpriteAnimController animController, Canvas canvas, SpriteSheet spriteSheet) {
        super(canvas);

        requireNonNull(gameScene);
        requireNonNull(animController);
        this.spriteSheet = requireNonNull(spriteSheet);
        final SceneCanvasRenderingComp r2D = gameScene.reqComp(SceneCanvasRenderingComp.class);
        final GameVariantRenderConfig renderConfig = gameScene.app().gameVariants().currentGameVariant().uiConfig().renderConfig();

        setDebugInfoRenderer(createDefaultSceneDebugRenderer(gameScene, canvas));
        levelRenderer = r2D.configureRenderer(renderConfig.createGameLevelRenderer(animController, canvas));
    }

    @Override
    public SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public void render(Object r, long tick) {
        if (!(r instanceof Arcade_PlayScene2D playScene)) {
            return;
        }

        final GameContext game = playScene.game();
        final GameSession session = game.session();

        // Level creation happens by handling a game event after the play scene has been activated. Therefore,
        // the game level is not yet existing for the first two ticks after this scene got active.
        session.optLevel().ifPresent(level -> {
            levelRenderer.setInfoMap(createLevelRenderInfo(level, playScene));
            levelRenderer.render(level, tick);
        });
    }

    private InfoMap createLevelRenderInfo(GameLevel level, Arcade_PlayScene2D playScene2D) {
        final var info = new InfoMap();
        final boolean energizerVisible = level.heartbeat().state() == Pulse.State.ON;
        final boolean mapIsEmpty = level.food().remainingFoodCount() == 0;
        info.put(CommonRenderInfoKey.ENERGIZER_VISIBLE, energizerVisible);
        info.put(CommonRenderInfoKey.MAP_EMPTY, mapIsEmpty);
        info.put(CommonRenderInfoKey.MAP_BRIGHT, false);
        info.put(CommonRenderInfoKey.MAP_FLASHING, false);
        playScene2D.optLevelCompletedAnimation().flatMap(LevelCompletedAnimation::flashingState).ifPresent(flashing -> {
            info.put(CommonRenderInfoKey.MAP_BRIGHT,   flashing.isHighlighted());
            info.put(CommonRenderInfoKey.MAP_FLASHING, flashing.isFlashing());
        });
        return info;
    }
}