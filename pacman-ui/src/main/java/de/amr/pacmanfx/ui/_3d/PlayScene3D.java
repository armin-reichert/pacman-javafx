/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.worldmap.FoodLayer;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.model.ScoreManager;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.ui.action.CheatActions;
import de.amr.pacmanfx.ui.action.DefaultActionBindingsManager;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.api.ActionBindingsManager;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.SubSceneProvider;
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
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.action.CommonGameActions.*;
import static de.amr.pacmanfx.ui.api.GameUI.*;
import static de.amr.pacmanfx.ui.input.Keyboard.control;
import static de.amr.pacmanfx.uilib.Ufx.*;
import static java.util.Objects.requireNonNull;

/**
 * 3D play scene.
 *
 * <p>Provides different camera perspectives that can be stepped through using key combinations
 * <code>Alt+LEFT</code> and <code>Alt+RIGHT</code>.
 */
public class PlayScene3D extends Group implements GameScene, SubSceneProvider {

    private static final Color SUB_SCENE_FILL_DARK = Color.BLACK;
    private static final Color SUB_SCENE_FILL_BRIGHT = Color.TRANSPARENT;

    private final Map<PerspectiveID, Perspective> perspectivesByID = new EnumMap<>(PerspectiveID.class);

    private final ObjectProperty<PerspectiveID> perspectiveID = new SimpleObjectProperty<>();

    private Optional<Perspective> currentPerspective() {
        return perspectiveID.get() == null ? Optional.empty() : Optional.of(perspectivesByID.get(perspectiveID.get()));
    }

    private final GameAction actionDroneUp = new GameAction("DroneUp") {
        @Override
        public void execute(GameUI ui) {
            currentPerspective().ifPresent(perspective -> {
                if (perspective instanceof DronePerspective dronePerspective) {
                    dronePerspective.moveUp();
                }
            });
        }
        @Override
        public boolean isEnabled(GameUI ui) {
            return perspectiveID.get() == PerspectiveID.DRONE;
        }
    };

    private final GameAction actionDroneDown = new GameAction("DroneDown") {
        @Override
        public void execute(GameUI ui) {
            currentPerspective().ifPresent(perspective -> {
                if (perspective instanceof DronePerspective dronePerspective) {
                    dronePerspective.moveDown();
                }
            });
        }
        @Override
        public boolean isEnabled(GameUI ui) {
            return perspectiveID.get() == PerspectiveID.DRONE;
        }
    };

    protected final GameUI ui;
    protected final SubScene subScene;
    protected final PerspectiveCamera camera;
    protected final ActionBindingsManager actionBindings;
    protected final Group gameLevel3DParent = new Group();
    protected GameLevel3D gameLevel3D;
    protected Scores3D scores3D;

