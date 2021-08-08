package de.amr.games.pacman.ui.fx.scenes.common;

import de.amr.games.pacman.ui.fx.util.RandomEntrySelector;

public class TrashTalk {

	public static final RandomEntrySelector<String> CHEAT_TALK = new RandomEntrySelector<>(//
			"You old cheating bastard!", //
			"I told you, I will erase your hard disk!", //
			"Cheaters are the worst human beings!", //
			"Do you think I will not notice this?", //
			"Ah, Mr. Super-Clever again", //
			"STOP! CHEATING! NOW!" //
	);

	public static final RandomEntrySelector<String> LEVEL_COMPLETE_TALK = new RandomEntrySelector<>(//
			"Well done!", //
			"Congratulations!", //
			"Awesome!", //
			"You really did it!", //
			"You're the man*in!", //
			"WTF!"//
	);

	public static final RandomEntrySelector<String> GAME_OVER_TALK = new RandomEntrySelector<>(//
			"You stone cold loser!", //
			"I would say you fucked up!", //
			"This game is OVER!", //
			"Go ahead and cry!", //
			"That's all you've got?"//
	);
}