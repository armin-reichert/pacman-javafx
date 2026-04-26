/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package experiments;

import de.amr.objparser.ObjFileParser;
import de.amr.objparser.ObjModel;
import de.amr.pacmanfx.uilib.model3D.TriangleMeshBuilder;
import de.amr.pacmanfx.uilib.model3D.actor.GhostModel3D;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.*;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MeshViewerApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

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

        Group pivot = new Group();
        pivot.getTransforms().addAll(rotateX, rotateY);

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

        enableMouseControl(sub);

        // --- MENU BAR ---
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open OBJ…");
        MenuItem exitItem = new MenuItem("Exit");

        fileMenu.getItems().addAll(openItem, exitItem);
        menuBar.getMenus().add(fileMenu);

        openItem.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Open OBJ File");
            chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("OBJ Files", "*.obj")
            );

            File file = chooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    loadMeshFromFile(file, world);
                } catch (Exception x) {
                    Logger.error(x);
                }
            }
        });

        exitItem.setOnAction(e -> Platform.exit());

        // --- LAYOUT ---
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(sub);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Mesh Viewer");
        stage.setWidth(width);
        stage.setHeight(height);
        stage.show();

        Platform.runLater(() -> sub.requestFocus());
    }

    private void center(Node node) {
        Bounds b = node.getBoundsInLocal();
        node.getTransforms().add(new Translate(
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
                rotateY.setAngle(rotateY.getAngle() + dx * 0.5);
                rotateX.setAngle(rotateX.getAngle() - dy * 0.5);
            }

            mouseOldX = e.getSceneX();
            mouseOldY = e.getSceneY();
        });

        sub.setOnScroll(e -> zoom.setZ(zoom.getZ() + e.getDeltaY() * 0.1));

        sub.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case PLUS  -> zoom.setZ(zoom.getZ() + 2);
                case MINUS -> zoom.setZ(zoom.getZ() - 2);
            }
        });
    }

    private void loadMeshFromFile(File file, Group world) throws IOException {
        Map<String, MeshView> meshes = createMeshes(file);

        world.getChildren().clear();

        Group meshGroup = new Group();
        for (MeshView mv : meshes.values()) {
            mv.setCullFace(CullFace.NONE);
            mv.drawModeProperty().bind(drawMode);
            meshGroup.getChildren().add(mv);
        }
        center(meshGroup);

        Group pivot = new Group(meshGroup);
        pivot.getTransforms().addAll(rotateX, rotateY);
        world.getChildren().add(pivot);
    }

    private Map<String, MeshView> createMeshes(File objFile) throws IOException {
        URL objFileURL = objFile.toURI().toURL();
        var parser = new ObjFileParser(objFileURL, StandardCharsets.UTF_8);
        ObjModel objModel = parser.parse();
        var meshBuilder = new TriangleMeshBuilder(objModel);
        return meshBuilder.buildMeshViewsByGroup();
    }
}
