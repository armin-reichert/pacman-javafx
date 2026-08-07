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
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.Marquee;
import de.amr.pacmanfx.core.entities.marquee.system.MarqueeSystem;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.flow.TengenMsPacMan_GameState;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.GhostSettings;
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
    public GameEntity presents;

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

        marquee = createMarquee();

        presents = new GameEntity();
        presents.pos().set(9 * WorldMap.TS, MARQUEE_Y - WorldMap.TS);

        flow.restartState(this, SceneState.WAITING_FOR_START);
    }

    private Marquee createMarquee() {
        final var marquee = new Marquee();

        marquee.pos().set(MARQUEE_X, MARQUEE_Y);

        marquee.layout().setNumBulbsHorizontally(35);
        marquee.layout().setNumBulbsVertically(15);
        marquee.layout().setBulbSize(4);
        marquee.layout().setBrightBulbsCount(6);
        marquee.layout().setBrightBulbsDistance(16);

        marquee.visualization().setBulbOnColor(NES_Palette.rgb(0x20));
        marquee.visualization().setBulbOffColor(NES_Palette.rgb(0x15));

        return marquee;
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
                MarqueeSystem.instance().start(scene.marquee);

                final var factory = TengenMsPacMan_ActorFactory.instance();

                scene.msPacMan = factory.createMsPacMan();
                scene.msPacMan.pos().set(WorldMap.TS * 33, ACTOR_Y);
                scene.msPacMan.show();

                sys.worldNavigator().setMoveDir(scene.msPacMan, Direction.LEFT);
                sys.worldNavigator().setSpeed(scene.msPacMan, SPEED);

                sys.spriteAnim().setAnimations(scene.msPacMan, renderConfig.createPacAnimations(spriteAnimations));
                sys.spriteAnim().select(scene.msPacMan, CommonSpriteAnimationID.PAC_MUNCHING);
                sys.spriteAnim().playSelected(scene.msPacMan);

                scene.ghosts = List.of(
                    renderConfig.createAnimatedGhost(scene.gameContext(), spriteAnimations, GhostPersonality.RED_GHOST_SHADOW),
                    renderConfig.createAnimatedGhost(scene.gameContext(), spriteAnimations, GhostPersonality.CYAN_GHOST_BASHFUL),
                    renderConfig.createAnimatedGhost(scene.gameContext(), spriteAnimations, GhostPersonality.PINK_GHOST_SPEEDY),
                    renderConfig.createAnimatedGhost(scene.gameContext(), spriteAnimations, GhostPersonality.ORANGE_GHOST_POKEY)
                );

                for (Ghost ghost : scene.ghosts) {
                    ghost.pos().set(WorldMap.TS * 33, ACTOR_Y);
                    ghost.show();

                    sys.worldNavigator().setMoveDir(ghost, Direction.LEFT);
                    sys.worldNavigator().setWishDir(ghost, Direction.LEFT);
                    sys.worldNavigator().setSpeed(ghost, SPEED);

                    sys.spriteAnim().playSelected(ghost);

                    sys.ghostState().changeState(ghost, GhostState.HUNTING_PAC);
                }
                scene.ghostIndex = 0;
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene scene) {
                MarqueeSystem.instance().update(scene.marquee);

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
                MarqueeSystem.instance().update(scene.marquee);

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
                final WorldNavigationSystem navigator = scene.gameContext().systems().worldNavigator();

                final Ghost ghost = scene.ghosts.get(scene.ghostIndex);
                if (ghost.worldNavigation().moveDir() == Direction.LEFT) {
                    if (ghost.pos().x() <= GHOST_STOP_X) {
                        ghost.pos().setX(GHOST_STOP_X);
                        navigator.setMoveDir(ghost, Direction.UP);
                        navigator.setWishDir(ghost, Direction.UP);
                        scene.waitBeforeRising = 2;
                    } else {
                        motor.move(ghost);
                        Logger.debug("{} moves {} x={}", ghost.name(), ghost.worldNavigation().moveDir(), ghost.pos().x());
                    }
                }
                else if (ghost.worldNavigation().moveDir() == Direction.UP) {
                    int endPositionY = MARQUEE_Y + scene.ghostIndex * 16;
                    if (scene.waitBeforeRising > 0) {
                        scene.waitBeforeRising--;
                    }
                    else if (ghost.pos().y() <= endPositionY) {
                        navigator.setSpeed(ghost, 0);
                        navigator.setMoveDir(ghost, Direction.RIGHT);
                        navigator.setWishDir(ghost, Direction.RIGHT);
                        return true;
                    }
                    else {
                        motor.move(ghost);
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

                MarqueeSystem.instance().update(scene.marquee);

                sys.motor().move(scene.msPacMan);
                if (scene.msPacMan.pos().x() <= MS_PAC_MAN_STOP_X) {
                    sys.worldNavigator().setSpeed(scene.msPacMan, 0);
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