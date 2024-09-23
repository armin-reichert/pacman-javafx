/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering.pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.actors.AnimatedEntity;
import de.amr.games.pacman.model.actors.Entity;
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
public class PacManGamePacAnimations extends SpriteAnimations {

    public PacManGamePacAnimations(GameSpriteSheet spriteSheet) {
        checkNotNull(spriteSheet);

        var munching = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.pacMunchingSprites(Direction.LEFT))
            .loop();

        var dying = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.pacDyingSprites())
            .frameTicks(8)
            .end();

        var bigPacMan = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.bigPacManSprites())
            .frameTicks(3)
            .loop();

        add(Map.of(
            Pac.ANIM_MUNCHING, munching,
            Pac.ANIM_DYING, dying,
            Pac.ANIM_BIG_PACMAN, bigPacMan));
    }

    @Override
    protected RectArea[] selectedSprites(GameSpriteSheet spriteSheet, Entity entity) {
        if (entity instanceof Pac pac) {
            if (Pac.ANIM_MUNCHING.equals(currentAnimationName)) {
                return spriteSheet.pacMunchingSprites(pac.moveDir());
            }
        }
        return super.selectedSprites(spriteSheet, entity);
    }
}