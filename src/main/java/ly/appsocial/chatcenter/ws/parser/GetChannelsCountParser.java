/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.ws.parser;


import com.google.gson.Gson;

import ly.appsocial.chatcenter.dto.ws.response.GetChannelsCountResponseDto;
import ly.appsocial.chatcenter.ws.ApiRequest;

public class GetChannelsCountParser implements ApiRequest.Parser<GetChannelsCountResponseDto>{
    // //////////////////////////////////////////////////////////////////////////
    // インスタンスフィールド
    // //////////////////////////////////////////////////////////////////////////

    /** エラーコード */
    private int mErrorCode;

    // //////////////////////////////////////////////////////////////////////////
    // イベントメソッド
    // //////////////////////////////////////////////////////////////////////////

    @Override
    public int getErrorCode() {
        return mErrorCode;
    }
    /**
     * パース実行時に呼ばれます。
     *
     * @param response HTTPレスポンス文字列
     * @return レスポンスDTO
     */
    @Override
    public GetChannelsCountResponseDto parser(String response) {
        try {
            return new Gson().fromJson(response , GetChannelsCountResponseDto.class);
        } catch (Exception e) {
            mErrorCode = -1;
            e.printStackTrace();
        }
        return null;
    }
}
