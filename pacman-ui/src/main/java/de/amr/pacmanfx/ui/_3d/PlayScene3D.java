/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameStateChangeEvent;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.worldmap.FoodLayer;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl.StateName;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.ui.action.DefaultActionBindingsManager;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.api.ActionBindingsManager;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.layout.GameUI_ContextMenu;
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
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape3D;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_QUIT_GAME_SCENE;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_TOGGLE_PLAY_SCENE_2D_3D;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_3D_AXES_VISIBLE;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_3D_PERSPECTIVE_ID;
import static de.amr.pacmanfx.uilib.Ufx.doNow;
import static de.amr.pacmanfx.uilib.Ufx.pauseSec;
import static java.util.Objects.requireNonNull;

/**
 * Common 3D play scene base class for all game variants.
 *
 * <p>Provides different camera perspectives that can be stepped through using key combinations
 * <code>Alt+LEFT</code> and <code>Alt+RIGHT</code>.
 */
public abstract class PlayScene3D extends Group implements GameScene {

    // Colors for fade-in effect
    private static final Color SUB_SCENE_FILL_DARK = Color.BLACK;
    private static final Color SUB_SCENE_FILL_BRIGHT = Color.TRANSPARENT;

    private static final float DISPLAY_SECONDS_READY_MESSAGE = 2.5f;

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

    protected final GameUI ui;
    protected final SubScene subScene;
    protected final PerspectiveCamera camera;
    protected final ActionBindingsManager actionBindings;
    protected final Group gameLevel3DParent = new Group();

    protected GameLevel3D gameLevel3D;
    protected Scores3D scores3D;

    // context menu radio button group
    private final ToggleGroup perspectiveToggleGroup = new ToggleGroup();

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

    @Override
    public GameUI ui() {
        return ui;
    }

    @Override
    public ActionBindingsManager actionBindings() {
        return actionBindings;
    }

    public Optional<GameLevel3D> level3D() {
        return Optional.ofNullable(gameLevel3D);
    }

    public ObjectProperty<PerspectiveID> perspectiveIDProperty() {
        return perspectiveID;
    }

    protected abstract void setActionBindings(GameLevel gameLevel);

    @Override
    public List<MenuItem> supplyContextMenuItems(Game game, ContextMenuEvent menuEvent) {
        final var dummy = new GameUI_ContextMenu(ui);
        dummy.addLocalizedTitleItem("scene_display");
        dummy.addLocalizedActionItem(ACTION_TOGGLE_PLAY_SCENE_2D_3D, "use_2D_scene");
        dummy.addLocalizedCheckBox(GameUI.PROPERTY_MINI_VIEW_ON, "pip");
        dummy.addLocalizedTitleItem("select_perspective");
        addPerspectiveRadioItems(dummy);
        dummy.addLocalizedTitleItem("pacman");
        dummy.addLocalizedCheckBox(game.usingAutopilotProperty(), "autopilot");
        dummy.addLocalizedCheckBox(game.immuneProperty(), "immunity");
        dummy.addSeparator();
        dummy.addLocalizedCheckBox(GameUI.PROPERTY_MUTED, "muted");
        dummy.addLocalizedActionItem(ACTION_QUIT_GAME_SCENE, "quit");
        return dummy.itemsCopy();
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
        game.hud().showScore(true);
        perspectiveIDProperty().bind(PROPERTY_3D_PERSPECTIVE_ID);
    }

    @Override
    public void end(Game game) {
        ui.soundManager().stopAll();
        if (gameLevel3D != null) {
            gameLevel3D.dispose();
            gameLevel3D = null;
        }
        gameLevel3DParent.getChildren().clear();
        perspectiveIDProperty().unbind();
    }

    @Override
    public void update(Game game) {
        final GameLevel gameLevel = game.level();
        if (gameLevel == null) {
            // Scene is already updated 2 ticks before the game level gets created!
            Logger.info("Tick #{}: Game level not yet created, update ignored", ui.clock().tickCount());
            return;
        }
        if (gameLevel3D == null) {
            Logger.info("Tick #{}: 3D game level not yet created", ui.clock().tickCount());
            return;
        }
        ui.soundManager().setEnabled(!gameLevel.isDemoLevel());
        gameLevel3D.update();
        updateCamera();
        updateHUD();
        updateSound(gameLevel, game.control().state());
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

        final StateMachine.State<?> state = context().currentGame().control().state();
        if (state.matches(StateName.HUNTING, StateName.EATING_GHOST)) { //TODO check this
            gameLevel3D.energizers3D().stream()
                .filter(energizer3D -> energizer3D.shape().isVisible())
                .forEach(Energizer3D::startPumping);
        }

        if (state.matches(StateName.HUNTING)) {
            if (level.pac().powerTimer().isRunning()) {
                ui.soundManager().loop(SoundID.PAC_MAN_POWER);
            }
            gameLevel3D.livesCounter3D().startTracking(gameLevel3D.pac3D());
        }
        gameLevel3D.updateLevelCounter3D();
        updateHUD();
        setActionBindings(level);
        playSubSceneFadingInAnimation();
    }

