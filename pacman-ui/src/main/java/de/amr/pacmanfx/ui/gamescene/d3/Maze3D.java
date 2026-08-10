/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.uilib.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.entities3D.world.Wall3D;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;

import static java.util.Objects.requireNonNull;

/**
 * Renders the complete 3D representation of a Pac-Man maze for a single level.
 */
public class Maze3D implements DisposableGraphicsObject {

    public record Materials(
        PhongMaterial floorMaterial,
        PhongMaterial wallBaseMaterial,
        PhongMaterial wallTopMaterial
    ) {}

    private final ObjectProperty<DrawMode> drawMode = new SimpleObjectProperty<>(DrawMode.FILL);

    private final DoubleProperty wallBaseHeight = new SimpleDoubleProperty(Wall3D.DEFAULT_BASE_HEIGHT);

    private final DoubleProperty wallOpacity = new SimpleDoubleProperty(1);

    private final ObjectProperty<Color> floorColor = new SimpleObjectProperty<>(Color.valueOf("#1a1a1a"));

    private final TerrainLayer terrain;

    private final Group root = new Group();

    private final Group particlesGroup = new Group();

    private Box floor3D;

    private final Materials materials;

    public Maze3D(TerrainLayer terrain, Materials materials) {
        this.terrain = requireNonNull(terrain);
        this.materials = requireNonNull(materials);
    }

    @Override
    public void dispose() {
        wallBaseHeight.unbind();
        wallOpacity.unbind();
        cleanupGroup(particlesGroup, true);
        cleanupGroup(root, true);
    }

    public TerrainLayer terrain() {
        return terrain;
    }

    public Materials materials() {
        return materials;
    }

    public Group root() {
        return root;
    }

    public Group particlesGroup() {
        return particlesGroup;
    }

    public double floorTop() {
        return floor3D().getTranslateZ() - 0.5 * floor3D().getDepth();
    }

    public void setFloor3D(Box newFloor3D) {
        if (floor3D != null) {
            root.getChildren().remove(floor3D);
        }
        floor3D = requireNonNull(newFloor3D);
        root.getChildren().add(newFloor3D);
    }

    public Box floor3D() {
        return floor3D;
    }

    public ObjectProperty<DrawMode> drawModeProperty() {
        return drawMode;
    }

    public DoubleProperty wallBaseHeightProperty() {
        return wallBaseHeight;
    }

    public DoubleProperty wallOpacityProperty() {
        return wallOpacity;
    }

    public ObjectProperty<Color> floorColorProperty() {
        return floorColor;
    }

}