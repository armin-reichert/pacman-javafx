package de.amr.pacmanfx.ui.views.playview;

import de.amr.pacmanfx.uilib.view2d.RenderingSurface;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class GameSceneContainer extends StackPane {

    private final RenderingSurface renderingSurface;

    public GameSceneContainer() {
        renderingSurface = new RenderingSurface();

        Text info = new Text();
        info.textProperty().bind(Bindings.createStringBinding(
            () -> "Surface w=%.0f h=%.0f".formatted(renderingSurface.width(), renderingSurface.height()),
            renderingSurface.widthProperty(), renderingSurface.heightProperty()
        ));
        info.setFill(Color.WHITE);
        info.setFont(Font.font(16));
        info.setTranslateY(-48);

        getChildren().addAll(renderingSurface.canvas(), info);
        StackPane.setAlignment(info, Pos.BOTTOM_CENTER);
    }

    public RenderingSurface renderingSurface() {
        return renderingSurface;
    }
}
