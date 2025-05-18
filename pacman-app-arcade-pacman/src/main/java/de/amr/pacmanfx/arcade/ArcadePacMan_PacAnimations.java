/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Animations;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import de.amr.pacmanfx.ui._2d.SpriteAnimationSet;

import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.from;

public class ArcadePacMan_PacAnimations extends SpriteAnimationSet implements Animations {

    public static final String BIG_PAC_MAN = "big_pac_man";

    public ArcadePacMan_PacAnimations(ArcadePacMan_SpriteSheet ss) {
        super(ss);
        add(ANY_PAC_MUNCHING, from(ss).take(ss.pacMunchingSprites(Direction.LEFT)).endless());
        add(ANY_PAC_DYING,    from(ss).take(ss.pacDyingSprites()).frameTicks(8).end());
        add(BIG_PAC_MAN,      from(ss).take(ss.bigPacManSprites()).frameTicks(3).endless());
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Pac pac && isCurrentAnimationID(ANY_PAC_MUNCHING)) {
            var gss = (GameSpriteSheet) spriteSheet;
            currentAnimation().setSprites(gss.pacMunchingSprites(pac.moveDir()));
        }
    }
}