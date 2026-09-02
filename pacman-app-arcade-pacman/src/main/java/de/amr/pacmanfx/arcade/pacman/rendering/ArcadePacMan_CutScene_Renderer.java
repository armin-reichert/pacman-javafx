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
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;

import static java.util.Objects.requireNonNull;

public class ArcadePacMan_CutScene_Renderer extends BaseRenderer implements GameScene2D_Renderer, SpriteRenderer {

    protected final ActorSpriteAnimController animSystem;
    protected final ActorRenderer actorRenderer;
    protected BaseDebugInfoRenderer debugRenderer;

    public ArcadePacMan_CutScene_Renderer(GameScene gameScene, ActorSpriteAnimController animSystem, Canvas canvas) {
        super(canvas);
        final CanvasRenderingComp r2D = gameScene.components().reqComp(CanvasRenderingComp.class);
        this.animSystem = requireNonNull(animSystem);
        final GameVariantRenderConfig renderConfig = gameScene.app().gameVariants().currentGameVariant().uiConfig().renderConfig();
        actorRenderer = r2D.configureRenderer(renderConfig.createActorRenderer(animSystem, canvas));
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(gameScene, canvas);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public void draw(GameScene scene, long tick) {
        switch (scene) {
            case ArcadePacMan_CutScene1 cutScene1 -> drawCutScene1(cutScene1);
            case ArcadePacMan_CutScene2 cutScene2 -> drawCutScene2(cutScene2);
            case ArcadePacMan_CutScene3 cutScene3 -> drawCutScene3(cutScene3);
            default -> throw new IllegalStateException("Unexpected value: " + scene);
        }
        if (scene.viewModel().debugModeOnProperty().get()) {
            debugRenderer.draw(scene, tick);
        }
    }

    private void drawCutScene1(ArcadePacMan_CutScene1 cutScene) {
        actorRenderer.drawActor(cutScene.blinky);
        actorRenderer.drawActor(cutScene.pacMan);
    }

    private void drawCutScene2(ArcadePacMan_CutScene2 cutScene) {
        drawSprite(cutScene.nailDressAnimation.sprite(), cutScene.nailX, cutScene.nailY, true);
        actorRenderer.drawActor(cutScene.pacMan);
        actorRenderer.drawActor(cutScene.blinky);
    }

    private void drawCutScene3(ArcadePacMan_CutScene3 cutScene) {
        actorRenderer.drawActor(cutScene.pacMan);
        actorRenderer.drawActor(cutScene.blinky);
    }
}