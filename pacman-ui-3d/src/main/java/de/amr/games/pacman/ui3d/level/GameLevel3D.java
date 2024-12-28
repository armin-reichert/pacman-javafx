/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.ObstacleType;
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
import de.amr.games.pacman.ui2d.lib.Ufx;
import de.amr.games.pacman.ui3d.GlobalProperties3d;
import de.amr.games.pacman.ui3d.animation.Squirting;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Point3D;
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
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.lib.Ufx.*;
import static de.amr.games.pacman.ui3d.GlobalProperties3d.*;

/**
 * @author Armin Reichert
 */
public class GameLevel3D {

    static final int   LIVES_COUNTER_MAX     = 5;
    static final float LIVES_SHAPE_SIZE      = 10.0f;
    static final float FLOOR_THICKNESS       = 0.5f;
    static final float OBSTACLE_HEIGHT       = 5.5f;
    static final float OBSTACLE_COAT_HEIGHT  = 0.1f;
    static final float OBSTACLE_THICKNESS    = 1.25f;
    static final float BORDER_WALL_HEIGHT    = 6.0f;
    static final float BORDER_WALL_THICKNESS = 2.5f;
    static final float HOUSE_HEIGHT          = 12.0f;
    static final float HOUSE_OPACITY         = 0.4f;
    static final float HOUSE_SENSITIVITY     = 1.5f * TS;
    static final float PAC_SIZE              = 14.0f;
    static final float GHOST_SIZE            = 13.5f;
    static final float ENERGIZER_RADIUS      = 3.5f;
    static final float PELLET_RADIUS         = 1.0f;

    static final PhongMaterial DEFAULT_MATERIAL = new PhongMaterial();

    private final StringProperty floorTextureNamePy = new SimpleStringProperty(this, "floorTextureName", GlobalProperties3d.NO_TEXTURE);
    private final DoubleProperty borderWallHeightPy = new SimpleDoubleProperty(this, "borderWallHeight", BORDER_WALL_HEIGHT);
    private final DoubleProperty obstacleHeightPy   = new SimpleDoubleProperty(this, "obstacleHeight", OBSTACLE_HEIGHT);
    private final DoubleProperty wallOpacityPy      = new SimpleDoubleProperty(this, "wallOpacity",1.0);

    private final ObjectProperty<Color> floorColorPy      = new SimpleObjectProperty<>(this, "floorColor", Color.BLACK);
    private final ObjectProperty<Color> wallFillColorPy   = new SimpleObjectProperty<>(this, "wallFillColor", Color.BLUE);
    private final ObjectProperty<Color> wallStrokeColorPy = new SimpleObjectProperty<>(this, "wallStrokeColor", Color.LIGHTBLUE);

    private final ObjectProperty<PhongMaterial> wallFillMaterialPy   = new SimpleObjectProperty<>(this, "wallFillMaterial", DEFAULT_MATERIAL);
    private final ObjectProperty<PhongMaterial> wallStrokeMaterialPy = new SimpleObjectProperty<>(this, "wallStrokeMaterial", DEFAULT_MATERIAL);

    public final IntegerProperty livesCounterPy = new SimpleIntegerProperty(0);

    private final GameContext context;

    private final Group root = new Group();
    private final Group worldGroup = new Group();
    private final Group mazeGroup = new Group();
    private final Message3D message3D;
    private final Pac3D pac3D;
    private final List<MutableGhost3D> ghosts3D;
    private final Map<Vector2i, Pellet3D> pellets3D = new HashMap<>();
    private final ArrayList<Energizer3D> energizers3D = new ArrayList<>();
    private final LivesCounter3D livesCounter3D;
    private House3D house3D;
    private Bonus3D bonus3D;

    private final WallBuilder wallBuilder = new WallBuilder();

