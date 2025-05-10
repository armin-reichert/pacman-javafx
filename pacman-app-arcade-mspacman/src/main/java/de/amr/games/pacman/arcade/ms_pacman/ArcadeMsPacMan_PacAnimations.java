/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.actors.Actor;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import de.amr.games.pacman.ui._2d.SpriteAnimationSet;
import de.amr.games.pacman.uilib.animation.SpriteAnimation;
import de.amr.games.pacman.uilib.assets.SpriteSheet;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class ArcadeMsPacMan_PacAnimations extends SpriteAnimationSet implements PacAnimations {

    public static final String ANIM_PAC_MAN_MUNCHING = "pacman_munching";

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
            PacAnimations.ANIM_MUNCHING, munching,
            PacAnimations.ANIM_DYING, dying,
            ANIM_PAC_MAN_MUNCHING, husbandMunching
        ));
    }

    @Override
    protected RectArea[] selectedSprites(SpriteSheet spriteSheet, Actor actor) {
        ArcadeMsPacMan_SpriteSheet gss = (ArcadeMsPacMan_SpriteSheet) spriteSheet;
        if (actor instanceof Pac msPacMan) {
            if (isCurrentAnimationID(PacAnimations.ANIM_MUNCHING)) {
                return gss.pacMunchingSprites(msPacMan.moveDir());
            }
            if (isCurrentAnimationID(ANIM_PAC_MAN_MUNCHING)) {
                return gss.mrPacManMunchingSprites(msPacMan.moveDir());
            }
        }
        return super.selectedSprites(spriteSheet, actor);
    }
}