/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.StopWatch;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.Obstacle;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.Explosion;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.*;
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
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static de.amr.pacmanfx.uilib.Ufx.*;
import static java.util.Objects.requireNonNull;

/**
 * 3D representation of game level.
 */
public class GameLevel3D implements Disposable {

    // memory leak still exists
    static boolean EXPLOSIONS = true;

    private final DoubleProperty  houseBaseHeightProperty = new SimpleDoubleProperty();
    private final BooleanProperty houseLightOnProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty houseOpenProperty = new SimpleBooleanProperty();
    private final IntegerProperty livesCountProperty = new SimpleIntegerProperty(0);
    private final DoubleProperty  obstacleBaseHeightProperty = new SimpleDoubleProperty();
    private final DoubleProperty  wallOpacityProperty = new SimpleDoubleProperty(1);

    protected final GameUI ui;
    protected final Group root;

    protected final GameLevel gameLevel;
    protected WorldMapColorScheme colorScheme;

    private final AnimationManager animationManager = new AnimationManager();
    private ManagedAnimation wallColorFlashingAnimation;
    private ManagedAnimation levelCompletedFullAnimation;
    private ManagedAnimation levelCompletedShortAnimation;

    private MeshView[] dressMeshViews;
    private MeshView[] pupilsMeshViews;
    private MeshView[] eyesMeshViews;

    private Node[] livesCounterShapes;

    private PhongMaterial wallBaseMaterial;
    private PhongMaterial wallTopMaterial;
    private PhongMaterial cornerBaseMaterial;
    private PhongMaterial cornerTopMaterial;
    private PhongMaterial pelletMaterial;

    private AmbientLight ambientLight;
    private Group maze3D;
    private Box floor3D;
    private ArcadeHouse3D house3D;
    private LevelCounter3D levelCounter3D;
    private LivesCounter3D livesCounter3D;
    private PacBase3D pac3D;
    private List<MutatingGhost3D> ghosts3D;
    private Bonus3D bonus3D;
    private ArrayList<Pellet3D> pellets3D = new ArrayList<>();
    private ArrayList<Energizer3D> energizers3D = new ArrayList<>();
    private final Group particlesGroupContainer = new Group();
    private MessageView messageView;

    private int wall3DCount;

    private class MazeDisappearingAnimation extends ManagedAnimation {

        public MazeDisappearingAnimation(AnimationManager animationManager) {
            super(animationManager, "Maze_Disappearing");
        }

        @Override
        protected Animation createAnimation() {
            return new Timeline(
                new KeyFrame(Duration.seconds(0.5), new KeyValue(houseBaseHeightProperty, 0, Interpolator.EASE_IN)),
                new KeyFrame(Duration.seconds(1.5), new KeyValue(obstacleBaseHeightProperty, 0, Interpolator.EASE_IN)),
                new KeyFrame(Duration.seconds(2.5), e -> maze3D.setVisible(false))
            );
        }
    }

    private class LevelCompletedAnimation extends ManagedAnimation {
        private static final int FLASH_DURATION_MILLIS = 250;
        private static final int MESSAGE_FREQUENCY = 20; // 20% of cases
        private static final float SPINNING_SECONDS = 1.5f;

        private ManagedAnimation mazeDisappearingAnimation;

        public LevelCompletedAnimation(AnimationManager animationManager) {
            super(animationManager, "Level_Complete");
            mazeDisappearingAnimation = new MazeDisappearingAnimation(animationManager);
        }

        @Override
        protected Animation createAnimation() {
            return new SequentialTransition(
                doNow(() -> {
                    livesCounter3D().map(LivesCounter3D::light).ifPresent(light -> light.setLightOn(false));
                    showLevelCompleteFlashMessage(gameLevel.number());
                }),
                pauseSec(0.5, () -> gameLevel.ghosts().forEach(Ghost::hide)),
                pauseSec(0.5),
                createWallsFlashAnimation(gameLevel.data().numFlashes()),
                pauseSec(0.5, () -> gameLevel.pac().hide()),
                pauseSec(0.5),
                createSpinningAnimation(new Random().nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS),
                pauseSec(0.5, () -> ui.theSound().play(SoundID.LEVEL_COMPLETE)),
                pauseSec(0.5),
                mazeDisappearingAnimation.getOrCreateAnimation(),
                pauseSec(1.0, () -> ui.theSound().play(SoundID.LEVEL_CHANGED))
            );
        }

