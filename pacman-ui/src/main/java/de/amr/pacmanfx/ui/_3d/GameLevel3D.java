/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.Obstacle;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.animation.MaterialColorAnimation;
import de.amr.pacmanfx.uilib.animation.SquirtingAnimation;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.*;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static de.amr.pacmanfx.ui.GameUI.Settings3D;
import static de.amr.pacmanfx.uilib.Ufx.*;
import static java.util.Objects.requireNonNull;

/**
 * 3D representation of game level.
 */
public class GameLevel3D extends Group implements Destroyable {

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

    private final BooleanProperty houseOpenProperty = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            if (houseOpenProperty.get()) {
                house3D.doorOpenCloseAnimation().playFromStart();
            }
        }
    };

    private final IntegerProperty livesCountProperty = new SimpleIntegerProperty(0);
    private final DoubleProperty  obstacleBaseHeightProperty = new SimpleDoubleProperty(Settings3D.OBSTACLE_3D_BASE_HEIGHT);
    private final DoubleProperty  wallOpacityProperty = new SimpleDoubleProperty(1);
    private final DoubleProperty  houseBaseHeightProperty = new SimpleDoubleProperty(Settings3D.HOUSE_3D_BASE_HEIGHT);
    private final BooleanProperty houseLightOnProperty = new SimpleBooleanProperty(false);

    private final GameUI ui;
    private final GameLevel gameLevel;

    private final AnimationManager animationManager = new AnimationManager();
    private ManagedAnimation wallColorFlashingAnimation;
    private ManagedAnimation levelCompletedFullAnimation;
    private ManagedAnimation levelCompletedShortAnimation;

    private MeshView[] dressMeshViews;
    private MeshView[] pupilsMeshViews;
    private MeshView[] eyesMeshViews;

    private PhongMaterial wallBaseMaterial;
    private PhongMaterial wallTopMaterial;
    private PhongMaterial cornerBaseMaterial;
    private PhongMaterial cornerTopMaterial;

    private WorldMapColorScheme colorScheme;
    private TerrainRenderer3D r3D;

    private Group mazeGroup = new Group();
    private Group maze3D = new Group();
    private AmbientLight ambientLight;
    private ArcadeHouse3D house3D;
    private Floor3D floor3D;
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
    public GameLevel3D(
        GameUI ui,
        Model3DRepository model3DRepository,
        WorldMapColorScheme proposedColorScheme)
    {
        this.ui = requireNonNull(ui);
        this.gameLevel = requireNonNull(ui.theGameContext().theGameLevel());

        requireNonNull(model3DRepository);
        requireNonNull(proposedColorScheme);

        // Add some contrast with floor if wall fill color is black
        colorScheme = proposedColorScheme.fill().equals(Color.BLACK)
            ? new WorldMapColorScheme(Color.grayRgb(42), proposedColorScheme.stroke(), proposedColorScheme.door(), proposedColorScheme.pellet())
            : proposedColorScheme;

        dressMeshViews = new MeshView[] {
                new MeshView(model3DRepository.ghostDressMesh()),
                new MeshView(model3DRepository.ghostDressMesh()),
                new MeshView(model3DRepository.ghostDressMesh()),
                new MeshView(model3DRepository.ghostDressMesh()),
        };

        pupilsMeshViews = new MeshView[] {
                new MeshView(model3DRepository.ghostPupilsMesh()),
                new MeshView(model3DRepository.ghostPupilsMesh()),
                new MeshView(model3DRepository.ghostPupilsMesh()),
                new MeshView(model3DRepository.ghostPupilsMesh()),
        };

        eyesMeshViews = new MeshView[] {
                new MeshView(model3DRepository.ghostEyeballsMesh()),
                new MeshView(model3DRepository.ghostEyeballsMesh()),
                new MeshView(model3DRepository.ghostEyeballsMesh()),
                new MeshView(model3DRepository.ghostEyeballsMesh()),
        };

        ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(ui.property3DLightColor());
        getChildren().add(ambientLight);

        levelCounter3D = new LevelCounter3D(ui.theConfiguration(), animationManager, ui.theGameContext().theGame().hud().levelCounter());
        levelCounter3D.setTranslateX(ui.theGameContext().theGameLevel().worldMap().numCols() * TS - 2 * TS);
        levelCounter3D.setTranslateY(2 * TS);
        levelCounter3D.spinningAnimation().playFromStart();
        getChildren().add(levelCounter3D);

        for (int i = 0; i < livesCounterShapes.length; ++i) {
            livesCounterShapes[i] = ui.theConfiguration().createLivesCounterShape3D(model3DRepository);
        }
        livesCounter3D = new LivesCounter3D(animationManager, livesCounterShapes);
        livesCounter3D.setTranslateX(2 * TS);
        livesCounter3D.setTranslateY(2 * TS);
        livesCounter3D.livesCountProperty().bind(livesCountProperty);
        livesCounter3D.pillarColorProperty().set(Settings3D.LIVES_COUNTER_PILLAR_COLOR);
        livesCounter3D.plateColorProperty().set(Settings3D.LIVES_COUNTER_PLATE_COLOR);
        livesCounter3D.light().colorProperty().set(Color.CORNFLOWERBLUE);
        livesCounter3D.lookingAroundAnimation().playFromStart();
        getChildren().add(livesCounter3D);

        pac3D = ui.theConfiguration().createPac3D(model3DRepository, animationManager, gameLevel.pac());
        pac3D.init();
        getChildren().addAll(pac3D, pac3D.light());

        final String ans = ui.theConfiguration().assetNamespace();
        ghosts3D = gameLevel.ghosts().map(ghost -> {
            var ghostColoring = new GhostColoring(
                    ui.theAssets().color("%s.ghost.%d.color.normal.dress".formatted(ans, ghost.personality())),
                    ui.theAssets().color("%s.ghost.%d.color.normal.pupils".formatted(ans, ghost.personality())),
                    ui.theAssets().color("%s.ghost.%d.color.normal.eyeballs".formatted(ans, ghost.personality())),
                    ui.theAssets().color("%s.ghost.color.frightened.dress".formatted(ans)),
                    ui.theAssets().color("%s.ghost.color.frightened.pupils".formatted(ans)),
                    ui.theAssets().color("%s.ghost.color.frightened.eyeballs".formatted(ans)),
                    ui.theAssets().color("%s.ghost.color.flashing.dress".formatted(ans)),
                    ui.theAssets().color("%s.ghost.color.flashing.pupils".formatted(ans))
            );
            return new MutatingGhost3D(
                animationManager,
                gameLevel,
                ghost,
                ghostColoring,
                dressMeshViews[ghost.personality()],
                pupilsMeshViews[ghost.personality()],
                eyesMeshViews[ghost.personality()],
                Settings3D.GHOST_3D_SIZE,
                gameLevel.data().numFlashes()
            );
        }).toList();
        getChildren().addAll(ghosts3D);
        ghosts3D.forEach(ghost3D -> ghost3D.init(gameLevel));

        getChildren().add(mazeGroup);

        Logger.info("Build 3D maze for map (URL '{}') and color scheme {}", gameLevel.worldMap().url(), colorScheme);

        {
            floor3D = new Floor3D(
                gameLevel.worldMap().numCols() * TS,
                gameLevel.worldMap().numRows() * TS,
                Settings3D.FLOOR_3D_THICKNESS,
                Settings3D.FLOOR_3D_PADDING
            );
            floor3D.materialProperty().bind(ui.property3DFloorColor().map(Ufx::coloredPhongMaterial));

            wallBaseMaterial = new PhongMaterial();
            wallBaseMaterial.diffuseColorProperty().bind(Bindings.createObjectBinding(
                    () -> opaqueColor(colorScheme.stroke(), wallOpacityProperty.get()),
                wallOpacityProperty
            ));
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

            wallOpacityProperty.bind(ui.property3DWallOpacity());

            r3D = new TerrainRenderer3D();
            r3D.setOnWallCreated(wall3D -> wall3D.baseHeightProperty().bind(obstacleBaseHeightProperty));
            r3D.setCylinderDivisions(24);
            for (Obstacle obstacle : gameLevel.worldMap().obstacles()) {
                Vector2i tile = tileAt(obstacle.startPoint().toVector2f());
                if (gameLevel.house().isEmpty() || !gameLevel.house().get().isTileInHouseArea(tile)) {
                    r3D.renderObstacle3D(
                        maze3D,
                        obstacle, isObstacleTheWorldBorder(gameLevel.worldMap(), obstacle),
                        Settings3D.OBSTACLE_3D_WALL_THICKNESS,
                        wallBaseMaterial, wallTopMaterial);
                }
            }

            if (gameLevel.house().isEmpty()) {
                Logger.error("There is no house in this game level!");
            } else {
                house3D = new ArcadeHouse3D(
                    animationManager,
                    gameLevel.house().get(),
                    Settings3D.HOUSE_3D_BASE_HEIGHT,
                    Settings3D.HOUSE_3D_WALL_THICKNESS,
                    Settings3D.HOUSE_3D_OPACITY,
                    colorScheme.fill(),
                    colorScheme.stroke(),
                    colorScheme.door()
                );
                house3D.wallBaseHeightProperty().bind(houseBaseHeightProperty);
                house3D.light().lightOnProperty().bind(houseLightOnProperty);
                maze3D.getChildren().add(house3D);
            }

            mazeGroup.getChildren().addAll(floor3D, maze3D);
        }

        createPelletsAndEnergizers3D(colorScheme, model3DRepository.pelletMesh());
        energizers3D.stream().map(Eatable3D::shape3D).forEach(getChildren()::add);
        pellets3D   .stream().map(Eatable3D::shape3D).forEach(getChildren()::add);

        ui.property3DWallHeight().addListener(this::handleWallHeightChange);
        ui.property3DDrawMode().addListener(this::handleDrawModeChange);

        setMouseTransparent(true); // this increases performance, they say...

        wallColorFlashingAnimation = new ManagedAnimation(animationManager, "MazeWallColorFlashing") {
            @Override
            protected Animation createAnimation() {
                return new MaterialColorAnimation(Duration.seconds(0.25), wallTopMaterial, colorScheme.fill(), colorScheme.stroke());
            }
        };

        levelCompletedFullAnimation = new LevelCompletedAnimation(ui, animationManager, this);
        levelCompletedShortAnimation = new LevelCompletedAnimationShort(animationManager, this);
    }

    public GameLevel gameLevel() {
        return gameLevel;
    }

    public DoubleProperty houseBaseHeightProperty() {
        return houseBaseHeightProperty;
    }

    public DoubleProperty obstacleBaseHeightProperty() {
        return obstacleBaseHeightProperty;
    }

    public PacBase3D pac3D() { return pac3D; }
    public Stream<MutatingGhost3D> ghosts3D() { return ghosts3D.stream(); }
    public MutatingGhost3D ghost3D(byte id) { return ghosts3D.get(id); }
    public Optional<Bonus3D> bonus3D() { return Optional.ofNullable(bonus3D); }
    public Group maze3D() { return maze3D; }
    public LivesCounter3D livesCounter3D() { return livesCounter3D; }
    public Stream<Pellet3D> pellets3D() { return pellets3D.stream(); }
    public Stream<Energizer3D> energizers3D() { return energizers3D.stream(); }
    public double floorThickness() { return floor3D.getDepth(); }

    public AnimationManager animationManager() { return animationManager; }
    public ManagedAnimation levelCompletedAnimation() { return levelCompletedFullAnimation; }
    public ManagedAnimation levelCompletedAnimationBeforeCutScene() { return levelCompletedShortAnimation; }
    public ManagedAnimation wallColorFlashingAnimation() { return wallColorFlashingAnimation; }

    /**
     * Called on each clock tick (frame).
     */
    public void tick(GameContext gameContext) {
        pac3D.update(gameLevel);
        ghosts3D.forEach(ghost3D -> ghost3D.update(gameLevel));
        bonus3D().ifPresent(bonus3D -> bonus3D.update(gameContext));
        boolean houseAccessRequired = gameLevel.ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .anyMatch(Ghost::isVisible);
        houseLightOnProperty.set(houseAccessRequired);

        gameLevel.house().ifPresent(house -> {
            boolean ghostNearHouseEntry = gameLevel.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
                .filter(ghost -> ghost.position().euclideanDist(house.entryPosition()) <= Settings3D.HOUSE_3D_SENSITIVITY)
                .anyMatch(Ghost::isVisible);
            houseOpenProperty.set(ghostNearHouseEntry);
        });

        int livesCounterSize = gameContext.theGame().lifeCount() - 1;
        // when the game starts and Pac-Man is not yet visible, show one more
        boolean oneMore = gameContext.theGameState() == GameState.STARTING_GAME && !gameLevel.pac().isVisible();
        if (oneMore) livesCounterSize += 1;
        livesCountProperty.set(livesCounterSize);

        boolean visible = gameContext.theGame().canStartNewGame();
        livesCounter3D.setVisible(visible);
        livesCounter3D.light().setLightOn(visible);
    }

    public void onLevelComplete() {
        ui.theSound().stopAll();
        animationManager.stopAllAnimations();
        // hide explicitly because level might have been completed using cheat!
        pellets3D.forEach(pellet3D -> pellet3D.shape3D().setVisible(false));
        energizers3D.forEach(energizer3D -> {
            energizer3D.pumpingAnimation().stop();
            energizer3D.shape3D().setVisible(false);
        });
        house3D.setDoorVisible(false);
        bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
        if (messageView != null) {
            messageView.setVisible(false);
        }
    }

    private void createPelletsAndEnergizers3D(WorldMapColorScheme colorScheme, Mesh pelletMesh) {
        final PhongMaterial pelletMaterial = coloredPhongMaterial(colorScheme.pellet());
        gameLevel.tilesContainingFood().forEach(tile -> {
            if (gameLevel.isEnergizerPosition(tile)) {
                var center = new Point3D(
                    tile.x() * TS + HTS,
                    tile.y() * TS + HTS,
                    -2 * Settings3D.ENERGIZER_3D_RADIUS - 0.5 * Settings3D.FLOOR_3D_THICKNESS  // sitting just on floor
                );
                var energizer3D = new Energizer3D(
                    animationManager,
                    Settings3D.ENERGIZER_3D_RADIUS,
                    Settings3D.ENERGIZER_3D_MIN_SCALING,
                    Settings3D.ENERGIZER_3D_MAX_SCALING);
                energizer3D.setMaterial(pelletMaterial);
                energizer3D.setTile(tile);
                energizer3D.setTranslateX(center.getX());
                energizer3D.setTranslateY(center.getY());
                energizer3D.setTranslateZ(center.getZ());
                energizers3D.add(energizer3D);

                var hideAndExplodeAnimation = new ManagedAnimation(animationManager, "Energizer_Explosion") {
                    private SquirtingAnimation squirtingAnimation;

                    @Override
                    protected Animation createAnimation() {
                        squirtingAnimation = new SquirtingAnimation(GameLevel3D.this, Duration.seconds(2), 23, 69, pelletMaterial, center) {
                            @Override
                            public boolean particleShouldVanish(Particle particle) {
                                return particle.getTranslateZ() >= -1
                                    && isInsideWorldMap(gameLevel.worldMap(), particle.getTranslateX(), particle.getTranslateY());
                            }
                        };
                        return new SequentialTransition(
                            pauseSec(0.5, () -> energizer3D.shape3D().setVisible(false)),
                            squirtingAnimation
                        );
                    }

                    @Override
                    public void destroy() {
                        super.destroy();
                        if (squirtingAnimation != null) {
                            squirtingAnimation.destroy();
                        }
                    }
                };
                energizer3D.setHideAndEatAnimation(hideAndExplodeAnimation);
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

    private void setDrawModeForAllDescendantShapes(Node root, DrawMode drawMode) {
        root.lookupAll("*").stream()
            .filter(Shape3D.class::isInstance)
            .map(Shape3D.class::cast)
            .forEach(shape3D -> shape3D.setDrawMode(drawMode));
    }

    public void showAnimatedMessage(String text, float displaySeconds, double centerX, double y) {
        if (messageView != null) {
            getChildren().remove(messageView);
        }
        messageView = MessageView.builder()
                .text(text)
                .font(ui.theAssets().arcadeFont(6))
                .borderColor(Color.WHITE)
                .displaySeconds(displaySeconds)
                .textColor(Color.YELLOW)
                .build(animationManager);

        double halfHeight = 0.5 * messageView.getBoundsInLocal().getHeight();
        messageView.setTranslateX(centerX - 0.5 * messageView.getFitWidth());
        messageView.setTranslateY(y);
        messageView.setTranslateZ(halfHeight); // just under floor
        getChildren().add(messageView);
        messageView.movementAnimation().playFromStart();
    }

    public void updateBonus3D(Bonus bonus) {
        requireNonNull(bonus);
        if (bonus3D != null) {
            mazeGroup.getChildren().remove(bonus3D);
            bonus3D.destroy();
        }
        bonus3D = new Bonus3D(animationManager, bonus,
            ui.theConfiguration().bonusSymbolImage(bonus.symbol()), Settings3D.BONUS_3D_SYMBOL_WIDTH,
            ui.theConfiguration().bonusValueImage(bonus.symbol()), Settings3D.BONUS_3D_POINTS_WIDTH);
        mazeGroup.getChildren().add(bonus3D);
        bonus3D.showEdible();
    }

    private void handleDrawModeChange(ObservableValue<? extends DrawMode> py, DrawMode ov, DrawMode drawMode) {
        if (isDestroyed()) return; //TODO how can that be?
        setDrawModeForAllDescendantShapes(mazeGroup, drawMode);
        setDrawModeForAllDescendantShapes(levelCounter3D, drawMode);
        setDrawModeForAllDescendantShapes(livesCounter3D, drawMode);
        setDrawModeForAllDescendantShapes(pac3D, drawMode);
        ghosts3D.forEach(ghost3D -> setDrawModeForAllDescendantShapes(ghost3D, drawMode));
        Logger.info("Draw mode set to {}", drawMode);
    }

    private void handleWallHeightChange(ObservableValue<? extends Number> py, Number ov, Number newHeight) {
        if (isDestroyed()) return; //TODO how can that be?
        obstacleBaseHeightProperty.set(newHeight.doubleValue());
    }

    // still work in progress...

    private boolean destroyed;

    public boolean isDestroyed() {
        return destroyed;
    }

    /**
     * Attempt to help objects getting garbage-collected.
     */
    public void destroy() {
        if (destroyed) {
            Logger.warn("Game level has already been destroyed");
            return;
        }
        destroyed = true;
        Logger.info("Destroying game level 3D, clearing resources...");

        //TODO avoid access to global UI here?
        ui.property3DDrawMode().removeListener(this::handleDrawModeChange);
        Logger.info("Removed 'draw mode' listener");

        ui.property3DWallHeight().removeListener(this::handleWallHeightChange);
        Logger.info("Removed 'wall height' listener");

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
        Logger.info("Unbound and cleared material references");

        colorScheme = null;

        livesCountProperty.unbind();
        houseOpenProperty.unbind();
        obstacleBaseHeightProperty.unbind();
        houseBaseHeightProperty.unbind();
        houseLightOnProperty.unbind();

        animationManager.destroyAllAnimations();
        wallColorFlashingAnimation = null;
        levelCompletedFullAnimation = null;
        levelCompletedShortAnimation = null;
        Logger.info("Destroyed and removed all managed animations");

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
        r3D = null;
        Logger.info("Removed 3D renderer");
        getChildren().clear();
        Logger.info("Removed all nodes under game level");

        if (ambientLight != null) {
            ambientLight.colorProperty().unbind();
            ambientLight = null;
            Logger.info("Unbound and cleared ambient light");
        }
        if (pellets3D != null) {
            pellets3D.forEach(Pellet3D::destroy);
            pellets3D = null;
            Logger.info("Destroyed 3D pellets");
        }
        if (energizers3D != null) {
            energizers3D.forEach(Energizer3D::destroy);
            energizers3D = null;
            Logger.info("Destroyed 3D energizers");
        }
        if (mazeGroup != null) {
            mazeGroup.getChildren().clear();
            mazeGroup = null;
            Logger.info("Removed all nodes under maze group");
        }
        if (floor3D != null) {
            floor3D.destroy();
            floor3D = null;
            Logger.info("Unbound and cleared 3D floor");
        }
        if (house3D != null) {
            house3D.light().lightOnProperty().unbind();
            house3D.destroy();
            house3D = null;
            Logger.info("Destroyed and cleared 3D house");
        }
        if (maze3D != null) {
            maze3D.getChildren().forEach(child -> {
                if (child instanceof Wall3D wall3D) {
                    wall3D.destroy();
                }
            });
            maze3D.getChildren().clear();
            maze3D = null;
            Logger.info("3D maze destroyed");
        }
        if (livesCounterShapes != null) {
            for (var shape : livesCounterShapes) {
                if (shape instanceof Destroyable destroyable) {
                    destroyable.destroy();
                }
            }
            livesCounterShapes = null;
        }
        if (livesCounter3D != null) {
            livesCounter3D.destroy();
            livesCounter3D = null;
            Logger.info("Removed lives counter 3D");
        }
        if (levelCounter3D != null) {
            levelCounter3D = null;
            Logger.info("Removed level counter 3D");
        }
        if (pac3D != null) {
            pac3D.destroy();
            pac3D = null;
            Logger.info("Removed Pac 3D");
        }
        if (ghosts3D != null) {
            ghosts3D.forEach(MutatingGhost3D::destroy);
            ghosts3D = null;
            Logger.info("Destroyed and cleared 3D ghosts");
        }
        if (bonus3D != null) {
            bonus3D.destroy();
            bonus3D = null;
            Logger.info("Destroyed and cleared 3D bonus");
        }
        if (messageView != null) {
            messageView.destroy();
            messageView = null;
            Logger.info("Destroyed and cleared message view");
        }
    }
}