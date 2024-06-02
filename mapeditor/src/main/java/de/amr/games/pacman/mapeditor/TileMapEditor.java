/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import de.amr.games.pacman.lib.TileMap;
import de.amr.games.pacman.lib.Tiles;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.WorldMap;
import de.amr.games.pacman.ui2d.tilemap.FoodMapRenderer;
import de.amr.games.pacman.ui2d.tilemap.TileMapRenderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class TileMapEditor  {

    private static Palette.EditorTool tool(byte value, String description) {
        return new Palette.EditorTool(value, description);
    }

    private static WorldMap createNewMap(int numRows, int numCols) {
        var map = new WorldMap(new TileMap(numRows, numCols), new TileMap(numRows, numCols));
        map.terrain().setProperty("wall_stroke_color", PropertyEditor.formatColor(DEFAULT_WALL_STROKE_COLOR));
        map.terrain().setProperty("wall_fill_color",   PropertyEditor.formatColor(DEFAULT_WALL_FILL_COLOR));
        map.terrain().setProperty("door_color",        PropertyEditor.formatColor(DEFAULT_DOOR_COLOR));
        map.food().setProperty("food_color",           PropertyEditor.formatColor(DEFAULT_FOOD_COLOR));
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

    private static final Color DEFAULT_WALL_STROKE_COLOR = Color.GREEN;
    private static final Color DEFAULT_WALL_FILL_COLOR = Color.MAROON;
    private static final Color DEFAULT_DOOR_COLOR = Color.YELLOW;
    private static final Color DEFAULT_FOOD_COLOR = Color.MAGENTA;

    public final ObjectProperty<String> titlePy = new SimpleObjectProperty<>(this, "title", "Map Editor");
    public final BooleanProperty terrainVisiblePy = new SimpleBooleanProperty(true);
    public final BooleanProperty foodVisiblePy = new SimpleBooleanProperty(true);
    public final BooleanProperty gridVisiblePy = new SimpleBooleanProperty(true);
    public final BooleanProperty editingEnabledPy = new SimpleBooleanProperty(false);

    private Window ownerWindow;
    private MenuBar menuBar;
    private Menu menuFile;
    private Menu menuEdit;
    private Menu menuLoadMap;
    private Pane layout;
    private Canvas editCanvas;
    private Canvas previewCanvas;
    private Label infoLabel;
    private FileChooser fileChooser;

    private TabPane palettesTab;
    private Palette terrainPalette;
    private PropertyEditor terrainMapPropertiesEditor;
    private TileMapEditorTerrainRenderer terrainMapRenderer;
    private boolean pathsUpToDate;

    private Palette actorPalette;

    private Palette foodPalette;
    private PropertyEditor foodMapPropertiesEditor;
    private FoodMapRenderer foodMapRenderer;

    private WorldMap map;
    private final Map<String, WorldMap> predefinedMaps = new HashMap<>();

    private final Text editHint = new Text("Click to Start Editing!");
    private boolean unsavedChanges;
    private Vector2i hoveredTile;
    private File lastUsedDir;
    private File currentMapFile;

    private Timeline clock;

    public TileMapEditor() {
        map = createNewMap(36, 28);
        lastUsedDir = new File(System.getProperty("user.home"));
    }

    public TileMapEditor(File workDir) {
        map = createNewMap(36, 28);
        lastUsedDir = workDir;
    }

    public void createUI(Window ownerWindow) {
        this.ownerWindow = ownerWindow;

        terrainMapRenderer = new TileMapEditorTerrainRenderer();
        terrainMapRenderer.setWallStrokeColor(DEFAULT_WALL_STROKE_COLOR);
        terrainMapRenderer.setWallFillColor(DEFAULT_WALL_FILL_COLOR);

        foodMapRenderer = new FoodMapRenderer();
        foodMapRenderer.setPelletColor(DEFAULT_FOOD_COLOR);
        foodMapRenderer.setEnergizerColor(DEFAULT_FOOD_COLOR);

        createLayout();
        createMenus();

        // Note: this must be done after having loaded the initial map!
        editCanvas.heightProperty().bind(layout.heightProperty().multiply(0.95));
        editCanvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> editCanvas.getHeight() * map.numCols() / map.numRows(), editCanvas.heightProperty()));

        previewCanvas.widthProperty().bind(editCanvas.widthProperty());
        previewCanvas.heightProperty().bind(editCanvas.heightProperty());

        int fps = 10;
        clock = new Timeline(fps, new KeyFrame(Duration.millis(1000.0 / fps), e -> {
            try {
                updateInfo();
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

    private void createLayout() {
        fileChooser = new FileChooser();
        var worldExtensionFilter = new FileChooser.ExtensionFilter("World Map Files", ".world");
        fileChooser.getExtensionFilters().add(worldExtensionFilter);
        fileChooser.setSelectedExtensionFilter(worldExtensionFilter);
        fileChooser.setInitialDirectory(lastUsedDir);

        editCanvas = new Canvas();
        editCanvas.setOnMouseClicked(this::onMouseClickedOnEditCanvas);
        editCanvas.setOnMouseMoved(this::onMouseMovedOverEditCanvas);

        previewCanvas = new Canvas();

        var cbTerrainVisible = new CheckBox("Terrain");
        cbTerrainVisible.selectedProperty().bindBidirectional(terrainVisiblePy);

        var cbFoodVisible = new CheckBox("Food");
        cbFoodVisible.selectedProperty().bindBidirectional(foodVisiblePy);

        var cbGridVisible = new CheckBox("Grid");
        cbGridVisible.selectedProperty().bindBidirectional(gridVisiblePy);

        terrainPalette = new Palette(32, 4, 4, terrainMapRenderer);
        terrainPalette.setTools(
            tool(Tiles.WALL_H, "Horiz. Wall"),
            tool(Tiles.WALL_V, "Vert. Wall"),
            tool(Tiles.DWALL_H, "Hor. Double-Wall"),
            tool(Tiles.DWALL_V, "Vert. Double-Wall"),
            tool(Tiles.CORNER_NW, "NW Corner"),
            tool(Tiles.CORNER_NE, "NE Corner"),
            tool(Tiles.CORNER_SW, "SW Corner"),
            tool(Tiles.CORNER_SE, "SE Corner"),
            tool(Tiles.DCORNER_NW, "NW Corner"),
            tool(Tiles.DCORNER_NE, "NE Corner"),
            tool(Tiles.DCORNER_SW, "SW Corner"),
            tool(Tiles.DCORNER_SE, "SE Corner"),
            tool(Tiles.EMPTY, "Empty Space"),
            tool(Tiles.TUNNEL, "Tunnel"),
            tool(Tiles.DOOR, "Door")
        );

        actorPalette = new Palette(32, 3, 4, terrainMapRenderer);
        actorPalette.setTools(
            tool(Tiles.HOME_RED_GHOST, "Red Ghost"),
            tool(Tiles.HOME_PINK_GHOST, "Pink Ghost"),
            tool(Tiles.HOME_CYAN_GHOST, "Cyan Ghost"),
            tool(Tiles.HOME_ORANGE_GHOST, "Orange Ghost"),
            tool(Tiles.SCATTER_TARGET_RED, "Red Ghost Scatter"),
            tool(Tiles.SCATTER_TARGET_PINK, "Pink Ghost Scatter"),
            tool(Tiles.SCATTER_TARGET_CYAN, "Cyan Ghost Scatter"),
            tool(Tiles.SCATTER_TARGET_ORANGE, "Orange Ghost Scatter"),
            tool(Tiles.PAC_HOME, "Pac-Man")
        );

        foodPalette = new Palette(32, 1, 4, foodMapRenderer);
        foodPalette.setTools(
            tool(Tiles.EMPTY, "No Food"),
            tool(Tiles.PELLET, "Pellet"),
            tool(Tiles.ENERGIZER, "Energizer")
        );


        var terrainPaletteTab = new Tab("Terrain", terrainPalette);
        terrainPaletteTab.setClosable(false);
        terrainPaletteTab.setUserData("Terrain");

        var actorPaletteTab = new Tab("Actors", actorPalette);
        actorPaletteTab.setClosable(false);
        actorPaletteTab.setUserData("Actors");

        var foodPaletteTab = new Tab("Food", foodPalette);
        foodPaletteTab.setClosable(false);
        foodPaletteTab.setUserData("Food");

        palettesTab = new TabPane(terrainPaletteTab, actorPaletteTab, foodPaletteTab);

        terrainMapPropertiesEditor = new PropertyEditor("Terrain", this);
        terrainMapPropertiesEditor.enabledPy.bind(editingEnabledPy);

        foodMapPropertiesEditor = new PropertyEditor("Food", this);
        foodMapPropertiesEditor.enabledPy.bind(editingEnabledPy);

        infoLabel = new Label();

        VBox controlsPane = new VBox();
        controlsPane.setMinWidth(200);
        controlsPane.setSpacing(10);
        controlsPane.getChildren().add(new HBox(20, new Label("Show"), cbTerrainVisible, cbFoodVisible, cbGridVisible));
        controlsPane.getChildren().add(infoLabel);
        controlsPane.getChildren().add(palettesTab);
        controlsPane.getChildren().add(terrainMapPropertiesEditor);
        controlsPane.getChildren().add(foodMapPropertiesEditor);

        terrainMapPropertiesEditor.setPadding(new Insets(10,0,0,0));
        foodMapPropertiesEditor.setPadding(new Insets(10,0,0,0));

        var editCanvasScroll = new ScrollPane(editCanvas);
        editCanvasScroll.setFitToHeight(true);

        var previewCanvasScroll = new ScrollPane(previewCanvas);
        previewCanvasScroll.setFitToHeight(true);

        var hbox = new HBox(editCanvasScroll, controlsPane, previewCanvasScroll);
        hbox.setSpacing(10);

        layout = new BorderPane(hbox);
    }

    private void createMenus() {
        createFileMenu();
        createActionsMenu();

        menuLoadMap = new Menu("Load Map");
        menuLoadMap.disableProperty().bind(editingEnabledPy.not());
        menuBar = new MenuBar();
        menuBar.getMenus().addAll(menuFile, menuEdit, menuLoadMap);
    }

    private void createFileMenu() {
        var miNewMap = new MenuItem("New...");
        miNewMap.setOnAction(e -> showCreateNewMapDialog());

        var miOpenMapFile = new MenuItem("Open...");
        miOpenMapFile.setOnAction(e -> openMapFile());

        var miSaveMapFileAs = new MenuItem("Save As...");
        miSaveMapFileAs.setOnAction(e -> saveMapFileAs());

        menuFile = new Menu("File");
        menuFile.getItems().addAll(miNewMap, miOpenMapFile, miSaveMapFileAs);
    }

    private void createActionsMenu() {
        var miAddBorder = new MenuItem("Add Border");
        miAddBorder.setOnAction(e -> addBorder(map.terrain(), 3, 2));

        var miClearTerrain = new MenuItem("Clear Terrain");
        miClearTerrain.setOnAction(e -> map.terrain().clear());

        var miClearFood = new MenuItem("Clear Food");
        miClearFood.setOnAction(e -> map.food().clear());

        var miAddHouse = new MenuItem("Add House");
        miAddHouse.setOnAction(e -> addHouse());

        menuEdit = new Menu("Edit");
        menuEdit.getItems().addAll(miAddBorder, miAddHouse, miClearTerrain, miClearFood);
        menuEdit.disableProperty().bind(editingEnabledPy.not());
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

    private void updateInfo() {
        var text = "Tile: ";
        text += hoveredTile != null ? String.format("x=%2d y=%2d", hoveredTile.x(), hoveredTile.y()) : "n/a";
        infoLabel.setText(text);
        if (currentMapFile != null) {
            titlePy.set("Map Editor: " + currentMapFile.getPath());
        } else {
            titlePy.set("Map Editor");
        }
    }

    private void updatePaths() {
        if (!pathsUpToDate) {
            map.terrain().computePaths();
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
        GHOST_HOUSE_SHAPE.addToMap(map.terrain(), 14, 10);
        map.terrain().set(26, 13, Tiles.PAC_HOME);
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

        terrain.set(0, 2, Tiles.SCATTER_TARGET_PINK);
        terrain.set(0, terrain.numCols() - 3, Tiles.SCATTER_TARGET_RED);
        terrain.set(terrain.numRows() - emptyRowsBottom, 0, Tiles.SCATTER_TARGET_ORANGE);
        terrain.set(terrain.numRows() - emptyRowsBottom, terrain.numCols() - 1, Tiles.SCATTER_TARGET_CYAN);

        invalidatePaths();
        markMapEdited();
    }

    public void setMap(WorldMap other) {
        checkNotNull(other);
        map = other;
        foodMapPropertiesEditor.edit(map.food().getProperties());
        terrainMapPropertiesEditor.edit(map.terrain().getProperties());
        invalidatePaths();
        updatePaths();
    }

    public void loadMap(WorldMap otherMap) {
        checkNotNull(otherMap);
        if (hasUnsavedChanges()) {
            showConfirmation(this::saveMapFileAs, () -> {
                setMap(WorldMap.copyOf(otherMap));
                currentMapFile = null;
            });
        } else {
            var copy  = WorldMap.copyOf(otherMap);
            setMap(copy);
            currentMapFile = null;
        }
    }

    private void showCreateNewMapDialog() {
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
            currentMapFile = file;
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
                map.save(file);
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
    private double tilePx() {
        return editCanvas.getHeight() / map.numRows();
    }

    /**
     * @param pixels number of pixels
     * @return number of full tiles spanned by pixels
     */
    private int fullTiles(double pixels) {
        return (int) (pixels / tilePx());
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

    private void drawEditCanvas() {
        GraphicsContext g = editCanvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, editCanvas.getWidth(), editCanvas.getHeight());
        drawGrid(g);
        if (terrainVisiblePy.get()) {
            updatePaths();
            terrainMapRenderer.setScaling(tilePx() / 8);
            terrainMapRenderer.setWallStrokeColor(TileMapRenderer.getColorFromMap(map.terrain(), "wall_stroke_color", DEFAULT_WALL_STROKE_COLOR));
            terrainMapRenderer.setWallFillColor(TileMapRenderer.getColorFromMap(map.terrain(), "wall_fill_color", DEFAULT_WALL_FILL_COLOR));
            terrainMapRenderer.setDoorColor(TileMapRenderer.getColorFromMap(map.terrain(), "door_color", DEFAULT_DOOR_COLOR));
            terrainMapRenderer.setRuntimeMode(false);
            terrainMapRenderer.drawMap(g, map.terrain());
        }
        if (foodVisiblePy.get()) {
            foodMapRenderer.setScaling(tilePx() / 8);
            Color foodColor = TileMapRenderer.getColorFromMap(map.food(), "food_color", DEFAULT_FOOD_COLOR);
            foodMapRenderer.setEnergizerColor(foodColor);
            foodMapRenderer.setPelletColor(foodColor);
            foodMapRenderer.drawMap(g, map.food());
        }
        if (!editingEnabledPy.get()) {
            drawEditingHint(g);
        } else {
            if (hoveredTile != null) {
                double tilePx = tilePx();
                g.setStroke(Color.YELLOW);
                g.setLineWidth(1);
                g.strokeRect(hoveredTile.x() * tilePx, hoveredTile.y() * tilePx, tilePx, tilePx);
            }
        }
    }

    private void drawEditingHint(GraphicsContext g) {
        editHint.setFont(Font.font("Sans", FontWeight.BLACK, Math.floor(editCanvas.getWidth() / 12)));
        double x = 0.5 * (editCanvas.getWidth()  - editHint.getBoundsInLocal().getWidth());
        double y = 0.5 * editCanvas.getHeight();
        g.setFont(editHint.getFont());
        g.setStroke(Color.LIGHTGREEN);
        g.setLineWidth(3);
        g.strokeText(editHint.getText(), x, y);
        g.setFill(Color.DARKGREEN);
        g.fillText(editHint.getText(), x, y);
    }

    private void drawPreviewCanvas() {
        GraphicsContext g = previewCanvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
        if (terrainVisiblePy.get()) {
            updatePaths();
            terrainMapRenderer.setScaling(tilePx() / 8);
            terrainMapRenderer.setWallStrokeColor(TileMapRenderer.getColorFromMap(map.terrain(), "wall_stroke_color", DEFAULT_WALL_STROKE_COLOR));
            terrainMapRenderer.setWallFillColor(TileMapRenderer.getColorFromMap(map.terrain(), "wall_fill_color", DEFAULT_WALL_FILL_COLOR));
            terrainMapRenderer.setDoorColor(TileMapRenderer.getColorFromMap(map.terrain(), "door_color", DEFAULT_DOOR_COLOR));
            terrainMapRenderer.setRuntimeMode(true);
            terrainMapRenderer.drawMap(g, map.terrain());
        }
        if (foodVisiblePy.get()) {
            foodMapRenderer.setScaling(tilePx() / 8);
            Color foodColor = TileMapRenderer.getColorFromMap(map.food(), "food_color", DEFAULT_FOOD_COLOR);
            foodMapRenderer.setEnergizerColor(foodColor);
            foodMapRenderer.setPelletColor(foodColor);
            foodMapRenderer.drawMap(g, map.food());
        }
    }

    private void draw() {
        drawEditCanvas();
        drawPreviewCanvas();
        switch (selectedPaletteID()) {
            case "Terrain" -> terrainPalette.draw();
            case "Actors" -> actorPalette.draw();
            case "Food" -> foodPalette.draw();
            default -> Logger.error("Unknown palette selection");
        }
    }

    private void drawGrid(GraphicsContext g) {
        if (gridVisiblePy.get()) {
            g.setStroke(Color.LIGHTGRAY);
            g.setLineWidth(0.25);
            double gridSize = tilePx();
            for (int row = 1; row < map.terrain().numRows(); ++row) {
                g.strokeLine(0, row * gridSize, editCanvas.getWidth(), row * gridSize);
            }
            for (int col = 1; col < map.terrain().numCols(); ++col) {
                g.strokeLine(col * gridSize, 0, col * gridSize, editCanvas.getHeight());
            }
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
            case "Terrain" -> editTerrainMapTile(e, terrainPalette.selectedValue);
            case "Actors"  -> editTerrainMapTile(e, actorPalette.selectedValue);
            case "Food"    -> editFoodMapTile(e, foodPalette.selectedValue);
            default -> Logger.error("Unknown palette selection");
        }
    }

    private String selectedPaletteID() {
        return (String) palettesTab.getSelectionModel().getSelectedItem().getUserData();
    }

    private byte selectedPaletteValue() {
        return switch (selectedPaletteID()) {
            case "Terrain" -> terrainPalette.selectedValue;
            case "Actors"  -> actorPalette.selectedValue;
            case "Food"    -> foodPalette.selectedValue;
            default        -> throw new IllegalStateException("Illegal palette selection");
        };
    }

    private void onMouseMovedOverEditCanvas(MouseEvent e) {
        if (!editingEnabledPy.get()) {
            return;
        }
        hoveredTile = tileAtMousePosition(e.getX(), e.getY());
        if (e.isShiftDown()) {
            switch (selectedPaletteID()) {
                case "Terrain", "Actors" -> {
                    map.terrain().set(hoveredTile, selectedPaletteValue());
                    markMapEdited();
                    invalidatePaths();
                }
                case "Food" -> {
                    map.food().set(hoveredTile, selectedPaletteValue());
                    markMapEdited();
                }
                default -> {}
            }
        }
    }

    private void editTerrainMapTile(MouseEvent e, byte selectedValue) {
        var tile = tileAtMousePosition(e.getX(), e.getY());
        if (e.getButton() == MouseButton.SECONDARY) {
            map.terrain().set(tile, Tiles.EMPTY);
        }
        else if (e.isShiftDown()) { // cycle through all tile values
            byte content = map.terrain().get(tile);
            byte nextValue = content < Tiles.TERRAIN_TILES_END - 1 ? (byte) (content + 1) : 0;
            map.terrain().set(tile, nextValue);
        }
        else {
            map.terrain().set(tile, selectedValue);
        }
        invalidatePaths();
        markMapEdited();
    }

    private void editFoodMapTile(MouseEvent e, byte selectedValue) {
        var tile = tileAtMousePosition(e.getX(), e.getY());
        if (e.getButton() == MouseButton.SECONDARY) {
            map.food().set(tile, Tiles.EMPTY);
        }
        else if (e.isShiftDown()) {
            // cycle through all palette values
            byte content = map.food().get(tile);
            byte newValue = content < Tiles.FOOD_TILES_END - 1 ? (byte) (content + 1) : 0;
            map.food().set(tile, newValue);
        }
        else {
            map.food().set(tile, selectedValue);
        }
        markMapEdited();
    }
}