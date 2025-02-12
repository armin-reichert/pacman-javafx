/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.TileEncoding;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
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
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.*;
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
import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
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
    public static final short MAX_GRID_SIZE = 64;

    public static final byte PALETTE_ID_ACTORS  = 0;
    public static final byte PALETTE_ID_TERRAIN = 1;
    public static final byte PALETTE_ID_FOOD    = 2;

    public static final ResourceBundle TEXT_BUNDLE = ResourceBundle.getBundle(TileMapEditor.class.getPackageName() + ".texts");

    public static String tt(String key, Object... args) {
        return MessageFormat.format(TEXT_BUNDLE.getString(key), args);
    }

    public static final Node NO_GRAPHIC = null;

    public static final Color CANVAS_BACKGROUND = Color.BLACK;

    public static final Font FONT_STATUS_LINE = Font.font("Sans", FontWeight.BOLD, 14);
    public static final Font FONT_MESSAGE     = Font.font("Sans", FontWeight.NORMAL, 14);
    public static final Cursor RUBBER_CURSOR  = Cursor.cursor(urlString("graphics/radiergummi.jpg"));

    public static final FileChooser.ExtensionFilter FILTER_WORLD_MAP = new FileChooser.ExtensionFilter("World Map Files", "*.world");
    public static final FileChooser.ExtensionFilter FILTER_IMAGE = new FileChooser.ExtensionFilter("Image Files", "*.bmp", "*.gif", "*.jpg", "*.png");
    public static final FileChooser.ExtensionFilter FILTER_ALL = new FileChooser.ExtensionFilter("All Files", "*.*");

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
            invalidateTerrainData();
            updateSourceView();
        }
    };

    private final IntegerProperty gridSizePy = new SimpleIntegerProperty(16) {
        @Override
        protected void invalidated() {
            invalidateTerrainData();
        }
    };

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

    private final ObjectProperty<Vector2i> focussedTilePy = new SimpleObjectProperty<>();

    private final ObjectProperty<EditMode> editModePy = new SimpleObjectProperty<>(EditMode.INSPECT) {
        @Override
        protected void invalidated() {
            switch (get()) {
                case INSPECT -> onEnterInspectMode();
                case EDIT -> onEnterEditMode();
                case ERASE -> onEnterEraseMode();
            }
        }
    };

    private final BooleanProperty symmetricEditModePy = new SimpleBooleanProperty(true);

    private final ObjectProperty<Image> templateImagePy = new SimpleObjectProperty<>();

    private final ObjectProperty<Image> templateImageGreyPy = new SimpleObjectProperty<>();

    // Accessor methods

    public WorldMap worldMap() { return worldMapPy.get(); }

    public void setWorldMap(WorldMap worldMap) { worldMapPy.set(assertNotNull(worldMap)); }

    public boolean isSymmetricEditMode() { return symmetricEditModePy.get(); }

    public StringProperty titleProperty() { return titlePy; }

    public Vector2i focussedTile() { return focussedTilePy.get(); }

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

    /**
     * @return pixels used by one tile at current window zoom
     */
    private int gridSize() {
        return gridSizePy.get();
    }

    // Attributes

    private File currentDirectory;
    private Instant messageCloseTime;
    private Timeline clock;
    private boolean unsavedChanges;
    private boolean terrainChanged;
    private boolean foodChanged;
    private boolean terrainDataUpToDate;
    private boolean dragging = false;
    private final List<Vector2i> tilesWithErrors = new ArrayList<>();

    private final BorderPane contentPane = new BorderPane();
    private Stage stage;
    private Pane propertyEditorsPane;
    private Canvas editCanvas;
    private ScrollPane spEditCanvas;
    private ScrollPane spPreview2D;
    private Canvas preview2D;
    private Text sourceView;
    private ScrollPane spSourceView;
    private ScrollPane spTemplateImage;
    private Pane dropTargetForTemplateImage;
    private SplitPane splitPaneEditorAndPreviews;
    private Label messageLabel;
    private Label focussedTileStatusLabel;
    private Label editModeStatusLabel;
    private HBox sliderZoomContainer;
    private FileChooser fileChooser;
    private TabPane tabPaneWithPalettes;
    private HBox statusLine;
    private TabPane tabPaneEditorViews;
    private Tab tabEditCanvas;
    private Tab tabTemplateImage;
    private ImageView templateImageView;
    private ContextMenu templateImageContextMenu;
    private TabPane tabPanePreviews;
    private Tab tabPreview2D;
    private Tab tabPreview3D;
    private Tab tabSourceView;
    private MazePreview3D mazePreview3D;

    private final ContextMenu contextMenu = new ContextMenu();
    private MenuBar menuBar;
    private Menu menuFile;
    private Menu menuEdit;
    private Menu menuLoadMap;
    private Menu menuView;

    private final Palette[] palettes = new Palette[3];
    private PropertyEditorPane terrainMapPropertiesEditor;
    private PropertyEditorPane foodMapPropertiesEditor;
    private ObstacleEditor obstacleEditor;

    private TerrainRendererInEditor editorTerrainRenderer;
    private TerrainRenderer         previewTerrainRenderer;
    private FoodMapRenderer         foodMapRenderer;

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
        createMenuBarAndMenus();
        createEditCanvas();
        createPreview2D();
        createPreview3D();
        createTemplateImageView();
        createMapSourceView();
        createPalettes();
        createObstacleEditor();
        createPropertyEditors();
        createTabPaneWithEditViews();
        createTabPaneWithPreviews();
        createFocussedTileStatusLabel();
        createEditModeStatusLabel();
        createMessageDisplay();
        createZoomSlider();
        createStatusLine();

        arrangeMainLayout();
        initActiveRendering();

        titlePy.bind(createTitleBinding());
        templateImageGreyPy.bind(templateImagePy.map(Ufx::imageToGreyscale));

        // Input handlers
        contentPane.setOnKeyTyped(this::onKeyTyped);
        contentPane.setOnKeyPressed(this::onKeyPressed);

        editCanvas.setOnContextMenuRequested(this::onEditCanvasContextMenuRequested);
        editCanvas.setOnMouseClicked(this::onEditCanvasMouseClicked);
        editCanvas.setOnMouseDragged(this::onEditCanvasMouseDragged);
        editCanvas.setOnMouseMoved(this::onEditCanvasMouseMoved);
        editCanvas.setOnMouseReleased(this::onEditCanvasMouseReleased);
        editCanvas.setOnKeyPressed(this::onEditCanvasKeyPressed);
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

    private void onEnterInspectMode() {
        editCanvas.setCursor(Cursor.HAND); // TODO use other cursor
        obstacleEditor.setEnabled(false);
        clearMessage();
        showEditHelpText();
    }

    private void onEnterEditMode() {
        editCanvas.setCursor(Cursor.DEFAULT);
        obstacleEditor.setEnabled(true);
        clearMessage();
        showEditHelpText();
    }

    private void onEnterEraseMode() {
        editCanvas.setCursor(RUBBER_CURSOR);
        obstacleEditor.setEnabled(false);
        clearMessage();
        showEditHelpText();
    }

    private void createRenderers(TerrainColorScheme terrainColorScheme, Color foodColor) {
        editorTerrainRenderer = new TerrainRendererInEditor();
        editorTerrainRenderer.setColors(terrainColorScheme);
        previewTerrainRenderer = new TerrainRenderer();
        previewTerrainRenderer.setColors(terrainColorScheme);
        foodMapRenderer = new FoodMapRenderer();
        foodMapRenderer.setPelletColor(foodColor);
        foodMapRenderer.setEnergizerColor(foodColor);
    }

    private void createFileChooser() {
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(FILTER_WORLD_MAP, FILTER_ALL);
        fileChooser.setSelectedExtensionFilter(FILTER_WORLD_MAP);
        fileChooser.setInitialDirectory(currentDirectory);
    }

    private void createEditCanvas() {
        editCanvas = new Canvas();
        editCanvas.heightProperty().bind(Bindings.createDoubleBinding(
            () -> (double) worldMap().terrain().numRows() * gridSize(), worldMapPy, gridSizePy));
        editCanvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> (double) worldMap().terrain().numCols() * gridSize(), worldMapPy, gridSizePy));
        registerDragAndDropImageHandler(editCanvas);
        spEditCanvas = new ScrollPane(editCanvas);
        spEditCanvas.setFitToHeight(true);
    }

    private void onEditCanvasContextMenuRequested(ContextMenuEvent menuEvent) {
        if (isEditMode(EditMode.INSPECT)) {
            return;
        }
        if (menuEvent.isKeyboardTrigger()) {
            return; // ignore keyboard-triggered event e.g. by pressing Shift+F10 in Windows
        }
        Vector2i tile = tileAtMousePosition(menuEvent.getX(), menuEvent.getY());
        WorldMap worldMap = worldMapPy.get();

        var miAddCircle2x2 = new MenuItem(tt("menu.edit.add_circle"));
        miAddCircle2x2.setOnAction(actionEvent -> {
            addTerrainContentBlock(worldMap(), CIRCLE_2x2, tile);
            if (isSymmetricEditMode()) {
                addTerrainContentBlockMirrored(worldMap, CIRCLE_2x2, tile);
            }
            markTileMapEdited(worldMap.terrain());
            markTileMapEdited(worldMap.food());
        });

        var miAddHouse = new MenuItem(TileMapEditor.tt("menu.edit.add_house"));
        miAddHouse.setOnAction(actionEvent -> addHouse(worldMap, tile));

        var miInsertRow = new MenuItem(tt("menu.edit.insert_row"));
        miInsertRow.setOnAction(actionEvent -> {
            int rowIndex = tileAtMousePosition(menuEvent.getX(), menuEvent.getY()).y();
            setWorldMap(worldMap.insertRowBeforeIndex(rowIndex));
        });

        var miDeleteRow = new MenuItem(tt("menu.edit.delete_row"));
        miDeleteRow.setOnAction(actionEvent -> {
            int rowIndex = tileAtMousePosition(menuEvent.getX(), menuEvent.getY()).y();
            setWorldMap(worldMap.deleteRowAtIndex(rowIndex));
        });

        var miFloodWithPellets = new MenuItem(tt("menu.edit.flood_with_pellets"));
        miFloodWithPellets.setOnAction(ae -> {
            if (worldMap.terrain().get(tile) == TileEncoding.EMPTY && worldMap.food().get(tile) == TileEncoding.EMPTY) {
                floodWithPellets(tile);
            }
        });

        var miDetectPellets = new MenuItem(tt("menu.edit.detect_pellets"));
        //miDetectPellets.disableProperty().bind(templateImagePy.map(Objects::isNull));
        miDetectPellets.disableProperty().bind(Bindings.createBooleanBinding(() -> templateImagePy.get() == null, templateImagePy));
        miDetectPellets.setOnAction(ae -> detectTilesInTemplateImage());

        contextMenu.getItems().setAll(
            miInsertRow,
            miDeleteRow,
            new SeparatorMenuItem(),
            miAddCircle2x2,
            miAddHouse,
            new SeparatorMenuItem(),
            miDetectPellets,
            miFloodWithPellets);

        contextMenu.show(editCanvas, menuEvent.getScreenX(), menuEvent.getScreenY());
    }

    private void createPreview2D() {
        preview2D = new Canvas();
        preview2D.widthProperty().bind(editCanvas.widthProperty());
        preview2D.heightProperty().bind(editCanvas.heightProperty());
        spPreview2D = new ScrollPane(preview2D);
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
        templateImageView = new ImageView();
        templateImageView.setSmooth(false);
        templateImageView.setPreserveRatio(true);
        templateImageView.imageProperty().bind(templateImagePy);
        templateImageView.fitHeightProperty().bind(Bindings.createDoubleBinding(
            () -> {
                if (templateImagePy.get() == null) return 0.0;
                return preview2D.heightProperty().get();
            }, preview2D.heightProperty(), templateImagePy));

        templateImageView.setOnContextMenuRequested(e -> {
            if (templateImageContextMenu != null) {
                templateImageContextMenu.hide();
            }
            templateImageContextMenu = new ContextMenu();

            Color color = pickColor(templateImageView, e.getX(), e.getY());

            var colorPreview = new HBox();
            colorPreview.setMinWidth(32);
            colorPreview.setMinHeight(16);
            colorPreview.setBackground(Background.fill(color));

            var colorValue = new Text(formatColorHex(color));
            colorValue.setFont(Font.font("Monospace", FontWeight.BOLD, 14.0));
            colorValue.setFill(Color.BLACK);
            colorValue.setDisable(true);

            var colorPreviewBox = new HBox(colorPreview, colorValue);
            colorPreviewBox.setSpacing(3);

            var miColorPreview = new CustomMenuItem(colorPreviewBox);
            //TODO how to let item appear unselectable?

            var miPickFillColor = new MenuItem(tt("menu.pick_color.set_fill_color"));
            miPickFillColor.setOnAction(ae -> setTerrainMapProperty(PROPERTY_COLOR_WALL_FILL, formatColor(color)));

            var miPickStrokeColor = new MenuItem(tt("menu.pick_color.set_stroke_color"));
            miPickStrokeColor.setOnAction(ae -> setTerrainMapProperty(PROPERTY_COLOR_WALL_STROKE, formatColor(color)));

            var miPickDoorColor = new MenuItem(tt("menu.pick_color.set_door_color"));
            miPickDoorColor.setOnAction(ae -> setTerrainMapProperty(PROPERTY_COLOR_DOOR, formatColor(color)));

            var miPickFoodColor = new MenuItem(tt("menu.pick_color.set_food_color"));
            miPickFoodColor.setOnAction(ae -> setFoodMapProperty(PROPERTY_COLOR_FOOD, formatColor(color)));

            templateImageContextMenu.getItems().addAll(miColorPreview, miPickFillColor, miPickStrokeColor, miPickDoorColor, miPickFoodColor);
            templateImageContextMenu.show(templateImageView, e.getScreenX(), e.getScreenY());
        });

        templateImageView.setOnMouseClicked(e -> {
            if (templateImageContextMenu != null) {
                templateImageContextMenu.hide();
                templateImageContextMenu = null;
            }
        });

        StackPane pane = new StackPane(templateImageView);
        pane.setBackground(Background.fill(Color.BLACK));

        spTemplateImage = new ScrollPane(pane);
    }

    private void setTerrainMapProperty(String name, String value) {
        worldMap().terrain().setProperty(name, value);
        //TODO find better solution
        terrainPropertiesEditor().rebuildPropertyEditors();
        mazePreview3D.updateTerrain();
    }

    private void setFoodMapProperty(String name, String value) {
        worldMap().food().setProperty(name, value);
        //TODO find better solution
        foodPropertiesEditor().rebuildPropertyEditors();
        mazePreview3D.updateFood();
    }

    private Color pickColor(ImageView imageView, double x, double y) {
        Image image = imageView.getImage();
        double pickX = x * (image.getWidth() / imageView.getBoundsInLocal().getWidth());
        double pickY = y * (image.getHeight() / imageView.getBoundsInLocal().getHeight());
        return image.getPixelReader().getColor((int) pickX, (int) pickY);
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

    private void createTabPaneWithEditViews() {
        tabEditCanvas = new Tab(tt("tab_editor"), spEditCanvas);

        var hint = new Label(tt("image_drop_hint"));
        hint.setFont(Font.font(16));

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

        node.setOnDragDropped(e -> {
            if (e.getDragboard().hasFiles()) {
                File file = e.getDragboard().getFiles().getFirst();
                if (isSupportedImageFile(file)) {
                    e.acceptTransferModes(TransferMode.COPY);
                    try (FileInputStream in = new FileInputStream(file)) {
                        templateImagePy.set(new Image(in));
                    } catch (IOException x) {
                        Logger.error(x);
                    }
                }
            }
            e.consume();
        });
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
        palettes[PALETTE_ID_ACTORS]  = createActorPalette(PALETTE_ID_ACTORS, TOOL_SIZE, this, editorTerrainRenderer);
        palettes[PALETTE_ID_TERRAIN] = createTerrainPalette(PALETTE_ID_TERRAIN, TOOL_SIZE, this, editorTerrainRenderer);
        palettes[PALETTE_ID_FOOD]    = createFoodPalette(PALETTE_ID_FOOD, TOOL_SIZE, this, foodMapRenderer);

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

    private void createObstacleEditor() {
        obstacleEditor = new ObstacleEditor();
        obstacleEditor.setEditCallback((tile, value) -> {
            setTileValue(worldMap().terrain(), tile, value);
            setTileValue(worldMap().food(), tile, TileEncoding.EMPTY);
        });
        obstacleEditor.worldMapPy.bind(worldMapPy);
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

    private void createFocussedTileStatusLabel() {
        focussedTileStatusLabel = new Label();
        focussedTileStatusLabel.setFont(FONT_STATUS_LINE);
        focussedTileStatusLabel.setMinWidth(70);
        focussedTileStatusLabel.setMaxWidth(70);
        focussedTileStatusLabel.textProperty().bind(focussedTilePy.map(
            tile -> tile != null ? "(%2d,%2d)".formatted(tile.x(), tile.y()) : "n/a"));
    }

    private void createEditModeStatusLabel() {
        editModeStatusLabel = new Label();
        editModeStatusLabel.setFont(FONT_STATUS_LINE);
        editModeStatusLabel.setTextFill(Color.LIGHTSEAGREEN);
        editModeStatusLabel.textProperty().bind(Bindings.createStringBinding(
            () -> switch (editModePy.get()) {
                    case INSPECT -> "INSPECT";
                    case EDIT -> isSymmetricEditMode() ?  "SYMMETRIC" : "NORMAL";
                    case ERASE -> "ERASE";
            },
            editModePy, symmetricEditModePy
        ));
    }

    private void createMessageDisplay() {
        messageLabel = new Label();
        messageLabel.setFont(FONT_MESSAGE);
        messageLabel.setMinWidth(200);
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

    private void createStatusLine() {
        var mapSizeInfo = new Text();
        mapSizeInfo.setFont(FONT_STATUS_LINE);
        mapSizeInfo.textProperty().bind(worldMapPy.map(worldMap -> (worldMap != null)
            ? "%2d cols %2d rows".formatted(worldMap.terrain().numCols(), worldMap.terrain().numRows()) : "")
        );

        var spacer = new Region();
        statusLine = new HBox(mapSizeInfo, filler(10), focussedTileStatusLabel, editModeStatusLabel, filler(20), messageLabel, spacer, sliderZoomContainer);
        statusLine.setPadding(new Insets(10, 10, 10, 10));
        HBox.setHgrow(spacer, Priority.ALWAYS);
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
            handleMapChanges();
            updateMessageAnimation();
            TileMap terrainMap = worldMap().terrain();
            TerrainColorScheme colors = new TerrainColorScheme(
                CANVAS_BACKGROUND,
                getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_FILL, parseColor(MS_PACMAN_COLOR_WALL_FILL)),
                getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_STROKE, parseColor(MS_PACMAN_COLOR_WALL_STROKE)),
                getColorFromMap(terrainMap, PROPERTY_COLOR_DOOR, parseColor(MS_PACMAN_COLOR_DOOR))
            );
            try {
                drawSelectedPalette(colors);
            } catch (Exception x) {
                Logger.error(x);
            }
            try {
                drawEditCanvas(colors);
            } catch (Exception x) {
                Logger.error(x);
            }
            try {
                drawPreviewCanvas(colors);
            } catch (Exception x) {
                Logger.error(x);
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
        miAddBorder.setOnAction(e -> addBorderWall(worldMap().terrain(), 3, 2));

        var miClearTerrain = new MenuItem(tt("menu.edit.clear_terrain"));
        miClearTerrain.setOnAction(e -> {
            worldMap().terrain().clear();
            markTileMapEdited(worldMap().terrain());
        });

        var miClearFood = new MenuItem(tt("menu.edit.clear_food"));
        miClearFood.setOnAction(e -> {
            worldMap().food().clear();
            markTileMapEdited(worldMap().food());
        });

        var miDetectPellets = new MenuItem(tt("menu.edit.detect_pellets"));
        //TODO why doens't this work?
        //miDetectPellets.disableProperty().bind(templateImagePy.map(image -> image == null));
        miDetectPellets.disableProperty().bind(Bindings.createBooleanBinding(() -> templateImagePy.get() == null,  templateImagePy));
        miDetectPellets.setOnAction(ae -> detectTilesInTemplateImage());

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
        if (unsavedChanges) {
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
                unsavedChanges = false;
                readMapFile(file);
            } else {
                Logger.error("No .world file selected");
                showMessage("No .world file selected", 2, MessageType.WARNING);
            }
        }
    }

    public void showSaveConfirmationDialog(Runnable saveAction, Runnable noSaveAction) {
        if (unsavedChanges) {
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
                    unsavedChanges = false;
                } else if (choice == choiceCancel) {
                    confirmationDialog.close();
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

    //
    // Drawing
    //

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

    private void drawEditCanvas(TerrainColorScheme colors) {
        final WorldMap map = worldMap();
        final TileMap terrain = map.terrain(), food = map.food();

        double scaling = gridSize() / (double) TS;

        GraphicsContext g = editCanvas.getGraphicsContext2D();
        g.setImageSmoothing(false);

        // Background
        g.setFill(colors.backgroundColor());
        g.fillRect(0, 0, editCanvas.getWidth(), editCanvas.getHeight());

        if (templateImageGreyPy.getName() != null) {
            int emptyRowsTop = 3, emptyRowsBottom = 2; // TODO
            g.drawImage(templateImageGreyPy.get(),
                    0, emptyRowsTop * scaling * TS,
                    editCanvas.getWidth(), editCanvas.getHeight() - (emptyRowsTop + emptyRowsBottom) * scaling * TS);
        }

        drawGrid(g);

        // Terrain
        if (terrainVisiblePy.get()) {
            editorTerrainRenderer.setScaling(scaling);
            editorTerrainRenderer.setColors(colors);
            editorTerrainRenderer.setSegmentNumbersDisplayed(segmentNumbersDisplayedPy.get());
            editorTerrainRenderer.setObstacleInnerAreaDisplayed(obstacleInnerAreaDisplayedPy.get());
            editorTerrainRenderer.drawTerrain(g, terrain, map.obstacles());

            byte[][] editedObstacleContent = obstacleEditor.editedContent();
            if (editedObstacleContent != null) {
                for (int row = 0; row < editedObstacleContent.length; ++row) {
                    for (int col = 0; col < editedObstacleContent[0].length; ++col) {
                        Vector2i tile = obstacleEditor.minTile().plus(col, row);
                        editorTerrainRenderer.drawTile(g, tile, editedObstacleContent[row][col]);
                    }
                }
            }
        }

        // Tiles that seem to be wrong
        double gs = gridSize();
        for (Vector2i tile : tilesWithErrors) {
            g.setFont(Font.font("sans", gs - 2));
            g.setFill(Color.grayRgb(200, 0.8));
            g.fillText("?", tile.x() * gs + 0.25 * gs, tile.y() * gs + 0.8 * gs);
            if (isSymmetricEditMode()) {
                int x = terrain.numCols() - tile.x() - 1;
                g.fillText("?", x * gs + 0.25 * gs, tile.y() * gs + 0.8 * gs);
            }
        }

        // Vertical separator to indicate symmetric edit mode
        if (isEditMode(EditMode.EDIT) && isSymmetricEditMode()) {
            g.save();
            g.setStroke(Color.YELLOW);
            g.setLineWidth(0.75);
            g.setLineDashes(5, 5);
            g.strokeLine(editCanvas.getWidth() / 2, 0, editCanvas.getWidth() / 2, editCanvas.getHeight());
            g.restore();
        }

        // Food
        if (foodVisiblePy.get()) {
            Color foodColor = getColorFromMap(food, PROPERTY_COLOR_FOOD, parseColor(MS_PACMAN_COLOR_FOOD));
            foodMapRenderer.setScaling(scaling);
            foodMapRenderer.setEnergizerColor(foodColor);
            foodMapRenderer.setPelletColor(foodColor);
            foodMapRenderer.drawFood(g, food);
        }

        drawActorSprites(g);

        if (isEditMode(EditMode.INSPECT)) {
            drawEditingHint(g);
        }

        if (focussedTile() != null) {
            g.setStroke(Color.YELLOW);
            g.setLineWidth(1);
            g.strokeRect(focussedTile().x() * gs, focussedTile().y() * gs, gs, gs);
        }
    }

    private void drawActorSprites(GraphicsContext g) {
        drawSprite(g, PROPERTY_POS_PAC, PAC_SPRITE);
        drawSprite(g, PROPERTY_POS_RED_GHOST, RED_GHOST_SPRITE);
        drawSprite(g, PROPERTY_POS_PINK_GHOST, PINK_GHOST_SPRITE);
        drawSprite(g, PROPERTY_POS_CYAN_GHOST, CYAN_GHOST_SPRITE);
        drawSprite(g, PROPERTY_POS_ORANGE_GHOST, ORANGE_GHOST_SPRITE);
        drawSprite(g, PROPERTY_POS_BONUS, BONUS_SPRITE);
    }

    private void drawPreviewCanvas(TerrainColorScheme colors) {
        GraphicsContext g = preview2D.getGraphicsContext2D();
        g.setImageSmoothing(false);
        g.setFill(colors.backgroundColor());
        g.fillRect(0, 0, preview2D.getWidth(), preview2D.getHeight());
        if (terrainVisiblePy.get()) {
            TileMap terrainMap = worldMap().terrain();
            ensureTerrainDataUpdated();
            previewTerrainRenderer.setScaling(gridSize() / 8.0);
            previewTerrainRenderer.setColors(colors);
            previewTerrainRenderer.drawTerrain(g, terrainMap, worldMap().obstacles());
        }
        if (foodVisiblePy.get()) {
            foodMapRenderer.setScaling(gridSize() / 8.0);
            Color foodColor = getColorFromMap(worldMap().food(), PROPERTY_COLOR_FOOD, parseColor(MS_PACMAN_COLOR_FOOD));
            foodMapRenderer.setEnergizerColor(foodColor);
            foodMapRenderer.setPelletColor(foodColor);
            foodMapRenderer.drawFood(g, worldMap().food());
        }
        drawActorSprites(g);
    }

    private void ensureTerrainDataUpdated() {
        if (!terrainDataUpToDate) {
            tilesWithErrors.clear();
            tilesWithErrors.addAll(worldMap().updateObstacleList());
            mazePreview3D.updateTerrain();
            mazePreview3D.updateFood();
            terrainDataUpToDate = true;
        }
    }

    private void drawSprite(GraphicsContext g, String tilePropertyName, RectArea sprite) {
        Vector2i tile = worldMap().terrain().getTileProperty(tilePropertyName, null);
        if (tile != null) {
            drawSprite(g, sprite, tile.x() * gridSize() + 0.5 * gridSize(), tile.y() * gridSize(), 1.75 * gridSize(), 1.75 * gridSize());
        }
    }

    private void drawSprite(GraphicsContext g, RectArea sprite, double x, double y, double w, double h) {
        double ox = 0.5 * (w - gridSize());
        double oy = 0.5 * (h - gridSize());
        g.drawImage(SPRITE_SHEET, sprite.x(), sprite.y(), sprite.width(), sprite.height(), x - ox, y - oy, w, h);
    }

    private void drawSelectedPalette(TerrainColorScheme colors) {
        Palette selectedPalette = palettes[selectedPaletteID()];
        if (selectedPaletteID() == PALETTE_ID_TERRAIN) {
            double scaling = editorTerrainRenderer.scaling();
            editorTerrainRenderer.setScaling((double) TOOL_SIZE / 8);
            editorTerrainRenderer.setColors(colors);
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

    void clearMessage() {
        showMessage("", 0, MessageType.INFO);
    }

    // Controller part

    private void onEditCanvasMouseClicked(MouseEvent e) {
        Logger.debug("Mouse clicked {}", e);
        if (e.getButton() == MouseButton.PRIMARY) {
            editCanvas.requestFocus();
            contextMenu.hide();
            if (e.getClickCount() == 2 && isEditMode(EditMode.INSPECT)) {
                setEditMode(EditMode.EDIT);
            }
        }
    }

    private void onEditCanvasMouseDragged(MouseEvent e) {
        Logger.debug("Mouse dragged {}", e);
        if (!dragging) {
            Vector2i dragStartTile = tileAtMousePosition(e.getX(), e.getY());
            obstacleEditor.startEditing(dragStartTile);
            dragging = true;
            Logger.debug("Dragging started at tile {}", dragStartTile);
        } else {
            obstacleEditor.continueEditing(tileAtMousePosition(e.getX(), e.getY()));
        }
    }

    private void onEditCanvasMouseReleased(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            if (dragging) {
                dragging = false;
                obstacleEditor.endEditing(tileAtMousePosition(e.getX(), e.getY()));
            } else {
                editAtMousePosition(e);
            }
        }
    }

    private void onEditCanvasMouseMoved(MouseEvent e) {
        Vector2i tile = tileAtMousePosition(e.getX(), e.getY());
        focussedTilePy.set(tile);
        switch (editMode()) {
            case EditMode.EDIT -> {
                if (e.isShiftDown()) {
                    switch (selectedPaletteID()) {
                        case TileMapEditor.PALETTE_ID_TERRAIN -> {
                            if (selectedPalette().isToolSelected()) {
                                selectedPalette().selectedTool().apply(worldMap().terrain(), focussedTile());
                            }
                            markTileMapEdited(worldMap().terrain());
                        }
                        case TileMapEditor.PALETTE_ID_FOOD -> {
                            if (selectedPalette().isToolSelected()) {
                                selectedPalette().selectedTool().apply(worldMap().food(), focussedTile());
                            }
                            markTileMapEdited(worldMap().food());
                        }
                        default -> {}
                    }
                }
            }
            case EditMode.ERASE -> {
                if (e.isShiftDown()) {
                    switch (selectedPaletteID()) {
                        case TileMapEditor.PALETTE_ID_TERRAIN -> clearTileValue(worldMap().terrain(), tile);
                        case TileMapEditor.PALETTE_ID_FOOD -> clearTileValue(worldMap().food(), tile);
                    }
                }
            }
            case EditMode.INSPECT -> {}
        }
    }

    private void onKeyPressed(KeyEvent e) {
        KeyCode key = e.getCode();
        boolean alt = e.isAltDown(), control = e.isControlDown();

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

    private boolean isPreview3DSelected() {
        return tabPanePreviews.getSelectionModel().getSelectedItem() == tabPreview3D;
    }

    private void onEditCanvasKeyPressed(KeyEvent e) {
        KeyCode key = e.getCode();
        boolean control = e.isControlDown();

        if (control && key == KeyCode.LEFT) {
            moveCursorAndSetFoodAtTile(Direction.LEFT);
            e.consume();
        }
        else if (control && key == KeyCode.RIGHT) {
            moveCursorAndSetFoodAtTile(Direction.RIGHT);
            e.consume();
        }
        else if (control && key == KeyCode.UP) {
            moveCursorAndSetFoodAtTile(Direction.UP);
            e.consume();
        }
        else if (control && key == KeyCode.DOWN) {
            moveCursorAndSetFoodAtTile(Direction.DOWN);
            e.consume();
        }
        else if (key == KeyCode.LEFT) {
            moveCursor(Direction.LEFT, tile -> true);
        }
        else if (key == KeyCode.RIGHT) {
            moveCursor(Direction.RIGHT, tile -> true);
        }
        else if (key == KeyCode.UP) {
            moveCursor(Direction.UP, tile -> true);
        }
        else if (key == KeyCode.DOWN) {
            moveCursor(Direction.DOWN, tile -> true);
        }
        else if (control && key == KeyCode.SPACE) {
            selectNextPaletteEntry();
        }
    }

    private void onKeyTyped(KeyEvent e) {
        String key = e.getCharacter();
        switch (key) {
            case "i" -> setEditMode(EditMode.INSPECT);
            case "n" -> {
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

    /**
     * @param pixels number of pixels
     * @return number of full tiles spanned by pixels
     */
    private int fullTiles(double pixels) {
        return (int) (pixels / gridSize());
    }

    private void invalidateTerrainData() {
        terrainDataUpToDate = false;
    }

    public void markTileMapEdited(TileMap tileMap) {
        unsavedChanges = true;
        if (tileMap == worldMap().terrain()) {
            terrainChanged = true;
            invalidateTerrainData();
        }
        if (tileMap == worldMap().food()) {
            foodChanged = true;
        }
    }

    private void handleMapChanges() {
        if (terrainChanged) {
            mazePreview3D.updateTerrain();
        }
        if (foodChanged) {
            mazePreview3D.updateFood();
        }
        if (foodChanged || terrainChanged) {
            updateSourceView();
        }
    }

    private Vector2i tileAtMousePosition(double mouseX, double mouseY) {
        return new Vector2i(fullTiles(mouseX), fullTiles(mouseY));
    }

    private void editAtMousePosition(MouseEvent event) {
        Vector2i tile = tileAtMousePosition(event.getX(), event.getY());
        if (isEditMode(EditMode.INSPECT)) {
            identifyObstacleAtTile(tile);
            return;
        }
        boolean erase = event.isControlDown();
        switch (selectedPaletteID()) {
            case TileMapEditor.PALETTE_ID_TERRAIN -> editAtTile(worldMap().terrain(), tile, erase);
            case TileMapEditor.PALETTE_ID_FOOD -> editAtTile(worldMap().food(), tile, erase);
            case TileMapEditor.PALETTE_ID_ACTORS -> {
                if (selectedPalette().isToolSelected()) {
                    selectedPalette().selectedTool().apply(worldMap().terrain(), tile);
                    markTileMapEdited(worldMap().terrain());
                    terrainPropertiesEditor().updatePropertyEditorValues();
                }
            }
            default -> Logger.error("Unknown palette ID " + selectedPaletteID());
        }
    }

    private void editAtTile(TileMap tileMap, Vector2i tile, boolean erase) {
        if (erase) {
            clearTileValue(tileMap, tile);
        } else if (selectedPalette().isToolSelected()) {
            selectedPalette().selectedTool().apply(tileMap, tile);
        }
    }

    private void moveCursorAndSetFoodAtTile(Direction dir) {
        if (moveCursor(dir, this::canEditFoodAtTile)) {
            setFoodAtFocussedTile();
        }
    }

    private void setFoodAtFocussedTile() {
        if (editMode() == EditMode.EDIT && selectedPaletteID() == PALETTE_ID_FOOD) {
            if (canEditFoodAtTile(focussedTile())) {
                editAtTile(worldMap().food(), focussedTile(), false);
            }
        }
    }

    private boolean moveCursor(Direction dir, Predicate<Vector2i> canMoveIntoTile) {
        if (focussedTile() != null) {
            Vector2i nextTile = focussedTile().plus(dir.vector());
            if (!worldMap().terrain().outOfBounds(nextTile) && canMoveIntoTile.test(nextTile)) {
                focussedTilePy.set(nextTile);
                return true;
            }
        }
        return false;
    }

    private void selectNextPaletteEntry() {
        Palette palette = selectedPalette();
        int next = palette.selectedIndex() + 1;
        if (next == palette.numTools()) { next = 0; }
        palette.selectTool(next);
    }

    private boolean canEditFoodAtTile(Vector2i tile) {
        byte content = worldMap().terrain().get(tile);
        return content == TileEncoding.EMPTY
            || content == TileEncoding.ONE_WAY_DOWN
            || content == TileEncoding.ONE_WAY_UP
            || content == TileEncoding.ONE_WAY_LEFT
            || content == TileEncoding.ONE_WAY_RIGHT;
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
        markTileMapEdited(worldMap().terrain());
        if (tileMap == worldMap().terrain()) {
            worldMap().food().set(tile, TileEncoding.EMPTY);
            markTileMapEdited(worldMap().food());
        }
        if (isSymmetricEditMode()) {
            byte mirroredContent = mirroredTileContent(tileMap.get(tile));
            Vector2i mirrorTile = mirrored(tileMap, tile);
            tileMap.set(mirrorTile, mirroredContent);
            if (tileMap == worldMap().terrain()) {
                worldMap().food().set(mirrorTile, TileEncoding.EMPTY);
                markTileMapEdited(worldMap().food());
            }
        }
    }

    private Vector2i mirrored(TileMap tileMap, Vector2i tile) {
        return vec_2i(tileMap.numCols() - 1 - tile.x(), tile.y());
    }

    // ignores symmetric edit mode!
    private void clearTileValue(TileMap tileMap, Vector2i tile) {
        tileMap.set(tile, TileEncoding.EMPTY);
        markTileMapEdited(tileMap);
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
            addHouse(preConfiguredMap, houseOrigin);
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

    private void addBorderWall(TileMap terrain, int firstRow, int emptyRowsBottom) {
        int lastRow = terrain.numRows() - 1 - emptyRowsBottom, lastCol = terrain.numCols() - 1;
        terrain.set(firstRow, 0, TileEncoding.DCORNER_NW);
        terrain.set(firstRow, lastCol, TileEncoding.DCORNER_NE);
        terrain.set(lastRow, 0, TileEncoding.DCORNER_SW);
        terrain.set(lastRow, lastCol, TileEncoding.DCORNER_SE);
        for (int row = firstRow + 1; row < lastRow; ++row) {
            terrain.set(row, 0, TileEncoding.DWALL_V);
            terrain.set(row, lastCol, TileEncoding.DWALL_V);
        }
        for (int col = 1; col < lastCol; ++col) {
            terrain.set(firstRow, col, TileEncoding.DWALL_H);
            terrain.set(lastRow, col, TileEncoding.DWALL_H);
        }
        markTileMapEdited(terrain);
    }

    private void addHouse(WorldMap worldMap, Vector2i origin) {
        TileMap terrain = worldMap.terrain();
        addTerrainContentBlock(worldMap, GHOST_HOUSE_SHAPE, origin);
        terrain.setProperty(PROPERTY_POS_HOUSE_MIN_TILE, formatTile(origin));
        terrain.setProperty(PROPERTY_POS_RED_GHOST,      formatTile(origin.plus(3, -1)));
        terrain.setProperty(PROPERTY_POS_CYAN_GHOST,     formatTile(origin.plus(1, 2)));
        terrain.setProperty(PROPERTY_POS_PINK_GHOST,     formatTile(origin.plus(3, 2)));
        terrain.setProperty(PROPERTY_POS_ORANGE_GHOST,   formatTile(origin.plus(5, 2)));
        terrainPropertiesEditor().rebuildPropertyEditors();
        markTileMapEdited(worldMap.terrain());
        markTileMapEdited(worldMap.food());
    }

    // Does not add mirrored content, but deletes food at added block
    private void addTerrainContentBlock(WorldMap worldMap, byte[][] block, Vector2i origin) {
        int numRows = block.length, numCols = block[0].length;
        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < numCols; ++col) {
                int x = origin.x() + col, y = origin.y() + row;
                worldMap.terrain().set(y, x, block[row][col]);
                worldMap.food().set(y, x, TileEncoding.EMPTY);
            }
        }
    }

    private void addTerrainContentBlockMirrored(WorldMap worldMap, byte[][] block, Vector2i origin) {
        Vector2i mirroredOrigin = mirrored(worldMap.terrain(), origin);
        int numRows = block.length, numCols = block[0].length;
        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < numCols; ++col) {
                byte content = block[row][col];
                // Note: content is added from right to left from mirrored origin!
                int x = mirroredOrigin.x() - col, y = mirroredOrigin.y() + row;
                worldMap.terrain().set(y, x, mirroredTileContent(content));
                worldMap.food().set(y, x, TileEncoding.EMPTY);
            }
        }
    }

    private boolean isSupportedImageFile(File file) {
        return Stream.of(".bmp", ".gif", ".jpg", ".png").anyMatch(ext -> file.getName().toLowerCase().endsWith(ext));
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

    private void floodWithPellets(Vector2i startTile) {
        TileMap terrainMap = worldMap().terrain(), foodMap = worldMap().food();
        var q = new ArrayDeque<Vector2i>();
        q.push(startTile);
        while (!q.isEmpty()) {
            Vector2i tile = q.poll();
            if (foodMap.get(tile) == TileEncoding.EMPTY) {
                foodMap.set(tile, TileEncoding.PELLET);
                for (Direction dir : Direction.values()) {
                    Vector2i nb = tile.plus(dir.vector());
                    if  (!terrainMap.outOfBounds(nb)
                            && terrainMap.get(nb) == TileEncoding.EMPTY
                            && foodMap.get(nb) == TileEncoding.EMPTY) {
                        q.push(nb);
                    }
                }
            }
        }
    }

    private void detectTilesInTemplateImage() {
        Image templateImage = templateImagePy.get();
        if (templateImage == null) {
            return;
        }
        PixelReader rdr = templateImage.getPixelReader();
        Color[][] tileColors = new Color[TS][TS];
        // 3 empty rows on top, 2 on bottom
        int numRows = worldMap().terrain().numRows(), numCols = worldMap().terrain().numCols();
        for (int row = 3; row < numRows - 3; ++row) {
            for (int col = 0; col < numCols; ++col) {
                Vector2i tile = vec_2i(col, row);
                try {
                    for (int y = 0; y < TS; ++y) {
                        for (int x = 0; x < TS; ++x) {
                            tileColors[y][x] = rdr.getColor(col * TS + x, (row - 3) * TS + y);
                        }
                    }
                    byte foodValue = identifyFoodTile(tile, tileColors);
                    if (foodValue == TileEncoding.PELLET || foodValue == TileEncoding.ENERGIZER) {
                        worldMap().food().set(tile, foodValue);
                    }
                    /*
                    else {
                        byte terrainValue = identifyTerrainTile(tile, tileColors);
                        worldMap().terrain().set(tile, terrainValue);
                    }
                     */
                } catch (IndexOutOfBoundsException e) {
                    Logger.error("Could not get pixels for tile {}, maybe template image has been cropped incorrectly?", tile);
                } catch (Exception e) {
                    Logger.error("Could not get pixels for tile {}", tile);
                    Logger.error(e);
                }
            }
        }
        markTileMapEdited(worldMap().terrain());
        markTileMapEdited(worldMap().food());
    }

    private byte identifyFoodTile(Vector2i tile, Color[][] tileColors) {
        Color foodColor = getColorFromMap(worldMap().food(), PROPERTY_COLOR_FOOD, Color.WHITE);
        int numFoodPixels = 0;
        for (int row = 0; row < TS; ++row) {
            for (int col = 0; col < TS; ++col) {
                if (tileColors[row][col].equals(foodColor)) numFoodPixels++;
            }
        }
        if (numFoodPixels == 4) return TileEncoding.PELLET;
        if (numFoodPixels > 50) return TileEncoding.ENERGIZER;
        return TileEncoding.EMPTY;
    }

    private byte identifyTerrainTile(Vector2i tile, Color[][] tileColors) {
        Color fillColor = getColorFromMap(worldMap().terrain(), PROPERTY_COLOR_WALL_FILL, Color.WHITE);
        Color strokeColor = getColorFromMap(worldMap().terrain(), PROPERTY_COLOR_WALL_STROKE, Color.WHITE);
        Color doorColor = getColorFromMap(worldMap().terrain(), PROPERTY_COLOR_DOOR, Color.WHITE);

        if (empty(tileColors)) {
            return TileEncoding.EMPTY;
        }
        if (isNWCorner(tileColors)) {
            return TileEncoding.CORNER_NW;
        }
        if (isSWCorner(tileColors)) {
            return TileEncoding.CORNER_SW;
        }
        if (isNECorner(tileColors)) {
            return TileEncoding.CORNER_NE;
        }
        if (isSECorner(tileColors)) {
            return TileEncoding.CORNER_SE;
        }
        if (isHWall(tileColors)) {
            return TileEncoding.WALL_H;
        }
        return TileEncoding.EMPTY;
    }

    private boolean empty(Color[][] tileColors) {
        for (int row = 0; row < TS; ++row) {
            for (int col = 0; col < TS; ++col) {
                if (!tileColors[row][col].equals(Color.TRANSPARENT))
                    return false;
            }
        }
        return true;
    }

    private boolean isHWall(Color[][] tileColors) {
        boolean upperHalfEmpty = true;
        for (int row = 0; row < HTS; ++row) {
            for (int col = 0; col < TS; ++col) {
                if (!tileColors[row][col].equals(Color.TRANSPARENT))
                    upperHalfEmpty = false;
            }
        }
        boolean lowerHalfEmpty = true;
        for (int row = HTS; row < TS; ++row) {
            for (int col = 0; col < TS; ++col) {
                if (!tileColors[row][col].equals(Color.TRANSPARENT))
                    lowerHalfEmpty = false;
            }
        }
        return upperHalfEmpty || lowerHalfEmpty;
    }

    private boolean isNWCorner(Color[][] tileColors) {
        for (int row = 0; row < TS; ++row) {
            for (int col = 0; col < TS - row; ++col) {
                if (!tileColors[row][col].equals(Color.TRANSPARENT)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isSWCorner(Color[][] tileColors) {
        for (int row = 0; row < TS; ++row) {
            for (int col = 0; col <= row; ++col) {
                if (!tileColors[row][col].equals(Color.TRANSPARENT)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isNECorner(Color[][] tileColors) {
        for (int row = 0; row < TS; ++row) {
            for (int col = row; col < TS; ++col) {
                if (!tileColors[row][col].equals(Color.TRANSPARENT)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isSECorner(Color[][] tileColors) {
        for (int row = 0; row < TS; ++row) {
            for (int col = TS - row; col < TS; ++col) {
                if (!tileColors[row][col].equals(Color.TRANSPARENT)) {
                    return false;
                }
            }
        }
        return true;
    }
}