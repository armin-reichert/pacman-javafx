/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.meshviewer;

import de.amr.meshbuilder.MeshBuilder;
import de.amr.objparser.ObjFileParser;
import de.amr.objparser.ObjModel;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.geometry.Side;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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

    public static final Color FOCUS_COLOR = Color.gray(0.66);
    public static final Color NOFOCUS_COLOR = Color.gray(0.5);

    public static final Font SOURCE_FONT = Font.font("Consolas", FontWeight.NORMAL, 14);
    public static final String SOURCE_STYLE = "-fx-control-inner-background:#222; -fx-text-fill:#f0f0f0";

    public static final int DEFAULT_ANGLE_X = 0;
    public static final int DEFAULT_ANGLE_Y = 0;
    public static final int DEFAULT_ZOOM    = -30;

    private final ObjectProperty<ObjModel> objModel = new SimpleObjectProperty<>(new ObjModel());

    private final Rotate rotateX = new Rotate(DEFAULT_ANGLE_X, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(DEFAULT_ANGLE_Y, Rotate.Y_AXIS);
    private final Translate zoom = new Translate(0, 0, DEFAULT_ZOOM);

    private final ObjectProperty<DrawMode> drawMode = new SimpleObjectProperty<>(DrawMode.FILL);

    private final Stage stage;
    private final Group world;
    private final SubScene previewSubScene;
    private final Pane navigationPane;
    private final FileChooser fileChooser;

    private File workDir;
    private TreeView<NavigationTreeNode> navigationTreeView;
    private double mouseOldX, mouseOldY;

    private Animation autoRotate;
    private final Rotate autoRotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate autoRotateY = new Rotate(0, Rotate.Y_AXIS);
    private Point3D autoRotateAxis = Rotate.Y_AXIS; // horizontally be default

    public MeshViewerUI(Stage stage) {
        this.stage = stage;

        final double height = 0.8 * Screen.getPrimary().getBounds().getHeight();
        final double width = 1.5 * height;

        // Camera
        final var cam = new PerspectiveCamera(true);
        cam.getTransforms().add(zoom);
        cam.setNearClip(0.1);
        cam.setFarClip(10_000);

        // Light
        AmbientLight ambient = new AmbientLight(Color.color(0.3, 0.3, 0.3));

        PointLight keyLight = new PointLight(Color.WHITE);
        keyLight.setTranslateX(200);
        keyLight.setTranslateY(-200);
        keyLight.setTranslateZ(-300);

        PointLight fillLight = new PointLight(Color.color(0.6, 0.6, 0.8));
        fillLight.setTranslateX(-200);
        fillLight.setTranslateY(200);
        fillLight.setTranslateZ(-300);

        world = new Group(ambient, keyLight, fillLight);

        previewSubScene = new SubScene(world, width, height, true, SceneAntialiasing.BALANCED);
        previewSubScene.setCamera(cam);
        previewSubScene.setFill(NOFOCUS_COLOR);
        previewSubScene.fillProperty().bind(previewSubScene.focusedProperty().map(focussed -> focussed? FOCUS_COLOR : NOFOCUS_COLOR));

        enableMouseControl(previewSubScene);

        final MenuBar menuBar = createMenus(stage);

        fileChooser = new FileChooser();
        fileChooser.setTitle("Open OBJ File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("OBJ Files", "*.obj")
        );

        navigationPane = new VBox();
        navigationPane.setMinWidth(300);
        navigationPane.setMaxWidth(300);

        Tab previewTab = new Tab("Preview");
        previewTab.setClosable(false);
        previewTab.setContent(previewSubScene);

        Tab objSourceTab = new Tab("Source");
        objSourceTab.setClosable(false);
        TextArea sourceView = createSourceView();
        objSourceTab.setContent(sourceView);

        TabPane tabPane = new TabPane(previewTab, objSourceTab);
        tabPane.setSide(Side.BOTTOM);
        tabPane.getSelectionModel().selectedItemProperty().addListener((_, _, selection) -> {
            Logger.debug("Selected tab: {}", selection);
            if (selection == previewTab) {
                Platform.runLater(previewSubScene::requestFocus);
            }
        });

        final BorderPane rootPane = new BorderPane();
        rootPane.setTop(menuBar);
        rootPane.setLeft(navigationPane);
        rootPane.setCenter(tabPane);

        previewSubScene.widthProperty().bind(rootPane.widthProperty().subtract(navigationPane.widthProperty()));
        previewSubScene.heightProperty().bind(rootPane.heightProperty());

        final Scene scene = new Scene(rootPane);
        stage.setScene(scene);
        stage.setTitle("Mesh Viewer");
        stage.setWidth(width);
        stage.setHeight(height);

        addFileDropSupport(scene);
        objModel.addListener((_, _, newObjModel) -> onObjModelChanged(newObjModel));
    }

    public void show() {
        stage.show();
        Platform.runLater(previewSubScene::requestFocus);
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

    private void addFileDropSupport(Scene scene) {
        // Accept file drag-over
        scene.setOnDragOver(event -> {
            if (event.getGestureSource() != scene &&
                event.getDragboard().hasFiles()) {

                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        // Handle file drop
        scene.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                File file = db.getFiles().getFirst();

                // Only accept OBJ files
                if (file.getName().toLowerCase().endsWith(".obj")) {
                    try {
                        loadMeshesFromFile(file);
                        workDir = file.getParentFile();
                    } catch (Exception x) {
                        Logger.error(x);
                    }
                    success = true;
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    private TextArea createSourceView() {
        final var sourceView = new TextArea();
        sourceView.setEditable(false);
        sourceView.setWrapText(false);
        sourceView.setPrefWidth(600);
        sourceView.setPrefHeight(800);
        sourceView.setFont(SOURCE_FONT);
        sourceView.setStyle(SOURCE_STYLE);
        sourceView.textProperty().bind(objModel.map(ObjModel::source));
        return sourceView;
    }

    private void createAutoRotateAnimation() {
        autoRotate = new Timeline(
            new KeyFrame(Duration.millis(16), _ -> {
                if (autoRotateAxis == Rotate.X_AXIS) {
                    autoRotateX.setAngle(autoRotateX.getAngle() + 0.5);
                } else {
                    autoRotateY.setAngle(autoRotateY.getAngle() - 0.5);
                }
            }) // ~60 FPS
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

        root.getChildren().add(createSubTree(meshBuilder.buildMeshViewsByObject(),   "Mesh Views by Object"));
        root.getChildren().add(createSubTree(meshBuilder.buildMeshViewsByGroup(),    "Mesh Views by Group"));
        root.getChildren().add(createSubTree(meshBuilder.buildMeshViewsByMaterial(), "Mesh Views by Material"));

        navigationTreeView = new TreeView<>(root);
        navigationTreeView.setFocusTraversable(false);
        navigationTreeView.setShowRoot(true);
        VBox.setVgrow(navigationTreeView, Priority.ALWAYS);

        navigationTreeView.getSelectionModel().selectedItemProperty().addListener((_, _, item) -> {
            if (item == null) return;
            if (item.getValue() instanceof MeshNode meshNode) {
                previewMeshView(meshNode.meshView);
            } else {
                Logger.info("Selected node has value {}", item.getValue());
            }
        });

        navigationTreeView.setCellFactory(_ -> new TreeCell<>() {
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

        navigationPane.getChildren().setAll(navigationTreeView);
    }

    private TreeItem<NavigationTreeNode> createSubTree(Map<String, MeshView> meshViews, String title) {
        if (meshViews.isEmpty()) title += " (None)";
        final var root = new TreeItem<NavigationTreeNode>(new LabelNode(title));
        root.setExpanded(true);
        for (String meshName : meshViews.keySet()) {
            final var child = new MeshNode(meshName, meshViews.get(meshName));
            root.getChildren().add(new TreeItem<>(child));
        }
        return root;
    }

    private void selectFirstObjectNodeInTree() {
        final TreeItem<NavigationTreeNode> rootItem = navigationTreeView.getRoot();
        if (!rootItem.getChildren().isEmpty()) {
            final TreeItem<NavigationTreeNode> objectsItem = rootItem.getChildren().getFirst();
            if (!objectsItem.getChildren().isEmpty()) {
                final TreeItem<NavigationTreeNode> firstObjectItem = objectsItem.getChildren().getFirst();
                navigationTreeView.getSelectionModel().select(firstObjectItem);
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
        autoRotateX.setAngle(DEFAULT_ANGLE_X);
        autoRotateY.setAngle(DEFAULT_ANGLE_Y);
        zoom.setZ(DEFAULT_ZOOM);
    }

    private void enableMouseControl(SubScene sub) {
        sub.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case PLUS  -> zoom.setZ(zoom.getZ() + 2);
                case MINUS -> zoom.setZ(zoom.getZ() - 2);
                case LEFT  -> {
                    rotateY(-1);
                    e.consume(); // do not deliver event to tab pane
                }
                case RIGHT -> {
                    rotateY(1);
                    e.consume(); // do not deliver event to tab pane
                }
                case UP    -> {
                    rotateX(-1);
                    e.consume(); // do not deliver event to tab pane
                }
                case DOWN  -> {
                    rotateX(1);
                    e.consume(); // do not deliver event to tab pane
                }
            }
        });
        sub.setOnKeyTyped(e -> {
            switch (e.getCharacter()) {
                case "x" -> autoRotateAxis = Rotate.X_AXIS;
                case "y" -> autoRotateAxis = Rotate.Y_AXIS;
                case "w" -> drawMode.set(drawMode.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
                case " " -> toggleAutoplay();
            }
        });

        sub.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                resetTransforms();
            }
        });

        sub.setOnMousePressed(e -> {
            mouseOldX = e.getSceneX();
            mouseOldY = e.getSceneY();
            e.consume();
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

        sub.setOnScroll(e -> zoom.setZ(zoom.getZ() + e.getDeltaY() * 0.02));

    }

    private void rotateX(double delta) {
        rotateX.setAngle(rotateX.getAngle() + delta);
    }

    private void rotateY(double delta) {
        rotateY.setAngle(rotateY.getAngle() + delta);
    }

    public void startAutoplay() {
        if (autoRotate == null) {
            createAutoRotateAnimation();
        }
        autoRotate.play();
        Logger.info("Autorotate started");
    }

    public void pauseAutoplay() {
        if (autoRotate == null) {
            createAutoRotateAnimation();
        }
        autoRotate.pause();
        Logger.info("Autorotate paused");

    }

    private void toggleAutoplay() {
        if (autoRotate.getStatus() == Animation.Status.RUNNING) {
            pauseAutoplay();
        } else {
            startAutoplay();
        }
    }

    private void previewMeshView(MeshView meshView) {
        if (meshView == null) return;

        meshView.setCullFace(CullFace.NONE);
        meshView.drawModeProperty().bind(drawMode);

        //TODO reconsider
        meshView.getTransforms().clear();
        center(meshView);

        Group pivot = new Group(meshView);
        center(pivot);

        // Flip around x-axis (otherwise many objects are upside-down initially)
        final var flipUpsideDown = new Rotate(180, Rotate.X_AXIS);
        pivot.getTransforms().addAll(flipUpsideDown, rotateX, rotateY, autoRotateX, autoRotateY);

        world.getChildren().setAll(pivot);
        resetTransforms();
        previewSubScene.requestFocus();
    }
}
