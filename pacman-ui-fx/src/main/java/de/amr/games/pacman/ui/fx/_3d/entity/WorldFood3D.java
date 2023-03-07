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

import java.util.ArrayList;
import java.util.List;
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
public class WorldFood3D {

	public final BooleanProperty eatenAnimationEnabledPy = new SimpleBooleanProperty(this, "eatenAnimationEnabled");

	private final World world;
	private final Group root = new Group();
	private final Group particlesGroup = new Group();
	private final PhongMaterial pelletMaterial;
	private final List<Eatable3D> eatables = new ArrayList<>();

	public WorldFood3D(World world, Color foodColor) {
		this.world = world;
		pelletMaterial = new PhongMaterial(foodColor);
		world.tiles().filter(world::isFoodTile).filter(not(world::containsEatenFood)).forEach(tile -> {
			Eatable3D eatable3D = world.isEnergizerTile(tile) ? createEnergizer(tile) : createNormalPellet(tile);
			eatables.add(eatable3D);
			root.getChildren().add(eatable3D.getRoot());
		});
		root.getChildren().add(particlesGroup);
	}

	public Group getRoot() {
		return root;
	}

	private Pellet3D createNormalPellet(Vector2i tile) {
		var pellet3D = new Pellet3D(pelletMaterial, 1.0);
		pellet3D.setTile(tile);
		return pellet3D;
	}

	private Energizer3D createEnergizer(Vector2i tile) {
		var energizer3D = new Energizer3D(pelletMaterial, 3.5);
		energizer3D.setTile(tile);
		var eatenAnimation = new SquirtingAnimation(world, particlesGroup, energizer3D.getRoot());
		energizer3D.setEatenAnimation(eatenAnimation);
		return energizer3D;
	}

	public void eatPellet(Eatable3D eatable3D) {
		if (eatable3D instanceof Energizer3D energizer) {
			energizer.stopPumping();
		}
		// Delay hiding of pellet for some milliseconds because in case the player approaches the pellet from the right,
		// the pellet disappears too early (collision by same tile in game model is too simplistic).
		var delayHiding = Ufx.afterSeconds(0.05, () -> eatable3D.getRoot().setVisible(false));
		var eatenAnimation = eatable3D.getEatenAnimation();
		if (eatenAnimation.isPresent() && eatenAnimationEnabledPy.get()) {
			new SequentialTransition(delayHiding, eatenAnimation.get()).play();
		} else {
			delayHiding.play();
		}
	}

	/**
	 * @return all 3D pellets, including energizers
	 */
	public Stream<Eatable3D> eatables3D() {
		return eatables.stream();
	}

	public Stream<Energizer3D> energizers3D() {
		return eatables.stream().filter(Energizer3D.class::isInstance).map(Energizer3D.class::cast);
	}

	public Optional<Eatable3D> eatableAt(Vector2i tile) {
		return eatables.stream().filter(eatable -> eatable.tile().equals(tile)).findFirst();
	}
}