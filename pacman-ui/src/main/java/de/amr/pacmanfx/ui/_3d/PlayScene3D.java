/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl.StateName;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.ui.ActionBindingsManager;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Resources;
import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.layout.GameUI_ContextMenu;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.assets.RandomTextPicker;
import de.amr.pacmanfx.uilib.model3D.Bonus3D;
import de.amr.pacmanfx.uilib.model3D.Energizer3D;
import de.amr.pacmanfx.uilib.model3D.Scores3D;
import de.amr.pacmanfx.uilib.widgets.CoordinateSystem;
import javafx.animation.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape3D;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.ui.action.CommonGameActions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.alt;
import static de.amr.pacmanfx.ui.input.Keyboard.control;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSecThen;
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

    // Colors for fade effect
    private static final Color SCENE_FILL_DARK = Color.BLACK;
    private static final Color SCENE_FILL_BRIGHT = Color.TRANSPARENT;

    private static final String READY_MESSAGE_TEXT = "READY!";
    private static final String TEST_MESSAGE_TEXT = "LEVEL %d (TEST)";

    private static final float SCENE_FADE_IN_SECONDS = 3;
    private static final float READY_MESSAGE_DISPLAY_SECONDS = 2.5f;

    //TODO fix sound files
    private static final float SIREN_VOLUME = 0.33f;

    public static class PlaySceneContextMenu extends GameUI_ContextMenu implements Disposable {

        private final ToggleGroup perspectivesGroup = new ToggleGroup();

        private final ChangeListener<PerspectiveID> perspectiveListener = (_, _, perspectiveID) -> {
            for (Toggle toggle : perspectivesGroup.getToggles()) {
                if (Objects.equals(toggle.getUserData(), perspectiveID)) {
                    perspectivesGroup.selectToggle(toggle);
                    break;
                }
            }
        };

        public PlaySceneContextMenu(GameUI ui) {
            super(ui);
            final Game game = ui.gameContext().currentGame();
            addLocalizedTitleItem("scene_display");
            addLocalizedActionItem(ACTION_TOGGLE_PLAY_SCENE_2D_3D, "use_2D_scene");
            addLocalizedCheckBox(GameUI.PROPERTY_MINI_VIEW_ON, "pip");
            addLocalizedTitleItem("select_perspective");
            for (PerspectiveID id : PerspectiveID.values()) {
                final RadioMenuItem item = addLocalizedRadioButton("perspective_id_" + id.name());
                item.setUserData(id);
                item.setToggleGroup(perspectivesGroup);
                if (id == GameUI.PROPERTY_3D_PERSPECTIVE_ID.get())  {
                    item.setSelected(true);
                }
                item.setOnAction(_ -> GameUI.PROPERTY_3D_PERSPECTIVE_ID.set(id));
            }
            addLocalizedTitleItem("pacman");
            addLocalizedCheckBox(game.usingAutopilotProperty(), "autopilot");
            addLocalizedCheckBox(game.immuneProperty(), "immunity");
            addSeparator();
            addLocalizedCheckBox(GameUI.PROPERTY_MUTED, "muted");
            addLocalizedActionItem(ACTION_QUIT_GAME_SCENE, "quit");

            GameUI.PROPERTY_3D_PERSPECTIVE_ID.addListener(perspectiveListener);
        }

        @Override
        public void dispose() {
            GameUI.PROPERTY_3D_PERSPECTIVE_ID.removeListener(perspectiveListener);
        }
    }

    private final Map<PerspectiveID, Perspective<GameLevel>> perspectivesByID = new EnumMap<>(PerspectiveID.class);

    private final ObjectProperty<PerspectiveID> perspectiveID = new SimpleObjectProperty<>(PerspectiveID.NEAR_PLAYER);

    protected final GameAction actionDroneClimb = new GameAction("DroneClimb") {
        @Override
        public void execute(GameUI ui) {
            currentPerspective()
                .filter(DronePerspective.class::isInstance)
                .map(DronePerspective.class::cast)
                .ifPresent(DronePerspective::moveUp);
        }
        @Override
        public boolean isEnabled(GameUI ui) {
            return perspectiveID.get() == PerspectiveID.DRONE;
        }
    };

    protected final GameAction actionDroneDescent = new GameAction("DroneDescent") {
        @Override
        public void execute(GameUI ui) {
            currentPerspective()
                .filter(DronePerspective.class::isInstance)
                .map(DronePerspective.class::cast)
                .ifPresent(DronePerspective::moveDown);
        }
        @Override
        public boolean isEnabled(GameUI ui) {
            return perspectiveID.get() == PerspectiveID.DRONE;
        }
    };

    protected final GameAction actionDroneReset = new GameAction("DroneReset") {
        @Override
        public void execute(GameUI ui) {
            currentPerspective()
                .filter(DronePerspective.class::isInstance)
                .map(DronePerspective.class::cast)
                .ifPresent(DronePerspective::moveDefaultHeight);
        }
        @Override
        public boolean isEnabled(GameUI ui) {
            return perspectiveID.get() == PerspectiveID.DRONE;
        }
    };

    /** Key bindings for 3D play-scene navigation and rendering options. */
    protected final Set<ActionBinding> _3D_BINDINGS = Set.of(
        new ActionBinding(ACTION_PERSPECTIVE_PREVIOUS, alt(KeyCode.LEFT)),
        new ActionBinding(ACTION_PERSPECTIVE_NEXT,     alt(KeyCode.RIGHT)),
        new ActionBinding(actionDroneClimb,            control(KeyCode.MINUS)),
        new ActionBinding(actionDroneDescent,          control(KeyCode.PLUS)),
        new ActionBinding(actionDroneReset,            control(KeyCode.DIGIT0)),
        new ActionBinding(ACTION_TOGGLE_DRAW_MODE,     alt(KeyCode.W))
    );

    protected final Group subSceneRoot = new Group();
    protected final Group level3DParent = new Group();
    protected final PerspectiveCamera camera = new PerspectiveCamera(true);
    protected final SubScene subScene;
    protected final Animation fadeInAnimation;

    protected ActionBindingsManager actionBindings = ActionBindingsManager.NO_BINDINGS;
    protected GameUI ui;
    protected GameLevel3D gameLevel3D;
    protected Scores3D scores3D;
    protected RandomTextPicker<String> pickerGameOverMessages;
    protected PlaySceneContextMenu contextMenu;

    public PlayScene3D() {
        final var axes3D = new CoordinateSystem();
        axes3D.visibleProperty().bind(GameUI.PROPERTY_3D_AXES_VISIBLE);

        subSceneRoot.getChildren().setAll(level3DParent, axes3D);

        // Initial scene size is irrelevant (size gets bound to parent scene size eventually)
        subScene = new SubScene(subSceneRoot, 88, 88, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(SCENE_FILL_DARK);

        createPerspectives();
        fadeInAnimation = createFadeInAnimation();
    }

    @Override
    public void dispose() {
        actionBindings.dispose();
        perspectivesByID.clear();
        if (gameLevel3D != null) {
            gameLevel3D.dispose();
            gameLevel3D = null;
        }
    }

    @Override
    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
        pickerGameOverMessages = RandomTextPicker.fromBundle(ui.localizedTexts(), "game.over");
        //TODO reconsider this
        replaceScores3D();
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
            actionDroneClimb.executeIfEnabled(ui);
        } else if (scrollEvent.getDeltaY() > 0) {
            actionDroneDescent.executeIfEnabled(ui);
        }
    }

    @Override
    public void init(Game game) {
        game.hud().score(true).show();
        perspectiveIDProperty().bind(GameUI.PROPERTY_3D_PERSPECTIVE_ID);
    }

    @Override
    public void end(Game game) {
        ui.soundManager().stopAll();
        if (gameLevel3D != null) {
            gameLevel3D.dispose();
            gameLevel3D = null;
        }
        level3DParent.getChildren().clear();
        if (contextMenu != null) {
            contextMenu.dispose();
        }
        perspectiveIDProperty().unbind();
    }

    @Override
    public void update(Game game) {
        final GameLevel level = game.optGameLevel().orElse(null);

        // Scene is already updated 2 ticks before the game level gets created!
        if (level == null) {
            Logger.info("Tick #{}: Game level not yet created, update ignored", ui.clock().tickCount());
            return;
        }

        if (gameLevel3D == null) {
            Logger.info("Tick #{}: 3D game level not yet created", ui.clock().tickCount());
            return;
        }

        gameLevel3D.update();
        updatePerspective(level);
        updateHUD(game);
        ui.soundManager().setEnabled(!level.isDemoLevel());
        updateSound(level);
    }

    @Override
    public Optional<SubScene> optSubScene() {
        return Optional.of(subScene);
    }

    @Override
    public void onSwitch_2D_3D(GameScene scene2D) {
        final Game game = gameContext().currentGame();
        if (game.optGameLevel().isEmpty()) {
            return;
        }
        final GameLevel level = game.level();
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
        final StateMachine.State<?> state = game.control().state();

        mazeFood3D.pellets3D().forEach(pellet3D ->
            pellet3D.setVisible(!foodLayer.hasEatenFoodAtTile((Vector2i) pellet3D.getUserData())));

        mazeFood3D.energizers3D().forEach(energizer3D ->
            energizer3D.shape().setVisible(!foodLayer.hasEatenFoodAtTile(energizer3D.tile())));

        if (state.nameMatches(StateName.HUNTING.name(), StateName.EATING_GHOST.name())) { //TODO check this
            mazeFood3D.energizers3D().stream()
                .filter(energizer3D -> energizer3D.shape().isVisible())
                .forEach(Energizer3D::startPumping);
        }

        if (state.nameMatches(StateName.HUNTING.name())) {
            if (level.pac().powerTimer().isRunning()) {
                ui.soundManager().loop(SoundID.PAC_MAN_POWER);
            }
            gameLevel3D.livesCounter3D().startTracking(gameLevel3D.pac3D());
        }

        gameLevel3D.updateLevelCounter3D();
        updateHUD(game);
        replaceActionBindings(level);
        fadeInAnimation.playFromStart();
    }

    // Game event handlers

    @Override
    public void onBonusActivated(BonusActivatedEvent event) {
        if (gameLevel3D == null) {
            Logger.error("No game level3D exists!");
            return;
        }
        gameContext().currentGame().optGameLevel().flatMap(GameLevel::optBonus).ifPresent(bonus -> {
            gameLevel3D.updateBonus3D(bonus);
            ui.soundManager().loop(SoundID.BONUS_ACTIVE);
        });
    }

    @Override
    public void onBonusEaten(BonusEatenEvent event) {
        if (gameLevel3D == null) {
            Logger.error("No game level3D exists!");
            return;
        }
        gameContext().currentGame().optGameLevel().flatMap(GameLevel::optBonus).ifPresent(_ -> {
            gameLevel3D.bonus3D().ifPresent(Bonus3D::showEaten);
            ui.soundManager().stop(SoundID.BONUS_ACTIVE);
            ui.soundManager().play(SoundID.BONUS_EATEN);
        });
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent event) {
        if (gameLevel3D == null) {
            Logger.error("No game level3D exists!");
            return;
        }
        gameContext().currentGame().optGameLevel().flatMap(GameLevel::optBonus).ifPresent(_ -> {
            gameLevel3D.bonus3D().ifPresent(Bonus3D::expire);
            ui.soundManager().stop(SoundID.BONUS_ACTIVE);
        });
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent changeEvent) {
        final Game game = gameContext().currentGame();
        final StateMachine.State<Game> newState = changeEvent.newState();
        if (newState instanceof TestState) {
            game.optGameLevel().ifPresent(level -> {
                replaceGameLevel3D(level);
                showTestMessage(level.worldMap(), level.number());
                GameUI.PROPERTY_3D_PERSPECTIVE_ID.set(PerspectiveID.TOTAL);
            });
        }
        else {
            if (newState.nameMatches(StateName.HUNTING.name())) {
                gameLevel3D.onHuntingStart();
            }
            else if (newState.nameMatches(StateName.PACMAN_DYING.name())) {
                gameLevel3D.onPacManDying(newState);
            }
            else if (newState.nameMatches(StateName.EATING_GHOST.name())) {
                gameLevel3D.onEatingGhost();
            }
            else if (newState.nameMatches(StateName.LEVEL_COMPLETE.name())) {
                gameLevel3D.onLevelComplete(newState, perspectiveID);
            }
            else if (newState.nameMatches(StateName.GAME_OVER.name())) {
                gameLevel3D.onGameOver(newState);
                final boolean showMessage = randomInt(0, 1000) < 250;
                if (!game.level().isDemoLevel() && showMessage) {
                    final String message = pickerGameOverMessages.nextText();
                    ui.showFlashMessage(Duration.seconds(2.5), message);
                }
            }
            else if (newState.nameMatches(StateName.STARTING_GAME_OR_LEVEL.name())) {
                if (gameLevel3D != null) {
                    gameLevel3D.onStartingGame();
                } else {
                    Logger.error("No 3D game level available"); //TODO can this happen?
                }
            }
        }
    }

    @Override
    public void onGameContinues(GameContinuedEvent event) {
        final Game game = gameContext().currentGame();
        if (gameLevel3D != null) {
            game.optGameLevel().map(GameLevel::worldMap).ifPresent(this::showReadyMessage);
        }
    }

    @Override
    public void onGameStarts(GameStartedEvent event) {
        final Game game = gameContext().currentGame();
        final StateMachine.State<Game> state = game.control().state();
        final boolean silent = game.level().isDemoLevel() || state instanceof TestState;
        if (!silent) {
            ui.soundManager().play(SoundID.GAME_READY);
        }
    }

    @Override
    public void onGhostEaten(GhostEatenEvent event) {
        ui.soundManager().play(SoundID.GHOST_EATEN);
    }


    @Override
    public void onLevelCreated(LevelCreatedEvent event) {
        gameContext().currentGame().optGameLevel().ifPresent(this::replaceGameLevel3D);
    }

    @Override
    public void onLevelStarts(LevelStartedEvent event) {
        final Game game = gameContext().currentGame();
        if (game.optGameLevel().isEmpty()) {
            Logger.error("No game level exists on level start? WTF!");
            return;
        }
        final GameLevel level = game.level();
        final StateMachine.State<Game> state = game.control().state();

        if (state instanceof TestState) {
            replaceGameLevel3D(level); //TODO check when to destroy previous level
            gameLevel3D.maze3D().food().energizers3D().forEach(Energizer3D::startPumping);
            showTestMessage(level.worldMap(), level.number());
        }
        else {
            if (!level.isDemoLevel() && state.nameMatches(StateName.STARTING_GAME_OR_LEVEL.name(), StateName.LEVEL_TRANSITION.name())) {
                showReadyMessage(level.worldMap());
            }
        }

        gameLevel3D.updateLevelCounter3D();
        replaceActionBindings(level);
        fadeInAnimation.playFromStart();
    }

    private long lastMunchingSoundPlayedTick;

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        final Vector2i tile = e.pac().tile();
        if (e.allPellets()) {
            eatAllPellets3D();
        } else {
            final MazeFood3D mazeFood3D = gameLevel3D.maze3D().food();
            final Energizer3D energizer3D = mazeFood3D.energizers3D().stream()
                .filter(e3D -> tile.equals(e3D.tile()))
                .findFirst().orElse(null);
            if (energizer3D != null) {
                energizer3D.onEaten();
            } else {
                mazeFood3D.pellets3D().stream()
                    .filter(pellet3D -> tile.equals(pellet3D.getUserData()))
                    .findFirst()
                    .ifPresent(this::eatPellet3D);
            }
            // Play munching sound?
            final long now = ui.clock().tickCount();
            final long passed = now - lastMunchingSoundPlayedTick;
            Logger.debug("Pac found food, tick={} passed since last time={}", now, passed);
            byte minDelay = ui.currentConfig().munchingSoundDelay();
            if (passed > minDelay  || minDelay == 0) {
                ui.soundManager().play(SoundID.PAC_MAN_MUNCHING);
                lastMunchingSoundPlayedTick = now;
            }
        }
    }

    @Override
    public void onPacGetsPower(PacGetsPowerEvent e) {
        final Game game = gameContext().currentGame();
        ui.soundManager().stopSiren();
        if (!game.isLevelCompleted()) {
            gameLevel3D.pac3D().setMovementPowerMode(true);
            ui.soundManager().loop(SoundID.PAC_MAN_POWER);
            gameLevel3D.animations().playWallColorFlashing();
        }
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent e) {
        gameLevel3D.pac3D().setMovementPowerMode(false);
        gameLevel3D.animations().stopWallColorFlashing();
        ui.soundManager().stop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onSpecialScoreReached(SpecialScoreReachedEvent e) {
        ui.soundManager().play(SoundID.EXTRA_LIFE);
    }

    @Override
    public void onUnspecifiedChange(UnspecifiedChangeEvent event) {
        // TODO: remove (this is only used by game state GameState.TESTING_CUT_SCENES)
        ui.views().getPlayView().updateGameScene(gameContext().currentGame(), true);
    }

    // other stuff

    public ObjectProperty<PerspectiveID> perspectiveIDProperty() {
        return perspectiveID;
    }

    public Optional<Perspective<GameLevel>> currentPerspective() {
        return perspectiveID.get() == null ? Optional.empty() : Optional.of(perspectivesByID.get(perspectiveID.get()));
    }

    public Optional<GameLevel3D> level3D() {
        return Optional.ofNullable(gameLevel3D);
    }

    protected GameLevel3D createGameLevel3D(GameLevel level) {
        return new GameLevel3D(ui, level);
    }

    protected void replaceActionBindings(GameLevel gameLevel) {}

    protected void updateHUD(Game game) {
        final Score score = game.score(), highScore = game.highScore();
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

    private void createPerspectives() {
        perspectivesByID.put(PerspectiveID.DRONE, new DronePerspective(camera));
        perspectivesByID.put(PerspectiveID.TOTAL, new TotalPerspective(camera));
        perspectivesByID.put(PerspectiveID.TRACK_PLAYER, new TrackingPlayerPerspective(camera));
        perspectivesByID.put(PerspectiveID.NEAR_PLAYER, new StalkingPlayerPerspective(camera));

        perspectiveID.addListener((_, oldID, newID) -> {
            if (oldID != null) {
                final Perspective<GameLevel> oldPerspective = perspectivesByID.get(oldID);
                oldPerspective.stopControlling();
            }
            if (newID != null) {
                final Perspective<GameLevel> newPerspective = perspectivesByID.get(newID);
                newPerspective.startControlling();
            }
            else {
                Logger.error("New perspective ID is NULL!");
            }
        });
    }

    private void updatePerspective(GameLevel level) {
        final PerspectiveID id = perspectiveID.get();
        if (id != null && perspectivesByID.containsKey(id)) {
            perspectivesByID.get(id).update(level);
        } else {
            Logger.error("No perspective with ID '{}' exists", id);
        }
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
            Logger.info("Replacing existing game level 3D");
            gameLevel3D.getChildren().clear();
            gameLevel3D.dispose();
            Logger.info("Disposed old game level 3D");
        } else {
            Logger.info("Creating new game level 3D");
        }
        gameLevel3D = createGameLevel3D(level);
        Logger.info("Created new game level 3D");

        level3DParent.getChildren().setAll(gameLevel3D);

        gameLevel3D.pac3D().init(level);
        gameLevel3D.ghosts3D().forEach(ghost3D -> ghost3D.init(level));
        Logger.info("Initialized actors of game level 3D");

        gameLevel3D.livesCounter3D().startTracking(gameLevel3D.pac3D());
    }

    private void updateSiren(GameLevel level) {
        final boolean pacChased = !level.pac().powerTimer().isRunning();
        if (pacChased) {
            // siren numbers are 1..4, hunting phase index = 0..7
            final int huntingPhase = level.huntingTimer().phaseIndex();
            final int sirenNumber = 1 + huntingPhase / 2;
            ui.soundManager().playSiren(sirenNumber, SIREN_VOLUME); // TODO change sound file volume?
        }
    }

    private void updateGhostSounds(Pac pac, Stream<Ghost> ghosts) {
        boolean returningHome = pac.isAlive() && ghosts.anyMatch(ghost ->
            ghost.state() == GhostState.RETURNING_HOME || ghost.state() == GhostState.ENTERING_HOUSE);
        if (returningHome) {
            if (!ui.soundManager().isPlaying(SoundID.GHOST_RETURNS)) {
                ui.soundManager().loop(SoundID.GHOST_RETURNS);
            }
        } else {
            ui.soundManager().stop(SoundID.GHOST_RETURNS);
        }
    }

    private void updateSound(GameLevel level) {
        if (!ui.soundManager().isEnabled()) {
            return;
        }
        if (level.game().control().state().nameMatches(StateName.HUNTING.name())) {
            updateSiren(level);
            updateGhostSounds(level.pac(), level.ghosts());
        }
    }

    private Animation createFadeInAnimation() {
        return new Timeline(
            new KeyFrame(Duration.ZERO, _ -> {
                //TODO Check if this is needed:
                currentPerspective().ifPresent(Perspective::startControlling);
                subScene.setFill(SCENE_FILL_DARK);
                gameLevel3D.setVisible(true);
                scores3D.setVisible(true);
            }
            ),
            new KeyFrame(Duration.seconds(SCENE_FADE_IN_SECONDS),
                new KeyValue(subScene.fillProperty(), SCENE_FILL_BRIGHT, Interpolator.EASE_IN))
        );
    }

    private void eatAllPellets3D() {
        gameLevel3D.maze3D().food().pellets3D().forEach(pellet3D -> {
            if (pellet3D.getParent() instanceof Group group) {
                group.getChildren().remove(pellet3D);
            }
        });
    }

    private void eatPellet3D(Shape3D pellet3D) {
        if (pellet3D.getParent() instanceof Group group) {
            // remove after small delay to let pellet not directly disappear when Pac-Man enters tile
            pauseSecThen(0.05, () -> group.getChildren().remove(pellet3D)).play();
        }
    }

    private void showReadyMessage(WorldMap worldMap) {
        worldMap.terrainLayer().optHouse().ifPresentOrElse(house -> {
            final Vector2f center = house.centerPositionUnderHouse();
            gameLevel3D.showAnimatedMessage(READY_MESSAGE_TEXT, READY_MESSAGE_DISPLAY_SECONDS, center.x(), center.y());
        }, () -> Logger.error("Cannot display READY message: no house in this game level! WTF?"));
    }

    private void showTestMessage(WorldMap worldMap, int levelNumber) {
        final double x = worldMap.numCols() * HTS;
        final double y = (worldMap.numRows() - 2) * TS;
        gameLevel3D.showAnimatedMessage(TEST_MESSAGE_TEXT.formatted(levelNumber), 5, x, y);
    }
}