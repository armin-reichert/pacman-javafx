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
import de.amr.pacmanfx.model.actors.MovingBonus;
import de.amr.pacmanfx.ui.ActionBindingMap;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.*;
import de.amr.pacmanfx.uilib.widgets.CoordinateSystem;
import javafx.animation.Interpolator;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.isOneOf;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS_MEDIUM;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS_SHORT;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static de.amr.pacmanfx.ui.GameUI.GLOBAL_ACTION_BINDINGS;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
import static de.amr.pacmanfx.uilib.Ufx.*;
import static java.util.Objects.requireNonNull;

/**
 * 3D play scene.
 *
 * <p>Provides different camera perspectives that can be stepped through using key combinations
 * <code>Alt+LEFT</code> and <code>Alt+RIGHT</code> (Did he really said Alt-Right?).
 */
public class PlayScene3D implements GameScene {

    private static final Color SUBSCENE_FILL_DARK = Color.BLACK;
    private static final Color SUBSCENE_FILL_BRIGHT = Color.TRANSPARENT;

    protected final GameUI ui;
    protected final SubScene subScene;
    protected final PerspectiveCamera camera = new PerspectiveCamera(true);
    protected final PerspectiveManager perspectiveManager;
    protected final ActionBindingMap actionBindings;

    protected final Group level3DPlaceHolder = new Group();
    protected Scores3D scores3D;
    protected GameLevel3D gameLevel3D;

