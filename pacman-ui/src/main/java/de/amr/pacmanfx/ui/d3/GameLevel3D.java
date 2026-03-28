/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.lib.math.RandomNumberSupport;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.math.Vector3f;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelEntitySet;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.config.BonusConfig;
import de.amr.pacmanfx.ui.config.EnergizerConfig3D;
import de.amr.pacmanfx.ui.config.PelletConfig3D;
import de.amr.pacmanfx.ui.d3.animation.GhostLightAnimation;
import de.amr.pacmanfx.ui.d3.animation.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.d3.animation.LevelCompletedAnimationShort;
import de.amr.pacmanfx.ui.d3.animation.WallColorFlashingAnimation;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.EnergizerParticlesAnimation;
import de.amr.pacmanfx.uilib.assets.RandomTextPicker;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.GhostMaterials;
import de.amr.pacmanfx.uilib.model3D.actor.*;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import javafx.animation.SequentialTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.math.Vector2f.vec2_float;
import static de.amr.pacmanfx.model.GameControl.CommonGameState.*;
import static de.amr.pacmanfx.ui.GameUI.PROPERTY_3D_WALL_HEIGHT;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.*;
import static java.util.Objects.requireNonNull;

/**
 * Represents the complete 3D visualization of a single Pac-Man game level.
 * <p>
 * This class serves as the root node for all 3D elements of a level, including:
 * <ul>
 *   <li>Maze (floor, walls, house, pellets, energizers, particles)</li>
 *   <li>Actors (Pac-Man, ghosts, bonus symbol)</li>
 *   <li>Counters (lives, level number)</li>
 *   <li>Lights (ambient + ghost-specific point light)</li>
 *   <li>Dynamic messages (READY!, test mode, etc.)</li>
 *   <li>Animations (level complete, flashing walls, etc.)</li>
 * </ul>
 * <p>
 * It manages component creation, per-frame updates, draw mode changes, and full resource
 * cleanup via {@link DisposableGraphicsObject} and a {@link List} of {@link Disposable} components.
 * <p>
 * Instances are created by {@link PlayScene3D} and disposed when the level ends or the
 * 3D view is deactivated.
 *
 * @see PlayScene3D
 * @see Maze3D
 * @see Pac3D
 * @see GhostAppearance3D
 * @see DisposableGraphicsObject
 */
public class GameLevel3D extends Group implements DisposableGraphicsObject {

    private static final Comparator<GhostAppearance3D> BY_GHOST_PERSONALITY =
        Comparator.comparingInt(ga3D -> ga3D.ghost().personality());

    private final GameLevel level;

    private final UIConfig uiConfig;
    private final GameSoundEffects soundEffects;
    private final RandomTextPicker<String> gameOverMessagePicker;

    private final AnimationRegistry animationRegistry = new AnimationRegistry();
    private final GameLevelEntitySet entities = new GameLevelEntitySet();

    private GameLevel3DMessageManager messageManager;

    private WallColorFlashingAnimation wallColorFlashingAnimation;
    private LevelCompletedAnimation levelCompletedFullAnimation;
    private LevelCompletedAnimationShort levelCompletedShortAnimation;
    private GhostLightAnimation ghostLightAnimation;
    private EnergizerParticlesAnimation particlesAnimation;

    /**
     * Creates a new 3D level representation for the given game level.
     *
     * @param level          the game level to visualize
     * @param uiConfig       the global UI configuration (provides 3D settings, colors, models)
     * @param soundEffects   the play sound effects
     * @param localizedTexts the resource bundle containing the localized UI texts
     */
    public GameLevel3D(GameLevel level, UIConfig uiConfig, GameSoundEffects soundEffects, ResourceBundle localizedTexts) {
        this.level = requireNonNull(level);
        this.uiConfig = requireNonNull(uiConfig);
        this.soundEffects = requireNonNull(soundEffects);
        gameOverMessagePicker = RandomTextPicker.fromBundle(localizedTexts, "game.over");

        final WorldMapColorScheme mapColorScheme = uiConfig.colorScheme(level.worldMap());
        createEntitiesAndAddToGroup(mapColorScheme);
        // Maze3D must exist when energizer animations are created:
        final Maze3D maze3D = entities().first(Maze3D.class).orElseThrow();
        createAnimations(maze3D, mapColorScheme);

        resetPacZPosition();
        setMouseTransparent(true); // this increases performance they say...
    }

