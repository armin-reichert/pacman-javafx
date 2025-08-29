/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.tilemap.FoodTile;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.TerrainTile;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.tilemap.editor.actions.*;
import de.amr.pacmanfx.tilemap.editor.rendering.TerrainTileMapRenderer;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import de.amr.pacmanfx.uilib.tilemap.FoodMapRenderer;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapColorScheme;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapRenderer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
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
import java.util.EnumMap;
import java.util.Map;

import static de.amr.pacmanfx.lib.tilemap.WorldMapFormatter.formatTile;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.*;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.*;
import static de.amr.pacmanfx.tilemap.editor.rendering.ArcadeSprites.*;
import static java.util.Objects.requireNonNull;

public class TileMapEditorUI {

    private final TileMapEditor editor;
    private final MessageDisplay messageDisplay = new MessageDisplay();

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
    private SplitPane splitPaneMapEditorAndPreviews;
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

    private final Map<PaletteID, Palette> palettes = new EnumMap<>(PaletteID.class);

    private PropertyEditorPane terrainMapPropertiesEditor;
    private PropertyEditorPane foodMapPropertiesEditor;

    public TileMapEditorUI(Stage stage, TileMapEditor editor, Model3DRepository model3DRepository) {
        this.stage = requireNonNull(stage);
        this.editor = editor;

        createEditArea();
        createPreviewArea(model3DRepository);
        createPalettes(editCanvas);
        createPropertyEditors();
        createStatusLine();

        menuBar = new EditorMenuBar(this);
        TileMapEditor.SampleMaps sampleMaps = editor.loadSampleMaps();
        if (sampleMaps != null) {
            addSampleMapMenuEntries(sampleMaps);
        }

        arrangeLayout();

        contentPane.setOnKeyTyped(this::onKeyTyped);
        contentPane.setOnKeyPressed(this::onKeyPressed);

        editor.propertyEditorsVisibleProperty().addListener((py, ov, visible) ->
            contentPane.setLeft(visible ? propertyEditorsPane : null));

        editor.editModeProperty().addListener((py, on, newEditMode) -> {
            messageDisplay().clearMessage();
            showEditHelpText();
            switch (newEditMode) {
                case INSPECT -> editCanvas.enterInspectMode();
                case EDIT    -> editCanvas.enterEditMode();
                case ERASE   -> editCanvas.enterEraseMode();
            }
        });
    }

    public void init() {
        preview3D.reset();
    }

    public void start() {
        StringBinding titleBinding = createTitleBinding();
        editor.titleProperty().bind(titleBinding);
        stage.titleProperty().bind(titleBinding);
        contentPane.setLeft(null); // no properties editor
        contentPane.requestFocus();
        showEditHelpText();
    }

    public void draw() {
        final WorldMap worldMap = editor.currentWorldMap();
        TerrainMapColorScheme colorScheme = currentColorScheme(worldMap);
        palettes.get(selectedPaletteID()).draw();
        if (tabEditCanvas.isSelected()) {
            editCanvas.draw(colorScheme);
        }
        else if (tabTemplateImage.isSelected()) {
            templateImageCanvas.draw();
        }
        if (tabPreview2D.isSelected()) {
            preview2D.draw(worldMap, colorScheme);
        }
        palettes.values().forEach(Palette::draw);
    }

    public void decideWithCheckForUnsavedChanges(Runnable action) {
        if (!editor.isEdited()) {
            action.run();
            return;
        }
        SaveConfirmation confirmationDialog = new SaveConfirmation();
        confirmationDialog.showAndWait().ifPresent(choice -> {
            if (choice == SaveConfirmation.SAVE_CHANGES) {
                new Action_SaveMapFileInteractively(this).execute();
                action.run();
            } else if (choice == SaveConfirmation.NO_SAVE_CHANGES) {
                editor.setEdited(false);
                action.run();
            } else if (choice == SaveConfirmation.CLOSE) {
                confirmationDialog.close();
            }
        });
    }


    public void showEditHelpText() {
        messageDisplay.showMessage(translated("edit_help"), 30, MessageType.INFO);
    }

