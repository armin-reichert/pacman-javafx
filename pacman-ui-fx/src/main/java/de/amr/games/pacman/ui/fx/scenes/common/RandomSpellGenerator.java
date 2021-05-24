package de.amr.games.pacman.ui.fx.scenes.common;

import java.util.Random;

/**
 * Generates random, repetition-free trash talk sentences.
 * 
 * @author Armin Reichert
 */
public class RandomSpellGenerator {
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