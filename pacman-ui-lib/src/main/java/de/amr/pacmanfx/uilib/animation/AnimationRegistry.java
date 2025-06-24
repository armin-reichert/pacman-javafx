/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import javafx.animation.Animation;
import org.tinylog.Logger;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * 3D entities with animations implement this interface such that all potentially running animations can be stopped
 * when the containing 3D scene ends, e.g. when the quit action is executed.
 */
public class AnimationRegistry {

    private final Map<String, Animation> animationMap = new WeakHashMap<>();

    public void registerAnimation(String name, Animation animation) {
        animationMap.put(name, animation);
    }

    public void registerAnimationAndPlay(String name, Animation animation) {
        animationMap.put(name, animation);
        animation.play();
        Logger.info("Playing animation '{}' ({})", name, animation);
    }

    public void stopRegisteredAnimations() {
        @SuppressWarnings("unchecked") Map.Entry<String, Animation>[] copy
                = (Map.Entry<String, Animation>[]) animationMap.entrySet().toArray(Map.Entry[]::new);
        for (Map.Entry<String, Animation> entry : copy) {
            String name = entry.getKey();
            Animation animation = entry.getValue();
            try {
                animation.stop();
                Logger.info("Stopped animation '{}' ({})", name, animation);
            } catch (IllegalStateException x) {
                Logger.warn("Could not stop (embedded?) animation '{}' ({})", name, animation);
            }
            animationMap.remove(name);
        }
    }
}
