/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Animations;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import de.amr.pacmanfx.ui._2d.SpriteAnimationSet;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class ArcadePacMan_PacAnimations extends SpriteAnimationSet implements Animations {

    public static final String BIG_PAC_MAN = "big_pac_man";

    public ArcadePacMan_PacAnimations(ArcadePacMan_SpriteSheet spriteSheet) {
        requireNonNull(spriteSheet);
        add(Map.of(
            Animations.ANY_PAC_MUNCHING,
            SpriteAnimation.spriteSheet(spriteSheet)
                .sprites(spriteSheet.pacMunchingSprites(Direction.LEFT)).endLoop(),

            Animations.ANY_PAC_DYING,
            SpriteAnimation.spriteSheet(spriteSheet)
                .sprites(spriteSheet.pacDyingSprites()).frameTicks(8).end(),

            BIG_PAC_MAN,
            SpriteAnimation.spriteSheet(spriteSheet)
                .sprites(spriteSheet.bigPacManSprites()).frameTicks(3).endLoop()
        ));
    }

    @Override
    protected RectArea[] selectedSprites(SpriteSheet spriteSheet, Actor actor) {
        GameSpriteSheet gss = (GameSpriteSheet) spriteSheet;
        if (actor instanceof Pac pac) {
            if (isCurrentAnimationID(Animations.ANY_PAC_MUNCHING)) {
                return gss.pacMunchingSprites(pac.moveDir());
            }
        }
        return super.selectedSprites(spriteSheet, actor);
    }
}