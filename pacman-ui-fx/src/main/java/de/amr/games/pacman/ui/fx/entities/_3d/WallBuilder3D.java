package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.model.world.WallMap;
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

	private WallMap wallMap;
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

	public void setBaseMaterial(PhongMaterial material) {
		this.wallMaterial = material;
	}

	public void setTopMaterial(PhongMaterial material) {
		this.topMaterial = material;
	}

	public List<Node> build(PacManGameWorld world, int resolution) {
		wallMap = WallMap.build(resolution, world);
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

		double topHeight = 0.1;
		Box top = new Box(numBlocksX * blockSize, numBlocksY * blockSize, topHeight);
		top.setMaterial(topMaterial);
		top.setTranslateX(leftX * blockSize + numBlocksX * 0.5 * blockSize);
		top.setTranslateY(topY * blockSize + numBlocksY * 0.5 * blockSize);
		top.setTranslateZ(base.getTranslateZ() - 0.5 * wallHeight - topHeight - 0.1);
		top.drawModeProperty().bind(Env.$drawMode3D);
		walls.add(top);
	}

	// TODO I need a half cylinder or a special corner shape for smooth corners
	private void addCorner(int x, int y, double blockSize) {
		addBlock(x, y, 1, 1, blockSize);
	}

	private void createWalls(PacManGameWorld world, double blockSize) {
		walls = new ArrayList<>();
		// horizontal
		for (int y = 0; y < wallMap.sizeY(); ++y) {
			int leftX = -1;
			int sizeX = 0;
			for (int x = 0; x < wallMap.sizeX(); ++x) {
				if (wallMap.get(x, y) == WallMap.HORIZONTAL) {
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
				if (x == wallMap.sizeX() - 1 && leftX != -1) {
					addBlock(leftX, y, sizeX, 1, blockSize);
					leftX = -1;
				}
			}
			if (y == wallMap.sizeY() - 1 && leftX != -1) {
				addBlock(leftX, y, sizeX, 1, blockSize);
				leftX = -1;
			}
		}

		// vertical
		for (int x = 0; x < wallMap.sizeX(); ++x) {
			int topY = -1;
			int sizeY = 0;
			for (int y = 0; y < wallMap.sizeY(); ++y) {
				if (wallMap.get(x, y) == WallMap.VERTICAL) {
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
				if (y == wallMap.sizeY() - 1 && topY != -1) {
					addBlock(x, topY, 1, sizeY, blockSize);
					topY = -1;
				}
			}
			if (x == wallMap.sizeX() - 1 && topY != -1) {
				addBlock(x, topY, 1, sizeY, blockSize);
				topY = -1;
			}
		}

		// corners
		for (int y = 0; y < wallMap.sizeY(); ++y) {
			for (int x = 0; x < wallMap.sizeX(); ++x) {
				if (wallMap.get(x, y) == WallMap.CORNER) {
					addCorner(x, y, blockSize);
				}
			}
		}
	}
}