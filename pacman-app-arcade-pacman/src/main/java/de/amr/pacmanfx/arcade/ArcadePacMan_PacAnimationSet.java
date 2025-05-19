/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import de.amr.pacmanfx.ui._2d.SpriteAnimationSet;

import static de.amr.pacmanfx.arcade.ArcadePacMan_UIConfig.ANIM_BIG_PAC_MAN;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_ANY_PAC_DYING;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_ANY_PAC_MUNCHING;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.from;

public class ArcadePacMan_PacAnimationSet extends SpriteAnimationSet {

    public ArcadePacMan_PacAnimationSet(ArcadePacMan_SpriteSheet ss) {
        super(ss);
        set(ANIM_ANY_PAC_MUNCHING, from(ss).take(ss.pacMunchingSprites(Direction.LEFT)).endless());
        set(ANIM_ANY_PAC_DYING,    from(ss).take(ss.pacDyingSprites()).frameTicks(8).end());
        set(ANIM_BIG_PAC_MAN,      from(ss).take(ss.bigPacManSprites()).frameTicks(3).endless());
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Pac pac && isCurrentAnimationID(ANIM_ANY_PAC_MUNCHING)) {
            var gss = (GameSpriteSheet) spriteSheet;
            currentAnimation().setSprites(gss.pacMunchingSprites(pac.moveDir()));
        }
    }
}