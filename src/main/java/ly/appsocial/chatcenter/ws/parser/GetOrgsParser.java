/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.ws.parser;

import com.google.gson.Gson;

import ly.appsocial.chatcenter.dto.ws.response.GetOrgsResponseDto;
import ly.appsocial.chatcenter.ws.ApiRequest;

public class GetOrgsParser implements ApiRequest.Parser<GetOrgsResponseDto> {
    /** エラーコード */
    private int mErrorCode;

    @Override
    public int getErrorCode() {
        return mErrorCode;
    }

    @Override
    public GetOrgsResponseDto parser(String response) {
        try {
            return new Gson().fromJson("{items:" + response + "}", GetOrgsResponseDto.class);
        } catch (Exception e) {
            mErrorCode = -1;
        }
        return null;
    }
}
