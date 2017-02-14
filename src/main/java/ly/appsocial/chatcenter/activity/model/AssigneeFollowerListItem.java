package ly.appsocial.chatcenter.activity.model;

import java.io.Serializable;

import ly.appsocial.chatcenter.dto.UserItem;


public class AssigneeFollowerListItem implements Serializable {
    private boolean isSelected;
    private UserItem user;


    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public UserItem getUser() {
        return user;
    }

    public void setUser(UserItem user) {
        this.user = user;
    }
}
