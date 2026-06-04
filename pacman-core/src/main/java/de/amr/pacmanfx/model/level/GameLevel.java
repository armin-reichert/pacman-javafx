/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model.level;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.Globals;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.WorldMap;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.pacmanfx.core.Validations.*;
import static java.util.Objects.requireNonNull;

/**
 * A game level contains the world and the actors.
 */
public class GameLevel {

    // This is just an experimental class for a general entity set with cache
    public static class EntitySetWithCache implements Iterable<GameLevelEntity> {

        private final GameLevelEntitySet entitySet = new GameLevelEntitySet();

        private Pac cachedPac;
        private List<Ghost> cachedGhosts;
        private Bonus cachedBonus;

        private void maybeInvalidateCache(GameLevelEntity entity) {
            if (entity instanceof Pac) cachedPac = null;
            if (entity instanceof Ghost) cachedGhosts = null;
            if (entity instanceof Bonus) cachedBonus = null;
        }

        public void add(GameLevelEntity entity) {
            entitySet.add(entity);
            maybeInvalidateCache(entity);
        }

        public void remove(GameLevelEntity entity) {
            entitySet.remove(entity);
            maybeInvalidateCache(entity);
        }

        public Pac pac() {
            if (cachedPac == null) {
                cachedPac = entitySet.uniqueOfType(Pac.class);
            }
            return cachedPac;
        }

        public List<Ghost> ghosts() {
            if  (cachedGhosts == null) {
                cachedGhosts = List.copyOf(entitySet.selectAllOfType(Ghost.class)
                    .sorted(Comparator.comparing(Ghost::personality)).toList());
            }
            return cachedGhosts;
        }

        public Optional<Bonus> optBonus() {
            if (cachedBonus == null) {
                 cachedBonus = entitySet.anyOfType(Bonus.class);
            }
            return Optional.ofNullable(cachedBonus);
        }

        @Override
        public Iterator<GameLevelEntity> iterator() {
            return entitySet.iterator();
        }
    }

    private final GameModel game;
    private final int number; // 1=first level
    private final WorldMap worldMap;
    private final EntitySetWithCache entities = new EntitySetWithCache();
    private final HuntingTimer huntingTimer;
    private final Pulse heartbeat;
    private final List<Ghost> victims = new ArrayList<>();
    private final int[] bonusSymbolCodes = new int[2];
    private final int numFlashes;

    private byte currentBonusIndex; // -1=no bonus, 0=first, 1=second
    private GameLevelMessage message;

    private boolean demoLevel;
    private int gameOverStateTicks;
    private long startTimeMillis;
    private float pacPowerSeconds;
    private float pacPowerFadingSeconds;

    private int cutSceneNumber;

    public GameLevel(GameModel game, int number, WorldMap worldMap, HuntingTimer huntingTimer, int numFlashes) {
        this.game = requireNonNull(game);
        this.number = requireValidLevelNumber(number);
        this.worldMap = requireNonNull(worldMap);
        this.huntingTimer = requireNonNull(huntingTimer);
        this.numFlashes = requireNonNegativeInt(numFlashes);

        heartbeat = new Pulse(10, Pulse.State.OFF);
        currentBonusIndex = -1;
        huntingTimer.reset();
    }

    /**
     * @return the game (model) this level belongs to.
     */
    public GameModel game() {
        return game;
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
     * @param cutSceneNumber Sets the number of the cut scene played after this level (0=no cut scene).
     */
    public void setCutSceneNumber(int cutSceneNumber) {
        this.cutSceneNumber = (byte) cutSceneNumber;
    }

    /**
     * @return number of cut scene played after this level (0=no cut scene).
     */
    public int cutSceneNumber() {
        return cutSceneNumber;
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
    public HuntingTimer huntingTimer() {
        return huntingTimer;
    }

    /**
     * Makes Pac-Man and the ghosts invisible.
     */
    public void hidePacAndGhosts() {
        entities.pac().hide();
        entities.ghosts().forEach(Ghost::hide);
    }

    /**
     * @return the ghosts that have been killed using the power of the last energizer eaten.
     */
    public List<Ghost> killedGhostsForCurrentEnergizer() { return victims; }

    /**
     * @return {@code true} if this is a demo level (attract mode)
     */
    public boolean isDemoLevel() { return demoLevel; }

    /**
     * Makes this level into a demo level.
     * @param demoLevel {@code true} if this level should become a demo level
     */
    public void setDemoLevel(boolean demoLevel) { this.demoLevel = demoLevel; }

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
     * @param personality a valid ghost ID (e.g. {@link Globals#ORANGE_GHOST_POKEY})
     * @return the ghost with this ID
     */
    public Ghost ghost(byte personality) {
        return entities.ghosts().get(requireValidGhostPersonality(personality));
    }

    public Stream<Ghost> ghostsInAnyOfStates(Collection<GhostState> states) {
        requireNonNull(states);
        return entities.ghosts().stream().filter(ghost -> states.contains(ghost.state()));
    }

    public Stream<Ghost> ghostsInState(GhostState state) {
        requireNonNull(state);
        return entities.ghosts().stream().filter(ghost -> state.equals(ghost.state()));
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