/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.ui.ActionBindingsManager;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Resources;
import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.layout.GameUI_ContextMenu;
import de.amr.pacmanfx.ui.sound.GamePlaySoundEffects;
import de.amr.pacmanfx.uilib.model3D.Energizer3D;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import de.amr.pacmanfx.uilib.model3D.Scores3D;
import de.amr.pacmanfx.uilib.widgets.CoordinateSystem;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.model.GameControl.CommonGameState.*;
import static de.amr.pacmanfx.ui.action.CommonGameActions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.alt;
import static de.amr.pacmanfx.ui.input.Keyboard.control;
import static java.util.Objects.requireNonNull;

/**
 * 3D implementation of the Pac-Man play scene.
 * <p>
 * This scene is responsible for rendering and updating the full 3D representation
 * of the current game level, including the maze, actors, food, bonus items, HUD
 * elements, and all camera perspectives. It acts as the central coordinator between
 * the game model, the 3D world, the active camera controller, and the UI framework.
 * </p>
 *
 * <p>It manages:</p>
 * <ul>
 *   <li>Lifecycle of the 3D level — creation, replacement, disposal, and per-frame updates</li>
 *   <li>Camera perspectives — switching between multiple strategies (drone, total, tracking, stalking)</li>
 *   <li>3D rendering infrastructure — SubScene, camera setup, fade-in animation, coordinate axes, HUD placement</li>
 *   <li>Game event handling — reacting to model events and updating the 3D world</li>
 *   <li>Sound orchestration — contextual effects (munching, siren, ghost returning) synchronized with state</li>
 *   <li>Input bindings — keyboard and scroll-wheel controls for perspective and drone movement</li>
 *   <li>HUD and messaging — READY/test messages, score overlays, animated text</li>
 * </ul>
 *
 * <p>The class delegates actual rendering to {@link GameLevel3D} and camera control to {@link PerspectiveManager}.
 * It is intentionally large because it is the integration point for model ↔ 3D ↔ UI ↔ input ↔ audio.</p>
 *
 * <p>Instances are created and managed by {@link GameUI}. The scene is activated when switching
 * from 2D to 3D view and remains active until the user switches back or the game ends.</p>
 */
public class PlayScene3D implements GameScene {

    /** Fill color used at the start of the fade-in animation. */
    public static final Color SCENE_FILL_DARK = Color.BLACK;

    /** Final fill color after fade-in (fully transparent). */
    public static final Color SCENE_FILL_BRIGHT = Color.TRANSPARENT;

    /** Duration of the fade-in animation when the 3D scene becomes active. */
    public static final Duration FADE_IN_DURATION = Duration.seconds(3);

    protected final Group subSceneRoot = new Group();
    protected final Group gameLevel3DParent = new Group();
    protected final PerspectiveManager perspectiveManager;
    protected final PerspectiveCamera camera = new PerspectiveCamera(true);
    protected final SubScene subScene;
    protected final FadeInAnimation fadeInAnimation;

    protected GameLevel3DEventHandler level3D_EventHandler;
    protected GamePlaySoundEffects soundEffects;
    protected ActionBindingsManager actionBindings = ActionBindingsManager.NO_BINDINGS;
    protected GameUI ui;
    protected GameLevel3D gameLevel3D;
    protected Scores3D scores3D;
    protected PlaySceneContextMenu contextMenu;

    /**
     * Inner class managing the fade-in animation of the 3D sub-scene.
     * Darkens the background initially and gradually fades to transparent.
     */
    public class FadeInAnimation {
        private final Timeline timeline;

