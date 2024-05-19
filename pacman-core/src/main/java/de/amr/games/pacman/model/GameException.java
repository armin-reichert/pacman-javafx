package de.amr.games.pacman.model;

public class GameException extends RuntimeException {

    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
}
