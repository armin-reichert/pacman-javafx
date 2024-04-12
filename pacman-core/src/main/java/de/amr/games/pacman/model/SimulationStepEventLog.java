/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
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
public class SimulationStepEventLog {
    public Vector2i foodFoundTile = null;
    public boolean  energizerFound = false;
    public byte     bonusIndex = -1;
    public boolean  bonusEaten = false;
    public boolean  pacGetsPower = false;
    public boolean  pacStartsLosingPower = false;
    public boolean  pacLostPower = false;
    public boolean  pacDied = false;
    public Ghost    unlockedGhost = null;
    public String   unlockGhostReason = null;
    public final List<Ghost> killedGhosts = new ArrayList<>(4);

    public void report() {
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
        if (pacGetsPower) {
            messages.add("Pac gained power");
        }
        if (pacStartsLosingPower) {
            messages.add("Pac starts losing power");
        }
        if (pacLostPower) {
            messages.add("Pac lost power");
        }
        if (pacDied) {
            messages.add("Pac died");
        }
        if (unlockedGhost != null) {
            messages.add("Unlocked " + unlockedGhost.name() + ": " + unlockGhostReason);
        }
        if (!killedGhosts.isEmpty()) {
            messages.add("Ghosts killed: " + killedGhosts.stream().map(Ghost::name).toList());
        }
        if (!messages.isEmpty()) {
            Logger.info("During last step:");
            for (var msg : messages) {
                Logger.info("- " + msg);
            }
        }
    }
}
