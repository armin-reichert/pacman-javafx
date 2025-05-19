/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import de.amr.pacmanfx.ui._2d.SpriteAnimationSet;

import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.*;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.from;

public class ArcadeMsPacMan_GhostAnimations extends SpriteAnimationSet implements Animations {

    public ArcadeMsPacMan_GhostAnimations(GameSpriteSheet ss, byte personality) {
        super(ss);
        requireValidGhostPersonality(personality);
        add(ANIM_GHOST_NORMAL,     from(ss).take(ss.ghostNormalSprites(personality, Direction.LEFT)).frameTicks(8).endless());
        add(ANIM_GHOST_FRIGHTENED, from(ss).take(ss.ghostFrightenedSprites()).frameTicks(8).endless());
        add(ANIM_GHOST_FLASHING,   from(ss).take(ss.ghostFlashingSprites()).frameTicks(7).endless());
        add(ANIM_GHOST_EYES,       from(ss).take(ss.ghostEyesSprites(Direction.LEFT)).end());
        add(ANIM_GHOST_NUMBER,     from(ss).take(ss.ghostNumberSprites()).end());
        //TODO start animations when selected
        animation(ANIM_GHOST_EYES).play();
        animation(ANIM_GHOST_FRIGHTENED).play();
        animation(ANIM_GHOST_FLASHING).play();
    }

    @Override
    public void select(String id, int frameIndex) {
        super.select(id, frameIndex);
        if (ANIM_GHOST_NUMBER.equals(id)) {
            animation(ANIM_GHOST_NUMBER).setFrameIndex(frameIndex);
        }
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Ghost ghost) {
            GameSpriteSheet gss = (GameSpriteSheet) spriteSheet;
            switch (currentAnimationID) {
                case ANIM_GHOST_NORMAL -> currentAnimation().setSprites(gss.ghostNormalSprites(ghost.personality(), ghost.wishDir()));
                case ANIM_GHOST_EYES   -> currentAnimation().setSprites(gss.ghostEyesSprites(ghost.wishDir()));
            }
        }
    }
}