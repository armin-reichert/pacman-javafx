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
    private final Set<SpriteAnimation> animationsWaitingToBeAdded = new HashSet<>();
    private final Set<SpriteAnimation> animationsWaitingToBeRemoved = new HashSet<>();

    private boolean clearRequested;

    public SpriteAnimationContainer() {}

    public Iterable<SpriteAnimation> activeAnimations() {
        if (clearRequested) {
            activeAnimations.clear();
            animationsWaitingToBeAdded.clear();
            animationsWaitingToBeRemoved.clear();
            clearRequested = false;
        }

        if (!animationsWaitingToBeRemoved.isEmpty()) {
            activeAnimations.removeAll(animationsWaitingToBeRemoved);
            animationsWaitingToBeRemoved.clear();
        }

        if (!animationsWaitingToBeAdded.isEmpty()) {
            activeAnimations.addAll(animationsWaitingToBeAdded);
            animationsWaitingToBeAdded.clear();
        }

        return activeAnimations;
    }

    public void add(SpriteAnimation animation) {
        requireNonNull(animation);
        animationsWaitingToBeAdded.add(animation);
    }

    public void remove(SpriteAnimation animation) {
        requireNonNull(animation);
        animationsWaitingToBeRemoved.add(animation);
    }

    public void clear() {
        clearRequested = true;
    }
}
