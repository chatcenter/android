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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.ChannelItem;
import ly.appsocial.chatcenter.dto.UserItem;
import ly.appsocial.chatcenter.util.StringUtil;

public class TbChannel extends TBBase{

    private static final String ORG_UID = "org_uid";
    private static final String CHANNEL_STATUS = "channel_status";
    private static final String LAST_UPDATE = "last_id";
    private static final int LIMIT = ChatCenterConstants.MAX_CHANNEL_ON_LOAD;

    public TbChannel(Context context) {
        super(context);
    }

    /* Inner class that defines the table contents */
    public static class ChannelEntry implements BaseColumns {
        public static final String TABLE_NAME = "CC_CHANNEL";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DISPLAY_NAME = "display_name";
        public static final String COLUMN_UID = "uid";
        public static final String COLUMN_ORG_UID = "org_uid";
        public static final String COLUMN_ORG_NAME = "org_name";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_DIRECT_MESSAGE = "direct_message";
        public static final String COLUMN_FUNNEL_ID = "funnel_id";
        public static final String COLUMN_CREATED = "created";
        public static final String COLUMN_UPDATED = "updated";
        public static final String COLUMN_USERS = "users";
        public static final String COLUMN_ASSIGNEE = "assignee";
        public static final String COLUMN_ICON_URL = "icon_url";
        public static final String COLUMN_UNREAD_MESSAGES = "unread_messages";
        public static final String COLUMN_LATEST_MESSAGE = "latest_message";
        public static final String COLUMN_LAST_UPDATE_AT = "last_updated_at";
        public static final String COLUMN_STORE_ID = "store_id";
        public static final String COLUMN_CHANNEL_INFO = "channel_information";
        public static final String COLUMN_NOTE = "note";
    }

    /*To create new table use below string*/
    public static final String SQL_CREATE_TABLE = "CREATE TABLE "
            + ChannelEntry.TABLE_NAME + " ("
            + ChannelEntry._ID + " INTEGER PRIMARY KEY,"
            + ChannelEntry.COLUMN_NAME + " TEXT,"
            + ChannelEntry.COLUMN_DISPLAY_NAME + " TEXT,"
            + ChannelEntry.COLUMN_UID + " TEXT,"
            + ChannelEntry.COLUMN_ORG_UID + " TEXT,"
            + ChannelEntry.COLUMN_ORG_NAME + " TEXT,"
            + ChannelEntry.COLUMN_STATUS + " TEXT,"
            + ChannelEntry.COLUMN_DIRECT_MESSAGE + " INTEGER,"
            + ChannelEntry.COLUMN_FUNNEL_ID + " INTEGER,"
            + ChannelEntry.COLUMN_CREATED + " INTEGER,"
            + ChannelEntry.COLUMN_UPDATED + " INTEGER,"
            + ChannelEntry.COLUMN_USERS + " TEXT,"
            + ChannelEntry.COLUMN_ASSIGNEE + " TEXT,"
            + ChannelEntry.COLUMN_ICON_URL + " TEXT,"
            + ChannelEntry.COLUMN_UNREAD_MESSAGES + " INTEGER,"
            + ChannelEntry.COLUMN_LATEST_MESSAGE + " TEXT,"
            + ChannelEntry.COLUMN_LAST_UPDATE_AT + " INTEGER,"
            + ChannelEntry.COLUMN_STORE_ID + " INTEGER,"
            + ChannelEntry.COLUMN_CHANNEL_INFO + " TEXT,"
            + ChannelEntry.COLUMN_NOTE + " TEXT"
            + ")";


    /* Statement to drop table*/
    public static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + ChannelEntry.TABLE_NAME;

    /**
     * Insert the new row, returning the primary key value of the new row
     * @param channel
     * @return
     */
    private long insert(ChannelItem channel) {
        open();
        ContentValues values = convertChannelToContentValues(channel);
        long rowId = mWritableDB.insert(ChannelEntry.TABLE_NAME, null, values);

        close();

        return rowId;
    }

    /**
     * Update an exists channel
     * @param channel
     * @return
     */
    private long update(ChannelItem channel) {
        open();

        ContentValues values = convertChannelToContentValues(channel);

        String selection = ChannelEntry.COLUMN_UID + " = ?"
                + " AND "
                + ChannelEntry.COLUMN_ORG_UID + " = ?";
        String[] selectionArgs = new String[]{channel.uid, channel.orgUid};

        long rowId =  mWritableDB.update(ChannelEntry.TABLE_NAME, values, selection, selectionArgs);

        close();

        return rowId;
    }

    /**
     * If channel is already inserted, update channel info.
     * Insert new channel if it has not been inserted
     *
     * @param channel
     * @return
     */
    public long updateOrInsert(ChannelItem channel) {

        if (channel == null) {
            return -1;
        }

        if (isChannelExists(channel.uid, channel.orgUid)) {
            return update(channel);
        } else {
            return insert(channel);
        }
    }

