/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.StopWatch;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.Obstacle;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.EnergizerExplosionAndRecycling;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.*;
import de.amr.pacmanfx.uilib.widgets.MessageView;
import javafx.animation.*;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.*;
import static de.amr.pacmanfx.uilib.Ufx.*;
import static java.util.Objects.requireNonNull;

/**
 * 3D representation of game level.
 */
public class GameLevel3D extends Group implements Disposable {

    private final IntegerProperty livesCountProperty     = new SimpleIntegerProperty(0);
    private final DoubleProperty  wallBaseHeightProperty = new SimpleDoubleProperty(Wall3D.DEFAULT_BASE_HEIGHT);
    private final DoubleProperty  wallOpacityProperty    = new SimpleDoubleProperty(1);

    protected final GameUI ui;
    protected final GameLevel gameLevel;
    protected final WorldMapColorScheme colorScheme;

    private final AnimationRegistry animationRegistry = new AnimationRegistry();
    private ManagedAnimation wallColorFlashingAnimation;
    private ManagedAnimation levelCompletedFullAnimation;
    private ManagedAnimation levelCompletedShortAnimation;
    private ManagedAnimation ghostLightAnimation;

    private MeshView[] ghostDressMeshViews;
    private MeshView[] ghostPupilsMeshViews;
    private MeshView[] ghostEyesMeshViews;

    private Node[] livesCounterShapes;

    private PhongMaterial floorMaterial;
    private PhongMaterial wallBaseMaterial;
    private PhongMaterial wallTopMaterial;
    private PhongMaterial pelletMaterial;
    private PhongMaterial particleMaterial;

    protected AmbientLight ambientLight;
    protected PointLight ghostLight;

    protected Group maze3D;
    protected Box floor3D;
    protected ArcadeHouse3D house3D;
    protected LevelCounter3D levelCounter3D;
    protected LivesCounter3D livesCounter3D;
    protected PacBase3D pac3D;
    protected List<MutatingGhost3D> ghosts3D;
    protected Bonus3D bonus3D;
    protected Set<Shape3D> pellets3D;
    protected Set<Energizer3D> energizers3D;
    protected Group particleGroupsContainer = new Group();
    protected MessageView messageView;

    private int wall3DCount;

    private Animation wallsMovingUpAndDown(int numFlashes) {
        if (numFlashes == 0) {
            return pauseSec(1.0);
        }
        var timeline = new Timeline(
            new KeyFrame(Duration.millis(0.5 * 250),
                new KeyValue(wallBaseHeightProperty, 0, Interpolator.EASE_BOTH)
            )
        );
        timeline.setAutoReverse(true);
        timeline.setCycleCount(2 * numFlashes);
        return timeline;
    }

    private class LevelCompletedAnimation extends ManagedAnimation {
        private static final int MESSAGE_FREQUENCY = 20; // 20% of cases
        private static final float SPINNING_SECONDS = 1.5f;

        public LevelCompletedAnimation(AnimationRegistry animationRegistry) {
            super(animationRegistry, "Level_Completed");
        }

        @Override
        protected Animation createAnimationFX() {
            return new SequentialTransition(
                doNow(() -> sometimesLevelCompleteMessage(gameLevel.number())),
                pauseSec(0.5, () -> gameLevel.ghosts().forEach(Ghost::hide)),
                wallsMovingUpAndDown(gameLevel.data().numFlashes()),
                pauseSec(0.5, () -> gameLevel.pac().hide()),
                pauseSec(0.5),
                levelSpinningAroundAxis(new Random().nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS),
                pauseSec(0.5, () -> ui.soundManager().play(SoundID.LEVEL_COMPLETE)),
                pauseSec(0.5),
                wallsAndHouseDisappearing(),
                pauseSec(1.0, () -> ui.soundManager().play(SoundID.LEVEL_CHANGED))
            );
        }

        private Animation wallsAndHouseDisappearing() {
            return new Timeline(
                new KeyFrame(Duration.seconds(0.5), new KeyValue(house3D.wallBaseHeightProperty(), 0, Interpolator.EASE_IN)),
                new KeyFrame(Duration.seconds(1.5), new KeyValue(wallBaseHeightProperty, 0, Interpolator.EASE_IN)),
                new KeyFrame(Duration.seconds(2.5), e -> maze3D.setVisible(false))
            );
        }

        private void sometimesLevelCompleteMessage(int levelNumber) {
            if (randomInt(0, 100) < MESSAGE_FREQUENCY) {
                String message = ui.assets().translatedLevelCompleteMessage(levelNumber);
                ui.showFlashMessage(Duration.seconds(3), message);
            }
        }

        private Animation levelSpinningAroundAxis(Point3D axis) {
            var spin360 = new RotateTransition(Duration.seconds(SPINNING_SECONDS), GameLevel3D.this);
            spin360.setAxis(axis);
            spin360.setFromAngle(0);
            spin360.setToAngle(360);
            spin360.setInterpolator(Interpolator.LINEAR);
            return spin360;
        }
    }

