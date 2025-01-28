/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.actors.Actor2D;
import de.amr.games.pacman.model.actors.ActorAnimations;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.assets.GameSpriteSheet;
import de.amr.games.pacman.ui2d.assets.SpriteAnimation;
import de.amr.games.pacman.ui2d.assets.SpriteAnimationSet;

import java.util.Map;

/**
 * @author Armin Reichert
 */
public class PacAnimations extends SpriteAnimationSet {

    public PacAnimations(PacManGameSpriteSheet spriteSheet) {
        Globals.assertNotNull(spriteSheet);
        add(Map.of(
            ActorAnimations.ANIM_PAC_MUNCHING,
            SpriteAnimation.spriteSheet(spriteSheet).info("Pac-Man munching")
                .sprites(spriteSheet.pacMunchingSprites(Direction.LEFT)).loop(),

            ActorAnimations.ANIM_PAC_DYING,
            SpriteAnimation.spriteSheet(spriteSheet).info("Pac-Man dying")
                .sprites(spriteSheet.pacDyingSprites()).frameTicks(8).end(),

            ActorAnimations.ANIM_PAC_BIG,
            SpriteAnimation.spriteSheet(spriteSheet).info("BIG Pac-Man munching")
                .sprites(spriteSheet.bigPacManSprites()).frameTicks(3).loop()
        ));
    }

    @Override
    protected RectArea[] selectedSprites(GameSpriteSheet spriteSheet, Actor2D actor) {
        if (actor instanceof Pac pac) {
            if (isCurrentAnimationID(ActorAnimations.ANIM_PAC_MUNCHING)) {
                return spriteSheet.pacMunchingSprites(pac.moveDir());
            }
        }
        return super.selectedSprites(spriteSheet, actor);
    }
}