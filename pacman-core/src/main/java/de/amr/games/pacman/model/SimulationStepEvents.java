/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.actors.Ghost;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores important events that happened during a single simulation step.
 *
 * @author Armin Reichert
 */
public class SimulationStepEvents {
    public final long tick;
    public Vector2i   foodFoundTile = null;
    public Vector2i   foundEnergizerTile = null;
    public int        bonusIndex = -1;
    public Vector2i   bonusEatenTile = null;
    public boolean    pacGotPower = false;
    public boolean    pacStartsLosingPower = false;
    public boolean    pacLostPower = false;
    public Vector2i   pacKilledTile = null;
    public boolean    extraLifeWon = false;
    public int        extraLifeScore = 0;
    public Ghost      releasedGhost = null;
    public String     ghostReleaseInfo = null;
    public final List<Ghost> killedGhosts = new ArrayList<>(4);

    public SimulationStepEvents(long tick) {
        this.tick = tick;
    }

    public List<String> createMessageList() {
        var messages = new ArrayList<String>();
        if (foundEnergizerTile != null) {
            messages.add("Energizer found at " + foundEnergizerTile);
        }
        if (bonusIndex != -1) {
            messages.add("Bonus reached, index=" + bonusIndex);
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
        if (pacKilledTile != null) {
            messages.add("Pac died at " + pacKilledTile);
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

    public void print() {
        var messageList = createMessageList();
        if (!messageList.isEmpty()) {
            Logger.info("Simulation step #{}:", tick);
            for (var msg : messageList) {
                Logger.info("- " + msg);
            }
        }
    }
}