    //TODO avoid call in every animation frame
    private TerrainMapColorScheme currentColorScheme(WorldMap worldMap) {
        return new TerrainMapColorScheme(
                COLOR_CANVAS_BACKGROUND,
                getColorFromMap(worldMap, LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_FILL, parseColor(MS_PACMAN_COLOR_WALL_FILL)),
                getColorFromMap(worldMap, LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_STROKE, parseColor(MS_PACMAN_COLOR_WALL_STROKE)),
                getColorFromMap(worldMap, LayerID.TERRAIN, WorldMapProperty.COLOR_DOOR, parseColor(MS_PACMAN_COLOR_DOOR))
        );
    }

    public MessageDisplay messageDisplay() {
        return messageDisplay;
    }

    public Stage stage() {
        return stage;
    }

    public TileMapEditor editor() {
        return editor;
    }

    public BorderPane layoutPane() {
        return layoutPane;
    }

    public EditorMenuBar menuBar() {
        return menuBar;
    }

    public PaletteID selectedPaletteID() {
        return (PaletteID) tabPaneForPalettes.getSelectionModel().getSelectedItem().getUserData();
    }

    public Palette selectedPalette() {
        return palettes.get(selectedPaletteID());
    }

    public void selectTemplateImageTab() {
        tabPaneEditorViews.getSelectionModel().select(tabTemplateImage);
    }

    private void createEditCanvas() {
        editCanvas = new EditCanvas(this);
        editCanvas.editModeProperty().bind(editor.editModeProperty());
        editCanvas.gridSizeProperty().bind(editor.gridSizeProperty());
        editCanvas.gridVisibleProperty().bind(editor.gridVisibleProperty());
        editCanvas.worldMapProperty().bind(editor.currentWorldMapProperty());
        editCanvas.obstacleInnerAreaDisplayedProperty().bind(editor.obstacleInnerAreaDisplayedProperty());
        editCanvas.obstaclesJoiningProperty().bind(editor.obstaclesJoiningProperty());
        editCanvas.segmentNumbersVisibleProperty().bind(editor.segmentNumbersVisibleProperty());
        editCanvas.symmetricEditModeProperty().bind(editor.symmetricEditModeProperty());
        editCanvas.templateImageGrayProperty().bind(editor.templateImageProperty().map(Ufx::imageToGreyscale));
        editCanvas.terrainVisibleProperty().bind(editor.terrainVisibleProperty());
        editCanvas.foodVisibleProperty().bind(editor.foodVisibleProperty());
        editCanvas.actorsVisibleProperty().bind(editor.actorsVisibleProperty());

        editCanvas.obstacleEditor().setOnEditTile(
            (tile, code) -> new Action_SetTileCode(editor, editor.currentWorldMap(), LayerID.TERRAIN, tile, code).execute());
        editCanvas.setOnContextMenuRequested(event -> editCanvas.onContextMenuRequested(event));
        editCanvas.setOnMouseClicked(event -> editCanvas.onMouseClicked(event));
        editCanvas.setOnMouseMoved(event -> editCanvas.onMouseMoved(event));
        editCanvas.setOnMouseReleased(event -> editCanvas.onMouseReleased(event));
        editCanvas.setOnKeyPressed(event -> editCanvas.onKeyPressed(event));

        spEditCanvas = new ScrollPane(editCanvas);
        spEditCanvas.setFitToHeight(true);

        registerDragAndDropImageHandler(spEditCanvas);

        //TODO is there a better way to get the initial resize time of the scroll pane?
        spEditCanvas.heightProperty().addListener((py,oldHeight,newHeight) -> {
            if (oldHeight.doubleValue() == 0) { // initial resize
                int initialGridSize = (int) Math.max(newHeight.doubleValue() / editor.currentWorldMap().numRows(), MIN_GRID_SIZE);
                editor.setGridSize(initialGridSize);
            }
        });
    }

    private void createPreview2D() {
        preview2D = new Preview2D();
        preview2D.widthProperty().bind(editCanvas.widthProperty());
        preview2D.heightProperty().bind(editCanvas.heightProperty());
        preview2D.gridSizeProperty().bind(editor.gridSizeProperty());
        preview2D.terrainVisibleProperty().bind(editor.terrainVisibleProperty());
        preview2D.foodVisibleProperty().bind(editor.foodVisibleProperty());
        preview2D.actorsVisibleProperty().bind(editor.actorsVisibleProperty());

        spPreview2D = new ScrollPane(preview2D);
        spPreview2D.setFitToHeight(true);
        spPreview2D.hvalueProperty().bindBidirectional(spEditCanvas.hvalueProperty());
        spPreview2D.vvalueProperty().bindBidirectional(spEditCanvas.vvalueProperty());
    }

