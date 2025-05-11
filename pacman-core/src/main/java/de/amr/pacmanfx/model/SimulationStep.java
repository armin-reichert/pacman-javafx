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
    private long       tick;
    private Vector2i   foundEnergizerAtTile;
    private int        bonusIndex = -1;
    private Vector2i   bonusEatenTile;
    private boolean    pacGotPower;
    private boolean    pacStartsLosingPower;
    private boolean    pacLostPower;
    private Vector2i   pacKilledTile;
    private Ghost pacKiller;
    private boolean    extraLifeWon;
    private int        extraLifeScore;
    private Ghost      releasedGhost;
    private String     ghostReleaseInfo;
    private final List<Ghost> killedGhosts = new ArrayList<>();

    public void init(long tick) {
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

    public void setFoundEnergizerAtTile(Vector2i tile) {
        this.foundEnergizerAtTile = tile;
    }

    public void setBonusIndex(int bonusIndex) {
        this.bonusIndex = bonusIndex;
    }

    public void setBonusEatenTile(Vector2i tile) {
        this.bonusEatenTile = tile;
    }

    public void setPacGotPower() {
        this.pacGotPower = true;
    }

    public void setPacStartsLosingPower() {
        this.pacStartsLosingPower = true;
    }

    public void setPacLostPower() {
        this.pacLostPower = true;
    }

    public void setPacKilledTile(Vector2i tile) {
        this.pacKilledTile = tile;
    }

    public void setPacKiller(Ghost pacKiller) {
        this.pacKiller = pacKiller;
    }

    public void setExtraLifeWon() {
        this.extraLifeWon = true;
    }

    public void setExtraLifeScore(int extraLifeScore) {
        this.extraLifeScore = extraLifeScore;
    }

    public void setReleasedGhost(Ghost releasedGhost) {
        this.releasedGhost = releasedGhost;
    }

    public void setGhostReleaseInfo(String ghostReleaseInfo) {
        this.ghostReleaseInfo = ghostReleaseInfo;
    }

    public Vector2i pacKilledTile() {
        return pacKilledTile;
    }

    public List<Ghost> killedGhosts() {
        return killedGhosts;
    }

    public List<String> createMessageList() {
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

    public void log() {
        var messageList = createMessageList();
        if (!messageList.isEmpty()) {
            Logger.info("Simulation step #{}:", tick);
            for (var msg : messageList) {
                Logger.info("- " + msg);
            }
        }
    }
}