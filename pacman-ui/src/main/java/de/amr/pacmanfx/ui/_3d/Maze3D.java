/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.StopWatch;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.world.*;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.EnergizerExplosionAndRecyclingAnimation;
import de.amr.pacmanfx.uilib.model3D.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static de.amr.pacmanfx.ui.GameUI.PROPERTY_3D_FLOOR_COLOR;
import static de.amr.pacmanfx.uilib.Ufx.colorWithOpacity;
import static de.amr.pacmanfx.uilib.Ufx.defaultPhongMaterial;
import static java.util.Objects.requireNonNull;

public class Maze3D extends Group implements Disposable {

    /** Normalized wall top color for very dark colors */
    private static final String DARK_GRAY = "0x2a2a2a";

    private final GameUI ui;
    private final GameLevel level;
    private final AnimationRegistry animationRegistry;

    private WorldMapColorScheme colorScheme;
    private Box floor3D;
    private ArcadeHouse3D house3D;
    private Set<Shape3D> pellets3D;
    private Set<Energizer3D> energizers3D;
    private Group particleGroupsContainer = new Group();

    private int wall3DCount;

    private final DoubleProperty wallBaseHeight = new SimpleDoubleProperty(Wall3D.DEFAULT_BASE_HEIGHT);
    private final DoubleProperty wallOpacity = new SimpleDoubleProperty(1);

    private PhongMaterial floorMaterial;
    private PhongMaterial wallBaseMaterial;
    private PhongMaterial wallTopMaterial;
    private PhongMaterial pelletMaterial;
    private PhongMaterial particleMaterial;

    public Maze3D(GameUI ui, GameLevel level, AnimationRegistry animationRegistry, List<PhongMaterial> ghostMaterials) {
        this.ui = ui;
        this.animationRegistry = animationRegistry;
        this.level = level;

        createWorldMapColorScheme();
        createMaterials();

        final var terrainRenderer3D = new TerrainRenderer3D();
        terrainRenderer3D.setOnWallCreated(wall3D -> {
            wall3D.bindBaseHeight(wallBaseHeight);
            wall3D.setBaseMaterial(wallBaseMaterial);
            wall3D.setTopMaterial(wallTopMaterial);
            ++wall3DCount;
            getChildren().addAll(wall3D.base(), wall3D.top());
            return wall3D;
        });

        createFloor3D();

        final WorldMap worldMap = level.worldMap();
        final float wallThickness = ui.prefs().getFloat("3d.obstacle.wall_thickness");
        final float cornerRadius = ui.prefs().getFloat("3d.obstacle.corner_radius");
        final House house = worldMap.terrainLayer().optHouse().orElse(null);
        final var stopWatch = new StopWatch();
        wall3DCount = 0;

        for (Obstacle obstacle : worldMap.terrainLayer().obstacles()) {
            final Vector2i obstacleStartTile = tileAt(obstacle.startPoint().toVector2f());
            // exclude house placeholder obstacle
            if (house == null || !house.contains(obstacleStartTile)) {
                terrainRenderer3D.renderObstacle3D(obstacle, isWorldBorder(worldMap, obstacle), wallThickness, cornerRadius);
            }
        }
        final var passedTimeMillis = stopWatch.passedTime().toMillis();
        Logger.info("Built 3D maze with {} composite walls in {} milliseconds", wall3DCount, passedTimeMillis);

        if (house != null) {
            final Vector2i[] ghostRevivalTiles = {
                house.ghostRevivalTile(CYAN_GHOST_BASHFUL),
                house.ghostRevivalTile(PINK_GHOST_SPEEDY),
                house.ghostRevivalTile(ORANGE_GHOST_POKEY)
            };
            // Note: revival tile is the left of the pair of tiles in the house where the ghost is placed. The center
            //       of the 3D shape is one tile to the right and a half tile to the bottom from the tile origin.
            final Vector2f[] ghostRevivalPositions = Stream.of(ghostRevivalTiles)
                .map(tile -> tile.scaled((float) TS).plus(TS, HTS))
                .toArray(Vector2f[]::new);

            createHouse(house, ghostRevivalPositions);
            getChildren().add(house3D);
        }

        createPellets3D();
        createEnergizers3D(ghostMaterials);

        Logger.info("3D maze created for map (URL '{}'), color scheme: {}...", level.worldMap().url(), colorScheme);
    }