    private class LevelCompletedAnimationShort extends ManagedAnimation {

        public LevelCompletedAnimationShort(AnimationRegistry animationRegistry) {
            super(animationRegistry, "Level_Complete_Short_Animation");
        }

        @Override
        protected Animation createAnimationFX() {
            return new SequentialTransition(
                pauseSec(0.5, () -> gameLevel.ghosts().forEach(Ghost::hide)),
                pauseSec(0.5),
                wallsMovingUpAndDown(gameLevel.data().numFlashes()),
                pauseSec(0.5, () -> gameLevel.pac().hide())
            );
        }
    }

    private class WallColorFlashingAnimation extends ManagedAnimation {

        public WallColorFlashingAnimation(AnimationRegistry animationRegistry) {
            super(animationRegistry, "MazeWallColorFlashing");
        }

        @Override
        protected Animation createAnimationFX() {
            return new Transition() {
                {
                    setAutoReverse(true);
                    setCycleCount(Animation.INDEFINITE);
                    setCycleDuration(Duration.seconds(0.25));
                }
                @Override
                protected void interpolate(double t) {
                    Color color = colorScheme.fill().interpolate(colorScheme.stroke(), t);
                    wallTopMaterial.setDiffuseColor(color);
                    wallTopMaterial.setSpecularColor(color.brighter());
                }
            };
        }

        @Override
        public void stop() {
            super.stop();
            // reset wall colors when stopped
            wallTopMaterial.setDiffuseColor(colorScheme.fill());
            wallTopMaterial.setSpecularColor(colorScheme.fill().brighter());
        }
    }

    private class GhostLightAnimation extends ManagedAnimation {

        private byte currentlyLightedGhost;

        public GhostLightAnimation(AnimationRegistry animationRegistry) {
            super(animationRegistry, "GhostLighting");
            currentlyLightedGhost = RED_GHOST_SHADOW;
        }

        @Override
        protected Animation createAnimationFX() {
            var timeline = new Timeline(
                new KeyFrame(Duration.millis(3000), e -> {
                    for (int i = 1; i <= 3; ++i) {
                        currentlyLightedGhost = (byte) ((currentlyLightedGhost + 1) % 4);
                        if (gameLevel.ghost(currentlyLightedGhost).state() == GhostState.HUNTING_PAC) {
                            break;
                        }
                    }
                    MutatingGhost3D currentGhost3D = ghosts3D.get(currentlyLightedGhost);
                    ghostLight.setColor(currentGhost3D.coloring().normalDressColor());
                    ghostLight.translateXProperty().bind(currentGhost3D.translateXProperty());
                    ghostLight.translateYProperty().bind(currentGhost3D.translateYProperty());
                    ghostLight.setTranslateZ(-25);
                })
            );
            timeline.setCycleCount(Animation.INDEFINITE);
            return timeline;
        }

        @Override
        public void playFromStart() {
            ghostLight.setLightOn(true);
            currentlyLightedGhost = RED_GHOST_SHADOW;
            super.playFromStart();
        }

        @Override
        public void stop() {
            ghostLight.setLightOn(false);
            super.stop();
        }
    }

    /**
     * @param ui the game UI
     */
    public GameLevel3D(GameUI ui) {
        this.ui = requireNonNull(ui);
        this.gameLevel = requireNonNull(ui.gameContext().gameLevel());

        wallOpacityProperty.bind(PROPERTY_3D_WALL_OPACITY);

        wallBaseHeightProperty.bind(PROPERTY_3D_WALL_HEIGHT);
        PROPERTY_3D_DRAW_MODE.addListener(this::handleDrawModeChange);

        setMouseTransparent(true); // this increases performance, they say...

        colorScheme = createWorldMapColorScheme();
        createMaterials();
        createGhostMeshViews(
            (int) gameLevel.ghosts().count(),
            ui.assets().theModel3DRepository().ghostDressMesh(),
            ui.assets().theModel3DRepository().ghostPupilsMesh(),
            ui.assets().theModel3DRepository().ghostEyeballsMesh()
        );

        createLevelCounter3D();
        createLivesCounter3D();
        createPac3D();
        createGhosts3D(ui.gameContext());
        createMaze3D();
        createPellets3D();
        createEnergizers3D();
        createAmbientLight();
        createGhostLight();

        wallColorFlashingAnimation = new WallColorFlashingAnimation(animationRegistry);
        levelCompletedFullAnimation = new LevelCompletedAnimation(animationRegistry);
        levelCompletedShortAnimation = new LevelCompletedAnimationShort(animationRegistry);
        ghostLightAnimation = new GhostLightAnimation(animationRegistry);

        getChildren().add(floor3D);
        getChildren().add(levelCounter3D);
        getChildren().add(livesCounter3D);
        getChildren().addAll(pac3D, pac3D.light());
        getChildren().addAll(ghosts3D);
        getChildren().addAll(house3D.swirls());
        getChildren().add(particleGroupsContainer);
        getChildren().addAll(energizers3D.stream().map(Energizer3D::shape).toList());
        getChildren().addAll(pellets3D);
        getChildren().add(house3D.doors()); // !!
        // Note: The order in which children are added to the root matters!
        // Walls and house must be added *after* the actors and swirls, otherwise the transparency is not working correctly.
        getChildren().add(maze3D);
        getChildren().add(ambientLight);
        getChildren().add(ghostLight);

        house3D.startSwirlAnimations();
    }

