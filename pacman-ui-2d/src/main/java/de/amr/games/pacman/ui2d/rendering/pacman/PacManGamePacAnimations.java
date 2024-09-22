/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering.pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.actors.AnimatedEntity;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.rendering.RectangularArea;
import de.amr.games.pacman.ui2d.rendering.SpriteSheet;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.util.SpriteAnimations;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class PacManGamePacAnimations extends SpriteAnimations {

    private final Map<String, SpriteAnimation> animationsByName;

    public PacManGamePacAnimations(SpriteSheet spriteSheet) {
        checkNotNull(spriteSheet);

        var munching = SpriteAnimation.begin()
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.pacMunchingSprites(Direction.LEFT))
            .loop()
            .end();

        var dying = SpriteAnimation.begin()
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.pacDyingSprites())
            .frameTicks(8)
            .end();

        var bigPacMan = SpriteAnimation.begin()
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.bigPacManSprites())
            .frameTicks(3)
            .loop()
            .end();

        animationsByName = Map.of(
            Pac.ANIM_MUNCHING, munching,
            Pac.ANIM_DYING, dying,
            Pac.ANIM_BIG_PACMAN, bigPacMan);
    }

    @Override
    public SpriteAnimation animation(String name) {
        return animationsByName.get(name);
    }

    @Override
    public RectangularArea currentSprite(AnimatedEntity animatedEntity) {
        Pac pac = (Pac) animatedEntity.entity();
        var currentAnimation = currentAnimation();
        if (Pac.ANIM_MUNCHING.equals(currentAnimationName)) {
            currentAnimation.setSprites(currentAnimation.spriteSheet().pacMunchingSprites(pac.moveDir()));
        }
        return currentAnimation != null ? currentAnimation.currentSprite() : null;
    }
}