package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.pacmanfx.model.world.ArcadeHouse;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.config.WorldConfig;
import de.amr.pacmanfx.ui.gamescene.d3.entities.Maze3D;
import de.amr.pacmanfx.ui.gamescene.d3.entities.MazeHouse3D;
import de.amr.pacmanfx.uilib.UfxColors;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;

import java.util.Map;

import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

public class MazeFactory3D {

    public static final int FLOOR_SPECULAR_POWER = 128;
    public static final int WALL_BASE_SPECULAR_POWER = 64;
    public static final int WALL_TOP_SPECULAR_POWER = 128;

    public Maze3D createMaze3D(
        TerrainLayer terrain, WorldConfig config, WorldMapColorScheme colorScheme,
        AnimationRegistry animationRegistry,
        ObjectProperty<DrawMode> drawMode)
    {
        requireNonNull(terrain);
        requireNonNull(config);
        requireNonNull(colorScheme);
        requireNonNull(animationRegistry);
        requireNonNull(drawMode);

        final Map<String, PhongMaterial> materials = createMazeMaterialMap(colorScheme);

        final var maze3D = new Maze3D(terrain);
        maze3D.build(drawMode, materials, config.maze(), config.floor());

        bindFloorMaterialColor(maze3D, materials.get("floorMaterial"));
        bindWallBaseMaterialColor(maze3D, materials.get("wallBaseMaterial"), Color.valueOf(colorScheme.wallStroke()));

        // Currently, only Arcade house is supported
        terrain.optHouse()
            .filter(ArcadeHouse.class::isInstance)
            .map(ArcadeHouse.class::cast)
            .map(house -> new MazeHouse3D(colorScheme, config.house(), animationRegistry, house))
            .ifPresent(maze3D::setHouse3D);

        return maze3D;
    }

    private Map<String, PhongMaterial> createMazeMaterialMap(WorldMapColorScheme colorScheme) {
        final PhongMaterial floorMaterial = new PhongMaterial();
        floorMaterial.setSpecularPower(FLOOR_SPECULAR_POWER);

        final PhongMaterial wallBaseMaterial = new PhongMaterial();
        wallBaseMaterial.setSpecularPower(WALL_BASE_SPECULAR_POWER);

        final PhongMaterial wallTopMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.wallFill()));
        wallTopMaterial.setSpecularPower(WALL_TOP_SPECULAR_POWER);

        return Map.of(
            "floorMaterial", floorMaterial,
            "wallBaseMaterial", wallBaseMaterial,
            "wallTopMaterial", wallTopMaterial
        );
    }

    private void bindFloorMaterialColor(Maze3D maze3D, PhongMaterial floorMaterial) {
        floorMaterial.diffuseColorProperty().bind(maze3D.floorColorProperty());
        floorMaterial.specularColorProperty().bind(maze3D.floorColorProperty().map(Color::brighter));
    }

    private void bindWallBaseMaterialColor(Maze3D maze3D, PhongMaterial wallBaseMaterial, Color wallStrokeColor) {
        wallBaseMaterial.diffuseColorProperty().bind(maze3D.wallOpacityProperty()
            .map(opacity -> UfxColors.colorWithOpacity(wallStrokeColor, opacity.doubleValue()))
        );
    }
}
