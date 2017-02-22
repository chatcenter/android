/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.dto.ws.request;

import java.util.HashMap;
import java.util.Map;

import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.ChannelItem;
import ly.appsocial.chatcenter.util.StringUtil;

public class GetChannelsRequestDto {
    /** Channels per page */
    private int mLimit = ChatCenterConstants.MAX_CHANNEL_ON_LOAD;
    /** Page starts from (epoch timestamp) */
    private int mLastUpdatedAt;
    /** 0:unassigned, 1:assigned, 2:closed */
    private ChannelItem.ChannelStatus mStatus;
    /** Channel is archived? (true/false) */
    private ChannelItem.ChannelType mType = ChannelItem.ChannelType.CHANNEL_ALL;
    /** Org Uid */
    private String mOrgUid;
    /** Funnel ID*/
    private int mFunnelID;
    /** Assignee ID*/
    private int mAssigneeID;
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
            params.put("status[]", mStatus.ordinal() + "");
        }
        if (mType != null && mType != ChannelItem.ChannelType.CHANNEL_ALL) {
            params.put("archived", (mType == ChannelItem.ChannelType.CHANNEL_ARCHIVE ? "true" : "false"));
        }
        if (StringUtil.isNotBlank(mOrgUid)) {
            params.put("org_uid", mOrgUid);
        }
        if (mFunnelID > 0) {
            params.put("funnel_id", mFunnelID + "");
        }
        if (mAssigneeID > 0) {
            params.put("assignee_id", mAssigneeID + "");
        }
        return params;
    }

    public int getLimit() {
        return mLimit;
    }

    public int getLastUpdatedAt() {
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

    public void setLastUpdatedAt(int lastUpdatedAt) {
        mLastUpdatedAt = lastUpdatedAt;
    }

    public void setLimit(int limit) {
        mLimit = limit;
    }

    public void setType(ChannelItem.ChannelType type) {
        mType = type;
    }

    public String getOrgUid() {
        return mOrgUid;
    }

    public void setOrgUid(String orgUid) {
        mOrgUid = orgUid;
    }

    public int getFunnelID() {
        return mFunnelID;
    }

    public void setFunnelID(int funnelID) {
        mFunnelID = funnelID;
    }

    public int getAssigneeID() {
        return mAssigneeID;
    }

    public void setAssigneeID(int assigneeID) {
        mAssigneeID = assigneeID;
    }
}
