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
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.time.Instant;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.maps.editor.TileMapEditorViewModel.tt;
import static de.amr.games.pacman.maps.editor.TileMapUtil.*;
import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class TileMapEditor implements TileMapEditorViewModel {

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
    final ObjectProperty<WorldMap> mapPy = new SimpleObjectProperty<>();
    final BooleanProperty previewVisiblePy = new SimpleBooleanProperty(true);
    final BooleanProperty propertyEditorsVisiblePy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            changePropertyEditorsPaneVisibility(get());
        }
    };
    final BooleanProperty terrainVisiblePy = new SimpleBooleanProperty(true);
    final ObjectProperty<String> titlePy = new SimpleObjectProperty<>("Tile Map Editor");

    private final EditController editController;

    private final BorderPane contentPane = new BorderPane();
    private Stage stage;
    private Pane propertyEditorsPane;
    private Canvas editCanvas;
    private ScrollPane spEditCanvas;
    private Canvas previewCanvas;
    private ScrollPane spPreviewCanvas;
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
        spriteSheet = new Image(urlString("graphics/pacman_spritesheet.png"));
        rubberCursor = Cursor.cursor(urlString("graphics/radiergummi.jpg"));
        setMap(new WorldMap(36, 28));
    }

    private String urlString(String resourcePath) {
        URL url = requireNonNull(getClass().getResource(resourcePath));
        return url.toExternalForm();
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
    public byte selectedPaletteID() {
        return (Byte) tabPaneWithPalettes.getSelectionModel().getSelectedItem().getUserData();
    }

    @Override
    public Palette selectedPalette() {
        return palettes[selectedPaletteID()];
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
        messageCloseTime = Instant.now().plus(java.time.Duration.ofSeconds(seconds));
    }

    @Override
    public void indicateEditMode() {
        if (editCanvas != null) {
            editCanvas.setCursor(Cursor.DEFAULT);
        }
    }

    @Override
    public void indicateEraseMode() {
        if (editCanvas != null) {
            editCanvas.setCursor(rubberCursor);
        }
    }

    public void start() {
        stage.titleProperty().bind(titlePy);
        Logger.info("Canvas scrollpane height {0.00}", spEditCanvas.getHeight());
        double gridSize = spEditCanvas.getHeight() / map().terrain().numRows();
        gridSize = Math.max(gridSize, MIN_GRID_SIZE);
        Logger.info("Grid size {0.00}", gridSize);
        gridSizePy.set((int) gridSize);
        editController.showInfoMessage(tt("welcome_message"), 3);
        clock.play();
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

    public void createUI(Stage stage) {
        this.stage = checkNotNull(stage);
        createRenderers();
        createFileChooser();
        createMenuBarAndMenus();
        createEditCanvas();
        createPreviewCanvas();
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

        contentPane.setOnKeyTyped(editController::onKeyTyped);
        propertyEditorsVisiblePy.set(false);
    }

    private void createRenderers() {
        terrainMapEditRenderer = new TerrainMapEditRenderer();
        terrainMapEditRenderer.setWallStrokeColor(parseColor(DEFAULT_COLOR_WALL_STROKE));
        terrainMapEditRenderer.setWallFillColor(parseColor(DEFAULT_COLOR_WALL_FILL));

        terrainMapPreviewRenderer = new TerrainMapRenderer();
        terrainMapPreviewRenderer.setWallStrokeColor(parseColor(DEFAULT_COLOR_WALL_STROKE));
        terrainMapPreviewRenderer.setWallFillColor(parseColor(DEFAULT_COLOR_WALL_FILL));

        foodMapRenderer = new FoodMapRenderer();
        foodMapRenderer.setPelletColor(TileMapUtil.parseColor(DEFAULT_COLOR_FOOD));
        foodMapRenderer.setEnergizerColor(TileMapUtil.parseColor(DEFAULT_COLOR_FOOD));
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
        spEditCanvas = new ScrollPane(editCanvas);
        spEditCanvas.setFitToHeight(true);
        // Note: this must be done *after* the initial map has been created/loaded!
        editCanvas.heightProperty().bind(Bindings.createDoubleBinding(
                () -> (double) map().terrain().numRows() * gridSize(), mapPy, gridSizePy));
        editCanvas.widthProperty().bind(Bindings.createDoubleBinding(
                () -> (double) map().terrain().numCols() * gridSize(), mapPy, gridSizePy));
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
                WorldMap currentMap = map();
                String title = tt("map_editor");
                if (currentFilePy.get() != null) {
                    title += " - " + currentFilePy.get();
                } else if (currentMap != null && currentMap.url() != null) {
                    title += " - " + currentMap.url();
                } else {
                    title += " - <" + tt("unsaved_map") + ">";
                }
                if (currentMap != null) {
                    title += " (%d rows, %d cols)".formatted(currentMap.terrain().numRows(), currentMap.terrain().numCols());
                }
                return title;
            }, currentFilePy, mapPy
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
            } catch (Exception x) {
                Logger.error(x);
                drawBlueScreen(x); // TODO this is crap
            }
        }));
        clock.setCycleCount(Animation.INDEFINITE);
    }

    private Palette createTerrainPalette() {
        var palette = new Palette(TOOL_SIZE, 1, 19, terrainMapEditRenderer);
        palette.addTileTool(editController, Tiles.EMPTY, "Empty Space");
        palette.addTileTool(editController, Tiles.WALL_H, "Horiz. Wall");
        palette.addTileTool(editController, Tiles.WALL_V, "Vert. Wall");
        palette.addTileTool(editController, Tiles.DWALL_H, "Horiz. Double-Wall");
        palette.addTileTool(editController, Tiles.DWALL_V, "Vert. Double-Wall");
        palette.addTileTool(editController, Tiles.CORNER_NW, "NW Corner");
        palette.addTileTool(editController, Tiles.CORNER_NE, "NE Corner");
        palette.addTileTool(editController, Tiles.CORNER_SW, "SW Corner");
        palette.addTileTool(editController, Tiles.CORNER_SE, "SE Corner");
        palette.addTileTool(editController, Tiles.DCORNER_NW, "NW Corner");
        palette.addTileTool(editController, Tiles.DCORNER_NE, "NE Corner");
        palette.addTileTool(editController, Tiles.DCORNER_SW, "SW Corner");
        palette.addTileTool(editController, Tiles.DCORNER_SE, "SE Corner");
        palette.addTileTool(editController, Tiles.DCORNER_ANGULAR_NW, "NW Corner");
        palette.addTileTool(editController, Tiles.DCORNER_ANGULAR_NE, "NE Corner");
        palette.addTileTool(editController, Tiles.DCORNER_ANGULAR_SW, "SW Corner");
        palette.addTileTool(editController, Tiles.DCORNER_ANGULAR_SE, "SE Corner");
        palette.addTileTool(editController, Tiles.TUNNEL, "Tunnel");
        palette.addTileTool(editController, Tiles.DOOR, "Door");
        palette.selectTool(0); // EMPTY
        return palette;
    }

    private Palette createActorPalette() {
        var palette = new Palette(TOOL_SIZE, 1, 9, terrainMapEditRenderer);
        palette.addPropertyTool(PROPERTY_POS_PAC, "Pac-Man");
        palette.addPropertyTool(PROPERTY_POS_RED_GHOST, "Red Ghost");
        palette.addPropertyTool(PROPERTY_POS_PINK_GHOST, "Pink Ghost");
        palette.addPropertyTool(PROPERTY_POS_CYAN_GHOST, "Cyan Ghost");
        palette.addPropertyTool(PROPERTY_POS_ORANGE_GHOST, "Orange Ghost");
        palette.addPropertyTool(PROPERTY_POS_SCATTER_RED_GHOST, "Red Ghost Scatter");
        palette.addPropertyTool(PROPERTY_POS_SCATTER_PINK_GHOST, "Pink Ghost Scatter");
        palette.addPropertyTool(PROPERTY_POS_SCATTER_CYAN_GHOST, "Cyan Ghost Scatter");
        palette.addPropertyTool(PROPERTY_POS_SCATTER_ORANGE_GHOST, "Orange Ghost Scatter");
        palette.selectTool(0); // Pac-Man position
        return palette;
    }

    private Palette createFoodPalette() {
        var palette = new Palette(TOOL_SIZE, 1, 3, foodMapRenderer);
        palette.addTileTool(editController, Tiles.EMPTY, "No Food");
        palette.addTileTool(editController, Tiles.PELLET, "Pellet");
        palette.addTileTool(editController, Tiles.ENERGIZER, "Energizer");
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
        var miShowPropertyEditors = new CheckMenuItem("Properties"); //TODO localize
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
        if (sourceView == null) {
            Logger.warn("Cannot update source view as it doesn't exist yet");
            return;
        }
        try {
            String source = map.makeSource();
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
            Color foodColor = getColorFromMap(map().food(), PROPERTY_COLOR_FOOD, TileMapUtil.parseColor(DEFAULT_COLOR_FOOD));
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
            editController.ensureTerrainMapsPathsUpToDate();
            terrainMapPreviewRenderer.setScaling(gridSize() / 8.0);
            terrainMapPreviewRenderer.setWallStrokeColor(getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_STROKE, parseColor(DEFAULT_COLOR_WALL_STROKE)));
            terrainMapPreviewRenderer.setWallFillColor(getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_FILL, parseColor(DEFAULT_COLOR_WALL_FILL)));
            terrainMapPreviewRenderer.setDoorColor(getColorFromMap(terrainMap, PROPERTY_COLOR_DOOR, parseColor(DEFAULT_COLOR_DOOR)));
            terrainMapPreviewRenderer.drawMap(g, terrainMap);
        }
        if (foodVisiblePy.get()) {
            foodMapRenderer.setScaling(gridSize() / 8.0);
            Color foodColor = getColorFromMap(map().food(), PROPERTY_COLOR_FOOD, TileMapUtil.parseColor(DEFAULT_COLOR_FOOD));
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
        Palette selectedPalette = palettes[selectedPaletteID()];
        if (selectedPaletteID() == PALETTE_ID_TERRAIN) {
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