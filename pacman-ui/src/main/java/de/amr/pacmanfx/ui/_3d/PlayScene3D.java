/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameStateChangeEvent;
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
import de.amr.pacmanfx.ui.action.DefaultActionBindingsManager;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.layout.GameUI_ContextMenu;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.assets.RandomTextPicker;
import de.amr.pacmanfx.uilib.assets.Translator;
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
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape3D;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_QUIT_GAME_SCENE;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_TOGGLE_PLAY_SCENE_2D_3D;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.doNow;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSec;
import static java.util.Objects.requireNonNull;

/**
 * Common 3D play scene base class for all game variants.
 */
public abstract class PlayScene3D implements GameScene {

    private static final float FADE_IN_SECONDS = 3;

    // Colors for fade effect
    private static final Color SCENE_FILL_DARK = Color.BLACK;
    private static final Color SCENE_FILL_BRIGHT = Color.TRANSPARENT;

    private static final float READY_MESSAGE_DISPLAY_SECONDS = 2.5f;

    //TODO fix sound files
    private static final float SIREN_VOLUME = 0.33f;

    private final Map<PerspectiveID, Perspective> perspectivesByID = new EnumMap<>(PerspectiveID.class);

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

    protected final ActionBindingsManager actionBindings = new DefaultActionBindingsManager();

    protected final SubScene subScene;
    protected final Group subSceneRoot = new Group();
    protected final Group level3DParent = new Group();
    protected final PerspectiveCamera camera = new PerspectiveCamera(true);

    protected GameContext context;
    protected GameUI ui;
    protected GameLevel3D gameLevel3D;
    protected Scores3D scores3D;
    protected RandomTextPicker<String> pickerGameOverMessages;

    // context menu radio button group
    private final ToggleGroup perspectivesGroup = new ToggleGroup();

    public PlayScene3D() {
        // initial size is irrelevant (size gets bound to parent scene size eventually)
        subScene = new SubScene(subSceneRoot, 88, 88, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(SCENE_FILL_DARK);

        final var coordinateSystem = new CoordinateSystem();
        coordinateSystem.visibleProperty().bind(GameUI.PROPERTY_3D_AXES_VISIBLE);

        subSceneRoot.getChildren().setAll(level3DParent, coordinateSystem);

        createPerspectives();
    }

    protected abstract void setActionBindings(GameLevel gameLevel);

    public void setContext(GameContext context) {
        this.context = requireNonNull(context);
    }

    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
        pickerGameOverMessages = RandomTextPicker.fromBundle(ui.localizedTexts(), "game.over");
        //TODO reconsider this
        replaceScores3D();
    }

    public Optional<GameLevel3D> level3D() {
        return Optional.ofNullable(gameLevel3D);
    }

