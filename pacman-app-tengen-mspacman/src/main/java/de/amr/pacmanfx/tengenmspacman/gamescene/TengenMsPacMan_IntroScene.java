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
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.actors.*;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;
import de.amr.pacmanfx.core.model.systems.common.MovementSystem;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
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

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantConfig.NES_SCREEN_WIDTH;

public class TengenMsPacMan_IntroScene extends AbstractGameScene2D {

    // Anchor point for everything
    public static final int MARQUEE_X = 60, MARQUEE_Y = 64;
    public static final int ACTOR_Y = MARQUEE_Y + 72;
    public static final int GHOST_STOP_X = MARQUEE_X - 18;
    public static final int MS_PAC_MAN_STOP_X = MARQUEE_X + 62;
    public static final float SPEED = 2.2f; //TODO check exact speed

    public final StateMachine<TengenMsPacMan_IntroScene> flow;

    public TengenMsPacMan_SpriteSheet spriteSheet;

    public Color[] ghostColors;

    public Marquee marquee;
    public Actor presents;
    public Pac msPacMan;
    public List<Ghost> ghosts;
    public int ghostIndex;
    private int waitBeforeRising;
    public boolean dark;

    public TengenMsPacMan_IntroScene(GameAppContext appContext) {
        super(appContext);
        unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
        flow = new StateMachine<>(List.of(SceneState.values()));
    }

    @Override
    public void onActivate() {
        final GameVariantConfig variantConfig = appContext().variants().currentVariant().config();

        gameContext().hudState().hide();

        spriteSheet = TengenMsPacMan_SpriteSheet.instance();

        final var actions = appContext().getExtensionValue(TengenMsPacMan_GameExtension.ACTIONS, TengenMsPacMan_Actions.class);

        actionBindings().selectAnyMatchingBinding(actions.actionEnterStartScreen(), actions.localBindings());
        actionBindings().selectAnyMatchingBinding(actions.actionToggleJoypadBindingsDisplayed(), actions.localBindings());

        final List<GhostSettings> ghostSettings = variantConfig.worldSettings().ghosts();
        ghostColors = Stream.of(
                GhostPersonality.RED_GHOST_SHADOW,
                GhostPersonality.PINK_GHOST_SPEEDY,
                GhostPersonality.CYAN_GHOST_BASHFUL,
                GhostPersonality.ORANGE_GHOST_POKEY)
            .map(personality -> ghostSettings.get(personality.ordinal()).colors().normal().dressColor())
            .toArray(Color[]::new);

        marquee = new Marquee();
        marquee.position().set(MARQUEE_X, MARQUEE_Y);
        marquee.scalingProperty().bind(scalingProperty());

        presents = new Actor();
        presents.position().set(9 * WorldMap.TS, MARQUEE_Y - WorldMap.TS);

        flow.restartState(this, SceneState.WAITING_FOR_START);
    }

