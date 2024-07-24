/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.scene;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.GameKeys;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.PacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.PlaySceneSound;
import de.amr.games.pacman.ui3d.level.*;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.PacManGames2dUI.PY_AUTOPILOT;
import static de.amr.games.pacman.ui2d.util.Ufx.*;
import static de.amr.games.pacman.ui3d.PacManGames3dUI.*;

/**
 * 3D play scene.
 *
 * <p>Provides different camera perspectives that can be selected sequentially using keys <code>Alt+LEFT</code>
 * and <code>Alt+RIGHT</code>.</p>
 *
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene, PlaySceneSound {

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

    public PlayScene3D() {
        var ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PY_3D_LIGHT_COLOR);

        var coordSystem = new CoordSystem();
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

    @Override
    public void init() {
        context.setScoreVisible(true);
        scores3D.fontPy.set(context.theme().font("font.arcade", 8));
        perspectivePy.bind(PY_3D_PERSPECTIVE);
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
            Logger.warn("Cannot update 3D play scene, no game level exists");
            return;
        }

        // If level started in 2D and user switches to 3D scene, the 3D level must be created
        if (level3D == null) {
            replaceGameLevel3D(true);
        }
        level3D.update();

        // Update camera
        perspective().update(fxSubScene.getCamera(), game.world(), game.pac());

        // Autopilot usage is updated on every frame because autopilot flag is in core layer without JavaFX property to bind
        game.pac().setUseAutopilot(game.isDemoLevel() || PY_AUTOPILOT.get());

        scores3D.showHighScore(game.highScore().points(), game.highScore().levelNumber());
        if (context.gameController().hasCredit()) {
            scores3D.showScore(game.score().points(), game.score().levelNumber());
        } else { // demo level or "game over" state
            scores3D.showTextAsScore("GAME OVER!", Color.RED);
        }

        updateSound(context);
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
    public void handleKeyboardInput() {
        if (GameKeys.ADD_CREDIT.pressed() && !context.gameController().hasCredit()) {
            context.actionHandler().addCredit();
        } else if (GameKeys.PREV_PERSPECTIVE.pressed()) {
            context.actionHandler().selectPrevPerspective();
        } else if (GameKeys.NEXT_PERSPECTIVE.pressed()) {
            context.actionHandler().selectNextPerspective();
        } else if (GameKeys.CHEAT_EAT_ALL.pressed()) {
            context.actionHandler().cheatEatAllPellets();
        } else if (GameKeys.CHEAT_ADD_LIVES.pressed()) {
            context.actionHandler().cheatAddLives();
        } else if (GameKeys.CHEAT_NEXT_LEVEL.pressed()) {
            context.actionHandler().cheatEnterNextLevel();
        } else if (GameKeys.CHEAT_KILL_GHOSTS.pressed()) {
            context.actionHandler().cheatKillAllEatableGhosts();
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
        context.soundHandler().stopAllSounds();
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
        context.soundHandler().stopAllSounds();
        // last update before dying animation
        level3D.pac3D().updateAlive();
        playPacManDiesAnimation();
    }

    private void onEnterStateGhostDying() {
        switch (context.game().variant()) {
            case MS_PACMAN -> {
                var ss = (MsPacManGameSpriteSheet) context.spriteSheet(context.game().variant());
                Rectangle2D[] numberSprites = ss.ghostNumberSprites();
                context.game().eventLog().killedGhosts.forEach(ghost -> {
                    int index = context.game().victims().indexOf(ghost);
                    var numberImage = ss.subImage(numberSprites[index]);
                    level3D.ghost3D(ghost.id()).setNumberImage(numberImage);
                });
            }
            case PACMAN, PACMAN_XXL -> {
                var ss = (PacManGameSpriteSheet) context.spriteSheet(context.game().variant());
                Rectangle2D[] numberSprites = ss.ghostNumberSprites();
                context.game().eventLog().killedGhosts.forEach(ghost -> {
                    int index = context.game().victims().indexOf(ghost);
                    var numberImage = ss.subImage(numberSprites[index]);
                    level3D.ghost3D(ghost.id()).setNumberImage(numberImage);
                });
            }
        }
    }

    private void onEnterStateLevelComplete() {
        context.soundHandler().stopAllSounds();
        // if cheat has been used to complete level, 3D food might still exist, so eat it:
        level3D.pellets3D().forEach(Pellet3D::onEaten);
        level3D.energizers3D().forEach(Energizer3D::onEaten);
        level3D.livesCounter3D().stopAnimation();
        level3D.door3D().setVisible(false);
        playLevelCompleteAnimation();
    }

    private void onEnterStateLevelTransition() {
        context.gameState().timer().restartSeconds(3);
        replaceGameLevel3D(true);
        level3D.pac3D().init();
        perspective().init(fxSubScene.getCamera(), context.game().world());
    }

    private void onEnterStateLevelTest() {
        if (level3D == null) {
            replaceGameLevel3D(true);
        }
        level3D.pac3D().init();
        level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context));
        showLevelTestMessage();
        PY_3D_PERSPECTIVE.set(Perspective.TOTAL);
    }

    private void onEnterStateGameOver() {
        stopLevelAnimations();
        // delay state exit for 3 seconds
        context.gameState().timer().restartSeconds(3);
        context.actionHandler().showFlashMessageSeconds(3, PICKER_GAME_OVER.next());
        context.soundHandler().stopAllSounds();
        context.soundHandler().playAudioClip("audio.game_over");
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
        level3D.pac3D().updateAlive();

        if (context.game().isDemoLevel()) {
            return; // no sound in demo level
        }

        if (context.gameState() == GameState.HUNTING) {
            if (context.game().powerTimer().isRunning()) {
                context.soundHandler().playPowerSound();
            } else {
                context.soundHandler().ensureSirenPlaying(context.game().huntingPhaseIndex() / 2);
            }
        }
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        context.game().bonus().ifPresent(level3D::replaceBonus3D);
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::showEaten);
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::onBonusExpired);
    }

    @Override
    public void onLevelCreated(GameEvent event) {
        replaceGameLevel3D(false); // level counter in model not yet initialized
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        level3D.addLevelCounter3D(context.game().levelCounter());
        if (context.game().levelNumber() == 1 || context.gameState() == GameState.LEVEL_TEST) {
            if (context.gameState() == GameState.LEVEL_TEST) {
                showLevelTestMessage();
            } else if (!context.game().isDemoLevel()){
                showReadyMessage();
            }
        }
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
    }

    @Override
    public void onPacGetsPower(GameEvent event) {
        level3D.pac3D().setPower(true);
    }

    @Override
    public void onPacLostPower(GameEvent event) {
        level3D.pac3D().setPower(false);
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

    private void playPacManDiesAnimation() {
        Animation animation = level3D.pac3D().createDyingAnimation();
        animation.setDelay(Duration.seconds(1));
        animation.setOnFinished(e -> context.gameState().timer().expire());
        context.gameState().timer().resetIndefinitely();
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
            , level3D.createMazeFlashAnimation(numFlashes)
            , doAfterSec(2.5, () -> context.game().pac().hide())
        );
    }

    private Animation levelCompleteAnimation(int numFlashes) {
        Animation mazeFlashes    = level3D.createMazeFlashAnimation(numFlashes);
        Animation mazeRotates    = level3D.createMazeRotateAnimation(1.5);
        Animation mazeDisappears = level3D.createWallsDisappearAnimation(1.0);
        String message = PICKER_LEVEL_COMPLETE.next() + "\n\n" + context.tt("level_complete", context.game().levelNumber());
        //TODO is there are better way to do this e.g. using a TimeLine?
        return new SequentialTransition(
              now(() -> { perspectivePy.unbind(); perspectivePy.set(Perspective.TOTAL); })
            , pauseSec(2)
            , mazeFlashes
            , pauseSec(1)
            , now(() -> { context.game().pac().hide(); context.soundHandler().playAudioClip("audio.level_complete"); })
            , pauseSec(0.5)
            , mazeRotates
            , mazeDisappears
            , doAfterSec(1, () -> {
                context.soundHandler().playAudioClip("audio.sweep");
                context.actionHandler().showFlashMessageSeconds(1, message);
                perspectivePy.bind(PY_3D_PERSPECTIVE);
            })
        );
    }
}