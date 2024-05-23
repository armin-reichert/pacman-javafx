/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.lib.*;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.GameSceneContext;
import de.amr.games.pacman.ui.fx.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.tilemap.TileMapRenderer;
import de.amr.games.pacman.ui.fx.v3d.animation.Squirting;
import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.coloredMaterial;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.opaqueColor;
import static de.amr.games.pacman.ui.fx.util.Ufx.doAfterSec;
import static de.amr.games.pacman.ui.fx.util.Ufx.pauseSec;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.*;
import static java.lang.Math.PI;

/**
 * @author Armin Reichert
 */
public class GameLevel3D extends Group {

    static final float FLOOR_THICKNESS       = 0.4f;
    static final float WALL_HEIGHT           = 2.0f;
    static final float WALL_THICKNESS        = 0.75f;
    static final float WALL_STROKE_THICKNESS = 0.1f;
    static final float HOUSE_HEIGHT          = 12.0f;
    static final float HOUSE_OPACITY         = 0.4f;
    static final float PAC_SIZE              = 14.0f;
    static final float GHOST_SIZE            = 13.0f;
    static final float ENERGIZER_RADIUS      = 3.5f;
    static final float PELLET_RADIUS         = 1.0f;

    final Map<String, PhongMaterial> floorTextures = new HashMap<>();

    final ObjectProperty<String> floorTextureNamePy = new SimpleObjectProperty<>(this, "floorTextureName") {
        @Override
        protected void invalidated() {
            Color floorColor = floorColorPy.get();
            String textureName = get();
            if (NO_TEXTURE.equals(textureName)) {
                floor.setMaterial(coloredMaterial(floorColor));
            } else {
                floor.setMaterial(floorTextures.getOrDefault("texture." + textureName, coloredMaterial(floorColor)));
            }
        }
    };

    final ObjectProperty<Color> floorColorPy = new SimpleObjectProperty<>(this, "floorColor", Color.BLACK) {
        @Override
        protected void invalidated() {
            Color floorColor = get();
            String textureName = floorTextureNamePy.get();
            floor.setMaterial(floorTextures.getOrDefault("texture." + textureName, coloredMaterial(floorColor)));
        }
    };

    final DoubleProperty wallHeightPy  = new SimpleDoubleProperty(this, "wallHeight", WALL_HEIGHT);

    final DoubleProperty houseHeightPy = new SimpleDoubleProperty(this, "houseHeight", HOUSE_HEIGHT);

    final ObjectProperty<PhongMaterial> houseFillMaterialPy = new SimpleObjectProperty<>(this, "houseFillMaterial");

    final ObjectProperty<PhongMaterial> wallStrokeMaterialPy = new SimpleObjectProperty<>(this, "wallStrokeMaterial");

    final ObjectProperty<PhongMaterial> wallFillMaterialPy = new SimpleObjectProperty<>(this, "wallFillMaterial");

    final ObjectProperty<PhongMaterial> foodMaterialPy = new SimpleObjectProperty<>(this, "foodMaterial");

    final DoubleProperty wallOpacityPy = new SimpleDoubleProperty(this, "wallOpacity",1.0) {
        @Override
        protected void invalidated() {
            double opacity = get();
            Color fillColor = wallFillColorPy.get();
            Color color = opaqueColor(fillColor, opacity);
            PhongMaterial fillMaterial = wallFillMaterialPy.get();
            fillMaterial.setDiffuseColor(color);
            fillMaterial.setSpecularColor(color.brighter());
        }
    };

    final ObjectProperty<Color> wallStrokeColorPy = new SimpleObjectProperty<>(Color.WHITE) {
        @Override
        protected void invalidated() {
            Color strokeColor = get();
            wallStrokeMaterialPy.set(coloredMaterial(strokeColor));        }
    };

    final ObjectProperty<Color> wallFillColorPy = new SimpleObjectProperty<>(Color.GREEN) {
        @Override
        protected void invalidated() {
            Color fillColor = get();
            double opacity = wallOpacityPy.get();
            wallFillMaterialPy.set(coloredMaterial(opaqueColor(fillColor, opacity)));
            houseFillMaterialPy.set(coloredMaterial(opaqueColor(fillColor, HOUSE_OPACITY)));
        }
    };

    final ObjectProperty<Color> foodColorPy = new SimpleObjectProperty<>(Color.PINK) {
        @Override
        protected void invalidated() {
            foodMaterialPy.set(coloredMaterial(get()));
        }
    };

