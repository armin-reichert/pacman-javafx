/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.lib.Pulse;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui._2d.LevelCompletedAnimation;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.*;
import javafx.scene.canvas.Canvas;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static java.util.Objects.requireNonNull;

/**
 * Renders the 2D play scene for the Arcade Pac-Man games. The XXL games use a generic map renderer that does not need
 * any graphics.
 */
public class Arcade_PlayScene2D_Renderer extends GameScene2D_Renderer implements SpriteRenderer {

    private static final List<Byte> GHOST_Z_ORDER = List.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW);

    private final SpriteSheet<?> spriteSheet;
    private final GameLevelRenderer levelRenderer;
    private final ActorRenderer actorRenderer;
    private final List<Actor> actorsInZOrder = new ArrayList<>();

    public Arcade_PlayScene2D_Renderer(GameScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(canvas);
        requireNonNull(scene);
        this.spriteSheet = requireNonNull(spriteSheet);

        final UIConfig uiConfig = scene.ui().currentConfig();
        levelRenderer = scene.adaptRenderer(uiConfig.createGameLevelRenderer(canvas));
        actorRenderer = scene.adaptRenderer(uiConfig.createActorRenderer(canvas));
        debugRenderer = scene.adaptRenderer(new Arcade_PlayScene2D_DebugInfo_Renderer(scene.ui(), canvas));
    }

    @Override
    public SpriteSheet<?> spriteSheet() {
        return spriteSheet;
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();
        if (!(scene instanceof Arcade_PlayScene2D playScene)) {
            return;
        }
        final Game game = scene.gameContext().currentGame();
        // Level creation happens by handling a game event after the play scene has been activated. Therefore,
        // the game level is not yet existing for the first two ticks after this scene got active.
        game.optGameLevel().ifPresent(level -> {
            final RenderInfo info = createRenderInfo(level, playScene);
            levelRenderer.applyLevelSettings(level, info);
            levelRenderer.drawLevel(level, info);
            updateActorZOrder(level);
            actorsInZOrder.forEach(actorRenderer::drawActor);
            if (scene.debugInfoVisible()) {
                debugRenderer.draw(scene);
            }
        });
    }

    private RenderInfo createRenderInfo(GameLevel level, Arcade_PlayScene2D playScene2D) {
        final var info = new RenderInfo();
        final boolean energizerVisible = level.blinking().state() == Pulse.State.ON;
        final boolean mapIsEmpty = level.worldMap().foodLayer().uneatenFoodCount() == 0;
        info.put(CommonRenderInfoKey.ENERGIZER_VISIBLE, energizerVisible);
        info.put(CommonRenderInfoKey.MAP_EMPTY, mapIsEmpty);
        info.put(CommonRenderInfoKey.MAP_BRIGHT, false);
        info.put(CommonRenderInfoKey.MAP_FLASHING, false);
        playScene2D.optLevelCompletedAnimation().flatMap(LevelCompletedAnimation::flashingState).ifPresent(flashingState -> {
            info.put(CommonRenderInfoKey.MAP_BRIGHT,   flashingState.isHighlighted());
            info.put(CommonRenderInfoKey.MAP_FLASHING, flashingState.isFlashing());

        });
        return info;
    }

    // Actor z-order: Bonus under Pac-Man under ghosts in z-order.
    private void updateActorZOrder(GameLevel gameLevel) {
        actorsInZOrder.clear();
        gameLevel.optBonus().ifPresent(actorsInZOrder::add);
        actorsInZOrder.add(gameLevel.pac());
        GHOST_Z_ORDER.stream().map(gameLevel::ghost).forEach(actorsInZOrder::add);
    }
}