/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.pacman.rendering.SpriteID;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingComp;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.ecs.comp.SpriteAnimationComp;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.spriteanim.LazySAM;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimationBuilder;
import de.amr.pacmanfx.game.GameVariant;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.CutScene;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;


/**
 * Second cut scene in Arcade Pac-Man game:<br>
 * Red ghost chases Pac-Man from right to left over screen, at the middle of the screen, a nail
 * is stopping the red ghost, its dress gets stretched and eventually raptures.
 */
public class ArcadePacMan_CutScene2 extends CutScene {

    public enum NailDressState {
        NAIL, STRETCHED_SMALL, STRETCHED_MEDIUM, STRETCHED_LARGE, RAPTURED
    }

    public static class TimingComp extends CutSceneTimingComp {

        private final int TICK_PAC_MAN_STARTS_RUNNING;
        private final int TICK_BLINKY_STARTS_RUNNING;
        private final int TICK_BLINKY_GETS_CAUGHT;
        private final int TICK_DRESS_STRETCHED_SMALL;
        private final int TICK_DRESS_STRETCHED_MEDIUM;
        private final int TICK_DRESS_STRETCHED_LARGE;
        private final int TICK_BLINKY_STOPS_MOVING;
        private final int TICK_DRESS_RAPTURES;
        private final int TICK_BLINK_INSPECTS_DAMAGE;
        private final int TICK_ANIMATION_ENDS;

        public TimingComp(int animationStartTick) {
            super(120);
            TICK_PAC_MAN_STARTS_RUNNING = animationStartTick + 25;
            TICK_BLINKY_STARTS_RUNNING  = animationStartTick + 111;
            TICK_BLINKY_GETS_CAUGHT     = animationStartTick + 194;
            TICK_DRESS_STRETCHED_SMALL  = animationStartTick + 198;
            TICK_DRESS_STRETCHED_MEDIUM = animationStartTick + 230;
            TICK_DRESS_STRETCHED_LARGE  = animationStartTick + 262;
            TICK_BLINKY_STOPS_MOVING    = animationStartTick + 296;
            TICK_DRESS_RAPTURES         = animationStartTick + 360;
            TICK_BLINK_INSPECTS_DAMAGE  = animationStartTick + 420;
            TICK_ANIMATION_ENDS         = animationStartTick + 508;
        }
    }

    public final int nailX = WorldMap.TS * 15 - 1;
    public final int nailY = WorldMap.TS * 20 - 1;

    static class DressAnimation extends LazySAM {

        public DressAnimation(SpriteAnimContainer container) {
            setFactory(id -> switch (id) {

                case SpriteID.RED_GHOST_STRETCHED -> new SpriteAnimationBuilder()
                    .sprites(ArcadePacMan_SpriteSheet.instance().findSpriteSequence(SpriteID.RED_GHOST_STRETCHED))
                    .initiallyStopped()
                    .build(container);

                default -> throw new IllegalArgumentException("Unknown animation ID: " + id);
            });
        }
    }

    static class NailDress extends GameEntity {

        public NailDress(SpriteAnimContainer animContainer) {
            setComp(RenderingComp.class, new RenderingComp(RenderingLayer.PROPS));
            setComp(SpriteAnimationComp.class, new SpriteAnimationComp());

            reqComp(SpriteAnimationComp.class).setSpriteAnimations(new DressAnimation(animContainer));
            setState(NailDressState.NAIL);
        }

        public void setState(NailDressState state) {
            final int frame = state.ordinal();
            reqComp(SpriteAnimationComp.class).spriteAnimations().setAnimationFrame(SpriteID.RED_GHOST_STRETCHED, frame);
        }
    }

    private Pac pacMan;
    private Ghost blinky;
    private NailDress nailDress;

    public ArcadePacMan_CutScene2(GameAppContext app) {
        super(app);
        setComp(CanvasRenderingComp.class, new CanvasRenderingComp());
        setComp(CutSceneTimingComp.class, new TimingComp(120));
    }

    private TimingComp timing() {
        return (TimingComp) reqComp(CutSceneTimingComp.class);
    }

