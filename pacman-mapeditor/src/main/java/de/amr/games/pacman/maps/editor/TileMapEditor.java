/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.maps.editor;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.Tiles;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.maps.rendering.FoodMapRenderer;
import de.amr.games.pacman.maps.rendering.TerrainMapRenderer;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.tinylog.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.tilemap.TileMap.formatTile;
import static de.amr.games.pacman.maps.editor.TileMapUtil.*;
import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class TileMapEditor  {

    private static final Node NO_GRAPHIC = null;

    private static final ResourceBundle TEXT_BUNDLE = ResourceBundle.getBundle(TileMapEditor.class.getPackageName() + ".texts");

    public static String tt(String key, Object... args) {
        return MessageFormat.format(TEXT_BUNDLE.getString(key), args);
    }

    public static final Rectangle2D PAC_SPRITE          = new Rectangle2D(473,  16, 14, 14);
    public static final Rectangle2D RED_GHOST_SPRITE    = new Rectangle2D(505,  65, 14, 14);
    public static final Rectangle2D PINK_GHOST_SPRITE   = new Rectangle2D(553,  81, 14, 14);
    public static final Rectangle2D CYAN_GHOST_SPRITE   = new Rectangle2D(521,  97, 14, 14);
    public static final Rectangle2D ORANGE_GHOST_SPRITE = new Rectangle2D(521, 113, 14, 14);
    public static final Rectangle2D BONUS_SPRITE        = new Rectangle2D(505,  49, 14, 14);

    public static final String DEFAULT_COLOR_WALL_STROKE         = "rgb(33,33,255)";
    public static final String DEFAULT_COLOR_WALL_FILL           = "rgb(0,0,0)";
    public static final String DEFAULT_COLOR_DOOR                = "rgb(255,183, 255)";

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
    public static final String DEFAULT_FOOD_COLOR  = "rgb(255,0,0)";

    public static final Vector2i DEFAULT_POS_PAC          = new Vector2i(13, 26);
    public static final Vector2i DEFAULT_POS_RED_GHOST    = new Vector2i(13, 14);
    public static final Vector2i DEFAULT_POS_PINK_GHOST   = new Vector2i(13, 17);
    public static final Vector2i DEFAULT_POS_CYAN_GHOST   = new Vector2i(11, 17);
    public static final Vector2i DEFAULT_POS_ORANGE_GHOST = new Vector2i(15, 17);
    public static final Vector2i DEFAULT_POS_BONUS        = new Vector2i(13, 20);

    private static final String PALETTE_TERRAIN = "Terrain";
    private static final String PALETTE_ACTORS  = "Actors";
    private static final String PALETTE_FOOD    = "Food";

    private static final int TOOL_SIZE = 32;

    private static final RectShape GHOST_HOUSE_SHAPE = new RectShape(new byte[][] {
        {16, 8, 8,14,14, 8, 8,17},
        { 9, 0, 0, 0, 0, 0, 0, 9},
        { 9, 0, 0, 0, 0, 0, 0, 9},
        { 9, 0, 0, 0, 0, 0, 0, 9},
        {19, 8, 8, 8, 8, 8, 8,18}
    });

    private static final RectShape CIRCLE_2x2 = new RectShape(new byte[][] {
        {Tiles.CORNER_NW, Tiles.CORNER_NE},
        {Tiles.CORNER_SW, Tiles.CORNER_SE}
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

    private static byte mirroredTileContent(byte content) {
        return switch (content) {
            case Tiles.CORNER_NE -> Tiles.CORNER_NW;
            case Tiles.CORNER_NW -> Tiles.CORNER_NE;
            case Tiles.CORNER_SE -> Tiles.CORNER_SW;
            case Tiles.CORNER_SW -> Tiles.CORNER_SE;
            case Tiles.DCORNER_NE -> Tiles.DCORNER_NW;
            case Tiles.DCORNER_NW -> Tiles.DCORNER_NE;
            case Tiles.DCORNER_SE -> Tiles.DCORNER_SW;
            case Tiles.DCORNER_SW -> Tiles.DCORNER_SE;
            case Tiles.DCORNER_ANGULAR_NE -> Tiles.DCORNER_ANGULAR_NW;
            case Tiles.DCORNER_ANGULAR_NW -> Tiles.DCORNER_ANGULAR_NE;
            case Tiles.DCORNER_ANGULAR_SE -> Tiles.DCORNER_ANGULAR_SW;
            case Tiles.DCORNER_ANGULAR_SW -> Tiles.DCORNER_ANGULAR_SE;
            default -> content;
        };
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

    private final ObjectProperty<WorldMap> mapPy = new SimpleObjectProperty<>(this, "map") {
        @Override
        protected void invalidated() {
            WorldMap map = get();
            //TODO use binding?
            if (foodMapPropertiesEditor != null) {
                foodMapPropertiesEditor.setTileMap(map.food());
            }
            if (terrainMapPropertiesEditor != null) {
                terrainMapPropertiesEditor.setTileMap(map.terrain());
            }
            invalidateTerrainMapPaths();
            updateSourceView(map);
        }
    };

    private final ObjectProperty<Vector2i> focussedTilePy = new SimpleObjectProperty<>(this, "focussedTile") {
        @Override
        protected void invalidated() {
            Vector2i tile = get();
            var text = "Tile: " + (tile != null ? String.format("x=%2d y=%2d", tile.x(), tile.y()) : "n/a");
            focussedTileInfo.setText(text);
        }
    };

    private final BooleanProperty symmetricEditModePy = new SimpleBooleanProperty(this, "symmetricEditMode", true);

    private Window stage;
    private final BorderPane contentPane = new BorderPane();
    private Canvas editCanvas;
    private final ContextMenu contextMenu = new ContextMenu();
    private Canvas previewCanvas;
    private Text mapSourceView;
    private Label messageLabel;
    private Label focussedTileInfo;
    private FileChooser fileChooser;
    private TabPane palettesTabPane;
    private final Map<String, Palette> palettes = new HashMap<>();
    private final Image spriteSheet;

    private MenuBar menuBar;
    private Menu menuFile;
    private Menu menuEdit;
    private Menu menuLoadMap;
    private Menu menuView;

    private PropertyEditorPane     terrainMapPropertiesEditor;
    private PropertyEditorPane     foodMapPropertiesEditor;
    private TerrainMapEditRenderer terrainMapEditRenderer;
    private TerrainMapRenderer     terrainMapPreviewRenderer;
    private FoodMapRenderer        foodMapRenderer;
    private TerrainMapEditRenderer terrainPaletteRenderer;

    private boolean terrainMapPathsUpToDate;
    private boolean unsavedChanges;
    private File lastUsedDir;
    private Instant messageCloseTime;
    private Timeline clock;

    public TileMapEditor() {
        this(new File(System.getProperty("user.home")));
    }

    public TileMapEditor(File workDir) {
        lastUsedDir = workDir;
        titlePy.bind(createTitleBinding());
        URL url = requireNonNull(getClass().getResource("pacman_spritesheet.png"));
        spriteSheet = new Image(url.toExternalForm());
        WorldMap map = createWorldMap(36, 28); // standard Arcade map size
        setMap(map);
    }

    public WorldMap map() {
        return mapPy.get();
    }

    public void setMap(WorldMap map) {
        mapPy.set(checkNotNull(map));
    }

    private StringBinding createTitleBinding() {
        return Bindings.createStringBinding(
            () -> {
                String title = tt("map_editor");
                if (currentFilePy.get() != null) {
                    title += " - " + currentFilePy.get();
                } else if (map() != null && map().url() != null) {
                    title += " - " + map().url();
                } else {
                    title += " - <" + tt("unsaved_map") + ">";
                }
                if (map() != null) {
                    title += " (%d rows, %d cols)".formatted(map().terrain().numRows(), map().terrain().numCols());
                }
                return title;
            }, currentFilePy, mapPy
        );
    }

    private WorldMap createWorldMap(int numRows, int numCols) {
        var map = new WorldMap(numRows, numCols);
        Logger.info("Map created. rows={}, cols={}", numRows, numCols);

        TileMap terrain = map.terrain();
        addBorder(terrain, 3, 2);

        terrain.setProperty(PROPERTY_COLOR_WALL_STROKE, DEFAULT_COLOR_WALL_STROKE);
        terrain.setProperty(PROPERTY_COLOR_WALL_FILL,   DEFAULT_COLOR_WALL_FILL);
        terrain.setProperty(PROPERTY_COLOR_DOOR,        DEFAULT_COLOR_DOOR);
        terrain.setProperty(PROPERTY_POS_PAC,           formatTile(DEFAULT_POS_PAC));
        terrain.setProperty(PROPERTY_POS_RED_GHOST,     formatTile(DEFAULT_POS_RED_GHOST));
        terrain.setProperty(PROPERTY_POS_PINK_GHOST,    formatTile(DEFAULT_POS_PINK_GHOST));
        terrain.setProperty(PROPERTY_POS_CYAN_GHOST,    formatTile(DEFAULT_POS_CYAN_GHOST));
        terrain.setProperty(PROPERTY_POS_ORANGE_GHOST,  formatTile(DEFAULT_POS_ORANGE_GHOST));
        terrain.setProperty(PROPERTY_POS_BONUS,         formatTile(DEFAULT_POS_BONUS));
        map.food().setProperty(PROPERTY_COLOR_FOOD,     DEFAULT_FOOD_COLOR);

        invalidateTerrainMapPaths();
        return map;
    }

    public void createUI(Stage stage) {
        this.stage = stage;

        terrainMapEditRenderer = new TerrainMapEditRenderer();
        terrainMapEditRenderer.setWallStrokeColor(parseColor(DEFAULT_COLOR_WALL_STROKE));
        terrainMapEditRenderer.setWallFillColor(parseColor(DEFAULT_COLOR_WALL_FILL));

        terrainMapPreviewRenderer = new TerrainMapRenderer();
        terrainMapPreviewRenderer.setWallStrokeColor(parseColor(DEFAULT_COLOR_WALL_STROKE));
        terrainMapPreviewRenderer.setWallFillColor(parseColor(DEFAULT_COLOR_WALL_FILL));

        foodMapRenderer = new FoodMapRenderer();
        foodMapRenderer.setPelletColor(TileMapUtil.parseColor(DEFAULT_FOOD_COLOR));
        foodMapRenderer.setEnergizerColor(TileMapUtil.parseColor(DEFAULT_FOOD_COLOR));

        createLayout();
        createMenus();

        // Note: this must be done *after* the initial map has been created/loaded!
        editCanvas.heightProperty().bind(Bindings.createDoubleBinding(
            () -> (double) mapPy.get().terrain().numRows() * gridSize(), mapPy, gridSizePy));
        editCanvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> (double) mapPy.get().terrain().numCols() * gridSize(), mapPy, gridSizePy));
        previewCanvas.widthProperty().bind(editCanvas.widthProperty());
        previewCanvas.heightProperty().bind(editCanvas.heightProperty());

        // Cursor navigation, whatever it's good for
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

        editCanvas.setOnContextMenuRequested(mouse -> {
            if (editingEnabledPy.get()) {
                Vector2i tile = tileAtMousePosition(mouse.getX(), mouse.getY());

                var miAddCircle2x2 = new MenuItem("2x2 Circle");
                miAddCircle2x2.setOnAction(actionEvent -> CIRCLE_2x2.addToMap(this, map().terrain(), tile));

                var miAddHouse = new MenuItem(tt("menu.edit.add_house"));
                miAddHouse.setOnAction(actionEvent -> GHOST_HOUSE_SHAPE.addToMap(this, map().terrain(), tile));

                contextMenu.getItems().setAll(miAddCircle2x2, miAddHouse);
                contextMenu.show(editCanvas, mouse.getScreenX(), mouse.getScreenY());
            }
        });


        // Active rendering (good idea?)
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
                drawEditCanvas();
                drawPreviewCanvas();
                drawSelectedPalette();
            } catch (Exception x) {
                x.printStackTrace(System.err);
                drawBlueScreen(x);
            }
        }));
        clock.setCycleCount(Animation.INDEFINITE);
    }

    public void start() {
        // content pane and scroll pane have no height yet at this point!
        int gridSize = (int)(0.75 *  stage.getHeight() / map().terrain().numRows());
        gridSizePy.set(Math.max(gridSize, 8));
        clock.play();
        Logger.info("Window height {}", stage.getHeight());
        showMessage(tt("welcome_message"), 3, MessageType.INFO);
    }

    public void stop() {
        clock.stop();
        editingEnabledPy.set(false);
        unsavedChanges = false;
    }

    public void showMessage(String message, long seconds, MessageType type) {
        messageLabel.setText(message);
        messageLabel.setFont(Font.font("sans", FontWeight.BOLD, 12));
        Color color = switch (type) {
            case INFO -> Color.BLACK;
            case WARNING -> Color.GREEN;
            case ERROR -> Color.RED;
        };
        messageLabel.setTextFill(color);
        messageCloseTime = Instant.now().plus(Duration.ofSeconds(seconds));
    }

    private Palette createTerrainPalette() {
        terrainPaletteRenderer = new TerrainMapEditRenderer();
        terrainPaletteRenderer.setScaling(4);
        var palette = new Palette(TOOL_SIZE, 1, 19, terrainPaletteRenderer);
        palette.setTools(
            palette.createTileValueEditorTool(this, Tiles.WALL_H, "Horiz. Wall"),
            palette.createTileValueEditorTool(this, Tiles.WALL_V, "Vert. Wall"),
            palette.createTileValueEditorTool(this, Tiles.DWALL_H, "Hor. Double-Wall"),
            palette.createTileValueEditorTool(this, Tiles.DWALL_V, "Vert. Double-Wall"),
            palette.createTileValueEditorTool(this, Tiles.CORNER_NW, "NW Corner"),
            palette.createTileValueEditorTool(this, Tiles.CORNER_NE, "NE Corner"),
            palette.createTileValueEditorTool(this, Tiles.CORNER_SW, "SW Corner"),
            palette.createTileValueEditorTool(this, Tiles.CORNER_SE, "SE Corner"),
            palette.createTileValueEditorTool(this, Tiles.DCORNER_NW, "NW Corner"),
            palette.createTileValueEditorTool(this, Tiles.DCORNER_NE, "NE Corner"),
            palette.createTileValueEditorTool(this, Tiles.DCORNER_SW, "SW Corner"),
            palette.createTileValueEditorTool(this, Tiles.DCORNER_SE, "SE Corner"),
            palette.createTileValueEditorTool(this, Tiles.DCORNER_ANGULAR_NW, "NW Corner"),
            palette.createTileValueEditorTool(this, Tiles.DCORNER_ANGULAR_NE, "NE Corner"),
            palette.createTileValueEditorTool(this, Tiles.DCORNER_ANGULAR_SW, "SW Corner"),
            palette.createTileValueEditorTool(this, Tiles.DCORNER_ANGULAR_SE, "SE Corner"),
            palette.createTileValueEditorTool(this, Tiles.EMPTY, "Empty Space"),
            palette.createTileValueEditorTool(this, Tiles.TUNNEL, "Tunnel"),
            palette.createTileValueEditorTool(this, Tiles.DOOR, "Door")
        );
        palette.selectTool(16); // EMPTY
        return palette;
    }

    private Palette createActorPalette() {
        var palette = new Palette(TOOL_SIZE, 1, 9, terrainMapEditRenderer);
        palette.setTools(
            palette.createPropertyValueEditorTool(PROPERTY_POS_RED_GHOST, "Red Ghost"),
            palette.createPropertyValueEditorTool(PROPERTY_POS_PINK_GHOST, "Pink Ghost"),
            palette.createPropertyValueEditorTool(PROPERTY_POS_CYAN_GHOST, "Cyan Ghost"),
            palette.createPropertyValueEditorTool(PROPERTY_POS_ORANGE_GHOST, "Orange Ghost"),
            palette.createPropertyValueEditorTool(PROPERTY_POS_SCATTER_RED_GHOST, "Red Ghost Scatter"),
            palette.createPropertyValueEditorTool(PROPERTY_POS_SCATTER_PINK_GHOST, "Pink Ghost Scatter"),
            palette.createPropertyValueEditorTool(PROPERTY_POS_SCATTER_CYAN_GHOST, "Cyan Ghost Scatter"),
            palette.createPropertyValueEditorTool(PROPERTY_POS_SCATTER_ORANGE_GHOST, "Orange Ghost Scatter"),
            palette.createPropertyValueEditorTool(PROPERTY_POS_PAC, "Pac-Man")
        );
        return palette;
    }

    private Palette createFoodPalette() {
        var palette = new Palette(TOOL_SIZE, 1, 3, foodMapRenderer);
        palette.setTools(
            palette.createTileValueEditorTool(this, Tiles.EMPTY, "No Food"),
            palette.createTileValueEditorTool(this, Tiles.PELLET, "Pellet"),
            palette.createTileValueEditorTool(this, Tiles.ENERGIZER, "Energizer")
        );
        palette.selectTool(0); // EMPTY
        return palette;
    }

    private String selectedPaletteID() {
        return (String) palettesTabPane.getSelectionModel().getSelectedItem().getUserData();
    }

    private Palette selectedPalette() {
        return palettes.get(selectedPaletteID());
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
        ScrollPane editCanvasScroll = new ScrollPane(editCanvas);
        editCanvasScroll.setFitToHeight(true);

        previewCanvas = new Canvas();
        ScrollPane previewCanvasScroll = new ScrollPane(previewCanvas);
        previewCanvasScroll.setFitToHeight(true);
        previewCanvasScroll.hvalueProperty().bindBidirectional(editCanvasScroll.hvalueProperty());
        previewCanvasScroll.vvalueProperty().bindBidirectional(editCanvasScroll.vvalueProperty());
        previewCanvasScroll.visibleProperty().bind(previewVisiblePy);

        mapSourceView = new Text();
        mapSourceView.setSmooth(true);
        mapSourceView.setFontSmoothingType(FontSmoothingType.LCD);
        mapSourceView.setFont(Font.font("Monospace", 14));

        var vbox = new VBox(mapSourceView);
        vbox.setPadding(new Insets(10,20,10,20));

        var mapSourceViewScroll = new ScrollPane(vbox);
        mapSourceViewScroll.setFitToHeight(true);

        palettes.put(PALETTE_TERRAIN, createTerrainPalette());
        palettes.put(PALETTE_ACTORS, createActorPalette());
        palettes.put(PALETTE_FOOD, createFoodPalette());

        var terrainPaletteTab = new Tab(tt("terrain"), palettes.get(PALETTE_TERRAIN).root());
        terrainPaletteTab.setClosable(false);
        terrainPaletteTab.setUserData(PALETTE_TERRAIN);

        var actorPaletteTab = new Tab(tt("actors"), palettes.get(PALETTE_ACTORS).root());
        actorPaletteTab.setClosable(false);
        actorPaletteTab.setUserData(PALETTE_ACTORS);

        var foodPaletteTab = new Tab(tt("pellets"), palettes.get(PALETTE_FOOD).root());
        foodPaletteTab.setClosable(false);
        foodPaletteTab.setUserData(PALETTE_FOOD);

        palettesTabPane = new TabPane(terrainPaletteTab, actorPaletteTab, foodPaletteTab);
        palettesTabPane.setPadding(new Insets(5,5,5,5));

        terrainMapPropertiesEditor = new PropertyEditorPane(this);
        terrainMapPropertiesEditor.enabledPy.bind(editingEnabledPy);
        terrainMapPropertiesEditor.setPadding(new Insets(10,0,0,0));

        foodMapPropertiesEditor = new PropertyEditorPane(this);
        foodMapPropertiesEditor.enabledPy.bind(editingEnabledPy);
        foodMapPropertiesEditor.setPadding(new Insets(10,0,0,0));

        var terrainPropertiesArea = new TitledPane();
        terrainPropertiesArea.setExpanded(true);
        terrainPropertiesArea.setText(tt("terrain"));
        terrainPropertiesArea.setContent(terrainMapPropertiesEditor);

        var foodPropertiesArea = new TitledPane();
        foodPropertiesArea.setExpanded(true);
        foodPropertiesArea.setText(tt("pellets"));
        foodPropertiesArea.setContent(foodMapPropertiesEditor);

        var propertyEditorsPane = new VBox();
        propertyEditorsPane.getChildren().addAll(terrainPropertiesArea, foodPropertiesArea);

        focussedTileInfo = new Label();
        focussedTileInfo.setMinWidth(100);
        focussedTileInfo.setMaxWidth(100);

        messageLabel = new Label();
        messageLabel.setMinWidth(200);
        messageLabel.setPadding(new Insets(0, 0, 0, 10));

        var filler = new Region();
        HBox.setHgrow(filler, Priority.ALWAYS);

        Slider sliderZoom = new Slider(8, 48, 16);
        sliderZoom.valueProperty().bindBidirectional(gridSizePy);
        sliderZoom.setShowTickLabels(false);
        sliderZoom.setShowTickMarks(true);
        sliderZoom.setPrefWidth(200);

        var sliderZoomContainer = new HBox(new Label("Zoom"), sliderZoom);
        sliderZoomContainer.setSpacing(5);

        var footer = new HBox(focussedTileInfo, messageLabel, filler, sliderZoomContainer);
        footer.setPadding(new Insets(0, 50, 0, 10));

        var splitPane = new SplitPane(editCanvasScroll, previewCanvasScroll);
        splitPane.setDividerPositions(0.5);

        var tabSourceView = new Tab(tt("source"), mapSourceViewScroll);
        tabSourceView.setClosable(false);

        var tabPreview = new Tab(tt("preview"), splitPane);
        tabPreview.setClosable(false);

        var tabPaneViews = new TabPane(tabPreview, tabSourceView);
        tabPaneViews.setSide(Side.BOTTOM);

        var vbox2 = new VBox(tabPaneViews);
        vbox2.setPadding(new Insets(0,5,0,5));

        var hbox = new HBox(propertyEditorsPane, vbox2);
        hbox.setPadding(new Insets(0,0,10,0));
        HBox.setHgrow(vbox2, Priority.ALWAYS);

        contentPane.setTop(palettesTabPane);
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
        miSaveAs.setOnAction(e -> showSaveDialog());

        menuFile = new Menu(tt("menu.file"), NO_GRAPHIC, miNew, miOpen, miSaveAs);
    }

    private void createEditMenu() {
        var miSymmetricMode = new CheckMenuItem(tt("menu.edit.symmetric"));
        miSymmetricMode.selectedProperty().bindBidirectional(symmetricEditModePy);

        var miAddBorder = new MenuItem(tt("menu.edit.add_border"));
        miAddBorder.setOnAction(e -> {
            addBorder(map().terrain(), 3, 2);
            markTileMapEdited(map().terrain());
        });

        var miClearTerrain = new MenuItem(tt("menu.edit.clear_terrain"));
        miClearTerrain.setOnAction(e -> {
            map().terrain().clear();
            markTileMapEdited(map().terrain());
        });

        var miClearFood = new MenuItem(tt("menu.edit.clear_food"));
        miClearFood.setOnAction(e -> {
            map().food().clear();
            markTileMapEdited(map().food());
        });

        menuEdit = new Menu(tt("menu.edit"), NO_GRAPHIC,
            miSymmetricMode,
            new SeparatorMenuItem(),
            miAddBorder,
            miClearTerrain,
            miClearFood);

        menuEdit.disableProperty().bind(editingEnabledPy.not());
    }

    private void createLoadMapMenu() {
        menuLoadMap = new Menu(tt("menu.load_map"));
        //menuLoadMap.disableProperty().bind(editingEnabledPy.not());
    }

    private void createViewMenu() {
        var miViewTerrain = new CheckMenuItem(tt("menu.view.terrain"));
        miViewTerrain.selectedProperty().bindBidirectional(terrainVisiblePy);

        var miViewFood = new CheckMenuItem(tt("menu.view.food"));
        miViewFood.selectedProperty().bindBidirectional(foodVisiblePy);

        var miViewGrid = new CheckMenuItem(tt("menu.view.grid"));
        miViewGrid.selectedProperty().bindBidirectional(gridVisiblePy);

        var miViewPreview = new CheckMenuItem(tt("menu.view.preview"));
        miViewPreview.selectedProperty().bindBidirectional(previewVisiblePy);

        menuView = new Menu(tt("menu.view"), NO_GRAPHIC,
            miViewTerrain,
            miViewFood,
            miViewGrid,
            new SeparatorMenuItem(),
            miViewPreview);
    }

    public void addLoadMapMenuItem(String description, WorldMap map) {
        checkNotNull(description);
        checkNotNull(map);
        var miLoadMap = new MenuItem(description);
        miLoadMap.setOnAction(e -> loadMap(map));
        menuLoadMap.getItems().add(miLoadMap);
    }

    private void invalidateTerrainMapPaths() {
        terrainMapPathsUpToDate = false;
    }

    public void markTileMapEdited(TileMap tileMap) {
        unsavedChanges = true;
        updateSourceView(map());
        if (tileMap == map().terrain()) {
            invalidateTerrainMapPaths();
        }
    }

    public boolean hasUnsavedChanges() {
        return unsavedChanges;
    }

    public void loadMap(WorldMap worldMap) {
        checkNotNull(worldMap);
        if (unsavedChanges) {
            showSaveConfirmationDialog(this::showSaveDialog, () -> {
                setMap(new WorldMap(worldMap));
                currentFilePy.set(null);
            });
        } else {
            setMap(new WorldMap(worldMap));
            currentFilePy.set(null);
        }
    }

    private void showNewMapDialog() {
        var dialog = new TextInputDialog("28x36");
        dialog.setTitle(tt("new_dialog.title"));
        dialog.setHeaderText(tt("new_dialog.header_text"));
        dialog.setContentText(tt("new_dialog.content_text"));
        dialog.showAndWait().ifPresent(text -> {
            Vector2i size = parseSize(text);
            if (size != null) {
                WorldMap map = createWorldMap(size.y(), size.x());
                setMap(map);
                currentFilePy.set(null);
            }
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

    private void openMapFile() {
        fileChooser.setTitle(tt("open_file"));
        fileChooser.setInitialDirectory(lastUsedDir);
        File file = fileChooser.showOpenDialog(stage);
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

    public void showSaveDialog() {
        fileChooser.setTitle(tt("save_file"));
        fileChooser.setInitialDirectory(lastUsedDir);
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            lastUsedDir = file.getParentFile();
            if (file.getName().endsWith(".world")) {
                map().save(file);
                unsavedChanges = false;
                readMapFile(file);
            } else {
                Logger.error("No .world file selected");
                showMessage("No .world file selected", 2, MessageType.WARNING);
            }
        }
    }

    public void showSaveConfirmationDialog(Runnable saveAction, Runnable noSaveAction) {
        if (hasUnsavedChanges()) {
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
                    saveAction.run();
                } else if (choice == choiceNoSave) {
                    noSaveAction.run();
                } else if (choice == choiceCancel) {
                    confirmationDialog.close();
                }
            });
        } else {
            stop();
            noSaveAction.run();
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

    private Vector2i tileAtMousePosition(double mouseX, double mouseY) {
        return new Vector2i(fullTiles(mouseX), fullTiles(mouseY));
    }

    private void onMouseClickedOnEditCanvas(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            if (e.getClickCount() == 2) { // double-click

                editingEnabledPy.set(true);
                editCanvas.requestFocus();
            } else if (e.getClickCount() == 1) {
                switch (selectedPaletteID()) {
                    case PALETTE_TERRAIN -> editMapTileAtMousePosition(map().terrain(), e);
                    case PALETTE_ACTORS -> {
                        if (selectedPalette().isToolSelected()) {
                            Vector2i tile = tileAtMousePosition(e.getX(), e.getY());
                            selectedPalette().selectedTool().apply(map().terrain(), tile);
                            markTileMapEdited(map().terrain());
                            terrainMapPropertiesEditor.updatePropertyEditorValues();
                        }
                    }
                    case PALETTE_FOOD -> editMapTileAtMousePosition(map().food(), e);
                    default -> Logger.error("Unknown palette selection");
                }
            }
        }
    }

    private void onMouseMovedOverEditCanvas(MouseEvent e) {
        Vector2i tile = tileAtMousePosition(e.getX(), e.getY());
        focussedTilePy.set(tile);
        if (!editingEnabledPy.get()) {
            return;
        }
        if (e.isShiftDown()) {
            switch (selectedPaletteID()) {
                case PALETTE_TERRAIN -> {
                    if (selectedPalette().isToolSelected()) {
                        selectedPalette().selectedTool().apply(map().terrain(), focussedTilePy.get());
                    }
                    markTileMapEdited(map().terrain());
                }
                case PALETTE_FOOD -> {
                    if (selectedPalette().isToolSelected()) {
                        selectedPalette().selectedTool().apply(map().food(), focussedTilePy.get());
                    }
                    markTileMapEdited(map().food());
                }
                default -> {}
            }
        } else if (e.isControlDown()) {
            // delete content while moving
            switch (selectedPaletteID()) {
                case PALETTE_TERRAIN -> setTileValue(map().terrain(), tile, Tiles.EMPTY);
                case PALETTE_FOOD    -> setTileValue(map().food(), tile, Tiles.EMPTY);
                default -> {}
            }
        }
    }

    private void editMapTileAtMousePosition(TileMap tileMap, MouseEvent mouse) {
        var tile = tileAtMousePosition(mouse.getX(), mouse.getY());
        if (mouse.isControlDown()) { // Control-Click clears tile content
            setTileValue(tileMap, tile, Tiles.EMPTY);
        }
        else if (selectedPalette().isToolSelected()) {
            selectedPalette().selectedTool().apply(tileMap, tile);
        }
    }

    /**
     * This method should be used whenever a tile value is set! It takes editor enabled state and symmetric editing mode
     * into account.
     */
    void setTileValue(TileMap tileMap, Vector2i tile, byte value) {
        checkNotNull(tileMap);
        checkNotNull(tile);
        if (editingEnabledPy.get()) {
            tileMap.set(tile, value);
            if (symmetricEditModePy.get()) {
                tileMap.set(tile.y(), tileMap.numCols() - 1 - tile.x(), mirroredTileContent(tileMap.get(tile)));
            }
        }
        markTileMapEdited(tileMap);
    }

    private void updateSourceView(WorldMap map) {
        if (mapSourceView == null) {
            Logger.warn("Cannot update source view as it doesn't exist yet");
            return;
        }
        try {
            String source = map.makeSource();
            String[] lines = source.split("\n");
            for (int i = 0; i < lines.length; ++i) {
                lines[i] = "%5d:   %s".formatted(i+1, lines[i]);
            }
            mapSourceView.setText(String.join("\n", lines));
        } catch (Exception x) {
            Logger.error("Could not create text for map");
            Logger.error(x);
        }
    }

    //
    // Drawing
    //

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
            terrainMapEditRenderer.setScaling(gridSize() / 8.0);
            terrainMapEditRenderer.setWallStrokeColor(getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_STROKE, parseColor(DEFAULT_COLOR_WALL_STROKE)));
            terrainMapEditRenderer.setWallFillColor(getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_FILL, parseColor(DEFAULT_COLOR_WALL_FILL)));
            terrainMapEditRenderer.setDoorColor(getColorFromMap(terrainMap, PROPERTY_COLOR_DOOR, parseColor(DEFAULT_COLOR_DOOR)));
            terrainMapEditRenderer.drawMap(g, terrainMap);
        }
        if (foodVisiblePy.get()) {
            Color foodColor = getColorFromMap(map().food(), PROPERTY_COLOR_FOOD, TileMapUtil.parseColor(DEFAULT_FOOD_COLOR));
            foodMapRenderer.setScaling(gridSize() / 8.0);
            foodMapRenderer.setEnergizerColor(foodColor);
            foodMapRenderer.setPelletColor(foodColor);
            foodMapRenderer.drawMap(g, map().food());
        }
        drawActorSprites(g);
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

    private void drawActorSprites(GraphicsContext g) {
        drawSprite(g, PROPERTY_POS_PAC, PAC_SPRITE, DEFAULT_POS_PAC);
        drawSprite(g, PROPERTY_POS_RED_GHOST, RED_GHOST_SPRITE, DEFAULT_POS_RED_GHOST);
        drawSprite(g, PROPERTY_POS_PINK_GHOST, PINK_GHOST_SPRITE, DEFAULT_POS_PINK_GHOST);
        drawSprite(g, PROPERTY_POS_CYAN_GHOST, CYAN_GHOST_SPRITE, DEFAULT_POS_CYAN_GHOST);
        drawSprite(g, PROPERTY_POS_ORANGE_GHOST, ORANGE_GHOST_SPRITE, DEFAULT_POS_ORANGE_GHOST);
        drawSprite(g, PROPERTY_POS_BONUS, BONUS_SPRITE, DEFAULT_POS_BONUS);
    }

    private void drawPreviewCanvas() {
        GraphicsContext g = previewCanvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
        if (terrainVisiblePy.get()) {
            TileMap terrainMap = map().terrain();
            if (!terrainMapPathsUpToDate) {
                terrainMap.computeTerrainPaths();
                terrainMapPathsUpToDate = true;
            }
            terrainMapPreviewRenderer.setScaling(gridSize() / 8.0);
            terrainMapPreviewRenderer.setWallStrokeColor(getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_STROKE, parseColor(DEFAULT_COLOR_WALL_STROKE)));
            terrainMapPreviewRenderer.setWallFillColor(getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_FILL, parseColor(DEFAULT_COLOR_WALL_FILL)));
            terrainMapPreviewRenderer.setDoorColor(getColorFromMap(terrainMap, PROPERTY_COLOR_DOOR, parseColor(DEFAULT_COLOR_DOOR)));
            terrainMapPreviewRenderer.drawMap(g, terrainMap);
        }
        if (foodVisiblePy.get()) {
            foodMapRenderer.setScaling(gridSize() / 8.0);
            Color foodColor = getColorFromMap(map().food(), PROPERTY_COLOR_FOOD, TileMapUtil.parseColor(DEFAULT_FOOD_COLOR));
            foodMapRenderer.setEnergizerColor(foodColor);
            foodMapRenderer.setPelletColor(foodColor);
            foodMapRenderer.drawMap(g, map().food());
        }
        drawActorSprites(g);
    }

    private void drawSprite(GraphicsContext g, String tilePropertyName, Rectangle2D sprite, Vector2i defaultTile) {
        var tile = getTileFromMap(map().terrain(), tilePropertyName, defaultTile);
        if (tile != null) {
            drawSprite(g, sprite, tile.x() * gridSize() + 0.5 * gridSize(), tile.y() * gridSize(), 1.75 * gridSize(), 1.75 * gridSize());
        }
    }

    private void drawSprite(GraphicsContext g, Rectangle2D sprite, double x, double y, double w, double h) {
        double ox = 0.5 * (w - gridSize());
        double oy = 0.5 * (h - gridSize());
        g.drawImage(spriteSheet, sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(), x - ox, y - oy, w, h);
    }

    private void drawSelectedPalette() {
        Palette selectedPalette = palettes.get(selectedPaletteID());
        if (selectedPaletteID().equals(PALETTE_TERRAIN)) {
            terrainPaletteRenderer.setWallStrokeColor(terrainMapEditRenderer.wallStrokeColor);
        }
        selectedPalette.draw();
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
}