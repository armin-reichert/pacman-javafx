/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.StopWatch;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.world.*;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.GlobalPreferencesManager;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.EnergizerExplosionAndRecycling;
import de.amr.pacmanfx.uilib.animation.RegisteredAnimation;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.RandomTextPicker;
import de.amr.pacmanfx.uilib.assets.Translator;
import de.amr.pacmanfx.uilib.model3D.*;
import de.amr.pacmanfx.uilib.widgets.MessageView;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static de.amr.pacmanfx.ui.GameUI.*;
import static de.amr.pacmanfx.uilib.Ufx.colorWithOpacity;
import static de.amr.pacmanfx.uilib.Ufx.defaultPhongMaterial;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.*;
import static java.util.Objects.requireNonNull;

/**
 * 3D representation of game level.
 */
public class GameLevel3D extends Group implements Disposable {

    private final DoubleProperty wallBaseHeightProperty = new SimpleDoubleProperty(Wall3D.DEFAULT_BASE_HEIGHT);
    private final DoubleProperty wallOpacityProperty    = new SimpleDoubleProperty(1);

    private final GameUI ui;
    private final UIConfig uiConfig;
    private final GameLevel level;
    private final WorldMapColorScheme colorScheme;

    private final RandomTextPicker<String> pickerLevelCompleteMessages;

    private final AnimationRegistry animationRegistry = new AnimationRegistry();
    private RegisteredAnimation wallColorFlashingAnimation;
    private RegisteredAnimation levelCompletedFullAnimation;
    private RegisteredAnimation levelCompletedShortAnimation;
    private RegisteredAnimation ghostLightAnimation;

    private MeshView[] ghostDressMeshViews;
    private MeshView[] ghostPupilsMeshViews;
    private MeshView[] ghostEyesMeshViews;

    private Node[] livesCounterShapes;

    private PhongMaterial floorMaterial;
    private PhongMaterial wallBaseMaterial;
    private PhongMaterial wallTopMaterial;
    private PhongMaterial pelletMaterial;
    private PhongMaterial particleMaterial;

    private AmbientLight ambientLight;
    private PointLight ghostLight;

    private Group maze3D;
    private Box floor3D;
    private ArcadeHouse3D house3D;
    private LevelCounter3D levelCounter3D;
    private LivesCounter3D livesCounter3D;
    private PacBase3D pac3D;
    private List<MutableGhost3D> ghosts3D;
    private Bonus3D bonus3D;
    private Set<Shape3D> pellets3D;
    private Set<Energizer3D> energizers3D;
    private Group particleGroupsContainer = new Group();
    private MessageView messageView;

    private int wall3DCount;

