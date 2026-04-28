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
import javafx.scene.paint.Color;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
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

    public static final Color FOCUSSED_COLOR = Color.gray(0.66);
    public static final Color UNFOCUSSED_COLOR = Color.gray(0.5);

    public static final Font SOURCE_FONT = Font.font("Consolas", FontWeight.NORMAL, 14);
    public static final String SOURCE_STYLE = "-fx-control-inner-background:#222; -fx-text-fill:#f0f0f0";

    public static final int DEFAULT_ANGLE_X = 0;
    public static final int DEFAULT_ANGLE_Y = 0;
    public static final int DEFAULT_ZOOM    = -30;
    public static final double AUTO_ROTATE_SPEED = 0.1;

    public static final int ZOOM_MIN = -200;
    public static final int ZOOM_MAX = -2;

    private final ObjectProperty<ObjModel> objModel = new SimpleObjectProperty<>(new ObjModel());

    private final Rotate rotateX = new Rotate(DEFAULT_ANGLE_X, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(DEFAULT_ANGLE_Y, Rotate.Y_AXIS);
    private final Translate zoom = new Translate(0, 0, DEFAULT_ZOOM);

    private final ObjectProperty<DrawMode> drawMode = new SimpleObjectProperty<>(DrawMode.FILL);
    private final ObjectProperty<Duration> parsingTime = new SimpleObjectProperty<>(Duration.ZERO);

    private final Stage stage;
    private final Group world;
    private final SubScene previewSubScene;
    private final BorderPane navigationPane;
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

        world = new Group();

        addLights(world);

        previewSubScene = new SubScene(world, width, height, true, SceneAntialiasing.BALANCED);
        previewSubScene.setCamera(cam);
        previewSubScene.fillProperty().bind(previewSubScene.focusedProperty()
            .map(hasFocus -> hasFocus? FOCUSSED_COLOR : UNFOCUSSED_COLOR));
        setPreviewControlHandlers();

        final Pane previewArea = new Pane(previewSubScene);
        previewSubScene.widthProperty().bind(previewArea.widthProperty());
        previewSubScene.heightProperty().bind(previewArea.heightProperty());

        fileChooser = new FileChooser();
        fileChooser.setTitle("Open OBJ File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("OBJ Files", "*.obj")
        );

        navigationPane = new BorderPane();
        navigationPane.setMinWidth(300);
        navigationPane.setMaxWidth(300);

        Tab previewTab = new Tab("Preview");
        previewTab.setClosable(false);
        previewTab.setContent(previewArea);

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

        final MenuBar menuBar = createMenus(stage);

        final BorderPane rootPane = new BorderPane();
        rootPane.setTop(menuBar);
        rootPane.setLeft(navigationPane);
        rootPane.setCenter(tabPane);

        final Scene scene = new Scene(rootPane);
        stage.setScene(scene);
        stage.setTitle("OBJ Mesh Viewer");
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

    // load data

    private void loadMeshesFromURL(URL objFileURL) throws IOException {
        final var parser = new ObjFileParser(objFileURL, StandardCharsets.UTF_8);
        final long start = System.nanoTime();
        final ObjModel parsedModel = parser.parse();
        final long durationNanos = System.nanoTime() - start;
        parsingTime.set(Duration.seconds(durationNanos / 1_000_000_000.0));
        objModel.set(parsedModel);
    }

    private boolean loadMeshesFromFile(File objFile) {
        try {
            loadMeshesFromURL(objFile.toURI().toURL());
            workDir = objFile.getParentFile();
            return true;
        } catch (IOException x) {
            Logger.error(x);
            return false;
        }
    }

    private void onObjModelChanged(ObjModel newModel) {
        String url = newModel.url();
        int lastSlash = url.lastIndexOf('/');
        url = url.substring(lastSlash + 1);
        updateNavigationPane(url);
        selectFirstObjectNodeInTree();
    }

    // create UI

    private void addLights(Group parent) {
        final var ambient = new AmbientLight(Color.color(0.3, 0.3, 0.3));

        final var keyLight = new PointLight(Color.WHITE);
        keyLight.setTranslateX(200);
        keyLight.setTranslateY(-200);
        keyLight.setTranslateZ(-300);

        final var fillLight = new PointLight(Color.color(0.6, 0.6, 0.8));
        fillLight.setTranslateX(-200);
        fillLight.setTranslateY(200);
        fillLight.setTranslateZ(-300);

        parent.getChildren().addAll(ambient, keyLight, fillLight);
    }

    private void addFileDropSupport(Scene scene) {
        // Accept file drag-over
        scene.setOnDragOver(e -> {
            if (e.getGestureSource() != scene && e.getDragboard().hasFiles()) {
                e.acceptTransferModes(TransferMode.COPY);
            } else {
                // This seems to have no effect on Windows
                e.acceptTransferModes(TransferMode.NONE);
            }
            e.consume();
        });

        // Handle file drop
        scene.setOnDragDropped(e -> {
            final Dragboard db = e.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                final File file = db.getFiles().getFirst();
                // Only accept OBJ files
                if (file.getName().toLowerCase().endsWith(".obj")) {
                    success = loadMeshesFromFile(file);
                }
            }
            e.setDropCompleted(success);
            e.consume();
            if (success) {
                Platform.runLater(() -> {
                    stage.toFront();
                    stage.requestFocus();
                    previewSubScene.requestFocus();
                });
            }
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
                    autoRotateX.setAngle(autoRotateX.getAngle() + AUTO_ROTATE_SPEED);
                } else {
                    autoRotateY.setAngle(autoRotateY.getAngle() - AUTO_ROTATE_SPEED);
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
                loadMeshesFromFile(objFile);
            }
        });

        exitItem.setOnAction(_ -> Platform.exit());

        final Menu viewMenu = new Menu("View");

        final CheckMenuItem miNavigationVisible = new CheckMenuItem("Navigation");
        miNavigationVisible.selectedProperty().bindBidirectional(navigationPane.managedProperty());
        navigationPane.visibleProperty().bind(navigationPane.managedProperty());

        final CheckMenuItem miWireframeMode = new CheckMenuItem("Wireframe");
        miWireframeMode.selectedProperty().addListener((_, _, sel) ->
            drawMode.set(sel ? DrawMode.LINE : DrawMode.FILL)
        );

        drawMode.addListener((_, _, mode) ->
            miWireframeMode.setSelected(mode == DrawMode.LINE)
        );

        viewMenu.getItems().addAll(miNavigationVisible, miWireframeMode);
        menuBar.getMenus().add(viewMenu);

        return menuBar;
    }

    private void updateNavigationPane(String title) {
        final MeshBuilder meshBuilder = new MeshBuilder(objModel.get());

        final TreeItem<NavigationTreeNode> root = new TreeItem<>(new LabelNode(title));
        root.setExpanded(true);

        addSubTree(root, meshBuilder.buildMeshViewsByObject(),   "Mesh Views by Object");
        addSubTree(root, meshBuilder.buildMeshViewsByGroup(),    "Mesh Views by Group");
        addSubTree(root, meshBuilder.buildMeshViewsByMaterial(), "Mesh Views by Material");

        navigationTreeView = new TreeView<>(root);
        navigationTreeView.setFocusTraversable(false);
        navigationTreeView.setShowRoot(true);

        navigationTreeView.getSelectionModel().selectedItemProperty().addListener((_, _, item) -> {
            if (item == null) return;
            Logger.info("Selected node: {}", item.getValue());
            if (item.getValue() instanceof MeshNode meshNode) {
                previewMeshView(meshNode.meshView);
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

        navigationPane.setCenter(navigationTreeView);


        final Text parsingTimeText = new Text("Parsing time: xx.xxx sec");
        parsingTimeText.textProperty().bind(parsingTime.map(duration -> "Parsing time: %.3f sec".formatted(duration.toSeconds())));
        navigationPane.setBottom(parsingTimeText);
    }

    private void addSubTree(TreeItem<NavigationTreeNode> parentTreeItem, Map<String, MeshView> meshViews, String title) {
        if (meshViews.isEmpty()) title += " (None)";
        final var root = new TreeItem<NavigationTreeNode>(new LabelNode(title));
        root.setExpanded(true);
        for (String meshName : meshViews.keySet()) {
            final var child = new MeshNode(meshName, meshViews.get(meshName));
            root.getChildren().add(new TreeItem<>(child));
        }
        parentTreeItem.getChildren().add(root);
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

    private void setPreviewControlHandlers() {
        previewSubScene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case PLUS  -> zoom(1);
                case MINUS -> zoom(-1);
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
        previewSubScene.setOnKeyTyped(e -> {
            switch (e.getCharacter()) {
                case "x" -> autoRotateAxis = Rotate.X_AXIS;
                case "y" -> autoRotateAxis = Rotate.Y_AXIS;
                case "w" -> drawMode.set(drawMode.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
                case " " -> toggleAutoplay();
            }
        });

        previewSubScene.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                resetTransforms();
            }
            previewSubScene.requestFocus();
        });

        previewSubScene.setOnMousePressed(e -> {
            mouseOldX = e.getSceneX();
            mouseOldY = e.getSceneY();
            e.consume();
        });

        previewSubScene.setOnMouseDragged(e -> {
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

        previewSubScene.setOnScroll(e -> zoom.setZ(zoom.getZ() + e.getDeltaY() * 0.02));

    }

    private void zoom(double delta) {
        if (zoom.getZ() < 0.5 * (ZOOM_MIN + ZOOM_MAX)) delta *= 2;
        double z = Math.clamp(zoom.getZ() + delta, ZOOM_MIN, ZOOM_MAX);
        zoom.setZ(z);
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
        previewSubScene.requestFocus();
    }
}
