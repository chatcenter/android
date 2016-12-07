package ly.appsocial.chatcenter.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.dto.ChatItem;
import ly.appsocial.chatcenter.dto.ResponseType;
import ly.appsocial.chatcenter.util.AuthUtil;
import ly.appsocial.chatcenter.widgets.BasicWidget;
import ly.appsocial.chatcenter.widgets.views.WidgetView;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreviewWidgetFragment extends Fragment {

	public String mContent;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.fragment_preview_widget, container, false);
		WidgetView widgetView = (WidgetView)layout.findViewById(R.id.widget);

		try {
			JSONObject content = new JSONObject(mContent);
			String userToken = AuthUtil.getUserToken(getContext());
			ChatItem item = new ChatItem();
			item.type = ResponseType.STICKER;
			item.setupContent(BasicWidget.class, content);
			if ( userToken != null && !userToken.isEmpty() ){
				widgetView.setupClientView(userToken, item, null);
			} else {
				widgetView.setupCustomerView(item, null);
			}
		} catch (JSONException e){
		}

		return layout;
	}

}
