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
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Resources;
import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.action.ActionBindingsManager;
import de.amr.pacmanfx.ui.d3.animation.PlaySceneFadeInAnimation;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveManager;
import de.amr.pacmanfx.ui.layout.GameUI_ContextMenu;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.actor.Pac3D;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import de.amr.pacmanfx.uilib.model3D.world.Pellet3D;
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
    protected final Group level3DParent = new Group();
    protected final SubScene subScene;

    protected final PerspectiveManager perspectives;
    protected final PerspectiveCamera camera = new PerspectiveCamera(true);

    protected ActionBindingsManager actionBindings = ActionBindingsManager.NO_BINDINGS;

    protected GameUI ui;
    protected GameLevel3D level3D;
    protected Scores3D scores3D;
    protected PlaySceneContextMenu contextMenu;
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
        perspectives = new PerspectiveManager(camera);
        // Initial size is irrelevant (will be bound to parent scene size later)
        subScene = new SubScene(subSceneRoot, 88, 88, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);

        final var coordinateSystem = new CoordinateSystem();
        coordinateSystem.visibleProperty().bind(GameUI.PROPERTY_3D_AXES_VISIBLE);

        ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PROPERTY_3D_LIGHT_COLOR);

        subSceneRoot.getChildren().addAll(level3DParent, coordinateSystem, ambientLight);
        bindPlaySceneActions();
    }

    public SubScene subScene() {
        return subScene;
    }

    public Optional<GameLevel3D> optGameLevel3D() {
        return Optional.ofNullable(level3D);
    }

    protected Optional<GameLevel> optGameLevel() {
        return gameContext().game().optGameLevel();
    }

    public PerspectiveManager perspectiveManager() {
        return perspectives;
    }

    public Optional<Scores3D> optScores3D() {
        return Optional.ofNullable(scores3D);
    }

    public void fadeIn() {
        new PlaySceneFadeInAnimation(FADE_IN_DURATION, this).play();
    }

    @Override
    public void dispose() {
        actionBindings.dispose();
        perspectives.dispose();
        disposeContextMenu();
        removeAndDisposeGameLevel3D();
        cleanupLight(ambientLight);
        ambientLight = null;
    }

    @Override
    public void onEmbed(GameUI ui) {
        this.ui = requireNonNull(ui);
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

    @Override
    public void init(Game game) {
        perspectives.activeIDProperty().bind(GameUI.PROPERTY_3D_PERSPECTIVE_ID);
        subScene.setFill(Color.BLACK);
        PROPERTY_3D_DRAW_MODE.addListener(drawModeChangeListener);
    }

    @Override
    public void end(Game game) {
        soundEffects().ifPresent(GameSoundEffects::stopAll);
        perspectives.activeIDProperty().unbind();
        PROPERTY_3D_DRAW_MODE.removeListener(drawModeChangeListener);
        //removeAndDisposeGameLevel3D();
        disposeContextMenu();
    }

    @Override
    public void onTick(long tick) {
        final GameLevel level = optGameLevel().orElse(null);

        if (level == null) {
            Logger.info("Tick {}: Game level not yet created, update ignored", tick);
            return;
        }

        if (level3D == null) {
            Logger.info("Tick {}: Game level 3D not yet created, update ignored", tick);
            return;
        }

        level3D.entities().all().forEach(e -> e.update(level));
        updateHUD3D(level);
        perspectives.updatePerspective(level);
        soundEffects().ifPresent(soundEffects -> {
            soundEffects.setEnabled(!level.isDemoLevel());
            soundEffects.playLevelRunningSound(level);
        });
    }

    @Override
    public void onScroll(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() < 0) {
            perspectives.actionDroneClimb().executeIfEnabled(ui);
        } else if (scrollEvent.getDeltaY() > 0) {
            perspectives.actionDroneDescent().executeIfEnabled(ui);
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
                level3D.messageManager().showMessage(MessageManager3D.MessageType.TEST, level.number());
                GameUI.PROPERTY_3D_PERSPECTIVE_ID.set(PerspectiveID.TOTAL);
            });
            return;
        }
        if (level3D != null) level3D.handleGameStateChange(ui, event);
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
    public void onGameContinued(GameContinuedEvent event) {
        level3D.onGameContinues(event);
    }

    @Override
    public void onGameStarted(GameStartedEvent event) {
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
    public void onLevelStarted(LevelStartedEvent event) {
        final GameLevel level = event.level();
        final State<Game> state = level.game().flow().state();
        if (state instanceof TestState) {
            replaceGameLevel3D(level);
            level3D.entities().all(Energizer3D.class).forEach(Energizer3D::startPumping);
            level3D.messageManager().showMessage(MessageManager3D.MessageType.TEST, level.number());
        }
        if (level3D == null) {
            throw new IllegalStateException("Level starts but no 3D level exists?");
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
    public void onSpecialScore(SpecialScoreEvent e) {
        level3D.onSpecialScoreReached(e);
    }

    @Override
    public void onGenericChange(GenericChangeEvent event) {
        // TODO: remove (currently only used by GameState.TESTING_CUT_SCENES)
        ui.forceGameSceneUpdate();
    }

    // Other stuff

    /**
     * Binds global scene-level keyboard actions (perspective switching, drone controls, etc.).
     */
    protected void bindPlaySceneActions() {
        final Set<ActionBinding> bindings = Set.of(
            new ActionBinding(ACTION_PERSPECTIVE_PREVIOUS,       alt(KeyCode.LEFT)),
            new ActionBinding(ACTION_PERSPECTIVE_NEXT,           alt(KeyCode.RIGHT)),
            new ActionBinding(perspectives.actionDroneClimb(),   control(KeyCode.MINUS)),
            new ActionBinding(perspectives.actionDroneDescent(), control(KeyCode.PLUS)),
            new ActionBinding(perspectives.actionDroneReset(),   control(KeyCode.DIGIT0)),
            new ActionBinding(ACTION_TOGGLE_DRAW_MODE,           alt(KeyCode.W))
        );
        actionBindings.bindAll(bindings);
    }

    /**
     * Can be overridden by 3D scenes that e.g. decorate the 3D level with
     * additional stuff as done by the Tengen Ms. Pac-Man game that displays the game difficulty, map category etc.
     */
    protected void decorateGameLevel3D(GameLevel3D level3D) {}

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
        // If score is disabled, show "GAME OVER" text
        final Score score = level.game().score();
        if (score.isEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        } else {
            scores3D.showTextForScore(ui.translate("score.game_over"), ui.currentConfig().assets().color("color.game_over_message"));
        }

        // High score is always visible
        final Score highScore = level.game().highScore();
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
    }

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
            level3D.entities().allWhere(Energizer3D.class, e3D -> e3D.shape().isVisible())
                .forEach(Energizer3D::startPumping);
        }
    }

    public void replaceGameLevel3D(GameLevel level) {
        if (level3D != null) {
            Logger.info("Replacing game level 3D...");
            level3D.dispose();
        }
        level3D = new GameLevel3D(level, ui.currentConfig(), ui.localizedTexts());
        decorateGameLevel3D(level3D);
        level3DParent.getChildren().setAll(level3D);
        level3D.entities().all().forEach(e -> e.init(level));
        level3D.startTrackingPac();
        Logger.info("Created and added new game level 3D to play scene");
    }

    // Scores are always displayed towards viewer, independent of level camera perspective
    private void replaceScores3D() {
        if (scores3D != null) {
            subSceneRoot.getChildren().remove(scores3D);
        }
        scores3D = new Scores3D(ui.translate("score.score"), ui.translate("score.high_score"), GameUI_Resources.FONT_ARCADE_8);
        scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
        scores3D.rotateProperty().bind(camera.rotateProperty());
        scores3D.translateXProperty().bind(level3DParent.translateXProperty().add(TS));
        scores3D.translateYProperty().bind(level3DParent.translateYProperty().subtract(4.5 * TS));
        scores3D.translateZProperty().bind(level3DParent.translateZProperty().subtract(4.5 * TS));
        scores3D.setVisible(false);
        subSceneRoot.getChildren().add(scores3D);
    }

    private void removeAndDisposeGameLevel3D() {
        if (level3D != null) {
            level3DParent.getChildren().clear();
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