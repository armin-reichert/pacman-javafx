/*
MIT License

Copyright (c) 2021 Armin Reichert

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

import static de.amr.games.pacman.model.common.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
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

	public final V2i tile;

	public Door3D(V2i tile) {
		super(TS - 1, 1, HTS);
		this.tile = tile;
		setMaterial(new PhongMaterial(Color.PINK));
		setTranslateX(tile.x * TS + HTS);
		setTranslateY(tile.y * TS + HTS);
		setTranslateZ(-HTS / 2);
		drawModeProperty().bind(Env.$drawMode3D);
	}

	public void update(GameModel game) {
		setVisible(!isAnyGhostNearby(game.ghosts().filter(ghost -> ghost.is(ENTERING_HOUSE) || ghost.is(LEAVING_HOUSE))));
	}

	private boolean isAnyGhostNearby(Stream<Ghost> ghosts) {
		return ghosts.anyMatch(this::isGhostNearby);
	}

	private boolean isGhostNearby(Ghost ghost) {
		if (!ghost.visible) {
			return false;
		}
		V2i ghostTile = ghost.tile();
		return Math.abs(ghostTile.x - tile.x) <= 1 || Math.abs(ghostTile.y - tile.y) <= 1;
	}
}