/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d;

import de.amr.games.pacman.Globals;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.LevelCounter;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui.GameUIConfig;
import de.amr.games.pacman.ui._2d.GameSpriteSheet;
import de.amr.games.pacman.uilib.Ufx;
import de.amr.games.pacman.uilib.assets.WorldMapColorScheme;
import de.amr.games.pacman.uilib.model3D.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.ui.Globals.*;
import static de.amr.games.pacman.uilib.Ufx.*;
import static java.util.Objects.requireNonNull;

/**
 * 3D representation of game level.
 *
 * @author Armin Reichert
 */
public class GameLevel3D {

    private final BooleanProperty houseOpenPy = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            if (houseOpenPy.get()) {
                maze3D.door3D().playOpenCloseAnimation();
            }
        }
    };

    private final IntegerProperty livesCountPy = new SimpleIntegerProperty(0);

    private final List<Pellet3D> pellets3D = new ArrayList<>();
    private final ArrayList<Energizer3D> energizers3D = new ArrayList<>();

    private final Group root = new Group();
    private final LivesCounter3D livesCounter3D;
    private final Group mazeGroup = new Group();
    private final Box floor3D;
    private final Maze3D maze3D;
    private Message3D message3D;
    private Bonus3D bonus3D;
    private final XMan3D pac3D;
    private final List<Ghost3DAppearance> ghost3DAppearances;

    private Animation levelCompleteAnimation;
    private Animation livesCounterAnimation;

    public GameLevel3D(GameVariant gameVariant, GameLevel level) {
        final GameUIConfig uiConfig = THE_UI_CONFIGS.current();
        final WorldMap worldMap = level.worldMap();
        final int numRows = worldMap.numRows(), numCols = worldMap.numCols();
        final WorldMapColorScheme colorScheme = uiConfig.worldMapColorScheme(worldMap);
        final PhongMaterial foodMaterial = coloredMaterial(colorScheme.pellet()); // TODO move into UI config?

        livesCounter3D = createLivesCounter3D(level.game().canStartNewGame());
        livesCounter3D.livesCountPy.bind(livesCountPy);

        pac3D = createPac3D(gameVariant, uiConfig.assetNamespace(), level.pac());
        ghost3DAppearances = level.ghosts()
            .map(ghost -> createGhostAppearance3D(uiConfig.assetNamespace(), ghost, level.numFlashes()))
            .toList();

        floor3D = createFloor(numCols * TS, numRows * TS);
        maze3D = new Maze3D(uiConfig, level, colorScheme);
        mazeGroup.getChildren().addAll(floor3D, maze3D);

        createFood3D(level, Model3DRepository.instance().pelletMesh(), foodMaterial);

        // Note: The order in which children are added matters!
        // Walls and house must be added last, otherwise, transparency is not working correctly.
        energizers3D.forEach(energizer3D -> root.getChildren().add(energizer3D.shape3D()));
        pellets3D.forEach(pellet3D -> root.getChildren().add(pellet3D.shape3D()));
        root.getChildren().addAll(pac3D.root(), pac3D.light());
        root.getChildren().addAll(ghost3DAppearances);
        root.getChildren().add(livesCounter3D);
        root.getChildren().add(mazeGroup);

        // For wireframe mode view. Pac-Man and ghost shapes are already bound to global draw mode property.
        // Pellets are not included because this would cause huge performance penalty.
        Stream.concat(mazeGroup.lookupAll("*").stream(), livesCounter3D.lookupAll("*").stream())
            .filter(Shape3D.class::isInstance)
            .map(Shape3D.class::cast)
            .forEach(shape3D -> shape3D.drawModeProperty().bind(PY_3D_DRAW_MODE));

        var ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PY_3D_LIGHT_COLOR);
        root.getChildren().add(ambientLight);

        root.setMouseTransparent(true); //TODO does this really increase performance?
    }

    private void createFood3D(GameLevel level, Mesh pelletMesh, PhongMaterial foodMaterial) {
        level.worldMap().tiles().filter(level::hasFoodAt).forEach(tile -> {
            if (level.isEnergizerPosition(tile)) {
                Energizer3D energizer3D = createEnergizer3D(tile, foodMaterial);
                addSquirtingAnimation(level.worldMap(), energizer3D, foodMaterial);
                energizers3D.add(energizer3D);
            } else {
                var pelletMeshView = new MeshView(pelletMesh);
                pelletMeshView.setRotationAxis(Rotate.Z_AXIS);
                pelletMeshView.setRotate(90);
                Pellet3D pellet3D = createPellet3D(tile, pelletMeshView, foodMaterial);
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

    private boolean isInsideWorldMap(WorldMap worldMap, double x, double y) {
        return 0 <= x && x <= worldMap.numCols() * TS && 0 <= y && y <= worldMap.numRows() * TS;
    }

    private void addSquirtingAnimation(WorldMap worldMap, Energizer3D energizer3D, PhongMaterial dropMaterial) {
        Vector2i tile = energizer3D.tile();
        var center = new Point3D(tile.x() * TS + HTS, tile.y() * TS + HTS, -6);
        var squirting = new SquirtingAnimation(Duration.seconds(2));
        squirting.createDrops(23, 69, dropMaterial, center);
        squirting.setDropFinalPosition(drop -> drop.getTranslateZ() >= -1
                && isInsideWorldMap(worldMap, drop.getTranslateX(), drop.getTranslateY()));
        squirting.setOnFinished(e -> root.getChildren().remove(squirting.root()));
        root.getChildren().add(squirting.root());
        energizer3D.setEatenAnimation(squirting);
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

    private XMan3D createPac3D(GameVariant gameVariant, String ans, Pac pac) {
        XMan3D pac3D = switch (gameVariant) {
            case MS_PACMAN, MS_PACMAN_TENGEN, MS_PACMAN_XXL -> new MsPacMan3D(pac, PAC_3D_SIZE, THE_ASSETS, ans);
            case PACMAN, PACMAN_XXL -> new PacMan3D(pac, PAC_3D_SIZE, THE_ASSETS, ans);
        };
        pac3D.light().setColor(THE_ASSETS.color(ans + ".pac.color.head").desaturate());
        Model3D.allMeshViewsUnder(pac3D.root()).map(MeshView::drawModeProperty).forEach(py -> py.bind(PY_3D_DRAW_MODE));
        return pac3D;
    }

    private Ghost3DAppearance createGhostAppearance3D(String ans, Ghost ghost, int numFlashes) {
        Shape3D dressShape    = new MeshView(Model3DRepository.instance().ghostDressMesh());
        Shape3D pupilsShape   = new MeshView(Model3DRepository.instance().ghostPupilsMesh());
        Shape3D eyeballsShape = new MeshView(Model3DRepository.instance().ghostEyeballsMesh());
        var ghost3DAppearance = new Ghost3DAppearance(ans, dressShape, pupilsShape, eyeballsShape, ghost, GHOST_3D_SIZE, numFlashes);
        Model3D.allMeshViewsUnder(ghost3DAppearance).map(MeshView::drawModeProperty).forEach(py -> py.bind(PY_3D_DRAW_MODE));
        return ghost3DAppearance;
    }

    private LivesCounter3D createLivesCounter3D(boolean canStartNewGame) {
        GameUIConfig config3D = THE_UI_CONFIGS.current();
        Node[] counterShapes = new Node[LIVES_COUNTER_MAX];
        for (int i = 0; i < counterShapes.length; ++i) {
            counterShapes[i] = config3D.createLivesCounterShape(THE_ASSETS, LIVES_COUNTER_3D_SIZE);
        }
        var counter3D = new LivesCounter3D(counterShapes);
        counter3D.setTranslateX(2 * TS);
        counter3D.setTranslateY(2 * TS);
        counter3D.setVisible(canStartNewGame);
        counter3D.drawModePy.bind(PY_3D_DRAW_MODE);
        counter3D.light().colorProperty().set(Color.CORNFLOWERBLUE);
        counter3D.light().setLightOn(canStartNewGame);
        return counter3D;
    }

    public void addLevelCounter() {
        THE_GAME_CONTROLLER.game().level().map(GameLevel::worldMap).ifPresent(worldMap -> {
            // Place level counter at top right maze corner
            double x = worldMap.numCols() * TS - 2 * TS;
            double y = 2 * TS;
            Node levelCounter3D = createLevelCounter3D(
                    THE_UI_CONFIGS.current().spriteSheet(),
                    THE_GAME_CONTROLLER.game().levelCounter(), x, y);
            root.getChildren().add(levelCounter3D);
        });
    }

    private Node createLevelCounter3D(GameSpriteSheet spriteSheet, LevelCounter levelCounter, double x, double y) {
        double spacing = 2 * TS;
        var levelCounter3D = new Group();
        levelCounter3D.setTranslateX(x);
        levelCounter3D.setTranslateY(y);
        levelCounter3D.setTranslateZ(-6);
        levelCounter3D.getChildren().clear();
        int n = 0;
        for (byte symbol : levelCounter.symbols().toList()) {
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

    private Box createFloor(double sizeX, double sizeY) {
        // add some extra space
        double extraSpace = 10;
        var floor3D = new Box(sizeX + extraSpace, sizeY, FLOOR_3D_THICKNESS);
        floor3D.materialProperty().bind(PY_3D_FLOOR_COLOR.map(Ufx::coloredMaterial));
        floor3D.translateXProperty().bind(floor3D.widthProperty().multiply(0.5).subtract(0.5*extraSpace));
        floor3D.translateYProperty().bind(floor3D.heightProperty().multiply(0.5));
        floor3D.translateZProperty().set(FLOOR_3D_THICKNESS * 0.5);
        floor3D.drawModeProperty().bind(PY_3D_DRAW_MODE);
        return floor3D;
    }

    public Group root() { return root; }

    public void update(GameLevel level) {
        pac3D.update(level);
        ghosts3D().forEach(Ghost3DAppearance::update);
        bonus3D().ifPresent(Bonus3D::update);
        boolean houseAccessRequired = level.ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .anyMatch(Ghost::isVisible);
        maze3D().setHouseLightOn(houseAccessRequired);

        boolean ghostNearHouseEntry = level.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .filter(ghost -> ghost.position().euclideanDist(level.houseEntryPosition()) <= HOUSE_3D_SENSITIVITY)
            .anyMatch(Ghost::isVisible);
        houseOpenPy.set(ghostNearHouseEntry);

        int symbolsDisplayed = Math.max(0, THE_GAME_CONTROLLER.game().livesProperty().get() - 1);
        if (!level.pac().isVisible() && THE_GAME_CONTROLLER.state() == GameState.STARTING_GAME) {
            livesCountPy.set(symbolsDisplayed + 1);
        } else {
            livesCountPy.set(symbolsDisplayed);
        }
    }

    public void showAnimatedMessage(String text, double displaySeconds, double centerX, double y) {
        if (message3D != null) {
            root.getChildren().remove(message3D);
        }
        message3D = Message3D.builder()
            .text(text)
            .font(THE_ASSETS.font("font.arcade", 6))
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

        var moveDownAnimation = new TranslateTransition(Duration.seconds(1), message3D);
        moveDownAnimation.setDelay(Duration.seconds(displaySeconds));
        moveDownAnimation.setToZ(halfHeight);
        moveDownAnimation.setOnFinished(e -> message3D.setVisible(false));

        new SequentialTransition(moveUpAnimation, moveDownAnimation).play();
    }

    public void replaceBonus3D(Bonus bonus, GameSpriteSheet spriteSheet) {
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
        rotation.setAxis(THE_RNG.nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS);
        rotation.setFromAngle(0);
        rotation.setToAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        return rotation;
    }

    public void playLevelCompleteAnimation(GameLevel level, double delaySeconds, Runnable onStart, Runnable onFinished) {
        levelCompleteAnimation = new SequentialTransition(
            now(() -> {
                // keep game state until animation has finished
                THE_GAME_CONTROLLER.state().timer().resetIndefiniteTime();
                onStart.run();
            }),
            level.cutSceneNumber() != 0
                ? levelTransformationBeforeIntermission(level, level.numFlashes())
                : levelTransformation(level, level.numFlashes())
        );
        levelCompleteAnimation.setOnFinished(e -> {
            onFinished.run();
            THE_GAME_CONTROLLER.terminateCurrentState();
        });
        levelCompleteAnimation.setDelay(Duration.seconds(delaySeconds));
        levelCompleteAnimation.play();
    }

    private Animation levelTransformationBeforeIntermission(GameLevel level, int numFlashes) {
        return new SequentialTransition(
                doAfterSec(1.0, () -> level.ghosts().forEach(Ghost::hide)),
                maze3D.mazeFlashAnimation(numFlashes),
                doAfterSec(2.5, () -> level.pac().hide())
        );
    }

    private Animation levelTransformation(GameLevel level, int numFlashes) {
        return new Timeline(
            new KeyFrame(Duration.ZERO, e -> {
                livesCounter3D().light().setLightOn(false);
                if (Globals.randomInt(1, 100) < 25) {
                    THE_UI.showFlashMessageSec(3, THE_ASSETS.localizedLevelCompleteMessage(level.number()));
                }
            }),
            new KeyFrame(Duration.seconds(1.0), e -> level.ghosts().forEach(Ghost::hide)),
            new KeyFrame(Duration.seconds(1.5), e -> maze3D.mazeFlashAnimation(numFlashes).play()),
            new KeyFrame(Duration.seconds(4.5), e -> level.pac().hide()),
            new KeyFrame(Duration.seconds(5.0), e -> levelRotateAnimation(1.5).play()),
            new KeyFrame(Duration.seconds(7.0), e -> {
                maze3D.wallsDisappearAnimation(2.0).play();
                THE_SOUND.playLevelCompleteSound();
            }),
            new KeyFrame(Duration.seconds(9.5), e -> THE_SOUND.playLevelChangedSound())
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