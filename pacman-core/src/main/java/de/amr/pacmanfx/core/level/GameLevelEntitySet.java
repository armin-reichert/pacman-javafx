/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.level;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.model.GhostPersonality;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class GameLevelEntitySet {

    private Pac thePac;
    private final EnumMap<GhostPersonality, Ghost> theGhosts = new EnumMap<>(GhostPersonality.class);
    private Bonus theBonus;
    private House theHouse;

    public void add(GameEntity entity) {
        requireNonNull(entity);
        switch (entity) {
            case Ghost ghost -> {
                if (theGhosts.containsKey(ghost.personality())) {
                    throw new IllegalArgumentException("Ghost %s already added to entity set!".formatted(ghost.name()));
                }
                theGhosts.put(ghost.personality(), ghost);
            }
            case Pac pac -> {
                if (thePac != null) {
                    throw new IllegalArgumentException("Pac %s already added to entity set!".formatted(pac.name()));
                }
                thePac = pac;
            }
            case Bonus bonus -> {
                if (theBonus != null) {
                    throw new IllegalArgumentException("Bonus %s already added to entity set!".formatted(bonus.name()));
                }
                theBonus = bonus;
            }
            case House house -> {
                if (theHouse != null) {
                    throw new IllegalArgumentException("House %s already added to entity set!".formatted(house.name()));
                }
                theHouse = house;
            }
            default -> throw new IllegalArgumentException("Unknown entity type!");
        }
    }

    public void remove(GameEntity entity) {
        requireNonNull(entity);
        switch (entity) {
            case Ghost ghost -> theGhosts.remove(ghost.personality());
            case Pac   _ -> thePac = null;
            case Bonus _ -> theBonus = null;
            case House _ -> theHouse = null;
            default -> throw new IllegalArgumentException("Unknown entity type!");
        }
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

    public House house() {
        return theHouse;
    }
}
