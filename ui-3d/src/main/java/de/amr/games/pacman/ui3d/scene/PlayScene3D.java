/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.scene;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.GameKeys;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.PacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui3d.entity.Bonus3D;
import de.amr.games.pacman.ui3d.entity.Eatable3D;
import de.amr.games.pacman.ui3d.entity.GameLevel3D;
import de.amr.games.pacman.ui3d.entity.Scores3D;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.List;
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
public class PlayScene3D implements GameScene {

    private static final byte CHILD_INDEX_LEVEL_3D = 0;

    public final ObjectProperty<Perspective> perspectivePy = new SimpleObjectProperty<>(this, "perspective") {
        @Override
        protected void invalidated() {
            get().init(fxSubScene.getCamera(), context.game().world());
        }
    };

    private final SubScene fxSubScene;
    private final Scores3D scores3D;

    private GameLevel3D level3D;
    private GameContext context;

    public PlayScene3D() {
        var camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);

        var ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PY_3D_LIGHT_COLOR);

        var coordSystem = new CoordSystem();
        coordSystem.visibleProperty().bind(PY_3D_AXES_VISIBLE);

        scores3D = new Scores3D("SCORE", "HIGH SCORE");
        // keep the scores rotated such that the viewer always sees them frontally
        scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
        scores3D.rotateProperty().bind(camera.rotateProperty());

        var level3DPlaceholder = new Group();
        var root = new Group(level3DPlaceholder, scores3D,  coordSystem, ambientLight);

        // initial scene size is irrelevant, gets bound to parent scene later
        fxSubScene = new SubScene(root, 42, 42, true, SceneAntialiasing.BALANCED);
        fxSubScene.setFill(null); // transparent
        fxSubScene.setCamera(camera);
    }

    public void setParentScene(Scene parentScene) {
        fxSubScene.widthProperty().bind(parentScene.widthProperty());
        fxSubScene.heightProperty().bind(parentScene.heightProperty());
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
        if (game.level().isEmpty()) {
            Logger.warn("Cannot update 3D play scene, no game level exists");
            return;
        }
        if (level3D != null) {
            level3D.update();
            perspective().update(fxSubScene.getCamera(), game.world(), game.pac());
        } else {
            // if level has been started in 2D scene and user switches to 3D scene, the 3D level must be created
            replaceGameLevel3D();
        }

        // Update autopilot here. Autopilot flag is in core layer where we don't have a JavaFX property to bind with
        game.pac().setUseAutopilot(game.isDemoLevel() || PY_AUTOPILOT.get());

        scores3D.showHighScore(game.highScore().points(), game.highScore().levelNumber());
        if (context.gameController().hasCredit()) {
            scores3D.showScore(game.score().points(), game.score().levelNumber());
        } else {
            // demo level or "game over" state
            scores3D.showTextAsScore("GAME OVER!", Color.RED);
        }
        updateSound();
    }

    @Override
    public void setContext(GameContext context) {
        this.context = checkNotNull(context);
    }

    @Override
    public GameContext context() {
        return context;
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
    public void onSceneVariantSwitch() {
        World world = context.game().world();
        if (level3D == null) {
            replaceGameLevel3D();
        }
        level3D.pellets3D().forEach(pellet3D -> pellet3D.root().setVisible(!world.hasEatenFoodAt(pellet3D.tile())));
        level3D.energizers3D().forEach(energizer3D -> energizer3D.root().setVisible(!world.hasEatenFoodAt(energizer3D.tile())));
        if (oneOf(context.gameState(), GameState.HUNTING, GameState.GHOST_DYING)) {
            level3D.startEnergizerAnimation();
        }
        context.game().pac().show();
        context.game().ghosts().forEach(Ghost::show);
        level3D.pac3D().init(context);
        level3D.pac3D().update(context);
        if (!context.game().isDemoLevel() && context.gameState() == GameState.HUNTING) {
            context.soundHandler().ensureSirenStarted(context.game().huntingPhaseIndex() / 2);
        }
    }

    @Override
    public void onGameStateEntry(GameState state) {
        switch (state) {

            case READY -> {
                context.soundHandler().stopAllSounds();
                if (level3D != null) {
                    level3D.pac3D().init(context);
                    level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context));
                    level3D.stopEnergizerAnimation();
                    level3D.bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
                    level3D.livesCounter3D().stopAnimation();
                    showLevelMessage();
                }
            }

            case HUNTING -> {
                level3D.pac3D().init(context);
                level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context));
                level3D.livesCounter3D().startAnimation();
                level3D.startEnergizerAnimation();
            }

            case PACMAN_DYING -> {
                context.soundHandler().stopAllSounds();
                var animation = level3D.pac3D().createDyingAnimation(context);
                lockGameStateAndPlayAfterOneSecond(animation);
            }

            case GAME_OVER -> {
                context.soundHandler().stopAllSounds();
                context.gameState().timer().restartSeconds(3);
                level3D.stopEnergizerAnimation();
                level3D.bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
                level3D.livesCounter3D().stopAnimation();
                context.actionHandler().showFlashMessageSeconds(3, PICKER_GAME_OVER.next());
                context.soundHandler().playAudioClip("audio.game_over");
            }

            case GHOST_DYING -> {
                switch (context.game().variant()) {
                    case MS_PACMAN -> {
                        var ss = (MsPacManGameSpriteSheet) context.getSpriteSheet(context.game().variant());
                        Rectangle2D[] numberSprites = ss.ghostNumberSprites();
                        context.game().eventLog().killedGhosts.forEach(ghost -> {
                            int index = context.game().victims().indexOf(ghost);
                            var numberImage = ss.subImage(numberSprites[index]);
                            level3D.ghost3D(ghost.id()).setNumberImage(numberImage);
                        });
                    }
                    case PACMAN, PACMAN_XXL -> {
                        var ss = (PacManGameSpriteSheet) context.getSpriteSheet(context.game().variant());
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
                playLevelCompleteAnimation();
            }

            case LEVEL_TRANSITION -> {
                context.gameState().timer().restartSeconds(3);
                replaceGameLevel3D();
                level3D.pac3D().init(context);
                perspective().init(fxSubScene.getCamera(), context.game().world());
            }

            case LEVEL_TEST -> {
                if (level3D == null) {
                    replaceGameLevel3D();
                }
                level3D.pac3D().init(context);
                level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context));
                showLevelMessage();
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
        replaceGameLevel3D();
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        if (context.game().levelNumber() == 1 || context.gameState() == GameState.LEVEL_TEST) {
            showLevelMessage();
        }
        level3D.createLevelCounter3D();
        if (PY_3D_FLOOR_TEXTURE_RND.get()) {
            List<String> names = context.theme().getMap("floorTextures").keySet().stream().toList();
            PY_3D_FLOOR_TEXTURE.set(names.get(randomInt(0, names.size())));
        }
    }

    @Override
    public void onPacFoundFood(GameEvent event) {
        World world = context.game().world();
        // When cheat "eat all pellets" has been used, no tile is present in the event.
        // In that case, ensure that the 3D pellets are in sync with the model.
        if (event.tile().isEmpty()) {
            world.tiles()
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

    private void replaceGameLevel3D() {
        // Might be called before the world has been created
        if (context.game().world() == null) {
            Logger.warn("Cannot create 3D level, world not yet created");
            return;
        }

        level3D = new GameLevel3D(context);

        scores3D.translateXProperty().bind(level3D.translateXProperty().add(TS));
        scores3D.translateYProperty().bind(level3D.translateYProperty().subtract(3.5 * TS));
        scores3D.translateZProperty().bind(level3D.translateZProperty().subtract(3 * TS));

        // replace initial placeholder or previous 3D level
        var root = (Group) fxSubScene.getRoot();
        root.getChildren().set(CHILD_INDEX_LEVEL_3D, level3D);

        Logger.info("3D game level {} created.", context.game().levelNumber());
    }

    private void showLevelMessage() {
        World world = context.game().world();
        checkNotNull(world);
        if (context.gameState() == GameState.LEVEL_TEST) {
            level3D.showMessage("TEST LEVEL " + context.game().levelNumber(), 5,
                world.numCols() * HTS, (world.numRows() - 2) * TS);
        } else if (!context.game().isDemoLevel()) {
            var house = world.house();
            double x = TS * (house.topLeftTile().x() + 0.5 * house.size().x());
            double y = TS * (house.topLeftTile().y() +       house.size().y());
            double seconds = context.game().isPlaying() ? 0.5 : 2.5;
            level3D.showMessage("READY!", seconds, x, y);
        }
    }

    private void playLevelCompleteAnimation() {
        boolean intermissionFollows = context.game().intermissionNumberAfterLevel(context.game().levelNumber()) != 0;
        int nunFlashes = context.game().level().orElseThrow().numFlashes();
        lockGameStateAndPlayAfterOneSecond(intermissionFollows
            ? levelCompleteAnimationBeforeIntermission(nunFlashes)
            : levelCompleteAnimation(nunFlashes));
    }

    private Animation levelCompleteAnimation(int numFlashes) {
        final Perspective perspectiveBeforeAnimation = perspective();
        var mazeFlashes = level3D.createMazeFlashAnimation(numFlashes,level3D.wallHeightPy.get());
        var mazeRotates = level3D.createMazeRotateAnimation(1.5);
        var wallsDisappear = level3D.createWallsDisappearAnimation(1.5);
        return new SequentialTransition(
            now(() -> PY_3D_PERSPECTIVE.set(Perspective.TOTAL))
            , pauseSec(1)
            , mazeFlashes
            , pauseSec(2.5)
            , now(() -> {
                context.game().pac().hide();
                context.soundHandler().playAudioClip("audio.level_complete");
            })
            , mazeRotates
            , wallsDisappear
            , doAfterSec(1.5, () -> {
                PY_3D_PERSPECTIVE.set(perspectiveBeforeAnimation);
                context.soundHandler().playAudioClip("audio.sweep");
                context.actionHandler().showFlashMessageSeconds(1, pickLevelCompleteMessage());
            })
        );
    }

    private Animation levelCompleteAnimationBeforeIntermission(int numFlashes) {
        return new SequentialTransition(
             pauseSec(1)
            , level3D.createMazeFlashAnimation(numFlashes, level3D.wallHeightPy.get())
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

    private void updateSound() {
        if (context.game().isDemoLevel()) {
            return;
        }
        if (context.game().pac().starvingTicks() > 8) { // TODO not sure how this is done in Arcade game
            context.soundHandler().stopAudioClip("audio.pacman_munch");
        }
        if (context.game().pac().isAlive()
            && context.game().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible)) {
            context.soundHandler().ensureAudioLoop("audio.ghost_returning");
        } else {
            context.soundHandler().stopAudioClip("audio.ghost_returning");
        }
    }
}