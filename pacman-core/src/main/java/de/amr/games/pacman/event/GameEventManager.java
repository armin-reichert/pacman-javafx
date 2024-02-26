package de.amr.games.pacman.event;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class GameEventManager {
	private static final List<GameEventListener> gameEventListeners = new ArrayList<>();

	public static void addListener(GameEventListener gameEventListener) {
		checkNotNull(gameEventListener);
		gameEventListeners.add(gameEventListener);
	}

	public static void removeListener(GameEventListener gameEventListener) {
		checkNotNull(gameEventListener);
		gameEventListeners.remove(gameEventListener);
	}

	public static void publishGameEvent(GameModel game, GameEventType type) {
		publishGameEvent(new GameEvent(type, game, null));
	}

	public static void publishGameEvent(GameModel game, GameEventType type, Vector2i tile) {
		publishGameEvent(new GameEvent(type, game, tile));
	}

	public static void publishGameEvent(GameEvent event) {
		Logger.trace("Publish game event: {}", event);
		gameEventListeners.forEach(subscriber -> subscriber.onGameEvent(event));
	}
}