        @Override
        public void dispose() {
            mazeDisappearingAnimation.stop();
            mazeDisappearingAnimation.dispose();
            mazeDisappearingAnimation = null;
        }

        private void showLevelCompleteFlashMessage(int levelNumber) {
            if (randomInt(0, 100) < MESSAGE_FREQUENCY) {
                String message = ui.theAssets().localizedLevelCompleteMessage(levelNumber);
                ui.showFlashMessageSec(3, message);
            }
        }

        private Animation createSpinningAnimation(Point3D axis) {
            var spin360 = new RotateTransition(Duration.seconds(SPINNING_SECONDS), root);
            spin360.setAxis(axis);
            spin360.setFromAngle(0);
            spin360.setToAngle(360);
            spin360.setInterpolator(Interpolator.LINEAR);
            return spin360;
        }

        private Animation createWallsFlashAnimation(int numFlashes) {
            if (numFlashes == 0) {
                return pauseSec(1.0);
            }
            var flashingTimeline = new Timeline(
                    new KeyFrame(Duration.millis(0.5 * FLASH_DURATION_MILLIS),
                            new KeyValue(obstacleBaseHeightProperty, 0, Interpolator.EASE_BOTH)
                    )
            );
            flashingTimeline.setAutoReverse(true);
            flashingTimeline.setCycleCount(2 * numFlashes);
            return flashingTimeline;
        }
    }

    private class LevelCompletedAnimationShort extends ManagedAnimation {

        public static final int FLASH_DURATION_MILLIS = 250;

        public LevelCompletedAnimationShort(AnimationManager animationManager) {
            super(animationManager, "Level_Complete_Short_Animation");
        }

        @Override
        protected Animation createAnimation() {
            return new SequentialTransition(
                pauseSec(0.5, () -> gameLevel.ghosts().forEach(Ghost::hide)),
                pauseSec(0.5),
                mazeFlashAnimation(gameLevel.data().numFlashes()),
                pauseSec(0.5, () -> gameLevel.pac().hide())
            );
        }

        private Animation mazeFlashAnimation(int numFlashes) {
            if (numFlashes == 0) {
                return pauseSec(1.0);
            }
            var flashing = new Timeline(
                new KeyFrame(Duration.millis(0.5 * FLASH_DURATION_MILLIS),
                        new KeyValue(obstacleBaseHeightProperty, 0, Interpolator.EASE_BOTH)
                )
            );
            flashing.setAutoReverse(true);
            flashing.setCycleCount(2 * numFlashes);
            return flashing;
        }
    }

    private class WallColorFlashingAnimation extends ManagedAnimation {

        public WallColorFlashingAnimation(AnimationManager animationManager) {
            super(animationManager, "MazeWallColorFlashing");
        }

        @Override
        protected Animation createAnimation() {
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
        obstacleBaseHeightProperty.set(ui.thePrefs().getFloat("3d.obstacle.base_height"));
        houseBaseHeightProperty.set(ui.thePrefs().getFloat("3d.house.base_height"));

        ui.property3DWallHeight().addListener(this::handleWallHeightChange);
        ui.property3DDrawMode().addListener(this::handleDrawModeChange);
        houseOpenProperty.addListener(this::handleHouseOpenChange);

        root.setMouseTransparent(true); // this increases performance, they say...

        createWorldMapColorScheme();
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

        wallColorFlashingAnimation = new WallColorFlashingAnimation(animationManager);
        levelCompletedFullAnimation = new LevelCompletedAnimation(animationManager);
        levelCompletedShortAnimation = new LevelCompletedAnimationShort(animationManager);

        root.getChildren().add(ambientLight);
        if (levelCounter3D != null) {
            root.getChildren().add(levelCounter3D);
        }
        if (livesCounter3D != null) {
            root.getChildren().add(livesCounter3D);
        }
        root.getChildren().addAll(pac3D, pac3D.light());
        root.getChildren().addAll(ghosts3D);

        // Note: The order in which children are added to the root matters!
        // Walls and house must be added *after* the actors, otherwise the transparency is not working correctly.
        root.getChildren().addAll(floor3D, maze3D);

        energizers3D.stream().map(Eatable3D::shape3D).forEach(root.getChildren()::add);
        pellets3D   .stream().map(Eatable3D::shape3D).forEach(root.getChildren()::add);
        root.getChildren().add(particlesGroupContainer);
    }

    public Group root() {
        return root;
    }

