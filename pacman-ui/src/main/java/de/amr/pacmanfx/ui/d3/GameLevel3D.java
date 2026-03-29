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

import java.util.*;
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

    private static final Comparator<GhostAppearance3D> BY_GHOST_PERSONALITY = Comparator.comparingInt(ga3D -> ga3D.ghost().personality());

    public enum AnimationID { 
        GHOST_LIGHT, 
        LEVEL_COMPLETED_FULL, 
        LEVEL_COMPLETED_SHORT,
        EXPLOSION_PARTICLES,
        WALL_COLOR_FLASHING
    }

    private final GameLevel level;
    private final GameLevelEntitySet entities = new GameLevelEntitySet();
    private final AnimationRegistry animations = new AnimationRegistry();
    private final UIConfig uiConfig;
    private final GameSoundEffects soundEffects;
    private final RandomTextPicker<String> gameOverMessagePicker;

    private GameLevel3DMessageManager messageManager;

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
        createPac3D();
        createGhostAppearances3D();
        createMaze3D(mapColorScheme);
        createLevelCounter3D();
        createLivesCounter3D();
        createFood3D();
        createMessageManager();
        addChildrenToGroup();

        // Maze3D must exist when energizer animations are created:
        final Maze3D maze3D = entities().theOne(Maze3D.class);
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
        animations.dispose();
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
        return animations;
    }

    public GameLevel level() {
        return level;
    }

    public GameLevel3DMessageManager messageManager() {
        return messageManager;
    }

    public Optional<GhostAppearance3D> ghostAppearance3D(byte personality) {
        Validations.requireValidGhostPersonality(personality);
        return entities.where(GhostAppearance3D.class, ga3D -> ga3D.ghost().personality() == personality).findFirst();
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
    private void addChildrenToGroup() {
        final Maze3D maze3D = entities().theOne(Maze3D.class);
        final Pac3D pac3D = entities.theOne(Pac3D.class);
        final LevelCounter3D levelCounter3D = entities.theOne(LevelCounter3D.class);
        final LivesCounter3D livesCounter3D = entities.theOne(LivesCounter3D.class);
        addChildren(maze3D.floor(), maze3D.particlesGroup());
        addChild(levelCounter3D);
        addChild(livesCounter3D);
        addChild(pac3D);
        pac3D.light().ifPresent(this::addChild);
        entities.all(GhostAppearance3D.class).sorted(BY_GHOST_PERSONALITY).forEach(this::addChild);
        entities.all(Energizer3D.class).map(Energizer3D::shape).forEach(this::addChild);
        entities.all(Pellet3D.class).map(Pellet3D::shape).forEach(this::addChild);
        addChildren(maze3D, maze3D.house().root(), maze3D.house().doors());
    }

    private void addChild(Node child) {
        getChildren().add(child);
    }

    private void addChildren(Node... children) {
        getChildren().addAll(children);
    }

    private void createPac3D() {
        final PacConfig pacConfig = uiConfig.entityConfig().pacConfig();
        final var pac3D = uiConfig.factory3D().createPac3D(level.pac(), pacConfig, animations);
        pac3D.createPowerLight(pacConfig);
        entities.add(pac3D);
    }

    private void createGhostAppearances3D() {
        level.ghosts().map(ghost -> {
            final var ga3D = createGhostAppearance3D(uiConfig.entityConfig().ghostConfigs().get(ghost.personality()), ghost);
            ga3D.init(level);
            ga3D.setTranslateZ(-0.5 * ga3D.getBoundsInLocal().getDepth() - 1);
            return ga3D;
        }).forEach(entities::add);
    }

    private GhostAppearance3D createGhostAppearance3D(GhostConfig ghostConfig, Ghost ghost) {
        final var ghostAppearance3D = uiConfig.factory3D().createGhostAppearance3D(
            ghost,
            ghostConfig,
            animations
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
        final var livesCounter3D = new LivesCounter3D(uiConfig, animations);
        livesCounter3D.setTranslateX(2 * TS);
        livesCounter3D.setTranslateY(2 * TS);
        livesCounter3D.pillarColorProperty().set(uiConfig.entityConfig().livesCounter().pillarColor());
        livesCounter3D.plateColorProperty().set(uiConfig.entityConfig().livesCounter().plateColor());
        entities.add(livesCounter3D);
    }

    private void createLevelCounter3D() {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final var levelCounter3D = new LevelCounter3D(animations, uiConfig);
        levelCounter3D.setTranslateX(TS * (terrain.numCols() - 2));
        levelCounter3D.setTranslateY(2 * TS);
        levelCounter3D.setTranslateZ(-uiConfig.entityConfig().levelCounter().elevation());
        entities.add(levelCounter3D);
    }

    private void createMaze3D(WorldMapColorScheme colorScheme) {
        final var maze3D = uiConfig.factory3D().createMaze3D(level, uiConfig.entityConfig(), colorScheme, animations);
        entities.add(maze3D);
    }

    private void createMessageManager() {
        this.messageManager = new GameLevel3DMessageManager(animations, this);
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

    // Bonus

    /**
     * Replaces or creates the bonus visualization for the given bonus item.
     *
     * @param bonus the new bonus
     */
    private void addOrReplaceBonus3D(Bonus bonus) {
        requireNonNull(uiConfig);
        requireNonNull(bonus);
        // Avoid exception when removing inside for-each
        final List<Bonus3D> existing = new ArrayList<>(entities.all(Bonus3D.class).toList());
        existing.forEach(bonus3D -> {
            bonus3D.dispose();
            entities.remove(bonus3D);
            getChildren().remove(bonus3D);
        });
        addChild(createBonus3D(bonus));
    }

    private Bonus3D createBonus3D(Bonus bonus) {
        final var bonusConfig = uiConfig.entityConfig().bonusConfig();
        final var bonus3D = new Bonus3D(animations, bonus,
            uiConfig.bonusSymbolImage(bonus.symbol()), bonusConfig.bonusSymbolWidth(),
            uiConfig.bonusValueImage(bonus.symbol()),  bonusConfig.bonusPointsWidth());
        bonus3D.showEdible();
        entities.add(bonus3D);
        return bonus3D;
    }

    // Food (pellets and energizers)

    public static final double PELLET_EATING_DELAY_SEC = 0.05;

    private void createFood3D() {
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        final Maze3D maze3D = entities().theOne(Maze3D.class);

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
                final Energizer3D energizer3D = uiConfig.factory3D().createEnergizer3D(energizerConfig3D, animations, pelletMaterial);
                energizer3D.setLocation(tile, energizerZ);
                return energizer3D;
            })
            .forEach(entities::add);
    }

    private void eatFood(Group pelletContainer, Vector2i tile) {
        final Optional<Energizer3D> eatenEnergizer3D = entities.where(Energizer3D.class, e3D -> tile.equals(e3D.tile())).findAny();
        eatenEnergizer3D.ifPresentOrElse(energizer3D -> {
            energizer3D.stopPumping();
            energizer3D.hide();
            final Point3D center = energizer3D.shape().localToScene(Point3D.ZERO);
            animations.animation(AnimationID.EXPLOSION_PARTICLES, EnergizerParticlesAnimation.class).triggerEnergizerExplosion(center);
        }, () -> entities.where(Pellet3D.class, p3D -> tile.equals(p3D.tile()))
            .findFirst()
            .ifPresent(p3D -> removePelletAfterDelay(pelletContainer, p3D)));
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

    private EnergizerParticlesAnimation createParticlesAnimation(Maze3D maze3D, List<PhongMaterial> ghostMaterials) {
        // The bottom center positions of the swirls where the particles of exploded energizers eventually are displayed
        final List<Vector2f> swirlBaseCenters = Stream.of(CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, ORANGE_GHOST_POKEY)
            .map(level::ghost)
            .map(Ghost::startPosition)
            .map(pos -> pos.plus(HTS, HTS))
            .toList();

        return new EnergizerParticlesAnimation(
            EnergizerParticlesAnimation.DEFAULT_CONFIG,
            swirlBaseCenters,
            ghostMaterials,
            maze3D.floor(),
            maze3D.particlesGroup());
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
            eatFood(this, gameEvent.pac().tile());
            soundEffects.playPacMunchingSound(tick);
        }
    }

    /**
     * Handles Pac gaining power: stops siren, starts power animation/sound.
     */
    public void onPacGetsPower(PacGetsPowerEvent ignoredEvent) {
        final Pac3D pac3D = entities.theOne(Pac3D.class);
        final Game game = level.game();
        soundEffects.stopSiren();
        if (!game.isLevelCompleted(level)) {
            pac3D.setMovementAnimationPowerMode(true);
            animations.animation(AnimationID.WALL_COLOR_FLASHING, WallColorFlashingAnimation.class).playFromStart();
            soundEffects.playPacPowerSound();
        }
    }

    /**
     * Handles Pac losing power: stops power animation/sound.
     */
    public void onPacLostPower(PacLostPowerEvent ignoredEvent) {
        final Pac3D pac3D = entities.theOne(Pac3D.class);
        pac3D.setMovementAnimationPowerMode(false);
        animations.animation(AnimationID.WALL_COLOR_FLASHING, WallColorFlashingAnimation.class).stop();
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
        animations.animation(AnimationID.EXPLOSION_PARTICLES).playFromStart();
        animations.animation(AnimationID.GHOST_LIGHT).playFromStart();
    }

    private void onPacManDying() {
        final Pac3D pac3D = entities.theOne(Pac3D.class);
        final TickTimer stateTimer = level.game().control().state().timer();

        soundEffects.stopAll();
        animations.animation(AnimationID.GHOST_LIGHT).stop();
        animations.animation(AnimationID.WALL_COLOR_FLASHING, WallColorFlashingAnimation.class).stop();
        entities.all(GhostAppearance3D.class).forEach(GhostAppearance3D::stopAllAnimations);
        entities.first(Bonus3D.class).ifPresent(Bonus3D::expire);

        // One last update before dying animation
        pac3D.update(level);

        stateTimer.resetIndefiniteTime(); // freeze until animation ends
        final var dyingAnimation = new SequentialTransition(
            pauseSec(1.5),
            doNow(soundEffects::playPacDeadSound),
            //TODO can we assume that this animation always exists?
            animations.animation(Pac3D.AnimationID.PAC_DYING).animationFX(),
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
        final Maze3D maze3D = entities().theOne(Maze3D.class);

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
        animations.animation(AnimationID.GHOST_LIGHT).stop();
        cleanupFoodAndParticles(entities().theOne(Maze3D.class));
        entities.first(Bonus3D.class).ifPresent(Bonus3D::expire);
        if (!level.isDemoLevel() && RandomNumberSupport.chance(0.25)) {
            ui.showFlashMessage(Duration.seconds(2.5), gameOverMessagePicker.nextText());
        }
        soundEffects.playGameOverSound();
    }

    private void cleanupFoodAndParticles(Maze3D maze3D) {
        animations.animation(AnimationID.EXPLOSION_PARTICLES).stop();
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
        animations.register(AnimationID.WALL_COLOR_FLASHING, new WallColorFlashingAnimation(this, colorScheme));
        animations.register(AnimationID.LEVEL_COMPLETED_FULL, new LevelCompletedAnimation(this, soundEffects));
        animations.register(AnimationID.LEVEL_COMPLETED_SHORT, new LevelCompletedAnimationShort(this));
        animations.register(AnimationID.GHOST_LIGHT, new GhostLightAnimation(ghostAppearancesByPersonality()));
        animations.register(AnimationID.EXPLOSION_PARTICLES, createParticlesAnimation(maze3D, dressMaterials(ghostAppearancesByPersonality())));

        //TODO: this looks ugly
        addChild(animations.animation(AnimationID.GHOST_LIGHT, GhostLightAnimation.class).light());
    }
    
    private List<GhostAppearance3D> ghostAppearancesByPersonality() {
        return entities.all(GhostAppearance3D.class).sorted(BY_GHOST_PERSONALITY).toList();
    }

    /**
     * Plays the level completion animation sequence and resets game timer.
     *
     * @param maze3D the 3D maze to be animated
     * @param level the completed level (used to determine animation details)
     * @param state the current game state (used to determine cut-scene follow-up)
     */
    public void playLevelEndAnimation(Maze3D maze3D, GameLevel level, State<Game> state) {
        final boolean cutScene = level.cutSceneNumber() != 0;
        final PerspectiveID perspectiveBeforeAnimation = GameUI.PROPERTY_3D_PERSPECTIVE_ID.get();

        final var seq = new SequentialTransition(
            pauseSecThen(2, () -> {
                GameUI.PROPERTY_3D_PERSPECTIVE_ID.set(PerspectiveID.TOTAL);
                maze3D.wallBaseHeightProperty().unbind();
            }),
            animations.animation(cutScene ? AnimationID.LEVEL_COMPLETED_SHORT: AnimationID.LEVEL_COMPLETED_FULL).animationFX(),
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