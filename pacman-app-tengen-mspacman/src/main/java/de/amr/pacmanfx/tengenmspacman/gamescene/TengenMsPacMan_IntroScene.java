/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateMachine;
import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gameplay.FrameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.*;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.flow.TengenMsPacMan_GameState;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.uilib.model3D.ghost.GhostSettings;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacManGameVariant.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacManGameVariant.NES_SCREEN_WIDTH;

public class TengenMsPacMan_IntroScene extends AbstractGameScene2D {

    // Anchor point for everything
    public static final int MARQUEE_X = 60, MARQUEE_Y = 64;
    public static final int ACTOR_Y = MARQUEE_Y + 72;
    public static final int GHOST_STOP_X = MARQUEE_X - 18;
    public static final int MS_PAC_MAN_STOP_X = MARQUEE_X + 62;
    public static final float SPEED = 2.2f; //TODO check exact speed

    public final StateMachine<TengenMsPacMan_IntroScene> sceneFlow;

    public TengenMsPacMan_SpriteSheet spriteSheet;

    public Color[] ghostColors;

    public Marquee marquee;
    public Actor presentsText;
    public Pac msPacMan;
    public List<Ghost> ghosts;
    public int ghostIndex;
    private int waitBeforeRising;
    public boolean dark;

    public TengenMsPacMan_IntroScene(GameAppContext appContext) {
        super(appContext);
        unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
        sceneFlow = new StateMachine<>(List.of(SceneState.values()));
    }

    @Override
    public void onActivate() {
        final GameVariantConfig gameVariantConfig = appContext().variants().currentVariant().config();

        gameContext().hudState().hide();

        spriteSheet = (TengenMsPacMan_SpriteSheet) gameVariantConfig.spriteSheet();

        final var actions = appContext().getExtensionValue(
            TengenMsPacMan_GameExtension.ACTIONS, TengenMsPacMan_Actions.class);

        actionBindings().selectAnyMatchingBinding(actions.actionEnterStartScreen(), actions.localBindings());
        actionBindings().selectAnyMatchingBinding(actions.actionToggleJoypadBindingsDisplayed(), actions.localBindings());

        final List<GhostSettings> ghostConfigs = gameVariantConfig.worldSettings().ghosts();
        ghostColors = Stream.of(GameModel.RED_GHOST_SHADOW, GameModel.PINK_GHOST_SPEEDY, GameModel.CYAN_GHOST_BASHFUL, GameModel.ORANGE_GHOST_POKEY)
            .map(personality -> ghostConfigs.get(personality).colors().normal().dressColor())
            .toArray(Color[]::new);

        marquee = new Marquee();
        marquee.setPosition(MARQUEE_X, MARQUEE_Y);
        marquee.scalingProperty().bind(scalingProperty());

        presentsText = new Actor();
        presentsText.setPosition(9 * WorldMap.TS, MARQUEE_Y - WorldMap.TS);

        sceneFlow.restartState(this, SceneState.WAITING_FOR_START);
    }

    @Override
    public void onTick(FrameContext frame) {
        sceneFlow.update(this);
    }

    public enum SceneState implements State<TengenMsPacMan_IntroScene> {

        WAITING_FOR_START {

            @Override
            public void onEnter(TengenMsPacMan_IntroScene scene) {
                timer.restartTicks(TickTimer.INDEFINITE);
                scene.dark = false;
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene scene) {
                if (timer.atSecond(7.8)) {
                    scene.dark = true;
                } else if (timer.atSecond(9)) {
                    scene.dark = false;
                    scene.sceneFlow.enterState(scene, SHOWING_MARQUEE);
                }
            }
        },

