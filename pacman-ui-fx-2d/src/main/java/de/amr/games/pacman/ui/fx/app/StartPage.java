package de.amr.games.pacman.ui.fx.app;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import static javafx.scene.layout.BackgroundSize.AUTO;

/**
 * @author Armin Reichert
 */
public class StartPage {

	private final PacManGames2dUI ui;
	private final StackPane root;
	private final BorderPane content;
	private BorderPane playButton;

	// TODO This should be a real button but it seems WebFX/GWT has issues with graphic buttons
	private void createPlayButton() {
		var label = new Text("Play!");
		label.setFill(ui.theme.color("startpage.button.color"));
		label.setFont(ui.theme.font("startpage.button.font"));

		var ds = new DropShadow();
		ds.setOffsetY(3.0f);
		ds.setColor(Color.color(0.2f, 0.2f, 0.2f));
		label.setEffect(ds);

		playButton = new BorderPane(label);
		playButton.setMaxSize(200, 100);
		playButton.setPadding(new Insets(10));
		playButton.setCursor(Cursor.HAND);
		playButton.setBackground(ResourceManager.coloredRoundedBackground(ui.theme.color("startpage.button.bgColor"), 20));
		playButton.setOnMouseClicked(e -> {
			if (e.getButton().equals(MouseButton.PRIMARY)) {
				ui.showGamePage();
			}
		});
	}

	public StartPage(PacManGames2dUI ui) {
		this.ui = ui;
		createPlayButton();
		content = new BorderPane();
		content.setBottom(playButton);
		BorderPane.setAlignment(playButton, Pos.CENTER);
		playButton.setTranslateY(-10);
		root = new StackPane(content);
		root.setOnKeyPressed(this::handleKeyPressed);
		root.setBackground(ResourceManager.coloredBackground(Color.BLACK));
	}

	public StackPane root() {
		return root;
	}

	public void setGameVariant(GameVariant gameVariant) {
		Image image = null;
		switch (gameVariant) {
		case MS_PACMAN:
			image = ui.theme().image("mspacman.startpage.image");
			break;
		case PACMAN:
			image = ui.theme().image("pacman.startpage.image");
			break;
		default:
			throw new IllegalGameVariantException(gameVariant);
		}
		//@formatter:off
		var backgroundImage = new BackgroundImage(image, 
				BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, 
				new BackgroundSize(
						AUTO,	AUTO, // width, height
						false, false, // as percentage
						true, // contain
						false // cover
		));
		//@formatter:on
		content.setBackground(new Background(backgroundImage));
	}

	private void handleKeyPressed(KeyEvent e) {
		switch (e.getCode()) {
		case ENTER:
		case SPACE:
			ui.showGamePage();
			break;
		case V:
			ui.switchGameVariant();
			break;
		case F11:
			ui.stage.setFullScreen(true);
			break;
		default:
			break;
		}
	}
}