package ly.appsocial.chatcenter.database.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.BaseColumns;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ly.appsocial.chatcenter.dto.OrgItem;
import ly.appsocial.chatcenter.dto.UserItem;

public class TbOrg extends TBBase {

    public TbOrg(Context context) {
        super(context);
    }

    /* Inner class that defines the table contents */
    public static class OrgEntry implements BaseColumns {
        public static final String TABLE_NAME = "CC_ORG";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_UID = "uid";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_PHONE_NUMBER = "phone_number";
        public static final String COLUMN_ICON_URL = "icon_url";
        public static final String COLUMN_UNREAD_MESSAGES_CHANNEL = "unread_messages_channels";
        public static final String COLUMN_USERS = "users";
    }

    /*To create new table use below string*/
    public static final String SQL_CREATE_TABLE = "CREATE TABLE "
            + OrgEntry.TABLE_NAME + " ("
            + OrgEntry._ID + " INTEGER PRIMARY KEY,"
            + OrgEntry.COLUMN_ID + " INTEGER,"
            + OrgEntry.COLUMN_UID + " TEXT,"
            + OrgEntry.COLUMN_NAME + " TEXT,"
            + OrgEntry.COLUMN_ADDRESS + " TEXT,"
            + OrgEntry.COLUMN_PHONE_NUMBER + " TEXT,"
            + OrgEntry.COLUMN_ICON_URL + " TEXT,"
            + OrgEntry.COLUMN_UNREAD_MESSAGES_CHANNEL + " TEXT,"
            + OrgEntry.COLUMN_USERS +  " TEXT"
            + ")";


    /* Statement to drop table*/
    public static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + OrgEntry.TABLE_NAME;


    /**
     * Insert the new row, returning the primary key value of the new row
     * @param orgItem
     * @return
     */
    private long insert(OrgItem orgItem) {
        if (orgItem == null) {
            return -1;
        }

        ContentValues values = convertOrgToContentValues(orgItem);

        return mWritableDB.insert(OrgEntry.TABLE_NAME, null, values);
    }