    private void createMaterials() {
        pelletMaterial = new PhongMaterial(colorScheme.pellet());
        pelletMaterial.setSpecularColor(colorScheme.pellet().brighter());

        particleMaterial = new PhongMaterial(colorScheme.pellet().deriveColor(0, 0.5, 1.5, 0.5));
        particleMaterial.setSpecularColor(particleMaterial.getDiffuseColor().brighter());

        floorMaterial = new PhongMaterial();
        floorMaterial.diffuseColorProperty().bind(PROPERTY_3D_FLOOR_COLOR);
        floorMaterial.specularColorProperty().bind(floorMaterial.diffuseColorProperty().map(Color::brighter));
        floorMaterial.setSpecularPower(128);

        wallBaseMaterial = new PhongMaterial();
        //TODO the opacity change does not work as expected. Why?
        var diffuseColor = wallOpacityProperty.map(opacity -> colorWithOpacity(colorScheme.stroke(), opacity.doubleValue()));
        wallBaseMaterial.diffuseColorProperty().bind(diffuseColor);
        wallBaseMaterial.specularColorProperty().bind(diffuseColor.map(Color::brighter));
        wallBaseMaterial.setSpecularPower(64);

        wallTopMaterial = new PhongMaterial();
        wallTopMaterial.setDiffuseColor(colorScheme.fill());
        wallTopMaterial.setSpecularColor(colorScheme.fill().brighter());
    }

    private void disposeMaterials() {
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
        Logger.info("Unbound material references");
    }

    private WorldMapColorScheme createWorldMapColorScheme() {
        WorldMap worldMap = gameLevel.worldMap();
        WorldMapColorScheme proposedColorScheme = ui.currentConfig().colorScheme(worldMap);
        requireNonNull(proposedColorScheme);
        // Add some contrast with floor if wall fill color is black
        return proposedColorScheme.fill().equals(Color.BLACK)
            ? new WorldMapColorScheme(Color.grayRgb(42), proposedColorScheme.stroke(), proposedColorScheme.door(), proposedColorScheme.pellet())
            : proposedColorScheme;
    }

    private void createGhostMeshViews(int numGhosts, Mesh ghostDressMesh, Mesh ghostPupilsMesh, Mesh ghostEyeballsMesh) {
        ghostDressMeshViews  = IntStream.range(0, numGhosts).mapToObj(i -> new MeshView(ghostDressMesh)).toArray(MeshView[]::new);
        ghostPupilsMeshViews = IntStream.range(0, numGhosts).mapToObj(i -> new MeshView(ghostPupilsMesh)).toArray(MeshView[]::new);
        ghostEyesMeshViews   = IntStream.range(0, numGhosts).mapToObj(i -> new MeshView(ghostEyeballsMesh)).toArray(MeshView[]::new);
    }

    private void disposeGhostMeshViews() {
        if (ghostDressMeshViews != null) {
            for (MeshView meshView : ghostDressMeshViews) {
                meshView.setMesh(null);
                meshView.materialProperty().unbind();
                meshView.setMaterial(null);
            }
            ghostDressMeshViews = null;
            Logger.info("Cleared dress mesh views");
        }
        if (ghostPupilsMeshViews != null) {
            for (MeshView meshView : ghostPupilsMeshViews) {
                meshView.setMesh(null);
                meshView.materialProperty().unbind();
                meshView.setMaterial(null);
            }
            ghostPupilsMeshViews = null;
            Logger.info("Cleared pupils mesh views");
        }
        if (ghostEyesMeshViews != null) {
            for (MeshView meshView : ghostEyesMeshViews) {
                meshView.setMesh(null);
                meshView.materialProperty().unbind();
                meshView.setMaterial(null);
            }
            ghostEyesMeshViews = null;
            Logger.info("Cleared eyes mesh views");
        }
    }

