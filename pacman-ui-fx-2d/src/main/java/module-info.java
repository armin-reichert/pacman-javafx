/*
 * Copyright (c) 2021-2023 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
module de.amr.games.pacman.ui.fx {

	requires transitive de.amr.games.pacman;
	requires transitive javafx.controls;
	requires transitive javafx.media;
	requires transitive org.tinylog.api;

	exports de.amr.games.pacman.ui.fx;
	exports de.amr.games.pacman.ui.fx.input;
	exports de.amr.games.pacman.ui.fx.scene;
	exports de.amr.games.pacman.ui.fx.util;
	exports de.amr.games.pacman.ui.fx.rendering2d;
	exports de.amr.games.pacman.ui.fx.scene2d;
}