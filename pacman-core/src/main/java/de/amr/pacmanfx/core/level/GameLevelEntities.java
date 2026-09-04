/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.level;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.GameEntityComp;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.model.GhostPersonality;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class GameLevelEntities {

    private Pac thePac;
    private final EnumMap<GhostPersonality, Ghost> theGhosts = new EnumMap<>(GhostPersonality.class);
    private Bonus theBonus;
    private House theHouse;
    private final List<GhostPoints> theGhostPoints = new ArrayList<>();
    private final List<BonusPoints> theBonusPoints = new ArrayList<>();

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
            case GhostPoints points -> theGhostPoints.add(points);
            case Bonus bonus -> {
                if (theBonus != null) {
                    throw new IllegalArgumentException("Bonus %s already added to entity set!".formatted(bonus.name()));
                }
                theBonus = bonus;
            }
            case BonusPoints bonusPoints -> theBonusPoints.add(bonusPoints);
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
            case Pac   _ -> thePac = null;
            case Ghost ghost -> theGhosts.remove(ghost.personality());
            case GhostPoints points -> theGhostPoints.remove(points);
            case Bonus _ -> theBonus = null;
            case BonusPoints bonusPoints -> theBonusPoints.remove(bonusPoints);
            case House _ -> theHouse = null;
            default -> throw new IllegalArgumentException("Unknown entity type!");
        }
    }

    public Stream<? extends GameEntity> all() {
        return Stream.of(
            Optional.ofNullable(thePac).stream(),
            theGhosts.values().stream(),
            Optional.ofNullable(theBonus).stream(),
            Optional.ofNullable(theHouse).stream(),
            theGhostPoints.stream(),
            theBonusPoints.stream()).flatMap(Function.identity());
    }

    @SafeVarargs
    public final Stream<? extends GameEntity> allWith(Class<? extends GameEntityComp>... componentClasses) {
        return all().filter(entity -> Stream.of(componentClasses).allMatch(entity::hasComp));
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

    public List<BonusPoints> theBonusPoints() {
        return theBonusPoints;
    }

    public House house() {
        return theHouse;
    }

    public List<GhostPoints> theGhostPoints() {
        return theGhostPoints;
    }
}
