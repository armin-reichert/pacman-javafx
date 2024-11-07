package de.amr.games.pacman.model;

import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;

import java.util.ArrayList;
import java.util.List;
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

    public final int number; // 1=first level
    private boolean demoLevel;
    private long startTime;
    private int killedGhostCount;
    private GameWorld world;
    private Pac pac;
    private Ghost[] ghosts;
    private final List<Ghost> victims = new ArrayList<>();
    public Bonus bonus;
    public final byte[] bonusSymbols = new byte[2];
    public byte nextBonusIndex; // -1=no bonus, 0=first, 1=second
    public MapConfig currentMapConfig;

    public GameLevel(int number) {
        this.number = number;
        nextBonusIndex = -1;
    }

    public void setDemoLevel(boolean demoLevel) {
        this.demoLevel = demoLevel;
    }

    public boolean isDemoLevel() {
        return demoLevel;
    }

    public void resetKiledGhostCount() {
        killedGhostCount = 0;
    }

    public void addKilledGhost() {
        killedGhostCount += 1;
    }

    public int killedGhostCount() {
        return killedGhostCount;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long startTime() {
        return startTime;
    }

    public void setWorld(GameWorld world) {
        this.world = world;
    }

    public GameWorld world() {
        return world;
    }

    public void setPac(Pac pac) {
        this.pac = pac;
    }

    public Pac pac() { return pac; }

    public Ghost ghost(byte id) {
        checkGhostID(id);
        return ghosts != null ? ghosts[id] : null;
    }

    public void setGhosts(Ghost[] ghosts) {
        this.ghosts = ghosts;
    }

    public Stream<Ghost> ghosts(GhostState... states) {
        checkNotNull(states);
        if (ghosts == null) {
            return Stream.empty();
        }
        return states.length == 0 ? Stream.of(ghosts) : Stream.of(ghosts).filter(ghost -> ghost.inState(states));
    }

    public List<Ghost> victims() {
        return victims;
    }

    public Optional<Bonus> bonus() {
        return Optional.ofNullable(bonus);
    }

    public MapConfig currentMapConfig() {
        return currentMapConfig;
    }
}