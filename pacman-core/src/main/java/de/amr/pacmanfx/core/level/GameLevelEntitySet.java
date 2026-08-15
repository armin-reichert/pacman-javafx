/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.level;

import de.amr.basics.QuerySet;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.model.GhostPersonality;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class GameLevelEntitySet {

    private final QuerySet<GameEntity> entities = new QuerySet<>();

    private Pac thePac;
    private final List<Ghost> ghosts = new ArrayList<>();
    private Bonus theBonus;
    private House theHouse;
    private MessageView theMessageView;

    public void add(GameEntity entity) {
        requireNonNull(entity);
        switch (entity) {
            case Ghost ghost -> {
                if (ghosts.contains(ghost)) {
                    throw new IllegalArgumentException("Ghost %s already added to entity set!".formatted(ghost.name()));
                }
                ghosts.add(ghost);
            }
            case Pac   pac ->   thePac = pac;
            case Bonus bonus -> theBonus = bonus;
            case House house -> theHouse = house;
            case MessageView messageView -> theMessageView = messageView;
            default -> entities.add(entity);
        }
    }

    public void remove(GameEntity entity) {
        requireNonNull(entity);
        switch (entity) {
            case Ghost _ -> ghosts.remove(entity);
            case Pac   _ -> thePac = null;
            case Bonus _ -> theBonus = null;
            case House _ -> theHouse = null;
            case MessageView _ -> theMessageView = null;
            default -> entities.remove(entity);
        }
    }

    public Pac pac() {
        return thePac;
    }

    public List<Ghost> ghosts() {
        return ghosts;
    }

    public Stream<Ghost> ghostsInState(GhostState state) {
        requireNonNull(state);
        return ghosts().stream().filter(ghost -> state.equals(ghost.ghostStateEnum()));
    }

    /**
     * @param personality a ghost personality (e.g. {@link GhostPersonality#ORANGE_GHOST_POKEY})
     * @return the ghost with this ID
     */
    public Ghost ghost(GhostPersonality personality) {
        requireNonNull(personality);
        return ghosts.stream().filter(ghost -> ghost.personality() == personality).findAny().orElseThrow();
    }

    public Stream<Ghost> ghostsInAnyOfStates(Collection<GhostState> states) {
        requireNonNull(states);
        return ghosts().stream().filter(ghost -> states.contains(ghost.ghostStateEnum()));
    }

    public Optional<Bonus> optBonus() {
        return Optional.ofNullable(theBonus);
    }

    public House house() {
        return theHouse;
    }

    public MessageView messageView() {
        return theMessageView;
    }
}
