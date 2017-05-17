/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.dto.ws.response;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class GetChannelsCountResponseDto {
    @SerializedName("all")
    public String all;

    @SerializedName("unassigned")
    public String unassigned;

    @SerializedName("assigned")
    public String assigned;

    @SerializedName("closed")
    public String close;

    @SerializedName("mine")
    public String mine;

    @SerializedName("unread")
    public String unread;

    @SerializedName("funnels")
    public JsonObject funnels;
}
