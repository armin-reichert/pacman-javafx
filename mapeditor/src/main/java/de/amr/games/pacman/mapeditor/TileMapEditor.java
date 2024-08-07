/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.Tiles;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.tinylog.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.tilemap.TileMap.formatTile;
import static de.amr.games.pacman.mapeditor.TileMapUtil.*;

/**
 * @author Armin Reichert
 */
public class TileMapEditor  {

    private static final ResourceBundle TEXTS = ResourceBundle.getBundle("de.amr.games.pacman.mapeditor.texts");

    public static final Rectangle2D PAC_SPRITE          = new Rectangle2D(473,  16, 14, 14);
    public static final Rectangle2D RED_GHOST_SPRITE    = new Rectangle2D(505,  65, 14, 14);
    public static final Rectangle2D PINK_GHOST_SPRITE   = new Rectangle2D(553,  81, 14, 14);
    public static final Rectangle2D CYAN_GHOST_SPRITE   = new Rectangle2D(521,  97, 14, 14);
    public static final Rectangle2D ORANGE_GHOST_SPRITE = new Rectangle2D(521, 113, 14, 14);
    public static final Rectangle2D BONUS_SPRITE        = new Rectangle2D(505,  49, 14, 14);

    public static final String DEFAULT_COLOR_WALL_STROKE         = "rgb(0,0,255)";
    public static final String DEFAULT_COLOR_WALL_FILL           = "rgb(0,0,0)";
    public static final String DEFAULT_COLOR_DOOR                = "rgb(0,255,255)";
    public static final String PROPERTY_COLOR_WALL_STROKE        = "color_wall_stroke";
    public static final String PROPERTY_COLOR_WALL_FILL          = "color_wall_fill";
    public static final String PROPERTY_COLOR_DOOR               = "color_door";
    public static final String PROPERTY_POS_BONUS                = "pos_bonus";
    public static final String PROPERTY_POS_PAC                  = "pos_pac";
    public static final String PROPERTY_POS_RED_GHOST            = "pos_ghost_1_red";
    public static final String PROPERTY_POS_PINK_GHOST           = "pos_ghost_2_pink";
    public static final String PROPERTY_POS_CYAN_GHOST           = "pos_ghost_3_cyan";
    public static final String PROPERTY_POS_ORANGE_GHOST         = "pos_ghost_4_orange";
    public static final String PROPERTY_POS_SCATTER_RED_GHOST    = "pos_scatter_ghost_1_red";
    public static final String PROPERTY_POS_SCATTER_PINK_GHOST   = "pos_scatter_ghost_2_pink";
    public static final String PROPERTY_POS_SCATTER_CYAN_GHOST   = "pos_scatter_ghost_3_cyan";
    public static final String PROPERTY_POS_SCATTER_ORANGE_GHOST = "pos_scatter_ghost_4_orange";

    public static final String PROPERTY_COLOR_FOOD = "color_food";

    public static final Vector2i DEFAULT_POS_PAC          = new Vector2i(13, 26);
    public static final Vector2i DEFAULT_POS_RED_GHOST    = new Vector2i(13, 14);
    public static final Vector2i DEFAULT_POS_PINK_GHOST   = new Vector2i(13, 17);
    public static final Vector2i DEFAULT_POS_CYAN_GHOST   = new Vector2i(11, 17);
    public static final Vector2i DEFAULT_POS_ORANGE_GHOST = new Vector2i(15, 17);
    public static final Vector2i DEFAULT_POS_BONUS        = new Vector2i(13, 20);

    public static final String DEFAULT_FOOD_COLOR         = "rgb(255,0,0)";

    public static String tt(String key, Object... args) {
        return MessageFormat.format(TEXTS.getString(key), args);
    }

    private static final String PALETTE_TERRAIN = "Terrain";
    private static final String PALETTE_ACTORS  = "Actors";
    private static final String PALETTE_FOOD    = "Food";

    private static final RectShape GHOST_HOUSE_SHAPE = new RectShape(new byte[][] {
        {10, 8, 8,14,14, 8, 8,11},
        { 9, 0, 0, 0, 0, 0, 0, 9},
        { 9, 0, 0, 0, 0, 0, 0, 9},
        { 9, 0, 0, 0, 0, 0, 0, 9},
        {13, 8, 8, 8, 8, 8, 8,12}
    });

    private static void addBorder(TileMap terrain, int emptyRowsTop, int emptyRowsBottom) {
        for (int row = emptyRowsTop; row < terrain.numRows() - emptyRowsBottom; ++row) {
            terrain.set(row, 0, Tiles.DWALL_V);
            terrain.set(row, terrain.numCols() - 1, Tiles.DWALL_V);
        }
        for (int col = 1; col < terrain.numCols() - 1; ++col) {
            terrain.set(emptyRowsTop, col, Tiles.DWALL_H);
            terrain.set(terrain.numRows() - 1 - emptyRowsBottom, col, Tiles.DWALL_H);
        }
        terrain.set(emptyRowsTop, 0, Tiles.DCORNER_NW);
        terrain.set(emptyRowsTop, terrain.numCols() - 1, Tiles.DCORNER_NE);
        terrain.set(terrain.numRows() - 1 - emptyRowsBottom, 0, Tiles.DCORNER_SW);
        terrain.set(terrain.numRows() - 1 - emptyRowsBottom, terrain.numCols() - 1, Tiles.DCORNER_SE);
    }

