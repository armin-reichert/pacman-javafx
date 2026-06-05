/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.simulation;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class HuntingStepResult {

    private Vector2i foodFoundTile;
    private boolean energizerFound;
    private int bonusIndex;
    private Bonus edibleBonus;
    private boolean pacKilled;
    private boolean pacGotPower;
    private boolean pacStartsLosingPower;
    private boolean pacLostPower;
    private final List<Ghost> ghostsKilled = new ArrayList<>();
    private final Set<Ghost> ghostsCollidingWithPac = new HashSet<>(4);

    public HuntingStepResult() {
        bonusIndex = -1;
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

    public boolean detectedPacGhostCollision() {
        return !ghostsCollidingWithPac.isEmpty();
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

    public int bonusIndex() {
        return bonusIndex;
    }

    public void setBonusIndex(int bonusIndex) {
        this.bonusIndex = bonusIndex;
    }

    public boolean pacGotPower() {
        return pacGotPower;
    }

    public void setPacGotPower(boolean pacGotPower) {
        this.pacGotPower = pacGotPower;
    }

    public boolean pacStartsLosingPower() {
        return pacStartsLosingPower;
    }

    public boolean pacKilled() {
        return pacKilled;
    }

    public void setPacKilled(boolean pacKilled) {
        this.pacKilled = pacKilled;
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


    public static List<String> createReport(HuntingStepResult result) {
        var messages = new ArrayList<String>();
        for (Ghost ghost : result.ghostsCollidingWithPac()) {
            messages.add("%s collided with Pac at tile %s, state after collision: %s".formatted(ghost.name(), ghost.computeTile(), ghost.state()));
        }
        if (result.energizerFound()) {
            messages.add("Energizer found at " + result.foodFoundTile());
        }
        if (result.bonusIndex() != -1) {
            messages.add("Bonus score reached, index=" + result.bonusIndex());
        }
        if (result.edibleBonus() != null) {
            messages.add("Bonus eaten: %s".formatted(result.edibleBonus()));
        }
        if (result.pacGotPower()) {
            messages.add("Pac gained power");
        }
        if (result.pacStartsLosingPower()) {
            messages.add("Pac starts losing power");
        }
        if (result.pacLostPower()) {
            messages.add("Pac lost power");
        }
        for (Ghost ghost : result.ghostsKilled()) {
            messages.add("%s killed at %s".formatted(ghost.name(), ghost.computeTile()));
        }
        return messages;
    }
}
