/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.basics.math.Vector2i;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.FoodLayer;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapColorSchemeImpl;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.entities3D.levelcounter.system.LevelCounter3DViewSystem;
import de.amr.pacmanfx.ui.entities3D.livescounter.comp.LivesCounter3DViewComp;
import de.amr.pacmanfx.ui.gamescene.d3.animation.HideGhost3DRiseNumberBoxAnimation;
import de.amr.pacmanfx.ui.settings.world.Energizer3DSettings;
import de.amr.pacmanfx.ui.settings.world.Pellet3DSettings;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.vm.GameUISettingsVM;
import de.amr.pacmanfx.uilib.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.entities3D.bonus.anim.Bonus3DAnimationID;
import de.amr.pacmanfx.uilib.entities3D.bonus.comp.Bonus3DSettings;
import de.amr.pacmanfx.uilib.entities3D.bonus.comp.Bonus3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.GhostSettings;
import de.amr.pacmanfx.uilib.entities3D.house.comp.House3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.levelcounter.comp.LevelCounter3DAnimationComp;
import de.amr.pacmanfx.uilib.entities3D.levelcounter.comp.LevelCounter3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.messageview.MessageView3DBuilder;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.PacSettings;
import de.amr.pacmanfx.uilib.entities3D.world.Energizer3D;
import de.amr.pacmanfx.uilib.entities3D.world.NumberBox3D;
import de.amr.pacmanfx.uilib.entities3D.world.Pellet3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.basics.util.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

/**
 * Represents the 3D visualization of a Pac-Man game level.
 */
public class GameLevel3D extends Group implements DisposableGraphicsObject {

    private final GameLevel level;

    private final GameVariantUIConfig gameVariantConfig;

    private final PointLight ghostHunterLight = new PointLight();

    private final Map<Vector2i, Energizer3D> energizer3DByTile = new HashMap<>();

    private final Map<Vector2i, Pellet3D> pellet3DByTile = new HashMap<>();

    private Maze3D maze3D;

    private GameLevel3DAnimationManager animationManager;

    public GameLevel3D(GameContext game, GameLevel level, AnimationRegistry registry, GameUISettingsVM viewModel, GameVariantUIConfig gameVariantConfig) {
        requireNonNull(game);

        this.level = requireNonNull(level);
        this.gameVariantConfig = requireNonNull(gameVariantConfig);

        createMaze3D(viewModel);
        createFood3D();
        createPac3D(viewModel);
        createGhosts3D(viewModel);
        createLevelCounter3D(game.session().levelCounter(), registry);
        createLivesCounter3D(game.session());
        createMessageView3D(registry);
        arrangeLayout(game.session());

        setMouseTransparent(true); // this increases performance they say...
    }

    public void setAnimationManager(GameLevel3DAnimationManager animationManager) {
        this.animationManager = requireNonNull(animationManager);
    }

    @Override
    public void dispose() {
        if (maze3D != null) {
            maze3D.dispose();
        }
        cleanupGroup(this, true);
    }

    // Public accessors

    public GameLevel3DAnimationManager animationManager() {
        return animationManager;
    }

    public Maze3D maze3D() {
        return maze3D;
    }

    public Optional<GameSoundEffects> optSoundEffects() {
        return gameVariantConfig.optSoundEffects();
    }

    public GameLevel level() {
        return level;
    }

    public PointLight ghostHunterLight() {
        return ghostHunterLight;
    }

    public Stream<Energizer3D> energizers3D() {
        return energizer3DByTile.values().stream();
    }

    public Optional<Energizer3D> energizer3DAt(Vector2i tile) {
        return Optional.ofNullable(energizer3DByTile.get(tile));
    }

    public Stream<Pellet3D> pellets3D() {
        return pellet3DByTile.values().stream();
    }

    public Optional<Pellet3D> pellet3DAtTile(Vector2i tile) {
        return Optional.ofNullable(pellet3DByTile.get(tile));
    }

    public void cleanupFoodAndParticles() {
        energizer3DByTile.values().forEach(Energizer3D::hide);
        // Hide 3D food explicitly (handles cheat-eat-all case)
        pellet3DByTile.values().forEach(pellet3D -> pellet3D.root().setVisible(false));
        maze3D.particlesGroup().getChildren().clear();
    }

    public void setDrawMode(DrawMode drawMode) {
        requireNonNull(drawMode);
        Ufx.setDrawMode(level.entities().pac().requireComp(Pac3DViewComp.class).root(), drawMode);
        for (var ghost : level.entities().ghosts()) {
            Ufx.setDrawMode(ghost.requireComp(Ghost3DViewComp.class).root(), drawMode);
        }
        Ufx.setDrawMode(maze3D.root(), drawMode);
    }

