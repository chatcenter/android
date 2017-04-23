package ly.appsocial.chatcenter.database.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.BaseColumns;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import ly.appsocial.chatcenter.dto.FunnelItem;
import ly.appsocial.chatcenter.dto.ws.response.GetAppsResponseDto;

public class TbApp extends TBBase {

    private final int READ_FOR_GUEST_ENABLE = 1; // Display "read" status on list message for guest
    private final int READ_FOR_GUEST_DISABLE = 0; // DO NOT display "read" status on list message for guest

    public TbApp(Context context) {
        super(context);
    }

    /* Inner class that defines the table contents */
    public static class AppEntry implements BaseColumns {
        public static final String TABLE_NAME = "CC_APP";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_UID = "uid";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_BUSINESS_TYPE = "business_type";
        public static final String COLUMN_TOKEN = "token";
        public static final String COLUMN_READ_FOR_GUEST = "read_for_guest";
        public static final String COLUMN_STICKER = "stickers";
        public static final String COLUMN_APP_ICONS = "app_icons";
        public static final String COLUMN_FUNNELS = "funnels";
    }

    /*To create new table use below string*/
    public static final String SQL_CREATE_TABLE = "CREATE TABLE "
            + AppEntry.TABLE_NAME + " ("
            + AppEntry._ID + " INTEGER PRIMARY KEY,"
            + AppEntry.COLUMN_ID + " INTEGER,"
            + AppEntry.COLUMN_UID + " TEXT,"
            + AppEntry.COLUMN_NAME + " TEXT,"
            + AppEntry.COLUMN_BUSINESS_TYPE + " TEXT,"
            + AppEntry.COLUMN_TOKEN + " TEXT,"
            + AppEntry.COLUMN_READ_FOR_GUEST + " INTEGER,"
            + AppEntry.COLUMN_STICKER + " TEXT,"
            + AppEntry.COLUMN_APP_ICONS +  " TEXT,"
            + AppEntry.COLUMN_FUNNELS +  " TEXT"
            + ")";


    /* Statement to drop table*/
    public static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + AppEntry.TABLE_NAME;


    /**
     * Insert the new row, returning the primary key value of the new row
     * @param app
     * @return
     */
    private long insert(GetAppsResponseDto.App app) {

        if (app == null) {
            return -1;
        }

        open();

        ContentValues values = convertAppToContentValues(app);
        long rowId = mWritableDB.insert(AppEntry.TABLE_NAME, null, values);

        close();

        return rowId;
    }

    /**
     * Update an exists org
     * @param app
     * @return
     */
    private long update(GetAppsResponseDto.App app) {
        if (app == null) {
            return -1;
        }

        open();

        ContentValues values = convertAppToContentValues(app);

        String selection = AppEntry.COLUMN_UID + " = ?";
        String[] selectionArgs = new String[]{app.uid};

        long rowId = mWritableDB.update(AppEntry.TABLE_NAME, values, selection, selectionArgs);
        close();

        return rowId;
    }

    /**
     * If the app is added before update its information,
     * else insert a new row to database
     * @param app
     * @return
     */
    private long insertOrUpdateApp(GetAppsResponseDto.App app) {
        if (isAppExist(app)) {
            return update(app);
        } else {
            return insert(app);
        }
    }

    /**
     * Insert or update a list of org
     * @param apps
     */
    public void saveListApps(List<GetAppsResponseDto.App> apps, InsertAppCallback callback) {
        if (apps == null) {
            return;
        }
        new WriteListAppAsyncTask(callback).execute(apps);
    }

    public void readListOfApp(GetAppCallback callback) {
        new ReadListAppAsyncTask(callback).execute();
    }

