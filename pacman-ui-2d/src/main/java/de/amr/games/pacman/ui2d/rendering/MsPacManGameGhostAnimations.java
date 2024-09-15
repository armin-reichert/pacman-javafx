/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.util.SpriteAnimations;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class MsPacManGameGhostAnimations extends SpriteAnimations {

    private final Map<String, SpriteAnimation> animationsByName;
    private final Ghost ghost;
    private final MsPacManGameSpriteSheet spriteSheet;

    public MsPacManGameGhostAnimations(Ghost ghost, MsPacManGameSpriteSheet spriteSheet) {
        checkNotNull(ghost);
        checkNotNull(spriteSheet);
        this.ghost = ghost;
        this.spriteSheet = spriteSheet;

        var normal = SpriteAnimation.begin()
            .sprites(spriteSheet.ghostNormalSprites(ghost.id(), Direction.LEFT))
            .frameTicks(8)
            .loop()
            .end();

        var frightened = SpriteAnimation.begin()
            .sprites(spriteSheet.ghostFrightenedSprites())
            .frameTicks(8)
            .loop()
            .end();

        var flashing = SpriteAnimation.begin()
            .sprites(spriteSheet.ghostFlashingSprites())
            .frameTicks(7)
            .loop()
            .end();

        var eyes = SpriteAnimation.begin()
            .sprites(spriteSheet.ghostEyesSprites(Direction.LEFT))
            .end();

        var number = SpriteAnimation.begin()
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
    public SpriteArea currentSprite() {
        var currentAnimation = currentAnimation();
        if (Ghost.ANIM_GHOST_NORMAL.equals(currentAnimationName)) {
            currentAnimation.setSprites(spriteSheet.ghostNormalSprites(ghost.id(), ghost.wishDir()));
        } else if (Ghost.ANIM_GHOST_EYES.equals(currentAnimationName)) {
            currentAnimation.setSprites(spriteSheet.ghostEyesSprites(ghost.wishDir()));
        }
        return currentAnimation != null ? currentAnimation.currentSprite() : null;
    }
}