    public void ensureBonus3DViewAddedToSceneGraph(Bonus bonus) {
        if (!bonus.hasComp(Bonus3DViewComp.class)) {
            final var view3D = createBonusView3D(bonus);
            getChildren().add(view3D.root());
        }
    }

    public void addKilledGhostNumberBox(Ghost ghost) {
        final Factory3D factory3D = gameVariantConfig.factory3D();

        final int killIndex = level.indexInGhostKilledChain(ghost);
        final Node numberBoxNode = factory3D.createNumberBox3D(gameVariantConfig, killIndex);

        final Ghost3DViewComp ghost3DView = ghost.requireComp(Ghost3DViewComp.class);
        numberBoxNode.setTranslateX(ghost3DView.root().getTranslateX());
        numberBoxNode.setTranslateY(ghost3DView.root().getTranslateY());
        numberBoxNode.setTranslateZ(ghost3DView.root().getTranslateZ());
        getChildren().add(numberBoxNode);

        //TODO move into animation system
        if (numberBoxNode instanceof NumberBox3D numberBox3D) {
            final double risingHeight = (killIndex + 1) * 12;
            final var animation = new HideGhost3DRiseNumberBoxAnimation(ghost3DView, numberBox3D, risingHeight);
            animation.delegate().setOnFinished(_ -> getChildren().remove(numberBoxNode));
            animation.playFromStart();
        }
    }

    // Private area, no trespassing!

    private void createMaze3D(GameUISettingsVM viewModel) {
        final WorldMapColorSchemeImpl colorScheme = gameVariantConfig.renderConfig().colorScheme(level.worldMap(), gameVariantConfig.worldSettings());
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final House house = level.entities().house();
        maze3D = gameVariantConfig.factory3D().createMaze3D(
            house,
            terrain,
            gameVariantConfig.worldSettings(),
            colorScheme
        );

        maze3D.drawModeProperty()      .bind(viewModel.common3D.drawModeProperty);
        maze3D.wallOpacityProperty()   .bind(viewModel.maze3D.wallOpacityProperty);
        maze3D.wallBaseHeightProperty().bind(viewModel.maze3D.wallHeightProperty);
        maze3D.floorColorProperty()    .bind(viewModel.maze3D.floorColorProperty);
    }

