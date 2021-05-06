package de.amr.games.pacman.ui.fx;

import java.util.Random;

/**
 * Generates random, repetition-free trash talk sentences.
 * 
 * @author Armin Reichert
 */
public class TrashTalk {

	public static class RandomSpellGenerator {
		private String[] texts;
		private int spell;

		public RandomSpellGenerator(String... texts) {
			this.texts = texts;
			spell = 0;
		}

		public String nextSpell() {
			int last = spell;
			if (texts.length > 1) {
				while (spell == last) {
					spell = new Random().nextInt(texts.length);
				}
			} else {
				spell = 0;
			}
			return texts[spell];
		}
	}

	public static final RandomSpellGenerator CHEAT_SPELLS = new RandomSpellGenerator(//
			"You old cheating bastard!", //
			"I told you, I will erase your hard disk!", //
			"Cheaters are the worst human beings!", //
			"Do you think I will not notice this?", //
			"Ah, Mr. Super-Clever again", //
			"STOP! CHEATING! NOW!" //
	);

	public static final RandomSpellGenerator LEVEL_COMPLETE_SPELLS = new RandomSpellGenerator(//
			"Well done!", //
			"Congratulations!", //
			"Awesome!", //
			"You really did it!", //
			"You're the man*in!", //
			"WTF!"//
	);

	public static final RandomSpellGenerator GAME_OVER_SPELLS = new RandomSpellGenerator(//
			"You stone cold loser!", //
			"I would say you fucked up!", //
			"This game is OVER!", //
			"Go ahead any cry!", //
			"That's all you've got?"//
	);

}