package ly.appsocial.chatcenter.dto.ws.request;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Trung on 11/29/2015.
 */
public class PostChannelsRequestDto {

    public String orgUID;
    public String storeID;
    public Map<String, String> channelInformation;

    /**
     * リクストパラメータを生成します。
     *
     * @return リクエストパラメータ
     */
    public Map<String, String> toParams() {
        Map<String, String> params = new HashMap<>();
        params.put("org_uid", orgUID);
        if (storeID != null) {
            params.put("store_id", storeID);
        }
        if (channelInformation != null) {
            for (String key : channelInformation.keySet()) {
                params.put("channel_informations[" + key + "]", channelInformation.get(key));
            }
        }
        return params;
    }
}
