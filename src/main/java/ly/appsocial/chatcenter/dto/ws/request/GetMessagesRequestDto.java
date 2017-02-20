package ly.appsocial.chatcenter.dto.ws.request;

import java.util.HashMap;
import java.util.Map;

import ly.appsocial.chatcenter.constants.ChatCenterConstants;

/**
 * [GET /api/channels/:channel_uid/messages] request.
 */
public class GetMessagesRequestDto {

	public Integer lastId = null;
	public Integer maxLoadNum = ChatCenterConstants.MAX_MESSAGE_ON_LOAD;

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
		params.put("limit", maxLoadNum.toString());
		if ( lastId != null ){
			params.put("last_id", lastId.toString());
		}
		return params;
	}
}
