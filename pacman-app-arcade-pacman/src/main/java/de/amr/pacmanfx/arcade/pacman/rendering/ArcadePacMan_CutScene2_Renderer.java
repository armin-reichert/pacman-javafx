/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_CutScene2;
import de.amr.pacmanfx.ui._2d.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.TS;

public class ArcadePacMan_CutScene2_Renderer extends ArcadePacMan_CutScene_Renderer {

    public ArcadePacMan_CutScene2_Renderer(UIPreferences prefs, GameScene2D scene, Canvas canvas) {
        super(scene, canvas);
        debugRenderer = adaptRenderer(new BaseDebugInfoRenderer(prefs, canvas) {
            @Override
            public void draw(GameScene2D scene) {
                super.draw(scene);
                if (scene instanceof ArcadePacMan_CutScene2 cutScene2) {
                    String text = cutScene2.tick() < ArcadePacMan_CutScene2.ANIMATION_START_TICK
                        ? String.format("Wait %d", ArcadePacMan_CutScene2.ANIMATION_START_TICK - cutScene2.tick())
                        : String.format("Frame %d", cutScene2.tick());
                    fillText(text, debugTextFill, debugTextFont, TS(1), TS(5));
                }
            }
        }, scene);
    }

    @Override
    protected void drawSceneContent(GameScene2D scene) {
        final ArcadePacMan_CutScene2 cutScene = (ArcadePacMan_CutScene2) scene;
        drawSprite(cutScene.nailDressRaptureAnimation().currentSprite(), TS(14), TS(19) + 3, true);
        actorRenderer.drawActor(cutScene.pac());
        actorRenderer.drawActor(cutScene.blinky());
    }
}