    public GameLevel3D(GameContext context) {
        this.context = assertNotNull(context);

        final GameModel game = context.game();
        final GameLevel level = context.level();
        final GameWorld world = level.world();
        final WorldMapColoring coloring = context.gameConfiguration().worldMapColoring(world.map());

        pac3D = createPac3D(level.pac());
        ghosts3D = level.ghosts().map(ghost -> createGhost3D(ghost, level.numFlashes())).toList();

        livesCounter3D = createLivesCounter3D(game.canStartNewGame());
        livesCounter3D.livesCountPy.bind(livesCounterPy);

        message3D = new Message3D("", context.assets().font("font.arcade", 6), Color.YELLOW, Color.WHITE);
        message3D.setRotation(Rotate.X_AXIS, 90);
        message3D.setVisible(false);

        wallFillMaterialPy.setValue(createWallFillMaterial());
        wallStrokeMaterialPy.setValue(createWallStrokeMaterial());

        buildWorld3D(world, coloring);
        addFood3D(world, context.assets().get("model3D.pellet"), coloredMaterial(coloring.pellet()));

        // Walls and house must be added after the guys! Otherwise, transparency is not working correctly.
        root.getChildren().addAll(pac3D.shape3D(), pac3D.shape3D().light());
        ghosts3D.forEach(ghost3D -> root.getChildren().add(ghost3D.root()));
        root.getChildren().addAll(message3D, livesCounter3D, worldGroup);

        PY_3D_WALL_HEIGHT.addListener((py,ov,nv) -> obstacleHeightPy.set(nv.doubleValue()));
        wallOpacityPy.bind(PY_3D_WALL_OPACITY);
    }

    private PhongMaterial createWallFillMaterial() {
        PhongMaterial material = new PhongMaterial();
        material.diffuseColorProperty().bind(Bindings.createObjectBinding(
                () -> opaqueColor(wallFillColorPy.get(), wallOpacityPy.get()), wallFillColorPy, wallOpacityPy
        ));
        material.specularColorProperty().bind(material.diffuseColorProperty().map(Color::brighter));
        return material;
    }