    final GameSceneContext context;
    final Group worldGroup = new Group();
    final Group mazeGroup = new Group();
    final Group levelCounterGroup = new Group();
    final Box floor;
    final PointLight houseLight = new PointLight();
    final Pac3D pac3D;
    final List<Ghost3D> ghosts3D;
    final Set<Pellet3D> pellets3D = new HashSet<>();
    final Set<Energizer3D> energizers3D = new HashSet<>();

    LivesCounter3D livesCounter3D;
    Bonus3D bonus3D;
    Message3D message3D;

    public GameLevel3D(GameSceneContext context) {
        this.context = checkNotNull(context);

        // Floor
        List<String> textureNames = context.theme().getArray("texture.names");
        for (String textureName : textureNames) {
            String key = "texture." + textureName;
            floorTextures.put(key, context.theme().get(key));
        }
        floor = new Box(context.game().world().numCols() * TS - 1, context.game().world().numRows() * TS - 1, FLOOR_THICKNESS);
        // place floor such that surface is at z=0
        floor.getTransforms().add(new Translate(0.5 * floor.getWidth(), 0.5 * floor.getHeight(), 0.5 * floor.getDepth()));
        floor.drawModeProperty().bind(PY_3D_DRAW_MODE);
        floorColorPy.bind(PY_3D_FLOOR_COLOR);
        floorTextureNamePy.bind(PY_3D_FLOOR_TEXTURE);

        // Maze
        var terrain = context.game().world().map().terrain();
        wallStrokeColorPy.set(TileMapRenderer.getColorFromMap(terrain, "wall_stroke_color", Color.rgb(33, 33, 255)));
        wallFillColorPy.set(TileMapRenderer.getColorFromMap(terrain, "wall_fill_color", Color.rgb(0,0,0)));
        foodColorPy.set(TileMapRenderer.getColorFromMap(terrain, "food_color", Color.PINK));
        addMazeWalls(mazeGroup);
        addArcadeGhostHouse(mazeGroup);
        addFood3D(mazeGroup);

        pac3D = switch (context.game().variant()) {
            case MS_PACMAN -> Pac3D.createFemalePac3D(context.theme(), context.game().pac(), PAC_SIZE);
            case PACMAN, PACMAN_XXL -> Pac3D.createMalePac3D(context.theme(), context.game().pac(), PAC_SIZE);
        };
        ghosts3D = context.game().ghosts().map(this::createGhost3D).toList();

        createLivesCounter3D();
        createLevelCounter3D();
        createMessage3D();

        worldGroup.getChildren().addAll(floor, mazeGroup);
        getChildren().addAll(ghosts3D);
        getChildren().addAll(pac3D, pac3D.light());
        // Walls group must come after the guys! Otherwise, transparency is not working correctly.
        getChildren().addAll(message3D, levelCounterGroup, livesCounter3D, worldGroup);

        pac3D.lightedPy.bind(PY_3D_PAC_LIGHT_ENABLED);
        pac3D.drawModePy.bind(PY_3D_DRAW_MODE);
        ghosts3D.forEach(ghost3D -> ghost3D.drawModePy.bind(PY_3D_DRAW_MODE));
        livesCounter3D.drawModePy.bind(PY_3D_DRAW_MODE);
        wallHeightPy.bind(PY_3D_WALL_HEIGHT);
        wallOpacityPy.bind(PY_3D_WALL_OPACITY);
    }

    private void addArcadeGhostHouse(Group parent) {
        addHouseWall(parent, 10,15, 12,15);
        addHouseWall(parent, 10,15, 10,19);
        addHouseWall(parent, 10,19, 17,19);
        addHouseWall(parent, 17,19, 17,15);
        addHouseWall(parent, 17,15, 15,15);

        House house = context.game().world().house();
        Color doorColor = TileMapRenderer.getColorFromMap(context.game().world().map().terrain(), "door_color", Color.rgb(254,184,174));
        for (Vector2i wingTile : List.of(house.door().leftWing(), house.door().rightWing())) {
            var doorWing3D = new DoorWing3D(wingTile, doorColor, PY_3D_FLOOR_COLOR.get());
            doorWing3D.drawModePy.bind(PY_3D_DRAW_MODE);
            parent.getChildren().add(doorWing3D);
        }

        float centerX = house.topLeftTile().x() * TS + house.size().x() * HTS;
        float centerY = house.topLeftTile().y() * TS + house.size().y() * HTS;
        houseLight.setColor(Color.GHOSTWHITE);
        houseLight.setMaxRange(3 * TS);
        houseLight.setTranslateX(centerX);
        houseLight.setTranslateY(centerY);
        houseLight.setTranslateZ(-TS);
        parent.getChildren().add(houseLight);
    }

