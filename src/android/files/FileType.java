package org.elastos.plugins.hive.files;

public enum FileType {
    FILE(0),
    FOLDER(1);

    private int mValue;

    FileType(int value) {
        mValue = value;
    }

    public static FileType fromId(int value) {
        for(FileType t : values()) {
            if (t.mValue == value) {
                return t;
            }
        }
        return FILE;
    }
}