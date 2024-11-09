/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.scene.common;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.model.*;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameActions2D;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.common.CameraControlledGameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenGameActions;
import de.amr.games.pacman.ui2d.sound.GameSound;
import de.amr.games.pacman.ui3d.GameActions3D;
import de.amr.games.pacman.ui3d.level.*;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.games.pacman.controller.GameState.TESTING_LEVEL_BONI;
import static de.amr.games.pacman.controller.GameState.TESTING_LEVEL_TEASERS;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.pacman.PacManArcadeGame.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui2d.GameActions2D.*;
import static de.amr.games.pacman.ui2d.GameAssets2D.assetPrefix;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_AUTOPILOT;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_IMMUNITY;
import static de.amr.games.pacman.ui2d.input.Keyboard.alt;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenGameActions.bindDefaultJoypadActions;
import static de.amr.games.pacman.ui2d.util.Ufx.*;
import static de.amr.games.pacman.ui3d.PacManGames3dApp.*;

/**
 * 3D play scene.
 *
 * <p>Provides different camera perspectives that can be selected sequentially using keys <code>Alt+LEFT</code>
 * and <code>Alt+RIGHT</code>.</p>
 *
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene, CameraControlledGameScene {

    // Each 3D play scene has its own set of cameras/perspectives
    private final Map<Perspective.Name, Perspective> perspectiveMap = new EnumMap<>(Perspective.Name.class);
    {
        perspectiveMap.put(Perspective.Name.DRONE, new Perspective.DronePerspective());
        perspectiveMap.put(Perspective.Name.TOTAL, new Perspective.TotalPerspective());
        perspectiveMap.put(Perspective.Name.FOLLOWING_PLAYER, new Perspective.FollowingPlayerPerspective());
        perspectiveMap.put(Perspective.Name.NEAR_PLAYER, new Perspective.NearPlayerPerspective());
    }

    public final ObjectProperty<Perspective.Name> perspectiveNamePy = new SimpleObjectProperty<>(Perspective.Name.TOTAL) {
        @Override
        protected void invalidated() {
            Perspective.Name name = get();
            Perspective perspective = perspectiveMap.get(name);
            fxSubScene.setCamera(perspective.getCamera());
            context.game().level().ifPresent(level -> perspective.init(level.world()));
        }
    };

    private final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();
    private GameContext context;

    private final SubScene fxSubScene;
    private final Group root = new Group();
    private final Scores3D scores3D;

    private GameLevel3D level3D;
    private Animation levelCompleteAnimation;

    public PlayScene3D() {
        var ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PY_3D_LIGHT_COLOR);

        var axes = new CoordinateSystem();
        axes.visibleProperty().bind(PY_3D_AXES_VISIBLE);

        scores3D = new Scores3D("SCORE", "HIGH SCORE");

        // initial size is irrelevant as it is bound to parent scene later
        fxSubScene = new SubScene(root, 42, 42, true, SceneAntialiasing.BALANCED);
        fxSubScene.setFill(null); // transparent

        // last child is placeholder for level 3D
        root.getChildren().setAll(scores3D, axes, ambientLight, new Group());
    }

    @Override
    public final void init() {
        doInit();
        bindGameActions();
        registerGameActionKeyBindings(context().keyboard());
    }

    protected void doInit() {
        context.setScoreVisible(true);
        scores3D.fontPy.set(context.assets().font("font.arcade", 8));
        perspectiveNamePy.bind(PY_3D_PERSPECTIVE);
        Logger.info("3D play scene initialized. {}", this);
    }

    @Override
    public final void end() {
        doEnd();
        unregisterGameActionKeyBindings(context().keyboard());
    }

    @Override
    public void bindGameActions() {
        bind(GameActions3D.PREV_PERSPECTIVE, alt(KeyCode.LEFT));
        bind(GameActions3D.NEXT_PERSPECTIVE, alt(KeyCode.RIGHT));
    }

    private void setGameActions() {
        if (context.level().isDemoLevel()) {
            if (context.currentGameVariant() == GameVariant.MS_PACMAN_TENGEN) {
                bind(TengenGameActions.QUIT_DEMO_LEVEL, context.joypad().keyCombination(NES.Joypad.START));
            } else {
                bind(GameActions2D.ADD_CREDIT, context.arcade().keyCombination(Arcade.Controls.COIN));
            }
        }
        else {
            if (context.currentGameVariant() == GameVariant.MS_PACMAN_TENGEN) {
                bindDefaultJoypadActions(this, context.joypad());
            } else {
                bindDefaultArcadeControllerActions(this, context.arcade());
            }
            bindFallbackPlayerControlActions(this);
            bindCheatActions(this);
            context.setScoreVisible(true);
        }
        registerGameActionKeyBindings(context.keyboard());
    }

    @Override
    public void onLevelCreated(GameEvent event) {
        setGameActions();
        if (!hasLevel3D()) {
            replaceGameLevel3D(false); // level counter in model not yet initialized
        } else {
            Logger.error("3D level already created?");
        }
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        addLevelCounter();
        switch (context.gameState()) {
            case TESTING_LEVEL_BONI -> {
                replaceGameLevel3D(false);
                showLevelTestMessage("BONI LEVEL " + context.level().number);
            }
            case TESTING_LEVEL_TEASERS -> {
                replaceGameLevel3D(false);
                showLevelTestMessage("PREVIEW LEVEL " + context.level().number);
            }
            default -> {
                if (!context.level().isDemoLevel()){
                    showReadyMessage();
                }
            }
        }
        updateScores();
        perspective().init(context.level().world());
    }

    protected void doEnd() {
        perspectiveNamePy.unbind();
        if (levelCompleteAnimation != null) {
            levelCompleteAnimation.stop();
        }
        level3D = null;
        Logger.info("3D play scene ended. {}", this);
    }

    public boolean hasLevel3D() {
        return level3D != null;
    }

    @Override
    public void update() {
        if (context.game().level().isEmpty()) {
            // Scene is visible for 1 (2?) ticks before game level has been created
            Logger.warn("Tick #{}: Cannot update PlayScene3D: game level not yet available", context.tick());
            return;
        }
        // TODO: check this
        if (!hasLevel3D()) {
            Logger.warn("Tick #{}: Cannot update 3D play scene, 3D game level not yet created?", context.tick());
            return;
        }

        GameLevel level = context.level();
        level3D.update(context);

        perspective().update(level.world(), level.pac());
        scores3D.setRotationAxis(perspective().getCamera().getRotationAxis());
        scores3D.setRotate(perspective().getCamera().getRotate());

        //TODO check if this has to de done on every tick
        if (level.isDemoLevel()) {
            context.game().setDemoLevelBehavior();
        }
        else {
            level.pac().setUsingAutopilot(PY_AUTOPILOT.get());
            level.pac().setImmune(PY_IMMUNITY.get());
            updateScores();
            updateSound(context.sound());
        }
    }

    private void updateScores( ){
        ScoreManager scoreMgr = context.game().scoreManager();
        Score score = scoreMgr.score(), highScore = scoreMgr.highScore();

        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
        if (scoreMgr.isScoreEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else {
            // when score is disabled, show text "game over"
            String assetPrefix = assetPrefix(context.currentGameVariant());
            Color color = context.currentGameVariant() == GameVariant.MS_PACMAN_TENGEN
                ? Color.web(context.level().mapConfig().colorScheme().get("stroke"))
                : context.assets().color(assetPrefix + ".color.game_over_message");
            scores3D.showTextAsScore("GAME OVER!", color);
        }
    }

    private void updateSound(GameSound sound) {
        if (context.gameState() == GameState.HUNTING && !context.level().powerTimer().isRunning()) {
            int sirenNumber = 1 + context.game().huntingControl().phaseIndex() / 2;
            sound.selectSiren(sirenNumber);
            sound.playSiren();
        }
        if (context.level().pac().starvingTicks() > 8) { // TODO not sure how to do this right
            sound.stopMunchingSound();
        }
        boolean ghostsReturning = context.level().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (context.level().pac().isAlive() && ghostsReturning) {
            sound.playGhostReturningHomeSound();
        } else {
            sound.stopGhostReturningHomeSound();
        }
    }

    @Override
    public Map<KeyCodeCombination, GameAction> actionBindings() {
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
    public GameContext context() {
        return context;
    }

    @Override
    public void setGameContext(GameContext context) {
        this.context = checkNotNull(context);
    }

    public Perspective perspective() {
        return perspectiveMap.get(perspectiveNamePy.get());
    }

    @Override
    public Vector2f size() {
        return ARCADE_MAP_SIZE_IN_PIXELS; // irrelevant
    }

    @Override
    public double scaling() {
        return 1; // irrelevant
    }

    @Override
    public void onEnterGameState(GameState state) {
        Logger.info("Entering game state {}", state);
        switch (state) {
            case STARTING_GAME         -> onEnterStateStartingGame();
            case HUNTING               -> onEnterStateHunting();
            case PACMAN_DYING          -> onEnterStatePacManDying();
            case GHOST_DYING           -> onEnterStateGhostDying();
            case LEVEL_COMPLETE        -> onEnterStateLevelComplete();
            case LEVEL_TRANSITION      -> onEnterStateLevelTransition();
            case TESTING_LEVEL_BONI    -> onEnterStateTestingLevelBoni();
            case TESTING_LEVEL_TEASERS -> onEnterStateTestingLevelTeasers();
            case GAME_OVER             -> onEnterStateGameOver();
            default -> {}
        }
    }

    private void onEnterStateStartingGame() {
        if (hasLevel3D()) {
            stopLevelAnimations();
            level3D.pac3D().init();
            level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context));
            showReadyMessage();
        }
    }

    private void onEnterStateHunting() {
        level3D.livesCounter3D().shapesRotation().play();
        level3D.energizers3D().forEach(Energizer3D::startPumping);
    }

    private void onEnterStatePacManDying() {
        context.sound().stopAll();
        // last update before dying animation
        level3D.pac3D().update(context);
        playPacManDiesAnimation();
    }

    private void onEnterStateGhostDying() {
        GameSpriteSheet spriteSheet = context.currentGameSceneConfig().spriteSheet();
        RectArea[] numberSprites = spriteSheet.ghostNumberSprites();
        context.game().eventLog().killedGhosts.forEach(ghost -> {
            int victimIndex = context.level().victims().indexOf(ghost);
            var numberImage = spriteSheet.subImage(numberSprites[victimIndex]);
            level3D.ghost3D(ghost.id()).setNumberImage(numberImage);
        });
    }

    private void onEnterStateLevelComplete() {
        context.sound().stopAll();
        // if cheat has been used to complete level, food might still exist, so eat it:
        GameWorld world = context.level().world();
        world.map().food().tiles().forEach(world::registerFoodEatenAt);
        level3D.pellets3D().forEach(Pellet3D::onEaten);
        level3D.energizers3D().forEach(Energizer3D::onEaten);
        level3D.livesCounter3D().shapesRotation().stop();
        level3D.house3D().door3D().setVisible(false);
        playLevelCompleteAnimation();
    }

    private void onEnterStateLevelTransition() {
        context.gameState().timer().restartSeconds(3);
        replaceGameLevel3D(true);
        level3D.pac3D().init();
        perspective().init(context.level().world());
    }

    private void onEnterStateTestingLevelBoni() {
        replaceGameLevel3D(true);
        level3D.pac3D().init();
        level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context));
        showLevelTestMessage("BONI LEVEL" + context.level().number);
        PY_3D_PERSPECTIVE.set(Perspective.Name.TOTAL);
    }

    private void onEnterStateTestingLevelTeasers() {
        replaceGameLevel3D(true);
        level3D.pac3D().init();
        level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context));
        showLevelTestMessage("PREVIEW LEVEL " + context.level().number);
        PY_3D_PERSPECTIVE.set(Perspective.Name.TOTAL);
    }

    private void onEnterStateGameOver() {
        stopLevelAnimations();
        // delay state exit for 3 seconds
        context.gameState().timer().restartSeconds(3);
        context.showFlashMessageSec(3, context.locGameOverMessage());
        context.sound().stopAll();
        context.sound().playGameOverSound();
    }

    private void stopLevelAnimations() {
        level3D.energizers3D().forEach(Energizer3D::stopPumping);
        level3D.livesCounter3D().shapesRotation().stop();
        level3D.bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
    }

    @Override
    public void onSceneVariantSwitch(GameScene fromScene) {
        Logger.info("{} entered from {}", getClass().getSimpleName(), fromScene.getClass().getSimpleName());

        bindGameActions();
        registerGameActionKeyBindings(context.keyboard());
        setGameActions();

        if (!hasLevel3D()) {
            replaceGameLevel3D(true);
        }

        GameLevel level = context.level();
        level3D.pellets3D().forEach(pellet -> pellet.shape3D().setVisible(!level.world().hasEatenFoodAt(pellet.tile())));
        level3D.energizers3D().forEach(energizer -> energizer.shape3D().setVisible(!level.world().hasEatenFoodAt(energizer.tile())));
        if (oneOf(context.gameState(), GameState.HUNTING, GameState.GHOST_DYING)) { //TODO check this
            level3D.energizers3D().filter(energizer -> energizer.shape3D().isVisible()).forEach(Energizer3D::startPumping);
        }
        level.pac().show();
        level3D.pac3D().init();
        level.ghosts().forEach(Ghost::show);
        level3D.pac3D().update(context);

        if (context.gameState() == GameState.HUNTING) {
            if (level.powerTimer().isRunning()) {
                context.sound().playPacPowerSound();
            }
            level3D.livesCounter3D().shapesRotation().play();
        }

        updateScores();
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        context.level().bonus().ifPresent(bonus -> level3D.replaceBonus3D(bonus, context.currentGameSceneConfig().spriteSheet()));
        if (context.currentGameVariant() == GameVariant.MS_PACMAN_TENGEN) {
            //TODO also in Ms. Pac-Man!
            context.sound().playBonusBouncingSound();
        }
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::showEaten);
        if (context.currentGameVariant() == GameVariant.MS_PACMAN_TENGEN) {
            //TODO also in Ms. Pac-Man!
            context.sound().stopBonusBouncingSound();
        }
        context.sound().playBonusEatenSound();
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::onBonusExpired);
        if (context.currentGameVariant() == GameVariant.MS_PACMAN_TENGEN) {
            //TODO also in Ms. Pac-Man!
            context.sound().stopBonusBouncingSound();
        }
    }

    @Override
    public void onExtraLifeWon(GameEvent e) {
        context.sound().playExtraLifeSound();
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        context.sound().playGhostEatenSound();
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = context.level().isDemoLevel() ||
                context.gameState() == TESTING_LEVEL_BONI ||
                context.gameState() == TESTING_LEVEL_TEASERS;
        if (!silent) {
            context.sound().playGameReadySound();
        }
    }

    @Override
    public void onPacFoundFood(GameEvent event) {
        GameWorld world = context.level().world();
        if (event.tile().isEmpty()) {
            // When cheat "eat all pellets" has been used, no tile is present in the event.
            // In that case, ensure that the 3D representations are in sync with the game model.
            world.map().food().tiles()
                .filter(world::hasEatenFoodAt)
                .map(level3D::pellet3D)
                .flatMap(Optional::stream)
                .forEach(Eatable3D::onEaten);
        } else {
            Vector2i tile = event.tile().get();
            level3D.energizer3D(tile).ifPresent(Energizer3D::onEaten);
            level3D.pellet3D(tile).ifPresent(Pellet3D::onEaten);
        }
        context.sound().playMunchingSound();
    }

    @Override
    public void onPacGetsPower(GameEvent event) {
        level3D.pac3D().setPowerMode(true);
        context.sound().stopSiren();
        context.sound().playPacPowerSound();
    }

    @Override
    public void onPacLostPower(GameEvent event) {
        level3D.pac3D().setPowerMode(false);
        context.sound().stopPacPowerSound();
    }

    private void addLevelCounter() {
        // Place level counter at top right maze corner
        double x = context.level().world().map().terrain().numCols() * TS - 2 * TS;
        double y = 2 * TS;
        Node levelCounter3D = GameLevel3D.createLevelCounter3D(context.currentGameSceneConfig().spriteSheet(),
                context.game().levelCounter(), x, y);
        level3D.root().getChildren().add(levelCounter3D);
    }

    private void replaceGameLevel3D(boolean createLevelCounter) {
        level3D = new GameLevel3D(context);
        if (createLevelCounter) {
            addLevelCounter();
        }
        int lastIndex = root.getChildren().size() - 1;
        root.getChildren().set(lastIndex, level3D.root());
        scores3D.translateXProperty().bind(level3D.root().translateXProperty().add(TS));
        scores3D.translateYProperty().bind(level3D.root().translateYProperty().subtract(3.5 * TS));
        scores3D.translateZProperty().bind(level3D.root().translateZProperty().subtract(3 * TS));

        Logger.info("3D game level {} created.", context.level().number);
    }

    private void showLevelTestMessage(String message) {
        TileMap terrainMap = context.level().world().map().terrain();
        double x = terrainMap.numCols() * HTS;
        double y = (terrainMap.numRows() - 2) * TS;
        level3D.showAnimatedMessage(message, 5, x, y);
    }

    private void showReadyMessage() {
        GameWorld world = context.level().world();
        Vector2i houseTopLeft = world.houseTopLeftTile();
        Vector2i houseSize = world.houseSize();
        double x = TS * (houseTopLeft.x() + 0.5 * houseSize.x());
        double y = TS * (houseTopLeft.y() +       houseSize.y());
        double seconds = context.game().isPlaying() ? 0.5 : 2.5;
        level3D.showAnimatedMessage("READY!", seconds, x, y);
    }

    private void playPacManDiesAnimation() {
        context.gameState().timer().resetIndefinitely();
        Animation animation = level3D.pac3D().createDyingAnimation();
        animation.setDelay(Duration.seconds(1));
        animation.setOnFinished(e -> context.gameState().timer().expire());
        animation.play();
    }

    private void playLevelCompleteAnimation() {
        context.gameState().timer().resetIndefinitely(); // block game state until animation has finished
        levelCompleteAnimation = context.game().intermissionNumberAfterLevel() != 0
            ? levelCompleteAnimationBeforeIntermission(context.game().numFlashes())
            : levelCompleteAnimation(context.game().numFlashes());
        levelCompleteAnimation.setDelay(Duration.seconds(1.0));
        levelCompleteAnimation.setOnFinished(e -> context.gameState().timer().expire());
        levelCompleteAnimation.play();
    }

    private Animation levelCompleteAnimationBeforeIntermission(int numFlashes) {
        return new SequentialTransition(
            pauseSec(1)
            , level3D.mazeFlashAnimation(numFlashes)
            , doAfterSec(2.5, () -> context.level().pac().hide())
        );
    }

    private Animation levelCompleteAnimation(int numFlashes) {
        return new SequentialTransition(
              now(() -> {
                  perspectiveNamePy.unbind();
                  perspectiveNamePy.set(Perspective.Name.TOTAL);
                  level3D.livesCounter3D().light().setLightOn(false);
                  context.showFlashMessageSec(3, context.locLevelCompleteMessage());
                  context.sound().playLevelCompleteSound();
              })
            , doAfterSec(1, level3D.mazeFlashAnimation(numFlashes))
            , doAfterSec(0.5, () -> context.level().pac().hide())
            , doAfterSec(0.5, level3D.levelRotateAnimation(1.5))
            , level3D.wallsDisappearAnimation(2.0)
            , doAfterSec(1, () -> {
                context.sound().playLevelChangedSound();
                perspectiveNamePy.bind(PY_3D_PERSPECTIVE);
            })
        );
    }
}