    private Animation wallsSwinging(int numFlashes) {
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

    private class LevelCompletedAnimation extends RegisteredAnimation {
        private static final int MESSAGE_FREQUENCY = 20; // 20% of cases
        private static final float SPINNING_SECONDS = 1.5f;

        public LevelCompletedAnimation(AnimationRegistry animationRegistry) {
            super(animationRegistry, "Level_Completed");
        }

        @Override
        protected Animation createAnimationFX() {
            return new SequentialTransition(
                //doNow(() -> sometimesLevelCompleteMessage(level.number())),
                pauseSecThen(0.5, () -> level.ghosts().forEach(Ghost::hide)),
                wallsSwinging(level.numFlashes()),
                pauseSecThen(0.5, () -> level.pac().hide()),
                pauseSec(0.5),
                levelSpinningAroundAxis(new Random().nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS),
                pauseSecThen(0.5, () -> ui.soundManager().play(SoundID.LEVEL_COMPLETE)),
                pauseSec(0.5),
                wallsAndHouseDisappearing(),
                pauseSecThen(1.0, () -> ui.soundManager().play(SoundID.LEVEL_CHANGED))
            );
        }

        private Animation wallsAndHouseDisappearing() {
            return new Timeline(
                new KeyFrame(Duration.seconds(0.5), new KeyValue(house3D.wallBaseHeightProperty(), 0, Interpolator.EASE_IN)),
                new KeyFrame(Duration.seconds(1.5), new KeyValue(wallBaseHeightProperty, 0, Interpolator.EASE_IN)),
                new KeyFrame(Duration.seconds(2.5), _ -> maze3D.setVisible(false))
            );
        }

        /*
        private void sometimesLevelCompleteMessage(int levelNumber) {
            if (randomInt(0, 100) < MESSAGE_FREQUENCY) {
                String message = translatedLevelCompleteMessage(localizedTextsAccessor, levelNumber);
                ui.showFlashMessage(Duration.seconds(3), message);
            }
        }
         */

        private Animation levelSpinningAroundAxis(Point3D axis) {
            var spin360 = new RotateTransition(Duration.seconds(SPINNING_SECONDS), GameLevel3D.this);
            spin360.setAxis(axis);
            spin360.setFromAngle(0);
            spin360.setToAngle(360);
            spin360.setInterpolator(Interpolator.LINEAR);
            return spin360;
        }
    }

    private class LevelCompletedAnimationShort extends RegisteredAnimation {

        public LevelCompletedAnimationShort(AnimationRegistry animationRegistry) {
            super(animationRegistry, "Level_Complete_Short_Animation");
        }

        @Override
        protected Animation createAnimationFX() {
            return new SequentialTransition(
                pauseSecThen(0.5, () -> level.ghosts().forEach(Ghost::hide)),
                pauseSec(0.5),
                wallsSwinging(level.numFlashes()),
                pauseSecThen(0.5, () -> level.pac().hide())
            );
        }
    }

    //TODO This animation sometimes does not stop. Why?
    private class WallColorFlashingAnimation extends RegisteredAnimation {

        public WallColorFlashingAnimation(AnimationRegistry animationRegistry) {
            super(animationRegistry, "WallColorFlashing");
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
                    Color color = Color.valueOf(colorScheme.wallFill()).interpolate(Color.valueOf(colorScheme.wallStroke()), t);
                    wallTopMaterial.setDiffuseColor(color);
                    wallTopMaterial.setSpecularColor(color.brighter());
                }
            };
        }

