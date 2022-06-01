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
import javafx.geometry.Rectangle2D;

public class SpriteAnimation implements ISpriteAnimation {

	private TimedSeq<Rectangle2D> seq;

	public static SpriteAnimation of(Rectangle2D... sprites) {
		if (sprites.length == 0) {
			throw new IllegalArgumentException("Animation must have at least one frame");
		}
		SpriteAnimation animation = new SpriteAnimation();
		animation.seq = TimedSeq.of(sprites);
		return animation;
	}

	public SpriteAnimation repetitions(int n) {
		seq.repetitions(n);
		return this;
	}

	public SpriteAnimation frameDuration(long ticks) {
		seq.frameDuration(ticks);
		return this;
	}

	public SpriteAnimation endless() {
		seq.endless();
		return this;
	}

	public SpriteAnimation run() {
		seq.run();
		return this;
	}

	public Rectangle2D animate() {
		return seq.animate();
	}

	public void advance() {
		seq.advance();
	}

	@Override
	public void reset() {
		seq.reset();
	}

	public void restart() {
		seq.restart();
	}

	@Override
	public void stop() {
		seq.stop();
	}

	public boolean isRunning() {
		return seq.isRunning();
	}

	public int numFrames() {
		return seq.numFrames();
	}

	public void setFrameIndex(int i) {
		seq.setFrameIndex(i);
	}

	public Rectangle2D frame() {
		return seq.frame();
	}

	public Rectangle2D frame(int i) {
		return seq.frame(i);
	}

	public long getFrameDuration() {
		return seq.getFrameDuration();
	}

	@Override
	public void ensureRunning() {
		if (seq.isRunning()) {
			seq.run();
		}
	}
}