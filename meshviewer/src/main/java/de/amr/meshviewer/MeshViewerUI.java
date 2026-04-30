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
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.geometry.VPos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
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
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class MeshViewerUI {

    public static final String STAGE_TITLE = "JavaFX OBJ Mesh Viewer";

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
    public static final int ZOOM_MIN = -10_000;
    public static final int ZOOM_MAX = -2;

    public static final double ZOOM_RATE_NORMAL = 0.02;
    public static final double ZOOM_RATE_LARGE = 0.5;

    private final ObjectProperty<ObjModel> objModel = new SimpleObjectProperty<>();
    private final ObjectProperty<DrawMode> drawMode = new SimpleObjectProperty<>(DrawMode.FILL);
    private final ObjectProperty<Duration> loadingTime = new SimpleObjectProperty<>(Duration.ZERO);

    private final ObservableList<SampleModel> sampleModels = FXCollections.observableArrayList();

    // Flip around x-axis (otherwise many objects are upside-down initially)
    private final Rotate flipYDirection = new Rotate(180, Rotate.X_AXIS);

    private final Rotate rotateX = new Rotate(DEFAULT_ANGLE_X, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(DEFAULT_ANGLE_Y, Rotate.Y_AXIS);

    private final Translate camZoom = new Translate(0, 0, DEFAULT_ZOOM);

    // UI
    private final Stage stage;
    private BorderPane rootPane;
    private Group world;
    private FlashMessageOverlay flashMessageOverlay;
    private SubScene previewSubScene;
    private PerspectiveCamera cam;
    private VBox selectionArea;
    private ObjModelInfoPanel modelInfoPane;
    private FileChooser fileChooser;
    // Displayed mesh view is contained in this group:
    private Group pivot;
    private TreeView<NavigationTreeNode> navigationTreeView;
    private MenuBar menuBar;
    private Menu samplesMenu;

    private File workDir;
    private double mouseOldX, mouseOldY;

    // Animation
    private Animation autoRotateAnimation;
    private final Rotate autoRotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate autoRotateY = new Rotate(0, Rotate.Y_AXIS);
    private Point3D autoRotateAxis = Rotate.Y_AXIS; // horizontally be default

    public MeshViewerUI(Stage stage) {
        this.stage = requireNonNull(stage);

        final double height = 0.8 * Screen.getPrimary().getBounds().getHeight();
        final double width = 1.5 * height;

        createUI(width, height);

        objModel.addListener((_, _, newModel) -> {
            if (newModel != null) {
                populateNavigationTree(newModel, createTreeTitle(newModel));
                selectFirstObjectNodeInNavigationTree();
                modelInfoPane.update(newModel, loadingTime.get());
            }
        });
    }

    // Public

    public void show() {
        stage.show();
        if (!sampleModels.isEmpty()) {
            showSampleModel(sampleModels.getFirst());
            Platform.runLater(previewSubScene::requestFocus);
        }
    }

    public void showObjModel(File objFile) {
        requireNonNull(objFile);
        try {
            final URL url = objFile.toURI().toURL();
            showObjModel(url);
            workDir = objFile.getParentFile();
        } catch (MalformedURLException x) {
            Logger.error(x, "Cannot show OBJ model, file={}", objFile);
        }
    }

    public void showObjModel(URL url) {
        requireNonNull(url);
        try {
            loadModelFromURL(url);
            selectFirstObjectNodeInNavigationTree();
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

        if (autoRotateAnimation == null) {
            createAutoRotateAnimation();
        }
        if (sample.initialState().autoRotate()) {
            autoRotateAnimation.play();
        } else {
            autoRotateAnimation.stop();
        }
    }

    // Private

    private void createUI(double width, double height) {
        createCamera();
        createLayout(width, height);

        final Scene scene = new Scene(rootPane);
        setPreviewControlHandlers();
        addFileDragNDropSupport(scene);

        stage.setScene(scene);
        stage.setTitle(STAGE_TITLE);
        stage.setWidth(width);
        stage.setHeight(height);
        stage.heightProperty().addListener((_,_,h) -> {
            Logger.info("Stage height={}", h);
            //TODO After resizing stage and resizing back, layout gets corrupt
        });
    }

    private void createCamera() {
        cam = new PerspectiveCamera(true);
        cam.getTransforms().addAll(camZoom);
        cam.setNearClip(0.1);
        cam.setFarClip(10_000);
    }

    private void createLayout(double width, double height) {
        world = new Group();
        addLights(world);

        previewSubScene = new SubScene(world, width, height, true, SceneAntialiasing.BALANCED);
        previewSubScene.setCamera(cam);
        previewSubScene.fillProperty().bind(previewSubScene.focusedProperty()
            .map(hasFocus -> hasFocus? FOCUSSED_COLOR : UNFOCUSSED_COLOR));

        flashMessageOverlay = new FlashMessageOverlay();

        final StackPane previewArea = new StackPane(previewSubScene, flashMessageOverlay);

        previewSubScene.widthProperty().bind(previewArea.widthProperty());
        previewSubScene.heightProperty().bind(previewArea.heightProperty());

        fileChooser = new FileChooser();
        fileChooser.setTitle("Open OBJ File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ Files", "*.obj"));

        createSelectionArea();
        createMenus(stage, selectionArea);

        rootPane = new BorderPane();
        rootPane.setTop(menuBar);
        rootPane.setCenter(previewArea);
        rootPane.setLeft(selectionArea);
    }

    private void loadModelFromURL(URL objFileURL) throws IOException {
        final var parser = new ObjFileParser(objFileURL, StandardCharsets.UTF_8);
        final long start = System.nanoTime();
        final ObjModel parsedModel = parser.parse();
        final long millis = (System.nanoTime() - start) / 1_000_000;
        loadingTime.set(Duration.millis(millis));
        objModel.set(parsedModel);
    }

    private void displayMeshView(MeshView meshView) {
        if (meshView == null) return;

        meshView.setCullFace(CullFace.NONE);
        meshView.drawModeProperty().bind(drawMode);

        //TODO reconsider
        meshView.getTransforms().clear();
        center(meshView);

        pivot = new Group(meshView);
        center(pivot);

        pivot.getTransforms().addAll(flipYDirection, rotateX, rotateY, autoRotateX, autoRotateY);

        world.getChildren().setAll(pivot);
        previewSubScene.requestFocus();
    }

    private void flash(String message) {
        flashMessageOverlay.showMessage(message);
    }

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
        autoRotateAnimation = new Timeline(
            new KeyFrame(Duration.millis(16), _ -> {
                if (autoRotateAxis == Rotate.X_AXIS) {
                    autoRotateX.setAngle(autoRotateX.getAngle() + AUTO_ROTATE_SPEED);
                } else {
                    autoRotateY.setAngle(autoRotateY.getAngle() - AUTO_ROTATE_SPEED);
                }
            }) // ~60 FPS
        );
        autoRotateAnimation.setCycleCount(Animation.INDEFINITE);
    }

    private void createMenus(Stage stage, Pane selectionArea) {
        // File menu

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

        // View menu

        final Menu viewMenu = new Menu("View");

        final CheckMenuItem miNavigation = new CheckMenuItem("Navigation");
        miNavigation.selectedProperty().bindBidirectional(selectionArea.managedProperty());

        final CheckMenuItem miWireframe = new CheckMenuItem("Wireframe");
        miWireframe.selectedProperty().addListener((_, _, sel) -> drawMode.set(sel ? DrawMode.LINE : DrawMode.FILL));
        drawMode.addListener((_, _, mode) -> miWireframe.setSelected(mode == DrawMode.LINE));

        viewMenu.getItems().addAll(miNavigation, miWireframe);

        // Samples menu

        samplesMenu = new Menu("Samples");
        samplesMenu.disableProperty().bind(Bindings.isEmpty(sampleModels));

        menuBar = new MenuBar(fileMenu, viewMenu, samplesMenu);
    }

    private void createSelectionArea() {
        createNavigationTree();

        final ScrollPane treeScrollPane = new ScrollPane(navigationTreeView);
        treeScrollPane.setMaxHeight(300);
        treeScrollPane.setFitToWidth(true);
        treeScrollPane.setFitToHeight(true);

        modelInfoPane = new ObjModelInfoPanel();
        modelInfoPane.maxHeightProperty().bind(modelInfoPane.prefHeightProperty());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        selectionArea = new VBox(treeScrollPane, spacer, modelInfoPane);
        selectionArea.setFillWidth(true);
    }

    private void createNavigationTree() {
        final TreeItem<NavigationTreeNode> root = new TreeItem<>(new LabelNode("No OBJ file loaded"));
        root.setExpanded(true);

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
                    default -> "Unknown tree node";
                });
            }
        });
    }

    private void populateNavigationTree(ObjModel objModel, String title) {
        final TreeItem<NavigationTreeNode> root = navigationTreeView.getRoot();
        root.setValue(new LabelNode(title));
        root.getChildren().clear();
        final MeshBuilder meshBuilder = new MeshBuilder(objModel);
        addNavigationTreeLevel(meshBuilder.buildMeshViewsByObject(),   "Mesh Views by Object");
        addNavigationTreeLevel(meshBuilder.buildMeshViewsByGroup(),    "Mesh Views by Group");
        addNavigationTreeLevel(meshBuilder.buildMeshViewsByMaterial(), "Mesh Views by Material");
    }

    private void addNavigationTreeLevel(Map<String, MeshView> meshViews, String title) {
        if (meshViews.isEmpty()) title += " (None)";
        final TreeItem<NavigationTreeNode> root = new TreeItem<>(new LabelNode(title));
        root.setExpanded(true);
        for (String meshName : meshViews.keySet()) {
            final var meshNode = new MeshNode(meshName, meshViews.get(meshName));
            root.getChildren().add(new TreeItem<>(meshNode));
        }
        navigationTreeView.getRoot().getChildren().add(root);
    }

    private void selectFirstObjectNodeInNavigationTree() {
        final TreeItem<NavigationTreeNode> root = navigationTreeView.getRoot();
        if (!root.getChildren().isEmpty()) {
            final TreeItem<NavigationTreeNode> objects = root.getChildren().getFirst();
            if (!objects.getChildren().isEmpty()) {
                final TreeItem<NavigationTreeNode> firstObject = objects.getChildren().getFirst();
                navigationTreeView.getSelectionModel().select(firstObject);
            }
        }
    }

    private String createTreeTitle(ObjModel objModel) {
        final String url = objModel.url();
        return URLDecoder.decode(url.substring(url.lastIndexOf('/') + 1), StandardCharsets.UTF_8);
    }

    private void center(Node node) {
        final Bounds b = node.getBoundsInLocal();
        node.getTransforms().add(new Translate(-b.getCenterX(), -b.getCenterY(), -b.getCenterZ()));
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
            boolean shift = e.isShiftDown();
            boolean control = e.isControlDown();
            boolean controlShift = control && shift;
            switch (e.getCode()) {
                case PLUS  -> {
                    int delta = controlShift ? 100 : shift ? 10 : 1;
                    zoomBy(delta);
                    e.consume();
                }
                case MINUS -> {
                    int delta = controlShift ? 100 : shift ? 10 : 1;
                    zoomBy(-delta);
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
            flash("Auto-Rotate horizontally");
        }
        else if (KEY_AUTO_ROTATE_VERTICALLY.equals(key)) {
            autoRotateAxis = Rotate.X_AXIS;
            flash("Auto-Rotate vertically");
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
            toggleAutoRotateWithFlashMessage();
        }
    }

    private void addFileDragNDropSupport(Scene scene) {
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
        autoRotateAnimation().play();
        Logger.info("Auto-Rotate started");
    }

    public void pauseAutoRotate() {
        autoRotateAnimation().pause();
        Logger.info("Auto-Rotate paused");
    }

    private void toggleAutoRotateWithFlashMessage() {
        if (autoRotateAnimation().getStatus() == Animation.Status.RUNNING) {
            pauseAutoRotate();
            flash("Auto-Rotate paused");
        } else {
            startAutoRotate();
            flash("Auto-Rotate started");
        }
    }

    private Animation autoRotateAnimation() {
        if (autoRotateAnimation == null) {
            createAutoRotateAnimation();
        }
        return autoRotateAnimation;
    }
}
