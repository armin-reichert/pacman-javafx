/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import javafx.animation.Animation;
import org.tinylog.Logger;

import java.util.Map;

/**
 * 3D entities with animations implement this interface such that all potentially running animations can be stopped
 * when the containing 3D scene ends, e.g. when the quit action is executed.
 */
public interface AnimationRegistry {

    Map<String, Animation> registeredAnimations();

     default <T extends Animation> T playRegisteredAnimation(String name, T animation) {
        registeredAnimations().put(name, animation);
        animation.playFromStart();
        Logger.info("Playing animation '{}'", name);
        return animation;
     }

     default void stopActiveAnimations() {
        Map<String, Animation> original = registeredAnimations();
        @SuppressWarnings("unchecked") Map.Entry<String, Animation>[] copy = (Map.Entry<String, Animation>[]) original.entrySet().toArray(Map.Entry[]::new);
        String host = getClass().getSimpleName();
        for (Map.Entry<String, Animation> entry : copy) {
            String name = entry.getKey();
            Animation animation = entry.getValue();
            try {
                animation.stop();
                Logger.info("{}: Stopped animation '{}' ({})", host, name, animation);
            } catch (IllegalStateException x) {
                Logger.warn("{}: Could not stop (embedded?) animation '{}' ({})", host, name, animation);
            }
            registeredAnimations().remove(name);
        }
    }
}
