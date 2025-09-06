/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.lib.tilemap.WorldMapChecker;
import de.amr.pacmanfx.tilemap.editor.actions.Action_SetDefaultMapColors;
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
import java.util.List;
import java.util.Optional;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.SAMPLE_MAPS_PATH;
import static de.amr.pacmanfx.tilemap.editor.EditorUtil.generateSourceCode;
import static java.util.Objects.requireNonNull;

public class TileMapEditor {

    private SampleMaps sampleMaps;
    private final EditorUI ui;

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
        ui = new EditorUI(stage, this, model3DRepository);
        currentWorldMap.addListener((py, ov, nv) -> setWorldMapChanged());
    }

    public void init(File workDir) {
        loadSampleMaps();
        setCurrentDirectory(workDir);
        setCurrentWorldMap(WorldMap.emptyMap(28, 36));
        new Action_SetDefaultMapColors(this).execute();
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

    public EditorUI ui() {
        return ui;
    }

    private WorldMapChecker.WorldMapCheckResult checkResult;

    // Change management
    private boolean edited;
    private boolean terrainMapChanged;
    private boolean foodMapChanged;

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

    public void setFoodMapChanged() {
        foodMapChanged = true;
    }

    private void processChanges() {
        if (terrainMapChanged || foodMapChanged) {
            checkResult = WorldMapChecker.check(currentWorldMap());
            sourceCode.set(generateSourceCode(currentWorldMap()));
        }
        if (terrainMapChanged) {
            //TODO use events?
            ui.onTerrainMapChanged();
            terrainMapChanged = false;
        }
        if (foodMapChanged) {
            ui.onFoodMapChanged();
            foodMapChanged = false;
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

    public record SampleMaps(WorldMap pacManMap, List<WorldMap> msPacmanMaps, List<WorldMap> xxlMaps) {}

    public SampleMaps sampleMaps() {
        return sampleMaps;
    }

    private void loadSampleMaps() {
        WorldMap pacManMap = null;
        ArrayList<WorldMap> msPacManMaps = new ArrayList<>();
        ArrayList<WorldMap> xxlMaps = new ArrayList<>();
        try {
            pacManMap = loadMap("pacman/pacman.world", 1).orElse(null);
            for (int n = 1; n <= 6; ++n) {
                loadMap("mspacman/mspacman_%d.world", n).ifPresent(msPacManMaps::add);
            }
            for (int n = 1; n <= 8; ++n) {
                loadMap("pacman_xxl/masonic_%d.world", n).ifPresent(xxlMaps::add);
            }
        } catch (Exception x) {
            Logger.error(x);
        }
        msPacManMaps.trimToSize();
        xxlMaps.trimToSize();
        sampleMaps = new SampleMaps(pacManMap, msPacManMaps, xxlMaps);
    }

    private Optional<WorldMap> loadMap(String namePattern, int number) {
        String path = SAMPLE_MAPS_PATH + namePattern.formatted(number);
        URL url = getClass().getResource(path);
        if (url == null) {
            Logger.error("Could not access resource with URL path '{}'", path);
            return Optional.empty();
        }
        try {
            return Optional.of(WorldMap.mapFromURL(url));
        } catch (IOException x) {
            Logger.error(x);
            Logger.error("Could not load world map from URL '{}'", url);
            return Optional.empty();
        }
    }
}