/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.level;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.rules.HuntingTimerStrategy;
import de.amr.pacmanfx.core.model.world.map.FoodState;
import de.amr.pacmanfx.core.model.world.map.WorldMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.core.Validations.requireValidLevelNumber;
import static java.util.Objects.requireNonNull;

/**
 * A game level contains the world and the actors.
 */
public class GameLevel {

    private final int number; // 1=first level

    private final WorldMap worldMap;
    private final GameLevelEntitySet entities = new GameLevelEntitySet();
    private final Pulse heartbeat;
    private final List<Ghost> ghostKillChain = new ArrayList<>();
    private final int[] bonusSymbolCodes = new int[2];

    private final HuntingTimerStrategy huntingTimerStrategy;

    private byte currentBonusIndex; // -1=no bonus, 0=first, 1=second

    private final FoodState foodState;

    public GameLevel(int number, WorldMap worldMap, HuntingTimerStrategy huntingTimerStrategy) {
        this.number = requireValidLevelNumber(number);
        this.worldMap = requireNonNull(worldMap);
        this.huntingTimerStrategy = requireNonNull(huntingTimerStrategy);

        foodState = new FoodState(worldMap.foodLayer());

        heartbeat = new Pulse(10, Pulse.State.OFF);
        currentBonusIndex = -1;

        huntingTimerStrategy.reset();
    }

    public FoodState food() {
        return foodState;
    }

    /**
     * @return level number (starting with 1)
     */
    public int number() {
        return number;
    }

    /**
     * @return the pulse driving the blinking animation for the energizers.
     */
    public Pulse heartbeat() {
        return heartbeat;
    }

    /**
     * @return the map used in this level.
     */
    public WorldMap worldMap() {
        return worldMap;
    }

    /**
     * @return the timer controlling the hunting phases (scattering and chasing).
     */
    public HuntingTimerStrategy huntingTimerStrategy() {
        return huntingTimerStrategy;
    }

    /**
     * Makes Pac-Man and the ghosts invisible.
     */
    public void hidePacAndGhosts() {
        entities.pac().hide();
        entities.ghosts().forEach(GameEntity::hide);
    }

    // Ghost kill chain

    public void clearGhostKillChain() {
        ghostKillChain.clear();
    }

    public void addToGhostKillChain(Ghost ghost) {
        requireNonNull(ghost);
        if (ghostKillChain.contains(ghost)) {
            throw new IllegalArgumentException("Ghost kill chain already contains ghost %s".formatted(ghost.name()));
        }
        ghostKillChain.add(ghost);
    }

    public int ghostKillChainSize() {
        return ghostKillChain.size();
    }

    public boolean isInGhostKilledChain(Ghost ghost) {
        requireNonNull(ghost);
        return ghostKillChain.contains(ghost);
    }

    public int indexInGhostKilledChain(Ghost ghost) {
        requireNonNull(ghost);
        return ghostKillChain.indexOf(ghost);
    }

    public GameLevelEntitySet entities() {
        return entities;
    }

    /**
     * Sets the Pac-Man used in this level.
     * @param pac Pac-Man or Ms. Pac-Man
     */
    public void setPac(Pac pac) {
        requireNonNull(pac);
        entities.add(pac);
    }

    /**
     * Sets the ghosts used in this level.
     * @param redGhost Blinky, the red ghost
     * @param pinkGhost Pinky, the pink ghost
     * @param cyanGhost Inky, the cyan ghost
     * @param orangeGhost Clyde/Sue, the orange ghost
     */
    public void setGhosts(Ghost redGhost, Ghost pinkGhost, Ghost cyanGhost, Ghost orangeGhost) {
        entities.add(requireNonNull(redGhost));
        entities.add(requireNonNull(pinkGhost));
        entities.add(requireNonNull(cyanGhost));
        entities.add(requireNonNull(orangeGhost));
    }

    /**
     * @param personality a ghost personality (e.g. {@link GhostPersonality#ORANGE_GHOST_POKEY})
     * @return the ghost with this ID
     */
    public Ghost ghost(GhostPersonality personality) {
        requireNonNull(personality);
        return entities.ghosts().get(personality.ordinal());
    }

    public Stream<Ghost> ghostsInAnyOfStates(Collection<GhostState> states) {
        requireNonNull(states);
        return entities.ghosts().stream().filter(ghost -> states.contains(ghost.ghostStateEnum()));
    }

    public Stream<Ghost> ghostsInState(GhostState state) {
        requireNonNull(state);
        return entities.ghosts().stream().filter(ghost -> state.equals(ghost.ghostStateEnum()));
    }

    // Bonus

    /**
     * @return (Optional) bonus actor used in this level
     */
    public Optional<Bonus> optBonus() {
        return entities.optBonus();
    }

    /**
     * Sets the bonus actor used in this level. This happens when the bonus gets activated.
     * @param bonus the bonus
     */
    public void setBonus(Bonus bonus) {
        entities.optBonus().ifPresent(entities::remove);
        entities.add(requireNonNull(bonus));
    }

    /**
     * @return the index of the current bonus
     */
    public int currentBonusIndex() {
        return currentBonusIndex;
    }

    /**
     * Selects the next bonus and increments the bonus index.
     */
    public void selectNextBonus() {
        ++currentBonusIndex;
    }

    /**
     * @param i the bonus index (0 for the first bonus spawned in the level, ...)
     * @return the bonus symbol code of the bonus with the given index
     */
    public int bonusSymbolCode(int i) {
        return bonusSymbolCodes[i];
    }

    /**
     * @param i the bonus index (0 for the first bonus spawned in the level, ...)
     * @param symbolCode the bonus symbol code
     */
    public void setBonusSymbolCode(int i, int symbolCode) {
        if (0 <= i && i < bonusSymbolCodes.length) {
            bonusSymbolCodes[i] = symbolCode;
        } else {
            throw new IllegalArgumentException("Cannot set bonus symbol at index " + i);
        }
    }
}