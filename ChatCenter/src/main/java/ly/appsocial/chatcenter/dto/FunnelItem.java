package ly.appsocial.chatcenter.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class FunnelItem implements Serializable {

    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("order")
    public int order;

    public boolean isSelected;
}