    public PlayScene3D(GameUI ui) {
        this.ui = requireNonNull(ui);
        this.actionBindings = new ActionBindingMap(ui.theKeyboard());

        scores3D = new Scores3D(
                ui.theAssets().text("score.score"),
                ui.theAssets().text("score.high_score"),
                ui.theAssets().arcadeFont(TS)
        );
        scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
        scores3D.rotateProperty().bind(camera.rotateProperty());

        var coordinateSystem = new CoordinateSystem();
        coordinateSystem.visibleProperty().bind(ui.property3DAxesVisible());

        var root = new Group(level3DPlaceHolder, scores3D, coordinateSystem);

        // initial size is irrelevant because size gets bound to parent scene size anyway
        subScene = new SubScene(root, 88, 88, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(SUBSCENE_FILL_DARK);

        perspectiveManager = new PerspectiveManager(subScene);
    }

    @Override
    public void destroy() {
        actionBindings.removeFromKeyboard();
        perspectiveManager.perspectiveIDProperty().unbind();
        if (gameLevel3D != null) {
            gameLevel3D.destroy();
            gameLevel3D = null;
            Logger.info("GameLevel3D has been destroyed");
        }
    }

    @Override
    public GameUI theUI() {
        return ui;
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
        miAutopilot.selectedProperty().bindBidirectional(ui.theGameContext().propertyUsingAutopilot());

        var miImmunity = new CheckMenuItem(ui.theAssets().text("immunity"));
        miImmunity.selectedProperty().bindBidirectional(ui.theGameContext().propertyImmunity());

        var miMuted = new CheckMenuItem(ui.theAssets().text("muted"));
        miMuted.selectedProperty().bindBidirectional(ui.propertyMuted());

        var miQuit = new MenuItem(ui.theAssets().text("quit"));
        miQuit.setOnAction(e -> ACTION_QUIT_GAME_SCENE.executeIfEnabled(ui));

        var items = new ArrayList<MenuItem>();
        items.add(menuTitleItem(ui.theAssets().text("scene_display")));
        items.add(miUse2DScene);
        items.add(miToggleMiniView);
        items.add(menuTitleItem(ui.theAssets().text("select_perspective")));
        items.addAll(createPerspectiveRadioItems(menu));
        items.add(menuTitleItem(ui.theAssets().text("pacman")));
        items.add(miAutopilot);
        items.add(miImmunity);
        items.add(new SeparatorMenuItem());
        items.add(miMuted);
        items.add(miQuit);

        return items;
    }

    private List<RadioMenuItem> createPerspectiveRadioItems(ContextMenu menu) {
        var items = new ArrayList<RadioMenuItem>();
        for (Perspective.ID id : Perspective.ID.values()) {
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
        ObservableValue<? extends Perspective.ID> property,
        Perspective.ID oldPerspectiveID,
        Perspective.ID newPerspectiveID) {
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
     * Overridden by Tengen play scene 3D to use keys corresponding to "Joypad" buttons
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
        perspectiveManager.perspectiveIDProperty().bind(ui.property3DPerspective());
    }

    @Override
    public final void end() {
        ui.theSound().stopAll();
        destroy();
    }

    @Override
    public final void update() {
        if (gameContext().optGameLevel().isEmpty()) {
            // Scene gets already update 2 ticks before level has been created!
            Logger.info("Tick #{}: Game level not yet created, update ignored", ui.theGameClock().tickCount());
            return;
        }
        if (gameLevel3D == null) {
            Logger.warn("Tick #{}: 3D game level not yet created", ui.theGameClock().tickCount());
            return;
        }
        if (gameLevel3D.isDestroyed()) {
            Logger.error("Tick #{}: 3D game level is being destroyed, terminating app", ui.theGameClock().tickCount());
            ui.terminateApp();
        }
        gameLevel3D.tick(gameContext());
        updateScores(gameContext().theGameLevel());
        if (gameContext().theGameLevel().isDemoLevel()) {
            ui.theSound().setEnabled(false);
        } else {
            ui.theSound().setEnabled(true);
            updateSound(gameContext().theGameLevel());
        }
        perspectiveManager.updatePerspective(gameContext().theGameLevel());
    }

    @Override
    public Optional<SubScene> optSubScene() {
        return Optional.of(subScene);
    }

    @Override
    public void onEnterGameState(GameState state) {
        requireNonNull(state);
        Logger.trace("Entering game state {}", state);
        switch (state) {
            case HUNTING -> {
                gameLevel3D.pac3D().init();
                gameLevel3D.ghosts3D().forEach(ghost3D -> ghost3D.init(gameContext().theGameLevel()));
                gameLevel3D.energizers3D().forEach(energizer3D -> energizer3D.pumpingAnimation().playFromStart());
                gameLevel3D.livesCounter3D().lookingAroundAnimation().playFromStart();
            }
            case PACMAN_DYING -> {
                state.timer().resetIndefiniteTime(); // expires when animation ends
                ui.theSound().stopAll();
                // do one last update before dying animation starts
                gameLevel3D.pac3D().update(gameContext().theGameLevel());
                gameLevel3D.livesCounter3D().lookingAroundAnimation().stop();
                gameLevel3D.livesCounter3D().lookingAroundAnimation().invalidate();
                gameLevel3D.ghosts3D().forEach(MutatingGhost3D::stopAllAnimations);
                gameLevel3D.bonus3D().ifPresent(Bonus3D::expire);
                var animation = new SequentialTransition(
                    pauseSec(2),
                    doNow(() -> ui.theSound().play(SoundID.PAC_MAN_DEATH)),
                    gameLevel3D.pac3D().dyingAnimation().getOrCreateAnimation(),
                    pauseSec(1)
                );
                // Note: adding this inside the animation as last action does not work!
                animation.setOnFinished(e -> gameContext().theGameController().letCurrentGameStateExpire());
                animation.play();
            }
            case GHOST_DYING ->
                gameContext().theSimulationStep().killedGhosts.forEach(killedGhost -> {
                    byte personality = killedGhost.personality();
                    int killedIndex = gameContext().theGameLevel().victims().indexOf(killedGhost);
                    Image pointsImage = ui.theConfiguration().killedGhostPointsImage(killedGhost, killedIndex);
                    gameLevel3D.ghost3D(personality).setNumberImage(pointsImage);
                });
            case LEVEL_COMPLETE -> {
                state.timer().resetIndefiniteTime(); // expires when animation ends
                gameLevel3D.onLevelComplete();
                boolean cutSceneFollows = gameContext().theGame().cutSceneNumber(gameContext().theGameLevel().number()).isPresent();
                ManagedAnimation levelCompletedAnimation = cutSceneFollows
                    ? gameLevel3D.levelCompletedAnimationBeforeCutScene()
                    : gameLevel3D.levelCompletedAnimation();

                var animation = new SequentialTransition(
                    pauseSec(2, () -> {
                        perspectiveManager.perspectiveIDProperty().unbind();
                        perspectiveManager.setPerspective(Perspective.ID.TOTAL);
                    }),
                    levelCompletedAnimation.getOrCreateAnimation(),
                    pauseSec(1)
                );
                animation.setOnFinished(e -> {
                    perspectiveManager.perspectiveIDProperty().bind(ui.property3DPerspective());
                    gameContext().theGameController().letCurrentGameStateExpire();
                });
                animation.play();
            }
            case LEVEL_TRANSITION -> {
                state.timer().resetIndefiniteTime();
                replaceGameLevel3D();
                perspectiveManager.initPerspective();
                state.timer().expire();
            }
            case GAME_OVER -> {
                state.timer().restartSeconds(3);
                gameLevel3D.energizers3D().forEach(energizer3D -> energizer3D.shape3D().setVisible(false));
                gameLevel3D.bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
                ui.theSound().stopAll();
                ui.theSound().play(SoundID.GAME_OVER);
                boolean inOneOf4Cases = randomInt(0, 1000) < 250;
                if (!gameContext().theGameLevel().isDemoLevel() && inOneOf4Cases) {
                    ui.showFlashMessageSec(2.5, ui.theAssets().localizedGameOverMessage());
                }
            }
            case TESTING_LEVELS_SHORT, TESTING_LEVELS_MEDIUM -> {
                replaceGameLevel3D();
                showLevelTestMessage(gameContext().theGameLevel().number());
                ui.property3DPerspective().set(Perspective.ID.TOTAL);
            }
        }
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        if (theGameContext().optGameLevel().isEmpty()) {
            Logger.error("No game level exists on level start! WTF?");
            return;
        }
        final GameLevel gameLevel = gameContext().theGameLevel();
        if (gameLevel3D == null) {
            replaceGameLevel3D();
        }
        switch (gameContext().theGameState()) {
            case STARTING_GAME -> {
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
                gameLevel3D.livesCounter3D().lookingAroundAnimation().playFromStart();
                gameLevel3D.energizers3D().forEach(energizer3D -> energizer3D.pumpingAnimation().playFromStart());
                showLevelTestMessage(gameLevel.number());
            }
            default -> Logger.error("Unexpected game state '{}' on level start", gameContext().theGameState());
        }
        perspectiveManager.initPerspective();
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
        gameLevel3D.pac3D().update(gameLevel);
        gameLevel3D.pellets3D().forEach(pellet -> pellet.shape3D().setVisible(!gameLevel.tileContainsEatenFood(pellet.tile())));
        gameLevel3D.energizers3D().forEach(energizer -> energizer.shape3D().setVisible(!gameLevel.tileContainsEatenFood(energizer.tile())));
        if (isOneOf(gameContext().theGameState(), GameState.HUNTING, GameState.GHOST_DYING)) { //TODO check this
            gameLevel3D.energizers3D()
                .filter(energizer3D -> energizer3D.shape3D().isVisible())
                .forEach(energizer3D -> energizer3D.pumpingAnimation().playFromStart());
        }

        if (gameContext().theGameState() == GameState.HUNTING) {
            if (gameLevel.pac().powerTimer().isRunning()) {
                ui.theSound().loop(SoundID.PAC_MAN_POWER);
            }
            gameLevel3D.livesCounter3D().lookingAroundAnimation().playFromStart();
        }
        updateScores(gameLevel);
        setActionBindings();
        fadeInSubScene();
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        gameContext().theGameLevel().bonus().ifPresent(bonus -> {
            gameLevel3D.updateBonus3D(bonus);
            if (bonus instanceof MovingBonus) {
                ui.theSound().loop(SoundID.BONUS_ACTIVE);
            }
        });
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        gameContext().theGameLevel().bonus().ifPresent(bonus -> {
            gameLevel3D.bonus3D().ifPresent(Bonus3D::showEaten);
            if (bonus instanceof MovingBonus) {
                ui.theSound().stop(SoundID.BONUS_ACTIVE);
            }
            ui.theSound().play(SoundID.BONUS_EATEN);
        });
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        gameContext().theGameLevel().bonus().ifPresent(bonus -> {
            gameLevel3D.bonus3D().ifPresent(Bonus3D::expire);
            if (bonus instanceof MovingBonus) {
                ui.theSound().stop(SoundID.BONUS_ACTIVE);
            }
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

    protected void replaceGameLevel3D() {
        if (gameLevel3D != null) {
            gameLevel3D.destroy();
        }
        gameLevel3D = new GameLevel3D(
            ui,
            ui.theConfiguration().colorScheme(gameContext().theGameLevel().worldMap())
        );
        level3DPlaceHolder.getChildren().setAll(gameLevel3D);

        gameLevel3D.pac3D().init();
        gameLevel3D.ghosts3D().forEach(ghost3D -> ghost3D.init(gameContext().theGameLevel()));
        gameLevel3D.levelCounter3D().spinningAnimation().playFromStart();

        scores3D.translateXProperty().bind(gameLevel3D.translateXProperty().add(TS));
        scores3D.translateYProperty().bind(gameLevel3D.translateYProperty().subtract(4.5 * TS));
        scores3D.translateZProperty().bind(gameLevel3D.translateZProperty().subtract(4.5 * TS));
    }

    protected void updateSound(GameLevel gameLevel) {
        if (gameContext().theGameState() == GameState.HUNTING && !gameLevel.pac().powerTimer().isRunning()) {
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
        if (gameLevel.pac().starvingTicks() > 10) { // TODO not sure how to do this right
            ui.theSound().pause(SoundID.PAC_MAN_MUNCHING);
        }
        boolean ghostsReturning = gameLevel.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (gameLevel.pac().isAlive() && ghostsReturning) {
            ui.theSound().loop(SoundID.GHOST_RETURNS);
        } else {
            ui.theSound().stop(SoundID.GHOST_RETURNS);
        }
    }

    protected void updateScores(GameLevel gameLevel) {
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

    protected void showLevelTestMessage(int levelNumber) {
        WorldMap worldMap = gameContext().theGameLevel().worldMap();
        double x = worldMap.numCols() * HTS;
        double y = (worldMap.numRows() - 2) * TS;
        gameLevel3D.showAnimatedMessage("LEVEL %d (TEST)".formatted(levelNumber), 5, x, y);
    }

    private void fadeInSubScene() {
        new Transition() {
            {
                setCycleDuration(Duration.seconds(4));
                setInterpolator(Interpolator.EASE_IN);
            }
            @Override
            protected void interpolate(double t) {
                subScene.setFill(SUBSCENE_FILL_DARK.interpolate(SUBSCENE_FILL_BRIGHT, t));
            }
        }.play();
    }
}