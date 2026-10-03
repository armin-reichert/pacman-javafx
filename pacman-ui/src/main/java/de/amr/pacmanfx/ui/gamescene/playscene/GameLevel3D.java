/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.playscene;

import de.amr.basics.ecs.system.PositionSystem;
import de.amr.basics.math.Vector2i;
import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.basics.ui.assets.DisposableGraphicsObject;
import de.amr.basics.ui.entities.hud.levelCounter.LevelCounter;
import de.amr.basics.ui.entities.hud.livescounter.LivesCounter;
import de.amr.basics.ui.entities.props.messageview.MessageView;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.FoodLayer;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapColorScheme;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.ui.GameSystems3D;
import de.amr.pacmanfx.ui.entities3D.levelcounter.system.LevelCounter3DViewSystem;
import de.amr.pacmanfx.ui.entities3D.livescounter.comp.LivesCounter3DViewComp;
import de.amr.pacmanfx.ui.gamescene.d3.animation.HideGhost3DRiseNumberBoxAnimation;
import de.amr.pacmanfx.ui.settings.world.Energizer3DSettings;
import de.amr.pacmanfx.ui.settings.world.Pellet3DSettings;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import de.amr.pacmanfx.ui.entities3D.bonus.anim.Bonus3DAnimationID;
import de.amr.pacmanfx.ui.entities3D.bonus.comp.Bonus3DSettings;
import de.amr.pacmanfx.ui.entities3D.bonus.comp.Bonus3DViewComp;
import de.amr.pacmanfx.ui.entities3D.ghost.comp.Ghost3DViewComp;
import de.amr.pacmanfx.ui.entities3D.ghost.comp.GhostSettings;
import de.amr.pacmanfx.ui.entities3D.house.comp.House3DViewComp;
import de.amr.pacmanfx.ui.entities3D.levelcounter.comp.LevelCounter3DAnimationComp;
import de.amr.pacmanfx.ui.entities3D.levelcounter.comp.LevelCounter3DViewComp;
import de.amr.pacmanfx.ui.entities3D.messageview.MessageView3DBuilder;
import de.amr.pacmanfx.ui.entities3D.pac.comp.Pac3DViewComp;
import de.amr.pacmanfx.uilib.view3d.PacSettings;
import de.amr.pacmanfx.ui.entities3D.world.Energizer3D;
import de.amr.pacmanfx.ui.entities3D.world.NumberBox3D;
import de.amr.pacmanfx.ui.entities3D.world.Pellet3D;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.basics.TileDimension.TS;
import static de.amr.basics.util.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

/**
 * Represents the 3D visualization of a Pac-Man game level.
 */
public class GameLevel3D implements DisposableGraphicsObject {

    private final Group root = new Group();

    private final GameLevel level;

    private final GameVariantUIConfig uiConfig;

    private final PointLight ghostHunterLight = new PointLight();

    private final Map<Vector2i, Energizer3D> energizerViews3D = new HashMap<>();

    private final Map<Vector2i, Pellet3D> pelletViews3D = new HashMap<>();

    private WorldMapView3D mapView3D;

    private PlayScene3DAnimationSystem animationManager;

    private final GameViewModel viewModel;

    public GameLevel3D(GameContext game, AnimationRegistry animationRegistry, GameViewModel viewModel, GameVariantUIConfig uiConfig) {
        requireNonNull(game);
        requireNonNull(animationRegistry);
        this.viewModel = requireNonNull(viewModel);
        this.uiConfig = requireNonNull(uiConfig);

        final GameSession session = game.session();
        this.level = session.level();

        final WorldMap worldMap = level.worldMap();
        final House house = level.entitySet().entities().theOne(House.class);
        final Pac pac = level.entitySet().pac();
        final List<Ghost> ghosts = level.entitySet().ghosts().toList();

        final LevelCounter levelCounter = session.hud().levelCounter();
        final LivesCounter livesCounter = session.hud().livesCounter();
        final MessageView messageView   = level.entitySet().entities().theOne(MessageView.class);

        final WorldMapColorScheme colorScheme = uiConfig.renderConfig().colorScheme(level.worldMap(), uiConfig.worldSettings());

        createMaze3DView(worldMap, house, colorScheme);
        createFood3DViews();
        new HouseFactory3D().createHouse3D(house, uiConfig.worldSettings().house(), colorScheme);
        createPac3DView(pac, uiConfig.worldSettings().pac());
        createGhost3DViews(ghosts, uiConfig.worldSettings().ghosts());
        createLevelCounter3DView(levelCounter, animationRegistry);
        createLivesCounter3DView(livesCounter);
        MessageView3DBuilder.createAnim3D(messageView, animationRegistry);

        composeLevel3D(pac, ghosts, livesCounter, house);

        root.setMouseTransparent(true); // this increases performance they say...
    }

    public Group root() {
        return root;
    }

    public void setAnimationManager(PlayScene3DAnimationSystem animationManager) {
        this.animationManager = requireNonNull(animationManager);
    }

    @Override
    public void dispose() {
        if (mapView3D != null) {
            mapView3D.dispose();
        }
        cleanupGroup(root, true);
    }

