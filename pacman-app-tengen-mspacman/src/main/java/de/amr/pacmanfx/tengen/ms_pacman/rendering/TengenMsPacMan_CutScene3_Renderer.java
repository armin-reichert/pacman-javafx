package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_CutScene3;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.canvas.Canvas;

import java.util.stream.Stream;

public class TengenMsPacMan_CutScene3_Renderer extends GameScene2D_Renderer {

    private final TengenMsPacMan_ActorRenderer actorRenderer;

    public TengenMsPacMan_CutScene3_Renderer(GameScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(scene, canvas, spriteSheet);
        final GameUI_Config uiConfig = scene.ui().currentConfig();
        actorRenderer = GameScene2D_Renderer.configureRendererForGameScene(
            (TengenMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas), scene);
        createDefaultDebugInfoRenderer(canvas, uiConfig.spriteSheet());
    }

    public void draw() {
        clearCanvas();

        final TengenMsPacMan_CutScene3 cutScene = scene();
        cutScene.clapperboard.setFont(actorRenderer.arcadeFont8());
        if (!cutScene.darkness) {
            Stream.of(cutScene.clapperboard, cutScene.stork, cutScene.flyingBag, cutScene.msPacMan, cutScene.pacMan)
                .forEach(actorRenderer::drawActor);
        }

        if (scene.debugInfoVisible()) {
            debugInfoRenderer.draw();
        }
    }
}