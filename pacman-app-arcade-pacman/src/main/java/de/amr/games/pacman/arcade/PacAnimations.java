/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.Globals;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.actors.Actor2D;
import de.amr.games.pacman.model.actors.ActorAnimations;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui._2d.GameSpriteSheet;
import de.amr.games.pacman.ui._2d.SpriteAnimationSet;
import de.amr.games.pacman.uilib.SpriteAnimation;
import de.amr.games.pacman.uilib.SpriteSheet;

import java.util.Map;

/**
 * @author Armin Reichert
 */
public class PacAnimations extends SpriteAnimationSet {

    public PacAnimations(ArcadePacMan_SpriteSheet spriteSheet) {
        Globals.assertNotNull(spriteSheet);
        add(Map.of(
            ActorAnimations.ANIM_PAC_MUNCHING,
            SpriteAnimation.spriteSheet(spriteSheet)
                .sprites(spriteSheet.pacMunchingSprites(Direction.LEFT)).endLoop(),

            ActorAnimations.ANIM_PAC_DYING,
            SpriteAnimation.spriteSheet(spriteSheet)
                .sprites(spriteSheet.pacDyingSprites()).frameTicks(8).end(),

            ActorAnimations.ANIM_PAC_BIG,
            SpriteAnimation.spriteSheet(spriteSheet)
                .sprites(spriteSheet.bigPacManSprites()).frameTicks(3).endLoop()
        ));
    }

    @Override
    protected RectArea[] selectedSprites(SpriteSheet spriteSheet, Actor2D actor) {
        GameSpriteSheet gss = (GameSpriteSheet) spriteSheet;
        if (actor instanceof Pac pac) {
            if (isCurrentAnimationID(ActorAnimations.ANIM_PAC_MUNCHING)) {
                return gss.pacMunchingSprites(pac.moveDir());
            }
        }
        return super.selectedSprites(spriteSheet, actor);
    }
}