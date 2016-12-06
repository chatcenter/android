package ly.appsocial.chatcenter.ws.parser;

import com.google.gson.Gson;

import ly.appsocial.chatcenter.dto.ws.response.GetAppsResponseDto;
import ly.appsocial.chatcenter.ws.ApiRequest;

public class GetAppsParser implements ApiRequest.Parser<GetAppsResponseDto> {
    /** エラーコード */
    private int mErrorCode;

    @Override
    public int getErrorCode() {
        return mErrorCode;
    }

    @Override
    public GetAppsResponseDto parser(String response) {
        try {
            return new Gson().fromJson("{items:" + response + "}", GetAppsResponseDto.class);
        } catch (Exception e) {
            mErrorCode = -1;
        }
        return null;
    }
}
