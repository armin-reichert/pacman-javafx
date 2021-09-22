package de.amr.games.pacman.ui.fx;

import java.util.ResourceBundle;

import de.amr.games.pacman.ui.fx.util.RandomEntrySelector;

public class TrashTalk {

	public static final RandomEntrySelector<String> CHEAT_TALK = load("/common/cheating_talk");
	public static final RandomEntrySelector<String> LEVEL_COMPLETE_TALK = load("/common/level_complete_talk");
	public static final RandomEntrySelector<String> GAME_OVER_TALK = load("/common/game_over_talk");

	private static RandomEntrySelector<String> load(String bundleName) {
		ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
		return new RandomEntrySelector<>(
				bundle.keySet().stream().sorted().map(bundle::getString).toArray(String[]::new));
	}
}