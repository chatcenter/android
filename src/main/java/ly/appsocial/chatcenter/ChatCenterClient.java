package ly.appsocial.chatcenter;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ly.appsocial.chatcenter.dto.ws.request.PostDevicesRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.PostUsersAuthRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.PostUsersRequestDto;
import ly.appsocial.chatcenter.dto.ws.response.PostDevicesSignInResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostDevicesSignOutResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostUsersAuthResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostUsersResponseDto;
import ly.appsocial.chatcenter.gcm.ChatCenterDeviceTokenRequest;
import ly.appsocial.chatcenter.util.AuthUtil;
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
	private ChatCenterDeviceTokenRequest mDeviceTokenRequest;
	private Context mAppContext;
	private ArrayList<GetDeviceTokenCallback> mCallbacks = new ArrayList<>();

	protected ChatCenterClient(Context appContext) {
		mAppContext = appContext;

		// Try to get the token now, and do nothing.
		getDeviceToken(new GetDeviceTokenCallback() {
			@Override
			public void onSuccess(String deviceToken) {
			}
			@Override
			public void onError() {
			}
		});
	}

	public void signOutPushNotification(final ApiRequest.Callback<PostDevicesSignOutResponseDto> callback) {
		getDeviceToken(new GetDeviceTokenCallback() {
			@Override
			public void onSuccess(String deviceToken) {
				String path = "devices/sign_out";
				Map<String, String> headers = new HashMap<>();
				headers.put("Authentication", AuthUtil.getUserToken(mAppContext));

				Map<String, String> params = new HashMap<>();
				params.put("device_type", "android");
				params.put("device_token", deviceToken);
				mPostDevicesSignOutRequest = new OkHttpApiRequest<>(
						mAppContext, ApiRequest.Method.POST, path, params,
						headers, callback, new ApiRequest.Parser<PostDevicesSignOutResponseDto>() {
					@Override
					public int getErrorCode() {
						return 0;
					}

					@Override
					public PostDevicesSignOutResponseDto parser(String response) {
						return null;
					}
				});
				NetworkQueueHelper.enqueue(mPostDevicesSignOutRequest, REQUEST_TAG);
			}

			@Override
			public void onError() {
				if (callback != null) {
					callback.onSuccess(null);
					return;
				}
			}
		});
	}

	public void signInPushNotification(final String appToken,
									   final ApiRequest.Callback<PostDevicesSignInResponseDto> callback) {
		// Try to get the token now, and do nothing.
		getDeviceToken(new GetDeviceTokenCallback() {
			@Override
			public void onSuccess(String deviceToken) {
				String path = "devices/";

				Map<String, String> headers = new HashMap<>();
				headers.put("Authentication", AuthUtil.getUserToken(mAppContext));

				PostDevicesRequestDto request = new PostDevicesRequestDto();
				request.deviceToken = deviceToken;
				mPostDevicesSignInRequest = new OkHttpApiRequest<>(
						mAppContext, ApiRequest.Method.POST, path, request.toParams(),
						headers, callback, new ApiRequest.Parser<PostDevicesSignInResponseDto>() {
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
			@Override
			public void onError() {
				if (callback != null) {
					callback.onSuccess(null);
					return;
				}
			}
		});
	}

	public void getUserToken(final String email, final String password,
							 final String provider, final String providerToken,
							 final String providerTokenSecret,
							 final long providerCreatedAt, final long providerExpiresAt,
							 final ApiRequest.Callback<PostUsersAuthResponseDto> callback) {
		if (mPostUsersAuthRequest != null) {
			return;
		}

		getDeviceToken(new GetDeviceTokenCallback() {
			@Override
			public void onSuccess(String deviceToken) {
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
						AuthUtil.saveTokens(mAppContext, providerCreatedAt, responseDto);

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
			@Override
			public void onError() {
				if (callback != null) {
					callback.onError(null);
				}
			}
		});
	}

	public void getUserToken(final String org_uid, final String email,
							 final String firstName, final String familyName,
							 final String provider, final String providerToken,
							 final long providerCreatedAt, final long providerExpiresAt,
							 final ApiRequest.Callback<PostUsersResponseDto> callback) {
		if (mPostUsersRequest != null) {
			return;
		}

		getDeviceToken(new GetDeviceTokenCallback() {
			@Override
			public void onSuccess(String deviceToken) {
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
									AuthUtil.saveTokens(mAppContext, providerCreatedAt, responseDto.users.get(0));

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
			@Override
			public void onError() {
				if (callback != null) {
					callback.onError(null);
				}
			}
		});
	}

	public static boolean hasChatUser(Context context) {
		return !StringUtil.isBlank(AuthUtil.getUserToken(context));
	}

	public void getDeviceToken(GetDeviceTokenCallback listener) {
		String deviceToken = AuthUtil.getDeviceToken(mAppContext);
		if (deviceToken != null && !deviceToken.isEmpty() ) {
			listener.onSuccess(deviceToken);
			return;
		}

		mCallbacks.add(listener);

		if ( mDeviceTokenRequest == null ) {
			mDeviceTokenRequest = new ChatCenterDeviceTokenRequest(mAppContext, new ChatCenterDeviceTokenRequest.DeviceTokenRequestCallback() {
				@Override
				public void onTokenReceived(String deviceToken) {
					mDeviceTokenRequest = null;
					synchronized (ChatCenterClient.this){
						for ( GetDeviceTokenCallback listener : mCallbacks ){
							if ( deviceToken != null){
								listener.onSuccess(deviceToken);
							} else {
								listener.onError();
							}
						}
						mCallbacks.clear();
					}
				}
			});
			mDeviceTokenRequest.execute();
		}
	}

	public interface GetDeviceTokenCallback {
		void onSuccess(String deviceToken);
		void onError();
	}


}