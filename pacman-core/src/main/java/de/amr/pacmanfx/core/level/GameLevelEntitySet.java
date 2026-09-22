/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.level;

import de.amr.basics.QuerySet;
import de.amr.basics.ui.ecs.GameEntity;
import de.amr.basics.ui.ecs.GameEntityComp;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.ghost.GhostState;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class GameLevelEntitySet {

    private final QuerySet<GameEntity> entities = new QuerySet<>();

    public void add(GameEntity entity) {
        requireNonNull(entity);
        entities.add(entity);
    }

    public void remove(GameEntity entity) {
        requireNonNull(entity);
        entities.remove(entity);
    }

    public void removeAll() {
        all().collect(Collectors.toCollection(ArrayList::new)).forEach(this::remove);
    }

    public Stream<? extends GameEntity> all() {
        return entities.all();
    }

    @SafeVarargs
    public final Stream<? extends GameEntity> allWithComponents(Class<? extends GameEntityComp>... componentClasses) {
        return all().filter(entity -> Stream.of(componentClasses).allMatch(entity::hasComp));
    }

    public QuerySet<GameEntity> entities() {
        return entities;
    }

    public List<Ghost> ghosts() {
        return entities.ofType(Ghost.class).sorted(Comparator.comparing(Ghost::personality)).toList();
    }

    public Stream<Ghost> ghostsInState(GhostState state) {
        requireNonNull(state);
        return entities.ofType(Ghost.class).filter(ghost -> state.equals(ghost.state().enumValue()));
    }

    public Stream<Ghost> ghostsInAnyOfStates(Collection<GhostState> stateAlternatives) {
        requireNonNull(stateAlternatives);
        return entities.ofType(Ghost.class).filter(ghost -> stateAlternatives.contains(ghost.state().enumValue()));
    }

    /**
     * @param personality a ghost personality (e.g. {@link GhostPersonality#ORANGE_GHOST_POKEY})
     * @return the ghost with this ID
     */
    public Ghost ghost(GhostPersonality personality) {
        requireNonNull(personality);
        return entities.ofType(Ghost.class)
            .filter(ghost -> ghost.personality() == personality)
            .findFirst()
            .orElseThrow();
    }

    public Pac pac() {
        return entities.theOne(Pac.class);
    }
}
