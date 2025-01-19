/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.*;
import de.amr.games.pacman.tilemap.rendering.FoodMapRenderer;
import de.amr.games.pacman.tilemap.rendering.TerrainRenderer;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.Globals.vec_2i;
import static de.amr.games.pacman.lib.tilemap.TileMap.formatTile;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.tilemap.editor.TileMapEditorUtil.getColorFromMap;
import static de.amr.games.pacman.tilemap.editor.TileMapEditorUtil.parseColor;
import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class TileMapEditor {

    public static final byte MIN_GRID_SIZE = 8;
    public static final byte MAX_GRID_SIZE = 64;

    public static final byte PALETTE_ID_ACTORS  = 0;
    public static final byte PALETTE_ID_TERRAIN = 1;
    public static final byte PALETTE_ID_FOOD    = 2;

    public static final String DEFAULT_COLOR_FOOD         = "rgb(255,255,255)";
    public static final String DEFAULT_COLOR_WALL_STROKE  = "rgb(33,33,255)";
    public static final String DEFAULT_COLOR_WALL_FILL    = "rgb(0,0,0)";
    public static final String DEFAULT_COLOR_DOOR         = "rgb(255,183, 255)";
    public static final Vector2i DEFAULT_POS_HOUSE        = new Vector2i(10, 15);
    public static final Vector2i DEFAULT_POS_RED_GHOST    = DEFAULT_POS_HOUSE.plus(3, -1);
    public static final Vector2i DEFAULT_POS_CYAN_GHOST   = DEFAULT_POS_HOUSE.plus(1, 2);
    public static final Vector2i DEFAULT_POS_PINK_GHOST   = DEFAULT_POS_HOUSE.plus(3, 2);
    public static final Vector2i DEFAULT_POS_ORANGE_GHOST = DEFAULT_POS_HOUSE.plus(5, 2);
    public static final Vector2i DEFAULT_POS_BONUS        = new Vector2i(13, 20);
    public static final Vector2i DEFAULT_POS_PAC          = new Vector2i(13, 26);

    public static final ResourceBundle TEXT_BUNDLE = ResourceBundle.getBundle(TileMapEditor.class.getPackageName() + ".texts");

    public static String tt(String key, Object... args) {
        return MessageFormat.format(TEXT_BUNDLE.getString(key), args);
    }

    static final Node NO_GRAPHIC = null;

    static final Font FONT_STATUS_LINE = Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 12);
    static final Font FONT_MESSAGE     = Font.font("Serif", FontWeight.EXTRA_BOLD, 14);

    static final RectArea PAC_SPRITE          = new RectArea(473,  16, 14, 14);
    static final RectArea RED_GHOST_SPRITE    = new RectArea(505,  65, 14, 14);
    static final RectArea PINK_GHOST_SPRITE   = new RectArea(553,  81, 14, 14);
    static final RectArea CYAN_GHOST_SPRITE   = new RectArea(521,  97, 14, 14);
    static final RectArea ORANGE_GHOST_SPRITE = new RectArea(521, 113, 14, 14);
    static final RectArea BONUS_SPRITE        = new RectArea(505,  49, 14, 14);

    static final int TOOL_SIZE = 32;
    static final int ACTIVE_RENDERING_FPS = 20;

    final ObjectProperty<File> currentFilePy = new SimpleObjectProperty<>();
    final BooleanProperty foodVisiblePy = new SimpleBooleanProperty(true);
    final IntegerProperty gridSizePy = new SimpleIntegerProperty(16);
    final BooleanProperty gridVisiblePy = new SimpleBooleanProperty(true);
    final BooleanProperty segmentNumbersDisplayedPy = new SimpleBooleanProperty(false);
    final BooleanProperty previewVisiblePy = new SimpleBooleanProperty(true);
    final BooleanProperty propertyEditorsVisiblePy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            changePropertyEditorsPaneVisibility(get());
        }
    };
    final BooleanProperty terrainVisiblePy = new SimpleBooleanProperty(true);
    final StringProperty titlePy = new SimpleStringProperty("Tile Map Editor");
    final ObjectProperty<Vector2i> focussedTilePy = new SimpleObjectProperty<>();

    final ObjectProperty<WorldMap> worldMapPy = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            WorldMap map = get();
            if (foodPropertiesEditor() != null) {
                foodPropertiesEditor().setTileMap(map.food());
            }
            if (terrainPropertiesEditor() != null) {
                terrainPropertiesEditor().setTileMap(map.terrain());
            }
            invalidateTerrainData();
            updateSourceView();
        }
    };

    final ObjectProperty<EditMode> modePy = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            switch (get()) {
                case INSPECT -> indicateInspectMode();
                case DRAW -> indicateEditMode();
                case ERASE -> indicateEraseMode();
            }
        }
    };

    final BooleanProperty symmetricEditModePy = new SimpleBooleanProperty(true);

    private final ObstacleEditor obstacleEditor;
    private boolean unsavedChanges;
    private boolean terrainDataUpToDate;
    private boolean dragging = false;
    private final List<Vector2i> tilesWithErrors = new ArrayList<>();

    private final BorderPane contentPane = new BorderPane();
    private Stage stage;
    private Pane propertyEditorsPane;
    private Canvas editCanvas;
    private ScrollPane spEditCanvas;
    private Canvas previewCanvas;
    private ScrollPane spPreviewCanvas;
    private Preview3D preview3D;
    private Text sourceView;
    private ScrollPane spSourceView;
    private TabPane tabPaneMapViews;
    private Label messageLabel;
    private Label focussedTileInfo;
    private Label editModeIndicator;
    private HBox sliderZoomContainer;
    private FileChooser fileChooser;
    private TabPane tabPaneWithPalettes;
    private final Image spriteSheet;
    private final Cursor rubberCursor;

    private MenuBar menuBar;
    private Menu menuFile;
    private Menu menuEdit;
    private Menu menuLoadMap;
    private Menu menuView;

    private final ContextMenu      contextMenu = new ContextMenu();
    private final Palette[]        palettes = new Palette[3];
    private PropertyEditorPane     terrainMapPropertiesEditor;
    private PropertyEditorPane     foodMapPropertiesEditor;
    private TileMapEditorTerrainRenderer editorTerrainRenderer;
    private TerrainRenderer        previewTerrainRenderer;
    private FoodMapRenderer        foodMapRenderer;

    private File lastUsedDir;
    private Instant messageCloseTime;
    private Timeline clock;

    public TileMapEditor() {
        this(new File(System.getProperty("user.home")));
    }

    public TileMapEditor(File workDir) {
        gridSizeProperty().addListener((py,ov,nv) -> invalidateTerrainData());
        obstacleEditor = new ObstacleEditor(this);
        obstacleEditor.enabledPy.bind(modePy.map(mode -> mode != EditMode.INSPECT));
        setMode(EditMode.INSPECT);

        lastUsedDir = workDir;
        titlePy.bind(createTitleBinding());
        spriteSheet = new Image(urlString("graphics/pacman_spritesheet.png"));
        rubberCursor = Cursor.cursor(urlString("graphics/radiergummi.jpg"));
        setWorldMap(new WorldMap(36, 28));
    }

    public StringProperty titleProperty() { return titlePy; }

    private String urlString(String resourcePath) {
        URL url = requireNonNull(getClass().getResource(resourcePath));
        return url.toExternalForm();
    }

    public ObjectProperty<WorldMap> worldMapProperty() {
        return worldMapPy;
    }

    public IntegerProperty gridSizeProperty() {
        return gridSizePy;
    }

    public Canvas canvas() {
        return editCanvas;
    }

    public ContextMenu contextMenu() {
        return contextMenu;
    }

    public byte selectedPaletteID() {
        return (Byte) tabPaneWithPalettes.getSelectionModel().getSelectedItem().getUserData();
    }

    public Palette selectedPalette() {
        return palettes[selectedPaletteID()];
    }

    public PropertyEditorPane terrainPropertiesEditor() {
        return terrainMapPropertiesEditor;
    }

    public PropertyEditorPane foodPropertiesEditor() {
        return foodMapPropertiesEditor;
    }

    public void updateSourceView() {
        updateSourceView(worldMap());
    }

    public void showMessage(String message, long seconds, MessageType type) {
        messageLabel.setText(message);
        Color color = switch (type) {
            case INFO -> Color.BLACK;
            case WARNING -> Color.GREEN;
            case ERROR -> Color.RED;
        };
        messageLabel.setTextFill(color);
        messageCloseTime = Instant.now().plus(java.time.Duration.ofSeconds(seconds));
    }

    public void indicateInspectMode() {
        if (editCanvas != null) {
            editCanvas.setCursor(Cursor.HAND); // TODO use other cursor
        }
    }

    public void indicateEditMode() {
        if (editCanvas != null) {
            editCanvas.setCursor(Cursor.DEFAULT);
        }
    }

    public void indicateEraseMode() {
        if (editCanvas != null) {
            editCanvas.setCursor(rubberCursor);
        }
    }

    public void start() {
        stage.titleProperty().bind(titlePy);
        spEditCanvas.heightProperty().addListener((py,ov,nv) -> {
            if (ov.doubleValue() == 0) { // initial resize
                Logger.info("Canvas scrollpane height {0.00}", spEditCanvas.getHeight());
                double gridSize = spEditCanvas.getHeight() / worldMap().terrain().numRows();
                gridSize = (int) Math.max(gridSize, MIN_GRID_SIZE);
                Logger.info("Grid size {0.00}", gridSize);
                gridSizePy.set((int) gridSize);
            }
        });
        showInfoMessage(tt("welcome_message"), 3);
        clock.play();
    }

    public void stop() {
        clock.stop();
        setMode(EditMode.INSPECT);
        //editController.clearUnsavedChanges();
        preview3D.hide();
    }

    public WorldMap worldMap() {
        return worldMapPy.get();
    }

    public void setWorldMap(WorldMap map) {
        worldMapPy.set(Globals.assertNotNull(map));
    }

    public void createUI(Stage stage) {
        this.stage = Globals.assertNotNull(stage);
        createRenderers();
        createFileChooser();
        createMenuBarAndMenus();
        createEditCanvas();
        createPreviewCanvas();
        createPreview3D();
        createMapSourceView();
        createPalettes();
        createPropertyEditors();
        createTabPaneWithMapViews();
        createFocussedTileIndicator();
        createEditModeIndicator();
        createMessageDisplay();
        createZoomSlider();
        arrangeMainLayout();
        initActiveRendering();

        contentPane.setOnKeyTyped(this::onKeyTyped);
        contentPane.setOnKeyPressed(this::onKeyPressed);

        contentPane.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case KeyCode.DIGIT3 -> {
                    if (preview3D.isVisible()) {
                        preview3D.hide();
                    } else {
                        updatePreview3D();
                        preview3D.show();
                    }
                }
            }
        });

        propertyEditorsVisiblePy.set(false);
    }

    private void createRenderers() {
        editorTerrainRenderer = new TileMapEditorTerrainRenderer();
        editorTerrainRenderer.setWallStrokeColor(parseColor(DEFAULT_COLOR_WALL_STROKE));
        editorTerrainRenderer.setWallFillColor(parseColor(DEFAULT_COLOR_WALL_FILL));

        previewTerrainRenderer = new TerrainRenderer();
        previewTerrainRenderer.setWallStrokeColor(parseColor(DEFAULT_COLOR_WALL_STROKE));
        previewTerrainRenderer.setWallFillColor(parseColor(DEFAULT_COLOR_WALL_FILL));

        foodMapRenderer = new FoodMapRenderer();
        foodMapRenderer.setPelletColor(parseColor(DEFAULT_COLOR_FOOD));
        foodMapRenderer.setEnergizerColor(parseColor(DEFAULT_COLOR_FOOD));
    }

    private void createFileChooser() {
        fileChooser = new FileChooser();
        var worldExtensionFilter = new FileChooser.ExtensionFilter("World Map Files", "*.world");
        var anyExtensionFilter = new FileChooser.ExtensionFilter("All Files", "*.*");
        fileChooser.getExtensionFilters().addAll(worldExtensionFilter, anyExtensionFilter);
        fileChooser.setSelectedExtensionFilter(worldExtensionFilter);
        fileChooser.setInitialDirectory(lastUsedDir);
    }

    private void createEditCanvas() {
        editCanvas = new Canvas();

        //TODO check this
        editCanvas.setOnContextMenuRequested(event -> onEditCanvasContextMenuRequested(contextMenu, event));
        initEventHandlers();

        spEditCanvas = new ScrollPane(editCanvas);
        spEditCanvas.setFitToHeight(true);

        // Note: this must be done *after* the initial map has been created/loaded!
        editCanvas.heightProperty().bind(Bindings.createDoubleBinding(
            () -> (double) worldMap().terrain().numRows() * gridSize(), worldMapPy, gridSizePy));
        editCanvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> (double) worldMap().terrain().numCols() * gridSize(), worldMapPy, gridSizePy));
    }

    private void createPreviewCanvas() {
        previewCanvas = new Canvas();
        spPreviewCanvas = new ScrollPane(previewCanvas);
        spPreviewCanvas.setFitToHeight(true);
        spPreviewCanvas.hvalueProperty().bindBidirectional(spEditCanvas.hvalueProperty());
        spPreviewCanvas.vvalueProperty().bindBidirectional(spEditCanvas.vvalueProperty());
        spPreviewCanvas.visibleProperty().bind(previewVisiblePy);
        previewCanvas.widthProperty().bind(editCanvas.widthProperty());
        previewCanvas.heightProperty().bind(editCanvas.heightProperty());
    }

    private void createPreview3D() {
        preview3D = new Preview3D();
        worldMapPy.addListener((py,ov,nv) -> updatePreview3D());
    }

    private void updatePreview3D() {
        TileMap terrainMap = worldMap().terrain();
        Color wallBaseColor = getColorFromMap(terrainMap, WorldMap.PROPERTY_COLOR_WALL_STROKE,
                parseColor(DEFAULT_COLOR_WALL_STROKE));
        Color wallTopColor = getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_FILL,
                parseColor(DEFAULT_COLOR_WALL_FILL));
        Color foodColor = getColorFromMap(worldMap().food(), PROPERTY_COLOR_FOOD, parseColor(DEFAULT_COLOR_FOOD));
        preview3D.updateContent(worldMapPy.get(), wallBaseColor, wallTopColor, foodColor);
    }

    private void createMapSourceView() {
        sourceView = new Text();
        sourceView.setSmooth(true);
        sourceView.setFontSmoothingType(FontSmoothingType.LCD);
        sourceView.setFont(Font.font("Monospace", 14));

        var vbox = new VBox(sourceView);
        vbox.setPadding(new Insets(10, 20, 10, 20));

        spSourceView = new ScrollPane(vbox);
        spSourceView.setFitToHeight(true);
    }

    private void createTabPaneWithMapViews() {
        var tabSourceView = new Tab(tt("source"), spSourceView);
        tabSourceView.setClosable(false);

        var splitPane = new SplitPane(spEditCanvas, spPreviewCanvas);
        splitPane.setDividerPositions(0.5);

        var tabPreview = new Tab(tt("preview"), splitPane);
        tabPreview.setClosable(false);

        tabPaneMapViews = new TabPane(tabPreview, tabSourceView);
        tabPaneMapViews.setSide(Side.BOTTOM);
    }

    private void createPalettes() {
        palettes[PALETTE_ID_ACTORS]  = createActorPalette();
        palettes[PALETTE_ID_TERRAIN] = createTerrainPalette();
        palettes[PALETTE_ID_FOOD]    = createFoodPalette();

        var tab1 = new Tab(tt("terrain"), palettes[PALETTE_ID_TERRAIN].root());
        tab1.setClosable(false);
        tab1.setUserData(PALETTE_ID_TERRAIN);

        var tab2 = new Tab(tt("actors"), palettes[PALETTE_ID_ACTORS].root());
        tab2.setClosable(false);
        tab2.setUserData(PALETTE_ID_ACTORS);

        var tab3 = new Tab(tt("pellets"), palettes[PALETTE_ID_FOOD].root());
        tab3.setClosable(false);
        tab3.setUserData(PALETTE_ID_FOOD);

        tabPaneWithPalettes = new TabPane(tab1, tab2, tab3);
        tabPaneWithPalettes.setPadding(new Insets(5, 5, 5, 5));
        tabPaneWithPalettes.setMinHeight(75);
    }

    private void createPropertyEditors() {
        terrainMapPropertiesEditor = new PropertyEditorPane(this);
        terrainMapPropertiesEditor.enabledPy.bind(modePy.map(mode -> mode != EditMode.INSPECT));
        terrainMapPropertiesEditor.setPadding(new Insets(10,0,0,0));

        foodMapPropertiesEditor = new PropertyEditorPane(this);
        foodMapPropertiesEditor.enabledPy.bind(modePy.map(mode -> mode != EditMode.INSPECT));
        foodMapPropertiesEditor.setPadding(new Insets(10,0,0,0));

        var terrainPropertiesPane = new TitledPane();
        terrainPropertiesPane.setMinWidth(300);
        terrainPropertiesPane.setExpanded(true);
        terrainPropertiesPane.setText(tt("terrain"));
        terrainPropertiesPane.setContent(terrainMapPropertiesEditor);

        var foodPropertiesPane = new TitledPane();
        foodPropertiesPane.setExpanded(true);
        foodPropertiesPane.setText(tt("pellets"));
        foodPropertiesPane.setContent(foodMapPropertiesEditor);

        propertyEditorsPane = new VBox(terrainPropertiesPane, foodPropertiesPane);
        propertyEditorsPane.visibleProperty().bind(propertyEditorsVisiblePy);
    }

    private void changePropertyEditorsPaneVisibility(boolean visible) {
        if (visible) {
            contentPane.setLeft(propertyEditorsPane);
        } else {
            contentPane.setLeft(null);
        }
    }

    private void createFocussedTileIndicator() {
        focussedTileInfo = new Label();
        focussedTileInfo.setFont(FONT_STATUS_LINE);
        focussedTileInfo.setMinWidth(100);
        focussedTileInfo.setMaxWidth(100);
        focussedTileInfo.textProperty().bind(focussedTilePy.map(
            tile -> tile != null ? "Tile: x=%2d y=%2d".formatted(tile.x(), tile.y()) : "n/a"));
    }

    private void createEditModeIndicator() {
        editModeIndicator = new Label();
        editModeIndicator.setFont(FONT_STATUS_LINE);
        editModeIndicator.setTextFill(Color.RED);
        editModeIndicator.textProperty().bind(Bindings.createStringBinding(
            () -> switch (modePy.get()) {
                    case INSPECT -> "Inspection Mode";
                    case DRAW -> symmetricEditModePy.get() ?  "Symmetric Mode" : "Normal Mode";
                    case ERASE -> "Erase Mode";
            },
            modePy, symmetricEditModePy
    ));
    }

    private void createMessageDisplay() {
        messageLabel = new Label();
        messageLabel.setFont(FONT_MESSAGE);
        messageLabel.setMinWidth(200);
        messageLabel.setPadding(new Insets(0, 0, 0, 10));
    }

    private void createZoomSlider() {
        var sliderZoom = new Slider(MIN_GRID_SIZE, MAX_GRID_SIZE, 0.5 * (MIN_GRID_SIZE + MAX_GRID_SIZE));
        sliderZoom.valueProperty().bindBidirectional(gridSizePy);
        sliderZoom.setShowTickLabels(false);
        sliderZoom.setShowTickMarks(true);
        sliderZoom.setPrefWidth(150);

        sliderZoomContainer = new HBox(new Label("Zoom"), sliderZoom);
        sliderZoomContainer.setSpacing(5);
    }

    private HBox filler(int pixels) {
        var filler = new HBox();
        filler.setMinWidth(pixels);
        filler.setMaxWidth(pixels);
        return filler;
    }

    private void arrangeMainLayout() {
        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        var bottom = new HBox(focussedTileInfo, editModeIndicator, filler(50), messageLabel, spacer, sliderZoomContainer);
        bottom.setPadding(new Insets(10, 10, 10, 10));

        var right = new VBox(tabPaneWithPalettes, tabPaneMapViews, bottom);
        right.setPadding(new Insets(0,5,0,5));

        contentPane.setLeft(propertyEditorsPane);
        contentPane.setCenter(right);
    }

    private StringBinding createTitleBinding() {
        return Bindings.createStringBinding(() -> {
                File currentFile = currentFilePy.get();
                WorldMap worldMap = worldMap();
                String desc = "";
                if (currentFile != null) {
                    desc = "[%s] - %s".formatted(currentFile.getName(), currentFile.getPath());
                } else if (worldMap != null && worldMap.url() != null) {
                    desc = "[%s]".formatted(worldMap.url());
                } else {
                    desc = "[%s]".formatted(tt("unsaved_map"));
                }
                if (worldMap != null) {
                    String prefix = "(%d rows, %d cols)".formatted(worldMap.terrain().numRows(), worldMap.terrain().numCols());
                    desc = prefix + " " + desc;
                }
                return tt("map_editor") + ": " + desc;
            }, currentFilePy, worldMapPy
        );
    }

    private void updateMessageAnimation() {
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

    // Active rendering (good idea?)
    private void initActiveRendering() {
        double frameDuration = 1000.0 / ACTIVE_RENDERING_FPS;
        clock = new Timeline(ACTIVE_RENDERING_FPS, new KeyFrame(Duration.millis(frameDuration), e -> {
            updateMessageAnimation();
            try {
                drawEditCanvas();
                drawPreviewCanvas();
                drawSelectedPalette();
                if (preview3D.isVisible()) {
                    updatePreview3D(); //TODO do this only if terrain is invalid
                }
            } catch (Exception x) {
                Logger.error(x);
                drawBlueScreen(x); // TODO this is crap
                clock.stop();
            }
        }));
        clock.setCycleCount(Animation.INDEFINITE);
    }

    private Palette createTerrainPalette() {
        var palette = new Palette(PALETTE_ID_TERRAIN, TOOL_SIZE, 1, 23, editorTerrainRenderer);
        palette.addTileTool(this, TileEncoding.EMPTY, "Empty Space");
        palette.addTileTool(this, TileEncoding.WALL_H, "Horiz. Wall");
        palette.addTileTool(this, TileEncoding.WALL_V, "Vert. Wall");
        palette.addTileTool(this, TileEncoding.DWALL_H, "Horiz. Double-Wall");
        palette.addTileTool(this, TileEncoding.DWALL_V, "Vert. Double-Wall");
        palette.addTileTool(this, TileEncoding.CORNER_NW, "NW Corner");
        palette.addTileTool(this, TileEncoding.CORNER_NE, "NE Corner");
        palette.addTileTool(this, TileEncoding.CORNER_SW, "SW Corner");
        palette.addTileTool(this, TileEncoding.CORNER_SE, "SE Corner");
        palette.addTileTool(this, TileEncoding.DCORNER_NW, "NW Corner");
        palette.addTileTool(this, TileEncoding.DCORNER_NE, "NE Corner");
        palette.addTileTool(this, TileEncoding.DCORNER_SW, "SW Corner");
        palette.addTileTool(this, TileEncoding.DCORNER_SE, "SE Corner");
        palette.addTileTool(this, TileEncoding.DCORNER_ANGULAR_NW, "NW Corner");
        palette.addTileTool(this, TileEncoding.DCORNER_ANGULAR_NE, "NE Corner");
        palette.addTileTool(this, TileEncoding.DCORNER_ANGULAR_SW, "SW Corner");
        palette.addTileTool(this, TileEncoding.DCORNER_ANGULAR_SE, "SE Corner");
        palette.addTileTool(this, TileEncoding.TUNNEL, "Tunnel");
        palette.addTileTool(this, TileEncoding.DOOR, "Door");
        palette.addTileTool(this, TileEncoding.ONE_WAY_UP, "One-Way Up");
        palette.addTileTool(this, TileEncoding.ONE_WAY_RIGHT, "One-Way Right");
        palette.addTileTool(this, TileEncoding.ONE_WAY_DOWN, "One-Way Down");
        palette.addTileTool(this, TileEncoding.ONE_WAY_LEFT, "One-Way Left");

        palette.selectTool(0); // "No Tile"
        return palette;
    }

    private Palette createActorPalette() {
        var palette = new Palette(PALETTE_ID_ACTORS, TOOL_SIZE, 1, 10, editorTerrainRenderer);
        palette.addTileTool(this, TileEncoding.EMPTY, "");
        palette.addPropertyTool(PROPERTY_POS_PAC, "Pac-Man");
        palette.addPropertyTool(PROPERTY_POS_RED_GHOST, "Red Ghost");
        palette.addPropertyTool(PROPERTY_POS_PINK_GHOST, "Pink Ghost");
        palette.addPropertyTool(PROPERTY_POS_CYAN_GHOST, "Cyan Ghost");
        palette.addPropertyTool(PROPERTY_POS_ORANGE_GHOST, "Orange Ghost");
        palette.addPropertyTool(PROPERTY_POS_SCATTER_RED_GHOST, "Red Ghost Scatter");
        palette.addPropertyTool(PROPERTY_POS_SCATTER_PINK_GHOST, "Pink Ghost Scatter");
        palette.addPropertyTool(PROPERTY_POS_SCATTER_CYAN_GHOST, "Cyan Ghost Scatter");
        palette.addPropertyTool(PROPERTY_POS_SCATTER_ORANGE_GHOST, "Orange Ghost Scatter");
        palette.selectTool(0); // "No actor"
        return palette;
    }

    private Palette createFoodPalette() {
        var palette = new Palette(PALETTE_ID_FOOD, TOOL_SIZE, 1, 3, foodMapRenderer);
        palette.addTileTool(this, TileEncoding.EMPTY, "No Food");
        palette.addTileTool(this, TileEncoding.PELLET, "Pellet");
        palette.addTileTool(this, TileEncoding.ENERGIZER, "Energizer");
        palette.selectTool(0); // "No Food"
        return palette;
    }

    private void createMenuBarAndMenus() {
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
        miOpen.setOnAction(e -> openMapFileInteractively());

        var miSaveAs = new MenuItem(tt("menu.file.save_as"));
        miSaveAs.setOnAction(e -> showSaveDialog());

        menuFile = new Menu(tt("menu.file"), NO_GRAPHIC, miNew, miOpen, miSaveAs);
    }

    private void createEditMenu() {
        var miSymmetricMode = new CheckMenuItem(tt("menu.edit.symmetric"));
        miSymmetricMode.selectedProperty().bindBidirectional(symmetricEditModePy);

        var miAddBorder = new MenuItem(tt("menu.edit.add_border"));
        miAddBorder.setOnAction(e -> addBorder(worldMap().terrain(), 3, 2));

        var miClearTerrain = new MenuItem(tt("menu.edit.clear_terrain"));
        miClearTerrain.setOnAction(e -> {
            clearTerrain(worldMap());
            markTileMapEdited(worldMap().terrain());
        });

        var miClearFood = new MenuItem(tt("menu.edit.clear_food"));
        miClearFood.setOnAction(e -> clearFood(worldMap()));

        menuEdit = new Menu(tt("menu.edit"), NO_GRAPHIC,
            miSymmetricMode,
            new SeparatorMenuItem(),
            miAddBorder,
            miClearTerrain,
            miClearFood);

        menuEdit.disableProperty().bind(modePy.map(mode -> mode == EditMode.INSPECT));
    }

    private void createLoadMapMenu() {
        menuLoadMap = new Menu(tt("menu.load_map"));
    }

    private void createViewMenu() {
        var miShowPropertyEditors = new CheckMenuItem(tt("menu.view.properties"));
        miShowPropertyEditors.selectedProperty().bindBidirectional(propertyEditorsVisiblePy);

        var miShowTerrain = new CheckMenuItem(tt("menu.view.terrain"));
        miShowTerrain.selectedProperty().bindBidirectional(terrainVisiblePy);

        var miShowFood = new CheckMenuItem(tt("menu.view.food"));
        miShowFood.selectedProperty().bindBidirectional(foodVisiblePy);

        var miShowGrid = new CheckMenuItem(tt("menu.view.grid"));
        miShowGrid.selectedProperty().bindBidirectional(gridVisiblePy);

        var miShowSegmentNumbers = new CheckMenuItem(tt("menu.view.segment_numbers"));
        miShowSegmentNumbers.selectedProperty().bindBidirectional(segmentNumbersDisplayedPy);

        var miShowPreview = new CheckMenuItem(tt("menu.view.preview"));
        miShowPreview.selectedProperty().bindBidirectional(previewVisiblePy);

        menuView = new Menu(tt("menu.view"), NO_GRAPHIC,
            miShowPropertyEditors,
            miShowTerrain,
            miShowSegmentNumbers,
            miShowFood,
            miShowGrid,
            new SeparatorMenuItem(),
            miShowPreview);
    }

    public void addLoadMapMenuItem(String description, WorldMap map) {
        Globals.assertNotNull(description);
        Globals.assertNotNull(map);
        var miLoadMap = new MenuItem(description);
        miLoadMap.setOnAction(e -> loadMap(map));
        menuLoadMap.getItems().add(miLoadMap);
    }

    public void loadMap(WorldMap worldMap) {
        Globals.assertNotNull(worldMap);
        if (hasUnsavedChanges()) {
            showSaveConfirmationDialog(this::showSaveDialog, () -> {
                setWorldMap(new WorldMap(worldMap));
                currentFilePy.set(null);
            });
        } else {
            setWorldMap(new WorldMap(worldMap));
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
                WorldMap map = createPreconfiguredMap(size.x(), size.y());
                setWorldMap(map);
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

    private void openMapFileInteractively() {
        fileChooser.setTitle(tt("open_file"));
        fileChooser.setInitialDirectory(lastUsedDir);
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            readMapFile(file);
        }
    }

    private boolean readMapFile(File file) {
        if (file.getName().endsWith(".world")) {
            try {
                loadMap(new WorldMap(file));
                lastUsedDir = file.getParentFile();
                currentFilePy.set(file);
                Logger.info("Map read from file {}", file);
            } catch (IOException x) {
                Logger.error(x);
                Logger.info("Map could not be read from file {}", file);
            }
        }
        return false;
    }

    public Optional<File> readNextMapFileInDirectory() {
        return nextMapFileInDirectory(currentFilePy.get(), true).filter(this::readMapFile);
    }

    public Optional<File> readPrevMapFileInDirectory() {
        return nextMapFileInDirectory(currentFilePy.get(), false).filter(this::readMapFile);
    }

    private Optional<File> nextMapFileInDirectory(File currentFile, boolean forward) {
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
        fileChooser.setInitialDirectory(lastUsedDir);
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            lastUsedDir = file.getParentFile();
            if (file.getName().endsWith(".world")) {
                worldMap().save(file);
                clearUnsavedChanges();
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

    public Menu getFileMenu() {
        return menuFile;
    }

    public Menu getLoadMapMenu() {
        return menuLoadMap;
    }

    /**
     * @return pixels used by one tile at current window zoom
     */
    private int gridSize() {
        return gridSizePy.get();
    }

    private void updateSourceView(WorldMap worldMap) {
        if (sourceView == null) {
            Logger.warn("Cannot update source view as it doesn't exist yet");
            return;
        }
        try {
            String source = worldMap.sourceCode();
            String[] lines = source.split("\n");
            for (int i = 0; i < lines.length; ++i) {
                lines[i] = "%5d:   %s".formatted(i+1, lines[i]);
            }
            sourceView.setText(String.join("\n", lines));
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
        g.setImageSmoothing(false);
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, editCanvas.getWidth(), editCanvas.getHeight());
        drawGrid(g);
        if (terrainVisiblePy.get()) {
            TileMap terrainMap = worldMap().terrain();
            editorTerrainRenderer.setScaling(gridSize() / 8.0);
            editorTerrainRenderer.setWallStrokeColor(getColorFromMap(terrainMap, WorldMap.PROPERTY_COLOR_WALL_STROKE, parseColor(DEFAULT_COLOR_WALL_STROKE)));
            editorTerrainRenderer.setWallFillColor(getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_FILL, parseColor(DEFAULT_COLOR_WALL_FILL)));
            editorTerrainRenderer.setDoorColor(getColorFromMap(terrainMap, PROPERTY_COLOR_DOOR, parseColor(DEFAULT_COLOR_DOOR)));
            editorTerrainRenderer.setSegmentNumbersDisplayed(segmentNumbersDisplayedPy.get());
            editorTerrainRenderer.drawTerrain(g, terrainMap, worldMap().obstacles());

            byte[][] editedContent = editedContent();
            if (editedContent != null) {
                for (int row = 0; row < editedContent.length; ++row) {
                    for (int col = 0; col < editedContent[0].length; ++col) {
                        Vector2i tile = editedContentMinTile().plus(col, row);
                        editorTerrainRenderer.drawTile(g, tile, editedContent[row][col]);
                    }
                }
            }
        }
        double gs = gridSize();
        for (Vector2i tile : tilesWithErrors()) {
            g.setFont(Font.font("sans", gs-2));
            g.setFill(Color.grayRgb(200, 0.8));
            g.fillText("?", tile.x() * gs + 0.25 * gs, tile.y() * gs + 0.8*gs);
            if (symmetricEditModePy.get()) {
                int x = worldMap().terrain().numCols() - tile.x() - 1;
                g.fillText("?", x * gs + 0.25 * gs, tile.y() * gs + 0.8*gs);
            }
        }
        if (symmetricEditModePy.get()) {
            g.save();
            g.setStroke(Color.YELLOW);
            g.setLineWidth(0.75);
            g.setLineDashes(5, 5);
            g.strokeLine(canvas().getWidth() / 2, 0, canvas().getWidth() / 2, canvas().getHeight());
            g.restore();
        }

        if (foodVisiblePy.get()) {
            Color foodColor = getColorFromMap(worldMap().food(), PROPERTY_COLOR_FOOD, parseColor(DEFAULT_COLOR_FOOD));
            foodMapRenderer.setScaling(gridSize() / 8.0);
            foodMapRenderer.setEnergizerColor(foodColor);
            foodMapRenderer.setPelletColor(foodColor);
            foodMapRenderer.drawFood(g, worldMap().food());
        }
        drawActorSprites(g);
        if (modePy.get() == EditMode.INSPECT) {
            drawEditingHint(g);
        }
        Vector2i focussedTile = focussedTilePy.get();
        if (focussedTile != null) {
            double tilePx = gridSize();
            g.setStroke(Color.YELLOW);
            g.setLineWidth(1);
            g.strokeRect(focussedTile.x() * tilePx, focussedTile.y() * tilePx, tilePx, tilePx);
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
        g.setImageSmoothing(false);
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
        if (terrainVisiblePy.get()) {
            TileMap terrainMap = worldMap().terrain();
            ensureTerrainMapsPathsUpToDate();
            previewTerrainRenderer.setScaling(gridSize() / 8.0);
            previewTerrainRenderer.setWallStrokeColor(getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_STROKE, parseColor(DEFAULT_COLOR_WALL_STROKE)));
            previewTerrainRenderer.setWallFillColor(getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_FILL, parseColor(DEFAULT_COLOR_WALL_FILL)));
            previewTerrainRenderer.setDoorColor(getColorFromMap(terrainMap, PROPERTY_COLOR_DOOR, parseColor(DEFAULT_COLOR_DOOR)));
            previewTerrainRenderer.drawTerrain(g, terrainMap, worldMap().obstacles());
        }
        if (foodVisiblePy.get()) {
            foodMapRenderer.setScaling(gridSize() / 8.0);
            Color foodColor = getColorFromMap(worldMap().food(), PROPERTY_COLOR_FOOD, parseColor(DEFAULT_COLOR_FOOD));
            foodMapRenderer.setEnergizerColor(foodColor);
            foodMapRenderer.setPelletColor(foodColor);
            foodMapRenderer.drawFood(g, worldMap().food());
        }
        drawActorSprites(g);
    }

    private void drawSprite(GraphicsContext g, String tilePropertyName, RectArea sprite, Vector2i defaultTile) {
        Vector2i tile = worldMap().terrain().getTileProperty(tilePropertyName, defaultTile);
        if (tile != null) {
            drawSprite(g, sprite, tile.x() * gridSize() + 0.5 * gridSize(), tile.y() * gridSize(), 1.75 * gridSize(), 1.75 * gridSize());
        }
    }

    private void drawSprite(GraphicsContext g, RectArea sprite, double x, double y, double w, double h) {
        double ox = 0.5 * (w - gridSize());
        double oy = 0.5 * (h - gridSize());
        g.drawImage(spriteSheet, sprite.x(), sprite.y(), sprite.width(), sprite.height(), x - ox, y - oy, w, h);
    }

    private void drawSelectedPalette() {
        Palette selectedPalette = palettes[selectedPaletteID()];
        if (selectedPaletteID() == PALETTE_ID_TERRAIN) {
            double scaling = editorTerrainRenderer.scaling();
            editorTerrainRenderer.setScaling((double) TOOL_SIZE / 8);
            editorTerrainRenderer.setWallStrokeColor(editorTerrainRenderer.wallStrokeColor);
            editorTerrainRenderer.setScaling(scaling);
        }
        selectedPalette.draw();
    }

    private void drawGrid(GraphicsContext g) {
        if (gridVisiblePy.get()) {
            g.save();
            g.setStroke(Color.LIGHTGRAY);
            g.setLineWidth(0.25);
            double gridSize = gridSize();
            for (int row = 1; row < worldMap().terrain().numRows(); ++row) {
                g.strokeLine(0, row * gridSize, editCanvas.getWidth(), row * gridSize);
            }
            for (int col = 1; col < worldMap().terrain().numCols(); ++col) {
                g.strokeLine(col * gridSize, 0, col * gridSize, editCanvas.getHeight());
            }
            g.restore();
        }
    }

    // Controller part


    // For now, here:
    static final byte[][] GHOST_HOUSE_SHAPE = {
        {16, 8, 8, 14, 14, 8, 8, 17},
        {9, 0, 0, 0, 0, 0, 0, 9},
        {9, 0, 0, 0, 0, 0, 0, 9},
        {9, 0, 0, 0, 0, 0, 0, 9},
        {19, 8, 8, 8, 8, 8, 8, 18}
    };

    static final byte[][] CIRCLE_2x2 = {
        {TileEncoding.CORNER_NW, TileEncoding.CORNER_NE},
        {TileEncoding.CORNER_SW, TileEncoding.CORNER_SE}
    };

    static byte mirroredTileContent(byte content) {
        return switch (content) {
            case TileEncoding.CORNER_NE -> TileEncoding.CORNER_NW;
            case TileEncoding.CORNER_NW -> TileEncoding.CORNER_NE;
            case TileEncoding.CORNER_SE -> TileEncoding.CORNER_SW;
            case TileEncoding.CORNER_SW -> TileEncoding.CORNER_SE;
            case TileEncoding.DCORNER_NE -> TileEncoding.DCORNER_NW;
            case TileEncoding.DCORNER_NW -> TileEncoding.DCORNER_NE;
            case TileEncoding.DCORNER_SE -> TileEncoding.DCORNER_SW;
            case TileEncoding.DCORNER_SW -> TileEncoding.DCORNER_SE;
            case TileEncoding.DCORNER_ANGULAR_NE -> TileEncoding.DCORNER_ANGULAR_NW;
            case TileEncoding.DCORNER_ANGULAR_NW -> TileEncoding.DCORNER_ANGULAR_NE;
            case TileEncoding.DCORNER_ANGULAR_SE -> TileEncoding.DCORNER_ANGULAR_SW;
            case TileEncoding.DCORNER_ANGULAR_SW -> TileEncoding.DCORNER_ANGULAR_SE;
            default -> content;
        };
    }

    Vector2i editedContentMinTile() {
        return obstacleEditor.minTile();
    }

    Vector2i editedContentMaxTile() {
        return obstacleEditor.maxTile();
    }

    byte[][] editedContent() {
        return obstacleEditor.editedContent();
    }

    void initEventHandlers() {
        Canvas editCanvas = canvas();
        editCanvas.setOnMouseClicked(this::onEditCanvasMouseClicked);
        editCanvas.setOnMouseReleased(this::onEditCanvasMouseReleased);
        editCanvas.setOnMouseDragged(this::onEditCanvasMouseDragged);
        editCanvas.setOnMouseMoved(this::onEditCanvasMouseMoved);
        editCanvas.setOnKeyPressed(this::onEditCanvasKeyPressed);
    }

    void onEditCanvasMouseClicked(MouseEvent event) {
        Logger.debug("Mouse clicked {}", event);
        if (event.getButton() == MouseButton.PRIMARY) {
            canvas().requestFocus();
            contextMenu().hide();
            if (event.getClickCount() == 2 && isMode(EditMode.INSPECT)) {
                setMode(EditMode.DRAW);
            }
        }
    }

    void onEditCanvasMouseDragged(MouseEvent event) {
        Logger.debug("Mouse dragged {}", event);
        if (!dragging) {
            Vector2i dragStartTile = tileAtMousePosition(event.getX(), event.getY());
            obstacleEditor.startEditing(dragStartTile);
            dragging = true;
            Logger.debug("Dragging started at tile {}", dragStartTile);
        } else {
            obstacleEditor.continueEditing(tileAtMousePosition(event.getX(), event.getY()));
        }
    }

    void onEditCanvasMouseReleased(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            Logger.debug("Mouse released: {}", event);
            if (dragging) {
                dragging = false;
                Vector2i tile = tileAtMousePosition(event.getX(), event.getY());
                Logger.debug("Dragging ends at tile {}", tile);
                obstacleEditor.endEditing(tile);
            } else {
                editAtMousePosition(event);
            }
        }
    }

    void onEditCanvasMouseMoved(MouseEvent event) {
        Vector2i tile = tileAtMousePosition(event.getX(), event.getY());
        focussedTilePy.set(tile);
        if (isMode(EditMode.INSPECT)) {
            return;
        }
        WorldMap worldMap = worldMapPy.get();
        if (isMode(EditMode.ERASE)) {
            switch (selectedPaletteID()) {
                case TileMapEditor.PALETTE_ID_TERRAIN -> eraseTileValue(worldMap.terrain(), tile);
                case TileMapEditor.PALETTE_ID_FOOD -> eraseTileValue(worldMap.food(), tile);
            }
        } else {
            if (event.isShiftDown()) {
                switch (selectedPaletteID()) {
                    case TileMapEditor.PALETTE_ID_TERRAIN -> {
                        if (selectedPalette().isToolSelected()) {
                            selectedPalette().selectedTool().apply(worldMap.terrain(), focussedTilePy.get());
                        }
                        markTileMapEdited(worldMap.terrain());
                    }
                    case TileMapEditor.PALETTE_ID_FOOD -> {
                        if (selectedPalette().isToolSelected()) {
                            selectedPalette().selectedTool().apply(worldMap.food(), focussedTilePy.get());
                        }
                        markTileMapEdited(worldMap.food());
                    }
                    default -> {
                    }
                }
            }
        }
    }

    void identifyObstacleAtTilePosition(Vector2i tile) {
        Obstacle obstacleAtTile = worldMap().obstacles().stream()
            .filter(obstacle -> Globals.tileAt(obstacle.startPoint().minus(HTS, 0)).equals(tile))
            .findFirst().orElse(null);
        if (obstacleAtTile != null) {
            String encoding = obstacleAtTile.encoding();
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(encoding);
            clipboard.setContent(content);
            Optional<ObstacleType> type = ObstacleType.identify(encoding);
            if (type.isPresent()) {
                showInfoMessage("'%s' identified (copied to clipboard)".formatted(type.get()), 5);
            } else {
                showInfoMessage("Obstacle identified (copied to clipboard)", 5);
            }
        } else {
            showInfoMessage("", 1);
        }
    }

    void onKeyPressed(KeyEvent event) {
        if (event.isAltDown()) {
            if (event.getCode() == KeyCode.LEFT) {
                event.consume();
                readPrevMapFileInDirectory().ifPresentOrElse(
                    file -> showInfoMessage("Previous map file read: %s".formatted(file.getName()), 3),
                    () -> showErrorMessage("Previous file not available", 1));
            } else if (event.getCode() == KeyCode.RIGHT) {
                event.consume();
                readNextMapFileInDirectory().ifPresentOrElse(
                    file -> showInfoMessage("Next map file read: %s".formatted(file.getName()), 3),
                    () -> showErrorMessage("Next file not available", 1));
            }
        }
    }

    void onEditCanvasKeyPressed(KeyEvent event) {
        Direction cursor = switch (event.getCode()) {
            case LEFT -> Direction.LEFT;
            case RIGHT -> Direction.RIGHT;
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            default -> null;
        };
        if (cursor != null && focussedTilePy.get() != null) {
            WorldMap worldMap = worldMapPy.get();
            Vector2i newTile = focussedTilePy.get().plus(cursor.vector());
            if (!worldMap.terrain().outOfBounds(newTile)) {
                focussedTilePy.set(newTile);
            }
        }
    }

    void onKeyTyped(KeyEvent event) {
        Logger.debug("Typed {}", event);
        String ch = event.getCharacter();
        switch (ch) {
            case "i" -> {
                setMode(EditMode.INSPECT);
            }
            case "n" -> {
                setMode(EditMode.DRAW);
                symmetricEditModePy.set(false);
            }
            case "s" -> {
                setMode(EditMode.DRAW);
                symmetricEditModePy.set(true);
            }
            case "x" -> {
                setMode(isMode(EditMode.ERASE) ? EditMode.INSPECT : EditMode.ERASE);
            }
            case "+" -> {
                if (gridSizeProperty().get() < TileMapEditor.MAX_GRID_SIZE) {
                    gridSizeProperty().set(gridSizeProperty().get() + 1);
                }
            }
            case "-" -> {
                if (gridSizeProperty().get() > TileMapEditor.MIN_GRID_SIZE) {
                    gridSizeProperty().set(gridSizeProperty().get() - 1);
                }
            }
        }
    }

    void onEditCanvasContextMenuRequested(ContextMenu contextMenu, ContextMenuEvent event) {
        if (!isMode(EditMode.INSPECT)) {
            Vector2i tile = tileAtMousePosition(event.getX(), event.getY());
            WorldMap worldMap = worldMapPy.get();

            var miAddCircle2x2 = new MenuItem("2x2 Circle");
            miAddCircle2x2.setOnAction(actionEvent -> addShapeMirrored(worldMap.terrain(), CIRCLE_2x2, tile));

            var miAddHouse = new MenuItem(TileMapEditor.tt("menu.edit.add_house"));
            miAddHouse.setOnAction(actionEvent -> addHouse(worldMap.terrain(), tile));

            contextMenu.getItems().setAll(miAddCircle2x2, miAddHouse);
            contextMenu.show(canvas(), event.getScreenX(), event.getScreenY());
        }
    }

    EditMode mode() { return modePy.get(); }

    boolean isMode(EditMode mode) { return mode() == mode; }

    void setMode(EditMode mode) {
        modePy.set(assertNotNull(mode));
    }

    boolean hasUnsavedChanges() {
        return unsavedChanges;
    }

    void clearUnsavedChanges() {
        unsavedChanges = false;
    }

    /**
     * @param pixels number of pixels
     * @return number of full tiles spanned by pixels
     */
    private int fullTiles(double pixels) {
        return (int) (pixels / gridSizeProperty().get());
    }

    void invalidateTerrainData() {
        terrainDataUpToDate = false;
    }

    void ensureTerrainMapsPathsUpToDate() {
        if (!terrainDataUpToDate) {
            WorldMap worldMap = worldMapPy.get();
            tilesWithErrors.clear();
            tilesWithErrors.addAll(worldMap.updateObstacleList());
            terrainDataUpToDate = true;
        }
    }

    void markTileMapEdited(TileMap tileMap) {
        unsavedChanges = true;
        WorldMap worldMap = worldMapPy.get();
        if (worldMap != null) {
            updateSourceView();
            if (tileMap == worldMap.terrain()) {
                invalidateTerrainData();
            }
        }
    }

    Vector2i tileAtMousePosition(double mouseX, double mouseY) {
        return new Vector2i(fullTiles(mouseX), fullTiles(mouseY));
    }

    void editMapTileAtMousePosition(TileMap tileMap, Vector2i tile, boolean erase) {
        if (erase) { // Control-Click clears tile content
            eraseTileValue(tileMap, tile);
        } else if (selectedPalette().isToolSelected()) {
            selectedPalette().selectedTool().apply(tileMap, tile);
        }
    }

    void editAtMousePosition(MouseEvent event) {
        Vector2i tile = tileAtMousePosition(event.getX(), event.getY());
        if (isMode(EditMode.INSPECT)) {
            identifyObstacleAtTilePosition(tile);
            return;
        }
        WorldMap worldMap = worldMapPy.get();
        boolean erase = event.isControlDown();
        switch (selectedPaletteID()) {
            case TileMapEditor.PALETTE_ID_TERRAIN -> editMapTileAtMousePosition(worldMap.terrain(), tile, erase);
            case TileMapEditor.PALETTE_ID_ACTORS -> {
                if (selectedPalette().isToolSelected()) {
                    selectedPalette().selectedTool().apply(worldMap.terrain(), tile);
                    markTileMapEdited(worldMap.terrain());
                    terrainPropertiesEditor().updatePropertyEditorValues();
                }
            }
            case TileMapEditor.PALETTE_ID_FOOD -> editMapTileAtMousePosition(worldMap.food(), tile, erase);
            default -> Logger.error("Unknown palette selection");
        }
    }

    void clearTerrain(WorldMap worldMap) {
        worldMap.terrain().clear();
    }

    public List<Vector2i> tilesWithErrors() {
        return tilesWithErrors;
    }

    void clearFood(WorldMap worldMap) {
        worldMap.food().clear();
        markTileMapEdited(worldMap.food());
    }

    void addBorder(TileMap terrain, int emptyRowsTop, int emptyRowsBottom) {
        for (int row = emptyRowsTop; row < terrain.numRows() - emptyRowsBottom; ++row) {
            terrain.set(row, 0, TileEncoding.DWALL_V);
            terrain.set(row, terrain.numCols() - 1, TileEncoding.DWALL_V);
        }
        for (int col = 1; col < terrain.numCols() - 1; ++col) {
            terrain.set(emptyRowsTop, col, TileEncoding.DWALL_H);
            terrain.set(terrain.numRows() - 1 - emptyRowsBottom, col, TileEncoding.DWALL_H);
        }
        terrain.set(emptyRowsTop, 0, TileEncoding.DCORNER_NW);
        terrain.set(emptyRowsTop, terrain.numCols() - 1, TileEncoding.DCORNER_NE);
        terrain.set(terrain.numRows() - 1 - emptyRowsBottom, 0, TileEncoding.DCORNER_SW);
        terrain.set(terrain.numRows() - 1 - emptyRowsBottom, terrain.numCols() - 1, TileEncoding.DCORNER_SE);

        markTileMapEdited(terrain);
    }

    void addHouse(TileMap terrain, Vector2i tile) {
        addShape(terrain, GHOST_HOUSE_SHAPE, tile);
        terrain.setProperty(PROPERTY_POS_HOUSE_MIN_TILE, formatTile(tile));
        terrain.setProperty(PROPERTY_POS_RED_GHOST, formatTile(tile.plus(3, -1)));
        terrain.setProperty(PROPERTY_POS_CYAN_GHOST, formatTile(tile.plus(1, 2)));
        terrain.setProperty(PROPERTY_POS_PINK_GHOST, formatTile(tile.plus(3, 2)));
        terrain.setProperty(PROPERTY_POS_ORANGE_GHOST, formatTile(tile.plus(5, 2)));

        terrainPropertiesEditor().rebuildPropertyEditors();
    }

    void addShapeMirrored(TileMap map, byte[][] content, Vector2i originTile) {
        int numRows = content.length, numCols = content[0].length;
        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < numCols; ++col) {
                setTileValue(map, originTile.plus(col, row), content[row][col]);
            }
        }
        markTileMapEdited(map);
    }

    void addShape(TileMap map, byte[][] content, Vector2i originTile) {
        int numRows = content.length, numCols = content[0].length;
        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < numCols; ++col) {
                map.set(originTile.plus(col, row), content[row][col]);
            }
        }
        markTileMapEdited(map);
    }

    /**
     * This method should be used whenever a tile value is set! It takes editor enabled state and symmetric editing mode
     * into account.
     */
    public void setTileValue(TileMap tileMap, Vector2i tile, byte value) {
        assertNotNull(tileMap);
        assertNotNull(tile);
        tileMap.set(tile, value);
        if (symmetricEditModePy.get()) {
            tileMap.set(tile.y(), tileMap.numCols() - 1 - tile.x(), mirroredTileContent(tileMap.get(tile)));
        }
        markTileMapEdited(tileMap);
    }

    void eraseTileValue(TileMap tileMap, Vector2i tile) {
        tileMap.set(tile, TileEncoding.EMPTY);
        markTileMapEdited(tileMap);
    }

    public void showInfoMessage(String message, long seconds) {
        showMessage(message, seconds, MessageType.INFO);
    }

    public void showWarningMessage(String message, long seconds) {
        showMessage(message, seconds, MessageType.WARNING);
    }

    public void showErrorMessage(String message, long seconds) {
        showMessage(message, seconds, MessageType.ERROR);
    }

    WorldMap createPreconfiguredMap(int tilesX, int tilesY) {
        var worldMap = new WorldMap(tilesY, tilesX);
        TileMap terrain = worldMap.terrain();

        Vector2i houseOrigin = vec_2i(tilesX / 2 - 4, tilesY / 2 - 3);

        addBorder(terrain, 3, 2);
        addHouse(terrain, houseOrigin);

        terrain.setProperty(PROPERTY_COLOR_WALL_STROKE, TileMapEditor.DEFAULT_COLOR_WALL_STROKE);
        terrain.setProperty(PROPERTY_COLOR_WALL_FILL, TileMapEditor.DEFAULT_COLOR_WALL_FILL);
        terrain.setProperty(PROPERTY_COLOR_DOOR, TileMapEditor.DEFAULT_COLOR_DOOR);

        terrain.setProperty(PROPERTY_POS_PAC, formatTile(houseOrigin.plus(3, 11)));
        terrain.setProperty(PROPERTY_POS_BONUS, formatTile(houseOrigin.plus(3, 5)));

        terrain.setProperty(PROPERTY_POS_SCATTER_RED_GHOST, formatTile(vec_2i(tilesX - 3, 0)));
        terrain.setProperty(PROPERTY_POS_SCATTER_PINK_GHOST, formatTile(vec_2i(3, 0)));
        terrain.setProperty(PROPERTY_POS_SCATTER_CYAN_GHOST, formatTile(vec_2i(tilesX - 1, tilesY - 2)));
        terrain.setProperty(PROPERTY_POS_SCATTER_ORANGE_GHOST, formatTile(vec_2i(0, tilesY - 2)));

        invalidateTerrainData();

        worldMap.food().setProperty(PROPERTY_COLOR_FOOD, TileMapEditor.DEFAULT_COLOR_FOOD);

        Logger.info("Map created. rows={}, cols={}", tilesY, tilesX);
        return worldMap;
    }
}