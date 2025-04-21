/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.Globals;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.*;
import de.amr.games.pacman.model.WorldMapProperty;
import de.amr.games.pacman.uilib.tilemap.FoodMapRenderer;
import de.amr.games.pacman.uilib.tilemap.TerrainMapColorScheme;
import de.amr.games.pacman.uilib.tilemap.TerrainMapRenderer;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.IntBuffer;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Stream;

import static de.amr.games.pacman.Globals.HTS;
import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.lib.tilemap.WorldMap.formatTile;
import static de.amr.games.pacman.tilemap.editor.ArcadeMap.*;
import static de.amr.games.pacman.tilemap.editor.TileMapEditorUtil.*;
import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.rangeClosed;

/**
 * @author Armin Reichert
 */
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
    public static String tt(String key, Object... args) {
        try {
            return MessageFormat.format(TEXT_BUNDLE.getString(key), args);
        } catch (MissingResourceException x) {
            Logger.error("No resource with key {} found in {}", key, TEXT_BUNDLE);
            return "[%s]".formatted(key);
        }
    }

    private static boolean isSupportedImageFile(File file) {
        return Stream.of(".bmp", ".gif", ".jpg", ".png").anyMatch(ext -> file.getName().toLowerCase().endsWith(ext));
    }

    private static boolean isWorldMapFile(File file) {
        return file.getName().toLowerCase().endsWith(".world");
    }

    public static final Font FONT_DROP_HINT               = Font.font("Sans", FontWeight.BOLD, 16);
    public static final Font FONT_MESSAGE                 = Font.font("Sans", FontWeight.BOLD, 14);
    public static final Font FONT_SOURCE_VIEW             = Font.font("Monospace", FontWeight.NORMAL, 14);
    public static final Font FONT_STATUS_LINE_EDIT_MODE   = Font.font("Sans", FontWeight.BOLD, 18);
    public static final Font FONT_STATUS_LINE_NORMAL      = Font.font("Sans", FontWeight.NORMAL, 14);

    public static final Color COLOR_CANVAS_BACKGROUND = Color.BLACK;

    public static final Node NO_GRAPHIC = null;

    public static final FileChooser.ExtensionFilter FILTER_WORLD_MAP = new FileChooser.ExtensionFilter("World Map Files", "*.world");
    public static final FileChooser.ExtensionFilter FILTER_IMAGE_FILES = new FileChooser.ExtensionFilter("Image Files", "*.bmp", "*.gif", "*.jpg", "*.png");
    public static final FileChooser.ExtensionFilter FILTER_ALL_FILES = new FileChooser.ExtensionFilter("All Files", "*.*");

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
                tilesWithErrors.addAll(editedWorldMap().updateObstacleList());
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

    // Attributes

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
    private Canvas canvasPreview2D;
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
    private MazePreview3D mazePreview3D;

    private MenuBar menuBar;
    private Menu menuFile;
    private Menu menuEdit;
    private Menu menuLoadMap;
    private Menu menuView;

    private final Palette[] palettes = new Palette[3];
    private PropertyEditorPane terrainMapPropertiesEditor;
    private PropertyEditorPane foodMapPropertiesEditor;

    private TerrainTileMapRenderer terrainTileRenderer;
    private TerrainMapRenderer terrainPathRenderer;
    private FoodMapRenderer foodRenderer;

    // Properties

    private final ObjectProperty<EditMode> editModePy = new SimpleObjectProperty<>(EditMode.INSPECT) {
        @Override
        protected void invalidated() {
            onEditModeChanged(get());
        }
    };

    private final ObjectProperty<File> currentFilePy = new SimpleObjectProperty<>();

    private final ObjectProperty<WorldMap> editedWorldMapPy = new SimpleObjectProperty<>(new WorldMap(28, 36)) {
        @Override
        protected void invalidated() {
            templateImagePy.set(null);
            changeManager.setWorldMapChanged();
        }
    };

    private final IntegerProperty gridSizePy = new SimpleIntegerProperty(8) {
        @Override
        protected void invalidated() {
            changeManager.requestRedraw();
        }
    };

    private final BooleanProperty actorsVisiblePy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            changeManager.requestRedraw();
        }
    };

    private final BooleanProperty gridVisiblePy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            changeManager.requestRedraw();
        }
    };

    private final BooleanProperty foodVisiblePy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            changeManager.requestRedraw();
        }
    };

    private final BooleanProperty terrainVisiblePy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            changeManager.requestRedraw();
        }
    };

    private final BooleanProperty segmentNumbersDisplayedPy = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            changeManager.requestRedraw();
        }
    };

    private final BooleanProperty obstacleInnerAreaDisplayedPy = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            changeManager.requestRedraw();
        }
    };

    private final BooleanProperty propertyEditorsVisiblePy = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            setPropertyEditorsVisible(get());
        }
    };

    private final StringProperty titlePy = new SimpleStringProperty("Tile Map Editor");

    private final BooleanProperty symmetricEditPy = new SimpleBooleanProperty(true);

    private final BooleanProperty obstaclesJoiningPy = new SimpleBooleanProperty(true);

    private final ObjectProperty<Image> templateImagePy = new SimpleObjectProperty<>();

    // Accessor methods

    public ChangeManager getChangeManager() { return changeManager;}

    public ObjectProperty<WorldMap> editedWorldMapProperty() { return editedWorldMapPy; }

    public WorldMap editedWorldMap() { return editedWorldMapPy.get(); }

    public void setEditedWorldMap(WorldMap worldMap) { editedWorldMapPy.set(requireNonNull(worldMap)); }

    public IntegerProperty gridSizeProperty() { return gridSizePy; }

    public int gridSize() { return gridSizePy.get(); }

    public BooleanProperty actorsVisibleProperty() { return actorsVisiblePy; }

    public BooleanProperty gridVisibleProperty() { return gridVisiblePy; }

    public BooleanProperty terrainVisibleProperty() { return terrainVisiblePy; }

    public BooleanProperty foodVisibleProperty() { return foodVisiblePy; }

    public BooleanProperty segmentNumbersDisplayedProperty() { return segmentNumbersDisplayedPy; }

    public BooleanProperty obstacleInnerAreaDisplayedProperty() { return obstacleInnerAreaDisplayedPy; }

    public BooleanProperty obstaclesJoiningProperty() { return obstaclesJoiningPy; }

    public BooleanProperty symmetricEditProperty() { return symmetricEditPy; }

    public boolean isSymmetricEdit() { return symmetricEditPy.get(); }

    public TerrainTileMapRenderer terrainTileRenderer() { return terrainTileRenderer; }

    public FoodMapRenderer foodRenderer() { return foodRenderer; }

    public StringProperty titleProperty() { return titlePy; }

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

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public Menu getFileMenu() {
        return menuFile;
    }

    public Menu getLoadMapMenu() {
        return menuLoadMap;
    }

    public List<Vector2i> tilesWithErrors() {
        return tilesWithErrors;
    }

    public TileMapEditor(Stage stage) {
        this.stage = requireNonNull(stage);

        TerrainMapColorScheme initialColors = new TerrainMapColorScheme(
            Color.BLACK,
            parseColor(MS_PACMAN_COLOR_WALL_FILL),
            parseColor(MS_PACMAN_COLOR_WALL_STROKE),
            parseColor(MS_PACMAN_COLOR_DOOR)
        );
        // renderers must be created before palettes!
        createRenderers(initialColors, parseColor(MS_PACMAN_COLOR_FOOD));

        createFileChooser();
        createEditCanvas();
        createPreview2D();
        createPreview3D();
        createTemplateImageCanvas();
        createMapSourceView();
        createPalettes();
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
        titlePy.bind(createTitleBinding());
        stage.titleProperty().bind(titlePy);
        setPropertyEditorsVisible(propertyEditorsVisiblePy.get());
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
        showMessage(tt("edit_help"), 30, MessageType.INFO);
    }

    private void createRenderers(TerrainMapColorScheme colors, Color foodColor) {
        terrainTileRenderer = new TerrainTileMapRenderer();
        terrainTileRenderer.setColorScheme(colors);
        terrainPathRenderer = new TerrainMapRenderer();
        terrainPathRenderer.setColorScheme(colors);
        foodRenderer = new FoodMapRenderer();
        foodRenderer.setPelletColor(foodColor);
        foodRenderer.setEnergizerColor(foodColor);
    }

    private void createFileChooser() {
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(FILTER_WORLD_MAP, FILTER_ALL_FILES);
        fileChooser.setSelectedExtensionFilter(FILTER_WORLD_MAP);
        fileChooser.setInitialDirectory(currentDirectory);
    }

    private void createEditCanvas() {
        editCanvas = new EditCanvas(this);
        spEditCanvas = new ScrollPane(editCanvas.canvas());
        spEditCanvas.setFitToHeight(true);
        registerDragAndDropImageHandler(spEditCanvas);
        //TODO is there a better way to get the initial resize time of the scroll pane?
        spEditCanvas.heightProperty().addListener((py,oldHeight,newHeight) -> {
            if (oldHeight.doubleValue() == 0) { // initial resize
                int initialGridSize = (int) Math.max(newHeight.doubleValue() / editedWorldMap().numRows(), MIN_GRID_SIZE);
                gridSizePy.set(initialGridSize);
            }
        });
    }

    private void createPreview2D() {
        canvasPreview2D = new Canvas();
        canvasPreview2D.widthProperty().bind(editCanvas.canvas().widthProperty());
        canvasPreview2D.heightProperty().bind(editCanvas.canvas().heightProperty());
        spPreview2D = new ScrollPane(canvasPreview2D);
        spPreview2D.setFitToHeight(true);
        spPreview2D.hvalueProperty().bindBidirectional(spEditCanvas.hvalueProperty());
        spPreview2D.vvalueProperty().bindBidirectional(spEditCanvas.vvalueProperty());
    }

    private void createPreview3D() {
        mazePreview3D = new MazePreview3D(500, 500);
        mazePreview3D.foodVisibleProperty().bind(foodVisiblePy);
        mazePreview3D.terrainVisibleProperty().bind(terrainVisiblePy);
        mazePreview3D.worldMapProperty().bind(editedWorldMapPy);
    }

    private void createTemplateImageCanvas() {
        templateImageCanvas = new TemplateImageCanvas(this);
        Pane pane = new Pane(templateImageCanvas, templateImageCanvas.getColorIndicator());
        pane.setBackground(Background.fill(Color.TRANSPARENT));
        spTemplateImage = new ScrollPane(pane);
    }

    public void setTerrainMapPropertyValue(String propertyName, String value) {
        requireNonNull(value);
        if (editedWorldMap().hasProperty(LayerID.TERRAIN, propertyName)
            && editedWorldMap().getProperty(LayerID.TERRAIN, propertyName).equals(value))
            return;
        editedWorldMap().setProperty(LayerID.TERRAIN, propertyName, value);
        changeManager.setTerrainMapChanged();
        changeManager.setEdited(true);
    }

    private void removeTerrainMapProperty(String name) {
        editedWorldMap().removeProperty(LayerID.TERRAIN, name);
        changeManager.setTerrainMapChanged();
        changeManager.setEdited(true);
    }

    public void setFoodMapPropertyValue(String propertyName, String value) {
        requireNonNull(value);
        if (editedWorldMap().hasProperty(LayerID.FOOD, propertyName)
            && editedWorldMap().getProperty(LayerID.FOOD, propertyName).equals(value))
            return;
        editedWorldMap().setProperty(LayerID.FOOD, propertyName, value);
        changeManager.setFoodMapChanged();
        changeManager.setEdited(true);
    }

    private void removeFoodMapProperty(String name) {
        editedWorldMap().removeProperty(LayerID.FOOD, name);
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
    }

    private void createTabPaneWithEditViews() {
        tabEditCanvas = new Tab(tt("tab_editor"), spEditCanvas);

        var hint = new Button(tt("image_drop_hint"));
        hint.setFont(FONT_DROP_HINT);
        hint.setOnAction(ae -> initWorldMapForTemplateImage());
        hint.disableProperty().bind(editModePy.map(mode -> mode == EditMode.INSPECT));

        dropTargetForTemplateImage = new BorderPane(hint);
        registerDragAndDropImageHandler(dropTargetForTemplateImage);

        var stackPane = new StackPane(spTemplateImage, dropTargetForTemplateImage);
        templateImagePy.addListener((py, ov, nv) -> {
            stackPane.getChildren().remove(dropTargetForTemplateImage);
            if (nv == null) {
                stackPane.getChildren().add(dropTargetForTemplateImage);
            }
        });
        tabTemplateImage = new Tab(tt("tab_template_image"), stackPane);

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
        tabPreview2D = new Tab(tt("preview2D"), spPreview2D);
        tabPreview3D = new Tab(tt("preview3D"), mazePreview3D.getSubScene());
        tabSourceView = new Tab(tt("source"), sourceView);

        tabPanePreviews = new TabPane(tabPreview2D, tabPreview3D, tabSourceView);
        tabPanePreviews.setSide(Side.BOTTOM);
        tabPanePreviews.getTabs().forEach(tab -> tab.setClosable(false));
        tabPanePreviews.getSelectionModel().select(tabPreview2D);

        mazePreview3D.getSubScene().widthProperty().bind(tabPanePreviews.widthProperty());
        mazePreview3D.getSubScene().heightProperty().bind(tabPanePreviews.heightProperty());

        splitPaneEditorAndPreviews = new SplitPane(tabPaneEditorViews, tabPanePreviews);
        splitPaneEditorAndPreviews.setDividerPositions(0.5);
    }

    private void createPalettes() {
        palettes[PALETTE_ID_ACTORS]  = createActorPalette(PALETTE_ID_ACTORS, TOOL_SIZE, this, terrainTileRenderer);
        palettes[PALETTE_ID_TERRAIN] = createTerrainPalette(PALETTE_ID_TERRAIN, TOOL_SIZE, this, terrainTileRenderer);
        palettes[PALETTE_ID_FOOD]    = createFoodPalette(PALETTE_ID_FOOD, TOOL_SIZE, this, foodRenderer);

        var tabTerrain = new Tab(tt("terrain"), palettes[PALETTE_ID_TERRAIN].root());
        tabTerrain.setClosable(false);
        tabTerrain.setUserData(PALETTE_ID_TERRAIN);

        var tabActors = new Tab(tt("actors"), palettes[PALETTE_ID_ACTORS].root());
        tabActors.setClosable(false);
        tabActors.setUserData(PALETTE_ID_ACTORS);

        var tabPellets = new Tab(tt("pellets"), palettes[PALETTE_ID_FOOD].root());
        tabPellets.setClosable(false);
        tabPellets.setUserData(PALETTE_ID_FOOD);

        tabPaneWithPalettes = new TabPane(tabTerrain, tabActors, tabPellets);
        tabPaneWithPalettes.setPadding(new Insets(5, 5, 5, 5));
        tabPaneWithPalettes.setMinHeight(75);
    }

    private void createPropertyEditors() {
        terrainMapPropertiesEditor = new PropertyEditorPane(this);
        terrainMapPropertiesEditor.enabledPy.bind(editModePy.map(mode -> mode != EditMode.INSPECT));
        terrainMapPropertiesEditor.setPadding(new Insets(10,0,0,0));

        foodMapPropertiesEditor = new PropertyEditorPane(this);
        foodMapPropertiesEditor.enabledPy.bind(editModePy.map(mode -> mode != EditMode.INSPECT));
        foodMapPropertiesEditor.setPadding(new Insets(10,0,0,0));

        var terrainPropertiesPane = new TitledPane(tt("terrain"), terrainMapPropertiesEditor);
        terrainPropertiesPane.setMinWidth(300);
        terrainPropertiesPane.setExpanded(true);

        var foodPropertiesPane = new TitledPane(tt("pellets"), foodMapPropertiesEditor);
        foodPropertiesPane.setExpanded(true);

        propertyEditorsPane = new VBox(terrainPropertiesPane, foodPropertiesPane);
        propertyEditorsPane.visibleProperty().bind(propertyEditorsVisiblePy);
    }

    private void setPropertyEditorsVisible(boolean visible) {
        contentPane.setLeft(visible ? propertyEditorsPane : null);
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
        Bindings.bindBidirectional(sliderZoom.valueProperty(), gridSizePy);
        Tooltip tt = new Tooltip();
        tt.setFont(Font.font(14));
        tt.textProperty().bind(gridSizePy.map("Grid Size: %s"::formatted));
        sliderZoom.setTooltip(tt);
    }

    private void createStatusLine() {
        var lblMapSize = new Label();
        lblMapSize.setFont(FONT_STATUS_LINE_NORMAL);
        lblMapSize.textProperty().bind(editedWorldMapPy.map(worldMap -> (worldMap != null)
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
            () -> switch (editModePy.get()) {
                case INSPECT -> tt("mode.inspect");
                case EDIT    -> isSymmetricEdit() ?  tt("mode.symmetric") : tt("mode.edit");
                case ERASE   -> tt("mode.erase");
            }, editModePy, symmetricEditPy
        ));
        lblEditMode.textFillProperty().bind(
            editModePy.map(mode -> switch (mode) {
            case INSPECT -> Color.GRAY;
            case EDIT    -> Color.FORESTGREEN;
            case ERASE   -> Color.RED;
        }));

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
                File mapFile = currentFilePy.get();
                if (mapFile != null) {
                    return "%s: [%s] - %s".formatted( tt("map_editor"), mapFile.getName(), mapFile.getPath() );
                }
                if (editedWorldMap() != null && editedWorldMap().url() != null) {
                    return  "%s: [%s]".formatted( tt("map_editor"), editedWorldMap().url() );
                }
                return "%s: [%s: %d rows %d cols]".formatted(
                        tt("map_editor"), tt("unsaved_map"),
                        editedWorldMap().numRows(), editedWorldMap().numCols() );
            }, currentFilePy, editedWorldMapPy
        );
    }

    private void createMenuBarAndMenus() {

        // File
        var miNewPreconfiguredMap = new MenuItem(tt("menu.file.new"));
        miNewPreconfiguredMap.setOnAction(e -> showNewMapDialog(true));

        var miNewBlankMap = new MenuItem(tt("menu.file.new_blank_map"));
        miNewBlankMap.setOnAction(e -> showNewMapDialog(false));

        var miOpenMapFile = new MenuItem(tt("menu.file.open"));
        miOpenMapFile.setOnAction(e -> openMapFileInteractively());

        var miSaveMapFileAs = new MenuItem(tt("menu.file.save_as"));
        miSaveMapFileAs.setOnAction(e -> showSaveDialog());

        var miOpenTemplateImage = new MenuItem(tt("menu.file.open_template_image"));
        miOpenTemplateImage.setOnAction(e -> initWorldMapForTemplateImage());

        var miCloseTemplateImage = new MenuItem(tt("menu.file.close_template_image"));
        miCloseTemplateImage.setOnAction(e -> closeTemplateImage());

        menuFile = new Menu(tt("menu.file"), NO_GRAPHIC,
                miNewPreconfiguredMap,
                miNewBlankMap,
                miOpenMapFile,
                miSaveMapFileAs,
                new SeparatorMenuItem(),
                miOpenTemplateImage,
                miCloseTemplateImage);

        // Edit
        var miObstacleJoining = new CheckMenuItem(tt("menu.edit.obstacles_joining"));
        miObstacleJoining.selectedProperty().bindBidirectional(obstaclesJoiningPy);

        var miAddBorder = new MenuItem(tt("menu.edit.add_border"));
        miAddBorder.setOnAction(e -> addBorderWall(editedWorldMap()));
        miAddBorder.disableProperty().bind(editModePy.map(mode -> mode == EditMode.INSPECT));

        var miAddHouse = new MenuItem(tt("menu.edit.add_house"));
        miAddHouse.setOnAction(e -> addArcadeHouseAtMapCenter(editedWorldMap()));
        miAddHouse.disableProperty().bind(editModePy.map(mode -> mode == EditMode.INSPECT));

        var miClearTerrain = new MenuItem(tt("menu.edit.clear_terrain"));
        miClearTerrain.setOnAction(e -> {
            editedWorldMap().layer(LayerID.TERRAIN).setAll(TerrainTiles.EMPTY);
            changeManager.setTerrainMapChanged();
            changeManager.setEdited(true);
        });
        miClearTerrain.disableProperty().bind(editModePy.map(mode -> mode == EditMode.INSPECT));

        var miClearFood = new MenuItem(tt("menu.edit.clear_food"));
        miClearFood.setOnAction(e -> {
            editedWorldMap().layer(LayerID.FOOD).setAll(FoodTiles.EMPTY);
            changeManager.setFoodMapChanged();
            changeManager.setEdited(true);
        });
        miClearFood.disableProperty().bind(editModePy.map(mode -> mode == EditMode.INSPECT));

        var miIdentifyTiles = new MenuItem(tt("menu.edit.identify_tiles"));
        miIdentifyTiles.disableProperty().bind(Bindings.createBooleanBinding(
            () -> editMode() == EditMode.INSPECT || templateImagePy.get() == null, editModePy, templateImagePy));
        miIdentifyTiles.setOnAction(e -> populateMapFromTemplateImage(editedWorldMap()));

        var miAssignDefaultColors = new MenuItem("Assign default colors"); //TODO localize
        miAssignDefaultColors.setOnAction(e -> setDefaultColors(editedWorldMap()));
        miAssignDefaultColors.disableProperty().bind(editModePy.map(mode -> mode == EditMode.INSPECT));

        menuEdit = new Menu(tt("menu.edit"), NO_GRAPHIC,
            miObstacleJoining,
            new SeparatorMenuItem(),
            miAddBorder,
            miAddHouse,
            miClearTerrain,
            miClearFood,
            miIdentifyTiles,
            miAssignDefaultColors);

        // Maps
        menuLoadMap = new Menu(tt("menu.load_map"));

        // View
        var miPropertiesVisible = new CheckMenuItem(tt("menu.view.properties"));
        miPropertiesVisible.selectedProperty().bindBidirectional(propertyEditorsVisiblePy);

        var miTerrainVisible = new CheckMenuItem(tt("menu.view.terrain"));
        miTerrainVisible.selectedProperty().bindBidirectional(terrainVisiblePy);

        var miFoodVisible = new CheckMenuItem(tt("menu.view.food"));
        miFoodVisible.selectedProperty().bindBidirectional(foodVisiblePy);

        var miActorsVisible = new CheckMenuItem("Actors"); //TODO localize
        miActorsVisible.selectedProperty().bindBidirectional(actorsVisiblePy);

        var miGridVisible = new CheckMenuItem(tt("menu.view.grid"));
        miGridVisible.selectedProperty().bindBidirectional(gridVisiblePy);

        var miSegmentNumbersVisible = new CheckMenuItem(tt("menu.view.segment_numbers"));
        miSegmentNumbersVisible.selectedProperty().bindBidirectional(segmentNumbersDisplayedPy);

        var miObstacleInnerAreaVisible = new CheckMenuItem(tt("inner_obstacle_area"));
        miObstacleInnerAreaVisible.selectedProperty().bindBidirectional(obstacleInnerAreaDisplayedPy);

        menuView = new Menu(tt("menu.view"), NO_GRAPHIC,
            miPropertiesVisible, miTerrainVisible, miSegmentNumbersVisible, miObstacleInnerAreaVisible,
            miFoodVisible, miActorsVisible, miGridVisible);

        menuBar = new MenuBar(menuFile, menuEdit, menuLoadMap, menuView);
    }

    // also called from EditorPage
    public void addLoadMapMenuItem(String description, WorldMap map) {
        requireNonNull(description);
        requireNonNull(map);
        var miLoadMap = new MenuItem(description);
        miLoadMap.setOnAction(e -> loadMap(map));
        menuLoadMap.getItems().add(miLoadMap);
    }

    public void loadMap(WorldMap worldMap) {
        executeWithCheckForUnsavedChanges(() -> {
            setEditedWorldMap(new WorldMap(worldMap));
            currentFilePy.set(null);
        });
    }

    private void showNewMapDialog(boolean preconfigured) {
        executeWithCheckForUnsavedChanges(() -> {
            var dialog = new TextInputDialog("28x36");
            dialog.setTitle(tt("new_dialog.title"));
            dialog.setHeaderText(tt("new_dialog.header_text"));
            dialog.setContentText(tt("new_dialog.content_text"));
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
                    currentFilePy.set(null);
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

    private void openMapFileInteractively() {
        executeWithCheckForUnsavedChanges(() -> {
            fileChooser.setTitle(tt("open_file"));
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
                loadMap(new WorldMap(file));
                currentDirectory = file.getParentFile();
                currentFilePy.set(file);
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
        File currentFile = currentFilePy.get();
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
        fileChooser.setTitle(tt("save_file"));
        fileChooser.setInitialDirectory(currentDirectory);
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            currentDirectory = file.getParentFile();
            if (file.getName().endsWith(".world")) {
                editedWorldMap().save(file);
                changeManager.setEdited(false);
                readMapFile(file);
            } else {
                Logger.error("No .world file selected");
                showMessage("No .world file selected", 2, MessageType.WARNING);
            }
        }
    }

    public void executeWithCheckForUnsavedChanges(Runnable action) {
        if (!changeManager.isEdited()) {
            action.run();
            return;
        }
        var confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle(tt("save_dialog.title"));
        confirmationDialog.setHeaderText(tt("save_dialog.header_text"));
        confirmationDialog.setContentText(tt("save_dialog.content_text"));
        var choiceSave   = new ButtonType(tt("save_changes"));
        var choiceNoSave = new ButtonType(tt("no_save_changes"));
        var choiceCancel = new ButtonType(tt("cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
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
        String[] lines = editedWorldMap().sourceText().split("\n");
        for (int i = 0; i < lines.length; ++i) {
            sb.append("%5d: ".formatted(i + 1)).append(lines[i]).append("\n");
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
            gridSizePy.set(gridSize() + 1);
        }
    }

    private void zoomOut() {
        if (gridSize() > TileMapEditor.MIN_GRID_SIZE) {
            gridSizePy.set(gridSize() - 1);
        }
    }

    //
    // Drawing
    //

    public void drawActorSprites(GraphicsContext g, WorldMap worldMap, double gridSize) {
        terrainTileRenderer.drawSpriteBetweenTiles(g, worldMap.getTerrainTileProperty(WorldMapProperty.POS_PAC, null), PAC_SPRITE, gridSize);
        terrainTileRenderer.drawSpriteBetweenTiles(g, worldMap.getTerrainTileProperty(WorldMapProperty.POS_RED_GHOST, null), RED_GHOST_SPRITE, gridSize);
        terrainTileRenderer.drawSpriteBetweenTiles(g, worldMap.getTerrainTileProperty(WorldMapProperty.POS_PINK_GHOST, null), PINK_GHOST_SPRITE, gridSize);
        terrainTileRenderer.drawSpriteBetweenTiles(g, worldMap.getTerrainTileProperty(WorldMapProperty.POS_CYAN_GHOST, null), CYAN_GHOST_SPRITE, gridSize);
        terrainTileRenderer.drawSpriteBetweenTiles(g, worldMap.getTerrainTileProperty(WorldMapProperty.POS_ORANGE_GHOST, null), ORANGE_GHOST_SPRITE, gridSize);
        terrainTileRenderer.drawSpriteBetweenTiles(g, worldMap.getTerrainTileProperty(WorldMapProperty.POS_BONUS, null), BONUS_SPRITE, gridSize);
    }

    private void draw(TerrainMapColorScheme colors) {
        try {
            Logger.trace("Draw palette");
            drawSelectedPalette(colors);
        } catch (Exception x) {
            Logger.error(x);
        }
        if (tabEditCanvas.isSelected()) {
            try {
                Logger.trace("Draw edit canvas");
                editCanvas.draw(colors);
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
                drawPreview2D(colors);
            } catch (Exception x) {
                Logger.error(x);
            }
        }
    }

    private void drawPreview2D(TerrainMapColorScheme colors) {
        GraphicsContext g = canvasPreview2D.getGraphicsContext2D();
        g.setImageSmoothing(false);
        g.setFill(colors.backgroundColor());
        g.fillRect(0, 0, canvasPreview2D.getWidth(), canvasPreview2D.getHeight());
        if (terrainVisiblePy.get()) {
            terrainPathRenderer.setScaling(gridSize() / 8.0);
            terrainPathRenderer.setColorScheme(colors);
            terrainPathRenderer.drawTerrain(g, editedWorldMap(), editedWorldMap().obstacles());
            Vector2i houseMinTile = editedWorldMap().getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MIN_TILE, null);
            Vector2i houseMaxTile = editedWorldMap().getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MAX_TILE, null);
            if (houseMinTile != null && houseMaxTile != null) {
                terrainPathRenderer.drawHouse(g, houseMinTile, houseMaxTile.minus(houseMinTile).plus(1, 1));
            }
        }
        if (foodVisiblePy.get()) {
            Color foodColor = getColorFromMap(editedWorldMap(), LayerID.FOOD, WorldMapProperty.COLOR_FOOD, parseColor(MS_PACMAN_COLOR_FOOD));
            foodRenderer.setScaling(gridSize() / 8.0);
            foodRenderer.setEnergizerColor(foodColor);
            foodRenderer.setPelletColor(foodColor);
            editedWorldMap().tiles().forEach(tile -> foodRenderer.drawTile(g, tile, editedWorldMap().get(LayerID.FOOD, tile)));
        }
        if (actorsVisiblePy.get()) {
            drawActorSprites(g, editedWorldMap(), gridSize());
        }
    }

    private void drawSelectedPalette(TerrainMapColorScheme colors) {
        Palette selectedPalette = palettes[selectedPaletteID()];
        if (selectedPaletteID() == PALETTE_ID_TERRAIN) {
            double scaling = terrainTileRenderer.scaling();
            terrainTileRenderer.setScaling((double) TOOL_SIZE / 8);
            terrainTileRenderer.setColorScheme(colors);
            terrainTileRenderer.setScaling(scaling);
        }
        selectedPalette.draw();
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
                symmetricEditPy.set(false);
            }
            case "s" -> {
                setEditMode(EditMode.EDIT);
                symmetricEditPy.set(true);
            }
            case "x" -> setEditMode(EditMode.ERASE);
        }
    }

    public EditMode editMode() { return editModePy.get(); }

    public boolean isEditMode(EditMode mode) { return editMode() == mode; }

    public void setEditMode(EditMode mode) {
        editModePy.set(requireNonNull(mode));
    }

    public void editAtMousePosition(MouseEvent event) {
        Vector2i tile = tileAtMousePosition(event.getX(), event.getY(), gridSize());
        if (isEditMode(EditMode.INSPECT)) {
            identifyObstacleAtTile(tile);
            return;
        }
        boolean erase = event.isControlDown();
        switch (selectedPaletteID()) {
            case TileMapEditor.PALETTE_ID_TERRAIN -> editTerrainAtTile(tile, erase);
            case TileMapEditor.PALETTE_ID_FOOD -> editFoodAtTile(tile, erase);
            case TileMapEditor.PALETTE_ID_ACTORS -> {
                if (selectedPalette().isToolSelected()) {
                    selectedPalette().selectedTool().apply(editedWorldMap(), LayerID.TERRAIN, tile);
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
            selectedPalette().selectedTool().apply(editedWorldMap(), LayerID.TERRAIN, tile);
        }
        changeManager.setTerrainMapChanged();
        changeManager.setEdited(true);
    }

    private void editFoodAtTile(Vector2i tile, boolean erase) {
        if (erase) {
            clearFoodTileValue(tile);
        } else if (selectedPalette().isToolSelected()) {
            selectedPalette().selectedTool().apply(editedWorldMap(), LayerID.FOOD, tile);
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
            .filter(obstacle -> Globals.tileAt(obstacle.startPoint().minus(HTS, 0).toVector2f()).equals(tile))
            .findFirst().orElse(null);
        if (obstacleAtTile != null) {
            String encoding = obstacleAtTile.encoding();
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(encoding);
            clipboard.setContent(content);
            showMessage("Obstacle (copied to clipboard)", 5, MessageType.INFO);
        } else {
            byte terrainValue = editedWorldMap().get(LayerID.TERRAIN, tile);
            byte foodValue = editedWorldMap().get(LayerID.FOOD, tile);
            String info = "";
            if (terrainValue != TerrainTiles.EMPTY)
                info = "Terrain #%02X (%s)".formatted(terrainValue, TerrainTiles.name(terrainValue));
            if (foodValue != TerrainTiles.EMPTY)
                info = "Food #%02X (%s)".formatted(foodValue, FoodTiles.name(foodValue));
            showMessage(info, 4, MessageType.INFO);
        }
    }

    /**
     * This method should be used whenever a tile value has to be set and symmetric editing should be executed.
     */
    public void setTileValueAndRespectSymmetricEditing(WorldMap worldMap, LayerID layerID, Vector2i tile, byte value) {
        requireNonNull(worldMap);
        requireNonNull(layerID);
        requireNonNull(tile);

        if (layerID == LayerID.FOOD && !canEditFoodAtTile(tile)) {
            return;
        }

        worldMap.set(layerID, tile, value);
        if (layerID == LayerID.TERRAIN) {
            worldMap.set(LayerID.FOOD, tile, FoodTiles.EMPTY);
        }

        changeManager.setEdited(true);
        changeManager.setWorldMapChanged();

        if (isSymmetricEdit()) {
            Vector2i mirrorTile = worldMap.vSymmetricTile(tile);
            if (layerID == LayerID.FOOD) {
                if (canEditFoodAtTile(mirrorTile)) {
                    worldMap.set(layerID, mirrorTile, value);
                }
            } else {
                byte mirroredValue = mirroredTileValue(value);
                worldMap.set(layerID, mirrorTile, mirroredValue);
                worldMap.set(LayerID.FOOD, mirrorTile, FoodTiles.EMPTY);
            }
        }
    }

    public void setTileValueAndRespectSymmetricEditing(WorldMap worldMap, LayerID layerID, int row, int col, byte value) {
        Vector2i tile = new Vector2i(col, row);
        setTileValueAndRespectSymmetricEditing(worldMap, layerID, tile, value);
    }


    // ignores symmetric edit mode!
    public void clearTerrainTileValue(Vector2i tile) {
        editedWorldMap().set(LayerID.TERRAIN, tile, TerrainTiles.EMPTY);
        changeManager.setTerrainMapChanged();
        changeManager.setEdited(true);
    }

    // ignores symmetric edit mode!
    public void clearFoodTileValue(Vector2i tile) {
        editedWorldMap().set(LayerID.FOOD, tile, FoodTiles.EMPTY);
        changeManager.setFoodMapChanged();
        changeManager.setEdited(true);
    }

    private void setBlankMap(int tilesX, int tilesY) {
        var blankMap = new WorldMap(tilesY, tilesX);
        setDefaultColors(blankMap);
        setDefaultScatterPositions(blankMap);
        setEditedWorldMap(blankMap);
    }

    private void setPreconfiguredMap(int tilesX, int tilesY) {
        var worldMap = new WorldMap(tilesY, tilesX);
        addBorderWall(worldMap);
        setDefaultScatterPositions(worldMap);
        if (worldMap.numRows() >= 20) {
            Vector2i houseMinTile = Vector2i.of(tilesX / 2 - 4, tilesY / 2 - 3);
            placeArcadeHouse(worldMap, houseMinTile);
            worldMap.setProperty(LayerID.TERRAIN, WorldMapProperty.POS_PAC,   formatTile(houseMinTile.plus(3, 11)));
            worldMap.setProperty(LayerID.TERRAIN, WorldMapProperty.POS_BONUS, formatTile(houseMinTile.plus(3, 5)));
        }
        worldMap.updateObstacleList();
        setDefaultColors(worldMap);
        setEditedWorldMap(worldMap);
    }

    private void setDefaultColors(WorldMap worldMap) {
        worldMap.setProperty(LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_STROKE, MS_PACMAN_COLOR_WALL_STROKE);
        worldMap.setProperty(LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_FILL, MS_PACMAN_COLOR_WALL_FILL);
        worldMap.setProperty(LayerID.TERRAIN, WorldMapProperty.COLOR_DOOR, MS_PACMAN_COLOR_DOOR);
        worldMap.setProperty(LayerID.FOOD, WorldMapProperty.COLOR_FOOD, MS_PACMAN_COLOR_FOOD);
        changeManager.setTerrainMapChanged();
        changeManager.setFoodMapChanged();
    }

    private void setDefaultScatterPositions(WorldMap worldMap) {
        int numCols = worldMap.numCols(), numRows = worldMap.numRows();
        if (numCols >= 3 && numRows >= 2) {
            worldMap.setProperty(LayerID.TERRAIN, WorldMapProperty.POS_SCATTER_RED_GHOST,    formatTile(Vector2i.of(numCols - 3, 0)));
            worldMap.setProperty(LayerID.TERRAIN, WorldMapProperty.POS_SCATTER_PINK_GHOST,   formatTile(Vector2i.of(2, 0)));
            worldMap.setProperty(LayerID.TERRAIN, WorldMapProperty.POS_SCATTER_CYAN_GHOST,   formatTile(Vector2i.of(numCols - 1, numRows - 2)));
            worldMap.setProperty(LayerID.TERRAIN, WorldMapProperty.POS_SCATTER_ORANGE_GHOST, formatTile(Vector2i.of(0, numRows - 2)));
            changeManager.setTerrainMapChanged();
        }
    }

    private void addBorderWall(WorldMap worldMap) {
        int lastRow = worldMap.numRows() - 1 - EMPTY_ROWS_BELOW_MAZE, lastCol = worldMap.numCols() - 1;
        setTileValueAndRespectSymmetricEditing(worldMap, LayerID.TERRAIN, EMPTY_ROWS_BEFORE_MAZE, 0, TerrainTiles.ARC_NW);
        setTileValueAndRespectSymmetricEditing(worldMap, LayerID.TERRAIN, EMPTY_ROWS_BEFORE_MAZE, lastCol, TerrainTiles.ARC_NE);
        setTileValueAndRespectSymmetricEditing(worldMap, LayerID.TERRAIN, lastRow, 0, TerrainTiles.ARC_SW);
        setTileValueAndRespectSymmetricEditing(worldMap, LayerID.TERRAIN, lastRow, lastCol, TerrainTiles.ARC_SE);
        for (int row = EMPTY_ROWS_BEFORE_MAZE + 1; row < lastRow; ++row) {
            setTileValueAndRespectSymmetricEditing(worldMap, LayerID.TERRAIN, row, 0, TerrainTiles.WALL_V);
            setTileValueAndRespectSymmetricEditing(worldMap, LayerID.TERRAIN, row, lastCol, TerrainTiles.WALL_V);
        }
        for (int col = 1; col < lastCol; ++col) {
            setTileValueAndRespectSymmetricEditing(worldMap, LayerID.TERRAIN, EMPTY_ROWS_BEFORE_MAZE, col, TerrainTiles.WALL_H);
            setTileValueAndRespectSymmetricEditing(worldMap, LayerID.TERRAIN, lastRow, col, TerrainTiles.WALL_H);
        }
        changeManager.setTerrainMapChanged();
    }

    private void addArcadeHouseAtMapCenter(WorldMap worldMap) {
        int numRows = worldMap.numRows(), numCols = worldMap.numCols();
        int houseMinX = numCols / 2 - 4, houseMinY = numRows / 2 - 3;
        placeArcadeHouse(worldMap, Vector2i.of(houseMinX, houseMinY));
    }

    public void placeArcadeHouse(WorldMap worldMap, Vector2i houseMinTile) {
        Vector2i houseMaxTile = houseMinTile.plus(7, 4);

        Vector2i oldHouseMinTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MIN_TILE, null);
        Vector2i oldHouseMaxTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MAX_TILE, null);
        worldMap.setProperty(LayerID.TERRAIN, WorldMapProperty.POS_HOUSE_MIN_TILE, formatTile(houseMinTile));
        worldMap.setProperty(LayerID.TERRAIN, WorldMapProperty.POS_HOUSE_MAX_TILE, formatTile(houseMaxTile));

        // clear tiles where house walls/doors were located (created at runtime!)
        if (oldHouseMinTile != null && oldHouseMaxTile != null) {
            clearTerrainAreaOneSided(worldMap, oldHouseMinTile, oldHouseMaxTile);
            clearFoodAreaOneSided(worldMap, oldHouseMinTile, oldHouseMaxTile);
        }
        // clear new house area
        clearTerrainAreaOneSided(worldMap, houseMinTile, houseMaxTile);
        clearFoodAreaOneSided(worldMap, houseMinTile, houseMaxTile);

        worldMap.setProperty(LayerID.TERRAIN, WorldMapProperty.POS_RED_GHOST,      formatTile(houseMinTile.plus(3, -1)));
        worldMap.setProperty(LayerID.TERRAIN, WorldMapProperty.POS_CYAN_GHOST,     formatTile(houseMinTile.plus(1, 2)));
        worldMap.setProperty(LayerID.TERRAIN, WorldMapProperty.POS_PINK_GHOST,     formatTile(houseMinTile.plus(3, 2)));
        worldMap.setProperty(LayerID.TERRAIN, WorldMapProperty.POS_ORANGE_GHOST,   formatTile(houseMinTile.plus(5, 2)));

        // clear pellets around house
        Vector2i min = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MIN_TILE, null).minus(1, 1);
        Vector2i max = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MAX_TILE, null).plus(1, 1);
        for (int x = min.x(); x <= max.x(); ++x) {
            // Note: parameters are row and col (y and x)
            worldMap.set(LayerID.FOOD, min.y(), x, FoodTiles.EMPTY);
            worldMap.set(LayerID.FOOD, max.y(), x, FoodTiles.EMPTY);
        }
        for (int y = min.y(); y <= max.y(); ++y) {
            // Note: parameters are row and col (y and x)
            worldMap.set(LayerID.FOOD, y, min.x(), FoodTiles.EMPTY);
            worldMap.set(LayerID.FOOD, y, max.x(), FoodTiles.EMPTY);
        }

        changeManager.setWorldMapChanged();
        changeManager.setEdited(true);
    }

    private void clearTerrainAreaOneSided(WorldMap worldMap, Vector2i minTile, Vector2i maxTile) {
        for (int row = minTile.y(); row <= maxTile.y(); ++row) {
            for (int col = minTile.x(); col <= maxTile.x(); ++col) {
                // No symmetric editing!
                worldMap.set(LayerID.TERRAIN, row, col, TerrainTiles.EMPTY);
            }
        }
        changeManager.setTerrainMapChanged();
        changeManager.setEdited(true);
    }

    private void clearFoodAreaOneSided(WorldMap worldMap, Vector2i minTile, Vector2i maxTile) {
        for (int row = minTile.y(); row <= maxTile.y(); ++row) {
            for (int col = minTile.x(); col <= maxTile.x(); ++col) {
                // No symmetric editing!
                worldMap.set(LayerID.FOOD, row, col, FoodTiles.EMPTY);
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

    private void initWorldMapForTemplateImage() {
        selectImageFile(tt("open_template_image")).ifPresent(file -> readImageFromFile(file).ifPresentOrElse(image -> {
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

    private void closeTemplateImage() {
        templateImagePy.set(null);
    }

    public void floodWithFoodValue(Vector2i startTile, byte value) {
        if (!canEditFoodAtTile(startTile)) {
            return;
        }
        var q = new ArrayDeque<Vector2i>();
        Set<Vector2i> visited = new HashSet<>();
        q.push(startTile);
        visited.add(startTile);
        while (!q.isEmpty()) {
            Vector2i current = q.poll();
            // use this method such that symmmetric editing etc. is taken into account:
            setTileValueAndRespectSymmetricEditing(editedWorldMap(), LayerID.FOOD, current, value);
            for (Direction dir : Direction.values()) {
                Vector2i neighborTile = current.plus(dir.vector());
                if  (!visited.contains(neighborTile) && canEditFoodAtTile(neighborTile)) {
                    q.push(neighborTile);
                }
                visited.add(neighborTile);
            }
        }
        changeManager.setFoodMapChanged();
        changeManager.setEdited(true);
    }

    private boolean hasAccessibleTerrainAtTile(Vector2i tile) {
        byte value = editedWorldMap().get(LayerID.TERRAIN, tile);
        return value == TerrainTiles.EMPTY
            || value == TerrainTiles.ONE_WAY_DOWN
            || value == TerrainTiles.ONE_WAY_UP
            || value == TerrainTiles.ONE_WAY_LEFT
            || value == TerrainTiles.ONE_WAY_RIGHT;
    }

    public boolean canEditFoodAtTile(Vector2i tile) {
        return !editedWorldMap().outOfBounds(tile)
                && tile.y() >= EMPTY_ROWS_BEFORE_MAZE
                && tile.y() < editedWorldMap().numRows() - EMPTY_ROWS_BELOW_MAZE
                && !isPartOfHouse(editedWorldMap(), tile)
                && hasAccessibleTerrainAtTile(tile);
    }


    private boolean isPartOfHouse(WorldMap worldMap, Vector2i tile) {
        Vector2i minTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MIN_TILE, null);
        Vector2i maxTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MAX_TILE, null);
        if (minTile != null && maxTile != null) {
            return minTile.x() <= tile.x() && tile.x() <= maxTile.x()
                && minTile.y() <= tile.y() && tile.y() <= maxTile.y();
        }
        return false;
    }

    private void populateMapFromTemplateImage(WorldMap worldMap) {
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
                    if (foodValue == FoodTiles.PELLET || foodValue == FoodTiles.ENERGIZER) {
                        worldMap.set(LayerID.FOOD, worldMapTile, foodValue);
                    } else {
                        byte terrainValue = matcher.matchTerrainTile(pixelsOfTile);
                        worldMap.set(LayerID.TERRAIN, worldMapTile, terrainValue);
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
            .filter(tile -> worldMap.get(LayerID.TERRAIN, tile) == TerrainTiles.DCORNER_NW)
            .findFirst().orElse(null);

        Vector2i houseMaxTile = worldMap.tiles()
            .filter(tile -> worldMap.get(LayerID.TERRAIN, tile) == TerrainTiles.DCORNER_SE)
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

    static final String SAMPLE_MAPS_PATH = "/de/amr/games/pacman/tilemap/editor/maps/";

    private WorldMap mapPacManGame;
    private List<WorldMap> mapsMsPacManGame;
    private List<WorldMap> mapsPacManXXLGame;

    private void loadSampleMapsAndAddMenuEntries() {
        try {
            loadSampleMaps();
            addLoadMapMenuItem("Pac-Man", mapPacManGame);
            getLoadMapMenu().getItems().add(new SeparatorMenuItem());
            rangeClosed(1, 6).forEach(num -> addLoadMapMenuItem("Ms. Pac-Man " + num, mapsMsPacManGame.get(num - 1)));
            getLoadMapMenu().getItems().add(new SeparatorMenuItem());
            rangeClosed(1, 8).forEach(num -> addLoadMapMenuItem("Pac-Man XXL " + num, mapsPacManXXLGame.get(num - 1)));
        } catch (IOException x) {
            Logger.error("Could not load sample maps");
            Logger.error(x);
        }
    }

    private void loadSampleMaps() throws IOException {
        mapPacManGame = new WorldMap(sampleMapURL("pacman/pacman.world", 1));
        mapsMsPacManGame = new ArrayList<>();
        for (int n = 1; n <= 6; ++n) {
            mapsMsPacManGame.add(new WorldMap(sampleMapURL("mspacman/mspacman_%d.world", n)));
        }
        mapsPacManXXLGame = new ArrayList<>();
        for (int n = 1; n <= 8; ++n) {
            mapsPacManXXLGame.add(new WorldMap(sampleMapURL("pacman_xxl/masonic_%d.world", n)));
        }
    }

    private URL sampleMapURL(String namePattern, int number) {
        return getClass().getResource(SAMPLE_MAPS_PATH + namePattern.formatted(number));
    }
}