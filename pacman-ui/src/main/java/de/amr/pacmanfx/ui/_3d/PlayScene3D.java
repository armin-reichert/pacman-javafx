/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
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
import de.amr.pacmanfx.uilib.CameraControlledView;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.SequentialTransition;
import javafx.beans.property.DoubleProperty;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    protected final AnimationManager animationManager = new AnimationManager();
    protected final Group root = new Group();
    protected final SubScene subScene3D;
    protected final PerspectiveCamera camera = new PerspectiveCamera(true);
    protected final PerspectiveManager perspectiveManager;
    protected final Map<KeyCombination, GameAction> actionBindingMap = new HashMap<>();
    protected final Scores3D scores3D;

    protected GameLevel3D level3D;

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
        root.getChildren().addAll(scores3D, coordinateSystem, new Group());

        // initial size is irrelevant because size gets bound to parent scene size anyway
        subScene3D = new SubScene(root, 88, 88, true, SceneAntialiasing.BALANCED);
        subScene3D.setCamera(camera);
        subScene3D.setFill(Color.BLACK); // gets transparent when level is available

        perspectiveManager = new PerspectiveManager(subScene3D);
    }

    public AnimationManager animationManager() {
        return animationManager;
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent e) {
        List<MenuItem> items = new ArrayList<>();

        items.add(contextMenuTitleItem(theAssets().text("scene_display")));

        var item = new MenuItem(theAssets().text("use_2D_scene"));
        item.setOnAction(ae -> GameAction.executeIfEnabled(theUI(), ACTION_TOGGLE_PLAY_SCENE_2D_3D));
        items.add(item);

        // Toggle picture-in-picture display
        var miPiP = new CheckMenuItem(theAssets().text("pip"));
        miPiP.selectedProperty().bindBidirectional(PY_PIP_ON);
        items.add(miPiP);

        items.add(contextMenuTitleItem(theAssets().text("select_perspective")));

        // Camera perspective radio buttons
        var radioButtonGroup = new ToggleGroup();
        for (var perspective : PerspectiveID.values()) {
            var miPerspective = new RadioMenuItem(theAssets().text("perspective_id_" + perspective.name()));
            miPerspective.setToggleGroup(radioButtonGroup);
            miPerspective.setUserData(perspective);
            if (perspective == PY_3D_PERSPECTIVE.get())  { // == allowed for enum values
                miPerspective.setSelected(true);
            }
            items.add(miPerspective);
        }
        // keep radio button group in sync with global property value
        radioButtonGroup.selectedToggleProperty().addListener((py, ov, radioButton) -> {
            if (radioButton != null) {
                PY_3D_PERSPECTIVE.set((PerspectiveID) radioButton.getUserData());
            }
        });
        PY_3D_PERSPECTIVE.addListener((py, ov, name) -> {
            for (Toggle toggle : radioButtonGroup.getToggles()) {
                if (toggle.getUserData() == name) { // == allowed for enum values
                    radioButtonGroup.selectToggle(toggle);
                }
            }
        });

        // Common items
        items.add(contextMenuTitleItem(theAssets().text("pacman")));

        var miAutopilot = new CheckMenuItem(theAssets().text("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_USING_AUTOPILOT);
        items.add(miAutopilot);

        var miImmunity = new CheckMenuItem(theAssets().text("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        items.add(miImmunity);

        items.add(new SeparatorMenuItem());

        var miMuted = new CheckMenuItem(theAssets().text("muted"));
        miMuted.selectedProperty().bindBidirectional(theSound().mutedProperty());
        items.add(miMuted);

        var miQuit = new MenuItem(theAssets().text("quit"));
        miQuit.setOnAction(ae -> GameAction.executeIfEnabled(theUI(), ACTION_QUIT_GAME_SCENE));
        items.add(miQuit);

        return items;
    }

    protected void bindActions() {
        clearActionBindings();
        bindAction(ACTION_PERSPECTIVE_PREVIOUS, GLOBAL_ACTION_BINDINGS);
        bindAction(ACTION_PERSPECTIVE_NEXT, GLOBAL_ACTION_BINDINGS);
        bindAction(ACTION_TOGGLE_DRAW_MODE, GLOBAL_ACTION_BINDINGS);
        if (optGameLevel().isPresent()) {
            if (theGameLevel().isDemoLevel()) {
                bindAction(ACTION_ARCADE_INSERT_COIN, GLOBAL_ACTION_BINDINGS);
            } else {
                bindPlayerSteeringActions();
                bindAction(ACTION_CHEAT_EAT_ALL_PELLETS, GLOBAL_ACTION_BINDINGS);
                bindAction(ACTION_CHEAT_ADD_LIVES, GLOBAL_ACTION_BINDINGS);
                bindAction(ACTION_CHEAT_ENTER_NEXT_LEVEL, GLOBAL_ACTION_BINDINGS);
                bindAction(ACTION_CHEAT_KILL_GHOSTS, GLOBAL_ACTION_BINDINGS);
            }
        }
        updateActionBindings();
    }

    protected void bindPlayerSteeringActions() {
        bindAction(ACTION_STEER_UP, GLOBAL_ACTION_BINDINGS);
        bindAction(ACTION_STEER_DOWN, GLOBAL_ACTION_BINDINGS);
        bindAction(ACTION_STEER_LEFT, GLOBAL_ACTION_BINDINGS);
        bindAction(ACTION_STEER_RIGHT, GLOBAL_ACTION_BINDINGS);
        updateActionBindings();
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
        animationManager.stopAllAnimations();
        animationManager.clearAnimations();
        perspectiveManager.perspectiveIDProperty().unbind();
        level3D = null;
    }

    @Override
    public Keyboard keyboard() { return theKeyboard(); }

    @Override
    public final void update() {
        if (optGameLevel().isEmpty()) {
            // Scene is already visible 2 ticks before level has been created
            Logger.warn("Tick #{}: Game level not yet existing", theClock().tickCount());
            return;
        }
        if (level3D == null) {
            Logger.warn("Tick #{}: 3D game level not yet existing", theClock().tickCount());
            return;
        }
        level3D.update(theGameLevel());
        updateScores(theGameLevel());
        updateSound(theGameLevel());
        perspectiveManager.updatePerspective(theGameLevel());
    }

    @Override
    public Map<KeyCombination, GameAction> actionBindings() {
        return actionBindingMap;
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
                level3D.pac3D().init();
                level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(theGameLevel()));
                level3D.energizers3D().forEach(energizer3D -> energizer3D.pumpingAnimation().play(ManagedAnimation.FROM_START));
                level3D.livesCounter3D().lookingAroundAnimation().play(ManagedAnimation.CONTINUE);
            }
            case PACMAN_DYING -> {
                theGameState().timer().resetIndefiniteTime(); // expires when animation ends
                theSound().stopAll();
                // do one last update before dying animation starts
                level3D.pac3D().update(theGameLevel());
                level3D.livesCounter3D().lookingAroundAnimation().stop();
                level3D.ghosts3D().forEach(MutatingGhost3D::stopAllAnimations);
                var animation = new SequentialTransition(
                    pauseSec(2),
                    now(theSound()::playPacDeathSound),
                    level3D.pac3D().dyingAnimation().getOrCreateAnimation(),
                    pauseSec(1)
                );
                animation.setOnFinished(e -> theGameController().letCurrentGameStateExpire());
                animation.play();
            }
            case GHOST_DYING ->
                theSimulationStep().killedGhosts.forEach(ghost -> {
                    int victimIndex = theGameLevel().victims().indexOf(ghost);
                    Image numberImage = theUI().configuration().createGhostNumberImage(victimIndex);
                    level3D.ghost3D(ghost.personality()).setNumberTexture(numberImage);
                });
            case LEVEL_COMPLETE -> {
                theGameState().timer().resetIndefiniteTime(); // expires when animation ends
                theSound().stopAll();
                level3D.onLevelCompleted();
                new SequentialTransition(
                    Ufx.doAfterSec(3, () -> {
                        perspectiveManager.perspectiveIDProperty().unbind();
                        perspectiveManager.setPerspective(PerspectiveID.TOTAL);
                    }),
                    level3D.levelCompletedAnimation().getOrCreateAnimation(),
                    Ufx.doAfterSec(1, () -> {
                        perspectiveManager.perspectiveIDProperty().bind(PY_3D_PERSPECTIVE);
                        animationManager.stopAllAnimations();
                        theGameController().letCurrentGameStateExpire();
                    })
                ).play();
            }
            case LEVEL_TRANSITION -> {
                theGameState().timer().restartSeconds(3);
                replaceGameLevel3D();
                level3D.pac3D().init();
                perspectiveManager.initPerspective();
            }
            case GAME_OVER -> {
                theGameState().timer().restartSeconds(3);
                boolean oneOutOf4Times = randomInt(0, 1000) < 250;
                if (!theGameLevel().isDemoLevel() && oneOutOf4Times) {
                    theUI().showFlashMessageSec(3, theAssets().localizedGameOverMessage());
                }
                theSound().stopAll();
                theSound().playGameOverSound();
                level3D.energizers3D().forEach(energizer3D -> energizer3D.shape3D().setVisible(false));
                level3D.bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
                animationManager.stopAllAnimations();
            }
            case TESTING_LEVELS_SHORT, TESTING_LEVELS_MEDIUM -> {
                replaceGameLevel3D();
                level3D.pac3D().init();
                level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(theGameLevel()));
                showLevelTestMessage(theGameLevel().number());
                PY_3D_PERSPECTIVE.set(PerspectiveID.TOTAL);
            }
            default -> {}
        }
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        requireNonNull(theGameLevel()); // just to be sure nothing bad happened
        bindActions();
        switch (theGameState()) {
            case STARTING_GAME -> {
                if (level3D == null) {
                    replaceGameLevel3D();
                }
                //TODO default position if no house
                Vector2f position = theGameLevel().house().map(House::centerPositionUnderHouse).orElse(Vector2f.ZERO);
                level3D.showAnimatedMessage("READY!", 2.5f, position.x(), position.y());
                bindPlayerSteeringActions();
            }
            case TESTING_LEVELS_SHORT, TESTING_LEVELS_MEDIUM -> {
                replaceGameLevel3D();
                level3D.livesCounter3D().lookingAroundAnimation().play(ManagedAnimation.FROM_START);
                level3D.energizers3D().forEach(energizer3D -> energizer3D.pumpingAnimation().play(ManagedAnimation.FROM_START));
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

        if (level3D == null) {
            replaceGameLevel3D();
        }
        level3D.pellets3D().forEach(pellet -> pellet.shape3D().setVisible(!theGameLevel().tileContainsEatenFood(pellet.tile())));
        level3D.energizers3D().forEach(energizer -> energizer.shape3D().setVisible(!theGameLevel().tileContainsEatenFood(energizer.tile())));
        if (isOneOf(theGameState(), GameState.HUNTING, GameState.GHOST_DYING)) { //TODO check this
            level3D.energizers3D()
                .filter(energizer3D -> energizer3D.shape3D().isVisible())
                .forEach(energizer3D -> energizer3D.pumpingAnimation().play(ManagedAnimation.FROM_START));
        }
        theGameLevel().pac().show();
        theGameLevel().ghosts().forEach(Ghost::show);
        level3D.pac3D().init();
        level3D.pac3D().update(theGameLevel());

        if (theGameState() == GameState.HUNTING) {
            if (theGameLevel().pac().powerTimer().isRunning()) {
                theSound().playPacPowerSound();
            }
            level3D.livesCounter3D().lookingAroundAnimation().play(ManagedAnimation.FROM_START);
        }

        subScene3D.setFill(Color.TRANSPARENT);
        updateScores(theGameLevel());
        bindActions();
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        theGameLevel().bonus().ifPresent(bonus -> {
            level3D.updateBonus3D(bonus);
            if (bonus instanceof MovingBonus) {
                theSound().playBonusBouncingSound();
            }
        });
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        theGameLevel().bonus().ifPresent(bonus -> {
            level3D.bonus3D().ifPresent(Bonus3D::showEaten);
            if (bonus instanceof MovingBonus) {
                theSound().stopBonusBouncingSound();
            }
            theSound().playBonusEatenSound();
        });
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        theGameLevel().bonus().ifPresent(bonus -> {
            level3D.bonus3D().ifPresent(Bonus3D::expire);
            if (bonus instanceof MovingBonus) {
                theSound().stopBonusBouncingSound();
            }
        });
    }

    @Override
    public void onSpecialScoreReached(GameEvent e) {
        theSound().playExtraLifeSound();
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        theSound().playGhostEatenSound();
    }

    @Override
    public void onGameContinued(GameEvent e) {
        Vector2f position = theGameLevel().house().map(House::centerPositionUnderHouse).orElse(Vector2f.ZERO);
        level3D.showAnimatedMessage("READY!", 0.5f, position.x(), position.y());
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = theGameLevel().isDemoLevel()
                || theGameState() == TESTING_LEVELS_SHORT || theGameState() == TESTING_LEVELS_MEDIUM;
        if (!silent) {
            theSound().playGameReadySound();
        }
    }

    @Override
    public void onPacFoundFood(GameEvent event) {
        Vector2i tile = event.tile().orElse(null);
        if (tile == null) {
            // When cheat "eat all pellets" has been used, no tile is present in the event.
            level3D.pellets3D().forEach(Pellet3D::onEaten);
        } else {
            Energizer3D energizer3D = level3D.energizers3D()
                .filter(e3D -> tile.equals(e3D.tile()))
                .findFirst().orElse(null);
            if (energizer3D != null) {
                energizer3D.onEaten();
            } else {
                level3D.pellets3D()
                    .filter(pellet3D -> tile.equals(pellet3D.tile()))
                    .findFirst()
                    .ifPresent(Pellet3D::onEaten);
            }
            theSound().playMunchingSound();
        }
    }

    @Override
    public void onPacGetsPower(GameEvent event) {
        level3D.pac3D().setMovementPowerMode(true);
        level3D.maze3D().wallColorFlashingAnimation().play(ManagedAnimation.FROM_START);
        theSound().stopSiren();
        theSound().playPacPowerSound();
    }

    @Override
    public void onPacLostPower(GameEvent event) {
        level3D.pac3D().setMovementPowerMode(false);
        level3D.maze3D().wallColorFlashingAnimation().stop();
        theSound().stopPacPowerSound();
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
        level3D = new GameLevel3D(theGameLevel(), animationManager);
        root.getChildren().set(root.getChildren().size() - 1, level3D.root());
        scores3D.translateXProperty().bind(level3D.root().translateXProperty().add(TS));
        scores3D.translateYProperty().bind(level3D.root().translateYProperty().subtract(3.5 * TS));
        scores3D.translateZProperty().bind(level3D.root().translateZProperty().subtract(3.5 * TS));
    }

    protected void updateSound(GameLevel gameLevel) {
        if (gameLevel.isDemoLevel()) {
            return; // demo level is silent
        }
        if (theGameState() == GameState.HUNTING && !gameLevel.pac().powerTimer().isRunning()) {
            int sirenNumber = 1 + theGame().huntingTimer().phaseIndex() / 2;
            theSound().selectSiren(sirenNumber);
            theSound().playSiren();
        }
        if (gameLevel.pac().starvingTicks() > 5) { // TODO not sure how to do this right
            theSound().stopMunchingSound();
        }
        boolean ghostsReturning = gameLevel.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (gameLevel.pac().isAlive() && ghostsReturning) {
            theSound().playGhostReturningHomeSound();
        } else {
            theSound().stopGhostReturningHomeSound();
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
        level3D.showAnimatedMessage("LEVEL %d (TEST)".formatted(levelNumber), 5, x, y);
    }
}