    public final ObjectProperty<String> titlePy = new SimpleObjectProperty<>(this, "title");

    public final BooleanProperty terrainVisiblePy = new SimpleBooleanProperty(this, "terrainVisible", true);

    public final BooleanProperty foodVisiblePy = new SimpleBooleanProperty(this, "foodVisible", true);

    public final BooleanProperty gridVisiblePy = new SimpleBooleanProperty(this, "gridVisible", true);

    public final BooleanProperty editingEnabledPy = new SimpleBooleanProperty(this,"editingEnabled", false);

    public final BooleanProperty previewVisiblePy = new SimpleBooleanProperty(this, "previewVisible", true);

    public final IntegerProperty gridSizePy = new SimpleIntegerProperty(this, "gridSize", 16) {
        @Override
        protected void invalidated() {
            invalidateTerrainMapPaths();
        }
    };

    public final ObjectProperty<File> currentFilePy = new SimpleObjectProperty<>(this, "currentFile");

    public final ObjectProperty<WorldMap> mapPy = new SimpleObjectProperty<>(this, "map");

    private ObjectProperty<Vector2i> focussedTilePy = new SimpleObjectProperty<>(this, "focussedTile") {
        @Override
        protected void invalidated() {
            Vector2i tile = get();
            var text = "Tile: " + (tile != null ? String.format("x=%2d y=%2d", tile.x(), tile.y()) : "n/a");
            focussedTileInfo.setText(text);
        }
    };

    private Window ownerWindow;
    private MenuBar menuBar;
    private Menu menuFile;
    private Menu menuEdit;
    private Menu menuLoadMap;
    private Menu menuView;
    private final BorderPane contentPane = new BorderPane();
    private Canvas editCanvas;
    private ScrollPane editCanvasScroll;
    private Canvas previewCanvas;
    private ScrollPane previewCanvasScroll;
    private WebView mapSourceView;
    private Label messageLabel;
    private Label focussedTileInfo;
    private FileChooser fileChooser;
    private TabPane palettesTabPane;
    private PropertyEditorPane terrainMapPropertiesEditor;
    private TileMapEditorTerrainRenderer terrainMapRenderer;
    private PropertyEditorPane foodMapPropertiesEditor;
    private FoodMapRenderer foodMapRenderer;

    private final Map<String, Palette> palettes = new HashMap<>();

    private boolean pathsUpToDate;
    private boolean unsavedChanges;
    private File lastUsedDir;
    private Instant messageCloseTime;
    private Timeline clock;

    private final Image spriteSheet;

    public TileMapEditor() {
        this(new File(System.getProperty("user.home")));
    }

    public TileMapEditor(File workDir) {
        lastUsedDir = workDir;
        mapPy.set(newPacManGameMap(36, 28));
        titlePy.bind(Bindings.createStringBinding(
            () -> tt("map_editor") + (currentFilePy.get() != null ? " - " + currentFilePy.get() : ""),
            currentFilePy
        ));
        var url = Objects.requireNonNull(getClass().getResource("pacman_spritesheet.png"));
        spriteSheet = new Image(url.toExternalForm());
    }

    public WorldMap map() {
        return mapPy.get();
    }

    private WorldMap newPacManGameMap(int numRows, int numCols) {
        var map = new WorldMap(numRows, numCols);
        map.terrain().setProperty("color_wall_stroke", DEFAULT_COLOR_WALL_STROKE);
        map.terrain().setProperty("color_wall_fill",   DEFAULT_COLOR_WALL_FILL);
        map.terrain().setProperty("color_door",        DEFAULT_COLOR_DOOR);
        map.terrain().setProperty("pos_pac",           formatTile(DEFAULT_POS_PAC));
        map.terrain().setProperty("pos_ghost_1_red",     formatTile(DEFAULT_POS_RED_GHOST));
        map.terrain().setProperty("pos_ghost_2_pink",    formatTile(DEFAULT_POS_PINK_GHOST));
        map.terrain().setProperty("pos_ghost_3_cyan",    formatTile(DEFAULT_POS_CYAN_GHOST));
        map.terrain().setProperty("pos_ghost_4_orange",  formatTile(DEFAULT_POS_ORANGE_GHOST));
        map.terrain().setProperty("pos_bonus",         formatTile(DEFAULT_POS_BONUS));
        map.food().setProperty("color_food",           DEFAULT_FOOD_COLOR);
        addBorder(map.terrain(), 3, 2);
        GHOST_HOUSE_SHAPE.addToMap(map.terrain(), DEFAULT_POS_RED_GHOST.y() + 1, map.terrain().numCols() / 2 - 4);
        invalidateTerrainMapPaths();
        return map;
    }