    private void createGhosts3D(GameContext gameContext) {
        ghosts3D = gameLevel.ghosts().map(ghost -> {
            var ghostColoring = new GhostColoring(
                ui.currentConfig().assets().color("ghost.%d.color.normal.dress".formatted(ghost.id().personality())),
                ui.currentConfig().assets().color("ghost.%d.color.normal.pupils".formatted(ghost.id().personality())),
                ui.currentConfig().assets().color("ghost.%d.color.normal.eyeballs".formatted(ghost.id().personality())),
                ui.currentConfig().assets().color("ghost.color.frightened.dress"),
                ui.currentConfig().assets().color("ghost.color.frightened.pupils"),
                ui.currentConfig().assets().color("ghost.color.frightened.eyeballs"),
                ui.currentConfig().assets().color("ghost.color.flashing.dress"),
                ui.currentConfig().assets().color("ghost.color.flashing.pupils")
            );
            return new MutatingGhost3D(
                animationRegistry,
                gameLevel,
                ghost,
                ghostColoring,
                ghostDressMeshViews[ghost.id().personality()],
                ghostPupilsMeshViews[ghost.id().personality()],
                ghostEyesMeshViews[ghost.id().personality()],
                ui.preferences().getFloat("3d.ghost.size"),
                gameLevel.data().numFlashes()
            );
        }).toList();
        ghosts3D.forEach(ghost3D -> ghost3D.init(gameContext, gameLevel));
    }

    private void createPac3D() {
        pac3D = ui.currentConfig().createPac3D(animationRegistry, gameLevel, gameLevel.pac());
        pac3D.init(gameLevel);
    }

    private void createLivesCounter3D() {
        int capacity = ui.preferences().getInt("3d.lives_counter.capacity");
        Color pillarColor = ui.preferences().getColor("3d.lives_counter.pillar_color");
        Color plateColor = ui.preferences().getColor("3d.lives_counter.plate_color");
        livesCounterShapes = new Node[capacity];
        for (int i = 0; i < livesCounterShapes.length; ++i) {
            livesCounterShapes[i] = ui.currentConfig().createLivesCounterShape3D();
        }
        livesCounter3D = new LivesCounter3D(animationRegistry, livesCounterShapes);
        livesCounter3D.setTranslateX(2 * TS);
        livesCounter3D.setTranslateY(2 * TS);
        livesCounter3D.livesCountProperty().bind(livesCountProperty);
        livesCounter3D.pillarColorProperty().set(pillarColor);
        livesCounter3D.plateColorProperty().set(plateColor);
    }

    private void createLevelCounter3D() {
        WorldMap worldMap = gameLevel.worldMap();
        levelCounter3D = new LevelCounter3D(animationRegistry);
        levelCounter3D.setTranslateX(TS * (worldMap.numCols() - 2));
        levelCounter3D.setTranslateY(2 * TS);
        levelCounter3D.setTranslateZ(-ui.preferences().getFloat("3d.level_counter.elevation"));
    }

