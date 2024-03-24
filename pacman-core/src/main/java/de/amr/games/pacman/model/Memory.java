/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.actors.Ghost;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public class Memory {
    public Vector2i foodLocation;
    public boolean energizerFound;
    public byte bonusReachedIndex; // 0=first, 1=second, -1=no bonus
    public boolean levelCompleted;
    public boolean pacKilled;
    public boolean pacPowerStarts;
    public boolean pacPowerLost;
    public boolean pacPowerFading;
    public final List<Ghost> pacPrey = new ArrayList<>(4);
    public final List<Ghost> killedGhosts = new ArrayList<>(4);

    public Memory() {
        forgetEverything();
    }

    /**
     * Ich scholze jetzt.
     */
    public void forgetEverything() {
        foodLocation = null;
        energizerFound = false;
        bonusReachedIndex = -1;
        levelCompleted = false;
        pacKilled = false;
        pacPowerStarts = false;
        pacPowerLost = false;
        pacPowerFading = false;
        pacPrey.clear();
        killedGhosts.clear();
    }

    @Override
    public String toString() {
        String levelCompleted = this.levelCompleted ? "Level completed" : "";
        String food = foodLocation != null
            ? String.format("%s eaten at %s", energizerFound ? "Energizer" : "Pellet", foodLocation)
            : "";
        String bonus = bonusReachedIndex != -1
            ? String.format("Bonus %d reached", bonusReachedIndex)
            : "";
        var power = new StringBuilder();
        power.append(pacPowerStarts ? " starts" : "");
        power.append(pacPowerFading ? " fading" : "");
        power.append(pacPowerLost   ? " lost" : "");
        if (!power.isEmpty()) {
            power.insert(0, "Pac power:");
        }
        String killed = pacKilled ? "Pac killed" : "";
        String prey = pacPrey.isEmpty() ? "" : String.format("Prey: %s", pacPrey);
        String killedGhosts = this.killedGhosts.isEmpty() ? "" : this.killedGhosts.toString();

        String summary = Stream.of(levelCompleted, food, bonus, power.toString(), killed, prey, killedGhosts)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.joining(" "));
        return summary.isBlank() ? "" : String.format("[Last frame: %s]", summary);
    }
}