/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package experiments.meshviewer;

import de.amr.objparser.ObjFileParser;
import de.amr.objparser.ObjModel;
import de.amr.pacmanfx.uilib.model3D.MeshBuilder;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MeshViewerUI {


    public static final Color FOCUS_COLOR = Color.gray(0.5);
    public static final Color NOFOCUS_COLOR = Color.gray(0.4);

    public static final int DEFAULT_ANGLE_X = 0;
    public static final int DEFAULT_ANGLE_Y = 0;
    public static final int DEFAULT_ZOOM    = -30;

    private final ObjectProperty<ObjModel> objModel = new SimpleObjectProperty<>(new ObjModel());

    private final Rotate rotateX = new Rotate(DEFAULT_ANGLE_X, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(DEFAULT_ANGLE_Y, Rotate.Y_AXIS);
    private final Translate zoom = new Translate(0, 0, DEFAULT_ZOOM);

    private final ObjectProperty<DrawMode> drawMode = new SimpleObjectProperty<>(DrawMode.LINE);

    private final Stage stage;
    private final Group world;
    private final SubScene sub;
    private final Pane navigationPane;
    private final FileChooser fileChooser;

    private File workDir;
    private TreeView<NavigationTreeNode> treeView;
    private Group pivot;
    private double mouseOldX, mouseOldY;

    private Animation autoRotate;
    private final Rotate autoRotateY = new Rotate(0, Rotate.Y_AXIS);

    public MeshViewerUI(Stage stage) {
        this.stage = stage;

        final double height = 0.8 * Screen.getPrimary().getBounds().getHeight();
        final double width = 1.5 * height;

        // Camera
        final var cam = new PerspectiveCamera(true);
        cam.getTransforms().add(zoom);
        cam.setNearClip(0.1);
        cam.setFarClip(10_000);

        world = new Group();
        sub = new SubScene(world, width, height, true, SceneAntialiasing.BALANCED);
        sub.setCamera(cam);
        sub.setFill(NOFOCUS_COLOR);
        sub.fillProperty().bind(sub.focusedProperty().map(focussed -> focussed? FOCUS_COLOR : NOFOCUS_COLOR));

        enableMouseControl(sub);

        final MenuBar menuBar = createMenus(stage);

        fileChooser = new FileChooser();
        fileChooser.setTitle("Open OBJ File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("OBJ Files", "*.obj")
        );

        navigationPane = new VBox();
        navigationPane.setMinWidth(300);
        navigationPane.setMaxWidth(300);

        final BorderPane rootPane = new BorderPane();
        rootPane.setTop(menuBar);
        rootPane.setLeft(navigationPane);
        rootPane.setCenter(sub);

        sub.widthProperty().bind(rootPane.widthProperty().subtract(navigationPane.widthProperty()));
        sub.heightProperty().bind(rootPane.heightProperty());

        final Scene scene = new Scene(rootPane);
        stage.setScene(scene);
        stage.setTitle("Mesh Viewer");
        stage.setWidth(width);
        stage.setHeight(height);

        objModel.addListener((_, _, newObjModel) -> onObjModelChanged(newObjModel));
    }

    public void show() {
        stage.show();
    }

    public void showObjModel(URL url) {
        try {
            loadMeshesFromURL(url);
            selectFirstObjectNodeInTree();
        } catch (Exception x) {
            Logger.error(x, "Could not load OBJ model, URL={}", url);
        }
    }

    // Handle data model

    private void loadMeshesFromURL(URL objFileURL) throws IOException {
        final var parser = new ObjFileParser(objFileURL, StandardCharsets.UTF_8);
        objModel.set(parser.parse());
    }

    private void loadMeshesFromFile(File objFile) throws IOException {
        loadMeshesFromURL(objFile.toURI().toURL());
    }

    private void onObjModelChanged(ObjModel newModel) {
        String url = newModel.url();
        int lastSlash = url.lastIndexOf('/');
        url = url.substring(lastSlash + 1);
        updateNavigationPane(url);
        selectFirstObjectNodeInTree();
    }

    // create UI

    private void createAutoRotateAnimation() {
        autoRotate = new Timeline(
            new KeyFrame(Duration.millis(16), _ -> autoRotateY.setAngle(autoRotateY.getAngle() - 0.5)) // ~60 FPS
        );
        autoRotate.setCycleCount(Animation.INDEFINITE);
    }

    private MenuBar createMenus(Stage stage) {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open OBJ…");
        MenuItem exitItem = new MenuItem("Exit");

        fileMenu.getItems().addAll(openItem, exitItem);
        menuBar.getMenus().add(fileMenu);

        openItem.setOnAction(_ -> {
            if (workDir != null && workDir.exists()) {
                fileChooser.setInitialDirectory(workDir);
            }
            final File objFile = fileChooser.showOpenDialog(stage);
            if (objFile != null) {
                try {
                    loadMeshesFromFile(objFile);
                    workDir = objFile.getParentFile();
                } catch (Exception x) {
                    Logger.error(x);
                }
            }
        });

        exitItem.setOnAction(_ -> Platform.exit());

        return menuBar;
    }

    private void updateNavigationPane(String title) {
        final MeshBuilder meshBuilder = new MeshBuilder(objModel.get());

        final TreeItem<NavigationTreeNode> root = new TreeItem<>(new LabelNode(title));
        root.setExpanded(true);

        root.getChildren().add(createSubTree(meshBuilder.buildMeshViewsByObject(), "Mesh Views by Object"));
        root.getChildren().add(createSubTree(meshBuilder.buildMeshViewsByGroup(), "Mesh Views by Group"));
        root.getChildren().add(createSubTree(meshBuilder.buildMeshViewsByMaterial(), "Mesh Views by Material"));

        treeView = new TreeView<>(root);
        treeView.setShowRoot(true);
        VBox.setVgrow(treeView, Priority.ALWAYS);
        treeView.setFocusTraversable(false);

        treeView.getSelectionModel().selectedItemProperty().addListener((_, _, selectedNode) -> {
            switch (selectedNode.getValue()) {
                case LabelNode labelNode -> Logger.info("Selected node has label '{}'", labelNode.label);
                case MeshNode meshNode -> {
                    String meshName = meshNode.meshName;
                    MeshView meshView = meshNode.meshView;
                    showMeshView(meshView);
                    Logger.info("Mesh View displayed: {}", meshName);
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

        navigationPane.getChildren().setAll(treeView);
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

    private void selectFirstObjectNodeInTree() {
        final TreeItem<NavigationTreeNode> rootItem = treeView.getRoot();
        if (!rootItem.getChildren().isEmpty()) {
            final TreeItem<NavigationTreeNode> objectsItem = rootItem.getChildren().getFirst();
            if (!objectsItem.getChildren().isEmpty()) {
                final TreeItem<NavigationTreeNode> firstObjectItem = objectsItem.getChildren().getFirst();
                treeView.getSelectionModel().select(firstObjectItem);
            }
        }
    }

    private void center(Node node) {
        Bounds b = node.getBoundsInLocal();
        node.getTransforms().add(new Translate(
            -b.getCenterX(),
            -b.getCenterY(),
            -b.getCenterZ()
        ));
    }

    private void resetTransforms() {
        rotateX.setAngle(DEFAULT_ANGLE_X);
        rotateY.setAngle(DEFAULT_ANGLE_Y);
        zoom.setZ(DEFAULT_ZOOM);
    }

    private void enableMouseControl(SubScene sub) {

        sub.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case PLUS  -> zoom.setZ(zoom.getZ() + 2);
                case MINUS -> zoom.setZ(zoom.getZ() - 2);
                case LEFT  -> rotateY(1);
                case RIGHT -> rotateY(-1);
                case UP    -> rotateX(-1);
                case DOWN  -> rotateX(1);
            }
        });
        sub.setOnKeyTyped(e -> {
            if ("w".equals(e.getCharacter())) {
                drawMode.set(drawMode.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
            }
            else if (" ".equals(e.getCharacter())) {
                Logger.info("SPACE typed");
                if (autoRotate == null) {
                    createAutoRotateAnimation();
                }
                if (autoRotate.getStatus() == Animation.Status.RUNNING) {
                    autoRotate.pause();
                    Logger.info("Autorotate paused");
                } else {
                    autoRotate.play();
                    Logger.info("Autorotate started");
                }
            }
        });

        sub.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                resetTransforms();
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
                if (shift) {
                    rotateX(-dx * 1.5);
                } else {
                    rotateY(-dy * 1.5);
                }
            }

            mouseOldX = e.getSceneX();
            mouseOldY = e.getSceneY();
        });

        sub.setOnScroll(e -> zoom.setZ(zoom.getZ() + e.getDeltaY() * 0.05));

    }

    private void rotateX(double delta) {
        rotateX.setAngle(rotateX.getAngle() + delta);
    }

    private void rotateY(double delta) {
        rotateY.setAngle(rotateY.getAngle() + delta);
    }

    private void showMeshView(MeshView meshView) {
        if (meshView == null) return;

        meshView.setCullFace(CullFace.NONE);
        meshView.drawModeProperty().bind(drawMode);

        //TODO reconsider
        meshView.getTransforms().clear();
        center(meshView);

        pivot = new Group(meshView);
        center(pivot);
        pivot.getTransforms().addAll(rotateX, rotateY, autoRotateY);
        // Flip around x-axis (otherwise many objects are upside-down initially)
        pivot.getTransforms().add(new Rotate(180, Rotate.X_AXIS));

        world.getChildren().setAll(pivot);
        resetTransforms();
        sub.requestFocus();
    }
}
