package de.amr.games.pacman.ui.fx.common;

import de.amr.games.pacman.lib.CountdownTimer;

public class FlashMessage {

	public final CountdownTimer timer = new CountdownTimer();
	public String text;

	public FlashMessage(String text, long displayDuration) {
		this.text = text;
		timer.setDuration(displayDuration);
	}

}
