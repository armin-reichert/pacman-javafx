package de.amr.games.pacman.ui.fx._2d.rendering.common;

public interface ISpriteAnimation {

	void run();

	void stop();

	void reset();

	void restart();

	void ensureRunning();
}