    /**
     * Releases all resources held by this level.
     * <p>
     * Clears animations, unbinds listeners, disposes all registered components,
     * cleans lights and the entire scene graph, and removes all children.
     */
    @Override
    public void dispose() {
        animationRegistry.clear();
        entities.dispose();
        cleanupGroup(this, true);
        if (messageManager != null) {
            messageManager.dispose();
            messageManager = null;
        }
        Logger.info("Cleaned game level 3D");
    }

    // Set height over floor. Top of floor is at z=0.
    public void resetPacZPosition() {
        entities.first(Pac3D.class).ifPresent(pac3D -> pac3D.setTranslateZ(-0.5 * pac3D.getBoundsInLocal().getDepth()));
    }

    /**
     * Starts the lives counter symbols following Pac-Man with their eyes.
     */
    public void startTrackingPac() {
        entities.first(LivesCounter3D.class).ifPresent(livesCounter3D ->
            entities.first(Pac3D.class).ifPresentOrElse(
                livesCounter3D::startTracking,
                () -> Logger.error("Lives counter cannot track Pac-Man: 3D Pac-Man not existing")));
    }

    // Accessors

    public GameLevelEntitySet entities() {
        return entities;
    }

    public AnimationRegistry animationRegistry() {
        return animationRegistry;
    }

    public GameLevel level() {
        return level;
    }

    public GameLevel3DMessageManager messageManager() {
        return messageManager;
    }

    public Optional<GhostAppearance3D> ghostAppearance3D(byte personality) {
        Validations.requireValidGhostPersonality(personality);
        return entities.all(GhostAppearance3D.class)
            .filter(ga3D -> ga3D.ghost().personality() == personality).findFirst();
    }

    // Others

    /**
     * Replaces or creates the bonus visualization for the given bonus item.
     *
     * @param bonus the new bonus
     */
    public void addOrReplaceBonus3D(Bonus bonus) {
        requireNonNull(uiConfig);
        requireNonNull(bonus);
        final BonusConfig bonusConfig = uiConfig.entityConfig().bonusConfig();
        entities.first(Bonus3D.class).ifPresent(bonus3D -> {
            bonus3D.dispose();
            entities.remove(bonus3D);
            getChildren().remove(bonus3D);
        });
        final var bonus3D = new Bonus3D(animationRegistry, bonus,
            uiConfig.bonusSymbolImage(bonus.symbol()), bonusConfig.bonusSymbolWidth(),
            uiConfig.bonusValueImage(bonus.symbol()),  bonusConfig.bonusPointsWidth());
        bonus3D.showEdible();

        entities.add(bonus3D);
        addChild(bonus3D);
    }

    // private

    private List<PhongMaterial> dressMaterials(List<GhostAppearance3D> ghostsAppearances) {
        return ghostsAppearances.stream()
            .map(GhostAppearance3D::ghost3D)
            .map(Ghost3D::materials)
            .map(GhostMaterials::normal)
            .map(GhostComponentMaterials::dress)
            .toList();
    }

    /**
     * Arranges all direct children in the correct rendering order.
     * <p>
     * Order matters for correct transparency: actors and effects must appear
     * in front of walls/house.
     */
    private void addChildrenInRightOrder() {
        entities().first(Maze3D.class).ifPresent(maze3D -> addChildren(maze3D.floor(), maze3D.particlesGroup()));

        addChild(entities.first(LevelCounter3D.class).orElseThrow());
        addChild(entities.first(LivesCounter3D.class).orElseThrow());

        final Pac3D pac3D = entities.first(Pac3D.class).orElseThrow();
        addChild(pac3D);
        pac3D.light().ifPresent(this::addChild);

        entities.all(GhostAppearance3D.class).sorted(BY_GHOST_PERSONALITY).forEach(this::addChild);

        entities.all(Energizer3D.class).map(Energizer3D::shape).forEach(this::addChild);
        entities.all(Pellet3D.class).map(Pellet3D::shape).forEach(this::addChild);

        entities().first(Maze3D.class).ifPresent(maze3D -> addChildren(maze3D, maze3D.house().root(), maze3D.house().doors()));
    }

    private void addChild(Node child) {
        getChildren().add(child);
    }

    private void addChildren(Node... children) {
        getChildren().addAll(children);
    }

    private void createEntitiesAndAddToGroup(WorldMapColorScheme colorScheme) {
        createPac3D();
        createGhostAppearances3D();
        createMaze3D(colorScheme);
        createLevelCounter3D();
        createLivesCounter3D();
        createFood3D();
        createMessageManager();
        addChildrenInRightOrder();
    }

