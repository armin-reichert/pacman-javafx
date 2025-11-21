package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_CutScene1;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2DRenderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.canvas.Canvas;

import java.util.stream.Stream;

public class ArcadeMsPacMan_CutScene1_Renderer extends GameScene2DRenderer {

    private final ArcadeMsPacMan_ActorRenderer actorRenderer;

    public ArcadeMsPacMan_CutScene1_Renderer(GameScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(scene, canvas, spriteSheet);
        final GameUI_Config uiConfig = scene.ui().currentConfig();
        actorRenderer = configureRendererForGameScene(
            ((ArcadeMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas)), scene);
    }

    public void draw() {
        ArcadeMsPacMan_CutScene1 cutScene = (ArcadeMsPacMan_CutScene1) scene();
        cutScene.clapperboard.setFont(arcadeFont8());
        Stream.of(cutScene.clapperboard, cutScene.msPacMan, cutScene.pacMan, cutScene.inky, cutScene.pinky, cutScene.heart)
                .forEach(actorRenderer::drawActor);

    }
}
