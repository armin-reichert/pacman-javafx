/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.ActionHandler;
import de.amr.games.pacman.ui.fx.app.SoundHandler;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.scene.Node;
import javafx.scene.Scene;

import java.util.Optional;

/**
 * Common interface of all game scenes (2D and 3D).
 * 
 * @author Armin Reichert
 */
public interface GameScene extends GameEventListener {

	boolean isScoreVisible();

	void setScoreVisible(boolean visible);

	boolean isCreditVisible();

	void setCreditVisible(boolean visible);

	/**
	 * Called when the scene becomes the current one.
	 */
	default void init() {
		// empty default
	}

	/**
	 * Called when the scene needs to be updated.
	 */
	void update();

	/**
	 * Called when the scene ends and gets replaced by another scene.
	 */
	default void end() {
		// empty default
	}

	/**
	 * @return the root of the game scene (used to embed the scene into the scene graph)
	 */
	Node root();

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

	Theme getTheme();

	void setTheme(Theme theme);

	Spritesheet getSpritesheet();

	void setSpritesheet(Spritesheet spritesheet);

	SoundHandler getSoundHandler();

	void setSoundHandler(SoundHandler soundHandler);

	Optional<ActionHandler> actionHandler();

	void setActionHandler(ActionHandler actionHandler);

	default GameController gameController() {
		return GameController.it();
	}

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