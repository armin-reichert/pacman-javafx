/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.Pulse;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.world.WorldMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Validations.*;
import static java.util.Objects.requireNonNull;

/**
 * A game level contains the world and the actors.
 */
public class GameLevel {

    private final Game game;
    private final int number; // 1=first level
    private final WorldMap worldMap;
    private final AbstractHuntingTimer huntingTimer;
    private final Pulse blinking;
    private final List<Ghost> victims = new ArrayList<>();
    private final byte[] bonusSymbols = new byte[2];
    private final int numFlashes;

    private Pac pac;
    private Ghost[] ghosts;
    private Bonus bonus;
    private byte currentBonusIndex; // -1=no bonus, 0=first, 1=second
    private GameLevelMessage message;

    private boolean demoLevel;
    private int ghostKillCount;
    private int gameOverStateTicks;
    private long startTimeMillis;
    private float pacPowerSeconds;
    private float pacPowerFadingSeconds;

    private byte cutSceneNumber;

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
    public byte cutSceneNumber() {
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
     * Resets Pac-Man and the ghosts and places them at their start positions in their start states. Pac-Man initially
     * wants to move to the left.
     */
    public void getReadyToPlay() {
        pac.reset(); // initially invisible!
        pac.setPosition(worldMap.terrainLayer().pacStartPosition());
        pac.setMoveDir(Direction.LEFT);
        pac.setWishDir(Direction.LEFT);
        pac.powerTimer().resetIndefiniteTime();
        ghosts().forEach(ghost -> {
            ghost.reset(); // initially invisible!
            ghost.setPosition(ghost.startPosition());
            final Direction startDir = worldMap.terrainLayer().house().ghostStartDirection(ghost.personality());
            ghost.setMoveDir(startDir);
            ghost.setWishDir(startDir);
            ghost.setState(GhostState.LOCKED);
            ghost.optAnimationManager().ifPresent(AnimationManager::stop);
        });
        blinking.setStartState(Pulse.State.ON); // Energizers are visible when ON
        blinking.reset();
    }

    /**
     * Makes Pac-Man and the ghosts visible.
     */
    public void showPacAndGhosts() {
        pac.show();
        ghosts().forEach(Ghost::show);
    }

    /**
     * Makes Pac-Man and the ghosts invisible.
     */
    public void hidePacAndGhosts() {
        pac.hide();
        ghosts().forEach(Ghost::hide);
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

    /**
     * Sets the Pac-Man used in this level.
     * @param pac Pac-Man or Ms. Pac-Man
     */
    public void setPac(Pac pac) { this.pac = pac; }

    /**
     * @return  the Pac-Man used in this level.
     */
    public Pac pac() { return pac; }

    /**
     * Sets the ghosts used in this level.
     * @param redGhost Blinky, the red ghost
     * @param pinkGhost Pinky, the pink ghost
     * @param cyanGhost Inky, the cayn ghost
     * @param orangeGhost Clyde/Sue, the orange ghost
     */
    public void setGhosts(Ghost redGhost, Ghost pinkGhost, Ghost cyanGhost, Ghost orangeGhost) {
        ghosts = new Ghost[] {
            requireNonNull(redGhost),
            requireNonNull(pinkGhost),
            requireNonNull(cyanGhost),
            requireNonNull(orangeGhost)
        };
    }

    /**
     * @param id a valid ghost ID (e.g. {@link de.amr.pacmanfx.Globals#ORANGE_GHOST_POKEY})
     * @return the ghost with this ID
     */
    public Ghost ghost(byte id) {
        return ghosts[requireValidGhostPersonality(id)];
    }

    /**
     * @param states a list of ghost states
     * @return if no states are specified, all ghosts are returned. Otherwise, all ghosts that have any of the specified
     * states are returned.
     */
    public Stream<Ghost> ghosts(GhostState... states) {
        requireNonNull(states);
        requireNonNull(ghosts);
        return states.length == 0 ? Stream.of(ghosts) : Stream.of(ghosts).filter(ghost -> ghost.inAnyOfStates(states));
    }

    /**
     * Increases the number of ghosts killed during this level.
     */
    public void incrementGhostKillCount() { ghostKillCount++; }

    /**
     * @return the number of ghosts killed during this level
     */
    public int ghostKillCount() { return ghostKillCount; }

    // Bonus

    /**
     * @return (Optional) bonus actor used in this level
     */
    public Optional<Bonus> optBonus() {
        return Optional.ofNullable(bonus);
    }

    /**
     * Sets the bonus actor used in this level. This happens when the bonus gets activated.
     * @param bonus the bonus
     */
    public void setBonus(Bonus bonus) {
        this.bonus = bonus;
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
        currentBonusIndex += 1;
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