    private void createAmbientLight() {
        ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PROPERTY_3D_LIGHT_COLOR);
    }

    private void createGhostLight() {
        ghostLight = new PointLight(Color.WHITE);
        ghostLight.setLightOn(true);
        ghostLight.setMaxRange(30);
        ghostLight.lightOnProperty().addListener((obs, wasOn, isOn)
                -> Logger.info("Ghost light is {}", isOn ? "on" : "off"));
    }

    private void createMaze3D() {
        Logger.info("Building 3D maze for map (URL '{}'), color scheme: {}...", gameLevel.worldMap().url(), colorScheme);

        maze3D = new Group();

        var r3D = new TerrainRenderer3D();
        r3D.setOnWallCreated(wall3D -> {
            wall3D.bindBaseHeight(wallBaseHeightProperty);
            wall3D.setBaseMaterial(wallBaseMaterial);
            wall3D.setTopMaterial(wallTopMaterial);
            ++wall3DCount;
            maze3D.getChildren().addAll(wall3D.base(), wall3D.top());
            return wall3D;
        });

        createFloor3D();

        float wallThickness = ui.preferences().getFloat("3d.obstacle.wall_thickness");
        float cornerRadius = ui.preferences().getFloat("3d.obstacle.corner_radius");
        wall3DCount = 0;
        var stopWatch = new StopWatch();
        for (Obstacle obstacle : gameLevel.worldMap().obstacles()) {
            // exclude house placeholder
            Vector2i startTile = tileAt(obstacle.startPoint().toVector2f());
            if (gameLevel.house().isPresent() && !gameLevel.house().get().isTileInHouseArea(startTile)) {
                r3D.renderObstacle3D(obstacle, isWorldBorder(gameLevel.worldMap(), obstacle), wallThickness, cornerRadius);
            }
        }
        var passedTimeMillis = stopWatch.passedTime().toMillis();
        Logger.info("Built 3D maze with {} composite walls in {} milliseconds", wall3DCount, passedTimeMillis);

        gameLevel.house().ifPresent(house -> {
            Vector2i[] ghostRevivalTiles = {
                house.ghostRevivalTile(gameLevel.ghost(CYAN_GHOST_BASHFUL).id()),
                house.ghostRevivalTile(gameLevel.ghost(PINK_GHOST_SPEEDY).id()),
                house.ghostRevivalTile(gameLevel.ghost(ORANGE_GHOST_POKEY).id()),
            };
            // Note: revival tile is the left of the pair of tiles in the house where the ghost is placed. The center
            //       of the 3D shape is one tile to the right and a half tile to the bottom from the tile origin.
            Vector2f[] ghostRevivalPositions = Stream.of(ghostRevivalTiles)
                .map(tile -> tile.scaled((float) TS).plus(TS, HTS))
                .toArray(Vector2f[]::new);

            house3D = new ArcadeHouse3D(
                animationRegistry,
                house,
                ghostRevivalPositions,
                ui.preferences().getFloat("3d.house.base_height"),
                ui.preferences().getFloat("3d.house.wall_thickness"),
                ui.preferences().getFloat("3d.house.opacity")
            );
            house3D.setWallBaseColor(colorScheme.fill());
            house3D.setWallTopColor(colorScheme.stroke());
            house3D.setDoorColor(colorScheme.door());
            house3D.wallBaseHeightProperty().set(ui.preferences().getFloat("3d.house.base_height"));
            house3D.openProperty().addListener(this::handleHouseOpenChange);
            house3D.setDoorSensitivity(ui.preferences().getFloat("3d.house.sensitivity"));
            maze3D.getChildren().add(house3D);
        });
    }

    private void createFloor3D() {
        float padding   = ui.preferences().getFloat("3d.floor.padding");
        float thickness = ui.preferences().getFloat("3d.floor.thickness");
        Vector2i worldSizePx = gameLevel.worldSizePx();
        float sizeX = worldSizePx.x() + 2 * padding;
        float sizeY = worldSizePx.y();
        floor3D = new Box(sizeX, sizeY, thickness);
        floor3D.setMaterial(floorMaterial);
        // Translate: top-left corner (without padding) at origin, surface top at z=0
        Translate translate = new Translate(0.5 * sizeX - padding, 0.5 * sizeY, 0.5 * thickness);
        floor3D.getTransforms().add(translate);
    }

    private float floorTopZ() {
        return 0;
    }

    private boolean particleTouchesFloor(EnergizerExplosionAndRecycling.Particle particle) {
        Vector2i worldSizePx = gameLevel.worldSizePx();
        Point3D center = particle.center();
        double r = particle.getRadius(), cx = center.getX(), cy = center.getY();
        if (cx + r < 0 || cx - r > worldSizePx.x()) return false;
        if (cy + r < 0 || cy - r > worldSizePx.y()) return false;
        return center.getZ() >= floorTopZ();
    }

    private boolean isWorldBorder(WorldMap worldMap, Obstacle obstacle) {
        Vector2i start = obstacle.startPoint();
        if (obstacle.isClosed()) {
            return start.x() == TS || start.y() == GameLevel.EMPTY_ROWS_OVER_MAZE * TS + HTS;
        } else {
            return start.x() == 0 || start.x() == worldMap.numCols() * TS;
        }
    }

    private void createPellets3D() {
        float radius = ui.preferences().getFloat("3d.pellet.radius");
        Mesh mesh = ui.assets().theModel3DRepository().pelletMesh();
        var prototype = new MeshView(mesh);
        Bounds bounds = prototype.getBoundsInLocal();
        double maxExtent = Math.max(Math.max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
        double scaling = (2 * radius) / maxExtent;
        var scale = new Scale(scaling, scaling, scaling);
        pellets3D = gameLevel.tiles().filter(gameLevel::tileContainsFood)
            .filter(tile -> !gameLevel.isEnergizerPosition(tile))
            .map(tile -> createPellet3D(mesh, scale, tile))
            .collect(Collectors.toCollection(HashSet::new));
    }

    private Shape3D createPellet3D(Mesh mesh, Scale scale, Vector2i tile) {
        var meshView = new MeshView(mesh);
        meshView.setMaterial(pelletMaterial);
        meshView.setRotationAxis(Rotate.Z_AXIS);
        meshView.setRotate(90);
        meshView.setTranslateX(tile.x() * TS + HTS);
        meshView.setTranslateY(tile.y() * TS + HTS);
        meshView.setTranslateZ(-floorTopZ() - 6);
        meshView.getTransforms().add(scale);
        meshView.setUserData(tile);
        return meshView;
    }

    private void createEnergizers3D() {
        float radius     = ui.preferences().getFloat("3d.energizer.radius");
        float minScaling = ui.preferences().getFloat("3d.energizer.scaling.min");
        float maxScaling = ui.preferences().getFloat("3d.energizer.scaling.max");
        Material[] ghostDressMaterials = {
            ghosts3D.get(RED_GHOST_SHADOW).ghost3D().dressMaterialNormal(),
            ghosts3D.get(PINK_GHOST_SPEEDY).ghost3D().dressMaterialNormal(),
            ghosts3D.get(CYAN_GHOST_BASHFUL).ghost3D().dressMaterialNormal(),
            ghosts3D.get(ORANGE_GHOST_POKEY).ghost3D().dressMaterialNormal(),
        };
        House house = gameLevel.house().orElseThrow();
        Vector2i[] ghostRevivalTiles = {
            house.ghostRevivalTile(gameLevel.ghost(RED_GHOST_SHADOW).id()),
            house.ghostRevivalTile(gameLevel.ghost(PINK_GHOST_SPEEDY).id()),
            house.ghostRevivalTile(gameLevel.ghost(CYAN_GHOST_BASHFUL).id()),
            house.ghostRevivalTile(gameLevel.ghost(ORANGE_GHOST_POKEY).id()),
        };

        Vector2f[] ghostRevivalCenters = {
            revivalPositionCenter(ghostRevivalTiles[RED_GHOST_SHADOW]),
            revivalPositionCenter(ghostRevivalTiles[PINK_GHOST_SPEEDY]),
            revivalPositionCenter(ghostRevivalTiles[CYAN_GHOST_BASHFUL]),
            revivalPositionCenter(ghostRevivalTiles[ORANGE_GHOST_POKEY])
        };

        energizers3D = gameLevel.tiles().filter(gameLevel::tileContainsFood)
            .filter(gameLevel::isEnergizerPosition)
            .map(tile -> createEnergizer3D(tile, radius, minScaling, maxScaling, ghostDressMaterials, ghostRevivalCenters))
            .collect(Collectors.toCollection(HashSet::new));
    }

    private Vector2f revivalPositionCenter(Vector2i revivalTile) {
        return revivalTile.scaled(8f).plus(TS, HTS);
    }

    private Energizer3D createEnergizer3D(
        Vector2i tile,
        float energizerRadius,
        float minScaling,
        float maxScaling,
        Material[] ghostDressMaterials,
        Vector2f[] ghostRevivalPositions)
    {
        var energizerCenter = new Point3D(tile.x() * TS + HTS, tile.y() * TS + HTS, floorTopZ() - 6);
        var energizer3D = new SphericalEnergizer3D(
            animationRegistry,
            energizerRadius,
            energizerCenter,
            minScaling,
            maxScaling,
            pelletMaterial,
            tile);

        var explosion = new EnergizerExplosionAndRecycling(
            animationRegistry,
            energizerCenter,
            house3D,
            ghostRevivalPositions,
            particleGroupsContainer,
            particleMaterial,
            ghostDressMaterials,
            this::particleTouchesFloor);

        energizer3D.setEatenAnimation(explosion);
        return energizer3D;
    }

    public Box floor3D() {
        return floor3D;
    }

    public PacBase3D pac3D() { return pac3D; }
    public List<MutatingGhost3D> ghosts3D() { return Collections.unmodifiableList(ghosts3D); }
    public Optional<Bonus3D> bonus3D() { return Optional.ofNullable(bonus3D); }
    public Set<Shape3D> pellets3D() { return Collections.unmodifiableSet(pellets3D); }
    public Set<Energizer3D> energizers3D() { return Collections.unmodifiableSet(energizers3D); }

    public AnimationRegistry animationManager() { return animationRegistry; }

    public void playWallColorFlashing() {
        wallColorFlashingAnimation.playFromStart();
    }

    public void stopWallColorFlashing() {
        wallColorFlashingAnimation.stop();
    }

    /**
     * Called on each clock tick (frame).
     */
    public void tick(GameContext gameContext) {
        pac3D.update(gameLevel);
        ghosts3D.forEach(ghost3D -> ghost3D.update(gameContext, gameLevel));
        bonus3D().ifPresent(bonus3D -> bonus3D.update(ui.gameContext()));
        if (house3D != null) {
            house3D.tick(gameLevel);
        }
        int livesCounterSize = ui.gameContext().game().lifeCount() - 1;
        // when the game starts and Pac-Man is not yet visible, show one more
        boolean oneMore = ui.gameContext().gameState() == GamePlayState.STARTING_GAME_OR_LEVEL && !gameLevel.pac().isVisible();
        if (oneMore) livesCounterSize += 1;
        livesCountProperty.set(livesCounterSize);

        boolean visible = ui.gameContext().game().canStartNewGame();
        if (livesCounter3D != null) {
            livesCounter3D.setVisible(visible);
        }
    }

    public void onStartingGame() {
        energizers3D().forEach(Energizer3D::stopPumping);
        if (levelCounter3D != null) {
            levelCounter3D.update(ui, ui.gameContext().game());
        }
    }

    public void onHuntingStart(GameContext gameContext) {
        pac3D.init(gameLevel);
        ghosts3D.forEach(ghost3D -> ghost3D.init(gameContext, gameLevel));
        energizers3D().forEach(Energizer3D::startPumping);
        house3D.startSwirlAnimations();
        ghostLightAnimation.playFromStart();
    }

    public void onPacManDying(GameState state) {
        state.timer().resetIndefiniteTime(); // expires when level animation ends
        ui.soundManager().stopAll();
        ghostLightAnimation.stop();
        // do one last update before dying animation starts
        pac3D.update(gameLevel);
        ghosts3D.forEach(MutatingGhost3D::stopAllAnimations);
        bonus3D().ifPresent(Bonus3D::expire);
        var animation = new SequentialTransition(
                pauseSec(2),
                doNow(() -> ui.soundManager().play(SoundID.PAC_MAN_DEATH)),
                pac3D.dyingAnimation().getOrCreateAnimationFX(),
                pauseSec(1)
        );
        // Note: adding this inside the animation as last action does not work!
        animation.setOnFinished(e -> ui.gameContext().gameController().letCurrentGameStateExpire());
        animation.play();
    }

    public void onGhostDying() {
        ui.gameContext().game().simulationStep().killedGhosts.forEach(killedGhost -> {
            byte personality = killedGhost.id().personality();
            int killedIndex = gameLevel.victims().indexOf(killedGhost);
            Image pointsImage = ui.currentConfig().killedGhostPointsImage(killedGhost, killedIndex);
            ghosts3D.get(personality).setNumberImage(pointsImage);
        });
    }

    public void onLevelComplete(GameState state, ObjectProperty<PerspectiveID> perspectiveIDProperty) {
        state.timer().resetIndefiniteTime(); // expires when animation ends
        ui.soundManager().stopAll();
        animationRegistry.stopAllAnimations();
        energizers3D.forEach(Energizer3D::stopPumping); //TODO needed?
        // hide 3D food explicitly because level might have been completed using cheat!
        pellets3D.forEach(pellet3D -> pellet3D.setVisible(false));
        energizers3D.forEach(Energizer3D::hide);
        particleGroupsContainer.getChildren().clear();
        for (Group swirl : house3D.swirls()) {
            swirl.getChildren().clear();
        }
        house3D.setDoorsVisible(false);
        bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
        if (messageView != null) {
            messageView.setVisible(false);
        }
        boolean cutSceneFollows = ui.gameContext().game().optCutSceneNumber(gameLevel.number()).isPresent();
        ManagedAnimation levelCompletedAnimation = cutSceneFollows
            ? levelCompletedShortAnimation
            : levelCompletedFullAnimation;

        var animation = new SequentialTransition(
            pauseSec(2, () -> {
                perspectiveIDProperty.unbind();
                perspectiveIDProperty.set(PerspectiveID.TOTAL);
                wallBaseHeightProperty.unbind();
            }),
            levelCompletedAnimation.getOrCreateAnimationFX(),
            pauseSec(1)
        );
        animation.setOnFinished(e -> {
            wallBaseHeightProperty.bind(PROPERTY_3D_WALL_HEIGHT);
            perspectiveIDProperty.bind(PROPERTY_3D_PERSPECTIVE);
            ui.gameContext().gameController().letCurrentGameStateExpire();
        });
        animation.play();
    }

    public void onGameOver(GameState state) {
        state.timer().restartSeconds(3);
        ghostLightAnimation.stop();
        energizers3D().forEach(Energizer3D::hide);
        house3D.stopSwirlAnimations();
        bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
        ui.soundManager().stopAll();
        ui.soundManager().play(SoundID.GAME_OVER);
        boolean inOneOf4Cases = randomInt(0, 1000) < 250;
        if (!gameLevel.isDemoLevel() && inOneOf4Cases) {
            ui.showFlashMessage(Duration.seconds(2.5), ui.assets().translatedGameOverMessage());
        }
    }

    public void showAnimatedMessage(String messageText, float displaySeconds, double centerX, double centerY) {
        if (messageView != null) {
            messageView.dispose();
            getChildren().remove(messageView);
        }
        messageView = MessageView.builder()
            .backgroundColor(Color.BLACK)
            .borderColor(Color.WHITE)
            .displaySeconds(displaySeconds)
            .font(ui.assets().arcadeFont(6))
            .text(messageText)
            .textColor(Color.YELLOW)
            .build(animationRegistry);
        getChildren().add(messageView);
        messageView.showCenteredAt(centerX, centerY);
    }

    public void updateBonus3D(Bonus bonus) {
        requireNonNull(bonus);
        if (bonus3D != null) {
            getChildren().remove(bonus3D);
            bonus3D.dispose();
        }
        bonus3D = new Bonus3D(animationRegistry, bonus,
            ui.currentConfig().bonusSymbolImage(bonus.symbol()), ui.preferences().getFloat("3d.bonus.symbol.width"),
            ui.currentConfig().bonusValueImage(bonus.symbol()), ui.preferences().getFloat("3d.bonus.points.width"));
        getChildren().add(bonus3D);
        bonus3D.showEdible();
    }

    public void updateLevelCounter3D() {
        if (levelCounter3D != null) {
            levelCounter3D.update(ui, ui.gameContext().game());
        }
    }

    private void handleHouseOpenChange(ObservableValue<? extends Boolean> obs,  boolean wasOpen, boolean isOpen) {
        if (isOpen && house3D != null) {
            house3D.doorsOpenCloseAnimation().playFromStart();
        }
    }

    private void handleDrawModeChange(ObservableValue<? extends DrawMode> obs, DrawMode oldDrawMode, DrawMode newDrawMode) {
        try {
            Predicate<Node> includeAll = excludedNode -> false;
            setDrawModeUnder(maze3D, node -> node instanceof Shape3D && pellets3D.contains(node), newDrawMode);
            setDrawModeUnder(pac3D, includeAll, newDrawMode);
            setDrawModeUnder(livesCounter3D, includeAll, newDrawMode);
            ghosts3D.forEach(ghost3D -> setDrawModeUnder(ghost3D, includeAll, newDrawMode));
        }
        catch (Exception x) {
            Logger.error(x);
            Logger.error("Could not change 3D draw mode");
        }
    }

    private static void setDrawModeUnder(Node node, Predicate<Node> exclusionFilter, DrawMode drawMode) {
        if (node == null) return; //TODO why does this happen?
        node.lookupAll("*").stream()
            .filter(exclusionFilter.negate())
            .filter(Shape3D.class::isInstance)
            .map(Shape3D.class::cast)
            .forEach(shape3D -> shape3D.setDrawMode(drawMode));
    }

    // still work in progress...

    private boolean disposed = false;

    public void dispose() {
        if (disposed) {
            Logger.warn("Game level 3D already has been disposed!");
            return;
        }
        Logger.info("Disposing game level 3D...");
        disposed = true;

        animationRegistry.stopAllAnimations();
        Logger.info("Stopped all managed animations");

        if (wallColorFlashingAnimation != null) {
            wallColorFlashingAnimation.dispose();
            wallColorFlashingAnimation = null;
        }
        if (levelCompletedFullAnimation != null) {
            levelCompletedFullAnimation.dispose();
            levelCompletedFullAnimation = null;
        }
        if (levelCompletedShortAnimation != null) {
            levelCompletedShortAnimation.dispose();
            levelCompletedShortAnimation = null;
        }
        if (ghostLightAnimation != null) {
            ghostLightAnimation.dispose();
            ghostLightAnimation = null;
        }

        // Dispose all remaining animations
        if (!animationRegistry.animations().isEmpty()) {
            Logger.info("There are {} un-disposed animations left:", animationRegistry.animations().size());
            // create a copy to avoid CME
            for (ManagedAnimation animation : new ArrayList<>(animationRegistry.animations())) {
                Logger.info("\tDisposing" + animation.label());
                animation.dispose();
            }
        }
        animationRegistry.clear();

        PROPERTY_3D_DRAW_MODE.removeListener(this::handleDrawModeChange);
        Logger.info("Removed 'draw mode' listener");

        house3D.openProperty().removeListener(this::handleHouseOpenChange);
        Logger.info("Removed 'house open' listener");

        house3D.openProperty().unbind();
        house3D.wallBaseHeightProperty().unbind();
        house3D.light().lightOnProperty().unbind();

        livesCountProperty.unbind();
        wallBaseHeightProperty.unbind();
        wallOpacityProperty.unbind();

        getChildren().clear();
        Logger.info("Removed all nodes under game level");

        if (ambientLight != null) {
            ambientLight.colorProperty().unbind();
            ambientLight = null;
            Logger.info("Unbound and cleared ambient light");
        }
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
        if (floor3D != null) {
            floor3D.translateXProperty().unbind();
            floor3D.translateYProperty().unbind();
            floor3D.translateZProperty().unbind();
            floor3D.materialProperty().unbind();
            floor3D = null;
            Logger.info("Unbound and cleared 3D floor");
        }
        if (house3D != null) {
            house3D.dispose();
            house3D = null;
            Logger.info("Disposed 3D house");
        }
        if (maze3D != null) {
            maze3D.getChildren().forEach(Wall3D::dispose);
            maze3D.getChildren().clear();
            maze3D = null;
            Logger.info("Disposed 3D maze");
        }
        if (livesCounterShapes != null) {
            disposeAll(List.of(livesCounterShapes));
            livesCounterShapes = null;
        }
        if (livesCounter3D != null) {
            livesCounter3D.dispose();
            livesCounter3D = null;
            Logger.info("Disposed lives counter 3D");
        }
        if (levelCounter3D != null) {
            levelCounter3D.dispose();
            levelCounter3D = null;
            Logger.info("Disposed level counter 3D");
        }
        if (pac3D != null) {
            pac3D.dispose();
            pac3D = null;
            Logger.info("Disposed Pac 3D");
        }
        if (ghosts3D != null) {
            disposeAll(ghosts3D);
            ghosts3D = null;
            Logger.info("Disposed 3D ghosts");
        }
        if (bonus3D != null) {
            bonus3D.dispose();
            bonus3D = null;
            Logger.info("Disposed 3D bonus");
        }
        if (messageView != null) {
            messageView.dispose();
            messageView = null;
            Logger.info("Disposed message view");
        }

        disposeGhostMeshViews();
        disposeMaterials();
    }
}