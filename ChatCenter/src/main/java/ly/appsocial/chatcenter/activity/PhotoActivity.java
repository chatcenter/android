package ly.appsocial.chatcenter.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import ly.appsocial.chatcenter.BuildConfig;
import ly.appsocial.chatcenter.fragment.AlertDialogFragment;
import ly.appsocial.chatcenter.fragment.ProgressDialogFragment;
import ly.appsocial.chatcenter.task.AsyncTaskCallback;
import ly.appsocial.chatcenter.util.DialogUtil;
import ly.appsocial.chatcenter.util.StringUtil;
import uk.co.senab.photoview.PhotoView;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.dto.param.PhotoParamDto;

/**
 * 「写真」アクティビティ。
 */
public class PhotoActivity extends BaseActivity implements View.OnClickListener, ProgressDialogFragment.DialogListener, AlertDialogFragment.DialogListener {

	public static final String PHOTO_DATA = "PHOTO_DATA";
	public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1000;

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** ParamDto */
	private PhotoParamDto mParamDto;

	// タスク
	/** ダウンロードタスク */
	private DownloadTask mDownloadTask;

	// etc
	/** 保存した画像ファイルパス */
	private String mSaveFilePath;

	private Button mBtnDone;
	private ProgressBar mProgressBar;
	private ImageButton mBtShare;

	// //////////////////////////////////////////////////////////////////////////
	// イベントメソッド
	// //////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo);

		// パラメータの取得
		mParamDto = getIntent().getParcelableExtra(PHOTO_DATA);
		if (mParamDto == null) {
			return;
		}

		if (savedInstanceState != null) {
			mSaveFilePath = savedInstanceState.getString("saveFilePath");
		}

		mBtShare = (ImageButton) findViewById(R.id.photo_share_button);
		mBtnDone = (Button) findViewById(R.id.btn_done);
		mProgressBar = (ProgressBar) findViewById(R.id.progress);

		// 写真
		PhotoView photoView = (PhotoView) findViewById(R.id.photo_imageview);
		Picasso.with(this).load(mParamDto.url).fit().into(photoView, new Callback() {
			@Override
			public void onSuccess() {
				mProgressBar.setVisibility(View.GONE);
				// 共有ボタン
				if (getExternalCacheDir() != null) {
					mBtShare.setVisibility(View.VISIBLE);
					mBtShare.setOnClickListener(PhotoActivity.this);
					/*
					 * 外部ストレージのキャッシュディレクトリが使えない場合は共有ボタンを非表示にします。
					 */
				}
			}

			@Override
			public void onError() {
				mProgressBar.setVisibility(View.GONE);
			}
		});

