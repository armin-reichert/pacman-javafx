/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

/**
 * @author Armin Reichert
 */
public interface Animations {

    // Common Pac-Man/Ms. Pac-Man animation IDs
    String ANIM_PAC_MUNCHING     = "pac_munching";
    String ANIM_PAC_DYING        = "pac_dying";

    // Common ghost animation IDs
    String ANIM_GHOST_NORMAL     = "ghost_normal";
    String ANIM_GHOST_FRIGHTENED = "ghost_frightened";
    String ANIM_GHOST_EYES       = "ghost_eyes";
    String ANIM_GHOST_FLASHING   = "ghost_flashing";
    String ANIM_GHOST_NUMBER     = "ghost_number";

    // Ms. Pac-Man game specific
    String ANIM_MR_PACMAN_MUNCHING = "pacman_munching";
    String ANIM_PAC_BIG = "big_pacman";

    String ANIM_BLINKY_DAMAGED   = "damaged";
    String ANIM_BLINKY_STRETCHED = "stretched";
    String ANIM_BLINKY_PATCHED   = "patched";
    String ANIM_BLINKY_NAKED     = "naked";

    String currentAnimationID();

    Object currentAnimation();

    void select(String name, int index);

    void startCurrentAnimation();

    void stopCurrentAnimation();

    void resetCurrentAnimation();
}