    private void createPac3D() {
        final PacConfig pacConfig = uiConfig.entityConfig().pacConfig();
        final var pac3D = uiConfig.factory3D().createPac3D(level.pac(), pacConfig, animationRegistry);
        pac3D.createPowerLight(pacConfig);
        entities.add(pac3D);
    }

    private void createGhostAppearances3D() {
        level.ghosts().map(ghost -> {
            final var ga3D = createGhostAppearance3D(uiConfig.factory3D(), uiConfig.entityConfig().ghostConfigs(), ghost);
            ga3D.init(level);
            ga3D.setTranslateZ(-0.5 * ga3D.getBoundsInLocal().getDepth() - 1);
            return ga3D;
        }).forEach(entities::add);
    }

    private GhostAppearance3D createGhostAppearance3D(Factory3D factory3D, List<GhostConfig> ghostConfigs, Ghost ghost) {
        final var ghostAppearance3D = factory3D.createGhostAppearance3D(
            ghost,
            ghostConfigs.get(ghost.personality()),
            animationRegistry
        );
        ghostAppearance3D.setNumFlashes(level.numFlashes());
        final BooleanBinding visibleInsideWorld = Bindings.createBooleanBinding(
            () -> ghost.isVisible() && !outsideWorld(level.worldMap(), ghost),
            ghost.visibleProperty(), ghost.positionProperty()
        );
        ghostAppearance3D.visibleProperty().bind(visibleInsideWorld);
        return ghostAppearance3D;
    }

    private void createLivesCounter3D() {
        final var livesCounter3D = new LivesCounter3D(uiConfig, animationRegistry);
        livesCounter3D.setTranslateX(2 * TS);
        livesCounter3D.setTranslateY(2 * TS);
        livesCounter3D.pillarColorProperty().set(uiConfig.entityConfig().livesCounter().pillarColor());
        livesCounter3D.plateColorProperty().set(uiConfig.entityConfig().livesCounter().plateColor());
        entities.add(livesCounter3D);
    }

    private void createLevelCounter3D() {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final var levelCounter3D = new LevelCounter3D(animationRegistry, uiConfig);
        levelCounter3D.setTranslateX(TS * (terrain.numCols() - 2));
        levelCounter3D.setTranslateY(2 * TS);
        levelCounter3D.setTranslateZ(-uiConfig.entityConfig().levelCounter().elevation());
        entities.add(levelCounter3D);
    }

    private void createMaze3D(WorldMapColorScheme colorScheme) {
        final var maze3D = uiConfig.factory3D().createMaze3D(level, uiConfig.entityConfig(), colorScheme, animationRegistry);
        entities.add(maze3D);
    }

    private void createMessageManager() {
        this.messageManager = new GameLevel3DMessageManager(animationRegistry, this);
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        terrain.optHouse().ifPresentOrElse(
            house -> messageManager.setReadyMessageCenter(house.centerPositionUnderHouse()),
            () -> {
                Logger.error("No house in this game level! WTF?");
                final double x = terrain.numCols() * HTS, y = terrain.numRows() * HTS;
                messageManager.setReadyMessageCenter(vec2_float(x, y));
            });
        messageManager.setTestMessageCenter(
            vec2_float(terrain.numCols() * HTS, (terrain.numRows() - 2) * TS));
    }

    /**
     * Determines if the given ghost's center position is outside the visible world bounds.
     *
     * @param worldMap the world map
     * @param ghost the ghost to check
     * @return true if the ghost is outside the world bounds
     */
    private static boolean outsideWorld(WorldMap worldMap, Ghost ghost) {
        final Vector2f center = ghost.center();
        return center.x() < HTS || center.x() > worldMap.numCols() * TS - HTS;
    }

    // Food (pellets and energizers)

    public static final double PELLET_EATING_DELAY_SEC = 0.05;

