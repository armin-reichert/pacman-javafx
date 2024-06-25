/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.Tiles;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.tilemap.TileMap.formatTile;
import static de.amr.games.pacman.lib.tilemap.TileMap.parseVector2i;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.mapeditor.TileMapUtil.getColorFromMap;
import static de.amr.games.pacman.mapeditor.TileMapUtil.parseColor;

/**
 * @author Armin Reichert
 */
public class TileMapEditor  {

    private static final ResourceBundle TEXTS = ResourceBundle.getBundle("de.amr.games.pacman.mapeditor.texts");

    public static final Rectangle2D PAC_SPRITE = new Rectangle2D(473, 16, 14, 14);
    public static final Rectangle2D RED_GHOST_SPRITE = new Rectangle2D(505, 65, 14, 14);
    public static final Rectangle2D PINK_GHOST_SPRITE = new Rectangle2D(553, 81, 14, 14);
    public static final Rectangle2D CYAN_GHOST_SPRITE = new Rectangle2D(521, 97, 14, 14);
    public static final Rectangle2D ORANGE_GHOST_SPRITE = new Rectangle2D(521, 113, 14, 14);

    public static String tt(String key, Object... args) {
        return MessageFormat.format(TEXTS.getString(key), args);
    }

    private static final String PALETTE_TERRAIN = "Terrain";
    private static final String PALETTE_ACTORS  = "Actors";
    private static final String PALETTE_FOOD    = "Food";

    private static WorldMap createNewMap(int numRows, int numCols) {
        var map = new WorldMap(numRows, numCols);
        map.terrain().setProperty(PROPERTY_COLOR_WALL_STROKE, DEFAULT_COLOR_WALL_STROKE);
        map.terrain().setProperty(PROPERTY_COLOR_WALL_FILL, DEFAULT_COLOR_WALL_FILL);
        map.terrain().setProperty(PROPERTY_COLOR_DOOR, DEFAULT_COLOR_DOOR);
        map.terrain().setProperty(PROPERTY_POS_PAC,          formatTile(DEFAULT_POS_PAC));
        map.terrain().setProperty(PROPERTY_POS_RED_GHOST,    formatTile(DEFAULT_POS_RED_GHOST));
        map.terrain().setProperty(PROPERTY_POS_PINK_GHOST,   formatTile(DEFAULT_POS_PINK_GHOST));
        map.terrain().setProperty(PROPERTY_POS_CYAN_GHOST,   formatTile(DEFAULT_POS_CYAN_GHOST));
        map.terrain().setProperty(PROPERTY_POS_ORANGE_GHOST, formatTile(DEFAULT_POS_ORANGE_GHOST));
        map.terrain().setProperty(PROPERTY_POS_BONUS,        formatTile(DEFAULT_POS_BONUS));
        map.food().setProperty(PROPERTY_COLOR_FOOD, DEFAULT_FOOD_COLOR);
        return map;
    }

    private static final RectShape GHOST_HOUSE_SHAPE = new RectShape(new byte[][] {
        { 0, 0, 0,20, 0, 0, 0, 0},
        {10, 8, 8,14,14, 8, 8,11},
        { 9, 0, 0, 0, 0, 0, 0, 9},
        { 9,22, 0,21, 0,23, 0, 9},
        { 9, 0, 0, 0, 0, 0, 0, 9},
        {13, 8, 8, 8, 8, 8, 8,12}
    });

    public final ObjectProperty<String> titlePy = new SimpleObjectProperty<>(this, "title");

    public final BooleanProperty terrainVisiblePy = new SimpleBooleanProperty(this, "terrainVisible", true);

    public final BooleanProperty foodVisiblePy = new SimpleBooleanProperty(this, "foodVisible", true);

    public final BooleanProperty gridVisiblePy = new SimpleBooleanProperty(this, "gridVisible", true);

    public final BooleanProperty editingEnabledPy = new SimpleBooleanProperty(this,"editingEnabled", false);

    public final BooleanProperty previewVisiblePy = new SimpleBooleanProperty(this, "previewVisible", true);

    public final IntegerProperty gridSizePy = new SimpleIntegerProperty(this, "gridSize", 16) {
        @Override
        protected void invalidated() {
            invalidatePaths();
        }
    };

