/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.MovingBonus;
import de.amr.pacmanfx.ui.GameAction;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.CameraControlledView;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.*;
import de.amr.pacmanfx.uilib.widgets.CoordinateSystem;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.isOneOf;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS_MEDIUM;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS_SHORT;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static de.amr.pacmanfx.ui.PacManGames.*;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
import static de.amr.pacmanfx.ui.PacManGames_UI.*;
import static de.amr.pacmanfx.uilib.Ufx.*;
import static java.util.Objects.requireNonNull;

/**
 * 3D play scene.
 *
 * <p>Provides different camera perspectives that can be stepped through using key combinations
 * <code>Alt+LEFT</code> and <code>Alt+RIGHT</code> (Did he really said Alt-Right?).
 */
public class PlayScene3D implements GameScene, CameraControlledView {

    protected final SubScene subScene3D;
    protected final PerspectiveCamera camera = new PerspectiveCamera(true);
    protected final PerspectiveManager perspectiveManager;
    protected final Map<KeyCombination, GameAction> actionBindingMap = new HashMap<>();
    protected final Scores3D scores3D;

    protected GameLevel3D gameLevel3D;

    public PlayScene3D() {
        scores3D = new Scores3D(
            theAssets().text("score.score"),
            theAssets().text("score.high_score"),
            theAssets().arcadeFont(TS)
        );
        scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
        scores3D.rotateProperty().bind(camera.rotateProperty());

        var coordinateSystem = new CoordinateSystem();
        coordinateSystem.visibleProperty().bind(PY_3D_AXES_VISIBLE);

        // last child is placeholder for level 3D
        var root = new Group(scores3D, coordinateSystem, new Group());

        // initial size is irrelevant because size gets bound to parent scene size anyway
        subScene3D = new SubScene(root, 88, 88, true, SceneAntialiasing.BALANCED);
        subScene3D.setCamera(camera);
        subScene3D.setFill(Color.BLACK); // gets transparent when level is available

        perspectiveManager = new PerspectiveManager(subScene3D);
    }

