/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.LevelCounter;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.*;
import javafx.animation.*;
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
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static de.amr.pacmanfx.uilib.Ufx.doAfterSec;
import static java.util.Objects.requireNonNull;

/**
 * 3D representation of current game theGameLevel().
 */
public class GameLevel3D {

    private static boolean isInsideWorldMap(WorldMap worldMap, double x, double y) {
        return 0 <= x && x <= worldMap.numCols() * TS && 0 <= y && y <= worldMap.numRows() * TS;
    }

    private final BooleanProperty houseOpenPy = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            if (houseOpenPy.get()) {
                maze3D.door3D().playOpenCloseAnimation();
            }
        }
    };

    private final IntegerProperty livesCountPy = new SimpleIntegerProperty(0);

    private final Group root = new Group();
    private final Group mazeGroup = new Group();
    private final Box floor3D;
    private final Maze3D maze3D;
    private final LivesCounter3D livesCounter3D;
    private final List<Pellet3D> pellets3D = new ArrayList<>();
    private final ArrayList<Energizer3D> energizers3D = new ArrayList<>();
    private final XMan3D pac3D;
    private final List<Ghost3DAppearance> ghost3DAppearances;
    private Message3D message3D;
    private Bonus3D bonus3D;
    private Animation levelCompleteAnimation;
    private Animation livesCounterAnimation;

    public GameLevel3D() {
        pac3D = createPac3D();
        ghost3DAppearances = theGameLevel().ghosts()
            .map(ghost -> createGhost3D(ghost, theGameLevel().data().numFlashes()))
            .toList();

        final WorldMapColorScheme colorScheme = theUIConfig().current().worldMapColorScheme(theGameLevel().worldMap());
        floor3D = createFloor3D(theGameLevel().worldMap().numCols() * TS, theGameLevel().worldMap().numRows() * TS);
        maze3D = new Maze3D(theGameLevel(), colorScheme);
        mazeGroup.getChildren().addAll(floor3D, maze3D);
        createFood3D(colorScheme);

        livesCounter3D = createLivesCounter3D(theGame().canStartNewGame());

        var ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PY_3D_LIGHT_COLOR);

        // Note: The order in which children are added matters!
        // Walls and house must be added last, otherwise, transparency is not working correctly.
        energizers3D.forEach(energizer3D -> root.getChildren().add(energizer3D.shape3D()));
        pellets3D.forEach(pellet3D -> root.getChildren().add(pellet3D.shape3D()));
        root.getChildren().addAll(pac3D.root(), pac3D.light());
        root.getChildren().addAll(ghost3DAppearances);
        root.getChildren().add(livesCounter3D);
        root.getChildren().add(mazeGroup);
        root.getChildren().add(ambientLight);

        root.setMouseTransparent(true); //TODO does this really increase performance?
        // Pac-Man and ghost shapes are already bound to global draw mode property.
        // Pellets are not included because this would cause huge performance penalty.
        Stream.concat(mazeGroup.lookupAll("*").stream(), livesCounter3D.lookupAll("*").stream())
            .filter(Shape3D.class::isInstance)
            .map(Shape3D.class::cast)
            .forEach(shape3D -> shape3D.drawModeProperty().bind(PY_3D_DRAW_MODE));
    }

    public void update() {
        pac3D.update(theGameLevel());
        ghosts3D().forEach(ghost3DAppearance -> ghost3DAppearance.update(theGameLevel()));
        bonus3D().ifPresent(Bonus3D::update);
        boolean houseAccessRequired = theGameLevel().ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
                .anyMatch(Ghost::isVisible);
        maze3D.setHouseLightOn(houseAccessRequired);

        boolean ghostNearHouseEntry = theGameLevel().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
                .filter(ghost -> ghost.position().euclideanDist(theGameLevel().houseEntryPosition()) <= HOUSE_3D_SENSITIVITY)
                .anyMatch(Ghost::isVisible);
        houseOpenPy.set(ghostNearHouseEntry);

        livesCountPy.set(livesCounterSize());
    }

    private Box createFloor3D(double sizeX, double sizeY) {
        double extraSpace = 10;
        var floor3D = new Box(sizeX + extraSpace, sizeY, FLOOR_3D_THICKNESS);
        floor3D.translateXProperty().bind(floor3D.widthProperty().divide(2).subtract(0.5 * extraSpace));
        floor3D.translateYProperty().bind(floor3D.heightProperty().divide(2));
        floor3D.translateZProperty().bind(floor3D.depthProperty().divide(2));
        floor3D.materialProperty().bind(PY_3D_FLOOR_COLOR.map(Ufx::coloredPhongMaterial));
        floor3D.drawModeProperty().bind(PY_3D_DRAW_MODE);
        return floor3D;
    }

    private Ghost3DAppearance createGhost3D(Ghost ghost, int numFlashes) {
        var ghost3D = new Ghost3DAppearance(theAssets(), theUIConfig().current().assetNamespace(),
            new MeshView(Model3DRepository.get().ghostDressMesh()),
            new MeshView(Model3DRepository.get().ghostPupilsMesh()),
            new MeshView(Model3DRepository.get().ghostEyeballsMesh()),
            ghost, GHOST_3D_SIZE, numFlashes);
        Model3D.bindDrawMode(ghost3D, PY_3D_DRAW_MODE);
        return ghost3D;
    }

    private void createFood3D(WorldMapColorScheme colorScheme) {
        final Mesh pelletMesh = Model3DRepository.get().pelletMesh();
        PhongMaterial foodMaterial = coloredPhongMaterial(colorScheme.pellet());
        theGameLevel().tilesContainingFood().forEach(tile -> {
            if (theGameLevel().isEnergizerPosition(tile)) {
                Energizer3D energizer3D = createEnergizer3D(tile, foodMaterial);
                SquirtingAnimation squirting = createSquirtingAnimation(theGameLevel().worldMap(), energizer3D, foodMaterial);
                root.getChildren().add(squirting.root());
                energizers3D.add(energizer3D);
            } else {
                var pelletShape3D = new MeshView(pelletMesh);
                pelletShape3D.setRotationAxis(Rotate.Z_AXIS);
                pelletShape3D.setRotate(90);
                Pellet3D pellet3D = createPellet3D(tile, pelletShape3D, foodMaterial);
                pellets3D.add(pellet3D);
            }
        });
        energizers3D.trimToSize();
    }

    private Energizer3D createEnergizer3D(Vector2i tile, PhongMaterial foodMaterial) {
        var center = new Point3D(tile.x() * TS + HTS, tile.y() * TS + HTS, -6);
        var energizer3D = new Energizer3D(ENERGIZER_3D_RADIUS);
        energizer3D.setTile(tile);
        energizer3D.shape3D().setTranslateX(center.getX());
        energizer3D.shape3D().setTranslateY(center.getY());
        energizer3D.shape3D().setTranslateZ(center.getZ());
        energizer3D.shape3D().setMaterial(foodMaterial);
        return energizer3D;
    }

    private SquirtingAnimation createSquirtingAnimation(WorldMap worldMap, Energizer3D energizer3D, PhongMaterial dropMaterial) {
        Vector2i tile = energizer3D.tile();
        var center = new Point3D(tile.x() * TS + HTS, tile.y() * TS + HTS, -6);
        var animation = new SquirtingAnimation(Duration.seconds(2));
        animation.createDrops(23, 69, dropMaterial, center);
        animation.setDropFinalPosition(drop -> drop.getTranslateZ() >= -1
                && isInsideWorldMap(worldMap, drop.getTranslateX(), drop.getTranslateY()));
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

    private XMan3D createPac3D() {
        Pac pac = theGameLevel().pac();
        String ans = theUIConfig().current().assetNamespace();
        XMan3D pac3D = switch (theGameVariant()) {
            case MS_PACMAN, MS_PACMAN_TENGEN, MS_PACMAN_XXL -> new MsPacMan3D(pac, PAC_3D_SIZE, theAssets(), ans);
            case PACMAN, PACMAN_XXL -> new PacMan3D(pac, PAC_3D_SIZE, theAssets(), ans);
        };
        pac3D.light().setColor(theAssets().color(ans + ".pac.color.head").desaturate());
        Model3D.bindDrawMode(pac3D.root(), PY_3D_DRAW_MODE);
        return pac3D;
    }

    private LivesCounter3D createLivesCounter3D(boolean visible) {
        Node[] counterShapes = new Node[LIVES_COUNTER_MAX];
        for (int i = 0; i < counterShapes.length; ++i) {
            counterShapes[i] = theUIConfig().current().createLivesCounterShape(theAssets(), LIVES_COUNTER_3D_SIZE);
        }
        var counter3D = new LivesCounter3D(counterShapes);
        counter3D.drawModePy.bind(PY_3D_DRAW_MODE);
        counter3D.livesCountPy.bind(livesCountPy);
        counter3D.setTranslateX(2 * TS);
        counter3D.setTranslateY(2 * TS);
        counter3D.setVisible(visible);
        counter3D.light().colorProperty().set(Color.CORNFLOWERBLUE);
        counter3D.light().setLightOn(visible);
        return counter3D;
    }

    public void addLevelCounter() {
        // Place level counter at top right maze corner
        double x = theGameLevel().worldMap().numCols() * TS - 2 * TS;
        double y = 2 * TS;
        Node levelCounter3D = createLevelCounter3D(theGame().levelCounter(), x, y);
        root.getChildren().add(levelCounter3D);
    }

    private Node createLevelCounter3D(LevelCounter levelCounter, double x, double y) {
        double spacing = 2 * TS;
        var levelCounter3D = new Group();
        levelCounter3D.setTranslateX(x);
        levelCounter3D.setTranslateY(y);
        levelCounter3D.setTranslateZ(-6);
        levelCounter3D.getChildren().clear();
        int n = 0;
        GameSpriteSheet spriteSheet = theUIConfig().current().spriteSheet();
        for (byte symbol : levelCounter.symbols()) {
            Box cube = new Box(TS, TS, TS);
            cube.setTranslateX(-n * spacing);
            cube.setTranslateZ(-HTS);
            levelCounter3D.getChildren().add(cube);

            var material = new PhongMaterial(Color.WHITE);
            Image texture = spriteSheet.crop(spriteSheet.bonusSymbolSprite(symbol));
            material.setDiffuseMap(texture);
            cube.setMaterial(material);

            var spinning = new RotateTransition(Duration.seconds(6), cube);
            spinning.setAxis(Rotate.X_AXIS);
            spinning.setCycleCount(Animation.INDEFINITE);
            spinning.setByAngle(360);
            spinning.setRate(n % 2 == 0 ? 1 : -1);
            spinning.setInterpolator(Interpolator.LINEAR);
            spinning.play();

            n += 1;
        }
        return levelCounter3D;
    }

    public Group root() { return root; }

    private int livesCounterSize() {
        int n = theGame().lifeCount();
        if (!theGameLevel().pac().isVisible() && theGameState() == GameState.STARTING_GAME) {
            return n;
        }
        return n - 1;
    }

    public void showAnimatedMessage(String text, double displaySeconds, double centerX, double y) {
        if (message3D != null) {
            root.getChildren().remove(message3D);
        }
        message3D = Message3D.builder()
            .text(text)
            .font(theAssets().arcadeFontAtSize(6))
            .borderColor(Color.WHITE)
            .textColor(Color.YELLOW)
            .build();
        root.getChildren().add(message3D);

        double halfHeight = 0.5 * message3D.getBoundsInLocal().getHeight();
        message3D.setTranslateX(centerX - 0.5 * message3D.getFitWidth());
        message3D.setTranslateY(y);
        message3D.setTranslateZ(halfHeight); // just under floor

        var moveUpAnimation = new TranslateTransition(Duration.seconds(1), message3D);
        moveUpAnimation.setToZ(-(halfHeight + 0.5 * OBSTACLE_3D_BASE_HEIGHT));
        moveUpAnimation.setOnFinished(e -> Logger.info("Moving message3D up finished: {}", message3D));

        var moveDownAnimation = new TranslateTransition(Duration.seconds(1), message3D);
        moveDownAnimation.setToZ(halfHeight);
        moveDownAnimation.setOnFinished(e -> {
            message3D.setVisible(false);
            Logger.info("Moving message3D down finished: {}", message3D);
        });

        Logger.info("Message3D before move: {}", message3D);
        new SequentialTransition(
            moveUpAnimation,
            new PauseTransition(Duration.seconds(displaySeconds)),
            moveDownAnimation
        ).play();
    }

    public void updateBonus3D(Bonus bonus, GameSpriteSheet spriteSheet) {
        requireNonNull(bonus);
        if (bonus3D != null) {
            mazeGroup.getChildren().remove(bonus3D);
        }
        bonus3D = new Bonus3D(bonus,
            spriteSheet.crop(spriteSheet.bonusSymbolSprite(bonus.symbol())),
            spriteSheet.crop(spriteSheet.bonusValueSprite(bonus.symbol())));
        bonus3D.showEdible();
        mazeGroup.getChildren().add(bonus3D);
    }

    public void playLivesCounterAnimation() {
        //TODO new animation creation needed?
        livesCounterAnimation = livesCounter3D.createAnimation();
        livesCounter3D.resetShapes();
        livesCounterAnimation.play();
    }

    public RotateTransition levelRotateAnimation(double seconds) {
        var rotation = new RotateTransition(Duration.seconds(seconds), root);
        rotation.setAxis(theRNG().nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS);
        rotation.setFromAngle(0);
        rotation.setToAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        return rotation;
    }

    public Animation createLevelCompleteAnimation() {
        levelCompleteAnimation = theGameLevel().cutSceneNumber() != 0
            ? levelTransformationBeforeIntermission(theGameLevel().data().numFlashes())
            : levelTransformation(theGameLevel().data().numFlashes());
        return levelCompleteAnimation;
    }

    private Animation levelTransformationBeforeIntermission(int numFlashes) {
        return new SequentialTransition(
                doAfterSec(1.0, () -> theGameLevel().ghosts().forEach(Ghost::hide)),
                maze3D.mazeFlashAnimation(numFlashes),
                doAfterSec(2.5, () -> theGameLevel().pac().hide())
        );
    }

    private Animation levelTransformation(int numFlashes) {
        return new Timeline(
            new KeyFrame(Duration.ZERO, e -> {
                livesCounter3D().light().setLightOn(false);
                if (randomInt(1, 100) < 25) {
                    theUI().showFlashMessageSec(3, theAssets().localizedLevelCompleteMessage(theGameLevel().number()));
                }
            }),
            new KeyFrame(Duration.seconds(1.0), e -> theGameLevel().ghosts().forEach(Ghost::hide)),
            new KeyFrame(Duration.seconds(1.5), e -> maze3D.mazeFlashAnimation(numFlashes).play()),
            new KeyFrame(Duration.seconds(4.5), e -> theGameLevel().pac().hide()),
            new KeyFrame(Duration.seconds(5.0), e -> levelRotateAnimation(1.5).play()),
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
    public XMan3D pac3D() { return pac3D; }
    public Stream<Ghost3DAppearance> ghosts3D() { return ghost3DAppearances.stream(); }
    public Ghost3DAppearance ghost3D(byte id) { return ghost3DAppearances.get(id); }
    public Optional<Bonus3D> bonus3D() { return Optional.ofNullable(bonus3D); }
    public LivesCounter3D livesCounter3D() { return livesCounter3D; }
    public Stream<Pellet3D> pellets3D() { return pellets3D.stream(); }
    public Stream<Energizer3D> energizers3D() { return energizers3D.stream(); }
    public Color floorColor() { return PY_3D_FLOOR_COLOR.get(); }
    public double floorThickness() { return floor3D.getDepth(); }
}