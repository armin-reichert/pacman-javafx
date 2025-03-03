/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.actors.Actor2D;
import de.amr.games.pacman.model.actors.ActorAnimations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.SpriteAnimationSet;
import de.amr.games.pacman.uilib.SpriteAnimation;
import de.amr.games.pacman.uilib.SpriteSheet;

import java.util.Map;

/**
 * @author Armin Reichert
 */
public class GhostAnimations extends SpriteAnimationSet {

    public GhostAnimations(ArcadePacMan_SpriteSheet spriteSheet, byte ghostID) {

        var normal = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Normal ghost")
            .sprites(spriteSheet.ghostNormalSprites(ghostID, Direction.LEFT))
            .frameTicks(8)
            .endLoop();

        var frightened = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Frightened ghost")
            .sprites(spriteSheet.ghostFrightenedSprites())
            .frameTicks(8)
            .endLoop();

        var flashing = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Flashing ghost")
            .sprites(spriteSheet.ghostFlashingSprites())
            .frameTicks(7)
            .endLoop();

        var eyes = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Eyes-only ghost")
            .sprites(spriteSheet.ghostEyesSprites(Direction.LEFT))
            .end();

        var number = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Number for dead ghost")
            .sprites(spriteSheet.ghostNumberSprites())
            .end();

        var damaged = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Damaged dress ghost")
            .sprites(spriteSheet.blinkyDamagedSprites())
            .end();

        var stretching = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Stretching dress ghost")
            .sprites(spriteSheet.blinkyStretchedSprites())
            .end();

        var patched = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Patched dress ghost")
            .sprites(spriteSheet.blinkyPatchedSprites())
            .frameTicks(4)
            .endLoop();

        var naked = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Naked ghost")
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