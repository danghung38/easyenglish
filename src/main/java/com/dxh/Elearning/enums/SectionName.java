package com.dxh.Elearning.enums;

public enum SectionName {
    SECTION1("Section 1"),
    SECTION2("Section 2"),
    SECTION3("Section 3"),
    SECTION4("Section 4"),
    SECTION5("Section 5");

    private final String displayName;

    SectionName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SectionName fromDisplayName(String displayName) {
        for (SectionName section : values()) {
            if (section.displayName.equalsIgnoreCase(displayName)) {
                return section;
            }
        }
        throw new IllegalArgumentException("Invalid section name: " + displayName);
    }
}
