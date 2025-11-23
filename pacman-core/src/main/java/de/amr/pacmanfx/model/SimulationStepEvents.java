/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.actors.Ghost;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores what happened during the current simulation step.
 */
public class SimulationStepEvents {
    public long       tick;
    public Vector2i   foundEnergizerAtTile;
    public int        bonusIndex = -1;
    public Vector2i   bonusEatenTile;
    public boolean    pacGotPower;
    public boolean    pacStartsLosingPower;
    public boolean    pacLostPower;
    public Ghost      pacKiller;
    public boolean    extraLifeWon;
    public int        extraLifeScore;
    public Ghost      releasedGhost;
    public String     ghostReleaseInfo;
    public final List<Ghost> killedGhosts = new ArrayList<>();
    public final List<Ghost> ghostsCollidingWithPac = new ArrayList<>();

    public void reset() {
        foundEnergizerAtTile = null;
        bonusIndex = -1;
        bonusEatenTile = null;
        pacGotPower = false;
        pacStartsLosingPower = false;
        pacLostPower = false;
        pacKiller = null;
        extraLifeWon = false;
        extraLifeScore = 0;
        releasedGhost = null;
        ghostReleaseInfo = null;
        killedGhosts.clear();
        ghostsCollidingWithPac.clear();
    }

    public void setTick(long tick) {
        this.tick = tick;
    }

    public List<String> createReport() {
        var messages = new ArrayList<String>();
        for (Ghost ghost : ghostsCollidingWithPac) {
            messages.add("%s collided with Pac at tile %s, state after collision: %s".formatted(ghost.name(), ghost.tile(), ghost.state()));
        }
        if (foundEnergizerAtTile != null) {
            messages.add("Energizer found at " + foundEnergizerAtTile);
        }
        if (bonusIndex != -1) {
            messages.add("Bonus score reached, index=" + bonusIndex);
        }
        if (bonusEatenTile != null) {
            messages.add("Bonus eaten at " + bonusEatenTile);
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
        if (releasedGhost != null) {
            messages.add("%s unlocked: %s".formatted(releasedGhost.name(), ghostReleaseInfo));
        }
        for (Ghost ghost : killedGhosts) {
            messages.add("%s killed at %s".formatted(ghost.name(), ghost.tile()));
        }
        return messages;
    }

    public void printLog() {
        var report = createReport();
        if (!report.isEmpty()) {
            Logger.info("Step #{} of simulation:", tick);
            for (var msg : report) {
                Logger.info("- " + msg);
            }
        }
    }
}