/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import javafx.animation.Animation;
import org.tinylog.Logger;

import java.util.Collection;
import java.util.List;

/**
 * 3D entities with animations implement this interface such that all potentially running animations can be stopped
 * when the containing 3D scene ends, e.g. when the quit action is executed.
 */
public interface AnimationProvider {

    Collection<Animation> animations();

     default void stopAnimations() {
        animations().forEach(animation -> {
            try {
                animation.stop();
                Logger.info("{}: Animation {} stopped", getClass().getSimpleName(), animation);
            } catch (IllegalStateException x) {
                Logger.warn("{}: Animation could not be stopped (embedded?)", getClass().getSimpleName());
            }
        });
    }
}
