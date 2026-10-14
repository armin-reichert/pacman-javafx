/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.pacmanfx.uilib.view2d.RenderingSurface;
import javafx.scene.layout.StackPane;

public class PlainGameSceneContainer extends StackPane {

    private RenderingSurface renderingSurface;

    public PlainGameSceneContainer() {
        reset();
    }

    public void reset() {
        renderingSurface = new RenderingSurface();
        getChildren().setAll(renderingSurface.canvas());
    }

    public RenderingSurface renderingSurface() {
        return renderingSurface;
    }
}