    /**
     * Update an exists org
     * @param orgItem
     * @return
     */
    private long update(OrgItem orgItem) {
        if (orgItem == null) {
            return -1;
        }

        ContentValues values = convertOrgToContentValues(orgItem);

        String selection = OrgEntry.COLUMN_UID + " = ?";
        String[] selectionArgs = new String[]{orgItem.uid};

        return mWritableDB.update(OrgEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    /**
     * If the org is added before update its information,
     * else insert a new row to database
     * @param orgItem
     * @return
     */
    private long insertOrUpdateOrg(OrgItem orgItem) {
        if (isOrgExist(orgItem)) {
            return update(orgItem);
        } else {
            return insert(orgItem);
        }
    }

    /**
     * Insert or update a list of org
     * @param orgItems
     */
    public void insertOrUpdateListOrgs(List<OrgItem> orgItems, InsertOrgCallback callback) {
        if (orgItems == null) {
            return;
        }
        clearTable();
        new WriteListOrgAsyncTask(callback).execute(orgItems);
    }

    public void readListOfOrg(GetOrgCallback callback) {
        new ReadListOrgAsyncTask(callback).execute();
    }

    /**
     * Check if the org is already added into database
     * @param orgItem
     * @return
     */
    private boolean isOrgExist(OrgItem orgItem) {
        if (orgItem == null) {
            return false;
        }

        String[] projection = new String[] {OrgEntry.COLUMN_UID};

        String selection = OrgEntry.COLUMN_UID + " = ?";
        String[] selectionArgs = new String[]{orgItem.uid};

        Cursor cursor = mReadableDB.query(OrgEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, null, null);

        return (cursor != null) && (cursor.getCount() > 0);
    }

    public OrgItem getOrg(String orgUid) {

        OrgItem orgItem = null;

        open();
        if (mReadableDB == null) {
            return orgItem;
        }

        String[] projection = new String[]{
                OrgEntry.COLUMN_ID,
                OrgEntry.COLUMN_UID,
                OrgEntry.COLUMN_NAME,
                OrgEntry.COLUMN_ADDRESS,
                OrgEntry.COLUMN_PHONE_NUMBER,
                OrgEntry.COLUMN_ICON_URL,
                OrgEntry.COLUMN_UNREAD_MESSAGES_CHANNEL,
                OrgEntry.COLUMN_USERS
        };

        String selection = OrgEntry.COLUMN_UID + " = ?";
        String[] selectionArgs = new String[]{orgUid};

        Cursor cursor = mReadableDB.query(OrgEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            orgItem = convertCursorToOrg(cursor);
        }

        cursor.close();
        close();

        return orgItem;
    }

    /**
     * ASYNCTASK: To write multiple Org into database
     */
    private class WriteListOrgAsyncTask extends AsyncTask<List<OrgItem>, String, String> {

        private InsertOrgCallback mCallback;

        public WriteListOrgAsyncTask(InsertOrgCallback callback) {
            mCallback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            open();
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
        protected String doInBackground(List<OrgItem>... params) {
            if (params == null || params.length == 0) {
                return null;
            }

            List<OrgItem> orgItems = params[0];
            for (OrgItem item: orgItems) {
                insertOrUpdateOrg(item);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            close();
            if (mCallback != null) {
                mCallback.onInsertOrgSuccess();
            }
        }
    }

    private class ReadListOrgAsyncTask extends AsyncTask<Integer, String, List<OrgItem>> {

        private GetOrgCallback mCallback;

        public ReadListOrgAsyncTask (GetOrgCallback callback) {
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
        protected List<OrgItem> doInBackground(Integer... params) {

            List<OrgItem> items = new ArrayList<>();

            String[] projection = new String[]{
                    OrgEntry.COLUMN_ID,
                    OrgEntry.COLUMN_UID,
                    OrgEntry.COLUMN_NAME,
                    OrgEntry.COLUMN_ADDRESS,
                    OrgEntry.COLUMN_PHONE_NUMBER,
                    OrgEntry.COLUMN_ICON_URL,
                    OrgEntry.COLUMN_UNREAD_MESSAGES_CHANNEL,
                    OrgEntry.COLUMN_USERS
            };

            Cursor cursor = mReadableDB.query(OrgEntry.TABLE_NAME, projection, null, null, null, null, null);

            while (cursor != null && cursor.moveToNext()) {

                OrgItem orgItem = convertCursorToOrg(cursor);

                if (orgItem != null) {
                    items.add(orgItem);
                }
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
        protected void onPostExecute(List<OrgItem> items) {
            super.onPostExecute(items);
            close();
            if (mCallback!= null) {
                mCallback.onGetOrgSuccess(items);
            }
        }
    }

    /** Delete all org from database*/
    public int clearTable() {
        open();
        int numberColumnDeleted = mWritableDB.delete(OrgEntry.TABLE_NAME, null, null);
        close();
        return numberColumnDeleted;
    }

    private ContentValues convertOrgToContentValues(OrgItem orgItem) {
        Gson gson = new Gson();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(OrgEntry.COLUMN_ID, orgItem.id);
        values.put(OrgEntry.COLUMN_UID, orgItem.uid);
        values.put(OrgEntry.COLUMN_NAME, orgItem.name);
        values.put(OrgEntry.COLUMN_ADDRESS, orgItem.address);
        values.put(OrgEntry.COLUMN_PHONE_NUMBER, orgItem.phoneNumber);
        values.put(OrgEntry.COLUMN_ICON_URL, orgItem.iconUrl);
        values.put(OrgEntry.COLUMN_UNREAD_MESSAGES_CHANNEL, gson.toJson(orgItem.unreadMessagesChannels));
        values.put(OrgEntry.COLUMN_USERS, gson.toJson(orgItem.users));

        return values;
    }

    private OrgItem convertCursorToOrg(Cursor cursor) {

        if (cursor == null) {
            return null;
        }

        Gson gson = new Gson();

        OrgItem orgItem = new OrgItem();
        orgItem.id = cursor.getInt(cursor.getColumnIndexOrThrow(OrgEntry.COLUMN_ID));
        orgItem.uid = cursor.getString(cursor.getColumnIndexOrThrow(OrgEntry.COLUMN_UID));
        orgItem.name = cursor.getString(cursor.getColumnIndexOrThrow(OrgEntry.COLUMN_NAME));
        orgItem.address = cursor.getString(cursor.getColumnIndexOrThrow(OrgEntry.COLUMN_ADDRESS));
        orgItem.phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(OrgEntry.COLUMN_PHONE_NUMBER));
        orgItem.iconUrl = cursor.getString(cursor.getColumnIndexOrThrow(OrgEntry.COLUMN_ICON_URL));

        orgItem.unreadMessagesChannels = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(cursor.getString(cursor.getColumnIndexOrThrow(OrgEntry.COLUMN_UNREAD_MESSAGES_CHANNEL)));
            for (int i = 0; jsonArray != null && i < jsonArray.length(); i++) {
                orgItem.unreadMessagesChannels.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        orgItem.users = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(cursor.getString(cursor.getColumnIndexOrThrow(OrgEntry.COLUMN_USERS)));
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    orgItem.users.add(gson.fromJson(jsonObject.toString(), UserItem.class));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return orgItem;
    }

    public interface InsertOrgCallback {
        /** Finish inserting a list of Org into Database*/
        void onInsertOrgSuccess();
    }

    public interface GetOrgCallback {
        /** Finish reading a list of Org from Database*/
        void onGetOrgSuccess(List<OrgItem> orgItems);
    }
}
