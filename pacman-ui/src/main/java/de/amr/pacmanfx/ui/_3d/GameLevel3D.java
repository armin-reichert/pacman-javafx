/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.StopWatch;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.Obstacle;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.Explosion;
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

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static de.amr.pacmanfx.uilib.Ufx.*;
import static java.util.Objects.requireNonNull;

/**
 * 3D representation of game level.
 */
public class GameLevel3D implements Disposable {

    private final DoubleProperty  houseBaseHeightProperty = new SimpleDoubleProperty(Wall3D.DEFAULT_BASE_HEIGHT);
    private final BooleanProperty houseLightOnProperty    = new SimpleBooleanProperty(false);
    private final BooleanProperty houseOpenProperty       = new SimpleBooleanProperty(false);
    private final IntegerProperty livesCountProperty      = new SimpleIntegerProperty(0);
    private final DoubleProperty  wallBaseHeightProperty  = new SimpleDoubleProperty(Wall3D.DEFAULT_BASE_HEIGHT);
    private final DoubleProperty  wallOpacityProperty     = new SimpleDoubleProperty(1);

    protected final GameUI ui;
    protected final Group root;
    protected final GameLevel gameLevel;
    protected final WorldMapColorScheme colorScheme;

    private final AnimationRegistry animationRegistry = new AnimationRegistry();
    private ManagedAnimation wallColorFlashingAnimation;
    private ManagedAnimation levelCompletedFullAnimation;
    private ManagedAnimation levelCompletedShortAnimation;

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
    protected Group maze3D;
    protected Box floor3D;
    protected ArcadeHouse3D house3D;
    protected LevelCounter3D levelCounter3D;
    protected LivesCounter3D livesCounter3D;
    protected PacBase3D pac3D;
    protected List<MutatingGhost3D> ghosts3D;
    protected Bonus3D bonus3D;
    protected final ArrayList<Shape3D> pellets3D = new ArrayList<>();
    protected final ArrayList<Energizer3D> energizers3D = new ArrayList<>();
    protected final Group particleGroupsContainer = new Group();
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
                doNow(() -> {
                    turnLivesCounterLightOff();
                    sometimesLevelCompleteMessage(gameLevel.number());
                }),
                pauseSec(0.5, () -> gameLevel.ghosts().forEach(Ghost::hide)),
                wallsMovingUpAndDown(gameLevel.data().numFlashes()),
                pauseSec(0.5, () -> gameLevel.pac().hide()),
                pauseSec(0.5),
                levelSpinningAroundAxis(new Random().nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS),
                pauseSec(0.5, () -> ui.theSound().play(SoundID.LEVEL_COMPLETE)),
                pauseSec(0.5),
                wallsAndHouseDisappearing(),
                pauseSec(1.0, () -> ui.theSound().play(SoundID.LEVEL_CHANGED))
            );
        }

        private void turnLivesCounterLightOff() {
            livesCounter3D().map(LivesCounter3D::light).ifPresent(light -> light.setLightOn(false));
        }

        private Animation wallsAndHouseDisappearing() {
            return new Timeline(
                new KeyFrame(Duration.seconds(0.5), new KeyValue(houseBaseHeightProperty, 0, Interpolator.EASE_IN)),
                new KeyFrame(Duration.seconds(1.5), new KeyValue(wallBaseHeightProperty, 0, Interpolator.EASE_IN)),
                new KeyFrame(Duration.seconds(2.5), e -> maze3D.setVisible(false))
            );
        }

        private void sometimesLevelCompleteMessage(int levelNumber) {
            if (randomInt(0, 100) < MESSAGE_FREQUENCY) {
                String message = ui.theAssets().localizedLevelCompleteMessage(levelNumber);
                ui.showFlashMessageSec(3, message);
            }
        }

        private Animation levelSpinningAroundAxis(Point3D axis) {
            var spin360 = new RotateTransition(Duration.seconds(SPINNING_SECONDS), root);
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

    /**
     * @param ui the game UI
     * @param root a group provided by the play scene serving as the root of the tree representing the 3D game level
     */
    public GameLevel3D(GameUI ui, Group root) {
        this.ui = requireNonNull(ui);
        this.root = requireNonNull(root);
        this.gameLevel = requireNonNull(ui.theGameContext().theGameLevel());

        wallOpacityProperty.bind(ui.property3DWallOpacity());

        wallBaseHeightProperty.bind(ui.property3DWallHeight());
        houseBaseHeightProperty.set(ui.thePrefs().getFloat("3d.house.base_height"));

        houseOpenProperty.addListener(this::handleHouseOpenChange);
        ui.property3DDrawMode().addListener(this::handleDrawModeChange);

        root.setMouseTransparent(true); // this increases performance, they say...

        colorScheme = createWorldMapColorScheme();
        createMaterials();
        createGhostMeshViews();

        createAmbientLight();
        createLevelCounter3D();
        createLivesCounter3D();
        createPac3D();
        createGhosts3D();
        createMaze3D();
        createPellets3D();
        createEnergizers3D();

        wallColorFlashingAnimation = new WallColorFlashingAnimation(animationRegistry);
        levelCompletedFullAnimation = new LevelCompletedAnimation(animationRegistry);
        levelCompletedShortAnimation = new LevelCompletedAnimationShort(animationRegistry);

        root.getChildren().add(ambientLight);
        if (levelCounter3D != null) {
            root.getChildren().add(levelCounter3D);
        }
        if (livesCounter3D != null) {
            root.getChildren().add(livesCounter3D);
        }
        root.getChildren().addAll(pac3D, pac3D.light());
        root.getChildren().addAll(ghosts3D);

        root.getChildren().add(particleGroupsContainer);
        energizers3D.forEach(root.getChildren()::add);
        pellets3D.forEach(root.getChildren()::add);

        // Note: The order in which children are added to the root matters!
        // Walls and house must be added *after* the actors, otherwise the transparency is not working correctly.
        root.getChildren().addAll(floor3D, maze3D);

    }

    public Group root() {
        return root;
    }

    private void createMaterials() {
        pelletMaterial = new PhongMaterial(colorScheme.pellet());
        pelletMaterial.setSpecularColor(colorScheme.pellet().brighter());

        particleMaterial = new PhongMaterial(colorScheme.pellet().deriveColor(0, 0.5, 1.5, 0.5));
        particleMaterial.setSpecularColor(particleMaterial.getDiffuseColor().brighter());

        floorMaterial = new PhongMaterial();
        floorMaterial.diffuseColorProperty().bind(ui.property3DFloorColor());
        floorMaterial.specularColorProperty().bind(floorMaterial.diffuseColorProperty().map(Color::brighter));

        wallBaseMaterial = new PhongMaterial();

        //TODO the opacity change does not work as expected. Why?
        var diffuseColor = wallOpacityProperty.map(opacity -> colorWithOpacity(colorScheme.stroke(), opacity.doubleValue()));
        wallBaseMaterial.diffuseColorProperty().bind(diffuseColor);
        wallBaseMaterial.specularColorProperty().bind(diffuseColor.map(Color::brighter));

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
        WorldMapColorScheme proposedColorScheme = ui.theConfiguration().colorScheme(worldMap);
        requireNonNull(proposedColorScheme);
        // Add some contrast with floor if wall fill color is black
        return proposedColorScheme.fill().equals(Color.BLACK)
            ? new WorldMapColorScheme(Color.grayRgb(42), proposedColorScheme.stroke(), proposedColorScheme.door(), proposedColorScheme.pellet())
            : proposedColorScheme;
    }

    private void createGhostMeshViews() {
        Mesh ghostDressMesh = ui.theAssets().theModel3DRepository().ghostDressMesh();
        ghostDressMeshViews = new MeshView[] {
                new MeshView(ghostDressMesh),
                new MeshView(ghostDressMesh),
                new MeshView(ghostDressMesh),
                new MeshView(ghostDressMesh),
        };

        Mesh ghostPupilsMesh = ui.theAssets().theModel3DRepository().ghostPupilsMesh();
        ghostPupilsMeshViews = new MeshView[] {
                new MeshView(ghostPupilsMesh),
                new MeshView(ghostPupilsMesh),
                new MeshView(ghostPupilsMesh),
                new MeshView(ghostPupilsMesh),
        };

        Mesh ghostEyeballsMesh = ui.theAssets().theModel3DRepository().ghostEyeballsMesh();
        ghostEyesMeshViews = new MeshView[] {
                new MeshView(ghostEyeballsMesh),
                new MeshView(ghostEyeballsMesh),
                new MeshView(ghostEyeballsMesh),
                new MeshView(ghostEyeballsMesh),
        };
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

    private void createGhosts3D() {
        ghosts3D = gameLevel.ghosts().map(ghost -> {
            var ghostColoring = new GhostColoring(
                ui.theConfiguration().getAssetNS("ghost.%d.color.normal.dress".formatted(ghost.personality())),
                ui.theConfiguration().getAssetNS("ghost.%d.color.normal.pupils".formatted(ghost.personality())),
                ui.theConfiguration().getAssetNS("ghost.%d.color.normal.eyeballs".formatted(ghost.personality())),
                ui.theConfiguration().getAssetNS("ghost.color.frightened.dress"),
                ui.theConfiguration().getAssetNS("ghost.color.frightened.pupils"),
                ui.theConfiguration().getAssetNS("ghost.color.frightened.eyeballs"),
                ui.theConfiguration().getAssetNS("ghost.color.flashing.dress"),
                ui.theConfiguration().getAssetNS("ghost.color.flashing.pupils")
            );
            return new MutatingGhost3D(
                animationRegistry,
                gameLevel,
                ghost,
                ghostColoring,
                ghostDressMeshViews[ghost.personality()],
                ghostPupilsMeshViews[ghost.personality()],
                ghostEyesMeshViews[ghost.personality()],
                ui.thePrefs().getFloat("3d.ghost.size"),
                gameLevel.data().numFlashes()
            );
        }).toList();
        ghosts3D.forEach(ghost3D -> ghost3D.init(gameLevel));
    }

    private void createPac3D() {
        pac3D = ui.theConfiguration().createPac3D(animationRegistry, gameLevel.pac());
        pac3D.init();
    }

    private void createLivesCounter3D() {
        int capacity = ui.thePrefs().getInt("3d.lives_counter.capacity");
        Color pillarColor = ui.thePrefs().getColor("3d.lives_counter.pillar_color");
        Color plateColor = ui.thePrefs().getColor("3d.lives_counter.plate_color");
        livesCounterShapes = new Node[capacity];
        for (int i = 0; i < livesCounterShapes.length; ++i) {
            livesCounterShapes[i] = ui.theConfiguration().createLivesCounterShape3D();
        }
        livesCounter3D = new LivesCounter3D(animationRegistry, livesCounterShapes);
        livesCounter3D.setTranslateX(2 * TS);
        livesCounter3D.setTranslateY(2 * TS);
        livesCounter3D.livesCountProperty().bind(livesCountProperty);
        livesCounter3D.pillarColorProperty().set(pillarColor);
        livesCounter3D.plateColorProperty().set(plateColor);
        livesCounter3D.light().colorProperty().set(Color.CORNFLOWERBLUE);
    }

    private void createLevelCounter3D() {
        WorldMap worldMap = gameLevel.worldMap();
        levelCounter3D = new LevelCounter3D(animationRegistry);
        levelCounter3D.setTranslateX(TS * (worldMap.numCols() - 2));
        levelCounter3D.setTranslateY(2 * TS);
        levelCounter3D.setTranslateZ(-ui.thePrefs().getFloat("3d.level_counter.elevation"));
    }

    private void createAmbientLight() {
        ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(ui.property3DLightColor());
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
        });

        createFloor3D();

        float wallThickness = ui.thePrefs().getFloat("3d.obstacle.wall_thickness");
        float cornerRadius = ui.thePrefs().getFloat("3d.obstacle.corner_radius");
        wall3DCount = 0;
        var stopWatch = new StopWatch();
        for (Obstacle obstacle : gameLevel.worldMap().obstacles()) {
            // exclude house placeholder
            Vector2i startTile = tileAt(obstacle.startPoint().toVector2f());
            if (gameLevel.house().isPresent() && !gameLevel.house().get().isTileInHouseArea(startTile)) {
                r3D.renderObstacle3D(maze3D, obstacle, isWorldBorder(gameLevel.worldMap(), obstacle), wallThickness, cornerRadius);
            }
        }
        var passedTimeMillis = stopWatch.passedTime().toMillis();
        Logger.info("Built 3D maze with {} composite walls in {} milliseconds", wall3DCount, passedTimeMillis);

        gameLevel.house().ifPresent(house -> {
            house3D = new ArcadeHouse3D(
                animationRegistry,
                house,
                ui.thePrefs().getFloat("3d.house.base_height"),
                ui.thePrefs().getFloat("3d.house.wall_thickness"),
                ui.thePrefs().getFloat("3d.house.opacity"),
                colorScheme.fill(),
                colorScheme.stroke(),
                colorScheme.door()
            );
            house3D.wallBaseHeightProperty().bind(houseBaseHeightProperty);
            house3D.light().lightOnProperty().bind(houseLightOnProperty);
            maze3D.getChildren().add(house3D);
        });
    }

    private void createFloor3D() {
        float padding   = ui.thePrefs().getFloat("3d.floor.padding");
        float thickness = ui.thePrefs().getFloat("3d.floor.thickness");
        Vector2f worldSizePx = gameLevel.worldSizePx();
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

    private boolean particleTouchesFloor(Explosion.Particle particle) {
        Vector2f worldSizePx = gameLevel.worldSizePx();
        Point3D particleCenter = particle.center();
        if (particleCenter.getX() < 0 || particleCenter.getX() > worldSizePx.x()) return false;
        if (particleCenter.getY() < 0 || particleCenter.getY() > worldSizePx.y()) return false;
        return particleCenter.getZ() >= floorTopZ();
        //TODO: make this work
/*
        Point3D center = particle.center();
        Bounds fb = floor3D.getBoundsInParent(); //TODO correct?
        return Ufx.intersectsSphereBox(
            center.getX(), center.getY(), center.getZ(), particle.getRadius(),
            fb.getMinX(), fb.getMinY(), fb.getMinZ(),
            fb.getMaxX(), fb.getMaxY(), fb.getMaxZ()
        );
 */
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
        float radius = ui.thePrefs().getFloat("3d.pellet.radius");
        Mesh mesh = ui.theAssets().theModel3DRepository().pelletMesh();
        var prototype = new MeshView(mesh);
        Bounds bounds = prototype.getBoundsInLocal();
        double maxExtent = Math.max(Math.max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
        double scaling = (2 * radius) / maxExtent;
        var scale = new Scale(scaling, scaling, scaling);
        pellets3D.addAll(gameLevel.tilesContainingFood()
            .filter(tile -> !gameLevel.isEnergizerPosition(tile))
            .map(tile -> createPellet3D(mesh, scale, tile))
            .toList());
        pellets3D.trimToSize();
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
        float radius     = ui.thePrefs().getFloat("3d.energizer.radius");
        float minScaling = ui.thePrefs().getFloat("3d.energizer.scaling.min");
        float maxScaling = ui.thePrefs().getFloat("3d.energizer.scaling.max");
        Material[] ghostDressMaterials = {
            ghosts3D.get(RED_GHOST_SHADOW).ghost3D().dressMaterialNormal(),
            ghosts3D.get(PINK_GHOST_SPEEDY).ghost3D().dressMaterialNormal(),
            ghosts3D.get(CYAN_GHOST_BASHFUL).ghost3D().dressMaterialNormal(),
            ghosts3D.get(ORANGE_GHOST_POKEY).ghost3D().dressMaterialNormal(),
        };
        energizers3D.addAll(gameLevel.tilesContainingFood()
            .filter(gameLevel::isEnergizerPosition)
            .map(tile -> createEnergizer3D(tile, radius, minScaling, maxScaling, ghostDressMaterials))
            .toList());
        energizers3D.trimToSize();
    }

    private Energizer3D createEnergizer3D(Vector2i tile, float energizerRadius, float minScaling, float maxScaling, Material[] ghostDressMaterials) {
        var center = new Point3D(tile.x() * TS + HTS, tile.y() * TS + HTS, floorTopZ() - 6);
        var energizer3D = new Energizer3D(animationRegistry, energizerRadius, center, minScaling, maxScaling, pelletMaterial);
        energizer3D.setUserData(tile);
        Vector2f[] ghostRevivalPositionCenters = {
            gameLevel.ghost(RED_GHOST_SHADOW).revivalPosition().plus(HTS, HTS),
            gameLevel.ghost(PINK_GHOST_SPEEDY).revivalPosition().plus(HTS, HTS),
            gameLevel.ghost(CYAN_GHOST_BASHFUL).revivalPosition().plus(HTS, HTS),
            gameLevel.ghost(ORANGE_GHOST_POKEY).revivalPosition().plus(HTS, HTS),
        };
        var explosion = new Explosion(animationRegistry, center, ghostRevivalPositionCenters, particleGroupsContainer,
                particleMaterial, ghostDressMaterials, this::particleTouchesFloor);
        energizer3D.setEatenAnimation(explosion);
        return energizer3D;
    }

    public PacBase3D pac3D() { return pac3D; }
    public List<MutatingGhost3D> ghosts3D() { return Collections.unmodifiableList(ghosts3D); }
    public Optional<Bonus3D> bonus3D() { return Optional.ofNullable(bonus3D); }
    public Optional<LivesCounter3D> livesCounter3D() { return Optional.ofNullable(livesCounter3D); }
    public List<Shape3D> pellets3D() { return Collections.unmodifiableList(pellets3D); }
    public List<Energizer3D> energizers3D() { return Collections.unmodifiableList(energizers3D); }

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
    public void tick() {
        pac3D.update();
        ghosts3D.forEach(ghost3D -> ghost3D.update(gameLevel));
        bonus3D().ifPresent(bonus3D -> bonus3D.update(ui.theGameContext()));
        boolean houseAccessRequired = gameLevel.ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .anyMatch(Ghost::isVisible);
        houseLightOnProperty.set(houseAccessRequired);

        // experimental
        consumeParticlesInsideHouse();

        gameLevel.house().ifPresent(house -> {
            float sensitivity = ui.thePrefs().getFloat("3d.house.sensitivity");
            boolean ghostNearHouseEntry = gameLevel.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
                .filter(ghost -> ghost.position().euclideanDist(house.entryPosition()) <= sensitivity)
                .anyMatch(Ghost::isVisible);
            houseOpenProperty.set(ghostNearHouseEntry);
        });

        int livesCounterSize = ui.theGameContext().theGame().lifeCount() - 1;
        // when the game starts and Pac-Man is not yet visible, show one more
        boolean oneMore = ui.theGameContext().theGameState() == GameState.STARTING_GAME && !gameLevel.pac().isVisible();
        if (oneMore) livesCounterSize += 1;
        livesCountProperty.set(livesCounterSize);

        boolean visible = ui.theGameContext().theGame().canStartNewGame();
        if (livesCounter3D != null) {
            livesCounter3D.setVisible(visible);
            livesCounter3D.light().setLightOn(visible);
        }
    }

    public void onStartingGame() {
        energizers3D().forEach(Energizer3D::pausePumping);
        if (levelCounter3D != null) {
            levelCounter3D.update(ui, ui.theGameContext().theGame().theHUD().theLevelCounter());
        }
    }

    public void onHuntingStart() {
        pac3D.init();
        ghosts3D.forEach(ghost3D -> ghost3D.init(gameLevel));
        energizers3D().forEach(Energizer3D::playPumping);
    }

    public void onPacManDying(GameState state) {
        state.timer().resetIndefiniteTime(); // expires when level animation ends
        ui.theSound().stopAll();
        // do one last update before dying animation starts
        pac3D.update();
        ghosts3D.forEach(MutatingGhost3D::stopAllAnimations);
        bonus3D().ifPresent(Bonus3D::expire);
        var animation = new SequentialTransition(
                pauseSec(2),
                doNow(() -> ui.theSound().play(SoundID.PAC_MAN_DEATH)),
                pac3D.dyingAnimation().getOrCreateAnimationFX(),
                pauseSec(1)
        );
        // Note: adding this inside the animation as last action does not work!
        animation.setOnFinished(e -> ui.theGameContext().theGameController().letCurrentGameStateExpire());
        animation.play();
    }

    public void onGhostDying() {
        ui.theGameContext().theGame().simulationStep().killedGhosts.forEach(killedGhost -> {
            byte personality = killedGhost.personality();
            int killedIndex = gameLevel.victims().indexOf(killedGhost);
            Image pointsImage = ui.theConfiguration().killedGhostPointsImage(killedGhost, killedIndex);
            ghosts3D.get(personality).setNumberImage(pointsImage);
        });
    }

    public void onLevelComplete(GameState state, ObjectProperty<PerspectiveID> perspectiveIDProperty) {
        state.timer().resetIndefiniteTime(); // expires when animation ends
        ui.theSound().stopAll();
        animationRegistry.stopAllAnimations();
        // hide 3d food explicitly because level might have been completed using cheat!
        pellets3D.forEach(pellet3D -> pellet3D.setVisible(false));
        energizers3D.forEach(Energizer3D::pausePumping);
        energizers3D.forEach(energizer3D -> energizer3D.setVisible(false));
        particleGroupsContainer.getChildren().clear();
        house3D.setDoorVisible(false);
        bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
        if (messageView != null) {
            messageView.setVisible(false);
        }
        boolean cutSceneFollows = ui.theGameContext().theGame().cutSceneNumber(gameLevel.number()).isPresent();
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
            wallBaseHeightProperty.bind(ui.property3DWallHeight());
            perspectiveIDProperty.bind(ui.property3DPerspective());
            ui.theGameContext().theGameController().letCurrentGameStateExpire();
        });
        animation.play();
    }

    public void onGameOver(GameState state) {
        state.timer().restartSeconds(3);
        energizers3D().forEach(energizer3D -> energizer3D.setVisible(false));
        bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
        ui.theSound().stopAll();
        ui.theSound().play(SoundID.GAME_OVER);
        boolean inOneOf4Cases = randomInt(0, 1000) < 250;
        if (!gameLevel.isDemoLevel() && inOneOf4Cases) {
            ui.showFlashMessageSec(2.5, ui.theAssets().localizedGameOverMessage());
        }
    }

    public void showAnimatedMessage(String messageText, float displaySeconds, double centerX, double centerY) {
        if (messageView != null) {
            messageView.dispose();
            root.getChildren().remove(messageView);
        }
        messageView = MessageView.builder()
            .backgroundColor(Color.BLACK)
            .borderColor(Color.WHITE)
            .displaySeconds(displaySeconds)
            .font(ui.theAssets().arcadeFont(6))
            .text(messageText)
            .textColor(Color.YELLOW)
            .build(animationRegistry);
        root.getChildren().add(messageView);
        messageView.showCenteredAt(centerX, centerY);
    }

    public void updateBonus3D(Bonus bonus) {
        requireNonNull(bonus);
        if (bonus3D != null) {
            root.getChildren().remove(bonus3D);
            bonus3D.dispose();
        }
        bonus3D = new Bonus3D(animationRegistry, bonus,
            ui.theConfiguration().bonusSymbolImage(bonus.symbol()), ui.thePrefs().getFloat("3d.bonus.symbol.width"),
            ui.theConfiguration().bonusValueImage(bonus.symbol()), ui.thePrefs().getFloat("3d.bonus.points.width"));
        root.getChildren().add(bonus3D);
        bonus3D.showEdible();
    }

    public void updateLevelCounter3D() {
        if (levelCounter3D != null) {
            levelCounter3D.update(ui, ui.theGameContext().theGame().theHUD().theLevelCounter());
        }
    }

    private void handleHouseOpenChange(ObservableValue<? extends Boolean> obs,  boolean wasOpen, boolean isOpen) {
        if (isOpen && house3D != null) {
            house3D.doorOpenCloseAnimation().playFromStart();
        }
    }

    private void handleDrawModeChange(ObservableValue<? extends DrawMode> obs, DrawMode oldDrawMode, DrawMode newDrawMode) {
        setDrawModeForAllShapesExcept(root, pellets3D::contains, newDrawMode);
    }

    private void consumeParticlesInsideHouse() {
        for (Node child : particleGroupsContainer.getChildren()) {
            if (child instanceof Group particlesGroup) {
                if (!particlesGroup.getChildren().isEmpty()) {
                    gameLevel.ghosts(GhostState.LOCKED, GhostState.LEAVING_HOUSE)
                        .forEach(ghost -> consumeParticlesInsideHouse(ghost, particlesGroup));
                }
            }
        }
    }

    private void consumeParticlesInsideHouse(Ghost ghost, Group particlesGroup) {
        House house = gameLevel.house().orElse(null);
        if (house == null) return;
        Vector2f houseInnerMin = house.minTile().scaled((float) TS);
        Vector2f houseInnerMax = house.maxTile().scaled((float) TS);
        particlesGroup.getChildren().stream()
            .filter(Explosion.Particle.class::isInstance)
            .map(Explosion.Particle.class::cast)
            .filter(particle -> particle.personality == ghost.personality())
            .filter(particles -> isParticleInsideRect(particles, houseInnerMin, houseInnerMax))
            .forEach(particle -> particle.setVisible(false));
    }

    private boolean isParticleInsideRect(Explosion.Particle particle, Vector2f min, Vector2f max) {
        Point3D particleCenter = particle.center();
        return min.x() <= particleCenter.getX() && min.y() <= particleCenter.getY()
            && particleCenter.getX() <= max.x() && particleCenter.getY() <= max.y();
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

        // Dispose all remaining animations
        if (!animationRegistry.animations().isEmpty()) {
            Logger.info("There are {} un-disposed animations left:", animationRegistry.animations().size());
            // create a copy to avoid CME
            for (ManagedAnimation animation : animationRegistry.animations().stream().toList()) {
                Logger.info("\tDisposing" + animation.label());
                animation.dispose();
            }
        }

        ui.property3DDrawMode().removeListener(this::handleDrawModeChange);
        Logger.info("Removed 'draw mode' listener");

        houseOpenProperty.removeListener(this::handleHouseOpenChange);
        Logger.info("Removed 'house open' listener");

        livesCountProperty.unbind();
        houseOpenProperty.unbind();
        wallBaseHeightProperty.unbind();
        houseBaseHeightProperty.unbind();
        houseLightOnProperty.unbind();
        wallOpacityProperty.unbind();

        root.getChildren().clear();
        Logger.info("Removed all nodes under game level");

        if (ambientLight != null) {
            ambientLight.colorProperty().unbind();
            ambientLight = null;
            Logger.info("Unbound and cleared ambient light");
        }
        pellets3D.forEach(shape3D -> {
            if (shape3D instanceof MeshView meshView) {
                meshView.setMaterial(null);
                meshView.setMesh(null);
            }
        });
        Logger.info("Disposed 3D pellets");
        energizers3D.forEach(Energizer3D::dispose);
        Logger.info("Disposed 3D energizers");
        particleGroupsContainer.getChildren().clear();
        Logger.info("Removed all particle groups");
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
            for (var shape : livesCounterShapes) {
                if (shape instanceof Disposable disposable) {
                    disposable.dispose();
                }
            }
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
            ghosts3D.forEach(MutatingGhost3D::dispose);
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