package ly.appsocial.chatcenter.ws;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ly.appsocial.chatcenter.BuildConfig;
import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.di.InjectorHelper;
import ly.appsocial.chatcenter.di.NetworkUtilitiesWrapper;
import ly.appsocial.chatcenter.util.ApiUtil;

/**
 * Chat Center リクエスト
 */
public class OkHttpApiRequest<T> implements ApiRequest<T> {

	private static final String TAG = OkHttpApiRequest.class.getSimpleName();

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** パラメータ */
	private final Map<String, String> mParams;
	/** ヘッダー */
	private final Map<String, String> mHeaders;
	/** コールバック */
	private final Callback<T> mCallback;
	/** パーサー */
	private final Parser<T> mParser;
	/** HTTPボディにセットするJSON(POSTの場合のみ有効) */
	private String mJsonBody;
	/** API トークン */
	private String mApiToken;
	/** This request's method */
	private int mMethod;
	/** This request's Url */
	private String mUrl;
	/** Net work utilities wrapper */
	private NetworkUtilitiesWrapper mNetworkUtilitiesWrapper;
	/** OkHttp's call object */
	private Call mCall;
	/** Multipart body*/
	private RequestBody mMultipartBody;

	private com.squareup.okhttp.Callback mOkHttpCallback = new com.squareup.okhttp.Callback() {
		@Override
		public void onFailure(Request request, final IOException e) {
			Log.e(TAG, "onFailure: ", e);
			if (mCall != null && mCall.isCanceled()) {
				return;
			}

			Handler handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable() {
				@Override
				public void run() {
					Error error = new Error(e);
					mCallback.onError(error);
				}
			});

		}

		@Override
		public void onResponse(final Response response) throws IOException {
			if (mCall != null && mCall.isCanceled()) {
				return;
			}

			// This will runs on UI thread
			Handler handler = new Handler(Looper.getMainLooper());
			String responseStr = response.body().string();
			Log.d(TAG, "onResponse " + response.request().urlString() + " " + responseStr);

			if (response.isSuccessful()) {
				final T responseDto = mParser.parser(responseStr);

				if (mCallback != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							mCallback.onSuccess(responseDto);
						}
					});
				}
				return;
			}

			handler.post(new Runnable() {
				@Override
				public void run() {
					Error error = new Error(response);
					mCallback.onError(error);
				}
			});
		}
	};

	/**
	 * コンストラクタ
	 *
	 * @param context
	 * @param method
	 * @param path
	 * @param params
	 * @param headers
	 * @param callback
	 * @param parser
	 */
	public OkHttpApiRequest(Context context, int method, String path,
							final Map<String, String> params, Map<String, String> headers,
							final Callback<T> callback, Parser<T> parser) {
		mParams = params;
		mHeaders = headers;
		mCallback = callback;
		mParser = parser;
		mMethod = method;
		mUrl = getUrl(context, method, path, params);

		ApplicationInfo info = null;
		try {
			info = context.getPackageManager().getApplicationInfo(
					context.getPackageName(), PackageManager.GET_META_DATA);
			mApiToken = info.metaData.getString(context.getString(R.string.apptoken_param));
		}   catch (PackageManager.NameNotFoundException e) {
		}

		mNetworkUtilitiesWrapper = new NetworkUtilitiesWrapper();
		InjectorHelper.getInstance().injectNetworkModule(context, mNetworkUtilitiesWrapper);
	}

	public Map<String, String> getHeaders() {
		Map<String, String> newHeaders = new HashMap<String, String>();
		if (mHeaders != null) {
			newHeaders.putAll(mHeaders);
		}
		newHeaders.put("App-Token", mApiToken);
		if (mHeaders != null) {
			newHeaders.putAll(mHeaders);
		}

		newHeaders.put("Device-Model", android.os.Build.MODEL);
		newHeaders.put("Device-Os", "Android " + android.os.Build.VERSION.RELEASE);
		newHeaders.put("Sdk-Version", BuildConfig.SDK_VERSION);
		newHeaders.put("App-Version", BuildConfig.VERSION_NAME);
		newHeaders.put("Dev-Version", "" + BuildConfig.VERSION_CODE);
		newHeaders.put("Accept-Language", Locale.getDefault().getLanguage());
		newHeaders.put("Supports-Video-Chat", "true");

		return newHeaders;
	}

	public void setJsonBody(String jsonString) {
		mJsonBody = jsonString;
	}

	public void setMultipartBody (RequestBody body) {
		mMultipartBody = body;
	}

	/**
	 *
	 * @param apiToken
	 */
	public void setApiToken(String apiToken) {
		mApiToken = apiToken;
	}

	public String getApiToken() {
		return mApiToken;
	}

	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	public Request getRequest() {
		return getRequest(null);
	}
	public Request getRequest(Object tag) {
		// Build header
		Headers.Builder headerBuilder = new Headers.Builder();
		Map<String, String> header = getHeaders();
		for (String key : header.keySet()) {
			if (header.get(key) == null) {
				continue;
			}
			headerBuilder.add(key, header.get(key));
		}

		// Build default header
		Request.Builder builder = new Request.Builder()
				.url(mUrl)
				.headers(headerBuilder.build())
				.tag(tag);

		// Adding parameters
		if (mMethod == Method.GET) {
			builder.get();
		} else if (mMethod == Method.POST || mMethod == Method.DELETE || mMethod == Method.PUT || mMethod == Method.PATCH) {
			RequestBody body;
			if (mJsonBody != null) {
				body = RequestBody.create(JSON, mJsonBody);
			} else if (mMultipartBody != null) {
				body = mMultipartBody;
			} else {
				body = getFormRequestBody(mParams);
			}
			if (mMethod == Method.PATCH) {
				builder.patch(body);
			} else {
				builder.post(body);
			}
		}
		return builder.build();
	}

	private static RequestBody getFormRequestBody(Map<String, String> params) {
		FormEncodingBuilder builder = new FormEncodingBuilder();
		for (String key : params.keySet()) {
			builder.add(key, params.get(key));
		}
		return builder.build();
	}

	/**
	 * @param context
	 * @param method
	 * @param path
	 * @param params
	 * @return
	 */
	private static String getUrl(Context context, int method, String path, Map<String, String> params) {
		String apiUrl = ApiUtil.getApiUrl(context) + "/api/";

		Uri.Builder builder = Uri.parse(apiUrl).buildUpon();
		builder.appendEncodedPath(path);

		// URL にパラメータ設定
		if (method == Method.GET && params != null) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				builder.appendQueryParameter(entry.getKey(), entry.getValue());
			}
		}

		return builder.build().toString();
	}

	public void enqueue() {
		enqueue(null);
	}

	public void enqueue(String tag) {
		Request okReq = this.getRequest(tag);
		mCall = mNetworkUtilitiesWrapper.getOkHttpClient().newCall(okReq);
		mCall.enqueue(mOkHttpCallback);
	}
}
