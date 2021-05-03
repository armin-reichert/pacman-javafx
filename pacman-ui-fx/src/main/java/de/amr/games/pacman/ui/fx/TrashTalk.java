package de.amr.games.pacman.ui.fx;

import java.util.Random;

public class TrashTalk {

	private static final String[] CHEAT_TEXTS = { //
			"You old cheating bastard!", //
			"I told you, I will erase your hard disk!", //
			"Cheaters are the worst human beings!", //
			"Do you think I will not notice this?", //
			"Ah, Mr. Super-Clever again", //
			"STOP! CHEATING! NOW!"
	};

	public static String randomCheaterSpell() {
		return CHEAT_TEXTS[new Random().nextInt(CHEAT_TEXTS.length)];
	}

	private static final String[] LEVEL_COMPLETE_TEXTS = { //
			"Well done!", //
			"Congratulations!", //
			"Awesome!", //
			"You really did it!", //
			"You're the man*in!", //
			"WTF!"//
	};

	public static String randomLevelCompleteSpell() {
		return LEVEL_COMPLETE_TEXTS[new Random().nextInt(LEVEL_COMPLETE_TEXTS.length)];
	}
}