		mBtnDone.setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mDownloadTask != null) {
			mDownloadTask.cancel(true);
		}
	}

	@Override
	public void finish() {
		super.finish();

		// アニメーションの設定
		TypedArray activityStyle = getTheme().obtainStyledAttributes(new int[] {android.R.attr.windowAnimationStyle});
		int windowAnimationStyleResId = activityStyle.getResourceId(0, 0);
		activityStyle.recycle();

		activityStyle = getTheme().obtainStyledAttributes(windowAnimationStyleResId, new int[]{android.R.attr.activityCloseEnterAnimation, android.R.attr.activityCloseExitAnimation});
		int activityCloseEnterAnimation = activityStyle.getResourceId(0, 0);
		int activityCloseExitAnimation = activityStyle.getResourceId(1, 0);
		activityStyle.recycle();

		overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
	}

	@Override
	public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
		super.onSaveInstanceState(outState, outPersistentState);
		outState.putString("saveFilePath", mSaveFilePath);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		if (id == R.id.photo_share_button) { // 共有ボタン
			if (ContextCompat.checkSelfPermission(this,
					Manifest.permission.WRITE_EXTERNAL_STORAGE)
					!= PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
						MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
			} else {
				onButtonShareClicked();
			}
		} else if (id == R.id.btn_done) {
			finish();
		}
	}

	@Override
	public void onDialogCancel(String tag) {
		if (DialogUtil.Tag.PROGRESS.equals(tag)) {
			if (mDownloadTask != null) {
				mDownloadTask.cancel(true);
			}
		}
	}

	@Override
	public void onPositiveButtonClick(String tag) {
		// empty
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					onButtonShareClicked();
				}
				return;
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// プライベートメソッド
	// //////////////////////////////////////////////////////////////////////////

	private void onButtonShareClicked() {
		if (StringUtil.isNotBlank(mSaveFilePath) && new File(mSaveFilePath).exists()) {
				/*
				 * 既に画像をダウンロード済みの場合は再ダウンロードは行わず共有を行います。
				 */
			share();
			return;
		}

		// ダウンロードタスクの実行
		if (mDownloadTask != null) {
			return;
		}
		DialogUtil.showProgressDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
		mDownloadTask = new DownloadTask(new DownloadTaskCallback());
		mDownloadTask.execute(mParamDto.url, mParamDto.fileName);
	}


	/**
	 * 共有インテントを発行します。
	 */
	private void share() {
		Uri imageUri = Uri.fromFile(new File((mSaveFilePath)));
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_STREAM, imageUri);
		try {
			startActivity(Intent.createChooser(intent, getString(R.string.share_image)));
		} catch (ActivityNotFoundException e) {
			if (BuildConfig.DEBUG) {
				Log.d("###", "App not found.");
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// インナークラス
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * ダウンロードタスク。
	 * <p>
	 * {@link #execute(Object[])} の第一引数は画像URL、第二引数はファイル名を指定します。<br>
	 * ファイル名は拡張子を判定するためだけに使用します。
	 * </p>
	 */
	public class DownloadTask extends AsyncTask<String, Void, String> {

		/** コールバック */
		private AsyncTaskCallback<String> mCallback;

		/**
		 * コンストラクタ
		 *
		 * @param callback コールバック
		 */
		public DownloadTask(AsyncTaskCallback<String> callback) {
			mCallback = callback;
		}

		@Override
		protected String doInBackground(String... params) {
			if (params.length < 2) {
				return null;
			}

			// HTTP で画像をダウンロード
			return download(params[0], params[1]);
		}

		@Override
		protected void onPostExecute(String result) {
			if (result == null) {
				mCallback.onError(0);
			} else {
				mCallback.onSuccess(result);
			}
		}

		@Override
		protected void onCancelled() {
			mCallback.onCancel();
		}

		/**
		 * 画像をダウンロードし、外部ストレージのキャッシュに保存します。
		 *
		 * @param imageUrl 画像URL
		 * @param fileName ファイル名
		 * @return 保存したキャッシュファイルのパス。失敗の場合は null
		 */
		private String download(String imageUrl, String fileName) {

			InputStream in = null;
			FileOutputStream out = null;
			try {
				Request request = new Request.Builder().url(imageUrl).build();
				Response response = mOkHttpClient.newCall(request).execute();

				in = response.body().byteStream();

				// キャッシュディレクトリのチェック
				File externalCacheDir = Environment.getExternalStorageDirectory();
				if (externalCacheDir == null) {
					return null;
				}

				// 拡張子の取得
				int extPos = fileName.lastIndexOf(".");
				String ext = ".png";
				if (extPos != -1) {
					ext = fileName.substring(extPos);
				}

				// 保存先パスの生成
				String saveDirPath = externalCacheDir.getAbsolutePath() + "/" + getPackageName() + "/Chat/";
				File saveDir = new File(saveDirPath);
				if (!saveDir.exists() && !saveDir.mkdirs()) {
					return null;
				}
				for (File file : saveDir.listFiles()) {
					file.delete();
				}
				String saveFilePath = saveDirPath + new SimpleDateFormat("yyyyMMddHHmmss", Locale.JAPAN).format(new Date()) + ext;

				// ファイル保存
				out = new FileOutputStream(saveFilePath);
				Bitmap bitmap = BitmapFactory.decodeStream(in);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
				out.flush();

				return saveFilePath;

			} catch (Exception e) {
				if (BuildConfig.DEBUG) {
					Log.e("###", e.getMessage(), e);
				}
			} finally {
				try {
					if (in != null) {
						in.close();
					}
					if (out != null) {
						out.close();
					}
				} catch (IOException e) {
					if (BuildConfig.DEBUG) {
						Log.e("###", e.getMessage(), e);
					}
				}
			}
			return null;
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// インナークラス
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * 画像ダウンロードタスクのコールバック。
	 */
	private class DownloadTaskCallback implements AsyncTaskCallback<String> {

		@Override
		public void onCancel() {
			mDownloadTask = null;
		}

		@Override
		public void onError(int errorCode) {
			mDownloadTask = null;

			DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
			DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ALERT, "", getString(R.string.download_image_failed));
		}

		@Override
		public void onSuccess(String result) {
			mDownloadTask = null;
			mSaveFilePath = result;

			DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);

			share();
		}
	}
}
