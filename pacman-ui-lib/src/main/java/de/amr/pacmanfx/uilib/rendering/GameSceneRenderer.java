/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.rendering;

import javafx.scene.canvas.Canvas;

import java.util.Optional;

public class GameSceneRenderer extends BaseRenderer {

    private BaseRenderer debugInfoRenderer;

    public GameSceneRenderer(Canvas canvas) {
        super(canvas);
    }

    public Optional<BaseRenderer> optDebugInfoRenderer() {
        return Optional.ofNullable(debugInfoRenderer);
    }

    public void setDebugInfoRenderer(BaseRenderer debugInfoRenderer) {
        this.debugInfoRenderer = debugInfoRenderer;
    }
}
