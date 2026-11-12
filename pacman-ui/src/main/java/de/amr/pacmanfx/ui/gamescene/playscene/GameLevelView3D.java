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
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.CommonGameSystems;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.*;
import de.amr.pacmanfx.ui.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.GameVariantUIConfig;
import de.amr.pacmanfx.ui.entities3D.bonus.anim.Bonus3DAnimationID;
import de.amr.pacmanfx.ui.entities3D.bonus.comp.Bonus3DSettings;
import de.amr.pacmanfx.ui.entities3D.bonus.comp.BonusView3D;
import de.amr.pacmanfx.ui.entities3D.ghost.comp.GhostSettings;
import de.amr.pacmanfx.ui.entities3D.ghost.comp.GhostView3D;
import de.amr.pacmanfx.ui.entities3D.house.HouseView3D;
import de.amr.pacmanfx.ui.entities3D.levelcounter.LevelCounter3DFactory;
import de.amr.pacmanfx.ui.entities3D.levelcounter.LevelCounterView3D;
import de.amr.pacmanfx.ui.entities3D.livescounter.LivesCounterView3D;
import de.amr.pacmanfx.ui.entities3D.livescounter.LivesCounterView3DSystem;
import de.amr.pacmanfx.ui.entities3D.pac.PacView3D;
import de.amr.pacmanfx.ui.entities3D.world.EnergizerView3D;
import de.amr.pacmanfx.ui.entities3D.world.NumberBox3D;
import de.amr.pacmanfx.ui.entities3D.world.PelletView3D;
import de.amr.pacmanfx.ui.gamescene.view3d.animation.HideGhost3DRiseNumberBoxAnimation;
import de.amr.pacmanfx.ui.settings.world.Energizer3DSettings;
import de.amr.pacmanfx.ui.settings.world.Pellet3DSettings;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import de.amr.pacmanfx.uilib.view3d.PacSettings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.basics.TileDimension.TS;
import static de.amr.basics.TileDimension.tilesPx;
import static de.amr.basics.util.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

/**
 * Represents the 3D visualization of a Pac-Man game level.
 */
public class GameLevelView3D implements ChangeListener<DrawMode>, DisposableGraphicsObject {

    private final Group root = new Group();

    private final GameLevel level;

    private final GameVariantUIConfig uiConfig;

    private final PointLight ghostHunterLight = new PointLight();

    private final Map<Vector2i, EnergizerView3D> energizerViews3D = new HashMap<>();

    private final Map<Vector2i, PelletView3D> pelletViews3D = new HashMap<>();

    private WorldMapView3D mapView3D;

    private final GameViewModel viewModel;

    public GameLevelView3D(GameContext game, GameLevel level, GameViewModel viewModel, GameVariantUIConfig uiConfig) {
        requireNonNull(game);
        this.level = requireNonNull(level);
        this.viewModel = requireNonNull(viewModel);
        this.uiConfig = requireNonNull(uiConfig);

        final House house = level.entitySet().theOne(House.class);
        final Pac pac = level.entitySet().pac();
        final List<Ghost> ghosts = level.entitySet().ghosts().toList();
        final LevelCounter levelCounter = game.session().hud().levelCounter();
        final LivesCounter livesCounter = game.session().hud().livesCounter();
        final WorldMapColorScheme colorScheme = uiConfig.renderConfig().colorScheme(level.worldMap(), uiConfig.worldSettings());

        createMapView3D(level.worldMap(), house, colorScheme);
        createFoodView3D();
        new HouseFactory3D().createHouse3D(house, uiConfig.worldSettings().house(), colorScheme);
        createPacView3D(game.systems(), pac, uiConfig.worldSettings().pac());
        createGhostsView3D(game.systems(), ghosts, uiConfig.worldSettings().ghosts());

        replaceLevelCounterView3D(level, levelCounter);
        replaceLivesCounterView3D(game, livesCounter);

        arrangeChildren(pac, ghosts, livesCounter, house);

        root.setMouseTransparent(true); // this increases performance they say...
    }

