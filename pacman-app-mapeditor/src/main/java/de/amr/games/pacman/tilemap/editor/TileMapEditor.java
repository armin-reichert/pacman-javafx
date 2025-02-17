/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.*;
import de.amr.games.pacman.tilemap.rendering.FoodMapRenderer;
import de.amr.games.pacman.tilemap.rendering.TerrainColorScheme;
import de.amr.games.pacman.tilemap.rendering.TerrainRenderer;
import de.amr.games.pacman.uilib.Ufx;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.tilemap.TileMap.formatTile;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.tilemap.editor.ArcadeMap.*;
import static de.amr.games.pacman.tilemap.editor.TileMapEditorUtil.*;

/**
 * @author Armin Reichert
 */
public class TileMapEditor {

    public static final short REFRESH_RATE = 10;

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
        return MessageFormat.format(TEXT_BUNDLE.getString(key), args);
    }

    private static boolean isSupportedImageFile(File file) {
        return Stream.of(".bmp", ".gif", ".jpg", ".png").anyMatch(ext -> file.getName().toLowerCase().endsWith(ext));
    }

    public static final Font FONT_CONTEXT_MENU_COLOR_TEXT = Font.font("Monospace", FontWeight.BOLD, 14);
    public static final Font FONT_DROP_HINT               = Font.font("Sans", FontWeight.BOLD, 16);
    public static final Font FONT_MESSAGE                 = Font.font("Sans", FontWeight.NORMAL, 14);
    public static final Font FONT_SOURCE_VIEW             = Font.font("Monospace", FontWeight.NORMAL, 14);
    public static final Font FONT_STATUS_LINE_EDIT_MODE   = Font.font("Sans", FontWeight.BOLD, 16);
    public static final Font FONT_STATUS_LINE_NORMAL      = Font.font("Sans", FontWeight.NORMAL, 14);

    public static final Color COLOR_CANVAS_BACKGROUND = Color.BLACK;

    public static final Node NO_GRAPHIC = null;

    public static final FileChooser.ExtensionFilter FILTER_WORLD_MAP = new FileChooser.ExtensionFilter("World Map Files", "*.world");
    public static final FileChooser.ExtensionFilter FILTER_IMAGE = new FileChooser.ExtensionFilter("Image Files", "*.bmp", "*.gif", "*.jpg", "*.png");
    public static final FileChooser.ExtensionFilter FILTER_ALL = new FileChooser.ExtensionFilter("All Files", "*.*");

    // Change management

    public class ChangeManager {
        private boolean unsavedChanges;
        private boolean terrainChangesPending;
        private boolean foodChangesPending;
        private boolean terrainDataUpToDate;

        public void setUnsavedChanges() { unsavedChanges = true; }
        public void clearUnsavedChanges() { unsavedChanges = false; }
        public boolean hasUnsavedChanges() { return unsavedChanges; }

        public void markChanged() {
            markTerrainChanged();
            markFoodChanged();
            setUnsavedChanges();
        }

        public void markTerrainChanged() {
            unsavedChanges = true;
            terrainChangesPending = true;
            terrainDataUpToDate = false;
        }

        public void markFoodChanged() {
            foodChangesPending = true;
        }

        private void processChanges() {
            if (!terrainDataUpToDate) {
                tilesWithErrors.clear();
                tilesWithErrors.addAll(worldMap().updateObstacleList());
                terrainDataUpToDate = true;
            }
            if (terrainChangesPending) {
                terrainPropertiesEditor().rebuildPropertyEditors();
                mazePreview3D.updateTerrain();
                updateSourceView();
                terrainChangesPending = false;
                Logger.info("Terrain updated");
            }
            if (foodChangesPending) {
                foodPropertiesEditor().rebuildPropertyEditors();
                mazePreview3D.updateFood();
                updateSourceView();
                foodChangesPending = false;
                Logger.info("Food updated");
            }
        }
    }

    private final ChangeManager changeManager = new ChangeManager();

    // Properties

    private final ObjectProperty<File> currentFilePy = new SimpleObjectProperty<>();

    private final ObjectProperty<WorldMap> worldMapPy = new SimpleObjectProperty<>(new WorldMap(28, 36)) {
        @Override
        protected void invalidated() {
            WorldMap worldMap = get();
            if (foodPropertiesEditor() != null) {
                foodPropertiesEditor().setTileMap(worldMap.food());
            }
            if (terrainPropertiesEditor() != null) {
                terrainPropertiesEditor().setTileMap(worldMap.terrain());
            }
            templateImagePy.set(null);
            if (tabTemplateImage.isSelected()) {
                tabPaneEditorViews.getSelectionModel().select(tabEditCanvas);
            }
            changeManager.markFoodChanged();
            changeManager.markTerrainChanged();
            changeManager.setUnsavedChanges();
        }
    };

    private final IntegerProperty gridSizePy = new SimpleIntegerProperty(8);

    private final BooleanProperty gridVisiblePy = new SimpleBooleanProperty(true);
    private final BooleanProperty foodVisiblePy = new SimpleBooleanProperty(true);
    private final BooleanProperty terrainVisiblePy = new SimpleBooleanProperty(true);
    private final BooleanProperty segmentNumbersDisplayedPy = new SimpleBooleanProperty(false);
    private final BooleanProperty obstacleInnerAreaDisplayedPy = new SimpleBooleanProperty(false);

    private final BooleanProperty propertyEditorsVisiblePy = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            setPropertyEditorsVisible(get());
        }
    };

    private final StringProperty titlePy = new SimpleStringProperty("Tile Map Editor");

    private final ObjectProperty<EditMode> editModePy = new SimpleObjectProperty<>(EditMode.INSPECT) {
        @Override
        protected void invalidated() {
            onEditModeChanged(get());
        }
    };

    private final BooleanProperty symmetricEditModePy = new SimpleBooleanProperty(true);

    private final ObjectProperty<Image> templateImagePy = new SimpleObjectProperty<>();

    private final ObjectProperty<Image> templateImageGreyPy = new SimpleObjectProperty<>();

    // Accessor methods


    public ChangeManager getChangeManager() { return changeManager;}

    public ObjectProperty<WorldMap> worldMapProperty() { return worldMapPy; }

    public WorldMap worldMap() { return worldMapPy.get(); }

    public void setWorldMap(WorldMap worldMap) { worldMapPy.set(assertNotNull(worldMap)); }

    public IntegerProperty gridSizeProperty() { return gridSizePy; }

    public int gridSize() { return gridSizePy.get(); }

    public BooleanProperty gridVisibleProperty() { return gridVisiblePy; }

    public BooleanProperty terrainVisibleProperty() { return terrainVisiblePy; }

    public BooleanProperty foodVisibleProperty() { return foodVisiblePy; }

    public BooleanProperty segmentNumbersDisplayedProperty() { return segmentNumbersDisplayedPy; }

    public BooleanProperty obstacleInnerAreaDisplayedProperty() { return obstacleInnerAreaDisplayedPy; }

    public boolean isSymmetricEditMode() { return symmetricEditModePy.get(); }

    public TerrainRendererInEditor terrainRendererInEditor() { return terrainRendererInEditor; }

    public FoodMapRenderer foodRenderer() { return foodRenderer; }

    public StringProperty titleProperty() { return titlePy; }

    public ObjectProperty<Image> templateImageProperty() { return templateImagePy; }

    public ObjectProperty<Image> templateImageGreyProperty() { return templateImageGreyPy; }

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

    // Attributes

    private File currentDirectory;
    private Instant messageCloseTime;
    private Timeline clock;
    private final List<Vector2i> tilesWithErrors = new ArrayList<>();

    private final BorderPane contentPane = new BorderPane();
    private Stage stage;
    private Pane propertyEditorsPane;
    private EditCanvas editCanvas;
    private ScrollPane spEditCanvas;
    private ScrollPane spPreview2D;
    private Canvas canvasPreview2D;
    private Text sourceView;
    private ScrollPane spSourceView;
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

    private TerrainRendererInEditor terrainRendererInEditor;
    private TerrainRenderer terrainRendererInPreview;
    private FoodMapRenderer foodRenderer;

    public void createUI(Stage stage) {
        this.stage = assertNotNull(stage);

        // renderers must be created before palettes!
        TerrainColorScheme colors = new TerrainColorScheme(
            Color.BLACK,
            parseColor(MS_PACMAN_COLOR_WALL_FILL),
            parseColor(MS_PACMAN_COLOR_WALL_STROKE),
            parseColor(MS_PACMAN_COLOR_DOOR)
        );
        createRenderers(colors, parseColor(MS_PACMAN_COLOR_FOOD));

        createFileChooser();
        createEditCanvas();
        createPreview2D();
        createPreview3D();
        createTemplateImageView();
        createMapSourceView();
        createPalettes();
        createPropertyEditors();
        createTabPaneWithEditViews();
        createTabPaneWithPreviews();
        createMessageDisplay();
        createZoomSlider();
        createStatusLine();
        createMenuBarAndMenus();

        arrangeMainLayout();
        initActiveRendering();

        titlePy.bind(createTitleBinding());
        templateImageGreyPy.bind(templateImagePy.map(Ufx::imageToGreyscale));

        // Input handlers
        contentPane.setOnKeyTyped(this::onKeyTyped);
        contentPane.setOnKeyPressed(this::onKeyPressed);

    }

    public void init(File workDir) {
        currentDirectory = workDir;
        setWorldMap(new WorldMap(36, 28));
        mazePreview3D.reset();
        setEditMode(EditMode.INSPECT);
    }

    public void start() {
        stage.titleProperty().bind(titlePy);
        setPropertyEditorsVisible(propertyEditorsVisiblePy.get());
        spEditCanvas.heightProperty().addListener((py,ov,nv) -> {
            if (ov.doubleValue() == 0) { // initial resize
                double gridSize = Math.max(spEditCanvas.getHeight() / worldMap().terrain().numRows(), MIN_GRID_SIZE);
                gridSizePy.set((int) gridSize);
            }
        });
        showEditHelpText();
        clock.play();
    }

    public void stop() {
        clock.stop();
        setEditMode(EditMode.INSPECT);
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

    private void showEditHelpText() {
        showMessage(tt("edit_help"), 30, MessageType.INFO);
    }

    private void createRenderers(TerrainColorScheme colors, Color foodColor) {
        terrainRendererInEditor = new TerrainRendererInEditor();
        terrainRendererInEditor.setColors(colors);
        terrainRendererInPreview = new TerrainRenderer();
        terrainRendererInPreview.setColors(colors);
        foodRenderer = new FoodMapRenderer();
        foodRenderer.setPelletColor(foodColor);
        foodRenderer.setEnergizerColor(foodColor);
    }

    private void createFileChooser() {
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(FILTER_WORLD_MAP, FILTER_ALL);
        fileChooser.setSelectedExtensionFilter(FILTER_WORLD_MAP);
        fileChooser.setInitialDirectory(currentDirectory);
    }

    private void createEditCanvas() {
        editCanvas = new EditCanvas(this);
        registerDragAndDropImageHandler(editCanvas);
        spEditCanvas = new ScrollPane(editCanvas);
        spEditCanvas.setFitToHeight(true);
    }

    private void createPreview2D() {
        canvasPreview2D = new Canvas();
        canvasPreview2D.widthProperty().bind(editCanvas.widthProperty());
        canvasPreview2D.heightProperty().bind(editCanvas.heightProperty());
        spPreview2D = new ScrollPane(canvasPreview2D);
        spPreview2D.setFitToHeight(true);
        spPreview2D.hvalueProperty().bindBidirectional(spEditCanvas.hvalueProperty());
        spPreview2D.vvalueProperty().bindBidirectional(spEditCanvas.vvalueProperty());
    }

    private void createPreview3D() {
        mazePreview3D = new MazePreview3D(500, 500);
        mazePreview3D.foodVisibleProperty().bind(foodVisiblePy);
        mazePreview3D.terrainVisibleProperty().bind(terrainVisiblePy);
        mazePreview3D.worldMapProperty().bind(worldMapPy);
    }

    private void createTemplateImageView() {
        templateImageCanvas = new TemplateImageCanvas(this);
        StackPane pane = new StackPane(templateImageCanvas);
        pane.setBackground(Background.fill(Color.BLACK));
        spTemplateImage = new ScrollPane(pane);
    }

    public void setTerrainMapProperty(String name, String value) {
        worldMap().terrain().setProperty(name, value);
        changeManager.markTerrainChanged();
    }

    private void removeTerrainMapProperty(String name) {
        worldMap().terrain().removeProperty(name);
        changeManager.markTerrainChanged();
    }

    public void setFoodMapProperty(String name, String value) {
        worldMap().food().setProperty(name, value);
        changeManager.markFoodChanged();
    }

    private void removeFoodMapProperty(String name) {
        worldMap().food().removeProperty(name);
        changeManager.markFoodChanged();
    }

    private void createMapSourceView() {
        sourceView = new Text();
        sourceView.setSmooth(true);
        sourceView.setFontSmoothingType(FontSmoothingType.LCD);
        sourceView.setFont(FONT_SOURCE_VIEW);

        var vbox = new VBox(sourceView);
        vbox.setPadding(new Insets(10, 20, 10, 20));

        spSourceView = new ScrollPane(vbox);
        spSourceView.setFitToHeight(true);
    }

    private void createTabPaneWithEditViews() {
        tabEditCanvas = new Tab(tt("tab_editor"), spEditCanvas);

        var hint = new Label(tt("image_drop_hint"));
        hint.setFont(FONT_DROP_HINT);

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
                if (isSupportedImageFile(file)) {
                    e.acceptTransferModes(TransferMode.COPY);
                }
            }
            e.consume();
        });
        node.setOnDragDropped(this::onMazeImageFileDropped);
    }

    private void onMazeImageFileDropped(DragEvent e) {
        if (e.getDragboard().hasFiles()) {
            File file = e.getDragboard().getFiles().getFirst();
            if (isSupportedImageFile(file)) {
                e.acceptTransferModes(TransferMode.COPY);
                try (FileInputStream in = new FileInputStream(file)) {
                    Image image = new Image(in);
                    if (image.getHeight() % TS != 0 || image.getWidth() % TS != 0) {
                        showMessage("Image size seems dubious", 3, MessageType.WARNING);
                        return;
                    }
                    // 3 empty rows above maze, 2 empty rows below
                    int tilesY = 5 + (int) (image.getHeight() / TS), tilesX = (int) (image.getWidth() / TS);
                    setBlankMap(tilesX, tilesY);
                    templateImagePy.set(image);
                    removeTerrainMapProperty(PROPERTY_COLOR_WALL_FILL);
                    removeTerrainMapProperty(PROPERTY_COLOR_WALL_STROKE);
                    removeTerrainMapProperty(PROPERTY_COLOR_DOOR);
                    removeFoodMapProperty(PROPERTY_COLOR_FOOD);
                    showMessage("Select colors for tile identification!", 10, MessageType.INFO);
                    tabPaneEditorViews.getSelectionModel().select(tabTemplateImage);
                } catch (IOException x) {
                    showMessage("Error dropping file " + file, 3, MessageType.ERROR);
                    Logger.error(x);
                }
            }
        }
        e.consume();
    }

    private void createTabPaneWithPreviews() {
        tabPreview2D = new Tab(tt("preview2D"), spPreview2D);
        tabPreview3D = new Tab(tt("preview3D"), mazePreview3D.getSubScene());
        tabSourceView = new Tab(tt("source"), spSourceView);

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
        palettes[PALETTE_ID_ACTORS]  = createActorPalette(PALETTE_ID_ACTORS, TOOL_SIZE, this, terrainRendererInEditor);
        palettes[PALETTE_ID_TERRAIN] = createTerrainPalette(PALETTE_ID_TERRAIN, TOOL_SIZE, this, terrainRendererInEditor);
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
        sliderZoom.valueProperty().bindBidirectional(gridSizePy);
        sliderZoom.setShowTickLabels(false);
        sliderZoom.setShowTickMarks(true);
        sliderZoom.setPrefWidth(100);
    }

    private void createStatusLine() {
        var lblMapSize = new Label();
        lblMapSize.setFont(FONT_STATUS_LINE_NORMAL);
        lblMapSize.textProperty().bind(worldMapPy.map(worldMap -> (worldMap != null)
            ? "Cols: %d Rows: %d".formatted(worldMap.terrain().numCols(), worldMap.terrain().numRows()) : "")
        );

        var lblFocussedTile = new Label();
        lblFocussedTile.setFont(FONT_STATUS_LINE_NORMAL);
        lblFocussedTile.setMinWidth(70);
        lblFocussedTile.setMaxWidth(70);
        lblFocussedTile.textProperty().bind(editCanvas.focussedTileProperty().map(
            tile -> tile != null ? "(%2d,%2d)".formatted(tile.x(), tile.y()) : "n/a"));

        var lblEditMode = new Label();
        lblEditMode.setAlignment(Pos.CENTER_RIGHT);
        lblEditMode.setMinWidth(80);
        lblEditMode.setFont(FONT_STATUS_LINE_EDIT_MODE);
        lblEditMode.setTextFill(Color.FORESTGREEN);
        lblEditMode.setEffect(new Glow());
        lblEditMode.textProperty().bind(Bindings.createStringBinding(
            () -> switch (editModePy.get()) {
                case INSPECT -> tt("mode.inspect");
                case EDIT    -> isSymmetricEditMode() ?  tt("mode.symmetric") : tt("mode.edit");
                case ERASE   -> tt("mode.erase");
            }, editModePy, symmetricEditModePy
        ));


        var lblSliderZoom = new Label("Zoom:"); //TODO localize
        lblSliderZoom.setFont((FONT_STATUS_LINE_NORMAL));
        lblSliderZoom.setLabelFor(sliderZoom);
        lblSliderZoom.setPadding(new Insets(0, 10, 0, 0));

        statusLine = new HBox(
            lblMapSize,
            filler(10),
            lblFocussedTile,
            spacer(),
            messageLabel,
            spacer(),
            lblSliderZoom,
            sliderZoom,
            filler(30),
            lblEditMode
        );

        statusLine.setPadding(new Insets(10, 10, 10, 10));
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
                if (worldMap() != null && worldMap().url() != null) {
                    return  "%s: [%s]".formatted( tt("map_editor"), worldMap().url() );
                }
                return "%s: [%s: %d rows %d cols]".formatted(
                        tt("map_editor"), tt("unsaved_map"),
                        worldMap().terrain().numRows(), worldMap().terrain().numCols() );
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
        double frameDuration = 1000.0 / REFRESH_RATE;
        clock = new Timeline(REFRESH_RATE, new KeyFrame(Duration.millis(frameDuration), e -> {

            changeManager.processChanges();

            updateMessageAnimation();

            TileMap terrainMap = worldMap().terrain();
            TerrainColorScheme colors = new TerrainColorScheme(
                    COLOR_CANVAS_BACKGROUND,
                getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_FILL, parseColor(MS_PACMAN_COLOR_WALL_FILL)),
                getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_STROKE, parseColor(MS_PACMAN_COLOR_WALL_STROKE)),
                getColorFromMap(terrainMap, PROPERTY_COLOR_DOOR, parseColor(MS_PACMAN_COLOR_DOOR))
            );
            try {
                drawSelectedPalette(colors);
            } catch (Exception x) {
                Logger.error(x);
            }
            if (tabEditCanvas.isSelected()) {
                try {
                    editCanvas.draw(colors);
                } catch (Exception x) {
                    Logger.error(x);
                }
            }
            if (tabTemplateImage.isSelected()) {
                try {
                    templateImageCanvas.draw();
                } catch (Exception x) {
                    Logger.error(x);
                }
            }
            if (tabPreview2D.isSelected()) {
                try {
                    drawPreview2D(colors);
                } catch (Exception x) {
                    Logger.error(x);
                }
            }
        }));
        clock.setCycleCount(Animation.INDEFINITE);
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
        miOpenTemplateImage.setOnAction(e -> openTemplateImage());

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
        var miSymmetricMode = new CheckMenuItem(tt("menu.edit.symmetric"));
        miSymmetricMode.selectedProperty().bindBidirectional(symmetricEditModePy);

        var miAddBorder = new MenuItem(tt("menu.edit.add_border"));
        miAddBorder.setOnAction(e -> addBorderWall(worldMap().terrain(), EMPTY_ROWS_BEFORE_MAZE, EMPTY_ROWS_BELOW_MAZE));

        var miClearTerrain = new MenuItem(tt("menu.edit.clear_terrain"));
        miClearTerrain.setOnAction(e -> {
            worldMap().terrain().clear(TerrainTiles.EMPTY);
            changeManager.markTerrainChanged();
        });

        var miClearFood = new MenuItem(tt("menu.edit.clear_food"));
        miClearFood.setOnAction(e -> {
            worldMap().food().clear(FoodTiles.EMPTY);
            changeManager.markFoodChanged();
        });

        var miDetectPellets = new MenuItem(tt("menu.edit.identify_tiles"));
        //TODO why doesn't this work?
        //miDetectPellets.disableProperty().bind(templateImagePy.map(image -> image == null));
        miDetectPellets.disableProperty().bind(Bindings.createBooleanBinding(() -> templateImagePy.get() == null,  templateImagePy));
        miDetectPellets.setOnAction(ae -> identifyTilesFromTemplateImage());

        menuEdit = new Menu(tt("menu.edit"), NO_GRAPHIC,
            miSymmetricMode,
            new SeparatorMenuItem(),
            miAddBorder,
            miClearTerrain,
            miClearFood,
            miDetectPellets);

        menuEdit.disableProperty().bind(editModePy.map(mode -> mode == EditMode.INSPECT));

        // Maps
        menuLoadMap = new Menu(tt("menu.load_map"));

        // View
        var miProperties = new CheckMenuItem(tt("menu.view.properties"));
        miProperties.selectedProperty().bindBidirectional(propertyEditorsVisiblePy);

        var miTerrain = new CheckMenuItem(tt("menu.view.terrain"));
        miTerrain.selectedProperty().bindBidirectional(terrainVisiblePy);

        var miFood = new CheckMenuItem(tt("menu.view.food"));
        miFood.selectedProperty().bindBidirectional(foodVisiblePy);

        var miGrid = new CheckMenuItem(tt("menu.view.grid"));
        miGrid.selectedProperty().bindBidirectional(gridVisiblePy);

        var miSegmentNumbers = new CheckMenuItem(tt("menu.view.segment_numbers"));
        miSegmentNumbers.selectedProperty().bindBidirectional(segmentNumbersDisplayedPy);

        var miObstacleInnerArea = new CheckMenuItem(tt("inner_obstacle_area"));
        miObstacleInnerArea.selectedProperty().bindBidirectional(obstacleInnerAreaDisplayedPy);

        menuView = new Menu(tt("menu.view"), NO_GRAPHIC,
            miProperties, miTerrain, miSegmentNumbers, miObstacleInnerArea, miFood, miGrid);

        menuBar = new MenuBar(menuFile, menuEdit, menuLoadMap, menuView);
    }

    // also called from EditorPage
    public void addLoadMapMenuItem(String description, WorldMap map) {
        assertNotNull(description);
        assertNotNull(map);
        var miLoadMap = new MenuItem(description);
        miLoadMap.setOnAction(e -> loadMap(map));
        menuLoadMap.getItems().add(miLoadMap);
    }

    public void loadMap(WorldMap worldMap) {
        assertNotNull(worldMap);
        if (changeManager.hasUnsavedChanges()) {
            showSaveConfirmationDialog(this::showSaveDialog, () -> {
                setWorldMap(new WorldMap(worldMap));
                currentFilePy.set(null);
            });
        } else {
            setWorldMap(new WorldMap(worldMap));
            currentFilePy.set(null);
        }
    }

    private void showNewMapDialog(boolean preconfigured) {
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
        fileChooser.setInitialDirectory(currentDirectory);
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            readMapFile(file);
        }
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
                worldMap().save(file);
                changeManager.clearUnsavedChanges();
                readMapFile(file);
            } else {
                Logger.error("No .world file selected");
                showMessage("No .world file selected", 2, MessageType.WARNING);
            }
        }
    }

    public void showSaveConfirmationDialog(Runnable saveAction, Runnable noSaveAction) {
        if (changeManager.hasUnsavedChanges()) {
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
                    changeManager.clearUnsavedChanges();
                } else if (choice == choiceCancel) {
                    confirmationDialog.close(); // TODO check this
                }
            });
        } else {
            stop();
            noSaveAction.run();
        }
    }

    private void updateSourceView() {
        if (sourceView == null) {
            Logger.warn("Cannot update source view as it doesn't exist yet");
            return;
        }
        try {
            StringBuilder sb = new StringBuilder();
            String[] lines = worldMap().sourceCode().split("\n");
            for (int i = 0; i < lines.length; ++i) {
                sb.append("%5d: ".formatted(i + 1)).append(lines[i]).append("\n");
            }
            sourceView.setText(sb.toString());
        } catch (Exception x) {
            Logger.error(x);
        }
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

    //
    // Drawing
    //

    private void drawPreview2D(TerrainColorScheme colors) {
        GraphicsContext g = canvasPreview2D.getGraphicsContext2D();
        g.setImageSmoothing(false);
        g.setFill(colors.backgroundColor());
        g.fillRect(0, 0, canvasPreview2D.getWidth(), canvasPreview2D.getHeight());
        if (terrainVisiblePy.get()) {
            terrainRendererInPreview.setScaling(gridSize() / 8.0);
            terrainRendererInPreview.setColors(colors);
            terrainRendererInPreview.drawTerrain(g, worldMap().terrain(), worldMap().obstacles());
            Vector2i houseMinTile = worldMap().terrain().getTileProperty(PROPERTY_POS_HOUSE_MIN_TILE, null);
            Vector2i houseMaxTile = worldMap().terrain().getTileProperty(PROPERTY_POS_HOUSE_MAX_TILE, null);
            if (houseMinTile != null && houseMaxTile != null) {
                terrainRendererInPreview.drawHouse(g, houseMinTile, houseMaxTile.minus(houseMinTile).plus(1, 1));
            }
        }
        if (foodVisiblePy.get()) {
            Color foodColor = getColorFromMap(worldMap().food(), PROPERTY_COLOR_FOOD, parseColor(MS_PACMAN_COLOR_FOOD));
            foodRenderer.setScaling(gridSize() / 8.0);
            foodRenderer.setEnergizerColor(foodColor);
            foodRenderer.setPelletColor(foodColor);
            foodRenderer.drawFood(g, worldMap().food());
        }
        editCanvas.drawActorSprites(g);
    }

    private void drawSelectedPalette(TerrainColorScheme colors) {
        Palette selectedPalette = palettes[selectedPaletteID()];
        if (selectedPaletteID() == PALETTE_ID_TERRAIN) {
            double scaling = terrainRendererInEditor.scaling();
            terrainRendererInEditor.setScaling((double) TOOL_SIZE / 8);
            terrainRendererInEditor.setColors(colors);
            terrainRendererInEditor.setScaling(scaling);
        }
        selectedPalette.draw();
    }

    void clearMessage() {
        showMessage("", 0, MessageType.INFO);
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
            if (gridSize() < TileMapEditor.MAX_GRID_SIZE) {
                gridSizePy.set(gridSize() + 1);
            }
        }
        else if (key == KeyCode.MINUS) {
            if (gridSize() > TileMapEditor.MIN_GRID_SIZE) {
                gridSizePy.set(gridSize() - 1);
            }
        }
    }

    private void onKeyTyped(KeyEvent e) {
        String key = e.getCharacter();
        switch (key) {
            case "i" -> setEditMode(EditMode.INSPECT);
            case "e" -> {
                setEditMode(EditMode.EDIT);
                symmetricEditModePy.set(false);
            }
            case "s" -> {
                setEditMode(EditMode.EDIT);
                symmetricEditModePy.set(true);
            }
            case "x" -> setEditMode(EditMode.ERASE);
        }
    }

    public EditMode editMode() { return editModePy.get(); }

    public boolean isEditMode(EditMode mode) { return editMode() == mode; }

    public void setEditMode(EditMode mode) {
        editModePy.set(assertNotNull(mode));
    }

    void editAtMousePosition(MouseEvent event) {
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
                    selectedPalette().selectedTool().apply(worldMap().terrain(), tile);
                    changeManager.markTerrainChanged();
                    terrainPropertiesEditor().updatePropertyEditorValues(); //TODO check
                }
            }
            default -> Logger.error("Unknown palette ID " + selectedPaletteID());
        }
    }

    private void editTerrainAtTile(Vector2i tile, boolean erase) {
        if (erase) {
            clearTerrainTileValue(tile);
        } else if (selectedPalette().isToolSelected()) {
            selectedPalette().selectedTool().apply(worldMap().terrain(), tile);
        }
    }

    private void editFoodAtTile(Vector2i tile, boolean erase) {
        if (erase) {
            clearFoodTileValue(tile);
        } else if (selectedPalette().isToolSelected()) {
            selectedPalette().selectedTool().apply(worldMap().food(), tile);
        }
    }

    public void moveCursorAndSetFoodAtTile(Direction dir) {
        if (editCanvas.moveCursor(dir, this::canEditFoodAtTile)) {
            setFoodAtFocussedTile();
        }
    }

    private void setFoodAtFocussedTile() {
        if (editMode() == EditMode.EDIT && selectedPaletteID() == PALETTE_ID_FOOD) {
            if (canEditFoodAtTile(editCanvas.focussedTile())) {
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

    private boolean canEditFoodAtTile(Vector2i tile) {
        byte content = worldMap().terrain().get(tile);
        return content == TerrainTiles.EMPTY
            || content == TerrainTiles.ONE_WAY_DOWN
            || content == TerrainTiles.ONE_WAY_UP
            || content == TerrainTiles.ONE_WAY_LEFT
            || content == TerrainTiles.ONE_WAY_RIGHT;
    }

    private void identifyObstacleAtTile(Vector2i tile) {
        Obstacle obstacleAtTile = worldMap().obstacles().stream()
            .filter(obstacle -> Globals.tileAt(obstacle.startPoint().minus(HTS, 0).toVector2f()).equals(tile))
            .findFirst().orElse(null);
        if (obstacleAtTile != null) {
            String encoding = obstacleAtTile.encoding();
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(encoding);
            clipboard.setContent(content);
            showMessage("Obstacle identified (copied to clipboard)", 5, MessageType.INFO);
        } else {
            showMessage("", 1, MessageType.INFO);
        }
    }

    /**
     * This method should be used whenever a tile value has to be set.
     */
    public void setTileValue(TileMap tileMap, Vector2i tile, byte value) {
        assertNotNull(tileMap);
        assertNotNull(tile);
        tileMap.set(tile, value);
        if (tileMap == worldMap().terrain()) {
            changeManager.markTerrainChanged();
            worldMap().food().set(tile, FoodTiles.EMPTY);
            changeManager.markFoodChanged();
        } else {
            changeManager.markFoodChanged();
        }
        if (isSymmetricEditMode()) {
            byte mirroredContent = mirroredTileContent(tileMap.get(tile));
            Vector2i mirrorTile = mirrored(tileMap, tile);
            tileMap.set(mirrorTile, mirroredContent);
            if (tileMap == worldMap().terrain()) {
                changeManager.markTerrainChanged();
                worldMap().food().set(mirrorTile, FoodTiles.EMPTY);
                changeManager.markFoodChanged();
            } else {
                changeManager.markFoodChanged();
            }
        }
    }

    private Vector2i mirrored(TileMap tileMap, Vector2i tile) {
        return vec_2i(tileMap.numCols() - 1 - tile.x(), tile.y());
    }

    // ignores symmetric edit mode!
    public void clearTerrainTileValue(Vector2i tile) {
        worldMap().terrain().set(tile, TerrainTiles.EMPTY);
        changeManager.markTerrainChanged();
    }

    // ignores symmetric edit mode!
    public void clearFoodTileValue(Vector2i tile) {
        worldMap().food().set(tile, FoodTiles.EMPTY);
        changeManager.markFoodChanged();
    }

    private void setBlankMap(int tilesX, int tilesY) {
        var blankMap = new WorldMap(tilesY, tilesX);
        TileMap terrain = blankMap.terrain();
        terrain.setProperty(PROPERTY_COLOR_WALL_STROKE, MS_PACMAN_COLOR_WALL_STROKE);
        terrain.setProperty(PROPERTY_COLOR_WALL_FILL, MS_PACMAN_COLOR_WALL_FILL);
        terrain.setProperty(PROPERTY_COLOR_DOOR, MS_PACMAN_COLOR_DOOR);
        if (tilesX >= 3 && tilesY >= 2) {
            terrain.setProperty(PROPERTY_POS_SCATTER_RED_GHOST, formatTile(vec_2i(tilesX - 3, 0)));
            terrain.setProperty(PROPERTY_POS_SCATTER_PINK_GHOST, formatTile(vec_2i(2, 0)));
            terrain.setProperty(PROPERTY_POS_SCATTER_CYAN_GHOST, formatTile(vec_2i(tilesX - 1, tilesY - 2)));
            terrain.setProperty(PROPERTY_POS_SCATTER_ORANGE_GHOST, formatTile(vec_2i(0, tilesY - 2)));
        }
        blankMap.food().setProperty(PROPERTY_COLOR_FOOD, MS_PACMAN_COLOR_FOOD);
        setWorldMap(blankMap);
    }

    private void setPreconfiguredMap(int tilesX, int tilesY) {
        var preConfiguredMap = new WorldMap(tilesY, tilesX);
        TileMap terrain = preConfiguredMap.terrain();
        terrain.setProperty(PROPERTY_COLOR_WALL_STROKE, MS_PACMAN_COLOR_WALL_STROKE);
        terrain.setProperty(PROPERTY_COLOR_WALL_FILL, MS_PACMAN_COLOR_WALL_FILL);
        terrain.setProperty(PROPERTY_COLOR_DOOR, MS_PACMAN_COLOR_DOOR);
        addBorderWall(terrain, 3, 2);
        if (terrain.numRows() >= 20) {
            Vector2i houseOrigin = vec_2i(tilesX / 2 - 4, tilesY / 2 - 3);
            placeHouse(preConfiguredMap, houseOrigin);
            terrain.setProperty(PROPERTY_POS_PAC,                  formatTile(houseOrigin.plus(3, 11)));
            terrain.setProperty(PROPERTY_POS_BONUS,                formatTile(houseOrigin.plus(3, 5)));
            terrain.setProperty(PROPERTY_POS_SCATTER_RED_GHOST,    formatTile(vec_2i(tilesX - 3, 0)));
            terrain.setProperty(PROPERTY_POS_SCATTER_PINK_GHOST,   formatTile(vec_2i(2, 0)));
            terrain.setProperty(PROPERTY_POS_SCATTER_CYAN_GHOST,   formatTile(vec_2i(tilesX - 1, tilesY - 2)));
            terrain.setProperty(PROPERTY_POS_SCATTER_ORANGE_GHOST, formatTile(vec_2i(0, tilesY - 2)));
        }
        preConfiguredMap.updateObstacleList();
        preConfiguredMap.food().setProperty(PROPERTY_COLOR_FOOD, MS_PACMAN_COLOR_FOOD);
        setWorldMap(preConfiguredMap);
    }

    private void addBorderWall(TileMap terrain, int emptyRowsBeforeMaze, int emptyRowsAfterMaze) {
        int lastRow = terrain.numRows() - 1 - emptyRowsAfterMaze, lastCol = terrain.numCols() - 1;
        terrain.set(emptyRowsBeforeMaze, 0, TerrainTiles.CORNER_NW);
        terrain.set(emptyRowsBeforeMaze, lastCol, TerrainTiles.CORNER_NE);
        terrain.set(lastRow, 0, TerrainTiles.CORNER_SW);
        terrain.set(lastRow, lastCol, TerrainTiles.CORNER_SE);
        for (int row = emptyRowsBeforeMaze + 1; row < lastRow; ++row) {
            terrain.set(row, 0, TerrainTiles.WALL_V);
            terrain.set(row, lastCol, TerrainTiles.WALL_V);
        }
        for (int col = 1; col < lastCol; ++col) {
            terrain.set(emptyRowsBeforeMaze, col, TerrainTiles.WALL_H);
            terrain.set(lastRow, col, TerrainTiles.WALL_H);
        }
        changeManager.markTerrainChanged();
    }

    public void placeHouse(WorldMap worldMap, Vector2i origin) {
        TileMap terrain = worldMap.terrain();
        terrain.setProperty(PROPERTY_POS_HOUSE_MIN_TILE, formatTile(origin));
        terrain.setProperty(PROPERTY_POS_HOUSE_MAX_TILE, formatTile(origin.plus(7, 4)));
        terrain.setProperty(PROPERTY_POS_RED_GHOST,      formatTile(origin.plus(3, -1)));
        terrain.setProperty(PROPERTY_POS_CYAN_GHOST,     formatTile(origin.plus(1, 2)));
        terrain.setProperty(PROPERTY_POS_PINK_GHOST,     formatTile(origin.plus(3, 2)));
        terrain.setProperty(PROPERTY_POS_ORANGE_GHOST,   formatTile(origin.plus(5, 2)));
        Vector2i houseMinTile = terrain.getTileProperty(PROPERTY_POS_HOUSE_MIN_TILE, null);
        Vector2i houseMaxTile = terrain.getTileProperty(PROPERTY_POS_HOUSE_MAX_TILE, null);
        for (int row = houseMinTile.y(); row <= houseMaxTile.y(); ++row) {
            for (int col = houseMinTile.x(); col <= houseMaxTile.x(); ++col) {
                terrain.set(row, col, TerrainTiles.EMPTY);
            }
        }
        changeManager.markChanged();
    }

    private void openTemplateImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Open Template Maze Image"); // TODO localize
        fc.setInitialDirectory(currentDirectory);
        fc.getExtensionFilters().addAll(FILTER_IMAGE, FILTER_ALL);
        fc.setSelectedExtensionFilter(FILTER_IMAGE);
        File file = fc.showOpenDialog(stage);
        if (file != null) {
            try (FileInputStream stream = new FileInputStream(file)) {
                templateImagePy.set(new Image(stream));
            } catch (IOException x) {
                Logger.error(x);
            }
        }
    }

    private void closeTemplateImage() {
        templateImagePy.set(null);
    }

    public void floodWithPellets(Vector2i startTile) {
        TileMap terrainMap = worldMap().terrain(), foodMap = worldMap().food();
        var q = new ArrayDeque<Vector2i>();
        q.push(startTile);
        while (!q.isEmpty()) {
            Vector2i tile = q.poll();
            if (foodMap.get(tile) == FoodTiles.EMPTY) {
                foodMap.set(tile, FoodTiles.PELLET);
                for (Direction dir : Direction.values()) {
                    Vector2i nb = tile.plus(dir.vector());
                    if  (!terrainMap.outOfBounds(nb)
                            && terrainMap.get(nb) == TerrainTiles.EMPTY
                            && foodMap.get(nb) == FoodTiles.EMPTY) {
                        q.push(nb);
                    }
                }
            }
        }
    }

    private void identifyTilesFromTemplateImage() {
        Image templateImage = templateImagePy.get();
        if (templateImage == null) {
            return;
        }
        Color fillColor   = getColorFromMap(worldMap().terrain(), PROPERTY_COLOR_WALL_FILL, null);
        Color strokeColor = getColorFromMap(worldMap().terrain(), PROPERTY_COLOR_WALL_STROKE, null);
        Color doorColor   = getColorFromMap(worldMap().terrain(), PROPERTY_COLOR_DOOR, Color.PINK);
        Color foodColor   = getColorFromMap(worldMap().food(), PROPERTY_COLOR_FOOD, null);

        if (fillColor == null) {
            showMessage("No fill color defined", 3, MessageType.ERROR);
            return;
        }
        if (strokeColor == null) {
            showMessage("No stroke color defined", 3, MessageType.ERROR);
            return;
        }
        if (foodColor == null) {
            showMessage("No food color defined", 3, MessageType.ERROR);
            return;
        }

        TileMatcher.PixelScheme pixelScheme = new TileMatcher.PixelScheme(Color.TRANSPARENT, fillColor, strokeColor, doorColor, foodColor);
        TileMatcher matcher = new TileMatcher(pixelScheme);

        WritablePixelFormat<IntBuffer> pixelFormat = WritablePixelFormat.getIntArgbInstance();
        PixelReader rdr = templateImage.getPixelReader();
        int[] pixelsOfTile = new int[TS*TS]; // pixels row-wise

        // 3 empty rows on top, 2 on bottom
        int numRows = worldMap().terrain().numRows(), numCols = worldMap().terrain().numCols();
        for (int row = 0; row < numRows - 5; ++row) {
            for (int col = 0; col < numCols; ++col) {
                Vector2i mapTile = vec_2i(col, row + 3);
                try {
                    // read pixel values for current tile
                    rdr.getPixels(col * TS, row * TS, TS, TS, pixelFormat, pixelsOfTile, 0, TS);
                    byte foodValue = matcher.identifyFoodTile(pixelsOfTile);
                    if (foodValue == FoodTiles.PELLET || foodValue == FoodTiles.ENERGIZER) {
                        worldMap().food().set(mapTile, foodValue);
                    }
                    else {
                        byte terrainValue = matcher.identifyTerrainTile(pixelsOfTile);
                        worldMap().terrain().set(mapTile, terrainValue);
                    }
                } catch (IndexOutOfBoundsException e) {
                    Logger.error("Could not get pixels for tile {}, maybe image has been cropped incorrectly?", mapTile);
                } catch (Exception e) {
                    Logger.error("Could not get pixels for tile {}", mapTile);
                    Logger.error(e);
                }
            }
        }
        // Find house: require at least min and max tile have been detected
        Vector2i houseMinTile = worldMap().terrain().tiles()
            .filter(tile -> worldMap().terrain().get(tile) == TerrainTiles.DCORNER_ANGULAR_NW)
            .findFirst().orElse(null);
        Vector2i houseMaxTile = worldMap().terrain().tiles()
            .filter(tile -> worldMap().terrain().get(tile) == TerrainTiles.DCORNER_ANGULAR_SE)
            .findFirst().orElse(null);
        if (houseMinTile != null && houseMaxTile != null) {
            placeHouse(worldMap(), houseMinTile);
        }

        changeManager.markChanged();
    }
}