/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.actors.Actor2D;
import de.amr.games.pacman.model.actors.ActorAnimations;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.assets.GameSpriteSheet;
import de.amr.games.pacman.ui2d.assets.SpriteAnimation;
import de.amr.games.pacman.ui2d.assets.SpriteAnimationSet;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.assertNotNull;

/**
 * @author Armin Reichert
 */
public class PacAnimations extends SpriteAnimationSet {

    public PacAnimations(MsPacManGameSpriteSheet spriteSheet) {
        assertNotNull(spriteSheet);

        var munching = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man munching")
            .sprites(spriteSheet.pacMunchingSprites(Direction.LEFT))
            .loop();

        var dying = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man dying")
            .sprites(spriteSheet.pacDyingSprites())
            .frameTicks(8)
            .end();

        var husbandMunching = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man's husband munching")
            .sprites(spriteSheet.mrPacManMunchingSprites(Direction.LEFT))
            .frameTicks(2)
            .loop();

        add(Map.of(
            ActorAnimations.ANIM_PAC_MUNCHING, munching,
            ActorAnimations.ANIM_PAC_DYING, dying,
            ActorAnimations.ANIM_MR_PACMAN_MUNCHING, husbandMunching
        ));
    }

    @Override
    protected RectArea[] selectedSprites(GameSpriteSheet spriteSheet, Actor2D actor) {
        if (actor instanceof Pac msPacMan) {
            if (isCurrentAnimationID(ActorAnimations.ANIM_PAC_MUNCHING)) {
                return spriteSheet.pacMunchingSprites(msPacMan.moveDir());
            }
            if (isCurrentAnimationID(ActorAnimations.ANIM_MR_PACMAN_MUNCHING)) {
                return ((MsPacManGameSpriteSheet) spriteSheet).mrPacManMunchingSprites(msPacMan.moveDir());
            }
        }
        return super.selectedSprites(spriteSheet, actor);
    }
}