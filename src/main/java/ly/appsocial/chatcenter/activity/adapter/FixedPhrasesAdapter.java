package ly.appsocial.chatcenter.activity.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.FixedPhraseActivity;
import ly.appsocial.chatcenter.widgets.views.WidgetView;


public class FixedPhrasesAdapter extends ArrayAdapter<FixedPhraseActivity.FPListItem> {

    private List<FixedPhraseActivity.FPListItem> mChatItems;
    private OnFixedPhrasesItemClickListener mOnItemClickListener;

    public FixedPhrasesAdapter(Context context, int resource, List<FixedPhraseActivity.FPListItem> objects) {
        super(context, resource, objects);

        mChatItems = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final FixedPhraseActivity.FPListItem item = getItem(position);

        if (item.getType() == FixedPhraseActivity.FPListItem.TYPE_STICKER) {

            final FixedPhraseActivity.FPListItemSticker stickerItem = (FixedPhraseActivity.FPListItemSticker) item;

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_fixed_phrase, parent, false);

            WidgetView view = (WidgetView) convertView.findViewById(R.id.fixed_phrase_view);
            View overlayView = convertView.findViewById(R.id.fixed_phrase_overlay);

            view.setupCustomerView(stickerItem.getChatItem(), null, false);

            overlayView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(stickerItem);
                    }
                }
            });

            return convertView;
        } else if (item.getType() == FixedPhraseActivity.FPListItem.TYPE_SESSION_EMPTY) {

            int verticalPadding = (int) convertDpToPixel(20, getContext());
            int horizontalPadding = (int) convertDpToPixel(10, getContext());

            FixedPhraseActivity.FPListItemSessionEmptyLabel labelItem = (FixedPhraseActivity.FPListItemSessionEmptyLabel) item;
            TextView textView = new TextView(getContext());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            textView.setLayoutParams(params);
            textView.setText(labelItem.getLabel());
            textView.setTextColor(getContext().getResources().getColor(R.color.color_chatcenter_text));
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);

            return textView;
        }
//        else {
//            int verticalPadding = (int) convertDpToPixel(5, getContext());
//            int horizontalPadding = (int) convertDpToPixel(10, getContext());
//
//            FixedPhraseActivity.FPListItemSessionLabel labelItem = (FixedPhraseActivity.FPListItemSessionLabel) item;
//            TextView textView = new TextView(getContext());
//            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            textView.setLayoutParams(params);
//            textView.setText(labelItem.getLabel());
//            textView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
//            textView.setTypeface(null, Typeface.BOLD);
//            textView.setBackgroundColor(getContext().getResources().getColor(R.color.color_chatcenter_widget_background));
//
//            return textView;
//        }

        return convertView;
    }

    @Override
    public int getCount() {
        return mChatItems.size();
    }

    @Override
    public FixedPhraseActivity.FPListItem getItem(int position) {
        return mChatItems.get(position);
    }

    public void setOnItemClickListener(OnFixedPhrasesItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnFixedPhrasesItemClickListener{
        void onItemClick(FixedPhraseActivity.FPListItemSticker item);
    }

    public float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }
}
