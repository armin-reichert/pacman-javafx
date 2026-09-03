/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_CutScene1;
import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_CutScene2;
import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_CutScene3;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_CutScene_Renderer extends BaseRenderer implements SpriteRenderer {

    protected final ActorSpriteAnimController animSystem;
    protected final BaseRenderer actorRenderer;
    protected BaseGameSceneDebugInfoRenderer debugRenderer;

    public ArcadePacMan_CutScene_Renderer(GameScene gameScene, ActorSpriteAnimController animSystem, Canvas canvas) {
        super(canvas);
        final CanvasRenderingComp r2D = gameScene.reqComp(CanvasRenderingComp.class);
        this.animSystem = requireNonNull(animSystem);
        final GameVariantRenderConfig renderConfig = gameScene.app().gameVariants().currentGameVariant().uiConfig().renderConfig();
        actorRenderer = r2D.configureRenderer(renderConfig.createActorRenderer(animSystem, canvas));
        debugRenderer = createDefaultSceneDebugRenderer(gameScene, canvas);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public void render(Object r, long tick) {
        if (!(r instanceof GameScene gameScene)) {
            return;
        }
        switch (gameScene) {
            case ArcadePacMan_CutScene1 cutScene1 -> drawCutScene1(cutScene1, tick);
            case ArcadePacMan_CutScene2 cutScene2 -> drawCutScene2(cutScene2, tick);
            case ArcadePacMan_CutScene3 cutScene3 -> drawCutScene3(cutScene3, tick);
            default -> throw new IllegalStateException("Unexpected value: " + gameScene);
        }
        if (gameScene.viewModel().debugModeOnProperty().get()) {
            debugRenderer.render(gameScene, tick);
        }
    }

    private void drawCutScene1(ArcadePacMan_CutScene1 cutScene, long tick) {
        actorRenderer.render(cutScene.blinky, tick);
        actorRenderer.render(cutScene.pacMan, tick);
    }

    private void drawCutScene2(ArcadePacMan_CutScene2 cutScene, long tick) {
        drawSprite(cutScene.nailDressAnimation.sprite(), cutScene.nailX, cutScene.nailY, true);
        actorRenderer.render(cutScene.pacMan, tick);
        actorRenderer.render(cutScene.blinky, tick);
    }

    private void drawCutScene3(ArcadePacMan_CutScene3 cutScene, long tick) {
        actorRenderer.render(cutScene.pacMan, tick);
        actorRenderer.render(cutScene.blinky, tick);
    }
}