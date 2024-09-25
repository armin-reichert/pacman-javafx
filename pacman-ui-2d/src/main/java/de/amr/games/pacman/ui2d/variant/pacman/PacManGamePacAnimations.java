/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.variant.pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.RectArea;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.util.SpriteAnimationCollection;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class PacManGamePacAnimations extends SpriteAnimationCollection {

    public PacManGamePacAnimations(GameSpriteSheet spriteSheet) {
        checkNotNull(spriteSheet);
        add(Map.of(
            Pac.ANIM_MUNCHING,
            SpriteAnimation.use(spriteSheet).info("Pac-Man munching").sprites(spriteSheet.pacMunchingSprites(Direction.LEFT)).loop(),

            Pac.ANIM_DYING,
            SpriteAnimation.use(spriteSheet).info("Pac-Man dying").sprites(spriteSheet.pacDyingSprites()).frameTicks(8).end(),

            Pac.ANIM_BIG_PACMAN,
            SpriteAnimation.use(spriteSheet).info("BIG Pac-Man munching").sprites(spriteSheet.bigPacManSprites()).frameTicks(3).loop()
        ));
    }

    @Override
    protected RectArea[] selectedSprites(GameSpriteSheet spriteSheet, Entity entity) {
        if (entity instanceof Pac pac) {
            if (currently(Pac.ANIM_MUNCHING)) {
                return spriteSheet.pacMunchingSprites(pac.moveDir());
            }
        }
        return super.selectedSprites(spriteSheet, entity);
    }
}