    public GetAppsResponseDto.App getAppByUid(String appUid) {
        open();

        GetAppsResponseDto.App result = null;

        String[] projection = new String[]{
                AppEntry.COLUMN_ID,
                AppEntry.COLUMN_UID,
                AppEntry.COLUMN_NAME,
                AppEntry.COLUMN_BUSINESS_TYPE,
                AppEntry.COLUMN_TOKEN,
                AppEntry.COLUMN_READ_FOR_GUEST,
                AppEntry.COLUMN_STICKER,
                AppEntry.COLUMN_APP_ICONS,
                AppEntry.COLUMN_FUNNELS
        };

        String selection = AppEntry.COLUMN_UID + " = ?";
        String[] selectionArg = new String[] {appUid};

        Cursor cursor = mReadableDB.query(AppEntry.TABLE_NAME, projection, selection, selectionArg, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            result = convertCursorToAppObject(cursor);
        }

        cursor.close();

        close();
        return  result;
    }

    public GetAppsResponseDto.App getAppByToken(String appToken) {
        open();

        GetAppsResponseDto.App result = null;

        String[] projection = new String[]{
                AppEntry.COLUMN_ID,
                AppEntry.COLUMN_UID,
                AppEntry.COLUMN_NAME,
                AppEntry.COLUMN_BUSINESS_TYPE,
                AppEntry.COLUMN_TOKEN,
                AppEntry.COLUMN_READ_FOR_GUEST,
                AppEntry.COLUMN_STICKER,
                AppEntry.COLUMN_APP_ICONS,
                AppEntry.COLUMN_FUNNELS
        };

        String selection = AppEntry.COLUMN_TOKEN + " = ?";
        String[] selectionArg = new String[] {appToken};

        Cursor cursor = mReadableDB.query(AppEntry.TABLE_NAME, projection, selection, selectionArg, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            result = convertCursorToAppObject(cursor);
        }

        cursor.close();

        close();
        return  result;
    }

    /**
     * Check if the org is already added into database
     * @param app
     * @return
     */
    private boolean isAppExist(GetAppsResponseDto.App app) {

        open();

        if (app == null) {
            return false;
        }

        String[] projection = new String[] {AppEntry.COLUMN_UID};

        String selection = AppEntry.COLUMN_UID + " = ?";
        String[] selectionArgs = new String[]{app.uid};

        Cursor cursor = mReadableDB.query(AppEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, null, null);

        boolean result = (cursor != null) && (cursor.getCount() > 0);

        close();

        return result;
    }

    /**
     * ASYNCTASK: To write multiple Org into database
     */
    private class WriteListAppAsyncTask extends AsyncTask<List<GetAppsResponseDto.App>, String, String> {

        private InsertAppCallback mCallback;

