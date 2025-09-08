/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.controller.teststates.LevelMediumTestState;
import de.amr.pacmanfx.controller.teststates.LevelShortTestState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.model.ScoreManager;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.AbstractGameAction;
import de.amr.pacmanfx.ui.DefaultActionBindingsManager;
import de.amr.pacmanfx.ui.api.ActionBindingsManager;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.model3D.Bonus3D;
import de.amr.pacmanfx.uilib.model3D.Energizer3D;
import de.amr.pacmanfx.uilib.model3D.Scores3D;
import de.amr.pacmanfx.uilib.widgets.CoordinateSystem;
import javafx.animation.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape3D;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.isOneOf;
import static de.amr.pacmanfx.controller.GamePlayState.*;
import static de.amr.pacmanfx.ui.CommonGameActions.*;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.*;
import static de.amr.pacmanfx.ui.input.Keyboard.control;
import static de.amr.pacmanfx.uilib.Ufx.createContextMenuTitle;
import static de.amr.pacmanfx.uilib.Ufx.pauseSec;
import static java.util.Objects.requireNonNull;

/**
 * 3D play scene.
 *
 * <p>Provides different camera perspectives that can be stepped through using key combinations
 * <code>Alt+LEFT</code> and <code>Alt+RIGHT</code> (Did he really said Alt-Right?).
 */
public class PlayScene3D implements GameScene {

    private static final Color SUB_SCENE_FILL_DARK = Color.BLACK;
    private static final Color SUB_SCENE_FILL_BRIGHT = Color.TRANSPARENT;

    private final Map<PerspectiveID, Perspective> perspectiveMap = new EnumMap<>(PerspectiveID.class);

