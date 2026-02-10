/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor;

import de.amr.pacmanfx.mapeditor.actions.*;
import de.amr.pacmanfx.mapeditor.palette.Palette;
import de.amr.pacmanfx.mapeditor.palette.PaletteID;
import de.amr.pacmanfx.mapeditor.properties.MapLayerPropertiesEditor;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapLayer;
import de.amr.pacmanfx.model.world.WorldMapLayerID;
import de.amr.pacmanfx.model.world.WorldMapPropertyName;
import de.amr.pacmanfx.uilib.model3D.PacManModel3DRepository;
import de.amr.pacmanfx.uilib.rendering.TerrainMapColorScheme;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.mapeditor.EditMode.INSPECT;
import static de.amr.pacmanfx.mapeditor.EditorGlobals.*;
import static de.amr.pacmanfx.mapeditor.rendering.ArcadeSprites.*;
import static java.util.Objects.requireNonNull;

public class TileMapEditorUI {

    private final TileMapEditor editor;

    private final Stage stage;

    private final EditorMenuBar menuBar;
    private final BorderPane layoutPane = new BorderPane();
    private final MessageDisplay messageDisplay = new MessageDisplay();

    private final BorderPane contentPane = new BorderPane();
    private SplitPane splitPaneMapEditorAndPreviews;

    private final EditorPaletteTabPane editorPaletteTabPane;

    private Pane propertyEditorsPane;
    private MapLayerPropertiesEditor terrainPropertiesEditor;
    private MapLayerPropertiesEditor foodPropertiesEditor;

    private TabPane tabPaneEditorViews;
    private Tab tabEditCanvas;
    private Tab tabTemplateImage;

    private ScrollPane spEditCanvas;
    private EditCanvas editCanvas;

    private TemplateImageCanvas templateImageCanvas;
    private Pane templateImageDropTarget;
    private ScrollPane spTemplateImage;

    private Tab tabPreview2D;
    private ScrollPane spPreview2D;
    private Preview2D preview2D;

    private Preview3D preview3D;

    private TextArea sourceView;
    private ContextMenu sourceViewContextMenu;

    private HBox statusLine;
    private Slider sliderZoom;

    // -- actorsVisible

    public boolean DEFAULT_ACTORS_VISIBLE = true;

    private BooleanProperty actorsVisible;

    public BooleanProperty actorsVisibleProperty() {
        if (actorsVisible == null) {
            actorsVisible = new SimpleBooleanProperty(DEFAULT_ACTORS_VISIBLE);
        }
        return actorsVisible;
    }

    public boolean actorsVisible() {
        return actorsVisible == null ? DEFAULT_ACTORS_VISIBLE : actorsVisibleProperty().get();
    }

    public void setActorsVisible(boolean visible) {
        actorsVisibleProperty().set(visible);
    }

    // -- inputEnabled

    private final BooleanProperty inputEnabled = new SimpleBooleanProperty(true);

    public BooleanProperty inputEnabledProperty() {
        return inputEnabled;
    }

    public void setInputEnabled(boolean enabled) {
        inputEnabled.set(enabled);
    }

    public boolean inputEnabled() {
        return inputEnabled.get();
    }

    // -- editMode

    public static final EditMode DEFAULT_EDIT_MODE = INSPECT;

    private ObjectProperty<EditMode> editMode;

    public ObjectProperty<EditMode> editModeProperty() {
        if (editMode == null) {
            editMode = new SimpleObjectProperty<>(DEFAULT_EDIT_MODE);
        }
        return editMode;
    }

    public EditMode editMode() { return editMode == null ? DEFAULT_EDIT_MODE : editModeProperty().get(); }

    public void setEditMode(EditMode mode) {
        editModeProperty().set(requireNonNull(mode));
    }

    public boolean editModeIs(EditMode mode) { return editMode() == mode; }

    // -- foodVisible

    public static final boolean DEFAULT_FOOD_VISIBLE = true;

    private BooleanProperty foodVisible;

