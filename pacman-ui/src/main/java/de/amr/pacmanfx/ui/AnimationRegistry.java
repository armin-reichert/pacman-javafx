/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import javafx.animation.Animation;
import org.tinylog.Logger;

import java.util.Set;

/**
 * 3D entities with animations implement this interface such that all potentially running animations can be stopped
 * when the containing 3D scene ends, e.g. when the quit action is executed.
 */
public interface AnimationRegistry {

    Set<Animation> registeredAnimations();

     default <T extends Animation> T playRegisteredAnimation(T animation) {
        registeredAnimations().add(animation);
        animation.playFromStart();
        return animation;
     }

     default void stopActiveAnimations() {
        Set<Animation> original = registeredAnimations();
        Animation[] copy = original.toArray(Animation[]::new);
        for (Animation animation : copy) {
            try {
                animation.stop();
                Logger.info("{}: Animation {} stopped", getClass().getSimpleName(), animation);
            } catch (IllegalStateException x) {
                Logger.warn("{}: Animation could not be stopped (embedded?)", getClass().getSimpleName());
            }
            registeredAnimations().remove(animation);
        }
    }
}