    private final ObjectProperty<PerspectiveID> perspectiveIDProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            if (get() != null) {
                initPerspective();
            }
        }
    };

    private final AbstractGameAction droneUp = new AbstractGameAction("DroneUp") {
        @Override
        public void execute(GameUI ui) {
            if (perspectiveMap.get(PerspectiveID.DRONE) instanceof DronePerspective dronePerspective) {
                dronePerspective.moveUp();
            }
        }
        @Override
        public boolean isEnabled(GameUI ui) {
            return perspectiveIDProperty.get() == PerspectiveID.DRONE;
        }
    };

    private final AbstractGameAction droneDown = new AbstractGameAction("DroneDown") {
        @Override
        public void execute(GameUI ui) {
            if (perspectiveMap.get(PerspectiveID.DRONE) instanceof DronePerspective dronePerspective) {
                dronePerspective.moveDown();
            }
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return perspectiveIDProperty.get() == PerspectiveID.DRONE;
        }
    };

    protected final GameUI ui;
    protected final SubScene subScene;
    protected final PerspectiveCamera camera = new PerspectiveCamera(true);
    protected final ActionBindingsManager actionBindings;
    protected final Group gameLevel3DParent = new Group();
    protected GameLevel3D gameLevel3D;
    protected Scores3D scores3D;

    public PlayScene3D(GameUI ui) {
        this.ui = requireNonNull(ui);
        this.actionBindings = new DefaultActionBindingsManager();

        createPerspectives();

        var root = new Group();
        // initial size is irrelevant because size gets bound to parent scene size later
        subScene = new SubScene(root, 88, 88, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(SUB_SCENE_FILL_DARK);

        // The score is always displayed in full view, regardless which perspective is used
        scores3D = new Scores3D(
            ui.assets().translated("score.score"),
            ui.assets().translated("score.high_score"),
            ui.assets().arcadeFont(TS)
        );
        scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
        scores3D.rotateProperty().bind(camera.rotateProperty());

        scores3D.translateXProperty().bind(gameLevel3DParent.translateXProperty().add(TS));
        scores3D.translateYProperty().bind(gameLevel3DParent.translateYProperty().subtract(4.5 * TS));
        scores3D.translateZProperty().bind(gameLevel3DParent.translateZProperty().subtract(4.5 * TS));
        scores3D.setVisible(false);

        // Just for debugging and posing
        var coordinateSystem = new CoordinateSystem();
        coordinateSystem.visibleProperty().bind(PROPERTY_3D_AXES_VISIBLE);

        root.getChildren().setAll(gameLevel3DParent, scores3D, coordinateSystem);
    }

    public ObjectProperty<PerspectiveID> perspectiveIDProperty() {
        return perspectiveIDProperty;
    }

    private void createPerspectives() {
        perspectiveMap.put(PerspectiveID.DRONE, new DronePerspective());
        perspectiveMap.put(PerspectiveID.TOTAL, new TotalPerspective());
        perspectiveMap.put(PerspectiveID.TRACK_PLAYER, new TrackingPlayerPerspective());
        perspectiveMap.put(PerspectiveID.NEAR_PLAYER, new StalkingPlayerPerspective());
    }

    private void handleScrollEvent(ScrollEvent e) {
        if (ui.currentGameScene().isPresent() && ui.currentGameScene().get() == this) {
            if (e.getDeltaY() < 0) {
                droneUp.executeIfEnabled(ui);
            } else if (e.getDeltaY() > 0) {
                droneDown.executeIfEnabled(ui);
            }
        }
    }

    private void initPerspective() {
        PerspectiveID id = perspectiveIDProperty.get();
        if (id != null && perspectiveMap.containsKey(id)) {
            perspectiveMap.get(id).init(camera);
        } else {
            Logger.error("Cannot init camera perspective with ID '{}'", id);
        }
    }

    @Override
    public void handleKeyboardInput() {
        actionBindings.matchingAction(ui.keyboard()).ifPresent(gameAction -> gameAction.executeIfEnabled(ui));
    }

    @Override
    public GameContext context() {
        return ui.gameContext();
    }

    @Override
    public ActionBindingsManager actionBindings() {
        return actionBindings;
    }

    public Optional<GameLevel3D> level3D() {
        return Optional.ofNullable(gameLevel3D);
    }

    // Context menu

    private final ToggleGroup perspectiveToggleGroup = new ToggleGroup();

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent menuEvent, ContextMenu menu) {
        var miUse2DScene = new MenuItem(ui.assets().translated("use_2D_scene"));
        miUse2DScene.setOnAction(e -> ACTION_TOGGLE_PLAY_SCENE_2D_3D.executeIfEnabled(ui));

        var miToggleMiniView = new CheckMenuItem(ui.assets().translated("pip"));
        miToggleMiniView.selectedProperty().bindBidirectional(PROPERTY_MINI_VIEW_ON);

        var miAutopilot = new CheckMenuItem(ui.assets().translated("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(theGameContext().gameController().propertyUsingAutopilot());

        var miImmunity = new CheckMenuItem(ui.assets().translated("immunity"));
        miImmunity.selectedProperty().bindBidirectional(theGameContext().gameController().propertyImmunity());

        var miMuted = new CheckMenuItem(ui.assets().translated("muted"));
        miMuted.selectedProperty().bindBidirectional(PROPERTY_MUTED);

        var miQuit = new MenuItem(ui.assets().translated("quit"));
        miQuit.setOnAction(e -> ACTION_QUIT_GAME_SCENE.executeIfEnabled(ui));

        var items = new ArrayList<MenuItem>();
        items.add(createContextMenuTitle("scene_display", ui.preferences(), ui.assets()));
        items.add(miUse2DScene);
        items.add(miToggleMiniView);
        items.add(createContextMenuTitle("select_perspective", ui.preferences(), ui.assets()));
        items.addAll(createPerspectiveRadioItems(menu));
        items.add(createContextMenuTitle("pacman", ui.preferences(), ui.assets()));
        items.add(miAutopilot);
        items.add(miImmunity);
        items.add(new SeparatorMenuItem());
        items.add(miMuted);
        items.add(miQuit);

        return items;
    }

    private List<RadioMenuItem> createPerspectiveRadioItems(ContextMenu menu) {
        var items = new ArrayList<RadioMenuItem>();
        for (PerspectiveID id : PerspectiveID.values()) {
            var item = new RadioMenuItem(ui.assets().translated("perspective_id_" + id.name()));
            item.setUserData(id);
            item.setToggleGroup(perspectiveToggleGroup);
            if (id == PROPERTY_3D_PERSPECTIVE.get())  {
                item.setSelected(true);
            }
            item.setOnAction(e -> PROPERTY_3D_PERSPECTIVE.set(id));
            items.add(item);
        }
        Logger.info("Added listener to UI property3DPerspective property");
        PROPERTY_3D_PERSPECTIVE.addListener(this::handle3DPerspectiveChange);
        menu.setOnHidden(e -> {
            PROPERTY_3D_PERSPECTIVE.removeListener(this::handle3DPerspectiveChange);
            Logger.info("Removed listener from UI property3DPerspective property");
        });
        return items;
    }

    private void handle3DPerspectiveChange(
        ObservableValue<? extends PerspectiveID> property,
        PerspectiveID oldPerspectiveID,
        PerspectiveID newPerspectiveID) {
        for (Toggle toggle : perspectiveToggleGroup.getToggles()) {
            if (toggle.getUserData() == newPerspectiveID) {
                perspectiveToggleGroup.selectToggle(toggle);
            }
        }
    }

    protected void setActionBindings() {
        actionBindings.uninstallBindings(ui.keyboard());
        actionBindings.assign(ACTION_PERSPECTIVE_PREVIOUS, ui.actionBindings());
        actionBindings.assign(ACTION_PERSPECTIVE_NEXT, ui.actionBindings());
        actionBindings.assign(ACTION_TOGGLE_DRAW_MODE, ui.actionBindings());
        if (context().optGameLevel().isPresent()) {
            if (context().gameLevel().isDemoLevel()) {
                actionBindings.assign(ACTION_ARCADE_INSERT_COIN, ui.actionBindings());
            } else {
                setPlayerSteeringActionBindings();
                actionBindings.assign(ACTION_CHEAT_EAT_ALL_PELLETS, ui.actionBindings());
                actionBindings.assign(ACTION_CHEAT_ADD_LIVES, ui.actionBindings());
                actionBindings.assign(ACTION_CHEAT_ENTER_NEXT_LEVEL, ui.actionBindings());
                actionBindings.assign(ACTION_CHEAT_KILL_GHOSTS, ui.actionBindings());
            }
        }
        actionBindings.installBindings(ui.keyboard());
    }

    /**
     * Overridden by Tengen Ms. Pac-Man play scene 3D to use keys representing "Joypad" buttons.
     */
    protected void setPlayerSteeringActionBindings() {
        actionBindings.assign(ACTION_STEER_UP, ui.actionBindings());
        actionBindings.assign(ACTION_STEER_DOWN, ui.actionBindings());
        actionBindings.assign(ACTION_STEER_LEFT, ui.actionBindings());
        actionBindings.assign(ACTION_STEER_RIGHT, ui.actionBindings());
    }

    @Override
    public void init() {
        context().game().hud().showScore(true);
        perspectiveIDProperty().bind(PROPERTY_3D_PERSPECTIVE);
        actionBindings.bind(droneUp, control(KeyCode.MINUS));
        actionBindings.bind(droneDown, control(KeyCode.PLUS));
        actionBindings.installBindings(ui.keyboard());
        // TODO: integrate into input framework?
        ui.stage().getScene().addEventHandler(ScrollEvent.SCROLL, this::handleScrollEvent);
    }

    @Override
    public void end() {
        ui.stage().removeEventHandler(ScrollEvent.SCROLL, this::handleScrollEvent);
        ui.soundManager().stopAll();
        if (gameLevel3D != null) {
            gameLevel3D.dispose();
            gameLevel3D = null;
        }
        gameLevel3DParent.getChildren().clear();
    }

    @Override
    public void update() {
        if (context().optGameLevel().isEmpty()) {
            // Scene is already updated 2 ticks before the game level gets created!
            Logger.info("Tick #{}: Game level not yet created, update ignored", ui.clock().tickCount());
            return;
        }
        if (gameLevel3D == null) {
            Logger.info("Tick #{}: 3D game level not yet created", ui.clock().tickCount());
            return;
        }
        ui.soundManager().setEnabled(!context().gameLevel().isDemoLevel());
        gameLevel3D.tick(context());
        updateCamera();
        updateHUD();
        updateSound();
    }

    @Override
    public Optional<SubScene> optSubScene() {
        return Optional.of(subScene);
    }

    @Override
    public void onEnterGameState(GameState state) {
        requireNonNull(state);
        if (state.is(LevelShortTestState.class) || state.is(LevelMediumTestState.class)) {
            replaceGameLevel3D();
            showLevelTestMessage();
            PROPERTY_3D_PERSPECTIVE.set(PerspectiveID.TOTAL);
        }
        else {
            switch (state) {
                case BOOT, INTRO, SETTING_OPTIONS_FOR_START, SHOWING_CREDITS, LEVEL_TRANSITION, INTERMISSION -> {}
                case HUNTING -> gameLevel3D.onHuntingStart(context());
                case PACMAN_DYING -> gameLevel3D.onPacManDying(state);
                case GHOST_DYING -> gameLevel3D.onGhostDying();
                case LEVEL_COMPLETE -> gameLevel3D.onLevelComplete(state, perspectiveIDProperty);
                case GAME_OVER -> gameLevel3D.onGameOver(state);
                case STARTING_GAME_OR_LEVEL -> {
                    if (gameLevel3D != null) {
                        gameLevel3D.onStartingGame();
                    } else {
                        Logger.error("No 3D game level available"); //TODO can this happen?
                    }
                }
                default -> throw new IllegalStateException("Unexpected state: " + state);
            }
        }
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        replaceGameLevel3D();
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        if (theGameContext().optGameLevel().isEmpty()) {
            Logger.error("No game level exists on level start! WTF?");
            return;
        }
        final GameLevel gameLevel = context().gameLevel();
        final GameState state = context().gameState();

        if (state.is(LevelShortTestState.class) || state.is(LevelMediumTestState.class)) {
            replaceGameLevel3D(); //TODO check when to destroy previous level
            gameLevel3D.energizers3D().forEach(Energizer3D::startPumping);
            showLevelTestMessage();
        }
        else {
            switch (state) {
                case STARTING_GAME_OR_LEVEL, LEVEL_TRANSITION -> {
                    if (!gameLevel.isDemoLevel()) {
                        if (gameLevel.house().isEmpty()) {
                            Logger.error("No house found in this game level! WTF?");
                        } else {
                            Vector2f messageCenter = gameLevel.house().get().centerPositionUnderHouse();
                            gameLevel3D.showAnimatedMessage("READY!", 2.5f, messageCenter.x(), messageCenter.y());
                        }
                        setPlayerSteeringActionBindings();
                    }
                }
                default -> Logger.error("Unexpected game state '{}' on level start", context().gameState());
            }
        }

        gameLevel3D.updateLevelCounter3D();
        setActionBindings();
        fadeInGameLevel3D();
    }

    @Override
    public void onSwitch_2D_3D(GameScene scene2D) {
        if (context().optGameLevel().isEmpty()) {
            return;
        }
        final GameLevel gameLevel = context().gameLevel();
        if (gameLevel3D == null) {
            replaceGameLevel3D();
        }
        gameLevel.pac().show();
        gameLevel.ghosts().forEach(Ghost::show);

        gameLevel3D.pac3D().init(gameLevel);
        gameLevel3D.pac3D().update(gameLevel);
        gameLevel3D.pellets3D().forEach(pellet -> pellet.setVisible(!gameLevel.tileContainsEatenFood((Vector2i) pellet.getUserData())));
        gameLevel3D.energizers3D().forEach(energizer ->
                energizer.shape().setVisible(!gameLevel.tileContainsEatenFood(energizer.tile())));
        if (isOneOf(context().gameState(), HUNTING, GHOST_DYING)) { //TODO check this
            gameLevel3D.energizers3D().stream()
                .filter(energizer3D -> energizer3D.shape().isVisible())
                .forEach(Energizer3D::startPumping);
        }

        if (context().gameState() == HUNTING) {
            if (gameLevel.pac().powerTimer().isRunning()) {
                ui.soundManager().loop(SoundID.PAC_MAN_POWER);
            }
            gameLevel3D.livesCounter3D.startTracking(gameLevel3D.pac3D);
        }
        gameLevel3D.updateLevelCounter3D();
        updateHUD();
        setActionBindings();
        fadeInGameLevel3D();
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        context().gameLevel().bonus().ifPresent(bonus -> {
            gameLevel3D.updateBonus3D(bonus);
            ui.soundManager().loop(SoundID.BONUS_ACTIVE);
        });
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        context().gameLevel().bonus().ifPresent(bonus -> {
            gameLevel3D.bonus3D().ifPresent(Bonus3D::showEaten);
            ui.soundManager().stop(SoundID.BONUS_ACTIVE);
            ui.soundManager().play(SoundID.BONUS_EATEN);
        });
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        context().gameLevel().bonus().ifPresent(bonus -> {
            gameLevel3D.bonus3D().ifPresent(Bonus3D::expire);
            ui.soundManager().stop(SoundID.BONUS_ACTIVE);
        });
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        ui.soundManager().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onGameContinued(GameEvent e) {
        if (gameLevel3D != null) {
            if (context().gameLevel().house().isEmpty()) {
                Logger.error("No house found in this game level! WTF?");
            } else {
                Vector2f messageCenter = context().gameLevel().house().get().centerPositionUnderHouse();
                gameLevel3D.showAnimatedMessage("READY!", 2.5f, messageCenter.x(), messageCenter.y());
            }
        }
    }

    @Override
    public void onGameStarted(GameEvent e) {
        GameState state = context().gameState();
        boolean silent = context().gameLevel().isDemoLevel()
            || state.is(LevelShortTestState.class)
            || state.is(LevelMediumTestState.class);
        if (!silent) {
            ui.soundManager().play(SoundID.GAME_READY);
        }
    }

    @Override
    public void onPacFoundFood(GameEvent event) {
        if (event.tile() == null) {
            // When cheat "eat all pellets" has been used, no tile is present in the event.
            gameLevel3D.pellets3D().forEach(this::eatPellet3D);
        } else {
            Energizer3D energizer3D = gameLevel3D.energizers3D().stream()
                .filter(e3D -> event.tile().equals(e3D.tile())).findFirst().orElse(null);
            if (energizer3D != null) {
                energizer3D.onEaten();
            } else {
                gameLevel3D.pellets3D().stream()
                    .filter(pellet3D -> event.tile().equals(pellet3D.getUserData()))
                    .findFirst()
                    .ifPresent(this::eatPellet3D);
            }
            if (!ui.soundManager().isPlaying(SoundID.PAC_MAN_MUNCHING)) {
                ui.soundManager().loop(SoundID.PAC_MAN_MUNCHING);
                Logger.info("Play munching sound, starving ticks={}", theGameContext().gameLevel().pac().starvingTicks());
            }
        }
    }

    @Override
    public void onPacGetsPower(GameEvent event) {
        ui.soundManager().stopSiren();
        if (!theGameContext().game().isLevelCompleted()) {
            gameLevel3D.pac3D().setMovementPowerMode(true);
            ui.soundManager().loop(SoundID.PAC_MAN_POWER);
            gameLevel3D.playWallColorFlashing();
        }
    }

    @Override
    public void onPacLostPower(GameEvent event) {
        gameLevel3D.pac3D().setMovementPowerMode(false);
        gameLevel3D.stopWallColorFlashing();
        ui.soundManager().stop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onSpecialScoreReached(GameEvent e) {
        ui.soundManager().play(SoundID.EXTRA_LIFE);
    }

    @Override
    public void onStopAllSounds(GameEvent event) {
        ui.soundManager().stopAll();
    }

    @Override
    public void onUnspecifiedChange(GameEvent event) {
        // TODO: remove (this is only used by game state GameState.TESTING_CUT_SCENES)
        ui.updateGameScene(true);
    }

    protected GameLevel3D createGameLevel3D() {
        return new GameLevel3D(ui);
    }

    protected void replaceGameLevel3D() {
        if (gameLevel3D != null) {
            Logger.info("Replacing existing game level 3D");
            gameLevel3D.getChildren().clear();
            gameLevel3D.dispose();
            Logger.info("Disposed old game level 3D");
        } else {
            Logger.info("Creating new game level 3D");
        }
        gameLevel3D = createGameLevel3D();
        Logger.info("Created new game level 3D");

        gameLevel3DParent.getChildren().setAll(gameLevel3D);

        gameLevel3D.pac3D().init(context().gameLevel());
        gameLevel3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context(), context().gameLevel()));
        Logger.info("Initialized actors of game level 3D");


        gameLevel3D.livesCounter3D.startTracking(gameLevel3D.pac3D);
    }

    protected void updateCamera() {
        PerspectiveID id = perspectiveIDProperty.get();
        if (id != null && perspectiveMap.containsKey(id)) {
            perspectiveMap.get(id).update(camera, context());
        }
        else {
            Logger.error("No perspective with ID '{}' exists", id);
        }
    }

    protected void updateHUD() {
        ScoreManager scoreManager = context().game().scoreManager();
        final Score score = scoreManager.score(), highScore = scoreManager.highScore();
        if (score.isEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else { // disabled, show text "GAME OVER"
            Color color = ui.currentConfig().assets().color("color.game_over_message");
            scores3D.showTextForScore(ui.assets().translated("score.game_over"), color);
        }
        // Always show high score
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
    }

    protected void updateSound() {
        if (!ui.soundManager().isEnabled()) return;
        Pac pac = context().gameLevel().pac();
        boolean pacChased = context().gameState() == HUNTING && !pac.powerTimer().isRunning();
        if (pacChased) {
            // siren numbers are 1..4, hunting phase index = 0..7
            int huntingPhase = context().game().huntingTimer().phaseIndex();
            int sirenNumber = 1 + huntingPhase / 2;
            switch (sirenNumber) {
                case 1 -> ui.soundManager().playSiren(SoundID.SIREN_1, 1.0);
                case 2 -> ui.soundManager().playSiren(SoundID.SIREN_2, 1.0);
                case 3 -> ui.soundManager().playSiren(SoundID.SIREN_3, 1.0);
                case 4 -> ui.soundManager().playSiren(SoundID.SIREN_4, 1.0);
                default -> throw new IllegalArgumentException("Illegal siren number " + sirenNumber);
            }
        }

        // TODO Still not sure how to do this right
        if (pac.starvingTicks() >= 10 && !ui.soundManager().isPaused(SoundID.PAC_MAN_MUNCHING)) {
            ui.soundManager().pause(SoundID.PAC_MAN_MUNCHING);
        }

        boolean isGhostReturningHome = context().gameLevel().pac().isAlive()
            && context().gameLevel().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).findAny().isPresent();
        if (isGhostReturningHome) {
            ui.soundManager().loop(SoundID.GHOST_RETURNS);
        } else {
            ui.soundManager().stop(SoundID.GHOST_RETURNS);
        }
    }

    protected void showLevelTestMessage() {
        WorldMap worldMap = context().gameLevel().worldMap();
        int levelNumber = context().gameLevel().number();
        double x = worldMap.numCols() * HTS;
        double y = (worldMap.numRows() - 2) * TS;
        gameLevel3D.showAnimatedMessage("LEVEL %d (TEST)".formatted(levelNumber), 5, x, y);
    }

    protected void fadeInGameLevel3D() {
        initPerspective();
        gameLevel3D.setVisible(false);
        scores3D.setVisible(false);
        subScene.setFill(SUB_SCENE_FILL_DARK);
        var makeVisibleAfterDelay = pauseSec(0.5, () -> {
            gameLevel3D.setVisible(true);
            scores3D.setVisible(true);
        });
        var fadeFillColorIn = new Timeline(
            new KeyFrame(Duration.seconds(3),
                new KeyValue(subScene.fillProperty(), SUB_SCENE_FILL_BRIGHT, Interpolator.EASE_OUT)));
        new SequentialTransition(makeVisibleAfterDelay, fadeFillColorIn).play();
    }

    protected void eatPellet3D(Shape3D pellet3D) {
        // remove after small delay for better visualization
        if (pellet3D.getParent() instanceof Group group) {
            pauseSec(0.05, () -> group.getChildren().remove(pellet3D)).play();
        }
    }
}