    @Override
    public void changed(ObservableValue<? extends DrawMode> observable, DrawMode oldValue, DrawMode newValue) {
        Ufx.setDrawMode(level.entitySet().pac().assertComponent(PacView3D.class).root(), newValue);
        level.entitySet().ghosts().forEach(ghost -> Ufx.setDrawMode(ghost.assertComponent(GhostView3D.class).root(), newValue));
        Ufx.setDrawMode(mapView3D.root(), newValue);
    }

    @Override
    public void dispose() {
        if (mapView3D != null) {
            mapView3D.dispose();
        }
        cleanupGroup(root, true);
    }

    // Public accessors

    public Group root() {
        return root;
    }

    public WorldMapView3D maze3D() {
        return mapView3D;
    }

    public GameLevel level() {
        return level;
    }

    public PointLight ghostHunterLight() {
        return ghostHunterLight;
    }

    public Stream<EnergizerView3D> energizers3D() {
        return energizerViews3D.values().stream();
    }

    public Optional<EnergizerView3D> energizer3DAt(Vector2i tile) {
        return Optional.ofNullable(energizerViews3D.get(tile));
    }

    public Stream<PelletView3D> pellets3D() {
        return pelletViews3D.values().stream();
    }

    public Optional<PelletView3D> pellet3DAtTile(Vector2i tile) {
        return Optional.ofNullable(pelletViews3D.get(tile));
    }

    public void cleanupFoodAndParticles() {
        energizerViews3D.values().forEach(EnergizerView3D::hide);
        // Hide 3D food explicitly (handles cheat-eat-all case)
        pelletViews3D.values().forEach(pelletView3D -> pelletView3D.root().setVisible(false));
        mapView3D.particlesGroup().getChildren().clear();
    }

    public void ensureBonus3DViewAddedToSceneGraph(Bonus bonus, AnimationRegistry animationRegistry) {
        if (!bonus.hasComponent(BonusView3D.class)) {
            final var view3D = createBonusView3D(bonus, animationRegistry);
            root.getChildren().add(view3D.root());
        }
    }

    public void addKilledGhostNumberBox(Ghost ghost, GameVariantUIConfig uiConfig, int killIndex) {
        final Image numberImage = uiConfig.renderConfig().createGhostPointsImage(killIndex);
        final NumberBox3D numberBox = new NumberBox3D(numberImage);

        final GhostView3D ghost3DView = ghost.assertComponent(GhostView3D.class);
        numberBox.setTranslateX(ghost3DView.root().getTranslateX());
        numberBox.setTranslateY(ghost3DView.root().getTranslateY());
        numberBox.setTranslateZ(ghost3DView.root().getTranslateZ());
        root.getChildren().add(numberBox);

        //TODO move elsewhere (animation system)
        final double risingHeight = (killIndex + 1) * 12;
        final var animation = new HideGhost3DRiseNumberBoxAnimation(ghost3DView, numberBox, risingHeight);
        animation.delegate().setOnFinished(_ -> root.getChildren().remove(numberBox));
        animation.replay();
    }

    public void replaceLevelCounterView3D(GameLevel level, LevelCounter levelCounter) {
        levelCounter.optComponent(LevelCounterView3D.class).ifPresent(
            oldView3D -> root.getChildren().remove(oldView3D.root()));

        final LevelCounterView3D view3D = new LevelCounterView3D();
        levelCounter.setComponent(LevelCounterView3D.class, view3D);

        final Group levelCounterRoot = LevelCounter3DFactory.buildLevelCounter3D(
            levelCounter,
            uiConfig.worldSettings().levelCounter(),
            uiConfig.renderConfig()
        );
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        levelCounterRoot.setTranslateX(tilesPx(terrain.numCols() - 2));
        levelCounterRoot.setTranslateY(tilesPx(2));
        levelCounterRoot.setTranslateZ(-uiConfig.worldSettings().levelCounter().elevation());

        view3D.setRoot(levelCounterRoot);
        root.getChildren().add(levelCounterRoot);
    }

    // Private area, no trespassing!

