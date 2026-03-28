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
import de.amr.pacmanfx.ui.*;
import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.d3.animation.PlaySceneFadeInAnimation;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveManager;
import de.amr.pacmanfx.ui.layout.GameUI_ContextMenu;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.actor.Pac3D;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import de.amr.pacmanfx.uilib.model3D.world.Scores3D;
import de.amr.pacmanfx.uilib.widgets.CoordinateSystem;
import javafx.beans.value.ChangeListener;
import javafx.scene.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.model.GameControl.CommonGameState.LEVEL_TRANSITION;
import static de.amr.pacmanfx.model.GameControl.CommonGameState.STARTING_GAME_OR_LEVEL;
import static de.amr.pacmanfx.ui.GameUI.PROPERTY_3D_DRAW_MODE;
import static de.amr.pacmanfx.ui.GameUI.PROPERTY_3D_LIGHT_COLOR;
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
public class PlayScene3D implements GameScene, DisposableGraphicsObject {

    /** Duration of the fade-in animation when the 3D scene becomes active. */
    public static final Duration FADE_IN_DURATION = Duration.seconds(3);

    protected final Group subSceneRoot = new Group();
    protected final Group gameLevel3DParent = new Group();
    protected final PerspectiveManager perspectiveManager;
    protected final PerspectiveCamera camera = new PerspectiveCamera(true);
    protected final SubScene subScene;

    protected ActionBindingsManager actionBindings = ActionBindingsManager.NO_BINDINGS;
    protected GameUI ui;
    protected GameLevel3D level3D;
    protected Scores3D scores3D;
    protected PlaySceneContextMenu contextMenu;
    protected GameSoundEffects soundEffects;
    protected AmbientLight ambientLight;

    private final ChangeListener<DrawMode> drawModeChangeListener = (_, _, drawMode) -> {
        if (level3D != null) {
            Ufx.setDrawMode(level3D, drawMode);
        }
    };

    /**
     * Creates a new 3D play scene with default camera, sub-scene, axes, and perspective manager.
     */
    public PlayScene3D() {
        perspectiveManager = new PerspectiveManager(camera);
        // Initial size is irrelevant (will be bound to parent scene size later)
        subScene = new SubScene(subSceneRoot, 88, 88, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);

        final var coordinateSystem = new CoordinateSystem();
        coordinateSystem.visibleProperty().bind(GameUI.PROPERTY_3D_AXES_VISIBLE);

        ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PROPERTY_3D_LIGHT_COLOR);

