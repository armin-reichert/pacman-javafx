/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.lib.tilemap.WorldMapFormatter;
import de.amr.pacmanfx.tilemap.editor.actions.*;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.SAMPLE_MAPS_PATH;
import static java.util.Objects.requireNonNull;

public class TileMapEditor {

    public class ChangeManager {

        private boolean edited;
        private boolean terrainMapChanged;
        private boolean foodMapChanged;
        private boolean obstaclesUpToDate;

        private final List<Vector2i> tilesWithErrors = new ArrayList<>();

        public List<Vector2i> tilesWithErrors() {
            return tilesWithErrors;
        }

        public void setEdited(boolean edited) {
            this.edited = edited;
        }

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

        private void processChanges() {
            if (!obstaclesUpToDate) {
                tilesWithErrors.clear();
                tilesWithErrors.addAll(currentWorldMap().buildObstacleList());
                obstaclesUpToDate = true;
            }
            if (terrainMapChanged || foodMapChanged) {
                sourceCode.set(sourceCode());
            }
            if (terrainMapChanged) {
                //TODO use events?
                ui.onTerrainMapChanged(currentWorldMap());
                terrainMapChanged = false;
            }
            if (foodMapChanged) {
                ui.onFoodMapChanged(currentWorldMap());
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

    private final TileMapEditorUI ui;
    private final ChangeManager changeManager = new ChangeManager();
    private final UpdateTimer updateTimer = new UpdateTimer();

    private class UpdateTimer extends AnimationTimer {
        @Override
        public void handle(long now) {
            ui.messageDisplay().update();
            changeManager.processChanges();
            try {
                ui.draw();
            } catch (Exception x) {
                Logger.error(x);
            }
        }
    }

    public TileMapEditor(Stage stage, Model3DRepository model3DRepository) {
        requireNonNull(stage);
        requireNonNull(model3DRepository);
        ui = new TileMapEditorUI(stage, this, model3DRepository);
    }

    public void init(File workDir) {
        setCurrentDirectory(workDir);
        WorldMap emptyMap = new Action_CreateEmptyMap(this, 36, 28).execute();
        setCurrentWorldMap(emptyMap);
        setEditMode(EditMode.INSPECT);
        changeManager.edited = false;
        ui.init();
    }

    public void start() {
        Platform.runLater(() -> {
            ui.start();
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
            gridSize = new SimpleDoubleProperty(DEFAULT_GRID_SIZE);
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

    // -- paletteID

    private final ObjectProperty<PaletteID> paletteID = new SimpleObjectProperty<>(PaletteID.PALETTE_ID_TERRAIN);

    public ObjectProperty<PaletteID> paletteIDProperty() {
        return paletteID;
    }

    public PaletteID paletteID() {
        return paletteID.get();
    }

    public void setPaletteID(PaletteID id) {
        paletteID.set(id);
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

    // -- sourceCode

    private final StringProperty sourceCode = new SimpleStringProperty("");

    public StringProperty sourceCodeProperty() {
        return sourceCode;
    }

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

    // -- templateImage

    public ObjectProperty<Image> templateImageProperty() { return templateImage; }

    // -- title

    private final StringProperty title = new SimpleStringProperty("Tile Map Editor");

    public StringProperty titleProperty() { return title; }

    // Accessor methods

    public TileMapEditorUI ui() {
        return ui;
    }

    public ChangeManager changeManager() { return changeManager;}

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
        switch (editMode) {
            case INSPECT -> ui.editCanvas().enterInspectMode();
            case EDIT    -> ui.editCanvas().enterEditMode();
            case ERASE   -> ui.editCanvas().enterEraseMode();
        }
    }

    // Event handlers

    public void onKeyPressed(KeyEvent e) {
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

    public void onKeyTyped(KeyEvent e) {
        String ch = e.getCharacter();
        if (ch.equals("e")) {
            new Action_SelectNextEditMode(this).execute();
        }
    }

    // Sample maps loading

    public record SampleMaps(WorldMap pacManMap, List<WorldMap> msPacmanMaps, List<WorldMap> xxlMaps) {}

    public SampleMaps loadSampleMaps() {
        try {
            var pacManMap = loadMap("pacman/pacman.world", 1);
            var msPacManMaps = new ArrayList<WorldMap>();
            for (int n = 1; n <= 6; ++n) {
                msPacManMaps.add(loadMap("mspacman/mspacman_%d.world", n));
            }
            msPacManMaps.trimToSize();
            var xxlMaps = new ArrayList<WorldMap>();
            for (int n = 1; n <= 8; ++n) {
                xxlMaps.add(loadMap("pacman_xxl/masonic_%d.world", n));
            }
            xxlMaps.trimToSize();
            return new SampleMaps(pacManMap, msPacManMaps, xxlMaps);
        } catch (IOException x) {
            Logger.error(x);
            Logger.error("Error loading sample maps");
            return null;
        }
    }

    private WorldMap loadMap(String namePattern, int number) throws IOException {
        return WorldMap.fromURL(getClass().getResource(SAMPLE_MAPS_PATH + namePattern.formatted(number)));
    }
}