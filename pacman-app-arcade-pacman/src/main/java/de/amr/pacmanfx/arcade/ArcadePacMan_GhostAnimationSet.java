/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import de.amr.pacmanfx.ui._2d.SpriteAnimationSet;

import static de.amr.pacmanfx.arcade.ArcadePacMan_UIConfig.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.*;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.from;

public class ArcadePacMan_GhostAnimationSet extends SpriteAnimationSet {

    public ArcadePacMan_GhostAnimationSet(ArcadePacMan_SpriteSheet ss, byte personality) {
        super(ss);
        set(ANIM_GHOST_NORMAL,              from(ss).take(ss.ghostNormalSprites(personality, Direction.LEFT)).frameTicks(8).endless());
        set(ANIM_GHOST_FRIGHTENED,          from(ss).take(ss.ghostFrightenedSprites()).frameTicks(8).endless());
        set(ANIM_GHOST_FLASHING,            from(ss).take(ss.ghostFlashingSprites()).frameTicks(7).endless());
        set(ANIM_GHOST_EYES,                from(ss).take(ss.ghostEyesSprites(Direction.LEFT)).end());
        set(ANIM_GHOST_NUMBER,              from(ss).take(ss.ghostNumberSprites()).end());
        set(ANIM_BLINKY_DAMAGED,            from(ss).take(ss.blinkyDamagedSprites()).end());
        set(ANIM_BLINKY_NAIL_DRESS_RAPTURE, from(ss).take(ss.blinkyStretchedSprites()).end());
        set(ANIM_BLINKY_PATCHED,            from(ss).take(ss.blinkyPatchedSprites()).frameTicks(4).endless());
        set(ANIM_BLINKY_NAKED,              from(ss).take(ss.blinkyNakedSprites()).frameTicks(4).endless());
    }

    @Override
    public void selectAnimationAtFrame(String id, int frameIndex) {
        super.selectAnimationAtFrame(id, frameIndex);
        if (ANIM_GHOST_NUMBER.equals(id)) {
            animation(ANIM_GHOST_NUMBER).setFrameIndex(frameIndex);
        }
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Ghost ghost) {
            GameSpriteSheet gss = (GameSpriteSheet) spriteSheet;
            if (isCurrentAnimationID(ANIM_GHOST_NORMAL)) {
                currentAnimation().setSprites(gss.ghostNormalSprites(ghost.personality(), ghost.wishDir()));
            }
            if (isCurrentAnimationID(ANIM_GHOST_EYES)) {
                currentAnimation().setSprites(gss.ghostEyesSprites(ghost.wishDir()));
            }
        }
    }
}