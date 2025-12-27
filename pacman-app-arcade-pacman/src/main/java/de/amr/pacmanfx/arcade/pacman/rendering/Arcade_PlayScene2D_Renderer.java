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
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui._2d.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.*;
import javafx.scene.canvas.Canvas;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static java.util.Objects.requireNonNull;

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

        final GameUI_Config uiConfig = scene.ui().currentConfig();
        levelRenderer = adaptRenderer(uiConfig.createGameLevelRenderer(canvas), scene);
        actorRenderer = adaptRenderer(uiConfig.createActorRenderer(canvas), scene);
        debugRenderer = adaptRenderer(new Arcade_PlayScene2D_DebugInfo_Renderer(canvas, scene), scene);
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
            levelRenderer.drawGameLevel(level, info);

            updateActorDrawingOrder(level);
            actorsInZOrder.forEach(actorRenderer::drawActor);

            if (scene.debugInfoVisible()) {
                debugRenderer.draw(scene);
            }
        });
    }

    private RenderInfo buildRenderInfo(GameLevel level, LevelCompletedAnimation levelCompletedAnimation) {
        final var info = new RenderInfo();
        final boolean bright = levelCompletedAnimation != null && levelCompletedAnimation.isHighlighted();
        final boolean flashing = levelCompletedAnimation != null && levelCompletedAnimation.isFlashing();
        info.put(CommonRenderInfoKey.ENERGIZER_BLINKING, level.blinking().state() == Pulse.State.ON);
        info.put(CommonRenderInfoKey.MAZE_EMPTY, level.worldMap().foodLayer().uneatenFoodCount() == 0);
        info.put(CommonRenderInfoKey.MAZE_BRIGHT, bright);
        info.put(CommonRenderInfoKey.MAZE_FLASHING, flashing);
        return info;
    }

    private void updateActorDrawingOrder(GameLevel gameLevel) {
        // Actor drawing order: (Bonus) < Pac-Man < Ghosts in order.
        // TODO: also take ghost state into account!
        actorsInZOrder.clear();
        gameLevel.optBonus().ifPresent(actorsInZOrder::add);
        actorsInZOrder.add(gameLevel.pac());
        GHOST_Z_ORDER.stream().map(gameLevel::ghost).forEach(actorsInZOrder::add);
    }
}