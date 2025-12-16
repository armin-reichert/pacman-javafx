package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_BootScene;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesColor;

public class TengenMsPacMan_BootScene_Renderer extends GameScene2D_Renderer {

    public static final String TENGEN_PRESENTS = "TENGEN PRESENTS";

    private final TengenMsPacMan_ActorRenderer actorRenderer;

    public TengenMsPacMan_BootScene_Renderer(GameScene2D scene, Canvas canvas) {
        super(canvas);

        final GameUI_Config uiConfig = scene.ui().currentConfig();

        actorRenderer = GameScene2D_Renderer.adaptRenderer(
            (TengenMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas), scene);

        createDefaultDebugInfoRenderer(scene, canvas);
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();

        final TengenMsPacMan_BootScene bootScene = (TengenMsPacMan_BootScene) scene;
        if (bootScene.gray) {
            actorRenderer.fillCanvas(nesColor(0x10));
        } else {
            actorRenderer.fillText(TENGEN_PRESENTS, bootScene.shadeOfBlue, actorRenderer.arcadeFont8(),
                bootScene.movingText.x(), bootScene.movingText.y());
            actorRenderer.drawActor(bootScene.ghost);
        }

        if (scene.debugInfoVisible()) {
            debugRenderer.draw(scene);
        }
    }
}