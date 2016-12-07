package ly.appsocial.chatcenter.widgets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by karasawa on 2016/10/27.
 */

public class Widget {
	private static final String TAG = BasicWidget.class.getSimpleName();

	public JSONObject content = null;

	public Widget(){
		//jsonData = new JSONObject();
	}

	public void setJsonObject(JSONObject data){
		content = data;
	}


	// Utility Functions

	public String getString(String name){
		try {
			return content.getString(name);
		} catch(JSONException e){
			return null;
		}
	}

	public Integer getInt(String name){
		try {
			return content.getInt(name);
		} catch(JSONException e){
			return null;
		}
	}

	public JSONObject getJSONObject(String name){
		try {
			return content.getJSONObject(name);
		} catch(JSONException e){
			return null;
		}
	}

	public JSONArray getJSONArray(String name){
		if ( content == null || name == null )
			return null;
		try {
			return content.getJSONArray(name);
		} catch(JSONException e){
			return null;
		}
	}

}