    public void setMap(WorldMap worldMap) {
        checkNotNull(worldMap);
        mapPy.set(worldMap);
        foodMapPropertiesEditor.setTileMap(worldMap.food());
        terrainMapPropertiesEditor.setTileMap(worldMap.terrain());
        invalidateTerrainMapPaths();
        updateTerrainMapPaths();
        updateSourceHtml();
        Logger.debug("Edit canvas size: w={} h={}", editCanvas.getWidth(), editCanvas.getHeight());
    }

    public void createUI(Window ownerWindow) {
        this.ownerWindow = ownerWindow;

        terrainMapRenderer = new TileMapEditorTerrainRenderer();
        terrainMapRenderer.setWallStrokeColor(parseColor(DEFAULT_COLOR_WALL_STROKE));
        terrainMapRenderer.setWallFillColor(parseColor(DEFAULT_COLOR_WALL_FILL));

        foodMapRenderer = new FoodMapRenderer();
        foodMapRenderer.setPelletColor(TileMapUtil.parseColor(DEFAULT_FOOD_COLOR));
        foodMapRenderer.setEnergizerColor(TileMapUtil.parseColor(DEFAULT_FOOD_COLOR));

        createLayout();
        createMenus();

        // Note: this must be done after having loaded the initial map!
        editCanvas.heightProperty().bind(Bindings.createDoubleBinding(
                () -> (double) mapPy.get().terrain().numRows() * gridSize(), mapPy, gridSizePy));
        editCanvas.widthProperty().bind(Bindings.createDoubleBinding(
                () -> (double) mapPy.get().terrain().numCols() * gridSize(), mapPy, gridSizePy));
        previewCanvas.widthProperty().bind(editCanvas.widthProperty());
        previewCanvas.heightProperty().bind(editCanvas.heightProperty());

        // Cursor navigation
        editCanvas.setOnKeyPressed(e -> {
            Direction moveDir = switch (e.getCode()) {
                case LEFT -> Direction.LEFT;
                case RIGHT -> Direction.RIGHT;
                case UP -> Direction.UP;
                case DOWN -> Direction.DOWN;
                default -> null;
            };
            if (moveDir != null && focussedTilePy.get() != null) {
                Vector2i newTile = focussedTilePy.get().plus(moveDir.vector());
                if (!map().terrain().outOfBounds(newTile)) {
                    focussedTilePy.set(newTile);
                }
            }
        });


        // Active rendering
        int fps = 10;
        clock = new Timeline(fps, new KeyFrame(javafx.util.Duration.millis(1000.0 / fps), e -> {
            if (messageCloseTime != null && Instant.now().isAfter(messageCloseTime)) {
                messageCloseTime = null;
                FadeTransition fade = new FadeTransition(javafx.util.Duration.seconds(2));
                fade.setNode(messageLabel);
                fade.setFromValue(1);
                fade.setToValue(0.1);
                fade.play();
                fade.setOnFinished(event -> {
                    messageLabel.setText("");
                    messageLabel.setOpacity(1);
                });
            }
            try {
                draw();
            } catch (Exception x) {
                x.printStackTrace(System.err);
                drawBlueScreen(x);
            }
        }));
        clock.setCycleCount(Animation.INDEFINITE);
    }

    public void start() {
        // content pane and scroll pane have no height yet at this point!
        int gridSize = (int)(0.75 *  ownerWindow.getHeight() / map().terrain().numRows());
        gridSizePy.set(Math.max(gridSize, 8));
        clock.play();
        Logger.info("Window height {}", ownerWindow.getHeight());
        showMessage("Welcome to the tile map editor!", 3, MessageType.INFO);
    }

    public void stop() {
        clock.stop();
        editingEnabledPy.set(false);
        unsavedChanges = false;
    }

    public void showMessage(String message, long seconds, MessageType type) {
        messageLabel.setText(message);
        messageLabel.setFont(Font.font("sans", FontWeight.BOLD, 12));
        switch (type) {
            case INFO -> {
                messageLabel.setTextFill(Color.BLACK);
            }
            case WARNING -> {
                messageLabel.setTextFill(Color.GREEN);
            }
            case ERROR -> {
                messageLabel.setTextFill(Color.RED);

            }
        }
        messageCloseTime = Instant.now().plus(Duration.ofSeconds(seconds));
    }

