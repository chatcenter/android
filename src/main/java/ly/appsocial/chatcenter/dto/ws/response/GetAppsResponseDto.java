package ly.appsocial.chatcenter.dto.ws.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * [GET /api/users/:id] response.
 */
public class GetAppsResponseDto {
	@SerializedName("items")
	public List<App> items;

	public static class App implements Serializable {
		@SerializedName("id")
		public int id;
		@SerializedName("name")
		public String name;
		@SerializedName("uid")
		public String uid;
		@SerializedName("app_icons")
		public ArrayList<AppIcon> icons;
		@SerializedName("token")
		public String token;
		@SerializedName("stickers")
		public List<String> stickers;
	}

	public static class AppIcon implements Serializable {
		@SerializedName("name")
		public String name;

		@SerializedName("icon_url")
		public String iconUrl;
	}
}
