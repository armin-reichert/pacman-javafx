/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package experiments.meshviewer;

import de.amr.objparser.ObjFileParser;
import de.amr.objparser.ObjModel;
import de.amr.pacmanfx.uilib.model3D.MeshBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
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

    public static final int DEFAULT_ANGLE_X = 0;
    public static final int DEFAULT_ANGLE_Y = 0;
    public static final int DEFAULT_ZOOM = -50;

    public static void main(String[] args) {
        launch(args);
    }

    public static sealed class NavigationTreeNode permits LabelNode, MeshNode { }

    public static final class LabelNode extends NavigationTreeNode {
        public final String label;

        public LabelNode(String label) {
            this.label = label;
        }
    }

    public static final class MeshNode extends NavigationTreeNode {
        public final String meshName;
        public final MeshView meshView;

        public MeshNode(String meshName, MeshView meshView) {
            this.meshName = meshName;
            this.meshView = meshView;
        }
    }


    private double mouseOldX, mouseOldY;

    private final Rotate rotateX = new Rotate(DEFAULT_ANGLE_X, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(DEFAULT_ANGLE_Y, Rotate.Y_AXIS);
    private final Translate zoom = new Translate(0, 0, DEFAULT_ZOOM);

    private final ObjectProperty<DrawMode> drawMode = new SimpleObjectProperty<>(DrawMode.LINE);

    private Group world;
    private SubScene sub;
    private Pane navigationPane;

    private TreeView<NavigationTreeNode> treeView;

    // Data model
    private final ObjectProperty<ObjModel> objModel = new SimpleObjectProperty<>();

    @Override
    public void start(Stage stage) {

        double height = 0.8 * Screen.getPrimary().getBounds().getHeight();
        double width = 1.2 * height;

        world = new Group();

        // Camera
        PerspectiveCamera cam = new PerspectiveCamera(true);
        cam.getTransforms().add(zoom);
        cam.setNearClip(0.1);
        cam.setFarClip(10_000);

        sub = new SubScene(world, width, height, true, SceneAntialiasing.BALANCED);
        sub.setCamera(cam);
        sub.setFill(Color.gray(0.5));

        enableMouseControl(sub);

        // --- MENU BAR ---
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open OBJ…");
        MenuItem exitItem = new MenuItem("Exit");

        fileMenu.getItems().addAll(openItem, exitItem);
        menuBar.getMenus().add(fileMenu);

        openItem.setOnAction(_ -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Open OBJ File");
            chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("OBJ Files", "*.obj")
            );

            File objFile = chooser.showOpenDialog(stage);
            if (objFile != null) {
                try {
                    loadMeshesFromFile(objFile);
                } catch (Exception x) {
                    Logger.error(x);
                }
            }
        });

        exitItem.setOnAction(_ -> Platform.exit());

        // --- LAYOUT ---
        treeView = createObjModelTreeView(new ObjModel(), "OBJ");
        navigationPane = new VBox();
        navigationPane.setMinWidth(300);
        navigationPane.setMaxWidth(300);
        navigationPane.setBorder(Border.stroke(Color.RED));
        navigationPane.getChildren().add(treeView);

        BorderPane rootPane = new BorderPane();
        rootPane.setTop(menuBar);
        rootPane.setCenter(sub);
        rootPane.setLeft(navigationPane);

        sub.widthProperty().bind(rootPane.widthProperty().subtract(navigationPane.widthProperty()));
        sub.heightProperty().bind(rootPane.heightProperty());

        Scene scene = new Scene(rootPane);
        stage.setScene(scene);
        stage.setTitle("Mesh Viewer");
        stage.setWidth(width);
        stage.setHeight(height);
        stage.show();

        objModel.addListener((_, _, newModel) -> {
            onObjModelChanged(newModel);
        });

        try {
            loadMeshesFromURL(getClass().getResource("/newell_teaset/teapot.obj"));
            TreeItem<NavigationTreeNode> rootItem = treeView.getRoot();
            if (rootItem.getChildren().size() == 3) {
                TreeItem<NavigationTreeNode> objectsItem = rootItem.getChildren().getFirst();
                if (!objectsItem.getChildren().isEmpty()) {
                    TreeItem<NavigationTreeNode> firstObjectItem = objectsItem.getChildren().getFirst();
                    treeView.getSelectionModel().select(firstObjectItem);
                }
            }
        } catch (Exception x) {
            Logger.error(x, "Could not load teapot.obj");
        }
    }

    private TreeView<NavigationTreeNode> createObjModelTreeView(ObjModel objModel, String title) {
        final MeshBuilder meshBuilder = new MeshBuilder(objModel);

        final TreeItem<NavigationTreeNode> root = new TreeItem<>(new LabelNode(title));
        root.setExpanded(true);

        root.getChildren().add(createSubTree(meshBuilder.buildMeshViewsByObject(), "Mesh Views by Object"));
        root.getChildren().add(createSubTree(meshBuilder.buildMeshViewsByGroup(), "Mesh Views by Group"));
        root.getChildren().add(createSubTree(meshBuilder.buildMeshViewsByMaterial(), "Mesh Views by Material"));

        final TreeView<NavigationTreeNode> treeView = new TreeView<>(root);
        treeView.setShowRoot(true);
        VBox.setVgrow(treeView, Priority.ALWAYS);

        treeView.getSelectionModel().selectedItemProperty().addListener((_, _, selectedNode) -> {
            switch (selectedNode.getValue()) {
                case LabelNode labelNode -> Logger.info("Selected node has label '{}'", labelNode.label);
                case MeshNode meshNode -> {
                    String meshName = meshNode.meshName;
                    MeshView meshView = meshNode.meshView;
                    Logger.info("Selected node has mesh '{}': {}", meshName, meshView);
                    setDisplayedMeshView(meshView);
                }
                default -> Logger.info("Selected node has value {}", selectedNode.getValue());
            }
        });

        treeView.setCellFactory(_ -> new TreeCell<>() {
            @Override
            protected void updateItem(NavigationTreeNode value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                    return;
                }

                setText(switch (value) {
                    case LabelNode labelNode -> labelNode.label;
                    case MeshNode meshNode -> meshNode.meshName;
                    default -> "?";
                });
            }
        });


        return treeView;
    }

    private TreeItem<NavigationTreeNode> createSubTree(Map<String, MeshView> meshViews, String title) {
        if (meshViews.isEmpty()) title += " (None)";
        final var root = new TreeItem<NavigationTreeNode>(new LabelNode(title));
        root.setExpanded(true);
        for (String meshName : meshViews.keySet()) {
            final var item = new TreeItem<NavigationTreeNode>(new MeshNode(meshName, meshViews.get(meshName)));
            root.getChildren().add(item);
        }
        return root;
    }

    private void center(Node node) {
        Bounds b = node.getBoundsInLocal();
        node.getTransforms().add(new Translate(
            -b.getCenterX(),
            -b.getCenterY(),
            -b.getCenterZ()
        ));
    }

    private void resetView() {
        rotateX.setAngle(DEFAULT_ANGLE_X);
        rotateY.setAngle(DEFAULT_ANGLE_Y);
        zoom.setZ(DEFAULT_ZOOM);
    }

    private void enableMouseControl(SubScene sub) {

        sub.setOnKeyTyped(e -> {
            if ("w".equals(e.getCharacter())) {
                drawMode.set(drawMode.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
            }
        });

        sub.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                resetView();
            }
            sub.requestFocus();
        });

        sub.setOnMousePressed(e -> {
            mouseOldX = e.getSceneX();
            mouseOldY = e.getSceneY();
        });

        sub.setOnMouseDragged(e -> {
            double dx = e.getSceneX() - mouseOldX;
            double dy = e.getSceneY() - mouseOldY;

            boolean shift = e.isShiftDown();
            if (e.getButton() == MouseButton.PRIMARY) {
                if (!shift) {
                    rotateY.setAngle(rotateY.getAngle() + dx * 0.5);
                } else {
                    rotateX.setAngle(rotateX.getAngle() - dy * 0.5);
                }
            }

            mouseOldX = e.getSceneX();
            mouseOldY = e.getSceneY();
        });

        sub.setOnScroll(e -> zoom.setZ(zoom.getZ() + e.getDeltaY() * 0.05));

        sub.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case PLUS  -> zoom.setZ(zoom.getZ() + 2);
                case MINUS -> zoom.setZ(zoom.getZ() - 2);
            }
        });
    }

    private void loadMeshesFromURL(URL objFileURL) throws IOException {
        final var parser = new ObjFileParser(objFileURL, StandardCharsets.UTF_8);
        objModel.set(parser.parse());
    }

    private void loadMeshesFromFile(File objFile) throws IOException {
        loadMeshesFromURL(objFile.toURI().toURL());
    }

    private void onObjModelChanged(ObjModel newModel) {
        treeView = createObjModelTreeView(newModel, "OBJ");
        navigationPane.getChildren().setAll(treeView);
    }

    private void setDisplayedMeshView(MeshView meshView) {
        if (meshView == null) return;

        meshView.setCullFace(CullFace.NONE);
        meshView.drawModeProperty().bind(drawMode);

        //TODO reconsider
        center(meshView);

        Group pivot = new Group(meshView);
        center(pivot);
        pivot.getTransforms().addAll(rotateX, rotateY);

        world.getChildren().setAll(pivot);
    }
}