        subSceneRoot.getChildren().addAll(gameLevel3DParent, coordinateSystem, ambientLight);
        bindSceneActions();
    }

    public SubScene subScene() {
        return subScene;
    }

    public Optional<GameLevel3D> optGameLevel3D() {
        return Optional.ofNullable(level3D);
    }

    public PerspectiveManager perspectiveManager() {
        return perspectiveManager;
    }

    public Optional<Scores3D> optScores3D() {
        return Optional.ofNullable(scores3D);
    }

    public GameSoundEffects soundEffects() {
        return soundEffects;
    }

    public void fadeIn() {
        new PlaySceneFadeInAnimation(FADE_IN_DURATION, this).play();
    }

    @Override
    public void dispose() {
        actionBindings.dispose();
        perspectiveManager.dispose();
        disposeContextMenu();
        removeAndDisposeGameLevel3D();
        cleanupLight(ambientLight);
        ambientLight = null;
    }

    @Override
    public void onEmbed(GameUI ui) {
        this.ui = requireNonNull(ui);
        this.soundEffects = ui.currentConfig().getGameSoundEffects(ui.soundManager());
        // TODO: reconsider whether scores need recreation here (variant/font change?)
        replaceScores3D();
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
        subScene.setFill(Color.BLACK);
        PROPERTY_3D_DRAW_MODE.addListener(drawModeChangeListener);
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
        PROPERTY_3D_DRAW_MODE.removeListener(drawModeChangeListener);
        //removeAndDisposeGameLevel3D();
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

        if (level3D == null) {
            Logger.info("Tick #{}: 3D game level not yet created, update ignored", tick);
            return;
        }

        final GameLevel level = optGameLevel().get();
        level3D.entities().all().forEach(e -> e.update(level));
        updateHUD3D(level);
        perspectiveManager.updatePerspective(level);
        soundEffects.setEnabled(!level.isDemoLevel());
        soundEffects.playLevelPlayingSound(level);
    }

    @Override
    public void onScroll(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() < 0) {
            perspectiveManager.actionDroneClimb().executeIfEnabled(ui);
        } else if (scrollEvent.getDeltaY() > 0) {
            perspectiveManager.actionDroneDescent().executeIfEnabled(ui);
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
                level3D.messageManager().showLevelTestMessage(level);
                GameUI.PROPERTY_3D_PERSPECTIVE_ID.set(PerspectiveID.TOTAL);
            });
            return;
        }
        level3D.handleGameStateChange(ui, event);
    }

    @Override
    public void onBonusActivated(BonusActivatedEvent event) {
        level3D.onBonusActivated(event);
    }

    @Override
    public void onBonusEaten(BonusEatenEvent event) {
        level3D.onBonusEaten(event);
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent event)  {
        level3D.onBonusExpired(event);
    }

    @Override
    public void onGameContinues(GameContinuedEvent event) {
        level3D.onGameContinues(event);
    }

    @Override
    public void onGameStarts(GameStartedEvent event) {
        level3D.onGameStarts(event);
    }

    @Override
    public void onGhostEaten(GhostEatenEvent event) {
        level3D.onGhostEaten(event);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent event) {
        replaceGameLevel3D(event.level());
    }

    @Override
    public void onLevelStarts(LevelStartedEvent event) {
        final GameLevel level = event.level();
        if (level3D == null) {
            Logger.warn("Level starts but no 3D level exists? Creating one...");
            replaceGameLevel3D(level);
        }
        final State<Game> state = level.game().control().state();
        if (state instanceof TestState) {
            replaceGameLevel3D(level);
            level3D.entities().all(Energizer3D.class).forEach(Energizer3D::startPumping);
            level3D.messageManager().showLevelTestMessage(level);
        } else {
            if (!level.isDemoLevel() &&
                    state.nameMatches(STARTING_GAME_OR_LEVEL.name(), LEVEL_TRANSITION.name())) {
                level3D.messageManager().showReadyMessage();
            }
        }
        level3D.entities().all().forEach(e -> e.init(level));
        replaceActionBindings(level);
        fadeIn();
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        level3D.onPacEatsFood(e, gameContext().clock().tickCount());
    }

    @Override
    public void onPacGetsPower(PacGetsPowerEvent e) {
        level3D.onPacGetsPower(e);
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent e) {
        level3D.onPacLostPower(e);
    }

    @Override
    public void onSpecialScoreReached(SpecialScoreReachedEvent e) {
        level3D.onSpecialScoreReached(e);
    }

    @Override
    public void onUnspecifiedChange(UnspecifiedChangeEvent event) {
        // TODO: remove (currently only used by GameState.TESTING_CUT_SCENES)
        ui.views().getPlayView().updateGameScene(gameContext().game(), true);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Protected / helper methods
    // ────────────────────────────────────────────────────────────────────────────

    protected Optional<GameLevel> optGameLevel() {
        return gameContext().game().optGameLevel();
    }

    /**
     * Binds global scene-level keyboard actions (perspective switching, drone controls, etc.).
     */
    protected void bindSceneActions() {
        final Set<ActionBinding> bindings = Set.of(
            new ActionBinding(ACTION_PERSPECTIVE_PREVIOUS,             alt(KeyCode.LEFT)),
            new ActionBinding(ACTION_PERSPECTIVE_NEXT,                 alt(KeyCode.RIGHT)),
            new ActionBinding(perspectiveManager.actionDroneClimb(),   control(KeyCode.MINUS)),
            new ActionBinding(perspectiveManager.actionDroneDescent(), control(KeyCode.PLUS)),
            new ActionBinding(perspectiveManager.actionDroneReset(),   control(KeyCode.DIGIT0)),
            new ActionBinding(ACTION_TOGGLE_DRAW_MODE,                 alt(KeyCode.W))
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
    protected GameLevel3D createGameLevel3D(GameLevel level, UIConfig uiConfig) {
        final var level3D = new GameLevel3D(level, uiConfig, soundEffects, ui.localizedTexts());
        level3D.entities().all().forEach(e -> e.init(level));
        level3D.startTrackingPac();
        return level3D;
    }

    /**
     * Hook for replacing action bindings when a new level starts.
     * <p>Empty by default — override in subclasses if needed (e.g. variant-specific keys).</p>
     *
     * @param level the new game level
     */
    public void replaceActionBindings(GameLevel level) {
        // No-op — override in subclasses if variant needs different bindings
    }

    /**
     * Updates the 3D score and high-score display based on current game state.
     *
     * @param level current game level
     */
    public void updateHUD3D(GameLevel level) {
        final Score score = level.game().score(), highScore = level.game().highScore();
        if (score.isEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        } else {
            // Show "GAME OVER" when score is disabled
            final Color color = ui.currentConfig().assets().color("color.game_over_message");
            scores3D.showTextForScore(ui.translate("score.game_over"), color);
        }
        // High score always visible
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ────────────────────────────────────────────────────────────────────────────

    public void initPac3D(Pac3D pac3D, GameLevel level) {
        pac3D.init(level);
        pac3D.update(level);
    }

    public void initFood3D(FoodLayer foodLayer, boolean startEnergizerPumping) {
        level3D.entities().all(Pellet3D.class)
            .forEach(p3D -> p3D.shape().setVisible(!foodLayer.hasEatenFoodAtTile(p3D.tile())));
        level3D.entities().all(Energizer3D.class)
            .forEach(e3D -> e3D.shape().setVisible(!foodLayer.hasEatenFoodAtTile(e3D.tile())));
        if (startEnergizerPumping) {
            level3D.entities().where(Energizer3D.class, e3D -> e3D.shape().isVisible())
                .forEach(Energizer3D::startPumping);
        }
    }

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

    public void replaceGameLevel3D(GameLevel level) {
        if (level3D != null) {
            Logger.info("Replacing game level 3D...");
            level3D.dispose();
        }
        level3D = createGameLevel3D(level, ui.currentConfig());
        gameLevel3DParent.getChildren().setAll(level3D);
        Logger.info("Created and added new game level 3D to play scene");
    }

    private void removeAndDisposeGameLevel3D() {
        if (level3D != null) {
            gameLevel3DParent.getChildren().clear();
            level3D.dispose();
            level3D = null;
        }
    }

    private void disposeContextMenu() {
        if (contextMenu != null) {
            contextMenu.dispose();
        }
    }
}