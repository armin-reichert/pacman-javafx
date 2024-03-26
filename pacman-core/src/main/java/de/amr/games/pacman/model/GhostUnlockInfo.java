/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.model.actors.Ghost;

/**
 * @author Armin Reichert
 */
public record GhostUnlockInfo(Ghost ghost, String reason) {
    public GhostUnlockInfo(Ghost ghost, String reason, Object... args) {
        this(ghost, String.format(reason, args));
    }
}
