package de.amr.pacmanfx.tilemap.editor;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractEditorAction implements EditorAction {

    private final Map<String, Object> args = new HashMap<>();

    public <T> T getArg(String name, Class<T> expectedClass) {
        Object value = args.get(name);
        if (expectedClass.isInstance(value)) {
            return expectedClass.cast(value);
        }
        throw new IllegalArgumentException("%s is not of type %s".formatted(name, expectedClass.getSimpleName()));
    }

    public void setArg(String name, Object value) {
        args.put(name, value);
    }
}
