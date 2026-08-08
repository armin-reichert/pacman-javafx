package de.amr.pacmanfx.core.entities.score.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import javafx.beans.property.*;

import java.time.LocalDate;

public class ScoreDataComp implements GameEntityComponent {

    private final BooleanProperty enabled = new SimpleBooleanProperty();

    private final IntegerProperty points = new SimpleIntegerProperty();

    private final IntegerProperty levelNumber = new SimpleIntegerProperty();

    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();

    public BooleanProperty enabledProperty() { return enabled; }

    public boolean isEnabled() { return enabledProperty().get(); }

    public void setEnabled(boolean enabled) { enabledProperty().set(enabled); }

    public IntegerProperty pointsProperty() { return points; }

    public void setPoints(int points) { pointsProperty().set(points);  }

    public int points() {
        return pointsProperty().get();
    }

    public IntegerProperty levelNumberProperty() { return levelNumber; }

    public void setLevelNumber(int levelNumber) { levelNumberProperty().set(levelNumber); }

    public int levelNumber() {
        return levelNumberProperty().get();
    }

    public ObjectProperty<LocalDate> dateProperty() { return date; }

    public void setDate(LocalDate date) { dateProperty().set(date); }

    public LocalDate date() {
        return dateProperty().get();
    }

    public ScoreDataComp() {
        reset();
    }

    @Override
    public void reset() {
        setEnabled(true);
        setPoints(0);
        setLevelNumber(1);
        setDate(LocalDate.now());
    }
}
