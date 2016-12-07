package ly.appsocial.chatcenter.dto.ws.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import ly.appsocial.chatcenter.dto.FunnelItem;

public class GetFunnelResponseDto implements Serializable{

    @SerializedName("funnels")
    public List<FunnelItem> funnels;

}
