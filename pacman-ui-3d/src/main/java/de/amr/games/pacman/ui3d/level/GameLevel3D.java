/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
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
import javafx.beans.property.*;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.lib.Ufx.*;
import static de.amr.games.pacman.ui3d.GlobalProperties3d.*;
import static de.amr.games.pacman.ui3d.level.WorldRenderer3D.TAG_WALL_BASE;
import static de.amr.games.pacman.ui3d.level.WorldRenderer3D.isTagged;

/**
 * @author Armin Reichert
 */
public class GameLevel3D extends Group {

    private static final String OSHAPES_FILLED_PROPERTY_NAME = "rendering_oshapes_filled";

    private final StringProperty floorTextureNamePy   = new SimpleStringProperty(GlobalProperties3d.NO_TEXTURE);
    private final DoubleProperty obstacleBaseHeightPy = new SimpleDoubleProperty(OBSTACLE_BASE_HEIGHT);
    private final DoubleProperty wallOpacityPy        = new SimpleDoubleProperty(1);
    private final ObjectProperty<Color> floorColorPy  = new SimpleObjectProperty<>(Color.BLACK);
    private final IntegerProperty livesCounterPy      = new SimpleIntegerProperty(0);
    private final DoubleProperty houseBaseHeightPy    = new SimpleDoubleProperty(HOUSE_BASE_HEIGHT);
    private final BooleanProperty houseLightOnPy      = new SimpleBooleanProperty(false);