    public PlayScene3D(GameUI ui) {
        this.ui = requireNonNull(ui);
        actionBindings = new DefaultActionBindingsManager();
        camera = new PerspectiveCamera(true);

        createPerspectives();
        createScores3D();
        var coordinateSystem = new CoordinateSystem();
        coordinateSystem.visibleProperty().bind(PROPERTY_3D_AXES_VISIBLE);

        getChildren().setAll(gameLevel3DParent, scores3D, coordinateSystem);

        // initial size is irrelevant (size gets bound to parent scene size eventually)
        subScene = new SubScene(this, 88, 88, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(SUB_SCENE_FILL_DARK);
    }

    private void createPerspectives() {
        perspectivesByID.put(PerspectiveID.DRONE, new DronePerspective());
        perspectivesByID.put(PerspectiveID.TOTAL, new TotalPerspective());
        perspectivesByID.put(PerspectiveID.TRACK_PLAYER, new TrackingPlayerPerspective());
        perspectivesByID.put(PerspectiveID.NEAR_PLAYER, new StalkingPlayerPerspective());

        perspectiveID.addListener((py, oldID, newID) -> {
            if (oldID != null) {
                Perspective oldPerspective = perspectivesByID.get(oldID);
                oldPerspective.detach(camera);
            }
            if (newID != null) {
                Perspective newPerspective = perspectivesByID.get(newID);
                newPerspective.attach(camera);
            }
            else {
                Logger.error("New perspective ID is NULL!");
            }
        });
    }

    private void createScores3D() {
        scores3D = new Scores3D(
            ui.assets().translated("score.score"),
            ui.assets().translated("score.high_score"),
            ui.assets().font_Arcade_8
        );

        // The scores are always displayed in full view, regardless which perspective is used
        scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
        scores3D.rotateProperty().bind(camera.rotateProperty());

        scores3D.translateXProperty().bind(gameLevel3DParent.translateXProperty().add(TS));
        scores3D.translateYProperty().bind(gameLevel3DParent.translateYProperty().subtract(4.5 * TS));
        scores3D.translateZProperty().bind(gameLevel3DParent.translateZProperty().subtract(4.5 * TS));
        scores3D.setVisible(false);
    }

    public ObjectProperty<PerspectiveID> perspectiveIDProperty() {
        return perspectiveID;
    }

    @Override
    public void handleKeyboardInput() {
        actionBindings.matchingAction(ui.keyboard()).ifPresent(gameAction -> gameAction.executeIfEnabled(ui));
    }

    @Override
    public void handleScrollEvent(ScrollEvent e) {
        if (e.getDeltaY() < 0) {
            actionDroneUp.executeIfEnabled(ui);
        } else {
            actionDroneDown.executeIfEnabled(ui);
        }
    }

    @Override
    public GameUI ui() {
        return ui;
    }

    @Override
    public GameContext context() {
        return ui.context();
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
        miAutopilot.selectedProperty().bindBidirectional(THE_GAME_BOX.usingAutopilotProperty());

        var miImmunity = new CheckMenuItem(ui.assets().translated("immunity"));
        miImmunity.selectedProperty().bindBidirectional(THE_GAME_BOX.immunityProperty());

        var miMuted = new CheckMenuItem(ui.assets().translated("muted"));
        miMuted.selectedProperty().bindBidirectional(PROPERTY_MUTED);

        var miQuit = new MenuItem(ui.assets().translated("quit"));
        miQuit.setOnAction(e -> ACTION_QUIT_GAME_SCENE.executeIfEnabled(ui));

        var items = new ArrayList<MenuItem>();
        items.add(createContextMenuTitle(ui.preferences(), ui.assets().translated("scene_display")));
        items.add(miUse2DScene);
        items.add(miToggleMiniView);
        items.add(createContextMenuTitle(ui.preferences(), ui.assets().translated("select_perspective")));
        items.addAll(createPerspectiveRadioItems(menu));
        items.add(createContextMenuTitle(ui.preferences(), ui.assets().translated("pacman")));
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
            if (id == PROPERTY_3D_PERSPECTIVE_ID.get())  {
                item.setSelected(true);
            }
            item.setOnAction(e -> PROPERTY_3D_PERSPECTIVE_ID.set(id));
            items.add(item);
        }
        PROPERTY_3D_PERSPECTIVE_ID.addListener(this::handlePerspectiveIDChange);
        menu.setOnHidden(e -> PROPERTY_3D_PERSPECTIVE_ID.removeListener(this::handlePerspectiveIDChange));
        return items;
    }

    private void handlePerspectiveIDChange(ObservableValue<? extends PerspectiveID> property, PerspectiveID oldID, PerspectiveID newID) {
        for (Toggle toggle : perspectiveToggleGroup.getToggles()) {
            if (toggle.getUserData() == newID) {
                perspectiveToggleGroup.selectToggle(toggle);
            }
        }
    }

