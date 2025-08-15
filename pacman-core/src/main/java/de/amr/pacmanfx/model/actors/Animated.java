/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import java.util.Optional;

/**
 * Implemented by animated actors.
 */
public interface Animated {
    Optional<AnimationManager> animations();
}
