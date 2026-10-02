package de.amr.pacmanfx.ui.views.playview;

import de.amr.pacmanfx.uilib.view2d.RenderingSurface;
import javafx.scene.SubScene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class SubSceneGameSceneContainer {

    private final StackPane root;
    private final SubScene subScene;
    private final RenderingSurface renderingSurface;

    public SubSceneGameSceneContainer() {
        renderingSurface = new RenderingSurface();

        root = new StackPane();

        subScene = new SubScene(root, 400, 600);
        subScene.setFill(Color.GRAY);

        renderingSurface.canvas().heightProperty().bind(subScene.heightProperty());
        renderingSurface.canvas().widthProperty().bind(subScene.widthProperty());

        renderingSurface.scalingProperty().bind(subScene.widthProperty().divide(32.0 * 8));

        root.getChildren().add(renderingSurface.canvas());
    }

    public StackPane root() {
        return root;
    }

    public SubScene subScene() {
        return subScene;
    }

    public RenderingSurface renderingSurface() {
        return renderingSurface;
    }
}
