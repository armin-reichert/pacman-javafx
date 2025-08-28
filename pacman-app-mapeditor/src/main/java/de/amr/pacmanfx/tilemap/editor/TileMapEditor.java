/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.*;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.tilemap.editor.actions.*;
import de.amr.pacmanfx.tilemap.editor.rendering.TerrainTileMapRenderer;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import de.amr.pacmanfx.uilib.tilemap.FoodMapRenderer;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapColorScheme;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapRenderer;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.*;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.*;
import static de.amr.pacmanfx.tilemap.editor.rendering.ArcadeSprites.*;
import static java.util.Objects.requireNonNull;

public class TileMapEditor {

    public class ChangeManager {

        private boolean edited;
        private boolean terrainMapChanged;
        private boolean foodMapChanged;
        private boolean obstaclesUpToDate;
        private boolean redrawRequested;

        private final List<Vector2i> tilesWithErrors = new ArrayList<>();

        public List<Vector2i> tilesWithErrors() {
            return tilesWithErrors;
        }

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
                tilesWithErrors.addAll(currentWorldMap().buildObstacleList());
                obstaclesUpToDate = true;
                requestRedraw();
            }
            if (terrainMapChanged || foodMapChanged) {
                sourceCode.set(sourceCode());
                requestRedraw();

            }
            if (terrainMapChanged) {
                if (terrainMapPropertiesEditor != null) {
                    terrainMapPropertiesEditor.setTileMap(currentWorldMap(), LayerID.TERRAIN);
                }
                preview3D.updateTerrain();
                terrainMapChanged = false;
            }
            if (foodMapChanged) {
                if (foodMapPropertiesEditor != null) {
                    foodMapPropertiesEditor.setTileMap(currentWorldMap(), LayerID.FOOD);
                }
                preview3D.updateFood();
                foodMapChanged = false;
            }
        }

        private String sourceCode() {
            StringBuilder sb = new StringBuilder();
            String[] sourceTextLines = WorldMapFormatter.formatted(currentWorldMap()).split("\n");
            for (int i = 0; i < sourceTextLines.length; ++i) {
                sb.append("%5d: ".formatted(i + 1)).append(sourceTextLines[i]).append("\n");
            }
            return sb.toString();
        }
    }

    private final ChangeManager changeManager = new ChangeManager();
    private final MessageManager messageManager = new MessageManager();
    private final UpdateTimer updateTimer = new UpdateTimer();

    private final Stage stage;
    private final BorderPane layoutPane = new BorderPane();
    private final BorderPane contentPane = new BorderPane();
    private Pane propertyEditorsPane;
    private EditCanvas editCanvas;
    private ScrollPane spEditCanvas;
    private Preview2D preview2D;
    private ScrollPane spPreview2D;
    private Preview3D preview3D;
    private TextArea sourceView;
    private SplitPane splitEditorAndPreviewArea;
    private TabPane tabPaneForPalettes;
    private HBox statusLine;
    private Slider sliderZoom;
    private TabPane tabPaneEditorViews;
    private Tab tabEditCanvas;
    private Tab tabTemplateImage;
    private TemplateImageCanvas templateImageCanvas;
    private Pane templateImageDropTarget;
    private ScrollPane spTemplateImage;
    private Tab tabPreview2D;

    private final EditorMenuBar menuBar;

    private final Palette[] palettes = new Palette[3];

    private PropertyEditorPane terrainMapPropertiesEditor;
    private PropertyEditorPane foodMapPropertiesEditor;

    private final Model3DRepository model3DRepository;

    private class UpdateTimer extends AnimationTimer {
        @Override
        public void handle(long now) {
            messageManager.update();
            changeManager.processChanges();
            if (changeManager.isRedrawRequested()) {
                try {
                    drawUI();
                } catch (Exception x) {
                    Logger.error(x);
                }
            }
        }

    }

    private void drawUI() {
        //TODO avoid creation in every draw call
        var colorScheme = new TerrainMapColorScheme(
            COLOR_CANVAS_BACKGROUND,
            getColorFromMap(currentWorldMap(), LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_FILL, parseColor(MS_PACMAN_COLOR_WALL_FILL)),
            getColorFromMap(currentWorldMap(), LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_STROKE, parseColor(MS_PACMAN_COLOR_WALL_STROKE)),
            getColorFromMap(currentWorldMap(), LayerID.TERRAIN, WorldMapProperty.COLOR_DOOR, parseColor(MS_PACMAN_COLOR_DOOR))
        );
        palettes[selectedPaletteID()].draw();
        if (tabEditCanvas.isSelected()) {
            editCanvas.draw(changeManager, colorScheme);
        }
        else if (tabTemplateImage.isSelected()) {
            templateImageCanvas.draw();
        }
        if (tabPreview2D.isSelected()) {
            preview2D.draw(currentWorldMap(), colorScheme);
        }
    }

    public TileMapEditor(Stage stage, Model3DRepository model3DRepository) {
        this.stage = requireNonNull(stage);
        this.model3DRepository = requireNonNull(model3DRepository);

        createEditArea();
        createPreviewArea();
        createPalettes(editCanvas.terrainRenderer(), editCanvas.foodRenderer());
        createPropertyEditors();
        createStatusLine();
        arrangeContent();

        menuBar = new EditorMenuBar(this);
        loadSampleMapsAndUpdateMenu(menuBar.menuMaps());

        layoutPane.setTop(menuBar);
        layoutPane.setCenter(contentPane);

        contentPane.setOnKeyTyped(this::onKeyTyped);
        contentPane.setOnKeyPressed(this::onKeyPressed);
    }

    public void init(File workDir) {
        setCurrentDirectory(workDir);
        WorldMap emptyMap = new Action_CreateEmptyMap(this, 36, 28).execute();
        setCurrentWorldMap(emptyMap);
        setEditMode(EditMode.INSPECT);
        preview3D.reset();
        changeManager.edited = false;
    }

    public void start(Stage stage) {
        Platform.runLater(() -> {
            title.bind(createTitleBinding());
            stage.titleProperty().bind(title);
            contentPane.setLeft(null); // no properties editor
            contentPane.requestFocus();
            showEditHelpText();
            updateTimer.start();
        });
    }

    public void stop() {
        updateTimer.stop();
        setEditMode(EditMode.INSPECT);
    }

    // -- actorsVisible

    public boolean DEFAULT_ACTORS_VISIBLE = true;

    private BooleanProperty actorsVisible;

    public BooleanProperty actorsVisibleProperty() {
        if (actorsVisible == null) {
            actorsVisible = new SimpleBooleanProperty(DEFAULT_ACTORS_VISIBLE) {
                @Override
                protected void invalidated() {
                    changeManager.requestRedraw();
                }
            };
        }
        return actorsVisible;
    }

    public boolean actorsVisible() {
        return actorsVisible == null ? DEFAULT_ACTORS_VISIBLE : actorsVisibleProperty().get();
    }

    public void setActorsVisible(boolean visible) {
        actorsVisibleProperty().set(visible);
    }

    // -- currentDirectory

    private final ObjectProperty<File> currentDirectory = new SimpleObjectProperty<>();

    public File currentDirectory() {
        return currentDirectory.get();
    }

    public void setCurrentDirectory(File dir) {
        currentDirectory.set(dir);
    }

    // -- currentFile

    private final ObjectProperty<File> currentFile = new SimpleObjectProperty<>();

    public ObjectProperty<File> currentFileProperty() {
        return currentFile;
    }

    public void setCurrentFile(File file) {
        currentFile.set(file);
    }

    public File currentFile() {
        return currentFile.get();
    }

    // -- currentWorldMap

    private final ObjectProperty<WorldMap> currentWorldMap = new SimpleObjectProperty<>(WorldMap.emptyMap(28, 36)) {
        @Override
        protected void invalidated() {
            changeManager.setWorldMapChanged();
        }
    };

    public ObjectProperty<WorldMap> currentWorldMapProperty() { return currentWorldMap; }

    public WorldMap currentWorldMap() { return currentWorldMap.get(); }

    public void setCurrentWorldMap(WorldMap worldMap) { currentWorldMap.set(worldMap); }

    // -- editMode

    public static final EditMode DEFAULT_EDIT_MODE = EditMode.INSPECT;

    private ObjectProperty<EditMode> editMode;

    public ObjectProperty<EditMode> editModeProperty() {
        if (editMode == null) {
            editMode = new SimpleObjectProperty<>(DEFAULT_EDIT_MODE) {
                @Override
                protected void invalidated() {
                    onEditModeChanged(get());
                }
            };
        }
        return editMode;
    }

    public EditMode editMode() { return editMode == null ? DEFAULT_EDIT_MODE : editModeProperty().get(); }

    public void setEditMode(EditMode mode) {
        editModeProperty().set(requireNonNull(mode));
    }

    public boolean editModeIs(EditMode mode) { return editMode() == mode; }

    // -- gridSize

    private static final double DEFAULT_GRID_SIZE = 8;

    private DoubleProperty gridSize;

    public DoubleProperty gridSizeProperty() {
        if (gridSize == null) {
            gridSize = new SimpleDoubleProperty(DEFAULT_GRID_SIZE) {
                @Override
                protected void invalidated() {
                    changeManager.requestRedraw();
                }
            };
        }
        return gridSize;
    }

    public double gridSize() { return gridSize.get(); }

    public void setGridSize(double size) {
        gridSizeProperty().set(size);
    }

    // -- foodVisible

    public static final boolean DEFAULT_FOOD_VISIBLE = true;

    private BooleanProperty foodVisible;

    public BooleanProperty foodVisibleProperty() {
        if (foodVisible == null) {
            foodVisible = new SimpleBooleanProperty(DEFAULT_FOOD_VISIBLE) {
                @Override
                protected void invalidated() {
                    changeManager.requestRedraw();
                }
            };
        }
        return foodVisible;
    }

    public boolean foodVisible() {
        return foodVisible == null ? DEFAULT_FOOD_VISIBLE : foodVisible.get();
    }

    public void setFoodVisible(boolean visible) {
        foodVisibleProperty().set(visible);
    }

    // -- gridVisible

    public static final boolean DEFAULT_GRID_VISIBLE = true;

    private BooleanProperty gridVisible;

    public BooleanProperty gridVisibleProperty() {
        if (gridVisible == null) {
            gridVisible = new SimpleBooleanProperty(DEFAULT_GRID_VISIBLE) {
                @Override
                protected void invalidated() {
                    changeManager.requestRedraw();
                }
            };
        }
        return gridVisible;
    }

    // -- mapPropertyEditorsVisible

    public static final boolean DEFAULT_MAP_PROPERTY_EDITORS_VISIBLE = false;

    private BooleanProperty mapPropertyEditorsVisible;

    public BooleanProperty propertyEditorsVisibleProperty() {
        if (mapPropertyEditorsVisible == null) {
            mapPropertyEditorsVisible = new SimpleBooleanProperty(DEFAULT_MAP_PROPERTY_EDITORS_VISIBLE) {
                @Override
                protected void invalidated() {
                    contentPane.setLeft(get() ? propertyEditorsPane : null);
                }
            };
        }
        return mapPropertyEditorsVisible;
    }

    public boolean mapPropertyEditorsVisible() {
        return mapPropertyEditorsVisible == null ? DEFAULT_MAP_PROPERTY_EDITORS_VISIBLE : propertyEditorsVisibleProperty().get();
    }

    public void setMapPropertyEditorsVisible(boolean value) {
        propertyEditorsVisibleProperty().set(value);
    }

    // -- obstacleInnerAreaDisplayed

    public static final boolean DEFAULT_OBSTACLE_INNER_AREA_DISPLAYED = false;

    private BooleanProperty obstacleInnerAreaDisplayed;

    public BooleanProperty obstacleInnerAreaDisplayedProperty() {
        if (obstacleInnerAreaDisplayed == null) {
            obstacleInnerAreaDisplayed = new SimpleBooleanProperty(DEFAULT_OBSTACLE_INNER_AREA_DISPLAYED) {
                @Override
                protected void invalidated() {
                    changeManager.requestRedraw();
                }
            };
        }
        return obstacleInnerAreaDisplayed;
    }

    public boolean obstacleInnerAreaDisplayed() {
        return obstacleInnerAreaDisplayed == null ? DEFAULT_OBSTACLE_INNER_AREA_DISPLAYED :obstacleInnerAreaDisplayedProperty().get();
    }

    public void setObstacleInnerAreaDisplayed(boolean value) {
        obstacleInnerAreaDisplayedProperty().set(value);
    }

    // -- obstaclesJoining

    public static boolean DEFAULT_OBSTACLES_JOINING = true;

    private BooleanProperty obstaclesJoining;

    public BooleanProperty obstaclesJoiningProperty() {
        if (obstaclesJoining == null) {
            obstaclesJoining = new SimpleBooleanProperty(DEFAULT_OBSTACLES_JOINING);
        }
        return obstaclesJoining;
    }

    public boolean obstaclesJoining() {
        return obstaclesJoining == null ? DEFAULT_OBSTACLES_JOINING : obstaclesJoiningProperty().get();
    }

    public void setObstaclesJoining(boolean value) {
        obstaclesJoiningProperty().set(value);
    }

    // -- segmentNumbersVisible

    public static final boolean DEFAULT_SEGMENT_NUMBERS_VISIBLE = false;

    private BooleanProperty segmentNumbersVisible;

    public BooleanProperty segmentNumbersVisibleProperty() {
        if (segmentNumbersVisible == null) {
            segmentNumbersVisible = new SimpleBooleanProperty(DEFAULT_SEGMENT_NUMBERS_VISIBLE) {
                @Override
                protected void invalidated() {
                    changeManager.requestRedraw();
                }
            };
        }
        return segmentNumbersVisible;
    }

    public boolean segmentNumbersVisible() {
        return segmentNumbersVisible == null ? DEFAULT_SEGMENT_NUMBERS_VISIBLE : segmentNumbersVisibleProperty().get();
    }

    public void setSegmentNumbersVisible(boolean value) {
        segmentNumbersVisibleProperty().set(value);
    }

    // -- sourceCode

    private final StringProperty sourceCode = new SimpleStringProperty("");


    // -- symmetricEditMode

    public static final boolean DEFAULT_SYMMETRIC_EDIT_MODE = true;

    private BooleanProperty symmetricEditMode;

    public BooleanProperty symmetricEditModeProperty() {
        if (symmetricEditMode == null) {
            symmetricEditMode = new SimpleBooleanProperty(DEFAULT_SYMMETRIC_EDIT_MODE);
        }
        return symmetricEditMode;
    }

    public boolean symmetricEditMode() {
        return symmetricEditMode == null ? DEFAULT_SYMMETRIC_EDIT_MODE : symmetricEditModeProperty().get();
    }

    public void setSymmetricEditMode(boolean value) {
        symmetricEditModeProperty().set(value);
    }

    // -- templateImage

    private final ObjectProperty<Image> templateImage = new SimpleObjectProperty<>();

    public Image templateImage() {
        return templateImage.get();
    }

    public void setTemplateImage(Image image) {
        templateImage.set(image);
    }

    // -- terrainVisible

    public static final boolean DEFAULT_TERRAIN_VISIBLE = true;

    private BooleanProperty terrainVisible;

    public BooleanProperty terrainVisibleProperty() {
        if (terrainVisible == null) {
            terrainVisible = new SimpleBooleanProperty(DEFAULT_TERRAIN_VISIBLE) {
                @Override
                protected void invalidated() {
                    changeManager.requestRedraw();
                }
            };
        }
        return terrainVisible;
    }

    public boolean terrainVisible() {
        return terrainVisible == null ? DEFAULT_TERRAIN_VISIBLE : terrainVisible.get();
    }

    public void setTerrainVisible(boolean visible) {
        terrainVisibleProperty().set(visible);
    }

    // -- templateImage

    public ObjectProperty<Image> templateImageProperty() { return templateImage; }


    // -- title

    private final StringProperty title = new SimpleStringProperty("Tile Map Editor");

    public StringProperty titleProperty() { return title; }

    // Accessor methods

    public ChangeManager changeManager() { return changeManager;}

    public Stage stage() {
        return stage;
    }

    public BorderPane layoutPane() {
        return layoutPane;
    }

    public EditCanvas editCanvas() {
        return editCanvas;
    }

    public EditorMenuBar menuBar() {
        return menuBar;
    }

    public MessageManager messageManager() {
        return messageManager;
    }

    public byte selectedPaletteID() {
        return (Byte) tabPaneForPalettes.getSelectionModel().getSelectedItem().getUserData();
    }

    public Palette selectedPalette() {
        return palettes[selectedPaletteID()];
    }

    public void selectTemplateImageTab() {
        tabPaneEditorViews.getSelectionModel().select(tabTemplateImage);
    }

    public void showEditHelpText() {
        messageManager.showMessage(translated("edit_help"), 30, MessageType.INFO);
    }

    private void createEditCanvas() {
        editCanvas = new EditCanvas();
        editCanvas.editModeProperty().bind(editModeProperty());
        editCanvas.gridSizeProperty().bind(gridSizeProperty());
        editCanvas.gridVisibleProperty().bind(gridVisibleProperty());
        editCanvas.worldMapProperty().bind(currentWorldMapProperty());
        editCanvas.obstacleInnerAreaDisplayedProperty().bind(obstacleInnerAreaDisplayedProperty());
        editCanvas.obstaclesJoiningProperty().bind(obstaclesJoiningProperty());
        editCanvas.segmentNumbersVisibleProperty().bind(segmentNumbersVisibleProperty());
        editCanvas.symmetricEditModeProperty().bind(symmetricEditModeProperty());
        editCanvas.templateImageGrayProperty().bind(templateImageProperty().map(Ufx::imageToGreyscale));
        editCanvas.terrainVisibleProperty().bind(terrainVisibleProperty());
        editCanvas.foodVisibleProperty().bind(foodVisibleProperty());
        editCanvas.actorsVisibleProperty().bind(actorsVisibleProperty());

        editCanvas.obstacleEditor().setOnEditTile(
            (tile, code) -> new Action_SetTileCode(this, currentWorldMap(), LayerID.TERRAIN, tile, code).execute());
        editCanvas.setOnContextMenuRequested(event -> editCanvas.onContextMenuRequested(this, event));
        editCanvas.setOnMouseClicked(event -> editCanvas.onMouseClicked(this, event));
        editCanvas.setOnMouseMoved(event -> editCanvas.onMouseMoved(this, event));
        editCanvas.setOnMouseReleased(event -> editCanvas.onMouseReleased(this, event));
        editCanvas.setOnKeyPressed(event -> editCanvas.onKeyPressed(this, event));

        spEditCanvas = new ScrollPane(editCanvas);
        spEditCanvas.setFitToHeight(true);
        registerDragAndDropImageHandler(spEditCanvas);
        //TODO is there a better way to get the initial resize time of the scroll pane?
        spEditCanvas.heightProperty().addListener((py,oldHeight,newHeight) -> {
            if (oldHeight.doubleValue() == 0) { // initial resize
                int initialGridSize = (int) Math.max(newHeight.doubleValue() / currentWorldMap().numRows(), MIN_GRID_SIZE);
                setGridSize(initialGridSize);
            }
        });
    }

    private void createPreview2D() {
        preview2D = new Preview2D();
        preview2D.widthProperty().bind(editCanvas.widthProperty());
        preview2D.heightProperty().bind(editCanvas.heightProperty());
        preview2D.gridSizeProperty().bind(gridSizeProperty());
        preview2D.terrainVisibleProperty().bind(terrainVisibleProperty());
        preview2D.foodVisibleProperty().bind(foodVisibleProperty());
        preview2D.actorsVisibleProperty().bind(actorsVisibleProperty());

        spPreview2D = new ScrollPane(preview2D);
        spPreview2D.setFitToHeight(true);
        spPreview2D.hvalueProperty().bindBidirectional(spEditCanvas.hvalueProperty());
        spPreview2D.vvalueProperty().bindBidirectional(spEditCanvas.vvalueProperty());
    }

    private void createPreview3D() {
        preview3D = new Preview3D(this, model3DRepository, 500, 500);
        preview3D.foodVisibleProperty().bind(foodVisibleProperty());
        preview3D.terrainVisibleProperty().bind(terrainVisibleProperty());
        preview3D.worldMapProperty().bind(currentWorldMap);
    }

    private void createTemplateImageCanvas() {
        templateImageCanvas = new TemplateImageCanvas(this);
        Pane pane = new Pane(templateImageCanvas, templateImageCanvas.getColorIndicator());
        pane.setBackground(Background.fill(Color.TRANSPARENT));
        spTemplateImage = new ScrollPane(pane);
    }

    private void createSourceView() {
        sourceView = new TextArea();
        sourceView.setEditable(false);
        sourceView.setWrapText(false);
        sourceView.setPrefWidth(600);
        sourceView.setPrefHeight(800);
        sourceView.setFont(FONT_SOURCE_VIEW);
        sourceView.setStyle("-fx-control-inner-background:#222; -fx-text-fill: #f0f0f0;");
        sourceView.textProperty().bind(sourceCode);
    }

    private void createEditArea() {
        createEditCanvas();
        createTemplateImageCanvas();

        tabEditCanvas = new Tab(translated("tab_editor"), spEditCanvas);

        var dropHintButton = new Button(translated("image_drop_hint"));
        dropHintButton.setFont(FONT_DROP_HINT);
        dropHintButton.setOnAction(ae -> new Action_OpenTemplateCreateMap(this).execute());
        dropHintButton.disableProperty().bind(editModeProperty().map(mode -> mode == EditMode.INSPECT));

        templateImageDropTarget = new BorderPane(dropHintButton);
        registerDragAndDropImageHandler(templateImageDropTarget);

        var stackPane = new StackPane(spTemplateImage, templateImageDropTarget);
        tabTemplateImage = new Tab(translated("tab_template_image"), stackPane);
        templateImage.addListener((py, ov, image) -> {
            Logger.info("Template image changed from {} to {}", ov, image);
            stackPane.getChildren().remove(templateImageDropTarget);
            if (image == null) {
                stackPane.getChildren().add(templateImageDropTarget);
            }
        });

        tabPaneEditorViews = new TabPane(tabEditCanvas, tabTemplateImage);
        tabPaneEditorViews.getTabs().forEach(tab -> tab.setClosable(false));
        tabPaneEditorViews.setSide(Side.BOTTOM);
        tabPaneEditorViews.getSelectionModel().select(tabEditCanvas);
    }

    private void registerDragAndDropImageHandler(Node node) {
        node.setOnDragOver(dragEvent -> {
            if (dragEvent.getDragboard().hasFiles()) {
                File file = dragEvent.getDragboard().getFiles().getFirst();
                if (isSupportedImageFile(file) && !editModeIs(EditMode.INSPECT) || isWorldMapFile(file)) {
                    dragEvent.acceptTransferModes(TransferMode.COPY);
                }
            }
            dragEvent.consume();
        });
        node.setOnDragDropped(dragEvent -> {
            if (dragEvent.getDragboard().hasFiles()) {
                File file = dragEvent.getDragboard().getFiles().getFirst();
                ifNoUnsavedChangesDo(() -> editCanvas.onFileDropped(this, file));
            }
            dragEvent.consume();
        });
    }

    private void createPreviewArea() {
        createPreview2D();
        createPreview3D();
        createSourceView();

        tabPreview2D = new Tab(translated("preview2D"), spPreview2D);
        Tab tabPreview3D = new Tab(translated("preview3D"), preview3D.getSubScene());
        Tab tabSourceView = new Tab(translated("source"), sourceView);

        TabPane tabPane = new TabPane(tabPreview2D, tabPreview3D, tabSourceView);
        tabPane.setSide(Side.BOTTOM);
        tabPane.getTabs().forEach(tab -> tab.setClosable(false));
        tabPane.getSelectionModel().select(tabPreview2D);

        preview3D.getSubScene().widthProperty().bind(tabPane.widthProperty());
        preview3D.getSubScene().heightProperty().bind(tabPane.heightProperty());

        splitEditorAndPreviewArea = new SplitPane(tabPaneEditorViews, tabPane);
        splitEditorAndPreviewArea.setDividerPositions(0.5);
    }

    private void createPalettes(TerrainTileMapRenderer terrainRenderer, FoodMapRenderer foodRenderer) {
        palettes[PALETTE_ID_TERRAIN] = createTerrainPalette(terrainRenderer);
        palettes[PALETTE_ID_FOOD]    = createFoodPalette(foodRenderer);
        palettes[PALETTE_ID_ACTORS]  = createActorsPalette(terrainRenderer);

        var tabTerrain = new Tab("", palettes[PALETTE_ID_TERRAIN].root());
        tabTerrain.setGraphic(new Text(translated("terrain")));
        tabTerrain.setClosable(false);
        tabTerrain.setUserData(PALETTE_ID_TERRAIN);

        var tabPellets = new Tab("", palettes[PALETTE_ID_FOOD].root());
        tabPellets.setGraphic(new Text(translated("pellets")));
        tabPellets.setClosable(false);
        tabPellets.setUserData(PALETTE_ID_FOOD);

        var tabActors = new Tab("", palettes[PALETTE_ID_ACTORS].root());
        tabActors.setGraphic(new Text(translated("actors")));
        tabActors.setClosable(false);
        tabActors.setUserData(PALETTE_ID_ACTORS);

        tabPaneForPalettes = new TabPane(tabTerrain, tabPellets, tabActors);
        tabPaneForPalettes.setPadding(new Insets(5, 5, 5, 5));
        tabPaneForPalettes.setMinHeight(75);

        tabPaneForPalettes.getSelectionModel().selectedItemProperty().addListener(
            (py, ov, selectedTab) -> updatePalettesTabPaneDisplay(selectedTab));
        updatePalettesTabPaneDisplay(tabPaneForPalettes.getSelectionModel().getSelectedItem());
    }

    private void updatePalettesTabPaneDisplay(Tab selectedTab) {
        for (Tab tab : tabPaneForPalettes.getTabs()) {
            if (tab.getGraphic() instanceof Text text) {
                text.setFont(tab == selectedTab ? FONT_SELECTED_PALETTE : FONT_UNSELECTED_PALETTE);
            }
        }
    }

    private Palette createTerrainPalette(TerrainMapRenderer terrainMapRenderer) {
        var palette = new Palette(this, PALETTE_ID_TERRAIN, TOOL_SIZE, 1, 13, terrainMapRenderer);
        palette.addTileTool(this, TerrainTile.EMPTY.$, "Empty Space");
        palette.addTileTool(this, TerrainTile.WALL_H.$, "Horiz. Wall");
        palette.addTileTool(this, TerrainTile.WALL_V.$, "Vert. Wall");
        palette.addTileTool(this, TerrainTile.ARC_NW.$, "NW Corner");
        palette.addTileTool(this, TerrainTile.ARC_NE.$, "NE Corner");
        palette.addTileTool(this, TerrainTile.ARC_SW.$, "SW Corner");
        palette.addTileTool(this, TerrainTile.ARC_SE.$, "SE Corner");
        palette.addTileTool(this, TerrainTile.TUNNEL.$, "Tunnel");
        palette.addTileTool(this, TerrainTile.DOOR.$, "Door");
        palette.addTileTool(this, TerrainTile.ONE_WAY_UP.$, "One-Way Up");
        palette.addTileTool(this, TerrainTile.ONE_WAY_RIGHT.$, "One-Way Right");
        palette.addTileTool(this, TerrainTile.ONE_WAY_DOWN.$, "One-Way Down");
        palette.addTileTool(this, TerrainTile.ONE_WAY_LEFT.$, "One-Way Left");

        palette.selectTool(0); // "No Tile"
        return palette;
    }

    private Palette createActorsPalette(TerrainTileMapRenderer renderer) {
        var palette = new Palette(this, PALETTE_ID_ACTORS, TOOL_SIZE, 1, 11, renderer);
        palette.addTileTool(this, TerrainTile.EMPTY.$, "Nope");
        palette.addPropertyTool(WorldMapProperty.POS_PAC, "Pac-Man");
        palette.addPropertyTool(WorldMapProperty.POS_RED_GHOST, "Red Ghost");
        palette.addPropertyTool(WorldMapProperty.POS_PINK_GHOST, "Pink Ghost");
        palette.addPropertyTool(WorldMapProperty.POS_CYAN_GHOST, "Cyan Ghost");
        palette.addPropertyTool(WorldMapProperty.POS_ORANGE_GHOST, "Orange Ghost");
        palette.addPropertyTool(WorldMapProperty.POS_BONUS, "Bonus");
        palette.addPropertyTool(WorldMapProperty.POS_SCATTER_RED_GHOST, "Red Ghost Scatter");
        palette.addPropertyTool(WorldMapProperty.POS_SCATTER_PINK_GHOST, "Pink Ghost Scatter");
        palette.addPropertyTool(WorldMapProperty.POS_SCATTER_CYAN_GHOST, "Cyan Ghost Scatter");
        palette.addPropertyTool(WorldMapProperty.POS_SCATTER_ORANGE_GHOST, "Orange Ghost Scatter");
        palette.selectTool(0); // "No actor"
        return palette;
    }

    private Palette createFoodPalette(FoodMapRenderer renderer) {
        var palette = new Palette(this, PALETTE_ID_FOOD, TOOL_SIZE, 1, 3, renderer);
        palette.addTileTool(this, FoodTile.EMPTY.code(), "No Food");
        palette.addTileTool(this, FoodTile.PELLET.code(), "Pellet");
        palette.addTileTool(this, FoodTile.ENERGIZER.code(), "Energizer");
        palette.selectTool(0); // "No Food"
        return palette;
    }

    private void createPropertyEditors() {
        terrainMapPropertiesEditor = new PropertyEditorPane(this);
        terrainMapPropertiesEditor.enabledPy.bind(editModeProperty().map(mode -> mode != EditMode.INSPECT));
        terrainMapPropertiesEditor.setPadding(new Insets(10,0,0,0));

        foodMapPropertiesEditor = new PropertyEditorPane(this);
        foodMapPropertiesEditor.enabledPy.bind(editModeProperty().map(mode -> mode != EditMode.INSPECT));
        foodMapPropertiesEditor.setPadding(new Insets(10,0,0,0));

        var terrainPropertiesPane = new TitledPane(translated("terrain"), terrainMapPropertiesEditor);
        terrainPropertiesPane.setMinWidth(300);
        terrainPropertiesPane.setExpanded(true);

        var foodPropertiesPane = new TitledPane(translated("pellets"), foodMapPropertiesEditor);
        foodPropertiesPane.setExpanded(true);

        propertyEditorsPane = new VBox(terrainPropertiesPane, foodPropertiesPane);
        propertyEditorsPane.visibleProperty().bind(propertyEditorsVisibleProperty());
    }

    private void createZoomControl() {
        sliderZoom = new Slider(MIN_GRID_SIZE, MAX_GRID_SIZE, 0.5 * (MIN_GRID_SIZE + MAX_GRID_SIZE));
        sliderZoom.setShowTickLabels(false);
        sliderZoom.setShowTickMarks(true);
        sliderZoom.setPrefWidth(120);
        Bindings.bindBidirectional(sliderZoom.valueProperty(), gridSize);
        Tooltip tt = new Tooltip();
        tt.setShowDelay(Duration.millis(50));
        tt.setFont(Font.font(14));
        tt.textProperty().bind(gridSizeProperty().map("Grid Size: %.0f"::formatted));
        sliderZoom.setTooltip(tt);
    }

    private void createStatusLine() {
        var lblMapSize = new Label();
        lblMapSize.setFont(FONT_STATUS_LINE_NORMAL);
        lblMapSize.textProperty().bind(currentWorldMap.map(worldMap -> (worldMap != null)
            ? "Cols: %d Rows: %d".formatted(worldMap.numCols(), worldMap.numRows()) : "")
        );

        var lblFocussedTile = new Label();
        lblFocussedTile.setFont(FONT_STATUS_LINE_NORMAL);
        lblFocussedTile.setMinWidth(100);
        lblFocussedTile.setMaxWidth(100);
        lblFocussedTile.textProperty().bind(editCanvas.focussedTileProperty().map(
            tile -> tile != null ? "(%2d,%2d)".formatted(tile.x(), tile.y()) : "n/a"));

        var statusIndicator = new StatusIndicator();
        statusIndicator.setAlignment(Pos.BASELINE_RIGHT);

        createZoomControl();

        statusLine = new HBox(
            lblMapSize,
            filler(10),
            lblFocussedTile,
            spacer(),
            messageManager.messageLabel(),
            spacer(),
            filler(10),
            sliderZoom,
            filler(10),
            statusIndicator
        );
        statusLine.setPadding(new Insets(6, 2, 2, 2));
    }

    private class StatusIndicator extends HBox {

        public StatusIndicator() {
            Label label = new Label();
            label.setMinWidth(75);
            label.setFont(FONT_STATUS_LINE_EDIT_MODE);
            label.setEffect(new Glow(0.2));
            getChildren().add(label);

            label.textProperty().bind(Bindings.createStringBinding(
                () -> switch (editMode()) {
                    case INSPECT -> translated("mode.inspect");
                    case EDIT    -> translated(symmetricEditMode() ? "mode.symmetric" : "mode.edit");
                    case ERASE   -> translated("mode.erase");
                }, editModeProperty(), symmetricEditModeProperty()
            ));

            label.textFillProperty().bind(editModeProperty().map(
                mode -> switch (mode) {
                    case INSPECT -> Color.GRAY;
                    case EDIT    -> Color.FORESTGREEN;
                    case ERASE   -> Color.RED;
                }));

            label.setOnMouseClicked(e -> selectNextEditMode());
        }
    }

    private void arrangeContent() {
        var content = new VBox(tabPaneForPalettes, splitEditorAndPreviewArea, statusLine);
        content.setPadding(new Insets(0,5,0,5));
        VBox.setVgrow(tabPaneForPalettes, Priority.NEVER);
        VBox.setVgrow(splitEditorAndPreviewArea, Priority.ALWAYS);
        VBox.setVgrow(statusLine, Priority.NEVER);
        contentPane.setLeft(propertyEditorsPane);
        contentPane.setCenter(content);
    }

    private StringBinding createTitleBinding() {
        return Bindings.createStringBinding(() -> {
                File mapFile = currentFile();
                if (mapFile != null) {
                    return "%s: [%s] - %s".formatted( translated("map_editor"), mapFile.getName(), mapFile.getPath() );
                }
                if (currentWorldMap() != null && currentWorldMap().url() != null) {
                    return  "%s: [%s]".formatted( translated("map_editor"), currentWorldMap().url() );
                }
                return "%s: [%s: %d rows %d cols]".formatted(
                        translated("map_editor"), translated("unsaved_map"),
                        currentWorldMap().numRows(), currentWorldMap().numCols() );
            }, currentFile, currentWorldMap
        );
    }

    // also called from EditorPage
    public MenuItem createLoadMapMenuItem(String description, WorldMap worldMap) {
        requireNonNull(description);
        requireNonNull(worldMap);
        var menuItem = new MenuItem(description);
        menuItem.setOnAction(e -> {
            WorldMap copy = WorldMap.copyMap(worldMap);
            ifNoUnsavedChangesDo(() -> setCurrentWorldMap(copy));
        });
        return menuItem;
    }

    public void ifNoUnsavedChangesDo(Runnable action) {
        if (!changeManager.isEdited()) {
            action.run();
            return;
        }
        SaveConfirmation confirmationDialog = new SaveConfirmation();
        confirmationDialog.showAndWait().ifPresent(choice -> {
            if (choice == SaveConfirmation.SAVE_CHANGES) {
                new Action_SaveMapFile(this).execute();
                action.run();
            } else if (choice == SaveConfirmation.NO_SAVE_CHANGES) {
                changeManager.setEdited(false);
                action.run();
            } else if (choice == SaveConfirmation.CLOSE) {
                confirmationDialog.close();
            }
        });
    }

    private void onEditModeChanged(EditMode editMode) {
        messageManager.clearMessage();
        showEditHelpText();
        switch (editMode) {
            case INSPECT -> editCanvas.enterInspectMode();
            case EDIT    -> editCanvas.enterEditMode();
            case ERASE   -> editCanvas.enterEraseMode();
        }
    }

    // Controller part

    public void selectNextEditMode() {
        switch (editMode()) {
            case INSPECT -> {
                setEditMode(EditMode.EDIT);
                setSymmetricEditMode(false);
            }
            case EDIT -> {
                if (symmetricEditMode()) {
                    setEditMode(EditMode.ERASE);
                } else {
                    setSymmetricEditMode(true);
                }
            }
            case ERASE -> setEditMode(EditMode.INSPECT);
        }
    }

    private void onKeyPressed(KeyEvent e) {
        KeyCode key = e.getCode();
        boolean alt = e.isAltDown();

        if (alt && key == KeyCode.LEFT) {
            new Action_SelectNextMapFile(this, false).execute();
        }
        else if (alt && key == KeyCode.RIGHT) {
            new Action_SelectNextMapFile(this, true).execute();
        }
        else if (key == KeyCode.PLUS) {
            new Action_ZoomIn(this).execute();
        }
        else if (key == KeyCode.MINUS) {
            new Action_ZoomOut(this).execute();
        }
    }

    private void onKeyTyped(KeyEvent e) {
        String ch = e.getCharacter();
        if (ch.equals("e")) {
            selectNextEditMode();
        }
    }

    public void moveCursorAndSetFoodAtTile(Direction dir) {
        if (editCanvas.moveCursor(dir, tile -> hasAccessibleTerrainAtTile(currentWorldMap(), tile))) {
            if (editModeIs(EditMode.EDIT) && selectedPaletteID() == PALETTE_ID_FOOD) {
                if (hasAccessibleTerrainAtTile(currentWorldMap(), editCanvas.focussedTile())) {
                    editFoodAtTile(editCanvas.focussedTile());
                }
            }
        }
    }

    private void editFoodAtTile(Vector2i tile) {
        if (selectedPalette().isToolSelected()) {
            selectedPalette().selectedTool().editor().accept(LayerID.FOOD, tile);
        }
        changeManager().setFoodMapChanged();
        changeManager().setEdited(true);
    }

    // Sample maps loading

    record SampleMaps(WorldMap pacManMap, List<WorldMap> msPacmanMaps, List<WorldMap> xxlMaps) {}

    private void loadSampleMapsAndUpdateMenu(Menu menu) {
        try {
            SampleMaps maps = loadSampleMaps();
            menu.getItems().clear();
            menu.getItems().add(createLoadMapMenuItem("Pac-Man", maps.pacManMap()));
            menu.getItems().add(new SeparatorMenuItem());
            for (int i = 0; i < maps.msPacmanMaps().size(); ++i) {
                menu.getItems().add(
                    createLoadMapMenuItem("Ms. Pac-Man %d".formatted(i+1), maps.msPacmanMaps().get(i)));
            }
            menu.getItems().add(new SeparatorMenuItem());
            for (int i = 0; i < maps.xxlMaps().size(); ++i) {
                menu.getItems().add(
                    createLoadMapMenuItem("Pac-Man XXL %d".formatted(i+1), maps.xxlMaps().get(i)));
            }
        } catch (IOException x) {
            Logger.error(x);
            Logger.error("Error loading sample maps");
        }
    }

    private SampleMaps loadSampleMaps() throws IOException {
        var pacManMap = WorldMap.fromURL(sampleMapURL("pacman/pacman.world", 1));
        var msPacManMaps = new ArrayList<WorldMap>();
        for (int n = 1; n <= 6; ++n) {
            URL url = sampleMapURL("mspacman/mspacman_%d.world", n);
            msPacManMaps.add(WorldMap.fromURL(url));
        }
        msPacManMaps.trimToSize();
        var xxlMaps = new ArrayList<WorldMap>();
        for (int n = 1; n <= 8; ++n) {
            URL url = sampleMapURL("pacman_xxl/masonic_%d.world", n);
            xxlMaps.add(WorldMap.fromURL(url));
        }
        xxlMaps.trimToSize();
        return new SampleMaps(pacManMap, msPacManMaps, xxlMaps);
    }

    private URL sampleMapURL(String namePattern, int number) {
        return getClass().getResource(SAMPLE_MAPS_PATH + namePattern.formatted(number));
    }
}