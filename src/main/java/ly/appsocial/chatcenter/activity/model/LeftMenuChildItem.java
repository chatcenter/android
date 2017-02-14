package ly.appsocial.chatcenter.activity.model;

import ly.appsocial.chatcenter.dto.OrgItem;
import ly.appsocial.chatcenter.dto.ws.response.GetAppsResponseDto;

public class LeftMenuChildItem {
    private String mValue;
    private String mDisplayText;
    private OrgItem mOrg;
    private GetAppsResponseDto.App mApp;

    public LeftMenuChildItem(String displaText, String value, OrgItem orgItem, GetAppsResponseDto.App app) {
        this.mValue = value;
        this.mDisplayText = displaText;
        this.mOrg = orgItem;
        this.mApp = app;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }

    public String getDisplayText() {
        return mDisplayText;
    }

    public void setDisplayText(String displayText) {
        mDisplayText = displayText;
    }

    public OrgItem getOrg() {
        return mOrg;
    }

    public void setOrg(OrgItem org) {
        this.mOrg = org;
    }

    public GetAppsResponseDto.App getApp() {
        return mApp;
    }
}
