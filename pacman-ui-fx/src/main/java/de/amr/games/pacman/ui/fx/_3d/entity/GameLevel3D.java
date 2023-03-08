/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

import static de.amr.games.pacman.model.common.actors.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.RETURNING_TO_HOUSE;
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;
import static java.util.function.Predicate.not;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.common.ArcadeTheme;
import de.amr.games.pacman.ui.fx._2d.rendering.common.MazeColoring;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpritesheetGameRenderer;
import de.amr.games.pacman.ui.fx._3d.animation.SquirtingAnimation;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.SequentialTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;

/**
 * @author Armin Reichert
 */
public class GameLevel3D {

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
	public final BooleanProperty eatenAnimationEnabledPy = new SimpleBooleanProperty(this, "eatenAnimationEnabled");

	private final GameLevel level;
	private final Group root = new Group();
	private final Group foodRoot;
	private final Group particlesGroup = new Group();
	private final World3D world3D;
	private final Pac3D pac3D;
	private final Ghost3D[] ghosts3D;
	private final Bonus3D bonus3D;
	private final LevelCounter3D levelCounter3D;
	private final LivesCounter3D livesCounter3D;
	private final Scores3D scores3D;
	private final List<Eatable3D> eatables = new ArrayList<>();
	private PointLight pacLight;

	public GameLevel3D(GameLevel level, Rendering2D r2D) {
		this.level = level;
		int mazeNumber = level.game().mazeNumber(level.number());

		world3D = createWorld3D(r2D.mazeColoring(mazeNumber));
		pac3D = createPac3D();
		ghosts3D = level.ghosts().map(this::createGhost3D).toArray(Ghost3D[]::new);
		bonus3D = createBonus3D(level.bonus(), r2D);
		levelCounter3D = createLevelCounter3D(r2D);
		livesCounter3D = createLivesCounter3D();
		scores3D = new Scores3D(r2D.screenFont(TS));
		foodRoot = createFoodTG(r2D.mazeColoring(mazeNumber));

		root.getChildren().addAll(world3D.getRoot(), foodRoot, pac3D.getRoot(), bonus3D.getRoot(), scores3D.getRoot(),
				levelCounter3D.getRoot(), livesCounter3D.getRoot());
		Arrays.stream(ghosts3D).map(Ghost3D::getRoot).forEach(root.getChildren()::add);
	}

	private World3D createWorld3D(MazeColoring mazeColoring) {
		var world3D = new World3D(level.world(), mazeColoring);
		world3D.drawModePy.bind(drawModePy);
		return world3D;
	}

	private Group createFoodTG(MazeColoring mazeColoring) {
		var tg = new Group();
		var material = new PhongMaterial(mazeColoring.foodColor());
		var world = level.world();
		world.tiles().filter(world::isFoodTile).filter(not(world::containsEatenFood)).forEach(tile -> {
			var eatable3D = world.isEnergizerTile(tile) ? createEnergizer3D(world, tile, material)
					: createNormalPellet3D(tile, material);
			eatables.add(eatable3D);
			tg.getChildren().add(eatable3D.getRoot());
		});
		tg.getChildren().add(particlesGroup);
		return tg;
	}

	private Pellet3D createNormalPellet3D(Vector2i tile, PhongMaterial pelletMaterial) {
		var pellet3D = new Pellet3D(1.0);
		pellet3D.getRoot().setMaterial(pelletMaterial);
		pellet3D.setTile(tile);
		return pellet3D;
	}

	private Energizer3D createEnergizer3D(World world, Vector2i tile, PhongMaterial pelletMaterial) {
		var energizer3D = new Energizer3D(3.5);
		energizer3D.getRoot().setMaterial(pelletMaterial);
		energizer3D.setTile(tile);
		var eatenAnimation = new SquirtingAnimation(world, particlesGroup, energizer3D.getRoot());
		energizer3D.setEatenAnimation(eatenAnimation);
		return energizer3D;
	}

	private Pac3D createPac3D() {
		var pac3D = new Pac3D(level.pac());
		pac3D.init();
		pacLight = new PointLight();
		pacLight.setColor(Color.rgb(255, 255, 0, 0.25));
		pacLight.setMaxRange(8 * TS);
		pacLight.setTranslateZ(0);
		pac3D.getRoot().getChildren().add(pacLight);
		pac3D.drawModePy.bind(Env.ThreeD.drawModePy);
		pac3D.lightOnPy.bind(Env.ThreeD.pacLightedPy);
		return pac3D;
	}

