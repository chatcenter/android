/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.dto.ws.response.GetChannelsCountResponseDto;

import ly.appsocial.chatcenter.dto.ChannelItem;
import ly.appsocial.chatcenter.dto.FunnelItem;

public class ChannelFilterView extends LinearLayout {

    private ListView mLvFunnel;
    private ListView mLvChannelStatus;
    private LinearLayout mRootView;

    private ArrayList<MessageStatusItem> mStatusItems = new ArrayList<>();
    private ArrayList<MessageFunnelItem> mFunnelItems = new ArrayList<>();

    private MessageStatusAdapter mStatusAdapter;
    private MessageFunnelAdapter mFunnelAdapter;

    private ChannelFilterDialogListener mListener;

    public ChannelFilterView(Context context, ArrayList<FunnelItem> items) {
        super(context);

        if (items != null && items.size() > 0) {
            for (FunnelItem item: items) {
                mFunnelItems.add(new MessageFunnelItem(item, 0));
            }
        }
        FunnelItem allItem = new FunnelItem();
        allItem.name = context.getString(R.string.all);
        allItem.id = -1;
        allItem.isSelected = true;
        mFunnelItems.add(0, new MessageFunnelItem(allItem, 0));

        setupView();
    }

    public ChannelFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupView();
    }

    public ChannelFilterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupView();
    }

    private void setupView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.dialog_channel_filter, this, true);

        mLvFunnel = (ListView) findViewById(R.id.business_funnel);
        mLvChannelStatus = (ListView) findViewById(R.id.message_status);
        mRootView = (LinearLayout) findViewById(R.id.root_view);

        mStatusAdapter = new MessageStatusAdapter(getContext(), R.layout.item_channel_filter, mStatusItems);
        mLvChannelStatus.setAdapter(mStatusAdapter);
        mLvChannelStatus.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < mStatusItems.size(); i++) {
                    if (i == position) {
                        mStatusItems.get(i).isSelected = true;
                    } else {
                        mStatusItems.get(i).isSelected = false;
                    }
                }

                mStatusAdapter.notifyDataSetChanged();

                if (mListener != null) {
                    mListener.onStatusItemSelected(mStatusItems.get(position));
                }
            }
        });

        mFunnelAdapter = new MessageFunnelAdapter(getContext(), R.layout.item_channel_filter, mFunnelItems);
        mLvFunnel.setAdapter(mFunnelAdapter);
        mLvFunnel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < mFunnelItems.size(); i++) {
                    if (i == position) {
                        mFunnelItems.get(i).funnel.isSelected = true;
                    } else {
                        mFunnelItems.get(i).funnel.isSelected = false;
                    }
                }

                mFunnelAdapter.notifyDataSetChanged();

                if (mListener != null) {
                    mListener.onFunnelItemSelected(mFunnelItems.get(position));
                }
            }
        });

        mRootView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onDialogOutsideTouched();
                }
            }
        });
    }

    public void updateData(GetChannelsCountResponseDto count) {

        if (mStatusItems.size() == 0) {
            mStatusItems.add(new MessageStatusItem(getContext().getString(R.string.all), ChannelItem.ChannelStatus.CHANNEL_ALL, count.all, true));
            mStatusItems.add(new MessageStatusItem(getContext().getString(R.string.unassigned_status), ChannelItem.ChannelStatus.CHANNEL_UNASSIGNED, count.unassigned, false));
            mStatusItems.add(new MessageStatusItem(getContext().getString(R.string.assigned_status), ChannelItem.ChannelStatus.CHANNEL_ASSIGNED, count.mine, false));
            mStatusItems.add(new MessageStatusItem(getContext().getString(R.string.close_status), ChannelItem.ChannelStatus.CHANNEL_CLOSE, count.close, false));
        } else {
            for (MessageStatusItem item: mStatusItems) {
                if (ChannelItem.ChannelStatus.CHANNEL_ALL == item.value) {
                        item.count = count.all;
                } else if (ChannelItem.ChannelStatus.CHANNEL_CLOSE == item.value) {
                    item.count = count.close;
                } else if (ChannelItem.ChannelStatus.CHANNEL_ASSIGNED == item.value) {
                    item.count = count.mine;
                } else if (ChannelItem.ChannelStatus.CHANNEL_UNASSIGNED == item.value) {
                    item.count = count.unassigned;
                }
            }
        }
        mStatusAdapter.notifyDataSetChanged();

        if (mFunnelItems != null && count.funnels != null) {
            for (MessageFunnelItem item : mFunnelItems) {
                JsonElement jsonElement;
                if (item.funnel.id == -1) {
                    jsonElement = count.funnels.get("");
                } else {
                    jsonElement = count.funnels.get(item.funnel.id + "");
                }

                if (jsonElement != null) {
                    item.count =jsonElement.getAsInt();
                }
            }
        }
        mFunnelAdapter.notifyDataSetChanged();
    }

    public ChannelFilterDialogListener getListener() {
        return mListener;
    }

    public void setFilterDialogListener(ChannelFilterDialogListener listener) {
        mListener = listener;
    }

    public static class MessageStatusItem {
        public String name;
        public ChannelItem.ChannelStatus value;
        public int count;
        public boolean isSelected;

        public MessageStatusItem(String name, ChannelItem.ChannelStatus value, int count, boolean isSelected) {
            this.name = name;
            this.value = value;
            this.count = count;
            this.isSelected = isSelected;
        }
    }

    public static class MessageFunnelItem {
        public FunnelItem funnel;
        public int count;
        public MessageFunnelItem(FunnelItem funnel, int count) {
            this.funnel = funnel;
            this.count = count;
        }
    }

    private class MessageStatusAdapter extends ArrayAdapter<MessageStatusItem> {

        public MessageStatusAdapter(Context context, int resource, List<MessageStatusItem> objects) {
            super(context, resource, objects);
        }

        @Override
        public int getCount() {
            return mStatusItems.size();
        }

        @Override
        public MessageStatusItem getItem(int position) {
            return mStatusItems.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_channel_filter, parent, false);

            MessageStatusItem item = getItem(position);

            (convertView.findViewById(R.id.checkbox)).setSelected(item.isSelected);
            ((TextView)convertView.findViewById(R.id.tv_filter_option)).setText(item.name);
            ((TextView)convertView.findViewById(R.id.tv_channel_matching_count)).setText(item.count + "");

            return convertView;
        }
    }

    private class MessageFunnelAdapter extends ArrayAdapter<MessageFunnelItem> {

        public MessageFunnelAdapter(Context context, int resource, List<MessageFunnelItem> objects) {
            super(context, resource, objects);
        }

        @Override
        public int getCount() {
            return mFunnelItems.size();
        }

        @Override
        public MessageFunnelItem getItem(int position) {
            return mFunnelItems.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_channel_filter, parent, false);

            MessageFunnelItem item = getItem(position);

            (convertView.findViewById(R.id.checkbox)).setSelected(item.funnel.isSelected);
            ((TextView)convertView.findViewById(R.id.tv_filter_option)).setText(item.funnel.name);
            ((TextView)convertView.findViewById(R.id.tv_channel_matching_count)).setText(item.count + "");

            return convertView;
        }
    }

    public interface ChannelFilterDialogListener {
        void onFunnelItemSelected(MessageFunnelItem item);
        void onStatusItemSelected(MessageStatusItem item);
        void onDialogOutsideTouched();
    }

}