    public final ObjectProperty<File> currentFilePy = new SimpleObjectProperty<>(this, "currentFile");

    public final ObjectProperty<WorldMap> mapPy = new SimpleObjectProperty<>(this, "map");

    private Window ownerWindow;
    private MenuBar menuBar;
    private Menu menuFile;
    private Menu menuEdit;
    private Menu menuLoadMap;
    private final BorderPane layout = new BorderPane();
    private Canvas editCanvas;
    private Canvas previewCanvas;
    private Label hoveredTileInfo;
    private FileChooser fileChooser;
    private TabPane palettesTabPane;
    private final Text editHint = new Text(tt("click_to_start"));
    private PropertyEditor terrainMapPropertiesEditor;
    private TileMapEditorTerrainRenderer terrainMapRenderer;
    private PropertyEditor foodMapPropertiesEditor;
    private FoodMapRenderer foodMapRenderer;

    private final Map<String, Palette> palettes = new HashMap<>();
    private final Map<String, WorldMap> predefinedMaps = new HashMap<>();

    private boolean pathsUpToDate;
    private boolean unsavedChanges;
    private Vector2i hoveredTile;
    private File lastUsedDir;

    private Timeline clock;

    private final Image spriteSheet;

    public TileMapEditor() {
        this(new File(System.getProperty("user.home")));
    }

    public TileMapEditor(File workDir) {
        lastUsedDir = workDir;
        mapPy.set(createNewMap(36, 28));
        titlePy.bind(Bindings.createStringBinding(
            () -> tt("map_editor") + (currentFilePy.get() != null ? " - " + currentFilePy.get() : ""),
            currentFilePy
        ));
        spriteSheet = new Image(getClass().getResource("pacman_spritesheet.png").toExternalForm());
    }