	private Ghost3D createGhost3D(Ghost ghost) {
		var ghost3D = new Ghost3D(ghost, ArcadeTheme.GHOST_COLORS[ghost.id()]);
		ghost3D.init(level);
		ghost3D.drawModePy.bind(drawModePy);
		return ghost3D;
	}

	private Bonus3D createBonus3D(Bonus bonus, Rendering2D r2D) {
		if (r2D instanceof SpritesheetGameRenderer sgr) {
			var symbolSprite = sgr.bonusSymbolRegion(bonus.symbol());
			var pointsSprite = sgr.bonusValueRegion(bonus.symbol());
			return new Bonus3D(bonus, sgr.image(symbolSprite), sgr.image(pointsSprite));
		}
		throw new UnsupportedOperationException(); // TODO make this work for other renderers too
	}

	private LivesCounter3D createLivesCounter3D() {
		var facingRight = level.game().variant() == GameVariant.MS_PACMAN;
		var counter3D = new LivesCounter3D(facingRight);
		counter3D.setPosition(2 * TS, TS, -HTS);
		counter3D.getRoot().setVisible(level.game().hasCredit());
		counter3D.drawModePy.bind(drawModePy);
		return counter3D;
	}

	private LevelCounter3D createLevelCounter3D(Rendering2D r2D) {
		var rightPosition = new Vector2f((level.world().numCols() - 2) * TS, TS);
		if (r2D instanceof SpritesheetGameRenderer sgr) {
			var symbolImages = level.game().levelCounter().stream().map(sgr::bonusSymbolRegion).map(sgr::image)
					.toArray(Image[]::new);
			return new LevelCounter3D(symbolImages, rightPosition);
		} else {
			throw new UnsupportedOperationException(); // TODO implement fom non spritesheet-based renderer
		}
	}

	public Group getRoot() {
		return root;
	}

	public void update() {
		pac3D.update(level);
		pacLight.setLightOn(pac3D.lightOnPy.get() && level.pac().isVisible() && !level.pac().isDead());
		Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.update(level));
		bonus3D.update();
		updateHouseLightingState();
		updateDoorState();
		livesCounter3D.update(level.game().isOneLessLifeDisplayed() ? level.game().lives() - 1 : level.game().lives());
		scores3D.update(level);
		if (level.game().hasCredit()) {
			scores3D.setShowPoints(true);
		} else {
			scores3D.setShowText(Color.RED, "GAME OVER!");
		}
	}

	public World3D world3D() {
		return world3D;
	}

	public Pac3D pac3D() {
		return pac3D;
	}

	public Ghost3D[] ghosts3D() {
		return ghosts3D;
	}

	public Bonus3D bonus3D() {
		return bonus3D;
	}

	public Scores3D scores3D() {
		return scores3D;
	}

	public void eat(Eatable3D eatable3D) {
		if (eatable3D instanceof Energizer3D energizer3D) {
			energizer3D.stopPumping();
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

	private void updateHouseLightingState() {
		boolean anyGhostInHouse = level.ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
				.count() > 0;
		world3D.houseLighting().setLightOn(anyGhostInHouse);
	}

	private void updateDoorState() {
		var door = level.world().ghostHouse().door();
		var accessGranted = isAccessGranted(level.ghosts(), door.entryPosition());
		world3D.doorWings3D().forEach(door3D -> door3D.setOpen(accessGranted));
	}

	private boolean isAccessGranted(Stream<Ghost> ghosts, Vector2f doorPosition) {
		return ghosts.anyMatch(ghost -> isAccessGranted(ghost, doorPosition));
	}

	private boolean isAccessGranted(Ghost ghost, Vector2f doorPosition) {
		return ghost.isVisible() && ghost.is(RETURNING_TO_HOUSE, ENTERING_HOUSE, LEAVING_HOUSE)
				&& inDoorDistance(ghost, doorPosition);
	}

	private boolean inDoorDistance(Ghost ghost, Vector2f doorPosition) {
		return ghost.position().euclideanDistance(doorPosition) <= 1.5 * TS;
	}
}