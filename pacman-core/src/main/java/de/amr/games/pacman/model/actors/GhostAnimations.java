package de.amr.games.pacman.model.actors;

public interface GhostAnimations extends Animations {
    String ANIM_GHOST_NORMAL     = "ghost_normal";
    String ANIM_GHOST_FRIGHTENED = "ghost_frightened";
    String ANIM_GHOST_EYES       = "ghost_eyes";
    String ANIM_GHOST_FLASHING   = "ghost_flashing";
    String ANIM_GHOST_NUMBER     = "ghost_number";

    // Pac-Man game specific
    String ANIM_BLINKY_DAMAGED   = "damaged";
    String ANIM_BLINKY_NAIL_DRESS_RAPTURE = "stretched";
    String ANIM_BLINKY_PATCHED   = "patched";
    String ANIM_BLINKY_NAKED     = "naked";
}
