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

import static java.util.function.Predicate.not;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.SquirtingAnimation;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.SequentialTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

/**
 * @author Armin Reichert
 */
public class Food3D extends Group {

	public final BooleanProperty squirtingEffectPy = new SimpleBooleanProperty() {
		@Override
		protected void invalidated() {
			energizers3D().forEach(energizer3D -> energizer3D.setEatenAnimation(
					squirtingEffectPy.get() ? new SquirtingAnimation(world, particlesGroup, energizer3D) : null));
		}
	};

	private final World world;
	private final Group particlesGroup = new Group();
	private final PhongMaterial pelletMaterial;

	public Food3D(World world, Color foodColor) {
		this.world = world;
		pelletMaterial = new PhongMaterial(foodColor);
		createNormalPellets();
		createEnergizers();
		getChildren().add(particlesGroup);
	}

	private void createNormalPellets() {
		world.tiles()//
				.filter(world::isFoodTile)//
				.filter(not(world::containsEatenFood))//
				.filter(not(world::isEnergizerTile))//
				.map(this::createNormalPellet)//
				.forEach(getChildren()::add);
	}

	private void createEnergizers() {
		world.tiles()//
				.filter(world::isFoodTile)//
				.filter(not(world::containsEatenFood))//
				.filter(world::isEnergizerTile)//
				.map(tile -> squirtingEffectPy.get() ? createSquirtingEnergizer(tile) : createNormalEnergizer(tile))//
				.forEach(getChildren()::add);
	}

	private Pellet3D createNormalPellet(Vector2i tile) {
		return new Pellet3D(tile, pelletMaterial, 1.0);
	}

	private Energizer3D createNormalEnergizer(Vector2i tile) {
		return new Energizer3D(tile, pelletMaterial);
	}

	private Energizer3D createSquirtingEnergizer(Vector2i tile) {
		var energizer3D = new Energizer3D(tile, pelletMaterial);
		energizer3D.setEatenAnimation(new SquirtingAnimation(world, particlesGroup, energizer3D));
		return energizer3D;
	}

	public void eatPellet(Pellet3D pellet3D) {
		if (pellet3D instanceof Energizer3D energizer) {
			energizer.stopPumping();
		}
		// Delay hiding of pellet for some milliseconds because in case the player approaches the pellet from the right,
		// the pellet disappears too early (collision by same tile in game model is too simplistic).
		var delayHiding = Ufx.afterSeconds(0.05, () -> pellet3D.setVisible(false));
		var eatenAnimation = pellet3D.getEatenAnimation();
		if (eatenAnimation.isPresent()) {
			new SequentialTransition(delayHiding, eatenAnimation.get()).play();
		} else {
			delayHiding.play();
		}
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

	public Optional<Pellet3D> pelletAt(Vector2i tile) {
		return pellets3D().filter(pellet3D -> pellet3D.tile().equals(tile)).findFirst();
	}
}