    private void createMaterials() {
        pelletMaterial = new PhongMaterial(colorScheme.pellet());
        pelletMaterial.setSpecularColor(colorScheme.pellet().brighter());

        wallBaseMaterial = new PhongMaterial();
        wallBaseMaterial.diffuseColorProperty().bind(wallOpacityProperty
                .map(opacity -> colorWithOpacity(colorScheme.stroke(), opacity.doubleValue())));
        wallBaseMaterial.specularColorProperty().bind(wallBaseMaterial.diffuseColorProperty().map(Color::brighter));

        wallTopMaterial = new PhongMaterial();
        wallTopMaterial.setDiffuseColor(colorScheme.fill());
        wallTopMaterial.setSpecularColor(colorScheme.fill().brighter());

        cornerBaseMaterial = new PhongMaterial();
        cornerBaseMaterial.setDiffuseColor(colorScheme.stroke());
        cornerBaseMaterial.specularColorProperty().bind(cornerBaseMaterial.diffuseColorProperty().map(Color::brighter));

        cornerTopMaterial = new PhongMaterial();
        cornerTopMaterial.setDiffuseColor(colorScheme.fill());
        cornerTopMaterial.specularColorProperty().bind(cornerTopMaterial.diffuseColorProperty().map(Color::brighter));
    }

    private void disposeMaterials() {
        if (pelletMaterial != null) {
            pelletMaterial.diffuseColorProperty().unbind();
            pelletMaterial.specularColorProperty().unbind();
            pelletMaterial = null;
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
        if (cornerBaseMaterial != null) {
            cornerBaseMaterial.diffuseColorProperty().unbind();
            cornerBaseMaterial.specularColorProperty().unbind();
            cornerBaseMaterial = null;
        }
        if (cornerTopMaterial != null) {
            cornerTopMaterial.diffuseColorProperty().unbind();
            cornerTopMaterial.specularColorProperty().unbind();
            cornerTopMaterial = null;
        }
        Logger.info("Unbound material references");
    }

    private void createWorldMapColorScheme() {
        WorldMap worldMap = gameLevel.worldMap();
        WorldMapColorScheme proposedColorScheme = ui.theConfiguration().colorScheme(worldMap);
        requireNonNull(proposedColorScheme);
        // Add some contrast with floor if wall fill color is black
        colorScheme = proposedColorScheme.fill().equals(Color.BLACK)
            ? new WorldMapColorScheme(Color.grayRgb(42), proposedColorScheme.stroke(), proposedColorScheme.door(), proposedColorScheme.pellet())
            : proposedColorScheme;
    }

    private void createGhostMeshViews() {
        Mesh ghostDressMesh = ui.theAssets().theModel3DRepository().ghostDressMesh();
        dressMeshViews = new MeshView[] {
                new MeshView(ghostDressMesh),
                new MeshView(ghostDressMesh),
                new MeshView(ghostDressMesh),
                new MeshView(ghostDressMesh),
        };

        Mesh ghostPupilsMesh = ui.theAssets().theModel3DRepository().ghostPupilsMesh();
        pupilsMeshViews = new MeshView[] {
                new MeshView(ghostPupilsMesh),
                new MeshView(ghostPupilsMesh),
                new MeshView(ghostPupilsMesh),
                new MeshView(ghostPupilsMesh),
        };

        Mesh ghostEyeballsMesh = ui.theAssets().theModel3DRepository().ghostEyeballsMesh();
        eyesMeshViews = new MeshView[] {
                new MeshView(ghostEyeballsMesh),
                new MeshView(ghostEyeballsMesh),
                new MeshView(ghostEyeballsMesh),
                new MeshView(ghostEyeballsMesh),
        };
    }

    private void disposeGhostMeshViews() {
        if (dressMeshViews != null) {
            for (MeshView meshView : dressMeshViews) {
                meshView.setMesh(null);
                meshView.materialProperty().unbind();
                meshView.setMaterial(null);
            }
            dressMeshViews = null;
            Logger.info("Cleared dress mesh views");
        }
        if (pupilsMeshViews != null) {
            for (MeshView meshView : pupilsMeshViews) {
                meshView.setMesh(null);
                meshView.materialProperty().unbind();
                meshView.setMaterial(null);
            }
            pupilsMeshViews = null;
            Logger.info("Cleared pupils mesh views");
        }
        if (eyesMeshViews != null) {
            for (MeshView meshView : eyesMeshViews) {
                meshView.setMesh(null);
                meshView.materialProperty().unbind();
                meshView.setMaterial(null);
            }
            eyesMeshViews = null;
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
                animationManager,
                gameLevel,
                ghost,
                ghostColoring,
                dressMeshViews[ghost.personality()],
                pupilsMeshViews[ghost.personality()],
                eyesMeshViews[ghost.personality()],
                ui.thePrefs().getFloat("3d.ghost.size"),
                gameLevel.data().numFlashes()
            );
        }).toList();
        ghosts3D.forEach(ghost3D -> ghost3D.init(gameLevel));
    }

