/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl.CommonGameState;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.ui.ActionBindingsManager;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Resources;
import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.layout.GameUI_ContextMenu;
import de.amr.pacmanfx.ui.sound.PlayingSoundEffects;
import de.amr.pacmanfx.uilib.assets.RandomTextPicker;
import de.amr.pacmanfx.uilib.model3D.Bonus3D;
import de.amr.pacmanfx.uilib.model3D.Energizer3D;
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
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.model.GameControl.CommonGameState.*;
import static de.amr.pacmanfx.ui.action.CommonGameActions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.alt;
import static de.amr.pacmanfx.ui.input.Keyboard.control;
import static java.util.Objects.requireNonNull;

/**
 * 3D implementation of the Pac‑Man play scene.
 *
 * <p>This scene is responsible for rendering and updating the full 3D
 * representation of the current game level, including the maze, actors,
 * food, bonus items, HUD elements, and all camera perspectives. It acts as
 * the central coordinator between the game model, the 3D world, the active
 * camera controller, and the UI framework.</p>
 *
 * <p>The scene manages:</p>
 * <ul>
 *   <li><strong>Lifecycle of the 3D level</strong> – creation, replacement,
 *       disposal, and per‑frame updates of {@code GameLevel3D} and its
 *       subcomponents.</li>
 *   <li><strong>Camera perspectives</strong> – switching between multiple
 *       {@link Perspective} strategies (drone, total, tracking, stalking),
 *       delegating camera control to the active perspective, and handling
 *       user input that affects perspective behavior.</li>
 *   <li><strong>3D rendering infrastructure</strong> – creation of the
 *       {@link SubScene}, camera setup, fade‑in animation, coordinate axes,
 *       and placement of HUD elements such as scores.</li>
 *   <li><strong>Game event handling</strong> – reacting to model events
 *       (food eaten, bonus activated, state changes, level transitions,
 *       Pac‑Man death, etc.) and updating the 3D world accordingly.</li>
 *   <li><strong>Sound orchestration</strong> – enabling/disabling sounds,
 *       playing contextual effects (munching, siren, ghost returning),
 *       and synchronizing audio with game state.</li>
 *   <li><strong>Input bindings</strong> – keyboard and scroll‑wheel
 *       controls for perspective switching, drone height adjustment,
 *       and rendering options.</li>
 *   <li><strong>HUD and messaging</strong> – displaying READY/test messages,
 *       score overlays, and animated text in the 3D world.</li>
 * </ul>
 *
 * <p>The class is intentionally large because it serves as the integration
 * point between many subsystems: the game model, the 3D rendering layer,
 * the UI, input handling, and audio. It does not perform rendering itself;
 * instead it delegates to {@code GameLevel3D} and the active
 * {@link Perspective} implementation.</p>
 *
 * <p>Instances of this scene are created and managed by the {@link GameUI}.
 * The scene is activated when switching from the 2D play scene to the 3D
 * view, and it remains active until the user switches back or the game
 * ends.</p>
 */
public class PlayScene3D implements GameScene {

    public static final Color SCENE_FILL_DARK = Color.BLACK;
    public static final Color SCENE_FILL_BRIGHT = Color.TRANSPARENT;
    public static final Duration FADE_IN_DURATION = Duration.seconds(3);

    protected final Group subSceneRoot = new Group();
    protected final Group level3DParent = new Group();
    protected final PerspectiveManager perspectiveManager;
    protected final PerspectiveCamera camera = new PerspectiveCamera(true);
    protected final SubScene subScene;
    protected final FadeInAnimation fadeInAnimation;

    protected PlayingSoundEffects soundEffects;
    protected ActionBindingsManager actionBindings = ActionBindingsManager.NO_BINDINGS;
    protected GameUI ui;
    protected GameLevel3D gameLevel3D;
    protected Scores3D scores3D;
    protected RandomTextPicker<String> pickerGameOverMessages;
    protected PlaySceneContextMenu contextMenu;

    public class FadeInAnimation {
        private final Timeline timeline;

        public FadeInAnimation(Duration fadeInDuration) {
            timeline = new Timeline(
                new KeyFrame(Duration.ZERO, _ -> {
                    subScene.setFill(SCENE_FILL_DARK);
                    gameLevel3D.setVisible(true);
                    scores3D.setVisible(true);
                    //TODO Check if this is needed:
                    perspectiveManager.currentPerspective().ifPresent(Perspective::startControlling);
                }),
                new KeyFrame(fadeInDuration,
                    new KeyValue(subScene.fillProperty(), SCENE_FILL_BRIGHT, Interpolator.EASE_IN))
            );
        }

