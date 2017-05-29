package ly.appsocial.chatcenter.dto.ws.request;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WsChannelActiveRequest {

    // //////////////////////////////////////////////////////////////////////////
    // インスタンスフィールド
    // //////////////////////////////////////////////////////////////////////////

    /** チャンネルUID */
    public String channelUid;

    // //////////////////////////////////////////////////////////////////////////
    // パブリックメソッド
    // //////////////////////////////////////////////////////////////////////////

    /**
     * @return
     */
    public String toJson() {
        try {
            JSONObject dataObject = new JSONObject();
            dataObject.put("channel_uid", channelUid);

            JSONArray jsonArray = new JSONArray();
            jsonArray.put("channel:active");
            jsonArray.put(dataObject);

            return jsonArray.toString();
        } catch (JSONException e) {
            return "";
        }
    }
}