    private final BooleanProperty houseOpenPy = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            if (houseOpenPy.get()) {
                door3D.playOpenCloseAnimation();
            }
        }
    };

    private final GameContext context;
    private final AmbientLight ambientLight;
    private final Group worldGroup = new Group();
    private final Group mazeGroup = new Group();
    private final Pac3D pac3D;
    private final List<Ghost3DAppearance> ghost3DAppearances;
    private final List<Pellet3D> pellets3D = new ArrayList<>();
    private final ArrayList<Energizer3D> energizers3D = new ArrayList<>();
    private final LivesCounter3D livesCounter3D;
    private Door3D door3D;
    private Box floor;
    private Message3D message3D;
    private Bonus3D bonus3D;

    // experimental
    private PhongMaterial cornerMaterial;
    private Set<Group> obstacleGroups;
    private PhongMaterial highlightMaterial = new PhongMaterial(Color.YELLOW);

    public GameLevel3D(GameContext context) {
        this.context = assertNotNull(context);

        final GameModel game = context.game();
        final GameLevel level = context.level();
        final GameWorld world = level.world();

        pac3D = createPac3D(level.pac());
        ghost3DAppearances = level.ghosts().map(ghost -> createGhost3D(ghost, level.numFlashes())).toList();

        livesCounter3D = createLivesCounter3D(game.canStartNewGame());
        livesCounter3D.livesCountPy.bind(livesCounterPy);

        worldGroup.getChildren().add(mazeGroup);
        addWorld3D(mazeGroup, world);

        // Walls and house must be added after the guys! Otherwise, transparency is not working correctly.
        getChildren().addAll(pac3D.shape3D(), pac3D.shape3D().light());
        getChildren().addAll(ghost3DAppearances);
        getChildren().addAll(livesCounter3D, worldGroup);

        PY_3D_WALL_HEIGHT.addListener((py,ov,nv) -> obstacleBaseHeightPy.set(nv.doubleValue()));
        wallOpacityPy.bind(PY_3D_WALL_OPACITY);

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
        houseLightOnPy.set(houseAccessRequired);

        boolean ghostNearHouseEntry = context.level()
            .ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .filter(ghost -> ghost.position().euclideanDist(context.level().world().houseEntryPosition()) <= HOUSE_SENSITIVITY)
            .anyMatch(Ghost::isVisible);
        houseOpenPy.set(ghostNearHouseEntry);

        int symbolsDisplayed = Math.max(0, context.game().lives() - 1);
        if (!context.level().pac().isVisible() && context.gameState() == GameState.STARTING_GAME) {
            livesCounterPy.set(symbolsDisplayed + 1);
        } else {
            livesCounterPy.set(symbolsDisplayed);
        }

        // experimental
        //highlightObstacleNearPac(context.level().pac().position());
    }

    private void highlightObstacleNearPac(Vector2f pacPosition) {
        for (Group obstacleGroup : obstacleGroups) {
            Set<Node> obstacleParts = obstacleGroup.lookupAll("*").stream()
                    .filter(node -> node instanceof Box || node instanceof Cylinder)
                    .collect(Collectors.toSet());
            boolean highlight = false;
            for (Node node : obstacleParts) {
                if (isTagged(node, TAG_WALL_BASE)) {
                    Vector2f nodePosition = vec_2f(node.getTranslateX(), node.getTranslateY());
                    highlight = nodePosition.euclideanDist(pacPosition) < 2 * TS;
                    break;
                }
            }
            for (Node node : obstacleParts) {
                if (isTagged(node, TAG_WALL_BASE) && node instanceof Shape3D shape3D) {
                    shape3D.setMaterial(highlight ? highlightMaterial : cornerMaterial); // TODO
                }
            }
        }
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

    private void addWorld3D(Group parent, GameWorld world) {
        Logger.info("Build world 3D. Map URL='{}'", URLDecoder.decode(world.map().url().toExternalForm(), StandardCharsets.UTF_8));

        WorldMapColoring coloring = context.gameConfiguration().worldMapColoring(world.map());        createFloor(world.map().terrain().numCols() * TS, world.map().terrain().numRows() * TS);
        Color wallBaseColor = coloring.stroke();
        // need some contrast with floor if fill color is black
        Color wallTopColor = coloring.fill().equals(Color.BLACK) ? Color.grayRgb(30) : coloring.fill();

        var wallTopMaterial = new PhongMaterial();
        wallTopMaterial.diffuseColorProperty().bind(Bindings.createObjectBinding(
            () -> opaqueColor(wallTopColor, wallOpacityPy.get()), wallOpacityPy
        ));
        wallTopMaterial.specularColorProperty().bind(wallTopMaterial.diffuseColorProperty().map(Color::brighter));

        var wallBaseMaterial = new PhongMaterial();
        wallBaseMaterial.diffuseColorProperty().bind(Bindings.createObjectBinding(
            () -> opaqueColor(wallBaseColor, wallOpacityPy.get()), wallOpacityPy
        ));
        wallBaseMaterial.specularColorProperty().bind(wallBaseMaterial.diffuseColorProperty().map(Color::brighter));

        cornerMaterial = new PhongMaterial();
        cornerMaterial.setDiffuseColor(wallBaseColor);
        cornerMaterial.specularColorProperty().bind(cornerMaterial.diffuseColorProperty().map(Color::brighter));

        var configuration3D = (GameConfiguration3D) context.gameConfiguration();
        WorldRenderer3D r3D = configuration3D.createWorldRenderer();
        r3D.setWallBaseMaterial(wallBaseMaterial);
        r3D.setWallBaseHeightProperty(obstacleBaseHeightPy);
        r3D.setWallTopMaterial(wallTopMaterial);
        r3D.setWallTopHeight(OBSTACLE_TOP_HEIGHT);
        r3D.setCornerMaterial(cornerMaterial);

        //TODO check this:
        obstacleBaseHeightPy.set(PY_3D_WALL_HEIGHT.get());

        //TODO just a temporary solution until I find something better
        if (world.map().terrain().hasProperty(OSHAPES_FILLED_PROPERTY_NAME)) {
            Object value = world.map().terrain().getProperty(OSHAPES_FILLED_PROPERTY_NAME);
            try {
                r3D.setOShapeFilled(Boolean.parseBoolean(String.valueOf(value)));
            } catch (Exception x) {
                Logger.error("Map property '{}}' is not a valid boolean value: {}", OSHAPES_FILLED_PROPERTY_NAME, value);
            }
        }

        for (Obstacle obstacle : world.map().obstacles()) {
            Logger.info("{}: {}", obstacle.computeType(), obstacle);
            if (!world.isPartOfHouse(tileAt(obstacle.startPoint()))) {
                r3D.setWallThickness(obstacle.hasDoubleWalls() ? BORDER_WALL_THICKNESS : OBSTACLE_THICKNESS);
                r3D.renderObstacle3D(parent, obstacle);
            }
        }

        // House
        houseBaseHeightPy.set(HOUSE_BASE_HEIGHT);
        door3D = r3D.addGhostHouse(
            parent, world,
            coloring.fill(), coloring.stroke(), coloring.door(),
            HOUSE_OPACITY,
            houseBaseHeightPy, HOUSE_WALL_TOP_HEIGHT, HOUSE_WALL_THICKNESS,
            houseLightOnPy);
        getChildren().add(door3D); //TODO check this

        addFood3D(world, context.assets().get("model3D.pellet"), coloredMaterial(coloring.pellet()));

        // experimental
        obstacleGroups = parent.lookupAll("*").stream()
                .filter(Group.class::isInstance)
                .map(Group.class::cast)
                .filter(group -> isTagged(group, WorldRenderer3D.TAG_INNER_OBSTACLE))
                .collect(Collectors.toSet());
    }

    private void createFloor(double sizeX, double sizeY) {
        // add some extra space
        floor = new Box(sizeX + 10, sizeY, FLOOR_THICKNESS);
        floor.materialProperty().bind(
            Bindings.createObjectBinding(this::createFloorMaterial, floorColorPy, floorTextureNamePy));
        floor.translateXProperty().bind(floor.widthProperty().multiply(0.5).subtract(5));
        floor.translateYProperty().bind(floor.heightProperty().multiply(0.5));
        floor.translateZProperty().set(FLOOR_THICKNESS * 0.5);
        floor.drawModeProperty().bind(PY_3D_DRAW_MODE);
        floorColorPy.bind(PY_3D_FLOOR_COLOR);
        floorTextureNamePy.bind(PY_3D_FLOOR_TEXTURE);

        worldGroup.getChildren().add(floor);
    }

    private PhongMaterial createFloorMaterial() {
        Map<String, PhongMaterial> textures = context.assets().get("floor_textures");
        String textureName = floorTextureNamePy.get();
        return GlobalProperties3d.NO_TEXTURE.equals(textureName) || !textures.containsKey(textureName)
            ? coloredMaterial(floorColorPy.get())
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
        moveUpAnimation.setToZ(-(halfHeight + 0.5 * obstacleBaseHeightPy.get()));

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

    public Animation wallsDisappearAnimation(double seconds) {
        var totalDuration = Duration.seconds(seconds);
        var obstaclesDisappear = new Timeline(
            new KeyFrame(totalDuration.multiply(0.33),
                new KeyValue(obstacleBaseHeightPy, 0, Interpolator.EASE_IN)
            ));
        var houseDisappears = new Timeline(
            new KeyFrame(totalDuration.multiply(0.33),
                new KeyValue(houseBaseHeightPy, 0, Interpolator.EASE_IN)
            ));
        var animation = new SequentialTransition(houseDisappears, obstaclesDisappear);
        animation.setOnFinished(e -> mazeGroup.setVisible(false));
        return animation;
    }

    public Animation mazeFlashAnimation(int numFlashes) {
        if (numFlashes == 0) {
            return pauseSec(1.0);
        }
        var animation = new Timeline(
            new KeyFrame(Duration.millis(125), new KeyValue(obstacleBaseHeightPy, 0, Interpolator.EASE_BOTH))
        );
        animation.setAutoReverse(true);
        animation.setCycleCount(2 * numFlashes);
        return animation;
    }

    public void stopAnimations() {
        energizers3D().forEach(Energizer3D::stopPumping);
        livesCounter3D().shapesRotation().stop();
        bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
    }

    public Pac3D pac3D() { return pac3D; }

    public List<Ghost3DAppearance> ghosts3D() { return ghost3DAppearances; }

    public Ghost3DAppearance ghost3D(byte id) { return ghost3DAppearances.get(id); }

    public Optional<Bonus3D> bonus3D() { return Optional.ofNullable(bonus3D); }

    public LivesCounter3D livesCounter3D() { return livesCounter3D; }

    public Stream<Pellet3D> pellets3D() { return pellets3D.stream(); }

    public Stream<Energizer3D> energizers3D() { return energizers3D.stream(); }

    public Optional<Energizer3D> energizer3D(Vector2i tile) {
        assertTileNotNull(tile);
        return energizers3D().filter(e3D -> e3D.tile().equals(tile)).findFirst();
    }

    public Color floorColor() { return floorColorPy.get(); }

    public double floorThickness() { return floor.getDepth(); }

    public Door3D door3D() {
        return door3D;
    }
}