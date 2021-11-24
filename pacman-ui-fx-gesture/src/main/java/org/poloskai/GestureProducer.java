package org.poloskai;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedDeque;

import de.amr.games.pacman.lib.Logging;

/**
 * Simulates gesture producing process. Produces some number of random gestures every second.
 */
public class GestureProducer {

	private static long INTERVAL_MILLIS = 250; // 4 gestures per second

	public static final String[] GESTURES = { "UP", "DOWN", "LEFT", "RIGHT" };

	// Test
	public static void main(String[] args) {
		GestureProducer gp = new GestureProducer();
		gp.start();
	}

	public final ConcurrentLinkedDeque<String> gestureQ = new ConcurrentLinkedDeque<>();
	private final Timer timer = new Timer();
	private final TimerTask produceGestureTask = new TimerTask() {

		@Override
		public void run() {
			String gesture = GESTURES[new Random().nextInt(GESTURES.length)];
			// Add gesture only if it is different from the last one added
			if (gestureQ.isEmpty() || !gestureQ.peekLast().equals(gesture)) {
				gestureQ.add(gesture);
				Logging.log("Added gesture: %s, Q size: %d", gesture, gestureQ.size());
			} else {
				Logging.log("Ignored gesture: %s", gesture);
			}
		}
	};

	public void start() {
		timer.scheduleAtFixedRate(produceGestureTask, 0, INTERVAL_MILLIS);
	}

	public void stop() {
		timer.cancel();
	}
}