    public WorldMapColorScheme colorScheme() {
        return colorScheme;
    }

    public DoubleProperty wallBaseHeightProperty() {
        return wallBaseHeight;
    }

    public DoubleProperty wallOpacityProperty() {
        return wallOpacity;
    }

    public PhongMaterial wallTopMaterial() {
        return wallTopMaterial;
    }

    public Box floor3D() {
        return floor3D;
    }

    public ArcadeHouse3D house3D() {
        return house3D;
    }

    public Set<Shape3D> pellets3D() { return Collections.unmodifiableSet(pellets3D); }

    public Set<Energizer3D> energizers3D() { return Collections.unmodifiableSet(energizers3D); }

    public Group particleGroupsContainer() {
        return particleGroupsContainer;
    }

    @Override
    public void dispose() {
        wallBaseHeight.unbind();
        wallOpacity.unbind();

        if (floorMaterial != null) {
            floorMaterial.diffuseColorProperty().unbind();
            floorMaterial.specularColorProperty().unbind();
            floorMaterial = null;
        }
        if (wallBaseMaterial != null) {
            wallBaseMaterial.diffuseColorProperty().unbind();
            wallBaseMaterial.specularColorProperty().unbind();
            wallBaseMaterial = null;
        }
        if (wallTopMaterial != null) {
            wallTopMaterial.diffuseColorProperty().unbind();
            wallTopMaterial.specularColorProperty().unbind();
            wallTopMaterial = null;
        }
        if (pelletMaterial != null) {
            pelletMaterial.diffuseColorProperty().unbind();
            pelletMaterial.specularColorProperty().unbind();
            pelletMaterial = null;
        }
        if (particleMaterial != null) {
            particleMaterial.diffuseColorProperty().unbind();
            particleMaterial.specularColorProperty().unbind();
            particleMaterial = null;
        }

        if (floor3D != null) {
            floor3D.translateXProperty().unbind();
            floor3D.translateYProperty().unbind();
            floor3D.translateZProperty().unbind();
            floor3D.materialProperty().unbind();
            floor3D = null;
            Logger.info("Unbound and cleared 3D floor");
        }
        if (house3D != null) {
            house3D.openProperty().removeListener(this::handleHouseOpenChange);
            house3D.dispose();
            house3D = null;
            Logger.info("Disposed 3D house");
        }
        Logger.info("Removed 'house open' listener");

        getChildren().forEach(Wall3D::dispose);

        if (pellets3D != null) {
            pellets3D.forEach(pellet3D -> {
                if (pellet3D instanceof MeshView meshView) {
                    meshView.setMaterial(null);
                    meshView.setMesh(null);
                }
            });
            pellets3D = null;
            Logger.info("Disposed 3D pellets");
        }
        if (energizers3D != null) {
            disposeAll(energizers3D);
            energizers3D.clear();
            energizers3D = null;
            Logger.info("Disposed 3D energizers");
        }
        if (particleGroupsContainer != null) {
            particleGroupsContainer.getChildren().clear();
            particleGroupsContainer = null;
            Logger.info("Removed all particle groups");
        }

        getChildren().clear();
        Logger.info("Disposed 3D maze");
    }

    private void createWorldMapColorScheme() {
        final WorldMap worldMap = level.worldMap();
        final WorldMapColorScheme proposedColorScheme = ui.currentConfig().colorScheme(worldMap);
        requireNonNull(proposedColorScheme);
        // Add some contrast with dark floor if wall fill color is very dark
        final boolean veryDarkWallFillColor = Color.valueOf(proposedColorScheme.wallFill()).getBrightness() < 0.1;
        colorScheme = veryDarkWallFillColor
            ? new WorldMapColorScheme(DARK_GRAY, proposedColorScheme.wallStroke(), proposedColorScheme.door(), proposedColorScheme.pellet())
            : proposedColorScheme;
    }

