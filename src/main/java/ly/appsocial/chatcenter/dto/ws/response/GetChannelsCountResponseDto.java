/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.dto.ws.response;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class GetChannelsCountResponseDto {
    @SerializedName("all")
    public int all;

    @SerializedName("unassigned")
    public int unassigned;

    @SerializedName("assigned")
    public int assigned;

    @SerializedName("closed")
    public int close;

    @SerializedName("mine")
    public int mine;

    @SerializedName("unread")
    public int unread;

    @SerializedName("funnels")
    public JsonObject funnels;
}
