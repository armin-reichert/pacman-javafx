/*
MIT License

Copyright (c) 2021-22 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx._2d.rendering.common;

import de.amr.games.pacman.lib.TimedSeq;

/**
 * @author Armin Reichert
 */
public class SpriteAnimation<S> implements ISpriteAnimation<S> {

	private final TimedSeq<S> ts;

	@SuppressWarnings("unchecked")
	public SpriteAnimation(S... sprites) {
		ts = new TimedSeq<>(sprites);
	}

	@Override
	public void run() {
		ts.run();
	}

	@Override
	public void stop() {
		ts.stop();
	}

	@Override
	public void reset() {
		ts.reset();
	}

	@Override
	public void restart() {
		ts.restart();
	}

	@Override
	public void ensureRunning() {
		ts.ensureRunning();
	}

	@Override
	public void setFrameIndex(int index) {
		ts.setFrameIndex(index);
	}

	public S animate() {
		return ts.animate();
	}

	public S frame() {
		return ts.frame();
	}

	public S frame(int index) {
		return ts.frame(index);
	}

	public int numFrames() {
		return ts.numFrames();
	}

	public boolean isRunning() {
		return ts.isRunning();
	}

	public SpriteAnimation<S> repetitions(int n) {
		ts.repetitions(n);
		return this;
	}

	public SpriteAnimation<S> frameDuration(long ticks) {
		ts.frameDuration(ticks);
		return this;
	}

	public SpriteAnimation<S> endless() {
		ts.endless();
		return this;
	}

	public SpriteAnimation<S> advance() {
		ts.advance();
		return this;
	}

}