    private Palette createTerrainPalette() {
        var terrainPalette = new Palette(32, 2, 10, terrainMapRenderer);
        terrainPalette.setTools(
            terrainPalette.createTileValueEditorTool(Tiles.WALL_H, "Horiz. Wall"),
            terrainPalette.createTileValueEditorTool(Tiles.WALL_V, "Vert. Wall"),
            terrainPalette.createTileValueEditorTool(Tiles.DWALL_H, "Hor. Double-Wall"),
            terrainPalette.createTileValueEditorTool(Tiles.DWALL_V, "Vert. Double-Wall"),
            terrainPalette.createTileValueEditorTool(Tiles.CORNER_NW, "NW Corner"),
            terrainPalette.createTileValueEditorTool(Tiles.CORNER_NE, "NE Corner"),
            terrainPalette.createTileValueEditorTool(Tiles.CORNER_SW, "SW Corner"),
            terrainPalette.createTileValueEditorTool(Tiles.CORNER_SE, "SE Corner"),
            terrainPalette.createTileValueEditorTool(Tiles.DCORNER_NW, "NW Corner"),
            terrainPalette.createTileValueEditorTool(Tiles.DCORNER_NE, "NE Corner"),
            terrainPalette.createTileValueEditorTool(Tiles.DCORNER_SW, "SW Corner"),
            terrainPalette.createTileValueEditorTool(Tiles.DCORNER_SE, "SE Corner"),
            terrainPalette.createTileValueEditorTool(Tiles.EMPTY, "Empty Space"),
            terrainPalette.createTileValueEditorTool(Tiles.TUNNEL, "Tunnel"),
            terrainPalette.createTileValueEditorTool(Tiles.DOOR, "Door")
        );
        terrainPalette.selectTool(12); // EMPTY
        return terrainPalette;
    }

    private Palette createActorPalette() {
        var actorPalette = new Palette(32, 1, 9, terrainMapRenderer);
        actorPalette.setTools(
            actorPalette.createPropertyValueEditorTool(PROPERTY_POS_RED_GHOST, "Red Ghost"),
            actorPalette.createPropertyValueEditorTool(PROPERTY_POS_PINK_GHOST, "Pink Ghost"),
            actorPalette.createPropertyValueEditorTool(PROPERTY_POS_CYAN_GHOST, "Cyan Ghost"),
            actorPalette.createPropertyValueEditorTool(PROPERTY_POS_ORANGE_GHOST, "Orange Ghost"),
            actorPalette.createPropertyValueEditorTool(PROPERTY_POS_SCATTER_RED_GHOST, "Red Ghost Scatter"),
            actorPalette.createPropertyValueEditorTool(PROPERTY_POS_SCATTER_PINK_GHOST, "Pink Ghost Scatter"),
            actorPalette.createPropertyValueEditorTool(PROPERTY_POS_SCATTER_CYAN_GHOST, "Cyan Ghost Scatter"),
            actorPalette.createPropertyValueEditorTool(PROPERTY_POS_SCATTER_ORANGE_GHOST, "Orange Ghost Scatter"),
            actorPalette.createPropertyValueEditorTool(PROPERTY_POS_PAC, "Pac-Man")
        );
        return actorPalette;
    }

    private Palette createFoodPalette() {
        var foodPalette = new Palette(32, 1, 3, foodMapRenderer);
        foodPalette.setTools(
            foodPalette.createTileValueEditorTool(Tiles.EMPTY, "No Food"),
            foodPalette.createTileValueEditorTool(Tiles.PELLET, "Pellet"),
            foodPalette.createTileValueEditorTool(Tiles.ENERGIZER, "Energizer")
        );
        foodPalette.selectTool(0); // EMPTY
        return foodPalette;
    }