    public ObjectProperty<PerspectiveID> perspectiveIDProperty() {
        return perspectiveID;
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
    public GameContext context() {
        return context;
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
        final var menu = new GameUI_ContextMenu();
        menu.setUI(ui);
        menu.addLocalizedTitleItem("scene_display");
        menu.addLocalizedActionItem(ACTION_TOGGLE_PLAY_SCENE_2D_3D, "use_2D_scene");
        menu.addLocalizedCheckBox(GameUI.PROPERTY_MINI_VIEW_ON, "pip");
        menu.addLocalizedTitleItem("select_perspective");
        addPerspectiveRadioItems(menu);
        menu.addLocalizedTitleItem("pacman");
        menu.addLocalizedCheckBox(game.usingAutopilotProperty(), "autopilot");
        menu.addLocalizedCheckBox(game.immuneProperty(), "immunity");
        menu.addSeparator();
        menu.addLocalizedCheckBox(GameUI.PROPERTY_MUTED, "muted");
        menu.addLocalizedActionItem(ACTION_QUIT_GAME_SCENE, "quit");
        return Optional.of(menu);
    }

    private final ChangeListener<PerspectiveID> perspectiveIDChangeListener = (_, _, newID) -> {
        for (Toggle toggle : perspectivesGroup.getToggles()) {
            if (Objects.equals(toggle.getUserData(), newID)) {
                perspectivesGroup.selectToggle(toggle);
                break;
            }
        }
    };

    protected void addPerspectiveRadioItems(GameUI_ContextMenu contextMenu) {
        for (PerspectiveID id : PerspectiveID.values()) {
            final RadioMenuItem item = contextMenu.addLocalizedRadioButton("perspective_id_" + id.name());
            item.setUserData(id);
            item.setToggleGroup(perspectivesGroup);
            if (id == GameUI.PROPERTY_3D_PERSPECTIVE_ID.get())  {
                item.setSelected(true);
            }
            item.setOnAction(_ -> GameUI.PROPERTY_3D_PERSPECTIVE_ID.set(id));
        }
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
        GameUI.PROPERTY_3D_PERSPECTIVE_ID.addListener(perspectiveIDChangeListener);
        perspectiveIDProperty().bind(GameUI.PROPERTY_3D_PERSPECTIVE_ID);
    }

    @Override
    public void end(Game game) {
        soundManager().stopAll();
        if (gameLevel3D != null) {
            gameLevel3D.dispose();
            gameLevel3D = null;
        }
        level3DParent.getChildren().clear();
        GameUI.PROPERTY_3D_PERSPECTIVE_ID.removeListener(perspectiveIDChangeListener);
        perspectiveIDProperty().unbind();
    }

    @Override
    public void update(Game game) {
        final long tick = ui.clock().tickCount();
        // Scene is already updated 2 ticks before the game level gets created!
        if (game.optGameLevel().isEmpty()) {
            Logger.info("Tick #{}: Game level not yet created, update ignored", tick);
            return;
        }
        if (gameLevel3D == null) {
            Logger.info("Tick #{}: 3D game level not yet created", tick);
            return;
        }
        gameLevel3D.update();
        updateCamera();
        updateHUD(game);
        soundManager().setEnabled(!game.level().isDemoLevel());
        updateSound(game.level());
    }

    @Override
    public Optional<SubScene> optSubScene() {
        return Optional.of(subScene);
    }

    @Override
    public void onSwitch_2D_3D(GameScene scene2D) {
        final Game game = context().currentGame();
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
        gameLevel3D.pellets3D().forEach(pellet3D ->
            pellet3D.setVisible(!foodLayer.hasEatenFoodAtTile((Vector2i) pellet3D.getUserData())));
        gameLevel3D.energizers3D().forEach(energizer3D ->
                energizer3D.shape().setVisible(!foodLayer.hasEatenFoodAtTile(energizer3D.tile())));

        final StateMachine.State<?> state = game.control().state();
        if (state.matches(StateName.HUNTING, StateName.EATING_GHOST)) { //TODO check this
            gameLevel3D.energizers3D().stream()
                .filter(energizer3D -> energizer3D.shape().isVisible())
                .forEach(Energizer3D::startPumping);
        }

        if (state.matches(StateName.HUNTING)) {
            if (level.pac().powerTimer().isRunning()) {
                soundManager().loop(SoundID.PAC_MAN_POWER);
            }
            gameLevel3D.livesCounter3D().startTracking(gameLevel3D.pac3D());
        }
        gameLevel3D.updateLevelCounter3D();
        updateHUD(game);
        setActionBindings(level);
        playSubSceneFadingInAnimation();
    }

    // Game event handlers

    @Override
    public void onBonusActivated(GameEvent event) {
        if (gameLevel3D == null) {
            Logger.error("No game level3D exists!");
            return;
        }
        event.game().optGameLevel().flatMap(GameLevel::optBonus).ifPresent(bonus -> {
            gameLevel3D.updateBonus3D(bonus);
            soundManager().loop(SoundID.BONUS_ACTIVE);
        });
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        if (gameLevel3D == null) {
            Logger.error("No game level3D exists!");
            return;
        }
        event.game().optGameLevel().flatMap(GameLevel::optBonus).ifPresent(_ -> {
            gameLevel3D.bonus3D().ifPresent(Bonus3D::showEaten);
            soundManager().stop(SoundID.BONUS_ACTIVE);
            soundManager().play(SoundID.BONUS_EATEN);
        });
    }

    @Override
    public void onBonusExpires(GameEvent event) {
        if (gameLevel3D == null) {
            Logger.error("No game level3D exists!");
            return;
        }
        event.game().optGameLevel().flatMap(GameLevel::optBonus).ifPresent(_ -> {
            gameLevel3D.bonus3D().ifPresent(Bonus3D::expire);
            soundManager().stop(SoundID.BONUS_ACTIVE);
        });
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent changeEvent) {
        final Game game = changeEvent.game();
        final StateMachine.State<Game> newState = changeEvent.newState();
        if (newState instanceof TestState) {
            game.optGameLevel().ifPresent(level -> {
                replaceGameLevel3D(level);
                showLevelTestMessage(level);
                GameUI.PROPERTY_3D_PERSPECTIVE_ID.set(PerspectiveID.TOTAL);
            });
        }
        else {
            if (newState.matches(StateName.HUNTING)) {
                gameLevel3D.onHuntingStart();
            }
            else if (newState.matches(StateName.PACMAN_DYING)) {
                gameLevel3D.onPacManDying(newState);
            }
            else if (newState.matches(StateName.EATING_GHOST)) {
                gameLevel3D.onEatingGhost();
            }
            else if (newState.matches(StateName.LEVEL_COMPLETE)) {
                gameLevel3D.onLevelComplete(newState, perspectiveID);
            }
            else if (newState.matches(StateName.GAME_OVER)) {
                gameLevel3D.onGameOver(newState);
                final boolean showMessage = randomInt(0, 1000) < 250;
                if (!game.level().isDemoLevel() && showMessage) {
                    final String message = pickerGameOverMessages.nextText();
                    ui.showFlashMessage(Duration.seconds(2.5), message);
                }
            }
            else if (newState.matches(StateName.STARTING_GAME_OR_LEVEL)) {
                if (gameLevel3D != null) {
                    gameLevel3D.onStartingGame();
                } else {
                    Logger.error("No 3D game level available"); //TODO can this happen?
                }
            }
        }
    }

    @Override
    public void onGameContinues(GameEvent event) {
        final Game game = event.game();
        if (gameLevel3D != null) {
            game.optGameLevel().ifPresent(this::showReadyMessage);
        }
    }

    @Override
    public void onGameStarts(GameEvent event) {
        final Game game = event.game();
        final StateMachine.State<Game> state = game.control().state();
        final boolean silent = game.level().isDemoLevel() || state instanceof TestState;
        if (!silent) {
            soundManager().play(SoundID.GAME_READY);
        }
    }

    @Override
    public void onGhostEaten(GameEvent event) {
        soundManager().play(SoundID.GHOST_EATEN);
    }


    @Override
    public void onLevelCreated(GameEvent event) {
        event.game().optGameLevel().ifPresent(this::replaceGameLevel3D);
    }

    @Override
    public void onLevelStarts(GameEvent event) {
        final Game game = event.game();
        if (game.optGameLevel().isEmpty()) {
            Logger.error("No game level exists on level start? WTF!");
            return;
        }
        final GameLevel level = game.level();
        final StateMachine.State<Game> state = game.control().state();

        if (state instanceof TestState) {
            replaceGameLevel3D(level); //TODO check when to destroy previous level
            gameLevel3D.energizers3D().forEach(Energizer3D::startPumping);
            showLevelTestMessage(level);
        }
        else {
            if (!level.isDemoLevel() && state.matches(StateName.STARTING_GAME_OR_LEVEL, StateName.LEVEL_TRANSITION)) {
                showReadyMessage(level);
            }
        }

        gameLevel3D.updateLevelCounter3D();
        setActionBindings(level);
        playSubSceneFadingInAnimation();
    }

    private long lastMunchingSoundPlayedTick;

    @Override
    public void onPacFindsFood(GameEvent event) {
        if (event.tile() == null) {
            // When cheat "eat all pellets" has been used, no tile is present in the event.
            gameLevel3D.pellets3D().forEach(this::eatPellet3D);
        } else {
            final Energizer3D energizer3D = gameLevel3D.energizers3D().stream()
                .filter(e3D -> event.tile().equals(e3D.tile())).findFirst().orElse(null);
            if (energizer3D != null) {
                energizer3D.onEaten();
            } else {
                gameLevel3D.pellets3D().stream()
                    .filter(pellet3D -> event.tile().equals(pellet3D.getUserData()))
                    .findFirst()
                    .ifPresent(this::eatPellet3D);
            }
            // Play munching sound?
            final long now = ui.clock().tickCount();
            final long passed = now - lastMunchingSoundPlayedTick;
            Logger.debug("Pac found food, tick={} passed since last time={}", now, passed);
            byte minDelay = ui.currentConfig().munchingSoundDelay();
            if (passed > minDelay  || minDelay == 0) {
                soundManager().play(SoundID.PAC_MAN_MUNCHING);
                lastMunchingSoundPlayedTick = now;
            }
        }
    }

    @Override
    public void onPacPowerBegins(GameEvent event) {
        final Game game = event.game();
        soundManager().stopSiren();
        if (!game.isLevelCompleted()) {
            gameLevel3D.pac3D().setMovementPowerMode(true);
            soundManager().loop(SoundID.PAC_MAN_POWER);
            gameLevel3D.playWallColorFlashing();
        }
    }

    @Override
    public void onPacPowerEnds(GameEvent event) {
        gameLevel3D.pac3D().setMovementPowerMode(false);
        gameLevel3D.stopWallColorFlashing();
        soundManager().stop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onSpecialScoreReached(GameEvent event) {
        soundManager().play(SoundID.EXTRA_LIFE);
    }

    @Override
    public void onUnspecifiedChange(GameEvent event) {
        // TODO: remove (this is only used by game state GameState.TESTING_CUT_SCENES)
        ui.views().playView().updateGameScene(event.game(), true);
    }

    // protected

    protected void createPerspectives() {
        perspectivesByID.put(PerspectiveID.DRONE, new DronePerspective());
        perspectivesByID.put(PerspectiveID.TOTAL, new TotalPerspective());
        perspectivesByID.put(PerspectiveID.TRACK_PLAYER, new TrackingPlayerPerspective());
        perspectivesByID.put(PerspectiveID.NEAR_PLAYER, new StalkingPlayerPerspective());

        perspectiveID.addListener((_, oldID, newID) -> {
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

    private void replaceScores3D() {
        if (scores3D != null) {
            subSceneRoot.getChildren().remove(scores3D);
        }
        createScores3D(ui);
        subSceneRoot.getChildren().add(scores3D);
    }

    protected void createScores3D(Translator localizedTexts) {
        scores3D = new Scores3D(
            localizedTexts.translate("score.score"),
            localizedTexts.translate("score.high_score"),
            GameUI.FONT_ARCADE_8
        );

        // The scores are always displayed in full view, regardless which perspective is used
        scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
        scores3D.rotateProperty().bind(camera.rotateProperty());

        scores3D.translateXProperty().bind(level3DParent.translateXProperty().add(TS));
        scores3D.translateYProperty().bind(level3DParent.translateYProperty().subtract(4.5 * TS));
        scores3D.translateZProperty().bind(level3DParent.translateZProperty().subtract(4.5 * TS));
        scores3D.setVisible(false);
    }

    protected Optional<Perspective> currentPerspective() {
        return perspectiveID.get() == null ? Optional.empty() : Optional.of(perspectivesByID.get(perspectiveID.get()));
    }

    protected GameLevel3D createGameLevel3D(GameLevel level) {
        return new GameLevel3D(ui.currentConfig(), ui.userPrefs(), ui, level);
    }

    protected void replaceGameLevel3D(GameLevel level) {
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

    protected void updateCamera() {
        PerspectiveID id = perspectiveID.get();
        if (id != null && perspectivesByID.containsKey(id)) {
            perspectivesByID.get(id).update(camera, context());
        } else {
            Logger.error("No perspective with ID '{}' exists", id);
        }
    }

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

    protected void updateSiren(GameLevel level) {
        final boolean pacChased = !level.pac().powerTimer().isRunning();
        if (pacChased) {
            // siren numbers are 1..4, hunting phase index = 0..7
            final int huntingPhase = level.huntingTimer().phaseIndex();
            final int sirenNumber = 1 + huntingPhase / 2;
            soundManager().playSiren(sirenNumber, SIREN_VOLUME); // TODO change sound file volume?
        }
    }

    protected void updateGhostSounds(Pac pac, Stream<Ghost> ghosts) {
        boolean returningHome = pac.isAlive() && ghosts.anyMatch(ghost ->
            ghost.state() == GhostState.RETURNING_HOME || ghost.state() == GhostState.ENTERING_HOUSE);
        if (returningHome) {
            if (!soundManager().isPlaying(SoundID.GHOST_RETURNS)) {
                soundManager().loop(SoundID.GHOST_RETURNS);
            }
        } else {
            soundManager().stop(SoundID.GHOST_RETURNS);
        }
    }

    protected void updateSound(GameLevel level) {
        if (!soundManager().isEnabled()) {
            return;
        }
        if (level.game().control().state().matches(StateName.HUNTING)) {
            updateSiren(level);
            updateGhostSounds(level.pac(), level.ghosts());
        }
    }

    protected void showLevelTestMessage(GameLevel level) {
        final WorldMap worldMap = level.worldMap();
        final double x = worldMap.numCols() * HTS;
        final double y = (worldMap.numRows() - 2) * TS;
        gameLevel3D.showAnimatedMessage("LEVEL %d (TEST)".formatted(level.number()), 5, x, y);
    }

    protected void playSubSceneFadingInAnimation() {
        final var fadeInEffect = new Timeline(
            new KeyFrame(Duration.seconds(FADE_IN_SECONDS),
                new KeyValue(subScene.fillProperty(), SCENE_FILL_BRIGHT, Interpolator.LINEAR))
        );
        subScene.setFill(SCENE_FILL_DARK);
        new SequentialTransition(
            doNow(() -> {
                currentPerspective().ifPresent(perspective -> perspective.attach(camera));
                gameLevel3D.setVisible(true);
                scores3D.setVisible(true);
            }),
            fadeInEffect
        ).play();
    }

    protected void eatPellet3D(Shape3D pellet3D) {
        // remove after small delay for better visualization
        if (pellet3D.getParent() instanceof Group group) {
            pauseSec(0.05, () -> group.getChildren().remove(pellet3D)).play();
        }
    }

    protected void showReadyMessage(GameLevel level) {
        level.worldMap().terrainLayer().optHouse().ifPresentOrElse(house -> {
            final Vector2f center = house.centerPositionUnderHouse();
            gameLevel3D.showAnimatedMessage("READY!", READY_MESSAGE_DISPLAY_SECONDS, center.x(), center.y());
        }, () -> Logger.error("Cannot display READY message: no house in this game level! WTF?"));
    }
}