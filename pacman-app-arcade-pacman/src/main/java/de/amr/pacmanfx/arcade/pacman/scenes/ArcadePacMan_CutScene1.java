/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_PacAnimations;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnim;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

/**
 * First cut scene in Arcade Pac-Man game:<br>
 * Red ghost chases Pac-Man from right to left over the screen,
 * then a frightened ghost is chased by a big Pac-Man from left to right.
 */
public class ArcadePacMan_CutScene1 extends AbstractGameScene2D {

    public static final short ANIMATION_START_TICK = 120;

    public int sceneTick;
    public Pac pacMan;
    public Ghost blinky;

    public ArcadePacMan_CutScene1(GameAppContext appContext) {
        super(appContext);
    }
    
    @Override
    public void onActivate() {
        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer container = appContext().ui().sprites().animations();
        final var factory = ArcadePacMan_ActorFactory.instance();

        pacMan = factory.createPacMan();
        pacMan.assertComponent(SpriteAnim.class).setAnimations(renderConfig.createPacAnimations(container));

        blinky = renderConfig.createAnimatedGhost(gameContext(), container, GhostPersonality.RED_GHOST_SHADOW);

        sceneTick = -1;
    }

    @Override
    public void onTick(GameContext gameContext) {
        if (++sceneTick < ANIMATION_START_TICK) {
            return;
        }

        final GameSystems sys = gameContext.systems();

        if (sceneTick == ANIMATION_START_TICK) {
            appContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_1, 2);
            startBlinkyChasingPacMan(sys);
        }
        else if (sceneTick == ANIMATION_START_TICK + 260) {
            startBlinkyEscapingPacMan(sys);
        }
        else if (sceneTick == ANIMATION_START_TICK + 400) {
            startBigPacManChasingBlinky(sys);
        }
        else if (sceneTick == ANIMATION_START_TICK + 632) {
            gameState().triggerTimeout();
        }
        if (sceneTick >= ANIMATION_START_TICK) {
            sys.motor().moveAccelerated(pacMan);
            sys.motor().moveAccelerated(blinky);
        }
    }

    private void startBigPacManChasingBlinky(GameSystems sys) {
        sys.navigator().placeAtTile(pacMan, -3, 18, 0, 6.5f);
        sys.navigator().setMoveDir(pacMan, Direction.RIGHT);

        sys.spriteAnim().select(pacMan, ArcadePacMan_PacAnimations.AnimationID.ANIM_BIG_PAC_MAN);
        sys.spriteAnim().playSelected(pacMan);
    }

    private void startBlinkyEscapingPacMan(GameSystems sys) {
        sys.navigator().placeAtTile(blinky, -2, 20, 4, 0);
        sys.navigator().setMoveDir(blinky, Direction.RIGHT);
        sys.navigator().setWishDir(blinky, Direction.RIGHT);
        sys.navigator().setSpeed(blinky, 0.75f);

        sys.spriteAnim().select(blinky, CommonAnimationID.GHOST_FRIGHTENED);
        sys.spriteAnim().playSelected(blinky);
    }

    private void startBlinkyChasingPacMan(GameSystems sys) {
        pacMan.visibility().show();

        sys.navigator().placeAtTile(pacMan, 29, 20);
        sys.navigator().setMoveDir(pacMan, Direction.LEFT);
        sys.navigator().setSpeed(pacMan, 1.25f);

        sys.spriteAnim().select(pacMan, CommonAnimationID.PAC_MUNCHING);
        sys.spriteAnim().playSelected(pacMan);

        blinky.visibility().show();

        sys.navigator().placeAtTile(blinky, 32, 20);
        sys.navigator().setMoveDir(blinky, Direction.LEFT);
        sys.navigator().setWishDir(blinky, Direction.LEFT);
        sys.navigator().setSpeed(blinky, 1.3f);

        sys.spriteAnim().select(blinky, CommonAnimationID.GHOST_NORMAL);
        sys.spriteAnim().playSelected(blinky);
    }
}