    protected void setActionBindings() {
        actionBindings.removeBindingsFromKeyboard(ui.keyboard());
        actionBindings.bind(ACTION_PERSPECTIVE_PREVIOUS, ui.actionBindings());
        actionBindings.bind(ACTION_PERSPECTIVE_NEXT, ui.actionBindings());
        actionBindings.bind(ACTION_TOGGLE_DRAW_MODE, ui.actionBindings());
        if (context().currentGame().optGameLevel().isPresent()) {
            if (context().currentGame().level().isDemoLevel()) {
                //TODO This is game-specific and does not belong here!
                //actionBindings.bind(ArcadeActions.ACTION_INSERT_COIN, ui.actionBindings());
            } else {
                setPlayerSteeringActionBindings();
                actionBindings.bind(CheatActions.ACTION_EAT_ALL_PELLETS, ui.actionBindings());
                actionBindings.bind(CheatActions.ACTION_ADD_LIVES, ui.actionBindings());
                actionBindings.bind(CheatActions.ACTION_ENTER_NEXT_LEVEL, ui.actionBindings());
                actionBindings.bind(CheatActions.ACTION_KILL_GHOSTS, ui.actionBindings());
            }
        }
        actionBindings.assignBindingsToKeyboard(ui.keyboard());
    }

    /**
     * Overridden by "Tengen Ms. Pac-Man" subclass to bind to keys representing the Joypad buttons.
     */
    protected void setPlayerSteeringActionBindings() {
        actionBindings.bind(ACTION_STEER_UP, ui.actionBindings());
        actionBindings.bind(ACTION_STEER_DOWN, ui.actionBindings());
        actionBindings.bind(ACTION_STEER_LEFT, ui.actionBindings());
        actionBindings.bind(ACTION_STEER_RIGHT, ui.actionBindings());
    }

    @Override
    public void init() {
        context().currentGame().hud().showScore(true);
        perspectiveIDProperty().bind(PROPERTY_3D_PERSPECTIVE_ID);
        actionBindings.addKeyCombination(actionDroneUp, control(KeyCode.MINUS));
        actionBindings.addKeyCombination(actionDroneDown, control(KeyCode.PLUS));
        actionBindings.assignBindingsToKeyboard(ui.keyboard());
    }

    @Override
    public void end() {
        ui.soundManager().stopAll();
        if (gameLevel3D != null) {
            gameLevel3D.dispose();
            gameLevel3D = null;
        }
        gameLevel3DParent.getChildren().clear();
    }

    @Override
    public void update() {
        final GameLevel gameLevel = context().currentGame().level();
        if (gameLevel == null) {
            // Scene is already updated 2 ticks before the game level gets created!
            Logger.info("Tick #{}: Game level not yet created, update ignored", ui.clock().tickCount());
            return;
        }
        if (gameLevel3D == null) {
            Logger.info("Tick #{}: 3D game level not yet created", ui.clock().tickCount());
            return;
        }
        gameLevel3D.update();
        updateCamera();
        updateHUD();
        ui.soundManager().setEnabled(!gameLevel.isDemoLevel());
        updateSound(gameLevel, context().currentGameState());
    }

    @Override
    public SubScene subScene() {
        return subScene;
    }

    @Override
    public void onEnterGameState(FsmState<GameContext> state) {
        if (state instanceof TestState) {
            replaceGameLevel3D();
            showLevelTestMessage(context().currentGame().level());
            PROPERTY_3D_PERSPECTIVE_ID.set(PerspectiveID.TOTAL);
        }
        else {
            switch (state.name()) {
                case "HUNTING" -> gameLevel3D.onHuntingStart();
                case "PACMAN_DYING" -> gameLevel3D.onPacManDying(state);
                case "GHOST_DYING" -> gameLevel3D.onGhostDying();
                case "LEVEL_COMPLETE" -> gameLevel3D.onLevelComplete(state, perspectiveID);
                case "GAME_OVER" -> gameLevel3D.onGameOver(state);
                case "STARTING_GAME_OR_LEVEL" -> {
                    if (gameLevel3D != null) {
                        gameLevel3D.onStartingGame();
                    } else {
                        Logger.error("No 3D game level available"); //TODO can this happen?
                    }
                }
                default -> { /* do nothing */ }
            }
        }
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        replaceGameLevel3D();
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        if (context().currentGame().optGameLevel().isEmpty()) {
            Logger.error("No game level exists on level start! WTF?");
            return;
        }
        final GameLevel gameLevel = context().currentGame().level();
        final FsmState<GameContext> state = context().currentGameState();

        if (state instanceof TestState) {
            replaceGameLevel3D(); //TODO check when to destroy previous level
            gameLevel3D.energizers3D().forEach(Energizer3D::startPumping);
            showLevelTestMessage(gameLevel);
        }
        else {
            switch (state.name()) {
                case "STARTING_GAME_OR_LEVEL", "LEVEL_TRANSITION" -> {
                    if (!gameLevel.isDemoLevel()) {
                        Optional<House> optionalHouse = gameLevel.worldMap().terrainLayer().optHouse();
                        if (optionalHouse.isEmpty()) {
                            Logger.error("No house found in this game level! WTF?");
                        } else {
                            Vector2f messageCenter = optionalHouse.get().centerPositionUnderHouse();
                            gameLevel3D.showAnimatedMessage("READY!", 2.5f, messageCenter.x(), messageCenter.y());
                        }
                        setPlayerSteeringActionBindings();
                    }
                }
                default -> Logger.error("Unexpected game state '{}' on level start", context().currentGameState());
            }
        }

        gameLevel3D.updateLevelCounter3D();
        setActionBindings();
        playSubSceneFadingInAnimation();
    }

