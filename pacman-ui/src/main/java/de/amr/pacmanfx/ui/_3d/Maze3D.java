/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector2i;
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
    private final AnimationRegistry animationRegistry;

    private final DoubleProperty wallBaseHeight = new SimpleDoubleProperty(Wall3D.DEFAULT_BASE_HEIGHT);
    private final DoubleProperty wallOpacity = new SimpleDoubleProperty(1);

    private MazeMaterials3D materials3D;
    private MazeObstacles3D obstacles3D;
    private MazeFloor3D floor3D;
    private MazeHouse3D house3D;
    private MazeFood3D food3D;

    public Maze3D(GameUI ui, GameLevel level, AnimationRegistry animationRegistry, List<PhongMaterial> ghostMaterials) {
        requireNonNull(ui);
        this.prefs = ui.prefs();
        this.animationRegistry = requireNonNull(animationRegistry);
        requireNonNull(ghostMaterials);

        final WorldMapColorScheme proposedColorScheme = ui.currentConfig().colorScheme(level.worldMap());
        colorScheme = adjustColorScheme(proposedColorScheme);

        createMaterials();
        createFloor3D(level);
        createObstacles3D(level);
        level.worldMap().terrainLayer().optHouse().ifPresent(this::createHouse3D);
        createMazeFood3D(level, ghostMaterials);
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
        return materials3D;
    }

    public MazeFloor3D floor() {
        return floor3D;
    }

    public MazeHouse3D house() {
        return house3D;
    }

    public MazeFood3D food() {
        return food3D;
    }

    @Override
    public void dispose() {
        wallBaseHeight.unbind();
        wallOpacity.unbind();

        if (materials3D != null) {
            materials3D.dispose();
            materials3D = null;
        }
        if (floor3D != null) {
            floor3D.dispose();
            floor3D = null;
        }
        if (obstacles3D != null) {
            obstacles3D.dispose();
            obstacles3D = null;
        }
        if (house3D != null) {
            house3D.dispose();
            house3D = null;
        }
        if (food3D != null) {
            food3D.dispose();
            food3D = null;
        }
        getChildren().forEach(Wall3D::dispose);
        getChildren().clear();
        Logger.info("Disposed 3D maze");
    }

    private void createMaterials() {
        materials3D = MazeMaterials3D.create(colorScheme, wallOpacityProperty());
    }

    private void createObstacles3D(GameLevel level) {
        obstacles3D = new MazeObstacles3D();
        getChildren().add(obstacles3D);
        final float wallThickness = prefs.getFloat("3d.obstacle.wall_thickness");
        final float cornerRadius = prefs.getFloat("3d.obstacle.corner_radius");
        obstacles3D.renderObstacles(level, wallThickness, cornerRadius, materials3D, wallBaseHeight);
    }

    private void createFloor3D(GameLevel level) {
        final float padding = prefs.getFloat("3d.floor.padding");
        final float thickness = prefs.getFloat("3d.floor.thickness");
        final Vector2i worldSizePx = level.worldMap().terrainLayer().sizeInPixel();
        final float width = worldSizePx.x() + 2 * padding;
        final float height = worldSizePx.y();
        floor3D = new MazeFloor3D(materials3D.floor(), width, height, thickness, padding);
    }

    private void createHouse3D(House house) {
        house3D = new MazeHouse3D(prefs, colorScheme, animationRegistry, house);
        getChildren().add(house3D.root());
    }

    private void createMazeFood3D(GameLevel level, List<PhongMaterial> ghostMaterials) {
        food3D = new MazeFood3D(prefs, colorScheme, animationRegistry, level, ghostMaterials, house3D.swirls());
    }
}