    private void createPac3D() {
        pac3D = ui.theConfiguration().createPac3D(animationManager, gameLevel.pac());
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
        livesCounter3D = new LivesCounter3D(animationManager, livesCounterShapes);
        livesCounter3D.setTranslateX(2 * TS);
        livesCounter3D.setTranslateY(2 * TS);
        livesCounter3D.livesCountProperty().bind(livesCountProperty);
        livesCounter3D.pillarColorProperty().set(pillarColor);
        livesCounter3D.plateColorProperty().set(plateColor);
        livesCounter3D.light().colorProperty().set(Color.CORNFLOWERBLUE);
        livesCounter3D.lookingAroundAnimation().playFromStart();
    }

    private void createLevelCounter3D() {
        WorldMap worldMap = gameLevel.worldMap();
        levelCounter3D = new LevelCounter3D(animationManager);
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
            wall3D.bindBaseHeight(obstacleBaseHeightProperty);
            wall3D.setBaseMaterial(wallBaseMaterial);
            wall3D.setTopMaterial(wallTopMaterial);
            ++wall3DCount;
        });

        float floorPadding = ui.thePrefs().getFloat("3d.floor.padding");
        float floorThickness = ui.thePrefs().getFloat("3d.floor.thickness");
        double floorWidth = gameLevel.worldMap().numCols() * TS + 2 * floorPadding;
        double floorHeight = gameLevel.worldMap().numRows() * TS;
        floor3D = new Box(floorWidth, floorHeight, floorThickness);
        floor3D.translateXProperty().bind(floor3D.widthProperty().divide(2).subtract(floorPadding));
        floor3D.translateYProperty().bind(floor3D.heightProperty().divide(2));
        floor3D.translateZProperty().bind(floor3D.depthProperty().divide(2));
        floor3D.materialProperty().bind(ui.property3DFloorColor().map(Ufx::coloredPhongMaterial));

        float wallThickness = ui.thePrefs().getFloat("3d.obstacle.wall_thickness");
        float cornerRadius = ui.thePrefs().getFloat("3d.obstacle.corner_radius");
        wall3DCount = 0;
        var stopWatch = StopWatch.create();
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
                animationManager,
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
    private boolean isWorldBorder(WorldMap worldMap, Obstacle obstacle) {
        Vector2i start = obstacle.startPoint();
        if (obstacle.isClosed()) {
            return start.x() == TS || start.y() == GameLevel.EMPTY_ROWS_OVER_MAZE * TS + HTS;
        } else {
            return start.x() == 0 || start.x() == worldMap.numCols() * TS;
        }
    }


    public PacBase3D pac3D() { return pac3D; }
    public List<MutatingGhost3D> ghosts3D() { return Collections.unmodifiableList(ghosts3D); }
    public MutatingGhost3D ghost3D(byte id) { return ghosts3D.get(id); }
    public Optional<Bonus3D> bonus3D() { return Optional.ofNullable(bonus3D); }
    public Optional<LivesCounter3D> livesCounter3D() { return Optional.ofNullable(livesCounter3D); }
    public Stream<Pellet3D> pellets3D() { return pellets3D != null ? pellets3D.stream() : Stream.empty(); }
    public Stream<Energizer3D> energizers3D() { return energizers3D != null ? energizers3D.stream() : Stream.empty(); }
    public double floorThickness() { return floor3D.getDepth(); }

    public AnimationManager animationManager() { return animationManager; }
    public ManagedAnimation levelCompletedAnimation() { return levelCompletedFullAnimation; }
    public ManagedAnimation levelCompletedAnimationBeforeCutScene() { return levelCompletedShortAnimation; }
    public ManagedAnimation wallColorFlashingAnimation() { return wallColorFlashingAnimation; }

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
        livesCounter3D().map(LivesCounter3D::lookingAroundAnimation).ifPresent(ManagedAnimation::playFromStart);
    }

    public void onPacManDying(GameState state) {
        state.timer().resetIndefiniteTime(); // expires when level animation ends
        ui.theSound().stopAll();
        // do one last update before dying animation starts
        pac3D.update();
        livesCounter3D().map(LivesCounter3D::lookingAroundAnimation).ifPresent(ManagedAnimation::stop);
        livesCounter3D().map(LivesCounter3D::lookingAroundAnimation).ifPresent(ManagedAnimation::invalidate); //TODO
        ghosts3D.forEach(MutatingGhost3D::stopAllAnimations);
        bonus3D().ifPresent(Bonus3D::expire);
        var animation = new SequentialTransition(
                pauseSec(2),
                doNow(() -> ui.theSound().play(SoundID.PAC_MAN_DEATH)),
                pac3D.dyingAnimation().getOrCreateAnimation(),
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
            ghost3D(personality).setNumberImage(pointsImage);
        });
    }

    public void onLevelComplete(GameState state, ObjectProperty<PerspectiveID> perspectiveIDProperty) {
        state.timer().resetIndefiniteTime(); // expires when animation ends
        ui.theSound().stopAll();
        animationManager.stopAllAnimations();
        // hide 3d food explicitly because level might have been completed using cheat!
        pellets3D.forEach(pellet3D -> pellet3D.shape3D().setVisible(false));
        energizers3D.forEach(Energizer3D::pausePumping);
        energizers3D.forEach(energizer3D -> energizer3D.shape3D().setVisible(false));
        particlesGroupContainer.getChildren().clear();
        house3D.setDoorVisible(false);
        bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
        if (messageView != null) {
            messageView.setVisible(false);
        }
        boolean cutSceneFollows = ui.theGameContext().theGame().cutSceneNumber(gameLevel.number()).isPresent();
        ManagedAnimation levelCompletedAnimation = cutSceneFollows
            ? levelCompletedAnimationBeforeCutScene()
            : levelCompletedAnimation();

        var animation = new SequentialTransition(
            pauseSec(2, () -> {
                perspectiveIDProperty.unbind();
                perspectiveIDProperty.set(PerspectiveID.TOTAL);
            }),
            levelCompletedAnimation.getOrCreateAnimation(),
            pauseSec(1)
        );
        animation.setOnFinished(e -> {
            perspectiveIDProperty.bind(ui.property3DPerspective());
            ui.theGameContext().theGameController().letCurrentGameStateExpire();
        });
        animation.play();
    }

    public void onGameOver(GameState state) {
        state.timer().restartSeconds(3);
        energizers3D().forEach(energizer3D -> energizer3D.shape3D().setVisible(false));
        bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
        ui.theSound().stopAll();
        ui.theSound().play(SoundID.GAME_OVER);
        boolean inOneOf4Cases = randomInt(0, 1000) < 250;
        if (!gameLevel.isDemoLevel() && inOneOf4Cases) {
            ui.showFlashMessageSec(2.5, ui.theAssets().localizedGameOverMessage());
        }
    }

    private void createPellets3D() {
        Mesh pelletMesh = ui.theAssets().theModel3DRepository().pelletMesh();
        float radius = ui.thePrefs().getFloat("3d.pellet.radius");
        var protoMeshView = new MeshView(pelletMesh);
        Bounds bounds = protoMeshView.getBoundsInLocal();
        double meshExtent = Math.max(Math.max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
        double scaling = (2 * radius) / meshExtent;
        var scale = new Scale(scaling, scaling, scaling);
        gameLevel.tilesContainingFood().filter(tile -> !gameLevel.isEnergizerPosition(tile)).forEach(tile -> {
            var meshView = new MeshView(pelletMesh);
            meshView.setMaterial(pelletMaterial);
            meshView.setRotationAxis(Rotate.Z_AXIS);
            meshView.setRotate(90);
            meshView.setTranslateX(tile.x() * TS + HTS);
            meshView.setTranslateY(tile.y() * TS + HTS);
            meshView.setTranslateZ(-6);
            meshView.getTransforms().add(scale);
            Pellet3D pellet3D = new Pellet3D(meshView);
            pellet3D.setTile(tile);
            pellets3D.add(pellet3D);
        });
        pellets3D.trimToSize();
    }

    private void createEnergizers3D() {
        float radius         = ui.thePrefs().getFloat("3d.energizer.radius");
        float floorThickness = ui.thePrefs().getFloat("3d.floor.thickness");
        float minScaling     = ui.thePrefs().getFloat("3d.energizer.scaling.min");
        float maxScaling     = ui.thePrefs().getFloat("3d.energizer.scaling.max");
        gameLevel.tilesContainingFood().filter(gameLevel::isEnergizerPosition).forEach(tile -> {
            Energizer3D energizer3D = createEnergizer3D(tile, radius, floorThickness, minScaling, maxScaling);
            energizers3D.add(energizer3D);
        });
        energizers3D.trimToSize();
    }

    private boolean insideWorldMapArea(Explosion.Particle p) {
        WorldMap worldMap = gameLevel.worldMap();
        int width = worldMap.numCols() * TS;
        int height = worldMap.numRows() * TS;
        return 0 <= p.getTranslateX() && p.getTranslateX() < width && 0 <= p.getTranslateY() && p.getTranslateY() < height;
    }

    private Energizer3D createEnergizer3D(Vector2i tile, float energizerRadius, float floorThickness, float minScaling, float maxScaling) {
        float x = tile.x() * TS + HTS;
        float y = tile.y() * TS + HTS;
        float z = -2 * energizerRadius - 0.5f * floorThickness;
        var energizer3D = new Energizer3D(animationManager, energizerRadius, minScaling, maxScaling);
        energizer3D.setTile(tile);
        energizer3D.shape3D().setMaterial(pelletMaterial);
        energizer3D.shape3D().setTranslateX(x);
        energizer3D.shape3D().setTranslateY(y);
        energizer3D.shape3D().setTranslateZ(z);

        if (EXPLOSIONS) {
            //TODO this still has a memory leak :-(
            var explosion = new Explosion(animationManager, energizer3D.shape3D(), particlesGroupContainer, pelletMaterial,
                    particle -> particle.getTranslateZ() >= -1 && insideWorldMapArea(particle));
            energizer3D.setEatenAnimation(explosion);
        }

        return energizer3D;
    }

    public void showAnimatedMessage(String message, float displaySeconds, double centerX, double centerY) {
        if (messageView != null) {
            messageView.dispose();
            root.getChildren().remove(messageView);
        }
        messageView = MessageView.builder()
            .displaySeconds(displaySeconds)
            .borderColor(Color.WHITE)
            .textColor(Color.YELLOW)
            .font(ui.theAssets().arcadeFont(6))
            .text(message)
            .build(animationManager);
        root.getChildren().add(messageView);
        messageView.showCenteredAt(centerX, centerY);
    }

    public void updateBonus3D(Bonus bonus) {
        requireNonNull(bonus);
        if (bonus3D != null) {
            root.getChildren().remove(bonus3D);
            bonus3D.dispose();
        }
        bonus3D = new Bonus3D(animationManager, bonus,
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
        setDrawModeForAllShapesExcept(root, Pellet3D::isPellet3D, newDrawMode);
    }

    private void handleWallHeightChange(ObservableValue<? extends Number> obs, Number oldHeight, Number newHeight) {
        obstacleBaseHeightProperty.set(newHeight.doubleValue());
    }

    // still work in progress...

    public void dispose() {
        Logger.info("Disposing game level 3D...");

        animationManager.stopAllAnimations();
        animationManager.disposeAllAnimations();
        wallColorFlashingAnimation = null;
        levelCompletedFullAnimation = null;
        levelCompletedShortAnimation = null;
        Logger.info("Stopped, disposed and removed all managed animations");

        ui.property3DDrawMode().removeListener(this::handleDrawModeChange);
        Logger.info("Removed 'draw mode' listener");

        ui.property3DWallHeight().removeListener(this::handleWallHeightChange);
        Logger.info("Removed 'wall height' listener");

        houseOpenProperty.removeListener(this::handleHouseOpenChange);
        Logger.info("Removed 'house open' listener");

        livesCountProperty.unbind();
        houseOpenProperty.unbind();
        obstacleBaseHeightProperty.unbind();
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
        if (pellets3D != null) {
            pellets3D.forEach(Pellet3D::dispose);
            pellets3D = null;
            Logger.info("Disposed 3D pellets");
        }
        if (energizers3D != null) {
            energizers3D.forEach(Energizer3D::dispose);
            energizers3D = null;
            Logger.info("Disposed 3D energizers");
        }
        particlesGroupContainer.getChildren().clear();
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