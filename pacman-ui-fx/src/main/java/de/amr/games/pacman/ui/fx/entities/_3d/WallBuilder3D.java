package de.amr.games.pacman.ui.fx.entities._3d;

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
public class WallBuilder3D {

	private byte[][] wallInfo;
	private List<Node> walls;
	private PhongMaterial wallMaterial;
	private PhongMaterial topMaterial;

	private double wallHeight = PacManGameWorld.HTS;

	public WallBuilder3D() {
		wallMaterial = new PhongMaterial();
	}

	public List<Node> getWalls() {
		return Collections.unmodifiableList(walls);
	}

	public void setWallHeight(double height) {
		wallHeight = height;
	}

	public void setWallMaterial(PhongMaterial material) {
		this.wallMaterial = material;
	}

	public void setTopMaterial(PhongMaterial material) {
		this.topMaterial = material;
	}

	public List<Node> build(PacManGameWorld world, int resolution) {
		walls = new ArrayList<>();
		Optional<WallMap> map = world.getWallMap(resolution);
		if (map.isPresent()) {
			wallInfo = map.get().info();
		} else {
			WallScanner scanner = new WallScanner(resolution);
			wallInfo = scanner.scan(world);
		}
		double blockSize = TS / resolution;
		createWalls(world, blockSize);
		return getWalls();
	}

	private void addBlock(int leftX, int topY, int numBlocksX, int numBlocksY, double blockSize) {
		Box base = new Box(numBlocksX * blockSize, numBlocksY * blockSize, wallHeight);
		base.setMaterial(wallMaterial);
		base.setTranslateX(leftX * blockSize + numBlocksX * 0.5 * blockSize);
		base.setTranslateY(topY * blockSize + numBlocksY * 0.5 * blockSize);
		base.setTranslateZ(1.5);
		base.drawModeProperty().bind(Env.$drawMode3D);
		walls.add(base);

		Box top = new Box(numBlocksX * blockSize, numBlocksY * blockSize, 0.2);
		top.setMaterial(topMaterial);
		top.setTranslateX(leftX * blockSize + numBlocksX * 0.5 * blockSize);
		top.setTranslateY(topY * blockSize + numBlocksY * 0.5 * blockSize);
		top.setTranslateZ(-0.5);
		top.drawModeProperty().bind(Env.$drawMode3D);
		walls.add(top);
	}

	// TODO I need a half cylinder or a special corner shape for smooth corners
	private void addCorner(int x, int y, double blockSize) {
		Box base = new Box(blockSize, blockSize, wallHeight);
		base.setMaterial(wallMaterial);
		base.setTranslateX(x * blockSize + 0.5 * blockSize);
		base.setTranslateY(y * blockSize + 0.5 * blockSize);
		base.setTranslateZ(1.5);
		base.drawModeProperty().bind(Env.$drawMode3D);
		walls.add(base);

		Box top = new Box(blockSize, blockSize, 0.2);
		top.setMaterial(topMaterial);
		top.setTranslateX(x * blockSize + 0.5 * blockSize);
		top.setTranslateY(y * blockSize + 0.5 * blockSize);
		top.setTranslateZ(-0.5);
		top.drawModeProperty().bind(Env.$drawMode3D);
		walls.add(top);
	}

	private void createWalls(PacManGameWorld world, double blockSize) {
		int xSize = wallInfo[0].length, ySize = wallInfo.length;

		// horizontal
		for (int y = 0; y < ySize; ++y) {
			int leftX = -1;
			int sizeX = 0;
			for (int x = 0; x < xSize; ++x) {
				if (wallInfo[y][x] == WallMap.HORIZONTAL) {
					if (leftX == -1) {
						leftX = x;
						sizeX = 1;
					} else {
						sizeX++;
					}
				} else {
					if (leftX != -1) {
						addBlock(leftX, y, sizeX, 1, blockSize);
						leftX = -1;
					}
				}
				if (x == xSize - 1 && leftX != -1) {
					addBlock(leftX, y, sizeX, 1, blockSize);
					leftX = -1;
				}
			}
			if (y == ySize - 1 && leftX != -1) {
				addBlock(leftX, y, sizeX, 1, blockSize);
				leftX = -1;
			}
		}

		// vertical
		for (int x = 0; x < xSize; ++x) {
			int topY = -1;
			int sizeY = 0;
			for (int y = 0; y < ySize; ++y) {
				if (wallInfo[y][x] == WallMap.VERTICAL) {
					if (topY == -1) {
						topY = y;
						sizeY = 1;
					} else {
						sizeY++;
					}
				} else {
					if (topY != -1) {
						addBlock(x, topY, 1, sizeY, blockSize);
						topY = -1;
					}
				}
				if (y == ySize - 1 && topY != -1) {
					addBlock(x, topY, 1, sizeY, blockSize);
					topY = -1;
				}
			}
			if (x == xSize - 1 && topY != -1) {
				addBlock(x, topY, 1, sizeY, blockSize);
				topY = -1;
			}
		}

		// corners
		for (int y = 0; y < ySize; ++y) {
			for (int x = 0; x < xSize; ++x) {
				if (wallInfo[y][x] == WallMap.CORNER) {
					addCorner(x, y, blockSize);
				}
			}
		}
	}
}