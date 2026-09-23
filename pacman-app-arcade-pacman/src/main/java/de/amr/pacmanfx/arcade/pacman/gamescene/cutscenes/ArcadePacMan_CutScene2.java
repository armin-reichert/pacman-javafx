/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.cutscenes;

import de.amr.basics.math.Direction;
import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.spriteanim.CommonSpriteAnimationID;
import de.amr.basics.ui.spriteanim.SpriteAnimationContainer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.game.GameVariantRuntime;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import java.util.stream.Stream;

import static de.amr.pacmanfx.game.GameVariantRenderConfig.createPropView;


/**
 * Second cut scene in Arcade Pac-Man game:<br>
 * Red ghost chases Pac-Man from right to left over screen, at the middle of the screen, a nail
 * is stopping the red ghost, its dress gets stretched and eventually raptures.
 */
public class ArcadePacMan_CutScene2 extends AbstractGameScene {

    public final float nailX = WorldMap.TS * 14;
    public final float nailY = WorldMap.TS * 19.5f - 2;

    private Pac pacMan;
    private Ghost blinky;
    private NailDressRapturing nailDressRapturing;

    public ArcadePacMan_CutScene2() {
        setComp(GameSceneCanvasRenderingComp.class, new GameSceneCanvasRenderingComp());
        setComp(CutSceneTimingComp.class, new CutScene2TimingComp(120));
    }

    @Override
    public Stream<Renderable> renderables() {
        return Ufx.streamOf(
            createPropView(pacMan),
            createPropView(blinky),
            createPropView(nailDressRapturing, -1) // behind ghost
        );
    }

    private CutScene2TimingComp timing() {
        return (CutScene2TimingComp) reqComp(CutSceneTimingComp.class);
    }

    @Override
    public void onActivate() {
        final GameVariantRuntime variant = app().variantManager().currentRuntime();
        final GameVariantRenderConfig renderConfig = variant.uiConfig().renderConfig();
        final SpriteAnimationContainer animContainer = variant.spriteAnimContainer();
        final ActorSpriteAnimController animController = variant.playConfig().systems().actorSpriteAnimController();
        final var actorFactory = ArcadePacMan_ActorFactory.instance();

        pacMan = actorFactory.createPacMan();
        pacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animContainer));

        blinky = renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.RED_GHOST_SHADOW);

        nailDressRapturing = new NailDressRapturing(animContainer);
        nailDressRapturing.pos().set(nailX, nailY);
        nailDressRapturing.show();

        timing().setTick(-1);
    }

    @Override
    public void onTick(GameContext game) {
        final GameSystems systems = game.playConfig().systems();
        final CutScene2TimingComp timing = timing();

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
            nailDressRapturing.setState(NailDressRapturingState.STRETCHED_SMALL);
        } else if (timing.tick() == timing.TICK_DRESS_STRETCHED_MEDIUM) {
            nailDressRapturing.setState(NailDressRapturingState.STRETCHED_MEDIUM);
        } else if (timing.tick() == timing.TICK_DRESS_STRETCHED_LARGE) {
            nailDressRapturing.setState(NailDressRapturingState.STRETCHED_LARGE);
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
        nailDressRapturing.setState(NailDressRapturingState.NAIL);
    }

    private void endTheShow() {
        blinky.hide();
        game().state().triggerTimeout();
    }

    private void dressRaptures(GameSystems systems) {
        blinky.pos().sub(4, 0);
        systems.actorSpriteAnimController().select(blinky, CommonSpriteAnimationID.BLINKY_DAMAGED);
        nailDressRapturing.setState(NailDressRapturingState.RAPTURED);
    }

    private void blinkyStopsMoving(GameSystems systems) {
        systems.navigator().setSpeed(blinky, 0);
        systems.actorSpriteAnimController().stopSelected(blinky);
    }

    private void blinkyGetsCaughtOnNail(GameSystems systems) {
        systems.navigator().setSpeed(blinky, 0.09f);
        //TODO
        //blinkyAnimation(CommonAnimationID.GHOST_NORMAL).setFrameDurationTicks(32);
    }

    private void blinkyStartsRunning(GameSystems systems) {
        blinky.show();

        systems.navigator().placeAtTile(blinky, 28, 20, -3, 0);
        systems.navigator().setMoveDir(blinky, Direction.LEFT);
        systems.navigator().setWishDir(blinky, Direction.LEFT);
        systems.navigator().setSpeed(blinky, 1.25f);

        systems.actorSpriteAnimController().select(blinky, CommonSpriteAnimationID.GHOST_NORMAL);
        systems.actorSpriteAnimController().playSelected(blinky);
    }

    private void pacManStartsRunning(GameSystems systems) {
        pacMan.show();

        systems.navigator().placeAtTile(pacMan, 28, 20);
        systems.navigator().setMoveDir(pacMan, Direction.LEFT);
        systems.navigator().setSpeed(pacMan, 1.15f);

        systems.actorSpriteAnimController().select(pacMan, CommonSpriteAnimationID.PAC_MOUTH_MOVING);
        systems.actorSpriteAnimController().playSelected(pacMan);
    }
}