    private void createLayout() {
        fileChooser = new FileChooser();
        var worldExtensionFilter = new FileChooser.ExtensionFilter("World Map Files", "*.world");
        var anyExtensionFilter = new FileChooser.ExtensionFilter("All Files", "*.*");
        fileChooser.getExtensionFilters().addAll(worldExtensionFilter, anyExtensionFilter);
        fileChooser.setSelectedExtensionFilter(worldExtensionFilter);
        fileChooser.setInitialDirectory(lastUsedDir);

        editCanvas = new Canvas();
        editCanvas.setOnMouseClicked(this::onMouseClickedOnEditCanvas);
        editCanvas.setOnMouseMoved(this::onMouseMovedOverEditCanvas);
        editCanvasScroll = new ScrollPane(editCanvas);
        editCanvasScroll.setFitToHeight(true);

        previewCanvas = new Canvas();
        previewCanvasScroll = new ScrollPane(previewCanvas);
        previewCanvasScroll.setFitToHeight(true);
        previewCanvasScroll.hvalueProperty().bindBidirectional(editCanvasScroll.hvalueProperty());
        previewCanvasScroll.vvalueProperty().bindBidirectional(editCanvasScroll.vvalueProperty());
        previewCanvasScroll.visibleProperty().bind(previewVisiblePy);

        mapSourceView = new WebView();
        var mapSourceViewScroll = new ScrollPane(mapSourceView);
        mapSourceViewScroll.setFitToHeight(true);

        palettes.put(PALETTE_TERRAIN, createTerrainPalette());
        palettes.put(PALETTE_ACTORS, createActorPalette());
        palettes.put(PALETTE_FOOD, createFoodPalette());

        var terrainPaletteTab = new Tab(tt("terrain"), palettes.get(PALETTE_TERRAIN));
        terrainPaletteTab.setClosable(false);
        terrainPaletteTab.setUserData(PALETTE_TERRAIN);

        var actorPaletteTab = new Tab(tt("actors"), palettes.get(PALETTE_ACTORS));
        actorPaletteTab.setClosable(false);
        actorPaletteTab.setUserData(PALETTE_ACTORS);

        var foodPaletteTab = new Tab(tt("pellets"), palettes.get(PALETTE_FOOD));
        foodPaletteTab.setClosable(false);
        foodPaletteTab.setUserData(PALETTE_FOOD);

        palettesTabPane = new TabPane(terrainPaletteTab, actorPaletteTab, foodPaletteTab);

        terrainMapPropertiesEditor = new PropertyEditorPane(this);
        terrainMapPropertiesEditor.enabledPy.bind(editingEnabledPy);
        terrainMapPropertiesEditor.setPadding(new Insets(10,0,0,0));

        foodMapPropertiesEditor = new PropertyEditorPane(this);
        foodMapPropertiesEditor.enabledPy.bind(editingEnabledPy);
        foodMapPropertiesEditor.setPadding(new Insets(10,0,0,0));

        var terrainPropertiesArea = new TitledPane();
        terrainPropertiesArea.setExpanded(false);
        terrainPropertiesArea.setText(tt("terrain"));
        terrainPropertiesArea.setContent(terrainMapPropertiesEditor);

        var foodPropertiesArea = new TitledPane();
        foodPropertiesArea.setExpanded(false);
        foodPropertiesArea.setText(tt("pellets"));
        foodPropertiesArea.setContent(foodMapPropertiesEditor);

        VBox controlsPane = new VBox();
        controlsPane.setSpacing(10);
        controlsPane.setMinWidth(32*10);
        controlsPane.getChildren().add(palettesTabPane);
        controlsPane.getChildren().add(new VBox(terrainPropertiesArea, foodPropertiesArea));

        focussedTileInfo = new Label();
        focussedTileInfo.setMinWidth(100);
        focussedTileInfo.setMaxWidth(100);

        messageLabel = new Label();
        messageLabel.setPadding(new Insets(0, 0, 0, 10));

        var filler = new Region();
        HBox.setHgrow(filler, Priority.ALWAYS);

        Slider sliderGridSize = new Slider(8, 48, 16);
        sliderGridSize.valueProperty().bindBidirectional(gridSizePy);
        sliderGridSize.setShowTickLabels(false);
        sliderGridSize.setShowTickMarks(true);
        sliderGridSize.setPrefWidth(250);

        var sliderContainer = new HBox(new Label("Zoom"), sliderGridSize);
        sliderContainer.setSpacing(5);

        var footer = new HBox(focussedTileInfo, messageLabel, filler, sliderContainer);
        footer.setPadding(new Insets(0, 10, 0, 10));

        var splitPane = new SplitPane(editCanvasScroll, previewCanvasScroll, mapSourceViewScroll);
        splitPane.setDividerPositions(0.45, 0.9);

        var hbox = new HBox(controlsPane, splitPane);
        HBox.setHgrow(splitPane, Priority.ALWAYS);
        contentPane.setCenter(hbox);
        contentPane.setBottom(footer);
    }

    private void createMenus() {
        createFileMenu();
        createEditMenu();
        createLoadMapMenu();
        createViewMenu();
        menuBar = new MenuBar(menuFile, menuEdit, menuLoadMap, menuView);
    }

    private void createFileMenu() {
        var miNew = new MenuItem(tt("menu.file.new"));
        miNew.setOnAction(e -> showNewMapDialog());

        var miOpen = new MenuItem(tt("menu.file.open"));
        miOpen.setOnAction(e -> openMapFile());

        var miSaveAs = new MenuItem(tt("menu.file.save_as"));
        miSaveAs.setOnAction(e -> saveMapFileAs());

        menuFile = new Menu(tt("menu.file"), null, miNew, miOpen, miSaveAs);
    }

    private void createEditMenu() {
        var miAddBorder = new MenuItem(tt("menu.edit.add_border"));
        miAddBorder.setOnAction(e -> {
            addBorder(map().terrain(), 3, 2);
            invalidateTerrainMapPaths();
            markMapEdited();
        });

        var miAddHouse = new MenuItem(tt("menu.edit.add_house"));
        miAddHouse.setOnAction(e -> {
            int row = map().terrain().numRows() / 2 - 3;
            int col = map().terrain().numCols() / 2 - 4;
            GHOST_HOUSE_SHAPE.addToMap(map().terrain(), row, col);
            invalidateTerrainMapPaths();
            markMapEdited();
        });

        var miClearTerrain = new MenuItem(tt("menu.edit.clear_terrain"));
        miClearTerrain.setOnAction(e -> {
            map().terrain().clear();
            invalidateTerrainMapPaths();
            markMapEdited();
        });

        var miClearFood = new MenuItem(tt("menu.edit.clear_food"));
        miClearFood.setOnAction(e -> {
            map().food().clear();
            markMapEdited();
        });

        menuEdit = new Menu(tt("menu.edit"), null, miAddBorder, miAddHouse, miClearTerrain, miClearFood);
        menuEdit.disableProperty().bind(editingEnabledPy.not());
    }

