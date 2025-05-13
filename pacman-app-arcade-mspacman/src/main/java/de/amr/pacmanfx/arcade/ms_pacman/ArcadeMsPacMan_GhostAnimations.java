/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

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

import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class ArcadeMsPacMan_GhostAnimations extends SpriteAnimationSet implements GhostAnimations {

    public ArcadeMsPacMan_GhostAnimations(GameSpriteSheet spriteSheet, byte personality) {
        requireNonNull(spriteSheet);
        requireValidGhostPersonality(personality);

        var normal = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.ghostNormalSprites(personality, Direction.LEFT))
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

        add(Map.of(
            GhostAnimations.ANIM_GHOST_NORMAL, normal,
            GhostAnimations.ANIM_GHOST_FRIGHTENED, frightened,
            GhostAnimations.ANIM_GHOST_FLASHING, flashing,
            GhostAnimations.ANIM_GHOST_EYES, eyes,
            GhostAnimations.ANIM_GHOST_NUMBER, number));

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