    /**
     * Insert or update a list of channels
     * @param channels
     */
    public void saveListChannels(List<ChannelItem> channels, SaveChannelsCallback callback) {
        new SaveChannelsTask(callback).execute(channels);
    }

    /**
     * Get all channel of org
     * @param orgUid
     * @return
     */
    public void getListChannelInOrg(String orgUid, ChannelItem lastChannel, GetChannelsCallback callback) {
        Map<String, String> param = new HashMap<>();
        if (StringUtil.isNotBlank(orgUid)) {
            param.put(ORG_UID, orgUid);
        }
        if (lastChannel != null) {
            param.put(LAST_UPDATE, (long) Math.floor(lastChannel.lastUpdatedAt) + "");
        } else {
            param.put(LAST_UPDATE, Calendar.getInstance().getTimeInMillis() + "");
        }

        new GetChannelsTask(callback).execute(param);
    }

    public ChannelItem getChannel(String orgUid, String channelUid) {
        open();
        String[] projection = new String[]{
                ChannelEntry._ID,
                ChannelEntry.COLUMN_DISPLAY_NAME,
                ChannelEntry.COLUMN_UID,
                ChannelEntry.COLUMN_ORG_UID,
                ChannelEntry.COLUMN_ORG_NAME,
                ChannelEntry.COLUMN_STATUS,
                ChannelEntry.COLUMN_FUNNEL_ID,
                ChannelEntry.COLUMN_CREATED,
                ChannelEntry.COLUMN_UPDATED,
                ChannelEntry.COLUMN_USERS,
                ChannelEntry.COLUMN_ASSIGNEE,
                ChannelEntry.COLUMN_ICON_URL,
                ChannelEntry.COLUMN_UNREAD_MESSAGES,
                ChannelEntry.COLUMN_LATEST_MESSAGE,
                ChannelEntry.COLUMN_LAST_UPDATE_AT,
                ChannelEntry.COLUMN_NOTE
        };

        String selection = ChannelEntry.COLUMN_ORG_UID + " = ? AND " + ChannelEntry.COLUMN_UID + " = ?";
        String[] selectionArgs = new String[]{orgUid, StringUtil.isNotBlank(channelUid) ? channelUid : ""};


        Cursor cursor = mReadableDB.query(ChannelEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);

        ChannelItem result = null;
        if (cursor != null && cursor.moveToFirst()) {
            result = convertCursorToChannelItem(cursor);
        }

        cursor.close();
        close();

        return result;
    }

    /**
     * If user is guest there is only one channel in Org
     * @param orgUid
     * @return
     */
    public ChannelItem getChannelForGuest(String orgUid) {
        open();
        String[] projection = new String[]{
                ChannelEntry._ID,
                ChannelEntry.COLUMN_DISPLAY_NAME,
                ChannelEntry.COLUMN_UID,
                ChannelEntry.COLUMN_ORG_UID,
                ChannelEntry.COLUMN_ORG_NAME,
                ChannelEntry.COLUMN_STATUS,
                ChannelEntry.COLUMN_FUNNEL_ID,
                ChannelEntry.COLUMN_CREATED,
                ChannelEntry.COLUMN_UPDATED,
                ChannelEntry.COLUMN_USERS,
                ChannelEntry.COLUMN_ASSIGNEE,
                ChannelEntry.COLUMN_ICON_URL,
                ChannelEntry.COLUMN_UNREAD_MESSAGES,
                ChannelEntry.COLUMN_LATEST_MESSAGE,
                ChannelEntry.COLUMN_LAST_UPDATE_AT,
                ChannelEntry.COLUMN_NOTE
        };

        String selection = ChannelEntry.COLUMN_ORG_UID + " = ? ";
        String[] selectionArgs = new String[]{orgUid};


        Cursor cursor = mReadableDB.query(ChannelEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);

        ChannelItem result = null;
        if (cursor != null && cursor.moveToFirst()) {
            result = convertCursorToChannelItem(cursor);
        }

        cursor.close();
        close();

        return result;
    }

    /**
     * Delete a channel from database
     * @param channel
     * @return
     */
    public int deleteChannel(ChannelItem channel) {

        open();

        String selection = ChannelEntry.COLUMN_UID + " = ?"
                + " AND "
                + ChannelEntry.COLUMN_ORG_UID + " = ?";
        String[] selectionArgs = new String[]{channel.uid, channel.orgUid};

        int numberDeleted = mWritableDB.delete(ChannelEntry.TABLE_NAME, selection, selectionArgs);

        close();

        return numberDeleted;
    }