    // Game event handlers

    @Override
    public void onBonusActivated(GameEvent event) {
        context().currentGame().level().optBonus().ifPresent(bonus -> {
            gameLevel3D.updateBonus3D(bonus);
            ui.soundManager().loop(SoundID.ACTIVE);
        });
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        context().currentGame().level().optBonus().ifPresent(bonus -> {
            gameLevel3D.bonus3D().ifPresent(Bonus3D::showEaten);
            ui.soundManager().stop(SoundID.ACTIVE);
            ui.soundManager().play(SoundID.BONUS_EATEN);
        });
    }

    @Override
    public void onBonusExpires(GameEvent event) {
        context().currentGame().level().optBonus().ifPresent(bonus -> {
            gameLevel3D.bonus3D().ifPresent(Bonus3D::expire);
            ui.soundManager().stop(SoundID.ACTIVE);
        });
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        final Game game = e.game();
        final StateMachine.State<Game> state = e.newState();
        if (state instanceof TestState) {
            game.optGameLevel().ifPresent(level -> {
                replaceGameLevel3D(level);
                showLevelTestMessage(level);
                PROPERTY_3D_PERSPECTIVE_ID.set(PerspectiveID.TOTAL);
            });
        }
        else {
            if (state.matches(StateName.HUNTING)) {
                gameLevel3D.onHuntingStart();
            }
            else if (state.matches(StateName.PACMAN_DYING)) {
                gameLevel3D.onPacManDying(state);
            }
            else if (state.matches(StateName.EATING_GHOST)) {
                gameLevel3D.onEatingGhost();
            }
            else if (state.matches(StateName.LEVEL_COMPLETE)) {
                gameLevel3D.onLevelComplete(state, perspectiveID);
            }
            else if (state.matches(StateName.GAME_OVER)) {
                gameLevel3D.onGameOver(state);
            }
            else if (state.matches(StateName.STARTING_GAME_OR_LEVEL)) {
                if (gameLevel3D != null) {
                    gameLevel3D.onStartingGame();
                } else {
                    Logger.error("No 3D game level available"); //TODO can this happen?
                }
            }
        }
    }

    @Override
    public void onGameContinues(GameEvent e) {
        final Game game = context().currentGame();
        if (gameLevel3D != null) {
            game.optGameLevel().ifPresent(this::showReadyMessage);
        }
    }

    @Override
    public void onGameStarts(GameEvent e) {
        final Game game = context().currentGame();
        final StateMachine.State<Game> state = game.control().state();
        final boolean silent = game.level().isDemoLevel() || state instanceof TestState;
        if (!silent) {
            ui.soundManager().play(SoundID.GAME_READY);
        }
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        ui.soundManager().play(SoundID.GHOST_EATEN);
    }


    @Override
    public void onLevelCreated(GameEvent e) {
        e.game().optGameLevel().ifPresent(this::replaceGameLevel3D);
    }

