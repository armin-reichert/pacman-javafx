/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.scene3d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui.fx.GameScene;
import de.amr.games.pacman.ui.fx.GameSceneContext;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.v3d.ActionHandler3D;
import de.amr.games.pacman.ui.fx.v3d.animation.SinusCurveAnimation;
import de.amr.games.pacman.ui.fx.v3d.entity.*;
import javafx.animation.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui.fx.PacManGames2dUI.*;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.message;
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

    private static final byte CHILD_LEVEL_3D  = 0;

    private final ObjectProperty<Perspective> perspectivePy = new SimpleObjectProperty<>(this, "perspective") {
        @Override
        protected void invalidated() {
            currentCamController().reset(camera);
        }
    };

    private final Map<Perspective, CameraController> camControllerMap = new EnumMap<>(Map.of(
        Perspective.DRONE,            new CamDrone(),
        Perspective.FOLLOWING_PLAYER, new CamFollowingPlayer(),
        Perspective.NEAR_PLAYER,      new CamNearPlayer(),
        Perspective.TOTAL,            new CamTotal()
    ));
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Group subSceneRoot = new Group();
    private final SubScene fxSubScene;

    private GameLevel3D level3D;
    private GameSceneContext context;
    private boolean scoreVisible;

    public PlayScene3D() {
        var ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PY_3D_LIGHT_COLOR);
        var coordSystem = new CoordSystem();
        coordSystem.visibleProperty().bind(PY_3D_AXES_VISIBLE);
        subSceneRoot.getChildren().setAll(new Group(), coordSystem, ambientLight);
        // initial scene size is irrelevant, gets bound to parent scene later
        fxSubScene = new SubScene(subSceneRoot, 42, 42, true, SceneAntialiasing.BALANCED);
        fxSubScene.setCamera(camera);
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
        perspectivePy.bind(PY_3D_PERSPECTIVE);
        Logger.info("3D play scene init(). {}", this);
    }

    @Override
    public void end() {
        perspectivePy.unbind();
        Logger.info("3D play scene end(). {}", this);
    }

    @Override
    public void update() {
        if (level3D != null) {
            level3D.update();
            currentCamController().update(camera, level3D.pac3D());
            updateSound();
        }
    }

    public PerspectiveCamera camera() {
        return camera;
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

    private CameraController currentCamController() {
        return camControllerMap.getOrDefault(perspectivePy.get(), camControllerMap.get(Perspective.TOTAL));
    }

    private void replaceGameLevel3D(GameLevel level) {
        level3D = new GameLevel3D(level, context.theme(), context.spriteSheet());
        // replace initial placeholder or previous 3D level
        subSceneRoot.getChildren().set(CHILD_LEVEL_3D, level3D.root());

        level3D.updateLevelCounterSprites();
        // keep the scores rotated such that the viewer always sees them frontally
        level3D.scores3D().root().rotationAxisProperty().bind(camera.rotationAxisProperty());
        level3D.scores3D().root().rotateProperty().bind(camera.rotateProperty());

        if (PY_3D_FLOOR_TEXTURE_RND.get()) {
            List<String> names = context.theme().getArray("texture.names");
            PY_3D_FLOOR_TEXTURE.set(names.get(randomInt(0, names.size())));
        }
        Logger.info("3D game level {} created.", level.number());
    }

    private void showLevelMessage() {
        Logger.info("showLevelMessage()");
        context.gameLevel().ifPresent(level -> {
            if (context.gameState() == GameState.LEVEL_TEST) {
                level3D.showMessage("TEST LEVEL " + level.number(), 5, level.world().numCols() * HTS, 34 * TS);
            } else if (!level.isDemoLevel()){
                var house = level.world().house();
                double x = (house.topLeftTile().x() + 0.5 * house.size().x()) * TS;
                double y = (house.topLeftTile().y() + house.size().y()) * TS;
                level3D.showMessage("READY!", context.gameController().isPlaying() ? 0.5 : 2.5, x, y);
            }
        });
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
        context.gameLevel().ifPresent(level -> {
            if (level3D == null) {
                replaceGameLevel3D(level);
            }
            level3D.eatables3D().forEach(
                eatable3D -> eatable3D.root().setVisible(!level.world().hasEatenFoodAt(eatable3D.tile())));
            if (oneOf(context.gameState(), GameState.HUNTING, GameState.GHOST_DYING)) {
                level3D.energizers3D().forEach(Energizer3D::startPumping);
            }
            if (!level.isDemoLevel() && context.gameState() == GameState.HUNTING) {
                context.ensureSirenStarted(level.huntingPhaseIndex() / 2);
            }
        });
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        if (level3D == null) {
            Logger.error("WTF: No 3D game level exists?");
            return;
        }
        context.gameLevel().ifPresent(level -> {
            // When cheat "eat all pellets" has been used, no tile is present in the event.
            // In that case, ensure that the 3D pellets are in sync with the model.
            if (e.tile().isEmpty()) {
                level.world().tiles()
                    .filter(level.world()::hasEatenFoodAt)
                    .map(level3D::eatableAt)
                    .flatMap(Optional::stream)
                    .forEach(Eatable3D::onEaten);
            } else {
                Vector2i tile = e.tile().get();
                level3D.eatableAt(tile).ifPresent(level3D::eat);
            }
        });
    }

    @Override
    public void onBonusActivated(GameEvent e) {
        if (level3D != null) {
            context.gameLevel().flatMap(GameLevel::bonus).ifPresent(level3D::replaceBonus3D);
        }
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        if (level3D != null) {
            level3D.bonus3D().ifPresent(Bonus3D::showEaten);
        }
    }

    @Override
    public void onBonusExpired(GameEvent e) {
        if (level3D != null) {
            level3D.bonus3D().ifPresent(Bonus3D::hide);
        }
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        e.game.level().ifPresent(this::replaceGameLevel3D);
    }

    @Override
    public void onLevelStarted(GameEvent e) {
        Logger.info("onLevel started");
        context.gameLevel().ifPresent(level -> {
            if (level.number() == 1 || context.gameState() == GameState.LEVEL_TEST) {
                showLevelMessage();
            }
        });
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        if (level3D != null) {
            level3D.pac3D().walkingAnimation().setPowerWalking(true);
        }
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        if (level3D != null) {
            level3D.pac3D().walkingAnimation().setPowerWalking(false);
        }
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        switch (e.newState) {

            case READY -> {
                if (level3D != null) {
                    level3D.pac3D().init();
                    level3D.ghosts3D().forEach(Ghost3D::init);
                    showLevelMessage();
                }
            }

            case HUNTING -> {
                if (level3D == null) {
                    throw new IllegalStateException("No 3D level exists!");
                }
                level3D.pac3D().init();
                level3D.ghosts3D().forEach(Ghost3D::init);
                level3D.livesCounter3D().startAnimation();
                level3D.energizers3D().forEach(Energizer3D::startPumping);
            }

            case PACMAN_DYING -> {
                if (level3D == null) {
                    throw new IllegalStateException("No 3D level exists!");
                }
                lockGameStateAndPlayAfterSeconds(1.0, level3D.pac3D().createDyingAnimation(context.gameVariant()));
            }

            case GHOST_DYING -> {
                if (level3D == null) {
                    throw new IllegalStateException("No 3D level exists!");
                }
                context.gameLevel().ifPresent(level -> {
                    Rectangle2D[] sprites = switch (context.gameVariant()) {
                        case MS_PACMAN -> context.<MsPacManGameSpriteSheet>spriteSheet().ghostNumberSprites();
                        case PACMAN    -> context.<PacManGameSpriteSheet>spriteSheet().ghostNumberSprites();
                    };
                    var killedGhosts = level.thisFrame().killedGhosts;
                    killedGhosts.forEach(ghost -> {
                        Image numberImage = context.spriteSheet().subImage(sprites[ghost.killedIndex()]);
                        level3D.ghost3D(ghost.id()).setNumberImage(numberImage);
                    });
                });
            }

            case CHANGING_TO_NEXT_LEVEL -> {
                if (level3D == null) {
                    throw new IllegalStateException("No 3D level exists!");
                }
                context.gameLevel().ifPresent(level -> {
                    lockGameStateForSeconds(3);
                    replaceGameLevel3D(level);
                    level3D.pac3D().init();
                    currentCamController().reset(camera);
                });
            }

            case LEVEL_COMPLETE -> {
                if (level3D == null) {
                    throw new IllegalStateException("No 3D level exists!");
                }
                context.gameLevel().ifPresent(level -> {
                    // if cheat has been used to complete level, 3D food might still exist:
                    level3D.eatables3D().forEach(level3D::eat);
                    level3D.livesCounter3D().stopAnimation();
                    boolean intermissionAfterLevel = level.data().intermissionNumber() != 0;
                    lockGameStateAndPlayAfterSeconds(1.0,
                        createLevelCompleteAnimation(level),
                        actionAfterSeconds(1.0, () -> {
                            level.pac().hide();
                            level3D.livesCounter3D().lightOnPy.set(false);
                            if (!intermissionAfterLevel) {
                                context.playAudioClip("audio.level_complete");
                                context.actionHandler().showFlashMessageSeconds(2,
                                    pickLevelCompleteMessage(level.number()));
                            }
                        }),
                        intermissionAfterLevel ? pauseSeconds(0) : createLevelChangeAnimation(),
                        immediateAction(() -> level3D.livesCounter3D().lightOnPy.set(true))
                    );
                });
            }

            case GAME_OVER -> {
                if (level3D == null) {
                    throw new IllegalStateException("No 3D level exists!");
                }
                lockGameStateForSeconds(3);
                level3D.livesCounter3D().stopAnimation();
                context.actionHandler().showFlashMessageSeconds(3, PICKER_GAME_OVER.next());
                context.playAudioClip("audio.game_over");
            }

            case LEVEL_TEST ->
                context.gameLevel().ifPresent(level -> {
                    PY_3D_PERSPECTIVE.set(Perspective.TOTAL);
                    level.letsGetReadyToRumble(true);
                    replaceGameLevel3D(level);
                    level3D.pac3D().init();
                    for (Ghost3D ghost3D : level3D.ghosts3D()) {
                        ghost3D.init();
                    }
                    showLevelMessage();
                });

            default -> {}
        }

        // on state exit
        if (e.oldState == GameState.HUNTING && level3D != null) {
            level3D.energizers3D().forEach(Energizer3D::stopPumping);
            level3D.bonus3D().ifPresent(Bonus3D::hide);
        }
    }

    private String pickLevelCompleteMessage(int levelNumber) {
        return PICKER_LEVEL_COMPLETE.next() + "\n\n" + message(context.messageBundles(), "level_complete", levelNumber);
    }

    private Transition createLevelChangeAnimation() {
        return new SequentialTransition(
            immediateAction(() -> {
                perspectivePy.unbind();
                perspectivePy.set(Perspective.TOTAL);
            }),
            createLevelRotateAnimation(),
            actionAfterSeconds(0.5, () -> context.playAudioClip("audio.sweep")),
            actionAfterSeconds(0.5, () -> perspectivePy.bind(PY_3D_PERSPECTIVE))
        );
    }

    private Transition createLevelRotateAnimation() {
        var rotation = new RotateTransition(Duration.seconds(1.5), level3D.root());
        rotation.setAxis(RND.nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS);
        rotation.setFromAngle(0);
        rotation.setToAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        return rotation;
    }

    private Transition createLevelCompleteAnimation(GameLevel level) {
        if (level.data().numFlashes() == 0) {
            return pauseSeconds(1.0);
        }
        double wallHeight = PY_3D_WALL_HEIGHT.get();
        var animation = new SinusCurveAnimation(level.data().numFlashes());
        animation.setAmplitude(wallHeight);
        animation.elongationPy.set(level3D.world3D().wallHeightPy.get());
        level3D.world3D().wallHeightPy.bind(animation.elongationPy);
        animation.setOnFinished(e -> {
            level3D.world3D().wallHeightPy.bind(PY_3D_WALL_HEIGHT);
            PY_3D_WALL_HEIGHT.set(wallHeight);
        });
        return animation;
    }

    private void updateSound() {
        context.gameLevel().ifPresent(level -> {
            if (level.isDemoLevel()) {
                return;
            }
            if (level.pac().starvingTicks() > 8) { // TODO not sure how this is done in Arcade game
                context.stopAudioClip("audio.pacman_munch");
            }
            if (!level.thisFrame().pacKilled && level.ghosts(GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE)
                .anyMatch(Ghost::isVisible)) {
                context.ensureAudioLoop("audio.ghost_returning");
            } else {
                context.stopAudioClip("audio.ghost_returning");
            }
        });
    }

    private void lockGameStateForSeconds(double seconds) {
        context.gameState().timer().resetIndefinitely();
        actionAfterSeconds(seconds, () -> context.gameState().timer().expire()).play();
    }

    private void lockGameStateAndPlayAfterSeconds(double seconds, Animation... animations) {
        context.gameState().timer().resetIndefinitely();
        var animationSequence = new SequentialTransition(animations);
        if (seconds > 0) {
            animationSequence.setDelay(Duration.seconds(seconds));
        }
        animationSequence.setOnFinished(e -> context.gameState().timer().expire());
        animationSequence.play();
    }
}