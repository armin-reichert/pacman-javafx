/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui.assets.GameSpriteSheet;
import de.amr.games.pacman.ui.assets.SpriteAnimation;
import de.amr.games.pacman.ui.assets.SpriteAnimationCollection;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class GhostAnimations extends SpriteAnimationCollection {

    public static final int NORMAL_FRAME_TICKS = 8;  // TODO check this in emulator
    public static final int FRIGHTENED_FRAME_TICKS = 8;  // TODO check this in emulator
    public static final int FLASH_FRAME_TICKS = 7;  // TODO check this in emulator

    public GhostAnimations(GameSpriteSheet spriteSheet, byte ghostID) {
        checkNotNull(spriteSheet);

        var normal = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Normal ghost")
            .sprites(spriteSheet.ghostNormalSprites(ghostID, Direction.LEFT))
            .frameTicks(NORMAL_FRAME_TICKS)
            .loop();

        var frightened = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Frightened ghost")
            .sprites(spriteSheet.ghostFrightenedSprites())
            .frameTicks(FRIGHTENED_FRAME_TICKS)
            .loop();

        var flashing = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Flashing ghost")
            .sprites(spriteSheet.ghostFlashingSprites())
            .frameTicks(FLASH_FRAME_TICKS)
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

        // TODO check this
        eyes.start();
        frightened.start();
        flashing.start();
    }

    @Override
    public void select(String name, int frameIndex) {
        super.select(name, frameIndex);
        if (Animations.ANIM_GHOST_NUMBER.equals(name)) {
            animation(Animations.ANIM_GHOST_NUMBER).setFrameIndex(frameIndex);
        }
    }

    @Override
    protected RectArea[] selectedSprites(GameSpriteSheet spriteSheet, Entity entity) {
        if (entity instanceof Ghost ghost) {
            if (isCurrentAnimationID(Animations.ANIM_GHOST_NORMAL)) {
                return spriteSheet.ghostNormalSprites(ghost.id(), ghost.wishDir());
            }
            if (isCurrentAnimationID(Animations.ANIM_GHOST_EYES)) {
                return spriteSheet.ghostEyesSprites(ghost.wishDir());
            }
        }
        return super.selectedSprites(spriteSheet, entity);
    }
}