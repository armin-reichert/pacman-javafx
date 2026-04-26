/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package experiments;

import de.amr.pacmanfx.uilib.model3D.actor.GhostModel3D;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MeshViewerApp extends Application {

    private double mouseOldX, mouseOldY;

    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Translate zoom = new Translate(0, 0, -50);

    private final ObjectProperty<DrawMode> drawMode = new SimpleObjectProperty<>(DrawMode.FILL);

    private SubScene sub;

    @Override
    public void start(Stage stage) {

        double height = 0.8 * Screen.getPrimary().getBounds().getHeight();
        double width = 1.2 * height;

        // Load your mesh
        MeshView mesh = GhostModel3D.instance().dress();
        mesh.setCullFace(CullFace.NONE);
        mesh.drawModeProperty().bind(drawMode);

        // Center the mesh at the origin
        centerMesh(mesh);

        // Wrap mesh in a pivot group
        Group pivot = new Group(mesh);
        pivot.getTransforms().addAll(rotateX, rotateY);

        // World root
        Group world = new Group(pivot);

        // Camera
        PerspectiveCamera cam = new PerspectiveCamera(true);
        cam.getTransforms().add(zoom);
        cam.setNearClip(0.1);
        cam.setFarClip(10_000);

        sub = new SubScene(world, width, height, true, SceneAntialiasing.BALANCED);
        sub.setCamera(cam);
        sub.setFill(Color.gray(0.1));
        sub.widthProperty().bind(stage.widthProperty());
        sub.heightProperty().bind(stage.heightProperty());

        Scene scene = new Scene(new Group(sub));
        enableMouseControl(sub);

        stage.setScene(scene);
        stage.setTitle("Mesh Viewer");
        stage.setWidth(width);
        stage.setHeight(height);
        stage.show();

        Platform.runLater(() -> sub.requestFocus());
    }

    private void centerMesh(MeshView mesh) {
        Bounds b = mesh.getBoundsInLocal();
        mesh.getTransforms().add(new Translate(
            -b.getCenterX(),
            -b.getCenterY(),
            -b.getCenterZ()
        ));
    }

    private void enableMouseControl(SubScene sub) {

        sub.setOnKeyTyped(e -> {
            if ("w".equals(e.getCharacter())) {
                drawMode.set(drawMode.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
            }
        });

        sub.setOnMouseClicked(e -> sub.requestFocus());

        sub.setOnMousePressed(e -> {
            mouseOldX = e.getSceneX();
            mouseOldY = e.getSceneY();
        });


        sub.setOnMouseDragged(e -> {
            double dx = e.getSceneX() - mouseOldX;
            double dy = e.getSceneY() - mouseOldY;

            if (e.getButton() == MouseButton.PRIMARY) {
                rotateY.setAngle(rotateY.getAngle() + dx * 0.5); // horizontal orbit
                rotateX.setAngle(rotateX.getAngle() - dy * 0.5); // vertical tilt
            }

            mouseOldX = e.getSceneX();
            mouseOldY = e.getSceneY();
        });

        // Zoom
        sub.setOnScroll(e -> zoom.setZ(zoom.getZ() + e.getDeltaY() * 0.1));
        sub.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case PLUS  -> zoom.setZ(zoom.getZ() + 2);
                case MINUS -> zoom.setZ(zoom.getZ() - 2);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
