/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.maps.editor;

import de.amr.games.pacman.lib.RectArea;
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
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
import static de.amr.games.pacman.maps.editor.TileMapUtil.*;
import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class TileMapEditor implements TileMapEditorViewModel {

    static final byte MIN_GRID_SIZE = 8;
    static final byte MAX_GRID_SIZE = 48;

    static final Node NO_GRAPHIC = null;
    static final ResourceBundle TEXT_BUNDLE = ResourceBundle.getBundle(TileMapEditor.class.getPackageName() + ".texts");

    static final Font FONT_STATUS_LINE = Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 12);
    static final Font FONT_MESSAGE     = Font.font("Serif", FontWeight.EXTRA_BOLD, 14);

    static final RectArea PAC_SPRITE          = new RectArea(473,  16, 14, 14);
    static final RectArea RED_GHOST_SPRITE    = new RectArea(505,  65, 14, 14);
    static final RectArea PINK_GHOST_SPRITE   = new RectArea(553,  81, 14, 14);
    static final RectArea CYAN_GHOST_SPRITE   = new RectArea(521,  97, 14, 14);
    static final RectArea ORANGE_GHOST_SPRITE = new RectArea(521, 113, 14, 14);
    static final RectArea BONUS_SPRITE        = new RectArea(505,  49, 14, 14);

    static final String DEFAULT_COLOR_WALL_STROKE         = "rgb(33,33,255)";
    static final String DEFAULT_COLOR_WALL_FILL           = "rgb(0,0,0)";
    static final String DEFAULT_COLOR_DOOR                = "rgb(255,183, 255)";

    static final String PROPERTY_COLOR_WALL_STROKE        = "color_wall_stroke";
    static final String PROPERTY_COLOR_WALL_FILL          = "color_wall_fill";
    static final String PROPERTY_COLOR_DOOR               = "color_door";
    static final String PROPERTY_POS_BONUS                = "pos_bonus";
    static final String PROPERTY_POS_PAC                  = "pos_pac";
    static final String PROPERTY_POS_RED_GHOST            = "pos_ghost_1_red";
    static final String PROPERTY_POS_PINK_GHOST           = "pos_ghost_2_pink";
    static final String PROPERTY_POS_CYAN_GHOST           = "pos_ghost_3_cyan";
    static final String PROPERTY_POS_ORANGE_GHOST         = "pos_ghost_4_orange";
    static final String PROPERTY_POS_SCATTER_RED_GHOST    = "pos_scatter_ghost_1_red";
    static final String PROPERTY_POS_SCATTER_PINK_GHOST   = "pos_scatter_ghost_2_pink";
    static final String PROPERTY_POS_SCATTER_CYAN_GHOST   = "pos_scatter_ghost_3_cyan";
    static final String PROPERTY_POS_SCATTER_ORANGE_GHOST = "pos_scatter_ghost_4_orange";

    static final String PROPERTY_POS_HOUSE_MIN_TILE       = "pos_house_min_tile";

    static final String PROPERTY_COLOR_FOOD               = "color_food";
    static final String DEFAULT_FOOD_COLOR                = "rgb(255,255,255)";

    static final Vector2i DEFAULT_POS_HOUSE               = new Vector2i(10, 15);
    static final Vector2i DEFAULT_POS_RED_GHOST           = DEFAULT_POS_HOUSE.plus(3, -1);
    static final Vector2i DEFAULT_POS_CYAN_GHOST          = DEFAULT_POS_HOUSE.plus(1, 2);
    static final Vector2i DEFAULT_POS_PINK_GHOST          = DEFAULT_POS_HOUSE.plus(3, 2);
    static final Vector2i DEFAULT_POS_ORANGE_GHOST        = DEFAULT_POS_HOUSE.plus(5, 2);

    static final Vector2i DEFAULT_POS_BONUS               = new Vector2i(13, 20);
    static final Vector2i DEFAULT_POS_PAC                 = new Vector2i(13, 26);

    static final String PALETTE_TERRAIN = "Terrain";
    static final String PALETTE_ACTORS  = "Actors";
    static final String PALETTE_FOOD    = "Food";

    static final int TOOL_SIZE = 32;

    static String tt(String key, Object... args) {
        return MessageFormat.format(TEXT_BUNDLE.getString(key), args);
    }

    // --  end static --

    final ObjectProperty<File> currentFilePy = new SimpleObjectProperty<>();

    final BooleanProperty foodVisiblePy            = new SimpleBooleanProperty(true);

    final IntegerProperty gridSizePy = new SimpleIntegerProperty(16);

    final BooleanProperty gridVisiblePy = new SimpleBooleanProperty(true);

    final ObjectProperty<WorldMap> mapPy = new SimpleObjectProperty<>();

    final BooleanProperty propertyEditorsVisiblePy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            changePropertyEditorsPaneVisibility(get());
        }
    };

    final BooleanProperty previewVisiblePy = new SimpleBooleanProperty(true);

    final ObjectProperty<String> titlePy = new SimpleObjectProperty<>("Tile Map Editor");

    final BooleanProperty terrainVisiblePy = new SimpleBooleanProperty(true);

    private final EditController editController;

    private Stage stage;
    private final BorderPane contentPane = new BorderPane();
    private Pane propertyEditorsPane;
    private Canvas editCanvas;
    private ScrollPane editCanvasScroll;
    private Canvas previewCanvas;
    private ScrollPane previewCanvasScroll;
    private Text mapSourceView;
    private ScrollPane mapSourceViewScroll;
    private TabPane tabPaneMapViews;
    private Label messageLabel;
    private Label focussedTileInfo;
    private Label editModeIndicator;
    private HBox sliderZoomContainer;
    private FileChooser fileChooser;
    private TabPane tabPaneWithPalettes;
    private final Map<String, Palette> palettes = new HashMap<>();
    private final Image spriteSheet;

    private MenuBar menuBar;
    private Menu menuFile;
    private Menu menuEdit;
    private Menu menuLoadMap;
    private Menu menuView;

    private final ContextMenu contextMenu = new ContextMenu();

    private PropertyEditorPane     terrainMapPropertiesEditor;
    private PropertyEditorPane     foodMapPropertiesEditor;
    private TerrainMapEditRenderer terrainMapEditRenderer;
    private TerrainMapRenderer     terrainMapPreviewRenderer;
    private FoodMapRenderer        foodMapRenderer;

    private File lastUsedDir;
    private Instant messageCloseTime;
    private Timeline clock;

    public TileMapEditor() {
        this(new File(System.getProperty("user.home")));
    }

    public TileMapEditor(File workDir) {
        editController = new EditController(this);
        lastUsedDir = workDir;
        titlePy.bind(createTitleBinding());
        URL url = requireNonNull(getClass().getResource("pacman_spritesheet.png"));
        spriteSheet = new Image(url.toExternalForm());
        setMap(new WorldMap(36, 28));
    }

    @Override
    public ObjectProperty<WorldMap> worldMapProperty() {
        return mapPy;
    }

    @Override
    public IntegerProperty gridSizeProperty() {
        return gridSizePy;
    }

    @Override
    public Canvas canvas() {
        return editCanvas;
    }

    @Override
    public ContextMenu contextMenu() {
        return contextMenu;
    }

    @Override
    public String selectedPaletteID() {
        return (String) tabPaneWithPalettes.getSelectionModel().getSelectedItem().getUserData();
    }

    @Override
    public Palette selectedPalette() {
        return palettes.get(selectedPaletteID());
    }

    @Override
    public PropertyEditorPane terrainPropertiesEditor() {
        return terrainMapPropertiesEditor;
    }

    @Override
    public PropertyEditorPane foodPropertiesEditor() {
        return foodMapPropertiesEditor;
    }

    @Override
    public void updateSourceView() {
        updateSourceView(map());
    }

    @Override
    public void showMessage(String message, long seconds, MessageType type) {
        messageLabel.setText(message);
        Color color = switch (type) {
            case INFO -> Color.BLACK;
            case WARNING -> Color.GREEN;
            case ERROR -> Color.RED;
        };
        messageLabel.setTextFill(color);
        messageCloseTime = Instant.now().plus(Duration.ofSeconds(seconds));
    }

    public void start() {
        stage.titleProperty().bind(titlePy);
        // content pane and scroll pane have no height yet at this point!
        int gridSize = (int)(0.75 *  stage.getHeight() / map().terrain().numRows());
        gridSizePy.set(Math.max(gridSize, 8));
        clock.play();
        Logger.info("Stage height {}", stage.getHeight());
        showMessage(tt("welcome_message"), 3, MessageType.INFO);
    }

    public void stop() {
        clock.stop();
        editController.editingEnabledPy.set(false);
        editController.clearUnsavedChanges();
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

    public void createUI(Stage stage) {
        this.stage = checkNotNull(stage);
        createRenderers();
        createFileChooser();
        createMenuBarAndMenus();
        createEditCanvas();
        createPreviewCanvas();
        createMapSourceView();
        createTabPaneWithPalettes();
        createPropertyEditors();
        createTabPaneWithMapViews();
        createFocussedTileIndicator();
        createEditModeIndicator();
        createMessageDisplay();
        createZoomSlider();
        arrangeMainLayout();
        initActiveRendering();

        contentPane.setOnKeyTyped(this::onKeyTyped);
        propertyEditorsVisiblePy.set(false);
    }

    private void onKeyTyped(KeyEvent event) {
        Logger.info("Typed {}", event);
        String ch = event.getCharacter();
        switch (ch) {
            case "n" -> editController.setNormalDrawMode();
            case "s" -> editController.setSymmetricDrawMode();
            case "x" -> editController.setEraseMode();
            case "+" -> {
                if (gridSizePy.get() < MAX_GRID_SIZE) {
                    gridSizePy.set(gridSize() + 1);
                }
            }
            case "-" -> {
                if (gridSizePy.get() > MIN_GRID_SIZE) {
                    gridSizePy.set(gridSize() - 1);
                }
            }
        }
    }

    private void createRenderers() {
        terrainMapEditRenderer = new TerrainMapEditRenderer();
        terrainMapEditRenderer.setWallStrokeColor(parseColor(DEFAULT_COLOR_WALL_STROKE));
        terrainMapEditRenderer.setWallFillColor(parseColor(DEFAULT_COLOR_WALL_FILL));

        terrainMapPreviewRenderer = new TerrainMapRenderer();
        terrainMapPreviewRenderer.setWallStrokeColor(parseColor(DEFAULT_COLOR_WALL_STROKE));
        terrainMapPreviewRenderer.setWallFillColor(parseColor(DEFAULT_COLOR_WALL_FILL));

        foodMapRenderer = new FoodMapRenderer();
        foodMapRenderer.setPelletColor(TileMapUtil.parseColor(DEFAULT_FOOD_COLOR));
        foodMapRenderer.setEnergizerColor(TileMapUtil.parseColor(DEFAULT_FOOD_COLOR));
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
        editCanvas.setOnContextMenuRequested(event -> editController.onContextMenuRequested(contextMenu, event));
        editCanvas.setOnMouseClicked(editController::onMouseClicked);
        editCanvas.setOnMouseMoved(editController::onMouseMoved);
        editCanvas.setOnKeyPressed(editController::onKeyPressed);
        editCanvasScroll = new ScrollPane(editCanvas);
        editCanvasScroll.setFitToHeight(true);
        // Note: this must be done *after* the initial map has been created/loaded!
        editCanvas.heightProperty().bind(Bindings.createDoubleBinding(
                () -> (double) map().terrain().numRows() * gridSize(), mapPy, gridSizePy));
        editCanvas.widthProperty().bind(Bindings.createDoubleBinding(
                () -> (double) map().terrain().numCols() * gridSize(), mapPy, gridSizePy));
    }

    private void createPreviewCanvas() {
        previewCanvas = new Canvas();
        previewCanvasScroll = new ScrollPane(previewCanvas);
        previewCanvasScroll.setFitToHeight(true);
        previewCanvasScroll.hvalueProperty().bindBidirectional(editCanvasScroll.hvalueProperty());
        previewCanvasScroll.vvalueProperty().bindBidirectional(editCanvasScroll.vvalueProperty());
        previewCanvasScroll.visibleProperty().bind(previewVisiblePy);
        previewCanvas.widthProperty().bind(editCanvas.widthProperty());
        previewCanvas.heightProperty().bind(editCanvas.heightProperty());
    }

    private void createMapSourceView() {
        mapSourceView = new Text();
        mapSourceView.setSmooth(true);
        mapSourceView.setFontSmoothingType(FontSmoothingType.LCD);
        mapSourceView.setFont(Font.font("Monospace", 14));

        var vbox = new VBox(mapSourceView);
        vbox.setPadding(new Insets(10, 20, 10, 20));

        mapSourceViewScroll = new ScrollPane(vbox);
        mapSourceViewScroll.setFitToHeight(true);
    }

    private void createTabPaneWithMapViews() {
        var tabSourceView = new Tab(tt("source"), mapSourceViewScroll);
        tabSourceView.setClosable(false);

        var splitPane = new SplitPane(editCanvasScroll, previewCanvasScroll);
        splitPane.setDividerPositions(0.5);

        var tabPreview = new Tab(tt("preview"), splitPane);
        tabPreview.setClosable(false);

        tabPaneMapViews = new TabPane(tabPreview, tabSourceView);
        tabPaneMapViews.setSide(Side.BOTTOM);
    }

    private void createTabPaneWithPalettes() {
        palettes.put(PALETTE_ACTORS, createActorPalette());
        palettes.put(PALETTE_TERRAIN, createTerrainPalette());
        palettes.put(PALETTE_FOOD, createFoodPalette());

        var tab1 = new Tab(tt("terrain"), palettes.get(PALETTE_TERRAIN).root());
        tab1.setClosable(false);
        tab1.setUserData(PALETTE_TERRAIN);

        var tab2 = new Tab(tt("actors"), palettes.get(PALETTE_ACTORS).root());
        tab2.setClosable(false);
        tab2.setUserData(PALETTE_ACTORS);

        var tab3 = new Tab(tt("pellets"), palettes.get(PALETTE_FOOD).root());
        tab3.setClosable(false);
        tab3.setUserData(PALETTE_FOOD);

        tabPaneWithPalettes = new TabPane(tab2, tab1, tab3);
        tabPaneWithPalettes.setPadding(new Insets(5, 5, 5, 5));
        tabPaneWithPalettes.setMinHeight(75);
    }

    private void createPropertyEditors() {
        terrainMapPropertiesEditor = new PropertyEditorPane(editController);
        terrainMapPropertiesEditor.enabledPy.bind(editController.editingEnabledPy);
        terrainMapPropertiesEditor.setPadding(new Insets(10,0,0,0));

        foodMapPropertiesEditor = new PropertyEditorPane(editController);
        foodMapPropertiesEditor.enabledPy.bind(editController.editingEnabledPy);
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
        focussedTileInfo.textProperty().bind(editController.focussedTilePy.map(
            tile -> tile != null ? "Tile: x=%2d y=%2d".formatted(tile.x(), tile.y()) : "n/a"));
    }

    private void createEditModeIndicator() {
        editModeIndicator = new Label();
        editModeIndicator.setFont(FONT_STATUS_LINE);
        editModeIndicator.textProperty().bind(Bindings.createStringBinding(
                () -> {
                    if (!editController.editingEnabledPy.get()) {
                        return "Editing disabled";
                    }
                    return switch (editController.modePy.get()) {
                        case DRAW -> editController.symmetricEditModePy.get() ?  "Symmetric" : "Normal";
                        case ERASE -> "Erase";
                    };

                },
                editController.modePy, editController.editingEnabledPy, editController.symmetricEditModePy
        ));
    }

    private void createMessageDisplay() {
        messageLabel = new Label();
        messageLabel.setFont(FONT_MESSAGE);
        messageLabel.setMinWidth(200);
        messageLabel.setPadding(new Insets(0, 0, 0, 10));
    }

    private void createZoomSlider() {
        var sliderZoom = new Slider(MIN_GRID_SIZE, MAX_GRID_SIZE, 2 * MIN_GRID_SIZE);
        sliderZoom.valueProperty().bindBidirectional(gridSizePy);
        sliderZoom.setShowTickLabels(false);
        sliderZoom.setShowTickMarks(true);
        sliderZoom.setPrefWidth(150);

        sliderZoomContainer = new HBox(new Label("Zoom"), sliderZoom);
        sliderZoomContainer.setSpacing(5);
    }

    private void arrangeMainLayout() {
        var filler = new Region();
        HBox.setHgrow(filler, Priority.ALWAYS);

        var bottom = new HBox(focussedTileInfo, editModeIndicator, messageLabel, filler, sliderZoomContainer);
        bottom.setPadding(new Insets(10, 10, 10, 10));

        var right = new VBox(tabPaneWithPalettes, tabPaneMapViews, bottom);
        right.setPadding(new Insets(0,5,0,5));

        contentPane.setLeft(propertyEditorsPane);
        contentPane.setCenter(right);
    }

    // Active rendering (good idea?)
    private void initActiveRendering() {
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

    private Palette createTerrainPalette() {
        var palette = new Palette(TOOL_SIZE, 1, 19, terrainMapEditRenderer);
        palette.setTools(
            palette.createTileValueEditorTool(editController, Tiles.WALL_H, "Horiz. Wall"),
            palette.createTileValueEditorTool(editController, Tiles.WALL_V, "Vert. Wall"),
            palette.createTileValueEditorTool(editController, Tiles.DWALL_H, "Hor. Double-Wall"),
            palette.createTileValueEditorTool(editController, Tiles.DWALL_V, "Vert. Double-Wall"),
            palette.createTileValueEditorTool(editController, Tiles.CORNER_NW, "NW Corner"),
            palette.createTileValueEditorTool(editController, Tiles.CORNER_NE, "NE Corner"),
            palette.createTileValueEditorTool(editController, Tiles.CORNER_SW, "SW Corner"),
            palette.createTileValueEditorTool(editController, Tiles.CORNER_SE, "SE Corner"),
            palette.createTileValueEditorTool(editController, Tiles.DCORNER_NW, "NW Corner"),
            palette.createTileValueEditorTool(editController, Tiles.DCORNER_NE, "NE Corner"),
            palette.createTileValueEditorTool(editController, Tiles.DCORNER_SW, "SW Corner"),
            palette.createTileValueEditorTool(editController, Tiles.DCORNER_SE, "SE Corner"),
            palette.createTileValueEditorTool(editController, Tiles.DCORNER_ANGULAR_NW, "NW Corner"),
            palette.createTileValueEditorTool(editController, Tiles.DCORNER_ANGULAR_NE, "NE Corner"),
            palette.createTileValueEditorTool(editController, Tiles.DCORNER_ANGULAR_SW, "SW Corner"),
            palette.createTileValueEditorTool(editController, Tiles.DCORNER_ANGULAR_SE, "SE Corner"),
            palette.createTileValueEditorTool(editController, Tiles.EMPTY, "Empty Space"),
            palette.createTileValueEditorTool(editController, Tiles.TUNNEL, "Tunnel"),
            palette.createTileValueEditorTool(editController, Tiles.DOOR, "Door")
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
            palette.createTileValueEditorTool(editController, Tiles.EMPTY, "No Food"),
            palette.createTileValueEditorTool(editController, Tiles.PELLET, "Pellet"),
            palette.createTileValueEditorTool(editController, Tiles.ENERGIZER, "Energizer")
        );
        palette.selectTool(0); // EMPTY
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
        miOpen.setOnAction(e -> openMapFile());

        var miSaveAs = new MenuItem(tt("menu.file.save_as"));
        miSaveAs.setOnAction(e -> showSaveDialog());

        menuFile = new Menu(tt("menu.file"), NO_GRAPHIC, miNew, miOpen, miSaveAs);
    }

    private void createEditMenu() {
        var miSymmetricMode = new CheckMenuItem(tt("menu.edit.symmetric"));
        miSymmetricMode.selectedProperty().bindBidirectional(editController.symmetricEditModePy);

        var miAddBorder = new MenuItem(tt("menu.edit.add_border"));
        miAddBorder.setOnAction(e -> editController.addBorder(map().terrain(), 3, 2));

        var miClearTerrain = new MenuItem(tt("menu.edit.clear_terrain"));
        miClearTerrain.setOnAction(e -> {
            editController.clearTerrain(map());
            editController.markTileMapEdited(map().terrain());
        });

        var miClearFood = new MenuItem(tt("menu.edit.clear_food"));
        miClearFood.setOnAction(e -> editController.clearFood(map()));

        menuEdit = new Menu(tt("menu.edit"), NO_GRAPHIC,
            miSymmetricMode,
            new SeparatorMenuItem(),
            miAddBorder,
            miClearTerrain,
            miClearFood);

        menuEdit.disableProperty().bind(editController.editingEnabledPy.not());
    }

    private void createLoadMapMenu() {
        menuLoadMap = new Menu(tt("menu.load_map"));
    }

    private void createViewMenu() {
        var miShowPropertyEditors = new CheckMenuItem("Property Editors"); //TODO localize
        miShowPropertyEditors.selectedProperty().bindBidirectional(propertyEditorsVisiblePy);

        var miShowTerrain = new CheckMenuItem(tt("menu.view.terrain"));
        miShowTerrain.selectedProperty().bindBidirectional(terrainVisiblePy);

        var miShowFood = new CheckMenuItem(tt("menu.view.food"));
        miShowFood.selectedProperty().bindBidirectional(foodVisiblePy);

        var miShowGrid = new CheckMenuItem(tt("menu.view.grid"));
        miShowGrid.selectedProperty().bindBidirectional(gridVisiblePy);

        var miShowPreview = new CheckMenuItem(tt("menu.view.preview"));
        miShowPreview.selectedProperty().bindBidirectional(previewVisiblePy);

        menuView = new Menu(tt("menu.view"), NO_GRAPHIC,
            miShowPropertyEditors,
            miShowTerrain,
            miShowFood,
            miShowGrid,
            new SeparatorMenuItem(),
            miShowPreview);
    }

    public void addLoadMapMenuItem(String description, WorldMap map) {
        checkNotNull(description);
        checkNotNull(map);
        var miLoadMap = new MenuItem(description);
        miLoadMap.setOnAction(e -> loadMap(map));
        menuLoadMap.getItems().add(miLoadMap);
    }

    public void loadMap(WorldMap worldMap) {
        checkNotNull(worldMap);
        if (editController.hasUnsavedChanges()) {
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
                WorldMap map = editController.createPreconfiguredMap(size.x(), size.y());
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
                editController.clearUnsavedChanges();
                readMapFile(file);
            } else {
                Logger.error("No .world file selected");
                showMessage("No .world file selected", 2, MessageType.WARNING);
            }
        }
    }

    public void showSaveConfirmationDialog(Runnable saveAction, Runnable noSaveAction) {
        if (editController.hasUnsavedChanges()) {
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
        if (!editController.editingEnabledPy.get()) {
            drawEditingHint(g);
        }
        Vector2i focussedTile = editController.focussedTilePy.get();
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
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
        if (terrainVisiblePy.get()) {
            TileMap terrainMap = map().terrain();
            editController.ensureTerrainMapsUpToDate();
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

    private void drawSprite(GraphicsContext g, String tilePropertyName, RectArea sprite, Vector2i defaultTile) {
        var tile = getTileFromMap(map().terrain(), tilePropertyName, defaultTile);
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
        Palette selectedPalette = palettes.get(selectedPaletteID());
        if (selectedPaletteID().equals(PALETTE_TERRAIN)) {
            double scaling = terrainMapEditRenderer.scaling();
            terrainMapEditRenderer.setScaling((double) TOOL_SIZE / 8);
            terrainMapEditRenderer.setWallStrokeColor(terrainMapEditRenderer.wallStrokeColor);
            terrainMapEditRenderer.setScaling(scaling);
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