    private void createLoadMapMenu() {
        menuLoadMap = new Menu(tt("menu.load_map"));
        //menuLoadMap.disableProperty().bind(editingEnabledPy.not());
    }

    private void createViewMenu() {
        menuView = new Menu(tt("menu.view"));
        var miViewTerrain = new CheckMenuItem(tt("menu.view.terrain"));
        miViewTerrain.selectedProperty().bindBidirectional(terrainVisiblePy);
        var miViewFood = new CheckMenuItem(tt("menu.view.food"));
        miViewFood.selectedProperty().bindBidirectional(foodVisiblePy);
        var miViewGrid = new CheckMenuItem(tt("menu.view.grid"));
        miViewGrid.selectedProperty().bindBidirectional(gridVisiblePy);
        var miViewPreview = new CheckMenuItem(tt("menu.view.preview"));
        miViewPreview.selectedProperty().bindBidirectional(previewVisiblePy);
        menuView.getItems().setAll(miViewTerrain, miViewFood, miViewGrid, new SeparatorMenuItem(), miViewPreview);
    }

    public void addLoadMapMenuEntry(String description, WorldMap map) {
        checkNotNull(description);
        checkNotNull(map);
        var miLoadMap = new MenuItem(description);
        miLoadMap.setOnAction(e -> loadMap(map));
        menuLoadMap.getItems().add(miLoadMap);
    }

    private void updateTerrainMapPaths() {
        if (!pathsUpToDate) {
            map().terrain().computeTerrainPaths();
            pathsUpToDate = true;
        }
    }

    private void invalidateTerrainMapPaths() {
        pathsUpToDate = false;
    }

    public void markMapEdited() {
        unsavedChanges = true;
        updateSourceHtml();
    }

    public boolean hasUnsavedChanges() {
        return unsavedChanges;
    }

    public void loadMap(WorldMap worldMap) {
        checkNotNull(worldMap);
        if (unsavedChanges) {
            showConfirmation(this::saveMapFileAs, () -> {
                setMap(new WorldMap(worldMap));
                currentFilePy.set(null);
            });
        } else {
            setMap(new WorldMap(worldMap));
            currentFilePy.set(null);
        }
    }

    private void showNewMapDialog() {
        TextInputDialog dialog = new TextInputDialog("28x36");
        dialog.setTitle("New Map");
        dialog.setHeaderText("Enter Map Size (cols x rows)");
        dialog.setContentText("Map Size:");
        dialog.showAndWait().ifPresent(text -> {
            String[] tuple = text.split("x");
            try {
                int numCols = Integer.parseInt(tuple[0].trim());
                int numRows = Integer.parseInt(tuple[1].trim());
                setMap(newPacManGameMap(numRows, numCols));
            } catch (Exception x) {
                Logger.error(x);
            }
        });
    }

    private void openMapFile() {
        fileChooser.setTitle("Open Pac-Man Map");
        fileChooser.setInitialDirectory(lastUsedDir);
        File file = fileChooser.showOpenDialog(ownerWindow);
        if (file != null) {
            readMapFile(file);
        }
    }

    private void readMapFile(File file) {
        if (file.getName().endsWith(".world")) {
            loadMap(new WorldMap(file));
            lastUsedDir = file.getParentFile();
            currentFilePy.set(file);
            Logger.info("Map read from file {}", file);
        }
    }

    public void saveMapFileAs() {
        fileChooser.setTitle("Save Pac-Man Map");
        fileChooser.setInitialDirectory(lastUsedDir);
        File file = fileChooser.showSaveDialog(ownerWindow);
        if (file != null) {
            lastUsedDir = file.getParentFile();
            if (file.getName().endsWith(".world")) {
                map().save(file);
                unsavedChanges = false;
                readMapFile(file);
            } else {
                Logger.error("No .world file selected"); //TODO
            }
        }
    }

    public void showConfirmation(Runnable saveAction, Runnable dontSaveAction) {
        if (hasUnsavedChanges()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("There are unsaved changes");
            alert.setHeaderText("Save changes?");
            alert.setContentText("You can save your changes or leave without saving");
            var saveChoice = new ButtonType("Save Changes");
            var dontSaveChoice = new ButtonType("Don't Save");
            var cancelChoice = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(saveChoice, dontSaveChoice, cancelChoice);
            alert.showAndWait().ifPresent(choice -> {
                if (choice == saveChoice) {
                    saveAction.run();
                } else if (choice == dontSaveChoice) {
                    dontSaveAction.run();
                } else if (choice == cancelChoice) {
                    alert.close();
                }
            });
        } else {
            stop();
            dontSaveAction.run();
        }
    }

