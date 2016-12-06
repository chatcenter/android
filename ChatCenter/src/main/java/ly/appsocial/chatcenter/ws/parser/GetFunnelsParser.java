package ly.appsocial.chatcenter.ws.parser;


import com.google.gson.Gson;

import ly.appsocial.chatcenter.dto.ws.response.GetFunnelResponseDto;
import ly.appsocial.chatcenter.ws.ApiRequest;

public class GetFunnelsParser implements ApiRequest.Parser<GetFunnelResponseDto> {
    private int mErrorCode;

    /**
     * パース結果がエラーかどうかの判定時に呼ばれます。
     *
     * @return エラーコード
     */
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
    public GetFunnelResponseDto parser(String response) {
        try {
            return new Gson().fromJson("{funnels:" + response + "}", GetFunnelResponseDto.class);
        } catch (Exception e) {
            mErrorCode = -1;
        }
        return null;
    }
}