    private void addHouseWall(Group parent, int x1, int y1, int x2, int y2) {
        parent.getChildren().add(createWall(v2i(x1, y1), v2i(x2, y2), houseHeightPy, houseFillMaterialPy));
    }

    private void addFood3D(Group parent) {
        var world = context.game().world();
        Color color = TileMapRenderer.getColorFromMap(world.map().food(), "food_color", Color.WHITE);
        foodColorPy.set(color);
        world.tiles().filter(world::hasFoodAt).forEach(tile -> {
            if (world.isEnergizerTile(tile)) {
                var energizer3D = new Energizer3D(ENERGIZER_RADIUS);
                addEnergizerAnimation(world, energizer3D);
                energizers3D.add(energizer3D);
                energizer3D.root().materialProperty().bind(foodMaterialPy);
                energizer3D.placeAtTile(tile);
                parent.getChildren().add(energizer3D.root());
            } else {
                var pellet3D = new Pellet3D(context.theme().get("model3D.pellet"), PELLET_RADIUS);
                pellets3D.add(pellet3D);
                pellet3D.root().materialProperty().bind(foodMaterialPy);
                pellet3D.placeAtTile(tile);
                parent.getChildren().add(pellet3D.root());
            }
        });
    }

    private Stream<DoorWing3D> doorWings3D() {
        return worldGroup.getChildren().stream()
            .filter(node -> node instanceof DoorWing3D)
            .map(DoorWing3D.class::cast);
    }

    private void addMazeWalls(Group parent) {
        TileMap terrainMap = context.game().world().map().terrain();
        var explored = new BitSet();

        // Obstacles inside maze
        terrainMap.tiles(Tiles.CORNER_NW)
            .filter(corner -> corner.x() > 0 && corner.x() < terrainMap.numCols() - 1)
            .filter(corner -> corner.y() > 0 && corner.y() < terrainMap.numRows() - 1)
            .map(corner -> TileMapPath.build(terrainMap, explored, corner, LEFT))
            .forEach(path -> buildWallsAlongPath(parent, path));

        // Paths starting at left and right maze border (over and under tunnel ends)
        var handlesLeft = new ArrayList<Vector2i>();
        var handlesRight = new ArrayList<Vector2i>();
        for (int row = 0; row < terrainMap.numRows(); ++row) {
            if (terrainMap.get(row, 0) == Tiles.TUNNEL) {
                handlesLeft.add(new Vector2i(0, row - 1));
                handlesLeft.add(new Vector2i(0, row + 1));
            }
            if (terrainMap.get(row, terrainMap.numCols() - 1) == Tiles.TUNNEL) {
                handlesRight.add(new Vector2i(terrainMap.numCols() - 1, row - 1));
                handlesRight.add(new Vector2i(terrainMap.numCols() - 1, row + 1));
            }
        }

        handlesLeft.stream()
            .filter(handle -> !explored.get(terrainMap.index(handle)))
            .map(handle -> TileMapPath.build(terrainMap, explored, handle, RIGHT))
            .forEach(path -> buildWallsAlongPath(parent, path));

        handlesRight.stream()
            .filter(handle -> !explored.get(terrainMap.index(handle)))
            .map(handle -> TileMapPath.build(terrainMap, explored, handle, LEFT))
            .forEach(path -> buildWallsAlongPath(parent, path));

        // Closed outer wall?
        terrainMap.tiles(Tiles.DCORNER_NW)
            .filter(corner -> corner.x() == 0)
            .filter(corner -> !explored.get(terrainMap.index(corner)))
            .map(corner -> TileMapPath.build(terrainMap, explored, corner, LEFT))
            .forEach(path -> buildWallsAlongPath(parent, path));
    }

