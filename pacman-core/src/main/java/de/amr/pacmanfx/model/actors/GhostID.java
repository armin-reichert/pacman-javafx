/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.Validations;

public record GhostID(byte personality, String name) {

    public GhostID {
        Validations.requireValidGhostPersonality(personality);
        Validations.requireValidIdentifier(name);
    }
}