        SHOWING_MARQUEE {
            @Override
            public void onEnter(TengenMsPacMan_IntroScene scene) {
                final GameVariantConfig gameVariantConfig = scene.appContext().variants().currentVariant().config();
                final SpriteAnimationContainer spriteAnimationContainer = scene.appContext().ui().sprites().animations();

                timer.restartTicks(TickTimer.INDEFINITE);

                scene.msPacMan = TengenMsPacMan_ActorFactory.createMsPacMan();
                scene.msPacMan.setAnimations(gameVariantConfig.createPacAnimations(spriteAnimationContainer));
                scene.msPacMan.animations().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
                scene.msPacMan.animations().playSelected();
                scene.msPacMan.setPosition(WorldMap.TS * 33, ACTOR_Y);
                scene.msPacMan.setMoveDir(Direction.LEFT);
                scene.msPacMan.setSpeed(SPEED);
                scene.msPacMan.setVisible(true);

                scene.ghosts = List.of(
                    gameVariantConfig.createAnimatedGhost(spriteAnimationContainer, GameModel.RED_GHOST_SHADOW),
                    gameVariantConfig.createAnimatedGhost(spriteAnimationContainer, GameModel.CYAN_GHOST_BASHFUL),
                    gameVariantConfig.createAnimatedGhost(spriteAnimationContainer, GameModel.PINK_GHOST_SPEEDY),
                    gameVariantConfig.createAnimatedGhost(spriteAnimationContainer, GameModel.ORANGE_GHOST_POKEY)
                );
                for (Ghost ghost : scene.ghosts) {
                    ghost.setPosition(WorldMap.TS * 33, ACTOR_Y);
                    ghost.setMoveDir(Direction.LEFT);
                    ghost.setWishDir(Direction.LEFT);
                    ghost.setSpeed(SPEED);
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setVisible(true);
                    ghost.animations().playSelected();
                }
                scene.ghostIndex = 0;
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene scene) {
                scene.marquee.update(timer.tickCount());
                if (timer.atSecond(1)) {
                    scene.sceneFlow.enterState(scene, GHOSTS_MARCHING_IN);
                }
            }
        },

        GHOSTS_MARCHING_IN {
            @Override
            public void onEnter(TengenMsPacMan_IntroScene scene) {
                timer.restartTicks(TickTimer.INDEFINITE);
                scene.waitBeforeRising = 0;
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene scene) {
                scene.marquee.update(timer.tickCount());
                boolean reachedEndPosition = letGhostMarchIn(scene);
                if (reachedEndPosition) {
                    if (scene.ghostIndex == 3) {
                        scene.sceneFlow.enterState(scene, MS_PACMAN_MARCHING_IN);
                    } else {
                        ++scene.ghostIndex;
                    }
                }
            }

            boolean letGhostMarchIn(TengenMsPacMan_IntroScene scene) {
                Ghost ghost = scene.ghosts.get(scene.ghostIndex);
                if (ghost.moveDir() == Direction.LEFT) {
                    if (ghost.x() <= GHOST_STOP_X) {
                        ghost.setX(GHOST_STOP_X);
                        ghost.setMoveDir(Direction.UP);
                        ghost.setWishDir(Direction.UP);
                        scene.waitBeforeRising = 2;
                    } else {
                        ghost.move();
                        Logger.debug("{} moves {} x={}", ghost.name(), ghost.moveDir(), ghost.x());
                    }
                }
                else if (ghost.moveDir() == Direction.UP) {
                    int endPositionY = MARQUEE_Y + scene.ghostIndex * 16;
                    if (scene.waitBeforeRising > 0) {
                        scene.waitBeforeRising--;
                    }
                    else if (ghost.y() <= endPositionY) {
                        ghost.setSpeed(0);
                        ghost.setMoveDir(Direction.RIGHT);
                        ghost.setWishDir(Direction.RIGHT);
                        return true;
                    }
                    else {
                        ghost.move();
                        Logger.debug("{} moves {}", ghost.name(), ghost.moveDir());
                    }
                }
                return false;
            }
        },

        MS_PACMAN_MARCHING_IN {
            @Override
            public void onEnter(TengenMsPacMan_IntroScene scene) {
                timer.restartTicks(TickTimer.INDEFINITE);
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene scene) {
                final GameContext gameContext = scene.gameContext();

                scene.marquee.update(timer.tickCount());

                scene.msPacMan.move();
                if (scene.msPacMan.x() <= MS_PAC_MAN_STOP_X) {
                    scene.msPacMan.setSpeed(0);
                    scene.msPacMan.animations().resetSelected();
                }
                if (timer.atSecond(8)) {
                    // start demo level or show options
                    final TengenMsPacMan_GameModel gameModel = (TengenMsPacMan_GameModel) scene.gameModel();
                    if (gameModel.allOptionsHaveDefaultValue()) {
                        gameModel.setCanStartNewGame(false); // TODO check this
                        gameContext.flow().restartState(gameContext, TengenMsPacMan_GameState.GAME_OR_LEVEL_STARTING.state());
                    } else {
                        gameContext.flow().enterState(gameContext, TengenMsPacMan_GameState.GAME_PREPARATION.state());
                    }
                }
            }
        };

        final TickTimer timer = new TickTimer("Timer-" + name());

        @Override
        public TickTimer timer() {
            return timer;
        }
    }
}