/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.model.ScoreManager;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.MovingBonus;
import de.amr.pacmanfx.ui.GameAction;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import de.amr.pacmanfx.uilib.Action;
import de.amr.pacmanfx.uilib.CameraControlledView;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.isOneOf;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVEL_TEASERS;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static de.amr.pacmanfx.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;
import static de.amr.pacmanfx.uilib.Ufx.contextMenuTitleItem;
import static de.amr.pacmanfx.uilib.input.Keyboard.*;

/**
 * 3D play scene. Provides different camera perspectives that can be stepped
 * through using keys <code>Alt+LEFT</code> and <code>Alt+RIGHT</code>.
 *
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene, CameraControlledView {

    protected final ObjectProperty<PerspectiveID> perspectiveNamePy = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            optionalGameLevel().ifPresent(level -> perspective().init(fxSubScene, level));
        }
    };

    protected final Map<PerspectiveID, Perspective> perspectives = new EnumMap<>(PerspectiveID.class);
    protected final Map<KeyCodeCombination, Action> actionBindings = new HashMap<>();
    protected final Group root = new Group();
    protected final SubScene fxSubScene;
    protected final PerspectiveCamera camera = new PerspectiveCamera(true);
    protected final Scores3D scores3D;

    protected GameLevel3D level3D;

    public PlayScene3D() {
        var axes = new CoordinateSystem();
        axes.visibleProperty().bind(PY_3D_AXES_VISIBLE);

        scores3D = new Scores3D(theAssets().text("score.score"), theAssets().text("score.high_score"));

        // last child is placeholder for level 3D
        root.getChildren().addAll(scores3D, axes, new Group());

        // initial size is irrelevant, gets bound to parent scene size later
        fxSubScene = new SubScene(root, 88, 88, true, SceneAntialiasing.BALANCED);
        fxSubScene.setFill(Color.TRANSPARENT);
        fxSubScene.setCamera(camera);

        perspectives.put(PerspectiveID.DRONE, new Perspective.Drone());
        perspectives.put(PerspectiveID.TOTAL, new Perspective.Total());
        perspectives.put(PerspectiveID.TRACK_PLAYER, new Perspective.TrackingPlayer());
        perspectives.put(PerspectiveID.NEAR_PLAYER, new Perspective.StalkingPlayer());

        scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
        scores3D.rotateProperty().bind(camera.rotateProperty());
    }

    protected Perspective perspective() { return perspectives.get(perspectiveNamePy.get()); }

    protected void replaceGameLevel3D(GameLevel level) {
        final GameVariant gameVariant = theGameVariant();
        level3D = new GameLevel3D(gameVariant, level);
        level3D.addLevelCounter(theUIConfig().configuration(gameVariant));
        int lastIndex = root.getChildren().size() - 1;
        root.getChildren().set(lastIndex, level3D.root());
        scores3D.translateXProperty().bind(level3D.root().translateXProperty().add(TS));
        scores3D.translateYProperty().bind(level3D.root().translateYProperty().subtract(3.5 * TS));
        scores3D.translateZProperty().bind(level3D.root().translateZProperty().subtract(3.5 * TS));
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent e) {
        List<MenuItem> items = new ArrayList<>();

        items.add(contextMenuTitleItem(theAssets().text("scene_display")));

        var item = new MenuItem(theAssets().text("use_2D_scene"));
        item.setOnAction(ae -> GameAction.TOGGLE_PLAY_SCENE_2D_3D.execute());
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
        miAutopilot.selectedProperty().bindBidirectional(PY_AUTOPILOT);
        items.add(miAutopilot);

        var miImmunity = new CheckMenuItem(theAssets().text("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        items.add(miImmunity);

        items.add(new SeparatorMenuItem());

        var miMuted = new CheckMenuItem(theAssets().text("muted"));
        miMuted.selectedProperty().bindBidirectional(theSound().mutedProperty());
        items.add(miMuted);

        var miQuit = new MenuItem(theAssets().text("quit"));
        miQuit.setOnAction(ae -> GameAction.QUIT_GAME_SCENE.execute());
        items.add(miQuit);

        return items;
    }

    @Override
    public void init() {
        bindActions();
        enableActionBindings();
        theGame().scoreManager().setScoreVisible(true);
        perspectiveNamePy.bind(PY_3D_PERSPECTIVE);
        scores3D.setFont(theAssets().font("font.arcade", TS));
    }

    @Override
    public final void end() {
        disableActionBindings();
        perspectiveNamePy.unbind();
        level3D.stopAnimations();
        level3D = null;
        theSound().stopAll();
    }

    @Override
    public void bindActions() {
        bindArcadeInsertCoinAction();
        if (optionalGameLevel().isPresent() && !optionalGameLevel().get().isDemoLevel()) {
            if (!theGameController().isSelected(GameVariant.MS_PACMAN_TENGEN)) {
                bindArcadeStartGameAction();
            }
            bindCheatActions();
        }
        bind(GameAction.PERSPECTIVE_PREVIOUS, alt(KeyCode.LEFT));
        bind(GameAction.PERSPECTIVE_NEXT, alt(KeyCode.RIGHT));
        bind(GameAction.TOGGLE_DRAW_MODE, alt(KeyCode.W));
        enableActionBindings();
    }

    private void bindPlayerActions() {
        bind(GameAction.createPlayerAction(Direction.UP),    naked(KeyCode.UP),    control(KeyCode.UP));
        bind(GameAction.createPlayerAction(Direction.DOWN),  naked(KeyCode.DOWN),  control(KeyCode.DOWN));
        bind(GameAction.createPlayerAction(Direction.LEFT),  naked(KeyCode.LEFT),  control(KeyCode.LEFT));
        bind(GameAction.createPlayerAction(Direction.RIGHT), naked(KeyCode.RIGHT), control(KeyCode.RIGHT));
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        optionalGameLevel().ifPresent(level -> {
            bindActions(); //TODO check if this is necessary
            if (level3D == null) {
                replaceGameLevel3D(level);
            }
            switch (theGameState()) {
                case TESTING_LEVELS, TESTING_LEVEL_TEASERS -> {
                    replaceGameLevel3D(level);
                    level3D.playLivesCounterAnimation();
                    level3D.energizers3D().forEach(Energizer3D::startPumping);
                    showLevelTestMessage(level, "TEST LEVEL " + level.number());
                }
                default -> {
                    if (!level.isDemoLevel()) {
                        bindPlayerActions();
                        enableActionBindings();
                        showReadyMessage(level);
                    }
                }
            }
            updateScores();
            perspective().init(fxSubScene, level);
        });
    }

    @Override
    public void onSceneVariantSwitch(GameScene fromScene) {
        optionalGameLevel().ifPresent(level -> {
            bindActions();
            bindPlayerActions();
            enableActionBindings();
            if (level3D == null) {
                replaceGameLevel3D(level);
            }
            level3D.pellets3D().forEach(pellet -> pellet.shape3D().setVisible(!level.hasEatenFoodAt(pellet.tile())));
            level3D.energizers3D().forEach(energizer -> energizer.shape3D().setVisible(!level.hasEatenFoodAt(energizer.tile())));
            if (isOneOf(theGameState(), GameState.HUNTING, GameState.GHOST_DYING)) { //TODO check this
                level3D.energizers3D().filter(energizer -> energizer.shape3D().isVisible()).forEach(Energizer3D::startPumping);
            }
            level.pac().show();
            level.ghosts().forEach(Ghost::show);
            level3D.pac3D().init();
            level3D.pac3D().update(level);
            if (theGameState() == GameState.HUNTING) {
                if (level.pac().powerTimer().isRunning()) {
                    theSound().playPacPowerSound();
                }
                level3D.playLivesCounterAnimation();
            }
            updateScores();
        });
    }

    @Override
    public final void update() {
        if (optionalGameLevel().isEmpty()) {
            // Scene is already visible 2 ticks before level has been created
            Logger.warn("Tick #{}: Game level not yet existing", theClock().tickCount());
            return;
        }
        final GameLevel level = theGameLevel();
        if (level3D == null) {
            Logger.warn("Tick #{}: 3D game level not yet existing", theClock().tickCount());
            return;
        }
        level3D.update();
        //TODO how to avoid calling this on every tick?
        if (level.isDemoLevel()) {
            theGame().assignDemoLevelBehavior(level.pac());
        }
        else {
            level.pac().setUsingAutopilot(PY_AUTOPILOT.get());
            level.pac().setImmune(PY_IMMUNITY.get());
            updateScores();
            updateSound(level);
        }
        perspective().update(fxSubScene, level, level.pac());
    }

    protected void updateSound(GameLevel level) {
        if (theGameState() == GameState.HUNTING && !level.pac().powerTimer().isRunning()) {
            int sirenNumber = 1 + level.huntingTimer().phaseIndex() / 2;
            theSound().selectSiren(sirenNumber);
            theSound().playSiren();
        }
        if (level.pac().starvingTicks() > 5) { // TODO not sure how to do this right
            theSound().stopMunchingSound();
        }
        boolean ghostsReturning = level.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (level.pac().isAlive() && ghostsReturning) {
            theSound().playGhostReturningHomeSound();
        } else {
            theSound().stopGhostReturningHomeSound();
        }
    }

    protected void updateScores() {
        ScoreManager scoreManager = theGame().scoreManager();
        Score score = scoreManager.score(), highScore = scoreManager.highScore();
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
        if (score.isEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else { // score is disabled, show text "GAME OVER"
            String ans = theUIConfig().current().assetNamespace();
            Color color = theAssets().color(ans + ".color.game_over_message");
            scores3D.showTextAsScore(theAssets().text("score.game_over"), color);
        }
    }

    @Override
    public Map<KeyCodeCombination, Action> actionBindings() {
        return actionBindings;
    }

    @Override
    public DoubleProperty viewPortWidthProperty() {
        return fxSubScene.widthProperty();
    }

    @Override
    public DoubleProperty viewPortHeightProperty() {
        return fxSubScene.heightProperty();
    }

    @Override
    public SubScene viewPort() {
        return fxSubScene;
    }

    @Override
    public Camera camera() {
        return fxSubScene.getCamera();
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS; // irrelevant
    }

    @Override
    public void onEnterGameState(GameState state) {
        Logger.trace("Entering game state {}", state);
        optionalGameLevel().ifPresent(level -> {
            switch (state) {
                case HUNTING               -> onEnterStateHunting(level);
                case PACMAN_DYING          -> onEnterStatePacManDying(level);
                case GHOST_DYING           -> onEnterStateGhostDying(level);
                case LEVEL_COMPLETE        -> onEnterStateLevelComplete();
                case LEVEL_TRANSITION      -> onEnterStateLevelTransition(level);
                case TESTING_LEVELS        -> onEnterStateTestingLevels(level);
                case TESTING_LEVEL_TEASERS -> onEnterStateTestingLevelTeasers(level);
                case GAME_OVER             -> onEnterStateGameOver();
                default -> {}
            }
        });
    }

    private void onEnterStateHunting(GameLevel level) {
        level3D.pac3D().init();
        level3D.ghosts3D().forEach(ghost3DAppearance -> ghost3DAppearance.init(level));
        level3D.energizers3D().forEach(Energizer3D::startPumping);
        level3D.playLivesCounterAnimation();
    }

    private void onEnterStatePacManDying(GameLevel level) {
        level3D.stopAnimations();
        theSound().stopAll();
        // last update before dying animation
        level3D.pac3D().update(level);
        playPacManDiesAnimation();
    }

    private void onEnterStateGhostDying(GameLevel level) {
        GameSpriteSheet spriteSheet = theUIConfig().current().spriteSheet();
        RectArea[] numberSprites = spriteSheet.ghostNumberSprites();
        theSimulationStep().killedGhosts().forEach(ghost -> {
            int victimIndex = level.victims().indexOf(ghost);
            var numberImage = spriteSheet.crop(numberSprites[victimIndex]);
            level3D.ghost3D(ghost.personality()).setNumberImage(numberImage);
        });
    }

    private void onEnterStateLevelComplete() {
        theSound().stopAll();
        level3D.pellets3D().forEach(Pellet3D::onEaten);
        level3D.energizers3D().forEach(Energizer3D::onEaten);
        level3D.maze3D().door3D().setVisible(false);
        level3D.stopAnimations();
        Animation animation = level3D.createLevelCompleteAnimation();
        animation.setDelay(Duration.seconds(2));
        animation.setOnFinished(e -> {
            perspectiveNamePy.bind(PY_3D_PERSPECTIVE);
            theGameController().letCurrentGameStateExpire();
        });
        theGameState().timer().resetIndefiniteTime();
        perspectiveNamePy.unbind();
        perspectiveNamePy.set(PerspectiveID.TOTAL);
        animation.play();
    }

    private void onEnterStateLevelTransition(GameLevel level) {
        theGameState().timer().restartSeconds(3);
        replaceGameLevel3D(level);
        level3D.pac3D().init();
        perspective().init(fxSubScene, level);
    }

    private void onEnterStateTestingLevels(GameLevel level) {
        replaceGameLevel3D(level);
        level3D.pac3D().init();
        level3D.ghosts3D().forEach(ghost3DAppearance -> ghost3DAppearance.init(level));
        showLevelTestMessage(level, "TEST LEVEL" + level.number());
        PY_3D_PERSPECTIVE.set(PerspectiveID.TOTAL);
    }

    private void onEnterStateTestingLevelTeasers(GameLevel level) {
        replaceGameLevel3D(level);
        level3D.pac3D().init();
        level3D.ghosts3D().forEach(ghost3DAppearance -> ghost3DAppearance.init(level));
        showLevelTestMessage(level, "PREVIEW LEVEL " + level.number());
        PY_3D_PERSPECTIVE.set(PerspectiveID.TOTAL);
    }

    private void onEnterStateGameOver() {
        // delay state exit for 3 seconds:
        theGameState().timer().restartSeconds(3);
        level3D.stopAnimations();
        if (!theGameLevel().isDemoLevel() && randomInt(0, 100) < 25) {
            theUI().showFlashMessageSec(3, theAssets().localizedGameOverMessage());
        }
        theSound().stopAll();
        theSound().playGameOverSound();
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        theGameLevel().bonus().ifPresent(bonus -> {
            level3D.updateBonus3D(bonus, theUIConfig().current().spriteSheet());
            if (bonus instanceof MovingBonus) {
                theSound().playBonusActiveSound();
            }
        });
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::showEaten);
        theGameLevel().bonus().ifPresent(bonus -> {
            if (bonus instanceof MovingBonus) {
                theSound().stopBonusActiveSound();
            }
            theSound().playBonusEatenSound();
        });
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::expire);
        theGameLevel().bonus().ifPresent(bonus -> {
            if (bonus instanceof MovingBonus) {
                theSound().stopBonusActiveSound();
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
        optionalGameLevel().ifPresent(this::showReadyMessage);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        GameLevel level = theGameLevel();
        boolean silent = level.isDemoLevel() ||
            theGameState() == TESTING_LEVELS || theGameState() == TESTING_LEVEL_TEASERS;
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
        level3D.maze3D().playMaterialAnimation();
        theSound().stopSiren();
        theSound().playPacPowerSound();
    }

    @Override
    public void onPacLostPower(GameEvent event) {
        level3D.pac3D().setMovementPowerMode(false);
        level3D.maze3D().stopMaterialAnimation();
        theSound().stopPacPowerSound();
    }

    private void showLevelTestMessage(GameLevel level, String message) {
        WorldMap worldMap = level.worldMap();
        double x = worldMap.numCols() * HTS;
        double y = (worldMap.numRows() - 2) * TS;
        level3D.showAnimatedMessage(message, 5, x, y);
    }

    private void showReadyMessage(GameLevel level) {
        Vector2i houseTopLeft = level.houseMinTile();
        Vector2i houseSize = level.houseSizeInTiles();
        double x = TS * (houseTopLeft.x() + 0.5 * houseSize.x());
        double y = TS * (houseTopLeft.y() +       houseSize.y());
        double seconds = theGame().isPlaying() ? 0.5 : 2.5;
        level3D.showAnimatedMessage("READY!", seconds, x, y);
    }

    private void playPacManDiesAnimation() {
        theGameState().timer().resetIndefiniteTime();
        Animation pacDyingAnimation = level3D.pac3D().createDyingAnimation();
        Animation animation =
            new SequentialTransition(
                new ParallelTransition(
                    Ufx.now(theSound()::playPacDeathSound),
                    pacDyingAnimation
                ),
                Ufx.pauseSec(1)
            );
        animation.setDelay(Duration.seconds(2));
        animation.setOnFinished(e -> theGameController().letCurrentGameStateExpire());

        animation.play();
    }
}