    private void buildWallsAlongPath(Group parent, TileMapPath tileMapPath) {
        Vector2i wallStart = tileMapPath.startTile;
        Vector2i wallEnd = wallStart;
        Direction prevDir = null;
        for (int i = 0; i < tileMapPath.directions.size(); ++i) {
            var dir = tileMapPath.directions.get(i);
            if (prevDir != dir) {
                parent.getChildren().add(createWall(wallStart, wallEnd, wallHeightPy, wallFillMaterialPy));
                wallStart = wallEnd;
            }
            wallEnd = wallEnd.plus(dir.vector());
            prevDir = dir;
        }
        parent.getChildren().add(createWall(wallStart, wallEnd, wallHeightPy, wallFillMaterialPy));
    }

    private Node createWall(Vector2i first, Vector2i second, DoubleProperty heightPy, ObjectProperty<PhongMaterial> fillMaterialPy) {
        if (first.y() == second.y()) {
            // horizontal
            Logger.trace("Hor. Wall from {} to {}", first, second);
            if (first.x() > second.x()) {
                var tmp = first;
                first = second;
                second = tmp;
            }
            double w = (second.x() - first.x()) * 8 + WALL_THICKNESS;
            double m = (first.x() + second.x()) * 4;

            var base = new Box(w, WALL_THICKNESS, heightPy.get());
            base.materialProperty().bind(fillMaterialPy);
            base.depthProperty().bind(heightPy);
            base.drawModeProperty().bind(PY_3D_DRAW_MODE);
            base.setTranslateX(m + 4);
            base.setTranslateY(first.y() * 8 + 4);
            base.translateZProperty().bind(heightPy.multiply(-0.5));

            var top = new Box(w, WALL_THICKNESS, WALL_STROKE_THICKNESS);
            top.materialProperty().bind(wallStrokeMaterialPy);
            top.translateXProperty().bind(base.translateXProperty());
            top.translateYProperty().bind(base.translateYProperty());
            top.translateZProperty().bind(heightPy.multiply(-1).subtract(WALL_STROKE_THICKNESS));

            return new Group(base, top);
        }
        else if (first.x() == second.x()){
            // vertical
            Logger.trace("Vert. Wall from {} to {}", first, second);
            if (first.y() > second.y()) {
                var tmp = first;
                first = second;
                second = tmp;
            }
            double h = (second.y() - first.y()) * 8;
            double m = (first.y() + second.y()) * 4;

            var base = new Box(WALL_THICKNESS, h, heightPy.get());
            base.materialProperty().bind(fillMaterialPy);
            base.depthProperty().bind(heightPy);
            base.drawModeProperty().bind(PY_3D_DRAW_MODE);
            base.setTranslateX(first.x() * 8 + 4);
            base.setTranslateY(m + 4);
            base.translateZProperty().bind(heightPy.multiply(-0.5));

            var top = new Box(WALL_THICKNESS, h, WALL_STROKE_THICKNESS);
            top.materialProperty().bind(wallStrokeMaterialPy);
            top.translateXProperty().bind(base.translateXProperty());
            top.translateYProperty().bind(base.translateYProperty());
            top.translateZProperty().bind(heightPy.multiply(-1).subtract(WALL_STROKE_THICKNESS));

            return new Group(base, top);
        }
        throw new IllegalArgumentException(String.format("Cannot build wall between tiles %s and %s", first, second));
    }

    private void createLivesCounter3D() {
        var theme = context.theme();
        livesCounter3D = new LivesCounter3D(
            theme.get("livescounter.entries"),
            theme.get("livescounter.pillar.color"),
            theme.get("livescounter.pillar.height"),
            theme.get("livescounter.plate.color"),
            theme.get("livescounter.plate.thickness"),
            theme.get("livescounter.plate.radius"),
            theme.get("livescounter.light.color"));
        livesCounter3D.setTranslateX(2 * TS);
        livesCounter3D.setTranslateY(2 * TS);
        livesCounter3D.drawModePy.bind(PY_3D_DRAW_MODE);
        for (int i = 0; i < livesCounter3D.maxLives(); ++i) {
            var pac3D = switch (context.game().variant()) {
                case MS_PACMAN -> Pac3D.createFemalePac3D(context.theme(), null, theme.get("livescounter.pac.size"));
                case PACMAN, PACMAN_XXL -> Pac3D.createMalePac3D(context.theme(), null,  theme.get("livescounter.pac.size"));
            };
            livesCounter3D.addItem(pac3D, true);
        }
    }

