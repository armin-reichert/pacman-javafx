/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d.mspacman;

/**
 * @author Armin Reichert
 */
public class ClapperBoardAnimation {
	private String number;
	private String text;
	private long t;
	private boolean running;

	public ClapperBoardAnimation(String number, String text) {
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

	public int currentSpriteIndex() {
		if (t == 0) {
			return 2; // closed
		}
		if (t <= 47) {
			return 0; // wide open
		}
		if (t <= 53) {
			return 1; // middle open
		}
		if (t <= 58) {
			return 2;
		}
		if (t <= 87) {
			return 0;
		}
		return -1; // no sprite
	}
}