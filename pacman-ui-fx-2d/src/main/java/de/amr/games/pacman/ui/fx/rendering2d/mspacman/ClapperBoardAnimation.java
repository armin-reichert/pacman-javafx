/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d.mspacman;

import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class ClapperBoardAnimation {
	private Rectangle2D[] sprites;
	private String number;
	private String text;
	private long t;
	private boolean running;

	public ClapperBoardAnimation(Rectangle2D[] sprites, String number, String text) {
		this.sprites = sprites;
		this.number = number;
		this.text = text;
	}

	public String number() {
		return number;
	}

	public String text() {
		return text;
	}

	public void start() {
		t = 0;
		running = true;
	}

	public void tick() {
		if (running) {
			++t;
		}
	}

	public Rectangle2D currentSprite() {
		if (t == 0) {
			return sprites[2];
		}
		if (t <= 47) {
			return sprites[0];
		}
		if (t <= 53) {
			return sprites[1];
		}
		if (t <= 58) {
			return sprites[2];
		}
		if (t <= 87) {
			return sprites[0];
		}
		if (t <= 90) {
			return sprites[2];
		}
		return null;
	}
}