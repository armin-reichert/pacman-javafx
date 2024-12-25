/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

/**
 * @author Armin Reichert
 */
public interface Animations {

    // Common animation IDs
    String ANIM_PAC_MUNCHING     = "pac_munching";
    String ANIM_PAC_DYING        = "pac_dying";
    String ANIM_GHOST_NORMAL     = "ghost_normal";
    String ANIM_GHOST_FRIGHTENED = "ghost_frightened";
    String ANIM_GHOST_EYES       = "ghost_eyes";
    String ANIM_GHOST_FLASHING   = "ghost_flashing";
    String ANIM_GHOST_NUMBER     = "ghost_number";

    // Pac-Man game specific
    String ANIM_BLINKY_DAMAGED   = "damaged";
    String ANIM_BLINKY_STRETCHED = "stretched";
    String ANIM_BLINKY_PATCHED   = "patched";
    String ANIM_BLINKY_NAKED     = "naked";

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

    String currentID();
    void select(String id, int frameIndex);
    default void select(String id) { select(id, 0); }
    void start();
    void stop();
    void reset();
}