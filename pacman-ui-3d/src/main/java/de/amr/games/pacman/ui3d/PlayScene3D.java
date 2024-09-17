/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.GameSounds;
import de.amr.games.pacman.ui2d.rendering.SpriteArea;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.pacman.PacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.util.Picker;
import de.amr.games.pacman.ui3d.level.*;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_AUTOPILOT;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_IMMUNITY;
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
public class PlayScene3D implements GameScene {

    public final ObjectProperty<Perspective> perspectivePy = new SimpleObjectProperty<>(this, "perspective") {
        @Override
        protected void invalidated() {
            perspective().init(fxSubScene.getCamera(), context.game().world());
        }
    };

    private final SubScene fxSubScene;
    private final Group root = new Group();
    private final Scores3D scores3D;

    private GameLevel3D level3D;
    private GameContext context;
    private Picker<String> pickerGameOver;
    private Picker<String> pickerLevelComplete;

    public PlayScene3D() {
        var ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PY_3D_LIGHT_COLOR);

        var coordSystem = new CoordinateSystem();
        coordSystem.visibleProperty().bind(PY_3D_AXES_VISIBLE);

        scores3D = new Scores3D("SCORE", "HIGH SCORE");

        // initial size is irrelevant as it is bound to parent scene later
        fxSubScene = new SubScene(root, 42, 42, true, SceneAntialiasing.BALANCED);
        fxSubScene.setFill(null); // transparent

        var camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(30); // default: 30
        fxSubScene.setCamera(camera);

        // keep the scores rotated such that the viewer always sees them frontally
        scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
        scores3D.rotateProperty().bind(camera.rotateProperty());

