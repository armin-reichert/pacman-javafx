/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.ms_pacman.MsPacManArcadeGame;
import de.amr.games.pacman.model.ms_pacman_tengen.MsPacManTengenGame;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.util.SpriteAnimationCollection;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class PacAnimations extends SpriteAnimationCollection {

    public PacAnimations(MsPacManTengenGameSpriteSheet spriteSheet) {
        checkNotNull(spriteSheet);

        var msPacManMunching = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man munching")
            .sprites(MsPacManTengenGameSpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT)
            .loop();

        var msPacManMunchingBooster = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man munching booster mode")
            .sprites(MsPacManTengenGameSpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER)
            .loop();

        var msPacManWavingHand = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man waving hand")
            .sprites(MsPacManTengenGameSpriteSheet.MS_PAC_WAVING_HAND)
            .frameTicks(8)
            .loop();

        var msPacManTurningAway = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man turning away")
            .sprites(MsPacManTengenGameSpriteSheet.MS_PAC_TURNING_AWAY)
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
            .sprites(MsPacManTengenGameSpriteSheet.MR_PAC_MUNCHING_SPRITES_LEFT)
            .frameTicks(2)
            .loop();

        var mrPacManMunchingBooster = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Ms. Pac-Man's husband munching booster mode")
            .sprites(MsPacManTengenGameSpriteSheet.MR_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER)
            .frameTicks(2)
            .loop();

        var mrPacManWavingHand = SpriteAnimation
            .spriteSheet(spriteSheet)
            .info("Pac-Man waving hand")
            .sprites(MsPacManTengenGameSpriteSheet.MR_PAC_WAVING_HAND)
            .frameTicks(8)
            .loop();

        var mrPacManTurningAway = SpriteAnimation
                .spriteSheet(spriteSheet)
                .info("Mr. Pac-Man turning away")
                .sprites(MsPacManTengenGameSpriteSheet.MR_PAC_TURNING_AWAY)
                .frameTicks(15)
                .end();

        add(Map.of(
            GameModel.ANIM_PAC_DYING, msPacmanDying,
            GameModel.ANIM_PAC_MUNCHING, msPacManMunching,
            MsPacManTengenGame.ANIM_MS_PACMAN_BOOSTER, msPacManMunchingBooster,
            MsPacManTengenGame.ANIM_MS_PACMAN_WAVING_HAND, msPacManWavingHand,
            MsPacManTengenGame.ANIM_MS_PACMAN_TURNING_AWAY, msPacManTurningAway,

            MsPacManArcadeGame.ANIM_MR_PACMAN_MUNCHING, mrPacManMunching,
            MsPacManTengenGame.ANIM_MR_PACMAN_BOOSTER, mrPacManMunchingBooster,
            MsPacManTengenGame.ANIM_MR_PACMAN_WAVING_HAND, mrPacManWavingHand,
            MsPacManTengenGame.ANIM_MR_PACMAN_TURNING_AWAY, mrPacManTurningAway
        ));
    }

    @Override
    protected RectArea[] selectedSprites(GameSpriteSheet spriteSheet, Entity entity) {
        if (entity instanceof Pac msPacMan) {
            if (isCurrentAnimationID(GameModel.ANIM_PAC_MUNCHING)) {
                return MsPacManTengenGameSpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT;
            }
            if (isCurrentAnimationID(MsPacManTengenGame.ANIM_MS_PACMAN_BOOSTER)) {
                return MsPacManTengenGameSpriteSheet.MS_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER;
            }
            if (isCurrentAnimationID(MsPacManArcadeGame.ANIM_MR_PACMAN_MUNCHING)) {
                return ((MsPacManTengenGameSpriteSheet)spriteSheet).pacManMunchingSprites(msPacMan.moveDir());
            }
        }
        return super.selectedSprites(spriteSheet, entity);
    }
}