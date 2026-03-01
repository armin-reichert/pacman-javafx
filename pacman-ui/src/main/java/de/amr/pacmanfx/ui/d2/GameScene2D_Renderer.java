/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d2;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;

public abstract class GameScene2D_Renderer extends BaseRenderer {

    protected BaseDebugInfoRenderer debugRenderer;

    public GameScene2D_Renderer(Canvas canvas) {
        super(canvas);
    }

    public abstract void draw(GameScene2D scene);

    protected void createDefaultDebugInfoRenderer(GameUI ui, GameScene2D scene, Canvas canvas) {
        debugRenderer = scene.adaptRenderer(new BaseDebugInfoRenderer(ui, canvas));
    }
}