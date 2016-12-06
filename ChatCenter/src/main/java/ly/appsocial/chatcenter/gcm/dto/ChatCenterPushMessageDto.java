package ly.appsocial.chatcenter.gcm.dto;


import com.google.gson.annotations.SerializedName;

public class ChatCenterPushMessageDto {
	/** Alert message */
	@SerializedName("alert")
	public String alert;
    /** Badge number (number of unread message) */
    @SerializedName("badge")
    public int badge;
	/** Organization UID */
	@SerializedName("org_uid")
	public String org_uid;
	/** Channel UID */
	@SerializedName("channel_uid")
	public String channel_uid;
	/** Category */
	@SerializedName("category")
	public String category;
}
