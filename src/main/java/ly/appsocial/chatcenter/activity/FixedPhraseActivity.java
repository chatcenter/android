package ly.appsocial.chatcenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.adapter.FixedPhrasesAdapter;
import ly.appsocial.chatcenter.dto.ChatItem;
import ly.appsocial.chatcenter.dto.ResponseType;
import ly.appsocial.chatcenter.dto.ws.response.GetFixedPhraseResponseDto;
import ly.appsocial.chatcenter.fragment.ProgressDialogFragment;
import ly.appsocial.chatcenter.util.AuthUtil;
import ly.appsocial.chatcenter.util.DialogUtil;
import ly.appsocial.chatcenter.util.NetworkQueueHelper;
import ly.appsocial.chatcenter.ws.ApiRequest;
import ly.appsocial.chatcenter.ws.OkHttpApiRequest;
import ly.appsocial.chatcenter.ws.parser.GetFixedPhraseParser;

public class FixedPhraseActivity extends BaseActivity implements View.OnClickListener,
        ProgressDialogFragment.DialogListener, FixedPhrasesAdapter.OnFixedPhrasesItemClickListener,
        RadioGroup.OnCheckedChangeListener{

    public static final String ORG_UID = "org_uid";
    public static final String API_TOKEN = "api_token";

    /**
     * リクエストタグ
     */
    private static final String REQUEST_TAG = FixedPhraseActivity.class.getCanonicalName();
    public static final String SELECTED_CONTENT = "selected_fixed_phrase";
    public static final String SELECTED_TYPE = "selected_type";

    private Button mButtonCancel;
    private ListView mLvFixedPhrases;
    private RadioGroup mSegmentController;

    private String mOrgUid;
    private String mApiToken;

    private List<FPListItem> mListItems;
    private List<FPListItem> mListMineItems;
    private List<FPListItem> mListTeamItems;
    private List<FPListItem> mListEveryoneItems;
    private FixedPhrasesAdapter mFixedPhrasesAdapter;

    /**
     * GET /api/fixed_phrases/
     */
    private ApiRequest<GetFixedPhraseResponseDto> mGetFixedPhraseRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mOrgUid = getIntent().getStringExtra(ORG_UID);
        mApiToken = getIntent().getStringExtra(API_TOKEN);

        setContentView(R.layout.activity_fixed_phrase);

        mButtonCancel = (Button) findViewById(R.id.bt_cancel);
        mButtonCancel.setOnClickListener(this);

        mLvFixedPhrases = (ListView) findViewById(R.id.lv_fixed_phrases);

        mListItems = new ArrayList<>();
        mListMineItems = new ArrayList<>();
        mListTeamItems = new ArrayList<>();
        mListEveryoneItems = new ArrayList<>();

        mFixedPhrasesAdapter = new FixedPhrasesAdapter(this, 0, mListItems);
        mFixedPhrasesAdapter.setOnItemClickListener(this);
        mLvFixedPhrases.setAdapter(mFixedPhrasesAdapter);

        mSegmentController = (RadioGroup) findViewById(R.id.segment_controller);
        mSegmentController.setOnCheckedChangeListener(this);

        requestGetFixedPhrase();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mOkHttpClient.cancel(REQUEST_TAG);
        mGetFixedPhraseRequest = null;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.bt_cancel) {
            Intent intent = new Intent();

            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }

    /**
     * GET /api/fixed_phrases?org_uid
     */
    private void requestGetFixedPhrase() {
        if (mGetFixedPhraseRequest != null) {
            return;
        }

        String path = "fixed_phrases?org_uid=" + mOrgUid;

        Map<String, String> headers = new HashMap<>();
        headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

        mGetFixedPhraseRequest = new OkHttpApiRequest<>(getApplicationContext(), ApiRequest.Method.GET, path, null, headers, new GetFixedPhraseCallback(), new GetFixedPhraseParser());
        mGetFixedPhraseRequest.setApiToken(mApiToken);

        NetworkQueueHelper.enqueue(mGetFixedPhraseRequest, REQUEST_TAG);

        DialogUtil.showProgressDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
    }

    /**
     * ダイアログをキャンセルした際のコールバック。
     *
     * @param tag このフラグメントのタグ
     */
    @Override
    public void onDialogCancel(String tag) {
        if (DialogUtil.Tag.ERROR_401.equals(tag)) { // 401エラー
            finish();
        }
    }

    @Override
    public void onItemClick(FPListItemSticker item) {
        Gson gson = new Gson();
        Intent intent = new Intent();
        intent.putExtra(SELECTED_TYPE, item.getContentType());
        if (item != null
                && item.getContentType().equals(ResponseType.MESSAGE)
                && item.getChatItem() != null
                && item.getChatItem().widget != null
                && item.getChatItem().widget.message != null) {
            intent.putExtra(SELECTED_CONTENT, item.getChatItem().widget.message.text);
        } else {
            if (item != null
                    && item.getChatItem() != null
                    && item.getChatItem().widget != null) {
                intent.putExtra(SELECTED_CONTENT, gson.toJson(item.getChatItem().widget).toString());
            }
        }

        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * <p>Called when the checked radio button has changed. When the
     * selection is cleared, checkedId is -1.</p>
     *
     * @param group     the group in which the checked radio button has changed
     * @param checkedId the unique identifier of the newly checked radio button
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        mListItems.clear();
        if (checkedId == R.id.radio_mine) {
            mListItems.addAll(mListMineItems);
        } else if (checkedId == R.id.radio_team) {
            mListItems.addAll(mListTeamItems);
        } else if (checkedId == R.id.radio_everyone) {
            mListItems.addAll(mListEveryoneItems);
        }

        mFixedPhrasesAdapter.notifyDataSetChanged();
    }

    /**
     * GET /api/users/:id のコールバック
     */
    private class GetFixedPhraseCallback implements OkHttpApiRequest.Callback<GetFixedPhraseResponseDto> {
        @Override
        public void onError(OkHttpApiRequest.Error error) {
            mGetFixedPhraseRequest = null;
            DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);

            if (!isAuthErrorWithAlert(error)) {
                // 共通エラーダイアログの表示
                DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR, null, getString(R.string.dialog_error_body));
            }
        }

        @Override
        public void onSuccess(GetFixedPhraseResponseDto responseDto) {
            mGetFixedPhraseRequest = null;
            DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);

            if (responseDto == null || responseDto.orgFixedPhrases == null) {
                return;
            }

            // APPの定型ステッカー
            mListEveryoneItems.clear();
            if (responseDto.appFixedPhrases != null && responseDto.appFixedPhrases.size() > 0) {
                for (ChatItem item : responseDto.appFixedPhrases) {
                    FPListItemSticker itemSticker = new FPListItemSticker(item);
                    itemSticker.setContentType(item.type);
                    item.type = ResponseType.STICKER;
                    mListEveryoneItems.add(itemSticker);
                }
            } else {
                mListEveryoneItems.add(new FPListItemSessionEmptyLabel(getString(R.string.fixed_phrases_no_app_fixed_phrases)));
            }

            // ORGの定型ステッカー
            mListTeamItems.clear();
            if (responseDto.orgFixedPhrases != null && responseDto.orgFixedPhrases.size() > 0) {
                for (ChatItem item : responseDto.orgFixedPhrases) {
                    FPListItemSticker itemSticker = new FPListItemSticker(item);
                    itemSticker.setContentType(item.type);
                    item.type = ResponseType.STICKER;
                    mListTeamItems.add(itemSticker);
                }
            } else {
                mListTeamItems.add(new FPListItemSessionEmptyLabel(getString(R.string.fixed_phrases_no_org_fixed_phrases)));
            }

            // ユーザーの定型ステッカー
            mListMineItems.clear();
            if (responseDto.userFixedPhrases != null && responseDto.userFixedPhrases.size() > 0) {
                for (ChatItem item : responseDto.userFixedPhrases) {
                    FPListItemSticker itemSticker = new FPListItemSticker(item);
                    itemSticker.setContentType(item.type);
                    item.type = ResponseType.STICKER;
                    mListMineItems.add(itemSticker);
                }
            } else {
                mListMineItems.add(new FPListItemSessionEmptyLabel(getString(R.string.fixed_phrases_myphrase_empty)));
            }

            mListItems.clear();
            mListItems.addAll(mListMineItems);
            mFixedPhrasesAdapter.notifyDataSetChanged();
        }
    }

    /**
     * API が認証エラーか判定し、認証エラーの場合は認証エラーダイアログを表示します。
     *
     * @param error OkHttp's error response
     * @return 認証エラーの場合は true、そうでない場合は false
     */
    private boolean isAuthErrorWithAlert(OkHttpApiRequest.Error error) {
        if (error.getNetworkResponse() != null && error.getNetworkResponse().code() != 401) {
            return false;
        }
        DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR_401, null, getString(R.string.dialog_auth_error_body));
        return true;
    }

    /** 定型ステッカー一覧に表示される項目 */
    public static class FPListItem {
        public static int TYPE_STICKER = 1;
//        public static int TYPE_SESSION_LABEL = 2;
        public static int TYPE_SESSION_EMPTY = 3;

        private int type;

        private String contentType;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }
    }

//    public static class FPListItemSessionLabel extends FPListItem{
//        private String label;
//
//        public FPListItemSessionLabel (String label) {
//            this.label = label;
//            setType(TYPE_SESSION_LABEL);
//        }
//
//        public String getLabel() {
//            return label;
//        }
//
//        public void setLabel(String label) {
//            this.label = label;
//        }
//    }

    public static class FPListItemSessionEmptyLabel extends FPListItem{
        private String label;

        public FPListItemSessionEmptyLabel (String label) {
            this.label = label;
            setType(TYPE_SESSION_EMPTY);
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    public static class FPListItemSticker extends FPListItem {
        private ChatItem mChatItem;

        public FPListItemSticker(ChatItem chatItem) {
            this.mChatItem = chatItem;
            setType(TYPE_STICKER);
        }

        public ChatItem getChatItem() {
            return mChatItem;
        }

        public void setChatItem(ChatItem chatItem) {
            mChatItem = chatItem;
        }
    }
}
