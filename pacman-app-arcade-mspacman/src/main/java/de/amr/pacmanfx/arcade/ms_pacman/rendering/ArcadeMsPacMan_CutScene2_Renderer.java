package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_CutScene2;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import javafx.scene.canvas.Canvas;

import java.util.stream.Stream;

public class ArcadeMsPacMan_CutScene2_Renderer extends GameScene2D_Renderer {

    private final ArcadeMsPacMan_ActorRenderer actorRenderer;

    public ArcadeMsPacMan_CutScene2_Renderer(GameScene2D scene, Canvas canvas) {
        super(canvas);

        final GameUI_Config uiConfig = scene.ui().currentConfig();

        actorRenderer = configureRendererForGameScene(
            (ArcadeMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas), scene);

        createDefaultDebugInfoRenderer(scene, canvas);
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();

        final ArcadeMsPacMan_CutScene2 cutScene = (ArcadeMsPacMan_CutScene2) scene;
        cutScene.clapperboard().setFont(arcadeFont8());
        Stream.of(cutScene.clapperboard(), cutScene.msPacMan(), cutScene.pacMan()).forEach(actorRenderer::drawActor);

        if (cutScene.debugInfoVisible()) {
            debugInfoRenderer.draw(scene);
        }
    }
}