    @Override
    public void onSwitch_2D_3D(GameScene scene2D) {
        if (context().currentGame().optGameLevel().isEmpty()) {
            return;
        }
        final GameLevel gameLevel = context().currentGame().level();
        if (gameLevel3D == null) {
            replaceGameLevel3D();
        }
        gameLevel.pac().show();
        gameLevel.ghosts().forEach(Ghost::show);

        gameLevel3D.pac3D().init(gameLevel);
        gameLevel3D.pac3D().update(gameLevel);

        FoodLayer foodLayer = gameLevel.worldMap().foodLayer();
        gameLevel3D.pellets3D().forEach(pellet3D ->
            pellet3D.setVisible(!foodLayer.hasEatenFoodAtTile((Vector2i) pellet3D.getUserData())));
        gameLevel3D.energizers3D().forEach(energizer3D ->
                energizer3D.shape().setVisible(!foodLayer.hasEatenFoodAtTile(energizer3D.tile())));

        final String gameStateID = context().currentGameState().name();
        if (gameStateID.equals("HUNTING") || gameStateID.equals("GHOST_DYING")) { //TODO check this
            gameLevel3D.energizers3D().stream()
                .filter(energizer3D -> energizer3D.shape().isVisible())
                .forEach(Energizer3D::startPumping);
        }

        if (gameStateID.equals("HUNTING")) {
            if (gameLevel.pac().powerTimer().isRunning()) {
                ui.soundManager().loop(SoundID.PAC_MAN_POWER);
            }
            gameLevel3D.livesCounter3D().startTracking(gameLevel3D.pac3D());
        }
        gameLevel3D.updateLevelCounter3D();
        updateHUD();
        setActionBindings();
        playSubSceneFadingInAnimation();
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        context().currentGame().level().bonus().ifPresent(bonus -> {
            gameLevel3D.updateBonus3D(bonus);
            ui.soundManager().loop(SoundID.BONUS_ACTIVE);
        });
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        context().currentGame().level().bonus().ifPresent(bonus -> {
            gameLevel3D.bonus3D().ifPresent(Bonus3D::showEaten);
            ui.soundManager().stop(SoundID.BONUS_ACTIVE);
            ui.soundManager().play(SoundID.BONUS_EATEN);
        });
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        context().currentGame().level().bonus().ifPresent(bonus -> {
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
            Optional<House> optionalHouse = context().currentGame().level().worldMap().terrainLayer().optHouse();
            if (optionalHouse.isEmpty()) {
                Logger.error("No house found in this game level! WTF?");
            } else {
                Vector2f messageCenter = optionalHouse.get().centerPositionUnderHouse();
                gameLevel3D.showAnimatedMessage("READY!", 2.5f, messageCenter.x(), messageCenter.y());
            }
        }
    }

    @Override
    public void onGameStarted(GameEvent e) {
        FsmState<GameContext> state = context().currentGameState();
        boolean silent = context().currentGame().level().isDemoLevel() || state instanceof TestState;
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
            int eatenFoodCount = context().currentGame().level().worldMap().foodLayer().eatenFoodCount();
            if (ui.currentConfig().munchingSoundPlayed(eatenFoodCount)) {
                ui.soundManager().play(SoundID.PAC_MAN_MUNCHING);
            }
        }
    }

