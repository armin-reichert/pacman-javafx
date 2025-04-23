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
    public Vector2i foodFoundTile = null;
    public boolean  energizerFound = false;
    public int      bonusIndex = -1;
    public boolean  bonusEaten = false;
    public boolean pacGotPower = false;
    public boolean  pacStartsLosingPower = false;
    public boolean  pacLostPower = false;
    public boolean  pacKilled = false;
    public boolean  extraLifeWon = false;
    public int      extraLifeScore = 0;
    public Ghost    releasedGhost = null;
    public String  ghostReleaseInfo = null;
    public final List<Ghost> killedGhosts = new ArrayList<>(4);

    public SimulationStepEvents(long tick) {
        this.tick = tick;
    }

    public List<String> createMessageList() {
        List<String> messages = new ArrayList<>();
        if (energizerFound) {
            messages.add("Energizer found at " + foodFoundTile);
        }
        if (bonusIndex != -1) {
            messages.add("Bonus reached, index=" + bonusIndex);
        }
        if (bonusEaten) {
            messages.add("Bonus eaten");
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
        if (pacKilled) {
            messages.add("Pac died");
        }
        if (extraLifeWon) {
            messages.add("Extra life won for scoring %d points".formatted(extraLifeScore));
        }
        if (releasedGhost != null) {
            messages.add("Unlocked " + releasedGhost.name() + ": " + ghostReleaseInfo);
        }
        if (!killedGhosts.isEmpty()) {
            messages.add("Ghosts killed: " + killedGhosts.stream().map(Ghost::name).toList());
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
