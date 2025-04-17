/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.actors.Actor2D;
import de.amr.games.pacman.model.actors.ActorAnimations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui._2d.GameSpriteSheet;
import de.amr.games.pacman.ui._2d.SpriteAnimationSet;
import de.amr.games.pacman.uilib.animation.SpriteAnimation;
import de.amr.games.pacman.uilib.assets.SpriteSheet;

import java.util.Map;

/**
 * @author Armin Reichert
 */
public class ArcadePacMan_GhostAnimations extends SpriteAnimationSet {

    public ArcadePacMan_GhostAnimations(ArcadePacMan_SpriteSheet spriteSheet, byte ghostID) {

        var normal = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.ghostNormalSprites(ghostID, Direction.LEFT))
            .frameTicks(8)
            .endLoop();

        var frightened = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.ghostFrightenedSprites())
            .frameTicks(8)
            .endLoop();

        var flashing = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.ghostFlashingSprites())
            .frameTicks(7)
            .endLoop();

        var eyes = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.ghostEyesSprites(Direction.LEFT))
            .end();

        var number = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.ghostNumberSprites())
            .end();

        var damaged = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.blinkyDamagedSprites())
            .end();

        var stretching = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.blinkyStretchedSprites())
            .end();

        var patched = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.blinkyPatchedSprites())
            .frameTicks(4)
            .endLoop();

        var naked = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.blinkyNakedSprites())
            .frameTicks(4)
            .endLoop();

        add(Map.of(
            ActorAnimations.ANIM_GHOST_NORMAL, normal,
            ActorAnimations.ANIM_GHOST_FRIGHTENED, frightened,
            ActorAnimations.ANIM_GHOST_FLASHING, flashing,
            ActorAnimations.ANIM_GHOST_EYES, eyes,
            ActorAnimations.ANIM_GHOST_NUMBER, number,
            ActorAnimations.ANIM_BLINKY_DAMAGED, damaged,
            ActorAnimations.ANIM_BLINKY_STRETCHED, stretching,
            ActorAnimations.ANIM_BLINKY_PATCHED, patched,
            ActorAnimations.ANIM_BLINKY_NAKED, naked));

        eyes.play();
        frightened.play();
        flashing.play();
    }

    @Override
    public void select(String id, int frameIndex) {
        super.select(id, frameIndex);
        if (ActorAnimations.ANIM_GHOST_NUMBER.equals(id)) {
            animation(ActorAnimations.ANIM_GHOST_NUMBER).setFrameIndex(frameIndex);
        }
    }

    @Override
    protected RectArea[] selectedSprites(SpriteSheet spriteSheet, Actor2D actor) {
        GameSpriteSheet gss = (GameSpriteSheet) spriteSheet;
        if (actor instanceof Ghost ghost) {
            if (isCurrentAnimationID(ActorAnimations.ANIM_GHOST_NORMAL)) {
                return gss.ghostNormalSprites(ghost.id(), ghost.wishDir());
            }
            if (isCurrentAnimationID(ActorAnimations.ANIM_GHOST_EYES)) {
                return gss.ghostEyesSprites(ghost.wishDir());
            }
        }
        return super.selectedSprites(spriteSheet, actor);
    }
}