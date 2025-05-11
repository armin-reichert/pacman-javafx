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
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class ArcadeMsPacMan_PacAnimations extends SpriteAnimationSet implements Animations {

    public static final String PAC_MAN_MUNCHING = "pac_man_munching";

    public ArcadeMsPacMan_PacAnimations(ArcadeMsPacMan_SpriteSheet spriteSheet) {
        requireNonNull(spriteSheet);

        var munching = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.pacMunchingSprites(Direction.LEFT))
            .endLoop();

        var dying = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.pacDyingSprites())
            .frameTicks(8)
            .end();

        var husbandMunching = SpriteAnimation
            .spriteSheet(spriteSheet)
            .sprites(spriteSheet.mrPacManMunchingSprites(Direction.LEFT))
            .frameTicks(2)
            .endLoop();

        add(Map.of(
            Animations.ANY_PAC_MUNCHING, munching,
            Animations.ANY_PAC_DYING, dying,
            PAC_MAN_MUNCHING, husbandMunching
        ));
    }

    @Override
    protected RectArea[] selectedSprites(SpriteSheet spriteSheet, Actor actor) {
        ArcadeMsPacMan_SpriteSheet gss = (ArcadeMsPacMan_SpriteSheet) spriteSheet;
        if (actor instanceof Pac msPacMan) {
            if (isCurrentAnimationID(Animations.ANY_PAC_MUNCHING)) {
                return gss.pacMunchingSprites(msPacMan.moveDir());
            }
            if (isCurrentAnimationID(PAC_MAN_MUNCHING)) {
                return gss.mrPacManMunchingSprites(msPacMan.moveDir());
            }
        }
        return super.selectedSprites(spriteSheet, actor);
    }
}