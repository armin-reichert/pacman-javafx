/*
MIT License

Copyright (c) 2022 Armin Reichert

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

import java.util.function.Predicate;

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.SquirtingAnimation;
import de.amr.games.pacman.ui.fx._3d.entity.Maze3D.MazeStyle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

/**
 * @author Armin Reichert
 *
 */
public class Food3D extends Group {

	public final BooleanProperty squirtingPy = new SimpleBooleanProperty(false);

	private final GameVariant gameVariant;
	private final World world;
	private final Group particleGroup = new Group();

	public Food3D(GameVariant gameVariant, World world, MazeStyle mazeStyle) {
		this.gameVariant = gameVariant;
		this.world = world;
		squirtingPy.addListener((obs, oldVal, newVal) -> rebuildFood(mazeStyle));
		rebuildFood(mazeStyle);
	}

	private void rebuildFood(MazeStyle mazeStyle) {
		getChildren().clear();
		particleGroup.getChildren().clear();
		if (squirtingPy.get()) {
			createSquirtingPellets(mazeStyle);
		} else {
			createStandardPellets(mazeStyle.pelletColor);
		}
		getChildren().add(particleGroup);
	}

	private void createSquirtingPellets(MazeStyle mazeStyle) {
		var pelletMaterial = new PhongMaterial(mazeStyle.pelletColor);
		var squirtMaterial = new PhongMaterial(gameVariant == GameVariant.PACMAN ? Color.CORNFLOWERBLUE : Color.RED);
		world.tiles() //
				.filter(world::isFoodTile) //
				.filter(Predicate.not(world::containsEatenFood)) //
				.map(tile -> {
					if (world.isEnergizerTile(tile)) {
						var energizer3D = new Energizer3D(tile, pelletMaterial);
						energizer3D.setEatenAnimation(new SquirtingAnimation(world, particleGroup, energizer3D));
						return energizer3D;
					} else {
						if (tile.neighbors().filter(world::isWall).count() == 0) {
							var pellet3D = new Pellet3D(tile, squirtMaterial, 1.5);
							pellet3D.setEatenAnimation(new SquirtingAnimation(world, particleGroup, pellet3D));
							return pellet3D;
						} else {
							return new Pellet3D(tile, pelletMaterial);
						}
					}
				}).forEach(getChildren()::add);
	}

	private void createStandardPellets(Color pelletColor) {
		var pelletMaterial = new PhongMaterial(pelletColor);
		world.tiles()//
				.filter(world::isFoodTile)//
				.filter(Predicate.not(world::containsEatenFood))//
				.map(tile -> world.isEnergizerTile(tile)//
						? new Energizer3D(tile, pelletMaterial)//
						: new Pellet3D(tile, pelletMaterial, 1.0))//
				.forEach(getChildren()::add);
	}
}