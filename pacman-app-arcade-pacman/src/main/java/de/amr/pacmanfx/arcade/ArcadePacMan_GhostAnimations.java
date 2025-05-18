/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostAnimations;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import de.amr.pacmanfx.ui._2d.SpriteAnimationSet;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

import java.util.Map;

/**
 * @author Armin Reichert
 */
public class ArcadePacMan_GhostAnimations extends SpriteAnimationSet implements GhostAnimations {

    // Pac-Man game specific
    public static final String ANIM_BLINKY_DAMAGED   = "damaged";
    public static final String ANIM_BLINKY_NAIL_DRESS_RAPTURE = "stretched";
    public static final String ANIM_BLINKY_PATCHED   = "patched";
    public static final String ANIM_BLINKY_NAKED     = "naked";

    public ArcadePacMan_GhostAnimations(ArcadePacMan_SpriteSheet spriteSheet, byte personality) {

        var normal = SpriteAnimation
            .from(spriteSheet)
            .take(spriteSheet.ghostNormalSprites(personality, Direction.LEFT))
            .frameTicks(8)
            .endless();

        var frightened = SpriteAnimation
            .from(spriteSheet)
            .take(spriteSheet.ghostFrightenedSprites())
            .frameTicks(8)
            .endless();

        var flashing = SpriteAnimation
            .from(spriteSheet)
            .take(spriteSheet.ghostFlashingSprites())
            .frameTicks(7)
            .endless();

        var eyes = SpriteAnimation
            .from(spriteSheet)
            .take(spriteSheet.ghostEyesSprites(Direction.LEFT))
            .end();

        var number = SpriteAnimation
            .from(spriteSheet)
            .take(spriteSheet.ghostNumberSprites())
            .end();

        var damaged = SpriteAnimation
            .from(spriteSheet)
            .take(spriteSheet.blinkyDamagedSprites())
            .end();

        var stretching = SpriteAnimation
            .from(spriteSheet)
            .take(spriteSheet.blinkyStretchedSprites())
            .end();

        var patched = SpriteAnimation
            .from(spriteSheet)
            .take(spriteSheet.blinkyPatchedSprites())
            .frameTicks(4)
            .endless();

        var naked = SpriteAnimation
            .from(spriteSheet)
            .take(spriteSheet.blinkyNakedSprites())
            .frameTicks(4)
            .endless();

        add(Map.of(
            GhostAnimations.ANIM_GHOST_NORMAL, normal,
            GhostAnimations.ANIM_GHOST_FRIGHTENED, frightened,
            GhostAnimations.ANIM_GHOST_FLASHING, flashing,
            GhostAnimations.ANIM_GHOST_EYES, eyes,
            GhostAnimations.ANIM_GHOST_NUMBER, number,
            ANIM_BLINKY_DAMAGED, damaged,
            ANIM_BLINKY_NAIL_DRESS_RAPTURE, stretching,
            ANIM_BLINKY_PATCHED, patched,
            ANIM_BLINKY_NAKED, naked));

        eyes.play();
        frightened.play();
        flashing.play();
    }

    @Override
    public void select(String id, int frameIndex) {
        super.select(id, frameIndex);
        if (GhostAnimations.ANIM_GHOST_NUMBER.equals(id)) {
            animation(GhostAnimations.ANIM_GHOST_NUMBER).setFrameIndex(frameIndex);
        }
    }

    @Override
    protected RectArea[] selectedSprites(SpriteSheet spriteSheet, Actor actor) {
        GameSpriteSheet gss = (GameSpriteSheet) spriteSheet;
        if (actor instanceof Ghost ghost) {
            if (isCurrentAnimationID(GhostAnimations.ANIM_GHOST_NORMAL)) {
                return gss.ghostNormalSprites(ghost.personality(), ghost.wishDir());
            }
            if (isCurrentAnimationID(GhostAnimations.ANIM_GHOST_EYES)) {
                return gss.ghostEyesSprites(ghost.wishDir());
            }
        }
        return super.selectedSprites(spriteSheet, actor);
    }
}