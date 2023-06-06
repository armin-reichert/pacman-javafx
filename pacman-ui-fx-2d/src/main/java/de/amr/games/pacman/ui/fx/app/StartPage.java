package de.amr.games.pacman.ui.fx.app;

import static javafx.scene.layout.BackgroundSize.AUTO;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * @author Armin Reichert
 */
public class StartPage {

	private final StackPane root;
	private final BorderPane content;
	private final Pane button;
	private PacManGames2dUI ui;

	// TODO This should be a real button but it seems WebFX/GWT has issues with graphic buttons
	private static StackPane createButton(String text, Theme theme, Runnable action) {
		var textView = new Text(text);
		textView.setFill(theme.color("startpage.button.color"));
		textView.setFont(theme.font("startpage.button.font"));
		var ds = new DropShadow();
		ds.setOffsetY(3.0f);
		ds.setColor(Color.color(0.2f, 0.2f, 0.2f));
		textView.setEffect(ds);

		var button = new StackPane(textView);
		button.setMaxSize(200, 100);
		button.setPadding(new Insets(10));
		button.setCursor(Cursor.HAND);
		button.setBackground(ResourceManager.coloredRoundedBackground(theme.color("startpage.button.bgColor"), 20));
		button.setOnMouseClicked(e -> {
			if (e.getButton().equals(MouseButton.PRIMARY)) {
				action.run();
			}
		});
		return button;
	}

	public StartPage(PacManGames2dUI ui) {
		this.ui = ui;
		button = createButton("Play!", ui.theme(), ui::showGamePage);
		content = new BorderPane();
		content.setBottom(button);
		BorderPane.setAlignment(button, Pos.CENTER);
		button.setTranslateY(-10);
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