/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stores what happened during the current simulation step.
 */
public class SimulationStep {
    public long              tick;
    public Vector2i          foodTile;
    public boolean           energizerFound;
    public int               bonusIndex = -1;
    public Bonus             edibleBonus;
    public boolean           pacGotPower;
    public boolean           pacStartsLosingPower;
    public boolean           pacLostPower;
    public Ghost             pacKiller;
    public boolean           extraLifeWon;
    public int               extraLifeScore;
    public Ghost             ghostReleasedFromJailhouse;
    public String            ghostReleaseInfo;
    public final List<Ghost> ghostsKilled = new ArrayList<>();
    public final Set<Ghost>  ghostsCollidingWithPac = new HashSet<>(4);

    public void init(long tick) {
        this.tick = tick;
        foodTile = null;
        energizerFound = false;
        bonusIndex = -1;
        edibleBonus = null;
        pacGotPower = false;
        pacStartsLosingPower = false;
        pacLostPower = false;
        pacKiller = null;
        extraLifeWon = false;
        extraLifeScore = 0;
        ghostReleasedFromJailhouse = null;
        ghostReleaseInfo = null;
        ghostsKilled.clear();
        ghostsCollidingWithPac.clear();
    }

    public List<String> createReport() {
        var messages = new ArrayList<String>();
        for (Ghost ghost : ghostsCollidingWithPac) {
            messages.add("%s collided with Pac at tile %s, state after collision: %s".formatted(ghost.name(), ghost.tile(), ghost.state()));
        }
        if (energizerFound) {
            messages.add("Energizer found at " + foodTile);
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
            messages.add("Pac killed by %s at tile %s".formatted(pacKiller.name(), pacKiller.tile()));
        }
        if (extraLifeWon) {
            messages.add("Extra life won for scoring %d points".formatted(extraLifeScore));
        }
        if (ghostReleasedFromJailhouse != null) {
            messages.add("%s unlocked: %s".formatted(ghostReleasedFromJailhouse.name(), ghostReleaseInfo));
        }
        for (Ghost ghost : ghostsKilled) {
            messages.add("%s killed at %s".formatted(ghost.name(), ghost.tile()));
        }
        return messages;
    }

    public void printLog() {
        var report = createReport();
        if (!report.isEmpty()) {
            Logger.info("Step #{}:", tick);
            for (var msg : report) {
                Logger.info("- " + msg);
            }
        }
    }
}