package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_CutScene1;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.canvas.Canvas;

public class TengenMsPacMan_CutScene1_Renderer extends GameScene2D_Renderer {

    private final TengenMsPacMan_ActorRenderer actorRenderer;

    public TengenMsPacMan_CutScene1_Renderer(GameScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(canvas, spriteSheet);

        final GameUI_Config uiConfig = scene.ui().currentConfig();

        actorRenderer = configureRendererForGameScene(
            (TengenMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas), scene);

        createDefaultDebugInfoRenderer(scene, canvas, uiConfig.spriteSheet());
    }

    public void draw(GameScene2D scene) {
        clearCanvas();

        TengenMsPacMan_CutScene1 cutScene = (TengenMsPacMan_CutScene1) scene;
        cutScene.clapperboard().setFont(arcadeFont8());
        actorRenderer.drawActor(cutScene.clapperboard());
        actorRenderer.drawActor(cutScene.msPacMan());
        actorRenderer.drawActor(cutScene.pacMan());
        actorRenderer.drawActor(cutScene.inky());
        actorRenderer.drawActor(cutScene.pinky());
        actorRenderer.drawActor(cutScene.heart());

        if (scene.debugInfoVisible()) {
            debugInfoRenderer.draw(scene);
        }
    }
}