/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.timer.Pulse;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.assertLegalGhostID;

public class GameLevel {

    public enum MessageType { READY, GAME_OVER, TEST_LEVEL }
    public record Message(MessageType type) {}

    public final int number; // 1=first level
    private long startTime;
    private int killedGhostCount;
    private int numFlashes;
    private int intermissionNumber;

    private GameWorld world;
    private Pac pac;
    private Ghost[] ghosts;
    private final List<Ghost> victims = new ArrayList<>();
    private Bonus bonus;
    private final byte[] bonusSymbols = new byte[2];
    private int nextBonusIndex; // -1=no bonus, 0=first, 1=second
    private Message message;

    private final Pulse blinking = new Pulse(10, Pulse.OFF);
    private final TickTimer powerTimer = new TickTimer("PacPowerTimer");

    public GameLevel(int number) {
        this.number = number;
        nextBonusIndex = -1;
    }

    public void addKilledGhost(Ghost victim) {
        killedGhostCount += 1;
        victims.add(victim);
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

    public void setNumFlashes(int numFlashes) {
        this.numFlashes = numFlashes;
    }

    public int numFlashes() {
        return numFlashes;
    }

    public void setIntermissionNumber(int number) {
        this.intermissionNumber = number;
    }

    public int intermissionNumber() {
        return intermissionNumber;
    }

    public void showReadyMessage() {
        message = new Message(MessageType.READY);
    }

    public void showGameOverMessage() {
        message = new Message(MessageType.GAME_OVER);
    }

    public void clearMessage() {
        message = null;
    }

    public Message message() {
        return message;
    }

    public void setPac(Pac pac) {
        this.pac = pac;
    }

    public Pac pac() { return pac; }

    public Ghost ghost(byte id) {
        assertLegalGhostID(id);
        return ghosts != null ? ghosts[id] : null;
    }

    public void setGhosts(Ghost[] ghosts) {
        this.ghosts = ghosts;
    }

    public Stream<Ghost> ghosts(GhostState... states) {
        Globals.assertNotNull(states);
        if (ghosts == null) {
            return Stream.empty();
        }
        return states.length == 0 ? Stream.of(ghosts) : Stream.of(ghosts).filter(ghost -> ghost.inState(states));
    }

    public List<Ghost> victims() {
        return victims;
    }

    public void setBonus(Bonus bonus) {
        this.bonus = bonus;
    }

    public Optional<Bonus> bonus() {
        return Optional.ofNullable(bonus);
    }

    public int nextBonusIndex() {
        return nextBonusIndex;
    }

    public void advanceNextBonus() {
        nextBonusIndex += 1;
    }

    public byte bonusSymbol(int i) {
        return bonusSymbols[i];
    }

    public void setBonusSymbol(int i, byte symbol) {
        bonusSymbols[i] = symbol;
    }

    public Pulse blinking() {
        return blinking;
    }

    public TickTimer powerTimer() {
        return powerTimer;
    }
}