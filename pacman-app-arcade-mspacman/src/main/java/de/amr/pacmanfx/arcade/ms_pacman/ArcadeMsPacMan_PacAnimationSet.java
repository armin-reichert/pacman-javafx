/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.SpriteAnimationSet;

import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_ANY_PAC_DYING;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_ANY_PAC_MUNCHING;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.from;

public class ArcadeMsPacMan_PacAnimationSet extends SpriteAnimationSet {

    public static final String PAC_MAN_MUNCHING = "pac_man_munching";

    public ArcadeMsPacMan_PacAnimationSet(ArcadeMsPacMan_SpriteSheet ss) {
        super(ss);
        add(ANIM_ANY_PAC_MUNCHING, from(ss).take(ss.pacMunchingSprites(Direction.LEFT)).endless());
        add(ANIM_ANY_PAC_DYING,    from(ss).take(ss.pacDyingSprites()).frameTicks(8).end());
        add(PAC_MAN_MUNCHING,      from(ss).take(ss.mrPacManMunchingSprites(Direction.LEFT)).frameTicks(2).endless());
    }

    @Override
    protected void updateActorSprites(Actor actor) {
        if (actor instanceof Pac pac) {
            var gss = (ArcadeMsPacMan_SpriteSheet) spriteSheet;
            switch (currentAnimationID) {
                case ANIM_ANY_PAC_MUNCHING -> currentAnimation().setSprites(gss.pacMunchingSprites(pac.moveDir()));
                case PAC_MAN_MUNCHING -> currentAnimation().setSprites(gss.mrPacManMunchingSprites(pac.moveDir()));
            }
        }
    }
}