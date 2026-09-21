/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.level;

import de.amr.basics.QuerySet;
import de.amr.pacmanfx.core.Energizer;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.GameEntityComp;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.ghost.GhostState;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class GameLevelEntities {

    private Pac thePac;
    private final EnumMap<GhostPersonality, Ghost> theGhosts = new EnumMap<>(GhostPersonality.class);
    private Bonus theBonus;

    private final QuerySet<GameEntity> otherEntities = new QuerySet<>();

    private final List<Energizer> theEnergizers = new ArrayList<>();

    public void add(GameEntity entity) {
        requireNonNull(entity);
        switch (entity) {
            case Pac pac -> {
                if (thePac != null) {
                    throw new IllegalArgumentException("Pac %s already added to entity set!".formatted(pac.name()));
                }
                thePac = pac;
            }
            case Ghost ghost -> {
                if (theGhosts.containsKey(ghost.personality())) {
                    throw new IllegalArgumentException("Ghost %s already added to entity set!".formatted(ghost.name()));
                }
                theGhosts.put(ghost.personality(), ghost);
            }
            case Bonus bonus -> {
                if (theBonus != null) {
                    throw new IllegalArgumentException("Bonus %s already added to entity set!".formatted(bonus.name()));
                }
                theBonus = bonus;
            }
            case Energizer energizer -> theEnergizers.add(energizer);
            default -> otherEntities.add(entity);
        }
    }

    public void remove(GameEntity entity) {
        requireNonNull(entity);
        switch (entity) {
            case Pac   _ -> thePac = null;
            case Ghost ghost -> theGhosts.remove(ghost.personality());
            case Bonus _ -> theBonus = null;
            case Energizer energizer -> theEnergizers.remove(energizer);
            default -> otherEntities.remove(entity);
        }
    }

    public void removeAll() {
        all().collect(Collectors.toCollection(ArrayList::new)).forEach(this::remove);
    }

    public Stream<? extends GameEntity> all() {
        return Stream.of(
            Optional.ofNullable(thePac).stream(),
            theGhosts.values().stream(),
            theEnergizers.stream(),
            Optional.ofNullable(theBonus).stream(),
            otherEntities.all()
        )
        .flatMap(Function.identity());
    }

    @SafeVarargs
    public final Stream<? extends GameEntity> allWithComponents(Class<? extends GameEntityComp>... componentClasses) {
        return all().filter(entity -> Stream.of(componentClasses).allMatch(entity::hasComp));
    }

    public QuerySet<GameEntity> otherEntities() {
        return otherEntities;
    }

    public Pac pac() {
        return thePac;
    }

    public List<Ghost> ghosts() {
        return List.copyOf(theGhosts.values());
    }

    public Stream<Ghost> ghostsInState(GhostState state) {
        requireNonNull(state);
        return theGhosts.values().stream().filter(ghost -> state.equals(ghost.state().enumValue()));
    }

    /**
     * @param personality a ghost personality (e.g. {@link GhostPersonality#ORANGE_GHOST_POKEY})
     * @return the ghost with this ID
     */
    public Ghost ghost(GhostPersonality personality) {
        requireNonNull(personality);
        if (!theGhosts.containsKey(personality)) {
            throw new IllegalArgumentException("Ghost %s not added to entity set!".formatted(personality.name()));
        }
        return theGhosts.get(personality);
    }

    public Stream<Ghost> ghostsInAnyOfStates(Collection<GhostState> states) {
        requireNonNull(states);
        return theGhosts.values().stream().filter(ghost -> states.contains(ghost.state().enumValue()));
    }

    public Optional<Bonus> optBonus() {
        return Optional.ofNullable(theBonus);
    }

    public List<Energizer> theEnergizers() {
        return theEnergizers;
    }
}
