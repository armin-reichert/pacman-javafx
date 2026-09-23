/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.introscene;

import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateMachine;
import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.basics.timer.TickTimer;
import de.amr.basics.ui.ecs.systems.ActorSpriteAnimController;
import de.amr.basics.ui.ecs.systems.MovementSystem;
import de.amr.basics.ui.entities.props.marquee.Marquee;
import de.amr.basics.ui.spriteanim.CommonSpriteAnimationID;
import de.amr.basics.ui.spriteanim.SpriteAnimationContainer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.core.entities.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.actor.ghost.GhostState;
import de.amr.pacmanfx.core.gamestate.GameFlow;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.game.GameVariantRuntime;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.gamestate.Tengen_GameState;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import de.amr.pacmanfx.uilib.entities3d.ghost.comp.GhostSettings;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.game.GameVariantRenderConfig.createEntityView;
import static de.amr.pacmanfx.game.GameVariantRenderConfig.createPacView;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.gameOptions;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;

public class TengenMsPacMan_IntroScene extends AbstractGameScene {

    // Anchor point for everything
    public static final int ANCHOR_X = 76, ANCHOR_Y = 64;

    public static final int ACTOR_Y = ANCHOR_Y + 72;
    public static final int GHOST_STOP_X = ANCHOR_X - 18;
    public static final int MS_PAC_MAN_STOP_X = ANCHOR_X + 62;
    public static final float SPEED = 2.2f; //TODO check exact speed

    public final StateMachine<TengenMsPacMan_IntroScene> flow;

    public TengenMsPacMan_SpriteSheet spriteSheet;

    public Color[] ghostColors;

    private Marquee marquee;
    private Pac msPacMan;
    private List<Ghost> ghosts;

    public Vector2f presentsTextPosition;
    public int ghostIndex;
    private int waitBeforeRising;
    public boolean dark;

    public TengenMsPacMan_IntroScene() {
        setComp(GameSceneCanvasRenderingComp.class, new GameSceneCanvasRenderingComp());
        reqCanvasRendering().unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        reqCanvasRendering().unscaledHeightProperty().set(NES_SCREEN_HEIGHT);

        flow = new StateMachine<>(List.of(SceneState.values()));
    }

    public Ghost currentGhost() {
        return ghosts.get(ghostIndex);
    }

    @Override
    public Stream<Renderable> renderables() {
        return Ufx.streamOf(
            createEntityView(marquee, RenderingLayer.PROPS, 0),
            createPacView(msPacMan),
            ghosts.stream().map(GameVariantRenderConfig::createGhostView)
        );
    }

    @Override
    public void onActivate() {
        final GameVariantUIConfig variantConfig = app().variantManager().currentRuntime().uiConfig();

        game().session().setHudVisible(false);

        spriteSheet = TengenMsPacMan_SpriteSheet.instance();

        final var actions = app().variantManager().currentRuntime()
            .extensionValue(TengenMsPacMan_GameExtension.EXT_ACTIONS, TengenMsPacMan_Actions.class);

        final var bindingsMap = actionBindings().registry();
        bindingsMap.selectAnyMatchingBinding(actions.actionEnterStartScreen(), actions.localBindings());
        bindingsMap.selectAnyMatchingBinding(actions.actionToggleJoypadBindingsDisplayed(), actions.localBindings());

        final List<GhostSettings> ghostSettings = variantConfig.worldSettings().ghosts();
        ghostColors = Stream.of(
                GhostPersonality.RED_GHOST_SHADOW,
                GhostPersonality.PINK_GHOST_SPEEDY,
                GhostPersonality.CYAN_GHOST_BASHFUL,
                GhostPersonality.ORANGE_GHOST_POKEY)
            .map(personality -> ghostSettings.get(personality.ordinal()).colors().normal().dressColor())
            .toArray(Color[]::new);

        presentsTextPosition = new Vector2f(8 * TS, ANCHOR_Y - TS);

        createEntities();
        flow.restartState(this, SceneState.WAITING_FOR_START);
    }

    @Override
    public void onTick(GameContext game) {
        flow.update(this);
    }

    // --- private