    public Pane getContentPane() {
        return contentPane;
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public Menu menuFile() {
        return menuFile;
    }

    public Menu menuLoadMap() {
        return menuLoadMap;
    }

    /**
     * @return pixels used by one tile at current window zoom
     */
    private int gridSize() {
        return gridSizePy.get();
    }

    /**
     * @param pixels number of pixels
     * @return number of full tiles spanned by pixels
     */
    private int fullTiles(double pixels) {
        return (int) (pixels / gridSize());
    }

    // TODO use own canvas or Text control
    private void drawBlueScreen(Exception drawException) {
        GraphicsContext g = editCanvas.getGraphicsContext2D();
        g.setFill(Color.BLUE);
        g.fillRect(0, 0, editCanvas.getWidth(), editCanvas.getHeight());
        g.setStroke(Color.WHITE);
        g.setFont(Font.font("Monospace", 12));
        try {
            Logger.error(drawException);
            var trace = new StringWriter();
            drawException.printStackTrace(new PrintWriter(trace));
            g.strokeText(trace.toString(), 0, 20);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            Logger.error(e);
        }
    }

    private void drawEditingHint(GraphicsContext g) {
        double x = 16;
        double y = 24;
        String text = tt("click_to_start");
        g.setFont(Font.font("Sans", FontWeight.BLACK, 18));
        g.setStroke(Color.LIGHTGREEN);
        g.setLineWidth(3);
        g.strokeText(text, x, y);
        g.setFill(Color.DARKGREEN);
        g.fillText(text, x, y);
    }

    private void drawEditCanvas() {
        GraphicsContext g = editCanvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, editCanvas.getWidth(), editCanvas.getHeight());
        drawGrid(g);
        if (terrainVisiblePy.get()) {
            TileMap terrainMap = map().terrain();
            terrainMapRenderer.setScaling(gridSize() / 8.0);
            terrainMapRenderer.setWallStrokeColor(getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_STROKE, parseColor(DEFAULT_COLOR_WALL_STROKE)));
            terrainMapRenderer.setWallFillColor(getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_FILL, parseColor(DEFAULT_COLOR_WALL_FILL)));
            terrainMapRenderer.setDoorColor(getColorFromMap(terrainMap, PROPERTY_COLOR_DOOR, parseColor(DEFAULT_COLOR_DOOR)));
            terrainMapRenderer.setRuntimeMode(false);
            terrainMapRenderer.drawMap(g, terrainMap);
        }
        if (foodVisiblePy.get()) {
            Color foodColor = getColorFromMap(map().food(), PROPERTY_COLOR_FOOD, TileMapUtil.parseColor(DEFAULT_FOOD_COLOR));
            foodMapRenderer.setScaling(gridSize() / 8.0);
            foodMapRenderer.setEnergizerColor(foodColor);
            foodMapRenderer.setPelletColor(foodColor);
            foodMapRenderer.drawMap(g, map().food());
        }
        if (!editingEnabledPy.get()) {
            drawEditingHint(g);
        }
        if (focussedTilePy.get() != null) {
            double tilePx = gridSize();
            g.setStroke(Color.YELLOW);
            g.setLineWidth(1);
            g.strokeRect(focussedTilePy.get().x() * tilePx, focussedTilePy.get().y() * tilePx, tilePx, tilePx);
        }
    }

    private void drawPreviewCanvas() {
        GraphicsContext g = previewCanvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
        if (terrainVisiblePy.get()) {
            updateTerrainMapPaths();
            TileMap terrainMap = map().terrain();
            terrainMapRenderer.setScaling(gridSize() / 8.0);
            terrainMapRenderer.setWallStrokeColor(getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_STROKE, parseColor(DEFAULT_COLOR_WALL_STROKE)));
            terrainMapRenderer.setWallFillColor(getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_FILL, parseColor(DEFAULT_COLOR_WALL_FILL)));
            terrainMapRenderer.setDoorColor(getColorFromMap(terrainMap, PROPERTY_COLOR_DOOR, parseColor(DEFAULT_COLOR_DOOR)));
            terrainMapRenderer.setRuntimeMode(true);
            terrainMapRenderer.drawMap(g, terrainMap);
        }
        if (foodVisiblePy.get()) {
            foodMapRenderer.setScaling(gridSize() / 8.0);
            Color foodColor = getColorFromMap(map().food(), PROPERTY_COLOR_FOOD, TileMapUtil.parseColor(DEFAULT_FOOD_COLOR));
            foodMapRenderer.setEnergizerColor(foodColor);
            foodMapRenderer.setPelletColor(foodColor);
            foodMapRenderer.drawMap(g, map().food());
        }
        drawSprite(g, PROPERTY_POS_PAC, PAC_SPRITE);
        drawSprite(g, PROPERTY_POS_RED_GHOST, RED_GHOST_SPRITE);
        drawSprite(g, PROPERTY_POS_PINK_GHOST, PINK_GHOST_SPRITE);
        drawSprite(g, PROPERTY_POS_CYAN_GHOST, CYAN_GHOST_SPRITE);
        drawSprite(g, PROPERTY_POS_ORANGE_GHOST, ORANGE_GHOST_SPRITE);
        drawSprite(g, PROPERTY_POS_BONUS, BONUS_SPRITE);
    }

    private void drawSprite(GraphicsContext g, String propertyName, Rectangle2D sprite) {
        var tile = getTileFromMap(map().terrain(), propertyName, null);
        if (tile != null) {
            drawSprite(g, sprite, tile.x() * gridSize() + 0.5 * gridSize(), tile.y() * gridSize(), 1.75 * gridSize(), 1.75 * gridSize());
        }
    }

    private void drawSprite(GraphicsContext g, Rectangle2D sprite, double x, double y, double w, double h) {
        double ox = 0.5 * (w - gridSize());
        double oy = 0.5 * (h - gridSize());
        g.drawImage(spriteSheet,
            sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(),
            x - ox, y - oy, w, h
        );
    }

    private void draw() {
        try {
            drawEditCanvas();
            drawPreviewCanvas();
            palettes.get(selectedPaletteID()).draw();
        } catch (Exception x) {
            Logger.error("Exception while drawing: {}", x);
        }
    }

    private void drawGrid(GraphicsContext g) {
        if (gridVisiblePy.get()) {
            g.save();
            g.setStroke(Color.LIGHTGRAY);
            g.setLineWidth(0.25);
            double gridSize = gridSize();
            for (int row = 1; row < map().terrain().numRows(); ++row) {
                g.strokeLine(0, row * gridSize, editCanvas.getWidth(), row * gridSize);
            }
            for (int col = 1; col < map().terrain().numCols(); ++col) {
                g.strokeLine(col * gridSize, 0, col * gridSize, editCanvas.getHeight());
            }
            g.restore();
        }
    }

    private Vector2i tileAtMousePosition(double mouseX, double mouseY) {
        return new Vector2i(fullTiles(mouseX), fullTiles(mouseY));
    }

    private String selectedPaletteID() {
        return (String) palettesTabPane.getSelectionModel().getSelectedItem().getUserData();
    }

    private Palette selectedPalette() {
        return palettes.get(selectedPaletteID());
    }

    private void onMouseClickedOnEditCanvas(MouseEvent e) {
        if (!editingEnabledPy.get() && e.getClickCount() == 2) {
            editingEnabledPy.set(true);
            editCanvas.requestFocus();
            return;
        }
        switch (selectedPaletteID()) {
            case PALETTE_TERRAIN -> editMapTile(map().terrain(), tv -> 0 <= tv && tv <= Tiles.DOOR, e);
            case PALETTE_ACTORS  -> {
                if (selectedPalette().isToolSelected()) {
                    Vector2i tile = tileAtMousePosition(e.getX(), e.getY());
                    selectedPalette().selectedTool().apply(map().terrain(), tile);
                    markMapEdited();
                    terrainMapPropertiesEditor.updatePropertyEditorValues();
                }
            }
            case PALETTE_FOOD -> editMapTile(map().food(), tv -> 0 <= tv && tv <= Tiles.ENERGIZER, e);
            default -> Logger.error("Unknown palette selection");
        }
    }

    private void onMouseMovedOverEditCanvas(MouseEvent e) {
        focussedTilePy.set(tileAtMousePosition(e.getX(), e.getY()));
        if (!editingEnabledPy.get()) {
            return;
        }
        if (e.isShiftDown()) {
            switch (selectedPaletteID()) {
                case PALETTE_TERRAIN -> {
                    if (selectedPalette().isToolSelected()) {
                        selectedPalette().selectedTool().apply(map().terrain(), focussedTilePy.get());
                    }
                    markMapEdited();
                    invalidateTerrainMapPaths();
                }
                case PALETTE_FOOD -> {
                    if (selectedPalette().isToolSelected()) {
                        selectedPalette().selectedTool().apply(map().food(), focussedTilePy.get());
                    }
                    markMapEdited();
                }
                default -> {}
            }
        }
    }

    private void editMapTile(TileMap tileMap, Predicate<Byte> valueAllowed, MouseEvent e) {
        var tile = tileAtMousePosition(e.getX(), e.getY());
        if (e.getButton() == MouseButton.SECONDARY) {
            tileMap.set(tile, Tiles.EMPTY);
        }
        else if (e.isShiftDown()) {
            // cycle through all allowed values
            byte value = tileMap.get(tile);
            byte next = (byte) (value + 1);
            byte newValue =  valueAllowed.test(next) ? next : 0;
            tileMap.set(tile, newValue);
        }
        else if (selectedPalette().isToolSelected()) {
            selectedPalette().selectedTool().apply(tileMap, tile);
        }
        invalidateTerrainMapPaths();
        markMapEdited();
    }

    private void updateSourceHtml() {
        mapSourceView.getEngine().loadContent(map().htmlText());
    }
}