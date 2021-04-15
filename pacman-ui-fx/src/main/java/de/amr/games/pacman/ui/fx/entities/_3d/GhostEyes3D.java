package de.amr.games.pacman.ui.fx.entities._3d;

import java.util.function.Supplier;

import de.amr.games.pacman.ui.fx.model3D.GianmarcosModel3D;
import javafx.scene.Group;
import javafx.scene.Node;

public class GhostEyes3D implements Supplier<Node> {

	private final Group root;

	public GhostEyes3D() {
		root = GianmarcosModel3D.IT.createGhostEyes();
	}

	@Override
	public Node get() {
		return root;
	}

}
