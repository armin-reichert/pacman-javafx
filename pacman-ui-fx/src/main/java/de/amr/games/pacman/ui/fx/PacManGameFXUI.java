package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

import java.util.Optional;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.PacManGameAnimation;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.fx.scene.common.PacManGameScene;
import de.amr.games.pacman.ui.fx.scene.mspacman.MsPacManGameScenes;
import de.amr.games.pacman.ui.fx.scene.pacman.PacManGameScenes;
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
	private final double sizeX, sizeY;
	private final PacManGameScenes pacManGameScenes;
	private final MsPacManGameScenes msPacManGameScenes;

	private PacManGameModel game;
	private PacManGameScene currentScene;
	private boolean muted;

	public PacManGameFXUI(Stage stage, PacManGameController controller, int tilesX, int tilesY, double scaling) {
		this.scaling = scaling;
		this.stage = stage;
		sizeX = tilesX * TS * scaling;
		sizeY = tilesY * TS * scaling;
		pacManGameScenes = new PacManGameScenes();
		msPacManGameScenes = new MsPacManGameScenes();
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
			msPacManGameScenes.createScenes((MsPacManGame) game, sizeX, sizeY, scaling);
		} else if (game instanceof PacManGame) {
			pacManGameScenes.createScenes((PacManGame) game, sizeX, sizeY, scaling);
		} else {
			log("%s: Cannot create scenes for invalid game: %s", this, game);
		}
	}

	private boolean updateScene() {
		PacManGameScene newScene = selectScene();
		if (newScene == null) {
			log("%s: No scene matches current game state %s", this, game.state);
			return false;
		}
		if (currentScene != newScene) {
			currentScene.end();
			currentScene = newScene;
			log("%s: Scene changed from %s to %s", this, currentScene.getClass().getSimpleName(),
					newScene.getClass().getSimpleName());
			newScene.start();
			return true;
		}
		return false;
	}

	private PacManGameScene selectScene() {
		if (game instanceof MsPacManGame) {
			return msPacManGameScenes.selectScene(game);
		}
		if (game instanceof PacManGame) {
			return pacManGameScenes.selectScene(game);
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
	public void render() {
		Platform.runLater(() -> {
			boolean changed = updateScene();
			if (currentScene != null) {
				if (changed) {
					stage.setScene(currentScene.getFXScene());
				}
				currentScene.render();
			}
		});
	}

	@Override
	public void reset() {
		currentScene.end();
		setGame(game);
		updateScene();
	}

	@Override
	public void showFlashMessage(String message) {
		// TODO implement
	}

	@Override
	public boolean keyPressed(String keySpec) {
		boolean pressed = currentScene.keyboard().keyPressed(keySpec);
		currentScene.keyboard().clearKey(keySpec); // TODO
		return pressed;
	}

	@Override
	public Optional<SoundManager> sound() {
		return muted ? Optional.empty()
				: Optional.of(game instanceof PacManGame ? pacManGameScenes.soundManager : msPacManGameScenes.soundManager);
	}

	@Override
	public void mute(boolean b) {
		muted = b;
	}

	@Override
	public Optional<PacManGameAnimation> animation() {
		return currentScene.animations();
	}
}