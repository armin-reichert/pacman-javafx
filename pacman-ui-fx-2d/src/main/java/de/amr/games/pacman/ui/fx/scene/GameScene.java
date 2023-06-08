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
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.util.Duration;

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

	public void setContext(GameController gameController, PacManGames2dUI ui);

	public PacManGames2dUI ui();

	public boolean isScoreVisible();

	public void setScoreVisible(boolean visible);

	public boolean isCreditVisible();

	public void setCreditVisible(boolean visible);

	public GameController gameController();

	default GameModel game() {
		return gameController().game();
	}

	default GameVariant gameVariant() {
		return game().variant();
	}

	default GameState state() {
		return gameController().state();
	}

	default boolean hasCredit() {
		return game().hasCredit();
	}

	default Optional<GameLevel> level() {
		return game().level();
	}

	default Optional<World> world() {
		return level().map(GameLevel::world);
	}

	/**
	 * Locks the current game state by setting an indefinite timer duration.
	 */
	default void lockGameState() {
		state().timer().resetIndefinitely();
	}

	/**
	 * Unlocks the current game state by forcing the timer to expire.
	 */
	default void unlockGameState() {
		state().timer().expire();
	}

	/**
	 * Locks the current game state, waits given seconds, plays given animations and unlocks the state when the animations
	 * have finished.
	 */
	default void lockStateAndPlayAfterSeconds(double afterSeconds, Animation... animations) {
		lockGameState();
		var animationSequence = new SequentialTransition(animations);
		if (afterSeconds > 0) {
			animationSequence.setDelay(Duration.seconds(afterSeconds));
		}
		animationSequence.setOnFinished(e -> unlockGameState());
		animationSequence.play();
	}

	/**
	 * Locks the current game states, waits given number of seconds and unlocks the state.
	 * 
	 * @param seconds seconds to wait before unlock
	 */
	default void waitSeconds(double seconds) {
		lockGameState();
		var pause = Ufx.pauseSeconds(seconds);
		pause.setOnFinished(e -> unlockGameState());
		pause.play();
	}

}