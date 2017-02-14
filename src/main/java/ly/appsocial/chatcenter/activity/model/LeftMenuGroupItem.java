package ly.appsocial.chatcenter.activity.model;

public class LeftMenuGroupItem {
    private int mIconResource;
    private String mTitle;

    public LeftMenuGroupItem (int iconResource, String title) {
        mIconResource = iconResource;
        mTitle = title;
    }

    public int getIconResource() {
        return mIconResource;
    }

    public void setIconResource(int iconResource) {
        mIconResource = iconResource;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }
}
