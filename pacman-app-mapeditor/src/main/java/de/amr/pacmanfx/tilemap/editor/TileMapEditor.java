/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.tilemap.editor.actions.Action_CreateEmptyMap;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.Globals.ARCADE_MAP_SIZE_IN_TILES;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.SAMPLE_MAPS_PATH;
import static de.amr.pacmanfx.tilemap.editor.EditorUtil.sourceCode;
import static java.util.Objects.requireNonNull;

public class TileMapEditor {

    static final WorldMap EMPTY_MAP = WorldMap.emptyMap(ARCADE_MAP_SIZE_IN_TILES.x(), ARCADE_MAP_SIZE_IN_TILES.y());

    private final EditorUI ui;
    private final UpdateTimer updateTimer = new UpdateTimer();

    private class UpdateTimer extends AnimationTimer {
        private static final int FREQ = 30; // Hz
        private static final long FRAME_DURATION_NS = 1_000_000_000 / FREQ;
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
    }

    public TileMapEditor(Stage stage, Model3DRepository model3DRepository) {
        requireNonNull(stage);
        requireNonNull(model3DRepository);
        ui = new EditorUI(stage, this, model3DRepository);
        currentWorldMap.addListener((py, ov, nv) -> setWorldMapChanged());
    }

    public void init(File workDir) {
        setCurrentDirectory(workDir);
        WorldMap emptyMap = new Action_CreateEmptyMap(this, ARCADE_MAP_SIZE_IN_TILES.y(), ARCADE_MAP_SIZE_IN_TILES.x()).execute();
        setCurrentWorldMap(emptyMap);
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

    // Change management
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
            sourceCode.set(sourceCode(currentWorldMap()));
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

    private final ObjectProperty<WorldMap> currentWorldMap = new SimpleObjectProperty<>(EMPTY_MAP);

    public ObjectProperty<WorldMap> currentWorldMapProperty() { return currentWorldMap; }

    public WorldMap currentWorldMap() { return currentWorldMap.get(); }

    public void setCurrentWorldMap(WorldMap worldMap) { currentWorldMap.set(worldMap); }

    // -- sourceCode

    private final StringProperty sourceCode = new SimpleStringProperty("");

    public StringProperty sourceCodeProperty() {
        return sourceCode;
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