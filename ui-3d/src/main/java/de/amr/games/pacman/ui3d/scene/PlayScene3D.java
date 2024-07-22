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
import de.amr.games.pacman.ui3d.entity.Bonus3D;
import de.amr.games.pacman.ui3d.entity.Eatable3D;
import de.amr.games.pacman.ui3d.entity.GameLevel3D;
import de.amr.games.pacman.ui3d.entity.Scores3D;
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
    private final AmbientLight ambientLight;
    private final CoordSystem coordSystem;
    private final Scores3D scores3D;

    private GameLevel3D level3D;
    private GameContext context;

    public PlayScene3D() {
        ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PY_3D_LIGHT_COLOR);

        coordSystem = new CoordSystem();
        coordSystem.visibleProperty().bind(PY_3D_AXES_VISIBLE);

        scores3D = new Scores3D("SCORE", "HIGH SCORE");

        // initial size is irrelevant as it is bound to parent scene later
        root.getChildren().setAll(scores3D, coordSystem, ambientLight);
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
        if (level3D == null) {
            // if level has been started in 2D scene and user switches to 3D scene, the 3D level must be created
            replaceGameLevel3D(true);
        }
        level3D.update();
        perspective().update(fxSubScene.getCamera(), game.world(), game.pac());

        // Update autopilot usage on every update because autopilot flag is in core layer where we don't have a JavaFX property to bind with
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
    public void onSceneVariantSwitch(GameScene oldScene) {
        Logger.info("{} entered from {}", this, oldScene);
        if (level3D == null) {
            replaceGameLevel3D(true);
        }
        level3D.updateFood();
        if (oneOf(context.gameState(), GameState.HUNTING, GameState.GHOST_DYING)) {
            level3D.startEnergizerAnimation();
        }
        context.game().pac().show();
        context.game().ghosts().forEach(Ghost::show);
        level3D.pac3D().init();
        level3D.pac3D().updateAlive();
        ensureSirenPlaying(context);
        if (!context.game().isDemoLevel() && context.gameState() == GameState.HUNTING) {
            context.soundHandler().ensureSirenPlaying(context.game().huntingPhaseIndex() / 2);
        }
    }

    @Override
    public void onGameStateEntry(GameState state) {
        switch (state) {

            case READY -> {
                context.soundHandler().stopAllSounds();
                if (level3D != null) {
                    showReadyMessage();
                    level3D.getReadyToPlay();
                }
            }

            case HUNTING -> level3D.startHunting();

            case PACMAN_DYING -> {
                context.soundHandler().stopAllSounds();
                context.gameState().timer().resetIndefinitely();
                // last update before dying animation
                level3D.pac3D().updateAlive();
                Animation dying = level3D.pac3D().createDyingAnimation();
                dying.setDelay(Duration.seconds(1));
                dying.setOnFinished(e -> context.gameState().timer().expire());
                dying.play();
            }

            case GHOST_DYING -> {
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

            case LEVEL_COMPLETE -> {
                context.soundHandler().stopAllSounds();
                // if cheat has been used to complete level, 3D food might still exist:
                level3D.pellets3D().forEach(level3D::eat);
                level3D.energizers3D().forEach(level3D::eat);
                level3D.livesCounter3D().stopAnimation();
                level3D.door3D().setVisible(false);
                playLevelCompleteAnimation();
            }

            case LEVEL_TRANSITION -> {
                context.gameState().timer().restartSeconds(3);
                replaceGameLevel3D(true);
                level3D.pac3D().init();
                perspective().init(fxSubScene.getCamera(), context.game().world());
            }

            case GAME_OVER -> {
                // delay state exit for 3 seconds
                context.gameState().timer().restartSeconds(3);
                context.actionHandler().showFlashMessageSeconds(3, PICKER_GAME_OVER.next());
                context.soundHandler().stopAllSounds();
                context.soundHandler().playAudioClip("audio.game_over");
                level3D.stopHunting();
            }

            case LEVEL_TEST -> {
                if (level3D == null) {
                    replaceGameLevel3D(true);
                }
                level3D.pac3D().init();
                level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context));
                showLevelTestMessage();
                PY_3D_PERSPECTIVE.set(Perspective.TOTAL);
            }

            default -> {}
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
        // When cheat "eat all pellets" has been used, no tile is present in the event.
        // In that case, ensure that the 3D pellets are in sync with the model.
        if (event.tile().isEmpty()) {
            world.map().food().tiles()
                .filter(world::hasEatenFoodAt)
                .map(level3D::pellet3D)
                .flatMap(Optional::stream)
                .forEach(Eatable3D::onEaten);
        } else {
            Vector2i tile = event.tile().get();
            level3D.energizer3D(tile).ifPresent(level3D::eat);
            level3D.pellet3D(tile).ifPresent(level3D::eat);
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
        root.getChildren().setAll(scores3D, coordSystem, ambientLight, level3D);

        scores3D.translateXProperty().bind(level3D.translateXProperty().add(TS));
        scores3D.translateYProperty().bind(level3D.translateYProperty().subtract(3.5 * TS));
        scores3D.translateZProperty().bind(level3D.translateZProperty().subtract(3 * TS));

        Logger.info("3D game level {} created.", context.game().levelNumber());
    }

    private void playLevelCompleteAnimation() {
        boolean intermission = context.game().intermissionNumber(context.game().levelNumber()) != 0;
        lockGameStateAndPlayAfterOneSecond(intermission
            ? levelCompleteAnimationBeforeIntermission()
            : levelCompleteAnimation());
    }

    private Animation levelCompleteAnimation() {
        int numFlashes = context.game().level().orElseThrow().numFlashes();
        Perspective perspectiveBeforeAnimation = perspective();
        Animation mazeFlashes = level3D.createMazeFlashAnimation(numFlashes);
        Animation mazeRotates = level3D.createMazeRotateAnimation(1.5);
        Animation wallsDisappear = level3D.createWallsDisappearAnimation(1.0);
        //TODO is there are better way to do this?
        return new SequentialTransition(
            now(() -> PY_3D_PERSPECTIVE.set(Perspective.TOTAL))
            , pauseSec(2)
            , mazeFlashes
            , pauseSec(2)
            , now(() -> context.game().pac().hide())
            , now(() -> context.soundHandler().playAudioClip("audio.level_complete"))
            , pauseSec(1)
            , mazeRotates
            , wallsDisappear
            , doAfterSec(1, () -> {
                PY_3D_PERSPECTIVE.set(perspectiveBeforeAnimation);
                context.soundHandler().playAudioClip("audio.sweep");
                context.actionHandler().showFlashMessageSeconds(1, pickLevelCompleteMessage());
            })
        );
    }

    private Animation levelCompleteAnimationBeforeIntermission() {
        int numFlashes = context.game().level().orElseThrow().numFlashes();
        return new SequentialTransition(
             pauseSec(1)
            , level3D.createMazeFlashAnimation(numFlashes)
            , doAfterSec(2.5, () -> context.game().pac().hide())
        );
    }

    private void lockGameStateAndPlayAfterOneSecond(Animation animation) {
        context.gameState().timer().resetIndefinitely();
        animation.setDelay(Duration.seconds(1.0));
        animation.setOnFinished(e -> context.gameState().timer().expire());
        animation.play();
    }

    private String pickLevelCompleteMessage() {
        return PICKER_LEVEL_COMPLETE.next() + "\n\n" + context.tt("level_complete", context.game().levelNumber());
    }
}