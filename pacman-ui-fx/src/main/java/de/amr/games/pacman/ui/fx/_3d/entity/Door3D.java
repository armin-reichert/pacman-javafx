/*
MIT License

Copyright (c) 2021-22 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.actors.GhostState.DEAD;
import static de.amr.games.pacman.model.common.actors.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx.app.Env;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * Ghosthouse door.
 * 
 * @author Armin Reichert
 */
public class Door3D extends Box {

	private final boolean leftWing;

	public Door3D(V2i tile, boolean leftWing, Color color) {
		super(TS - 1, 1, HTS);
		this.leftWing = leftWing;
		setMaterial(new PhongMaterial(color));
		setTranslateX(tile.x * TS + HTS);
		setTranslateY(tile.y * TS + HTS);
		setTranslateZ(-HTS / 2);
		drawModeProperty().bind(Env.$drawMode3D);
	}

	public void update(GameModel game) {
		boolean ghostApproaching = game.ghosts() //
				.filter(ghost -> ghost.visible) //
				.filter(ghost -> ghost.is(DEAD) || ghost.is(ENTERING_HOUSE) || ghost.is(LEAVING_HOUSE)) //
				.anyMatch(this::isGhostNear);
		setVisible(!ghostApproaching);
	}

	private boolean isGhostNear(Ghost ghost) {
		V2d center = new V2d(getTranslateX(), getTranslateY()).plus(leftWing ? getWidth() / 2 : -getWidth() / 2, 0);
		double threshold = ghost.is(LEAVING_HOUSE) ? TS : 3 * TS;
		return ghost.position.euclideanDistance(center) <= threshold;
	}
}