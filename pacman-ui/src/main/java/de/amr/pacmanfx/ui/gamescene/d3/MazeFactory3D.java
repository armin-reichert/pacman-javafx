package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.entities.house.House;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMapColorSchemeImpl;
import de.amr.pacmanfx.ui.settings.world.House3DSettings;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.entities3D.house.comp.House3DAnimationComp;
import de.amr.pacmanfx.uilib.entities3D.house.comp.House3DViewComp;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

import java.util.Map;

import static de.amr.basics.util.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

public class MazeFactory3D {

    public static final int FLOOR_SPECULAR_POWER = 128;
    public static final int WALL_BASE_SPECULAR_POWER = 64;
    public static final int WALL_TOP_SPECULAR_POWER = 128;

    public Maze3D createMaze3D(
        House house,
        TerrainLayer terrain,
        WorldSettings config,
        WorldMapColorSchemeImpl colorScheme,
        AnimationRegistry animationRegistry)
    {
        requireNonNull(terrain);
        requireNonNull(config);
        requireNonNull(colorScheme);
        requireNonNull(animationRegistry);

        final Map<String, PhongMaterial> materials = createMazeMaterialMap(colorScheme);

        createHouse3D(house, config.house(), colorScheme, animationRegistry);

        final var maze3D = new Maze3D(terrain);
        maze3D.build(house, materials, config.maze(), config.floor());

        bindFloorMaterialColor(maze3D, materials.get("floorMaterial"));
        bindWallBaseMaterialColor(maze3D, materials.get("wallBaseMaterial"), Color.valueOf(colorScheme.wallStroke()));

        //final var house3D = new MazeHouse3D(colorScheme, config.house(), animationRegistry, house);
        //maze3D.setHouse3D(house3D);

        return maze3D;
    }

    private void createHouse3D(House house, House3DSettings config3D, WorldMapColorSchemeImpl colorScheme, AnimationRegistry animationRegistry) {
        if (!house.hasComponent(House3DViewComp.class)) {
            final var view3D = new House3DViewComp(
                animationRegistry,
                house.floorplan(),
                config3D.baseHeight(),
                config3D.wallThickness(),
                config3D.opacity()
            );
            house.setComponent(House3DViewComp.class, view3D);
            house.setComponent(House3DAnimationComp.class, new House3DAnimationComp(animationRegistry));
        }

        // apply color scheme
        final var view3D = house.requireComponent(House3DViewComp.class);
        view3D.setWallBaseColor(Color.valueOf(colorScheme.wallFill()));
        view3D.wallBaseHeightProperty().set(config3D.baseHeight());
        view3D.setWallTopColor(Color.valueOf(colorScheme.wallStroke()));
        view3D.setDoorColor(Color.valueOf(colorScheme.door()));
        view3D.setDoorSensitivity(config3D.sensitivity());

    }

    private Map<String, PhongMaterial> createMazeMaterialMap(WorldMapColorSchemeImpl colorScheme) {
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
            .map(opacity -> Ufx.colorWithOpacity(wallStrokeColor, opacity.doubleValue()))
        );
    }
}