    private PhongMaterial createWallStrokeMaterial( ){
        PhongMaterial material = new PhongMaterial();
        material.diffuseColorProperty().bind(Bindings.createObjectBinding(
                () -> opaqueColor(wallStrokeColorPy.get(), wallOpacityPy.get()), wallStrokeColorPy, wallOpacityPy
        ));
        material.specularColorProperty().bind(material.diffuseColorProperty().map(Color::brighter));
        return material;
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

    private MutableGhost3D createGhost3D(Ghost ghost, int numFlashes) {
        String assetKeyPrefix = context.gameConfiguration().assetKeyPrefix();
        Shape3D dressShape    = new MeshView(context.assets().get("model3D.ghost.mesh.dress"));
        Shape3D pupilsShape   = new MeshView(context.assets().get("model3D.ghost.mesh.pupils"));
        Shape3D eyeballsShape = new MeshView(context.assets().get("model3D.ghost.mesh.eyeballs"));
        return new MutableGhost3D(dressShape, pupilsShape, eyeballsShape, context.assets(), assetKeyPrefix, ghost, GHOST_SIZE, numFlashes);
    }

    private LivesCounter3D createLivesCounter3D(boolean canStartNewGame) {
        Node[] shapes = IntStream.range(0, LIVES_COUNTER_MAX).mapToObj(i -> createLivesCounterShape()).toArray(Node[]::new);
        var counter3D = new LivesCounter3D(shapes, 10);
        counter3D.setTranslateX(2 * TS);
        counter3D.setTranslateY(2 * TS);
        counter3D.setVisible(canStartNewGame);
        counter3D.drawModePy.bind(PY_3D_DRAW_MODE);
        counter3D.light().colorProperty().set(Color.CORNFLOWERBLUE);
        counter3D.light().setLightOn(canStartNewGame);
        return counter3D;
    }

    private Node createLivesCounterShape() {
        String assetKeyPrefix = context.gameConfiguration().assetKeyPrefix();
        return switch (context.gameVariant()) {
            case MS_PACMAN, MS_PACMAN_TENGEN -> new Group(
                PacModel3D.createPacShape(
                    context.assets().get("model3D.pacman"), LIVES_SHAPE_SIZE,
                    context.assets().color(assetKeyPrefix + ".pac.color.head"),
                    context.assets().color(assetKeyPrefix + ".pac.color.eyes"),
                    context.assets().color(assetKeyPrefix + ".pac.color.palate")
                ),
                PacModel3D.createFemaleParts(LIVES_SHAPE_SIZE,
                    context.assets().color(assetKeyPrefix + ".pac.color.hairbow"),
                    context.assets().color(assetKeyPrefix + ".pac.color.hairbow.pearls"),
                    context.assets().color(assetKeyPrefix + ".pac.color.boobs")
                )
            );
            case PACMAN, PACMAN_XXL ->
                PacModel3D.createPacShape(
                    context.assets().get("model3D.pacman"), LIVES_SHAPE_SIZE,
                    context.assets().color(assetKeyPrefix + ".pac.color.head"),
                    context.assets().color(assetKeyPrefix + ".pac.color.eyes"),
                    context.assets().color(assetKeyPrefix + ".pac.color.palate")
                );
        };
    }

    public void addLevelCounter() {
        // Place level counter at top right maze corner
        double x = context.level().world().map().terrain().numCols() * TS - 2 * TS;
        double y = 2 * TS;
        Node levelCounter3D = createLevelCounter3D(
            context.gameConfiguration().spriteSheet(),
            context.game().levelCounter(), x, y);
        root.getChildren().add(levelCounter3D);
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

    /**
     * Updates level from game state.
     */
    public void update(GameContext context) {
        pac3D.update(context);
        ghosts3D().forEach(ghost3D -> ghost3D.update(context));
        bonus3D().ifPresent(bonus -> bonus.update(context));

        boolean houseAccessRequired = context.level()
            .ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .anyMatch(Ghost::isVisible);

        boolean ghostNearHouseEntry = context.level()
            .ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .filter(ghost -> ghost.position().euclideanDist(context.level().world().houseEntryPosition()) <= HOUSE_SENSITIVITY)
            .anyMatch(Ghost::isVisible);

        house3D.usedPy.set(houseAccessRequired);
        house3D.openPy.set(ghostNearHouseEntry);

        int symbolsDisplayed = Math.max(0, context.game().lives() - 1);
        if (!context.level().pac().isVisible() && context.gameState() == GameState.STARTING_GAME) {
            livesCounterPy.set(symbolsDisplayed + 1);
        } else {
            livesCounterPy.set(symbolsDisplayed);
        }
    }

    private void buildWorld3D(GameWorld world, WorldMapColoring coloring) {
        obstacleHeightPy.set(PY_3D_WALL_HEIGHT.get());
        wallStrokeColorPy.set(coloring.stroke());
        wallFillColorPy.set(coloring.fill());
        if (coloring.fill().equals(Color.BLACK)) {
            wallFillColorPy.set(Color.grayRgb(42)); // to give some contrast with floor
        }

        Box floor = createFloor(world.map().terrain().numCols() * TS, world.map().terrain().numRows() * TS);
        for (Obstacle obstacle : world.map().obstacles()) {
            if (!world.isPartOfHouse(tileAt(obstacle.startPoint()))) {
                addObstacle(mazeGroup, obstacle, obstacle.hasDoubleWalls() ? BORDER_WALL_THICKNESS : OBSTACLE_THICKNESS);
            }
        }
        house3D = new House3D();
        house3D.heightPy.set(HOUSE_HEIGHT);
        house3D.wallBuilder().setBaseMaterial(Ufx.coloredMaterial(opaqueColor(coloring.fill(), HOUSE_OPACITY)));
        house3D.wallBuilder().setTopMaterial(wallStrokeMaterialPy.get());
        house3D.build(world, coloring);

        mazeGroup.getChildren().add(house3D.root());
        worldGroup.getChildren().addAll(floor, mazeGroup);
        root.getChildren().add(house3D.door3D());
    }

    private void addObstacle(Group parent, Obstacle obstacle, double thickness) {
        Group obstacleGroup = new Group();
        parent.getChildren().add(obstacleGroup);
        ObstacleType obstacleType = obstacle.computeType();
        switch (obstacleType) {
            case O_SHAPE -> {
                Logger.info("Found O-shape: {}", obstacle);
                // Invert colors
                wallBuilder.setTopMaterial(wallFillMaterialPy.get());
                wallBuilder.setBaseMaterial(wallStrokeMaterialPy.get());
                wallBuilder.addOShapeObstacle(obstacleGroup, obstacle, obstacleHeightPy, OBSTACLE_COAT_HEIGHT);
            }
            case L_SHAPE -> {
                Logger.info("Found L-shape: {}", obstacle);
                // Invert colors
                wallBuilder.setTopMaterial(wallFillMaterialPy.get());
                wallBuilder.setBaseMaterial(wallStrokeMaterialPy.get());
                wallBuilder.addLShapeObstacle(obstacleGroup, obstacle, obstacleHeightPy, OBSTACLE_COAT_HEIGHT);
            }
            case T_SHAPE -> {
                Logger.info("Found T-shape: {}", obstacle);
                // Invert colors
                wallBuilder.setTopMaterial(wallFillMaterialPy.get());
                wallBuilder.setBaseMaterial(wallStrokeMaterialPy.get());
                wallBuilder.addTShapeObstacle(obstacleGroup, obstacle, obstacleHeightPy, OBSTACLE_COAT_HEIGHT);
            }
            case CROSS_SHAPE -> {
                Logger.info("Found Cross-shape: {}", obstacle);
                // Invert colors
                wallBuilder.setTopMaterial(wallFillMaterialPy.get());
                wallBuilder.setBaseMaterial(wallStrokeMaterialPy.get());
                wallBuilder.addCrossShapeObstacle(obstacleGroup, obstacle, obstacleHeightPy, OBSTACLE_COAT_HEIGHT);
            }
            case U_SHAPE -> {
                Logger.info("Found U-shape: {}", obstacle);
                // Invert colors
                wallBuilder.setTopMaterial(wallFillMaterialPy.get());
                wallBuilder.setBaseMaterial(wallStrokeMaterialPy.get());
                wallBuilder.addUShapeObstacle(obstacleGroup, obstacle, obstacleHeightPy, OBSTACLE_COAT_HEIGHT);
            }
            default -> {
                Logger.info("Found other shape: closed={} num segments={} num dead-ends={}",
                        obstacle.isClosed(), obstacle.numSegments(), obstacle.numDeadEnds());
                wallBuilder.setBaseMaterial(wallFillMaterialPy.get());
                wallBuilder.setTopMaterial(wallStrokeMaterialPy.get());
                wallBuilder.addGeneralShapeObstacle(obstacleGroup, obstacle, thickness, obstacleHeightPy, OBSTACLE_COAT_HEIGHT);
            }
        }
    }

    private Box createFloor(double sizeX, double sizeY) {
        // add some extra space
        var floor = new Box(sizeX + 10, sizeY, FLOOR_THICKNESS);
        floor.materialProperty().bind(Bindings.createObjectBinding(this::createFloorMaterial, floorColorPy, floorTextureNamePy));
        floor.translateXProperty().bind(floor.widthProperty().multiply(0.5).subtract(5));
        floor.translateYProperty().bind(floor.heightProperty().multiply(0.5));
        floor.translateZProperty().set(FLOOR_THICKNESS * 0.5);
        floor.drawModeProperty().bind(PY_3D_DRAW_MODE);
        floorColorPy.bind(PY_3D_FLOOR_COLOR);
        floorTextureNamePy.bind(PY_3D_FLOOR_TEXTURE);
        return floor;
    }

    private PhongMaterial createFloorMaterial() {
        Color color = floorColorPy.get();
        String textureName = floorTextureNamePy.get();
        Map<String, PhongMaterial> textures = context.assets().get("floor_textures");
        return GlobalProperties3d.NO_TEXTURE.equals(textureName) || !textures.containsKey(textureName)
            ? coloredMaterial(color)
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
                var squirting = new Squirting(root, Duration.seconds(2));
                squirting.setDropReachesFinalPosition(drop ->
                    drop.getTranslateZ() >= -1 && world.containsPoint(drop.getTranslateX(), drop.getTranslateY()));
                squirting.createDrops(15, 46, foodMaterial, position);
                energizer3D.setEatenAnimation(squirting);
                root.getChildren().add(energizer3D.shape3D());
                energizers3D.add(energizer3D);
            } else {
                var pellet3D = new Pellet3D(pelletModel3D, PELLET_RADIUS);
                pellet3D.shape3D().setMaterial(foodMaterial);
                pellet3D.setTile(tile);
                pellet3D.setPosition(position);
                root.getChildren().add(pellet3D.shape3D());
                pellets3D.put(tile, pellet3D);
            }
        });
        energizers3D.trimToSize();
    }

    public void showAnimatedMessage(String text, double displaySeconds, double x, double y) {
        message3D.setText(text);
        message3D.setVisible(true);
        double halfHeight = 0.5 * message3D.getBoundsInLocal().getHeight();
        message3D.setTranslateX(x);
        message3D.setTranslateY(y);
        message3D.setTranslateZ(halfHeight); // just under floor
        var moveUpAnimation = new TranslateTransition(Duration.seconds(1), message3D);
        moveUpAnimation.setToZ(-(halfHeight + 0.5 * obstacleHeightPy.get()));
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
        var rotation = new RotateTransition(Duration.seconds(seconds), root);
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
                new KeyValue(obstacleHeightPy, 0, Interpolator.EASE_IN)
            ));
        var houseDisappears = new Timeline(
            new KeyFrame(totalDuration.multiply(0.33),
                new KeyValue(house3D.heightPy, 0, Interpolator.EASE_IN)
            ));
        var borderWallsDisappear = new Timeline(
            new KeyFrame(totalDuration.multiply(0.33),
                new KeyValue(borderWallHeightPy, 0, Interpolator.EASE_IN)
            )
        );
        var animation  = new SequentialTransition(houseDisappears, obstaclesDisappear, borderWallsDisappear);
        animation.setOnFinished(e -> mazeGroup.setVisible(false));
        return animation;
    }

    public Animation mazeFlashAnimation(int numFlashes) {
        if (numFlashes == 0) {
            return pauseSec(1.0);
        }
        var animation = new Timeline(
            new KeyFrame(Duration.millis(125), new KeyValue(obstacleHeightPy, 0, Interpolator.EASE_BOTH))
        );
        animation.setAutoReverse(true);
        animation.setCycleCount(2*numFlashes);
        return animation;
    }

    public Group root() { return root; }

    public Pac3D pac3D() { return pac3D; }

    public List<MutableGhost3D> ghosts3D() { return ghosts3D; }

    public MutableGhost3D ghost3D(byte id) { return ghosts3D.get(id); }

    public Optional<Bonus3D> bonus3D() { return Optional.ofNullable(bonus3D); }

    public LivesCounter3D livesCounter3D() { return livesCounter3D; }

    public House3D house3D() { return house3D; }

    public Stream<Pellet3D> pellets3D() { return pellets3D.values().stream(); }

    public Stream<Energizer3D> energizers3D() { return energizers3D.stream(); }

    public Optional<Energizer3D> energizer3D(Vector2i tile) {
        assertTileNotNull(tile);
        return energizers3D().filter(e3D -> e3D.tile().equals(tile)).findFirst();
    }

    public Optional<Pellet3D> pellet3D(Vector2i tile) {
        assertTileNotNull(tile);
        return Optional.ofNullable(pellets3D.get(tile));
    }
}