/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.ms_pacman_tengen.MsPacManGameTengen;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.util.SpriteAnimationCollection;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class PacAnimations extends SpriteAnimationCollection {

    public PacAnimations(MsPacManGameTengenSpriteSheet spriteSheet) {
        checkNotNull(spriteSheet);

        var msPacManMunching = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man munching")
            .sprites(MsPacManGameTengenSpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT)
            .loop();

        var msPacManMunchingBooster = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man munching booster mode")
            .sprites(MsPacManGameTengenSpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER)
            .loop();

        var msPacManWavingHand = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man waving hand")
            .sprites(MsPacManGameTengenSpriteSheet.MS_PAC_WAVING_HAND)
            .frameTicks(8)
            .loop();

        var msPacManTurningAway = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man turning away")
            .sprites(MsPacManGameTengenSpriteSheet.MS_PAC_TURNING_AWAY)
            .frameTicks(15)
            .end();

        var msPacmanDying = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man dying")
            .sprites(spriteSheet.pacDyingSprites())
            .frameTicks(8)
            .end();

        var mrPacManMunching = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man's husband munching")
            .sprites(MsPacManGameTengenSpriteSheet.MR_PAC_MUNCHING_SPRITES_LEFT)
            .frameTicks(2)
            .loop();

        var mrPacManMunchingBooster = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man's husband munching booster mode")
            .sprites(MsPacManGameTengenSpriteSheet.MR_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER)
            .frameTicks(2)
            .loop();

        var mrPacManWavingHand = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Pac-Man waving hand")
            .sprites(MsPacManGameTengenSpriteSheet.MR_PAC_WAVING_HAND)
            .frameTicks(8)
            .loop();

        var mrPacManTurningAway = SpriteAnimation
                .spriteSheet(spriteSheet)
                .info("Mr. Pac-Man turning away")
                .sprites(MsPacManGameTengenSpriteSheet.MR_PAC_TURNING_AWAY)
                .frameTicks(15)
                .end();

        var junior = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Junior Pac-Man")
            .sprites(MsPacManGameTengenSpriteSheet.JUNIOR_PAC_SPRITE)
            .end();

        add(Map.of(
            Animations.ANIM_PAC_DYING, msPacmanDying,
            Animations.ANIM_PAC_MUNCHING, msPacManMunching,
            MsPacManGameTengen.ANIM_MS_PACMAN_BOOSTER, msPacManMunchingBooster,
            MsPacManGameTengen.ANIM_MS_PACMAN_WAVING_HAND, msPacManWavingHand,
            MsPacManGameTengen.ANIM_MS_PACMAN_TURNING_AWAY, msPacManTurningAway,

            Animations.ANIM_MR_PACMAN_MUNCHING, mrPacManMunching,
            MsPacManGameTengen.ANIM_MR_PACMAN_BOOSTER, mrPacManMunchingBooster,
            MsPacManGameTengen.ANIM_MR_PACMAN_WAVING_HAND, mrPacManWavingHand,
            MsPacManGameTengen.ANIM_MR_PACMAN_TURNING_AWAY, mrPacManTurningAway,
            MsPacManGameTengen.ANIM_JUNIOR_PACMAN, junior
        ));
    }

    @Override
    protected RectArea[] selectedSprites(GameSpriteSheet spriteSheet, Entity entity) {
        if (entity instanceof Pac msPacMan) {
            if (isCurrentAnimationID(Animations.ANIM_PAC_MUNCHING)) {
                return MsPacManGameTengenSpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT;
            }
            if (isCurrentAnimationID(MsPacManGameTengen.ANIM_MS_PACMAN_BOOSTER)) {
                return MsPacManGameTengenSpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER;
            }
            if (isCurrentAnimationID(Animations.ANIM_MR_PACMAN_MUNCHING)) {
                return ((MsPacManGameTengenSpriteSheet)spriteSheet).pacManMunchingSprites(msPacMan.moveDir());
            }
        }
        return super.selectedSprites(spriteSheet, entity);
    }
}