/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;

public abstract class GameScene2D_Renderer extends BaseRenderer {

    protected BaseDebugInfoRenderer debugRenderer;

    public GameScene2D_Renderer(Canvas canvas) {
        super(canvas);
    }

    public abstract void draw(GameScene2D scene);

    protected void createDefaultDebugInfoRenderer(PreferencesManager prefs, GameScene2D scene, Canvas canvas) {
        debugRenderer = scene.adaptRenderer(new BaseDebugInfoRenderer(prefs, canvas));
    }
}