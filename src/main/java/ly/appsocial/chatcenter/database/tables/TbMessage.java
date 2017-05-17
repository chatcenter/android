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
import java.util.HashMap;
import java.util.List;

import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.ChatItem;
import ly.appsocial.chatcenter.dto.ResponseType;
import ly.appsocial.chatcenter.dto.UserItem;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.widgets.BasicWidget;
import ly.appsocial.chatcenter.widgets.VideoCallWidget;

public class TbMessage extends TBBase {

    private static final String LAST_MESSAGE_CREATED = "last_message_id";
    private final String ORG_UID = "org_uid";
    private final String CHANNEL_UID = "channel_uid";
    private static final int LIMIT = ChatCenterConstants.MAX_MESSAGE_ON_LOAD;


    public TbMessage(Context context) {
        super(context);
    }

    /* Inner class that defines the table contents */
    public static class MessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "CC_MESSAGE";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_UID = "uid";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_ANSWER = "answer";
        public static final String COLUMN_ANSWERS = "answers";
        public static final String COLUMN_QUESTION = "question";
        public static final String COLUMN_CREATED = "created";
        public static final String COLUMN_CHANNEL_UID = "channel_uid";
        public static final String COLUMN_CHANNEL_ID = "channel_id";
        public static final String COLUMN_USER = "user";
        public static final String COLUMN_USERS_READ_MESSAGE = "users_read_message";
        public static final String COLUMN_ORG_UID = "org_uid";
        public static final String COLUMN_RAW_CONTENT = "raw_content";
        public static final String COLUMN_STICKER_TYPE = "sticker_type";
    }


    /*To create new table use below string*/
    public static final String SQL_CREATE_TABLE = "CREATE TABLE "
            + MessageEntry.TABLE_NAME + " ("
            + MessageEntry._ID + " INTEGER PRIMARY KEY,"
            + MessageEntry.COLUMN_ID + " INTEGER,"
            + MessageEntry.COLUMN_UID + " TEXT,"
            + MessageEntry.COLUMN_CONTENT + " TEXT,"
            + MessageEntry.COLUMN_TYPE + " TEXT,"
            + MessageEntry.COLUMN_STATUS + " INTEGER,"
            + MessageEntry.COLUMN_ANSWER + " TEXT,"
            + MessageEntry.COLUMN_ANSWERS + " TEXT,"
            + MessageEntry.COLUMN_QUESTION + " TEXT,"
            + MessageEntry.COLUMN_CHANNEL_UID + " TEXT,"
            + MessageEntry.COLUMN_CHANNEL_ID + " INTEGER,"
            + MessageEntry.COLUMN_USER + " INTEGER,"
            + MessageEntry.COLUMN_CREATED + " INTEGER,"
            + MessageEntry.COLUMN_USERS_READ_MESSAGE + " INTEGER,"
            + MessageEntry.COLUMN_ORG_UID +  " TEXT,"
            + MessageEntry.COLUMN_RAW_CONTENT +  " TEXT,"
            + MessageEntry.COLUMN_STICKER_TYPE +  " TEXT"
            + ")";


    /* Statement to drop table*/
    public static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + MessageEntry.TABLE_NAME;


    /**
     * Insert the new row, returning the primary key value of the new row
     * @param chatItem
     * @return
     */
    private long insert(ChatItem chatItem) {
        if (chatItem == null) {
            return -1;
        }

        open();

        ContentValues values = convertChatItemToContentValues(chatItem);
        long rowId = mWritableDB.insert(MessageEntry.TABLE_NAME, null, values);

        close();

        return rowId;
    }

    /**
     * Update an exists chatItem
     * @param chatItem
     * @return
     */
    private long update(ChatItem chatItem) {
        if (chatItem == null) {
            return -1;
        }

        open();

        ContentValues values = convertChatItemToContentValues(chatItem);

        String selection;
        String[] selectionArgs;

        if (chatItem.widget != null && StringUtil.isNotBlank(chatItem.widget.uid)) {
            selection = MessageEntry.COLUMN_UID + " = ?"
                    + " AND "
                    + MessageEntry.COLUMN_CHANNEL_UID + " = ?"
                    + " AND "
                    + MessageEntry.COLUMN_ORG_UID + " = ?";
            selectionArgs = new String[]{chatItem.widget.uid, chatItem.channelUid, chatItem.orgUid};
        } else {
            selection = MessageEntry.COLUMN_ID + " = ?"
                    + " AND "
                    + MessageEntry.COLUMN_CHANNEL_UID + " = ?"
                    + " AND "
                    + MessageEntry.COLUMN_ORG_UID + " = ?";
            selectionArgs = new String[]{String.valueOf(chatItem.id), chatItem.channelUid, chatItem.orgUid};
        }

        long rowId = mWritableDB.update(MessageEntry.TABLE_NAME, values, selection, selectionArgs);

        close();

        return rowId;
    }

    /**
     * If chatItem is already inserted, update chatItem info.
     * Insert new chatItem if it has not been inserted
     *
     * @param chatItem
     * @return
     */
    public long updateOrInsert(ChatItem chatItem) {

        // Update message status
        if (chatItem.localStatus == null) {
            if (chatItem.isRead()) {
                chatItem.localStatus = ChatItem.ChatItemStatus.READ;
            } else {
                chatItem.localStatus = ChatItem.ChatItemStatus.SENT;
            }
        }

        if (isChatItemExists(chatItem)) {
            return update(chatItem);
        } else {
            return insert(chatItem);
        }
    }

    /**
     * Insert or update a list of message
     * @param chatItems
     */
    public void saveListMessages(List<ChatItem> chatItems, SaveMessagesCallback callback) {
        new SaveMessagesTask(callback).execute(chatItems);
    }

    /**
     * Get all messages of channel
     * @param orgUid
     * @return
     */
    public void getListMessageInChannel(String channelUid, String orgUid, String lastMessageCreated, GetMessagesCallback callback) {
        HashMap<String, String> param = new HashMap<>();
        param.put(CHANNEL_UID, channelUid);
        param.put(ORG_UID, orgUid);
        if (StringUtil.isNotBlank(lastMessageCreated)) {
            param.put(LAST_MESSAGE_CREATED, lastMessageCreated);
        } else {
            param.put(LAST_MESSAGE_CREATED, Calendar.getInstance().getTimeInMillis() + "");
        }

        new GetMessagesTask(callback).execute(param);
    }

    public void getListFailedMessageInChannel(String channelUid, String orgUid, GetFailedMessagesCallback callback) {
        HashMap<String, String> param = new HashMap<>();
        param.put(CHANNEL_UID, channelUid);
        param.put(ORG_UID, orgUid);

        new GetFailedMessagesTask(callback).execute(param);
    }

    public int deleteMessageInChannel(String orgUid, String channelUid) {
        open();

        String selection = MessageEntry.COLUMN_ORG_UID + " = ? AND " + MessageEntry.COLUMN_CHANNEL_UID + " = ?";
        String[] selectionArgs = new String[]{orgUid, channelUid};

        int numberDeleted = mWritableDB.delete(MessageEntry.TABLE_NAME, selection, selectionArgs);

        close();

        return numberDeleted;
    }

    public boolean isChatItemExists(ChatItem chatItem) {
        if (chatItem == null) {
            return false;
        }

        open();

        String[] projection = new String[] {MessageEntry.COLUMN_ORG_UID};

        String selection;
        String[] selectionArgs;
        if (chatItem.widget != null && StringUtil.isNotBlank(chatItem.widget.uid)) {
            selection = MessageEntry.COLUMN_UID + " = ?"
                    + " AND "
                    + MessageEntry.COLUMN_CHANNEL_UID + " = ?"
                    + " AND "
                    + MessageEntry.COLUMN_ORG_UID + " = ?";
            selectionArgs = new String[]{chatItem.widget.uid, chatItem.channelUid, chatItem.orgUid};
        } else {
            selection = MessageEntry.COLUMN_ID + " = ?"
                    + " AND "
                    + MessageEntry.COLUMN_CHANNEL_UID + " = ?"
                    + " AND "
                    + MessageEntry.COLUMN_ORG_UID + " = ?";
            selectionArgs = new String[]{String.valueOf(chatItem.id), chatItem.channelUid, chatItem.orgUid};
        }

        Cursor cursor = mReadableDB.query(MessageEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);
        boolean existing = false;
        if (cursor != null) {
            existing = cursor.getCount() > 0;
            cursor.close();
        }
        close();

        return existing;
    }

    /** Delete all channel from database*/
    public int clearTable() {
        open();
        int numberRowDeleted = mWritableDB.delete(MessageEntry.TABLE_NAME, null, null);
        close();

        return numberRowDeleted;
    }

    public int delete(ChatItem chatItem) {

        open();

        String selection = MessageEntry._ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(chatItem.localId)};

        int numberDeleted = mWritableDB.delete(MessageEntry.TABLE_NAME, selection, selectionArgs);

        close();

        return numberDeleted;
    }

    public ChatItem getMessage(String messageUid, String channelUid, String orgUid) {
        open();
        ChatItem item = null;
        Cursor cursor;

        String[] projection = new String[]{
                MessageEntry._ID,
                MessageEntry.COLUMN_ID,
                MessageEntry.COLUMN_CONTENT,
                MessageEntry.COLUMN_TYPE,
                MessageEntry.COLUMN_STATUS,
                MessageEntry.COLUMN_CHANNEL_UID,
                MessageEntry.COLUMN_CHANNEL_ID,
                MessageEntry.COLUMN_USER,
                MessageEntry.COLUMN_USERS_READ_MESSAGE,
                MessageEntry.COLUMN_CREATED,
                MessageEntry.COLUMN_ORG_UID,
                MessageEntry.COLUMN_RAW_CONTENT,
                MessageEntry.COLUMN_STICKER_TYPE
        };

        String selection = MessageEntry.COLUMN_UID + " = ? AND "
                + MessageEntry.COLUMN_CHANNEL_UID + " = ? AND "
                + MessageEntry.COLUMN_ORG_UID + " =?";
        String[] selectionArgs = new String[] {messageUid, channelUid, orgUid};

        cursor = mReadableDB.query(MessageEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            item = convertCursorToChatItem(cursor);
        }
        cursor.close();
        close();
        return item;
    }

    /**
     * Get draft message that you have saved before
     * @param channelUid
     * @param orgUid
     * @return
     */
    public ChatItem getDraftMessage(String channelUid, String orgUid) {
        open();
        ChatItem item = null;
        Cursor cursor;

        String[] projection = new String[]{
                MessageEntry._ID,
                MessageEntry.COLUMN_ID,
                MessageEntry.COLUMN_CONTENT,
                MessageEntry.COLUMN_TYPE,
                MessageEntry.COLUMN_STATUS,
                MessageEntry.COLUMN_CHANNEL_UID,
                MessageEntry.COLUMN_CHANNEL_ID,
                MessageEntry.COLUMN_USER,
                MessageEntry.COLUMN_USERS_READ_MESSAGE,
                MessageEntry.COLUMN_CREATED,
                MessageEntry.COLUMN_ORG_UID,
                MessageEntry.COLUMN_RAW_CONTENT,
                MessageEntry.COLUMN_STICKER_TYPE
        };

        String selection = MessageEntry.COLUMN_CHANNEL_UID + " = ? AND "
                + MessageEntry.COLUMN_ORG_UID + " =? AND "
                + MessageEntry.COLUMN_STATUS + " =?";
        String[] selectionArgs = new String[] {channelUid, orgUid, ChatItem.ChatItemStatus.DRAFT.ordinal() + ""};

        cursor = mReadableDB.query(MessageEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            item = convertCursorToChatItem(cursor);
        }
        cursor.close();
        close();
        return item;
    }

    public ChatItem getMessage(int messageId, String channelUid, String orgUid) {
        open();
        ChatItem item = null;
        Cursor cursor;

        String[] projection = new String[]{
                MessageEntry._ID,
                MessageEntry.COLUMN_ID,
                MessageEntry.COLUMN_CONTENT,
                MessageEntry.COLUMN_TYPE,
                MessageEntry.COLUMN_STATUS,
                MessageEntry.COLUMN_CHANNEL_UID,
                MessageEntry.COLUMN_CHANNEL_ID,
                MessageEntry.COLUMN_USER,
                MessageEntry.COLUMN_USERS_READ_MESSAGE,
                MessageEntry.COLUMN_CREATED,
                MessageEntry.COLUMN_ORG_UID,
                MessageEntry.COLUMN_RAW_CONTENT,
                MessageEntry.COLUMN_STICKER_TYPE
        };

        String selection = MessageEntry.COLUMN_ID + " = ? AND "
                + MessageEntry.COLUMN_CHANNEL_UID + " = ? AND "
                + MessageEntry.COLUMN_ORG_UID + " =?";
        String[] selectionArgs = new String[] {String.valueOf(messageId), channelUid, orgUid};

        cursor = mReadableDB.query(MessageEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            item = convertCursorToChatItem(cursor);
        }
        cursor.close();
        close();
        return item;
    }

    private class SaveMessagesTask extends AsyncTask<List<ChatItem>, String, String> {

        private SaveMessagesCallback mCallback;

        public SaveMessagesTask(SaveMessagesCallback callback) {
            mCallback = callback;
        }

        @Override
        protected String doInBackground(List<ChatItem>... params) {

            if (params == null || params.length == 0) {
                return null;
            }

            List<ChatItem> chatItems = params[0];

            for (ChatItem chatItem: chatItems) {
                updateOrInsert(chatItem);
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
                mCallback.saveMessagesSuccess();
            }
        }
    }

    public class GetMessagesTask extends AsyncTask<HashMap<String, String>, String, List<ChatItem>> {

        private GetMessagesCallback mCallback;

        public GetMessagesTask(GetMessagesCallback callback) {
            mCallback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            open();
        }

        @Override
        protected List<ChatItem> doInBackground(HashMap<String, String>... params) {

            List<ChatItem> chatItems = new ArrayList<>();

            if (params == null || params.length == 0) {
                return chatItems;
            }

            HashMap<String, String> param = params[0];

            String channelUid = param.get(CHANNEL_UID);
            String orgUid = param.get(ORG_UID);
            String created = param.get(LAST_MESSAGE_CREATED);

            if (StringUtil.isNotBlank(orgUid)) {

                Cursor cursor;

                String[] projection = new String[]{
                        MessageEntry._ID,
                        MessageEntry.COLUMN_ID,
                        MessageEntry.COLUMN_CONTENT,
                        MessageEntry.COLUMN_TYPE,
                        MessageEntry.COLUMN_STATUS,
                        MessageEntry.COLUMN_CHANNEL_UID,
                        MessageEntry.COLUMN_CHANNEL_ID,
                        MessageEntry.COLUMN_USER,
                        MessageEntry.COLUMN_USERS_READ_MESSAGE,
                        MessageEntry.COLUMN_CREATED,
                        MessageEntry.COLUMN_ORG_UID,
                        MessageEntry.COLUMN_RAW_CONTENT,
                        MessageEntry.COLUMN_STICKER_TYPE
                };

                String selection = MessageEntry.COLUMN_CHANNEL_UID + " = ? AND "
                        + MessageEntry.COLUMN_ORG_UID + " = ? AND "
                        + MessageEntry.COLUMN_CREATED + " < ? AND "
                        + MessageEntry.COLUMN_STATUS + " <> ?";

                String[] selectionArgs = new String[]{channelUid, orgUid, created, ""
                        + ChatItem.ChatItemStatus.DRAFT.ordinal()};

                String order = MessageEntry.COLUMN_CREATED + " DESC";
                String limit = String.valueOf(LIMIT);

                cursor = mReadableDB.query(MessageEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        order,
                        limit);

                while (cursor != null && cursor.moveToNext()) {

                    chatItems.add(convertCursorToChatItem(cursor));
                }

                cursor.close();
            }

            return chatItems;
        }

        @Override
        protected void onPostExecute(List<ChatItem> chatItems) {
            super.onPostExecute(chatItems);
            close();

            if (mCallback != null && chatItems != null) {
                mCallback.onGetMessagesSuccess(chatItems);
            }
        }
    }

    public class GetFailedMessagesTask extends AsyncTask<HashMap<String, String>, String, List<ChatItem>> {

        private GetFailedMessagesCallback mCallback;

        public GetFailedMessagesTask(GetFailedMessagesCallback callback) {
            mCallback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            open();
        }

        @Override
        protected List<ChatItem> doInBackground(HashMap<String, String>... params) {

            List<ChatItem> chatItems = new ArrayList<>();

            if (params == null || params.length == 0) {
                return chatItems;
            }

            HashMap<String, String> param = params[0];

            String channelUid = param.get(CHANNEL_UID);
            String orgUid = param.get(ORG_UID);

            if (StringUtil.isNotBlank(orgUid)) {

                Cursor cursor;

                String[] projection = new String[]{
                        MessageEntry._ID,
                        MessageEntry.COLUMN_ID,
                        MessageEntry.COLUMN_CONTENT,
                        MessageEntry.COLUMN_TYPE,
                        MessageEntry.COLUMN_STATUS,
                        MessageEntry.COLUMN_CHANNEL_UID,
                        MessageEntry.COLUMN_CHANNEL_ID,
                        MessageEntry.COLUMN_USER,
                        MessageEntry.COLUMN_USERS_READ_MESSAGE,
                        MessageEntry.COLUMN_CREATED,
                        MessageEntry.COLUMN_ORG_UID,
                        MessageEntry.COLUMN_RAW_CONTENT,
                        MessageEntry.COLUMN_STICKER_TYPE
                };

                String selection = MessageEntry.COLUMN_CHANNEL_UID + " = ? AND "
                            + MessageEntry.COLUMN_ORG_UID + " = ? AND ("
                            + MessageEntry.COLUMN_STATUS + " = ? OR "
                            + MessageEntry.COLUMN_STATUS + " = ?)";

                String[] selectionArgs = new String[]{channelUid, orgUid,
                            String.valueOf(ChatItem.ChatItemStatus.SEND_FAILED.ordinal()),
                            String.valueOf(ChatItem.ChatItemStatus.SENDING.ordinal())};

                // String order = MessageEntry.COLUMN_CREATED + " DESC";

                cursor = mReadableDB.query(MessageEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);

                while (cursor != null && cursor.moveToNext()) {

                    chatItems.add(convertCursorToChatItem(cursor));
                }

                cursor.close();
            }

            return chatItems;
        }

        @Override
        protected void onPostExecute(List<ChatItem> chatItems) {
            super.onPostExecute(chatItems);
            close();

            if (mCallback != null && chatItems != null) {
                mCallback.onGetFailedMessagesSuccess(chatItems);
            }
        }
    }

    private ChatItem convertCursorToChatItem(Cursor cursor) {
        Gson gson = new Gson();

        ChatItem chatItem = new ChatItem();

        chatItem.localId = cursor.getInt(cursor.getColumnIndexOrThrow(MessageEntry._ID));
        chatItem.id = cursor.getInt(cursor.getColumnIndexOrThrow(MessageEntry.COLUMN_ID));
        chatItem.orgUid = cursor.getString(cursor.getColumnIndexOrThrow(MessageEntry.COLUMN_ORG_UID));
        chatItem.channelUid = cursor.getString(cursor.getColumnIndexOrThrow(MessageEntry.COLUMN_CHANNEL_UID));
        chatItem.channelId = cursor.getString(cursor.getColumnIndexOrThrow(MessageEntry.COLUMN_CHANNEL_ID));
        chatItem.type = cursor.getString(cursor.getColumnIndexOrThrow(MessageEntry.COLUMN_TYPE));
        chatItem.rawContent = cursor.getString(cursor.getColumnIndexOrThrow(MessageEntry.COLUMN_RAW_CONTENT));
        chatItem.created = cursor.getLong(cursor.getColumnIndexOrThrow(MessageEntry.COLUMN_CREATED));
        chatItem.localStatus = ChatItem.ChatItemStatus.values()[cursor.getInt(cursor.getColumnIndexOrThrow(MessageEntry.COLUMN_STATUS))];
        chatItem.stickerType = cursor.getString(cursor.getColumnIndexOrThrow(MessageEntry.COLUMN_STICKER_TYPE));

        if (chatItem.type.equals(ResponseType.CALL)) {
            chatItem.widget = gson.fromJson(
                    cursor.getString(cursor.getColumnIndexOrThrow(MessageEntry.COLUMN_CONTENT)), VideoCallWidget.class);
        } else {
            chatItem.widget = gson.fromJson(
                    cursor.getString(cursor.getColumnIndexOrThrow(MessageEntry.COLUMN_CONTENT)), BasicWidget.class);
        }

        chatItem.usersReadMessage = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(cursor.getString(cursor.getColumnIndexOrThrow(MessageEntry.COLUMN_USERS_READ_MESSAGE)));
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    chatItem.usersReadMessage.add(gson.fromJson(jsonObject.toString(), UserItem.class));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        chatItem.user = gson.fromJson(
                cursor.getString(cursor.getColumnIndexOrThrow(MessageEntry.COLUMN_USER)), UserItem.class);

        return chatItem;
    }

    private ContentValues convertChatItemToContentValues(ChatItem chatItem) {
        Gson gson = new Gson();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(MessageEntry.COLUMN_ID, chatItem.id);
        values.put(MessageEntry.COLUMN_UID, chatItem.widget.uid);
        values.put(MessageEntry.COLUMN_CONTENT, gson.toJson(chatItem.widget).toString());
        values.put(MessageEntry.COLUMN_TYPE, chatItem.type);
        values.put(MessageEntry.COLUMN_STATUS, chatItem.localStatus.ordinal());
        // values.put(MessageEntry.COLUMN_ANSWER, gson.toJson(chatItem.answer).toString());
        // values.put(MessageEntry.COLUMN_ANSWERS, gson.toJson(chatItem.answers).toString());
        // values.put(MessageEntry.COLUMN_QUESTION, gson.toJson(chatItem.question).toString());
        values.put(MessageEntry.COLUMN_CHANNEL_UID, chatItem.channelUid);
        values.put(MessageEntry.COLUMN_CHANNEL_ID, chatItem.channelId);
        values.put(MessageEntry.COLUMN_USER, gson.toJson(chatItem.user).toString());
        values.put(MessageEntry.COLUMN_CREATED, chatItem.created);
        values.put(MessageEntry.COLUMN_USERS_READ_MESSAGE, gson.toJson(chatItem.usersReadMessage).toString());
        values.put(MessageEntry.COLUMN_ORG_UID, chatItem.orgUid);
        values.put(MessageEntry.COLUMN_RAW_CONTENT, chatItem.rawContent);
        values.put(MessageEntry.COLUMN_STICKER_TYPE, chatItem.stickerType);

        return values;
    }

    public interface SaveMessagesCallback {
        void saveMessagesSuccess();
    }

    public interface  GetMessagesCallback {
        void onGetMessagesSuccess(List<ChatItem> chatItems);
    }

    public interface  GetFailedMessagesCallback {
        void onGetFailedMessagesSuccess(List<ChatItem> chatItems);
    }

}
