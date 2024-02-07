/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

/**
 * @author Armin Reichert
 */
public class Pulse {

	private final boolean startValue;
	private final int ticksPerFrame;
	public int numFramesTotal;
	private boolean value;
	private int t;
	private int frames;
	private boolean stopped;

	public Pulse(int numCycles, int ticksPerFrame, boolean startValue) {
		this.numFramesTotal = numCycles;
		this.startValue = startValue;
		this.ticksPerFrame = ticksPerFrame;
		reset();
	}

	public Pulse(int ticksPerFrame, boolean startValue) {
		this(Integer.MAX_VALUE, ticksPerFrame, startValue);
	}

	public void reset() {
		t = 0;
		frames = 0;
		value = startValue;
		stopped = true;
	}

	public void restart() {
		reset();
		start();
	}

	public void restart(int numCycles) {
		this.numFramesTotal = numCycles;
		reset();
		start();
	}

	public void tick() {
		if (stopped || frames == numFramesTotal) {
			return;
		}
		++t;
		if (t == ticksPerFrame) {
			value = !value;
			frames++;
			t = 0;
		}
	}

	public boolean on() {
		return value;
	}

	public boolean off() {
		return !value;
	}

	public void start() {
		stopped = false;
	}

	public boolean isStopped() {
		return stopped;
	}

	public boolean isRunning() {
		return !stopped;
	}

	public void stop() {
		stopped = true;
	}
}