    // Public accessors

    public PlayScene3DAnimationSystem animationManager() {
        return animationManager;
    }

    public WorldMapView3D maze3D() {
        return mapView3D;
    }

    public Optional<GameSoundEffects> optSoundEffects() {
        return uiConfig.optSoundEffects();
    }

    public GameLevel level() {
        return level;
    }

    public PointLight ghostHunterLight() {
        return ghostHunterLight;
    }

    public Stream<Energizer3D> energizers3D() {
        return energizerViews3D.values().stream();
    }

    public Optional<Energizer3D> energizer3DAt(Vector2i tile) {
        return Optional.ofNullable(energizerViews3D.get(tile));
    }

    public Stream<Pellet3D> pellets3D() {
        return pelletViews3D.values().stream();
    }

    public Optional<Pellet3D> pellet3DAtTile(Vector2i tile) {
        return Optional.ofNullable(pelletViews3D.get(tile));
    }

    public void cleanupFoodAndParticles() {
        energizerViews3D.values().forEach(Energizer3D::hide);
        // Hide 3D food explicitly (handles cheat-eat-all case)
        pelletViews3D.values().forEach(pellet3D -> pellet3D.root().setVisible(false));
        mapView3D.particlesGroup().getChildren().clear();
    }

    public void setDrawMode(DrawMode drawMode) {
        requireNonNull(drawMode);
        Ufx.setDrawMode(level.entitySet().pac().assertComponent(Pac3DViewComp.class).root(), drawMode);
        level.entitySet().ghosts().forEach(ghost -> Ufx.setDrawMode(ghost.assertComponent(Ghost3DViewComp.class).root(), drawMode));
        Ufx.setDrawMode(mapView3D.root(), drawMode);
    }

    public void ensureBonus3DViewAddedToSceneGraph(Bonus bonus) {
        if (!bonus.hasComponent(Bonus3DViewComp.class)) {
            final var view3D = createBonusView3D(bonus);
            root.getChildren().add(view3D.root());
        }
    }

    public void addKilledGhostNumberBox(Ghost ghost, GameVariantUIConfig uiConfig, int killIndex) {
        final Image numberImage = uiConfig.renderConfig().createGhostPointsImage(killIndex);
        final NumberBox3D numberBox = new NumberBox3D(numberImage);

        final Ghost3DViewComp ghost3DView = ghost.assertComponent(Ghost3DViewComp.class);
        numberBox.setTranslateX(ghost3DView.root().getTranslateX());
        numberBox.setTranslateY(ghost3DView.root().getTranslateY());
        numberBox.setTranslateZ(ghost3DView.root().getTranslateZ());
        root.getChildren().add(numberBox);

        //TODO move elsewhere (animation system)
        final double risingHeight = (killIndex + 1) * 12;
        final var animation = new HideGhost3DRiseNumberBoxAnimation(ghost3DView, numberBox, risingHeight);
        animation.delegate().setOnFinished(_ -> root.getChildren().remove(numberBox));
        animation.playFromStart();
    }

    // Private area, no trespassing!

    private void createMaze3DView(WorldMap worldMap, House house, WorldMapColorScheme colorScheme) {
        mapView3D = new WorldMapView3DFactory().createMapView3D(
            p -> house.contains(PositionSystem.computeTileAt(p)),
            worldMap.terrainLayer(),
            uiConfig.worldSettings(),
            colorScheme);

        mapView3D.drawModeProperty()      .bind(viewModel.common3DSettings().drawModeProperty());
        mapView3D.wallOpacityProperty()   .bind(viewModel.maze3DSettings().wallOpacityProperty());
        mapView3D.wallBaseHeightProperty().bind(viewModel.maze3DSettings().wallHeightProperty());
        mapView3D.floorColorProperty()    .bind(viewModel.maze3DSettings().floorColorProperty());
    }