        @Override
        public void stop() {
            super.stop();
            // reset wall colors
            wallTopMaterial.setDiffuseColor(Color.valueOf(colorScheme.wallFill()));
            wallTopMaterial.setSpecularColor(Color.valueOf(colorScheme.wallFill()).brighter());
        }
    }

    /**
     * A light animation that switches from ghost to ghost (JavaFX can only display a limited amount of lights per scene).
     */
    private class GhostLightAnimation extends RegisteredAnimation {

        private byte currentGhostID;

        public GhostLightAnimation(AnimationRegistry animationRegistry) {
            super(animationRegistry, "GhostLight");
            currentGhostID = RED_GHOST_SHADOW;
        }

        private static byte nextGhostID(byte id) {
            return (byte) ((id + 1) % 4);
        }

        private void illuminateGhost(byte ghostID) {
            MutableGhost3D ghost3D = ghosts3D.get(ghostID);
            ghostLight.setColor(ghost3D.colorSet().normal().dress());
            ghostLight.translateXProperty().bind(ghost3D.translateXProperty());
            ghostLight.translateYProperty().bind(ghost3D.translateYProperty());
            ghostLight.setTranslateZ(-25);
            ghostLight.setLightOn(true);
            currentGhostID = ghostID;
            Logger.debug("Ghost light passed to ghost {}", currentGhostID);
        }

        @Override
        protected Animation createAnimationFX() {
            var timeline = new Timeline(new KeyFrame(Duration.millis(3000), _ -> {
                Logger.debug("Try to pass light from ghost {} to next", currentGhostID);
                // find the next hunting ghost, if exists, pass light to him
                byte candidate = nextGhostID(currentGhostID);
                while (candidate != currentGhostID) {
                    if (level.ghost(candidate).state() == GhostState.HUNTING_PAC) {
                        illuminateGhost(candidate);
                        return;
                    }
                    candidate = nextGhostID(candidate);
                }
                ghostLight.setLightOn(false);
            }));
            timeline.setCycleCount(Animation.INDEFINITE);
            return timeline;
        }

        @Override
        public void playFromStart() {
            illuminateGhost(RED_GHOST_SHADOW);
            super.playFromStart();
        }

        @Override
        public void stop() {
            ghostLight.setLightOn(false);
            super.stop();
        }
    }

    public GameLevel3D(GameUI ui, UIConfig uiConfig, GameLevel level) {
        this.ui = requireNonNull(ui);
        this.uiConfig = requireNonNull(uiConfig);
        this.level = requireNonNull(level);

        wallOpacityProperty.bind(PROPERTY_3D_WALL_OPACITY);

        wallBaseHeightProperty.bind(PROPERTY_3D_WALL_HEIGHT);
        PROPERTY_3D_DRAW_MODE.addListener(this::handleDrawModeChange);

        setMouseTransparent(true); // this increases performance, they say...

        colorScheme = createWorldMapColorScheme();

        createMaterials();
        ghostDressMeshViews  = createGhostComponentMeshViews(PacManModel3DRepository.instance().ghostDressMesh());
        ghostPupilsMeshViews = createGhostComponentMeshViews(PacManModel3DRepository.instance().ghostPupilsMesh());
        ghostEyesMeshViews   = createGhostComponentMeshViews(PacManModel3DRepository.instance().ghostEyeballsMesh());

        createLevelCounter3D();
        createLivesCounter3D();
        createPac3D(GlobalPreferencesManager.instance().getFloat("3d.pac.size"));
        ghosts3D = level.ghosts().map(this::createMutatingGhost3D).toList();
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

        ghosts3D.forEach(ghost3D -> ghost3D.init(level));
        house3D.startSwirlAnimations();

        pickerLevelCompleteMessages = RandomTextPicker.fromBundle(ui.localizedTexts(), "level.complete");
    }

    private void createMaterials() {
        pelletMaterial = defaultPhongMaterial(Color.valueOf(colorScheme.pellet()));
        particleMaterial = defaultPhongMaterial(Color.valueOf(colorScheme.pellet()).deriveColor(0, 0.5, 1.5, 0.5));

        floorMaterial = defaultPhongMaterial(PROPERTY_3D_FLOOR_COLOR);
        floorMaterial.setSpecularPower(128);

        var diffuseColor = wallOpacityProperty.map(opacity -> colorWithOpacity(Color.valueOf(colorScheme.wallStroke()), opacity.doubleValue()));
        wallBaseMaterial = defaultPhongMaterial(diffuseColor);
        wallBaseMaterial.setSpecularPower(64);

        wallTopMaterial = defaultPhongMaterial(Color.valueOf(colorScheme.wallFill()));
    }

    private WorldMapColorScheme createWorldMapColorScheme() {
        WorldMap worldMap = level.worldMap();
        WorldMapColorScheme proposedColorScheme = uiConfig.colorScheme(worldMap);
        requireNonNull(proposedColorScheme);
        // Add some contrast with floor if wall fill color is black
        return Color.valueOf(proposedColorScheme.wallFill()).equals(Color.BLACK)
            ? new WorldMapColorScheme("0x2a2a2a", proposedColorScheme.wallStroke(), proposedColorScheme.door(), proposedColorScheme.pellet())
            : proposedColorScheme;
    }

    private MeshView[] createGhostComponentMeshViews(Mesh componentMesh) {
        return IntStream.range(0, 4).mapToObj(_ -> new MeshView(componentMesh)).toArray(MeshView[]::new);
    }

    private GhostColorSet createGhostColorSet(byte personality) {
        AssetMap assets = uiConfig.assets();
        return new GhostColorSet(
            new GhostComponentColors(
                assets.color("ghost.%d.color.normal.dress".formatted(personality)),
                assets.color("ghost.%d.color.normal.pupils".formatted(personality)),
                assets.color("ghost.%d.color.normal.eyeballs".formatted(personality))
            ),
            new GhostComponentColors(
                assets.color("ghost.color.frightened.dress"),
                assets.color("ghost.color.frightened.pupils"),
                assets.color("ghost.color.frightened.eyeballs")
            ),
            new GhostComponentColors(
                assets.color("ghost.color.flashing.dress"),
                assets.color("ghost.color.flashing.pupils"),
                assets.color("ghost.color.frightened.eyeballs")
            )
        );
    }

    private MutableGhost3D createMutatingGhost3D(Ghost ghost) {
        var mutatingGhost3D = new MutableGhost3D(
            animationRegistry,
            ghost,
            createGhostColorSet(ghost.personality()),
            ghostDressMeshViews[ghost.personality()],
            ghostPupilsMeshViews[ghost.personality()],
            ghostEyesMeshViews[ghost.personality()],
            GlobalPreferencesManager.instance().getFloat("3d.ghost.size"),
            level.numFlashes()
        );
        mutatingGhost3D.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> ghost.isVisible() && !outsideWorld(ghost),
            ghost.visibleProperty(), ghost.positionProperty()
        ));
        return mutatingGhost3D;
    }

    private boolean outsideWorld(Ghost ghost) {
        Vector2f center = ghost.center();
        return center.x() < HTS || center.x() > level.worldMap().numCols() * TS - HTS;
    }

    private void createPac3D(double size) {
        pac3D = uiConfig.createPac3D(animationRegistry, level.pac(), size);
        pac3D.init(level);
    }

    private void createLivesCounter3D() {
        int capacity = GlobalPreferencesManager.instance().getInt("3d.lives_counter.capacity");
        Color pillarColor = GlobalPreferencesManager.instance().getColor("3d.lives_counter.pillar_color");
        Color plateColor = GlobalPreferencesManager.instance().getColor("3d.lives_counter.plate_color");
        livesCounterShapes = new Node[capacity];
        for (int i = 0; i < livesCounterShapes.length; ++i) {
            livesCounterShapes[i] = uiConfig.createLivesCounterShape3D();
        }
        livesCounter3D = new LivesCounter3D(animationRegistry, livesCounterShapes);
        livesCounter3D.setTranslateX(2 * TS);
        livesCounter3D.setTranslateY(2 * TS);
        livesCounter3D.pillarColorProperty().set(pillarColor);
        livesCounter3D.plateColorProperty().set(plateColor);
    }

    private void createLevelCounter3D() {
        WorldMap worldMap = level.worldMap();
        levelCounter3D = new LevelCounter3D(animationRegistry, uiConfig);
        levelCounter3D.setTranslateX(TS * (worldMap.numCols() - 2));
        levelCounter3D.setTranslateY(2 * TS);
        levelCounter3D.setTranslateZ(-GlobalPreferencesManager.instance().getFloat("3d.level_counter.elevation"));
    }

    private void createAmbientLight() {
        ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PROPERTY_3D_LIGHT_COLOR);
    }

    private void createGhostLight() {
        ghostLight = new PointLight(Color.WHITE);
        ghostLight.setMaxRange(30);
        ghostLight.lightOnProperty().addListener((_, _, on) -> Logger.info("Ghost light {}", on ? "ON" : "OFF"));
    }

    private void createMaze3D() {
        Logger.info("Building 3D maze for map (URL '{}'), color scheme: {}...", level.worldMap().url(), colorScheme);

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

        float wallThickness = GlobalPreferencesManager.instance().getFloat("3d.obstacle.wall_thickness");
        float cornerRadius = GlobalPreferencesManager.instance().getFloat("3d.obstacle.corner_radius");
        wall3DCount = 0;
        var stopWatch = new StopWatch();
        Optional<House> optionalHouse = level.worldMap().terrainLayer().optHouse();
        for (Obstacle obstacle : level.worldMap().terrainLayer().obstacles()) {
            // exclude house placeholder
            Vector2i startTile = tileAt(obstacle.startPoint().toVector2f());
            if (optionalHouse.isPresent() && !optionalHouse.get().isTileInHouseArea(startTile)) {
                r3D.renderObstacle3D(obstacle, isWorldBorder(level.worldMap(), obstacle), wallThickness, cornerRadius);
            }
        }
        var passedTimeMillis = stopWatch.passedTime().toMillis();
        Logger.info("Built 3D maze with {} composite walls in {} milliseconds", wall3DCount, passedTimeMillis);

        optionalHouse.ifPresent(house -> {
            Vector2i[] ghostRevivalTiles = {
                house.ghostRevivalTile(CYAN_GHOST_BASHFUL),
                house.ghostRevivalTile(PINK_GHOST_SPEEDY),
                house.ghostRevivalTile(ORANGE_GHOST_POKEY)
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
                GlobalPreferencesManager.instance().getFloat("3d.house.base_height"),
                GlobalPreferencesManager.instance().getFloat("3d.house.wall_thickness"),
                GlobalPreferencesManager.instance().getFloat("3d.house.opacity")
            );
            house3D.setWallBaseColor(Color.valueOf(colorScheme.wallFill()));
            house3D.setWallTopColor(Color.valueOf(colorScheme.wallStroke()));
            house3D.setDoorColor(Color.valueOf(colorScheme.door()));
            house3D.wallBaseHeightProperty().set(GlobalPreferencesManager.instance().getFloat("3d.house.base_height"));
            house3D.openProperty().addListener(this::handleHouseOpenChange);
            house3D.setDoorSensitivity(GlobalPreferencesManager.instance().getFloat("3d.house.sensitivity"));
            maze3D.getChildren().add(house3D);
        });
    }

    private void createFloor3D() {
        float padding   = GlobalPreferencesManager.instance().getFloat("3d.floor.padding");
        float thickness = GlobalPreferencesManager.instance().getFloat("3d.floor.thickness");
        Vector2i worldSizePx = level.worldMap().terrainLayer().sizeInPixel();
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
        Vector2i worldSizePx = level.worldMap().terrainLayer().sizeInPixel();
        Point3D center = particle.center();
        double r = particle.getRadius(), cx = center.getX(), cy = center.getY();
        if (cx + r < 0 || cx - r > worldSizePx.x()) return false;
        if (cy + r < 0 || cy - r > worldSizePx.y()) return false;
        return center.getZ() >= floorTopZ();
    }

    private boolean isWorldBorder(WorldMap worldMap, Obstacle obstacle) {
        Vector2i start = obstacle.startPoint();
        if (obstacle.isClosed()) {
            return start.x() == TS || start.y() == worldMap.terrainLayer().emptyRowsOverMaze() * TS + HTS;
        } else {
            return start.x() == 0 || start.x() == worldMap.numCols() * TS;
        }
    }

    private void createPellets3D() {
        float radius = GlobalPreferencesManager.instance().getFloat("3d.pellet.radius");
        Mesh mesh = PacManModel3DRepository.instance().pelletMesh();
        var prototype = new MeshView(mesh);
        Bounds bounds = prototype.getBoundsInLocal();
        double maxExtent = Math.max(Math.max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
        double scaling = (2 * radius) / maxExtent;
        var scale = new Scale(scaling, scaling, scaling);
        FoodLayer foodLayer = level.worldMap().foodLayer();
        pellets3D = foodLayer.tiles().filter(foodLayer::hasFoodAtTile)
            .filter(tile -> !foodLayer.isEnergizerTile(tile))
            .map(tile -> createPellet3D(mesh, scale, tile))
            .collect(Collectors.toCollection(HashSet::new));
    }

    private Shape3D createPellet3D(Mesh pelletMesh, Scale scale, Vector2i tile) {
        var pelletShape = new MeshView(pelletMesh);
        pelletShape.setMaterial(pelletMaterial);
        pelletShape.setRotationAxis(Rotate.Z_AXIS);
        pelletShape.setRotate(90);
        pelletShape.setTranslateX(tile.x() * TS + HTS);
        pelletShape.setTranslateY(tile.y() * TS + HTS);
        pelletShape.setTranslateZ(-floorTopZ() - 6);
        pelletShape.getTransforms().add(scale);
        pelletShape.setUserData(tile);
        return pelletShape;
    }

    private void createEnergizers3D() {
        float radius     = GlobalPreferencesManager.instance().getFloat("3d.energizer.radius");
        float minScaling = GlobalPreferencesManager.instance().getFloat("3d.energizer.scaling.min");
        float maxScaling = GlobalPreferencesManager.instance().getFloat("3d.energizer.scaling.max");
        House house = level.worldMap().terrainLayer().optHouse().orElseThrow();
        Vector2i[] ghostRevivalTiles = {
            house.ghostRevivalTile(RED_GHOST_SHADOW),
            house.ghostRevivalTile(PINK_GHOST_SPEEDY),
            house.ghostRevivalTile(CYAN_GHOST_BASHFUL),
            house.ghostRevivalTile(ORANGE_GHOST_POKEY),
        };

        Vector2f[] ghostRevivalCenters = {
            revivalPositionCenter(ghostRevivalTiles[RED_GHOST_SHADOW]),
            revivalPositionCenter(ghostRevivalTiles[PINK_GHOST_SPEEDY]),
            revivalPositionCenter(ghostRevivalTiles[CYAN_GHOST_BASHFUL]),
            revivalPositionCenter(ghostRevivalTiles[ORANGE_GHOST_POKEY])
        };

        FoodLayer foodLayer = level.worldMap().foodLayer();
        Material[] ghostParticleMaterials = {
            ghosts3D.get(RED_GHOST_SHADOW).ghost3D().normalMaterialSet().dress(),
            ghosts3D.get(PINK_GHOST_SPEEDY).ghost3D().normalMaterialSet().dress(),
            ghosts3D.get(CYAN_GHOST_BASHFUL).ghost3D().normalMaterialSet().dress(),
            ghosts3D.get(ORANGE_GHOST_POKEY).ghost3D().normalMaterialSet().dress(),
        };
        energizers3D = foodLayer.tiles().filter(foodLayer::hasFoodAtTile)
            .filter(foodLayer::isEnergizerTile)
            .map(tile -> createEnergizer3D(tile, radius, minScaling, maxScaling, ghostParticleMaterials, ghostRevivalCenters))
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
        Material[] ghostParticleMaterials,
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
            ghostParticleMaterials,
            this::particleTouchesFloor);

        energizer3D.setEatenAnimation(explosion);
        return energizer3D;
    }

    public LivesCounter3D livesCounter3D() {
        return livesCounter3D;
    }

    public Box floor3D() {
        return floor3D;
    }

    public PacBase3D pac3D() { return pac3D; }

    public List<MutableGhost3D> ghosts3D() { return Collections.unmodifiableList(ghosts3D); }

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
    public void update() {
        pac3D.update(level);
        ghosts3D.forEach(ghost3D -> ghost3D.update(level));
        bonus3D().ifPresent(bonus3D -> bonus3D.update(level));
        if (house3D != null) {
            house3D.update(level);
        }
        updateLivesCounter3D();
    }

    private void updateLivesCounter3D() {
        if (livesCounter3D != null) {
            final GameControl gameControl = level.game().control();
            final boolean oneMore = gameControl.state().matches(GameControl.StateName.STARTING_GAME_OR_LEVEL) && !level.pac().isVisible();
            final boolean visible = level.game().canStartNewGame();
            int lifeCount = level.game().lifeCount() - 1;
            // when the game starts and Pac-Man is not yet visible, show one more
            if (oneMore) lifeCount += 1;
            livesCounter3D.livesCountProperty().set(lifeCount);
            livesCounter3D.setVisible(visible);
        }
    }

    public void onStartingGame() {
        energizers3D().forEach(Energizer3D::stopPumping);
        if (levelCounter3D != null) {
            levelCounter3D.update(level.game());
        }
    }

    public void onHuntingStart() {
        pac3D.init(level);
        ghosts3D.forEach(ghost3D -> ghost3D.init(level));
        energizers3D().forEach(Energizer3D::startPumping);
        house3D.startSwirlAnimations();
        ghostLightAnimation.playFromStart();
    }

    public void onPacManDying(StateMachine.State<Game> state) {
        state.timer().resetIndefiniteTime(); // expires when level animation ends
        ui.soundManager().stopAll();
        ghostLightAnimation.stop();
        // do one last update before dying animation starts
        pac3D.update(level);
        ghosts3D.forEach(MutableGhost3D::stopAllAnimations);
        bonus3D().ifPresent(Bonus3D::expire);
        var animation = new SequentialTransition(
            pauseSec(1.5),
            doNow(() -> ui.soundManager().play(SoundID.PAC_MAN_DEATH)),
            pac3D.dyingAnimation().getOrCreateAnimationFX(),
            pauseSec(0.5)
        );
        // Note: adding this inside the animation as last action does not work!
        animation.setOnFinished(_ -> level.game().control().terminateCurrentGameState());
        animation.play();
    }

    public void onEatingGhost() {
        level.game().simulationStep().ghostsKilled.forEach(killedGhost -> {
            byte personality = killedGhost.personality();
            int killedIndex = level.energizerVictims().indexOf(killedGhost);
            Image pointsImage = uiConfig.killedGhostPointsImage(killedIndex);
            ghosts3D.get(personality).setNumberImage(pointsImage);
        });
    }

    public void onLevelComplete(StateMachine.State<Game> state, ObjectProperty<PerspectiveID> perspectiveIDProperty) {
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
        boolean cutSceneFollows = level.cutSceneNumber() != 0;
        RegisteredAnimation levelCompletedAnimation = cutSceneFollows
            ? levelCompletedShortAnimation
            : levelCompletedFullAnimation;

        var animation = new SequentialTransition(
            pauseSecThen(2, () -> {
                perspectiveIDProperty.unbind();
                perspectiveIDProperty.set(PerspectiveID.TOTAL);
                wallBaseHeightProperty.unbind();
            }),
            levelCompletedAnimation.getOrCreateAnimationFX(),
            pauseSec(0.25)
        );
        animation.setOnFinished(_ -> {
            wallBaseHeightProperty.bind(PROPERTY_3D_WALL_HEIGHT);
            perspectiveIDProperty.bind(PROPERTY_3D_PERSPECTIVE_ID);
            level.game().control().terminateCurrentGameState();
        });
        animation.play();
    }

    public void onGameOver(StateMachine.State<Game> state) {
        state.timer().restartSeconds(3);
        ghostLightAnimation.stop();
        energizers3D().forEach(Energizer3D::hide);
        house3D.stopSwirlAnimations();
        bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
        ui.soundManager().stopAll();
        ui.soundManager().play(SoundID.GAME_OVER);
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
            .font(FONT_ARCADE_6)
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
            uiConfig.bonusSymbolImage(bonus.symbol()), GlobalPreferencesManager.instance().getFloat("3d.bonus.symbol.width"),
            uiConfig.bonusValueImage(bonus.symbol()), GlobalPreferencesManager.instance().getFloat("3d.bonus.points.width"));
        getChildren().add(bonus3D);
        bonus3D.showEdible();
    }

    public void updateLevelCounter3D() {
        if (levelCounter3D != null) {
            levelCounter3D.update(level.game());
        }
    }

    private void handleHouseOpenChange(ObservableValue<? extends Boolean> obs,  boolean wasOpen, boolean isOpen) {
        if (isOpen && house3D != null) {
            house3D.doorsOpenCloseAnimation().playFromStart();
        }
    }

    private void handleDrawModeChange(ObservableValue<? extends DrawMode> obs, DrawMode oldDrawMode, DrawMode newDrawMode) {
        try {
            Predicate<Node> includeAll = _ -> false;
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


    private String translatedLevelCompleteMessage(Translator translator, int levelNumber) {
        return pickerLevelCompleteMessages.hasEntries()
            ? pickerLevelCompleteMessages.nextText() + "\n\n" + translator.translate("level_complete", levelNumber)
            : "";
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
        animationRegistry.dispose();

        PROPERTY_3D_DRAW_MODE.removeListener(this::handleDrawModeChange);
        Logger.info("Removed 'draw mode' listener");

        house3D.openProperty().removeListener(this::handleHouseOpenChange);
        Logger.info("Removed 'house open' listener");

        house3D.openProperty().unbind();
        house3D.wallBaseHeightProperty().unbind();
        house3D.light().lightOnProperty().unbind();

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
}