    @Override
    public void onPacGetsPower(GameEvent event) {
        ui.soundManager().stopSiren();
        if (!context().currentGame().isLevelCompleted(context().currentGame().level())) {
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

        gameLevel3D.pac3D().init(context().currentGame().level());
        gameLevel3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context().currentGame().level()));
        Logger.info("Initialized actors of game level 3D");

        gameLevel3D.livesCounter3D().startTracking(gameLevel3D.pac3D());
    }

    protected void updateCamera() {
        PerspectiveID id = perspectiveID.get();
        if (id != null && perspectivesByID.containsKey(id)) {
            perspectivesByID.get(id).update(camera, context());
        } else {
            Logger.error("No perspective with ID '{}' exists", id);
        }
    }

    protected void updateHUD() {
        ScoreManager scoreManager = context().currentGame().scoreManager();
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

    protected void updateSiren(Pac pac) {
        boolean pacChased = !pac.powerTimer().isRunning();
        if (pacChased) {
            // siren numbers are 1..4, hunting phase index = 0..7
            int huntingPhase = context().currentGame().level().huntingTimer().phaseIndex();
            int sirenNumber = 1 + huntingPhase / 2;
            float volume = 0.33f;
            switch (sirenNumber) {
                case 1 -> ui.soundManager().playSiren(SoundID.SIREN_1, volume);
                case 2 -> ui.soundManager().playSiren(SoundID.SIREN_2, volume);
                case 3 -> ui.soundManager().playSiren(SoundID.SIREN_3, volume);
                case 4 -> ui.soundManager().playSiren(SoundID.SIREN_4, volume);
                default -> throw new IllegalArgumentException("Illegal siren number " + sirenNumber);
            }
        }
    }

    protected void updateGhostSounds(Pac pac, Stream<Ghost> ghosts) {
        boolean returningHome = pac.isAlive() && ghosts.anyMatch(ghost ->
            ghost.state() == GhostState.RETURNING_HOME || ghost.state() == GhostState.ENTERING_HOUSE);
        if (returningHome) {
            ui.soundManager().loop(SoundID.GHOST_RETURNS);
        } else {
            ui.soundManager().stop(SoundID.GHOST_RETURNS);
        }
    }

    protected void updateSound(GameLevel gameLevel, FsmState<GameContext> gameState) {
        if (!ui.soundManager().isEnabled()) return;
        if (gameState.name().equals("HUNTING")) {
            updateSiren(gameLevel.pac());
            updateGhostSounds(gameLevel.pac(), gameLevel.ghosts());
        }
    }

    protected void showLevelTestMessage(GameLevel gameLevel) {
        WorldMap worldMap = gameLevel.worldMap();
        double x = worldMap.numCols() * HTS;
        double y = (worldMap.numRows() - 2) * TS;
        gameLevel3D.showAnimatedMessage("LEVEL %d (TEST)".formatted(gameLevel.number()), 5, x, y);
    }

    protected void playSubSceneFadingInAnimation() {
        subScene.setFill(SUB_SCENE_FILL_DARK);
        float fadingInSec = 3;
        new SequentialTransition(
            doNow(() -> {
                currentPerspective().ifPresent(perspective -> perspective.attach(camera));
                gameLevel3D.setVisible(true);
                scores3D.setVisible(true);
            }),
            new Timeline(
                new KeyFrame(Duration.seconds(fadingInSec),
                    new KeyValue(subScene.fillProperty(), SUB_SCENE_FILL_BRIGHT, Interpolator.LINEAR))
            )
        ).play();
    }

    protected void eatPellet3D(Shape3D pellet3D) {
        // remove after small delay for better visualization
        if (pellet3D.getParent() instanceof Group group) {
            pauseSec(0.05, () -> group.getChildren().remove(pellet3D)).play();
        }
    }
}