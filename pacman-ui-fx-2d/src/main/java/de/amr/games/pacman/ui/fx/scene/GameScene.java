/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene;

import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

/**
 * Common interface of all game scenes (2D and 3D).
 * 
 * @author Armin Reichert
 */
public interface GameScene extends GameEventListener {

	/**
	 * @return the scene context (game controller, game model, game level, rendering, sounds).
	 */
	GameSceneContext context();

	void setContext(GameSceneContext context);

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
     * @return the container of the game scene
     */
	BorderPane sceneContainer();

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

	/**
	 * Locks the current game state by setting an indefinite timer duration.
	 */
	default void lockGameState() {
		context().state().timer().resetIndefinitely();
	}

	/**
	 * Unlocks the current game state by forcing the timer to expire.
	 */
	default void unlockGameState() {
		context().state().timer().expire();
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