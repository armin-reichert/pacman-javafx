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
 * @author Armin Reichert
 */
public class Memory {

    public Vector2i foodFoundAt = null;
    public boolean energizerFound = false;
    public byte bonusReachedIndex = -1; // 0=first, 1=second, -1=no bonus
    public boolean levelCompleted = false;
    public boolean pacKilled = false;
    public final List<Ghost> pacPrey = new ArrayList<>(4);
    public final List<Ghost> killedGhosts = new ArrayList<>(4);

    public void report() {
        List<String> report = new ArrayList<>();
        if (energizerFound) {
            report.add("- Energizer found at " + foodFoundAt);
        }
        if (bonusReachedIndex != -1) {
            report.add("- Bonus reached: " + bonusReachedIndex);
        }
        if (levelCompleted) {
            report.add("- Level completed");
        }
        if (pacKilled) {
            report.add("- Pac killed");
        }
        if (!pacPrey.isEmpty()) {
            report.add("- Pac prey: " + pacPrey);
        }
        if (!killedGhosts.isEmpty()) {
            report.add("- Ghosts killed: " + killedGhosts);
        }
        if (!report.isEmpty()) {
            Logger.info("What happened last frame:");
            Logger.info(String.join("\n", report));
        }
    }
}