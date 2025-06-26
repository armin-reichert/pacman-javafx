/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import javafx.animation.Animation;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static de.amr.pacmanfx.Validations.requireValidIdentifier;
import static java.util.Objects.requireNonNull;

/**
 * 3D entities with animations implement this interface such that all potentially running animations can be stopped
 * when the containing 3D scene ends, e.g. when the quit action is executed.
 */
public class AnimationManager {

    private final Map<String, Animation> animationMap = new HashMap<>();

    private String makeID(String description) {
        return description + "#" + UUID.randomUUID();
    }

    public String register(String description, Animation animation) {
        requireValidIdentifier(description);
        requireNonNull(animation);
        String id = makeID(description);
        animationMap.put(id, animation);
        return id;
    }

    public void stopAllAnimations() {
        for (Map.Entry<String, Animation> entry : new ArrayList<>(animationMap.entrySet())) {
            String id = entry.getKey();
            Animation animation = entry.getValue();
            try {
                if (animation.getStatus() == Animation.Status.STOPPED) {
                    Logger.info("Already stopped: animation ID='{}' ({})", id, animation);
                } else {
                    animation.stop();
                    Logger.info("Stopped animation ID='{}' ({})", id, animation);
                }
            } catch (IllegalStateException x) {
                Logger.warn("Could not stop (embedded?) animation ID='{}' ({})", id, animation);
            }
        }
    }

    public void clearAnimations() {
        animationMap.clear();
    }

    public Map<String, Animation> animationMap() {
        return animationMap;
    }
}