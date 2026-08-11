/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.level;

import de.amr.basics.QuerySet;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.rules.HuntingTimerStrategy;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.pacmanfx.core.Validations.requireNonNegativeInt;
import static de.amr.pacmanfx.core.Validations.requireValidLevelNumber;
import static java.util.Objects.requireNonNull;

/**
 * A game level contains the world and the actors.
 */
public class GameLevel {

    // This is just an experimental class for a general entity set with cache
    public static class EntitySetWithCache extends QuerySet<GameEntity> {

        private Pac cachedPac;
        private List<Ghost> cachedGhosts;
        private Bonus cachedBonus;

        private void maybeInvalidateCache(GameEntity entity) {
            if (entity instanceof Pac) cachedPac = null;
            if (entity instanceof Ghost) cachedGhosts = null;
            if (entity instanceof Bonus) cachedBonus = null;
        }

        public void add(GameEntity entity) {
            super.add(entity);
            maybeInvalidateCache(entity);
        }

        public void remove(GameEntity entity) {
            super.remove(entity);
            maybeInvalidateCache(entity);
        }

        public Pac pac() {
            if (cachedPac == null) {
                cachedPac = theOne(Pac.class);
            }
            return cachedPac;
        }

        public List<Ghost> ghosts() {
            if  (cachedGhosts == null) {
                cachedGhosts = List.copyOf(selectAllOfType(Ghost.class)
                    .sorted(Comparator.comparing(Ghost::personality)).toList());
            }
            return cachedGhosts;
        }

        public Optional<Bonus> optBonus() {
            if (cachedBonus == null) {
                 cachedBonus = anyOfType(Bonus.class);
            }
            return Optional.ofNullable(cachedBonus);
        }
    }

    private final GameModel gameModel;
    private final int number; // 1=first level

    private final WorldMap worldMap;
    private final EntitySetWithCache entities = new EntitySetWithCache();
    private final Pulse heartbeat;
    private final List<Ghost> ghostKillChain = new ArrayList<>();
    private final int[] bonusSymbolCodes = new int[2];
    private final int numFlashes;

    private final HuntingTimerStrategy huntingTimerStrategy;

    private byte currentBonusIndex; // -1=no bonus, 0=first, 1=second
    private GameLevelMessage message;

    private int gameOverStateTicks;
    private long startTimeMillis;
    private float pacPowerSeconds;
    private float pacPowerFadingSeconds;

    public GameLevel(GameModel gameModel, int number, WorldMap worldMap, HuntingTimerStrategy huntingTimerStrategy, int numFlashes) {
        this.gameModel = requireNonNull(gameModel);
        this.number = requireValidLevelNumber(number);
        this.worldMap = requireNonNull(worldMap);
        this.huntingTimerStrategy = requireNonNull(huntingTimerStrategy);
        this.numFlashes = requireNonNegativeInt(numFlashes);

        heartbeat = new Pulse(10, Pulse.State.OFF);
        currentBonusIndex = -1;

        huntingTimerStrategy.reset();
    }

    /**
     * @return the game (model) this level belongs to.
     */
    public GameModel gameModel() {
        return gameModel;
    }

    /**
     * @return level number (starting with 1)
     */
    public int number() {
        return number;
    }

    /**
     * @return how often maze image flashes at the end of this level.
     */
    public int numFlashes() {
        return numFlashes;
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
     * Sets the duration (in seconds) of the power fading period of Pac-Man.
     * @param seconds number of seconds
     */
    public void setPacPowerFadingSeconds(float seconds) {
        this.pacPowerFadingSeconds = seconds;
    }

    /**
     * @return the duration (in seconds) of the power fading period of Pac-Man.
     */
    public float pacPowerFadingSeconds() {
        return pacPowerFadingSeconds;
    }

    /**
     * Sets the duration (in seconds) of the complete power period (including the fading at the end)  of Pac-Man.
     * @param seconds number of seconds
     */
    public void setPacPowerSeconds(float seconds) {
        this.pacPowerSeconds = seconds;
    }

    /**
     * @return the duration (in seconds) of the complete power period (including the fading at the end)  of Pac-Man.
     */
    public float pacPowerSeconds() {
        return pacPowerSeconds;
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

    /**
     * Record the start time of this level in milliseconds.
     * @param millis milliseconds
     */
    public void recordStartTime(long millis) { this.startTimeMillis = millis; }

    /**
     * @return the start time of this level in milliseconds
     */
    public long startTime() { return startTimeMillis; }

    /**
     * Sets the duration of the "game over" state in number of ticks.
     * @param ticks number of ticks
     */
    public void setGameOverStateTicks(int ticks) { gameOverStateTicks = ticks; }

    /**
     * @return the duration of the "game over" state in number of ticks
     */
    public int gameOverStateTicks() { return gameOverStateTicks; }

    /**
     * Sets the message that should be displayed in the level (READY, GAME OVER, TESTING).
     * @param message the message
     */
    public void setMessage(GameLevelMessage message) {
        this.message = message;
    }

    /**
     * Clears the level message.
     */
    public void clearMessage() {
        message = null;
    }

    /**
     * @return (optional) the current level message
     */
    public Optional<GameLevelMessage> optMessage() {
        return Optional.ofNullable(message);
    }

    public EntitySetWithCache entities() {
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

    // Others

    public boolean isIntersection(Vector2i tile) {
        final House house = entities().theOne(House.class);
        final TerrainLayer terrain = worldMap.terrainLayer();
        if (terrain.outOfBounds(tile) || terrain.isTileBlocked(tile)) {
            return false;
        }
        if (house != null && house.contains(tile)) {
            return false;
        }
        long inaccessible = 0;
        inaccessible += terrain.neighborTilesOutsideWorld(tile).count();
        inaccessible += terrain.neighborTilesInsideWorld(tile).filter(terrain::isTileBlocked).count();
        if (house != null) {
            inaccessible += terrain.neighborTilesInsideWorld(tile).filter(house::isDoorAt).count();
        }
        return inaccessible <= 1;
    }

}