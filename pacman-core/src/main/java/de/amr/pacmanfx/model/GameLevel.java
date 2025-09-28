/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.actors.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.Validations.requireValidLevelNumber;
import static java.util.Objects.requireNonNull;

/**
 * A game level contains the world, the actors and the food management.
 */
public class GameLevel {

    //TODO should this be stored in world map instead of hardcoding?
    public static final int EMPTY_ROWS_OVER_MAZE  = 3;
    public static final int EMPTY_ROWS_BELOW_MAZE = 2;

    private final Game game;
    private final int number; // 1=first level

    private final WorldMap worldMap;

    private boolean demoLevel;

    private Pac pac;
    private Ghost[] ghosts;
    private final List<Ghost> victims = new ArrayList<>();
    private Bonus bonus;
    private final byte[] bonusSymbols = new byte[2];
    private int currentBonusIndex; // -1=no bonus, 0=first, 1=second

    private GameLevelMessage message;

    private final Pulse blinking;

    private int numGhostsKilled;
    private int gameOverStateTicks;
    private long startTime;

    public GameLevel(Game game, int number, WorldMap worldMap, House house) {
        this.game = requireNonNull(game);
        this.number = requireValidLevelNumber(number);
        this.worldMap = requireNonNull(worldMap);

        worldMap.terrainLayer().setHouse(requireNonNull(house));
        //TODO check if this is still needed:
        worldMap.setContent(LayerID.TERRAIN, house.minTile(), house.content());

        blinking = new Pulse(10, Pulse.OFF);
        currentBonusIndex = -1;
    }

    public void getReadyToPlay() {
        pac.reset(); // initially invisible!
        pac.setPosition(worldMap.terrainLayer().pacStartPosition());
        pac.setMoveDir(Direction.LEFT);
        pac.setWishDir(Direction.LEFT);
        pac.powerTimer().resetIndefiniteTime();
        ghosts().forEach(ghost -> {
            ghost.reset(); // initially invisible!
            ghost.setPosition(ghost.startPosition());
            ghost.setMoveDir(worldMap.terrainLayer().house().ghostStartDirection(ghost.personality()));
            ghost.setWishDir(worldMap.terrainLayer().house().ghostStartDirection(ghost.personality()));
            ghost.setState(GhostState.LOCKED);
        });
        blinking.setStartPhase(Pulse.ON); // Energizers are visible when ON
        blinking.reset();
    }

    public void showPacAndGhosts() {
        pac.show();
        ghosts().forEach(Ghost::show);
    }

    public void hidePacAndGhosts() {
        pac.hide();
        ghosts().forEach(Ghost::hide);
    }

    public Game game() {
        return game;
    }

    public List<Ghost> victims() { return victims; }

    public boolean isDemoLevel() { return demoLevel; }
    public void setDemoLevel(boolean demoLevel) { this.demoLevel = demoLevel; }

    public void setStartTime(long startTime) { this.startTime = startTime; }
    public long startTime() { return startTime; }

    public void setGameOverStateTicks(int ticks) { gameOverStateTicks = ticks; }
    public int gameOverStateTicks() { return gameOverStateTicks; }

    public void setMessage(GameLevelMessage message) {
        this.message = message;
    }

    public void clearMessage() {
        message = null;
    }

    public Optional<GameLevelMessage> optMessage() {
        return Optional.ofNullable(message);
    }

    public void setPac(Pac pac) { this.pac = pac; }

    public Pac pac() { return pac; }

    public void setGhosts(Ghost redGhost, Ghost pinkGhost, Ghost cyanGhost, Ghost orangeGhost) {
        ghosts = new Ghost[] {
            requireNonNull(redGhost), requireNonNull(pinkGhost), requireNonNull(cyanGhost), requireNonNull(orangeGhost)
        };
    }

    public Ghost ghost(byte id) {
        return ghosts[requireValidGhostPersonality(id)];
    }

    public Stream<Ghost> ghosts(GhostState... states) {
        requireNonNull(states);
        requireNonNull(ghosts);
        return states.length == 0 ? Stream.of(ghosts) : Stream.of(ghosts).filter(ghost -> ghost.inAnyOfStates(states));
    }

    public void registerGhostKilled() { numGhostsKilled++; }
    public int numGhostsKilled() { return numGhostsKilled; }

    public Optional<Bonus> bonus() { return Optional.ofNullable(bonus); }
    public void setBonus(Bonus bonus) { this.bonus = bonus; }
    public boolean isBonusEdible() { return bonus != null && bonus.state() == BonusState.EDIBLE; }
    public int currentBonusIndex() { return currentBonusIndex; }
    public void selectNextBonus() { currentBonusIndex += 1; }
    public byte bonusSymbol(int i) { return bonusSymbols[i]; }
    public void setBonusSymbol(int i, byte symbol) {
        bonusSymbols[i] = symbol;
    }

    public Pulse blinking() {
        return blinking;
    }

    public int number() { return number; }

    public WorldMap worldMap() { return worldMap; }
}