    private void createMaterials() {
        pelletMaterial = defaultPhongMaterial(Color.valueOf(colorScheme.pellet()));
        particleMaterial = defaultPhongMaterial(Color.valueOf(colorScheme.pellet()).deriveColor(0, 0.5, 1.5, 0.5));

        floorMaterial = defaultPhongMaterial(PROPERTY_3D_FLOOR_COLOR);
        floorMaterial.setSpecularPower(128);

        final Color diffuseColor = wallOpacityProperty()
            .map(opacity -> colorWithOpacity(Color.valueOf(colorScheme.wallStroke()), opacity.doubleValue())).getValue();
        wallBaseMaterial = defaultPhongMaterial(diffuseColor);
        wallBaseMaterial.setSpecularPower(64);

        wallTopMaterial = defaultPhongMaterial(Color.valueOf(colorScheme.wallFill()));
    }

    private void createFloor3D() {
        final float padding = ui.prefs().getFloat("3d.floor.padding");
        final float thickness = ui.prefs().getFloat("3d.floor.thickness");
        final Vector2i worldSizePx = level.worldMap().terrainLayer().sizeInPixel();
        final float sizeX = worldSizePx.x() + 2 * padding;
        final float sizeY = worldSizePx.y();

        floor3D = new Box(sizeX, sizeY, thickness);
        floor3D.setMaterial(floorMaterial);
        // Translate: top-left corner (without padding) at origin, surface top at z=0
        final var translate = new Translate(0.5 * sizeX - padding, 0.5 * sizeY, 0.5 * thickness);
        floor3D.getTransforms().add(translate);
    }

    private boolean isWorldBorder(WorldMap worldMap, Obstacle obstacle) {
        final Vector2i start = obstacle.startPoint();
        if (obstacle.isClosed()) {
            return start.x() == TS || start.y() == worldMap.terrainLayer().emptyRowsOverMaze() * TS + HTS;
        } else {
            return start.x() == 0 || start.x() == worldMap.numCols() * TS;
        }
    }

    private void createHouse(House house, Vector2f[] ghostRevivalPositions) {
        house3D = new ArcadeHouse3D(
            animationRegistry,
            house,
            ghostRevivalPositions,
            ui.prefs().getFloat("3d.house.base_height"),
            ui.prefs().getFloat("3d.house.wall_thickness"),
            ui.prefs().getFloat("3d.house.opacity")
        );
        house3D.setWallBaseColor(Color.valueOf(colorScheme.wallFill()));
        house3D.setWallTopColor(Color.valueOf(colorScheme.wallStroke()));
        house3D.setDoorColor(Color.valueOf(colorScheme.door()));
        house3D.wallBaseHeightProperty().set(ui.prefs().getFloat("3d.house.base_height"));
        house3D.openProperty().addListener(this::handleHouseOpenChange);
        house3D.setDoorSensitivity(ui.prefs().getFloat("3d.house.sensitivity"));
    }

