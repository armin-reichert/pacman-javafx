/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering.pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.rendering.RectangularArea;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.util.SpriteAnimations;
import de.amr.games.pacman.ui2d.util.SpriteSheet;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class PacManGamePacAnimations extends SpriteAnimations {

    private final Map<String, SpriteAnimation> animationsByName;
    private final Pac pac;
    private final SpriteSheet spriteSheet;

    public PacManGamePacAnimations(Pac pac, SpriteSheet spriteSheet) {
        checkNotNull(pac);
        checkNotNull(spriteSheet);
        this.pac = pac;
        this.spriteSheet = spriteSheet;

        var munching = SpriteAnimation.begin()
            .sprites(spriteSheet.pacMunchingSprites(Direction.LEFT))
            .loop()
            .end();

        var dying = SpriteAnimation.begin()
            .sprites(spriteSheet.pacDyingSprites())
            .frameTicks(8)
            .end();

        var bigPacMan = SpriteAnimation.begin()
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
    public RectangularArea currentSprite() {
        var currentAnimation = currentAnimation();
        if (Pac.ANIM_MUNCHING.equals(currentAnimationName)) {
            currentAnimation.setSprites(spriteSheet.pacMunchingSprites(pac.moveDir()));
        }
        return currentAnimation != null ? currentAnimation.currentSprite() : null;
    }
}