    /** Delete all channel from database*/
    public int clearTable() {
        open();
        int numberRowToDelete = mWritableDB.delete(ChannelEntry.TABLE_NAME, null, null);
        close();
        return numberRowToDelete;
    }

    public int deleteChannelInOrg(String orgUid) {
        open();

        String selection = ChannelEntry.COLUMN_ORG_UID + " = ?";
        String[] selectionArgs = new String[]{orgUid};

        int numberDeleted = mWritableDB.delete(ChannelEntry.TABLE_NAME, selection, selectionArgs);

        close();

        return numberDeleted;
    }

    /**
     * Is channel inserted?
     * @param channelUid
     * @param orgUid
     * @return
     */
    private boolean isChannelExists(String channelUid, String orgUid) {
        open();

        String[] projection = new String[] {ChannelEntry.COLUMN_ORG_UID};

        String selection = ChannelEntry.COLUMN_UID + " = ?"
                + " AND "
                + ChannelEntry.COLUMN_ORG_UID + " = ?";
        String[] selectionArgs = new String[]{channelUid, orgUid};

        Cursor cursor = mReadableDB.query(ChannelEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);

        boolean existing = cursor.getCount() > 0;
        cursor.close();
        close();
        return existing;
    }

    /**
     * チャネル並び順のコンパレータ
     */
    private static final Comparator<ChannelItem> COMPARATOR = new Comparator<ChannelItem>() {
        @Override
        public int compare(final ChannelItem o1, final ChannelItem o2) {

            long t1 = (o1.latestMessage == null) ? o1.created : o1.latestMessage.created;
            long t2 = (o2.latestMessage == null) ? o2.created : o2.latestMessage.created;

            if (t1 == t2) {
                return 0;
            }
            return t1 > t2 ? -1 : 1;
        }
    };

    /**
     * User async task to save large data into database. In this case, we use to save
     * list of channels
     */
    private class SaveChannelsTask extends AsyncTask<List<ChannelItem>, String, String> {

        private SaveChannelsCallback mCallback;

        public SaveChannelsTask(SaveChannelsCallback callback) {
            mCallback = callback;
        }

        @Override
        protected String doInBackground(List<ChannelItem>... params) {

            if (params == null || params.length == 0) {
                return null;
            }

            List<ChannelItem> channels = params[0];

            if (channels == null || channels.size() == 0) {
                return null;
            }

            for (ChannelItem channelItem: channels) {
                updateOrInsert(channelItem);
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (mCallback != null) {
                mCallback.onSaveChannelsSuccess();
            }
        }
    }

    private class GetChannelsTask extends AsyncTask<Map<String, String>, String, List<ChannelItem>> {

        private GetChannelsCallback mCallback;

        public GetChannelsTask(GetChannelsCallback callback) {
            mCallback = callback;
        }

        @Override
        protected List<ChannelItem> doInBackground(Map<String, String>... params) {
            List<ChannelItem> items = new ArrayList<>();

            if (params == null || params.length == 0) {
                return items;
            }

            Map<String, String> param = params[0];
            String orgUid = param.get(ORG_UID);
            String lastId = param.get(LAST_UPDATE);

            Cursor cursor;

            String[] projection = new String[]{
                    ChannelEntry._ID,
                    ChannelEntry.COLUMN_DISPLAY_NAME,
                    ChannelEntry.COLUMN_UID,
                    ChannelEntry.COLUMN_ORG_UID,
                    ChannelEntry.COLUMN_ORG_NAME,
                    ChannelEntry.COLUMN_STATUS,
                    ChannelEntry.COLUMN_FUNNEL_ID,
                    ChannelEntry.COLUMN_CREATED,
                    ChannelEntry.COLUMN_UPDATED,
                    ChannelEntry.COLUMN_USERS,
                    ChannelEntry.COLUMN_ASSIGNEE,
                    ChannelEntry.COLUMN_ICON_URL,
                    ChannelEntry.COLUMN_UNREAD_MESSAGES,
                    ChannelEntry.COLUMN_LATEST_MESSAGE,
                    ChannelEntry.COLUMN_LAST_UPDATE_AT,
                    ChannelEntry.COLUMN_NOTE
            };

            String selection = null;
            String[] selectionArgs = null;
            String order= null;
            String limit = LIMIT + "";

            if (StringUtil.isNotBlank(orgUid)) {
                selection = ChannelEntry.COLUMN_ORG_UID + " = ? AND " + ChannelEntry.COLUMN_LAST_UPDATE_AT + " < ?";
                selectionArgs = new String[]{orgUid, lastId};
                order = ChannelEntry.COLUMN_LAST_UPDATE_AT + " DESC";
            } else {
                selection = ChannelEntry.COLUMN_LAST_UPDATE_AT + " < ?";
                selectionArgs = new String[]{lastId};
                order = ChannelEntry.COLUMN_LAST_UPDATE_AT + " DESC";
            }


            cursor = mReadableDB.query(ChannelEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    order,
                    limit);

            while (cursor != null && cursor.moveToNext()) {
                ChannelItem channelItem = convertCursorToChannelItem(cursor);
                items.add(channelItem);
            }

            Collections.sort(items, COMPARATOR);

            cursor.close();


            return items;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            open();
        }

