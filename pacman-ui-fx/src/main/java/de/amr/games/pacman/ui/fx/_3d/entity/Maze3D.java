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

import static de.amr.games.pacman.model.common.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.RaiseAndLowerWallAnimation;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;

/**
 * 3D-model for a maze. Creates walls/doors using information from the floor plan.
 * 
 * @author Armin Reichert
 */
public class Maze3D {

	public static class MazeStyle {
		public Color wallSideColor;
		public Color wallTopColor;
		public Color doorColor;
		public Image floorTexture;
		public Color floorTextureColor;
		public Color floorSolidColor;
		public Color foodColor;
	}

	public final IntegerProperty resolution = new SimpleIntegerProperty(8);
	public final DoubleProperty wallHeight = new SimpleDoubleProperty(1.0);
	public final BooleanProperty floorTextureVisible = new SimpleBooleanProperty(false);

	// package access for mason
	final World world;
	final Group root = new Group();
	final Group foundationGroup = new Group();
	final Group doorsGroup = new Group();
	final Group foodGroup = new Group();

	private final MazeStyle style;

	public Maze3D(World world, MazeStyle style) {
		this.world = world;
		this.style = style;
		root.getChildren().addAll(foundationGroup, doorsGroup, foodGroup);
		Mason.erectBuilding(this, world, style);
		addFood(world, style.foodColor);

		resolution.addListener((obs, oldVal, newVal) -> Mason.erectBuilding(this, world, style));
		MazeFloor3D floor = (MazeFloor3D) foundationGroup.getChildren().get(0);
		floor.textureVisible.bind(floorTextureVisible);
	}

	public World getWorld() {
		return world;
	}

	public Group getRoot() {
		return root;
	}

	public MazeStyle getStyle() {
		return style;
	}

	public Stream<Door3D> doors() {
		return doorsGroup.getChildren().stream().map(Node::getUserData).map(Door3D.class::cast);
	}

	public Animation createMazeFlashingAnimation(int times) {
		return times > 0 ? new RaiseAndLowerWallAnimation(times) : new PauseTransition(Duration.seconds(1));
	}

	public void reset() {
		energizerAnimations().forEach(Animation::stop);
		energizers().forEach(node -> {
			node.setScaleX(1.0);
			node.setScaleY(1.0);
			node.setScaleZ(1.0);
		});
	}

	public void updateDoorState(Stream<Ghost> ghosts) {
		doors().findFirst().ifPresent(firstDoor3D -> {
			var centerPosition = firstDoor3D.getCenterPosition();
			boolean openDoors = isAnyGhostGettingAccess(ghosts, centerPosition);
			doors().forEach(door3D -> door3D.setOpen(openDoors));
		});
	}

	private boolean isAnyGhostGettingAccess(Stream<Ghost> ghosts, V2d centerPosition) {
		return ghosts //
				.filter(ghost -> ghost.visible) //
				.filter(ghost -> U.oneOf(ghost.state, GhostState.DEAD, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)) //
				.anyMatch(ghost -> isGhostGettingAccess(ghost, centerPosition));
	}

	private boolean isGhostGettingAccess(Ghost ghost, V2d doorCenter) {
		return ghost.position.euclideanDistance(doorCenter) <= (ghost.is(LEAVING_HOUSE) ? TS : 3 * TS);
	}

	private void addFood(World world, Color foodColor) {
		var meatBall = new PhongMaterial(foodColor);
		world.tiles() //
				.filter(world::isFoodTile) //
				.map(tile -> world.isEnergizerTile(tile) //
						? new Energizer3D(tile, meatBall, 3.0)
						: new Pellet3D(tile, meatBall, 1.0))
				.forEach(foodGroup.getChildren()::add);
	}

	public Stream<Animation> energizerAnimations() {
		return energizers().map(Energizer3D::animation);
	}

	public Optional<Node> foodAt(V2i tile) {
		return foodNodes().filter(food -> tile(food).equals(tile)).findFirst();
	}

	public void hideFood(Node foodNode) {
		foodNode.setVisible(false);
		if (foodNode instanceof Energizer3D) {
			var energizer = (Energizer3D) foodNode;
			energizer.animation().stop();
		}
	}

	public void validateFoodNodes() {
		foodNodes().forEach(foodNode -> foodNode.setVisible(!world.containsEatenFood(tile(foodNode))));
	}

	private Stream<Node> foodNodes() {
		return foodGroup.getChildren().stream();
	}

	private V2i tile(Node foodNode) {
		return (V2i) foodNode.getUserData();
	}

	private Stream<Energizer3D> energizers() {
		return foodNodes().filter(Energizer3D.class::isInstance).map(Energizer3D.class::cast);
	}
}