package de.amr.pacmanfx.ui.views.playview;

import de.amr.pacmanfx.uilib.view2d.RenderingSurface;
import javafx.scene.ParallelCamera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class SubSceneGameSceneContainer {

    private final StackPane root;
    private final SubScene subScene;
    private final ParallelCamera camera;

    private final RenderingSurface renderingSurface;

    public SubSceneGameSceneContainer() {
        renderingSurface = new RenderingSurface();

        root = new StackPane();

        subScene = new SubScene(root, 400, 600);
        subScene.setFill(Color.GRAY);

        camera = new ParallelCamera();
        subScene.setCamera(camera);

        renderingSurface.canvas().heightProperty().bind(subScene.heightProperty());
        renderingSurface.canvas().widthProperty().bind(subScene.widthProperty());

        root.getChildren().add(renderingSurface.canvas());
    }

    public StackPane root() {
        return root;
    }

    public SubScene subScene() {
        return subScene;
    }

    public ParallelCamera camera() {
        return camera;
    }

    public RenderingSurface renderingSurface() {
        return renderingSurface;
    }
}
