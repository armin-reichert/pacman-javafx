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
    public boolean pacGetsPower = false;
    public boolean pacStartsLosingPower = false;
    public boolean pacLostPower = false;
    public byte bonusReachedIndex = -1; // 0=first, 1=second, -1=no bonus
    public final List<Ghost> killedGhosts = new ArrayList<>(4);

    public void report() {
        List<String> report = new ArrayList<>();
        if (energizerFound) {
            report.add("- Energizer found at " + foodFoundAt);
        }
        if (pacGetsPower) {
            report.add("- Pac gained power");
        }
        if (pacStartsLosingPower) {
            report.add("- Pac starts losing power");
        }
        if (pacLostPower) {
            report.add("- Pac lost power");
        }
        if (bonusReachedIndex != -1) {
            report.add("- Bonus reached: " + bonusReachedIndex);
        }
        if (!killedGhosts.isEmpty()) {
            report.add("- Ghosts killed: " + killedGhosts.stream().map(Ghost::name).toList());
        }
        if (!report.isEmpty()) {
            Logger.info("Notable events in last frame:");
            for (var msg : report) {
                Logger.info(msg);
            }
        }
    }
}