package de.amr.games.pacman.ui.fx.scenes.common;

public class TrashTalk {

	public static final RandomEntrySelector<String> CHEAT_SPELLS = new RandomEntrySelector<>(//
			"You old cheating bastard!", //
			"I told you, I will erase your hard disk!", //
			"Cheaters are the worst human beings!", //
			"Do you think I will not notice this?", //
			"Ah, Mr. Super-Clever again", //
			"STOP! CHEATING! NOW!" //
	);

	public static final RandomEntrySelector<String> LEVEL_COMPLETE_SPELLS = new RandomEntrySelector<>(//
			"Well done!", //
			"Congratulations!", //
			"Awesome!", //
			"You really did it!", //
			"You're the man*in!", //
			"WTF!"//
	);

	public static final RandomEntrySelector<String> GAME_OVER_SPELLS = new RandomEntrySelector<>(//
			"You stone cold loser!", //
			"I would say you fucked up!", //
			"This game is OVER!", //
			"Go ahead any cry!", //
			"That's all you've got?"//
	);
}