package de.amr.games.pacman.model;

import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;

import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

public class GameLevel {

    public static byte checkGhostID(byte id) {
        if (id < 0 || id > 3) {
            throw GameException.illegalGhostID(id);
        }
        return id;
    }

    public int number; // 1=first level
    public boolean demoLevel;
    public long startTime;
    public int killedGhostCount;
    public GameWorld world;
    public Pac pac;
    public Ghost[] ghosts;
    public Bonus bonus;
    public byte nextBonusIndex; // -1=no bonus, 0=first, 1=second

    public Pac pac() { return pac; }

    public Ghost ghost(byte id) {
        checkGhostID(id);
        return ghosts != null ? ghosts[id] : null;
    }

    public Stream<Ghost> ghosts(GhostState... states) {
        checkNotNull(states);
        if (ghosts == null) {
            return Stream.empty();
        }
        return states.length == 0 ? Stream.of(ghosts) : Stream.of(ghosts).filter(ghost -> ghost.inState(states));
    }

    public Optional<Bonus> bonus() {
        return Optional.ofNullable(bonus);
    }

}