    public void createLevelCounter3D() {
        World world = context.game().world();
        double spacing = 2 * TS;
        // this is the *right* edge of the level counter:
        levelCounterGroup.setTranslateX(world.numCols() * TS - spacing);
        levelCounterGroup.setTranslateY(spacing);
        levelCounterGroup.setTranslateZ(-6);
        levelCounterGroup.getChildren().clear();
        int n = 0;
        for (byte symbol : context.game().levelCounter()) {
            Box cube = new Box(TS, TS, TS);
            cube.setTranslateX(-n * spacing);
            cube.setTranslateZ(-HTS);
            levelCounterGroup.getChildren().add(cube);

            var material = new PhongMaterial(Color.WHITE);
            switch (context.game().variant()) {
                case MS_PACMAN -> {
                    var ss = PacManGames2dUI.SS_MS_PACMAN;
                    material.setDiffuseMap(ss.subImage(ss.bonusSymbolSprite(symbol)));
                }
                case PACMAN, PACMAN_XXL -> {
                    var ss = PacManGames2dUI.SS_PACMAN;
                    material.setDiffuseMap(ss.subImage(ss.bonusSymbolSprite(symbol)));
                }
            }
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
    }

    private void createMessage3D() {
        message3D = new Message3D();
        message3D.beginBatch();
        message3D.setBorderColor(Color.WHITE);
        message3D.setTextColor(Color.YELLOW);
        message3D.setFont(context.theme().font("font.arcade", 6));
        message3D.setVisible(false);
        message3D.endBatch();
    }

    public void showMessage(String text, double displaySeconds, double x, double y) {
        message3D.setText(text);
        message3D.setVisible(true);
        message3D.rotate(Rotate.X_AXIS, 90);
        double radius = 0.5 * message3D.getBoundsInLocal().getHeight();
        message3D.setTranslateX(x);
        message3D.setTranslateY(y);
        message3D.setTranslateZ(radius);
        var moveOutAnimation = new TranslateTransition(Duration.seconds(1), message3D);
        moveOutAnimation.setToZ(-(radius + 0.5 * wallHeightPy.get()));
        var moveInAnimation = new TranslateTransition(Duration.seconds(0.5), message3D);
        moveInAnimation.setDelay(Duration.seconds(displaySeconds));
        moveInAnimation.setToZ(radius);
        moveInAnimation.setOnFinished(e -> message3D.setVisible(false));
        new SequentialTransition(moveOutAnimation, moveInAnimation).play();
    }

    public void replaceBonus3D(Bonus bonus) {
        checkNotNull(bonus);
        if (bonus3D != null) {
            worldGroup.getChildren().remove(bonus3D);
        }
        switch (context.game().variant()) {
            case MS_PACMAN -> {
                var ss = PacManGames2dUI.SS_MS_PACMAN;
                bonus3D = new Bonus3D(bonus,
                    ss.subImage(ss.bonusSymbolSprite(bonus.symbol())), ss.subImage(ss.bonusValueSprite(bonus.symbol())));
            }
            case PACMAN, PACMAN_XXL -> {
                var ss = PacManGames2dUI.SS_PACMAN;
                bonus3D = new Bonus3D(bonus,
                    ss.subImage(ss.bonusSymbolSprite(bonus.symbol())), ss.subImage(ss.bonusValueSprite(bonus.symbol())));
            }
        }
        bonus3D.showEdible();
        worldGroup.getChildren().add(bonus3D);
    }

    private Ghost3D createGhost3D(Ghost ghost) {
        return new Ghost3D(
            context.theme().get("model3D.ghost"),
            context.theme(),
            ghost,
            context.game().level().orElseThrow().numFlashes(),
            GHOST_SIZE);
    }

    private void addEnergizerAnimation(World world, Energizer3D energizer3D) {
        var squirting = new Squirting() {
            @Override
            protected boolean reachedFinalPosition(Drop drop) {
                return drop.getTranslateZ() >= -1 && world.containsPoint(drop.getTranslateX(), drop.getTranslateY());
            }
        };
        squirting.setOrigin(energizer3D.root());
        squirting.setDropCountMin(15);
        squirting.setDropCountMax(45);
        squirting.setDropMaterial(coloredMaterial(foodColorPy.get().desaturate()));
        squirting.setOnFinished(e -> getChildren().remove(squirting.root()));
        getChildren().add(squirting.root());

        energizer3D.setEatenAnimation(squirting);
    }

    public void eat(Eatable3D eatable3D) {
        checkNotNull(eatable3D);
        if (eatable3D instanceof Energizer3D energizer3D) {
            energizer3D.stopPumping();
        }
        // Delay hiding of pellet for some milliseconds because in case the player approaches the pellet from the right,
        // the pellet disappears too early (collision by tile equality is too coarse).
        var hiding = doAfterSec(0.05, () -> eatable3D.root().setVisible(false));
        var energizerExplosion = eatable3D.getEatenAnimation().orElse(null);
        if (energizerExplosion != null && PY_3D_ENERGIZER_EXPLODES.get()) {
            new SequentialTransition(hiding, energizerExplosion).play();
        } else {
            hiding.play();
        }
    }

    public Transition createLevelRotateAnimation(double seconds) {
        var rotation = new RotateTransition(Duration.seconds(seconds), this);
        rotation.setAxis(RND.nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS);
        rotation.setFromAngle(0);
        rotation.setToAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        return rotation;
    }

    public Animation createMazeDisappearAnimation(double seconds) {
        return new Transition() {

            private final DoubleProperty valuePy = new SimpleDoubleProperty();

            {
                setRate(-1); // value goes 1 -> 0
                setCycleDuration(Duration.seconds(seconds));
                setInterpolator(Interpolator.EASE_BOTH);
                setOnFinished(e -> {
                    mazeGroup.setVisible(false);
                    wallHeightPy.bind(PY_3D_WALL_HEIGHT);
                });
            }

            @Override
            public void play() {
                wallHeightPy.bind(valuePy.multiply(wallHeightPy.get()));
                super.play();
            }

            @Override
            protected void interpolate(double t) {
                valuePy.set(t);
            }
        };
    }

    public Animation createMazeFlashingAnimation(int numFlashes) {
        if (numFlashes == 0) {
            return pauseSec(1.0);
        }
        return new Transition() {
            private final DoubleProperty elongationPy = new SimpleDoubleProperty();
            {
                setCycleDuration(Duration.seconds(0.33));
                setCycleCount(numFlashes);
                setOnFinished(e -> wallHeightPy.bind(PY_3D_WALL_HEIGHT));
                setInterpolator(Interpolator.LINEAR);
            }

            @Override
            public void play() {
                wallHeightPy.bind(elongationPy.multiply(wallHeightPy.get()));
                super.play();
            }

            @Override
            protected void interpolate(double t) {
                // t = [0, 1] is mapped to the interval [pi/2, 3*pi/2]. The value of the sin-function in that interval
                // starts at 1, goes to 0 and back to 1
                elongationPy.set(0.5 * (1 + Math.sin(PI * (2*t + 0.5))));
            }
        };
    }

    public void updateHouseState() {
        var game = context.game();
        var house = game.world().house();
        boolean houseUsed = game.ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .anyMatch(Ghost::isVisible);
        boolean houseOpen = game.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .filter(ghost -> ghost.position().euclideanDistance(house.door().entryPosition()) <= 1.5 * TS)
            .anyMatch(Ghost::isVisible);
        houseLight.setLightOn(houseUsed);
        if (houseOpen) {
            doorWings3D().map(DoorWing3D::traversalAnimation).forEach(Transition::play);
        }
    }

    public Pac3D pac3D() {
        return pac3D;
    }

    public List<Ghost3D> ghosts3D() {
        return ghosts3D;
    }

    public Optional<Bonus3D> bonus3D() {
        return Optional.ofNullable(bonus3D);
    }

    public LivesCounter3D livesCounter3D() {
        return livesCounter3D;
    }

    public Stream<Pellet3D> pellets3D() {
        return pellets3D.stream();
    }

    public Stream<Energizer3D> energizers3D() {
        return energizers3D.stream();
    }

    public void startEnergizerAnimation() {
        energizers3D.forEach(Energizer3D::startPumping);
    }

    public void stopEnergizerAnimation() {
        energizers3D.forEach(Energizer3D::stopPumping);
    }

    public Optional<Energizer3D> energizer3D(Vector2i tile) {
        checkTileNotNull(tile);
        return energizers3D().filter(e3D -> e3D.tile().equals(tile)).findFirst();
    }

    public Optional<Pellet3D> pellet3D(Vector2i tile) {
        checkTileNotNull(tile);
        return pellets3D().filter(p3D -> p3D.tile().equals(tile)).findFirst();
    }
}