    private void createPellets3D() {
        final Mesh mesh = PacManModel3DRepository.instance().pelletMesh();
        final var prototype = new MeshView(mesh);
        final Bounds bounds = prototype.getBoundsInLocal();
        final double maxExtent = Math.max(Math.max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
        final float radius = ui.prefs().getFloat("3d.pellet.radius");
        final double scaling = (2 * radius) / maxExtent;
        final var scale = new Scale(scaling, scaling, scaling);
        final FoodLayer foodLayer = level.worldMap().foodLayer();

        pellets3D = foodLayer.tiles().filter(foodLayer::hasFoodAtTile)
            .filter(tile -> !foodLayer.isEnergizerTile(tile))
            .map(tile -> createPellet3D(mesh, scale, tile))
            .collect(Collectors.toCollection(HashSet::new));
    }

    private Shape3D createPellet3D(Mesh pelletMesh, Scale scale, Vector2i tile) {
        final var pelletShape = new MeshView(pelletMesh);
        pelletShape.setMaterial(pelletMaterial);
        pelletShape.setRotationAxis(Rotate.Z_AXIS);
        pelletShape.setRotate(90);
        pelletShape.setTranslateX(tile.x() * TS + HTS);
        pelletShape.setTranslateY(tile.y() * TS + HTS);
        pelletShape.setTranslateZ(- 6);
        pelletShape.getTransforms().add(scale);
        pelletShape.setUserData(tile);
        return pelletShape;
    }

    private void createEnergizers3D(List<PhongMaterial> ghostMaterials) {
        final float radius     = ui.prefs().getFloat("3d.energizer.radius");
        final float minScaling = ui.prefs().getFloat("3d.energizer.scaling.min");
        final float maxScaling = ui.prefs().getFloat("3d.energizer.scaling.max");
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        energizers3D = foodLayer.tiles().filter(foodLayer::hasFoodAtTile)
            .filter(foodLayer::isEnergizerTile)
            .map(tile -> createAnimatedEnergizer3D(tile, radius, minScaling, maxScaling, ghostMaterials))
            .collect(Collectors.toCollection(HashSet::new));
    }

    private Energizer3D createAnimatedEnergizer3D(Vector2i tile, float radius, float minScaling, float maxScaling, List<PhongMaterial> ghostMaterials) {
        final var energizer3D = createEnergizer3D(tile, radius, minScaling, maxScaling);

        final House house = level.worldMap().terrainLayer().optHouse().orElseThrow();
        final Vector2i[] ghostRevivalTiles = {
            house.ghostRevivalTile(RED_GHOST_SHADOW),
            house.ghostRevivalTile(PINK_GHOST_SPEEDY),
            house.ghostRevivalTile(CYAN_GHOST_BASHFUL),
            house.ghostRevivalTile(ORANGE_GHOST_POKEY),
        };

        final Vector2f[] ghostRevivalCenters = {
            revivalPositionCenter(ghostRevivalTiles[RED_GHOST_SHADOW]),
            revivalPositionCenter(ghostRevivalTiles[PINK_GHOST_SPEEDY]),
            revivalPositionCenter(ghostRevivalTiles[CYAN_GHOST_BASHFUL]),
            revivalPositionCenter(ghostRevivalTiles[ORANGE_GHOST_POKEY])
        };

        setEatenAnimation(energizer3D, ghostMaterials.toArray(PhongMaterial[]::new), ghostRevivalCenters);
        return energizer3D;
    }

    private Vector2f revivalPositionCenter(Vector2i revivalTile) {
        return revivalTile.scaled(8f).plus(TS, HTS);
    }

    private Energizer3D createEnergizer3D(Vector2i tile, float energizerRadius, float minScaling, float maxScaling) {
        final var energizerCenter = new Point3D(tile.x() * TS + HTS, tile.y() * TS + HTS, -6);
        return new SphericalEnergizer3D(
            animationRegistry,
            energizerRadius,
            energizerCenter,
            minScaling,
            maxScaling,
            pelletMaterial,
            tile);
    }

    private void setEatenAnimation(Energizer3D energizer3D, Material[] ghostParticleMaterials, Vector2f[] ghostRevivalPositions) {
        final Point3D energizerCenter = new Point3D(
            energizer3D.shape().getTranslateX(),
            energizer3D.shape().getTranslateY(),
            energizer3D.shape().getTranslateZ());

        final var animation = new EnergizerExplosionAndRecyclingAnimation(
            animationRegistry,
            energizerCenter,
            house3D,
            ghostRevivalPositions,
            particleGroupsContainer,
            particleMaterial,
            ghostParticleMaterials,
            this::particleTouchesFloor);

        energizer3D.setEatenAnimation(animation);
    }

    private boolean particleTouchesFloor(EnergizerExplosionAndRecyclingAnimation.Particle particle) {
        final Vector2i worldSizePx = level.worldMap().terrainLayer().sizeInPixel();
        final Point3D center = particle.center();
        final double r = particle.getRadius(), cx = center.getX(), cy = center.getY();
        if (cx + r < 0 || cx - r > worldSizePx.x()) return false;
        if (cy + r < 0 || cy - r > worldSizePx.y()) return false;
        return center.getZ() >= 0;
    }

    private void handleHouseOpenChange(ObservableValue<? extends Boolean> obs, boolean wasOpen, boolean isOpen) {
        if (isOpen && house3D != null) {
            house3D.doorsOpenCloseAnimation().playFromStart();
        }
    }
}
