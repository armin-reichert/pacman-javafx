/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.actors.Actor2D;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui2d.assets.GameSpriteSheet;
import de.amr.games.pacman.ui2d.assets.SpriteAnimation;
import de.amr.games.pacman.ui2d.assets.SpriteAnimationSet;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.assertLegalGhostID;
import static de.amr.games.pacman.lib.Globals.assertNotNull;

/**
 * @author Armin Reichert
 */
public class GhostAnimations extends SpriteAnimationSet {

    public GhostAnimations(GameSpriteSheet spriteSheet, byte ghostID) {
        assertNotNull(spriteSheet);
        assertLegalGhostID(ghostID);

        var normal = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Normal ghost")
            .sprites(spriteSheet.ghostNormalSprites(ghostID, Direction.LEFT))
            .frameTicks(8)
            .loop();

        var frightened = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Frightened ghost")
            .sprites(spriteSheet.ghostFrightenedSprites())
            .frameTicks(8)
            .loop();

        var flashing = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Flashing ghost")
            .sprites(spriteSheet.ghostFlashingSprites())
            .frameTicks(7)
            .loop();

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

        add(Map.of(
            Animations.ANIM_GHOST_NORMAL, normal,
            Animations.ANIM_GHOST_FRIGHTENED, frightened,
            Animations.ANIM_GHOST_FLASHING, flashing,
            Animations.ANIM_GHOST_EYES, eyes,
            Animations.ANIM_GHOST_NUMBER, number));

        eyes.start();
        frightened.start();
        flashing.start();
    }

    @Override
    public void select(String id, int frameIndex) {
        super.select(id, frameIndex);
        if (Animations.ANIM_GHOST_NUMBER.equals(id)) {
            animation(Animations.ANIM_GHOST_NUMBER).setFrameIndex(frameIndex);
        }
    }

    @Override
    protected RectArea[] selectedSprites(GameSpriteSheet spriteSheet, Actor2D actor) {
        if (actor instanceof Ghost ghost) {
            if (isCurrentAnimationID(Animations.ANIM_GHOST_NORMAL)) {
                return spriteSheet.ghostNormalSprites(ghost.id(), ghost.wishDir());
            }
            if (isCurrentAnimationID(Animations.ANIM_GHOST_EYES)) {
                return spriteSheet.ghostEyesSprites(ghost.wishDir());
            }
        }
        return super.selectedSprites(spriteSheet, actor);
    }
}