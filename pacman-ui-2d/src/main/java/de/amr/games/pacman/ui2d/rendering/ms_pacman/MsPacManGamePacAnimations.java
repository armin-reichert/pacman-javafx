/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering.ms_pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.actors.AnimatedEntity;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.rendering.RectArea;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.util.SpriteAnimations;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class MsPacManGamePacAnimations extends SpriteAnimations {

    private final Map<String, SpriteAnimation> animationsByName;

    public MsPacManGamePacAnimations(GameSpriteSheet spriteSheet) {
        checkNotNull(spriteSheet);

        var munching = SpriteAnimation.begin()
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.pacMunchingSprites(Direction.LEFT))
            .loop();

        var dying = SpriteAnimation.begin()
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.pacDyingSprites())
            .frameTicks(8)
            .end();

        var husbandMunching = SpriteAnimation.begin()
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.pacManMunchingSprites(Direction.LEFT))
            .frameTicks(2)
            .loop();

        animationsByName = Map.of(
            Pac.ANIM_MUNCHING, munching,
            Pac.ANIM_DYING, dying,
            Pac.ANIM_HUSBAND_MUNCHING, husbandMunching
        );
    }

    @Override
    public SpriteAnimation animation(String name) {
        return animationsByName.get(name);
    }

    @Override
    public RectArea currentSprite(AnimatedEntity animatedEntity) {
        Pac msPacMan = (Pac) animatedEntity.entity();
        var currentAnimation = currentAnimation();
        if (Pac.ANIM_MUNCHING.equals(currentAnimationName)) {
            currentAnimation.setSprites(currentAnimation.spriteSheet().pacMunchingSprites(msPacMan.moveDir()));
        }
        if (Pac.ANIM_HUSBAND_MUNCHING.equals(currentAnimationName)) {
            currentAnimation.setSprites(currentAnimation.spriteSheet().pacManMunchingSprites(msPacMan.moveDir()));
        }
        return currentAnimation != null ? currentAnimation.currentSprite() : null;
    }
}