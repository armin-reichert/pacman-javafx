/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.spriteanim;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Manages the set of sprite animations in the game. Only active sprites are animated by the
 * sprite animation timer.
 */
public class SpriteAnimationContainer {

    private final Set<SpriteAnimation> activeAnimations = new HashSet<>();
    private final Set<SpriteAnimation> waitingToBeAdded = new HashSet<>();
    private final Set<SpriteAnimation> waitingToBeRemoved = new HashSet<>();

    private boolean clearRequested;

    public SpriteAnimationContainer() {}

    public void update() {
        if (clearRequested) {
            activeAnimations.clear();
            waitingToBeAdded.clear();
            waitingToBeRemoved.clear();
            clearRequested = false;
        }

        if (!waitingToBeRemoved.isEmpty()) {
            activeAnimations.removeAll(waitingToBeRemoved);
            waitingToBeRemoved.clear();
        }

        if (!waitingToBeAdded.isEmpty()) {
            activeAnimations.addAll(waitingToBeAdded);
            waitingToBeAdded.clear();
        }
    }

    public Set<SpriteAnimation> activeAnimations() {
        update();
        return activeAnimations;
    }

    public void add(SpriteAnimation animation) {
        requireNonNull(animation);
        waitingToBeAdded.add(animation);
    }

    public void remove(SpriteAnimation animation) {
        requireNonNull(animation);
        waitingToBeRemoved.add(animation);
    }

    public void clear() {
        clearRequested = true;
    }
}
