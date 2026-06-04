/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.simulation;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HuntingStepResult {
    private Vector2i foodFoundTile;
    private boolean energizerFound;
    private int bonusIndex;
    private Bonus edibleBonus;
    private boolean pacGotPower;
    private boolean pacStartsLosingPower;
    private boolean pacLostPower;
    private Ghost pacKiller;
    private final List<Ghost> ghostsKilled = new ArrayList<>();
    private final Set<Ghost> ghostsCollidingWithPac = new HashSet<>(4);

    public HuntingStepResult() {
        bonusIndex = -1;
    }

    public List<String> createReport() {
        var messages = new ArrayList<String>();
        for (Ghost ghost : ghostsCollidingWithPac) {
            messages.add("%s collided with Pac at tile %s, state after collision: %s".formatted(ghost.name(), ghost.computeTile(), ghost.state()));
        }
        if (energizerFound) {
            messages.add("Energizer found at " + foodFoundTile);
        }
        if (bonusIndex != -1) {
            messages.add("Bonus score reached, index=" + bonusIndex);
        }
        if (edibleBonus != null) {
            messages.add("Bonus eaten: %s".formatted(edibleBonus));
        }
        if (pacGotPower) {
            messages.add("Pac gained power");
        }
        if (pacStartsLosingPower) {
            messages.add("Pac starts losing power");
        }
        if (pacLostPower) {
            messages.add("Pac lost power");
        }
        if (pacKiller != null) {
            messages.add("Pac killed by %s at tile %s".formatted(pacKiller.name(), pacKiller.computeTile()));
        }
        for (Ghost ghost : ghostsKilled) {
            messages.add("%s killed at %s".formatted(ghost.name(), ghost.computeTile()));
        }
        return messages;
    }

    public void printLog() {
        var report = createReport();
        if (!report.isEmpty()) {
            Logger.info("Step:");
            for (var msg : report) {
                Logger.info("- " + msg);
            }
        }
    }

    public Vector2i foodFoundTile() {
        return foodFoundTile;
    }

    public void setFoodFoundTile(Vector2i foodFoundTile) {
        this.foodFoundTile = foodFoundTile;
    }

    public boolean foodFound() {
        return foodFoundTile != null;
    }

    public boolean energizerFound() {
        return energizerFound;
    }

    public void setEnergizerFound(boolean energizerFound) {
        this.energizerFound = energizerFound;
    }

    public Set<Ghost> ghostsCollidingWithPac() {
        return ghostsCollidingWithPac;
    }

    public void setPacKiller(Ghost pacKiller) {
        this.pacKiller = pacKiller;
    }

    public boolean detectedPacGhostCollision() {
        return !ghostsCollidingWithPac.isEmpty();
    }

    public boolean hasPacManBeenKilled() {
        return pacKiller != null;
    }

    public List<Ghost> ghostsKilled() {
        return ghostsKilled;
    }

    public boolean hasGhostBeenKilled() {
        return !ghostsKilled.isEmpty();
    }

    public void setEdibleBonus(Bonus edibleBonus) {
        this.edibleBonus = edibleBonus;
    }

    public Bonus edibleBonus() {
        return edibleBonus;
    }

    public boolean foundEdibleBonus() {
        return edibleBonus != null;
    }

    public void setBonusIndex(int bonusIndex) {
        this.bonusIndex = bonusIndex;
    }

    public void setPacGotPower(boolean pacGotPower) {
        this.pacGotPower = pacGotPower;
    }

    public void setPacStartsLosingPower(boolean pacStartsLosingPower) {
        this.pacStartsLosingPower = pacStartsLosingPower;
    }

    public void setPacLostPower(boolean pacLostPower) {
        this.pacLostPower = pacLostPower;
    }

    public boolean pacLostPower() {
        return pacLostPower;
    }
}