    private void createFood3D() {
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        final Maze3D maze3D = entities().first(Maze3D.class).orElseThrow();

        final WorldMapColorScheme colorScheme = uiConfig.colorScheme(level.worldMap());
        final PelletConfig3D pelletConfig3D = uiConfig.entityConfig().pellet();
        final var pelletMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.pellet()));
        final double pelletZ = maze3D.floorTop() - pelletConfig3D.floorElevation();
        foodLayer.tiles()
            .filter(foodLayer::hasFoodAtTile)
            .filter(tile -> !foodLayer.isEnergizerTile(tile))
            .map(tile -> {
                final Pellet3D pellet3D = uiConfig.factory3D().createPellet3D(pelletConfig3D, pelletMaterial);
                pellet3D.setLocation(tile, pelletZ);
                return pellet3D;
            }).forEach(entities::add);

        final EnergizerConfig3D energizerConfig3D = uiConfig.entityConfig().energizer();
        final double energizerZ = maze3D.floorTop() - energizerConfig3D.floorElevation();
        foodLayer.tiles()
            .filter(foodLayer::isEnergizerTile)
            .filter(foodLayer::hasFoodAtTile)
            .map(tile -> {
                final Energizer3D energizer3D = uiConfig.factory3D().createEnergizer3D(energizerConfig3D, animationRegistry, pelletMaterial);
                energizer3D.setLocation(tile, energizerZ);
                return energizer3D;
            })
            .forEach(entities::add);
    }

    /**
     * Handles Pac-Man eating food at the given tile (pellet or energizer).
     *
     * @param pelletContainer the JavaFX group containing the pellet shapes (used for removing eaten pellets)
     * @param tile the tile where food was eaten
     */
    public void removeFoodAt(Group pelletContainer, Vector2i tile) {
        final Energizer3D energizer3D = entities.all(Energizer3D.class)
            .filter(e3D -> tile.equals(e3D.tile()))
            .findFirst()
            .orElse(null);

        if (energizer3D != null) {
            energizer3D.stopPumping();
            energizer3D.hide();
            createEnergizerExplosion(energizer3D);
        } else {
            entities.all(Pellet3D.class)
                .filter(pellet3D -> tile.equals(pellet3D.tile()))
                .findFirst()
                .ifPresent(pellet3D -> removePelletAfterDelay(pelletContainer, pellet3D));
        }
    }

    private void removePelletAfterDelay(Group pelletContainer, Pellet3D pellet3D) {
        pauseSecThen(PELLET_EATING_DELAY_SEC, () -> pelletContainer.getChildren().remove(pellet3D.shape()))
            .play();
    }

    /**
     * Removes all pellet visualizations (used when all pellets are eaten at once).
     */
    public void removeAllPellets3D(Group pelletContainer) {
        entities.all(Pellet3D.class)
            .map(Pellet3D::shape)
            .forEach(shape -> pelletContainer.getChildren().remove(shape));
    }

    // Particles animation

    private void createParticlesAnimation(Maze3D maze3D, List<PhongMaterial> ghostMaterials) {
        // The bottom center positions of the swirls where the particles of exploded energizers eventually are displayed
        final List<Vector2f> swirlBaseCenters = Stream.of(CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, ORANGE_GHOST_POKEY)
            .map(level::ghost)
            .map(Ghost::startPosition)
            .map(pos -> pos.plus(HTS, HTS))
            .toList();

        particlesAnimation = new EnergizerParticlesAnimation(
            EnergizerParticlesAnimation.DEFAULT_CONFIG,
            animationRegistry,
            swirlBaseCenters,
            ghostMaterials,
            maze3D.floor(),
            maze3D.particlesGroup());
    }

    public void createEnergizerExplosion(Energizer3D energizer) {
        final Point3D point = energizer.shape().localToScene(Point3D.ZERO);
        final Vector3f origin = new Vector3f(point.getX(), point.getY(), point.getZ());
        particlesAnimation.addEnergizerExplosion(origin);
    }

    // Event handling

    /**
     * Dispatches game state change events to the appropriate handler method.
     *
     * @param ui the game UI
     * @param event   the state change event
     */
    public void handleGameStateChange(GameUI ui, GameStateChangeEvent event) {
        requireNonNull(event);

        final State<Game> gameState = event.newState();
        if (matches(gameState, STARTING_GAME_OR_LEVEL)) {
            onStartingGame();
        } else if (matches(gameState, HUNTING)) {
            onHuntingStart();
        } else if (matches(gameState, PACMAN_DYING)) {
            onPacManDying();
        } else if (matches(gameState, EATING_GHOST)) {
            onEatingGhost();
        } else if (matches(gameState, LEVEL_COMPLETE)) {
            onLevelComplete();
        } else if (matches(gameState, GAME_OVER)) {
            onGameOver(ui);
        }
    }

    private static boolean matches(State<Game> gameState, GameControl.CommonGameState expected) {
        return gameState.nameMatches(expected.name());
    }

    /**
     * Handles bonus activation: updates 3D representation and plays sound.
     */
    public void onBonusActivated(BonusActivatedEvent gameEvent) {
        addOrReplaceBonus3D(gameEvent.bonus());
        soundEffects.playBonusActiveSound();
    }

    /**
     * Handles bonus eaten: shows eaten animation and plays sound.
     */
    public void onBonusEaten(BonusEatenEvent ignoredEvent) {
        entities.first(Bonus3D.class).ifPresent(Bonus3D::showEaten);
        soundEffects.playBonusEatenSound();
    }

    /**
     * Handles bonus expiration: expires 3D bonus and plays sound.
     */
    public void onBonusExpired(BonusExpiredEvent ignoredEvent) {
        entities.first(Bonus3D.class).ifPresent(Bonus3D::expire);
        soundEffects.playBonusExpiredSound();
    }

    /**
     * Shows the "READY!" message when the game continues.
     */
    public void onGameContinues(GameContinuedEvent ignoredEvent) {
        resetPacZPosition();
        messageManager().showReadyMessage();
    }

    /**
     * Plays game ready sound unless in demo or test mode.
     */
    public void onGameStarts(GameStartedEvent event) {
        final Game game = event.game();
        final State<Game> state = game.control().state();
        final boolean silent = game.isDemoLevelRunning() || state instanceof TestState;
        if (!silent) {
            soundEffects.playGameReadySound();
        }
        resetPacZPosition();
    }

    /**
     * Plays sound when a ghost is eaten.
     */
    public void onGhostEaten(GhostEatenEvent ignoredEvent) {
        soundEffects.playGhostEatenSound();
    }

    /**
     * Handles Pac eating food: updates 3D food and plays munching sound (with rate limiting).
     */
    public void onPacEatsFood(PacEatsFoodEvent gameEvent, long tick) {
        if (gameEvent.allPellets()) {
            removeAllPellets3D(this);
        } else {
            removeFoodAt(this, gameEvent.pac().tile());
            soundEffects.playPacMunchingSound(tick);
        }
    }

    /**
     * Handles Pac gaining power: stops siren, starts power animation/sound.
     */
    public void onPacGetsPower(PacGetsPowerEvent ignoredEvent) {
        final Pac3D pac3D = entities.first(Pac3D.class).orElseThrow();
        final Game game = level.game();
        soundEffects.stopSiren();
        if (!game.isLevelCompleted(level)) {
            pac3D.setMovementPowerMode(true);
            wallColorFlashingAnimation.playFromStart();
            soundEffects.playPacPowerSound();
        }
    }

    /**
     * Handles Pac losing power: stops power animation/sound.
     */
    public void onPacLostPower(PacLostPowerEvent ignoredEvent) {
        final Pac3D pac3D = entities.first(Pac3D.class).orElseThrow();
        pac3D.setMovementPowerMode(false);
        wallColorFlashingAnimation.stop();
        soundEffects.stopPacPowerSound();
    }

    public void onSpecialScoreReached(SpecialScoreReachedEvent ignoredEvent) {
        soundEffects.playExtraLifeSound();
    }

    // Private state-specific handlers

    private void onStartingGame() {
        entities.all().forEach(e -> e.init(level));
    }

    private void onHuntingStart() {
        entities.first(Pac3D.class).ifPresent(pac3D -> pac3D.init(level));
        entities.all(GhostAppearance3D.class).forEach(ghost3D -> ghost3D.init(level));
        entities.all(Energizer3D.class).forEach(Energizer3D::startPumping);
        particlesAnimation.playFromStart();
        ghostLightAnimation.playFromStart();
    }

    private void onPacManDying() {
        final Pac3D pac3D = entities.first(Pac3D.class).orElseThrow();
        final TickTimer stateTimer = level.game().control().state().timer();

        soundEffects.stopAll();
        ghostLightAnimation.stop();
        wallColorFlashingAnimation.stop();
        entities.all(GhostAppearance3D.class).forEach(GhostAppearance3D::stopAllAnimations);
        entities.first(Bonus3D.class).ifPresent(Bonus3D::expire);

        // One last update before dying animation
        pac3D.update(level);

        stateTimer.resetIndefiniteTime(); // freeze until animation ends
        final var dyingAnimation = new SequentialTransition(
            pauseSec(1.5),
            doNow(soundEffects::playPacDeadSound),
            pac3D.dyingAnimation().animationFX(),
            pauseSec(0.5)
        );
        dyingAnimation.setOnFinished(_ -> {
            pac3D.setVisible(false);
            resetPacZPosition();
            stateTimer.expire();
        });
        dyingAnimation.play();
    }

    private void onEatingGhost() {
        level.game().simulationStep().ghostsKilled.forEach(killedGhost -> {
            final GhostAppearance3D ga3D = ghostAppearance3D(killedGhost.personality()).orElseThrow();
            final int numberIndex = level.energizerVictims().indexOf(killedGhost);
            final Shape3D numberShape3D = uiConfig.factory3D().createNumberShape3D(uiConfig, numberIndex);
            ga3D.showAsNumber(numberShape3D);
        });
    }

    private void onLevelComplete() {
        final State<Game> gameState = level.game().control().state();
        final Maze3D maze3D = entities().first(Maze3D.class).orElseThrow();

        soundEffects.stopAll();
        animationRegistry().stopAllAnimations();
        cleanupFoodAndParticles(maze3D);
        maze3D.house().hideDoors();
        entities.first(Bonus3D.class).ifPresent(Bonus3D::expire);
        messageManager.hideMessage();
        playLevelEndAnimation(maze3D, level, gameState);
    }

    private void onGameOver(GameUI ui) {
        final State<Game> gameState = level.game().control().state();
        gameState.timer().restartSeconds(3);
        ghostLightAnimation.stop();
        cleanupFoodAndParticles(entities().first(Maze3D.class).orElseThrow());
        entities.first(Bonus3D.class).ifPresent(Bonus3D::expire);
        if (!level.isDemoLevel() && RandomNumberSupport.chance(0.25)) {
            ui.showFlashMessage(Duration.seconds(2.5), gameOverMessagePicker.nextText());
        }
        soundEffects.playGameOverSound();
    }

    private void cleanupFoodAndParticles(Maze3D maze3D) {
        particlesAnimation.stop();
        entities.all(Energizer3D.class).forEach(energizer3D -> {
            energizer3D.stopPumping();
            energizer3D.hide();
        });
        // Hide 3D food explicitly (handles cheat-eat-all case)
        entities.all(Pellet3D.class).forEach(pellet3D -> pellet3D.shape().setVisible(false));
        maze3D.particlesGroup().getChildren().clear();
    }

    // Animations

    private void createAnimations(Maze3D maze3D, WorldMapColorScheme colorScheme) {
        final List<GhostAppearance3D> ghostAppearances3D = entities.all(GhostAppearance3D.class).sorted(BY_GHOST_PERSONALITY).toList();
        wallColorFlashingAnimation = new WallColorFlashingAnimation(animationRegistry, this, colorScheme);
        levelCompletedFullAnimation = new LevelCompletedAnimation(animationRegistry, this, soundEffects);
        levelCompletedShortAnimation = new LevelCompletedAnimationShort(animationRegistry, this);
        ghostLightAnimation = new GhostLightAnimation(animationRegistry, ghostAppearances3D);
        addChild(ghostLightAnimation.light());
        createParticlesAnimation(maze3D, dressMaterials(ghostAppearances3D));
    }

    /**
     * Plays the level completion animation sequence and resets game timer.
     *
     * @param maze3D the 3D maze to be animated
     * @param level the completed level (used to determine animation details)
     * @param state the current game state (used to determine cut-scene follow-up)
     */
    public void playLevelEndAnimation(Maze3D maze3D, GameLevel level, State<Game> state) {
        final boolean cutSceneFollows = level.cutSceneNumber() != 0;
        final PerspectiveID perspectiveBeforeAnimation = GameUI.PROPERTY_3D_PERSPECTIVE_ID.get();

        final var seq = new SequentialTransition(
            pauseSecThen(2, () -> {
                GameUI.PROPERTY_3D_PERSPECTIVE_ID.set(PerspectiveID.TOTAL);
                maze3D.wallBaseHeightProperty().unbind();
            }),
            (cutSceneFollows ? levelCompletedShortAnimation : levelCompletedFullAnimation).animationFX(),
            pauseSec(0.25)
        );

        seq.setOnFinished(_ -> {
            GameUI.PROPERTY_3D_PERSPECTIVE_ID.set(perspectiveBeforeAnimation);
            maze3D.wallBaseHeightProperty().bind(PROPERTY_3D_WALL_HEIGHT);
            state.timer().expire();
        });

        state.timer().resetIndefiniteTime(); // freeze game control until animation ends
        seq.play();
    }
}