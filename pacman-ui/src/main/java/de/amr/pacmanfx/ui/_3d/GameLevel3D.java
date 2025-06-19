/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.LevelCounter;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.Model3D;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import javafx.animation.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
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

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static de.amr.pacmanfx.ui.PacManGames.*;
import static de.amr.pacmanfx.ui.PacManGames_UI.*;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static de.amr.pacmanfx.uilib.Ufx.doAfterSec;
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
                maze3D.door3D().playOpenCloseAnimation();
            }
        }
    };

    private final GameLevel gameLevel;
    private final IntegerProperty livesCountPy = new SimpleIntegerProperty(0);

    private final Group root = new Group();
    private final Group mazeGroup = new Group();
    private final List<Pellet3D> pellets3D = new ArrayList<>();
    private final ArrayList<Energizer3D> energizers3D = new ArrayList<>();
    private AmbientLight ambientLight;
    private Box floor3D;
    private Maze3D maze3D;
    private LivesCounter3D livesCounter3D;
    private PacBase3D pac3D;
    private List<Ghost3D_Appearance> ghosts3D;
    private MessageView messageView;
    private Bonus3D bonus3D;

    private Animation levelCompleteAnimation;
    private Animation livesCounterAnimation;

    public GameLevel3D(GameLevel gameLevel) {
        this.gameLevel = requireNonNull(gameLevel);
        createAmbientLight();
        createPac3D();
        createGhosts3D();
        createMaze3D();
        createLivesCounter3D();
        compose();
        bindShape3DDrawingMode();
        root.setMouseTransparent(true); // this increases performance, they say...
    }

    // Note: The order in which children are added matters!
    // Walls and house must be added after actors, otherwise, transparency is not working correctly.
    private void compose() {
        root.getChildren().add(ambientLight);
        energizers3D.forEach(energizer3D -> root.getChildren().add(energizer3D.shape3D()));
        pellets3D.forEach(pellet3D -> root.getChildren().add(pellet3D.shape3D()));
        root.getChildren().addAll(pac3D.root(), pac3D.light());
        root.getChildren().addAll(ghosts3D);
        root.getChildren().add(livesCounter3D);
        root.getChildren().add(mazeGroup);
    }

    // Pellet shapes are not bound because this would cause huge performance penalty!
    private void bindShape3DDrawingMode() {
        Stream.concat(mazeGroup.lookupAll("*").stream(), livesCounter3D.lookupAll("*").stream())
            .filter(Shape3D.class::isInstance)
            .map(Shape3D.class::cast)
            .forEach(shape3D -> shape3D.drawModeProperty().bind(PY_3D_DRAW_MODE));
    }

    public void update() {
        pac3D.update(gameLevel);
        ghosts3D().forEach(ghost3D -> ghost3D.update(gameLevel));
        bonus3D().ifPresent(Bonus3D::update);
        boolean houseAccessRequired = gameLevel.ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
                .anyMatch(Ghost::isVisible);
        maze3D.setHouseLightOn(houseAccessRequired);

        boolean ghostNearHouseEntry = gameLevel.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
                .filter(ghost -> ghost.position().euclideanDist(gameLevel.houseEntryPosition()) <= HOUSE_3D_SENSITIVITY)
                .anyMatch(Ghost::isVisible);
        houseOpenPy.set(ghostNearHouseEntry);

        int livesCounterSize = theGame().lifeCount() - 1;
        // when the game starts and Pac-Man is not yet visible, show one more
        boolean oneMore = theGameState() == GameState.STARTING_GAME && !gameLevel.pac().isVisible();
        if (oneMore) livesCounterSize += 1;
        livesCountPy.set(livesCounterSize);

        boolean visible = theGame().canStartNewGame();
        livesCounter3D.setVisible(visible);
        livesCounter3D.light().setLightOn(visible);
    }

    private void createAmbientLight() {
        ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PY_3D_LIGHT_COLOR);
    }

    private void createPac3D() {
        pac3D = theUI().configuration().createPac3D(gameLevel.pac());
        Model3D.bindDrawMode(pac3D.root(), PY_3D_DRAW_MODE);
    }

    private void createGhosts3D() {
        ghosts3D = gameLevel.ghosts()
            .map(ghost -> createGhost3D(ghost, gameLevel.data().numFlashes()))
            .toList();
    }

    private Ghost3D_Appearance createGhost3D(Ghost ghost, int numFlashes) {
        var ghost3D = new Ghost3D_Appearance(theAssets(), theUI().configuration().assetNamespace(),
            new MeshView(Model3DRepository.get().ghostDressMesh()),
            new MeshView(Model3DRepository.get().ghostPupilsMesh()),
            new MeshView(Model3DRepository.get().ghostEyeballsMesh()),
            ghost, GHOST_3D_SIZE, numFlashes);
        Model3D.bindDrawMode(ghost3D, PY_3D_DRAW_MODE);
        return ghost3D;
    }

    private void createMaze3D() {
        final WorldMap worldMap = gameLevel.worldMap();
        final WorldMapColorScheme colorScheme = theUI().configuration().worldMapColorScheme(worldMap);
        floor3D = createFloor3D(worldMap.numCols() * TS, worldMap.numRows() * TS);
        maze3D = new Maze3D(gameLevel, colorScheme);
        mazeGroup.getChildren().addAll(floor3D, maze3D);
        createFood3D(colorScheme);
    }

    private Box createFloor3D(double sizeX, double sizeY) {
        double paddingX = 5;
        var floor3D = new Box(sizeX + 2*paddingX, sizeY, FLOOR_3D_THICKNESS);
        floor3D.translateXProperty().bind(floor3D.widthProperty().divide(2).subtract(paddingX));
        floor3D.translateYProperty().bind(floor3D.heightProperty().divide(2));
        floor3D.translateZProperty().bind(floor3D.depthProperty().divide(2));
        floor3D.materialProperty().bind(PY_3D_FLOOR_COLOR.map(Ufx::coloredPhongMaterial));
        floor3D.drawModeProperty().bind(PY_3D_DRAW_MODE);
        return floor3D;
    }

    private void createFood3D(WorldMapColorScheme colorScheme) {
        final Mesh pelletMesh = Model3DRepository.get().pelletMesh();
        final PhongMaterial material = coloredPhongMaterial(colorScheme.pellet());
        gameLevel.tilesContainingFood().forEach(tile -> {
            if (gameLevel.isEnergizerPosition(tile)) {
                Energizer3D energizer3D = createEnergizer3D(tile, material);
                SquirtingAnimation squirting = createSquirtingAnimation(energizer3D, material);
                root.getChildren().add(squirting.root());
                energizers3D.add(energizer3D);
            } else {
                var meshView = new MeshView(pelletMesh);
                meshView.setRotationAxis(Rotate.Z_AXIS);
                meshView.setRotate(90);
                pellets3D.add(createPellet3D(tile, meshView, material));
            }
        });
        energizers3D.trimToSize();
    }

    private Energizer3D createEnergizer3D(Vector2i tile, PhongMaterial material) {
        var center = new Point3D(tile.x() * TS + HTS, tile.y() * TS + HTS, -2*ENERGIZER_3D_RADIUS);
        var energizer3D = new Energizer3D(ENERGIZER_3D_RADIUS);
        energizer3D.setTile(tile);
        energizer3D.shape3D().setTranslateX(center.getX());
        energizer3D.shape3D().setTranslateY(center.getY());
        energizer3D.shape3D().setTranslateZ(center.getZ());
        energizer3D.shape3D().setMaterial(material);
        return energizer3D;
    }

    private SquirtingAnimation createSquirtingAnimation(Energizer3D energizer3D, PhongMaterial dropMaterial) {
        var center = new Point3D(energizer3D.tile().x() * TS + HTS, energizer3D.tile().y() * TS + HTS, -2*ENERGIZER_3D_RADIUS);
        var animation = new SquirtingAnimation(Duration.seconds(2));
        animation.createDrops(23, 69, dropMaterial, center);
        animation.setDropFinalPosition(drop -> drop.getTranslateZ() >= -1
                && isInsideWorldMap(gameLevel.worldMap(), drop.getTranslateX(), drop.getTranslateY()));
        animation.setOnFinished(e -> root.getChildren().remove(animation.root()));
        energizer3D.setEatenAnimation(animation);
        return animation;
    }

    private Pellet3D createPellet3D(Vector2i tile, Shape3D shape3D, PhongMaterial foodMaterial) {
        var center = new Point3D(tile.x() * TS + HTS, tile.y() * TS + HTS, -6);
        var pellet3D = new Pellet3D(shape3D, PELLET_3D_RADIUS);
        pellet3D.setTile(tile);
        pellet3D.shape3D().setTranslateX(center.getX());
        pellet3D.shape3D().setTranslateY(center.getY());
        pellet3D.shape3D().setTranslateZ(center.getZ());
        pellet3D.shape3D().setMaterial(foodMaterial);
        return pellet3D;
    }

    private void createLivesCounter3D() {
        Node[] counterShapes = new Node[5];
        for (int i = 0; i < counterShapes.length; ++i) {
            counterShapes[i] = theUI().configuration().createLivesCounter3D();
        }
        livesCounter3D = new LivesCounter3D(counterShapes);
        livesCounter3D.setTranslateX(2 * TS);
        livesCounter3D.setTranslateY(2 * TS);
        livesCounter3D.drawModePy.bind(PY_3D_DRAW_MODE);
        livesCounter3D.livesCountPy.bind(livesCountPy);
        livesCounter3D.light().colorProperty().set(Color.CORNFLOWERBLUE);
    }

    public void addLevelCounter() {
        // Place level counter at top right maze corner
        double x = gameLevel.worldMap().numCols() * TS - 2 * TS;
        double y = 2 * TS;
        root.getChildren().add(createLevelCounter3D(theGame().levelCounter(), x, y));
    }

    private Node createLevelCounter3D(LevelCounter levelCounter, double x, double y) {
        var levelCounter3D = new Group();
        levelCounter3D.setTranslateX(x);
        levelCounter3D.setTranslateY(y);
        levelCounter3D.setTranslateZ(-6);
        int n = 0;
        SpriteSheet<?> spriteSheet = theUI().configuration().spriteSheet();
        for (byte symbol : levelCounter.symbols()) {
            var material = new PhongMaterial(Color.WHITE);
            Sprite sprite = theUI().configuration().createBonusSymbolSprite(symbol);
            material.setDiffuseMap(spriteSheet.image(sprite));

            var cube = new Box(TS, TS, TS);
            cube.setMaterial(material);
            cube.setTranslateX(-n * (double) 16);
            cube.setTranslateZ(-HTS);

            var spinning = new RotateTransition(Duration.seconds(6), cube);
            spinning.setCycleCount(Animation.INDEFINITE);
            spinning.setInterpolator(Interpolator.LINEAR);
            spinning.setAxis(Rotate.X_AXIS);
            spinning.setByAngle(360);
            spinning.setRate(n % 2 == 0 ? 1 : -1);
            spinning.play();

            levelCounter3D.getChildren().add(cube);
            n += 1;
        }
        return levelCounter3D;
    }

    public Group root() { return root; }

    public void showAnimatedMessage(String text, double displaySeconds, double centerX, double y) {
        if (messageView != null) {
            root.getChildren().remove(messageView);
        }
        messageView = MessageView.builder()
            .text(text)
            .font(theAssets().arcadeFont(6))
            .borderColor(Color.WHITE)
            .textColor(Color.YELLOW)
            .build();
        root.getChildren().add(messageView);

        double halfHeight = 0.5 * messageView.getBoundsInLocal().getHeight();
        messageView.setTranslateX(centerX - 0.5 * messageView.getFitWidth());
        messageView.setTranslateY(y);
        messageView.setTranslateZ(halfHeight); // just under floor

        var moveUpAnimation = new TranslateTransition(Duration.seconds(1), messageView);
        moveUpAnimation.setToZ(-(halfHeight + 0.5 * OBSTACLE_3D_BASE_HEIGHT));
        moveUpAnimation.setOnFinished(e -> Logger.info("Moving message3D up finished: {}", messageView));

        var moveDownAnimation = new TranslateTransition(Duration.seconds(1), messageView);
        moveDownAnimation.setToZ(halfHeight);
        moveDownAnimation.setOnFinished(e -> {
            messageView.setVisible(false);
            Logger.info("Moving message3D down finished: {}", messageView);
        });

        Logger.info("Message3D before move: {}", messageView);
        new SequentialTransition(
            moveUpAnimation,
            new PauseTransition(Duration.seconds(displaySeconds)),
            moveDownAnimation
        ).play();
    }

    public void updateBonus3D(Bonus bonus, SpriteSheet<?> spriteSheet) {
        requireNonNull(bonus);
        if (bonus3D != null) {
            mazeGroup.getChildren().remove(bonus3D);
        }
        Sprite bonusSymbolSprite = theUI().configuration().createBonusSymbolSprite(bonus.symbol());
        Sprite bonusValueSprite = theUI().configuration().createBonusValueSprite(bonus.symbol());
        bonus3D = new Bonus3D(bonus, spriteSheet.image(bonusSymbolSprite), spriteSheet.image(bonusValueSprite));
        bonus3D.showEdible();
        mazeGroup.getChildren().add(bonus3D);
    }

    public void playLivesCounterAnimation() {
        livesCounterAnimation = livesCounter3D.createAnimation();
        livesCounter3D.resetShapes();
        livesCounterAnimation.play();
    }

    private void playLevelRotateAnimation() {
        var rotation = new RotateTransition(Duration.seconds(1.5), root);
        rotation.setAxis(theRNG().nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS);
        rotation.setFromAngle(0);
        rotation.setToAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        rotation.play();
    }

    public Animation createLevelCompleteAnimation() {
        levelCompleteAnimation = theGame().cutSceneNumber(gameLevel.number()).isPresent()
            ? levelTransformationBeforeIntermission(gameLevel.data().numFlashes())
            : levelTransformation(gameLevel.data().numFlashes());
        return levelCompleteAnimation;
    }

    private Animation levelTransformationBeforeIntermission(int numFlashes) {
        return new SequentialTransition(
                doAfterSec(1.0, () -> gameLevel.ghosts().forEach(Ghost::hide)),
                maze3D.mazeFlashAnimation(numFlashes),
                doAfterSec(2.5, () -> gameLevel.pac().hide())
        );
    }

    private Animation levelTransformation(int numFlashes) {
        return new Timeline(
            new KeyFrame(Duration.ZERO, e -> {
                livesCounter3D().light().setLightOn(false);
                if (randomInt(1, 100) < 25) {
                    theUI().showFlashMessageSec(3, theAssets().localizedLevelCompleteMessage(gameLevel.number()));
                }
            }),
            new KeyFrame(Duration.seconds(1.0), e -> gameLevel.ghosts().forEach(Ghost::hide)),
            new KeyFrame(Duration.seconds(1.5), e -> maze3D.mazeFlashAnimation(numFlashes).play()),
            new KeyFrame(Duration.seconds(4.5), e -> gameLevel.pac().hide()),
            new KeyFrame(Duration.seconds(5.0), e -> playLevelRotateAnimation()),
            new KeyFrame(Duration.seconds(7.0), e -> {
                maze3D.wallsDisappearAnimation(2.0).play();
                theSound().playLevelCompleteSound();
            }),
            new KeyFrame(Duration.seconds(9.5), e -> theSound().playLevelChangedSound())
        );
    }

    public void stopAnimations() {
        energizers3D().forEach(Energizer3D::stopPumping);
        bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
        maze3D.stopMaterialAnimation();
        if (livesCounterAnimation != null) {
            livesCounterAnimation.stop();
        }
        if (levelCompleteAnimation != null) {
            levelCompleteAnimation.stop();
        }
    }

    public Maze3D maze3D() { return maze3D; }
    public PacBase3D pac3D() { return pac3D; }
    public Stream<Ghost3D_Appearance> ghosts3D() { return ghosts3D.stream(); }
    public Ghost3D_Appearance ghost3D(byte id) { return ghosts3D.get(id); }
    public Optional<Bonus3D> bonus3D() { return Optional.ofNullable(bonus3D); }
    public LivesCounter3D livesCounter3D() { return livesCounter3D; }
    public Stream<Pellet3D> pellets3D() { return pellets3D.stream(); }
    public Stream<Energizer3D> energizers3D() { return energizers3D.stream(); }
    public Color floorColor() { return PY_3D_FLOOR_COLOR.get(); }
    public double floorThickness() { return floor3D.getDepth(); }
}