    public BooleanProperty foodVisibleProperty() {
        if (foodVisible == null) {
            foodVisible = new SimpleBooleanProperty(DEFAULT_FOOD_VISIBLE);
        }
        return foodVisible;
    }

    public boolean foodVisible() {
        return foodVisible == null ? DEFAULT_FOOD_VISIBLE : foodVisible.get();
    }

    public void setFoodVisible(boolean visible) {
        foodVisibleProperty().set(visible);
    }

    // -- gridSize

    private static final double DEFAULT_GRID_SIZE = 16;

    private DoubleProperty gridSize;

    public DoubleProperty gridSizeProperty() {
        if (gridSize == null) {
            gridSize = new SimpleDoubleProperty(DEFAULT_GRID_SIZE);
        }
        return gridSize;
    }

    public double gridSize() { return gridSize.get(); }

    public void setGridSize(double size) {
        gridSizeProperty().set(size);
    }

    // -- gridVisible

    public static final boolean DEFAULT_GRID_VISIBLE = true;

    private BooleanProperty gridVisible;

    public BooleanProperty gridVisibleProperty() {
        if (gridVisible == null) {
            gridVisible = new SimpleBooleanProperty(DEFAULT_GRID_VISIBLE);
        }
        return gridVisible;
    }

    // -- obstacleInnerAreaDisplayed

    public static final boolean DEFAULT_OBSTACLE_INNER_AREA_DISPLAYED = false;

    private BooleanProperty obstacleInnerAreaDisplayed;

