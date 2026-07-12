/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_PacAnimations;
import de.amr.pacmanfx.core.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.ui.game.GameVariantConfig;
import de.amr.pacmanfx.ui.game.PacManGamesCollection;
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

    public ArcadePacMan_CutScene1(PacManGamesCollection game) {
        super(game);
    }
    
    @Override
    public void onActivate() {
        final SpriteAnimationContainer spriteAnimationContainer = game().ui().sprites().animations();
        pacMan = ArcadePacMan_ActorFactory.createPacMan();
        GameVariantConfig variantConfig = game().variants().currentVariant().config();
        pacMan.setAnimations(variantConfig.createPacAnimations(spriteAnimationContainer));
        blinky = variantConfig.createAnimatedGhost(spriteAnimationContainer, RED_GHOST_SHADOW);
        sceneTick = -1;
    }

    @Override
    public void onTick(long tick) {
        if (++sceneTick < ANIMATION_START_TICK) {
            return;
        }
        if (sceneTick == ANIMATION_START_TICK) {
            game().ui().sounds().play(PacManGameSoundID.INTERMISSION_1, 2);
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
            pacMan.move();
            blinky.move();
        }
    }

    private void startBigPacManChasingBlinky() {
        pacMan.placeAtTile(-3, 18, 0, 6.5f);
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.animations().select(ArcadePacMan_PacAnimations.AnimationID.ANIM_BIG_PAC_MAN);
        pacMan.animations().playSelected();
    }

    private void startBlinkyEscapingPacMan() {
        blinky.placeAtTile(-2, 20, 4, 0);
        blinky.setMoveDir(Direction.RIGHT);
        blinky.setWishDir(Direction.RIGHT);
        blinky.setSpeed(0.75f);
        blinky.animations().select(ArcadePacMan_AnimationID.GHOST_FRIGHTENED);
        blinky.animations().playSelected();
    }

    private void startBlinkyChasingPacMan() {
        pacMan.placeAtTile(29, 20);
        pacMan.setMoveDir(Direction.LEFT);
        pacMan.setSpeed(1.25f);
        pacMan.animations().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
        pacMan.animations().playSelected();
        pacMan.show();

        blinky.placeAtTile(32, 20);
        blinky.setMoveDir(Direction.LEFT);
        blinky.setWishDir(Direction.LEFT);
        blinky.setSpeed(1.3f);
        blinky.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
        blinky.animations().playSelected();
        blinky.show();
    }
}