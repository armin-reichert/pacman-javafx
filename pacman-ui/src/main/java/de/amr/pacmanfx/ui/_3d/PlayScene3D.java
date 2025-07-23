/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.ActionBindingMap;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.Bonus3D;
import de.amr.pacmanfx.uilib.model3D.Energizer3D;
import de.amr.pacmanfx.uilib.model3D.Pellet3D;
import de.amr.pacmanfx.uilib.model3D.Scores3D;
import de.amr.pacmanfx.uilib.widgets.CoordinateSystem;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.isOneOf;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS_MEDIUM;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS_SHORT;
import static de.amr.pacmanfx.ui.GameUI.GLOBAL_ACTION_BINDINGS;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
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

    protected final GameUI ui;
    protected final SubScene subScene;
    protected final PerspectiveCamera camera = new PerspectiveCamera(true);
    protected final ActionBindingMap actionBindings;
    protected final Group gameLevel3DRoot = new Group();
    protected GameLevel3D gameLevel3D;
    protected Scores3D scores3D;

    public PlayScene3D(GameUI ui) {
        this.ui = requireNonNull(ui);
        this.actionBindings = new ActionBindingMap(ui.theKeyboard());

        perspectiveMap.put(PerspectiveID.DRONE, new DronePerspective());
        perspectiveMap.put(PerspectiveID.TOTAL, new TotalPerspective());
        perspectiveMap.put(PerspectiveID.TRACK_PLAYER, new TrackingPlayerPerspective());
        perspectiveMap.put(PerspectiveID.NEAR_PLAYER, new StalkingPlayerPerspective());

        var root = new Group();
        // initial size is irrelevant because size gets bound to parent scene size later
        subScene = new SubScene(root, 88, 88, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(SUB_SCENE_FILL_DARK);

        // The score is always displayed in full view, regardless which perspective is used
        scores3D = new Scores3D(
            ui.theAssets().text("score.score"),
            ui.theAssets().text("score.high_score"),
            ui.theAssets().arcadeFont(TS)
        );
        scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
        scores3D.rotateProperty().bind(camera.rotateProperty());

        // Just for debugging and posing
        var coordinateSystem = new CoordinateSystem();
        coordinateSystem.visibleProperty().bind(ui.property3DAxesVisible());

        root.getChildren().setAll(gameLevel3DRoot, scores3D, coordinateSystem);
    }

    public ObjectProperty<PerspectiveID> perspectiveIDProperty() {
        return perspectiveIDProperty;
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
        actionBindings.runMatchingAction(ui);
    }

    @Override
    public GameContext gameContext() {
        return ui.theGameContext();
    }

    @Override
    public ActionBindingMap actionBindings() {
        return actionBindings;
    }

    public Optional<GameLevel3D> level3D() {
        return Optional.ofNullable(gameLevel3D);
    }

    // Context menu

    private final ToggleGroup perspectiveToggleGroup = new ToggleGroup();

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent menuEvent, ContextMenu menu) {
        var miUse2DScene = new MenuItem(ui.theAssets().text("use_2D_scene"));
        miUse2DScene.setOnAction(e -> ACTION_TOGGLE_PLAY_SCENE_2D_3D.executeIfEnabled(ui));

        var miToggleMiniView = new CheckMenuItem(ui.theAssets().text("pip"));
        miToggleMiniView.selectedProperty().bindBidirectional(ui.propertyMiniViewOn());

        var miAutopilot = new CheckMenuItem(ui.theAssets().text("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(theGameContext().theGameController().propertyUsingAutopilot());

        var miImmunity = new CheckMenuItem(ui.theAssets().text("immunity"));
        miImmunity.selectedProperty().bindBidirectional(theGameContext().theGameController().propertyImmunity());

        var miMuted = new CheckMenuItem(ui.theAssets().text("muted"));
        miMuted.selectedProperty().bindBidirectional(ui.propertyMuted());

        var miQuit = new MenuItem(ui.theAssets().text("quit"));
        miQuit.setOnAction(e -> ACTION_QUIT_GAME_SCENE.executeIfEnabled(ui));

        var items = new ArrayList<MenuItem>();
        items.add(ui.createContextMenuTitleItem(ui.theAssets().text("scene_display")));
        items.add(miUse2DScene);
        items.add(miToggleMiniView);
        items.add(ui.createContextMenuTitleItem(ui.theAssets().text("select_perspective")));
        items.addAll(createPerspectiveRadioItems(menu));
        items.add(ui.createContextMenuTitleItem(ui.theAssets().text("pacman")));
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
            var item = new RadioMenuItem(ui.theAssets().text("perspective_id_" + id.name()));
            item.setUserData(id);
            item.setToggleGroup(perspectiveToggleGroup);
            if (id == ui.property3DPerspective().get())  {
                item.setSelected(true);
            }
            item.setOnAction(e -> ui.property3DPerspective().set(id));
            items.add(item);
        }
        Logger.info("Added listener to UI property3DPerspective property");
        ui.property3DPerspective().addListener(this::handle3DPerspectiveChange);
        menu.setOnHidden(e -> {
            ui.property3DPerspective().removeListener(this::handle3DPerspectiveChange);
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
        actionBindings.removeFromKeyboard();
        actionBindings.bind(ACTION_PERSPECTIVE_PREVIOUS, GLOBAL_ACTION_BINDINGS);
        actionBindings.bind(ACTION_PERSPECTIVE_NEXT, GLOBAL_ACTION_BINDINGS);
        actionBindings.bind(ACTION_TOGGLE_DRAW_MODE, GLOBAL_ACTION_BINDINGS);
        if (gameContext().optGameLevel().isPresent()) {
            if (gameContext().theGameLevel().isDemoLevel()) {
                actionBindings.bind(ACTION_ARCADE_INSERT_COIN, GLOBAL_ACTION_BINDINGS);
            } else {
                setPlayerSteeringActionBindings();
                actionBindings.bind(ACTION_CHEAT_EAT_ALL_PELLETS, GLOBAL_ACTION_BINDINGS);
                actionBindings.bind(ACTION_CHEAT_ADD_LIVES, GLOBAL_ACTION_BINDINGS);
                actionBindings.bind(ACTION_CHEAT_ENTER_NEXT_LEVEL, GLOBAL_ACTION_BINDINGS);
                actionBindings.bind(ACTION_CHEAT_KILL_GHOSTS, GLOBAL_ACTION_BINDINGS);
            }
        }
        actionBindings.updateKeyboard();
    }

    /**
     * Overridden by Tengen Ms. Pac-Man play scene 3D to use keys representing "Joypad" buttons.
     */
    protected void setPlayerSteeringActionBindings() {
        actionBindings.bind(ACTION_STEER_UP, GLOBAL_ACTION_BINDINGS);
        actionBindings.bind(ACTION_STEER_DOWN, GLOBAL_ACTION_BINDINGS);
        actionBindings.bind(ACTION_STEER_LEFT, GLOBAL_ACTION_BINDINGS);
        actionBindings.bind(ACTION_STEER_RIGHT, GLOBAL_ACTION_BINDINGS);
    }

    @Override
    public void init() {
        gameContext().theGame().theHUD().showScore(true);
        perspectiveIDProperty().bind(ui.property3DPerspective());
    }

    @Override
    public void end() {
        ui.theSound().stopAll();
        if (gameLevel3D != null) {
            gameLevel3DRoot.getChildren().clear();
            gameLevel3D.dispose();
            gameLevel3D = null;
        }
    }

    @Override
    public void update() {
        if (gameContext().optGameLevel().isEmpty()) {
            // update gets called already 2 times before game level has been created!
            Logger.info("Tick #{}: Game level not yet created, update ignored", ui.theGameClock().tickCount());
            return;
        }
        if (gameLevel3D == null) {
            Logger.info("Tick #{}: 3D game level not yet created", ui.theGameClock().tickCount());
            return;
        }
        gameLevel3D.tick();
        updateScores();
        updateSound();
        updateCamera();
    }

    @Override
    public Optional<SubScene> optSubScene() {
        return Optional.of(subScene);
    }

    @Override
    public void onEnterGameState(GameState state) {
        requireNonNull(state);
        switch (state) {
            case HUNTING          -> gameLevel3D.onHuntingStart();
            case PACMAN_DYING     -> gameLevel3D.onPacManDying(state);
            case GHOST_DYING      -> gameLevel3D.onGhostDying();
            case LEVEL_COMPLETE   -> gameLevel3D.onLevelComplete(state, perspectiveIDProperty);
            case GAME_OVER        -> gameLevel3D.onGameOver(state);
            case STARTING_GAME    -> {
                if (gameLevel3D != null) {
                    gameLevel3D.onStartingGame();
                }
            }
            case TESTING_LEVELS_SHORT, TESTING_LEVELS_MEDIUM -> {
                replaceGameLevel3D();
                showLevelTestMessage();
                ui.property3DPerspective().set(PerspectiveID.TOTAL);
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
        final GameLevel gameLevel = gameContext().theGameLevel();
        switch (gameContext().theGameState()) {
            case STARTING_GAME, LEVEL_TRANSITION -> {
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
            case TESTING_LEVELS_SHORT, TESTING_LEVELS_MEDIUM -> {
                replaceGameLevel3D(); //TODO check when to destroy previous level
                gameLevel3D.livesCounter3D().map(LivesCounter3D::lookingAroundAnimation).ifPresent(ManagedAnimation::playFromStart);
                gameLevel3D.energizers3D().forEach(Energizer3D::pump);
                showLevelTestMessage();
            }
            default -> Logger.error("Unexpected game state '{}' on level start", gameContext().theGameState());
        }
        gameLevel3D.levelCounter3D().ifPresent(levelCounter3D -> levelCounter3D.update(ui, gameContext().theGame().theHUD().theLevelCounter()));
        initPerspective();
        setActionBindings();
        fadeInSubScene();
    }

    @Override
    public void onSwitch_2D_3D(GameScene scene2D) {
        if (gameContext().optGameLevel().isEmpty()) {
            return;
        }
        final GameLevel gameLevel = gameContext().theGameLevel();
        if (gameLevel3D == null) {
            replaceGameLevel3D();
        }
        gameLevel.pac().show();
        gameLevel.ghosts().forEach(Ghost::show);

        gameLevel3D.pac3D().init();
        gameLevel3D.pac3D().update();
        gameLevel3D.pellets3D().forEach(pellet -> pellet.shape3D().setVisible(!gameLevel.tileContainsEatenFood(pellet.tile())));
        gameLevel3D.energizers3D().forEach(energizer -> energizer.shape3D().setVisible(!gameLevel.tileContainsEatenFood(energizer.tile())));
        if (isOneOf(gameContext().theGameState(), GameState.HUNTING, GameState.GHOST_DYING)) { //TODO check this
            gameLevel3D.energizers3D()
                .filter(energizer3D -> energizer3D.shape3D().isVisible())
                .forEach(Energizer3D::pump);
        }

        if (gameContext().theGameState() == GameState.HUNTING) {
            if (gameLevel.pac().powerTimer().isRunning()) {
                ui.theSound().loop(SoundID.PAC_MAN_POWER);
            }
            gameLevel3D.livesCounter3D().map(LivesCounter3D::lookingAroundAnimation).ifPresent(ManagedAnimation::playFromStart);
        }
        gameLevel3D.levelCounter3D().ifPresent(levelCounter3D -> levelCounter3D.update(ui, gameContext().theGame().theHUD().theLevelCounter()));
        updateScores();
        setActionBindings();
        fadeInSubScene();
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        gameContext().theGameLevel().bonus().ifPresent(bonus -> {
            gameLevel3D.updateBonus3D(bonus);
            ui.theSound().loop(SoundID.BONUS_ACTIVE);
        });
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        gameContext().theGameLevel().bonus().ifPresent(bonus -> {
            gameLevel3D.bonus3D().ifPresent(Bonus3D::showEaten);
            ui.theSound().stop(SoundID.BONUS_ACTIVE);
            ui.theSound().play(SoundID.BONUS_EATEN);
        });
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        gameContext().theGameLevel().bonus().ifPresent(bonus -> {
            gameLevel3D.bonus3D().ifPresent(Bonus3D::expire);
            ui.theSound().stop(SoundID.BONUS_ACTIVE);
        });
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        ui.theSound().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onGameContinued(GameEvent e) {
        if (gameLevel3D != null && !gameLevel3D.isDestroyed()) {
            if (gameContext().theGameLevel().house().isEmpty()) {
                Logger.error("No house found in this game level! WTF?");
            } else {
                Vector2f messageCenter = gameContext().theGameLevel().house().get().centerPositionUnderHouse();
                gameLevel3D.showAnimatedMessage("READY!", 2.5f, messageCenter.x(), messageCenter.y());
            }
        }
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = gameContext().theGameLevel().isDemoLevel()
            || gameContext().theGameState() == TESTING_LEVELS_SHORT
            || gameContext().theGameState() == TESTING_LEVELS_MEDIUM;
        if (!silent) {
            ui.theSound().play(SoundID.GAME_READY);
        }
    }

    @Override
    public void onPacFoundFood(GameEvent event) {
        if (event.tile() == null) {
            // When cheat "eat all pellets" has been used, no tile is present in the event.
            gameLevel3D.pellets3D().forEach(Pellet3D::onEaten);
        } else {
            Energizer3D energizer3D = gameLevel3D.energizers3D().filter(e3D -> event.tile().equals(e3D.tile())).findFirst().orElse(null);
            if (energizer3D != null) {
                energizer3D.onEaten();
            } else {
                gameLevel3D.pellets3D()
                    .filter(pellet3D -> event.tile().equals(pellet3D.tile()))
                    .findFirst()
                    .ifPresent(Pellet3D::onEaten);
            }
            ui.theSound().loop(SoundID.PAC_MAN_MUNCHING);
        }
    }

    @Override
    public void onPacGetsPower(GameEvent event) {
        gameLevel3D.pac3D().setMovementPowerMode(true);
        gameLevel3D.wallColorFlashingAnimation().playFromStart();
        ui.theSound().stopSiren();
        ui.theSound().loop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onPacLostPower(GameEvent event) {
        gameLevel3D.pac3D().setMovementPowerMode(false);
        gameLevel3D.wallColorFlashingAnimation().stop();
        ui.theSound().stop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onSpecialScoreReached(GameEvent e) {
        ui.theSound().play(SoundID.EXTRA_LIFE);
    }

    @Override
    public void onStopAllSounds(GameEvent event) {
        ui.theSound().stopAll();
    }

    @Override
    public void onUnspecifiedChange(GameEvent event) {
        // TODO: remove (this is only used by game state GameState.TESTING_CUT_SCENES)
        ui.updateGameScene(true);
    }

    protected GameLevel3D createGameLevel3D(Group root) {
        return new GameLevel3D(ui, root);
    }

    protected void replaceGameLevel3D() {
        if (gameLevel3D != null) {
            Logger.info("Replacing game level 3D");
            gameLevel3DRoot.getChildren().clear();
            gameLevel3D.dispose();
            Logger.info("Destroyed old game level 3D");
        } else {
            Logger.info("Creating game level 3D");
        }
        gameLevel3D = createGameLevel3D(gameLevel3DRoot);
        Logger.info("Created new game level 3D");

        gameLevel3D.pac3D().init();
        gameLevel3D.ghosts3D().forEach(ghost3D -> ghost3D.init(gameContext().theGameLevel()));
        Logger.info("Initialized actors of game level 3D");

        scores3D.translateXProperty().bind(gameLevel3DRoot.translateXProperty().add(TS));
        scores3D.translateYProperty().bind(gameLevel3DRoot.translateYProperty().subtract(4.5 * TS));
        scores3D.translateZProperty().bind(gameLevel3DRoot.translateZProperty().subtract(4.5 * TS));
        Logger.info("Positioned scores 3D");
    }

    protected void updateCamera() {
        PerspectiveID id = perspectiveIDProperty.get();
        if (id != null && perspectiveMap.containsKey(id)) {
            perspectiveMap.get(id).update(camera, gameContext());
        }
        else {
            Logger.error("No perspective with ID '{}' exists", id);
        }
    }

    protected void updateSound() {
        if (gameContext().optGameLevel().isEmpty()) return;

        if (gameContext().theGameLevel().isDemoLevel()) {
            ui.theSound().setEnabled(false);
            return;
        }

        ui.theSound().setEnabled(true);

        Pac pac = gameContext().theGameLevel().pac();

        // Play siren if Pac has no power and is chased by the ghosts
        if (gameContext().theGameState() == GameState.HUNTING && !pac.powerTimer().isRunning()) {
            //TODO clarify which siren plays when
            int sirenNumber = 1 + gameContext().theGame().huntingTimer().phaseIndex() / 2;
            SoundID sirenID = switch (sirenNumber) {
                case 1 -> SoundID.SIREN_1;
                case 2 -> SoundID.SIREN_2;
                case 3 -> SoundID.SIREN_3;
                case 4 -> SoundID.SIREN_4;
                default -> throw new IllegalArgumentException("Illegal siren number " + sirenNumber);
            };
            ui.theSound().playSiren(sirenID, 1.0);
        }

        // TODO Still not sure how to do this right
        if (pac.starvingTicks() > 10) {
            ui.theSound().pause(SoundID.PAC_MAN_MUNCHING);
        }

        boolean isGhostReturningHome = gameContext().theGameLevel().pac().isAlive()
            && gameContext().theGameLevel().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).findAny().isPresent();
        if (isGhostReturningHome) {
            ui.theSound().loop(SoundID.GHOST_RETURNS);
        } else {
            ui.theSound().stop(SoundID.GHOST_RETURNS);
        }
    }

    protected void updateScores() {
        final Score score = gameContext().theGame().score(), highScore = gameContext().theGame().highScore();
        if (score.isEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else { // disabled, show text "GAME OVER"
            Color color = ui.theConfiguration().getAssetNS("color.game_over_message");
            scores3D.showTextForScore(ui.theAssets().text("score.game_over"), color);
        }
        // Always show high score
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
    }

    protected void showLevelTestMessage() {
        WorldMap worldMap = gameContext().theGameLevel().worldMap();
        int levelNumber = gameContext().theGameLevel().number();
        double x = worldMap.numCols() * HTS;
        double y = (worldMap.numRows() - 2) * TS;
        gameLevel3D.showAnimatedMessage("LEVEL %d (TEST)".formatted(levelNumber), 5, x, y);
    }

    protected void fadeInSubScene() {
        new Transition() {
            {
                setCycleDuration(Duration.seconds(4));
                setInterpolator(Interpolator.EASE_OUT);
            }
            @Override
            protected void interpolate(double t) {
                subScene.setFill(SUB_SCENE_FILL_DARK.interpolate(SUB_SCENE_FILL_BRIGHT, t));
            }
        }.play();
    }
}