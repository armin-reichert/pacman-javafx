/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.Model3D;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
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

    private final BooleanProperty houseOpenPy = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            if (houseOpenPy.get()) {
                maze3D.door3D().openCloseAnimation().play(ManagedAnimation.FROM_START);
            }
        }
    };

    private final IntegerProperty livesCountPy = new SimpleIntegerProperty(0);

    private final AnimationManager animationManager;
    private final ManagedAnimation levelCompletedAnimation;

    private final Group root = new Group();
    private final Group mazeGroup = new Group();
    private final List<Pellet3D> pellets3D = new ArrayList<>();
    private final ArrayList<Energizer3D> energizers3D = new ArrayList<>();
    private Box floor3D;
    private Maze3D maze3D;
    private LevelCounter3D levelCounter3D;
    private LivesCounter3D livesCounter3D;
    private PacBase3D pac3D;
    private List<MutatingGhost3D> ghosts3D;
    private MessageView messageView;
    private Bonus3D bonus3D;

    public GameLevel3D(GameLevel gameLevel, AnimationManager animationManager) {
        requireNonNull(gameLevel);
        this.animationManager = requireNonNull(animationManager);

        var ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PY_3D_LIGHT_COLOR);

        createMaze3D(gameLevel);
        createLevelCounter(gameLevel);
        createLivesCounter3D();
        createPac3D(gameLevel);
        createGhosts3D(gameLevel);

        root.setMouseTransparent(true); // this increases performance, they say...

        root.getChildren().add(ambientLight);
        root.getChildren().add(livesCounter3D);
        root.getChildren().add(levelCounter3D);
        energizers3D.forEach(energizer3D -> root.getChildren().add(energizer3D.shape3D()));
        pellets3D.forEach(pellet3D -> root.getChildren().add(pellet3D.shape3D()));
        root.getChildren().addAll(pac3D.root(), pac3D.light());
        root.getChildren().addAll(ghosts3D);
        // Note: The order in which children are added matters! Walls and house must be added *after* the actors,
        // otherwise the transparency is not working correctly.
        root.getChildren().add(mazeGroup);

        // For wireframe view, bind all 3D maze building blocks to global "draw mode" property.
        mazeGroup.lookupAll("*").stream()
            .filter(Shape3D.class::isInstance)
            .map(Shape3D.class::cast)
            .forEach(shape3D -> shape3D.drawModeProperty().bind(PY_3D_DRAW_MODE));

        levelCompletedAnimation = new ManagedAnimation(animationManager, "Level_Complete") {
            @Override
            protected Animation createAnimation() {
                int levelNumber = gameLevel.number();
                int numMazeFlashes = gameLevel.data().numFlashes();
                if (theGame().cutSceneNumber(levelNumber).isPresent()) {
                    // when a cut scene follows, only play the maze flashing animation
                    return new SequentialTransition(
                        doAfterSec(1.0, () -> gameLevel.ghosts().forEach(Ghost::hide)),
                        maze3D.createMazeFlashAnimation(numMazeFlashes),
                        doAfterSec(2.0, () -> gameLevel.pac().hide())
                    );
                }
                else {
                    boolean showFlashMessage = randomInt(1, 1000) < 250; // every 4th time also show a message
                    return new SequentialTransition(
                        now(() -> {
                            livesCounter3D().light().setLightOn(false);
                            if (showFlashMessage) {
                                theUI().showFlashMessageSec(3, theAssets().localizedLevelCompleteMessage(levelNumber));
                            }
                        }),
                        doAfterSec(1.0, () -> gameLevel.ghosts().forEach(Ghost::hide)),
                        doAfterSec(1.0, maze3D.createMazeFlashAnimation(numMazeFlashes)),
                        doAfterSec(2.0, () -> gameLevel.pac().hide()),
                        doAfterSec(0.5, () -> theSound().playLevelCompleteSound()),
                        doAfterSec(1.0, createLevelRotateAnimation()),
                        doAfterSec(1.0, maze3D.wallsDisappearingAnimation().getOrCreateAnimation()),
                        doAfterSec(1.0, () -> theSound().playLevelChangedSound())
                    );
                }
            }
        };
    }

    public void update(GameLevel gameLevel) {
        pac3D.update(gameLevel);
        ghosts3D().forEach(ghost3D -> ghost3D.update(gameLevel));
        bonus3D().ifPresent(Bonus3D::update);
        boolean houseAccessRequired = gameLevel
            .ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .anyMatch(Ghost::isVisible);
        maze3D.setHouseLightOn(houseAccessRequired);

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

    public void onLevelCompleted() {
        // hide explicitly because level might have been completed using cheat!
        pellets3D().forEach(pellet3D -> pellet3D.shape3D().setVisible(false));
        energizers3D().forEach(energizer3D -> {
            energizer3D.pumpingAnimation().stop();
            energizer3D.shape3D().setVisible(false);
        });
        maze3D().door3D().setVisible(false);
        maze3D.wallColorFlashingAnimation().stop();
        bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
        levelCounter3D().spinningAnimation().stop();
        livesCounter3D().lookingAroundAnimation().stop();
    }

    public Maze3D maze3D() { return maze3D; }

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

    public ManagedAnimation levelCompletedAnimation() {
        return levelCompletedAnimation;
    }

    private void createPac3D(GameLevel gameLevel) {
        pac3D = theUI().configuration().createPac3D(animationManager, gameLevel.pac());
        Model3D.bindDrawMode(pac3D.root(), PY_3D_DRAW_MODE);
    }

    private void createGhosts3D(GameLevel gameLevel) {
        ghosts3D = gameLevel.ghosts()
            .map(ghost -> createGhost3D(ghost, gameLevel.data().numFlashes()))
            .toList();
    }

    private MutatingGhost3D createGhost3D(Ghost ghost, int numFlashes) {
        var ghost3D = new MutatingGhost3D(
            animationManager,
            theAssets(),
            theUI().configuration().assetNamespace(),
            new MeshView(Model3DRepository.get().ghostDressMesh()),
            new MeshView(Model3DRepository.get().ghostPupilsMesh()),
            new MeshView(Model3DRepository.get().ghostEyeballsMesh()),
            ghost,
            Settings3D.GHOST_3D_SIZE,
            numFlashes);
        Model3D.bindDrawMode(ghost3D, PY_3D_DRAW_MODE);
        return ghost3D;
    }

    private void createMaze3D(GameLevel gameLevel) {
        final WorldMap worldMap = gameLevel.worldMap();
        final WorldMapColorScheme colorScheme = theUI().configuration().worldMapColorScheme(worldMap);
        floor3D = createFloor3D(worldMap.numCols() * TS, worldMap.numRows() * TS);
        maze3D = new Maze3D(gameLevel, colorScheme, animationManager);
        mazeGroup.getChildren().addAll(floor3D, maze3D);
        createPelletsAndEnergizers3D(gameLevel, colorScheme);
    }

    private Box createFloor3D(double sizeX, double sizeY) {
        var floor3D = new Box(sizeX + 2 * Settings3D.FLOOR_3D_PADDING, sizeY, Settings3D.FLOOR_3D_THICKNESS);
        floor3D.translateXProperty().bind(floor3D.widthProperty().divide(2).subtract(Settings3D.FLOOR_3D_PADDING));
        floor3D.translateYProperty().bind(floor3D.heightProperty().divide(2));
        floor3D.translateZProperty().bind(floor3D.depthProperty().divide(2));
        floor3D.materialProperty().bind(PY_3D_FLOOR_COLOR.map(Ufx::coloredPhongMaterial));
        floor3D.drawModeProperty().bind(PY_3D_DRAW_MODE);
        return floor3D;
    }

    private void createPelletsAndEnergizers3D(GameLevel gameLevel, WorldMapColorScheme colorScheme) {
        final Mesh pelletMesh = Model3DRepository.get().pelletMesh();
        final PhongMaterial pelletMaterial = coloredPhongMaterial(colorScheme.pellet());
        gameLevel.tilesContainingFood().forEach(tile -> {
            if (gameLevel.isEnergizerPosition(tile)) {
                var center = new Point3D(
                    tile.x() * TS + HTS,
                    tile.y() * TS + HTS,
                    -2 * Settings3D.ENERGIZER_3D_RADIUS - 0.5 * Settings3D.FLOOR_3D_THICKNESS  // sitting just on floor
                );
                var energizer3D = new Energizer3D(Settings3D.ENERGIZER_3D_RADIUS, animationManager);
                energizer3D.setMaterial(pelletMaterial);
                energizer3D.setTile(tile);
                energizer3D.setTranslateX(center.getX());
                energizer3D.setTranslateY(center.getY());
                energizer3D.setTranslateZ(center.getZ());

                var eatenAnimation = new SquirtingAnimation(Duration.seconds(2));
                eatenAnimation.createDrops(23, 69, pelletMaterial, center);
                eatenAnimation.setDropFinalPosition(drop -> drop.getTranslateZ() >= -1
                    && isInsideWorldMap(gameLevel.worldMap(), drop.getTranslateX(), drop.getTranslateY()));
                eatenAnimation.setOnFinished(e -> root.getChildren().remove(eatenAnimation.root()));
                root.getChildren().add(eatenAnimation.root());
                energizer3D.setEatenAnimation(eatenAnimation);

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

    private void createLivesCounter3D() {
        Node[] counterShapes = new Node[Settings3D.LIVES_COUNTER_3D_CAPACITY];
        for (int i = 0; i < counterShapes.length; ++i) {
            counterShapes[i] = theUI().configuration().createLivesCounter3D();
        }
        livesCounter3D = new LivesCounter3D(animationManager, counterShapes);
        livesCounter3D.setTranslateX(2 * TS);
        livesCounter3D.setTranslateY(2 * TS);
        livesCounter3D.drawModeProperty().bind(PY_3D_DRAW_MODE);
        livesCounter3D.livesCountProperty().bind(livesCountPy);
        livesCounter3D.pillarColorProperty().set(Settings3D.LIVES_COUNTER_PILLAR_COLOR);
        livesCounter3D.plateColorProperty().set(Settings3D.LIVES_COUNTER_PLATE_COLOR);
        livesCounter3D.light().colorProperty().set(Color.CORNFLOWERBLUE);
        livesCounter3D.lookingAroundAnimation().play(ManagedAnimation.FROM_START);
    }

    public void createLevelCounter(GameLevel gameLevel) {
        levelCounter3D = new LevelCounter3D(animationManager, theGame().hud().levelCounter());
        // Place level counter at top right maze corner
        levelCounter3D.setTranslateX(gameLevel.worldMap().numCols() * TS - 2 * TS);
        levelCounter3D.setTranslateY(2 * TS);
        levelCounter3D.spinningAnimation().play(ManagedAnimation.FROM_START);
    }

    public Group root() { return root; }

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
        messageView.playMovementAnimation();
    }

    public void updateBonus3D(Bonus bonus) {
        requireNonNull(bonus);
        if (bonus3D != null) {
            mazeGroup.getChildren().remove(bonus3D);
        }
        Image bonusSymbolTexture = theUI().configuration().createBonusSymbolImage(bonus.symbol());
        Image bonusValueTexture  = theUI().configuration().createBonusValueImage(bonus.symbol());
        bonus3D = new Bonus3D(animationManager, bonus, bonusSymbolTexture, bonusValueTexture);
        bonus3D.showEdible();
        mazeGroup.getChildren().add(bonus3D);
    }

    private Animation createLevelRotateAnimation() {
        var rotation = new RotateTransition(Duration.seconds(1.5), root);
        rotation.setAxis(theRNG().nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS);
        rotation.setFromAngle(0);
        rotation.setToAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        return rotation;
    }
}