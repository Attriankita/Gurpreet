package com.project.wink;

import android.provider.BaseColumns;

public  final class Constants {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private Constants() {}

    /* Inner class that defines the table contents */
    public static class DBConstants implements BaseColumns {
        public static final String TABLE_NAME = "messageCount";
        public static final String COLUMN_NAME_OBJECT_ID = "objectId";
        public static final String COLUMN_NAME_COUNT_MESSAGES = "count";
    }
}