        public void play() {
            timeline.playFromStart();
        }
    }

    public PlayScene3D() {
        final var axes3D = new CoordinateSystem();
        axes3D.visibleProperty().bind(GameUI.PROPERTY_3D_AXES_VISIBLE);

        subSceneRoot.getChildren().setAll(level3DParent, axes3D);

        // Initial scene size is irrelevant (size gets bound to parent scene size eventually)
        subScene = new SubScene(subSceneRoot, 88, 88, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(SCENE_FILL_DARK);

        perspectiveManager = new PerspectiveManager(camera);
        fadeInAnimation = new FadeInAnimation(FADE_IN_DURATION);

        bindSceneActions();
    }

    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
        soundEffects = new PlayingSoundEffects(ui.soundManager());
        soundEffects.setMunchingSoundDelay(ui.currentConfig().munchingSoundDelay());
        pickerGameOverMessages = RandomTextPicker.fromBundle(ui.localizedTexts(), "game.over");
        //TODO reconsider this
        replaceScores3D();
    }

    @Override
    public void dispose() {
        actionBindings.dispose();
        perspectiveManager.dispose();
        if (gameLevel3D != null) {
            gameLevel3D.dispose();
            gameLevel3D = null;
        }
    }

    @Override
    public GameUI ui() {
        return ui;
    }

    @Override
    public ActionBindingsManager actionBindings() {
        return actionBindings;
    }