    public WorldMap map() {
        return mapPy.get();
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
                () -> (double) mapPy.get().numRows() * gridSize(), mapPy, gridSizePy));
        editCanvas.widthProperty().bind(Bindings.createDoubleBinding(
                () -> (double) mapPy.get().numCols() * gridSize(), mapPy, gridSizePy));
        previewCanvas.widthProperty().bind(editCanvas.widthProperty());
        previewCanvas.heightProperty().bind(editCanvas.heightProperty());

        // Active rendering
        int fps = 5;
        clock = new Timeline(fps, new KeyFrame(Duration.millis(1000.0 / fps), e -> {
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
        clock.play();
    }

    public void stop() {
        clock.stop();
        editingEnabledPy.set(false);
        unsavedChanges = false;
    }

    private Palette createTerrainPalette() {
        var terrainPalette = new Palette(32, 2, 10, terrainMapRenderer);
        terrainPalette.setTools(
            terrainPalette.changeTileValueTool(Tiles.WALL_H, "Horiz. Wall"),
            terrainPalette.changeTileValueTool(Tiles.WALL_V, "Vert. Wall"),
            terrainPalette.changeTileValueTool(Tiles.DWALL_H, "Hor. Double-Wall"),
            terrainPalette.changeTileValueTool(Tiles.DWALL_V, "Vert. Double-Wall"),
            terrainPalette.changeTileValueTool(Tiles.CORNER_NW, "NW Corner"),
            terrainPalette.changeTileValueTool(Tiles.CORNER_NE, "NE Corner"),
            terrainPalette.changeTileValueTool(Tiles.CORNER_SW, "SW Corner"),
            terrainPalette.changeTileValueTool(Tiles.CORNER_SE, "SE Corner"),
            terrainPalette.changeTileValueTool(Tiles.DCORNER_NW, "NW Corner"),
            terrainPalette.changeTileValueTool(Tiles.DCORNER_NE, "NE Corner"),
            terrainPalette.changeTileValueTool(Tiles.DCORNER_SW, "SW Corner"),
            terrainPalette.changeTileValueTool(Tiles.DCORNER_SE, "SE Corner"),
            terrainPalette.changeTileValueTool(Tiles.EMPTY, "Empty Space"),
            terrainPalette.changeTileValueTool(Tiles.TUNNEL, "Tunnel"),
            terrainPalette.changeTileValueTool(Tiles.DOOR, "Door")
        );
        return terrainPalette;
    }

    private Palette createActorPalette() {
        var actorPalette = new Palette(32, 1, 9, terrainMapRenderer);
        actorPalette.setTools(
            actorPalette.changePropertyValueTool(PROPERTY_POS_RED_GHOST, "Red Ghost"),
            actorPalette.changePropertyValueTool(PROPERTY_POS_PINK_GHOST, "Pink Ghost"),
            actorPalette.changePropertyValueTool(PROPERTY_POS_CYAN_GHOST, "Cyan Ghost"),
            actorPalette.changePropertyValueTool(PROPERTY_POS_ORANGE_GHOST, "Orange Ghost"),
            actorPalette.changePropertyValueTool(PROPERTY_POS_SCATTER_RED_GHOST, "Red Ghost Scatter"),
            actorPalette.changePropertyValueTool(PROPERTY_POS_SCATTER_PINK_GHOST, "Pink Ghost Scatter"),
            actorPalette.changePropertyValueTool(PROPERTY_POS_SCATTER_CYAN_GHOST, "Cyan Ghost Scatter"),
            actorPalette.changePropertyValueTool(PROPERTY_POS_SCATTER_ORANGE_GHOST, "Orange Ghost Scatter"),
            actorPalette.changePropertyValueTool(PROPERTY_POS_PAC, "Pac-Man")
        );
        return actorPalette;
    }

    private Palette createFoodPalette() {
        var foodPalette = new Palette(32, 1, 3, foodMapRenderer);
        foodPalette.setTools(
            foodPalette.changeTileValueTool(Tiles.EMPTY, "No Food"),
            foodPalette.changeTileValueTool(Tiles.PELLET, "Pellet"),
            foodPalette.changeTileValueTool(Tiles.ENERGIZER, "Energizer")
        );
        return foodPalette;
    }

    private void createLayout() {
        fileChooser = new FileChooser();
        var worldExtensionFilter = new FileChooser.ExtensionFilter("World Map Files", "*.world");
        fileChooser.getExtensionFilters().add(worldExtensionFilter);
        fileChooser.setSelectedExtensionFilter(worldExtensionFilter);
        fileChooser.setInitialDirectory(lastUsedDir);

        editCanvas = new Canvas();
        editCanvas.setOnMouseClicked(this::onMouseClickedOnEditCanvas);
        editCanvas.setOnMouseMoved(this::onMouseMovedOverEditCanvas);

        previewCanvas = new Canvas();

        var editCanvasScroll = new ScrollPane(editCanvas);
        editCanvasScroll.setFitToHeight(true);

        var previewCanvasScroll = new ScrollPane(previewCanvas);
        previewCanvasScroll.setFitToHeight(true);
        previewCanvasScroll.vvalueProperty().bindBidirectional(editCanvasScroll.vvalueProperty());
        previewCanvasScroll.visibleProperty().bind(previewVisiblePy);

        var cbPreviewVisible = new CheckBox(tt("show_preview"));
        cbPreviewVisible.selectedProperty().bindBidirectional(previewVisiblePy);

        var cbTerrainVisible = new CheckBox(tt("terrain"));
        cbTerrainVisible.selectedProperty().bindBidirectional(terrainVisiblePy);

        var cbFoodVisible = new CheckBox(tt("pellets"));
        cbFoodVisible.selectedProperty().bindBidirectional(foodVisiblePy);

        var cbGridVisible = new CheckBox(tt("grid"));
        cbGridVisible.selectedProperty().bindBidirectional(gridVisiblePy);

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

        terrainMapPropertiesEditor = new PropertyEditor(tt("terrain"), this);
        terrainMapPropertiesEditor.enabledPy.bind(editingEnabledPy);
        terrainMapPropertiesEditor.setPadding(new Insets(10,0,0,0));

        foodMapPropertiesEditor = new PropertyEditor(tt("pellets"), this);
        foodMapPropertiesEditor.enabledPy.bind(editingEnabledPy);
        foodMapPropertiesEditor.setPadding(new Insets(10,0,0,0));

        var propertyEditorArea = new TitledPane();
        propertyEditorArea.setText(tt("properties"));
        propertyEditorArea.setContent(new VBox(terrainMapPropertiesEditor, foodMapPropertiesEditor));

        VBox controlsPane = new VBox();
        controlsPane.setSpacing(10);
        controlsPane.setMinWidth(32*10);
        HBox checkBoxPanel = new HBox(5, cbTerrainVisible, cbFoodVisible, cbGridVisible, cbPreviewVisible);
        cbPreviewVisible.setPadding(new Insets(0, 0, 0, 25));
        checkBoxPanel.setAlignment(Pos.CENTER);
        controlsPane.getChildren().add(checkBoxPanel);
        controlsPane.getChildren().add(palettesTabPane);
        controlsPane.getChildren().add(propertyEditorArea);

        hoveredTileInfo = new Label();

        var filler = new Region();
        HBox.setHgrow(filler, Priority.ALWAYS);

        Slider sliderGridSize = new Slider(8, 48, 16);
        sliderGridSize.valueProperty().bindBidirectional(gridSizePy);
        sliderGridSize.setTooltip(new Tooltip("Grid Size"));
        sliderGridSize.setShowTickLabels(false);
        sliderGridSize.setShowTickMarks(true);
        sliderGridSize.setPrefWidth(250);

        var footer = new HBox(hoveredTileInfo, filler, sliderGridSize);
        footer.setPadding(new Insets(0, 10, 0, 10));

        var splitPane = new SplitPane(editCanvasScroll, previewCanvasScroll);
        splitPane.setDividerPositions(0.5);

        var hbox = new HBox(controlsPane, splitPane);
        HBox.setHgrow(splitPane, Priority.ALWAYS);
        layout.setCenter(hbox);
        layout.setBottom(footer);
    }

    private void createMenus() {
        createFileMenu();
        createEditMenu();
        createLoadMapMenu();
        menuBar = new MenuBar(menuFile, menuEdit, menuLoadMap);
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
        miAddBorder.setOnAction(e -> addBorder(map().terrain(), 3, 2));

        var miAddHouse = new MenuItem(tt("menu.edit.add_house"));
        miAddHouse.setOnAction(e -> addHouse());

        var miClearTerrain = new MenuItem(tt("menu.edit.clear_terrain"));
        miClearTerrain.setOnAction(e -> map().terrain().clear());

        var miClearFood = new MenuItem(tt("menu.edit.clear_food"));
        miClearFood.setOnAction(e -> map().food().clear());

        menuEdit = new Menu(tt("menu.edit"), null, miAddBorder, miAddHouse, miClearTerrain, miClearFood);
        menuEdit.disableProperty().bind(editingEnabledPy.not());
    }

    private void createLoadMapMenu() {
        menuLoadMap = new Menu(tt("menu.load_map"));
        menuLoadMap.disableProperty().bind(editingEnabledPy.not());
    }

    public void addPredefinedMap(String description, WorldMap map) {
        checkNotNull(description);
        checkNotNull(map);
        predefinedMaps.put(description, map);
        var miLoadMap = new MenuItem(description);
        miLoadMap.setOnAction(e -> loadMap(map));
        menuLoadMap.getItems().add(miLoadMap);
    }

    public WorldMap getPredefinedMap(String description) {
        if (!predefinedMaps.containsKey(description)) {
            Logger.error("No predefind map '{}' exists", description);
            return null;
        }
        return predefinedMaps.get(description);
    }

    private void updatePaths() {
        if (!pathsUpToDate) {
            map().terrain().computePaths();
            pathsUpToDate = true;
        }
    }

    private void invalidatePaths() {
        pathsUpToDate = false;
    }

    public void markMapEdited() {
        unsavedChanges = true;
    }

    public boolean hasUnsavedChanges() {
        return unsavedChanges;
    }

    private void addHouse() {
        int row = map().numRows() / 2 - 4;
        int col = map().numCols() / 2 - 4;
        GHOST_HOUSE_SHAPE.addToMap(map().terrain(), row, col);
        invalidatePaths();
        markMapEdited();
    }

    private void addBorder(TileMap terrain, int emptyRowsTop, int emptyRowsBottom) {
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

        invalidatePaths();
        markMapEdited();
    }

    public void setMap(WorldMap other) {
        checkNotNull(other);
        mapPy.set(other);
        foodMapPropertiesEditor.edit(map().food());
        terrainMapPropertiesEditor.edit(map().terrain());
        invalidatePaths();
        updatePaths();
        Logger.debug("Edit canvas size: w={} h={}", editCanvas.getWidth(), editCanvas.getHeight());
    }

    public void loadMap(WorldMap otherMap) {
        checkNotNull(otherMap);
        if (hasUnsavedChanges()) {
            showConfirmation(this::saveMapFileAs, () -> {
                setMap(new WorldMap(otherMap));
                currentFilePy.set(null);
            });
        } else {
            setMap(new WorldMap(otherMap));
            currentFilePy.set(null);
        }
    }

    private void showNewMapDialog() {
        TextInputDialog dialog = new TextInputDialog("28x36");
        dialog.setTitle("Map Size");
        dialog.setHeaderText("Enter Map Size (cols x rows)");
        dialog.setContentText("Map Size:");
        dialog.showAndWait().ifPresent(text -> {
            String[] tuple = text.split("x");
            try {
                int numCols = Integer.parseInt(tuple[0].trim());
                int numRows = Integer.parseInt(tuple[1].trim());
                var newMap = createNewMap(numRows, numCols);
                setMap(newMap);
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
                }
            });
        } else {
            stop();
            dontSaveAction.run();
        }
    }

    public Pane getLayout() {
        return layout;
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
        editHint.setFont(Font.font("Sans", FontWeight.BLACK, 18));
        double x = 16;
        double y = 24;
        g.setFont(editHint.getFont());
        g.setStroke(Color.LIGHTGREEN);
        g.setLineWidth(3);
        g.strokeText(editHint.getText(), x, y);
        g.setFill(Color.DARKGREEN);
        g.fillText(editHint.getText(), x, y);
    }

    private void drawEditCanvas() {
        GraphicsContext g = editCanvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, editCanvas.getWidth(), editCanvas.getHeight());
        drawGrid(g);
        if (terrainVisiblePy.get()) {
            terrainMapRenderer.setScaling(gridSize() / 8.0);
            terrainMapRenderer.setWallStrokeColor(getColorFromMap(map().terrain(), PROPERTY_COLOR_WALL_STROKE, parseColor(DEFAULT_COLOR_WALL_STROKE)));
            terrainMapRenderer.setWallFillColor(getColorFromMap(map().terrain(), PROPERTY_COLOR_WALL_FILL, parseColor(DEFAULT_COLOR_WALL_FILL)));
            terrainMapRenderer.setDoorColor(getColorFromMap(map().terrain(), PROPERTY_COLOR_DOOR, parseColor(DEFAULT_COLOR_DOOR)));
            terrainMapRenderer.setRuntimeMode(false);
            terrainMapRenderer.drawMap(g, map().terrain());
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
        } else {
            if (hoveredTile != null) {
                double tilePx = gridSize();
                g.setStroke(Color.YELLOW);
                g.setLineWidth(1);
                g.strokeRect(hoveredTile.x() * tilePx, hoveredTile.y() * tilePx, tilePx, tilePx);
            }
        }
    }

    private void drawPreviewCanvas() {
        GraphicsContext g = previewCanvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
        if (terrainVisiblePy.get()) {
            updatePaths();
            terrainMapRenderer.setScaling(gridSize() / 8.0);
            terrainMapRenderer.setWallStrokeColor(getColorFromMap(map().terrain(), PROPERTY_COLOR_WALL_STROKE, parseColor(DEFAULT_COLOR_WALL_STROKE)));
            terrainMapRenderer.setWallFillColor(getColorFromMap(map().terrain(), PROPERTY_COLOR_WALL_FILL, parseColor(DEFAULT_COLOR_WALL_FILL)));
            terrainMapRenderer.setDoorColor(getColorFromMap(map().terrain(), PROPERTY_COLOR_DOOR, parseColor(DEFAULT_COLOR_DOOR)));
            terrainMapRenderer.setRuntimeMode(true);
            terrainMapRenderer.drawMap(g, map().terrain());
        }
        if (foodVisiblePy.get()) {
            foodMapRenderer.setScaling(gridSize() / 8.0);
            Color foodColor = getColorFromMap(map().food(), PROPERTY_COLOR_FOOD, TileMapUtil.parseColor(DEFAULT_FOOD_COLOR));
            foodMapRenderer.setEnergizerColor(foodColor);
            foodMapRenderer.setPelletColor(foodColor);
            foodMapRenderer.drawMap(g, map().food());
        }
        drawActor(g, PROPERTY_POS_PAC, PAC_SPRITE);
        drawActor(g, PROPERTY_POS_RED_GHOST, RED_GHOST_SPRITE);
        drawActor(g, PROPERTY_POS_PINK_GHOST, PINK_GHOST_SPRITE);
        drawActor(g, PROPERTY_POS_CYAN_GHOST, CYAN_GHOST_SPRITE);
        drawActor(g, PROPERTY_POS_ORANGE_GHOST, ORANGE_GHOST_SPRITE);
    }

    private void drawActor(GraphicsContext g, String propertyName, Rectangle2D sprite) {
        var tile = TileMapUtil.getTileFromMap(map().terrain(), propertyName, null);
        if (tile != null) {
            drawSprite(g, sprite, tile.x()*gridSize() + 0.5*gridSize(), tile.y()*gridSize(), 1.8*gridSize(), 1.8*gridSize());
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

    private void onMouseClickedOnEditCanvas(MouseEvent e) {
        if (!editingEnabledPy.get()) {
            editingEnabledPy.set(true);
            return;
        }
        switch (selectedPaletteID()) {
            case PALETTE_TERRAIN -> editMapTile(map().terrain(), Tiles.TERRAIN_TILES_END, e);
            case PALETTE_ACTORS  -> {
                if (selectedPalette().selectedTool != null) {
                    Vector2i tile = tileAtMousePosition(e.getX(), e.getY());
                    selectedPalette().selectedTool.apply(map().terrain(), tile);
                    markMapEdited();
                    terrainMapPropertiesEditor.updateEditorValues();
                }
            }
            case PALETTE_FOOD -> editMapTile(map().food(), Tiles.FOOD_TILES_END, e);
            default -> Logger.error("Unknown palette selection");
        }
    }

    private String selectedPaletteID() {
        return (String) palettesTabPane.getSelectionModel().getSelectedItem().getUserData();
    }

    private Palette selectedPalette() {
        return palettes.get(selectedPaletteID());
    }

    private void setHoveredTile(Vector2i tile) {
        hoveredTile = tile;
        var text = "Tile: ";
        text += hoveredTile != null ? String.format("x=%2d y=%2d", hoveredTile.x(), hoveredTile.y()) : "n/a";
        hoveredTileInfo.setText(text);
    }

    private void onMouseMovedOverEditCanvas(MouseEvent e) {
        if (!editingEnabledPy.get()) {
            return;
        }
        setHoveredTile(tileAtMousePosition(e.getX(), e.getY()));
        if (e.isShiftDown()) {
            switch (selectedPaletteID()) {
                case PALETTE_TERRAIN -> {
                    if (selectedPalette().selectedTool != null) {
                        selectedPalette().selectedTool.apply(map().terrain(), hoveredTile);
                    }
                    markMapEdited();
                    invalidatePaths();
                }
                case PALETTE_FOOD -> {
                    if (selectedPalette().selectedTool != null) {
                        selectedPalette().selectedTool.apply(map().food(), hoveredTile);
                    }
                    markMapEdited();
                }
                default -> {}
            }
        }
    }

    private void editMapTile(TileMap tileMap, byte endValue, MouseEvent e) {
        var tile = tileAtMousePosition(e.getX(), e.getY());
        if (e.getButton() == MouseButton.SECONDARY) {
            tileMap.set(tile, Tiles.EMPTY);
        }
        else if (e.isShiftDown()) {
            // cycle through all palette values
            byte content = tileMap.get(tile);
            byte newValue = content < endValue - 1 ? (byte) (content + 1) : 0;
            tileMap.set(tile, newValue);
        }
        else if (selectedPalette().selectedTool != null) {
            selectedPalette().selectedTool.apply(tileMap, tile);
        }
        invalidatePaths();
        markMapEdited();
    }
}