        @Override
        protected void onPostExecute(List<ChannelItem> channelItems) {
            super.onPostExecute(channelItems);
            close();
            if (mCallback != null) {
                mCallback.onGetChannelsSuccess(channelItems);
            }
        }
    }

    private ChannelItem convertCursorToChannelItem(Cursor cursor) {

        Gson gson = new Gson();

        ChannelItem channelItem = new ChannelItem();

        channelItem.localId = cursor.getInt(cursor.getColumnIndexOrThrow(ChannelEntry._ID));
        channelItem.displayName = gson.fromJson(cursor.getString(
                cursor.getColumnIndexOrThrow(ChannelEntry.COLUMN_DISPLAY_NAME)), ChannelItem.DisplayName.class);

        channelItem.uid = cursor.getString(cursor.getColumnIndexOrThrow(ChannelEntry.COLUMN_UID));
        channelItem.orgUid = cursor.getString(cursor.getColumnIndexOrThrow(ChannelEntry.COLUMN_ORG_UID));
        channelItem.orgName = cursor.getString(cursor.getColumnIndexOrThrow(ChannelEntry.COLUMN_ORG_NAME));
        channelItem.status = cursor.getString(cursor.getColumnIndexOrThrow(ChannelEntry.COLUMN_STATUS));
        channelItem.funnel_id = cursor.getInt(cursor.getColumnIndexOrThrow(ChannelEntry.COLUMN_FUNNEL_ID));
        channelItem.created = cursor.getLong(cursor.getColumnIndexOrThrow(ChannelEntry.COLUMN_CREATED));
        channelItem.lastUpdatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(ChannelEntry.COLUMN_LAST_UPDATE_AT));
        channelItem.unreadMessages = cursor.getInt(cursor.getColumnIndexOrThrow(ChannelEntry.COLUMN_UNREAD_MESSAGES));

        channelItem.users = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(cursor.getString(cursor.getColumnIndexOrThrow(ChannelEntry.COLUMN_USERS)));
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    channelItem.users.add(gson.fromJson(jsonObject.toString(), UserItem.class));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        channelItem.assignee = gson.fromJson(
                cursor.getString(cursor.getColumnIndexOrThrow(ChannelEntry.COLUMN_ASSIGNEE)), UserItem.class);

        channelItem.latestMessage = gson.fromJson(cursor.getString(
                cursor.getColumnIndexOrThrow(ChannelEntry.COLUMN_LATEST_MESSAGE)), ChannelItem.LatestMessage.class);

        channelItem.note = gson.fromJson(
                cursor.getString(cursor.getColumnIndexOrThrow(ChannelEntry.COLUMN_NOTE)), ChannelItem.Note.class);

        return channelItem;
    }

    private ContentValues convertChannelToContentValues(ChannelItem channel) {
        Gson gson = new Gson();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(ChannelEntry.COLUMN_DISPLAY_NAME, gson.toJson(channel.displayName));
        values.put(ChannelEntry.COLUMN_UID, channel.uid);
        values.put(ChannelEntry.COLUMN_ORG_UID, channel.orgUid);
        values.put(ChannelEntry.COLUMN_STATUS, channel.status);
        values.put(ChannelEntry.COLUMN_FUNNEL_ID, channel.funnel_id);
        values.put(ChannelEntry.COLUMN_CREATED, channel.created);
        values.put(ChannelEntry.COLUMN_USERS, gson.toJson(channel.users));
        values.put(ChannelEntry.COLUMN_ASSIGNEE, gson.toJson(channel.assignee));
        values.put(ChannelEntry.COLUMN_ICON_URL, channel.iconUrl);
        values.put(ChannelEntry.COLUMN_UNREAD_MESSAGES, channel.unreadMessages);
        values.put(ChannelEntry.COLUMN_LATEST_MESSAGE, gson.toJson(channel.latestMessage));
        values.put(ChannelEntry.COLUMN_LAST_UPDATE_AT, channel.lastUpdatedAt);
        values.put(ChannelEntry.COLUMN_NOTE, gson.toJson(channel.note));

        return values;
    }

    public interface SaveChannelsCallback {
        /** Save list of Channels finished*/
        void onSaveChannelsSuccess();
    }

    public interface GetChannelsCallback {
        /** Finish loading list of channels from database*/
        void onGetChannelsSuccess(List<ChannelItem> channels);
    }
}
