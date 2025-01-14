/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.assets.GameSpriteSheet;
import de.amr.games.pacman.ui2d.assets.WorldMapColoring;
import de.amr.games.pacman.ui3d.GlobalProperties3d;
import de.amr.games.pacman.ui3d.animation.Squirting;
import de.amr.games.pacman.ui3d.model.Model3D;
import de.amr.games.pacman.ui3d.scene3d.GameConfiguration3D;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
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
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.lib.Ufx.coloredMaterial;
import static de.amr.games.pacman.ui3d.GlobalProperties3d.*;

/**
 * @author Armin Reichert
 */
public class GameLevel3D extends Group {

    private final BooleanProperty houseOpenPy = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            if (houseOpenPy.get()) {
                maze3D.door3D().playOpenCloseAnimation();
            }
        }
    };

    private final IntegerProperty livesCountPy = new SimpleIntegerProperty(0);

    private final GameContext context;
    private final AmbientLight ambientLight;

    private final Group worldGroup = new Group();

    private final Maze3D maze3D = new Maze3D();
    private final Pac3D pac3D;
    private final List<Ghost3DAppearance> ghost3DAppearances;
    private final List<Pellet3D> pellets3D = new ArrayList<>();
    private final ArrayList<Energizer3D> energizers3D = new ArrayList<>();
    private final LivesCounter3D livesCounter3D;

    private Box floor3D;
    private Message3D message3D;
    private Bonus3D bonus3D;

    public GameLevel3D(GameContext context) {
        this.context = assertNotNull(context);

        final GameModel game = context.game();
        final GameLevel level = context.level();
        final GameWorld world = level.world();

        pac3D = createPac3D(level.pac());
        ghost3DAppearances = level.ghosts().map(ghost -> createGhost3D(ghost, level.numFlashes())).toList();

        livesCounter3D = createLivesCounter3D(game.canStartNewGame());
        livesCounter3D.livesCountPy.bind(livesCountPy);

        floor3D = createFloor(world.map().terrain().numCols() * TS, world.map().terrain().numRows() * TS);
        worldGroup.getChildren().add(floor3D);

        WorldMapColoring coloring = context.gameConfiguration().worldMapColoring(world.map());
        maze3D.build((GameConfiguration3D) context.gameConfiguration(), world, coloring);
        worldGroup.getChildren().add(maze3D);

        addFood3D(world, context.assets().get("model3D.pellet"), coloredMaterial(coloring.pellet()));

        // Walls and house must be added after the guys! Otherwise, transparency is not working correctly.
        getChildren().addAll(pac3D.shape3D(), pac3D.shape3D().light());
        getChildren().addAll(ghost3DAppearances);
        getChildren().addAll(livesCounter3D, worldGroup);


        ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PY_3D_LIGHT_COLOR);
        getChildren().add(ambientLight);

        setMouseTransparent(true); //TODO does this increase performance?
    }

    public void update(GameContext context) {
        pac3D.update(context);
        ghosts3D().forEach(ghost3D -> ghost3D.update(context));
        bonus3D().ifPresent(bonus -> bonus.update(context));

        boolean houseAccessRequired = context.level()
            .ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .anyMatch(Ghost::isVisible);
        maze3D().setHouseLightOn(houseAccessRequired);

        boolean ghostNearHouseEntry = context.level()
            .ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .filter(ghost -> ghost.position().euclideanDist(context.level().world().houseEntryPosition()) <= HOUSE_SENSITIVITY)
            .anyMatch(Ghost::isVisible);
        houseOpenPy.set(ghostNearHouseEntry);

        int symbolsDisplayed = Math.max(0, context.game().lives() - 1);
        if (!context.level().pac().isVisible() && context.gameState() == GameState.STARTING_GAME) {
            livesCountPy.set(symbolsDisplayed + 1);
        } else {
            livesCountPy.set(symbolsDisplayed);
        }

        // experimental
        //maze3D.highlightObstacleNearPac(context.level().pac().position());
    }

    private Pac3D createPac3D(Pac pac) {
        String assetKeyPrefix = context.gameConfiguration().assetKeyPrefix();
        Pac3D pac3D = switch (context.gameVariant()) {
            case MS_PACMAN, MS_PACMAN_TENGEN -> new MsPacMan3D(context.gameVariant(), pac, PAC_SIZE, context.assets(), assetKeyPrefix);
            case PACMAN, PACMAN_XXL          -> new PacMan3D(context.gameVariant(), pac, PAC_SIZE, context.assets(), assetKeyPrefix);
        };
        pac3D.shape3D().light().setColor(context.assets().color(assetKeyPrefix + ".pac.color.head").desaturate());
        pac3D.shape3D().drawModeProperty().bind(PY_3D_DRAW_MODE);
        return pac3D;
    }

    private Ghost3DAppearance createGhost3D(Ghost ghost, int numFlashes) {
        String assetKeyPrefix = context.gameConfiguration().assetKeyPrefix();
        Shape3D dressShape    = new MeshView(context.assets().get("model3D.ghost.mesh.dress"));
        Shape3D pupilsShape   = new MeshView(context.assets().get("model3D.ghost.mesh.pupils"));
        Shape3D eyeballsShape = new MeshView(context.assets().get("model3D.ghost.mesh.eyeballs"));
        return new Ghost3DAppearance(dressShape, pupilsShape, eyeballsShape, context.assets(), assetKeyPrefix,
            ghost, GHOST_SIZE, numFlashes);
    }

    private LivesCounter3D createLivesCounter3D(boolean canStartNewGame) {
        GameConfiguration3D config3D = (GameConfiguration3D) context.gameConfiguration();
        Node[] shapes = IntStream.range(0, LIVES_COUNTER_MAX)
            .mapToObj(i -> config3D.createLivesCounterShape(context.assets())).toArray(Node[]::new);
        var counter3D = new LivesCounter3D(shapes, 10);
        counter3D.setTranslateX(2 * TS);
        counter3D.setTranslateY(2 * TS);
        counter3D.setVisible(canStartNewGame);
        counter3D.drawModePy.bind(PY_3D_DRAW_MODE);
        counter3D.light().colorProperty().set(Color.CORNFLOWERBLUE);
        counter3D.light().setLightOn(canStartNewGame);
        return counter3D;
    }

    public void addLevelCounter() {
        // Place level counter at top right maze corner
        double x = context.level().world().map().terrain().numCols() * TS - 2 * TS;
        double y = 2 * TS;
        Node levelCounter3D = createLevelCounter3D(
            context.gameConfiguration().spriteSheet(),
            context.game().levelCounter(), x, y);
        getChildren().add(levelCounter3D);
    }

    private Node createLevelCounter3D(GameSpriteSheet spriteSheet, List<Byte> symbols, double x, double y) {
        double spacing = 2 * TS;
        var levelCounter3D = new Group();
        levelCounter3D.setTranslateX(x);
        levelCounter3D.setTranslateY(y);
        levelCounter3D.setTranslateZ(-6);
        levelCounter3D.getChildren().clear();
        int n = 0;
        for (byte symbol : symbols) {
            Box cube = new Box(TS, TS, TS);
            cube.setTranslateX(-n * spacing);
            cube.setTranslateZ(-HTS);
            levelCounter3D.getChildren().add(cube);

            var material = new PhongMaterial(Color.WHITE);
            Image texture = spriteSheet.subImage(spriteSheet.bonusSymbolSprite(symbol));
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
        var floor3D = new Box(sizeX + extraSpace, sizeY, FLOOR_THICKNESS);
        floor3D.materialProperty().bind(
            Bindings.createObjectBinding(this::createFloorMaterial, PY_3D_FLOOR_COLOR, PY_3D_FLOOR_TEXTURE_NAME));
        floor3D.translateXProperty().bind(floor3D.widthProperty().multiply(0.5).subtract(0.5*extraSpace));
        floor3D.translateYProperty().bind(floor3D.heightProperty().multiply(0.5));
        floor3D.translateZProperty().set(FLOOR_THICKNESS * 0.5);
        floor3D.drawModeProperty().bind(PY_3D_DRAW_MODE);
        return floor3D;
    }

    private PhongMaterial createFloorMaterial() {
        Map<String, PhongMaterial> textures = context.assets().get("floor_textures");
        String textureName = PY_3D_FLOOR_TEXTURE_NAME.get();
        return GlobalProperties3d.NO_TEXTURE.equals(textureName) || !textures.containsKey(textureName)
            ? coloredMaterial(PY_3D_FLOOR_COLOR.get())
            : textures.get(textureName);
    }

    private void addFood3D(GameWorld world, Model3D pelletModel3D, Material foodMaterial) {
        world.map().food().tiles().filter(world::hasFoodAt).forEach(tile -> {
            Point3D position = new Point3D(tile.x() * TS + HTS, tile.y() * TS + HTS, -6);
            if (world.isEnergizerPosition(tile)) {
                var energizer3D = new Energizer3D(ENERGIZER_RADIUS);
                energizer3D.shape3D().setMaterial(foodMaterial);
                energizer3D.setTile(tile);
                energizer3D.setPosition(position);
                var squirting = new Squirting(this, Duration.seconds(2));
                squirting.setDropReachesFinalPosition(drop ->
                    drop.getTranslateZ() >= -1 && world.containsPoint(drop.getTranslateX(), drop.getTranslateY()));
                squirting.createDrops(15, 46, foodMaterial, position);
                energizer3D.setEatenAnimation(squirting);
                getChildren().add(energizer3D.shape3D());
                energizers3D.add(energizer3D);
            } else {
                var pellet3D = new Pellet3D(pelletModel3D, PELLET_RADIUS);
                pellet3D.shape3D().setMaterial(foodMaterial);
                pellet3D.setTile(tile);
                pellet3D.setPosition(position);
                getChildren().add(pellet3D.shape3D());
                pellets3D.add(pellet3D);
            }
        });
        energizers3D.trimToSize();
    }

    public void showAnimatedMessage(String text, double displaySeconds, double centerX, double y) {
        if (message3D != null) {
            getChildren().remove(message3D);
        }
        message3D = Message3D.builder()
            .text(text)
            .font(context.assets().font("font.arcade", 6))
            .borderColor(Color.WHITE)
            .textColor(Color.YELLOW)
            .build();
        getChildren().add(message3D);

        double halfHeight = 0.5 * message3D.getBoundsInLocal().getHeight();
        message3D.setTranslateX(centerX - 0.5 * message3D.getFitWidth());
        message3D.setTranslateY(y);
        message3D.setTranslateZ(halfHeight); // just under floor

        var moveUpAnimation = new TranslateTransition(Duration.seconds(1), message3D);
        moveUpAnimation.setToZ(-(halfHeight + 0.5 * OBSTACLE_BASE_HEIGHT));

        var moveDownAnimation = new TranslateTransition(Duration.seconds(1), message3D);
        moveDownAnimation.setDelay(Duration.seconds(displaySeconds));
        moveDownAnimation.setToZ(halfHeight);
        moveDownAnimation.setOnFinished(e -> message3D.setVisible(false));

        new SequentialTransition(moveUpAnimation, moveDownAnimation).play();
    }

    public void replaceBonus3D(Bonus bonus, GameSpriteSheet spriteSheet) {
        assertNotNull(bonus);
        if (bonus3D != null) {
            worldGroup.getChildren().remove(bonus3D);
        }
        bonus3D = new Bonus3D(bonus,
            spriteSheet.subImage(spriteSheet.bonusSymbolSprite(bonus.symbol())),
            spriteSheet.subImage(spriteSheet.bonusValueSprite(bonus.symbol())));
        bonus3D.showEdible();
        worldGroup.getChildren().add(bonus3D);
    }

    public RotateTransition levelRotateAnimation(double seconds) {
        var rotation = new RotateTransition(Duration.seconds(seconds), this);
        rotation.setAxis(RND.nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS);
        rotation.setFromAngle(0);
        rotation.setToAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        return rotation;
    }

    public void stopAnimations() {
        energizers3D().forEach(Energizer3D::stopPumping);
        livesCounter3D().shapesRotation().stop();
        bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
    }

    public Maze3D maze3D() { return maze3D; }

    public Pac3D pac3D() { return pac3D; }

    public List<Ghost3DAppearance> ghosts3D() { return ghost3DAppearances; }

    public Ghost3DAppearance ghost3D(byte id) { return ghost3DAppearances.get(id); }

    public Optional<Bonus3D> bonus3D() { return Optional.ofNullable(bonus3D); }

    public LivesCounter3D livesCounter3D() { return livesCounter3D; }

    public Stream<Pellet3D> pellets3D() { return pellets3D.stream(); }

    public Stream<Energizer3D> energizers3D() { return energizers3D.stream(); }

    public Color floorColor() { return PY_3D_FLOOR_COLOR.get(); }

    public double floorThickness() { return floor3D.getDepth(); }

}