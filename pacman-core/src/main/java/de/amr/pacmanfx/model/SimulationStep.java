/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.actors.Ghost;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores what happened during a simulation step.
 *
 * @author Armin Reichert
 */
public class SimulationStep {
    public long       tick;
    public Vector2i   foundEnergizerAtTile;
    public int        bonusIndex = -1;
    public Vector2i   bonusEatenTile;
    public boolean    pacGotPower;
    public boolean    pacStartsLosingPower;
    public boolean    pacLostPower;
    public Vector2i   pacKilledTile;
    public Ghost      pacKiller;
    public boolean    extraLifeWon;
    public int        extraLifeScore;
    public Ghost      releasedGhost;
    public String     ghostReleaseInfo;
    public final List<Ghost> killedGhosts = new ArrayList<>();

    public void start(long tick) {
        this.tick = tick;
        foundEnergizerAtTile = null;
        bonusIndex = -1;
        bonusEatenTile = null;
        pacGotPower = false;
        pacStartsLosingPower = false;
        pacLostPower = false;
        pacKilledTile = null;
        pacKiller = null;
        extraLifeWon = false;
        extraLifeScore = 0;
        releasedGhost = null;
        ghostReleaseInfo = null;
        killedGhosts.clear();
    }

    public List<String> createReport() {
        var messages = new ArrayList<String>();
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
            messages.add("Pac killed by %s at %s".formatted(pacKiller.name(), pacKilledTile));
        }
        if (extraLifeWon) {
            messages.add("Extra life won for scoring %d points".formatted(extraLifeScore));
        }
        if (releasedGhost != null) {
            messages.add("%s unlocked: %s".formatted(releasedGhost.name(), ghostReleaseInfo));
        }
        if (!killedGhosts.isEmpty()) {
            for (Ghost ghost : killedGhosts) {
                messages.add("%s killed at %s".formatted(ghost.name(), ghost.tile()));
            }
        }
        return messages;
    }

    public void logState() {
        var report = createReport();
        if (!report.isEmpty()) {
            Logger.info("Simulation step #{}:", tick);
            for (var msg : report) {
                Logger.info("- " + msg);
            }
        }
    }
}