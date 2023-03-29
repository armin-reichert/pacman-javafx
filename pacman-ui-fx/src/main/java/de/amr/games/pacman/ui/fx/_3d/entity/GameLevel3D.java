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

import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.common.ArcadeTheme;
import de.amr.games.pacman.ui.fx._2d.rendering.common.MazeColoring;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpritesheetRenderer;
import de.amr.games.pacman.ui.fx._3d.animation.FoodOscillation;
import de.amr.games.pacman.ui.fx._3d.animation.SquirtingAnimation;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
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

	private final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
	private final BooleanProperty eatenAnimationEnabledPy = new SimpleBooleanProperty(this, "eatenAnimationEnabled");
	private final BooleanProperty pacLightOnPy = new SimpleBooleanProperty(this, "pacLightOn", true);

	private final GameLevel level;
	private final Group root = new Group();
	private final Group particlesGroup = new Group();
	private final World3D world3D;
	private final Pac3D pac3D;
	private final Ghost3D[] ghosts3D;
	private final Bonus3D bonus3D;
	private final LevelCounter3D levelCounter3D;
	private final LivesCounter3D livesCounter3D;
	private final Scores3D scores3D;
	private final List<Eatable3D> eatables = new ArrayList<>();
	private FoodOscillation foodOscillation = new FoodOscillation(eatables);
	private PointLight light;

	public GameLevel3D(GameLevel level, Rendering2D r2D) {
		this.level = level;
		int mazeNumber = level.game().mazeNumber(level.number());

		world3D = createWorld3D(level.world(), r2D.mazeColoring(mazeNumber));
		pac3D = createPac3D();
		light = createPacLight();
		pacLightOnPy.bind(Env.d3pacLightedPy);

		ghosts3D = level.ghosts().map(this::createGhost3D).toArray(Ghost3D[]::new);
		bonus3D = createBonus3D(level.bonus(), r2D);
		levelCounter3D = createLevelCounter3D(r2D);
		livesCounter3D = createLivesCounter3D();
		scores3D = new Scores3D(r2D.screenFont(TS));

		root.getChildren().add(scores3D.getRoot());
		root.getChildren().add(levelCounter3D.getRoot());
		root.getChildren().add(livesCounter3D.getRoot());
		root.getChildren().add(bonus3D.getRoot());
		root.getChildren().add(pac3D.getRoot());
		Arrays.stream(ghosts3D).map(Ghost3D::getRoot).forEach(root.getChildren()::add);
		// Note: world/ghosthouse must be added after the guys if ghosthouse uses transparent material!
		root.getChildren().add(world3D.getRoot());
		root.getChildren().addAll(particlesGroup, light);

		drawModePy.bind(Env.d3drawModePy);
		eatenAnimationEnabledPy.bind(Env.d3energizerEatenAnimationEnabledPy);
		world3D.floorColorPy.bind(Env.d3floorColorPy);
		world3D.floorTexturePy.bind(Env.d3floorTexturePy);
		world3D.wallHeightPy.bind(Env.d3mazeWallHeightPy);
		world3D.wallThicknessPy.bind(Env.d3mazeWallThicknessPy);

		var keys = ResourceMgr.floorTextureKeys();
		var key = keys[U.randomInt(1, keys.length)]; // index 0 = No Texture
		Env.d3floorTexturePy.set(key);
	}

	private World3D createWorld3D(World world, MazeColoring mazeColoring) {
		var w3D = new World3D(world, mazeColoring);
		var foodMaterial = ResourceMgr.coloredMaterial(mazeColoring.foodColor());
		world.tiles().filter(world::isFoodTile).filter(not(world::containsEatenFood)).forEach(tile -> {
			var eatable3D = world.isEnergizerTile(tile) ? createEnergizer3D(world, tile, foodMaterial)
					: createNormalPellet3D(tile, foodMaterial);
			eatables.add(eatable3D);
			w3D.addFood(eatable3D);
		});
		w3D.drawModePy.bind(drawModePy);
		return w3D;
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
		var p3D = switch (level.game().variant()) {
		case MS_PACMAN -> new Pac3D(level.pac(),
				Pac3D.createMsPacMan(ArcadeTheme.HEAD_COLOR, ArcadeTheme.EYES_COLOR_MS_PACMAN, ArcadeTheme.PALATE_COLOR),
				ArcadeTheme.HEAD_COLOR);
		case PACMAN -> new Pac3D(level.pac(),
				Pac3D.createPacMan(ArcadeTheme.HEAD_COLOR, ArcadeTheme.EYES_COLOR_PACMAN, ArcadeTheme.PALATE_COLOR),
				ArcadeTheme.HEAD_COLOR);
		default -> throw new IllegalArgumentException("Unknown game variant: %s".formatted(level.game().variant()));
		};
		p3D.init();
		p3D.drawModePy.bind(Env.d3drawModePy);
		return p3D;
	}

	private PointLight createPacLight() {
		light = new PointLight();
		light.setColor(Color.rgb(255, 255, 0, 0.25));
		light.setMaxRange(2 * TS);
		light.translateXProperty().bind(pac3D.getRoot().translateXProperty());
		light.translateYProperty().bind(pac3D.getRoot().translateYProperty());
		light.setTranslateZ(-TS);
		return light;
	}

	private Ghost3D createGhost3D(Ghost ghost) {
		var ghost3D = new Ghost3D(ghost, ArcadeTheme.GHOST_COLORS[ghost.id()]);
		ghost3D.init(level);
		ghost3D.drawModePy.bind(drawModePy);
		return ghost3D;
	}

	private Bonus3D createBonus3D(Bonus bonus, Rendering2D r2D) {
		if (r2D instanceof SpritesheetRenderer sgr) {
			var symbolSprite = sgr.bonusSymbolRegion(bonus.symbol());
			var pointsSprite = sgr.bonusValueRegion(bonus.symbol());
			return new Bonus3D(bonus, sgr.image(symbolSprite), sgr.image(pointsSprite));
		}
		throw new UnsupportedOperationException(); // TODO make this work for other renderers too
	}

	private LivesCounter3D createLivesCounter3D() {
		var counter3D = new LivesCounter3D(level.game().variant(), 5);
		counter3D.setPosition(2 * TS, TS, -HTS);
		counter3D.getRoot().setVisible(level.game().hasCredit());
		counter3D.drawModePy.bind(drawModePy);
		return counter3D;
	}

	private LevelCounter3D createLevelCounter3D(Rendering2D r2D) {
		var rightPosition = new Vector2f((level.world().numCols() - 2) * TS, TS);
		if (r2D instanceof SpritesheetRenderer sgr) {
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

	public World3D world3D() {
		return world3D;
	}

	public FoodOscillation foodOscillation() {
		return foodOscillation;
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

	public void update() {
		pac3D.update(level);
		Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.update(level));
		bonus3D.update();
		livesCounter3D.update(level.game().isOneLessLifeDisplayed() ? level.game().lives() - 1 : level.game().lives());
		scores3D.update(level);
		if (level.game().hasCredit()) {
			scores3D.setShowPoints(true);
		} else {
			scores3D.setShowText(Color.RED, "GAME OVER!");
		}
		updatePacLightingState();
		updateHouseLightingState();
		updateDoorState();
	}

	private void updatePacLightingState() {
		boolean hasPower = level.pac().powerTimer().isRunning();
		light.setLightOn(pacLightOnPy.get() && level.pac().isVisible() && !level.pac().isDead() && hasPower);
		var range = !hasPower ? 0 : level.pac().isPowerFading(level) ? 4 : 8;
		light.setMaxRange(range * TS);
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
				.filter(Ghost::isVisible).count() > 0;
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