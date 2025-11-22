package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_PlayScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.CommonRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import javafx.scene.canvas.Canvas;

import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_PlayScene2D.CONTENT_INDENT;

public class TengenMsPacMan_PlayScene2D_Renderer extends TengenMsPacMan_CommonSceneRenderer {

    private final RenderInfo gameLevelRenderInfo = new RenderInfo();
    private final TengenMsPacMan_GameLevelRenderer gameLevelRenderer;
    private final TengenMsPacMan_ActorRenderer actorRenderer;

    public TengenMsPacMan_PlayScene2D_Renderer(GameScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(scene, canvas, spriteSheet);

        final TengenMsPacMan_UIConfig uiConfig = scene.ui().currentConfig();

        gameLevelRenderer = configureRendererForGameScene(
            uiConfig.createGameLevelRenderer(canvas), scene);

        actorRenderer = configureRendererForGameScene(
            uiConfig.createActorRenderer(canvas), scene);
    }

    public void draw() {
        clearCanvas();
        drawGameLevel(scene().context().gameLevel());
    }

    private void drawGameLevel(GameLevel gameLevel) {
        final TengenMsPacMan_PlayScene2D playScene = scene();
        final long tick = playScene.ui().clock().tickCount();

        gameLevelRenderInfo.clear();
        // this is needed for drawing animated maze with different images:
        gameLevelRenderInfo.put(CommonRenderInfoKey.TICK, tick);
        gameLevelRenderInfo.put(TengenMsPacMan_UIConfig.CONFIG_KEY_MAP_CATEGORY,
            gameLevel.worldMap().getConfigValue(TengenMsPacMan_UIConfig.CONFIG_KEY_MAP_CATEGORY));
        if (playScene.levelCompletedAnimation != null && playScene.mazeHighlighted.get()) {
            gameLevelRenderInfo.put(CommonRenderInfoKey.MAZE_BRIGHT, true);
            gameLevelRenderInfo.put(CommonRenderInfoKey.MAZE_FLASHING_INDEX, playScene.levelCompletedAnimation.flashingIndex());
        } else {
            gameLevelRenderInfo.put(CommonRenderInfoKey.MAZE_BRIGHT, false);
        }
        ctx.save();
        ctx.translate(scaled(CONTENT_INDENT), 0);
        gameLevelRenderer.drawGameLevel(gameLevel, gameLevelRenderInfo);

        actorsInZOrder.clear();
        gameLevel.bonus().ifPresent(actorsInZOrder::add);
        actorsInZOrder.add(gameLevel.pac());
        ghostsInZOrder(gameLevel).forEach(actorsInZOrder::add);
        actorsInZOrder.forEach(actorRenderer::drawActor);

        gameLevelRenderer.drawDoor(gameLevel.worldMap()); // ghosts appear under door when accessing house!
        ctx.restore();
    }

    private Stream<Ghost> ghostsInZOrder(GameLevel gameLevel) {
        return Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW).map(gameLevel::ghost);
    }
}