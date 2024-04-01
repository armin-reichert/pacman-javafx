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
 * Stores events during a single hunting step.
 *
 * @author Armin Reichert
 */
public class HuntingStepEventLog {
    public Vector2i foundFoodAtTile = null;
    public boolean energizerFound = false;
    public byte bonusIndex = -1;
    public boolean bonusEaten = false;
    public boolean pacGetsPower = false;
    public boolean pacStartsLosingPower = false;
    public boolean pacLostPower = false;
    public boolean pacDied = false;
    public Ghost unlockedGhost;
    public final List<Ghost> killedGhosts = new ArrayList<>(4);

    public void report() {
        List<String> news = new ArrayList<>();
        if (energizerFound) {
            news.add("Energizer found at " + foundFoodAtTile);
        }
        if (bonusIndex != -1) {
            news.add("Bonus reached, index=" + bonusIndex);
        }
        if (bonusEaten) {
            news.add("Bonus eaten");
        }
        if (pacGetsPower) {
            news.add("Pac gained power");
        }
        if (pacStartsLosingPower) {
            news.add("Pac starts losing power");
        }
        if (pacLostPower) {
            news.add("Pac lost power");
        }
        if (pacDied) {
            news.add("Pac died");
        }
        if (unlockedGhost != null) {
            news.add("Unlocked " + unlockedGhost.name());
        }
        if (!killedGhosts.isEmpty()) {
            news.add("Ghosts killed: " + killedGhosts.stream().map(Ghost::name).toList());
        }
        if (!news.isEmpty()) {
            Logger.info("Latest News:");
            for (var msg : news) {
                Logger.info("- " + msg);
            }
        }
    }
}