    @Override
    public void onTick(GameContext gameContext) {
        flow.update(this);
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
                    scene.flow.enterState(scene, SHOWING_MARQUEE);
                }
            }
        },

        SHOWING_MARQUEE {
            @Override
            public void onEnter(TengenMsPacMan_IntroScene scene) {
                GameSystems sys = scene.gameContext().systems();

                final GameVariantRenderConfig renderConfig = scene.appContext().variants().currentVariant().config().renderConfig();
                final SpriteAnimationContainer spriteAnimations = scene.appContext().ui().sprites().animations();

                timer.restartTicks(TickTimer.INDEFINITE);

                final var factory = new TengenMsPacMan_ActorFactory();

                scene.msPacMan = factory.createMsPacMan();
                scene.msPacMan.position().set(WorldMap.TS * 33, ACTOR_Y);
                scene.msPacMan.visibility().show();

                sys.navigator().setMoveDir(scene.msPacMan, Direction.LEFT);
                sys.navigator().setSpeed(scene.msPacMan, SPEED);

                sys.spriteAnim().setAnimations(scene.msPacMan, renderConfig.createPacAnimations(spriteAnimations));
                sys.spriteAnim().select(scene.msPacMan, CommonAnimationID.PAC_MUNCHING);
                sys.spriteAnim().playSelected(scene.msPacMan);

                scene.ghosts = List.of(
                    renderConfig.createAnimatedGhost(scene.gameContext(), spriteAnimations, GhostPersonality.RED_GHOST_SHADOW),
                    renderConfig.createAnimatedGhost(scene.gameContext(), spriteAnimations, GhostPersonality.CYAN_GHOST_BASHFUL),
                    renderConfig.createAnimatedGhost(scene.gameContext(), spriteAnimations, GhostPersonality.PINK_GHOST_SPEEDY),
                    renderConfig.createAnimatedGhost(scene.gameContext(), spriteAnimations, GhostPersonality.ORANGE_GHOST_POKEY)
                );

                for (Ghost ghost : scene.ghosts) {
                    ghost.position().set(WorldMap.TS * 33, ACTOR_Y);
                    ghost.visibility().show();

                    sys.navigator().setMoveDir(ghost, Direction.LEFT);
                    sys.navigator().setWishDir(ghost, Direction.LEFT);
                    sys.navigator().setSpeed(ghost, SPEED);

                    sys.spriteAnim().playSelected(ghost);

                    sys.ghostState().changeState(scene.gameContext(), ghost, GhostState.HUNTING_PAC);
                }
                scene.ghostIndex = 0;
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene scene) {
                scene.marquee.update(timer.tickCount());
                if (timer.atSecond(1)) {
                    scene.flow.enterState(scene, GHOSTS_MARCHING_IN);
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
                        scene.flow.enterState(scene, MS_PACMAN_MARCHING_IN);
                    } else {
                        ++scene.ghostIndex;
                    }
                }
            }

            boolean letGhostMarchIn(TengenMsPacMan_IntroScene scene) {
                final MovementSystem motor = scene.gameContext().systems().motor();
                final WorldNavigationSystem navigator = scene.gameContext().systems().navigator();

                final Ghost ghost = scene.ghosts.get(scene.ghostIndex);
                if (ghost.worldNavigation().moveDir() == Direction.LEFT) {
                    if (ghost.position().x <= GHOST_STOP_X) {
                        ghost.position().setX(GHOST_STOP_X);
                        navigator.setMoveDir(ghost, Direction.UP);
                        navigator.setWishDir(ghost, Direction.UP);
                        scene.waitBeforeRising = 2;
                    } else {
                        motor.moveAccelerated(ghost);
                        Logger.debug("{} moves {} x={}", ghost.name(), ghost.worldNavigation().moveDir(), ghost.position().x);
                    }
                }
                else if (ghost.worldNavigation().moveDir() == Direction.UP) {
                    int endPositionY = MARQUEE_Y + scene.ghostIndex * 16;
                    if (scene.waitBeforeRising > 0) {
                        scene.waitBeforeRising--;
                    }
                    else if (ghost.position().y <= endPositionY) {
                        navigator.setSpeed(ghost, 0);
                        navigator.setMoveDir(ghost, Direction.RIGHT);
                        navigator.setWishDir(ghost, Direction.RIGHT);
                        return true;
                    }
                    else {
                        motor.moveAccelerated(ghost);
                        Logger.debug("{} moves {}", ghost.name(), ghost.worldNavigation().moveDir());
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
                final GameSystems sys = gameContext.systems();

                scene.marquee.update(timer.tickCount());

                sys.motor().moveAccelerated(scene.msPacMan);
                if (scene.msPacMan.position().x <= MS_PAC_MAN_STOP_X) {
                    sys.navigator().setSpeed(scene.msPacMan, 0);
                    sys.spriteAnim().resetSelected(scene.msPacMan);
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