/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.scene3d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.GameScene;
import de.amr.games.pacman.ui.fx.GameSceneContext;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import de.amr.games.pacman.ui.fx.v3d.ActionHandler3D;
import de.amr.games.pacman.ui.fx.v3d.entity.*;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
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
import static de.amr.games.pacman.ui.fx.PacManGames2dUI.*;
import static de.amr.games.pacman.ui.fx.util.Ufx.*;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.*;

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
            perspectivePy.get().init(fxSubScene.getCamera());
        }
    };

    private final Group subSceneRoot = new Group();
    private final SubScene fxSubScene;
    private final Scores3D scores3D;

    private GameLevel3D level3D;
    private GameSceneContext context;
    private boolean scoreVisible;

    public PlayScene3D() {
        // initial scene size is irrelevant, gets bound to parent scene later
        fxSubScene = new SubScene(subSceneRoot, 42, 42, true, SceneAntialiasing.BALANCED);
        fxSubScene.setFill(null); // transparent

        var camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        fxSubScene.setCamera(camera);

        var ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PY_3D_LIGHT_COLOR);

        var coordSystem = new CoordSystem();
        coordSystem.visibleProperty().bind(PY_3D_AXES_VISIBLE);

        scores3D = new Scores3D();
        // keep the scores rotated such that the viewer always sees them frontally
        scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
        scores3D.rotateProperty().bind(camera.rotateProperty());

        // first child is placeholder for game level 3D
        subSceneRoot.getChildren().addAll(new Group(), scores3D,  coordSystem, ambientLight);

        Logger.info("3D play scene created. {}", this);
    }

    public void setParentScene(Scene parentScene) {
        fxSubScene.widthProperty().bind(parentScene.widthProperty());
        fxSubScene.heightProperty().bind(parentScene.heightProperty());
        Logger.info("3D play scene embedded. {}", this);
    }

    @Override
    public void init() {
        setScoreVisible(true);
        scores3D.fontPy.set(context.theme().font("font.arcade", 8));
        perspectivePy.bind(PY_3D_PERSPECTIVE);
        Logger.info("3D play scene initialized. {}", this);
    }

    @Override
    public void end() {
        perspectivePy.unbind();
        Logger.info("3D play scene ended. {}", this);
    }

    @Override
    public void update() {
        if (context.game().level().isEmpty()) {
            Logger.info("Cannot update 3D play scene, no game level exists");
            return;
        }
        if (level3D != null) {
            level3D.update();
            perspective().update(fxSubScene.getCamera(), context.game().pac());
        }
        context.game().pac().setUseAutopilot(context.game().isDemoLevel() || PY_USE_AUTOPILOT.get());
        updateSound();
        scores3D.setScores(
            context.game().score().points(), context.game().score().levelNumber(),
            context.game().highScore().points(), context.game().highScore().levelNumber());
        if (context.gameController().hasCredit()) {
            scores3D.showScore();
        } else {
            scores3D.showText(Color.RED, "GAME OVER!");
        }
    }

    @Override
    public void setContext(GameSceneContext context) {
        checkNotNull(context);
        this.context = context;
    }

    @Override
    public GameSceneContext context() {
        return context;
    }

    @Override
    public boolean isScoreVisible() {
        return scoreVisible;
    }

    @Override
    public void setScoreVisible(boolean scoreVisible) {
        this.scoreVisible = scoreVisible;
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

    private void replaceGameLevel3D() {
        World world = context.game().world();
        checkNotNull(world);

        level3D = new GameLevel3D(context);
        // replace initial placeholder or previous 3D level
        subSceneRoot.getChildren().set(CHILD_INDEX_LEVEL_3D, level3D);

        level3D.setTranslateX(-world.numCols() * HTS);
        level3D.setTranslateY(-world.numRows() * HTS);

        scores3D.setTranslateX(level3D.getTranslateX() + TS);
        scores3D.setTranslateY(level3D.getTranslateY() -3.5 * TS);
        scores3D.setTranslateZ(-3 * TS);

        if (PY_3D_FLOOR_TEXTURE_RND.get()) {
            List<String> names = context.theme().getArray("texture.names");
            PY_3D_FLOOR_TEXTURE.set(names.get(randomInt(0, names.size())));
        }
        Logger.info("3D game level {} created.", context.game().levelNumber());
    }

    @Override
    public void handleKeyboardInput() {
        var actionHandler = (ActionHandler3D) context.actionHandler();
        if (Keyboard.pressed(KEYS_ADD_CREDIT) && !context.gameController().hasCredit()) {
            actionHandler.addCredit();
        } else if (Keyboard.pressed(KEY_PREV_PERSPECTIVE)) {
            actionHandler.selectPrevPerspective();
        } else if (Keyboard.pressed(KEY_NEXT_PERSPECTIVE)) {
            actionHandler.selectNextPerspective();
        } else if (Keyboard.pressed(KEY_CHEAT_EAT_ALL)) {
            actionHandler.cheatEatAllPellets();
        } else if (Keyboard.pressed(KEY_CHEAT_ADD_LIVES)) {
            actionHandler.cheatAddLives();
        } else if (Keyboard.pressed(KEY_CHEAT_NEXT_LEVEL)) {
            actionHandler.cheatEnterNextLevel();
        } else if (Keyboard.pressed(KEY_CHEAT_KILL_GHOSTS)) {
            actionHandler.cheatKillAllEatableGhosts();
        }
    }

    @Override
    public void onSceneVariantSwitch() {
        World world = context.game().world();
        if (level3D == null) {
            replaceGameLevel3D();
        }
        level3D.allEatables().forEach(
            eatable3D -> eatable3D.root().setVisible(!world.hasEatenFoodAt(eatable3D.tile())));
        if (oneOf(context.gameState(), GameState.HUNTING, GameState.GHOST_DYING)) {
            level3D.startEnergizerAnimation();
        }
        context.game().pac().show();
        context.game().ghosts().forEach(Ghost::show);
        level3D.pac3D().init(context.game());
        level3D.pac3D().update(context.game());
        if (!context.game().isDemoLevel() && context.gameState() == GameState.HUNTING) {
            context.ensureSirenStarted(context.game().huntingPhaseIndex() / 2);
        }
    }

    @Override
    public void onGameStateEntry(GameState state) {
        switch (state) {

            case READY -> {
                context.stopAllSounds();
                if (level3D != null) {
                    level3D.pac3D().init(context.game());
                    level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context.game()));
                    level3D.stopEnergizerAnimation();
                    level3D.bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
                    level3D.livesCounter3D().stopAnimation();
                    showLevelMessage();
                }
            }

            case HUNTING -> {
                assertLevel3DExists();
                level3D.pac3D().init(context.game());
                level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context.game()));
                level3D.livesCounter3D().startAnimation();
                level3D.startEnergizerAnimation();
            }

            case PACMAN_DYING -> {
                assertLevel3DExists();
                context.stopAllSounds();
                var animation = switch (context.game()) {
                    case GameVariant.MS_PACMAN -> level3D.pac3D().createMsPacManDyingAnimation();
                    case GameVariant.PACMAN -> level3D.pac3D().createPacManDyingAnimation(context.game());
                    default -> throw new IllegalGameVariantException(context.game());
                };
                lockGameStateAndPlayAfterOnSecond(animation);
            }

            case GAME_OVER -> {
                assertLevel3DExists();
                context.stopAllSounds();
                context.gameState().timer().restartSeconds(3);
                level3D.stopEnergizerAnimation();
                level3D.bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
                level3D.livesCounter3D().stopAnimation();
                context.actionHandler().showFlashMessageSeconds(3, PICKER_GAME_OVER.next());
                context.playAudioClip("audio.game_over");
            }

            case GHOST_DYING -> {
                assertLevel3DExists();
                Rectangle2D[] sprites = switch (context.game()) {
                    case GameVariant.MS_PACMAN -> context.<MsPacManGameSpriteSheet>spriteSheet().ghostNumberSprites();
                    case GameVariant.PACMAN    -> context.<PacManGameSpriteSheet>spriteSheet().ghostNumberSprites();
                    default -> throw new IllegalGameVariantException(context.game());
                };
                context.game().eventLog().killedGhosts.forEach(ghost -> {
                    int index = context.game().victims().indexOf(ghost);
                    var numberImage = context.spriteSheet().subImage(sprites[index]);
                    level3D.ghosts3D().get(ghost.id()).setNumberImage(numberImage);
                });
            }

            case LEVEL_COMPLETE -> {
                assertLevel3DExists();
                context.stopAllSounds();
                // if cheat has been used to complete level, 3D food might still exist:
                level3D.allEatables().forEach(level3D::eat);
                level3D.livesCounter3D().stopAnimation();
                playLevelCompleteAnimation(context.game().level().orElseThrow());
            }

            case LEVEL_TRANSITION -> {
                assertLevel3DExists();
                context.gameState().timer().restartSeconds(3);
                replaceGameLevel3D();
                level3D.pac3D().init(context.game());
                perspective().init(fxSubScene.getCamera());
            }

            case LEVEL_TEST -> {
                PY_3D_PERSPECTIVE.set(Perspective.TOTAL);
                replaceGameLevel3D();
                level3D.pac3D().init(context.game());
                level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(context.game()));
                showLevelMessage();
            }

            default -> {}
        }
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        assertLevel3DExists();
        context.game().bonus().ifPresent(level3D::replaceBonus3D);
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        assertLevel3DExists();
        level3D.bonus3D().ifPresent(Bonus3D::showEaten);
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        assertLevel3DExists();
        level3D.bonus3D().ifPresent(Bonus3D::onBonusExpired);
    }

    @Override
    public void onLevelCreated(GameEvent event) {
        if (context.game().isDemoLevel() || context.game().levelNumber() == 1 || context.gameState() == GameState.LEVEL_TEST) {
            replaceGameLevel3D();
        }
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        assertLevel3DExists();
        if (context.game().levelNumber() == 1 || context.gameState() == GameState.LEVEL_TEST) {
            showLevelMessage();
        }
        level3D.createLevelCounter3D();
    }

    @Override
    public void onPacFoundFood(GameEvent event) {
        assertLevel3DExists();
        World world = context.game().world();
        // When cheat "eat all pellets" has been used, no tile is present in the event.
        // In that case, ensure that the 3D pellets are in sync with the model.
        if (event.tile().isEmpty()) {
            world.tiles()
                .filter(world::hasEatenFoodAt)
                .map(level3D::eatableAt)
                .flatMap(Optional::stream)
                .forEach(Eatable3D::onEaten);
        } else {
            Vector2i tile = event.tile().get();
            level3D.eatableAt(tile).ifPresent(level3D::eat);
        }
    }

    @Override
    public void onPacGetsPower(GameEvent event) {
        assertLevel3DExists();
        level3D.pac3D().walkingAnimation().setPowerWalking(true);
    }

    @Override
    public void onPacLostPower(GameEvent event) {
        assertLevel3DExists();
        level3D.pac3D().walkingAnimation().setPowerWalking(false);
    }

    private void assertLevel3DExists() {
        if (level3D == null) {
            throw new IllegalStateException("No 3D level exists!");
        }
    }

    private void showLevelMessage() {
        if (context.game().level().isEmpty()) {
            Logger.info("Cannot show level message, level is NULL");
            return;
        }
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

    private void playLevelCompleteAnimation(GameLevel level) {
        boolean noIntermission = level.intermissionNumber() == 0;
        context.actionHandler().showFlashMessageSeconds(2, pickLevelCompleteMessage(context.game().levelNumber()));
        var animation = new SequentialTransition(
            pauseSeconds(1),
            level3D.createLevelCompleteAnimation(context.game().level().orElseThrow().numFlashes()),
            doAfterSeconds(1.0, () -> {
                context.game().pac().hide();
                level3D.livesCounter3D().lightOnPy.set(false);
                if (noIntermission) {
                    context.playAudioClip("audio.level_complete");
                }
            }),
            noIntermission ? createLevelChangeAnimation() : pauseSeconds(0),
            doNow(() -> level3D.livesCounter3D().lightOnPy.set(true))
        );
        lockGameStateAndPlayAfterOnSecond(animation);
    }

    private String pickLevelCompleteMessage(int levelNumber) {
        return PICKER_LEVEL_COMPLETE.next() + "\n\n" + context.tt("level_complete", levelNumber);
    }

    private Transition createLevelChangeAnimation() {
        return new SequentialTransition(
            doNow(() -> {
                perspectivePy.unbind();
                perspectivePy.set(Perspective.TOTAL);
            }),
            level3D.createLevelRotateAnimation(),
            doAfterSeconds(0.5, () -> context.playAudioClip("audio.sweep")),
            doAfterSeconds(0.5, () -> perspectivePy.bind(PY_3D_PERSPECTIVE))
        );
    }

    private void updateSound() {
        if (context.game().isDemoLevel()) {
            return;
        }
        if (context.game().pac().starvingTicks() > 8) { // TODO not sure how this is done in Arcade game
            context.stopAudioClip("audio.pacman_munch");
        }
        if (context.game().pac().isAlive() && context.game().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
            .anyMatch(Ghost::isVisible)) {
            context.ensureAudioLoop("audio.ghost_returning");
        } else {
            context.stopAudioClip("audio.ghost_returning");
        }
    }

    private void lockGameStateAndPlayAfterOnSecond(Animation animation) {
        context.gameState().timer().resetIndefinitely();
        animation.setDelay(Duration.seconds(1.0));
        animation.setOnFinished(e -> context.gameState().timer().expire());
        animation.play();
    }
}