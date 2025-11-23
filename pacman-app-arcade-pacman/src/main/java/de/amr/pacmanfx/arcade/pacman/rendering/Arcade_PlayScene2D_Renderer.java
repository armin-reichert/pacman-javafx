/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.CommonRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import javafx.scene.canvas.Canvas;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;

public class Arcade_PlayScene2D_Renderer extends GameScene2D_Renderer {

    private final GameLevelRenderer gameLevelRenderer;
    private final ActorRenderer actorRenderer;
    private final List<Actor> actorsInZOrder = new ArrayList<>();

    public Arcade_PlayScene2D_Renderer(Arcade_PlayScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(canvas, spriteSheet);

        final GameUI_Config uiConfig = scene.ui().currentConfig();

        gameLevelRenderer = configureRendererForGameScene(uiConfig.createGameLevelRenderer(canvas), scene);

        actorRenderer = configureRendererForGameScene(uiConfig.createActorRenderer(canvas), scene);

        debugInfoRenderer = configureRendererForGameScene(
            new Arcade_PlayScene2D_DebugInfo_Renderer(scene, canvas, uiConfig.spriteSheet()), scene);
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();

        final Arcade_PlayScene2D playScene = (Arcade_PlayScene2D) scene;

        if (playScene.context().optGameLevel().isEmpty()) {
            return; // Scene is drawn already 2 ticks before level has been created
        }

        final GameLevel gameLevel = playScene.context().gameLevel();
        RenderInfo info = new RenderInfo();
        info.put(CommonRenderInfoKey.MAZE_BRIGHT, isMazeHighlighted(playScene));
        info.put(CommonRenderInfoKey.ENERGIZER_BLINKING, gameLevel.blinking().state() == Pulse.State.ON);
        info.put(CommonRenderInfoKey.MAZE_EMPTY, playScene.context().gameLevel().worldMap().foodLayer().uneatenFoodCount() == 0);
        gameLevelRenderer.applyLevelSettings(gameLevel, info);
        gameLevelRenderer.drawGameLevel(gameLevel, info);

        updateActorDrawingOrder(gameLevel);
        actorsInZOrder.forEach(actorRenderer::drawActor);

        if (playScene.debugInfoVisible()) {
            debugInfoRenderer.draw(scene);
        }
    }

    private boolean isMazeHighlighted(Arcade_PlayScene2D playScene) {
        return playScene.levelCompletedAnimation() != null
            && playScene.levelCompletedAnimation().isRunning()
            && playScene.levelCompletedAnimation().highlightedProperty().get();
    }

    private void updateActorDrawingOrder(GameLevel gameLevel) {
        // Actor drawing order: (Bonus) < Pac-Man < Ghosts in order.
        // TODO: also take ghost state into account!
        actorsInZOrder.clear();
        gameLevel.bonus().ifPresent(actorsInZOrder::add);
        actorsInZOrder.add(gameLevel.pac());
        Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW)
            .map(gameLevel::ghost)
            .forEach(actorsInZOrder::add);
    }
}