        public WriteListAppAsyncTask(InsertAppCallback callback) {
            mCallback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected String doInBackground(List<GetAppsResponseDto.App>... params) {
            if (params == null || params.length == 0) {
                return null;
            }

            clearDatabase();

            List<GetAppsResponseDto.App> orgItems = params[0];
            for (GetAppsResponseDto.App item: orgItems) {
                insertOrUpdateApp(item);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (mCallback != null) {
                mCallback.onInsertAppSuccess();
            }
        }
    }

    private class ReadListAppAsyncTask extends AsyncTask<Integer, String, List<GetAppsResponseDto.App>> {

        private GetAppCallback mCallback;

        public ReadListAppAsyncTask (GetAppCallback callback) {
            mCallback = callback;
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected List<GetAppsResponseDto.App> doInBackground(Integer... params) {

            List<GetAppsResponseDto.App> items = new ArrayList<>();

            String[] projection = new String[]{
                    AppEntry.COLUMN_ID,
                    AppEntry.COLUMN_UID,
                    AppEntry.COLUMN_NAME,
                    AppEntry.COLUMN_BUSINESS_TYPE,
                    AppEntry.COLUMN_TOKEN,
                    AppEntry.COLUMN_READ_FOR_GUEST,
                    AppEntry.COLUMN_STICKER,
                    AppEntry.COLUMN_APP_ICONS,
                    AppEntry.COLUMN_FUNNELS
            };

            Cursor cursor = mReadableDB.query(AppEntry.TABLE_NAME, projection, null, null, null, null, null);

            while (cursor != null && cursor.moveToNext()) {
                items.add(convertCursorToAppObject(cursor));
            }

            cursor.close();

            return items;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            open();
        }

        @Override
        protected void onPostExecute(List<GetAppsResponseDto.App> items) {
            super.onPostExecute(items);
            close();
            if (mCallback!= null) {
                mCallback.onGetAppSuccess(items);
            }
        }
    }

    /** Delete all org from database*/
    public int clearDatabase() {
        open();
        int numberRows = mWritableDB.delete(AppEntry.TABLE_NAME, null, null);
        close();
        return numberRows;
    }

    public interface InsertAppCallback {
        /** Finish inserting a list of App into Database*/
        void onInsertAppSuccess();
    }

    public interface GetAppCallback {
        /** Finish reading a list of App from Database*/
        void onGetAppSuccess(List<GetAppsResponseDto.App> apps);
    }

    private GetAppsResponseDto.App convertCursorToAppObject(Cursor cursor) {

        Gson gson = new Gson();

        GetAppsResponseDto.App app = new GetAppsResponseDto.App();
        app.id = cursor.getInt(cursor.getColumnIndexOrThrow(AppEntry.COLUMN_ID));
        app.uid = cursor.getString(cursor.getColumnIndexOrThrow(AppEntry.COLUMN_UID));
        app.name = cursor.getString(cursor.getColumnIndexOrThrow(AppEntry.COLUMN_NAME));
        app.businessType = cursor.getString(cursor.getColumnIndexOrThrow(AppEntry.COLUMN_BUSINESS_TYPE));
        app.token = cursor.getString(cursor.getColumnIndexOrThrow(AppEntry.COLUMN_TOKEN));
        app.readForGuest = cursor.getInt(cursor.getColumnIndexOrThrow(AppEntry.COLUMN_READ_FOR_GUEST)) == READ_FOR_GUEST_ENABLE;
        app.funnels = new ArrayList<>();

        // Get list of funnels
        try {
            JSONArray jsonArray = new JSONArray(cursor.getString(cursor.getColumnIndexOrThrow(AppEntry.COLUMN_FUNNELS)));
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    app.funnels.add(gson.fromJson(jsonArray.getString(i), FunnelItem.class));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Get list of stickers
        app.stickers = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(cursor.getString(cursor.getColumnIndexOrThrow(AppEntry.COLUMN_STICKER)));
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    app.stickers.add(jsonArray.getString(i));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Get list of app icons
        app.icons = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(cursor.getString(cursor.getColumnIndexOrThrow(AppEntry.COLUMN_APP_ICONS)));
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    app.icons.add(gson.fromJson(jsonArray.getString(i), GetAppsResponseDto.AppIcon.class));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return app;
    }

    private ContentValues convertAppToContentValues(GetAppsResponseDto.App app) {
        Gson gson = new Gson();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(AppEntry.COLUMN_ID, app.id);
        values.put(AppEntry.COLUMN_UID, app.uid);
        values.put(AppEntry.COLUMN_NAME, app.name);
        values.put(AppEntry.COLUMN_BUSINESS_TYPE, app.businessType);
        values.put(AppEntry.COLUMN_TOKEN, app.token);
        values.put(AppEntry.COLUMN_READ_FOR_GUEST, app.readForGuest ? READ_FOR_GUEST_DISABLE : READ_FOR_GUEST_ENABLE);
        values.put(AppEntry.COLUMN_STICKER, gson.toJson(app.stickers));
        values.put(AppEntry.COLUMN_APP_ICONS, gson.toJson(app.icons));
        values.put(AppEntry.COLUMN_FUNNELS, gson.toJson(app.funnels));

        return values;
    }
}
