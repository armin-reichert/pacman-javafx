/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.level;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.gameplay.ArcadeHouseGateKeeper;
import de.amr.pacmanfx.core.model.world.map.FoodState;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.rules.HuntingTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.core.Validations.requireValidLevelNumber;
import static java.util.Objects.requireNonNull;

public class GameLevel implements Renderable{

    private final int number; // 1=first level

    private WorldMap worldMap;
    private GameLevelEntities entities;
    private Pulse heartbeat;
    private List<Integer> bonusSymbolCodes;
    private HuntingTimer huntingTimer;
    private ArcadeHouseGateKeeper gateKeeper;
    private FoodState foodState;

    private byte currentBonusIndex = -1; // -1=no bonus, 0=first, 1=second
    private int ghostKillCount;

    public GameLevel(int number) {
        this.number = requireValidLevelNumber(number);
    }

    public GameLevel(int number, WorldMap worldMap, GameLevelEntities entities, HuntingTimer huntingTimer) {
        this.number = requireValidLevelNumber(number);
        this.worldMap = requireNonNull(worldMap);
        this.entities = requireNonNull(entities);
        this.huntingTimer = requireNonNull(huntingTimer);
        this.gateKeeper = new ArcadeHouseGateKeeper(number);
        this.foodState = new FoodState(worldMap.foodLayer());
        this.heartbeat = new Pulse(10, Pulse.State.OFF);
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.WORLD;
    }

    public Stream<Renderable> visibleRenderables() {
        return entities.all()
            .filter(GameEntity::isVisible)
            .filter(Renderable.class::isInstance).map(Renderable.class::cast);
    }

    /**
     * @return level number (starting with 1)
     */
    public int number() {
        return number;
    }

    public void setHeartbeat(Pulse heartbeat) {
        this.heartbeat = heartbeat;
    }

    /**
     * @return the pulse driving the blinking animation for the energizers.
     */
    public Pulse heartbeat() {
        return heartbeat;
    }

    public void setWorldMap(WorldMap worldMap) {
        this.worldMap = worldMap;
    }

    /**
     * @return the map used in this level.
     */
    public WorldMap worldMap() {
        return worldMap;
    }

    public void setFoodState(FoodState foodState) {
        this.foodState = foodState;
    }

    public FoodState food() {
        return foodState;
    }

    public void setHuntingTimer(HuntingTimer huntingTimer) {
        this.huntingTimer = huntingTimer;
    }

    /**
     * @return the timer controlling the hunting phases (scattering and chasing).
     */
    public HuntingTimer huntingTimer() {
        return huntingTimer;
    }

    public void setGateKeeper(ArcadeHouseGateKeeper gateKeeper) {
        this.gateKeeper = gateKeeper;
    }

    public ArcadeHouseGateKeeper gateKeeper() {
        return gateKeeper;
    }

    public void setGhostKillCount(int ghostKillCount) {
        this.ghostKillCount = ghostKillCount;
    }

    public int ghostKillCount() {
        return ghostKillCount;
    }

    public void setEntities(GameLevelEntities entities) {
        this.entities = entities;
    }

    public GameLevelEntities entities() {
        return entities;
    }

    public Stream<Renderable> renderableEntities() {
        return entities.all()
            .filter(Renderable.class::isInstance)
            .map(Renderable.class::cast);
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
        bonusSymbolCodes = new ArrayList<>(codes);
    }

    /**
     * @param i the bonus index (0 for the first bonus spawned in the level, ...)
     * @return the bonus symbol code of the bonus with the given index
     */
    public int bonusSymbolCode(int i) {
        requireNonNull(bonusSymbolCodes);
        if (0 <= i && i < bonusSymbolCodes.size()) {
            return bonusSymbolCodes.get(i);
        }
        throw new IndexOutOfBoundsException("Bonus index %d not in range 0..%d"
            .formatted(i, bonusSymbolCodes.size() - 1));
    }

    public void clearMessage() {
        requireNonNull(entities);
        entities.theMessageView().type().setMessageType(MessageType.NO_MESSAGE);
    }

    public void showMessage(MessageType messageType) {
        requireNonNull(entities);
        requireNonNull(messageType);

        entities.theMessageView().type().setMessageType(messageType);
        entities.theMessageView().show();
    }
}