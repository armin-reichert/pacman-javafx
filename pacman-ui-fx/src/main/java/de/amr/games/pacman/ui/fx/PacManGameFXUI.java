package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.FlashMessage;
import de.amr.games.pacman.ui.PacManGameAnimation;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.fx.common.PacManGameScene;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameFXUI implements PacManGameUI {

	private final Stage stage;
	private final double scaling;
	private final double width;
	private final double height;

	private PacManGameModel game;
	private PacManGameScene currentScene;

	private boolean muted;

	public static final Deque<FlashMessage> flashMessageQ = new ArrayDeque<>();

	public PacManGameFXUI(Stage stage, PacManGameController controller, double scaling) {
		this.scaling = scaling;
		this.stage = stage;
		width = 28 * TS * scaling;
		height = 36 * TS * scaling;
		stage.setTitle("JavaFX: Pac-Man / Ms. Pac-Man");
		stage.getIcons().add(new Image("/pacman/graphics/pacman.png"));
		stage.setOnCloseRequest(e -> {
			controller.endGame();
			Platform.exit();
		});
		setGame(controller.getGame());
		log("Pac-Man game JavaFX UI created");
	}

	@Override
	public void setGame(PacManGameModel game) {
		if (game == null) {
			throw new IllegalArgumentException("Cannot set game, game is null");
		}
		this.game = game;
		if (game instanceof MsPacManGame) {
			de.amr.games.pacman.ui.fx.mspacman.Scenes.createScenes((MsPacManGame) game, width, height, scaling);
		} else if (game instanceof PacManGame) {
			de.amr.games.pacman.ui.fx.pacman.Scenes.createScenes((PacManGame) game, width, height, scaling);
		} else {
			log("%s: Cannot create scenes for invalid game: %s", this, game);
		}
	}

	private PacManGameScene selectScene() {
		if (game instanceof MsPacManGame) {
			return de.amr.games.pacman.ui.fx.mspacman.Scenes.selectScene(game);
		}
		if (game instanceof PacManGame) {
			return de.amr.games.pacman.ui.fx.pacman.Scenes.selectScene(game);
		}
		throw new IllegalStateException("No scene found for game state " + game.stateDescription());
	}

	@Override
	public void show() {
		currentScene = selectScene();
		log("Initial scene is %s", currentScene);
		currentScene.start();
		stage.setScene(currentScene.getFXScene());
		stage.sizeToScene();
		stage.show();
	}

	@Override
	public void update() {
		PacManGameScene newScene = selectScene();
		if (newScene == null) {
			throw new IllegalStateException("No scene matches current game state " + game.state);
		}
		if (currentScene != newScene) {
			log("%s: Scene changes from %s to %s", this, currentScene, newScene);
			currentScene.end();
			newScene.start();
			currentScene = newScene;
		}
		currentScene.update();

		FlashMessage message = flashMessageQ.peek();
		if (message != null) {
			message.timer.tick();
			if (message.timer.expired()) {
				flashMessageQ.remove();
			}
		}
	}

	@Override
	public void render() {
		Platform.runLater(() -> {
			if (stage.getScene() != currentScene.getFXScene()) {
				stage.setScene(currentScene.getFXScene());
			}
			try {
				currentScene.render();
			} catch (Exception x) {
				log("Exception occurred when rendering scene %s", currentScene);
				x.printStackTrace();
			}
		});
	}

	@Override
	public void reset() {
		currentScene.end();
		setGame(game);
	}

	@Override
	public void showFlashMessage(String message, long ticks) {
		flashMessageQ.add(new FlashMessage(message, ticks));
	}

	@Override
	public boolean keyPressed(String keySpec) {
		boolean pressed = currentScene.keyboard().keyPressed(keySpec);
		currentScene.keyboard().clearKey(keySpec); // TODO
		return pressed;
	}

	@Override
	public Optional<SoundManager> sound() {
		if (muted) {
			return Optional.empty();
		}
		if (game instanceof PacManGame) {
			return Optional.of(de.amr.games.pacman.ui.fx.pacman.Scenes.soundManager);
		}
		if (game instanceof MsPacManGame) {
			return Optional.of(de.amr.games.pacman.ui.fx.mspacman.Scenes.soundManager);
		}
		return Optional.empty();
	}

	@Override
	public void mute(boolean b) {
		muted = b;
	}

	@Override
	public Optional<PacManGameAnimation> animation() {
		return currentScene.animation();
	}
}