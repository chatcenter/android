package ly.appsocial.chatcenter.ws.parser;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ly.appsocial.chatcenter.dto.ChatItem;
import ly.appsocial.chatcenter.dto.ResponseType;
import ly.appsocial.chatcenter.dto.UserItem;
import ly.appsocial.chatcenter.dto.ws.response.GetMessagesResponseDto;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.widgets.BasicWidget;
import ly.appsocial.chatcenter.widgets.VideoCallWidget;
import ly.appsocial.chatcenter.ws.ApiRequest;

/**
 * [GET /api/channels/:channel_uid/messages] parser.
 */
public class GetMessagesParser implements ApiRequest.Parser<GetMessagesResponseDto> {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** エラーコード */
	private int mErrorCode;

	// //////////////////////////////////////////////////////////////////////////
	// イベントメソッド
	// //////////////////////////////////////////////////////////////////////////

	@Override
	public int getErrorCode() {
		return mErrorCode;
	}

	@Override
	public GetMessagesResponseDto parser(String response) {

		GetMessagesResponseDto resps = new GetMessagesResponseDto();
		try {
			JSONArray itemArray = new JSONArray(response);
			for (int i = 0; i < itemArray.length(); i++ ){
				JSONObject obj = itemArray.getJSONObject(i);
				if ( obj != null ){
					ChatItem item = new Gson().fromJson(obj.toString(), ChatItem.class);
					if ( item != null ){
						if (item.isRead()) {
							item.localStatus = ChatItem.ChatItemStatus.READ;
						} else {
							item.localStatus = ChatItem.ChatItemStatus.SENT;
						}

						if (item.type.equals(ResponseType.INFORMATION)) {
							// Skip message type information for now
							continue;
						} else if ( item.type.equals(ResponseType.CALL) ){
							item.setupContent(VideoCallWidget.class, obj);
						} else if (item.type.equals(ResponseType.SUGGESTION)) {
							item.setupContent(BasicWidget.class, obj);
						}else {
							item.setupContent(BasicWidget.class, obj);
						}

						/** add message to list*/
						resps.items.add(item.rebuildChatItem());
					}
				}
			}
			return resps;
		} catch (JSONException e){
			mErrorCode = -1;
			return null;
		}
	}
}
