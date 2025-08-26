/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.*;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import de.amr.pacmanfx.uilib.tilemap.FoodMapRenderer;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapColorScheme;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static de.amr.pacmanfx.lib.tilemap.TerrainTile.*;
import static de.amr.pacmanfx.tilemap.editor.ArcadeSprites.*;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.*;
import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.rangeClosed;

public class TileMapEditor {

    public static final short TOOL_SIZE = 32;
    public static final short MIN_GRID_SIZE = 8;
    public static final short MAX_GRID_SIZE = 80;

    public static final int EMPTY_ROWS_BEFORE_MAZE = 3;
    public static final int EMPTY_ROWS_BELOW_MAZE = 2;

    public static final byte PALETTE_ID_ACTORS  = 0;
    public static final byte PALETTE_ID_TERRAIN = 1;
    public static final byte PALETTE_ID_FOOD    = 2;

    public static final ResourceBundle TEXT_BUNDLE = ResourceBundle.getBundle(TileMapEditor.class.getPackageName() + ".texts");

    public static String translated(String key, Object... args) {
        try {
            return MessageFormat.format(TEXT_BUNDLE.getString(key), args);
        }
        catch (MissingResourceException x) {
            Logger.error("No resource with key {} found in {}", key, TEXT_BUNDLE);
            return "[%s]".formatted(key);
        }
    }

    private static final byte[][] DEFAULT_HOUSE_ROWS = {
        { ARC_NW.$,  WALL_H.$,  WALL_H.$,  DOOR.$,    DOOR.$,    WALL_H.$,  WALL_H.$,  ARC_NE.$ },
        { WALL_V.$,  EMPTY.$,   EMPTY.$,   EMPTY.$,   EMPTY.$,   EMPTY.$,   EMPTY.$,   WALL_V.$ },
        { WALL_V.$,  EMPTY.$,   EMPTY.$,   EMPTY.$,   EMPTY.$,   EMPTY.$,   EMPTY.$,   WALL_V.$ },
        { WALL_V.$,  EMPTY.$,   EMPTY.$,   EMPTY.$,   EMPTY.$,   EMPTY.$,   EMPTY.$,   WALL_V.$ },
        { ARC_SW.$,  WALL_H.$,  WALL_H.$,  WALL_H.$,  WALL_H.$,  WALL_H.$,  WALL_H.$,  ARC_SE.$ },
    };

    private static boolean isSupportedImageFile(File file) {
        return Stream.of(".bmp", ".gif", ".jpg", ".png").anyMatch(ext -> file.getName().toLowerCase().endsWith(ext));
    }

    private static boolean isWorldMapFile(File file) {
        return file.getName().toLowerCase().endsWith(".world");
    }

    public static final Font FONT_DROP_HINT               = Font.font("Sans", FontWeight.BOLD, 16);
    public static final Font FONT_MESSAGE                 = Font.font("Sans", FontWeight.BOLD, 14);
    public static final Font FONT_SOURCE_VIEW             = Font.font("Consolas", FontWeight.NORMAL, 14);
    public static final Font FONT_STATUS_LINE_EDIT_MODE   = Font.font("Sans", FontWeight.BOLD, 18);
    public static final Font FONT_STATUS_LINE_NORMAL      = Font.font("Sans", FontWeight.NORMAL, 14);

    public static final Color COLOR_CANVAS_BACKGROUND = Color.BLACK;

    public static final Node NO_GRAPHIC = null;

    public static final ExtensionFilter FILTER_WORLD_MAP_FILES = new ExtensionFilter("World Map Files", "*.world");
    public static final ExtensionFilter FILTER_IMAGE_FILES     = new ExtensionFilter("Image Files", "*.bmp", "*.gif", "*.jpg", "*.png");
    public static final ExtensionFilter FILTER_ALL_FILES       = new ExtensionFilter("All Files", "*.*");

    // Change management

    public class ChangeManager {

        private boolean edited;
        private boolean terrainMapChanged;
        private boolean foodMapChanged;
        private boolean obstaclesUpToDate;
        private boolean redrawRequested;

        public void setEdited(boolean edited) { this.edited = edited; }

        public boolean isEdited() { return edited; }

        public void setWorldMapChanged() {
            setTerrainMapChanged();
            setFoodMapChanged();
        }

        public void setTerrainMapChanged() {
            terrainMapChanged = true;
            obstaclesUpToDate = false;
        }

        public void setFoodMapChanged() {
            foodMapChanged = true;
        }

        public void requestRedraw() {
            redrawRequested = true;
        }

        public boolean isRedrawRequested() {
            return redrawRequested;
        }

        private void processChanges() {
            if (!obstaclesUpToDate) {
                tilesWithErrors.clear();
                tilesWithErrors.addAll(editedWorldMap().buildObstacleList());
                obstaclesUpToDate = true;
                requestRedraw();
            }
            if (terrainMapChanged) {
                if (terrainMapPropertiesEditor != null) {
                    terrainMapPropertiesEditor.setTileMap(editedWorldMap(), LayerID.TERRAIN);
                }
                mazePreview3D.updateTerrain();
                updateSourceView();
                terrainMapChanged = false;
                Logger.trace("Terrain map updated");
                requestRedraw();
            }
            if (foodMapChanged) {
                if (foodMapPropertiesEditor != null) {
                    foodMapPropertiesEditor.setTileMap(editedWorldMap(), LayerID.FOOD);
                }
                mazePreview3D.updateFood();
                updateSourceView();
                foodMapChanged = false;
                Logger.trace("Food map updated");
                requestRedraw();
            }
        }
    }

    private final Model3DRepository model3DRepository;
    private final ChangeManager changeManager = new ChangeManager();
    private File currentDirectory;
    private Instant messageCloseTime;
    private final AnimationTimer updateLoop;
    private final List<Vector2i> tilesWithErrors = new ArrayList<>();

    private final BorderPane contentPane = new BorderPane();
    private final Stage stage;
    private Pane propertyEditorsPane;
    private EditCanvas editCanvas;
    private ScrollPane spEditCanvas;
    private ScrollPane spPreview2D;
    private EditorMazePreview2D editorMazePreview2D;
    private TextArea sourceView;
    private ScrollPane spTemplateImage;
    private Pane dropTargetForTemplateImage;
    private SplitPane splitPaneEditorAndPreviews;
    private Label messageLabel;
    private FileChooser fileChooser;
    private TabPane tabPaneWithPalettes;
    private Slider sliderZoom;
    private HBox statusLine;
    private TabPane tabPaneEditorViews;
    private Tab tabEditCanvas;
    private Tab tabTemplateImage;
    private TemplateImageCanvas templateImageCanvas;
    private TabPane tabPanePreviews;
    private Tab tabPreview2D;
    private Tab tabPreview3D;
    private Tab tabSourceView;
    private EditorMazePreview3D mazePreview3D;

    private EditorMenuBar menuBar;
    private final Palette[] palettes = new Palette[3];
    private PropertyEditorPane terrainMapPropertiesEditor;
    private PropertyEditorPane foodMapPropertiesEditor;

    // Properties

    // editMode

    public static final EditMode DEFAULT_EDIT_MODE = EditMode.INSPECT;

    private ObjectProperty<EditMode> editMode;

    public ObjectProperty<EditMode> editModeProperty() {
        if (editMode == null) {
            editMode = new SimpleObjectProperty<>(DEFAULT_EDIT_MODE) {
                @Override
                protected void invalidated() {
                    onEditModeChanged(get());
                }
            };
        }
        return editMode;
    }

    public EditMode editMode() { return editMode == null ? DEFAULT_EDIT_MODE : editModeProperty().get(); }

    public boolean isEditMode(EditMode mode) { return editMode() == mode; }

    public void setEditMode(EditMode mode) {
        editModeProperty().set(requireNonNull(mode));
    }

    // currentFile

    private final ObjectProperty<File> currentFile = new SimpleObjectProperty<>();

    // editedWorldMap

    private final ObjectProperty<WorldMap> editedWorldMap = new SimpleObjectProperty<>(WorldMap.emptyMap(28, 36)) {
        @Override
        protected void invalidated() {
            templateImagePy.set(null);
            changeManager.setWorldMapChanged();
        }
    };

    // gridSize

    private static final double DEFAULT_GRID_SIZE = 8;

    private DoubleProperty gridSize;

    public DoubleProperty gridSizeProperty() {
        if (gridSize == null) {
            gridSize = new SimpleDoubleProperty(DEFAULT_GRID_SIZE) {
                @Override
                protected void invalidated() {
                    changeManager.requestRedraw();
                }
            };
        }
        return gridSize;
    }

