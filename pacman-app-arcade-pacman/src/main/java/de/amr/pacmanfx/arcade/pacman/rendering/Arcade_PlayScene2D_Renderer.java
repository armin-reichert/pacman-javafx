/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.InfoMap;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingComp;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.rules.GameRules;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.*;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.*;
import javafx.scene.canvas.Canvas;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Renders the 2D play scene for the Arcade Pac-Man games. The XXL games use a generic map renderer that does not need
 * any graphics.
 */
public class Arcade_PlayScene2D_Renderer extends BaseRenderer implements SpriteRenderer {

    private final SpriteSheet spriteSheet;
    private final GameLevelRenderer levelRenderer;
    private final BaseRenderer actorRenderer;
    private final BaseGameSceneDebugInfoRenderer debugRenderer;

    public Arcade_PlayScene2D_Renderer(GameScene gameScene, ActorSpriteAnimController animSystem, Canvas canvas, SpriteSheet spriteSheet) {
        super(canvas);
        requireNonNull(gameScene);
        this.spriteSheet = requireNonNull(spriteSheet);

        final CanvasRenderingComp r2D = gameScene.reqComp(CanvasRenderingComp.class);

        final GameVariantRenderConfig renderConfig = gameScene.app().gameVariants().currentGameVariant().uiConfig().renderConfig();
        final ActorSpriteAnimController animController = gameScene.game().variant().systems().actorSpriteAnimController();

        levelRenderer = r2D.configureRenderer(renderConfig.createGameLevelRenderer(animSystem, canvas));
        actorRenderer = r2D.configureRenderer(renderConfig.createActorRenderer(animSystem, canvas));
        debugRenderer = r2D.configureRenderer(new BaseGameSceneDebugInfoRenderer(animController, canvas));
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
        final GameRules rules = game.variant().rules();
        final GameSession session = game.session();

        // Level creation happens by handling a game event after the play scene has been activated. Therefore,
        // the game level is not yet existing for the first two ticks after this scene got active.
        session.optLevel().ifPresent(level -> {
            final InfoMap info = createRenderInfo(level, playScene);
            levelRenderer.applyLevelSettings(rules, level, info);
            levelRenderer.drawLevel(game, level, info);

            entitiesInRenderingOrder(level.entities()).forEach(actor -> actorRenderer.render(actor, tick));

            if (playScene.viewModel().debugModeOnProperty().get()) {
                debugRenderer.render(playScene, tick);
            }
        });
    }

    private InfoMap createRenderInfo(GameLevel level, Arcade_PlayScene2D playScene2D) {
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

    private List<GameEntity> entitiesInRenderingOrder(GameLevelEntitySet entities) {
        return entities.all()
            .filter(e -> e.hasComp(RenderingComp.class))
            .sorted((e1, e2) -> RenderingComp.RENDERING_ORDER.compare(
                e1.reqComp(RenderingComp.class),
                e2.reqComp(RenderingComp.class)))
            .collect(Collectors.toCollection(ArrayList::new));
    }
}