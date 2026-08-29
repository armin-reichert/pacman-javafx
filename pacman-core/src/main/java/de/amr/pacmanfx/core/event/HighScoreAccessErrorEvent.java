package de.amr.pacmanfx.core.event;

import java.io.IOException;

public record HighScoreAccessErrorEvent(IOException reason) implements GameEvent { }