    private void createMapView3D(WorldMap worldMap, House house, WorldMapColorScheme colorScheme) {
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

    private void createFoodView3D() {
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
                    energizerViews3D.put(tile, createEnergizerView3D(tile, energizerZ, foodMaterial));
                } else {
                    pelletViews3D.put(tile, createPelletView3D(tile, pelletZ, foodMaterial));
                }
            });
    }

    private PelletView3D createPelletView3D(Vector2i tile, double z, PhongMaterial foodMaterial) {
        final PelletView3D pelletView3D = uiConfig.factory3D().createPellet3D(uiConfig.worldSettings().pellet(), foodMaterial);
        pelletView3D.setLocation(tile, z);
        return pelletView3D;
    }

    private EnergizerView3D createEnergizerView3D(Vector2i tile, double z, PhongMaterial foodMaterial) {
        final EnergizerView3D energizerView3D = uiConfig.factory3D().createEnergizer3D(
            uiConfig.worldSettings().energizer(), foodMaterial);
        energizerView3D.setLocation(tile, z);
        return energizerView3D;
    }

    private BonusView3D createBonusView3D(Bonus bonus, AnimationRegistry animationRegistry) {
        final Bonus3DSettings config = uiConfig.worldSettings().bonus();
        final GameVariantRenderConfig renderConfig = uiConfig.renderConfig();
        final BonusView3D view3D = new BonusView3D(
            renderConfig.createBonusSymbolImage(bonus.data().symbolCode()),
            config.symbolWidth(),
            renderConfig.createBonusPointsImage(bonus.data().symbolCode()),
            config.pointsWidth()
        );
        bonus.setComponent(BonusView3D.class, view3D);

        //TODO move elsewhere
        animationRegistry.register(Bonus3DAnimationID.BONUS_EATEN, view3D.eatenAnimation());

        return view3D;
    }

    private void createPacView3D(CommonGameSystems systems, Pac pac, PacSettings settings) {
        uiConfig.factory3D().createPac3D(systems, pac, settings);
        final PacView3D pac3D = pac.assertComponent(PacView3D.class);
        pac3D.drawModeProperty().bind(viewModel.common3DSettings().drawModeProperty());
    }

    private void createGhostsView3D(CommonGameSystems systems, List<Ghost> ghosts, List<GhostSettings> settings) {
        ghosts.forEach(ghost -> {
            final var ghostSettings = settings.get(ghost.personality().ordinal());
            uiConfig.factory3D().createGhost3D(systems, ghost, ghostSettings);
            ghost.assertComponent(GhostView3D.class).drawModeProperty().bind(viewModel.common3DSettings().drawModeProperty());
        });
    }

    private void replaceLivesCounterView3D(GameContext game, LivesCounter livesCounter) {
        final LivesCounterView3DSystem system = game.systems().assertSystem(LivesCounterView3DSystem.class);
        livesCounter.optComponent(LivesCounterView3D.class).ifPresent(oldView3D -> {
            system.stopTrackingPac(oldView3D);
            livesCounter.removeComponent(LivesCounterView3D.class);
        });
        final LivesCounterView3D view3D = system.createLivesCounterView3D(uiConfig.factory3D(), uiConfig.worldSettings());
        view3D.root().setTranslateX(2 * TS);
        view3D.root().setTranslateY(2 * TS);
        livesCounter.setComponent(LivesCounterView3D.class, view3D);
    }

    private void arrangeChildren(Pac pac, List<Ghost> ghosts, LivesCounter livesCounter, House house) {
        final var children = root.getChildren();

        children.clear();

        // Add-order matters for correct transparency!

        final LivesCounterView3D livesCounterView3D = livesCounter.assertComponent(LivesCounterView3D.class);
        children.add(livesCounterView3D.root());

        final PacView3D pacView3D = pac.assertComponent(PacView3D.class);
        children.add(pacView3D.root());
        children.add(pacView3D.powerLight());

        for (Ghost ghost: ghosts) {
            final GhostView3D ghostView3D = ghost.assertComponent(GhostView3D.class);
            children.add(ghostView3D.root());
        }

        for (EnergizerView3D energizerView3D : energizerViews3D.values()) {
            children.add(energizerView3D.root());
        }

        for (PelletView3D pelletView3D : pelletViews3D.values()) {
            children.add(pelletView3D.root());
        }

        children.add(mapView3D.particlesGroup());
        children.add(mapView3D.root());

        children.add(ghostHunterLight);

        final HouseView3D houseView3D = house.assertComponent(HouseView3D.class);
        children.add(houseView3D.root());
        children.add(houseView3D.doors());
    }
}