        /**
         * Creates a new fade-in animation with the specified duration.
         *
         * @param fadeInDuration duration of the fade from dark to transparent
         */
        public FadeInAnimation(Duration fadeInDuration) {
            timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, _ -> {
                        subScene.setFill(SCENE_FILL_DARK);
                        if (gameLevel3D != null) {
                            gameLevel3D.setVisible(true);
                        }
                        if (scores3D != null) {
                            scores3D.setVisible(true);
                        }
                        // TODO: Verify if startControlling is required here (may be redundant)
                        perspectiveManager.currentPerspective().ifPresent(Perspective::startControlling);
                    }),
                    new KeyFrame(fadeInDuration,
                            new KeyValue(subScene.fillProperty(), SCENE_FILL_BRIGHT, Interpolator.EASE_IN))
            );
        }

        /**
         * Plays the fade-in animation from the beginning.
         */
        public void play() {
            timeline.playFromStart();
        }
    }

    /**
     * Creates a new 3D play scene with default camera, sub-scene, axes, and perspective manager.
     */
    public PlayScene3D() {
        final var axes3D = new CoordinateSystem();
        axes3D.visibleProperty().bind(GameUI.PROPERTY_3D_AXES_VISIBLE);

        subSceneRoot.getChildren().setAll(gameLevel3DParent, axes3D);

        // Initial size is irrelevant (will be bound to parent scene size later)
        subScene = new SubScene(subSceneRoot, 88, 88, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);

        perspectiveManager = new PerspectiveManager(camera);
        fadeInAnimation = new FadeInAnimation(FADE_IN_DURATION);

        bindSceneActions();
    }

    @Override
    public void dispose() {
        actionBindings.dispose();
        perspectiveManager.dispose();
        disposeContextMenu();
        removeAndDisposeGameLevel3D();
    }

    /**
     * Injects the UI reference and initializes dependent components (sound effects,
     * event handler, scores). Called after construction.
     *
     * @param ui the game UI instance (must not be null)
     */
    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
        soundEffects = new GamePlaySoundEffects(ui.soundManager());
        soundEffects.setMunchingSoundDelay(ui.currentConfig().munchingSoundDelay());
        level3D_EventHandler = new GameLevel3DEventHandler(ui, soundEffects);

        // TODO: reconsider whether scores need recreation here (variant/font change?)
        replaceScores3D();
    }

    /**
     * Returns the current 3D level representation, if present.
     *
     * @return optional containing the 3D level or empty if not yet created
     */
    public Optional<GameLevel3D> level3D() {
        return Optional.ofNullable(gameLevel3D);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // GameScene interface implementation
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    public ActionBindingsManager actionBindings() {
        return actionBindings;
    }

    /**
     * Initializes the 3D scene when it becomes active.
     * <p>
     * Binds perspective ID, shows score HUD, and sets initial dark fill.
     * </p>
     *
     * @param game the active game instance
     */
    @Override
    public void init(Game game) {
        game.hud().score(true).show();
        perspectiveManager.activeIDProperty().bind(GameUI.PROPERTY_3D_PERSPECTIVE_ID);
        subScene.setFill(SCENE_FILL_DARK);
    }

    /**
     * Cleans up resources when the 3D scene is deactivated or the game ends.
     * <p>
     * Stops sounds, disposes 3D level, context menu, unbinds properties.
     * </p>
     *
     * @param game the game instance being ended
     */
    @Override
    public void end(Game game) {
        soundEffects.stopAll();
        perspectiveManager.activeIDProperty().unbind();
        removeAndDisposeGameLevel3D();
        disposeContextMenu();
    }

    /**
     * Updates the 3D scene each frame.
     * <p>
     * Skips update if level or 3D level is not yet created.
     * Updates 3D level, HUD, perspective, and plays contextual sounds.
     * </p>
     *
     * @param game the active game instance
     */
    @Override
    public void update(Game game) {
        final long tick = ui.gameContext().clock().tickCount();

        if (optGameLevel().isEmpty()) {
            Logger.info("Tick #{}: Game level not yet created, update ignored", tick);
            return;
        }

        if (gameLevel3D == null) {
            Logger.info("Tick #{}: 3D game level not yet created, update ignored", tick);
            return;
        }

        final GameLevel level = optGameLevel().get();
        gameLevel3D.update();
        updateHUD3D(level);
        perspectiveManager.updatePerspective(level);

        soundEffects.setEnabled(!level.isDemoLevel());
        soundEffects.playLevelPlayingSound(level);
    }

    @Override
    public void onScroll(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() < 0) {
            perspectiveManager.actionDroneClimb.executeIfEnabled(ui);
        } else if (scrollEvent.getDeltaY() > 0) {
            perspectiveManager.actionDroneDescent.executeIfEnabled(ui);
        }
    }

    @Override
    public Optional<SubScene> optSubScene() {
        return Optional.of(subScene);
    }

    @Override
    public Optional<GameUI_ContextMenu> supplyContextMenu(Game game) {
        contextMenu = new PlaySceneContextMenu(ui);
        return Optional.of(contextMenu);
    }

    @Override
    public GameUI ui() {
        return ui;
    }

    // ────────────────────────────────────────────────────────────────────────────
    // GameEventListener implementations (delegated to handler)
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    public void onGameStateChange(GameStateChangeEvent event) {
        if (event.newState() instanceof TestState) {
            optGameLevel().ifPresent(level -> {
                replaceGameLevel3D(level);
                gameLevel3D.messageManager().showLevelTestMessage(level);
                GameUI.PROPERTY_3D_PERSPECTIVE_ID.set(PerspectiveID.TOTAL);
            });
            return;
        }
        level3D_EventHandler.handleGameStateChange(event, gameLevel3D);
    }

    @Override
    public void onBonusActivated(BonusActivatedEvent event) {
        level3D_EventHandler.onBonusActivated(event, gameLevel3D);
    }

    @Override
    public void onBonusEaten(BonusEatenEvent event) {
        level3D_EventHandler.onBonusEaten(event, gameLevel3D);
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent event) {
        level3D_EventHandler.onBonusExpired(event, gameLevel3D);
    }

    @Override
    public void onGameContinues(GameContinuedEvent event) {
        level3D_EventHandler.onGameContinues(event, gameLevel3D);
    }

    @Override
    public void onGameStarts(GameStartedEvent event) {
        level3D_EventHandler.onGameStarts(event, gameLevel3D);
    }

    @Override
    public void onGhostEaten(GhostEatenEvent event) {
        level3D_EventHandler.onGhostEaten(event, gameLevel3D);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent event) {
        replaceGameLevel3D(event.level());
    }

    @Override
    public void onLevelStarts(LevelStartedEvent event) {
        final GameLevel gameLevel = event.level();
        if (gameLevel3D == null) {
            Logger.warn("Level starts but no 3D level exists? Creating one...");
            replaceGameLevel3D(gameLevel);
        }
        final State<Game> state = gameLevel.game().control().state();
        if (state instanceof TestState) {
            replaceGameLevel3D(gameLevel);
            gameLevel3D.maze3D().food().energizers3D().forEach(Energizer3D::startPumping);
            gameLevel3D.messageManager().showLevelTestMessage(gameLevel);
        } else {
            if (!gameLevel.isDemoLevel() &&
                    state.nameMatches(STARTING_GAME_OR_LEVEL.name(), LEVEL_TRANSITION.name())) {
                gameLevel3D.messageManager().showReadyMessage();
            }
        }
        gameLevel3D.rebuildLevelCounter3D();
        replaceActionBindings(gameLevel);
        fadeInAnimation.play();
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        level3D_EventHandler.onPacEatsFood(e, gameLevel3D);
    }

    @Override
    public void onPacGetsPower(PacGetsPowerEvent e) {
        level3D_EventHandler.onPacGetsPower(e, gameLevel3D);
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent e) {
        level3D_EventHandler.onPacLostPower(e, gameLevel3D);
    }

    @Override
    public void onSpecialScoreReached(SpecialScoreReachedEvent e) {
        level3D_EventHandler.onSpecialScoreReached(e, gameLevel3D);
    }

    @Override
    public void onSwitch_2D_3D(GameScene scene2D) {
        optGameLevel().ifPresent(level -> {
            if (gameLevel3D == null) {
                replaceGameLevel3D(level);
            }
            initFood3D(level.worldMap().foodLayer(), level.game().control().state().nameMatches(HUNTING.name(), EATING_GHOST.name()));

            final PacBase3D pac3D = gameLevel3D.pac3D().orElseThrow(() -> new IllegalStateException("Pac3D not found in GameLevel3D"));
            initPac3D(pac3D, level);

            gameLevel3D.livesCounter3D().ifPresent(livesCounter3D -> livesCounter3D.startTracking(pac3D));
            gameLevel3D.rebuildLevelCounter3D();

            updateHUD3D(level);
            replaceActionBindings(level);

            if (level.game().control().state().nameMatches(HUNTING.name())) {
                if (level.pac().powerTimer().isRunning()) {
                    soundEffects.playPacPowerSound();
                }
            }

            fadeInAnimation.play();
        });
    }

    private void initPac3D(PacBase3D pac3D, GameLevel level) {
        pac3D.init(level);
        pac3D.update(level);
    }

    private void initFood3D(FoodLayer foodLayer, boolean startEnergizerPumping) {
        final Maze3D maze3D = gameLevel3D.maze3D();

        maze3D.food().pellets3D().forEach(pellet3D ->
            pellet3D.setVisible(!foodLayer.hasEatenFoodAtTile(pellet3D.tile())));

        maze3D.food().energizers3D().forEach(energizer3D ->
            energizer3D.shape().setVisible(!foodLayer.hasEatenFoodAtTile(energizer3D.tile())));

        if (startEnergizerPumping) {
            maze3D.food().energizers3D().stream()
                .filter(energizer3D -> energizer3D.shape().isVisible())
                .forEach(Energizer3D::startPumping);
        }
    }

    @Override
    public void onUnspecifiedChange(UnspecifiedChangeEvent event) {
        // TODO: remove (currently only used by GameState.TESTING_CUT_SCENES)
        ui.views().getPlayView().updateGameScene(gameContext().currentGame(), true);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Protected / helper methods
    // ────────────────────────────────────────────────────────────────────────────

    protected Optional<GameLevel> optGameLevel() {
        return gameContext().currentGame().optGameLevel();
    }

    /**
     * Binds global scene-level keyboard actions (perspective switching, drone controls, etc.).
     */
    protected void bindSceneActions() {
        final Set<ActionBinding> bindings = Set.of(
            new ActionBinding(ACTION_PERSPECTIVE_PREVIOUS,           alt(KeyCode.LEFT)),
            new ActionBinding(ACTION_PERSPECTIVE_NEXT,               alt(KeyCode.RIGHT)),
            new ActionBinding(perspectiveManager.actionDroneClimb,   control(KeyCode.MINUS)),
            new ActionBinding(perspectiveManager.actionDroneDescent, control(KeyCode.PLUS)),
            new ActionBinding(perspectiveManager.actionDroneReset,   control(KeyCode.DIGIT0)),
            new ActionBinding(ACTION_TOGGLE_DRAW_MODE,               alt(KeyCode.W))
        );
        actionBindings.registerAllFrom(bindings);
    }

    /**
     * Factory method to create a new 3D level representation.
     * <p>May be overridden by subclasses for variant-specific 3D levels.</p>
     *
     * @param level the logical game level
     * @return new 3D level instance
     */
    protected GameLevel3D createGameLevel3D(GameLevel level) {
        return new GameLevel3D(ui.currentConfig(), level);
    }

    /**
     * Hook for replacing action bindings when a new level starts.
     * <p>Empty by default — override in subclasses if needed (e.g. variant-specific keys).</p>
     *
     * @param level the new game level
     */
    protected void replaceActionBindings(GameLevel level) {
        // No-op — override in subclasses if variant needs different bindings
    }

    /**
     * Updates the 3D score and high-score display based on current game state.
     *
     * @param level current game level
     */
    protected void updateHUD3D(GameLevel level) {
        final Score score = level.game().score(), highScore = level.game().highScore();
        if (score.isEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        } else {
            // Show "GAME OVER" when score is disabled
            Color color = ui.currentConfig().assets().color("color.game_over_message");
            scores3D.showTextForScore(ui.translate("score.game_over"), color);
        }
        // High score always visible
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ────────────────────────────────────────────────────────────────────────────

    private void replaceScores3D() {
        if (scores3D != null) {
            subSceneRoot.getChildren().remove(scores3D);
        }
        createScores3D();
        subSceneRoot.getChildren().add(scores3D);
    }

    // Scores are always displayed towards viewer, independent of level camera perspective
    private void createScores3D() {
        scores3D = new Scores3D(ui.translate("score.score"), ui.translate("score.high_score"), GameUI_Resources.FONT_ARCADE_8);

        scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
        scores3D.rotateProperty().bind(camera.rotateProperty());

        scores3D.translateXProperty().bind(gameLevel3DParent.translateXProperty().add(TS));
        scores3D.translateYProperty().bind(gameLevel3DParent.translateYProperty().subtract(4.5 * TS));
        scores3D.translateZProperty().bind(gameLevel3DParent.translateZProperty().subtract(4.5 * TS));

        scores3D.setVisible(false);
    }

    private void replaceGameLevel3D(GameLevel level) {
        Logger.info("Replacing game level 3D...");

        if (gameLevel3D != null) {
            gameLevel3D.dispose();
        }

        gameLevel3D = createGameLevel3D(level);

        final var animations = new GameLevel3DAnimations(gameLevel3D, soundEffects);
        gameLevel3D.setAnimations(animations);

        final PacBase3D pac3D = gameLevel3D.pac3D().orElseThrow(
            () -> new IllegalStateException("Pac3D not found in GameLevel3D"));

        pac3D.init(level);
        gameLevel3D.ghosts3D().forEach(ghost3D -> ghost3D.init(level));
        gameLevel3D.livesCounter3D().ifPresent(livesCounter3D -> livesCounter3D.startTracking(pac3D));

        gameLevel3DParent.getChildren().setAll(gameLevel3D);
        Logger.info("Created and added new game level 3D to play scene");
    }

    private void removeAndDisposeGameLevel3D() {
        if (gameLevel3D != null) {
            gameLevel3DParent.getChildren().clear();
            gameLevel3D.dispose();
            gameLevel3D = null;
        }
    }

    private void disposeContextMenu() {
        if (contextMenu != null) {
            contextMenu.dispose();
        }
    }
}