package ly.appsocial.chatcenter;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import ly.appsocial.chatcenter.dto.ws.request.PostDevicesRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.PostUsersAuthRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.PostUsersRequestDto;
import ly.appsocial.chatcenter.dto.ws.response.PostDevicesSignInResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostDevicesSignOutResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostUsersAuthResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostUsersResponseDto;
import ly.appsocial.chatcenter.util.CCAuthUtil;
import ly.appsocial.chatcenter.util.NetworkQueueHelper;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.ws.ApiRequest;
import ly.appsocial.chatcenter.ws.OkHttpApiRequest;
import ly.appsocial.chatcenter.ws.parser.PostUsersAuthParser;
import ly.appsocial.chatcenter.ws.parser.PostUsersParser;

public class ChatCenterClient {

	private static final String REQUEST_TAG = "ChatCenterClient";

	private ApiRequest<PostDevicesSignOutResponseDto> mPostDevicesSignOutRequest;
	private ApiRequest<PostDevicesSignInResponseDto> mPostDevicesSignInRequest;
	private ApiRequest<PostUsersAuthResponseDto> mPostUsersAuthRequest;
	private ApiRequest<PostUsersResponseDto> mPostUsersRequest;
	private Context mAppContext;

	protected ChatCenterClient(Context appContext) {
		mAppContext = appContext;
	}

	public void signOutPushNotification(String deviceToken, ApiRequest.Callback<PostDevicesSignOutResponseDto> callback) {
		String path = "devices/sign_out";
		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", CCAuthUtil.getUserToken(mAppContext));

		Map<String, String> params = new HashMap<>();
		params.put("device_type", "android");
		params.put("device_token", deviceToken);
		mPostDevicesSignOutRequest = new OkHttpApiRequest<>(
				mAppContext,
				ApiRequest.Method.POST,
				path,
				params,
				headers,
				callback,
				new ApiRequest.Parser<PostDevicesSignOutResponseDto>() {
					@Override
					public int getErrorCode() {
						return 0;
					}

					@Override
					public PostDevicesSignOutResponseDto parser(String response) {
						// CCAuthUtil.saveDeviceToken(mAppContext, "");
						return null;
					}
				});
		NetworkQueueHelper.enqueue(mPostDevicesSignOutRequest, REQUEST_TAG);
	}

	/**
	 *
	 * @param appToken
	 * @param deviceToken
	 * @param callback
	 */
	public void signInPushNotification(String appToken,
									   String deviceToken,
									   ApiRequest.Callback<PostDevicesSignInResponseDto> callback) {
		// To save device token
		CCAuthUtil.saveDeviceToken(mAppContext, deviceToken);

		String path = "devices/";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", CCAuthUtil.getUserToken(mAppContext));

		PostDevicesRequestDto request = new PostDevicesRequestDto();
		request.deviceToken = deviceToken;
		mPostDevicesSignInRequest = new OkHttpApiRequest<>(
				mAppContext,
				ApiRequest.Method.POST,
				path,
				request.toParams(),
				headers,
				callback,
				new ApiRequest.Parser<PostDevicesSignInResponseDto>() {
					@Override
					public int getErrorCode() {
						return 0;
					}

					@Override
					public PostDevicesSignInResponseDto parser(String response) {
						return null;
					}
				});
		if (appToken != null) {
			mPostDevicesSignInRequest.setApiToken(appToken);
		}
		NetworkQueueHelper.enqueue(mPostDevicesSignInRequest, REQUEST_TAG);
	}

