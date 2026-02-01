/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.world.House;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.model3D.Wall3D;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import org.tinylog.Logger;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class Maze3D extends Group implements Disposable {

    /** Normalized wall top color for very dark colors */
    private static final String DARK_WALL_FILL_COLOR = "0x2a2a2a";

    private final PreferencesManager prefs;
    private final WorldMapColorScheme colorScheme;
    private final GameLevel level;
    private final AnimationRegistry animationRegistry;

    private final DoubleProperty wallBaseHeight = new SimpleDoubleProperty(Wall3D.DEFAULT_BASE_HEIGHT);
    private final DoubleProperty wallOpacity = new SimpleDoubleProperty(1);

    private MazeMaterials3D materials;
    private MazeObstacles3D obstacles;
    private MazeFloor3D mazeFloor3D;
    private MazeHouse3D mazeHouse3D;
    private MazeFood3D mazeFood3D;

    public Maze3D(GameUI ui, GameLevel level, AnimationRegistry animationRegistry, List<PhongMaterial> ghostMaterials) {
        requireNonNull(ui);
        this.prefs = ui.prefs();
        this.level = requireNonNull(level);
        this.animationRegistry = requireNonNull(animationRegistry);
        requireNonNull(ghostMaterials);

        final WorldMapColorScheme proposedColorScheme = ui.currentConfig().colorScheme(level.worldMap());
        colorScheme = adjustColorScheme(proposedColorScheme);

        createMaterials();
        createFloor3D();
        createObstacles3D();
        level.worldMap().terrainLayer().optHouse().ifPresent(this::createHouse3D);
        createMazeFood3D(ghostMaterials);
    }

    // Adds some contrast if the wall fill color is very dark (assuming a very dark floor)
    private WorldMapColorScheme adjustColorScheme(WorldMapColorScheme proposedColorScheme) {
        final boolean isFillColorDark = Color.valueOf(proposedColorScheme.wallFill()).getBrightness() < 0.1;
        return isFillColorDark
            ? new WorldMapColorScheme(DARK_WALL_FILL_COLOR, proposedColorScheme.wallStroke(), proposedColorScheme.door(), proposedColorScheme.pellet())
            : proposedColorScheme;
    }

    public DoubleProperty wallBaseHeightProperty() {
        return wallBaseHeight;
    }

    public DoubleProperty wallOpacityProperty() {
        return wallOpacity;
    }

    public WorldMapColorScheme colorScheme() {
        return colorScheme;
    }

    public MazeMaterials3D materials() {
        return materials;
    }

    public MazeObstacles3D obstacles() {
        return obstacles;
    }

    public MazeFloor3D floor() {
        return mazeFloor3D;
    }

    public MazeHouse3D house() {
        return mazeHouse3D;
    }

    public MazeFood3D food() {
        return mazeFood3D;
    }

    @Override
    public void dispose() {
        wallBaseHeight.unbind();
        wallOpacity.unbind();

        if (materials != null) {
            materials.dispose();
            materials = null;
        }
        if (mazeFloor3D != null) {
            mazeFloor3D.dispose();
            mazeFloor3D = null;
        }
        if (obstacles != null) {
            obstacles.dispose();
            obstacles = null;
        }
        if (mazeHouse3D != null) {
            mazeHouse3D.dispose();
            mazeHouse3D = null;
        }
        if (mazeFood3D != null) {
            mazeFood3D.dispose();
            mazeFood3D = null;
        }
        getChildren().forEach(Wall3D::dispose);
        getChildren().clear();
        Logger.info("Disposed 3D maze");
    }

    private void createMaterials() {
        materials = MazeMaterials3D.create(colorScheme, wallOpacityProperty());
    }

    private void createObstacles3D() {
        obstacles = new MazeObstacles3D(prefs);
        obstacles.addObstacles(this, materials, wallBaseHeight, level);
    }

    private void createFloor3D() {
        mazeFloor3D = new MazeFloor3D(prefs, level, materials.floor());
    }

    private void createHouse3D(House house) {
        mazeHouse3D = new MazeHouse3D(prefs, colorScheme, animationRegistry, house);
        getChildren().add(mazeHouse3D);
    }

    private void createMazeFood3D(List<PhongMaterial> ghostMaterials) {
        mazeFood3D = new MazeFood3D(prefs, colorScheme, animationRegistry, level, ghostMaterials,
            mazeHouse3D.arcadeHouse3D().swirls());
    }
}