    public double gridSize() { return gridSize.get(); }

    public void setGridSize(double size) {
        gridSizeProperty().set(size);
    }

    // actorsVisible

    public boolean DEFAULT_ACTORS_VISIBLE = true;

    private BooleanProperty actorsVisible;

    public BooleanProperty actorsVisibleProperty() {
        if (actorsVisible == null) {
            actorsVisible = new SimpleBooleanProperty(DEFAULT_ACTORS_VISIBLE) {
                @Override
                protected void invalidated() {
                    changeManager.requestRedraw();
                }
            };
        }
        return actorsVisible;
    }

    public boolean isActorsVisible() {
        return actorsVisible == null ? DEFAULT_ACTORS_VISIBLE : actorsVisibleProperty().get();
    }

    public void setActorsVisible(boolean visible) {
        actorsVisibleProperty().set(visible);
    }

    // gridVisible

    public static final boolean DEFAULT_GRID_VISIBLE = true;

    private BooleanProperty gridVisible;

    public BooleanProperty gridVisibleProperty() {
        if (gridVisible == null) {
            gridVisible = new SimpleBooleanProperty(DEFAULT_GRID_VISIBLE) {
                @Override
                protected void invalidated() {
                    changeManager.requestRedraw();
                }
            };
        }
        return gridVisible;
    }

    // foodVisible

    public static final boolean DEFAULT_FOOD_VISIBLE = true;

    private BooleanProperty foodVisible;

    public BooleanProperty foodVisibleProperty() {
        if (foodVisible == null) {
            foodVisible = new SimpleBooleanProperty(DEFAULT_FOOD_VISIBLE) {
                @Override
                protected void invalidated() {
                    changeManager.requestRedraw();
                }
            };
        }
        return foodVisible;
    }

    public boolean isFoodVisible() {
        return foodVisible == null ? DEFAULT_FOOD_VISIBLE : foodVisible.get();
    }

    public void setFoodVisible(boolean visible) {
        foodVisibleProperty().set(visible);
    }

    // mapPropertyEditorsVisible

    public static final boolean DEFAULT_MAP_PROPERTY_EDITORS_VISIBLE = false;

    private BooleanProperty mapPropertyEditorsVisible;

    public BooleanProperty propertyEditorsVisibleProperty() {
        if (mapPropertyEditorsVisible == null) {
            mapPropertyEditorsVisible = new SimpleBooleanProperty(DEFAULT_MAP_PROPERTY_EDITORS_VISIBLE) {
                @Override
                protected void invalidated() {
                    contentPane.setLeft(get() ? propertyEditorsPane : null);
                }
            };
        }
        return mapPropertyEditorsVisible;
    }

    public boolean isMapPropertyEditorsVisible() {
        return mapPropertyEditorsVisible == null ? DEFAULT_MAP_PROPERTY_EDITORS_VISIBLE : propertyEditorsVisibleProperty().get();
    }

    public void setMapPropertyEditorsVisible(boolean value) {
        propertyEditorsVisibleProperty().set(value);
    }

    // obstacleInnerAreaDisplayed

    public static final boolean DEFAULT_OBSTACLE_INNER_AREA_DISPLAYED = false;

    private BooleanProperty obstacleInnerAreaDisplayed;

    public BooleanProperty obstacleInnerAreaDisplayedProperty() {
        if (obstacleInnerAreaDisplayed == null) {
            obstacleInnerAreaDisplayed = new SimpleBooleanProperty(DEFAULT_OBSTACLE_INNER_AREA_DISPLAYED) {
                @Override
                protected void invalidated() {
                    changeManager.requestRedraw();
                }
            };
        }
        return obstacleInnerAreaDisplayed;
    }

    public boolean isObstacleInnerAreaDisplayed() {
        return obstacleInnerAreaDisplayed == null ? DEFAULT_OBSTACLE_INNER_AREA_DISPLAYED :obstacleInnerAreaDisplayedProperty().get();
    }

    public void setObstacleInnerAreaDisplayed(boolean value) {
        obstacleInnerAreaDisplayedProperty().set(value);
    }

    // obstaclesJoining

    public static boolean DEFAULT_OBSTACLES_JOINING = true;

    private BooleanProperty obstaclesJoining;

    public BooleanProperty obstaclesJoiningProperty() {
        if (obstaclesJoining == null) {
            obstaclesJoining = new SimpleBooleanProperty(DEFAULT_OBSTACLES_JOINING);
        }
        return obstaclesJoining;
    }

    public boolean isObstaclesJoining() {
        return obstaclesJoining == null ? DEFAULT_OBSTACLES_JOINING : obstaclesJoiningProperty().get();
    }

    public void setObstaclesJoining(boolean value) {
        obstaclesJoiningProperty().set(value);
    }

    // segmentNumbersVisible

    public static final boolean DEFAULT_SEGMENT_NUMBERS_VISIBLE = false;

    private BooleanProperty segmentNumbersVisible;

    public BooleanProperty segmentNumbersVisibleProperty() {
        if (segmentNumbersVisible == null) {
            segmentNumbersVisible = new SimpleBooleanProperty(DEFAULT_SEGMENT_NUMBERS_VISIBLE) {
                @Override
                protected void invalidated() {
                    changeManager.requestRedraw();
                }
            };
        }
        return segmentNumbersVisible;
    }

    public boolean isSegmentNumbersVisible() {
        return segmentNumbersVisible == null ? DEFAULT_SEGMENT_NUMBERS_VISIBLE : segmentNumbersVisibleProperty().get();
    }

    public void setSegmentNumbersVisible(boolean value) {
        segmentNumbersVisibleProperty().set(value);
    }

    // symmetric edit mode

    public static final boolean DEFAULT_SYMMETRIC_EDIT_MODE = true;

    private BooleanProperty symmetricEditMode;

    public BooleanProperty symmetricEditModeProperty() {
        if (symmetricEditMode == null) {
            symmetricEditMode = new SimpleBooleanProperty(DEFAULT_SYMMETRIC_EDIT_MODE);
        }
        return symmetricEditMode;
    }

    public boolean isSymmetricEditMode() {
        return symmetricEditMode == null ? DEFAULT_SYMMETRIC_EDIT_MODE : symmetricEditModeProperty().get();
    }

    public void setSymmetricEditMode(boolean value) {
        symmetricEditModeProperty().set(value);
    }

    private final ObjectProperty<Image> templateImagePy = new SimpleObjectProperty<>();

    // terrainVisible

    public static final boolean DEFAULT_TERRAIN_VISIBLE = true;

    private BooleanProperty terrainVisible;

    public BooleanProperty terrainVisibleProperty() {
        if (terrainVisible == null) {
            terrainVisible = new SimpleBooleanProperty(DEFAULT_TERRAIN_VISIBLE) {
                @Override
                protected void invalidated() {
                    changeManager.requestRedraw();
                }
            };
        }
        return terrainVisible;
    }

    public boolean isTerrainVisible() {
        return terrainVisible == null ? DEFAULT_TERRAIN_VISIBLE : terrainVisible.get();
    }

    public void setTerrainVisible(boolean visible) {
        terrainVisibleProperty().set(visible);
    }

    // title

    private final StringProperty title = new SimpleStringProperty("Tile Map Editor");

    // Accessor methods

    public ChangeManager changeManager() { return changeManager;}

    public ObjectProperty<WorldMap> editedWorldMapProperty() { return editedWorldMap; }

    public WorldMap editedWorldMap() { return editedWorldMap.get(); }

    public void setEditedWorldMap(WorldMap worldMap) { editedWorldMap.set(requireNonNull(worldMap)); }

    public StringProperty titleProperty() { return title; }

    public ObjectProperty<Image> templateImageProperty() { return templateImagePy; }

    public byte selectedPaletteID() {
        return (Byte) tabPaneWithPalettes.getSelectionModel().getSelectedItem().getUserData();
    }

    public Palette selectedPalette() {
        return palettes[selectedPaletteID()];
    }

    public Pane getContentPane() {
        return contentPane;
    }

    public EditorMenuBar getMenuBar() {
        return menuBar;
    }

    public List<Vector2i> tilesWithErrors() {
        return tilesWithErrors;
    }

