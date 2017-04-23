package ly.appsocial.chatcenter.database.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ly.appsocial.chatcenter.database.tables.TbApp;
import ly.appsocial.chatcenter.database.tables.TbChannel;
import ly.appsocial.chatcenter.database.tables.TbMessage;
import ly.appsocial.chatcenter.database.tables.TbOrg;
import ly.appsocial.chatcenter.util.CCLog;

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ChatCenter.db";
    private static final String TAG = DBHelper.class.getCanonicalName();

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        CCLog.e(TAG, TbChannel.SQL_CREATE_TABLE);
        CCLog.e(TAG, TbMessage.SQL_CREATE_TABLE);
        CCLog.e(TAG, TbOrg.SQL_CREATE_TABLE);
        CCLog.e(TAG, TbApp.SQL_CREATE_TABLE);


        db.execSQL(TbChannel.SQL_CREATE_TABLE);
        db.execSQL(TbMessage.SQL_CREATE_TABLE);
        db.execSQL(TbOrg.SQL_CREATE_TABLE);
        db.execSQL(TbApp.SQL_CREATE_TABLE);
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(TbChannel.SQL_DELETE_TABLE);
        db.execSQL(TbMessage.SQL_DELETE_TABLE);
        db.execSQL(TbOrg.SQL_DELETE_TABLE);
        db.execSQL(TbApp.SQL_DELETE_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