    private void createPreview3D(Model3DRepository model3DRepository) {
        preview3D = new Preview3D(editor, model3DRepository, 500, 500);
        preview3D.foodVisibleProperty().bind(editor.foodVisibleProperty());
        preview3D.terrainVisibleProperty().bind(editor.terrainVisibleProperty());
        preview3D.worldMapProperty().bind(editor.currentWorldMapProperty());
    }

    private void createTemplateImageCanvas() {
        templateImageCanvas = new TemplateImageCanvas(editor);
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
        sourceView.setStyle(STYLE_SOURCE_VIEW);
        sourceView.textProperty().bind(editor.sourceCodeProperty());
    }

    private void createEditArea() {
        createEditCanvas();
        createTemplateImageCanvas();

        tabEditCanvas = new Tab(translated("tab_editor"), spEditCanvas);

        var dropHintButton = new Button(translated("image_drop_hint"));
        dropHintButton.setFont(FONT_DROP_HINT);
        dropHintButton.setOnAction(ae -> new Action_OpenTemplateCreateMap(this).execute());
        dropHintButton.disableProperty().bind(editor.editModeProperty().map(mode -> mode == EditMode.INSPECT));

        templateImageDropTarget = new BorderPane(dropHintButton);
        registerDragAndDropImageHandler(templateImageDropTarget);

        var stackPane = new StackPane(spTemplateImage, templateImageDropTarget);
        tabTemplateImage = new Tab(translated("tab_template_image"), stackPane);
        editor.templateImageProperty().addListener((py, ov, image) -> {
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
                if (isImageFile(file) && !editor.editModeIs(EditMode.INSPECT) || isWorldMapFile(file)) {
                    dragEvent.acceptTransferModes(TransferMode.COPY);
                }
            }
            dragEvent.consume();
        });
        node.setOnDragDropped(dragEvent -> {
            if (dragEvent.getDragboard().hasFiles()) {
                File file = dragEvent.getDragboard().getFiles().getFirst();
                decideWithCheckForUnsavedChanges(() -> editCanvas.onFileDropped(file));
            }
            dragEvent.consume();
        });
    }

    private void createPreviewArea(Model3DRepository model3DRepository) {
        createPreview2D();
        createPreview3D(model3DRepository);
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

        splitPaneMapEditorAndPreviews = new SplitPane(tabPaneEditorViews, tabPane);
        splitPaneMapEditorAndPreviews.setDividerPositions(0.5);
    }

    // Must be called after edit canvas creation because it binds to the renderers of the edit canvas!
    private void createPalettes(EditCanvas editCanvas) {
        palettes.put(PaletteID.TERRAIN, createTerrainPalette(editCanvas.terrainRenderer()));
        palettes.put(PaletteID.FOOD, createFoodPalette(editCanvas.foodRenderer()));
        palettes.put(PaletteID.ACTORS, createActorsPalette(editCanvas.terrainRenderer()));

        var tabTerrain = new Tab("", palettes.get(PaletteID.TERRAIN).root());
        tabTerrain.setGraphic(new Text(translated("terrain")));
        tabTerrain.setClosable(false);
        tabTerrain.setUserData(PaletteID.TERRAIN);

        var tabPellets = new Tab("", palettes.get(PaletteID.FOOD).root());
        tabPellets.setGraphic(new Text(translated("pellets")));
        tabPellets.setClosable(false);
        tabPellets.setUserData(PaletteID.FOOD);

        var tabActors = new Tab("", palettes.get(PaletteID.ACTORS).root());
        tabActors.setGraphic(new Text(translated("actors")));
        tabActors.setClosable(false);
        tabActors.setUserData(PaletteID.ACTORS);

        tabPaneForPalettes = new TabPane(tabTerrain, tabPellets, tabActors);
        tabPaneForPalettes.setPadding(new Insets(5, 5, 5, 5));
        tabPaneForPalettes.setMinHeight(75);

        tabPaneForPalettes.getSelectionModel().selectedItemProperty().addListener((py, ov, selectedTab) -> {
            editor.setPaletteID((PaletteID) selectedTab.getUserData());
            updatePalettesTabPaneDisplay(selectedTab);
        });

        updatePalettesTabPaneDisplay(tabPaneForPalettes.getSelectionModel().getSelectedItem());
    }

    private void updatePalettesTabPaneDisplay(Tab selectedTab) {
        for (Tab tab : tabPaneForPalettes.getTabs()) {
            if (tab.getGraphic() instanceof Text text) {
                text.setFont(tab == selectedTab ? FONT_SELECTED_PALETTE : FONT_UNSELECTED_PALETTE);
            }
        }
    }

    private Palette createTerrainPalette(TerrainMapRenderer renderer) {
        var palette = new Palette(PaletteID.TERRAIN, TOOL_SIZE, 1, 13);
        palette.addTool(makeTileTool(TerrainTile.EMPTY.$, "Empty Space"));
        palette.addTool(makeTileTool(TerrainTile.WALL_H.$, "Horizontal Wall"));
        palette.addTool(makeTileTool(TerrainTile.WALL_V.$, "Vertical Wall"));
        palette.addTool(makeTileTool(TerrainTile.ARC_NW.$, "NW Corner"));
        palette.addTool(makeTileTool(TerrainTile.ARC_NE.$, "NE Corner"));
        palette.addTool(makeTileTool(TerrainTile.ARC_SW.$, "SW Corner"));
        palette.addTool(makeTileTool(TerrainTile.ARC_SE.$, "SE Corner"));
        palette.addTool(makeTileTool(TerrainTile.TUNNEL.$, "Tunnel"));
        palette.addTool(makeTileTool(TerrainTile.DOOR.$, "Door"));
        palette.addTool(makeTileTool(TerrainTile.ONE_WAY_UP.$, "One-Way Up"));
        palette.addTool(makeTileTool(TerrainTile.ONE_WAY_RIGHT.$, "One-Way Right"));
        palette.addTool(makeTileTool(TerrainTile.ONE_WAY_DOWN.$, "One-Way Down"));
        palette.addTool(makeTileTool(TerrainTile.ONE_WAY_LEFT.$, "One-Way Left"));

        palette.selectTool(0); // "No Tile"

        TerrainTileMapRenderer paletteRenderer = new TerrainTileMapRenderer(palette.canvas());
        paletteRenderer.backgroundColorProperty().bind(renderer.backgroundColorProperty());
        paletteRenderer.colorSchemeProperty().bind(renderer.colorSchemeProperty());
        palette.setRenderer(paletteRenderer);

        return palette;
    }

    private Palette createActorsPalette(TerrainTileMapRenderer renderer) {
        var palette = new Palette(PaletteID.ACTORS, TOOL_SIZE, 1, 11);
        palette.addTool(makeTileTool(TerrainTile.EMPTY.$, "Nope"));
        palette.addTool(makePropertyTool(WorldMapProperty.POS_PAC, "Pac-Man"));
        palette.addTool(makePropertyTool(WorldMapProperty.POS_RED_GHOST, "Red Ghost"));
        palette.addTool(makePropertyTool(WorldMapProperty.POS_PINK_GHOST, "Pink Ghost"));
        palette.addTool(makePropertyTool(WorldMapProperty.POS_CYAN_GHOST, "Cyan Ghost"));
        palette.addTool(makePropertyTool(WorldMapProperty.POS_ORANGE_GHOST, "Orange Ghost"));
        palette.addTool(makePropertyTool(WorldMapProperty.POS_BONUS, "Bonus"));
        palette.addTool(makePropertyTool(WorldMapProperty.POS_SCATTER_RED_GHOST, "Red Ghost Scatter"));
        palette.addTool(makePropertyTool(WorldMapProperty.POS_SCATTER_PINK_GHOST, "Pink Ghost Scatter"));
        palette.addTool(makePropertyTool(WorldMapProperty.POS_SCATTER_CYAN_GHOST, "Cyan Ghost Scatter"));
        palette.addTool(makePropertyTool(WorldMapProperty.POS_SCATTER_ORANGE_GHOST, "Orange Ghost Scatter"));
        palette.selectTool(0); // "No actor"

        TerrainTileMapRenderer paletteRenderer = new TerrainTileMapRenderer(palette.canvas());
        paletteRenderer.backgroundColorProperty().bind(renderer.backgroundColorProperty());
        paletteRenderer.colorSchemeProperty().bind(renderer.colorSchemeProperty());
        palette.setRenderer(paletteRenderer);

        return palette;
    }

    private Palette createFoodPalette(FoodMapRenderer renderer) {
        var palette = new Palette(PaletteID.FOOD, TOOL_SIZE, 1, 3);
        palette.addTool(makeTileTool(FoodTile.EMPTY.code(), "No Food"));
        palette.addTool(makeTileTool(FoodTile.PELLET.code(), "Pellet"));
        palette.addTool(makeTileTool(FoodTile.ENERGIZER.code(), "Energizer"));
        palette.selectTool(0); // "No Food"

        FoodMapRenderer foodRenderer = new FoodMapRenderer(palette.canvas());
        foodRenderer.backgroundColorProperty().bind(renderer.backgroundColorProperty());
        foodRenderer.energizerColorProperty().bind(renderer.energizerColorProperty());
        foodRenderer.pelletColorProperty().bind(renderer.pelletColorProperty());
        palette.setRenderer(foodRenderer);

        return palette;
    }

    private TileValueEditorTool makeTileTool(byte code, String description) {
        return new TileValueEditorTool(
            (layerID, tile) -> new Action_SetTileCode(editor, editor.currentWorldMap(), layerID, tile, code).execute(),
            TOOL_SIZE, code, description);
    }

    protected PropertyValueEditorTool makePropertyTool(String propertyName, String description) {
        return new PropertyValueEditorTool(
            (layerID, tile) -> {
                editor.currentWorldMap().properties(layerID).put(propertyName, formatTile(tile));
                editor.setEdited(true);
            },
            TOOL_SIZE, propertyName, description);
    }

    private void createPropertyEditors() {
        terrainMapPropertiesEditor = new PropertyEditorPane(this);
        terrainMapPropertiesEditor.enabledPy.bind(editor.editModeProperty().map(mode -> mode != EditMode.INSPECT));
        terrainMapPropertiesEditor.setPadding(new Insets(10,0,0,0));

        foodMapPropertiesEditor = new PropertyEditorPane(this);
        foodMapPropertiesEditor.enabledPy.bind(editor.editModeProperty().map(mode -> mode != EditMode.INSPECT));
        foodMapPropertiesEditor.setPadding(new Insets(10,0,0,0));

        var terrainPropertiesPane = new TitledPane(translated("terrain"), terrainMapPropertiesEditor);
        terrainPropertiesPane.setMinWidth(300);
        terrainPropertiesPane.setExpanded(true);

        var foodPropertiesPane = new TitledPane(translated("pellets"), foodMapPropertiesEditor);
        foodPropertiesPane.setExpanded(true);

        propertyEditorsPane = new VBox(terrainPropertiesPane, foodPropertiesPane);
        propertyEditorsPane.visibleProperty().bind(editor.propertyEditorsVisibleProperty());
    }

    private void createZoomControl() {
        sliderZoom = new Slider(MIN_GRID_SIZE, MAX_GRID_SIZE, 0.5 * (MIN_GRID_SIZE + MAX_GRID_SIZE));
        sliderZoom.setShowTickLabels(false);
        sliderZoom.setShowTickMarks(true);
        sliderZoom.setPrefWidth(120);
        Bindings.bindBidirectional(sliderZoom.valueProperty(), editor.gridSizeProperty());
        Tooltip tt = new Tooltip();
        tt.setShowDelay(Duration.millis(50));
        tt.setFont(Font.font(14));
        tt.textProperty().bind(editor.gridSizeProperty().map("Grid Size: %.0f"::formatted));
        sliderZoom.setTooltip(tt);
    }

    private void createStatusLine() {
        var lblMapSize = new Label();
        lblMapSize.setFont(FONT_STATUS_LINE_NORMAL);
        lblMapSize.textProperty().bind(editor.currentWorldMapProperty().map(worldMap -> (worldMap != null)
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
                messageDisplay,
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
            label.setMinWidth(90);
            label.setFont(FONT_STATUS_LINE_EDIT_MODE);
            label.setEffect(new Glow(0.2));
            getChildren().add(label);

            label.textProperty().bind(Bindings.createStringBinding(
                    () -> switch (editor.editMode()) {
                        case INSPECT -> translated("mode.inspect");
                        case EDIT    -> translated(editor.symmetricEditMode() ? "mode.symmetric" : "mode.edit");
                        case ERASE   -> translated("mode.erase");
                    }, editor.editModeProperty(), editor.symmetricEditModeProperty()
            ));

            label.textFillProperty().bind(editor.editModeProperty().map(
                    mode -> switch (mode) {
                        case INSPECT -> Color.GRAY;
                        case EDIT    -> Color.FORESTGREEN;
                        case ERASE   -> Color.RED;
                    }));

            label.setOnMouseClicked(e -> new Action_SelectNextEditMode(editor).execute());
        }
    }

    private void arrangeLayout() {
        var centerPane = new VBox(tabPaneForPalettes, splitPaneMapEditorAndPreviews, statusLine);
        centerPane.setPadding(new Insets(0,5,0,5));
        VBox.setVgrow(tabPaneForPalettes, Priority.NEVER);
        VBox.setVgrow(splitPaneMapEditorAndPreviews, Priority.ALWAYS);
        VBox.setVgrow(statusLine, Priority.NEVER);
        contentPane.setLeft(propertyEditorsPane);
        contentPane.setCenter(centerPane);
        layoutPane.setTop(menuBar);
        layoutPane.setCenter(contentPane);
    }

    private StringBinding createTitleBinding() {
        return Bindings.createStringBinding(() -> {
                File mapFile = editor.currentFile();
                if (mapFile != null) {
                    return "%s: [%s] - %s".formatted( translated("map_editor"), mapFile.getName(), mapFile.getPath() );
                }
                final WorldMap worldMap = editor.currentWorldMap();
                if (worldMap == null) {
                    return "No Map"; // TODO can this ever happen?
                }
                if (worldMap.url() != null) {
                    return  "%s: [%s]".formatted( translated("map_editor"), worldMap.url() );
                }
                return "%s: [%s: %d rows %d cols]".formatted(
                    translated("map_editor"), translated("unsaved_map"), worldMap.numRows(), worldMap.numCols() );
            }, editor.currentFileProperty(), editor.currentWorldMapProperty()
        );
    }

    // also called from EditorPage
    public MenuItem createLoadMapMenuItem(String description, WorldMap worldMap) {
        requireNonNull(description);
        requireNonNull(worldMap);
        var menuItem = new MenuItem(description);
        menuItem.setOnAction(e -> {
            WorldMap copy = WorldMap.copyMap(worldMap);
            decideWithCheckForUnsavedChanges(() -> editor.setCurrentWorldMap(copy));
        });
        return menuItem;
    }

    public void addSampleMapMenuEntries(TileMapEditor.SampleMaps maps) {
        Menu menu = menuBar.menuMaps();
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
    }

    // Event handlers

    public void onKeyPressed(KeyEvent e) {
        KeyCode key = e.getCode();
        boolean alt = e.isAltDown();
        if (alt && key == KeyCode.LEFT) {
            new Action_SelectNextMapFile(editor, false).execute();
        }
        else if (alt && key == KeyCode.RIGHT) {
            new Action_SelectNextMapFile(editor, true).execute();
        }
        else if (key == KeyCode.PLUS) {
            new Action_ZoomIn(editor).execute();
        }
        else if (key == KeyCode.MINUS) {
            new Action_ZoomOut(editor).execute();
        }
    }

    public void onKeyTyped(KeyEvent e) {
        String ch = e.getCharacter();
        if (ch.equals("e")) {
            new Action_SelectNextEditMode(editor).execute();
        }
    }

    // Model change handling

    public void onTerrainMapChanged(WorldMap worldMap) {
        if (terrainMapPropertiesEditor != null) {
            terrainMapPropertiesEditor.setTileMap(worldMap, LayerID.TERRAIN);
        }
        preview3D.updateTerrain();
    }

    public void onFoodMapChanged(WorldMap worldMap) {
        if (foodMapPropertiesEditor != null) {
            foodMapPropertiesEditor.setTileMap(worldMap, LayerID.FOOD);
        }
        preview3D.updateFood();
    }
}