    @Override
    public Optional<GameUI_ContextMenu> supplyContextMenu(Game game) {
        contextMenu = new PlaySceneContextMenu(ui);
        return Optional.of(contextMenu);
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
    public void init(Game game) {
        game.hud().score(true).show();
        perspectiveManager.activeIDProperty().bind(GameUI.PROPERTY_3D_PERSPECTIVE_ID);
    }

    @Override
    public void end(Game game) {
        soundEffects.stopAll();
        if (gameLevel3D != null) {
            gameLevel3D.dispose();
            gameLevel3D = null;
        }
        level3DParent.getChildren().clear();
        if (contextMenu != null) {
            contextMenu.dispose();
        }
        perspectiveManager.activeIDProperty().unbind();
    }

    @Override
    public void update(Game game) {
        // update is already called before the game level has been created!
        if (optGameLevel().isEmpty()) {
            Logger.info("Tick #{}: Game level not yet created, update ignored", ui.gameContext().clock().tickCount());
            return;
        }

        // update is already called before the 3D game level has been created!
        if (gameLevel3D == null) {
            Logger.info("Tick #{}: 3D game level not yet created", ui.gameContext().clock().tickCount());
            return;
        }

        optGameLevel().ifPresent(level -> {
            gameLevel3D.update();
            perspectiveManager.updatePerspective(level);
            updateHUD(level);
            soundEffects.setEnabled(!level.isDemoLevel());
            soundEffects.playLevelPlayingSound(level);
        });
    }

    @Override
    public Optional<SubScene> optSubScene() {
        return Optional.of(subScene);
    }

    // Game event handlers

    @Override
    public void onBonusActivated(BonusActivatedEvent event) {
        optBonus().ifPresent(bonus -> {
            gameLevel3D.updateBonus3D(bonus);
            soundEffects.playBonusActiveSound();
        });
    }

    @Override
    public void onBonusEaten(BonusEatenEvent event) {
        optBonus().ifPresent(_ -> {
            gameLevel3D.bonus3D().ifPresent(Bonus3D::showEaten);
            soundEffects.playBonusEatenSound();
        });
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent event) {
        optBonus().ifPresent(_ -> {
            gameLevel3D.bonus3D().ifPresent(Bonus3D::expire);
            soundEffects.playBonusExpiredSound();
        });
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent event) {
        final State<Game> gameState = event.newState();
        final Game game = gameContext().currentGame();

        if (gameState instanceof TestState) {
            game.optGameLevel().ifPresent(level -> {
                replaceGameLevel3D(level);
                gameLevel3D.showTestMessage();
                GameUI.PROPERTY_3D_PERSPECTIVE_ID.set(PerspectiveID.TOTAL);
            });
            return;
        }

        //TODO check if this state change handler is needed
        if (stateIs(gameState, STARTING_GAME_OR_LEVEL) && gameLevel3D != null) {
            gameLevel3D.onStartingGame();
        }
        else if (stateIs(gameState, HUNTING)) {
            gameLevel3D.onHuntingStart();
        }
        else if (stateIs(gameState, PACMAN_DYING)) {
            gameLevel3D.onPacManDying(gameState, ui.soundManager());
        }
        else if (stateIs(gameState, EATING_GHOST)) {
            gameLevel3D.onEatingGhost();
        }
        else if (stateIs(gameState, LEVEL_COMPLETE)) {
            gameLevel3D.onLevelComplete(gameState, ui.soundManager());
        }
        else if (stateIs(gameState, GAME_OVER)) {
            gameLevel3D.onGameOver(gameState, ui.soundManager());
            final boolean showMsg = randomInt(0, 1000) < 250;
            if (!game.level().isDemoLevel() && showMsg) {
                ui.showFlashMessage(Duration.seconds(2.5), pickerGameOverMessages.nextText());
            }
        }
    }

    @Override
    public void onGameContinues(GameContinuedEvent event) {
        if (gameLevel3D != null) {
            gameLevel3D.showReadyMessage();
        }
    }

    @Override
    public void onGameStarts(GameStartedEvent event) {
        final Game game = gameContext().currentGame();
        final State<Game> state = game.control().state();
        final boolean silent = game.level().isDemoLevel() || state instanceof TestState;
        if (!silent) {
            soundEffects.playGameReadySound();
        }
    }

    @Override
    public void onGhostEaten(GhostEatenEvent event) {
        soundEffects.playGhostEatenSound();
    }


    @Override
    public void onLevelCreated(LevelCreatedEvent event) {
        gameContext().currentGame().optGameLevel().ifPresent(this::replaceGameLevel3D);
    }

    @Override
    public void onLevelStarts(LevelStartedEvent event) {
        if (optGameLevel().isEmpty()) {
            Logger.error("No game level exists on level start? WTF!");
            return;
        }
        optGameLevel().ifPresent(gameLevel -> {
            final State<Game> state = gameLevel.game().control().state();
            if (state instanceof TestState) {
                replaceGameLevel3D(gameLevel); //TODO check when to destroy previous level
                gameLevel3D.maze3D().food().energizers3D().forEach(Energizer3D::startPumping);
                gameLevel3D.showTestMessage();
            }
            else {
                if (!gameLevel.isDemoLevel() && state.nameMatches(STARTING_GAME_OR_LEVEL.name(), CommonGameState.LEVEL_TRANSITION.name())) {
                    gameLevel3D.showReadyMessage();
                }
            }
            gameLevel3D.rebuildLevelCounter3D(ui.currentConfig().config3D().levelCounter());
            replaceActionBindings(gameLevel);
            fadeInAnimation.play();
        });
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        if (e.allPellets()) {
            gameLevel3D.eatAllPellets3D();
        } else {
            gameLevel3D.eatFood(e.pac().tile());
            soundEffects.playPacMunchingSound(gameContext().clock().tickCount());
        }
    }

    @Override
    public void onPacGetsPower(PacGetsPowerEvent e) {
        optGameLevel().ifPresent(gameLevel -> {
            final Game game = gameLevel.game();
            soundEffects.stopSiren();
            if (!game.isLevelCompleted(gameLevel)) {
                gameLevel3D.pac3D().setMovementPowerMode(true);
                gameLevel3D.animations().ifPresent(animations -> animations.wallColorFlashingAnimation().playFromStart());
                soundEffects.playPacPowerSound();
            }
        });
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent e) {
        optGameLevel().ifPresent(_ -> {
            gameLevel3D.pac3D().setMovementPowerMode(false);
            gameLevel3D.animations().ifPresent(animations -> animations.wallColorFlashingAnimation().stop());
            soundEffects.stopPacPowerSound();
        });
    }

    @Override
    public void onSpecialScoreReached(SpecialScoreReachedEvent e) {
        soundEffects.playExtraLifeSound();
    }

    @Override
    public void onSwitch_2D_3D(GameScene scene2D) {
        if (optGameLevel().isEmpty()) {
            return;
        }
        optGameLevel().ifPresent(level -> {
            if (gameLevel3D == null) {
                replaceGameLevel3D(level);
            }
            level.pac().show();
            level.ghosts().forEach(Ghost::show);

            gameLevel3D.pac3D().init(level);
            gameLevel3D.pac3D().update(level);

            final FoodLayer foodLayer = level.worldMap().foodLayer();
            final Maze3D maze3D = gameLevel3D.maze3D();
            final MazeFood3D mazeFood3D = maze3D.food();
            final State<?> state = level.game().control().state();

            mazeFood3D.pellets3D().forEach(pellet3D ->
                    pellet3D.setVisible(!foodLayer.hasEatenFoodAtTile(pellet3D.tile())));

            mazeFood3D.energizers3D().forEach(energizer3D ->
                    energizer3D.shape().setVisible(!foodLayer.hasEatenFoodAtTile(energizer3D.tile())));

            if (state.nameMatches(HUNTING.name(), EATING_GHOST.name())) { //TODO check this
                mazeFood3D.energizers3D().stream()
                        .filter(energizer3D -> energizer3D.shape().isVisible())
                        .forEach(Energizer3D::startPumping);
            }

            if (state.nameMatches(HUNTING.name())) {
                if (level.pac().powerTimer().isRunning()) {
                    soundEffects.playPacPowerSound();
                }
                gameLevel3D.livesCounter3D().startTracking(gameLevel3D.pac3D());
            }

            gameLevel3D.rebuildLevelCounter3D(ui.currentConfig().config3D().levelCounter());
            updateHUD(level);
            replaceActionBindings(level);
            fadeInAnimation.play();
        });
    }

    @Override
    public void onUnspecifiedChange(UnspecifiedChangeEvent event) {
        // TODO: remove (this is only used by game state GameState.TESTING_CUT_SCENES)
        ui.views().getPlayView().updateGameScene(gameContext().currentGame(), true);
    }

    // other stuff

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

    public Optional<GameLevel3D> level3D() {
        return Optional.ofNullable(gameLevel3D);
    }

    protected GameLevel3D createGameLevel3D(GameLevel level) {
        return new GameLevel3D(ui, level);
    }

    protected void replaceActionBindings(GameLevel level) {}

    protected void updateHUD(GameLevel level) {
        final Score score = level.game().score(), highScore = level.game().highScore();
        if (score.isEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else { // disabled, show text "GAME OVER"
            Color color = ui.currentConfig().assets().color("color.game_over_message");
            scores3D.showTextForScore(ui.translate("score.game_over"), color);
        }
        // Always show high score
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
    }

    // private

    private boolean stateIs(State<Game> gameState, CommonGameState commonGameState) {
        return gameState.nameMatches(commonGameState.name());
    }

    private void replaceScores3D() {
        if (scores3D != null) {
            subSceneRoot.getChildren().remove(scores3D);
        }
        createScores3D();
        subSceneRoot.getChildren().add(scores3D);
    }

    private void createScores3D() {
        scores3D = new Scores3D(
            ui.translate("score.score"),
            ui.translate("score.high_score"),
            GameUI_Resources.FONT_ARCADE_8
        );

        // The scores are always displayed in full view, regardless which perspective is used
        scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
        scores3D.rotateProperty().bind(camera.rotateProperty());

        scores3D.translateXProperty().bind(level3DParent.translateXProperty().add(TS));
        scores3D.translateYProperty().bind(level3DParent.translateYProperty().subtract(4.5 * TS));
        scores3D.translateZProperty().bind(level3DParent.translateZProperty().subtract(4.5 * TS));
        scores3D.setVisible(false);
    }

    private void replaceGameLevel3D(GameLevel level) {
        if (gameLevel3D != null) {
            Logger.info("Replacing existing game level 3D...");
            gameLevel3D.getChildren().clear();
            gameLevel3D.dispose();
            Logger.info("Disposed old game level 3D");
        }
        gameLevel3D = createGameLevel3D(level);
        gameLevel3D.pac3D().init(level);
        gameLevel3D.ghosts3D().forEach(ghost3D -> ghost3D.init(level));
        gameLevel3D.livesCounter3D().startTracking(gameLevel3D.pac3D());
        gameLevel3D.setAnimations(new GameLevel3DAnimations(gameLevel3D, ui.soundManager()));

        level3DParent.getChildren().setAll(gameLevel3D);
        Logger.info("Created and added new game level 3D to play scene");
    }

    private Optional<GameLevel> optGameLevel() {
        return gameContext().currentGame().optGameLevel();
    }

    private Optional<Bonus> optBonus() {
        return gameContext().currentGame().optGameLevel().flatMap(GameLevel::optBonus);
    }
}