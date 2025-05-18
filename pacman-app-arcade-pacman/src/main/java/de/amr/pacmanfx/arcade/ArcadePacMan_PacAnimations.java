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

    public ArcadePacMan_PacAnimations(ArcadePacMan_SpriteSheet ss) {
        requireNonNull(ss);
        add(Map.of(
            ANY_PAC_MUNCHING, SpriteAnimation.from(ss).take(ss.pacMunchingSprites(Direction.LEFT)).endless(),
            ANY_PAC_DYING,    SpriteAnimation.from(ss).take(ss.pacDyingSprites()).frameTicks(8).end(),
            BIG_PAC_MAN,      SpriteAnimation.from(ss).take(ss.bigPacManSprites()).frameTicks(3).endless()
        ));
    }

    @Override
    protected RectArea[] selectedSprites(SpriteSheet ss, Actor actor) {
        if (actor instanceof Pac pac && isCurrentAnimationID(ANY_PAC_MUNCHING)) {
            var gss = (GameSpriteSheet) ss;
            return gss.pacMunchingSprites(pac.moveDir());
        }
        return super.selectedSprites(ss, actor);
    }
}