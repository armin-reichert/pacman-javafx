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
import de.amr.games.pacman.ui.fx.v3d.entity.*;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.List;
import java.util.Map;
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

    // Index where game level 3D is inserted into child list
    private static final byte CHILD_LEVEL_3D  = 0;

    public final ObjectProperty<Perspective> perspectivePy = new SimpleObjectProperty<>(this, "perspective") {
        @Override
        protected void invalidated() {
            currentCamController().reset(fxSubScene.getCamera());
        }
    };

    private final Map<Perspective, CameraController> camControllerMap = Map.of(
        Perspective.DRONE,            new CamDrone(),
        Perspective.FOLLOWING_PLAYER, new CamFollowingPlayer(),
        Perspective.NEAR_PLAYER,      new CamNearPlayer(),
        Perspective.TOTAL,            new CamTotal()
    );

    private final Group subSceneRoot = new Group();
    private final SubScene fxSubScene;
    private final Scores3D scores3D;

    private GameLevel3D level3D;
    private GameSceneContext context;
    private boolean scoreVisible;

    public PlayScene3D() {
        var ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PY_3D_LIGHT_COLOR);
        var coordSystem = new CoordSystem();
        coordSystem.visibleProperty().bind(PY_3D_AXES_VISIBLE);
        scores3D = new Scores3D();
        // first child is placeholder for game level 3D
        subSceneRoot.getChildren().setAll(new Group(), scores3D.root(),  coordSystem, ambientLight);
        // initial scene size is irrelevant, gets bound to parent scene later
        fxSubScene = new SubScene(subSceneRoot, 42, 42, true, SceneAntialiasing.BALANCED);
        fxSubScene.setCamera(new PerspectiveCamera(true));
        // keep the scores rotated such that the viewer always sees them frontally
        scores3D.root().rotationAxisProperty().bind(fxSubScene.getCamera().rotationAxisProperty());
        scores3D.root().rotateProperty().bind(fxSubScene.getCamera().rotateProperty());
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
        Logger.info("3D play scene init(). {}", this);
    }

    @Override
    public void end() {
        perspectivePy.unbind();
        Logger.info("3D play scene end(). {}", this);
    }

    @Override
    public void update() {
        context.gameLevel().ifPresent(level -> {
            if (level3D != null) {
                level3D.update();
                currentCamController().update(fxSubScene.getCamera(), level3D.pac3D());
            }
           updateSound(level);
        });
        scores3D.setScores(
            context.game().score().points(),     context.game().score().levelNumber(),
            context.game().highScore().points(), context.game().highScore().levelNumber());
        if (context.gameController().hasCredit()) {
            scores3D.hideText();
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

    private CameraController currentCamController() {
        return camControllerMap.getOrDefault(perspectivePy.get(), camControllerMap.get(Perspective.TOTAL));
    }

    private void replaceGameLevel3D(GameLevel level) {
        level3D = new GameLevel3D(level, context.theme(), context.spriteSheet());
        // replace initial placeholder or previous 3D level
        subSceneRoot.getChildren().set(CHILD_LEVEL_3D, level3D.root());

        // center over origin
        double tx = -level.world().numCols() * HTS;
        double ty = -level.world().numRows() * HTS;
        level3D.root().setTranslateX(tx);
        level3D.root().setTranslateY(ty);
        scores3D.root().setTranslateX(tx + TS);
        scores3D.root().setTranslateY(ty - 3 * TS);
        scores3D.root().setTranslateZ(- 3 * TS);

        if (PY_3D_FLOOR_TEXTURE_RND.get()) {
            List<String> names = context.theme().getArray("texture.names");
            PY_3D_FLOOR_TEXTURE.set(names.get(randomInt(0, names.size())));
        }
        Logger.info("3D game level {} created.", level.number());
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
    public void onGameStateChange(GameStateChangeEvent event) {
        switch (event.newState) {

            case READY -> {
                if (level3D != null) {
                    context.gameLevel().ifPresent(level -> {
                        level3D.pac3D().init();
                        level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(level));
                        showLevelMessage(level);
                    });
                }
            }

            case HUNTING -> {
                assertLevel3DExists();
                context.gameLevel().ifPresent(level -> {
                    level3D.pac3D().init();
                    level3D.ghosts3D().forEach(ghost3D -> ghost3D.init(level));
                    level3D.livesCounter3D().startAnimation();
                    level3D.energizers3D().forEach(Energizer3D::startPumping);
                });
            }

            case PACMAN_DYING -> {
                assertLevel3DExists();
                lockGameStateAndPlayAfterSeconds(1.0, level3D.pac3D().createDyingAnimation(context.gameVariant()));
            }

            case GHOST_DYING -> {
                assertLevel3DExists();
                context.gameLevel().ifPresent(level -> {
                    Rectangle2D[] sprites = switch (context.gameVariant()) {
                        case MS_PACMAN -> context.<MsPacManGameSpriteSheet>spriteSheet().ghostNumberSprites();
                        case PACMAN    -> context.<PacManGameSpriteSheet>spriteSheet().ghostNumberSprites();
                    };
                    var killedGhosts = level.thisFrame().killedGhosts;
                    killedGhosts.forEach(ghost -> {
                        Image numberImage = context.spriteSheet().subImage(sprites[ghost.killedIndex()]);
                        level3D.ghosts3D().get(ghost.id()).setNumberImage(numberImage);
                    });
                });
            }

            case CHANGING_TO_NEXT_LEVEL -> {
                assertLevel3DExists();
                context.gameLevel().ifPresent(level -> {
                    lockGameStateForSeconds(3);
                    replaceGameLevel3D(level);
                    level3D.pac3D().init();
                    currentCamController().reset(fxSubScene.getCamera());
                });
            }

            case LEVEL_COMPLETE -> {
                assertLevel3DExists();
                context.gameLevel().ifPresent(level -> {
                    // if cheat has been used to complete level, 3D food might still exist:
                    level3D.eatables3D().forEach(level3D::eat);
                    level3D.livesCounter3D().stopAnimation();
                    playLevelCompleteAnimation(level);
                });
            }

            case GAME_OVER -> {
                assertLevel3DExists();
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
                        ghost3D.init(level);
                    }
                    showLevelMessage(level);
                });

            default -> {}
        }

        // on state exit
        if (event.oldState == GameState.HUNTING && level3D != null) {
            level3D.energizers3D().forEach(Energizer3D::stopPumping);
            level3D.bonus3D().ifPresent(Bonus3D::hide);
        }
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        assertLevel3DExists();
        context.gameLevel().flatMap(GameLevel::bonus).ifPresent(level3D::replaceBonus3D);
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        assertLevel3DExists();
        level3D.bonus3D().ifPresent(Bonus3D::showEaten);
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        assertLevel3DExists();
        level3D.bonus3D().ifPresent(Bonus3D::hide);
    }

    @Override
    public void onLevelCreated(GameEvent event) {
        event.game.level().ifPresent(this::replaceGameLevel3D);
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        assertLevel3DExists();
        context.gameLevel().ifPresent(level -> {
            if (level.number() == 1 || context.gameState() == GameState.LEVEL_TEST) {
                showLevelMessage(level);
            }
            level3D.populateLevelCounter(context.game(), context.spriteSheet());
        });
    }

    @Override
    public void onPacFoundFood(GameEvent event) {
        assertLevel3DExists();
        context.gameLevel().ifPresent(level -> {
            // When cheat "eat all pellets" has been used, no tile is present in the event.
            // In that case, ensure that the 3D pellets are in sync with the model.
            if (event.tile().isEmpty()) {
                level.world().tiles()
                    .filter(level.world()::hasEatenFoodAt)
                    .map(level3D::eatableAt)
                    .flatMap(Optional::stream)
                    .forEach(Eatable3D::onEaten);
            } else {
                Vector2i tile = event.tile().get();
                level3D.eatableAt(tile).ifPresent(level3D::eat);
            }
        });
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

    private void showLevelMessage(GameLevel level) {
        if (context.gameState() == GameState.LEVEL_TEST) {
            level3D.showMessage("TEST LEVEL " + level.number(), 5, level.world().numCols() * HTS, 34 * TS);
        } else if (!level.isDemoLevel()){
            var house = level.world().house();
            double x = (house.topLeftTile().x() + 0.5 * house.size().x()) * TS;
            double y = (house.topLeftTile().y() + house.size().y()) * TS;
            level3D.showMessage("READY!", context.gameController().isPlaying() ? 0.5 : 2.5, x, y);
        }
    }

    private void playLevelCompleteAnimation(GameLevel level) {
        boolean noIntermission = level.data().intermissionNumber() == 0;
        lockGameStateAndPlayAfterSeconds(1.0,
            level3D.createLevelCompleteAnimation(),
            actionAfterSeconds(1.0, () -> {
                level.pac().hide();
                level3D.livesCounter3D().lightOnPy.set(false);
                if (noIntermission) {
                    context.playAudioClip("audio.level_complete");
                    context.actionHandler().showFlashMessageSeconds(2, pickLevelCompleteMessage(level.number()));
                }
            }),
            noIntermission ? createLevelChangeAnimation() : pauseSeconds(0),
            immediateAction(() -> level3D.livesCounter3D().lightOnPy.set(true))
        );
    }

    private String pickLevelCompleteMessage(int levelNumber) {
        return PICKER_LEVEL_COMPLETE.next() + "\n\n" + context.tt("level_complete", levelNumber);
    }

    private Transition createLevelChangeAnimation() {
        return new SequentialTransition(
            immediateAction(() -> {
                perspectivePy.unbind();
                perspectivePy.set(Perspective.TOTAL);
            }),
            level3D.createLevelRotateAnimation(),
            actionAfterSeconds(0.5, () -> context.playAudioClip("audio.sweep")),
            actionAfterSeconds(0.5, () -> perspectivePy.bind(PY_3D_PERSPECTIVE))
        );
    }


    private void updateSound(GameLevel level) {
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