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

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2i;
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
 */
public class Food3D extends Group {

	public final BooleanProperty squirtingPy = new SimpleBooleanProperty(false);

	private final World world;
	private final Group particleGroup = new Group();
	private final PhongMaterial squirtingPelletMaterial;
	private final PhongMaterial normalPelletMaterial;

	public Food3D(GameVariant gameVariant, World world, MazeStyle mazeStyle) {
		this.world = world;
		normalPelletMaterial = new PhongMaterial(mazeStyle.pelletColor);
		squirtingPelletMaterial = new PhongMaterial(gameVariant == GameVariant.PACMAN ? Color.CORNFLOWERBLUE : Color.RED);
		squirtingPy.addListener((obs, oldVal, newVal) -> updateFood());
		createPellets(squirtingPy.get());
		createEnergizers(squirtingPy.get());
		getChildren().add(particleGroup);
	}

	private void updateFood() {
		boolean squirtingEnabled = squirtingPy.get();
		energizers3D().forEach(energizer3D -> {
			if (squirtingEnabled) {
				energizer3D.setEatenAnimation(new SquirtingAnimation(world, particleGroup, energizer3D));
			} else {
				energizer3D.setEatenAnimation(null);
			}
		});
		pellets3D()//
				.filter(pellet3D -> world.containsFood(pellet3D.tile()))//
				.filter(Predicate.not(Energizer3D.class::isInstance))//
				.forEach(pellet3D -> {
					if (squirtingEnabled && isSquirterTile(pellet3D.tile())) {
						pellet3D.setRadius(1.5);
						pellet3D.setMaterial(squirtingPelletMaterial);
						pellet3D.setEatenAnimation(new SquirtingAnimation(world, particleGroup, pellet3D));
					} else {
						pellet3D.setRadius(1.0);
						pellet3D.setMaterial(normalPelletMaterial);
						pellet3D.setEatenAnimation(null);
					}
				});
	}

	private void createPellets(boolean squirtingEnabled) {
		world.tiles() //
				.filter(world::isFoodTile)//
				.filter(Predicate.not(world::containsEatenFood))//
				.filter(Predicate.not(world::isEnergizerTile))//
				.map(tile -> squirtingEnabled && isSquirterTile(tile) ? createSquirtingPellet(tile) : createNormalPellet(tile))
				.forEach(getChildren()::add);
	}

	private void createEnergizers(boolean squirtingEnabled) {
		world.tiles() //
				.filter(world::isFoodTile) //
				.filter(Predicate.not(world::containsEatenFood))//
				.filter(world::isEnergizerTile)//
				.map(tile -> squirtingEnabled && isSquirterTile(tile) ? createSquirtingEnergizer(tile)
						: createNormalEnergizer(tile))
				.forEach(getChildren()::add);
	}

	private Pellet3D createNormalPellet(V2i tile) {
		return new Pellet3D(tile, normalPelletMaterial, 1.0);
	}

	private Energizer3D createNormalEnergizer(V2i tile) {
		return new Energizer3D(tile, normalPelletMaterial);
	}

	private Pellet3D createSquirtingPellet(V2i tile) {
		var pellet3D = new Pellet3D(tile, squirtingPelletMaterial, 1.5);
		pellet3D.setEatenAnimation(new SquirtingAnimation(world, particleGroup, pellet3D));
		return pellet3D;
	}

	private Energizer3D createSquirtingEnergizer(V2i tile) {
		var energizer3D = new Energizer3D(tile, normalPelletMaterial);
		energizer3D.setEatenAnimation(new SquirtingAnimation(world, particleGroup, energizer3D));
		return energizer3D;
	}

	private boolean isSquirterTile(V2i tile) {
		return tile.neighbors().filter(world::isWall).count() == 0;
	}

	/**
	 * @return all 3D pellets, including energizers
	 */
	public Stream<Pellet3D> pellets3D() {
		return getChildren().stream().filter(Pellet3D.class::isInstance).map(Pellet3D.class::cast);
	}

	public Stream<Energizer3D> energizers3D() {
		return pellets3D().filter(Energizer3D.class::isInstance).map(Energizer3D.class::cast);
	}

	public Optional<Pellet3D> pelletAt(V2i tile) {
		return pellets3D().filter(pellet3D -> pellet3D.tile().equals(tile)).findFirst();
	}

	public void resetAnimations() {
		energizers3D().forEach(e3D -> {
			e3D.setScaleX(1.0);
			e3D.setScaleY(1.0);
			e3D.setScaleZ(1.0);
			e3D.stopPumping();
		});
	}
}