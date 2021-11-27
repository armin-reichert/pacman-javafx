package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.ui.fx.Env;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

public class GameLoop2 {

	public final IntegerProperty $fps = new SimpleIntegerProperty();
	public final IntegerProperty $totalTicks = new SimpleIntegerProperty();

	private Runnable update;
	private Runnable render;
	private long fpsCountStartTime;
	private int frames;

	public GameLoop2(Runnable update, Runnable render) {
		this.update = update;
		this.render = render;
	}

	public void start() {
		int fps = 60;
		Duration one_60th_sec = Duration.millis(1000d / fps);
		Timeline tl = new Timeline(fps);
		tl.setCycleCount(Animation.INDEFINITE);
		tl.getKeyFrames().add(new KeyFrame(one_60th_sec, e -> {
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
		}));
		tl.play();
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