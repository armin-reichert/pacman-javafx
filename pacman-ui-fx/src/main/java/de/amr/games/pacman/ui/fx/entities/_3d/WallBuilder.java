package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.model.world.WallMap;
import de.amr.games.pacman.model.world.WallScanner;
import de.amr.games.pacman.ui.fx.Env;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * Creates walls around inaccessible world areas.
 * 
 * @author Armin Reichert
 */
public class WallBuilder {

	private boolean[][] wallInfo;
	private List<Node> walls;
	private PhongMaterial material;
	private double wallHeight = PacManGameWorld.HTS;

	public WallBuilder() {
		material = new PhongMaterial();
	}

	public List<Node> getWalls() {
		return Collections.unmodifiableList(walls);
	}

	public void setWallHeight(double height) {
		wallHeight = height;
	}

	public void setWallMaterial(PhongMaterial material) {
		this.material = material;
	}

	public List<Node> build(PacManGameWorld world, int resolution) {
		long start, end;
		double millis;
		walls = new ArrayList<>();

		Optional<WallMap> wallMap = world.getWallMap(resolution);
		if (wallMap.isPresent()) {
			wallInfo = wallMap.get().wallInfo();
		} else {
			WallScanner scanner = new WallScanner(resolution);
			start = System.nanoTime();
			wallInfo = scanner.scan(world);
			end = System.nanoTime();
			millis = (end - start) * 1e-6;
			log("WallBuilder: scanning world took %.0f milliseconds", millis);
		}

		double blockSize = TS / resolution;
		start = System.nanoTime();
		createWalls(world, blockSize);
		end = System.nanoTime();
		millis = (end - start) * 1e-6;
		log("WallBuilder: building walls took %.0f milliseconds", millis);

		return getWalls();
	}

	private void addWall(int leftX, int topY, int numBlocksX, int numBlocksY, double blockSize) {
		if (numBlocksX == 1 && numBlocksY == 1) {
			return; // ignore 1x1 walls because they could be part of a larger wall in other orientation
		}
		Box wall = new Box(numBlocksX * blockSize, numBlocksY * blockSize, wallHeight);
		wall.setMaterial(material);
		wall.setTranslateX(leftX * blockSize + (numBlocksX - 4) * 0.5 * blockSize);
		wall.setTranslateY(topY * blockSize + (numBlocksY - 4) * 0.5 * blockSize);
		wall.setTranslateZ(1.5);
		wall.drawModeProperty().bind(Env.$drawMode);
		walls.add(wall);
	}

	private void createWalls(PacManGameWorld world, double blockSize) {
		int xSize = wallInfo[0].length, ySize = wallInfo.length;

		// horizontal
		for (int y = 0; y < ySize; ++y) {
			int leftX = -1;
			int sizeX = 0;
			for (int x = 0; x < xSize; ++x) {
				if (wallInfo[y][x]) {
					if (leftX == -1) {
						leftX = x;
						sizeX = 1;
					} else {
						sizeX++;
					}
				} else {
					if (leftX != -1) {
						addWall(leftX, y, sizeX, 1, blockSize);
						leftX = -1;
					}
				}
				if (x == xSize - 1 && leftX != -1) {
					addWall(leftX, y, sizeX, 1, blockSize);
					leftX = -1;
				}
			}
			if (y == ySize - 1 && leftX != -1) {
				addWall(leftX, y, sizeX, 1, blockSize);
				leftX = -1;
			}
		}

		// vertical
		for (int x = 0; x < xSize; ++x) {
			int topY = -1;
			int sizeY = 0;
			for (int y = 0; y < ySize; ++y) {
				if (wallInfo[y][x]) {
					if (topY == -1) {
						topY = y;
						sizeY = 1;
					} else {
						sizeY++;
					}
				} else {
					if (topY != -1) {
						addWall(x, topY, 1, sizeY, blockSize);
						topY = -1;
					}
				}
				if (y == ySize - 1 && topY != -1) {
					addWall(x, topY, 1, sizeY, blockSize);
					topY = -1;
				}
			}
			if (x == xSize - 1 && topY != -1) {
				addWall(x, topY, 1, sizeY, blockSize);
				topY = -1;
			}
		}
	}
}