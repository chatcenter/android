package ly.appsocial.chatcenter.activity.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ly.appsocial.chatcenter.R;

public class WidgetMenuGridAdapter extends ArrayAdapter<WidgetMenuGridAdapter.MenuButton> {


    private List<MenuButton> mButtons;
    Context mContext;
    WidgetMenuClickListener mListener;

    public WidgetMenuGridAdapter(Context context, int resource, List<MenuButton> objects, WidgetMenuClickListener listener) {
        super(context, resource, objects);
        mContext = context;
        mButtons = objects;
        mListener = listener;
    }


    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.view_widget_menu_button, parent, false);

        final MenuButton button = getItem(position);

        ImageView mButton = (ImageView) convertView.findViewById(R.id.sticker_menu_button);
        mButton.setImageResource(button.btIcon);

        TextView mLabel = (TextView) convertView.findViewById(R.id.sticker_menu_label);
        mLabel.setText(button.btText);

        convertView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.onMenuButtonClicked(button.getBtType());
            }
        });

        return convertView;
    }

    @Override
    public int getCount() {
        return mButtons.size();
    }

    @Override
    public MenuButton getItem(int position) {
        return mButtons.get(position);
    }

    public interface WidgetMenuClickListener {
        void onMenuButtonClicked(String type);
    }

    public static class MenuButton {
        private String btType;
        private int btIcon;
        private String btText;

        public MenuButton(String type, int iconId, String text) {
            this.btType = type;
            this.btIcon = iconId;
            this.btText = text;
        }

        public String getBtText() {
            return btText;
        }

        public void setBtText(String btText) {
            this.btText = btText;
        }

        public int getBtIcon() {
            return btIcon;
        }

        public void setBtIcon(int btIcon) {
            this.btIcon = btIcon;
        }

        public String getBtType() {
            return btType;
        }

        public void setBtType(String btType) {
            this.btType = btType;
        }
    }
}
