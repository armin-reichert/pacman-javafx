/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene;

import java.util.Optional;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.PacManGames2dUI;
import javafx.scene.Scene;
import javafx.scene.layout.Region;

/**
 * Common interface of all game scenes (2D and 3D).
 * 
 * @author Armin Reichert
 */
public interface GameScene extends GameEventListener {

	void setParentScene(Scene parentScene);

	/**
	 * Called when the scene becomes the current one.
	 */
	default void init() {
		// empty default
	}

	/**
	 * Called when the scene needs to be updated.
	 */
	default void update() {
		// empty default
	}

	/**
	 * Called when the scene needs to be rendered.
	 */
	default void render() {
		// empty default
	}

	/**
	 * Called when the scene ends and gets replaced by another scene.
	 */
	default void end() {
		// empty default
	}

	/**
	 * @return the root of the game scene (used to embed the scene into the scene graph)
	 */
	Region root();

	/**
	 * @return if this is a scene with 3D content
	 */
	boolean is3D();

	/**
	 * Called when scene variants for 2D and 3D exist and variant changes between 2D and 3D.
	 */
	default void onSceneVariantSwitch() {
		// empty default
	}

	/**
	 * Handles keyboard input.
	 */
	default void handleKeyboardInput() {
		// empty default
	}

	void setContext(GameController gameController, PacManGames2dUI ui);

	PacManGames2dUI ui();

	boolean isScoreVisible();

	void setScoreVisible(boolean visible);

	boolean isCreditVisible();

	void setCreditVisible(boolean visible);

	GameController gameController();

	default GameModel game() {
		return gameController().game();
	}

	default GameState state() {
		return gameController().state();
	}

	default Optional<World> world() {
		return game().level().map(GameLevel::world);
	}
}