    @Override
    public void onLevelStarts(GameEvent e) {
        final Game game = e.game();
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
                ui.soundManager().play(SoundID.PAC_MAN_MUNCHING);
                lastMunchingSoundPlayedTick = now;
            }
        }
    }

    @Override
    public void onPacPowerBegins(GameEvent event) {
        final Game game = context().currentGame();
        ui.soundManager().stopSiren();
        if (!game.isLevelCompleted()) {
            gameLevel3D.pac3D().setMovementPowerMode(true);
            ui.soundManager().loop(SoundID.PAC_MAN_POWER);
            gameLevel3D.playWallColorFlashing();
        }
    }

    @Override
    public void onPacPowerEnds(GameEvent event) {
        gameLevel3D.pac3D().setMovementPowerMode(false);
        gameLevel3D.stopWallColorFlashing();
        ui.soundManager().stop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onSpecialScoreReached(GameEvent e) {
        ui.soundManager().play(SoundID.EXTRA_LIFE);
    }

    @Override
    public void onUnspecifiedChange(GameEvent event) {
        // TODO: remove (this is only used by game state GameState.TESTING_CUT_SCENES)
        ui.updateGameScene(true);
    }

    // protected

    protected void createPerspectives() {
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

    protected void createScores3D() {
        scores3D = new Scores3D(
            ui.globalAssets().translated("score.score"),
            ui.globalAssets().translated("score.high_score"),
            ui.globalAssets().font_Arcade_8
        );

        // The scores are always displayed in full view, regardless which perspective is used
        scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
        scores3D.rotateProperty().bind(camera.rotateProperty());

        scores3D.translateXProperty().bind(gameLevel3DParent.translateXProperty().add(TS));
        scores3D.translateYProperty().bind(gameLevel3DParent.translateYProperty().subtract(4.5 * TS));
        scores3D.translateZProperty().bind(gameLevel3DParent.translateZProperty().subtract(4.5 * TS));
        scores3D.setVisible(false);
    }

    protected Optional<Perspective> currentPerspective() {
        return perspectiveID.get() == null ? Optional.empty() : Optional.of(perspectivesByID.get(perspectiveID.get()));
    }

    protected void addPerspectiveRadioItems(GameUI_ContextMenu contextMenu) {
        for (PerspectiveID id : PerspectiveID.values()) {
            final RadioMenuItem item = contextMenu.addLocalizedRadioButton("perspective_id_" + id.name());
            item.setUserData(id);
            item.setToggleGroup(perspectiveToggleGroup);
            if (id == PROPERTY_3D_PERSPECTIVE_ID.get())  {
                item.setSelected(true);
            }
            item.setOnAction(e -> PROPERTY_3D_PERSPECTIVE_ID.set(id));
        }
        PROPERTY_3D_PERSPECTIVE_ID.addListener(this::handlePerspectiveIDChange);
        contextMenu.setOnHidden(e -> PROPERTY_3D_PERSPECTIVE_ID.removeListener(this::handlePerspectiveIDChange));
    }

    protected void handlePerspectiveIDChange(ObservableValue<? extends PerspectiveID> property, PerspectiveID oldID, PerspectiveID newID) {
        for (Toggle toggle : perspectiveToggleGroup.getToggles()) {
            if (toggle.getUserData() == newID) {
                perspectiveToggleGroup.selectToggle(toggle);
            }
        }
    }

    protected GameLevel3D createGameLevel3D() {
        return new GameLevel3D(ui);
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
        gameLevel3D = createGameLevel3D();
        Logger.info("Created new game level 3D");

        gameLevel3DParent.getChildren().setAll(gameLevel3D);

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

    protected void updateHUD() {
        final Game game = context().currentGame();
        final Score score = game.score(), highScore = game.highScore();
        if (score.isEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else { // disabled, show text "GAME OVER"
            Color color = ui.currentConfig().assets().color("color.game_over_message");
            scores3D.showTextForScore(ui.globalAssets().translated("score.game_over"), color);
        }
        // Always show high score
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
    }

    protected void updateSiren(Pac pac) {
        final Game game = context().currentGame();
        boolean pacChased = !pac.powerTimer().isRunning();
        if (pacChased) {
            // siren numbers are 1..4, hunting phase index = 0..7
            int huntingPhase = game.level().huntingTimer().phaseIndex();
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
            if (!ui.soundManager().isPlaying(SoundID.GHOST_RETURNS)) {
                ui.soundManager().loop(SoundID.GHOST_RETURNS);
            }
        } else {
            ui.soundManager().stop(SoundID.GHOST_RETURNS);
        }
    }

    protected void updateSound(GameLevel gameLevel, StateMachine.State<Game> state) {
        if (!ui.soundManager().isEnabled()) return;
        if (state.matches(StateName.HUNTING)) {
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

    protected void showReadyMessage(GameLevel level) {
        final Optional<House> optHouse = level.worldMap().terrainLayer().optHouse();
        if (optHouse.isPresent()) {
            final Vector2f messagePosition = optHouse.get().centerPositionUnderHouse();
            gameLevel3D.showAnimatedMessage("READY!", DISPLAY_SECONDS_READY_MESSAGE, messagePosition.x(), messagePosition.y());
        } else {
            Logger.error("Cannot display level READY message: no house found in this game level! WTF?");
        }
    }
}