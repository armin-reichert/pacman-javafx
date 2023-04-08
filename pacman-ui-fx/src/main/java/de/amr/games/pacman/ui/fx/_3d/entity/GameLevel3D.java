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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.U;
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
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpritesheetRenderer;
import de.amr.games.pacman.ui.fx._3d.animation.FoodOscillation;
import de.amr.games.pacman.ui.fx._3d.animation.SquirtingAnimation;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.SequentialTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.Translate;

/**
 * @author Armin Reichert
 */
public class GameLevel3D {

	private final BooleanProperty energizerExplodesPy = new SimpleBooleanProperty(this, "energizerExplodes", true);

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
	private final FoodOscillation foodOscillation = new FoodOscillation(eatables);

	public GameLevel3D(GameLevel level, Rendering2D r2D) {
		this.level = level;

		int mazeNumber = level.game().mazeNumber(level.number());

		world3D = createWorld3D(r2D.mazeColoring(mazeNumber));
		pac3D = level.game().variant() == GameVariant.MS_PACMAN ? createMsPacMan3D() : createPacMan3D();
		ghosts3D = level.ghosts().map(this::createGhost3D).toArray(Ghost3D[]::new);
		bonus3D = createBonus3D(level.bonus(), r2D);
		levelCounter3D = createLevelCounter3D(r2D);
		livesCounter3D = createLivesCounter3D();
		scores3D = new Scores3D(r2D.screenFont(8));

		root.getChildren().add(scores3D.getRoot());
		root.getChildren().add(levelCounter3D.getRoot());
		root.getChildren().add(livesCounter3D.getRoot());
		root.getChildren().add(bonus3D.getRoot());
		root.getChildren().add(pac3D.getRoot());
		root.getChildren().add(pac3D.getLight());
		root.getChildren().add(ghosts3D[0].getRoot());
		root.getChildren().add(ghosts3D[1].getRoot());
		root.getChildren().add(ghosts3D[2].getRoot());
		root.getChildren().add(ghosts3D[3].getRoot());
		// Note: world/ghosthouse must be added after the guys if ghosthouse uses transparent material!
		root.getChildren().add(world3D.getRoot());
		root.getChildren().addAll(particlesGroup);

		pac3D.drawModePy.bind(Env.d3_drawModePy);
		pac3D.lightedPy.bind(Env.d3_pacLightedPy);
		ghosts3D[Ghost.ID_RED_GHOST].drawModePy.bind(Env.d3_drawModePy);
		ghosts3D[Ghost.ID_PINK_GHOST].drawModePy.bind(Env.d3_drawModePy);
		ghosts3D[Ghost.ID_CYAN_GHOST].drawModePy.bind(Env.d3_drawModePy);
		ghosts3D[Ghost.ID_ORANGE_GHOST].drawModePy.bind(Env.d3_drawModePy);
		energizerExplodesPy.bind(Env.d3_energizerExplodesPy);
		world3D.drawModePy.bind(Env.d3_drawModePy);
		world3D.floorColorPy.bind(Env.d3_floorColorPy);
		world3D.floorTexturePy.bind(Env.d3_floorTexturePy);
		world3D.wallHeightPy.bind(Env.d3_mazeWallHeightPy);
		world3D.wallThicknessPy.bind(Env.d3_mazeWallThicknessPy);
		livesCounter3D.drawModePy.bind(Env.d3_drawModePy);

		// lift guys a bit over floor
		var liftOverFloor = new Translate(0, 0, -1);
		pac3D.getRoot().getTransforms().add(liftOverFloor);
		Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.getRoot().getTransforms().add(liftOverFloor));

