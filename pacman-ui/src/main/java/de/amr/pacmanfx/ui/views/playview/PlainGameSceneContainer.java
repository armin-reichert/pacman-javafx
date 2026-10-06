package de.amr.pacmanfx.ui.views.playview;

import de.amr.pacmanfx.uilib.view2d.RenderingSurface;
import javafx.scene.layout.StackPane;

public class PlainGameSceneContainer extends StackPane {

    private final RenderingSurface renderingSurface;

    public PlainGameSceneContainer() {
        renderingSurface = new RenderingSurface();
        getChildren().add(renderingSurface.canvas());
    }

    public RenderingSurface renderingSurface() {
        return renderingSurface;
    }
}
