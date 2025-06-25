/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import javafx.animation.Animation;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

import static de.amr.pacmanfx.Validations.requireValidIdentifier;
import static java.util.Objects.requireNonNull;

/**
 * 3D entities with animations implement this interface such that all potentially running animations can be stopped
 * when the containing 3D scene ends, e.g. when the quit action is executed.
 */
public class AnimationRegistry {

    private final Map<String, Animation> animationMap = new WeakHashMap<>();

    public void register(String name, Animation animation) {
        animationMap.put(requireValidIdentifier(name), requireNonNull(animation));
    }

    public void registerAndPlayFromStart(String name, Animation animation) {
        register(name, animation);
        animation.playFromStart();
        Logger.info("Playing animation '{}' ({})", name, animation);
    }

    public void stopAll() {
        for (Map.Entry<String, Animation> entry : new ArrayList<>(animationMap.entrySet())) {
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
