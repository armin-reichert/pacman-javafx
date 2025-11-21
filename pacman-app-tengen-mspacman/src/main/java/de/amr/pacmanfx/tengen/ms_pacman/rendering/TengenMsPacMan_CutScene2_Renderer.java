package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_CutScene2;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2DRenderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.canvas.Canvas;

import java.util.stream.Stream;

public class TengenMsPacMan_CutScene2_Renderer extends GameScene2DRenderer {

    private TengenMsPacMan_ActorRenderer actorRenderer;

    public TengenMsPacMan_CutScene2_Renderer(GameScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(scene, canvas, spriteSheet);

        final GameUI_Config uiConfig = scene.ui().currentConfig();
        actorRenderer = configureRendererForGameScene(
            (TengenMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas), scene);
    }

    public void draw() {
        final TengenMsPacMan_CutScene2 cutScene = (TengenMsPacMan_CutScene2) scene();
        cutScene.clapperboard.setFont(actorRenderer.arcadeFont8());
        Stream.of(cutScene.clapperboard, cutScene.msPacMan, cutScene.pacMan)
            .forEach(actorRenderer::drawActor);
    }
}
