/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.*;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.tilemap.editor.actions.*;
import de.amr.pacmanfx.tilemap.editor.rendering.TerrainTileMapRenderer;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import de.amr.pacmanfx.uilib.tilemap.FoodMapRenderer;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapColorScheme;
import javafx.animation.AnimationTimer;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.IntBuffer;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.*;
import static de.amr.pacmanfx.tilemap.editor.TemplateImageManager.isTemplateImageSizeOk;
import static de.amr.pacmanfx.tilemap.editor.TemplateImageManager.selectTemplateImage;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.*;
import static de.amr.pacmanfx.tilemap.editor.rendering.ArcadeSprites.*;
import static java.util.Objects.requireNonNull;

public class TileMapEditor {

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
                tilesWithErrors.addAll(currentWorldMap().buildObstacleList());
                obstaclesUpToDate = true;
                requestRedraw();
            }
            if (terrainMapChanged) {
                if (terrainMapPropertiesEditor != null) {
                    terrainMapPropertiesEditor.setTileMap(currentWorldMap(), LayerID.TERRAIN);
                }
                mazePreview3D.updateTerrain();
                updateSourceCode();
                terrainMapChanged = false;
                Logger.trace("Terrain map updated");
                requestRedraw();
            }
            if (foodMapChanged) {
                if (foodMapPropertiesEditor != null) {
                    foodMapPropertiesEditor.setTileMap(currentWorldMap(), LayerID.FOOD);
                }
                mazePreview3D.updateFood();
                updateSourceCode();
                foodMapChanged = false;
                Logger.trace("Food map updated");
                requestRedraw();
            }
        }
    }

    private final ChangeManager changeManager = new ChangeManager();
    private final MessageManager messageManager = new MessageManager();
    private final UpdateTimer updateTimer;

    private final List<Vector2i> tilesWithErrors = new ArrayList<>();

    private final BorderPane contentPane = new BorderPane();
    private final Stage stage;
    private Pane propertyEditorsPane;
    private EditCanvas editCanvas;
    private ScrollPane spEditCanvas;
    private ScrollPane spPreview2D;
    private EditorMazePreview2D preview2D;
    private TextArea sourceView;
    private ScrollPane spTemplateImage;
    private Pane templateImageDropTarget;
    private SplitPane splitPaneEditorAndPreviews;
    private TabPane tabPaneWithPalettes;
    private Slider sliderZoom;
    private HBox statusLine;
    private TabPane tabPaneEditorViews;
    private Tab tabEditCanvas;
    private Tab tabTemplateImage;
    private TemplateImageCanvas templateImageCanvas;
    private Tab tabPreview2D;
    private EditorMazePreview3D mazePreview3D;

    private final EditorMenuBar menuBar;
    private final Palette[] palettes = new Palette[3];
    private PropertyEditorPane terrainMapPropertiesEditor;
    private PropertyEditorPane foodMapPropertiesEditor;

    private final Model3DRepository model3DRepository;

    private class UpdateTimer extends AnimationTimer {
        @Override
        public void handle(long now) {
            messageManager.update();
            changeManager.processChanges();
            if (changeManager.isRedrawRequested()) {
                try {
                    drawUI();
                } catch (Exception x) {
                    Logger.error(x);
                }
            }
        }
    }

    public TileMapEditor(Stage stage, Model3DRepository model3DRepository) {
        this.stage = requireNonNull(stage);
        this.model3DRepository = requireNonNull(model3DRepository);
        this.menuBar = new EditorMenuBar(this);

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
        createZoomSlider();
        createStatusLine();

        arrangeMainLayout();

        loadSampleMapsAndUpdateMenu(menuBar.menuMaps());

        contentPane.setOnKeyTyped(this::onKeyTyped);
        contentPane.setOnKeyPressed(this::onKeyPressed);

        updateTimer = new UpdateTimer();
    }

    public void init(File workDir) {
        setCurrentDirectory(workDir);
        setCurrentWorldMap(createEmptyMap(28, 36));
        setEditMode(EditMode.INSPECT);
        mazePreview3D.reset();
    }

    public void start(Stage stage) {
        title.bind(createTitleBinding());
        stage.titleProperty().bind(title);
        contentPane.setLeft(null); // no properties editor
        showEditHelpText();
        updateTimer.start();
    }

    public void stop() {
        updateTimer.stop();
        setEditMode(EditMode.INSPECT);
    }

    // -- actorsVisible

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

    public boolean actorsVisible() {
        return actorsVisible == null ? DEFAULT_ACTORS_VISIBLE : actorsVisibleProperty().get();
    }

    public void setActorsVisible(boolean visible) {
        actorsVisibleProperty().set(visible);
    }

    // -- currentDirectory

    private final ObjectProperty<File> currentDirectory = new SimpleObjectProperty<>();

    public File currentDirectory() {
        return currentDirectory.get();
    }

    public void setCurrentDirectory(File dir) {
        currentDirectory.set(dir);
    }

    // -- currentFile

    private final ObjectProperty<File> currentFile = new SimpleObjectProperty<>();

    public ObjectProperty<File> currentFileProperty() {
        return currentFile;
    }

    public void setCurrentFile(File file) {
        currentFile.set(file);
    }

    public File currentFile() {
        return currentFile.get();
    }

    // -- currentWorldMap

    private final ObjectProperty<WorldMap> currentWorldMap = new SimpleObjectProperty<>(WorldMap.emptyMap(28, 36)) {
        @Override
        protected void invalidated() {
            changeManager.setWorldMapChanged();
        }
    };

    public ObjectProperty<WorldMap> currentWorldMapProperty() { return currentWorldMap; }

    public WorldMap currentWorldMap() { return currentWorldMap.get(); }

    public void setCurrentWorldMap(WorldMap worldMap) { currentWorldMap.set(worldMap); }

    // -- editMode

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

    public void setEditMode(EditMode mode) {
        editModeProperty().set(requireNonNull(mode));
    }

    public boolean editModeIs(EditMode mode) { return editMode() == mode; }

    // -- gridSize

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

    // -- foodVisible

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

    public boolean foodVisible() {
        return foodVisible == null ? DEFAULT_FOOD_VISIBLE : foodVisible.get();
    }

    public void setFoodVisible(boolean visible) {
        foodVisibleProperty().set(visible);
    }

    // -- gridVisible

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

    // -- mapPropertyEditorsVisible

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

    public boolean mapPropertyEditorsVisible() {
        return mapPropertyEditorsVisible == null ? DEFAULT_MAP_PROPERTY_EDITORS_VISIBLE : propertyEditorsVisibleProperty().get();
    }

    public void setMapPropertyEditorsVisible(boolean value) {
        propertyEditorsVisibleProperty().set(value);
    }

    // -- obstacleInnerAreaDisplayed

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

    public boolean obstacleInnerAreaDisplayed() {
        return obstacleInnerAreaDisplayed == null ? DEFAULT_OBSTACLE_INNER_AREA_DISPLAYED :obstacleInnerAreaDisplayedProperty().get();
    }

    public void setObstacleInnerAreaDisplayed(boolean value) {
        obstacleInnerAreaDisplayedProperty().set(value);
    }

    // -- obstaclesJoining

    public static boolean DEFAULT_OBSTACLES_JOINING = true;

    private BooleanProperty obstaclesJoining;

    public BooleanProperty obstaclesJoiningProperty() {
        if (obstaclesJoining == null) {
            obstaclesJoining = new SimpleBooleanProperty(DEFAULT_OBSTACLES_JOINING);
        }
        return obstaclesJoining;
    }

    public boolean obstaclesJoining() {
        return obstaclesJoining == null ? DEFAULT_OBSTACLES_JOINING : obstaclesJoiningProperty().get();
    }

    public void setObstaclesJoining(boolean value) {
        obstaclesJoiningProperty().set(value);
    }

    // -- segmentNumbersVisible

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

    public boolean segmentNumbersVisible() {
        return segmentNumbersVisible == null ? DEFAULT_SEGMENT_NUMBERS_VISIBLE : segmentNumbersVisibleProperty().get();
    }

    public void setSegmentNumbersVisible(boolean value) {
        segmentNumbersVisibleProperty().set(value);
    }

    // -- sourceCode

    private final StringProperty sourceCode = new SimpleStringProperty("");


    // -- symmetricEditMode

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

    // -- templateImage

    private final ObjectProperty<Image> templateImage = new SimpleObjectProperty<>();

    public Image templateImage() {
        return templateImage.get();
    }

    public void setTemplateImage(Image image) {
        templateImage.set(image);
    }

    // -- terrainVisible

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

    public boolean terrainVisible() {
        return terrainVisible == null ? DEFAULT_TERRAIN_VISIBLE : terrainVisible.get();
    }

    public void setTerrainVisible(boolean visible) {
        terrainVisibleProperty().set(visible);
    }

    // -- templateImage

    public ObjectProperty<Image> templateImageProperty() { return templateImage; }


    // -- title

    private final StringProperty title = new SimpleStringProperty("Tile Map Editor");

    public StringProperty titleProperty() { return title; }

    // Accessor methods

    public ChangeManager changeManager() { return changeManager;}

    public Stage stage() {
        return stage;
    }

    public Pane contentPane() {
        return contentPane;
    }

    public EditorMenuBar menuBar() {
        return menuBar;
    }

    public MessageManager messageManager() {
        return messageManager;
    }

    public byte selectedPaletteID() {
        return (Byte) tabPaneWithPalettes.getSelectionModel().getSelectedItem().getUserData();
    }

    public Palette selectedPalette() {
        return palettes[selectedPaletteID()];
    }

    public List<Vector2i> tilesWithErrors() {
        return tilesWithErrors;
    }

    public void showEditHelpText() {
        messageManager.showMessage(translated("edit_help"), 30, MessageType.INFO);
    }

    private ObstacleEditor createObstacleEditor() {
        var obstacleEditor = new ObstacleEditor() {
            @Override
            public void setValue(Vector2i tile, byte value) {
                new Action_SetTileValue(TileMapEditor.this, currentWorldMap(), LayerID.TERRAIN, tile, value).execute();
            }
        };
        obstacleEditor.joiningProperty().bind(obstaclesJoiningProperty());
        obstacleEditor.worldMapProperty().bind(currentWorldMapProperty());
        obstacleEditor.symmetricEditModeProperty().bind(symmetricEditModeProperty());
        return obstacleEditor;
    }

    private void createEditCanvas() {
        ObstacleEditor obstacleEditor = createObstacleEditor();
        editCanvas = new EditCanvas(obstacleEditor);
        editCanvas.gridSizeProperty().bind(gridSizeProperty());
        editCanvas.gridVisibleProperty().bind(gridVisibleProperty());
        editCanvas.worldMapProperty().bind(currentWorldMapProperty());
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
                int initialGridSize = (int) Math.max(newHeight.doubleValue() / currentWorldMap().numRows(), MIN_GRID_SIZE);
                setGridSize(initialGridSize);
            }
        });
    }

    private void createPreview2D() {
        preview2D = new EditorMazePreview2D();
        preview2D.widthProperty().bind(editCanvas.widthProperty());
        preview2D.heightProperty().bind(editCanvas.heightProperty());
        preview2D.gridSizeProperty().bind(gridSizeProperty());
        preview2D.terrainVisibleProperty().bind(terrainVisibleProperty());
        preview2D.foodVisibleProperty().bind(foodVisibleProperty());
        preview2D.actorsVisibleProperty().bind(actorsVisibleProperty());

        spPreview2D = new ScrollPane(preview2D);
        spPreview2D.setFitToHeight(true);
        spPreview2D.hvalueProperty().bindBidirectional(spEditCanvas.hvalueProperty());
        spPreview2D.vvalueProperty().bindBidirectional(spEditCanvas.vvalueProperty());
    }

    private void createPreview3D() {
        mazePreview3D = new EditorMazePreview3D(this, model3DRepository, 500, 500);
        mazePreview3D.foodVisibleProperty().bind(foodVisibleProperty());
        mazePreview3D.terrainVisibleProperty().bind(terrainVisibleProperty());
        mazePreview3D.worldMapProperty().bind(currentWorldMap);
    }

    private void createTemplateImageCanvas() {
        templateImageCanvas = new TemplateImageCanvas(this);
        Pane pane = new Pane(templateImageCanvas, templateImageCanvas.getColorIndicator());
        pane.setBackground(Background.fill(Color.TRANSPARENT));
        spTemplateImage = new ScrollPane(pane);
    }

    public void setFoodMapPropertyValue(String propertyName, String value) {
        requireNonNull(value);
        if (currentWorldMap().properties(LayerID.FOOD).containsKey(propertyName)
            && currentWorldMap().properties(LayerID.FOOD).get(propertyName).equals(value))
            return;
        currentWorldMap().properties(LayerID.FOOD).put(propertyName, value);
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
        sourceView.textProperty().bind(sourceCode);
    }

    private void createTabPaneWithEditViews() {
        tabEditCanvas = new Tab(translated("tab_editor"), spEditCanvas);

        var dropHintButton = new Button(translated("image_drop_hint"));
        dropHintButton.setFont(FONT_DROP_HINT);
        dropHintButton.setOnAction(ae -> initWorldMapForTemplateImage());
        dropHintButton.disableProperty().bind(editModeProperty().map(mode -> mode == EditMode.INSPECT));

        templateImageDropTarget = new BorderPane(dropHintButton);
        registerDragAndDropImageHandler(templateImageDropTarget);

        var stackPane = new StackPane(spTemplateImage, templateImageDropTarget);
        tabTemplateImage = new Tab(translated("tab_template_image"), stackPane);
        templateImage.addListener((py, ov, image) -> {
            Logger.info("Template image changed from {} to {}", ov, image);
            stackPane.getChildren().remove(templateImageDropTarget);
            if (image == null) {
                stackPane.getChildren().add(templateImageDropTarget);
            }
        });

        tabPaneEditorViews = new TabPane(tabEditCanvas, tabTemplateImage);
        tabPaneEditorViews.getTabs().forEach(tab -> tab.setClosable(false));
        tabPaneEditorViews.setSide(Side.BOTTOM);
        tabPaneEditorViews.getSelectionModel().select(tabEditCanvas);
    }

    private void registerDragAndDropImageHandler(Node node) {
        node.setOnDragOver(dragEvent -> {
            if (dragEvent.getDragboard().hasFiles()) {
                File file = dragEvent.getDragboard().getFiles().getFirst();
                if (isSupportedImageFile(file) && !editModeIs(EditMode.INSPECT) || isWorldMapFile(file)) {
                    dragEvent.acceptTransferModes(TransferMode.COPY);
                }
            }
            dragEvent.consume();
        });
        node.setOnDragDropped(dragEvent -> {
            if (dragEvent.getDragboard().hasFiles()) {
                File file = dragEvent.getDragboard().getFiles().getFirst();
                ifNoUnsavedChangesDo(() -> onFileDroppedOnEditCanvas(file));
            }
            dragEvent.consume();
        });
    }

    private void onFileDroppedOnEditCanvas(File file) {
        if (isWorldMapFile(file)) {
            new Action_ReplaceCurrentWorldMapChecked(this, file);
        }
        else if (isSupportedImageFile(file) && !editModeIs(EditMode.INSPECT)) {
            Image image = loadImage(file).orElse(null);
            if (image == null) {
                messageManager.showMessage("Could not open image file '%s'".formatted(file), 3, MessageType.ERROR);
                return;
            }
            if (!isTemplateImageSizeOk(image)) {
                messageManager.showMessage("Template image file '%s' has dubios size".formatted(file), 3, MessageType.ERROR);
                return;
            }
            setTemplateImage(image);
            createEmptyMapFromTemplateImage(image);
            tabPaneEditorViews.getSelectionModel().select(tabTemplateImage);
            messageManager.showMessage("Select colors for tile identification!", 10, MessageType.INFO);
        }
    }

    private void createTabPaneWithPreviews() {
        tabPreview2D = new Tab(translated("preview2D"), spPreview2D);
        Tab tabPreview3D = new Tab(translated("preview3D"), mazePreview3D.getSubScene());
        Tab tabSourceView = new Tab(translated("source"), sourceView);

        TabPane tabPane = new TabPane(tabPreview2D, tabPreview3D, tabSourceView);
        tabPane.setSide(Side.BOTTOM);
        tabPane.getTabs().forEach(tab -> tab.setClosable(false));
        tabPane.getSelectionModel().select(tabPreview2D);

        mazePreview3D.getSubScene().widthProperty().bind(tabPane.widthProperty());
        mazePreview3D.getSubScene().heightProperty().bind(tabPane.heightProperty());

        splitPaneEditorAndPreviews = new SplitPane(tabPaneEditorViews, tabPane);
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
        lblMapSize.textProperty().bind(currentWorldMap.map(worldMap -> (worldMap != null)
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
            messageManager.messageLabel(),
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
                File mapFile = currentFile();
                if (mapFile != null) {
                    return "%s: [%s] - %s".formatted( translated("map_editor"), mapFile.getName(), mapFile.getPath() );
                }
                if (currentWorldMap() != null && currentWorldMap().url() != null) {
                    return  "%s: [%s]".formatted( translated("map_editor"), currentWorldMap().url() );
                }
                return "%s: [%s: %d rows %d cols]".formatted(
                        translated("map_editor"), translated("unsaved_map"),
                        currentWorldMap().numRows(), currentWorldMap().numCols() );
            }, currentFile, currentWorldMap
        );
    }

    // also called from EditorPage
    public MenuItem createLoadMapMenuItem(String description, WorldMap worldMap) {
        requireNonNull(description);
        requireNonNull(worldMap);
        var menuItem = new MenuItem(description);
        menuItem.setOnAction(e -> {
            WorldMap copy = WorldMap.copyMap(worldMap);
            ifNoUnsavedChangesDo(() -> setCurrentWorldMap(copy));
        });
        return menuItem;
    }

    public void ifNoUnsavedChangesDo(Runnable action) {
        if (!changeManager.isEdited()) {
            action.run();
            return;
        }
        SaveConfirmation confirmationDialog = new SaveConfirmation();
        confirmationDialog.showAndWait().ifPresent(choice -> {
            if (choice == SaveConfirmation.SAVE_CHANGES) {
                new Action_SaveMapFile(this).execute();
                action.run();
            } else if (choice == SaveConfirmation.NO_SAVE_CHANGES) {
                changeManager.setEdited(false);
                action.run();
            } else if (choice == SaveConfirmation.CLOSE) {
                confirmationDialog.close();
            }
        });
    }

    private void onEditModeChanged(EditMode editMode) {
        messageManager.clearMessage();
        showEditHelpText();
        switch (editMode) {
            case INSPECT -> editCanvas.enterInspectMode();
            case EDIT    -> editCanvas.enterEditMode();
            case ERASE   -> editCanvas.enterEraseMode();
        }
    }

    public void zoomIn() {
        if (gridSize() < MAX_GRID_SIZE) {
            gridSize.set(gridSize() + 1);
        }
    }

    public void zoomOut() {
        if (gridSize() > MIN_GRID_SIZE) {
            gridSize.set(gridSize() - 1);
        }
    }

    //
    // Drawing
    //

    private void drawUI() {
        if (currentWorldMap() == null) return;

        //TODO avoid creation in every draw call
        var colorScheme = new TerrainMapColorScheme(
            COLOR_CANVAS_BACKGROUND,
            getColorFromMap(currentWorldMap(), LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_FILL, parseColor(MS_PACMAN_COLOR_WALL_FILL)),
            getColorFromMap(currentWorldMap(), LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_STROKE, parseColor(MS_PACMAN_COLOR_WALL_STROKE)),
            getColorFromMap(currentWorldMap(), LayerID.TERRAIN, WorldMapProperty.COLOR_DOOR, parseColor(MS_PACMAN_COLOR_DOOR))
        );
        palettes[selectedPaletteID()].draw();
        if (tabEditCanvas.isSelected()) {
            editCanvas.draw(this, colorScheme);
        }
        else if (tabTemplateImage.isSelected()) {
            templateImageCanvas.draw();
        }
        if (tabPreview2D.isSelected()) {
            preview2D.draw(currentWorldMap(), colorScheme);
        }
    }

    // Controller part

    private void onKeyPressed(KeyEvent e) {
        KeyCode key = e.getCode();
        boolean alt = e.isAltDown();

        if (alt && key == KeyCode.LEFT) {
            new Action_SelectNextMapFile(this, false).execute();
        }
        else if (alt && key == KeyCode.RIGHT) {
            new Action_SelectNextMapFile(this, true).execute();
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

    public void moveCursorAndSetFoodAtTile(Direction dir) {
        if (editCanvas.moveCursor(dir, tile -> hasAccessibleTerrainAtTile(currentWorldMap(), tile))) {
            setFoodAtFocussedTile();
        }
    }

    private void setFoodAtFocussedTile() {
        if (editModeIs(EditMode.EDIT) && selectedPaletteID() == PALETTE_ID_FOOD) {
            if (hasAccessibleTerrainAtTile(currentWorldMap(), editCanvas.focussedTile())) {
                editFoodAtTile(editCanvas.focussedTile());
            }
        }
    }

    private void editFoodAtTile(Vector2i tile) {
        if (selectedPalette().isToolSelected()) {
            selectedPalette().selectedTool().apply(this, LayerID.FOOD, tile);
        }
        changeManager().setFoodMapChanged();
        changeManager().setEdited(true);
    }

    public void setTileValue(WorldMap worldMap, LayerID layerID, int row, int col, byte code) {
        new Action_SetTileValue(this, worldMap, layerID, new Vector2i(col, row), code).execute();
    }

    public WorldMap createEmptyMap(int numCols, int numRows) {
        var worldMap = WorldMap.emptyMap(numRows, numCols);
        new Action_SetDefaultMapColors(this, worldMap).execute();
        setDefaultScatterPositions(worldMap);
        changeManager.setTerrainMapChanged();
        return worldMap;
    }

    public void setDefaultScatterPositions(WorldMap worldMap) {
        int numCols = worldMap.numCols(), numRows = worldMap.numRows();
        if (numCols >= 3 && numRows >= 2) {
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_SCATTER_RED_GHOST,    WorldMapFormatter.formatTile(Vector2i.of(numCols - 3, 0)));
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_SCATTER_PINK_GHOST,   WorldMapFormatter.formatTile(Vector2i.of(2, 0)));
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_SCATTER_CYAN_GHOST,   WorldMapFormatter.formatTile(Vector2i.of(numCols - 1, numRows - 2)));
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_SCATTER_ORANGE_GHOST, WorldMapFormatter.formatTile(Vector2i.of(0, numRows - 2)));
        }
    }

    private void createEmptyMapFromTemplateImage(Image image) {
        int tilesX = (int) (image.getWidth() / TS);
        int tilesY = EMPTY_ROWS_BEFORE_MAZE + EMPTY_ROWS_BELOW_MAZE + (int) (image.getHeight() / TS);
        setCurrentWorldMap(createEmptyMap(tilesX, tilesY));
        currentWorldMap().properties(LayerID.TERRAIN).remove(WorldMapProperty.COLOR_WALL_FILL);
        currentWorldMap().properties(LayerID.TERRAIN).remove(WorldMapProperty.COLOR_WALL_STROKE);
        currentWorldMap().properties(LayerID.TERRAIN).remove(WorldMapProperty.COLOR_DOOR);
        changeManager.setTerrainMapChanged();
        currentWorldMap().properties(LayerID.FOOD).remove(WorldMapProperty.COLOR_FOOD);
        changeManager.setFoodMapChanged();
        changeManager.setEdited(true);
    }

    void initWorldMapForTemplateImage() {
        selectTemplateImage(stage, translated("open_template_image"), currentDirectory())
            .ifPresent(image -> {
                if (isTemplateImageSizeOk(image)) {
                    setTemplateImage(image);
                    createEmptyMapFromTemplateImage(image);
                    tabPaneEditorViews.getSelectionModel().select(tabTemplateImage);
                    messageManager.showMessage("Select map colors from template!", 20, MessageType.INFO);
                } else {
                    messageManager.showMessage("Template image size seems dubious", 3, MessageType.WARNING);
                }
            });
    }

    void populateMapFromTemplateImage(WorldMap worldMap, Image templateImage) {
        if (templateImage == null) {
            return;
        }

        Color fillColor = getColorFromMap(worldMap, LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_FILL, null);
        if (fillColor == null) {
            messageManager.showMessage("No fill color defined", 3, MessageType.ERROR);
            return;
        }
        Color strokeColor = getColorFromMap(worldMap, LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_STROKE, null);
        if (strokeColor == null) {
            messageManager.showMessage("No stroke color defined", 3, MessageType.ERROR);
            return;
        }
        Color doorColor = getColorFromMap(worldMap, LayerID.TERRAIN, WorldMapProperty.COLOR_DOOR, Color.PINK);
        if (doorColor == null) {
            messageManager.showMessage("No door color defined", 3, MessageType.ERROR);
            return;
        }
        Color foodColor = getColorFromMap(worldMap, LayerID.FOOD, WorldMapProperty.COLOR_FOOD, null);
        if (foodColor == null) {
            messageManager.showMessage("No food color defined", 3, MessageType.ERROR);
            return;
        }

        TileMatcher matcher = new TileMatcher(Color.TRANSPARENT, fillColor, strokeColor, doorColor, foodColor);
        WritablePixelFormat<IntBuffer> pixelFormat = WritablePixelFormat.getIntArgbInstance();
        PixelReader rdr = templateImage.getPixelReader();
        if (rdr == null) {
            messageManager.showMessage("Could not get pixel reader for this image", 5, MessageType.ERROR);
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
            new Action_PlaceArcadeHouse(this, worldMap, houseMinTile).execute();
        }

        java.time.Duration duration = java.time.Duration.between(startTime, LocalTime.now());
        messageManager.showMessage("Map creation took %d milliseconds".formatted(duration.toMillis()), 5, MessageType.INFO);

        changeManager.setWorldMapChanged();
        changeManager.setEdited(true);
    }

    private void updateSourceCode() {
        StringBuilder sb = new StringBuilder();
        String[] sourceTextLines = WorldMapFormatter.formatted(currentWorldMap()).split("\n");
        for (int i = 0; i < sourceTextLines.length; ++i) {
            sb.append("%5d: ".formatted(i + 1)).append(sourceTextLines[i]).append("\n");
        }
        sourceCode.set(sb.toString());
    }

    // Sample maps loading

    record SampleMaps(WorldMap pacManMap, List<WorldMap> msPacmanMaps, List<WorldMap> xxlMaps) {}

    private void loadSampleMapsAndUpdateMenu(Menu menu) {
        try {
            SampleMaps maps = loadSampleMaps();
            menu.getItems().clear();
            menu.getItems().add(createLoadMapMenuItem("Pac-Man", maps.pacManMap()));
            menu.getItems().add(new SeparatorMenuItem());
            for (int i = 0; i < maps.msPacmanMaps().size(); ++i) {
                menu.getItems().add(
                    createLoadMapMenuItem("Ms. Pac-Man %d".formatted(i+1), maps.msPacmanMaps().get(i)));
            }
            menu.getItems().add(new SeparatorMenuItem());
            for (int i = 0; i < maps.xxlMaps().size(); ++i) {
                menu.getItems().add(
                    createLoadMapMenuItem("Pac-Man XXL %d".formatted(i+1), maps.xxlMaps().get(i)));
            }
        } catch (IOException x) {
            Logger.error(x);
            Logger.error("Error loading sample maps");
        }
    }

    private SampleMaps loadSampleMaps() throws IOException {
        var pacManMap = WorldMap.fromURL(sampleMapURL("pacman/pacman.world", 1));
        var msPacManMaps = new ArrayList<WorldMap>();
        for (int n = 1; n <= 6; ++n) {
            URL url = sampleMapURL("mspacman/mspacman_%d.world", n);
            msPacManMaps.add(WorldMap.fromURL(url));
        }
        msPacManMaps.trimToSize();
        var xxlMaps = new ArrayList<WorldMap>();
        for (int n = 1; n <= 8; ++n) {
            URL url = sampleMapURL("pacman_xxl/masonic_%d.world", n);
            xxlMaps.add(WorldMap.fromURL(url));
        }
        xxlMaps.trimToSize();
        return new SampleMaps(pacManMap, msPacManMaps, xxlMaps);
    }

    private URL sampleMapURL(String namePattern, int number) {
        return getClass().getResource(SAMPLE_MAPS_PATH + namePattern.formatted(number));
    }
}