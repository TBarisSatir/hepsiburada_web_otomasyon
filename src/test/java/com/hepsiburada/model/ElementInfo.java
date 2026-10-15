package com.hepsiburada.model;

/**
 * element-infos/*.json içindeki tek bir locator kaydı.
 */
public class ElementInfo {

    private String key;
    private String value;
    private String type;

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return key + " [" + type + "=" + value + "]";
    }
}
