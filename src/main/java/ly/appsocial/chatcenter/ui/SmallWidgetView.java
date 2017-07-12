package ly.appsocial.chatcenter.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.dto.ChatItem;
import ly.appsocial.chatcenter.dto.ResponseType;
import ly.appsocial.chatcenter.dto.WidgetAction;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.widgets.BasicWidget;


/**
 * A custom view that will be used to display a widget with only icon and title
 */
public class SmallWidgetView extends LinearLayout {

    /** TextView to display title of widget*/
    private TextView mTvLabel;

    /** ImageView to display icon of widget*/
    private ImageView mImvIcon;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public SmallWidgetView(Context context) {
        super(context);
        createView(context);
    }

    /**
     * Constructor that is called when inflating a view from XML. This is called
     * when a view is being constructed from an XML file, supplying attributes
     * that were specified in the XML file. This version uses a default style of
     * 0, so the only attribute values applied are those in the Context's Theme
     * and the given AttributeSet.
     * <p>
     * <p>
     * The method onFinishInflate() will be called after all children have been
     * added.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public SmallWidgetView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        createView(context);
    }


    /** Init SmallWidgetView's components */
    private void createView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = inflater.inflate(R.layout.view_small_widget, this, true);

        mTvLabel = (TextView) rootView.findViewById(R.id.small_widget_title);
        mImvIcon = (ImageView) rootView.findViewById(R.id.small_view_icon);
    }

    /**
     * Bind view with Data of ChatItem
     * @param chatItem data will be shown on SmallWidgetView
     * @param onClickListener what happen when user clicked on SmallWidgetView
     */
    public void bindData(final ChatItem chatItem, final SmallWidgetTableView.OnItemClickListener onClickListener) {
        if (chatItem.widget != null) {
            if(chatItem.widget.message != null
                    && StringUtil.isNotBlank(chatItem.widget.message.text)) {
                mTvLabel.setText(chatItem.widget.message.text);
            }

            if (chatItem.widget.stickerAction != null
                    && StringUtil.isNotBlank(chatItem.widget.stickerAction.actionType)) {
                mImvIcon.setImageResource(chatItem.widget.getWidgetIcon(chatItem.widget.stickerAction.actionType));
                mImvIcon.setVisibility(View.VISIBLE);
            } else {
                mImvIcon.setVisibility(View.GONE);
            }
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onSmallWidgetClicked(chatItem);
                }
            }
        });
    }

    /**
     * Bind view with Data of ActionData
     * @param action data will be shown on SmallWidgetView
     * @param onClickListener what happen when user clicked on SmallWidgetView
     */
    public void bindData(final BasicWidget.StickerAction.ActionData action, final SmallWidgetTableView.OnItemClickListener onClickListener) {
        // Older suggestion object do not contain type
        if (StringUtil.isBlank(action.type)) {
            mImvIcon.setImageResource(R.drawable.ic_schedule);
        } else {
            if (action.type.equals(ResponseType.MESSAGE) || action.type.equals(ResponseType.URL)) {
                mImvIcon.setVisibility(View.GONE);
            } else {
                int iconDrawable = 0;
                if (action.suggestionSticker != null && StringUtil.isNotBlank(action.suggestionSticker.stickerType)) {
                    iconDrawable = getWidgetIcon(action.suggestionSticker.stickerAction);
                }

                if (iconDrawable > 0) {
                    mImvIcon.setImageResource(iconDrawable);
                } else {
                    mImvIcon.setVisibility(GONE);
                }
            }
        }

        if (StringUtil.isNotBlank(action.actionName)) {
            mTvLabel.setText(action.actionName);
        } else {
            mTvLabel.setText(R.string.suggestion_no_title);
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onSmallWidgetClicked(action);
                }
            }
        });
    }

    public int getWidgetIcon(BasicWidget.StickerAction stickerAction) {
        int drawable = 0;

        if (stickerAction != null) {
            String actionType = stickerAction.actionType;

            if (BasicWidget.WIDGET_TYPE_INPUT.equals(actionType)) {
                drawable = R.drawable.ic_question;
            } else if (BasicWidget.WIDGET_TYPE_SELECT.equals(actionType)) {

                drawable = R.drawable.ic_question;

                // Change icon if this is calendar widget
                if (stickerAction != null
                        && stickerAction.actionData != null) {
                    BasicWidget.StickerAction.ActionData lastActionData =
                            stickerAction.actionData.get(stickerAction.actionData.size() - 1);
                    if (lastActionData != null && lastActionData.action != null
                            && lastActionData.action.contains(WidgetAction.OPEN_CALENDAR)) {
                        drawable = R.drawable.ic_schedule;
                    }
                }
            } else if (BasicWidget.WIDGET_TYPE_LOCATION.equals(actionType)) {
                drawable = R.drawable.ic_location;
            } else if (BasicWidget.WIDGET_TYPE_COLOCATION.equals(actionType)) {
                drawable = R.drawable.icon_live_location;
            } else if (ResponseType.FILE.equals(actionType)) {
                drawable = R.drawable.ic_attachment;
            } else if (BasicWidget.WIDGET_TYPE_CONFIRM.equalsIgnoreCase(actionType)) {
                drawable = R.drawable.ic_confirm;
            }
        }

        return drawable;
    }
}
