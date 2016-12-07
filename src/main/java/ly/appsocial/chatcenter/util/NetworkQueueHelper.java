package ly.appsocial.chatcenter.util;

import ly.appsocial.chatcenter.ws.ApiRequest;

public class NetworkQueueHelper {
    private NetworkQueueHelper() {

    }

    public static void enqueue(ApiRequest request, String tag) {
        request.enqueue(tag);
    }
}
