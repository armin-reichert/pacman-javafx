/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.properties;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;

public class MapEditorProperty {

    public static final Pattern PATTERN_PROPERTY_NAME = Pattern.compile("[a-zA-Z]([a-zA-Z0-9_])*");

    public static boolean isInvalidPropertyName(String s) {
        return s == null || !PATTERN_PROPERTY_NAME.matcher(s).matches();
    }

    public static Set<MapEditorPropertyAttribute> emptyAttributeSet() {
        return EnumSet.noneOf(MapEditorPropertyAttribute.class);
    }

    private String name;
    private String value;
    private MapEditorPropertyType type;
    private final Set<MapEditorPropertyAttribute> attributes = MapEditorProperty.emptyAttributeSet();

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String value() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public MapEditorPropertyType type() {
        return type;
    }

        public void setType(MapEditorPropertyType type) {
        this.type = type;
    }

    public Set<MapEditorPropertyAttribute> attributes() {
        return attributes;
    }

    public boolean is(MapEditorPropertyAttribute attribute) {
        return attributes.contains(attribute);
    }

    @Override
    public String toString() {
        return "Property{" + "name='" + name + '\'' +
            ", value='" + value + '\'' +
            ", type=" + type +
            ", attributes=" + attributes +
            '}';
    }
}