    private void createFood3D() {
        final WorldMapColorSchemeImpl colorScheme = gameVariantConfig.renderConfig().colorScheme(level.worldMap(), gameVariantConfig.worldSettings());
        final FoodLayer foodLayer = level.worldMap().foodLayer();

        final PhongMaterial foodMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.pellet()));

        final Pellet3DSettings pelletConfig3D = gameVariantConfig.worldSettings().pellet();
        final double pelletZ = maze3D.floorTop() - pelletConfig3D.floorElevation();

        final Energizer3DSettings energizerConfig3D = gameVariantConfig.worldSettings().energizer();
        final double energizerZ = maze3D.floorTop() - energizerConfig3D.floorElevation();

        foodLayer.tiles()
            .filter(level.food()::hasFoodAtTile)
            .forEach(tile -> {
                if (foodLayer.isEnergizerTile(tile)) {
                    energizer3DByTile.put(tile, createEnergizer3D(tile, energizerZ, foodMaterial));
                } else {
                    pellet3DByTile.put(tile, createPellet3D(tile, pelletZ, foodMaterial));
                }
            });
    }

    private Pellet3D createPellet3D(Vector2i tile, double z, PhongMaterial foodMaterial) {
        final Pellet3D pellet3D = gameVariantConfig.factory3D().createPellet3D(gameVariantConfig.worldSettings().pellet(), foodMaterial);
        pellet3D.setLocation(tile, z);
        return pellet3D;
    }

    private Energizer3D createEnergizer3D(Vector2i tile, double z, PhongMaterial foodMaterial) {
        final Energizer3D energizer3D = gameVariantConfig.factory3D().createEnergizer3D(
            gameVariantConfig.worldSettings().energizer(), foodMaterial);
        energizer3D.setLocation(tile, z);
        return energizer3D;
    }

    private Bonus3DViewComp createBonusView3D(Bonus bonus) {
        final Bonus3DSettings config = gameVariantConfig.worldSettings().bonus();
        final GameVariantRenderConfig renderConfig = gameVariantConfig.renderConfig();
        final Bonus3DViewComp view3D = new Bonus3DViewComp(
            renderConfig.bonusSymbolImage(bonus.data().symbolCode()),
            config.symbolWidth(),
            renderConfig.bonusValueImage(bonus.data().symbolCode()),
            config.pointsWidth()
        );
        bonus.setComp(Bonus3DViewComp.class, view3D);

        //TODO move elsewhere
        animationManager.registry().register(Bonus3DAnimationID.BONUS_EATEN, view3D.eatenAnimation());

        return view3D;
    }

    private void createPac3D(GameUISettingsVM viewModel) {
        final Pac pac = level.entities().pac();
        final PacSettings settings = gameVariantConfig.worldSettings().pac();
        gameVariantConfig.factory3D().createPac3D(pac, settings);

        pac.requireComp(Pac3DViewComp.class).drawModeProperty().bind(viewModel.common3D.drawModeProperty);
    }

    private void createGhosts3D(GameUISettingsVM viewModel) {
        final List<GhostSettings> settings = gameVariantConfig.worldSettings().ghosts();
        level.entities().ghosts().forEach(ghost -> {
            final var ghostSettings = settings.get(ghost.personality().ordinal());
            gameVariantConfig.factory3D().createGhost3D(ghost, ghostSettings);
            ghost.requireComp(Ghost3DViewComp.class).drawModeProperty().bind(viewModel.common3D.drawModeProperty);
        });
    }

    private void createLivesCounter3D(GameSession session) {
        if (!session.livesCounter().hasComp(LivesCounter3DViewComp.class)) {
            final LivesCounter3DViewComp view3D = new LivesCounter3DViewComp(gameVariantConfig.factory3D(), gameVariantConfig.worldSettings());
            session.livesCounter().setComp(LivesCounter3DViewComp.class, view3D);
            view3D.root().setTranslateX(2 * WorldMap.TS);
            view3D.root().setTranslateY(2 * WorldMap.TS);
        }
    }

    private void createLevelCounter3D(LevelCounter levelCounter, AnimationRegistry registry) {
        if (!levelCounter.hasComp(LevelCounter3DViewComp.class)) {
            final LevelCounter3DViewComp view3D = new LevelCounter3DViewComp();
            levelCounter.setComp(LevelCounter3DViewComp.class, view3D);
            levelCounter.setComp(LevelCounter3DAnimationComp.class,
                new LevelCounter3DAnimationComp(view3D, registry));
            Logger.info("Level counter now has a 3D view and animation component");
        }
        else {
            Logger.info("Level counter already had a 3D view!");
        }

        replaceLevelCounter3D(levelCounter);
    }

    public void replaceLevelCounter3D(LevelCounter levelCounter) {
        final LevelCounter3DViewComp view3D = levelCounter.requireComp(LevelCounter3DViewComp.class);

        final Group oldRoot = view3D.root();
        if (oldRoot != null) {
            getChildren().remove(oldRoot);
        }

        LevelCounter3DViewSystem.updateLevelCounter3D(gameVariantConfig, levelCounter, level);
        getChildren().add(view3D.root());
    }

    private void createMessageView3D(AnimationRegistry registry) {
        MessageView3DBuilder.ensureAnim3DExists(level.entities().messageView(), registry);
    }

    // Order matters for correct transparency!
    private void arrangeLayout(GameSession session) {

        final LivesCounter livesCounter = session.livesCounter();
        final LivesCounter3DViewComp livesCounter3D = livesCounter.requireComp(LivesCounter3DViewComp.class);

        final Pac pac = level.entities().pac();
        final Pac3DViewComp pac3D = pac.requireComp(Pac3DViewComp.class);

        final House house = level.entities().house();
        final House3DViewComp house3D = house.requireComp(House3DViewComp.class);

        getChildren().add(livesCounter3D.root());

        getChildren().add(pac3D.root());
        getChildren().add(pac3D.powerLight());

        for (Ghost ghost: level.entities().ghosts()) {
            final Ghost3DViewComp ghost3D = ghost.requireComp(Ghost3DViewComp.class);
            getChildren().add(ghost3D.root());
        }

        for (Energizer3D energizer3D : energizer3DByTile.values()) {
            getChildren().add(energizer3D.root());
        }

        for (Pellet3D pellet3D : pellet3DByTile.values()) {
            getChildren().add(pellet3D.root());
        }

        getChildren().add(maze3D.particlesGroup());
        getChildren().add(maze3D.root());

        getChildren().add(house3D.root());
        getChildren().add(house3D.doors());

        getChildren().add(ghostHunterLight);
    }
}