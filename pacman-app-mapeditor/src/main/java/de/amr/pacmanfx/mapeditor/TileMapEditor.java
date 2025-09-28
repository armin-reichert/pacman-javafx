/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor;

import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.lib.worldmap.WorldMapChecker;
import de.amr.pacmanfx.mapeditor.actions.Action_CreateEmptyMap;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.amr.pacmanfx.mapeditor.EditorGlobals.*;
import static java.util.Objects.requireNonNull;

public class TileMapEditor {

    private SampleMaps sampleMaps;
    private final TileMapEditorUI ui;

    private final AnimationTimer updateTimer = new AnimationTimer() {
        private static final long FRAME_DURATION_NS = 1_000_000_000 / EditorGlobals.UPDATE_FREQ;
        private long lastUpdate = 0;

        @Override
        public void handle(long now) {
            if (now - lastUpdate >= FRAME_DURATION_NS) {
                lastUpdate = now;
                ui.messageDisplay().update();
                processChanges();
                try {
                    ui.draw();
                } catch (Exception x) {
                    Logger.error(x);
                }
            }
        }
    };

    public TileMapEditor(Stage stage, Model3DRepository model3DRepository) {
        requireNonNull(stage);
        requireNonNull(model3DRepository);
        ui = new TileMapEditorUI(stage, this, model3DRepository);
        currentWorldMap.addListener((py, ov, nv) -> setWorldMapChanged());
        sourceCodeLineNumbers.addListener((py, ov, lineNumbers) -> sourceCode.set(currentWorldMap().sourceCode(lineNumbers)));
    }

    public void init(File workDir) {
        setCurrentDirectory(workDir);
        WorldMap worldMap = new Action_CreateEmptyMap(this, 28, 36).execute();
        setCurrentWorldMap(worldMap);
        edited = false;
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
    }

    public TileMapEditorUI ui() {
        return ui;
    }

    private WorldMapChecker.WorldMapCheckResult checkResult;

    // Change management
    private boolean edited;
    private boolean terrainMapChanged;
    private boolean terrainMapPropertyChanged;
    private boolean foodMapChanged;
    private boolean foodMapPropertyChanged;

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
    }

    public void setTerrainMapPropertyChanged() {
        this.terrainMapPropertyChanged = true;
    }

    public void setFoodMapChanged() {
        foodMapChanged = true;
    }

    public void setFoodMapPropertyChanged() {
        this.foodMapPropertyChanged = true;
    }

    private void processChanges() {
        if (terrainMapChanged || foodMapChanged) {
            checkResult = WorldMapChecker.check(currentWorldMap());
            sourceCode.set(currentWorldMap().sourceCode(sourceCodeLineNumbers.get()));
        }
        if (terrainMapChanged) {
            //TODO use events?
            ui.onTerrainMapChanged();
            terrainMapChanged = false;
        }
        if (terrainMapPropertyChanged) {
            terrainMapPropertyChanged = false;
        }
        if (foodMapChanged) {
            ui.onFoodMapChanged();
            foodMapChanged = false;
        }
        if (foodMapPropertyChanged) {
            foodMapPropertyChanged = false;
        }
    }

    public WorldMapChecker.WorldMapCheckResult checkResult() {
        if (checkResult == null) {
            checkResult = WorldMapChecker.check(currentWorldMap()); // ensures not null initially
        }
        return checkResult;
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

    private final ObjectProperty<WorldMap> currentWorldMap = new SimpleObjectProperty<>();

    public ObjectProperty<WorldMap> currentWorldMapProperty() { return currentWorldMap; }

    public WorldMap currentWorldMap() { return currentWorldMap.get(); }

    public void setCurrentWorldMap(WorldMap worldMap) { currentWorldMap.set(worldMap); }

    // -- sourceCode

    private final StringProperty sourceCode = new SimpleStringProperty("");

    public StringProperty sourceCodeProperty() {
        return sourceCode;
    }

    // -- sourceCodeLineNumbers

    private final BooleanProperty sourceCodeLineNumbers = new SimpleBooleanProperty(true);

    public BooleanProperty sourceCodeLineNumbers() {
        return sourceCodeLineNumbers;
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

    public ObjectProperty<Image> templateImageProperty() { return templateImage; }

    public Image templateImage() {
        return templateImage.get();
    }

    public void setTemplateImage(Image image) {
        templateImage.set(image);
    }

    // Sample maps loading

    public record SampleMaps(WorldMap pacManMap, List<WorldMap> msPacmanMaps, List<WorldMap> masonicMaps) {}

    public SampleMaps sampleMaps() {
        if (sampleMaps == null) {
            WorldMap pacManMap = null;
            ArrayList<WorldMap> msPacManMaps = new ArrayList<>();
            ArrayList<WorldMap> masonicMaps = new ArrayList<>();
            try {
                pacManMap = loadMap(SAMPLE_MAP_PATH_PACMAN, 1).orElse(null);
                for (int number = 1; number <= 6; ++number) {
                    loadMap(SAMPLE_MAP_PATH_MS_PACMAN, number).ifPresent(msPacManMaps::add);
                }
                for (int number = 1; number <= 8; ++number) {
                    loadMap(SAMPLE_MAP_PATH_MASONIC, number).ifPresent(masonicMaps::add);
                }
            } catch (Exception x) {
                Logger.error(x);
            }
            msPacManMaps.trimToSize();
            masonicMaps.trimToSize();
            sampleMaps = new SampleMaps(
                pacManMap, Collections.unmodifiableList(msPacManMaps), Collections.unmodifiableList(masonicMaps));
        }
        return sampleMaps;
    }

    private Optional<WorldMap> loadMap(String pathPattern, int number) {
        String path = pathPattern.formatted(number);
        URL url = getClass().getResource(path);
        if (url == null) {
            Logger.error("Could not access resource with URL path '{}'", path);
            return Optional.empty();
        }
        try {
            return Optional.of(WorldMap.loadFromURL(url));
        } catch (IOException x) {
            Logger.error(x);
            Logger.error("Could not load world map from URL '{}'", url);
            return Optional.empty();
        }
    }
}