    public Optional<GameLevel3D> level3D() {
        return Optional.ofNullable(gameLevel3D);
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent menuEvent, ContextMenu menu) {
        var miUse2DScene = new MenuItem(theAssets().text("use_2D_scene"));
        miUse2DScene.setOnAction(e -> GameAction.executeIfEnabled(theUI(), ACTION_TOGGLE_PLAY_SCENE_2D_3D));

        var miToggleMiniView = new CheckMenuItem(theAssets().text("pip"));
        miToggleMiniView.selectedProperty().bindBidirectional(PY_MINI_VIEW_ON);

        var miAutopilot = new CheckMenuItem(theAssets().text("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_USING_AUTOPILOT);

        var miImmunity = new CheckMenuItem(theAssets().text("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);

        var miMuted = new CheckMenuItem(theAssets().text("muted"));
        miMuted.selectedProperty().bindBidirectional(theUI().mutedProperty());

        var miQuit = new MenuItem(theAssets().text("quit"));
        miQuit.setOnAction(e -> GameAction.executeIfEnabled(theUI(), ACTION_QUIT_GAME_SCENE));

        var items = new ArrayList<MenuItem>();
        items.add(menuTitleItem(theAssets().text("scene_display")));
        items.add(miUse2DScene);
        items.add(miToggleMiniView);
        items.add(menuTitleItem(theAssets().text("select_perspective")));
        items.addAll(createPerspectiveRadioItems());
        items.add(menuTitleItem(theAssets().text("pacman")));
        items.add(miAutopilot);
        items.add(miImmunity);
        items.add(new SeparatorMenuItem());
        items.add(miMuted);
        items.add(miQuit);

        return items;
    }

    private List<MenuItem> createPerspectiveRadioItems() {
        List<MenuItem> items = new ArrayList<>();
        var toggleGroup = new ToggleGroup();
        for (var perspectiveID : Perspective.ID.values()) {
            var radioItem = new RadioMenuItem(theAssets().text("perspective_id_" + perspectiveID.name()));
            radioItem.setToggleGroup(toggleGroup);
            radioItem.setOnAction(e -> PY_3D_PERSPECTIVE.set(perspectiveID));
            radioItem.setUserData(perspectiveID);
            if (perspectiveID == PY_3D_PERSPECTIVE.get())  {
                radioItem.setSelected(true);
            }
            items.add(radioItem);
        }
        PY_3D_PERSPECTIVE.addListener((py, ov, newPerspectiveID) -> {
            for (MenuItem item : items) {
                if (item.getUserData() == newPerspectiveID) {
                    toggleGroup.selectToggle((Toggle) item);
                }
            }
        });
        return items;
    }

    protected void setActionBindings() {
        clearActionBindings();
        bindAction(ACTION_PERSPECTIVE_PREVIOUS, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_PERSPECTIVE_NEXT, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_TOGGLE_DRAW_MODE, GLOBAL_ACTION_BINDING_MAP);
        if (optGameLevel().isPresent()) {
            if (theGameLevel().isDemoLevel()) {
                bindAction(ACTION_ARCADE_INSERT_COIN, GLOBAL_ACTION_BINDING_MAP);
            } else {
                setPlayerSteeringActionBindings();
                bindAction(ACTION_CHEAT_EAT_ALL_PELLETS, GLOBAL_ACTION_BINDING_MAP);
                bindAction(ACTION_CHEAT_ADD_LIVES, GLOBAL_ACTION_BINDING_MAP);
                bindAction(ACTION_CHEAT_ENTER_NEXT_LEVEL, GLOBAL_ACTION_BINDING_MAP);
                bindAction(ACTION_CHEAT_KILL_GHOSTS, GLOBAL_ACTION_BINDING_MAP);
            }
        }
        updateActionBindings();
    }

    /**
     * Overridden by Tengen play scene 3D to use keys corresponding to "Joypad" buttons
     */
    protected void setPlayerSteeringActionBindings() {
        bindAction(ACTION_STEER_UP, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_STEER_DOWN, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_STEER_LEFT, GLOBAL_ACTION_BINDING_MAP);
        bindAction(ACTION_STEER_RIGHT, GLOBAL_ACTION_BINDING_MAP);
    }

    @Override
    public Map<KeyCombination, GameAction> actionBindings() {
        return actionBindingMap;
    }

    @Override
    public void init() {
        theGame().hud().showScore(true);
        perspectiveManager.perspectiveIDProperty().bind(PY_3D_PERSPECTIVE);
    }

    @Override
    public final void end() {
        theSound().stopAll();
        clearActionBindings();
        perspectiveManager.perspectiveIDProperty().unbind();
        if (gameLevel3D != null) {
            gameLevel3D.destroy();
            gameLevel3D = null;
            Logger.info("GameLevel3D has been destroyed");
        }
    }

    @Override
    public Keyboard keyboard() { return theKeyboard(); }

    @Override
    public final void update() {
        if (optGameLevel().isEmpty()) {
            // Scene gets already update 2 ticks before level has been created!
            Logger.info("Tick #{}: Game level not yet created, update ignored", theClock().tickCount());
            return;
        }
        if (gameLevel3D == null) {
            Logger.warn("Tick #{}: 3D game level not yet created", theClock().tickCount());
            return;
        }
        if (gameLevel3D.isDestroyed()) {
            Logger.error("Tick #{}: 3D game level is being destroyed, terminating app", theClock().tickCount());
            theUI().terminateApp();
        }
        gameLevel3D.tick();
        updateScores(theGameLevel());
        updateSound(theGameLevel());
        perspectiveManager.updatePerspective(theGameLevel());
    }

    @Override
    public DoubleProperty viewPortWidthProperty() {
        return subScene3D.widthProperty();
    }

    @Override
    public DoubleProperty viewPortHeightProperty() {
        return subScene3D.heightProperty();
    }

    @Override
    public SubScene viewPort() {
        return subScene3D;
    }

    @Override
    public Camera camera() {
        return subScene3D.getCamera();
    }

    @Override
    public void onEnterGameState(GameState state) {
        requireNonNull(state);
        Logger.trace("Entering game state {}", state);
        switch (state) {
            case HUNTING -> {
                gameLevel3D.pac3D().init();
                gameLevel3D.ghosts3D().forEach(ghost3D -> ghost3D.init(theGameLevel()));
                gameLevel3D.energizers3D().forEach(energizer3D -> energizer3D.pumpingAnimation().playFromStart());
                gameLevel3D.livesCounter3D().lookingAroundAnimation().playOrContinue();
            }
            case PACMAN_DYING -> {
                theGameState().timer().resetIndefiniteTime(); // expires when animation ends
                theSound().stopAll();
                // do one last update before dying animation starts
                gameLevel3D.pac3D().update(theGameLevel());
                gameLevel3D.livesCounter3D().lookingAroundAnimation().stop();
                gameLevel3D.ghosts3D().forEach(MutatingGhost3D::stopAllAnimations);
                gameLevel3D.bonus3D().ifPresent(Bonus3D::expire);
                var animation = new SequentialTransition(
                    pauseSec(2),
                    doNow(() -> theSound().play(SoundID.PAC_MAN_DEATH)),
                    gameLevel3D.pac3D().dyingAnimation().getOrCreateAnimation(),
                    pauseSec(1)
                );
                // Note: adding this inside the animation as last action does not work!
                animation.setOnFinished(e -> theGameController().letCurrentGameStateExpire());
                animation.play();
            }
            case GHOST_DYING ->
                theSimulationStep().killedGhosts.forEach(killedGhost -> {
                    byte personality = killedGhost.personality();
                    int killedIndex = theGameLevel().victims().indexOf(killedGhost);
                    Image pointsImage = theUI().configuration().killedGhostPointsImage(killedGhost, killedIndex);
                    gameLevel3D.ghost3D(personality).setNumberImage(pointsImage);
                });
            case LEVEL_COMPLETE -> {
                theGameState().timer().resetIndefiniteTime(); // expires when animation ends
                gameLevel3D.onLevelComplete();
                boolean cutSceneFollows = theGame().cutSceneNumber(theGameLevel().number()).isPresent();
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
                    perspectiveManager.perspectiveIDProperty().bind(PY_3D_PERSPECTIVE);
                    theGameController().letCurrentGameStateExpire();
                });
                animation.play();
            }
            case LEVEL_TRANSITION -> {
                theGameState().timer().resetIndefiniteTime();
                Platform.runLater(() -> {
                    replaceGameLevel3D();
                    gameLevel3D.pac3D().init();
                    perspectiveManager.initPerspective();
                    theGameState().timer().expire();
                });
            }
            case GAME_OVER -> {
                theGameState().timer().restartSeconds(3);
                boolean oneOutOf4Times = randomInt(0, 1000) < 250;
                if (!theGameLevel().isDemoLevel() && oneOutOf4Times) {
                    theUI().showFlashMessageSec(3, theAssets().localizedGameOverMessage());
                }
                theSound().stopAll();
                theSound().play(SoundID.GAME_OVER);
                gameLevel3D.energizers3D().forEach(energizer3D -> energizer3D.shape3D().setVisible(false));
                gameLevel3D.bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
            }
            case TESTING_LEVELS_SHORT, TESTING_LEVELS_MEDIUM -> {
                replaceGameLevel3D();
                gameLevel3D.pac3D().init();
                gameLevel3D.ghosts3D().forEach(ghost3D -> ghost3D.init(theGameLevel()));
                showLevelTestMessage(theGameLevel().number());
                PY_3D_PERSPECTIVE.set(Perspective.ID.TOTAL);
            }
            default -> {}
        }
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        requireNonNull(theGameLevel()); // just to be sure nothing bad happened
        setActionBindings();
        if (gameLevel3D == null) {
            replaceGameLevel3D();
        }
        switch (theGameState()) {
            case STARTING_GAME -> {
                //TODO default position if no house
                Vector2f position = theGameLevel().house().map(House::centerPositionUnderHouse).orElse(Vector2f.ZERO);
                gameLevel3D.showAnimatedMessage("READY!", 2.5f, position.x(), position.y());
                setPlayerSteeringActionBindings();
            }
            case TESTING_LEVELS_SHORT, TESTING_LEVELS_MEDIUM -> {
                replaceGameLevel3D(); //TODO check when to destroy previous level
                gameLevel3D.livesCounter3D().lookingAroundAnimation().playFromStart();
                gameLevel3D.energizers3D().forEach(energizer3D -> energizer3D.pumpingAnimation().playFromStart());
                showLevelTestMessage(theGameLevel().number());
            }
            default -> Logger.error("Unexpected game state '{}' on level start", theGameState());
        }
        subScene3D.setFill(Color.TRANSPARENT);
        perspectiveManager.initPerspective();
    }

    @Override
    public void onSwitch_2D_3D(GameScene scene2D) {
        if (optGameLevel().isEmpty()) {
            return;
        }
        if (gameLevel3D == null) {
            replaceGameLevel3D();
        }
        gameLevel3D.pellets3D().forEach(pellet -> pellet.shape3D().setVisible(!theGameLevel().tileContainsEatenFood(pellet.tile())));
        gameLevel3D.energizers3D().forEach(energizer -> energizer.shape3D().setVisible(!theGameLevel().tileContainsEatenFood(energizer.tile())));
        if (isOneOf(theGameState(), GameState.HUNTING, GameState.GHOST_DYING)) { //TODO check this
            gameLevel3D.energizers3D()
                .filter(energizer3D -> energizer3D.shape3D().isVisible())
                .forEach(energizer3D -> energizer3D.pumpingAnimation().playFromStart());
        }
        theGameLevel().pac().show();
        theGameLevel().ghosts().forEach(Ghost::show);
        gameLevel3D.pac3D().init();
        gameLevel3D.pac3D().update(theGameLevel());

        if (theGameState() == GameState.HUNTING) {
            if (theGameLevel().pac().powerTimer().isRunning()) {
                theSound().loop(SoundID.PAC_MAN_POWER);
            }
            gameLevel3D.livesCounter3D().lookingAroundAnimation().playFromStart();
        }

        subScene3D.setFill(Color.TRANSPARENT);
        updateScores(theGameLevel());
        setActionBindings();
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        theGameLevel().bonus().ifPresent(bonus -> {
            gameLevel3D.updateBonus3D(bonus);
            if (bonus instanceof MovingBonus) {
                theSound().loop(SoundID.BONUS_ACTIVE);
            }
        });
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        theGameLevel().bonus().ifPresent(bonus -> {
            gameLevel3D.bonus3D().ifPresent(Bonus3D::showEaten);
            if (bonus instanceof MovingBonus) {
                theSound().stop(SoundID.BONUS_ACTIVE);
            }
            theSound().play(SoundID.BONUS_EATEN);
        });
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        theGameLevel().bonus().ifPresent(bonus -> {
            gameLevel3D.bonus3D().ifPresent(Bonus3D::expire);
            if (bonus instanceof MovingBonus) {
                theSound().stop(SoundID.BONUS_ACTIVE);
            }
        });
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        theSound().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onGameContinued(GameEvent e) {
        if (gameLevel3D != null && !gameLevel3D.isDestroyed()) {
            Vector2f position = theGameLevel().house().map(House::centerPositionUnderHouse).orElse(Vector2f.ZERO);
            gameLevel3D.showAnimatedMessage("READY!", 0.5f, position.x(), position.y());
        }
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = theGameLevel().isDemoLevel()
                || theGameState() == TESTING_LEVELS_SHORT || theGameState() == TESTING_LEVELS_MEDIUM;
        if (!silent) {
            theSound().play(SoundID.GAME_READY);
        }
    }

    @Override
    public void onPacFoundFood(GameEvent event) {
        if (event.tile() == null) {
            // When cheat "eat all pellets" has been used, no tile is present in the event.
            gameLevel3D.pellets3D().forEach(Pellet3D::onEaten);
        } else {
            Energizer3D energizer3D = gameLevel3D.energizers3D()
                .filter(e3D -> event.tile().equals(e3D.tile()))
                .findFirst().orElse(null);
            if (energizer3D != null) {
                energizer3D.onEaten();
            } else {
                gameLevel3D.pellets3D()
                    .filter(pellet3D -> event.tile().equals(pellet3D.tile()))
                    .findFirst()
                    .ifPresent(Pellet3D::onEaten);
            }
            theSound().loop(SoundID.PAC_MAN_MUNCHING);
        }
    }

    @Override
    public void onPacGetsPower(GameEvent event) {
        gameLevel3D.pac3D().setMovementPowerMode(true);
        gameLevel3D.wallColorFlashingAnimation().playFromStart();
        theSound().stopSiren();
        theSound().loop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onPacLostPower(GameEvent event) {
        gameLevel3D.pac3D().setMovementPowerMode(false);
        gameLevel3D.wallColorFlashingAnimation().stop();
        theSound().stop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onSpecialScoreReached(GameEvent e) {
        theSound().play(SoundID.EXTRA_LIFE);
    }

    @Override
    public void onStopAllSounds(GameEvent event) {
        theSound().stopAll();
    }

    @Override
    public void onUnspecifiedChange(GameEvent event) {
        // TODO: remove (this is only used by game state GameState.TESTING_CUT_SCENES)
        theUI().updateGameScene(true);
    }

    protected void replaceGameLevel3D() {
        if (gameLevel3D != null) {
            gameLevel3D.destroy();
        }
        gameLevel3D = new GameLevel3D(
                theUI().model3DRepository(),
                theGameLevel(),
                theUI().configuration().worldMapColorScheme(theGameLevel().worldMap())
        );
        gameLevel3D.pac3D().init();
        gameLevel3D.ghosts3D().forEach(ghost3D -> ghost3D.init(theGameLevel()));
        Group root = (Group) subScene3D.getRoot();
        root.getChildren().set(root.getChildren().size() - 1, gameLevel3D);

        scores3D.translateXProperty().bind(gameLevel3D.translateXProperty().add(TS));
        scores3D.translateYProperty().bind(gameLevel3D.translateYProperty().subtract(4.5 * TS));
        scores3D.translateZProperty().bind(gameLevel3D.translateZProperty().subtract(4.5 * TS));
    }

    protected void updateSound(GameLevel gameLevel) {
        if (gameLevel.isDemoLevel()) {
            theSound().setEnabled(false);
            return; // demo level is silent
        }
        theSound().setEnabled(true);
        if (theGameState() == GameState.HUNTING && !gameLevel.pac().powerTimer().isRunning()) {
            int sirenNumber = 1 + theGame().huntingTimer().phaseIndex() / 2;
            SoundID sirenID = switch (sirenNumber) {
                case 1 -> SoundID.SIREN_1;
                case 2 -> SoundID.SIREN_2;
                case 3 -> SoundID.SIREN_3;
                case 4 -> SoundID.SIREN_4;
                default -> throw new IllegalArgumentException("Illegal siren number " + sirenNumber);
            };
            theSound().playSiren(sirenID, 1.0);
        }
        if (gameLevel.pac().starvingTicks() > 10) { // TODO not sure how to do this right
            theSound().pause(SoundID.PAC_MAN_MUNCHING);
        }
        boolean ghostsReturning = gameLevel.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (gameLevel.pac().isAlive() && ghostsReturning) {
            theSound().loop(SoundID.GHOST_RETURNS);
        } else {
            theSound().stop(SoundID.GHOST_RETURNS);
        }
    }

    protected void updateScores(GameLevel gameLevel) {
        final Score score = theGame().score(), highScore = theGame().highScore();
        if (score.isEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else { // disabled, show text "GAME OVER"
            String ans = theUI().configuration().assetNamespace();
            Color color = theAssets().color(ans + ".color.game_over_message");
            scores3D.showTextAsScore(theAssets().text("score.game_over"), color);
        }
        // Always show high score
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
    }

    protected void showLevelTestMessage(int levelNumber) {
        WorldMap worldMap = theGameLevel().worldMap();
        double x = worldMap.numCols() * HTS;
        double y = (worldMap.numRows() - 2) * TS;
        gameLevel3D.showAnimatedMessage("LEVEL %d (TEST)".formatted(levelNumber), 5, x, y);
    }
}