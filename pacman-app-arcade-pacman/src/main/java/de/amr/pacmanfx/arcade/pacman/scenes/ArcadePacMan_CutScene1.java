/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_PacAnimations;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.Movement;
import de.amr.pacmanfx.core.model.component.WorldMovement;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import static de.amr.pacmanfx.core.model.GameModel.RED_GHOST_SHADOW;

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
        pacMan = ArcadePacMan_ActorFactory.createPacMan();
        pacMan.animations = renderConfig.createPacAnimations(container);
        blinky = renderConfig.createAnimatedGhost(container, RED_GHOST_SHADOW);
        sceneTick = -1;
    }

    @Override
    public void onTick(GameContext gameContext) {
        if (++sceneTick < ANIMATION_START_TICK) {
            return;
        }
        if (sceneTick == ANIMATION_START_TICK) {
            appContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_1, 2);
            startBlinkyChasingPacMan();
        }
        else if (sceneTick == ANIMATION_START_TICK + 260) {
            startBlinkyEscapingPacMan();
        }
        else if (sceneTick == ANIMATION_START_TICK + 400) {
            startBigPacManChasingBlinky();
        }
        else if (sceneTick == ANIMATION_START_TICK + 632) {
            gameState().triggerTimeout();
        }
        if (sceneTick >= ANIMATION_START_TICK) {
            Movement.SYSTEM.moveAccelerated(pacMan);
            Movement.SYSTEM.moveAccelerated(blinky);
        }
    }

    private void startBigPacManChasingBlinky() {
        WorldMovement.SYSTEM.placeAtTile(pacMan, -3, 18, 0, 6.5f);
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.animations.select(ArcadePacMan_PacAnimations.AnimationID.ANIM_BIG_PAC_MAN);
        pacMan.animations.playSelected();
    }

    private void startBlinkyEscapingPacMan() {
        WorldMovement.SYSTEM.placeAtTile(blinky, -2, 20, 4, 0);
        blinky.setMoveDir(Direction.RIGHT);
        blinky.setWishDir(Direction.RIGHT);
        WorldMovement.SYSTEM.setSpeed(blinky, 0.75f);
        blinky.animations.select(CommonAnimationID.GHOST_FRIGHTENED);
        blinky.animations.playSelected();
    }

    private void startBlinkyChasingPacMan() {
        WorldMovement.SYSTEM.placeAtTile(pacMan, 29, 20);
        pacMan.setMoveDir(Direction.LEFT);
        WorldMovement.SYSTEM.setSpeed(pacMan, 1.25f);
        pacMan.animations.select(CommonAnimationID.PAC_MUNCHING);
        pacMan.animations.playSelected();
        pacMan.visibility.show();

        WorldMovement.SYSTEM.placeAtTile(blinky, 32, 20);
        blinky.setMoveDir(Direction.LEFT);
        blinky.setWishDir(Direction.LEFT);
        WorldMovement.SYSTEM.setSpeed(blinky, 1.3f);
        blinky.animations.select(CommonAnimationID.GHOST_NORMAL);
        blinky.animations.playSelected();
        blinky.visibility.show();
    }
}