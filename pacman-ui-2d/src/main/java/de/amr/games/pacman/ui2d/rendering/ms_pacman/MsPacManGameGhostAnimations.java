/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering.ms_pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.actors.AnimatedEntity;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui2d.rendering.RectArea;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.util.SpriteAnimations;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class MsPacManGameGhostAnimations extends SpriteAnimations {

    private final Map<String, SpriteAnimation> animationsByName;

    public MsPacManGameGhostAnimations(GameSpriteSheet spriteSheet, byte ghostID) {
        checkNotNull(spriteSheet);

        var normal = SpriteAnimation.begin()
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.ghostNormalSprites(ghostID, Direction.LEFT))
            .frameTicks(8)
            .loop();

        var frightened = SpriteAnimation.begin()
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.ghostFrightenedSprites())
            .frameTicks(8)
            .loop();

        var flashing = SpriteAnimation.begin()
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.ghostFlashingSprites())
            .frameTicks(7)
            .loop();

        var eyes = SpriteAnimation.begin()
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.ghostEyesSprites(Direction.LEFT))
            .end();

        var number = SpriteAnimation.begin()
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.ghostNumberSprites())
            .end();

        animationsByName = Map.of(
            Ghost.ANIM_GHOST_NORMAL, normal,
            Ghost.ANIM_GHOST_FRIGHTENED, frightened,
            Ghost.ANIM_GHOST_FLASHING, flashing,
            Ghost.ANIM_GHOST_EYES, eyes,
            Ghost.ANIM_GHOST_NUMBER, number);

        // TODO check this
        eyes.start();
        frightened.start();
        flashing.start();
    }

    @Override
    public SpriteAnimation animation(String name) {
        return animationsByName.get(name);
    }

    @Override
    public void select(String name, int index) {
        super.select(name, index);
        if (Ghost.ANIM_GHOST_NUMBER.equals(name)) {
            animation(Ghost.ANIM_GHOST_NUMBER).setFrameIndex(index);
        }
    }

    @Override
    public RectArea currentSprite(AnimatedEntity animatedEntity) {
        Ghost ghost = (Ghost) animatedEntity.entity();
        var currentAnimation = currentAnimation();
        if (Ghost.ANIM_GHOST_NORMAL.equals(currentAnimationName)) {
            currentAnimation.setSprites(currentAnimation.spriteSheet().ghostNormalSprites(ghost.id(), ghost.wishDir()));
        } else if (Ghost.ANIM_GHOST_EYES.equals(currentAnimationName)) {
            currentAnimation.setSprites(currentAnimation.spriteSheet().ghostEyesSprites(ghost.wishDir()));
        }
        return currentAnimation != null ? currentAnimation.currentSprite() : null;
    }
}