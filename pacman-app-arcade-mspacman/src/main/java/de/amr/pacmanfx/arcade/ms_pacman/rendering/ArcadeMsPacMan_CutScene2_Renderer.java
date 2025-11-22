package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_CutScene2;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2DRenderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.canvas.Canvas;

import java.util.stream.Stream;

public class ArcadeMsPacMan_CutScene2_Renderer extends GameScene2DRenderer {

    private final ArcadeMsPacMan_ActorRenderer actorRenderer;

    public ArcadeMsPacMan_CutScene2_Renderer(GameScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(scene, canvas, spriteSheet);
        final GameUI_Config uiConfig = scene.ui().currentConfig();
        actorRenderer = configureRendererForGameScene(
            (ArcadeMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas), scene);
        createDefaultDebugInfoRenderer(canvas, uiConfig.spriteSheet());
    }

    public void draw() {
        clearCanvas();
        ArcadeMsPacMan_CutScene2 cutScene = (ArcadeMsPacMan_CutScene2) scene();
        cutScene.clapperboard.setFont(arcadeFont8());
        Stream.of(cutScene.clapperboard, cutScene.msPacMan, cutScene.pacMan).forEach(actorRenderer::drawActor);
        if (cutScene.debugInfoVisible()) {
            debugInfoRenderer.draw();
        }
    }
}