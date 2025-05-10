/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.actors.Actor;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import de.amr.games.pacman.ui._2d.GameSpriteSheet;
import de.amr.games.pacman.ui._2d.SpriteAnimationSet;
import de.amr.games.pacman.uilib.animation.SpriteAnimation;
import de.amr.games.pacman.uilib.assets.SpriteSheet;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class ArcadePacMan_PacAnimations extends SpriteAnimationSet implements PacAnimations {

    public static final String ANIM_BIG_PAC_MAN = "big_pacman";

    public ArcadePacMan_PacAnimations(ArcadePacMan_SpriteSheet spriteSheet) {
        requireNonNull(spriteSheet);
        add(Map.of(
            PacAnimations.ANIM_MUNCHING,
            SpriteAnimation.spriteSheet(spriteSheet)
                .sprites(spriteSheet.pacMunchingSprites(Direction.LEFT)).endLoop(),

            PacAnimations.ANIM_DYING,
            SpriteAnimation.spriteSheet(spriteSheet)
                .sprites(spriteSheet.pacDyingSprites()).frameTicks(8).end(),

            ANIM_BIG_PAC_MAN,
            SpriteAnimation.spriteSheet(spriteSheet)
                .sprites(spriteSheet.bigPacManSprites()).frameTicks(3).endLoop()
        ));
    }

    @Override
    protected RectArea[] selectedSprites(SpriteSheet spriteSheet, Actor actor) {
        GameSpriteSheet gss = (GameSpriteSheet) spriteSheet;
        if (actor instanceof Pac pac) {
            if (isCurrentAnimationID(PacAnimations.ANIM_MUNCHING)) {
                return gss.pacMunchingSprites(pac.moveDir());
            }
        }
        return super.selectedSprites(spriteSheet, actor);
    }
}