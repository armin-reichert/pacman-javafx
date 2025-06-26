/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import javafx.animation.Animation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Map;

import static de.amr.pacmanfx.Validations.requireValidIdentifier;
import static java.util.Objects.requireNonNull;

/**
 * 3D entities with animations implement this interface such that all potentially running animations can be stopped
 * when the containing 3D scene ends, e.g. when the quit action is executed.
 */
public class AnimationManager {

    private final ObservableMap<String, Animation> animationMap = FXCollections.observableHashMap();

    private String makeID(Node node, String animationName) {
        return animationName + "@" + node.hashCode();
    }

    public Animation register(Node node, String animationName, Animation animation) {
        requireNonNull(node);
        requireValidIdentifier(animationName);
        requireNonNull(animation);
        String id = makeID(node, animationName);
        if (animationMap.containsKey(id)) {
            Logger.warn("Animation map already contains animation with ID '{}'", id);
        }
        animationMap.put(id, animation);
        Logger.info("New animation map entry, ID={}", id);
        return animation;
    }

    public void registerAndPlayFromStart(Node node, String animationName, Animation animation) {
        register(node, animationName, animation);
        animation.playFromStart();
        Logger.info("Playing animation ID='{}' ({})", makeID(node, animationName), animation);
    }

    public void remove(Node node, String animationName) {
        requireNonNull(node);
        requireValidIdentifier(animationName);
        String id = makeID(node, animationName);
        if (animationMap.containsKey(id)) {
            animationMap.remove(id);
            Logger.info("Removed animation map entry, ID={}", id);
        } else {
            Logger.warn("Cannot remove: Animation map does not contain animation with ID '{}'", id);
        }
    }

    public void stopAll() {
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
            animationMap.remove(id);
        }
    }

    public ObservableMap<String, Animation> animationMap() {
        return animationMap;
    }
}