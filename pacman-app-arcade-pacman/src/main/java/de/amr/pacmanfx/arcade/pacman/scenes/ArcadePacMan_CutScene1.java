/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_PacSAM;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.game.GameVariant;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

/**
 * First cut scene in Arcade Pac-Man game:<br>
 * Red ghost chases Pac-Man from right to left over the screen,
 * then a frightened ghost is chased by a big Pac-Man from left to right.
 */
public class ArcadePacMan_CutScene1 extends GameScene {

    public Pac pacMan;
    public Ghost blinky;

    public ArcadePacMan_CutScene1(GameAppContext app) {
        super(app);
        setComp(CanvasRenderingComp.class, new CanvasRenderingComp());
        setComp(CutSceneTimingComp.class, new CutSceneTimingComp(120));
    }

    private CutSceneTimingComp timing() {
        return reqComp(CutSceneTimingComp.class);
    }
    
    @Override
    public void onActivate() {
        final GameVariant variant = app().gameVariants().currentGameVariant();
        final GameVariantRenderConfig renderConfig = variant.uiConfig().renderConfig();
        final SpriteAnimContainer animContainer = variant.spriteAnimContainer();
        final ActorSpriteAnimController animController = variant.config().systems().actorSpriteAnimController();
        final var factory = ArcadePacMan_ActorFactory.instance();

        pacMan = factory.createPacMan();
        pacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animContainer));

        blinky = renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.RED_GHOST_SHADOW);

        timing().setTick(-1);
    }

    @Override
    public void onTick(GameContext game) {
        final GameSystems systems = game.variant().systems();

        final CutSceneTimingComp timing = timing();
        timing.setTick(timing().tick() + 1);

        if (timing.tick() < timing.animationStartTick()) {
            return;
        }

        if (timing.tick() == timing.animationStartTick()) {
            soundManager().play(PacManGameSoundID.INTERMISSION_1, 2);
            startBlinkyChasingPacMan(systems);
        }
        else if (timing.tick() == timing.animationStartTick() + 260) {
            startBlinkyEscapingPacMan(systems);
        }
        else if (timing.tick() == timing.animationStartTick() + 400) {
            startBigPacManChasingBlinky(systems);
        }
        else if (timing.tick() == timing.animationStartTick() + 632) {
            game().state().triggerTimeout();
        }
        if (timing.tick() >= timing.animationStartTick()) {
            systems.motor().move(pacMan);
            systems.motor().move(blinky);
        }
    }

    private void startBigPacManChasingBlinky(GameSystems systems) {
        systems.navigator().placeAtTile(pacMan, -3, 18, 0, 6.5f);
        systems.navigator().setMoveDir(pacMan, Direction.RIGHT);

        systems.actorSpriteAnimController().select(pacMan, ArcadePacMan_PacSAM.AnimationID.ANIM_BIG_PAC_MAN);
        systems.actorSpriteAnimController().playSelected(pacMan);
    }

    private void startBlinkyEscapingPacMan(GameSystems systems) {
        systems.navigator().placeAtTile(blinky, -2, 20, 4, 0);
        systems.navigator().setMoveDir(blinky, Direction.RIGHT);
        systems.navigator().setWishDir(blinky, Direction.RIGHT);
        systems.navigator().setMoveDirSpeed(blinky, 0.75f);

        systems.actorSpriteAnimController().select(blinky, CommonSpriteAnimationID.GHOST_FRIGHTENED);
        systems.actorSpriteAnimController().playSelected(blinky);
    }

    private void startBlinkyChasingPacMan(GameSystems systems) {
        pacMan.show();

        systems.navigator().placeAtTile(pacMan, 29, 20);
        systems.navigator().setMoveDir(pacMan, Direction.LEFT);
        systems.navigator().setMoveDirSpeed(pacMan, 1.25f);

        systems.actorSpriteAnimController().select(pacMan, CommonSpriteAnimationID.PAC_MOUTH_MOVING);
        systems.actorSpriteAnimController().playSelected(pacMan);

        blinky.show();

        systems.navigator().placeAtTile(blinky, 32, 20);
        systems.navigator().setMoveDir(blinky, Direction.LEFT);
        systems.navigator().setWishDir(blinky, Direction.LEFT);
        systems.navigator().setMoveDirSpeed(blinky, 1.3f);

        systems.actorSpriteAnimController().select(blinky, CommonSpriteAnimationID.GHOST_NORMAL);
        systems.actorSpriteAnimController().playSelected(blinky);
    }
}