    @Override
    public void onActivate() {
        final GameVariant variant = app().gameVariants().currentGameVariant();
        final GameVariantRenderConfig renderConfig = variant.uiConfig().renderConfig();
        final SpriteAnimContainer animContainer = variant.spriteAnimContainer();
        final ActorSpriteAnimController animController = variant.config().systems().actorSpriteAnimController();
        final var actorFactory = ArcadePacMan_ActorFactory.instance();

        pacMan = actorFactory.createPacMan();
        pacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animContainer));

        blinky = renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.RED_GHOST_SHADOW);

        nailDress = new NailDress(animContainer);
        nailDress.pos().set(nailX, nailY);
        nailDress.show();

        entities().clear();
        entities().addAll(pacMan, blinky, nailDress);

        timing().setTick(-1);
    }

    @Override
    public void onTick(GameContext game) {
        final GameSystems systems = game.variant().systems();
        final TimingComp timing = timing();

        timing.setTick(timing.tick() + 1);

        if (timing.tick() < timing.animationStartTick()) {
            return;
        }

        if (timing.tick() == timing.animationStartTick()) {
            startTheShow();
        } else if (timing.tick() == timing.TICK_PAC_MAN_STARTS_RUNNING) {
            pacManStartsRunning(systems);
        } else if (timing.tick() == timing.TICK_BLINKY_STARTS_RUNNING) {
            blinkyStartsRunning(systems);
        } else if (timing.tick() == timing.TICK_BLINKY_GETS_CAUGHT) {
            blinkyGetsCaughtOnNail(systems);
        } else if (timing.tick() == timing.TICK_DRESS_STRETCHED_SMALL) {
            nailDress.setState(NailDressState.STRETCHED_SMALL);
        } else if (timing.tick() == timing.TICK_DRESS_STRETCHED_MEDIUM) {
            nailDress.setState(NailDressState.STRETCHED_MEDIUM);
        } else if (timing.tick() == timing.TICK_DRESS_STRETCHED_LARGE) {
            nailDress.setState(NailDressState.STRETCHED_LARGE);
        } else if (timing.tick() == timing.TICK_BLINKY_STOPS_MOVING) {
            blinkyStopsMoving(systems);
        } else if (timing.tick() == timing.TICK_DRESS_RAPTURES) {
            dressRaptures(systems);
        } else if (timing.tick() == timing.TICK_BLINK_INSPECTS_DAMAGE) {
            blinkyInspectsDamagedDress(systems.actorSpriteAnimController());
        } else if (timing.tick() == timing.TICK_ANIMATION_ENDS) {
            endTheShow();
        }
        systems.motor().move(pacMan);
        systems.motor().move(blinky);
    }

    private void blinkyInspectsDamagedDress(ActorSpriteAnimController animSystem) {
        animSystem.advanceFrame(blinky);
    }

    private void startTheShow() {
        soundManager().play(PacManGameSoundID.INTERMISSION_2);
        nailDress.setState(NailDressState.NAIL);
    }

    private void endTheShow() {
        blinky.hide();
        game().state().triggerTimeout();
    }

    private void dressRaptures(GameSystems systems) {
        blinky.pos().sub(4, 0);
        systems.actorSpriteAnimController().select(blinky, CommonSpriteAnimationID.BLINKY_DAMAGED);
        nailDress.setState(NailDressState.RAPTURED);
    }

    private void blinkyStopsMoving(GameSystems systems) {
        systems.navigator().setMoveDirSpeed(blinky, 0);
        systems.actorSpriteAnimController().stopSelected(blinky);
    }

    private void blinkyGetsCaughtOnNail(GameSystems systems) {
        systems.navigator().setMoveDirSpeed(blinky, 0.09f);
        //TODO
        //blinkyAnimation(CommonAnimationID.GHOST_NORMAL).setFrameDurationTicks(32);
    }

    private void blinkyStartsRunning(GameSystems systems) {
        blinky.show();

        systems.navigator().placeAtTile(blinky, 28, 20, -3, 0);
        systems.navigator().setMoveDir(blinky, Direction.LEFT);
        systems.navigator().setWishDir(blinky, Direction.LEFT);
        systems.navigator().setMoveDirSpeed(blinky, 1.25f);

        systems.actorSpriteAnimController().select(blinky, CommonSpriteAnimationID.GHOST_NORMAL);
        systems.actorSpriteAnimController().playSelected(blinky);
    }

    private void pacManStartsRunning(GameSystems systems) {
        pacMan.show();

        systems.navigator().placeAtTile(pacMan, 28, 20);
        systems.navigator().setMoveDir(pacMan, Direction.LEFT);
        systems.navigator().setMoveDirSpeed(pacMan, 1.15f);

        systems.actorSpriteAnimController().select(pacMan, CommonSpriteAnimationID.PAC_MOUTH_MOVING);
        systems.actorSpriteAnimController().playSelected(pacMan);
    }
}