	public void getUserToken(final String email,
							 final String password,
							 final String provider,
							 final String providerToken,
							 final String providerTokenSecret,
							 final long providerCreatedAt,
							 final long providerExpiresAt,
							 final String deviceToken,
							 final ApiRequest.Callback<PostUsersAuthResponseDto> callback) {

		// To save device token
		CCAuthUtil.saveDeviceToken(mAppContext, deviceToken);

		if (mPostUsersAuthRequest != null) {
			return;
		}

		String path = "users/auth";

		PostUsersAuthRequestDto postUsersAuthRequestDto = new PostUsersAuthRequestDto();
		postUsersAuthRequestDto.provider = provider;
		postUsersAuthRequestDto.providerToken = providerToken;
		postUsersAuthRequestDto.providerTokenSecret = providerTokenSecret;
		postUsersAuthRequestDto.setProviderTokenCreateAt(providerCreatedAt);
		postUsersAuthRequestDto.setProviderTokenExpires(providerExpiresAt);
		postUsersAuthRequestDto.deviceToken = deviceToken;
		postUsersAuthRequestDto.email = email;
		postUsersAuthRequestDto.password = password;
		ApiRequest.Callback<PostUsersAuthResponseDto> cb = new ApiRequest.Callback<PostUsersAuthResponseDto>() {
			@Override
			public void onSuccess(PostUsersAuthResponseDto responseDto) {
				mPostUsersAuthRequest = null;

				// 認証情報の保存
				responseDto.admin = true;
				CCAuthUtil.saveTokens(mAppContext, providerCreatedAt, responseDto);

				if (callback != null) {
					callback.onSuccess(responseDto);
				}
			}

			@Override
			public void onError(ApiRequest.Error error) {
				mPostUsersAuthRequest = null;
				if (callback != null) {
					callback.onError(error);
				}
			}
		};
		mPostUsersAuthRequest = new OkHttpApiRequest<>(mAppContext, ApiRequest.Method.POST, path,
				postUsersAuthRequestDto.toParams(), null, cb, new PostUsersAuthParser());
		NetworkQueueHelper.enqueue(mPostUsersAuthRequest, REQUEST_TAG);
	}

	public void getUserToken(final String org_uid,
							 final String email,
							 final String firstName,
							 final String familyName,
							 final String provider,
							 final String providerToken,
							 final long providerCreatedAt,
							 final long providerExpiresAt,
							 final String deviceToken,
							 final ApiRequest.Callback<PostUsersResponseDto> callback) {

		// To save device token
		CCAuthUtil.saveDeviceToken(mAppContext, deviceToken);

		if (mPostUsersRequest != null) {
			return;
		}

		String path = "users";

		PostUsersRequestDto postUsersRequestDto = new PostUsersRequestDto();
		postUsersRequestDto.provider = provider;
		postUsersRequestDto.providerToken = providerToken;
		postUsersRequestDto.setProviderTokenCreateAt(mAppContext, providerCreatedAt);
		postUsersRequestDto.setProviderExpires(mAppContext, providerExpiresAt);
		postUsersRequestDto.kissCd = org_uid;
		postUsersRequestDto.deviceToken = deviceToken;
		postUsersRequestDto.firstName = firstName;
		postUsersRequestDto.familyName = familyName;
		postUsersRequestDto.email = email;

		mPostUsersRequest = new OkHttpApiRequest<>(mAppContext, ApiRequest.Method.POST,
				path, postUsersRequestDto.toParams(), null,
				new ApiRequest.Callback<PostUsersResponseDto>() {
					@Override
					public void onSuccess(PostUsersResponseDto responseDto) {
						mPostUsersRequest = null;

						if ( responseDto != null && responseDto.users != null && responseDto.users.size() > 0 ){
							// 認証情報の保存
							CCAuthUtil.saveTokens(mAppContext, providerCreatedAt, responseDto.users.get(0));

							if (callback != null) {
								callback.onSuccess(responseDto);
							}
						} else {
							if (callback != null) {
								callback.onError(null);
							}
						}
					}

					@Override
					public void onError(ApiRequest.Error error) {
						mPostUsersRequest = null;
						if (callback != null) {
							callback.onError(error);
						}
					}
				}, new PostUsersParser());
		NetworkQueueHelper.enqueue(mPostUsersRequest, REQUEST_TAG);
	}

	public static boolean hasChatUser(Context context) {
		return !StringUtil.isBlank(CCAuthUtil.getUserToken(context));
	}


}