package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_CutScene1;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2DRenderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.canvas.Canvas;

import java.util.stream.Stream;

public class TengenMsPacMan_CutScene1_Renderer extends GameScene2DRenderer {

    private final TengenMsPacMan_ActorRenderer actorSpriteRenderer;

    public TengenMsPacMan_CutScene1_Renderer(GameScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(scene, canvas, spriteSheet);
        final GameUI_Config uiConfig = scene.ui().currentConfig();
        actorSpriteRenderer = configureRendererForGameScene(
            (TengenMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas), scene);
        createDefaultDebugInfoRenderer(canvas, uiConfig.spriteSheet());
    }

    public void draw() {
        TengenMsPacMan_CutScene1 cutScene = scene();
        cutScene.clapperboard.setFont(actorSpriteRenderer.arcadeFont8());
        Stream.of(cutScene.clapperboard, cutScene.msPacMan, cutScene.pacMan, cutScene.inky, cutScene.pinky, cutScene.heart)
            .forEach(actorSpriteRenderer::drawActor);

        if (scene.debugInfoVisible()) {
            debugInfoRenderer.draw();
        }
    }
}