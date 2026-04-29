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
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MeshViewerUI {

    public static final String KEY_AUTO_ROTATE_HORIZONTALLY = "h";
    public static final String KEY_AUTO_ROTATE_VERTICALLY = "v";
    public static final String KEY_ROTATE_LEFT = "l";
    public static final String KEY_ROTATE_LEFT_LARGE = "L";
    public static final String KEY_ROTATE_RIGHT = "r";
    public static final String KEY_ROTATE_RIGHT_LARGE = "R";
    public static final String KEY_AUTOPLAY_TOGGLE = " ";
    public static final String KEY_WIREFRAME_TOGGLE = "w";

    public static final Color FOCUSSED_COLOR   = Color.gray(0.66);
    public static final Color UNFOCUSSED_COLOR = Color.gray(0.33);

    public static final int ROTATE_SINGLE_STEP_DEGREES = 10;

    public static final int DEFAULT_ANGLE_X = 0;
    public static final int DEFAULT_ANGLE_Y = 0;
    public static final double AUTO_ROTATE_SPEED = 0.1;

    public static final int DEFAULT_ZOOM = -30;
    public static final int ZOOM_MIN = -1000;
    public static final int ZOOM_MAX = -2;

    public static final double ZOOM_RATE_NORMAL = 0.02;
    public static final double ZOOM_RATE_LARGE = 0.5;

    private final ObjectProperty<ObjModel> objModel = new SimpleObjectProperty<>(new ObjModel());

    private final Rotate rotateX = new Rotate(DEFAULT_ANGLE_X, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(DEFAULT_ANGLE_Y, Rotate.Y_AXIS);

    private final Translate camZoom = new Translate(0, 0, DEFAULT_ZOOM);

    private final ObjectProperty<DrawMode> drawMode = new SimpleObjectProperty<>(DrawMode.FILL);
    private final ObjectProperty<Duration> parsingTime = new SimpleObjectProperty<>(Duration.ZERO);

    private final Stage stage;
    private final Group world;
    private final FlashMessageOverlay flashMessageOverlay;
    private final SubScene previewSubScene;
    private final BorderPane navigationPane;
    private final FileChooser fileChooser;

    private Group pivot;

    private File workDir;
    private TreeView<NavigationTreeNode> navigationTreeView;
    private double mouseOldX, mouseOldY;

    private Animation rotateAnimation;
    private final Rotate autoRotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate autoRotateY = new Rotate(0, Rotate.Y_AXIS);
    private Point3D autoRotateAxis = Rotate.Y_AXIS; // horizontally be default

    private final List<SampleModel> sampleModels = new ArrayList<>();
    private Menu samplesMenu;

    public MeshViewerUI(Stage stage) {
        this.stage = stage;

        final double height = 0.8 * Screen.getPrimary().getBounds().getHeight();
        final double width = 1.5 * height;

        // Camera
        PerspectiveCamera cam = new PerspectiveCamera(true);
        cam.getTransforms().addAll(camZoom);
        cam.setNearClip(0.1);
        cam.setFarClip(10_000);

        world = new Group();

        addLights(world);

        previewSubScene = new SubScene(world, width, height, true, SceneAntialiasing.BALANCED);
        previewSubScene.setCamera(cam);
        previewSubScene.fillProperty().bind(previewSubScene.focusedProperty()
            .map(hasFocus -> hasFocus? FOCUSSED_COLOR : UNFOCUSSED_COLOR));
        setPreviewControlHandlers();

        flashMessageOverlay = new FlashMessageOverlay();

        final StackPane previewArea = new StackPane(previewSubScene, flashMessageOverlay);

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

        final MenuBar menuBar = createMenus(stage);

        final BorderPane rootPane = new BorderPane();
        rootPane.setTop(menuBar);
        rootPane.setLeft(navigationPane);
        rootPane.setCenter(previewArea);

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
        if (!sampleModels.isEmpty()) {
            showSampleModel(sampleModels.getFirst());
        }
        Platform.runLater(previewSubScene::requestFocus);
    }

    public void showObjModel(File objFile) {
        try {
            showObjModel(objFile.toURI().toURL());
            workDir = objFile.getParentFile();
        } catch (IOException x) {
            Logger.error(x, "Cannot show OBJ model, file={}", objFile);
        }
    }

    public void showObjModel(URL url) {
        try {
            loadModelFromURL(url);
            selectFirstObjectNodeInTree();
            resetTransformsAndCamera();
        } catch (Exception x) {
            Logger.error(x, "Cannot show OBJ model, URL={}", url);
        }
    }

    public void addSampleModel(SampleModel sample) {
        sampleModels.add(sample);
        MenuItem item = new MenuItem(sample.title());
        item.setOnAction(_ -> showSampleModel(sample));
        samplesMenu.getItems().add(item);
    }

    public void showSampleModel(SampleModel sample) {
        showObjModel(sample.url());

        SampleState initial = sample.initialState();
        camZoom.setZ(initial.zoom());

        if (initial.rotateX() != 0) {
            pivot.getTransforms().addLast(new Rotate(initial.rotateX(), Rotate.X_AXIS));
        }
        if (initial.rotateY() != 0) {
            pivot.getTransforms().addLast(new Rotate(initial.rotateY(), Rotate.Y_AXIS));
        }
        if (initial.rotateZ() != 0) {
            pivot.getTransforms().addLast(new Rotate(initial.rotateZ(), Rotate.Z_AXIS));
        }

        if (rotateAnimation == null) {
            createAutoRotateAnimation();
        }
        if (sample.initialState().autoRotate()) {
            rotateAnimation.play();
        } else {
            rotateAnimation.stop();
        }
    }

    private void loadModelFromURL(URL objFileURL) throws IOException {
        final var parser = new ObjFileParser(objFileURL, StandardCharsets.UTF_8);
        final long start = System.nanoTime();
        final ObjModel parsedModel = parser.parse();
        final long durationNanos = System.nanoTime() - start;
        parsingTime.set(Duration.seconds(durationNanos / 1_000_000_000.0));
        objModel.set(parsedModel);
    }

    private void onObjModelChanged(ObjModel newModel) {
        String url = newModel.url();
        int lastSlash = url.lastIndexOf('/');
        String title = URLDecoder.decode(url.substring(lastSlash + 1), StandardCharsets.UTF_8);
        updateNavigationPane(title);
        selectFirstObjectNodeInTree();
    }

    // display

    private void displayMeshView(MeshView meshView) {
        if (meshView == null) return;

        meshView.setCullFace(CullFace.NONE);
        meshView.drawModeProperty().bind(drawMode);

        //TODO reconsider
        meshView.getTransforms().clear();
        center(meshView);

        pivot = new Group(meshView);
        center(pivot);

        // Flip around x-axis (otherwise many objects are upside-down initially)
        final var flipUpsideDown = new Rotate(180, Rotate.X_AXIS);
        pivot.getTransforms().addAll(flipUpsideDown, rotateX, rotateY, autoRotateX, autoRotateY);

        world.getChildren().setAll(pivot);
        previewSubScene.requestFocus();
    }

    private void flash(String message) {
        flashMessageOverlay.showMessage(message);
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

    private void createAutoRotateAnimation() {
        rotateAnimation = new Timeline(
            new KeyFrame(Duration.millis(16), _ -> {
                if (autoRotateAxis == Rotate.X_AXIS) {
                    autoRotateX.setAngle(autoRotateX.getAngle() + AUTO_ROTATE_SPEED);
                } else {
                    autoRotateY.setAngle(autoRotateY.getAngle() - AUTO_ROTATE_SPEED);
                }
            }) // ~60 FPS
        );
        rotateAnimation.setCycleCount(Animation.INDEFINITE);
    }

    private MenuBar createMenus(Stage stage) {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open OBJ…");
        MenuItem exitItem = new MenuItem("Exit");

        fileMenu.getItems().addAll(openItem, exitItem);

        openItem.setOnAction(_ -> {
            if (workDir != null && workDir.exists()) {
                fileChooser.setInitialDirectory(workDir);
            }
            final File objFile = fileChooser.showOpenDialog(stage);
            if (objFile != null) {
                showObjModel(objFile);
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

        samplesMenu = new Menu("Samples");

        // Compose the menus
        menuBar.getMenus().addAll(fileMenu, viewMenu, samplesMenu);

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
            if (item.getValue() instanceof MeshNode meshNode) {
                displayMeshView(meshNode.meshView);
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

    private void resetTransformsAndCamera() {
        rotateX.setAngle(DEFAULT_ANGLE_X);
        rotateY.setAngle(DEFAULT_ANGLE_Y);
        autoRotateX.setAngle(DEFAULT_ANGLE_X);
        autoRotateY.setAngle(DEFAULT_ANGLE_Y);

        camZoom.setZ(DEFAULT_ZOOM);
    }

    private void setPreviewControlHandlers() {
        previewSubScene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case PLUS  -> {
                    zoomBy(1);
                    e.consume();
                }
                case MINUS -> {
                    zoomBy(-1);
                    e.consume();
                }
                case LEFT  -> {
                    rotateYBy(-1);
                    e.consume(); // do not deliver event to tab pane
                }
                case RIGHT -> {
                    rotateYBy(1);
                    e.consume(); // do not deliver event to tab pane
                }
                case UP    -> {
                    rotateXBy(-1);
                    e.consume(); // do not deliver event to tab pane
                }
                case DOWN  -> {
                    rotateXBy(1);
                    e.consume(); // do not deliver event to tab pane
                }
            }
        });

        previewSubScene.setOnKeyTyped(e -> onKeyTypedInPreview(e.getCharacter()));

        previewSubScene.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                resetTransformsAndCamera();
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
                    rotateXBy(-dx * 1.5);
                } else {
                    rotateYBy(-dy * 1.5);
                }
            }

            mouseOldX = e.getSceneX();
            mouseOldY = e.getSceneY();
        });

        previewSubScene.setOnScroll(e -> {
            final double rate = e.isShiftDown() ? ZOOM_RATE_LARGE : ZOOM_RATE_NORMAL;
            zoomBy(e.getDeltaY() * rate);
        });
    }

    private void onKeyTypedInPreview(String key) {
        if (KEY_AUTO_ROTATE_HORIZONTALLY.equals(key)) {
            autoRotateAxis = Rotate.Y_AXIS;
            flash("Autorotate horizontally");
        }
        else if (KEY_AUTO_ROTATE_VERTICALLY.equals(key)) {
            autoRotateAxis = Rotate.X_AXIS;
            flash("Autorotate vertically");
        }
        else if (KEY_ROTATE_LEFT.equals(key)) {
            rotateYBy(ROTATE_SINGLE_STEP_DEGREES);
        }
        else if (KEY_ROTATE_LEFT_LARGE.equals(key)) {
            rotateYBy(3 * ROTATE_SINGLE_STEP_DEGREES);
        }
        else if (KEY_ROTATE_RIGHT.equals(key)) {
            rotateYBy(-ROTATE_SINGLE_STEP_DEGREES);
        }
        else if (KEY_ROTATE_RIGHT_LARGE.equals(key)) {
            rotateYBy(-3 * ROTATE_SINGLE_STEP_DEGREES);
        }
        else if (KEY_WIREFRAME_TOGGLE.equals(key)) {
            final DrawMode mode = drawMode.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL;
            drawMode.set(mode);
        }
        else if (KEY_AUTOPLAY_TOGGLE.equals(key)) {
            toggleAutoRotate();
            if (rotateAnimation.getStatus() == Animation.Status.PAUSED) {
                flash("Autorotate paused");
            } else {
                flash("Autorotate started");
            }
        }
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
                    success = true;
                    showObjModel(file);
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

    private void zoomBy(double delta) {
        double z = Math.clamp(camZoom.getZ() + delta, ZOOM_MIN, ZOOM_MAX);
        camZoom.setZ(z);
        Logger.info("Zoom: " + z);
    }

    private void rotateXBy(double delta) {
        rotateX.setAngle((rotateX.getAngle() + delta) % 360);
    }

    private void rotateYBy(double delta) {
        rotateY.setAngle((rotateY.getAngle() + delta) % 360);
    }

    public void startAutoRotate() {
        if (rotateAnimation == null) {
            createAutoRotateAnimation();
        }
        rotateAnimation.play();
        Logger.info("Autorotate started");
    }

    public void pauseAutoRotate() {
        if (rotateAnimation == null) {
            createAutoRotateAnimation();
        }
        rotateAnimation.pause();
        Logger.info("Autorotate paused");
    }

    private void toggleAutoRotate() {
        if (rotateAnimation.getStatus() == Animation.Status.RUNNING) {
            pauseAutoRotate();
        } else {
            startAutoRotate();
        }
    }
}
