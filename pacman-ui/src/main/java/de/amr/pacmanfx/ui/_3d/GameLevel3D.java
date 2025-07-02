/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.Obstacle;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapRenderer3D;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static de.amr.pacmanfx.ui.PacManGames.*;
import static de.amr.pacmanfx.ui.PacManGames_UI.*;
import static de.amr.pacmanfx.uilib.Ufx.*;
import static java.util.Objects.requireNonNull;

/**
 * 3D representation of game level.
 */
public class GameLevel3D {

    private static boolean isInsideWorldMap(WorldMap worldMap, double x, double y) {
        return 0 <= x && x < worldMap.numCols() * TS && 0 <= y && y < worldMap.numRows() * TS;
    }

    private static boolean isObstacleTheWorldBorder(WorldMap worldMap, Obstacle obstacle) {
        Vector2i start = obstacle.startPoint();
        if (obstacle.isClosed()) {
            return start.x() == TS || start.y() == GameLevel.EMPTY_ROWS_OVER_MAZE * TS + HTS;
        } else {
            return start.x() == 0 || start.x() == worldMap.numCols() * TS;
        }
    }

    private final BooleanProperty houseOpenPy = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            if (houseOpenPy.get()) {
                house3D.doorOpenCloseAnimation().playFromStart();
            }
        }
    };

    private final IntegerProperty livesCountPy         = new SimpleIntegerProperty(0);
    private final DoubleProperty  obstacleBaseHeightPy = new SimpleDoubleProperty(Settings3D.OBSTACLE_3D_BASE_HEIGHT);
    private final DoubleProperty  wallOpacityPy        = new SimpleDoubleProperty(1);
    private final DoubleProperty  houseBaseHeightPy    = new SimpleDoubleProperty(Settings3D.HOUSE_3D_BASE_HEIGHT);
    private final BooleanProperty houseLightOnPy       = new SimpleBooleanProperty(false);

    private final AnimationManager animationManager = new AnimationManager();
    private ManagedAnimation wallColorFlashingAnimation;
    private ManagedAnimation wallsDisappearingAnimation;
    private ManagedAnimation levelCompletedAnimation;
    private ManagedAnimation levelCompletedAnimationBeforeCutScene;

    private MeshView[] dressMeshViews = {
            new MeshView(Model3DRepository.get().ghostDressMesh()),
            new MeshView(Model3DRepository.get().ghostDressMesh()),
            new MeshView(Model3DRepository.get().ghostDressMesh()),
            new MeshView(Model3DRepository.get().ghostDressMesh()),
    };

    private MeshView[] pupilsMeshViews = {
            new MeshView(Model3DRepository.get().ghostPupilsMesh()),
            new MeshView(Model3DRepository.get().ghostPupilsMesh()),
            new MeshView(Model3DRepository.get().ghostPupilsMesh()),
            new MeshView(Model3DRepository.get().ghostPupilsMesh()),
    };

    private MeshView[] eyesMeshViews = new MeshView[] {
            new MeshView(Model3DRepository.get().ghostEyeballsMesh()),
            new MeshView(Model3DRepository.get().ghostEyeballsMesh()),
            new MeshView(Model3DRepository.get().ghostEyeballsMesh()),
            new MeshView(Model3DRepository.get().ghostEyeballsMesh()),
    };

    private PhongMaterial wallBaseMaterial;
    private PhongMaterial wallTopMaterial;
    private PhongMaterial cornerBaseMaterial;
    private PhongMaterial cornerTopMaterial;

    private WorldMapColorScheme colorScheme;

    private Group root = new Group();
    private Group mazeGroup = new Group();
    private Group maze3D = new Group();
    private AmbientLight ambientLight;
    private ArcadeHouse3D house3D;
    private Box floor3D;
    private LevelCounter3D levelCounter3D;
    private Node[] livesCounterShapes = new Node[Settings3D.LIVES_COUNTER_3D_CAPACITY];
    private LivesCounter3D livesCounter3D;
    private PacBase3D pac3D;
    private List<MutatingGhost3D> ghosts3D;
    private MessageView messageView;
    private Bonus3D bonus3D;
    private List<Pellet3D> pellets3D = new ArrayList<>();
    private ArrayList<Energizer3D> energizers3D = new ArrayList<>();

    // Note: The order in which children are added to the root matters!
    // Walls and house must be added *after* the actors, otherwise the transparency is not working correctly.
    public GameLevel3D(GameLevel gameLevel, WorldMapColorScheme proposedColorScheme)
    {
        requireNonNull(gameLevel);

        requireNonNull(proposedColorScheme);
        // Add some contrast with floor if wall fill color is black
        colorScheme = proposedColorScheme.fill().equals(Color.BLACK)
            ? new WorldMapColorScheme(Color.grayRgb(42), proposedColorScheme.stroke(), proposedColorScheme.door(), proposedColorScheme.pellet())
            : proposedColorScheme;

        {
            ambientLight = new AmbientLight();
            ambientLight.colorProperty().bind(PY_3D_LIGHT_COLOR);
            root.getChildren().add(ambientLight);
        }

        {
            levelCounter3D = new LevelCounter3D(animationManager, theGame().hud().levelCounter());
            levelCounter3D.setTranslateX(gameLevel.worldMap().numCols() * TS - 2 * TS);
            levelCounter3D.setTranslateY(2 * TS);
            levelCounter3D.spinningAnimation().playFromStart();
            root.getChildren().add(levelCounter3D);
        }

        {
            for (int i = 0; i < livesCounterShapes.length; ++i) {
                livesCounterShapes[i] = theUI().configuration().createLivesCounter3D();
            }
            livesCounter3D = new LivesCounter3D(animationManager, livesCounterShapes);
            livesCounter3D.setTranslateX(2 * TS);
            livesCounter3D.setTranslateY(2 * TS);
            livesCounter3D.livesCountProperty().bind(livesCountPy);
            livesCounter3D.pillarColorProperty().set(Settings3D.LIVES_COUNTER_PILLAR_COLOR);
            livesCounter3D.plateColorProperty().set(Settings3D.LIVES_COUNTER_PLATE_COLOR);
            livesCounter3D.light().colorProperty().set(Color.CORNFLOWERBLUE);
            livesCounter3D.lookingAroundAnimation().playFromStart();
            root.getChildren().add(livesCounter3D);
        }

        {
            pac3D = theUI().configuration().createPac3D(animationManager, gameLevel.pac());
            root.getChildren().addAll(pac3D.root(), pac3D.light());
        }

        {
            ghosts3D = gameLevel.ghosts()
                .map(ghost -> new MutatingGhost3D(animationManager,
                    theAssets(), theUI().configuration().assetNamespace(),
                    dressMeshViews[ghost.personality()],
                    pupilsMeshViews[ghost.personality()],
                    eyesMeshViews[ghost.personality()],
                    ghost,
                    Settings3D.GHOST_3D_SIZE,
                    gameLevel.data().numFlashes()
                )).toList();
            root.getChildren().addAll(ghosts3D);
        }

        root.getChildren().add(mazeGroup);

        Logger.info("Build 3D maze for map (URL '{}') and color scheme {}", gameLevel.worldMap().url(), colorScheme);

        floor3D = createFloor3D(gameLevel.worldMap().numCols() * TS, gameLevel.worldMap().numRows() * TS);

        wallBaseMaterial = new PhongMaterial();
        wallTopMaterial = new PhongMaterial();
        cornerBaseMaterial= new PhongMaterial();
        cornerTopMaterial = new PhongMaterial();

        wallBaseMaterial.diffuseColorProperty().bind(Bindings.createObjectBinding(
            () -> opaqueColor(colorScheme.stroke(), wallOpacityPy.get()), wallOpacityPy
        ));
        wallBaseMaterial.specularColorProperty().bind(wallBaseMaterial.diffuseColorProperty().map(Color::brighter));

        wallTopMaterial.setDiffuseColor(colorScheme.fill());
        wallTopMaterial.setSpecularColor(colorScheme.fill().brighter());

        wallOpacityPy.bind(PY_3D_WALL_OPACITY);

        cornerBaseMaterial.setDiffuseColor(colorScheme.stroke()); // for now use same color
        cornerBaseMaterial.specularColorProperty().bind(cornerBaseMaterial.diffuseColorProperty().map(Color::brighter));

        cornerTopMaterial.setDiffuseColor(colorScheme.fill());
        cornerTopMaterial.specularColorProperty().bind(cornerTopMaterial.diffuseColorProperty().map(Color::brighter));

        TerrainMapRenderer3D r3D = new TerrainMapRenderer3D();
        r3D.setWallBaseHeightProperty(obstacleBaseHeightPy);
        r3D.setWallTopHeight(Settings3D.OBSTACLE_3D_TOP_HEIGHT);
        r3D.setWallTopMaterial(wallTopMaterial);
        r3D.setCornerBaseMaterial(cornerBaseMaterial);
        r3D.setCornerTopMaterial(wallTopMaterial); // for now such that power animation also affects corner top

        //TODO check this:
        obstacleBaseHeightPy.set(PY_3D_WALL_HEIGHT.get());

        for (Obstacle obstacle : gameLevel.worldMap().obstacles()) {
            Vector2i tile = tileAt(obstacle.startPoint().toVector2f());
            if (gameLevel.house().isPresent() && !gameLevel.house().get().isTileInHouseArea(tile)) {
                r3D.setWallThickness(Settings3D.OBSTACLE_3D_THICKNESS);
                r3D.setWallBaseMaterial(wallBaseMaterial);
                r3D.setWallTopMaterial(wallTopMaterial);
                r3D.renderObstacle3D(maze3D, obstacle, isObstacleTheWorldBorder(gameLevel.worldMap(), obstacle));
            }
        }

        if (gameLevel.house().isEmpty()) {
            Logger.error("There is no house in this game level!");
        } else {
            house3D = new ArcadeHouse3D(
                animationManager,
                gameLevel.house().get(),
                colorScheme.fill(),
                colorScheme.stroke(),
                colorScheme.door()
            );
            house3D.wallBaseHeightProperty().bind(houseBaseHeightPy);
            house3D.light().lightOnProperty().bind(houseLightOnPy);
            maze3D.getChildren().add(house3D);
        }

        mazeGroup.getChildren().addAll(floor3D, maze3D);

        createPelletsAndEnergizers3D(gameLevel, colorScheme, Model3DRepository.get().pelletMesh());
        energizers3D.forEach(energizer3D -> root.getChildren().add(energizer3D.shape3D()));
        pellets3D.forEach(pellet3D -> root.getChildren().add(pellet3D.shape3D()));

        PY_3D_WALL_HEIGHT.addListener(this::handleWallHeightChange);
        PY_3D_DRAW_MODE.addListener(this::handleDrawModeChange);

        root.setMouseTransparent(true); // this increases performance, they say...

        wallColorFlashingAnimation = new ManagedAnimation(animationManager, "MazeWallColorFlashing") {
            @Override
            protected Animation createAnimation() {
                return new MaterialColorAnimation(Duration.seconds(0.25), wallTopMaterial, colorScheme.fill(), colorScheme.stroke());
            }
        };

        wallsDisappearingAnimation = new ManagedAnimation(animationManager, "Maze_WallsDisappearing") {
            @Override
            protected Animation createAnimation() {
                var totalDuration = Duration.seconds(1);
                var houseDisappears = new Timeline(
                    new KeyFrame(totalDuration.multiply(0.33), new KeyValue(houseBaseHeightPy, 0, Interpolator.EASE_IN)));
                var obstaclesDisappear = new Timeline(
                    new KeyFrame(totalDuration.multiply(0.33), new KeyValue(obstacleBaseHeightPy, 0, Interpolator.EASE_IN)));
                var animation = new SequentialTransition(houseDisappears, obstaclesDisappear);
                animation.setOnFinished(e -> maze3D.setVisible(false));
                return animation;
            }
        };

        levelCompletedAnimation = new ManagedAnimation(animationManager, "Level_Complete") {
            @Override
            protected Animation createAnimation() {
                int levelNumber = gameLevel.number();
                int numMazeFlashes = gameLevel.data().numFlashes();
                boolean showFlashMessage = randomInt(1, 1000) < 250; // every 4th time also show a message
                return new SequentialTransition(
                    now(() -> {
                        livesCounter3D.light().setLightOn(false);
                        if (showFlashMessage) {
                            theUI().showFlashMessageSec(3, theAssets().localizedLevelCompleteMessage(levelNumber));
                        }
                    }),
                    doAfterSec(0.5, () -> gameLevel.ghosts().forEach(Ghost::hide)),
                    doAfterSec(0.5, createMazeFlashAnimation(numMazeFlashes, 250)),
                    doAfterSec(0.5, () -> gameLevel.pac().hide()),
                    doAfterSec(0.5, () -> {
                            var spin360 = new RotateTransition(Duration.seconds(1.5), root);
                            spin360.setAxis(theRNG().nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS);
                            spin360.setFromAngle(0);
                            spin360.setToAngle(360);
                            spin360.setInterpolator(Interpolator.LINEAR);
                            return spin360;
                    }),
                    doAfterSec(0.5, () -> theSound().playLevelCompleteSound()),
                    doAfterSec(0.5, wallsDisappearingAnimation.getOrCreateAnimation()),
                    doAfterSec(1.0, () -> theSound().playLevelChangedSound())
                );
            }
        };

        levelCompletedAnimationBeforeCutScene = new ManagedAnimation(animationManager, "Level_Complete_Before_CutScene") {
            @Override
            protected Animation createAnimation() {
                return new SequentialTransition(
                    doAfterSec(0.5, () -> gameLevel.ghosts().forEach(Ghost::hide)),
                    doAfterSec(0.5, createMazeFlashAnimation(gameLevel.data().numFlashes(), 250)),
                    doAfterSec(0.5, () -> gameLevel.pac().hide())
                );
            }
        };
    }

    public Group root() { return root; }
    public PacBase3D pac3D() { return pac3D; }
    public Stream<MutatingGhost3D> ghosts3D() { return ghosts3D.stream(); }
    public MutatingGhost3D ghost3D(byte id) { return ghosts3D.get(id); }
    public Optional<Bonus3D> bonus3D() { return Optional.ofNullable(bonus3D); }
    public LevelCounter3D levelCounter3D() { return levelCounter3D; }
    public LivesCounter3D livesCounter3D() { return livesCounter3D; }
    public Stream<Pellet3D> pellets3D() { return pellets3D.stream(); }
    public Stream<Energizer3D> energizers3D() { return energizers3D.stream(); }
    public Color floorColor() { return PY_3D_FLOOR_COLOR.get(); }
    public double floorThickness() { return floor3D.getDepth(); }

    public AnimationManager animationManager() { return animationManager; }
    public ManagedAnimation levelCompletedAnimation() { return levelCompletedAnimation; }
    public ManagedAnimation levelCompletedAnimationBeforeCutScene() { return levelCompletedAnimationBeforeCutScene; }
    public ManagedAnimation wallColorFlashingAnimation() { return wallColorFlashingAnimation; }

    /**
     * Called on each clock tick (frame).
     *
     * @param gameLevel the game level
     */
    public void tick(GameLevel gameLevel) {
        pac3D.update(gameLevel);
        ghosts3D.forEach(ghost3D -> ghost3D.update(gameLevel));
        bonus3D().ifPresent(Bonus3D::update);
        boolean houseAccessRequired = gameLevel
            .ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .anyMatch(Ghost::isVisible);
        houseLightOnPy.set(houseAccessRequired);

        gameLevel.house().ifPresent(house -> {
            boolean ghostNearHouseEntry = gameLevel
                .ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
                .filter(ghost -> ghost.position().euclideanDist(house.entryPosition()) <= Settings3D.HOUSE_3D_SENSITIVITY)
                .anyMatch(Ghost::isVisible);
            houseOpenPy.set(ghostNearHouseEntry);
        });

        int livesCounterSize = theGame().lifeCount() - 1;
        // when the game starts and Pac-Man is not yet visible, show one more
        boolean oneMore = theGameState() == GameState.STARTING_GAME && !gameLevel.pac().isVisible();
        if (oneMore) livesCounterSize += 1;
        livesCountPy.set(livesCounterSize);

        boolean visible = theGame().canStartNewGame();
        livesCounter3D.setVisible(visible);
        livesCounter3D.light().setLightOn(visible);
    }

    public void complete() {
        animationManager.stopAllAnimations();
        // hide explicitly because level might have been completed using cheat!
        pellets3D.forEach(pellet3D -> pellet3D.shape3D().setVisible(false));
        energizers3D.forEach(energizer3D -> {
            energizer3D.pumpingAnimation().stop();
            energizer3D.shape3D().setVisible(false);
        });
        house3D.setDoorVisible(false);
        bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
    }

    private Box createFloor3D(double sizeX, double sizeY) {
        var floor3D = new Box(sizeX + 2 * Settings3D.FLOOR_3D_PADDING, sizeY, Settings3D.FLOOR_3D_THICKNESS);
        floor3D.translateXProperty().bind(floor3D.widthProperty().divide(2).subtract(Settings3D.FLOOR_3D_PADDING));
        floor3D.translateYProperty().bind(floor3D.heightProperty().divide(2));
        floor3D.translateZProperty().bind(floor3D.depthProperty().divide(2));
        floor3D.materialProperty().bind(PY_3D_FLOOR_COLOR.map(Ufx::coloredPhongMaterial));
        return floor3D;
    }

    private void createPelletsAndEnergizers3D(GameLevel gameLevel, WorldMapColorScheme colorScheme, Mesh pelletMesh) {
        final PhongMaterial pelletMaterial = coloredPhongMaterial(colorScheme.pellet());
        gameLevel.tilesContainingFood().forEach(tile -> {
            if (gameLevel.isEnergizerPosition(tile)) {
                var center = new Point3D(
                    tile.x() * TS + HTS,
                    tile.y() * TS + HTS,
                    -2 * Settings3D.ENERGIZER_3D_RADIUS - 0.5 * Settings3D.FLOOR_3D_THICKNESS  // sitting just on floor
                );
                var energizer3D = new Energizer3D(Settings3D.ENERGIZER_3D_RADIUS, animationManager, true);
                energizer3D.setMaterial(pelletMaterial);
                energizer3D.setTile(tile);
                energizer3D.setTranslateX(center.getX());
                energizer3D.setTranslateY(center.getY());
                energizer3D.setTranslateZ(center.getZ());
                var explosion = new ManagedAnimation(animationManager, "Energizer_Explosion") {
                    @Override
                    protected Animation createAnimation() {
                        return new SquirtingAnimation(root, Duration.seconds(2), 23, 69, pelletMaterial, center) {
                            @Override
                            public boolean particleShouldVanish(Particle particle) {
                                return particle.getTranslateZ() >= -1
                                        && isInsideWorldMap(gameLevel.worldMap(), particle.getTranslateX(), particle.getTranslateY());
                            }
                        };
                    }
                };
                energizer3D.setEatenEffectAnimation(explosion);
                energizers3D.add(energizer3D);
            } else {
                var center = new Point3D(tile.x() * TS + HTS, tile.y() * TS + HTS, -6);
                var pelletShape = new MeshView(pelletMesh);
                pelletShape.setRotationAxis(Rotate.Z_AXIS);
                pelletShape.setRotate(90);
                pelletShape.setTranslateX(center.getX());
                pelletShape.setTranslateY(center.getY());
                pelletShape.setTranslateZ(center.getZ());
                pelletShape.setMaterial(pelletMaterial);
                var pellet3D = new Pellet3D(pelletShape, Settings3D.PELLET_3D_RADIUS);
                pellet3D.setTile(tile);
                pellets3D.add(pellet3D);
            }
        });
        energizers3D.trimToSize();
    }

    private void setDrawModeForTree(Node root, DrawMode drawMode) {
        root.lookupAll("*").stream()
            .filter(Shape3D.class::isInstance)
            .map(Shape3D.class::cast)
            .forEach(shape3D -> shape3D.setDrawMode(drawMode));
    }

    public void showAnimatedMessage(String text, float displaySeconds, double centerX, double y) {
        if (messageView != null) {
            root.getChildren().remove(messageView);
        }
        messageView = MessageView.builder()
                .text(text)
                .font(theAssets().arcadeFont(6))
                .borderColor(Color.WHITE)
                .displaySeconds(displaySeconds)
                .textColor(Color.YELLOW)
                .build(animationManager);

        double halfHeight = 0.5 * messageView.getBoundsInLocal().getHeight();
        messageView.setTranslateX(centerX - 0.5 * messageView.getFitWidth());
        messageView.setTranslateY(y);
        messageView.setTranslateZ(halfHeight); // just under floor
        root.getChildren().add(messageView);
        messageView.movementAnimation().playFromStart();
    }

    public void updateBonus3D(Bonus bonus) {
        requireNonNull(bonus);
        if (bonus3D != null) {
            mazeGroup.getChildren().remove(bonus3D);
            bonus3D.destroy();
        }
        bonus3D = new Bonus3D(animationManager, bonus,
            theUI().configuration().bonusSymbolImage(bonus.symbol()),
            theUI().configuration().bonusValueImage(bonus.symbol()));
        mazeGroup.getChildren().add(bonus3D);
        bonus3D.showEdible();
    }

    private Animation createMazeFlashAnimation(int numFlashes, int flashDurationMillis) {
        if (numFlashes == 0) {
            return pauseSec(1.0);
        }
        var flashing = new Timeline(
            new KeyFrame(Duration.millis(0.5 * flashDurationMillis),
                new KeyValue(obstacleBaseHeightPy, 0, Interpolator.EASE_BOTH)
            )
        );
        flashing.setAutoReverse(true);
        flashing.setCycleCount(2 * numFlashes);
        return flashing;
    }

    private void handleDrawModeChange(ObservableValue<? extends DrawMode> py, DrawMode ov, DrawMode drawMode) {
        if (inDestroyPhase()) return; //TODO how can that be?
        setDrawModeForTree(mazeGroup, drawMode);
        setDrawModeForTree(levelCounter3D, drawMode);
        setDrawModeForTree(livesCounter3D, drawMode);
        setDrawModeForTree(pac3D.root(), drawMode);
        ghosts3D.forEach(ghost3D -> setDrawModeForTree(ghost3D, drawMode));
        Logger.info("Draw mode set to {}", drawMode);
    }

    private void handleWallHeightChange(ObservableValue<? extends Number> py, Number ov, Number newHeight) {
        if (inDestroyPhase()) return; //TODO how can that be?
        obstacleBaseHeightPy.set(newHeight.doubleValue());
    }

    // experimental

    private boolean inDestroyPhase;

    public boolean inDestroyPhase() {
        return inDestroyPhase;
    }

    // Attempt to help objects getting garbage-collected
    public void destroy() {
        if (inDestroyPhase) {
            Logger.warn("destroy() called while in destroy phase?");
            return;
        }
        inDestroyPhase = true;
        Logger.info("Destroying game level 3D, clearing resources...");

        PY_3D_DRAW_MODE.removeListener(this::handleDrawModeChange);
        Logger.info("Removed draw mode listener");

        PY_3D_WALL_HEIGHT.removeListener(this::handleWallHeightChange);
        Logger.info("Removed wall height listener");

        animationManager.stopAllAnimations();
        animationManager.removeAllAnimations();
        wallColorFlashingAnimation = null;
        wallsDisappearingAnimation = null;
        levelCompletedAnimation = null;
        levelCompletedAnimationBeforeCutScene = null;
        Logger.info("Stopped and removed all managed animations");

        wallBaseMaterial = null;
        wallTopMaterial = null;
        cornerBaseMaterial= null;
        cornerTopMaterial = null;
        Logger.info("Cleared materials");

        for (MeshView meshView : dressMeshViews) {
            meshView.setMesh(null);
        }
        dressMeshViews = null;
        Logger.info("Cleared dress mesh views");

        for (MeshView meshView : pupilsMeshViews) {
            meshView.setMesh(null);
        }
        pupilsMeshViews = null;
        Logger.info("Cleared pupils mesh views");

        for (MeshView meshView : eyesMeshViews) {
            meshView.setMesh(null);
        }
        eyesMeshViews = null;
        Logger.info("Cleared eyes mesh views");

        pellets3D.forEach(Pellet3D::destroy);
        pellets3D = null;
        Logger.info("Cleared 3D pellets");

        energizers3D.forEach(Energizer3D::destroy);
        energizers3D = null;
        Logger.info("Cleared 3D energizers");

        root.getChildren().clear();
        root = null;
        Logger.info("Removed all child nodes of game level 3D root ");

        mazeGroup = null;

        //TODO
        floor3D = null;

        if (house3D != null) {
            house3D.light().lightOnProperty().unbind();
            house3D.destroy();
            house3D = null;
            Logger.info("Destroyed and cleared 3D house");
        }

        if (maze3D != null) {
            maze3D = null;
        }

        ambientLight.colorProperty().unbind();
        ambientLight = null;
        Logger.info("Removed ambient light");

        livesCounterShapes = null;
        livesCounter3D.destroy();
        livesCounter3D = null;
        Logger.info("Removed lives counter 3D");

        levelCounter3D = null;
        Logger.info("Removed level counter 3D");

        pac3D = null;
        Logger.info("Removed Pac 3D");

        ghosts3D.forEach(MutatingGhost3D::destroy);
        ghosts3D = null;
        Logger.info("Removed ghosts 3D");

        if (bonus3D != null) {
            bonus3D.destroy();
            bonus3D = null;
            Logger.info("Removed bonus 3D");
        }

        if (messageView != null) {
            messageView.destroy();
            messageView = null;
        }
    }
}