    public BooleanProperty obstacleInnerAreaDisplayedProperty() {
        if (obstacleInnerAreaDisplayed == null) {
            obstacleInnerAreaDisplayed = new SimpleBooleanProperty(DEFAULT_OBSTACLE_INNER_AREA_DISPLAYED);
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

    // -- propertyEditorsVisible

    public static final boolean DEFAULT_PROPERTY_EDITORS_VISIBLE = false;

    private BooleanProperty mapPropertyEditorsVisible;

    public BooleanProperty propertyEditorsVisibleProperty() {
        if (mapPropertyEditorsVisible == null) {
            mapPropertyEditorsVisible = new SimpleBooleanProperty(DEFAULT_PROPERTY_EDITORS_VISIBLE);
        }
        return mapPropertyEditorsVisible;
    }

    public boolean propertyEditorsVisible() {
        return mapPropertyEditorsVisible == null ? DEFAULT_PROPERTY_EDITORS_VISIBLE : propertyEditorsVisibleProperty().get();
    }

    public void setPropertyEditorsVisible(boolean value) {
        propertyEditorsVisibleProperty().set(value);
    }


    // -- segmentNumbersVisible

    public static final boolean DEFAULT_SEGMENT_NUMBERS_VISIBLE = false;

    private BooleanProperty segmentNumbersVisible;

    public BooleanProperty segmentNumbersVisibleProperty() {
        if (segmentNumbersVisible == null) {
            segmentNumbersVisible = new SimpleBooleanProperty(DEFAULT_SEGMENT_NUMBERS_VISIBLE);
        }
        return segmentNumbersVisible;
    }

    public boolean segmentNumbersVisible() {
        return segmentNumbersVisible == null ? DEFAULT_SEGMENT_NUMBERS_VISIBLE : segmentNumbersVisibleProperty().get();
    }

    public void setSegmentNumbersVisible(boolean value) {
        segmentNumbersVisibleProperty().set(value);
    }

    // -- terrainVisible

    public static final boolean DEFAULT_TERRAIN_VISIBLE = true;

    private BooleanProperty terrainVisible;

    public BooleanProperty terrainVisibleProperty() {
        if (terrainVisible == null) {
            terrainVisible = new SimpleBooleanProperty(DEFAULT_TERRAIN_VISIBLE);
        }
        return terrainVisible;
    }

    public boolean terrainVisible() {
        return terrainVisible == null ? DEFAULT_TERRAIN_VISIBLE : terrainVisible.get();
    }

    public void setTerrainVisible(boolean visible) {
        terrainVisibleProperty().set(visible);
    }


    // -- title

    private final StringProperty title = new SimpleStringProperty("Tile Map Editor");

    public StringProperty titleProperty() { return title; }

    // end of property section

    public TileMapEditorUI(Stage stage, TileMapEditor editor, PacManModel3DRepository model3DRepository) {
        this.stage = requireNonNull(stage);
        this.editor = editor;

        createEditArea();
        createPreviewArea(model3DRepository);
        createPropertyEditors();
        createStatusLine();
        editorPaletteTabPane = new EditorPaletteTabPane(this, editCanvas.terrainRenderer(), editCanvas.foodRenderer());
        menuBar = new EditorMenuBar(this);
        arrangeLayout();

        contentPane.setOnKeyTyped(keyEvent -> {
            if (inputEnabled()) onKeyTyped(keyEvent);
        });

        contentPane.setOnKeyPressed(keyEvent -> {
            if (inputEnabled()) onKeyPressed(keyEvent);
        });

        propertyEditorsVisibleProperty().addListener((_, _, visible) ->
            contentPane.setLeft(visible ? propertyEditorsPane : null));

        editModeProperty().addListener((_, _, newEditMode) -> {
            messageDisplay().clearMessage();
            showEditHelpText();
            switch (newEditMode) {
                case INSPECT -> editCanvas.enterInspectMode();
                case EDIT    -> editCanvas.enterEditMode();
                case ERASE   -> editCanvas.enterEraseMode();
            }
        });

        inputEnabled.bind(editCanvas.draggingProperty().not());

        actionBindings.put("e", new Action_SelectNextEditMode(this));
        actionBindings.put("p", new Action_TogglePropertyEditor(this));
        actionBindings.put("q", new Action_QuitEditor(this));

        stage.setOnCloseRequest(this::handleCloseEvent);
    }

    private void handleCloseEvent(WindowEvent e) {
        if (editor.isEdited()) {
            SaveConfirmationDialog saveDialog = new SaveConfirmationDialog();
            saveDialog.showAndWait().ifPresent(choice -> {
                if (choice == SaveConfirmationDialog.SAVE) {
                    File selectedFile = new Action_SaveMapFileInteractively(this).execute();
                    if (selectedFile == null) { // File selection was canceled
                        e.consume();
                    }
                }
                else if (choice == SaveConfirmationDialog.DONT_SAVE) {
                    editor.setEdited(false);
                }
                else if (choice == ButtonType.CANCEL) {
                    e.consume();
                }
            });
        }
    }

    public void init() {
        replaceSampleMapMenuEntries(editor.sampleMaps());
        preview3D.reset();
        setEditMode(INSPECT);
        Platform.runLater(() -> {
            double height = spEditCanvas.getHeight();
            int gridSize = (int) Math.max(height / editor.currentWorldMap().numRows(), DEFAULT_GRID_SIZE);
            setGridSize(gridSize);
        });
    }

    public void start() {
        StringBinding titleBinding = createTitleBinding();
        titleProperty().bind(titleBinding);
        stage.titleProperty().bind(titleBinding);
        contentPane.setLeft(null); // no properties editor
        contentPane.requestFocus();
        showEditHelpText();
    }

    public void draw() {
        editorPaletteTabPane.selectedPalette().ifPresent(Palette::draw);
        final WorldMap worldMap = editor.currentWorldMap();
        TerrainMapColorScheme colorScheme = currentColorScheme(worldMap);
        if (tabEditCanvas.isSelected()) {
            editCanvas.draw(colorScheme);
        }
        else if (tabTemplateImage.isSelected()) {
            templateImageCanvas.draw();
        }
        if (tabPreview2D.isSelected()) {
            preview2D.draw(worldMap, colorScheme);
        }
    }

    public void afterCheckForUnsavedChanges(Runnable action) {
        if (!editor.isEdited()) {
            action.run();
            return;
        }
        SaveConfirmationDialog confirmationDialog = new SaveConfirmationDialog();
        confirmationDialog.showAndWait().ifPresent(choice -> {
            if (choice == SaveConfirmationDialog.SAVE) {
                new Action_SaveMapFileInteractively(this).execute();
                action.run();
            } else if (choice == SaveConfirmationDialog.DONT_SAVE) {
                editor.setEdited(false);
                action.run();
            } else if (choice == ButtonType.CANCEL) {
                confirmationDialog.close();
            }
        });
    }

    public void showEditHelpText() {
        messageDisplay.showMessage(translated("edit_help"), 30, MessageType.INFO);
    }

    //TODO avoid call in every animation frame
    private TerrainMapColorScheme currentColorScheme(WorldMap worldMap) {
        WorldMapLayer terrain = worldMap.terrainLayer();
        return new TerrainMapColorScheme(
            COLOR_CANVAS_BACKGROUND,
            UfxMapEditor.getColorFromMapLayer(terrain, WorldMapPropertyName.COLOR_WALL_FILL, MS_PACMAN_COLOR_WALL_FILL),
            UfxMapEditor.getColorFromMapLayer(terrain, WorldMapPropertyName.COLOR_WALL_STROKE, MS_PACMAN_COLOR_WALL_STROKE),
            UfxMapEditor.getColorFromMapLayer(terrain, WorldMapPropertyName.COLOR_DOOR, MS_PACMAN_COLOR_DOOR)
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

    public void selectTemplateImageTab() {
        tabPaneEditorViews.getSelectionModel().select(tabTemplateImage);
    }

    public void selectEditCanvasTab() {
        tabPaneEditorViews.getSelectionModel().select(tabEditCanvas);
    }

    public Optional<PaletteID> selectedPaletteID() {
        return editorPaletteTabPane.selectedPalette().map(Palette::id);
    }

    public Optional<Palette> selectedPalette() {
        return editorPaletteTabPane.selectedPalette();
    }

    private void createEditCanvas() {
        editCanvas = new EditCanvas(this);

        spEditCanvas = new ScrollPane(editCanvas);
        spEditCanvas.setFitToHeight(true);
        registerDragAndDropImageHandler(spEditCanvas);
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

    private void createPreview3D(PacManModel3DRepository model3DRepository) {
        preview3D = new Preview3D(this, model3DRepository, 500, 500);
        preview3D.foodVisibleProperty().bind(foodVisibleProperty());
        preview3D.terrainVisibleProperty().bind(terrainVisibleProperty());
        preview3D.worldMapProperty().bind(editor.currentWorldMapProperty());
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
        sourceView.setStyle(STYLE_SOURCE_VIEW);
        sourceView.textProperty().bind(editor.sourceCodeProperty());
        sourceView.setOnContextMenuRequested(e -> {
            if (sourceViewContextMenu == null) {
                sourceViewContextMenu = new ContextMenu();
                // overwrite style of TextArea
                sourceViewContextMenu.setStyle("""
                        -fx-background-color: white;
                        -fx-text-fill: black;
                        -fx-font-size: 12px;
                        """);
                CheckMenuItem item = new CheckMenuItem(translated("sourceCodeView.menu.showLineNumbers"));
                item.selectedProperty().bindBidirectional(editor.sourceCodeLineNumbers());
                sourceViewContextMenu.getItems().add(item);
            } else if (sourceViewContextMenu.isShowing()) {
                sourceViewContextMenu.hide();
            }
            sourceViewContextMenu.show(sourceView, e.getScreenX(), e.getScreenY());
        });
    }

    private void createEditArea() {
        createEditCanvas();
        createTemplateImageCanvas();

        tabEditCanvas = new Tab(translated("tab_editor"), spEditCanvas);

        var dropHintButton = new Button(translated("image_drop_hint"));
        dropHintButton.setFont(FONT_DROP_HINT);
        dropHintButton.setOnAction(_ -> new Action_OpenTemplateCreateMap(this).execute());
        dropHintButton.disableProperty().bind(editModeProperty().map(mode -> mode == EditMode.INSPECT));

        templateImageDropTarget = new BorderPane(dropHintButton);
        registerDragAndDropImageHandler(templateImageDropTarget);

        var stackPane = new StackPane(spTemplateImage, templateImageDropTarget);
        tabTemplateImage = new Tab(translated("tab_template_image"), stackPane);
        editor.templateImageProperty().addListener((_, ov, image) -> {
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
                if (isImageFile(file) && !editModeIs(EditMode.INSPECT) || isWorldMapFile(file)) {
                    dragEvent.acceptTransferModes(TransferMode.COPY);
                }
            }
            dragEvent.consume();
        });
        node.setOnDragDropped(dragEvent -> {
            if (dragEvent.getDragboard().hasFiles()) {
                File file = dragEvent.getDragboard().getFiles().getFirst();
                afterCheckForUnsavedChanges(() -> onFileDropped(file));
            }
            dragEvent.consume();
        });
    }

    private void onFileDropped(File file) {
        if (isWorldMapFile(file)) {
            new Action_ReplaceCurrentWorldMapChecked(this, file).execute();
        }
        else if (isImageFile(file) && !editModeIs(EditMode.INSPECT)) {
            Image image = new Image(file.toURI().toString());
            if (!isTemplateImageSizeOk(image)) {
                messageDisplay().showMessage("Template image file '%s' has dubios size".formatted(file), 3, MessageType.ERROR);
                return;
            }
            editor().setTemplateImage(image);
            new Action_SetEmptyMapFromTemplateImage(this, image).execute();
        }
    }

    private boolean isTemplateImageSizeOk(Image image) {
        return image.getHeight() % TS == 0 && image.getWidth() % TS == 0;
    }

    private Node createPreview3DNavigationHint() {
        var text = new Label();
        String css = """
                -fx-padding: 10;
                -fx-background-color: #80808064;
                -fx-background-radius: 10;
                -fx-border-color: #ccc;
                -fx-border-width: 1;
                -fx-border-radius: 10;
                """;
        text.setStyle(css);
        text.setText( translated("preview3D.navigation_hint.no_focus"));
        text.setTextAlignment(TextAlignment.CENTER);
        text.setTextFill(COLOR_PREVIEW_3D_OVERLAY);
        text.setFont(FONT_PREVIEW_3D_OVERLAY);
        text.setMouseTransparent(true);

        //TODO There is still an issue: While the mouse is pressed inside the 3D view, the unfocused text is displayed!
        preview3D.subScene().focusedProperty().addListener((_, _, focused) ->
            text.setText(translated(focused ? "preview3D.navigation_hint" : "preview3D.navigation_hint.no_focus")));

        StackPane.setAlignment(text, Pos.TOP_CENTER);
        StackPane.setMargin(text, new Insets(20,0,0,0));
        return text;
    }

    private void createPreviewArea(PacManModel3DRepository model3DRepository) {
        createPreview2D();
        createPreview3D(model3DRepository);
        createSourceView();

        tabPreview2D = new Tab(translated("preview2D"), spPreview2D);

        var navigationHint = createPreview3DNavigationHint();
        var preview3DPane = new StackPane(preview3D.subScene(), navigationHint);

        Tab tabPreview3D = new Tab(translated("preview3D"), preview3DPane);

        Tab tabSourceView = new Tab(translated("source"), sourceView);

        TabPane tabPane = new TabPane(tabPreview2D, tabPreview3D, tabSourceView);
        tabPane.setSide(Side.BOTTOM);
        tabPane.getTabs().forEach(tab -> tab.setClosable(false));
        tabPane.getSelectionModel().select(tabPreview2D);

        // Let 3D preview sub-scene fill the complete tab content
        preview3D.subScene().widthProperty().bind(tabPane.widthProperty());
        preview3D.subScene().heightProperty().bind(tabPane.heightProperty());

        splitPaneMapEditorAndPreviews = new SplitPane(tabPaneEditorViews, tabPane);
        splitPaneMapEditorAndPreviews.setDividerPositions(0.5);
    }

    private void createPropertyEditors() {
        terrainPropertiesEditor = new MapLayerPropertiesEditor(this, WorldMapLayerID.TERRAIN);
        terrainPropertiesEditor.enabledProperty().bind(editModeProperty().map(mode -> mode != EditMode.INSPECT));
        terrainPropertiesEditor.setPadding(new Insets(10,0,0,0));

        foodPropertiesEditor = new MapLayerPropertiesEditor(this, WorldMapLayerID.FOOD);
        foodPropertiesEditor.enabledProperty().bind(editModeProperty().map(mode -> mode != EditMode.INSPECT));
        foodPropertiesEditor.setPadding(new Insets(10,0,0,0));

        var terrainPropertiesPane = new TitledPane(translated("terrain"), terrainPropertiesEditor);
        terrainPropertiesPane.setMinWidth(300);
        terrainPropertiesPane.setExpanded(true);

        var foodPropertiesPane = new TitledPane(translated("pellets"), foodPropertiesEditor);
        foodPropertiesPane.setExpanded(true);

        propertyEditorsPane = new VBox(terrainPropertiesPane, foodPropertiesPane);
        propertyEditorsPane.visibleProperty().bind(propertyEditorsVisibleProperty());
    }

    private void createZoomControl() {
        sliderZoom = new Slider(MIN_GRID_SIZE, MAX_GRID_SIZE, 0.5 * (MIN_GRID_SIZE + MAX_GRID_SIZE));
        sliderZoom.setShowTickLabels(false);
        sliderZoom.setShowTickMarks(true);
        sliderZoom.setPrefWidth(160);
        Bindings.bindBidirectional(sliderZoom.valueProperty(), gridSizeProperty());
        Tooltip tt = new Tooltip();
        tt.setShowDelay(Duration.millis(50));
        tt.setFont(FONT_TOOL_TIPS);
        tt.textProperty().bind(gridSizeProperty().map("Grid Size: %.0f"::formatted));
        sliderZoom.setTooltip(tt);
    }

    private void createStatusLine() {
        var lblMapSize = new Label();
        lblMapSize.setFont(FONT_STATUS_LINE_NORMAL);
        lblMapSize.textProperty().bind(editor.currentWorldMapProperty().map(worldMap -> (worldMap != null)
                ? "H: %d tiles V: %d tiles".formatted(worldMap.numCols(), worldMap.numRows()) : "")
        );

        var lblFocussedTile = new Label();
        lblFocussedTile.setFont(FONT_STATUS_LINE_NORMAL);
        lblFocussedTile.setMinWidth(100);
        lblFocussedTile.setMaxWidth(100);
        lblFocussedTile.textProperty().bind(editCanvas.focussedTileProperty().map(
                tile -> tile != null ? "(%2d,%2d)".formatted(tile.x(), tile.y()) : "n/a"));

        var statusIndicator = new StatusIndicator();
        createZoomControl();

        statusLine = new HBox(
                statusIndicator, UfxMapEditor.filler(10),
                lblMapSize, UfxMapEditor.filler(10),
                lblFocussedTile, UfxMapEditor.spacer(),
                messageDisplay, UfxMapEditor.spacer(),
                UfxMapEditor.filler(10), sliderZoom);

        statusLine.setPadding(new Insets(10, 2, 2, 2));
    }

    private class StatusIndicator extends Label {

        public StatusIndicator() {
            setMinWidth(90);
            setFont(FONT_STATUS_LINE_EDIT_MODE);
            setEffect(new Glow(0.2));
            setAlignment(Pos.CENTER);
            setTextAlignment(TextAlignment.CENTER);
            setBorder(Border.stroke(Color.LIGHTGRAY));
            Tooltip tooltip = new Tooltip(translated("editmode_label.tooltip"));
            tooltip.setShowDelay(Duration.millis(50));
            tooltip.setFont(FONT_TOOL_TIPS);
            setOnMouseClicked(_ -> new Action_SelectNextEditMode(TileMapEditorUI.this).execute());
            setTooltip(tooltip);

            textProperty().bind(Bindings.createStringBinding(() ->
                switch (editMode()) {
                    case INSPECT -> translated("mode.inspect");
                    case EDIT    -> translated(editor.symmetricEditMode() ? "mode.symmetric" : "mode.edit");
                    case ERASE   -> translated("mode.erase");
                }, editModeProperty(), editor.symmetricEditModeProperty()
            ));

            textFillProperty().bind(editModeProperty().map(mode ->
                switch (mode) {
                    case INSPECT -> Color.GRAY;
                    case EDIT    -> Color.FORESTGREEN;
                    case ERASE   -> Color.RED;
                }
            ));
        }
    }

    private void arrangeLayout() {
        var centerPane = new VBox(editorPaletteTabPane, splitPaneMapEditorAndPreviews, statusLine);
        centerPane.setPadding(new Insets(0,5,0,5));
        VBox.setVgrow(editorPaletteTabPane, Priority.NEVER);
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
                return "%s: [%s]".formatted(
                    translated("map_editor"), translated("unsaved_map"));
            }, editor.currentFileProperty(), editor.currentWorldMapProperty()
        );
    }

    // also called from EditorPage
    public MenuItem createLoadMapMenuItem(String title, WorldMap worldMap) {
        requireNonNull(title);
        requireNonNull(worldMap);
        var menuItem = new MenuItem(title);
        menuItem.setOnAction(_ -> afterCheckForUnsavedChanges(() -> editor.setCurrentWorldMap(new WorldMap(worldMap))));
        return menuItem;
    }

    public void replaceSampleMapMenuEntries(TileMapEditor.SampleMaps maps) {
        Menu menu = menuBar.menuMaps();
        menu.getItems().clear();
        if (maps.pacManMap() != null) {
            menu.getItems().add(createLoadMapMenuItem("Pac-Man", maps.pacManMap()));
            menu.getItems().add(new SeparatorMenuItem());
        }
        for (int i = 0; i < maps.msPacmanMaps().size(); ++i) {
            if (maps.msPacmanMaps().get(i) != null) {
                MenuItem item = createLoadMapMenuItem("Ms. Pac-Man %d".formatted(i+1), maps.msPacmanMaps().get(i));
                menu.getItems().add(item);
            }
        }
        for (int i = 0; i < maps.masonicMaps().size(); ++i) {
            if (maps.masonicMaps().get(i) != null) {
                MenuItem item = createLoadMapMenuItem("Pac-Man XXL %d".formatted(i+1), maps.masonicMaps().get(i));
                if (i == 0) {
                    menu.getItems().add(new SeparatorMenuItem());
                }
                menu.getItems().add(item);
            }
        }
    }

    // Event handlers

    private final Map<String, EditorUIAction<?>> actionBindings = new HashMap<>();

    private void onKeyPressed(KeyEvent keyEvent) {
        boolean alt = keyEvent.isAltDown();
        switch (keyEvent.getCode()) {
            case LEFT -> {
                if (alt) new Action_SelectNextMapFile(editor, false).execute();
            }
            case RIGHT -> {
                if (alt) new Action_SelectNextMapFile(editor, true).execute();
            }
            case PLUS -> new Action_ZoomIn(this).execute();
            case MINUS -> new Action_ZoomOut(this).execute();
        }
    }

    private void onKeyTyped(KeyEvent e) {
        actionBindings.entrySet().stream()
            .filter(entry -> e.getCharacter().equals(entry.getKey()))
            .findFirst()
            .ifPresent(entry -> entry.getValue().execute());
    }

    // Model change handling

    public void onTerrainMapChanged() {
        preview3D.updateMaze();
        terrainPropertiesEditor.updateEditorValues();
    }

    public void onFoodMapChanged() {
        preview3D.updateFood();
        foodPropertiesEditor.updateEditorValues();
    }
}