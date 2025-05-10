/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.actors.Actor;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostAnimations;
import de.amr.games.pacman.ui._2d.GameSpriteSheet;
import de.amr.games.pacman.ui._2d.SpriteAnimationSet;
import de.amr.games.pacman.uilib.animation.SpriteAnimation;
import de.amr.games.pacman.uilib.assets.SpriteSheet;

import java.util.Map;

/**
 * @author Armin Reichert
 */
public class ArcadePacMan_GhostAnimations extends SpriteAnimationSet implements GhostAnimations {

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
            GhostAnimations.ANIM_GHOST_NORMAL, normal,
            GhostAnimations.ANIM_GHOST_FRIGHTENED, frightened,
            GhostAnimations.ANIM_GHOST_FLASHING, flashing,
            GhostAnimations.ANIM_GHOST_EYES, eyes,
            GhostAnimations.ANIM_GHOST_NUMBER, number,
            GhostAnimations.ANIM_BLINKY_DAMAGED, damaged,
            GhostAnimations.ANIM_BLINKY_NAIL_DRESS_RAPTURE, stretching,
            GhostAnimations.ANIM_BLINKY_PATCHED, patched,
            GhostAnimations.ANIM_BLINKY_NAKED, naked));

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
                return gss.ghostNormalSprites(ghost.id(), ghost.wishDir());
            }
            if (isCurrentAnimationID(GhostAnimations.ANIM_GHOST_EYES)) {
                return gss.ghostEyesSprites(ghost.wishDir());
            }
        }
        return super.selectedSprites(spriteSheet, actor);
    }
}