package de.amr.games.pacman.ui.fx.v3d.entity;

import javafx.scene.PointLight;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class Pac3DLight extends PointLight {
	private final Pac3D pac3D;

	public Pac3DLight(Pac3D pac3D) {
		checkNotNull(pac3D);
		this.pac3D = pac3D;
		setColor(Color.rgb(255, 255, 0, 0.75));
		setMaxRange(2 * TS);
		translateXProperty().bind(pac3D.position().xProperty());
		translateYProperty().bind(pac3D.position().yProperty());
		setTranslateZ(-10);
	}

	public void update() {
		var pac = pac3D.pac();
		boolean isVisible = pac.isVisible();
		boolean isAlive = !pac.isDead();
		boolean hasPower = pac.powerTimer().isRunning();
		double radius = 0;
		if (pac.powerTimer().duration() > 0) {
			double t = (double) pac.powerTimer().remaining() / pac.powerTimer().duration();
			radius = t * 6 * TS;
		}
		setMaxRange(hasPower ? 2 * TS + radius : 0);
		setLightOn(pac3D.lightedPy.get() && isVisible && isAlive && hasPower);
	}
}
