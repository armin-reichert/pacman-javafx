/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.level;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.gameplay.ArcadeHouseGateKeeper;
import de.amr.pacmanfx.core.model.world.map.FoodState;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.rules.HuntingTimerStrategy;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.core.Validations.requireValidLevelNumber;
import static java.util.Objects.requireNonNull;

/**
 * A game level contains the world and the actors.
 */
public class GameLevel {

    private final int number; // 1=first level

    private final WorldMap worldMap;
    private final GameLevelEntitySet entities;
    private final Pulse heartbeat;
    private final List<Integer> bonusSymbolCodes = new ArrayList<>();

    private final HuntingTimerStrategy huntingTimerStrategy;
    private final ArcadeHouseGateKeeper gateKeeper;

    private byte currentBonusIndex; // -1=no bonus, 0=first, 1=second
    private int ghostKillCount;

    private final FoodState foodState;

    public GameLevel(int number, WorldMap worldMap, GameLevelEntitySet entities, HuntingTimerStrategy huntingTimerStrategy) {
        this.number = requireValidLevelNumber(number);
        this.worldMap = requireNonNull(worldMap);
        this.entities = requireNonNull(entities);
        this.huntingTimerStrategy = requireNonNull(huntingTimerStrategy);
        this.gateKeeper = new ArcadeHouseGateKeeper(number);
        this.foodState = new FoodState(worldMap.foodLayer());
        this.heartbeat = new Pulse(10, Pulse.State.OFF);
        this.currentBonusIndex = -1;

        huntingTimerStrategy.reset();
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

    public FoodState food() {
        return foodState;
    }

    /**
     * @return the timer controlling the hunting phases (scattering and chasing).
     */
    public HuntingTimerStrategy huntingTimerStrategy() {
        return huntingTimerStrategy;
    }

    public ArcadeHouseGateKeeper gateKeeper() {
        return gateKeeper;
    }

    public int ghostKillCount() {
        return ghostKillCount;
    }

    public void setGhostKillCount(int ghostKillCount) {
        this.ghostKillCount = ghostKillCount;
    }

    public GameLevelEntitySet entities() {
        return entities;
    }

    public void clearBonusIndex() {
        currentBonusIndex = -1;
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

    public void setBonusSymbolCodes(List<Integer> codes) {
        requireNonNull(codes);
        bonusSymbolCodes.clear();
        bonusSymbolCodes.addAll(codes);
    }

    /**
     * @param i the bonus index (0 for the first bonus spawned in the level, ...)
     * @return the bonus symbol code of the bonus with the given index
     */
    public int bonusSymbolCode(int i) {
        if (0 <= i && i < bonusSymbolCodes.size()) {
            return bonusSymbolCodes.get(i);
        }
        throw new IndexOutOfBoundsException("Bonus index %d not in range 0..%d"
            .formatted(i, bonusSymbolCodes.size() - 1));
    }
}