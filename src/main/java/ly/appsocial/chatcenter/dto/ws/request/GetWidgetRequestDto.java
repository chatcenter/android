package ly.appsocial.chatcenter.dto.ws.request;

import java.util.HashMap;
import java.util.Map;

import ly.appsocial.chatcenter.constants.ChatCenterConstants;

/**
 * [GET /api/channels/:channel_uid/messages] request.
 */
public class GetWidgetRequestDto {

	public int lastId;
	public Integer maxLoadNum = ChatCenterConstants.MAX_MESSAGE_ON_LOAD;
	public String stickerType;

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * リクストパラメータを生成します。
	 * 
	 * @return リクエストパラメータ
	 */
	public Map<String, String> toParams() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("sticker_type", stickerType);
		params.put("limit", maxLoadNum.toString());
		if ( lastId > 0 ){
			params.put("last_id", String.valueOf(lastId));
		}
		return params;
	}
}
