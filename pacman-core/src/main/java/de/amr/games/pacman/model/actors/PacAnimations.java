/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

/**
 * @author Armin Reichert
 */
public interface PacAnimations extends Animations {

    // Common animation IDs
    String ANIM_PAC_MUNCHING     = "pac_munching";
    String ANIM_PAC_DYING        = "pac_dying";

    // Ms. Pac-Man specific
    String ANIM_MR_PACMAN_MUNCHING = "pacman_munching";
    String ANIM_PAC_BIG            = "big_pacman";

    // Ms. Pac-Man (Tengen)
    String ANIM_MS_PACMAN_BOOSTER      = "ms_pacman_booster";
    String ANIM_MR_PACMAN_BOOSTER      = "pacman_booster";
    String ANIM_MS_PACMAN_WAVING_HAND  = "ms_pacman_waving_hand";
    String ANIM_MR_PACMAN_WAVING_HAND  = "mr_pacman_waving_hand";
    String ANIM_MS_PACMAN_TURNING_AWAY = "ms_pacman_turning_away";
    String ANIM_MR_PACMAN_TURNING_AWAY = "mr_pacman_turning_away";
    String ANIM_JUNIOR_PACMAN          = "junior";
}