package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_BootScene;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2DRenderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesColor;

public class TengenMsPacMan_BootScene_Renderer extends GameScene2DRenderer {

    public static final String TENGEN_PRESENTS = "TENGEN PRESENTS";

    private final TengenMsPacMan_ActorRenderer actorRenderer;

    public TengenMsPacMan_BootScene_Renderer(GameScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(scene, canvas, spriteSheet);

        final GameUI_Config uiConfig = scene.ui().currentConfig();

        actorRenderer = GameScene2DRenderer.configureRendererForGameScene(
            (TengenMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas), scene);

        createDefaultDebugInfoRenderer(canvas, uiConfig.spriteSheet());
    }

    public void draw() {
        final TengenMsPacMan_BootScene bootScene = scene();
        if (bootScene.gray) {
            actorRenderer.fillCanvas(nesColor(0x10));
        } else {
            actorRenderer.fillText(TENGEN_PRESENTS, bootScene.shadeOfBlue, actorRenderer.arcadeFont8(),
                bootScene.movingText.x(), bootScene.movingText.y());
            actorRenderer.drawActor(bootScene.ghost);
        }
        if (scene().debugInfoVisible()) {
            debugInfoRenderer.draw();
        }
    }
}