    public TileMapEditor(Stage stage, Model3DRepository model3DRepository) {
        this.stage = requireNonNull(stage);
        this.model3DRepository = requireNonNull(model3DRepository);
        this.menuBar = new EditorMenuBar(this);

        createFileChooser();
        createObstacleEditor();
        createEditCanvas();
        createPreview2D();
        createPreview3D();
        createTemplateImageCanvas();
        createMapSourceView();
        createPalettes(editCanvas.terrainRenderer(), editCanvas.foodRenderer());
        createPropertyEditors();
        createTabPaneWithEditViews();
        createTabPaneWithPreviews();
        createMessageDisplay();
        createZoomSlider();
        createStatusLine();
        createMenuBarAndMenus();
        loadSampleMapsAndAddMenuEntries();

        arrangeMainLayout();

        contentPane.setOnKeyTyped(this::onKeyTyped);
        contentPane.setOnKeyPressed(this::onKeyPressed);

        updateLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateAutoClosingMessage();
                changeManager.processChanges();
                if (changeManager.isRedrawRequested()) {
                    var colors = new TerrainMapColorScheme(
                            COLOR_CANVAS_BACKGROUND,
                            getColorFromMap(editedWorldMap(), LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_FILL, parseColor(MS_PACMAN_COLOR_WALL_FILL)),
                            getColorFromMap(editedWorldMap(), LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_STROKE, parseColor(MS_PACMAN_COLOR_WALL_STROKE)),
                            getColorFromMap(editedWorldMap(), LayerID.TERRAIN, WorldMapProperty.COLOR_DOOR, parseColor(MS_PACMAN_COLOR_DOOR))
                    );
                    draw(colors);
                }
            }
        };
    }

    public void init(File workDir) {
        currentDirectory = workDir;
        setBlankMap(28, 36);
        mazePreview3D.reset();
        setEditMode(EditMode.INSPECT);
    }

    public void start(Stage stage) {
        title.bind(createTitleBinding());
        stage.titleProperty().bind(title);
        contentPane.setLeft(null); // no properties editor
        showEditHelpText();
        updateLoop.start();
    }

    public void stop() {
        updateLoop.stop();
        setEditMode(EditMode.INSPECT);
    }

    private void updateAutoClosingMessage() {
        if (messageCloseTime != null && Instant.now().isAfter(messageCloseTime)) {
            messageCloseTime = null;
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(2), messageLabel);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> {
                messageLabel.setText("");
                messageLabel.setOpacity(1.0);
            });
            fadeOut.play();
        }
    }

    public void showMessage(String message, long seconds, MessageType type) {
        Color color = switch (type) {
            case INFO -> Color.BLACK;
            case WARNING -> Color.GREEN;
            case ERROR -> Color.RED;
        };
        messageLabel.setTextFill(color);
        messageLabel.setText(message);
        messageCloseTime = Instant.now().plus(java.time.Duration.ofSeconds(seconds));
    }

    private void clearMessage() {
        showMessage("", 0, MessageType.INFO);
    }

    public void showEditHelpText() {
        showMessage(translated("edit_help"), 30, MessageType.INFO);
    }

    private void createFileChooser() {
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(FILTER_WORLD_MAP_FILES, FILTER_ALL_FILES);
        fileChooser.setSelectedExtensionFilter(FILTER_WORLD_MAP_FILES);
        fileChooser.setInitialDirectory(currentDirectory);
    }

    private ObstacleEditor createObstacleEditor() {
        var obstacleEditor = new ObstacleEditor() {
            @Override
            public void setValue(Vector2i tile, byte value) {
                setTileValueRespectSymmetry(editedWorldMap(), LayerID.TERRAIN, tile, value);
            }
        };
        obstacleEditor.joiningProperty().bind(obstaclesJoiningProperty());
        obstacleEditor.worldMapProperty().bind(editedWorldMapProperty());
        obstacleEditor.symmetricEditModeProperty().bind(symmetricEditModeProperty());
        return obstacleEditor;
    }

    private void createEditCanvas() {
        ObstacleEditor obstacleEditor = createObstacleEditor();
        editCanvas = new EditCanvas(obstacleEditor);
        editCanvas.gridSizeProperty().bind(gridSizeProperty());
        editCanvas.gridVisibleProperty().bind(gridVisibleProperty());
        editCanvas.worldMapProperty().bind(editedWorldMapProperty());
        editCanvas.templateImageGrayProperty().bind(templateImageProperty().map(Ufx::imageToGreyscale));
        editCanvas.terrainVisibleProperty().bind(terrainVisibleProperty());
        editCanvas.foodVisibleProperty().bind(foodVisibleProperty());
        editCanvas.actorsVisibleProperty().bind(actorsVisibleProperty());

        editCanvas.setOnContextMenuRequested(event -> editCanvas.onContextMenuRequested(this, event));
        editCanvas.setOnMouseClicked(event -> editCanvas.onMouseClicked(this, event));
        editCanvas.setOnMouseMoved(event -> editCanvas.onMouseMoved(this, event));
        editCanvas.setOnMouseReleased(event -> editCanvas.onMouseReleased(this, event));
        editCanvas.setOnKeyPressed(event -> editCanvas.onKeyPressed(this, event));

        spEditCanvas = new ScrollPane(editCanvas);
        spEditCanvas.setFitToHeight(true);
        registerDragAndDropImageHandler(spEditCanvas);
        //TODO is there a better way to get the initial resize time of the scroll pane?
        spEditCanvas.heightProperty().addListener((py,oldHeight,newHeight) -> {
            if (oldHeight.doubleValue() == 0) { // initial resize
                int initialGridSize = (int) Math.max(newHeight.doubleValue() / editedWorldMap().numRows(), MIN_GRID_SIZE);
                setGridSize(initialGridSize);
            }
        });
    }

    private void createPreview2D() {
        editorMazePreview2D = new EditorMazePreview2D();
        editorMazePreview2D.widthProperty().bind(editCanvas.widthProperty());
        editorMazePreview2D.heightProperty().bind(editCanvas.heightProperty());
        editorMazePreview2D.gridSizeProperty().bind(gridSizeProperty());
        editorMazePreview2D.terrainVisibleProperty().bind(terrainVisibleProperty());
        editorMazePreview2D.foodVisibleProperty().bind(foodVisibleProperty());
        editorMazePreview2D.actorsVisibleProperty().bind(actorsVisibleProperty());

        spPreview2D = new ScrollPane(editorMazePreview2D);
        spPreview2D.setFitToHeight(true);
        spPreview2D.hvalueProperty().bindBidirectional(spEditCanvas.hvalueProperty());
        spPreview2D.vvalueProperty().bindBidirectional(spEditCanvas.vvalueProperty());
    }

    private void createPreview3D() {
        mazePreview3D = new EditorMazePreview3D(this, model3DRepository, 500, 500);
        mazePreview3D.foodVisibleProperty().bind(foodVisibleProperty());
        mazePreview3D.terrainVisibleProperty().bind(terrainVisibleProperty());
        mazePreview3D.worldMapProperty().bind(editedWorldMap);
    }

    private void createTemplateImageCanvas() {
        templateImageCanvas = new TemplateImageCanvas(this);
        Pane pane = new Pane(templateImageCanvas, templateImageCanvas.getColorIndicator());
        pane.setBackground(Background.fill(Color.TRANSPARENT));
        spTemplateImage = new ScrollPane(pane);
    }

    public void setTerrainMapPropertyValue(String propertyName, String value) {
        requireNonNull(value);
        if (editedWorldMap().properties(LayerID.TERRAIN).containsKey(propertyName)
            && editedWorldMap().properties(LayerID.TERRAIN).get(propertyName).equals(value))
            return;
        editedWorldMap().properties(LayerID.TERRAIN).put(propertyName, value);
        changeManager.setTerrainMapChanged();
        changeManager.setEdited(true);
    }

    private void removeTerrainMapProperty(String key) {
        editedWorldMap().properties(LayerID.TERRAIN).remove(key);
        changeManager.setTerrainMapChanged();
        changeManager.setEdited(true);
    }

    public void setFoodMapPropertyValue(String propertyName, String value) {
        requireNonNull(value);
        if (editedWorldMap().properties(LayerID.FOOD).containsKey(propertyName)
            && editedWorldMap().properties(LayerID.FOOD).get(propertyName).equals(value))
            return;
        editedWorldMap().properties(LayerID.FOOD).put(propertyName, value);
        changeManager.setFoodMapChanged();
        changeManager.setEdited(true);
    }

    private void removeFoodMapProperty(String key) {
        editedWorldMap().properties(LayerID.FOOD).remove(key);
        changeManager.setFoodMapChanged();
        changeManager.setEdited(true);
    }

    private void createMapSourceView() {
        sourceView = new TextArea();
        sourceView.setEditable(false);
        sourceView.setWrapText(false);
        sourceView.setPrefWidth(600);
        sourceView.setPrefHeight(800);
        sourceView.setFont(FONT_SOURCE_VIEW);
        sourceView.setStyle("-fx-control-inner-background:#222; -fx-text-fill: #f0f0f0;");
    }

    private void createTabPaneWithEditViews() {
        tabEditCanvas = new Tab(translated("tab_editor"), spEditCanvas);

        var hint = new Button(translated("image_drop_hint"));
        hint.setFont(FONT_DROP_HINT);
        hint.setOnAction(ae -> initWorldMapForTemplateImage());
        hint.disableProperty().bind(editModeProperty().map(mode -> mode == EditMode.INSPECT));

        dropTargetForTemplateImage = new BorderPane(hint);
        registerDragAndDropImageHandler(dropTargetForTemplateImage);

        var stackPane = new StackPane(spTemplateImage, dropTargetForTemplateImage);
        templateImagePy.addListener((py, ov, nv) -> {
            stackPane.getChildren().remove(dropTargetForTemplateImage);
            if (nv == null) {
                stackPane.getChildren().add(dropTargetForTemplateImage);
            }
        });
        tabTemplateImage = new Tab(translated("tab_template_image"), stackPane);

        tabPaneEditorViews = new TabPane(tabEditCanvas, tabTemplateImage);
        tabPaneEditorViews.getTabs().forEach(tab -> tab.setClosable(false));
        tabPaneEditorViews.setSide(Side.BOTTOM);
        tabPaneEditorViews.getSelectionModel().select(tabEditCanvas);
    }

    private void registerDragAndDropImageHandler(Node node) {
        node.setOnDragOver(e -> {
            if (e.getDragboard().hasFiles()) {
                File file = e.getDragboard().getFiles().getFirst();
                if (isSupportedImageFile(file)  & !isEditMode(EditMode.INSPECT)
                    || isWorldMapFile(file))
                {
                    e.acceptTransferModes(TransferMode.COPY);
                }
            }
            e.consume();
        });
        node.setOnDragDropped(this::onFileDroppedOnEditCanvas);
    }

    private void onFileDroppedOnEditCanvas(DragEvent e) {
        executeWithCheckForUnsavedChanges(() -> {
            if (e.getDragboard().hasFiles()) {
                File file = e.getDragboard().getFiles().getFirst();
                if (isSupportedImageFile(file) && !isEditMode(EditMode.INSPECT)) {
                    e.acceptTransferModes(TransferMode.COPY);
                    try (FileInputStream in = new FileInputStream(file)) {
                        Image image = new Image(in);
                        boolean accepted = setBlankMapForTemplateImage(image);
                        if (accepted) {
                            showMessage("Select colors for tile identification!", 10, MessageType.INFO);
                            tabPaneEditorViews.getSelectionModel().select(tabTemplateImage);
                        }
                    } catch (IOException x) {
                        showMessage("Could not open image file " + file, 3, MessageType.ERROR);
                        Logger.error(x);
                    }
                } else if (isWorldMapFile(file)) {
                    readMapFile(file);
                }
            }
        });
        e.consume();
    }

    private void createTabPaneWithPreviews() {
        tabPreview2D = new Tab(translated("preview2D"), spPreview2D);
        tabPreview3D = new Tab(translated("preview3D"), mazePreview3D.getSubScene());
        tabSourceView = new Tab(translated("source"), sourceView);

        tabPanePreviews = new TabPane(tabPreview2D, tabPreview3D, tabSourceView);
        tabPanePreviews.setSide(Side.BOTTOM);
        tabPanePreviews.getTabs().forEach(tab -> tab.setClosable(false));
        tabPanePreviews.getSelectionModel().select(tabPreview2D);

        mazePreview3D.getSubScene().widthProperty().bind(tabPanePreviews.widthProperty());
        mazePreview3D.getSubScene().heightProperty().bind(tabPanePreviews.heightProperty());

        splitPaneEditorAndPreviews = new SplitPane(tabPaneEditorViews, tabPanePreviews);
        splitPaneEditorAndPreviews.setDividerPositions(0.5);
    }

    private void createPalettes(TerrainTileMapRenderer terrainRenderer, FoodMapRenderer foodRenderer) {
        palettes[PALETTE_ID_TERRAIN] = createTerrainPalette(PALETTE_ID_TERRAIN, TOOL_SIZE, this, terrainRenderer);
        palettes[PALETTE_ID_FOOD]    = createFoodPalette(PALETTE_ID_FOOD, TOOL_SIZE, this, foodRenderer);
        palettes[PALETTE_ID_ACTORS]  = createActorPalette(PALETTE_ID_ACTORS, TOOL_SIZE, this, terrainRenderer);

        var tabTerrain = new Tab(translated("terrain"), palettes[PALETTE_ID_TERRAIN].root());
        tabTerrain.setClosable(false);
        tabTerrain.setUserData(PALETTE_ID_TERRAIN);

        var tabPellets = new Tab(translated("pellets"), palettes[PALETTE_ID_FOOD].root());
        tabPellets.setClosable(false);
        tabPellets.setUserData(PALETTE_ID_FOOD);

        var tabActors = new Tab(translated("actors"), palettes[PALETTE_ID_ACTORS].root());
        tabActors.setClosable(false);
        tabActors.setUserData(PALETTE_ID_ACTORS);

        tabPaneWithPalettes = new TabPane(tabTerrain, tabPellets, tabActors);
        tabPaneWithPalettes.setPadding(new Insets(5, 5, 5, 5));
        tabPaneWithPalettes.setMinHeight(75);
    }

    private void createPropertyEditors() {
        terrainMapPropertiesEditor = new PropertyEditorPane(this);
        terrainMapPropertiesEditor.enabledPy.bind(editModeProperty().map(mode -> mode != EditMode.INSPECT));
        terrainMapPropertiesEditor.setPadding(new Insets(10,0,0,0));

        foodMapPropertiesEditor = new PropertyEditorPane(this);
        foodMapPropertiesEditor.enabledPy.bind(editModeProperty().map(mode -> mode != EditMode.INSPECT));
        foodMapPropertiesEditor.setPadding(new Insets(10,0,0,0));

        var terrainPropertiesPane = new TitledPane(translated("terrain"), terrainMapPropertiesEditor);
        terrainPropertiesPane.setMinWidth(300);
        terrainPropertiesPane.setExpanded(true);

        var foodPropertiesPane = new TitledPane(translated("pellets"), foodMapPropertiesEditor);
        foodPropertiesPane.setExpanded(true);

        propertyEditorsPane = new VBox(terrainPropertiesPane, foodPropertiesPane);
        propertyEditorsPane.visibleProperty().bind(propertyEditorsVisibleProperty());
    }

    private void createMessageDisplay() {
        messageLabel = new Label();
        messageLabel.setFont(FONT_MESSAGE);
        messageLabel.setMinWidth(200);
    }

    private void createZoomSlider() {
        sliderZoom = new Slider(MIN_GRID_SIZE, MAX_GRID_SIZE, 0.5 * (MIN_GRID_SIZE + MAX_GRID_SIZE));
        sliderZoom.setShowTickLabels(false);
        sliderZoom.setShowTickMarks(true);
        sliderZoom.setPrefWidth(120);
        Bindings.bindBidirectional(sliderZoom.valueProperty(), gridSize);
        Tooltip tt = new Tooltip();
        tt.setFont(Font.font(14));
        tt.textProperty().bind(gridSizeProperty().map("Grid Size: %s"::formatted));
        sliderZoom.setTooltip(tt);
    }

    private void createStatusLine() {
        var lblMapSize = new Label();
        lblMapSize.setFont(FONT_STATUS_LINE_NORMAL);
        lblMapSize.textProperty().bind(editedWorldMap.map(worldMap -> (worldMap != null)
            ? "Cols: %d Rows: %d".formatted(worldMap.numCols(), worldMap.numRows()) : "")
        );

        var lblFocussedTile = new Label();
        lblFocussedTile.setFont(FONT_STATUS_LINE_NORMAL);
        lblFocussedTile.setMinWidth(70);
        lblFocussedTile.setMaxWidth(70);
        lblFocussedTile.textProperty().bind(editCanvas.focussedTileProperty().map(
            tile -> tile != null ? "(%2d,%2d)".formatted(tile.x(), tile.y()) : "n/a"));

        var lblEditMode = new Label();
        lblEditMode.setAlignment(Pos.BASELINE_RIGHT);
        lblEditMode.setMinWidth(100);
        lblEditMode.setFont(FONT_STATUS_LINE_EDIT_MODE);
        lblEditMode.setEffect(new Glow(0.2));
        lblEditMode.textProperty().bind(Bindings.createStringBinding(
            () -> switch (editMode()) {
                case INSPECT -> translated("mode.inspect");
                case EDIT    -> isSymmetricEditMode() ?  translated("mode.symmetric") : translated("mode.edit");
                case ERASE   -> translated("mode.erase");
            }, editModeProperty(), symmetricEditModeProperty()
        ));
        lblEditMode.textFillProperty().bind(
            editModeProperty().map(mode -> switch (mode) {
            case INSPECT -> Color.GRAY;
            case EDIT    -> Color.FORESTGREEN;
            case ERASE   -> Color.RED;
        }));
        lblEditMode.setOnMouseClicked(e -> showEditHelpText());

        statusLine = new HBox(
            lblMapSize,
            filler(10),
            lblFocussedTile,
            spacer(),
            messageLabel,
            spacer(),
            filler(10),
            sliderZoom,
            filler(10),
            lblEditMode
        );

        statusLine.setPadding(new Insets(6, 2, 2, 2));
    }

    private void arrangeMainLayout() {
        var contentArea = new VBox(tabPaneWithPalettes, splitPaneEditorAndPreviews, statusLine);
        contentArea.setPadding(new Insets(0,5,0,5));
        VBox.setVgrow(tabPaneWithPalettes, Priority.NEVER);
        VBox.setVgrow(splitPaneEditorAndPreviews, Priority.ALWAYS);
        VBox.setVgrow(statusLine, Priority.NEVER);

        contentPane.setLeft(propertyEditorsPane);
        contentPane.setCenter(contentArea);
    }

    private StringBinding createTitleBinding() {
        return Bindings.createStringBinding(() -> {
                File mapFile = currentFile.get();
                if (mapFile != null) {
                    return "%s: [%s] - %s".formatted( translated("map_editor"), mapFile.getName(), mapFile.getPath() );
                }
                if (editedWorldMap() != null && editedWorldMap().url() != null) {
                    return  "%s: [%s]".formatted( translated("map_editor"), editedWorldMap().url() );
                }
                return "%s: [%s: %d rows %d cols]".formatted(
                        translated("map_editor"), translated("unsaved_map"),
                        editedWorldMap().numRows(), editedWorldMap().numCols() );
            }, currentFile, editedWorldMap
        );
    }

    private void createMenuBarAndMenus() {
    }

    // also called from EditorPage
    public void addLoadMapMenuItem(String description, WorldMap map) {
        requireNonNull(description);
        requireNonNull(map);
        var miLoadMap = new MenuItem(description);
        miLoadMap.setOnAction(e -> loadMap(map));
        menuBar.getLoadMapMenu().getItems().add(miLoadMap);
    }

    public void loadMap(WorldMap worldMap) {
        executeWithCheckForUnsavedChanges(() -> {
            setEditedWorldMap(WorldMap.copyMap(worldMap));
            currentFile.set(null);
        });
    }

    void showNewMapDialog(boolean preconfigured) {
        executeWithCheckForUnsavedChanges(() -> {
            var dialog = new TextInputDialog("28x36");
            dialog.setTitle(translated("new_dialog.title"));
            dialog.setHeaderText(translated("new_dialog.header_text"));
            dialog.setContentText(translated("new_dialog.content_text"));
            dialog.showAndWait().ifPresent(text -> {
                Vector2i sizeInTiles = parseSize(text);
                if (sizeInTiles == null) {
                    showMessage("Map size not recognized", 2, MessageType.ERROR);
                }
                else if (sizeInTiles.y() < 6) {
                    showMessage("Map must have at least 6 rows", 2, MessageType.ERROR);
                }
                else {
                    if (preconfigured) {
                        setPreconfiguredMap(sizeInTiles.x(), sizeInTiles.y());
                    } else {
                        setBlankMap(sizeInTiles.x(), sizeInTiles.y());
                    }
                    currentFile.set(null);
                }
            });
        });
    }

    private Vector2i parseSize(String cols_x_rows) {
        String[] tuple = cols_x_rows.split("x");
        if (tuple.length != 2) {
            showMessage("Map size must be given as cols x rows", 2, MessageType.ERROR);
            return null;
        }
        try {
            int numCols = Integer.parseInt(tuple[0].trim());
            int numRows = Integer.parseInt(tuple[1].trim());
            return new Vector2i(numCols, numRows);
        } catch (Exception x) {
            showMessage("Map size must be given as cols x rows", 2, MessageType.ERROR);
            return null;
        }
    }

    void openMapFileInteractively() {
        executeWithCheckForUnsavedChanges(() -> {
            fileChooser.setTitle(translated("open_file"));
            fileChooser.setInitialDirectory(currentDirectory);
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                readMapFile(file);
            }
            changeManager.setEdited(false);
        });
    }

    private boolean readMapFile(File file) {
        if (file.getName().endsWith(".world")) {
            try {
                loadMap(WorldMap.fromFile(file));
                currentDirectory = file.getParentFile();
                currentFile.set(file);
                Logger.info("Map read from file {}", file);
                return true;
            } catch (IOException x) {
                Logger.error(x);
                Logger.info("Map could not be read from file {}", file);
                return false;
            }
        }
        return false;
    }

    private Optional<File> selectMapFileInDirectoryFollowing(boolean forward) {
        File currentFile = this.currentFile.get();
        if (currentFile == null) {
            return Optional.empty();
        }
        File dir = currentFile.getParentFile();
        if (dir == null) {
            Logger.error("Cannot load next map file for {}, parent is NULL", currentFile);
            return Optional.empty();
        }
        File[] mapFiles = dir.listFiles((folder, name) -> name.endsWith(".world"));
        if (mapFiles == null) {
            Logger.warn("No map files found in directory {}", dir);
            return Optional.empty();
        }
        Arrays.sort(mapFiles);
        int index = Arrays.binarySearch(mapFiles, currentFile);
        if (0 <= index && index < mapFiles.length) {
            int next;
            if (forward) {
                next = index == mapFiles.length - 1 ? 0 : index + 1;
            } else {
                next = index > 0 ? index - 1: mapFiles.length - 1;
            }
            File nextFile = mapFiles[next];
            return Optional.of(nextFile);
        }
        return Optional.empty();
    }

    public void showSaveDialog() {
        fileChooser.setTitle(translated("save_file"));
        fileChooser.setInitialDirectory(currentDirectory);
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            currentDirectory = file.getParentFile();
            if (file.getName().endsWith(".world")) {
                boolean saved = saveWorldMap(editedWorldMap(), file);
                if (saved) {
                    changeManager.setEdited(false);
                    readMapFile(file);
                    showMessage("Map saved as '%s'".formatted(file.getName()), 3, MessageType.INFO);
                } else {
                    showMessage("Map could not be saved!", 4, MessageType.ERROR);
                }
            } else {
                Logger.error("No .world file selected");
                showMessage("No .world file selected", 2, MessageType.WARNING);
            }
        }
    }

    private boolean saveWorldMap(WorldMap worldMap,File file) {
        try (PrintWriter pw = new PrintWriter(file, StandardCharsets.UTF_8)) {
            pw.print(WorldMapFormatter.formatted(worldMap));
            return true;
        } catch (IOException x) {
            Logger.error(x);
            return false;
        }
    }

    public void executeWithCheckForUnsavedChanges(Runnable action) {
        if (!changeManager.isEdited()) {
            action.run();
            return;
        }
        var confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle(translated("save_dialog.title"));
        confirmationDialog.setHeaderText(translated("save_dialog.header_text"));
        confirmationDialog.setContentText(translated("save_dialog.content_text"));
        var choiceSave   = new ButtonType(translated("save_changes"));
        var choiceNoSave = new ButtonType(translated("no_save_changes"));
        var choiceCancel = new ButtonType(translated("cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmationDialog.getButtonTypes().setAll(choiceSave, choiceNoSave, choiceCancel);
        confirmationDialog.showAndWait().ifPresent(choice -> {
            if (choice == choiceSave) {
                showSaveDialog();
                action.run();
            } else if (choice == choiceNoSave) {
                changeManager.setEdited(false);
                action.run();
            } else if (choice == choiceCancel) {
                confirmationDialog.close();
            }
        });
    }

    private void updateSourceView() {
        StringBuilder sb = new StringBuilder();
        String[] sourceTextLines = WorldMapFormatter.formatted(editedWorldMap()).split("\n");
        for (int i = 0; i < sourceTextLines.length; ++i) {
            sb.append("%5d: ".formatted(i + 1)).append(sourceTextLines[i]).append("\n");
        }
        sourceView.setText(sb.toString());
    }

    private void onEditModeChanged(EditMode editMode) {
        clearMessage();
        showEditHelpText();
        switch (editMode) {
            case INSPECT -> editCanvas.enterInspectMode();
            case EDIT    -> editCanvas.enterEditMode();
            case ERASE   -> editCanvas.enterEraseMode();
        }
    }

    private void zoomIn() {
        if (gridSize() < TileMapEditor.MAX_GRID_SIZE) {
            gridSize.set(gridSize() + 1);
        }
    }

    private void zoomOut() {
        if (gridSize() > TileMapEditor.MIN_GRID_SIZE) {
            gridSize.set(gridSize() - 1);
        }
    }

    //
    // Drawing
    //

    private void draw(TerrainMapColorScheme colorScheme) {
        try {
            Logger.trace("Draw palette");
            palettes[selectedPaletteID()].draw();
        } catch (Exception x) {
            Logger.error(x);
        }
        if (tabEditCanvas.isSelected()) {
            try {
                Logger.trace("Draw edit canvas");
                editCanvas.draw(this, colorScheme);
            } catch (Exception x) {
                Logger.error(x);
            }
        }
        if (tabTemplateImage.isSelected()) {
            try {
                Logger.trace("Draw template image");
                templateImageCanvas.draw();
            } catch (Exception x) {
                Logger.error(x);
            }
        }
        if (tabPreview2D.isSelected()) {
            try {
                Logger.trace("Draw preview 2D");
                editorMazePreview2D.draw(editedWorldMap(), colorScheme);
            } catch (Exception x) {
                Logger.error(x);
            }
        }
    }

    // Controller part

    private void onKeyPressed(KeyEvent e) {
        KeyCode key = e.getCode();
        boolean alt = e.isAltDown();

        if (alt && key == KeyCode.LEFT) {
            selectMapFileInDirectoryFollowing(false).ifPresent(
                file -> {
                    if (!readMapFile(file)) {
                        showMessage("Map file %s could not be loaded".formatted(file.getName()), 3, MessageType.ERROR);
                    }
                });
        } else if (alt && key == KeyCode.RIGHT) {
            selectMapFileInDirectoryFollowing(true).ifPresent(
                file -> {
                    if (!readMapFile(file)) {
                        showMessage("Map file %s could not be loaded".formatted(file.getName()), 3, MessageType.ERROR);
                    }
                });
        }
        else if (key == KeyCode.PLUS) {
            zoomIn();
        }
        else if (key == KeyCode.MINUS) {
            zoomOut();
        }
    }

    private void onKeyTyped(KeyEvent e) {
        String key = e.getCharacter();
        switch (key) {
            case "i" -> setEditMode(EditMode.INSPECT);
            case "e" -> {
                setEditMode(EditMode.EDIT);
                setSymmetricEditMode(false);
            }
            case "s" -> {
                setEditMode(EditMode.EDIT);
                setSymmetricEditMode(true);
            }
            case "x" -> setEditMode(EditMode.ERASE);
        }
    }

    public void editAtMousePosition(double x, double y, boolean erase) {
        Vector2i tile = tileAtMousePosition(x, y, gridSize());
        if (isEditMode(EditMode.INSPECT)) {
            identifyObstacleAtTile(tile);
            return;
        }
        switch (selectedPaletteID()) {
            case TileMapEditor.PALETTE_ID_TERRAIN -> editTerrainAtTile(tile, erase);
            case TileMapEditor.PALETTE_ID_FOOD -> editFoodAtTile(tile, erase);
            case TileMapEditor.PALETTE_ID_ACTORS -> {
                if (selectedPalette().isToolSelected()) {
                    selectedPalette().selectedTool().apply(this, LayerID.TERRAIN, tile);
                    changeManager.setTerrainMapChanged();
                    changeManager.setEdited(true);
                }
            }
            default -> Logger.error("Unknown palette ID " + selectedPaletteID());
        }
    }

    private void editTerrainAtTile(Vector2i tile, boolean erase) {
        if (erase) {
            clearTerrainTileValue(tile);
        } else if (selectedPalette().isToolSelected()) {
            selectedPalette().selectedTool().apply(this, LayerID.TERRAIN, tile);
        }
        changeManager.setTerrainMapChanged();
        changeManager.setEdited(true);
    }

    private void editFoodAtTile(Vector2i tile, boolean erase) {
        if (erase) {
            clearFoodTileValue(tile);
        } else if (selectedPalette().isToolSelected()) {
            selectedPalette().selectedTool().apply(this, LayerID.FOOD, tile);
        }
        changeManager.setFoodMapChanged();
        changeManager.setEdited(true);
    }

    public void moveCursorAndSetFoodAtTile(Direction dir) {
        if (editCanvas.moveCursor(dir, this::hasAccessibleTerrainAtTile)) {
            setFoodAtFocussedTile();
        }
    }

    private void setFoodAtFocussedTile() {
        if (editMode() == EditMode.EDIT && selectedPaletteID() == PALETTE_ID_FOOD) {
            if (hasAccessibleTerrainAtTile(editCanvas.focussedTile())) {
                editFoodAtTile(editCanvas.focussedTile(), false);
            }
        }
    }

    public void selectNextPaletteEntry() {
        Palette palette = selectedPalette();
        int next = palette.selectedIndex() + 1;
        if (next == palette.numTools()) { next = 0; }
        palette.selectTool(next);
    }

    private void identifyObstacleAtTile(Vector2i tile) {
        Obstacle obstacleAtTile = editedWorldMap().obstacles().stream()
            .filter(obstacle -> tileAt(obstacle.startPoint().minus(HTS, 0).toVector2f()).equals(tile))
            .findFirst().orElse(null);
        if (obstacleAtTile != null) {
            String encoding = obstacleAtTile.encoding();
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(encoding);
            clipboard.setContent(content);
            showMessage("Obstacle (copied to clipboard)", 5, MessageType.INFO);
        } else {
            byte terrainValue = editedWorldMap().content(LayerID.TERRAIN, tile);
            byte foodValue = editedWorldMap().content(LayerID.FOOD, tile);
            String info = "";
            if (terrainValue != TerrainTile.EMPTY.$)
                info = "Terrain #%02X (%s)".formatted(terrainValue, terrainValue);
            if (foodValue != FoodTile.EMPTY.code())
                info = "Food #%02X (%s)".formatted(foodValue, foodValue);
            showMessage(info, 4, MessageType.INFO);
        }
    }

    /**
     * This method should be used whenever a tile value has to be set and symmetric editing should be executed.
     */
    public void setTileValueRespectSymmetry(WorldMap worldMap, LayerID layerID, Vector2i tile, byte value) {
        requireNonNull(worldMap);
        requireNonNull(layerID);
        requireNonNull(tile);

        if (layerID == LayerID.FOOD && !canEditFoodAtTile(tile)) {
            return;
        }

        worldMap.setContent(layerID, tile, value);
        if (layerID == LayerID.TERRAIN) {
            worldMap.setContent(LayerID.FOOD, tile, FoodTile.EMPTY.code());
        }

        changeManager.setEdited(true);
        changeManager.setWorldMapChanged();

        if (isSymmetricEditMode()) {
            Vector2i mirroredTile = worldMap.mirroredTile(tile);
            if (layerID == LayerID.FOOD) {
                if (canEditFoodAtTile(mirroredTile)) {
                    worldMap.setContent(layerID, mirroredTile, value);
                }
            } else {
                byte mirroredValue = mirroredTileValue(value);
                worldMap.setContent(layerID, mirroredTile, mirroredValue);
                worldMap.setContent(LayerID.FOOD, mirroredTile, FoodTile.EMPTY.code());
            }
        }
    }

    public void setTileValueRespectSymmetry(WorldMap worldMap, LayerID layerID, int row, int col, byte value) {
        Vector2i tile = new Vector2i(col, row);
        setTileValueRespectSymmetry(worldMap, layerID, tile, value);
    }


    // ignores symmetric edit mode!
    public void clearTerrainTileValue(Vector2i tile) {
        editedWorldMap().setContent(LayerID.TERRAIN, tile, TerrainTile.EMPTY.$);
        changeManager.setTerrainMapChanged();
        changeManager.setEdited(true);
    }

    // ignores symmetric edit mode!
    public void clearFoodTileValue(Vector2i tile) {
        editedWorldMap().setContent(LayerID.FOOD, tile, FoodTile.EMPTY.code());
        changeManager.setFoodMapChanged();
        changeManager.setEdited(true);
    }

    private void setBlankMap(int tilesX, int tilesY) {
        var blankMap = WorldMap.emptyMap(tilesY, tilesX);
        setDefaultColors(blankMap);
        setDefaultScatterPositions(blankMap);
        setEditedWorldMap(blankMap);
    }

    private void setPreconfiguredMap(int tilesX, int tilesY) {
        var worldMap = WorldMap.emptyMap(tilesY, tilesX);
        addBorderWall(worldMap);
        setDefaultScatterPositions(worldMap);
        if (worldMap.numRows() >= 20) {
            Vector2i houseMinTile = Vector2i.of(tilesX / 2 - 4, tilesY / 2 - 3);
            placeArcadeHouse(worldMap, houseMinTile);
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_PAC,   WorldMapFormatter.formatTile(houseMinTile.plus(3, 11)));
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_BONUS, WorldMapFormatter.formatTile(houseMinTile.plus(3, 5)));
        }
        worldMap.buildObstacleList();
        setDefaultColors(worldMap);
        setEditedWorldMap(worldMap);
    }

    void setDefaultColors(WorldMap worldMap) {
        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.COLOR_WALL_STROKE, MS_PACMAN_COLOR_WALL_STROKE);
        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.COLOR_WALL_FILL, MS_PACMAN_COLOR_WALL_FILL);
        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.COLOR_DOOR, MS_PACMAN_COLOR_DOOR);
        worldMap.properties(LayerID.FOOD).put(WorldMapProperty.COLOR_FOOD, MS_PACMAN_COLOR_FOOD);
        changeManager.setTerrainMapChanged();
        changeManager.setFoodMapChanged();
    }

    private void setDefaultScatterPositions(WorldMap worldMap) {
        int numCols = worldMap.numCols(), numRows = worldMap.numRows();
        if (numCols >= 3 && numRows >= 2) {
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_SCATTER_RED_GHOST,    WorldMapFormatter.formatTile(Vector2i.of(numCols - 3, 0)));
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_SCATTER_PINK_GHOST,   WorldMapFormatter.formatTile(Vector2i.of(2, 0)));
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_SCATTER_CYAN_GHOST,   WorldMapFormatter.formatTile(Vector2i.of(numCols - 1, numRows - 2)));
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_SCATTER_ORANGE_GHOST, WorldMapFormatter.formatTile(Vector2i.of(0, numRows - 2)));
            changeManager.setTerrainMapChanged();
        }
    }

    void addBorderWall(WorldMap worldMap) {
        int lastRow = worldMap.numRows() - 1 - EMPTY_ROWS_BELOW_MAZE, lastCol = worldMap.numCols() - 1;
        setTileValueRespectSymmetry(worldMap, LayerID.TERRAIN, EMPTY_ROWS_BEFORE_MAZE, 0, TerrainTile.ARC_NW.$);
        setTileValueRespectSymmetry(worldMap, LayerID.TERRAIN, EMPTY_ROWS_BEFORE_MAZE, lastCol, TerrainTile.ARC_NE.$);
        setTileValueRespectSymmetry(worldMap, LayerID.TERRAIN, lastRow, 0, TerrainTile.ARC_SW.$);
        setTileValueRespectSymmetry(worldMap, LayerID.TERRAIN, lastRow, lastCol, TerrainTile.ARC_SE.$);
        for (int row = EMPTY_ROWS_BEFORE_MAZE + 1; row < lastRow; ++row) {
            setTileValueRespectSymmetry(worldMap, LayerID.TERRAIN, row, 0, TerrainTile.WALL_V.$);
            setTileValueRespectSymmetry(worldMap, LayerID.TERRAIN, row, lastCol, TerrainTile.WALL_V.$);
        }
        for (int col = 1; col < lastCol; ++col) {
            setTileValueRespectSymmetry(worldMap, LayerID.TERRAIN, EMPTY_ROWS_BEFORE_MAZE, col, TerrainTile.WALL_H.$);
            setTileValueRespectSymmetry(worldMap, LayerID.TERRAIN, lastRow, col, TerrainTile.WALL_H.$);
        }
        changeManager.setTerrainMapChanged();
    }

    void addArcadeHouseAtMapCenter(WorldMap worldMap) {
        int numRows = worldMap.numRows(), numCols = worldMap.numCols();
        int houseMinX = numCols / 2 - 4, houseMinY = numRows / 2 - 3;
        placeArcadeHouse(worldMap, Vector2i.of(houseMinX, houseMinY));
    }

    public void placeArcadeHouse(WorldMap worldMap, Vector2i houseMinTile) {
        Vector2i houseMaxTile = houseMinTile.plus(7, 4);

        Vector2i oldHouseMinTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MIN_TILE);
        Vector2i oldHouseMaxTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MAX_TILE);
        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_HOUSE_MIN_TILE, WorldMapFormatter.formatTile(houseMinTile));
        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_HOUSE_MAX_TILE, WorldMapFormatter.formatTile(houseMaxTile));

        // clear tiles where house walls/doors were located (created at runtime!)
        if (oldHouseMinTile != null && oldHouseMaxTile != null) {
            clearTerrainAreaOneSided(worldMap, oldHouseMinTile, oldHouseMaxTile);
            clearFoodAreaOneSided(worldMap, oldHouseMinTile, oldHouseMaxTile);
        }
        // clear new house area
        clearTerrainAreaOneSided(worldMap, houseMinTile, houseMaxTile);
        clearFoodAreaOneSided(worldMap, houseMinTile, houseMaxTile);

        // place house tile content
        Vector2i houseSize = houseMaxTile.minus(houseMinTile).plus(1,1);
        for (int y = 0; y < houseSize.y(); ++y) {
            for (int x = 0; x < houseSize.x(); ++x) {
                worldMap.setContent(LayerID.TERRAIN, houseMinTile.y() + y, houseMinTile.x() + x, DEFAULT_HOUSE_ROWS[y][x]);
            }
        }

        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_RED_GHOST,      WorldMapFormatter.formatTile(houseMinTile.plus(3, -1)));
        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_CYAN_GHOST,     WorldMapFormatter.formatTile(houseMinTile.plus(1, 2)));
        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_PINK_GHOST,     WorldMapFormatter.formatTile(houseMinTile.plus(3, 2)));
        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_ORANGE_GHOST,   WorldMapFormatter.formatTile(houseMinTile.plus(5, 2)));

        // clear pellets around house
        Vector2i minAround = houseMinTile.minus(1,1);
        Vector2i maxAround = houseMaxTile.plus(1,1);
        for (int x = minAround.x(); x <= maxAround.x(); ++x) {
            // Note: parameters are row and col (y and x)
            if (x >= 0) {
                worldMap.setContent(LayerID.FOOD, minAround.y(), x, FoodTile.EMPTY.code());
                worldMap.setContent(LayerID.FOOD, maxAround.y(), x, FoodTile.EMPTY.code());
            }
        }
        for (int y = minAround.y(); y <= maxAround.y(); ++y) {
            // Note: parameters are row and col (y and x)
            worldMap.setContent(LayerID.FOOD, y, minAround.x(), FoodTile.EMPTY.code());
            worldMap.setContent(LayerID.FOOD, y, maxAround.x(), FoodTile.EMPTY.code());
        }

        changeManager.setWorldMapChanged();
        changeManager.setEdited(true);
    }

    private void clearTerrainAreaOneSided(WorldMap worldMap, Vector2i minTile, Vector2i maxTile) {
        for (int row = minTile.y(); row <= maxTile.y(); ++row) {
            for (int col = minTile.x(); col <= maxTile.x(); ++col) {
                // No symmetric editing!
                worldMap.setContent(LayerID.TERRAIN, row, col, TerrainTile.EMPTY.$);
            }
        }
        changeManager.setTerrainMapChanged();
        changeManager.setEdited(true);
    }

    private void clearFoodAreaOneSided(WorldMap worldMap, Vector2i minTile, Vector2i maxTile) {
        for (int row = minTile.y(); row <= maxTile.y(); ++row) {
            for (int col = minTile.x(); col <= maxTile.x(); ++col) {
                // No symmetric editing!
                worldMap.setContent(LayerID.FOOD, row, col, FoodTile.EMPTY.code());
            }
        }
        changeManager.setFoodMapChanged();
        changeManager.setEdited(true);
    }

    private boolean hasTemplateImageSize(Image image) {
        return image.getHeight() % TS == 0 && image.getWidth() % TS == 0;
    }

    private boolean setBlankMapForTemplateImage(Image templateImage) {
        if (!hasTemplateImageSize(templateImage)) {
            showMessage("Template image size seems dubious", 3, MessageType.WARNING);
            return false;
        }
        int tilesX = (int) (templateImage.getWidth() / TS);
        int tilesY = EMPTY_ROWS_BEFORE_MAZE + EMPTY_ROWS_BELOW_MAZE + (int) (templateImage.getHeight() / TS);
        setBlankMap(tilesX, tilesY);
        removeTerrainMapProperty(WorldMapProperty.COLOR_WALL_FILL);
        removeTerrainMapProperty(WorldMapProperty.COLOR_WALL_STROKE);
        removeTerrainMapProperty(WorldMapProperty.COLOR_DOOR);
        removeFoodMapProperty(WorldMapProperty.COLOR_FOOD);
        templateImagePy.set(templateImage);
        return true;
    }

    void initWorldMapForTemplateImage() {
        selectImageFile(translated("open_template_image")).ifPresent(file -> readImageFromFile(file).ifPresentOrElse(image -> {
            if (setBlankMapForTemplateImage(image)) {
                showMessage("Select map colors from template!", 20, MessageType.INFO);
            }
        }, () -> showMessage("Could not open image file", 3, MessageType.ERROR)));
    }

    private Optional<File> selectImageFile(String title) {
        FileChooser selector = new FileChooser();
        selector.setTitle(title);
        selector.setInitialDirectory(currentDirectory);
        selector.getExtensionFilters().addAll(FILTER_IMAGE_FILES, FILTER_ALL_FILES);
        selector.setSelectedExtensionFilter(FILTER_IMAGE_FILES);
        return Optional.ofNullable(selector.showOpenDialog(stage));
    }

    private Optional<Image> readImageFromFile(File file) {
        try (FileInputStream stream = new FileInputStream(file)) {
            return Optional.of(new Image(stream));
        } catch (IOException x) {
            Logger.error(x);
            return Optional.empty();
        }
    }

    void closeTemplateImage() {
        templateImagePy.set(null);
    }

    private boolean hasAccessibleTerrainAtTile(Vector2i tile) {
        byte value = editedWorldMap().content(LayerID.TERRAIN, tile);
        return value == TerrainTile.EMPTY.$
            || value == TerrainTile.ONE_WAY_DOWN.$
            || value == TerrainTile.ONE_WAY_UP.$
            || value == TerrainTile.ONE_WAY_LEFT.$
            || value == TerrainTile.ONE_WAY_RIGHT.$;
    }

    public boolean canEditFoodAtTile(Vector2i tile) {
        return !editedWorldMap().outOfWorld(tile)
                && tile.y() >= EMPTY_ROWS_BEFORE_MAZE
                && tile.y() < editedWorldMap().numRows() - EMPTY_ROWS_BELOW_MAZE
                && !isPartOfHouse(editedWorldMap(), tile)
                && hasAccessibleTerrainAtTile(tile);
    }


    private boolean isPartOfHouse(WorldMap worldMap, Vector2i tile) {
        Vector2i minTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MIN_TILE);
        Vector2i maxTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MAX_TILE);
        if (minTile != null && maxTile != null) {
            return minTile.x() <= tile.x() && tile.x() <= maxTile.x()
                && minTile.y() <= tile.y() && tile.y() <= maxTile.y();
        }
        return false;
    }

    void populateMapFromTemplateImage(WorldMap worldMap) {
        Image templateImage = templateImagePy.get();
        if (templateImage == null) {
            return;
        }

        Color fillColor = getColorFromMap(worldMap, LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_FILL, null);
        if (fillColor == null) {
            showMessage("No fill color defined", 3, MessageType.ERROR);
            return;
        }
        Color strokeColor = getColorFromMap(worldMap, LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_STROKE, null);
        if (strokeColor == null) {
            showMessage("No stroke color defined", 3, MessageType.ERROR);
            return;
        }
        Color doorColor = getColorFromMap(worldMap, LayerID.TERRAIN, WorldMapProperty.COLOR_DOOR, Color.PINK);
        if (doorColor == null) {
            showMessage("No door color defined", 3, MessageType.ERROR);
            return;
        }
        Color foodColor = getColorFromMap(worldMap, LayerID.FOOD, WorldMapProperty.COLOR_FOOD, null);
        if (foodColor == null) {
            showMessage("No food color defined", 3, MessageType.ERROR);
            return;
        }

        TileMatcher matcher = new TileMatcher(Color.TRANSPARENT, fillColor, strokeColor, doorColor, foodColor);
        WritablePixelFormat<IntBuffer> pixelFormat = WritablePixelFormat.getIntArgbInstance();
        PixelReader rdr = templateImage.getPixelReader();
        if (rdr == null) {
            showMessage("Could not get pixel reader for this image", 5, MessageType.ERROR);
            return;
        }

        LocalTime startTime = LocalTime.now();

        int numMazeRows = worldMap.numRows() - (EMPTY_ROWS_BEFORE_MAZE + EMPTY_ROWS_BELOW_MAZE);
        int numMazeCols = worldMap.numCols();
        for (int row = 0; row < numMazeRows; ++row) {
            for (int col = 0; col < numMazeCols; ++col) {
                Vector2i worldMapTile = Vector2i.of(col, row + EMPTY_ROWS_BEFORE_MAZE);
                try {
                    int[] pixelsOfTile = new int[TS*TS]; // pixels row-wise
                    rdr.getPixels(col * TS, row * TS, TS, TS, pixelFormat, pixelsOfTile, 0, TS);
                    byte foodValue = matcher.matchFoodTile(pixelsOfTile);
                    if (foodValue == FoodTile.PELLET.code() || foodValue == FoodTile.ENERGIZER.code()) {
                        worldMap.setContent(LayerID.FOOD, worldMapTile, foodValue);
                    } else {
                        byte terrainValue = matcher.matchTerrainTile(pixelsOfTile);
                        worldMap.setContent(LayerID.TERRAIN, worldMapTile, terrainValue);
                    }
                } catch (IndexOutOfBoundsException e) {
                    Logger.error("Could not get pixels for tile {}, maybe image has been cropped incorrectly?", worldMapTile);
                } catch (Exception e) {
                    Logger.error("Could not get pixels for tile {}", worldMapTile);
                    Logger.error(e);
                }
            }
        }

        // Find house: requires that at least min and max tiles have been detected
        Vector2i houseMinTile = worldMap.tiles()
            .filter(tile -> worldMap.content(LayerID.TERRAIN, tile) == TerrainTile.DARC_NW.$)
            .findFirst().orElse(null);

        Vector2i houseMaxTile = worldMap.tiles()
            .filter(tile -> worldMap.content(LayerID.TERRAIN, tile) == TerrainTile.DARC_SE.$)
            .findFirst().orElse(null);

        if (houseMinTile != null && houseMaxTile != null
                && houseMinTile.x() < houseMaxTile.x() && houseMinTile.y() < houseMaxTile.y()) {
            placeArcadeHouse(worldMap, houseMinTile);
        }

        java.time.Duration duration = java.time.Duration.between(startTime, LocalTime.now());
        showMessage("Map creation took %d milliseconds".formatted(duration.toMillis()), 5, MessageType.INFO);

        changeManager.setWorldMapChanged();
        changeManager.setEdited(true);
    }


    // Sample maps loading

    static final String SAMPLE_MAPS_PATH = "/de/amr/pacmanfx/tilemap/editor/maps/";

    private WorldMap mapPacManGame;
    private List<WorldMap> mapsMsPacManGame;
    private List<WorldMap> mapsPacManXXLGame;

    private void loadSampleMapsAndAddMenuEntries() {
        try {
            loadSampleMaps();
            addLoadMapMenuItem("Pac-Man", mapPacManGame);
            menuBar.getLoadMapMenu().getItems().add(new SeparatorMenuItem());
            rangeClosed(1, 6).forEach(num -> addLoadMapMenuItem("Ms. Pac-Man " + num, mapsMsPacManGame.get(num - 1)));
            menuBar.getLoadMapMenu().getItems().add(new SeparatorMenuItem());
            rangeClosed(1, 8).forEach(num -> addLoadMapMenuItem("Pac-Man XXL " + num, mapsPacManXXLGame.get(num - 1)));
        } catch (IOException x) {
            Logger.error("Could not load sample maps");
            Logger.error(x);
        }
    }

    private void loadSampleMaps() throws IOException {
        mapPacManGame = WorldMap.fromURL(sampleMapURL("pacman/pacman.world", 1));
        mapsMsPacManGame = new ArrayList<>();
        for (int n = 1; n <= 6; ++n) {
            mapsMsPacManGame.add(WorldMap.fromURL(sampleMapURL("mspacman/mspacman_%d.world", n)));
        }
        mapsPacManXXLGame = new ArrayList<>();
        for (int n = 1; n <= 8; ++n) {
            mapsPacManXXLGame.add(WorldMap.fromURL(sampleMapURL("pacman_xxl/masonic_%d.world", n)));
        }
    }

    private URL sampleMapURL(String namePattern, int number) {
        return getClass().getResource(SAMPLE_MAPS_PATH + namePattern.formatted(number));
    }
}