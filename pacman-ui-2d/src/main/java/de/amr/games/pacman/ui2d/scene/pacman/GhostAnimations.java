/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.pacman.PacManArcadeGame;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.util.SpriteAnimationCollection;

import java.util.Map;

/**
 * @author Armin Reichert
 */
public class GhostAnimations extends SpriteAnimationCollection {

    public GhostAnimations(PacManGameSpriteSheet spriteSheet, byte ghostID) {

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
            .loop();

        var naked = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Naked ghost")
            .sprites(spriteSheet.blinkyNakedSprites())
            .frameTicks(4)
            .loop();

        add(Map.of(
            GameModel.ANIM_GHOST_NORMAL, normal,
            GameModel.ANIM_GHOST_FRIGHTENED, frightened,
            GameModel.ANIM_GHOST_FLASHING, flashing,
            GameModel.ANIM_GHOST_EYES, eyes,
            GameModel.ANIM_GHOST_NUMBER, number,
            PacManArcadeGame.ANIM_BLINKY_DAMAGED, damaged,
            PacManArcadeGame.ANIM_BLINKY_STRETCHED, stretching,
            PacManArcadeGame.ANIM_BLINKY_PATCHED, patched,
            PacManArcadeGame.ANIM_BLINKY_NAKED, naked));

        // TODO check this
        eyes.start();
        frightened.start();
        flashing.start();
    }

    @Override
    public void select(String name, int frameIndex) {
        super.select(name, frameIndex);
        if (GameModel.ANIM_GHOST_NUMBER.equals(name)) {
            animation(GameModel.ANIM_GHOST_NUMBER).setFrameIndex(frameIndex);
        }
    }

    @Override
    protected RectArea[] selectedSprites(GameSpriteSheet spriteSheet, Entity entity) {
        if (entity instanceof Ghost ghost) {
            if (isCurrentAnimationID(GameModel.ANIM_GHOST_NORMAL)) {
                return spriteSheet.ghostNormalSprites(ghost.id(), ghost.wishDir());
            }
            if (isCurrentAnimationID(GameModel.ANIM_GHOST_EYES)) {
                return spriteSheet.ghostEyesSprites(ghost.wishDir());
            }
        }
        return super.selectedSprites(spriteSheet, entity);
    }
}