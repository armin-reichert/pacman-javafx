/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.InfoMap;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.GameEntity;
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

import static java.util.Objects.requireNonNull;

/**
 * Renders the 2D play scene for the Arcade Pac-Man games. The XXL games use a generic map renderer that does not need
 * any graphics.
 */
public class Arcade_PlayScene2D_Renderer extends BaseRenderer implements GameScene2D_Renderer, SpriteRenderer {

    public static final List<GhostPersonality> GHOST_Z_ORDER = List.of(
        GhostPersonality.ORANGE_GHOST_POKEY,
        GhostPersonality.CYAN_GHOST_BASHFUL,
        GhostPersonality.PINK_GHOST_SPEEDY,
        GhostPersonality.RED_GHOST_SHADOW);

    private final SpriteSheet spriteSheet;
    private final GameLevelRenderer levelRenderer;
    private final BaseRenderer actorRenderer;
    private final BaseDebugInfoRenderer debugRenderer;
    private final List<GameEntity> actorsInZOrder = new ArrayList<>();

    public Arcade_PlayScene2D_Renderer(GameScene gameScene, ActorSpriteAnimController animSystem, Canvas canvas, SpriteSheet spriteSheet) {
        super(canvas);
        requireNonNull(gameScene);
        this.spriteSheet = requireNonNull(spriteSheet);

        final CanvasRenderingComp r2D = gameScene.reqComp(CanvasRenderingComp.class);

        final GameVariantRenderConfig renderConfig = gameScene.app().gameVariants().currentGameVariant().uiConfig().renderConfig();
        final ActorSpriteAnimController animController = gameScene.game().variant().systems().actorSpriteAnimController();

        levelRenderer = r2D.configureRenderer(renderConfig.createGameLevelRenderer(animSystem, canvas));
        actorRenderer = r2D.configureRenderer(renderConfig.createActorRenderer(animSystem, canvas));
        debugRenderer = r2D.configureRenderer(new BaseDebugInfoRenderer(animController, canvas));
    }

    @Override
    public SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public void draw(GameScene scene, long tick) {
        if (!(scene instanceof Arcade_PlayScene2D playScene)) {
            return;
        }

        final GameRules rules = scene.game().variant().rules();
        final GameSession session = scene.game().session();

        // Level creation happens by handling a game event after the play scene has been activated. Therefore,
        // the game level is not yet existing for the first two ticks after this scene got active.
        session.optLevel().ifPresent(level -> {
            final InfoMap info = createRenderInfo(level, playScene);
            levelRenderer.applyLevelSettings(rules, level, info);
            levelRenderer.drawLevel(scene.game(), level, info);
            updateActorZOrder(level.entities());
            actorsInZOrder.forEach(actorRenderer::render);
            if (scene.viewModel().debugModeOnProperty().get()) {
                debugRenderer.draw(scene, tick);
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

    // Actor z-order: Bonus under Pac-Man under ghosts in z-order.
    private void updateActorZOrder(GameLevelEntitySet entities) {
        actorsInZOrder.clear();
        entities.optBonus().ifPresent(actorsInZOrder::add);
        actorsInZOrder.addAll(entities.theGhostPoints());
        actorsInZOrder.addAll(entities.theBonusPoints());
        actorsInZOrder.add(entities.pac());
        GHOST_Z_ORDER.stream().map(entities::ghost).forEach(actorsInZOrder::add);
    }
}