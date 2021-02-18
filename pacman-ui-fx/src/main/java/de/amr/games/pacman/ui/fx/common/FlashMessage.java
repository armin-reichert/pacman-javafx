package de.amr.games.pacman.ui.fx.common;

import de.amr.games.pacman.lib.CountdownTimer;

public class FlashMessage {

	final CountdownTimer timer = new CountdownTimer();
	String text;

	public FlashMessage(String text, long displayDuration) {
		this.text = text;
		timer.setDuration(displayDuration);
	}

}