    private void createEntities() {
        marquee = createMarquee();

        final var actorFactory = TengenMsPacMan_ActorFactory.instance();

        final GameVariantRuntime variant = app().variantManager().currentRuntime();
        final GameVariantRenderConfig renderConfig = variant.uiConfig().renderConfig();
        final SpriteAnimationContainer animContainer    = variant.spriteAnimContainer();
        final ActorSpriteAnimController animController  = variant.playConfig().systems().actorSpriteAnimController();

        msPacMan = actorFactory.createMsPacMan();
        animController.setAnimations(msPacMan, renderConfig.createPacAnimations(animContainer));

        ghosts = List.of(
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.RED_GHOST_SHADOW),
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.CYAN_GHOST_BASHFUL),
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.PINK_GHOST_SPEEDY),
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.ORANGE_GHOST_POKEY)
        );
    }

    private Marquee createMarquee() {
        final var marquee = new Marquee();

        marquee.pos().set(ANCHOR_X, ANCHOR_Y);

        marquee.layout().setNumBulbsHorizontally(34);
        marquee.layout().setNumBulbsVertically(16);
        marquee.layout().setBulbSize(4);
        marquee.layout().setBrightBulbsCount(6);
        marquee.layout().setBrightBulbsDistance(16);

        marquee.visualization().setBulbOnColor(NES_Palette.rgb(0x20));
        marquee.visualization().setBulbOffColor(NES_Palette.rgb(0x15));

        return marquee;
    }

    // --- State machine ---

    public enum SceneState implements State<TengenMsPacMan_IntroScene> {

        WAITING_FOR_START {

            @Override
            public void onEnter(TengenMsPacMan_IntroScene scene) {
                timer.restartTicks(TickTimer.INDEFINITE);
                scene.dark = false;
                scene.marquee.hide();
                scene.msPacMan.hide();
                scene.ghosts.forEach(Ghost::hide);
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
                final GameVariantRuntime variant = scene.app().variantManager().currentRuntime();
                final ActorSpriteAnimController animController = variant.playConfig().systems().actorSpriteAnimController();
                final GameSystems systems = variant.playConfig().systems();
                final WorldNavigationSystem nav = systems.navigator();

                timer.restartTicks(TickTimer.INDEFINITE);

                scene.marquee.show();

                scene.msPacMan.pos().set(TS * 33, ACTOR_Y);
                scene.msPacMan.show();

                nav.setMoveDir(scene.msPacMan, Direction.LEFT);
                nav.setSpeed(scene.msPacMan, SPEED);

                animController.select(scene.msPacMan, CommonSpriteAnimationID.PAC_MOUTH_MOVING);
                animController.playSelected(scene.msPacMan);

                for (Ghost ghost : scene.ghosts) {
                    ghost.pos().set(TS * 33, ACTOR_Y);
                    ghost.show();

                    nav.setMoveDir(ghost, Direction.LEFT);
                    nav.setWishDir(ghost, Direction.LEFT);
                    nav.setSpeed(ghost, SPEED);

                    animController.playSelected(ghost);
                    systems.ghostState().setState(ghost, GhostState.HUNTING_PAC);
                }
                scene.ghostIndex = 0;
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene scene) {
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
                final GameSystems systems = scene.game().playConfig().systems();
                final MovementSystem motor = systems.motor();
                final WorldNavigationSystem nav = systems.navigator();

                final Ghost ghost = scene.ghosts.get(scene.ghostIndex);
                if (ghost.worldNavigation().moveDir() == Direction.LEFT) {
                    if (ghost.pos().x() <= GHOST_STOP_X) {
                        ghost.pos().setX(GHOST_STOP_X);
                        nav.setMoveDir(ghost, Direction.UP);
                        nav.setWishDir(ghost, Direction.UP);
                        scene.waitBeforeRising = 2;
                    } else {
                        motor.move(ghost);
                        Logger.debug("{} moves {} x={}", ghost.name(), ghost.worldNavigation().moveDir(), ghost.pos().x());
                    }
                }
                else if (ghost.worldNavigation().moveDir() == Direction.UP) {
                    final int endPositionY = ANCHOR_Y + scene.ghostIndex * 16;
                    if (scene.waitBeforeRising > 0) {
                        scene.waitBeforeRising--;
                    }
                    else if (ghost.pos().y() <= endPositionY) {
                        nav.setSpeed(ghost, 0);
                        nav.setMoveDir(ghost, Direction.RIGHT);
                        nav.setWishDir(ghost, Direction.RIGHT);
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
                final GameContext game = scene.game();
                final GameFlow flow = game.playConfig().gameFlow();
                final GameSystems systems = game.playConfig().systems();
                final ActorSpriteAnimController animController = systems.actorSpriteAnimController();
                final MovementSystem motor = systems.motor();
                final WorldNavigationSystem nav = systems.navigator();

                motor.move(scene.msPacMan);
                if (scene.msPacMan.pos().x() <= MS_PAC_MAN_STOP_X) {
                    nav.setSpeed(scene.msPacMan, 0);
                    animController.resetSelected(scene.msPacMan);
                }
                if (timer.atSecond(8)) {
                    // start demo level or show options
                    if (gameOptions(game.session()).areInitial()) {
                        gameOptions(game.session()).setCanStartNewGame(false); // TODO check this
                        flow.restartState(game, Tengen_GameState.GAME_OR_LEVEL_STARTING.state());
                    } else {
                        flow.enterState(game, Tengen_GameState.GAME_PREPARATION.state());
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