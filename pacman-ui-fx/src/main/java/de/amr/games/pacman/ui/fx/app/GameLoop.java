package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.ui.fx.Env;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

/**
 * Game loop that (in contrast to the {@link AnimationTimer} class) is always running at 60Hz.
 * 
 * @author Armin Reichert
 */
public class GameLoop {

	private static final int FRAME_RATE = 60;
	private static final Duration FRAME_DURATION = Duration.millis(1000d / FRAME_RATE);

	public final IntegerProperty $fps = new SimpleIntegerProperty();
	public final IntegerProperty $totalTicks = new SimpleIntegerProperty();

	private Timeline tl;
	private Runnable update;
	private Runnable render;
	private long fpsCountStartTime;
	private int frames;

	public GameLoop(Runnable update, Runnable render) {
		this.update = update;
		this.render = render;
		tl = new Timeline(FRAME_RATE);
		tl.setCycleCount(Animation.INDEFINITE);
		tl.getKeyFrames().add(new KeyFrame(FRAME_DURATION, e -> runSingleFrame()));
	}

	public void start() {
		tl.play();
	}

	public void stop() {
		tl.stop();
	}

	private void runSingleFrame() {
		long now = System.nanoTime();
		if (!Env.$paused.get() && $totalTicks.get() % Env.$slowDown.get() == 0) {
			runUpdate(now);
			// Note: we must also render at 60Hz because some animations depend on the rendering speed
			render.run();
			$totalTicks.set($totalTicks.get() + 1);
		}
		++frames;
		if (now - fpsCountStartTime > 1e9) {
			$fps.set(frames);
			frames = 0;
			fpsCountStartTime = now;
		}
	}

	private void runUpdate(long updateTime) {
		if (Env.$isTimeMeasured.get()) {
			double start_ns = System.nanoTime();
			update.run();
			double duration_ns = System.nanoTime() - start_ns;
			log("Update took %f milliseconds", duration_ns / 1e6);
		} else {
			update.run();
		}
	}
}