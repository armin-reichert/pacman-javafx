/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.level;

import de.amr.basics.QuerySet;
import de.amr.basics.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.ghost.GhostState;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class GameLevelEntitySet extends QuerySet<GameEntity> {

    public Stream<Ghost> ghosts() {
        return elementsOfType(Ghost.class).sorted(Comparator.comparing(Ghost::personality));
    }

    public Stream<Ghost> ghostsInState(GhostState state) {
        requireNonNull(state);
        return elementsOfType(Ghost.class).filter(ghost -> state.equals(ghost.state().enumValue()));
    }

    public Stream<Ghost> ghostsInAnyOfStates(Collection<GhostState> stateAlternatives) {
        requireNonNull(stateAlternatives);
        return elementsOfType(Ghost.class).filter(ghost -> stateAlternatives.contains(ghost.state().enumValue()));
    }

    /**
     * @param personality a ghost personality (e.g. {@link GhostPersonality#ORANGE_GHOST_POKEY})
     * @return the ghost with this ID
     */
    public Ghost ghost(GhostPersonality personality) {
        requireNonNull(personality);
        return elementsOfType(Ghost.class)
            .filter(ghost -> ghost.personality() == personality)
            .findFirst()
            .orElseThrow();
    }

    public Pac pac() {
        return theOne(Pac.class);
    }
}
