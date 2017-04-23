package ly.appsocial.chatcenter.ws.parser;

import com.google.gson.Gson;

import ly.appsocial.chatcenter.dto.ws.response.GetMeResponseDto;
import ly.appsocial.chatcenter.ws.ApiRequest;

public class GetMeParser implements ApiRequest.Parser<GetMeResponseDto>{
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
    public GetMeResponseDto parser(String response) {
        try {
            return new Gson().fromJson(response, GetMeResponseDto.class);
        } catch (Exception e) {
            mErrorCode = -1;
        }
        return null;
    }
}
