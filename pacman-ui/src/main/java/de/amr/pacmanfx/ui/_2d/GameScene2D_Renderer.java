/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.Canvas;

public abstract class GameScene2D_Renderer extends BaseRenderer {

    public static <T extends Renderer> T adaptRenderer(T renderer, GameScene2D scene) {
        renderer.backgroundProperty().bind(scene.backgroundProperty());
        renderer.scalingProperty().bind(scene.scalingProperty());
        return renderer;
    }

    protected BaseDebugInfoRenderer debugRenderer;

    public GameScene2D_Renderer(Canvas canvas) {
        super(canvas);
    }

    public abstract void draw(GameScene2D scene);

    protected void createDefaultDebugInfoRenderer(GameScene2D scene, Canvas canvas) {
        debugRenderer = adaptRenderer(new BaseDebugInfoRenderer(scene.ui(), canvas), scene);
    }
}