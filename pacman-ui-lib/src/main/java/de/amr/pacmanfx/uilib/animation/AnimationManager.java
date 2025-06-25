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

    private String dump() {
        StringBuilder sb = new StringBuilder();
        animationMap.forEach((id, animation) -> {
            sb.append("%10s => %20s (%s)\n".formatted(id, animation, animation.getStatus()));
        });
        return sb.toString();
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
        Logger.info("New animation map entry ID={}", id);
        return animation;
    }

    public void registerAndPlayFromStart(Node node, String animationName, Animation animation) {
        register(node, animationName, animation);
        animation.playFromStart();
        String id = makeID(node, animationName);
        Logger.info("Playing animation ID='{}' ({})", id, animation);
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