    private void createFood3DViews() {
        final GenericWorldMapColorScheme colorScheme = uiConfig.renderConfig().colorScheme(level.worldMap(), uiConfig.worldSettings());
        final FoodLayer foodLayer = level.worldMap().foodLayer();

        final PhongMaterial foodMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.pellet()));

        final Pellet3DSettings pelletConfig3D = uiConfig.worldSettings().pellet();
        final double pelletZ = mapView3D.floorTop() - pelletConfig3D.floorElevation();

        final Energizer3DSettings energizerConfig3D = uiConfig.worldSettings().energizer();
        final double energizerZ = mapView3D.floorTop() - energizerConfig3D.floorElevation();

        foodLayer.tiles()
            .filter(level.food()::hasFoodAtTile)
            .forEach(tile -> {
                if (foodLayer.isEnergizerTile(tile)) {
                    energizerViews3D.put(tile, createEnergizer3D(tile, energizerZ, foodMaterial));
                } else {
                    pelletViews3D.put(tile, createPellet3D(tile, pelletZ, foodMaterial));
                }
            });
    }

    private Pellet3D createPellet3D(Vector2i tile, double z, PhongMaterial foodMaterial) {
        final Pellet3D pellet3D = uiConfig.factory3D().createPellet3D(uiConfig.worldSettings().pellet(), foodMaterial);
        pellet3D.setLocation(tile, z);
        return pellet3D;
    }

    private Energizer3D createEnergizer3D(Vector2i tile, double z, PhongMaterial foodMaterial) {
        final Energizer3D energizer3D = uiConfig.factory3D().createEnergizer3D(
            uiConfig.worldSettings().energizer(), foodMaterial);
        energizer3D.setLocation(tile, z);
        return energizer3D;
    }

    private Bonus3DViewComp createBonusView3D(Bonus bonus) {
        final Bonus3DSettings config = uiConfig.worldSettings().bonus();
        final GameVariantRenderConfig renderConfig = uiConfig.renderConfig();
        final Bonus3DViewComp view3D = new Bonus3DViewComp(
            renderConfig.createBonusSymbolImage(bonus.data().symbolCode()),
            config.symbolWidth(),
            renderConfig.createBonusPointsImage(bonus.data().symbolCode()),
            config.pointsWidth()
        );
        bonus.setComponent(Bonus3DViewComp.class, view3D);

        //TODO move elsewhere
        animationManager.registry().register(Bonus3DAnimationID.BONUS_EATEN, view3D.eatenAnimation());

        return view3D;
    }

    private void createPac3DView(Pac pac, PacSettings settings) {
        uiConfig.factory3D().createPac3D(pac, settings);
        pac.assertComponent(Pac3DViewComp.class).drawModeProperty().bind(viewModel.common3DSettings().drawModeProperty());
    }

    private void createGhost3DViews(List<Ghost> ghosts, List<GhostSettings> settings) {
        ghosts.forEach(ghost -> {
            final var ghostSettings = settings.get(ghost.personality().ordinal());
            uiConfig.factory3D().createGhost3D(ghost, ghostSettings);
            ghost.assertComponent(Ghost3DViewComp.class).drawModeProperty().bind(viewModel.common3DSettings().drawModeProperty());
        });
    }

    private void createLivesCounter3DView(LivesCounter livesCounter) {
        if (!livesCounter.hasComponent(LivesCounter3DViewComp.class)) {
            final LivesCounter3DViewComp view3D = new LivesCounter3DViewComp(uiConfig.factory3D(), uiConfig.worldSettings());
            livesCounter.setComponent(LivesCounter3DViewComp.class, view3D);
            view3D.root().setTranslateX(2 * TS);
            view3D.root().setTranslateY(2 * TS);
        }
    }

    private void createLevelCounter3DView(LevelCounter levelCounter, AnimationRegistry registry) {
        if (!levelCounter.hasComponent(LevelCounter3DViewComp.class)) {
            final LevelCounter3DViewComp view3D = new LevelCounter3DViewComp();
            levelCounter.setComponent(LevelCounter3DViewComp.class, view3D);
            levelCounter.setComponent(LevelCounter3DAnimationComp.class,
                new LevelCounter3DAnimationComp(view3D, registry));
            Logger.info("Level counter now has a 3D view and animation component");
        }
        else {
            Logger.info("Level counter already had a 3D view!");
        }

        replaceLevelCounter3D(levelCounter);
    }

    public void replaceLevelCounter3D(LevelCounter levelCounter) {
        final LevelCounter3DViewSystem viewSystem = GameSystems3D.reqSystem(LevelCounter3DViewSystem.class);
        final LevelCounter3DViewComp view3D = levelCounter.assertComponent(LevelCounter3DViewComp.class);

        final Group oldRoot = view3D.root();
        if (oldRoot != null) {
            root.getChildren().remove(oldRoot);
        }

        viewSystem.updateLevelCounter3D(levelCounter, level.worldMap(), uiConfig);
        root.getChildren().add(view3D.root());
    }

    private void composeLevel3D(Pac pac, List<Ghost> ghosts, LivesCounter livesCounter, House house) {

        // Adding-order matters for correct transparency!

        final LivesCounter3DViewComp livesCounterView3D = livesCounter.assertComponent(LivesCounter3DViewComp.class);
        root.getChildren().add(livesCounterView3D.root());

        final Pac3DViewComp pacView3D = pac.assertComponent(Pac3DViewComp.class);
        root.getChildren().add(pacView3D.root());
        root.getChildren().add(pacView3D.powerLight());

        for (Ghost ghost: ghosts) {
            final Ghost3DViewComp ghostView3D = ghost.assertComponent(Ghost3DViewComp.class);
            root.getChildren().add(ghostView3D.root());
        }

        for (Energizer3D energizerView3D : energizerViews3D.values()) {
            root.getChildren().add(energizerView3D.root());
        }

        for (Pellet3D pelletView3D : pelletViews3D.values()) {
            root.getChildren().add(pelletView3D.root());
        }

        root.getChildren().add(mapView3D.particlesGroup());
        root.getChildren().add(mapView3D.root());

        root.getChildren().add(ghostHunterLight);

        final House3DViewComp houseView3D = house.assertComponent(House3DViewComp.class);
        root.getChildren().add(houseView3D.root());
        root.getChildren().add(houseView3D.doors());
    }
}