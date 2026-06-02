package de.amr.pacmanfx.state;

import de.amr.basics.fsm.State;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.model.GameModel;
import org.tinylog.Logger;

import java.util.Objects;

public class GameState implements State<GameModel> {

    private final String name;
    private final TickTimer timer;

    public GameState(String name) {
        this.name = Objects.requireNonNull(name);
        this.timer = new TickTimer("GameStateTimer-" + getClass().getSimpleName());
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void onEnter(GameModel context) {
        Logger.trace("onEnter");
    }

    @Override
    public void onUpdate(GameModel context) {
        Logger.trace("onUpdate");
    }

    @Override
    public void onExit(GameModel context) {
        Logger.trace("onExit");
    }

    @Override
    public TickTimer timer() {
        return timer;
    }
}
