/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import java.util.Optional;

import de.amr.games.pacman.lib.Vector2i;
import javafx.animation.Animation;
import javafx.geometry.Point3D;
import javafx.scene.Node;

/**
 * @author Armin Reichert
 */
public interface Eatable3D {

	Node getRoot();

	void eat();

	Optional<Animation> getEatenAnimation();

	Vector2i tile();

	Point3D position();
}
