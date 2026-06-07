package de.amr.pacmanfx.gamestate;

import de.amr.basics.fsm.State;

import static java.util.Objects.requireNonNull;

public interface GameStateIdentifier {

    String name();

    default boolean identifies(State<?> gameState) {
        requireNonNull(gameState);
        return gameState.name().equals(name());
    }
}
