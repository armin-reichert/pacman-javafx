/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import javafx.animation.Animation;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.pacmanfx.uilib.animation.ManagedAnimation.CONTINUE;
import static java.util.Objects.requireNonNull;

/**
 * A central registry for managed animations.
 */
public class AnimationManager {

    private final Map<String, ManagedAnimation> animationMap = new HashMap<>();

    public void register(String label, ManagedAnimation managedAnimation) {
        requireNonNull(label);
        requireNonNull(managedAnimation);
        if (animationMap.containsValue(managedAnimation)) {
            Logger.info("Animation with label '{}' is already registered", label);
        } else {
            String id = label + "_" + UUID.randomUUID();
            animationMap.put(id, managedAnimation);
            Logger.info("Animation registered: ID='{}'", id);
        }
    }

    public void playAnimation(ManagedAnimation ma, boolean playMode) {
        ma.getOrCreateAnimation();
        if (ma.animation.getStatus() != Animation.Status.RUNNING) {
            if (playMode == ManagedAnimation.FROM_START) {
                Logger.trace("Playing animation with label '{}' from start", ma.label);
                ma.animation.playFromStart();
            } else if (playMode == CONTINUE) {
                Logger.trace("Continuing animation with label '{}'", ma.label);
                ma.animation.play();
            }
        }
    }

    public void pauseAnimation(ManagedAnimation ma) {
        requireNonNull(ma);
        ma.animation().ifPresent(animation -> {
            try {
                if (animation.getStatus() == Animation.Status.PAUSED) {
                    Logger.debug("Already paused: animation with label='{}' ({})", ma.label(), ma);
                } else {
                    animation.pause();
                    Logger.debug("Paused animation with label='{}' ({})", ma.label(), ma);
                }
            } catch (IllegalStateException x) {
                Logger.warn("Could not pause (embedded?) animation with label='{}' ({})", ma.label(), ma);
            }
        });
    }

    public void stopAnimation(ManagedAnimation ma) {
        requireNonNull(ma);
        ma.animation().ifPresent(animation -> {
            try {
                if (animation.getStatus() == Animation.Status.STOPPED) {
                    Logger.debug("Already stopped: animation with label='{}' ({})", ma.label(), ma);
                } else {
                    animation.stop();
                    Logger.debug("Stopped animation with label='{}' ({})", ma.label(), ma);
                }
            } catch (IllegalStateException x) {
                Logger.warn("Could not stop (embedded?) animation with label='{}' ({})", ma.label(), ma);
            }
        });
    }

    public void pauseAllAnimations() {
        animationMap.values().forEach(this::pauseAnimation);
    }

    public void stopAllAnimations() {
        animationMap.values().forEach(this::stopAnimation);
    }

    public void removeAllAnimations() {
        animationMap.clear();
        Logger.info("Animations removed");
    }

    public Map<String, ManagedAnimation> animationMap() {
        return Collections.unmodifiableMap(animationMap);
    }
}