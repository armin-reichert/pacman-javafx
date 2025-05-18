/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Animations;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostAnimationID;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import de.amr.pacmanfx.ui._2d.SpriteAnimationSet;
import javafx.animation.Animation;

import java.util.stream.Stream;

import static de.amr.pacmanfx.arcade.ArcadePacMan_GhostAnimations.AnimationID.ANIM_BLINKY_PATCHED;
import static de.amr.pacmanfx.model.actors.GhostAnimationID.*;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.from;

public class ArcadePacMan_GhostAnimations extends SpriteAnimationSet implements Animations {

    public interface AnimationID {
        String ANIM_BLINKY_DAMAGED            = "damaged";
        String ANIM_BLINKY_PATCHED            = "patched";
        String ANIM_BLINKY_NAIL_DRESS_RAPTURE = "nail_dress_rapture";
        String ANIM_BLINKY_NAKED              = "naked";
    }

    public ArcadePacMan_GhostAnimations(ArcadePacMan_SpriteSheet ss, byte personality) {
        super(ss);
        add(ANIM_GHOST_NORMAL,              from(ss).take(ss.ghostNormalSprites(personality, Direction.LEFT)).frameTicks(8).endless());
        add(ANIM_GHOST_FRIGHTENED,          from(ss).take(ss.ghostFrightenedSprites()).frameTicks(8).endless());
        add(ANIM_GHOST_FLASHING,            from(ss).take(ss.ghostFlashingSprites()).frameTicks(7).endless());
        add(ANIM_GHOST_EYES,                from(ss).take(ss.ghostEyesSprites(Direction.LEFT)).end());
        add(ANIM_GHOST_NUMBER,              from(ss).take(ss.ghostNumberSprites()).end());
        add(AnimationID.ANIM_BLINKY_DAMAGED,            from(ss).take(ss.blinkyDamagedSprites()).end());
        add(AnimationID.ANIM_BLINKY_NAIL_DRESS_RAPTURE, from(ss).take(ss.blinkyStretchedSprites()).end());
        add(ANIM_BLINKY_PATCHED,            from(ss).take(ss.blinkyPatchedSprites()).frameTicks(4).endless());
        add(AnimationID.ANIM_BLINKY_NAKED,              from(ss).take(ss.blinkyNakedSprites()).frameTicks(4).endless());
        //TODO start animations when selected
        Stream.of(ANIM_GHOST_EYES, ANIM_GHOST_FRIGHTENED, ANIM_GHOST_FLASHING).map(this::animation).forEach(Animation::play);
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
            if (isCurrentAnimationID(ANIM_GHOST_NORMAL)) {
                currentAnimation().setSprites(gss.ghostNormalSprites(ghost.personality(), ghost.wishDir()));
            }
            if (isCurrentAnimationID(ANIM_GHOST_EYES)) {
                currentAnimation().setSprites(gss.ghostEyesSprites(ghost.wishDir()));
            }
        }
    }
}