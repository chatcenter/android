/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.dto.ws.request;

import java.util.HashMap;
import java.util.Map;

import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.ChannelItem;

public class GetChannelsMineRequestDto {
    /** Channels per page */
    private int mLimit = ChatCenterConstants.MAX_CHANNEL_ON_LOAD;
    /** Page starts from (epoch timestamp) */
    private long mLastUpdatedAt;
    /** 0:unassigned, 1:assigned, 2:closed */
    private ChannelItem.ChannelStatus mStatus;
    /** Channel is archived? (true/false) */
    private ChannelItem.ChannelType mType = ChannelItem.ChannelType.CHANNEL_ALL;

    /**
     * リクストパラメータを生成します。
     *
     * @return リクエストパラメータ
     */
    public Map<String, String> toParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("limit", mLimit + "");
        if (mLastUpdatedAt > 0) {
            params.put("last_updated_at", mLastUpdatedAt + "");
        }
        if (mStatus != null && mStatus != ChannelItem.ChannelStatus.CHANNEL_ALL) {
            params.put("status", mStatus.ordinal() + "");
        }
        if (mType != ChannelItem.ChannelType.CHANNEL_ALL) {
            params.put("archived", ((mType != null && mType == ChannelItem.ChannelType.CHANNEL_ARCHIVE) ? "true" : "false"));
        }
        return params;
    }

    public int getLimit() {
        return mLimit;
    }

    public long getLastUpdatedAt() {
        return mLastUpdatedAt;
    }

    public ChannelItem.ChannelStatus getStatus() {
        return mStatus;
    }

    public ChannelItem.ChannelType getType() {
        return mType;
    }

    public void setStatus(ChannelItem.ChannelStatus status) {
        mStatus = status;
    }

    public void setLastUpdatedAt(long lastUpdatedAt) {
        System.out.println("lastUpdatedAt " + lastUpdatedAt);
        mLastUpdatedAt = lastUpdatedAt;
    }

    public void setLimit(int limit) {
        mLimit = limit;
    }

    public void setType(ChannelItem.ChannelType type) {
        mType = type;
    }
}
