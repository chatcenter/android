package ly.appsocial.chatcenter.database.tables;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;

import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.database.helper.DBHelper;

public class TBBase {

    /** ロカールデータベースからデータを習得する時に使います。*/
    SQLiteDatabase mReadableDB;

    /** ロカールデータベースにデータを保存する時に使います。*/
    SQLiteDatabase mWritableDB;

    Context mContext;

    DBHelper mHelper;

    public TBBase(Context context) {
        mContext = context;
    }

    public void open() {
        mHelper = new DBHelper(mContext);
        mReadableDB = mHelper.getReadableDatabase();
        mWritableDB = mHelper.getWritableDatabase();
    }

    public void close() {
        if (mWritableDB != null && mWritableDB.isOpen()) {
            mWritableDB.close();
            mWritableDB = null;
        }

        if (mReadableDB != null && mReadableDB.isOpen()) {
            mReadableDB.close();
            mReadableDB = null;
        }
    }
}
