/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.util.SpriteAnimationCollection;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class GhostAnimations extends SpriteAnimationCollection {

    public GhostAnimations(GameSpriteSheet spriteSheet, byte ghostID) {
        checkNotNull(spriteSheet);

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
            GameModel.ANIM_GHOST_NORMAL, normal,
            GameModel.ANIM_GHOST_FRIGHTENED, frightened,
            GameModel.ANIM_GHOST_FLASHING, flashing,
            GameModel.ANIM_GHOST_EYES, eyes,
            GameModel.ANIM_GHOST_NUMBER, number));

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