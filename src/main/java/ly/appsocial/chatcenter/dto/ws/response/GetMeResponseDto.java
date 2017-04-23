package ly.appsocial.chatcenter.dto.ws.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ly.appsocial.chatcenter.dto.OrgItem;
import ly.appsocial.chatcenter.dto.UserItem;

public class GetMeResponseDto extends UserItem {

    @SerializedName("orgs")
    public List<OrgItem> orgs;
    @SerializedName("privilege")
    public  Privilege privilege;

    public class Privilege {
        @SerializedName("app")
        public List<String> app;
        @SerializedName("org")
        public List<String> org;
        @SerializedName("user")
        public List<String> user;
        @SerializedName("channel")
        public List<String> channel;
        @SerializedName("message")
        public List<String> message;
        @SerializedName("customer_report")
        public List<String> customerReport;
        @SerializedName("tag")
        public List<String> tag;
        @SerializedName("schedule")
        public List<String> schedule;
        @SerializedName("upload_file")
        public List<String> uploadFile;
        @SerializedName("fixed_phrase")
        public List<String> fixedPhrase;
        @SerializedName("funnel")
        public List<String> funnel;
    }
}
