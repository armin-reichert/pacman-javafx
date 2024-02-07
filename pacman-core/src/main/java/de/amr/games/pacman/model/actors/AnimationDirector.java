/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import java.util.Optional;

/**
 * @author Armin Reichert
 */
public interface AnimationDirector {

	@SuppressWarnings("rawtypes")
	Optional<Animations> animations();

	default void selectAnimation(String name, Object... args) {
		animations().ifPresent(a -> a.select(name, args));
	}

	default void startAnimation() {
		animations().ifPresent(Animations::startSelected);
	}

	default void stopAnimation() {
		animations().ifPresent(Animations::stopSelected);
	}

	default void resetAnimation() {
		animations().ifPresent(Animations::resetSelected);
	}
}