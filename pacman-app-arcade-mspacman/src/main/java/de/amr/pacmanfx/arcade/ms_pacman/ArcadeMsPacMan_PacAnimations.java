/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Animations;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.SpriteAnimationSet;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.from;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_PacAnimations extends SpriteAnimationSet implements Animations {

    public static final String PAC_MAN_MUNCHING = "pac_man_munching";

    public ArcadeMsPacMan_PacAnimations(ArcadeMsPacMan_SpriteSheet ss) {
        requireNonNull(ss);
        add(ANY_PAC_MUNCHING, from(ss).take(ss.pacMunchingSprites(Direction.LEFT)).endless());
        add(ANY_PAC_DYING,    from(ss).take(ss.pacDyingSprites()).frameTicks(8).end());
        add(PAC_MAN_MUNCHING, from(ss).take(ss.mrPacManMunchingSprites(Direction.LEFT)).frameTicks(2).endless());
    }

    @Override
    protected RectArea[] updateActorSprites(SpriteSheet ss, Actor actor) {
        if (actor instanceof Pac pac) {
            var gss = (ArcadeMsPacMan_SpriteSheet) ss;
            switch (currentAnimationID) {
                case ANY_PAC_MUNCHING -> {
                    return gss.pacMunchingSprites(pac.moveDir());
                }
                case PAC_MAN_MUNCHING -> {
                    return gss.mrPacManMunchingSprites(pac.moveDir());
                }
            }
        }
        return null;
    }
}