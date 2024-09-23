/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.variant.tengen;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.RectArea;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.util.SpriteAnimationCollection;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class TengenMsPacManGameGhostAnimations extends SpriteAnimationCollection {

    public TengenMsPacManGameGhostAnimations(GameSpriteSheet spriteSheet, byte ghostID) {
        checkNotNull(spriteSheet);

        var normal = SpriteAnimation
            .use(spriteSheet)
            .info("Normal ghost")
            .sprites(spriteSheet.ghostNormalSprites(ghostID, Direction.LEFT))
            .frameTicks(8)
            .loop();

        var frightened = SpriteAnimation
            .use(spriteSheet)
            .info("Frightened ghost")
            .sprites(spriteSheet.ghostFrightenedSprites())
            .frameTicks(8)
            .loop();

        var flashing = SpriteAnimation
            .use(spriteSheet)
            .info("Flashing ghost")
            .sprites(spriteSheet.ghostFlashingSprites())
            .frameTicks(7)
            .loop();

        var eyes = SpriteAnimation
            .use(spriteSheet)
            .info("Eyes-only ghost")
            .sprites(spriteSheet.ghostEyesSprites(Direction.LEFT))
            .end();

        var number = SpriteAnimation
            .use(spriteSheet)
            .info("Number for dead ghost")
            .sprites(spriteSheet.ghostNumberSprites())
            .end();

        add(Map.of(
            Ghost.ANIM_GHOST_NORMAL, normal,
            Ghost.ANIM_GHOST_FRIGHTENED, frightened,
            Ghost.ANIM_GHOST_FLASHING, flashing,
            Ghost.ANIM_GHOST_EYES, eyes,
            Ghost.ANIM_GHOST_NUMBER, number));

        // TODO check this
        eyes.start();
        frightened.start();
        flashing.start();
    }

    @Override
    public void select(String name, int index) {
        super.select(name, index);
        if (Ghost.ANIM_GHOST_NUMBER.equals(name)) {
            animation(Ghost.ANIM_GHOST_NUMBER).setFrameIndex(index);
        }
    }

    @Override
    protected RectArea[] selectedSprites(GameSpriteSheet spriteSheet, Entity entity) {
        if (entity instanceof Ghost ghost) {
            if (currently(Ghost.ANIM_GHOST_NORMAL)) {
                return spriteSheet.ghostNormalSprites(ghost.id(), ghost.wishDir());
            }
            if (currently(Ghost.ANIM_GHOST_EYES)) {
                return spriteSheet.ghostEyesSprites(ghost.wishDir());
            }
        }
        return super.selectedSprites(spriteSheet, entity);
    }
}