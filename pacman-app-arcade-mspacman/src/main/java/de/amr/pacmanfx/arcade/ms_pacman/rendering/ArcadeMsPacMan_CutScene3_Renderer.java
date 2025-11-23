package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_CutScene3;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.canvas.Canvas;

import java.util.stream.Stream;

public class ArcadeMsPacMan_CutScene3_Renderer extends GameScene2D_Renderer {

    private final ArcadeMsPacMan_ActorRenderer actorRenderer;

    public ArcadeMsPacMan_CutScene3_Renderer(GameScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(canvas, spriteSheet);

        final GameUI_Config uiConfig = scene.ui().currentConfig();

        actorRenderer = configureRendererForGameScene(
            (ArcadeMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas), scene);

        createDefaultDebugInfoRenderer(scene, canvas, uiConfig.spriteSheet());
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();

        final ArcadeMsPacMan_CutScene3 cutScene = (ArcadeMsPacMan_CutScene3) scene;
        cutScene.clapperboard().setFont(arcadeFont8());
        Stream.of(cutScene.clapperboard(), cutScene.msPacMan(), cutScene.pacMan(), cutScene.stork(), cutScene.bag())
            .forEach(actorRenderer::drawActor);

        if (cutScene.debugInfoVisible()) {
            debugInfoRenderer.draw(scene);
        }
    }
}