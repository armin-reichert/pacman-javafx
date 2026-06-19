package de.amr.pacmanfx.uilib.widgets.skin;

import de.amr.pacmanfx.uilib.widgets.FontAwesomeIcon;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.tinylog.Logger;

import java.net.URL;

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
public class FontAwesomeIconSkin extends SkinBase<FontAwesomeIcon> {

    public FontAwesomeIconSkin(FontAwesomeIcon control) {
        super(control);

        final StackPane container = new StackPane();
        container.getStyleClass().add(FontAwesomeIcon.DEFAULT_STYLE_CLASS);

        final Text text = new Text();
        text.setText(String.valueOf(control.symbol().unicode()));
        text.fillProperty().bind(control.fillProperty());
        text.fontProperty().bind(container.prefHeightProperty()
            .map(height -> {
                final Font font = Font.font(AWESOME_FONT.getFamily(), height.doubleValue());
                Logger.info("Icon font size: {}", font.getSize());
                return font;
            }));
        text.opacityProperty().bind(control.opacityProperty());
        text.visibleProperty().bind(control.visibleProperty());

        container.getChildren().add(text);
        getChildren().add(container);
    }

    // private

    //TODO adapt to control package
    private static final String AWESOME_FONT_PATH = "/de/amr/pacmanfx/uilib/fonts/fa7/Font Awesome 7 Free-Solid-900.otf";

    private static final Font AWESOME_FONT = loadAwesomeFont();

    private static Font loadAwesomeFont() {
        final int size = FontAwesomeIcon.DEFAULT_SIZE;
        final URL url = FontAwesomeIcon.class.getResource(AWESOME_FONT_PATH);
        Font font;
        if (url != null) {
            font = Font.loadFont(url.toExternalForm(), size);
            Logger.info("FontAwesome font loaded successfully: {}", font);
        } else {
            font = Font.font(size);
            Logger.error("Could not load Font Awesome fonts, using default font");
        }
        return font;
    }
}