        // last child is placeholder for level 3D
        root.getChildren().setAll(scores3D, coordSystem, ambientLight, new Group());
    }

    public DoubleProperty widthProperty() {
        return fxSubScene.widthProperty();
    }

    public DoubleProperty heightProperty() {
        return fxSubScene.heightProperty();
    }

    public void setContext(GameContext context) {
        this.context = checkNotNull(context);
    }

    @Override
    public Node root() {
        return fxSubScene;
    }

    public Camera camera() {
        return fxSubScene.getCamera();
    }

    public Perspective perspective() {
        return perspectivePy.get();
    }

    @Override
    public void init() {
        context.setScoreVisible(true);
        scores3D.fontPy.set(context.assets().font("font.arcade", 8));
        perspectivePy.bind(PY_3D_PERSPECTIVE);
        pickerGameOver = Picker.fromBundle(context.assets().bundles().getLast(), "game.over");
        pickerLevelComplete = Picker.fromBundle(context.assets().bundles().getLast(), "level.complete");
        Logger.info("3D play scene initialized. {}", this);
    }

    @Override
    public void end() {
        perspectivePy.unbind();
        level3D = null;
        Logger.info("3D play scene ended. {}", this);
    }

    @Override
    public void update() {
        var game = context.game();
        if (game.level().isEmpty() || game.world() == null) {
            Logger.error("Cannot update 3D play scene, no game level exists!");
            return;
        }
        if (level3D == null) {
            Logger.warn("Cannot update 3D play scene, 3D game level not yet created?");
            return;
        }
        level3D.update();
        perspective().update(fxSubScene.getCamera(), game.world(), game.pac());

        if (context.game().isDemoLevel()) {
            context.game().pac().setUseAutopilot(true);
            context.game().pac().setImmune(false);
        } else {
            context.setScoreVisible(true);
            context.game().pac().setUseAutopilot(PY_AUTOPILOT.get());
            context.game().pac().setImmune(PY_IMMUNITY.get());
        }

        // Scores
        scores3D.showHighScore(game.highScore().points(), game.highScore().levelNumber());
        if (context.game().hasCredit()) {
            scores3D.showScore(game.score().points(), game.score().levelNumber());
        } else { // demo level or "game over" state
            scores3D.showTextAsScore("GAME OVER!", Color.RED);
        }

        // Sound
        if (context.gameState() == GameState.HUNTING && !context.game().powerTimer().isRunning()) {
            int sirenNumber = 1 + context.game().huntingPhaseIndex() / 2;
            GameSounds.selectSiren(sirenNumber);
            GameSounds.playSiren();
        }
        if (context.game().pac().starvingTicks() > 8) { // TODO not sure how to do this right
            GameSounds.stopMunchingSound();
        }
        boolean ghostsReturning = context.game().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (context.game().pac().isAlive() && ghostsReturning) {
            GameSounds.playGhostReturningHomeSound();
        } else {
            GameSounds.stopGhostReturningHomeSound();
        }
    }

    @Override
    public void handleUserInput() {
        if (GameAction.ADD_CREDIT.requested() && this.context.game().isDemoLevel()) {
            context.addCredit();
        } else if (GameAction.PREV_PERSPECTIVE.requested()) {
            context.selectPrevPerspective();
        } else if (GameAction.NEXT_PERSPECTIVE.requested()) {
            context.selectNextPerspective();
        } else if (GameAction.CHEAT_EAT_ALL.requested()) {
            context.cheatEatAllPellets();
        } else if (GameAction.CHEAT_ADD_LIVES.requested()) {
            context.cheatAddLives();
        } else if (GameAction.CHEAT_NEXT_LEVEL.requested()) {
            context.cheatEnterNextLevel();
        } else if (GameAction.CHEAT_KILL_GHOSTS.requested()) {
            context.cheatKillAllEatableGhosts();
        }
    }

    @Override
    public void onGameStateEntry(GameState state) {
        switch (state) {
            case READY            -> onEnterStateReady();
            case HUNTING          -> onEnterStateHunting();
            case PACMAN_DYING     -> onEnterStatePacManDying();
            case GHOST_DYING      -> onEnterStateGhostDying();
            case LEVEL_COMPLETE   -> onEnterStateLevelComplete();
            case LEVEL_TRANSITION -> onEnterStateLevelTransition();
            case LEVEL_TEST       -> onEnterStateLevelTest();
            case GAME_OVER        -> onEnterStateGameOver();
            default -> {}
        }
    }

    private void onEnterStateReady() {
        GameSounds.stopAll();
        if (level3D != null) {
            stopLevelAnimations();
            level3D.pac3D().init();
            level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context));
            showReadyMessage();
        }
    }

    private void onEnterStateHunting() {
        level3D.livesCounter3D().startAnimation();
        level3D.energizers3D().forEach(Energizer3D::startPumping);
    }

    private void onEnterStatePacManDying() {
        GameSounds.stopAll();
        // last update before dying animation
        level3D.pac3D().update(context);
        playPacManDiesAnimation();
    }

    private void onEnterStateGhostDying() {
        switch (context.game().variant()) {
            case MS_PACMAN, MS_PACMAN_TENGEN -> {
                var ss = (MsPacManGameSpriteSheet) context.spriteSheet(context.game().variant());
                SpriteArea[] numberSprites = ss.ghostNumberSprites();
                context.game().eventLog().killedGhosts.forEach(ghost -> {
                    int index = context.game().victims().indexOf(ghost);
                    var numberImage = ss.subImage(numberSprites[index]);
                    level3D.ghost3D(ghost.id()).setNumberImage(numberImage);
                });
            }
            case PACMAN, PACMAN_XXL -> {
                var ss = (PacManGameSpriteSheet) context.spriteSheet(context.game().variant());
                SpriteArea[] numberSprites = ss.ghostNumberSprites();
                context.game().eventLog().killedGhosts.forEach(ghost -> {
                    int index = context.game().victims().indexOf(ghost);
                    var numberImage = ss.subImage(numberSprites[index]);
                    level3D.ghost3D(ghost.id()).setNumberImage(numberImage);
                });
            }
        }
    }

    private void onEnterStateLevelComplete() {
        GameSounds.stopAll();
        // if cheat has been used to complete level, food might still exist, so eat it:
        GameWorld world = context.game().world();
        world.map().food().tiles().forEach(world::eatFoodAt);
        level3D.pellets3D().forEach(Pellet3D::onEaten);
        level3D.energizers3D().forEach(Energizer3D::onEaten);
        level3D.livesCounter3D().stopAnimation();
        level3D.house3D().door3D().setVisible(false);
        playLevelCompleteAnimation();
    }

    private void onEnterStateLevelTransition() {
        context.gameState().timer().restartSeconds(3);
        replaceGameLevel3D(true);
        level3D.pac3D().init();
        perspective().init(fxSubScene.getCamera(), context.game().world());
    }

    private void onEnterStateLevelTest() {
        replaceGameLevel3D(true);
        level3D.pac3D().init();
        level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context));
        showLevelTestMessage();
        PY_3D_PERSPECTIVE.set(Perspective.TOTAL);
    }

    private void onEnterStateGameOver() {
        stopLevelAnimations();
        // delay state exit for 3 seconds
        context.gameState().timer().restartSeconds(3);
        context.showFlashMessageSeconds(3, pickerGameOver.next());
        GameSounds.stopAll();
        GameSounds.playGameOverSound();
    }

    private void stopLevelAnimations() {
        level3D.energizers3D().forEach(Energizer3D::stopPumping);
        level3D.livesCounter3D().stopAnimation();
        level3D.bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        Logger.info("{} entered from {}", this.getClass().getSimpleName(), oldScene.getClass().getSimpleName());
        if (level3D == null) {
            replaceGameLevel3D(true);
        }
        level3D.pellets3D().forEach(
            pellet3D -> pellet3D.shape3D().setVisible(!context.game().world().hasEatenFoodAt(pellet3D.tile()))
        );
        level3D.energizers3D().forEach(
            energizer3D -> energizer3D.shape3D().setVisible(!context.game().world().hasEatenFoodAt(energizer3D.tile()))
        );
        if (oneOf(context.gameState(), GameState.HUNTING, GameState.GHOST_DYING)) {
            level3D.energizers3D().filter(energizer3D -> energizer3D.shape3D().isVisible()).forEach(Energizer3D::startPumping);
        }
        context.game().pac().show();
        context.game().ghosts().forEach(Ghost::show);
        level3D.pac3D().init();
        level3D.pac3D().update(context);

        if (context.gameState() == GameState.HUNTING) {
            if (context.game().powerTimer().isRunning()) {
                GameSounds.playPacPowerSound();
            }
            level3D.livesCounter3D().startAnimation();
        }
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        context.game().bonus().ifPresent(level3D::replaceBonus3D);
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::showEaten);
        GameSounds.playBonusEatenSound();
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::onBonusExpired);
    }

    @Override
    public void onExtraLifeWon(GameEvent e) {
        GameSounds.playExtraLifeSound();
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        GameSounds.playGhostEatenSound();
    }

    @Override
    public void onLevelCreated(GameEvent event) {
        if (level3D == null) {
            replaceGameLevel3D(false); // level counter in model not yet initialized
        } else {
            Logger.error("3D level already created?");
        }
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        level3D.addLevelCounter3D(context.game().levelCounter());
        if (context.game().levelNumber() == 1 || context.gameState() == GameState.LEVEL_TEST) {
            if (context.gameState() == GameState.LEVEL_TEST) {
                replaceGameLevel3D(false);
                showLevelTestMessage();
            } else if (!context.game().isDemoLevel()){
                showReadyMessage();
            }
        }
    }

    @Override
    public void onPacFoundFood(GameEvent event) {
        GameWorld world = context.game().world();
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
        GameSounds.playMunchingSound();
    }

    @Override
    public void onPacGetsPower(GameEvent event) {
        level3D.pac3D().setPowerMode(true);
        GameSounds.stopSiren();
        GameSounds.playPacPowerSound();
    }

    @Override
    public void onPacLostPower(GameEvent event) {
        level3D.pac3D().setPowerMode(false);
        GameSounds.stopPacPowerSound();
    }

    private void replaceGameLevel3D(boolean createLevelCounter) {
        level3D = new GameLevel3D(context);
        if (createLevelCounter) {
            level3D.addLevelCounter3D(context.game().levelCounter());
        }
        int lastIndex = root.getChildren().size() - 1;
        root.getChildren().set(lastIndex, level3D.root());
        scores3D.translateXProperty().bind(level3D.root().translateXProperty().add(TS));
        scores3D.translateYProperty().bind(level3D.root().translateYProperty().subtract(3.5 * TS));
        scores3D.translateZProperty().bind(level3D.root().translateZProperty().subtract(3 * TS));
        Logger.info("3D game level {} created.", context.game().levelNumber());
    }

    private void showLevelTestMessage() {
        TileMap terrainMap = context.game().world().map().terrain();
        double x = terrainMap.numCols() * HTS;
        double y = (terrainMap.numRows() - 2) * TS;
        String message = "TEST LEVEL " + context.game().levelNumber();
        level3D.showAnimatedMessage(message, 5, x, y);
    }

    private void showReadyMessage() {
        GameWorld world = context.game().world();
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
        int numFlashes = context.game().level().orElseThrow().numFlashes();
        boolean intermission = context.game().intermissionNumber(context.game().levelNumber()) != 0;
        Animation animation = intermission ? levelCompleteAnimationBeforeIntermission(numFlashes) : levelCompleteAnimation(numFlashes);
        animation.setDelay(Duration.seconds(1.0));
        animation.setOnFinished(e -> context.gameState().timer().expire());
        context.gameState().timer().resetIndefinitely();
        animation.play();
    }

    private Animation levelCompleteAnimationBeforeIntermission(int numFlashes) {
        return new SequentialTransition(
            pauseSec(1)
            , level3D.mazeFlashAnimation(numFlashes)
            , doAfterSec(2.5, () -> context.game().pac().hide())
        );
    }

    //TODO is there are better way to do this e.g. using a Timeline?
    private Animation levelCompleteAnimation(int numFlashes) {
        String message = pickerLevelComplete.next() + "\n\n" + context.locText("level_complete", context.game().levelNumber());
        return new SequentialTransition(
              now(() -> {
                  perspectivePy.unbind();
                  perspectivePy.set(Perspective.TOTAL);
                  level3D.livesCounter3D().light().setLightOn(false);
                  context.showFlashMessageSeconds(3, message);
              })
            , doAfterSec(2, level3D.mazeFlashAnimation(numFlashes))
            , doAfterSec(1, () -> {
                context.game().pac().hide();
                GameSounds.playLevelCompleteSound();
            })
            , doAfterSec(0.5, level3D.levelRotateAnimation(1.5))
            , level3D.wallsDisappearAnimation(2.0)
            , doAfterSec(1, () -> {
                GameSounds.playLevelChangedSound();
                perspectivePy.bind(PY_3D_PERSPECTIVE);
            })
        );
    }
}