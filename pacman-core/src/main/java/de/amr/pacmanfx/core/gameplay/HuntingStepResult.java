/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.actors.Bonus;
import de.amr.pacmanfx.core.model.actors.Ghost;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class HuntingStepResult {

    private Vector2i foodFoundTile;
    private boolean energizerFound;
    private Bonus edibleBonus;
    private boolean pacKilled;
    private final List<Ghost> ghostsKilled = new ArrayList<>();
    private final Set<Ghost> ghostsCollidingWithPac = new HashSet<>(4);

    public HuntingStepResult() {}

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

    public boolean pacKilled() {
        return pacKilled;
    }

    public void setPacKilled(boolean pacKilled) {
        this.pacKilled = pacKilled;
    }

    public List<String> asText() {
        var lines = new ArrayList<String>();
        for (Ghost ghost : ghostsCollidingWithPac()) {
            lines.add("%s collided with Pac at tile %s, state after collision: %s".formatted(ghost.name(), ghost.computeTile(), ghost.state()));
        }
        if (energizerFound()) {
            lines.add("Energizer found at " + foodFoundTile());
        }
        if (edibleBonus() != null) {
            lines.add("Bonus eaten: " + edibleBonus());
        }
        for (Ghost ghost : ghostsKilled()) {
            lines.add("%s killed at %s".formatted(ghost.name(), ghost.computeTile()));
        }
        return lines;
    }
}