		selectRandomFloorTexture();
	}

	private void selectRandomFloorTexture() {
		var keys = ResourceMgr.floorTextureKeys();
		var key = keys[U.randomInt(1, keys.length)]; // index 0 = No Texture
		Env.d3_floorTexturePy.set(key);
	}

	private World3D createWorld3D(MazeColoring mazeColoring) {
		var w3D = new World3D(level.world(), mazeColoring);
		var foodMaterial = ResourceMgr.coloredMaterial(mazeColoring.foodColor());
		level.world().tilesContainingFood().forEach(tile -> {
			var eatable3D = level.world().isEnergizerTile(tile)//
					? createEnergizer3D(level.world(), tile, foodMaterial)//
					: createNormalPellet3D(tile, foodMaterial);
			eatables.add(eatable3D);
			w3D.addFood(eatable3D);
		});
		return w3D;
	}

	private Pellet3D createNormalPellet3D(Vector2i tile, PhongMaterial material) {
		var pellet3D = new Pellet3D(1.0);
		pellet3D.getRoot().setMaterial(material);
		pellet3D.setTile(tile);
		return pellet3D;
	}

	private Energizer3D createEnergizer3D(World world, Vector2i tile, PhongMaterial material) {
		var energizer3D = new Energizer3D(3.5);
		energizer3D.getRoot().setMaterial(material);
		energizer3D.setTile(tile);
		var eatenAnimation = new SquirtingAnimation(world, particlesGroup, energizer3D.getRoot());
		energizer3D.setEatenAnimation(eatenAnimation);
		return energizer3D;
	}

	private Pac3D createPacMan3D() {
		var shape = PacShape3D.createPacManShape(9, ArcadeTheme.HEAD_COLOR, ArcadeTheme.EYES_COLOR_PACMAN,
				ArcadeTheme.PALATE_COLOR);
		return new Pac3D(level, level.pac(), shape, ArcadeTheme.HEAD_COLOR);
	}

	private Pac3D createMsPacMan3D() {
		var shape = PacShape3D.createMsPacManShape(9, ArcadeTheme.HEAD_COLOR, ArcadeTheme.EYES_COLOR_MS_PACMAN,
				ArcadeTheme.PALATE_COLOR, ArcadeTheme.HAIRBOW_COLOR, ArcadeTheme.HAIRBOW_PEARLS_COLOR);
		return new Pac3D(level, level.pac(), shape, ArcadeTheme.HEAD_COLOR);
	}

	private Ghost3D createGhost3D(Ghost ghost) {
		return new Ghost3D(level, ghost, ArcadeTheme.GHOST_COLORS[ghost.id()]);
	}

	private Bonus3D createBonus3D(Bonus bonus, Rendering2D r2D) {
		if (r2D instanceof SpritesheetRenderer sgr) {
			var symbolSprite = sgr.bonusSymbolRegion(bonus.symbol());
			var pointsSprite = sgr.bonusValueRegion(bonus.symbol());
			return new Bonus3D(level, bonus, sgr.image(symbolSprite), sgr.image(pointsSprite));
		}
		throw new UnsupportedOperationException(); // TODO make this work for other renderers too
	}

	private LivesCounter3D createLivesCounter3D() {
		var counter3D = new LivesCounter3D(level.game().variant(), 5);
		counter3D.setPosition(2 * TS, TS, -HTS);
		counter3D.getRoot().setVisible(level.game().hasCredit());
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
		pac3D.update();
		Stream.of(ghosts3D).forEach(Ghost3D::update);
		bonus3D.update();
		// TODO get rid of this
		int numLivesShown = level.game().isOneLessLifeDisplayed() ? level.game().lives() - 1 : level.game().lives();
		livesCounter3D.update(numLivesShown);
		scores3D.update(level);
		if (level.game().hasCredit()) {
			scores3D.setShowPoints(true);
		} else {
			scores3D.setShowText(Color.RED, "GAME OVER!");
		}
		updateHouseState();
	}

	public void eat(Eatable3D eatable3D) {
		if (eatable3D instanceof Energizer3D energizer3D) {
			energizer3D.stopPumping();
		}
		// Delay hiding of pellet for some milliseconds because in case the player approaches the pellet from the right,
		// the pellet disappears too early (collision by same tile in game model is too simplistic).
		var delayHiding = Ufx.afterSeconds(0.05, () -> eatable3D.getRoot().setVisible(false));
		var eatenAnimation = eatable3D.getEatenAnimation();
		if (eatenAnimation.isPresent() && energizerExplodesPy.get()) {
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

	private void updateHouseState() {
		boolean isGhostNearHouse = level.ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
				.anyMatch(Ghost::isVisible);
		boolean accessGranted = isAccessGranted(level.ghosts(), level.world().ghostHouse().door().entryPosition());
		world3D.houseLighting().setLightOn(isGhostNearHouse);
		world3D.doorWings3D().forEach(door3D -> door3D.setOpen(accessGranted));
	}

	private boolean isAccessGranted(Stream<Ghost> ghosts, Vector2f doorPosition) {
		return ghosts.anyMatch(ghost -> ghost.isVisible() && ghost.is(RETURNING_TO_HOUSE, ENTERING_HOUSE, LEAVING_HOUSE)
				&& ghost.position().euclideanDistance(doorPosition) <= 1.5 * TS);
	}
}