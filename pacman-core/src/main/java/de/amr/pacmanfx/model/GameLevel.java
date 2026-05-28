/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.WorldMap;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Validations.*;
import static java.util.Objects.requireNonNull;

/**
 * A game level contains the world and the actors.
 */
public class GameLevel {

    public static class EntitySet implements Iterable<GameLevelEntity> {

        private final GameLevelEntitySet entitySet = new GameLevelEntitySet();

        public void add(GameLevelEntity entity) {
            entitySet.add(entity);
        }

        public void remove(GameLevelEntity entity) {
            entitySet.remove(entity);
        }

        public Pac pac() {
            return entitySet.uniqueOfType(Pac.class);
        }

        public List<Ghost> ghosts() {
            return entitySet.selectAllOfType(Ghost.class).sorted(Comparator.comparing(Ghost::personality)).toList();
        }

        public Optional<Bonus> optBonus() {
            return entitySet.anyOfType(Bonus.class);
        }

        @Override
        public Iterator<GameLevelEntity> iterator() {
            return entitySet.iterator();
        }
    }

    private final Game game;
    private final int number; // 1=first level
    private final WorldMap worldMap;
    private final EntitySet entities = new EntitySet();
    private final AbstractHuntingTimer huntingTimer;
    private final Pulse blinking;
    private final List<Ghost> victims = new ArrayList<>();
    private final byte[] bonusSymbols = new byte[2];
    private final int numFlashes;

    private byte currentBonusIndex; // -1=no bonus, 0=first, 1=second
    private GameLevelMessage message;

    private boolean demoLevel;
    private int gameOverStateTicks;
    private long startTimeMillis;
    private float pacPowerSeconds;
    private float pacPowerFadingSeconds;

    private int cutSceneNumber;

    public GameLevel(Game game, int number, WorldMap worldMap, AbstractHuntingTimer huntingTimer, int numFlashes) {
        this.game = requireNonNull(game);
        this.number = requireValidLevelNumber(number);
        this.worldMap = requireNonNull(worldMap);
        this.huntingTimer = requireNonNull(huntingTimer);
        this.numFlashes = requireNonNegativeInt(numFlashes);

        blinking = new Pulse(10, Pulse.State.OFF);
        currentBonusIndex = -1;
        huntingTimer.reset();
    }

    /**
     * @return the game (model) this level belongs to.
     */
    public Game game() {
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
     * @return the blinking animation used for the energizers.
     */
    public Pulse blinking() {
        return blinking;
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
    public AbstractHuntingTimer huntingTimer() {
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
    public List<Ghost> energizerVictims() { return victims; }

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

    public EntitySet entities() {
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
     * @param personality a valid ghost ID (e.g. {@link de.amr.pacmanfx.Globals#ORANGE_GHOST_POKEY})
     * @return the ghost with this ID
     */
    public Ghost ghost(byte personality) {
        return entities.ghosts().get(requireValidGhostPersonality(personality));
    }

    /**
     * @param states a list of ghost states
     * @return if no states are specified, all ghosts are returned. Otherwise, all ghosts that have any of the specified
     * states are returned.
     */
    public Stream<Ghost> ghosts(GhostState... states) {
        requireNonNull(states);
        return states.length == 0
            ? entities.ghosts().stream()
            : entities.ghosts().stream().filter(ghost -> ghost.inAnyOfStates(states));
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
     * @param i the bonus index
     * @return the bonus symbol code of the bonus with the given index
     */
    public byte bonusSymbol(int i) {
        return bonusSymbols[i];
    }

    /**
     * @param i the bonus index
     * @param symbol the bonus symbol code
     */
    public void setBonusSymbol(int i, byte symbol) {
        if (0 <= i && i < bonusSymbols.length) {
            bonusSymbols[i] = symbol;
        } else {
            throw new IllegalArgumentException("Cannot set bonus symbol at index " + i);
        }
    }
}