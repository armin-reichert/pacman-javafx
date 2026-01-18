/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.lib.Pulse;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.ui.GameUI_Config;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui._2d.LevelCompletedAnimation;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
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

    public Arcade_PlayScene2D_Renderer(PreferencesManager prefs, GameScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(canvas);
        requireNonNull(prefs);
        requireNonNull(scene);
        this.spriteSheet = requireNonNull(spriteSheet);

        final GameUI_Config uiConfig = scene.ui().currentConfig();
        levelRenderer = adaptRenderer(uiConfig.createGameLevelRenderer(canvas), scene);
        actorRenderer = adaptRenderer(uiConfig.createActorRenderer(canvas), scene);
        debugRenderer = adaptRenderer(new Arcade_PlayScene2D_DebugInfo_Renderer(prefs, canvas), scene);
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
        final Game game = scene.context().currentGame();
        // Level creation happens by handling a game event after the play scene has been activated. Therefore,
        // the game level is not yet existing for the first two ticks after this scene got active.
        game.optGameLevel().ifPresent(level -> {
            final RenderInfo info = buildRenderInfo(level, playScene.levelCompletedAnimation().orElse(null));

            levelRenderer.applyLevelSettings(level, info);
            levelRenderer.drawLevel(level, info);

            updateActorZOrder(level);
            actorsInZOrder.forEach(actorRenderer::drawActor);

            if (scene.debugInfoVisible()) {
                debugRenderer.draw(scene);
            }
        });
    }

    private RenderInfo buildRenderInfo(GameLevel level, LevelCompletedAnimation levelCompletedAnimation) {
        final boolean bright = levelCompletedAnimation != null && levelCompletedAnimation.isHighlighted();
        final boolean flashing = levelCompletedAnimation != null && levelCompletedAnimation.isFlashing();
        final boolean energizerOn = level.blinking().state() == Pulse.State.ON;
        final boolean allPelletsEaten = level.worldMap().foodLayer().uneatenFoodCount() == 0;
        final var info = new RenderInfo();
        info.put(CommonRenderInfoKey.ENERGIZER_ON, energizerOn);
        info.put(CommonRenderInfoKey.MAP_EMPTY, allPelletsEaten);
        info.put(CommonRenderInfoKey.MAP_BRIGHT, bright);
        info.put(CommonRenderInfoKey.MAP_FLASHING, flashing);
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