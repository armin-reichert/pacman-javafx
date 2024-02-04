/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
module de.amr.games.pacman.ui.fx3d {

	requires javafx.graphics;
	requires transitive javafx.controls;
	requires transitive javafx.media;
	requires org.tinylog.api;
	requires transitive de.amr.games.pacman;
	requires transitive de.amr.games.pacman.ui.fx;

	exports de.amr.games.pacman.ui.fx.v3d;
	exports de.amr.games.pacman.ui.fx.v3d.animation;
	exports de.amr.games.pacman.ui.fx.v3d.dashboard;
	exports de.amr.games.pacman.ui.fx.v3d.entity;
	exports de.amr.games.pacman.ui.fx.v3d.model;
	exports de.amr.games.pacman.ui.fx.v3d.scene3d;

	// give access for loading module (non-class) resources, see Class.getResource()
	opens de.amr.games.pacman.ui.fx.v3d.graphics;
	opens de.amr.games.pacman.ui.fx.v3d.graphics.icons;
	opens de.amr.games.pacman.ui.fx.v3d.graphics.textures;
	opens de.amr.games.pacman.ui.fx.v3d.model3D;
}