# ChatCenter iO Android SDKインストールガイド Ver. 1.0.9

## 目次
* [Getting Started](#GettingStarted)
	* [1. サンプルプロジェクトをダウンロード](#DLSample)
	* [2. アプリケーションを起動](#LaunchApp)
* [SDKをアプリに組み込む](#InstallYourApp)
	* [1. AndroidStudioでの設定](#SettingOfAndroidStudio)
	* [2. アプリトークンとチームIDのセット](#SetAppToken)
	* [3. チャットビューの呼び出し](#DispalyChatView)
	* [4. ヒストリービューの呼び出し](#DispalyHistoryView)
	* [5. ユーザーのログアウト](#LogoutUser)

<a id="GettingStarted"></a>
## Getting Started

<a id="DLSample"></a>
#### 1. サンプルプロジェクトをダウンロード
[こちら](https://github.com/chatcenter/android)よりChatCenterSDKをダウンロードします。
Sampleプロジェクトが含まれています。

<a id="LaunchApp"></a>
#### 2. アプリケーションを起動
<p align="center"><img src="InstallationImages/sample2.png" width="320"></p>

<a id="InstallYourApp"></a>
## SDKをアプリに組み込む

<a id="SettingOfAndroidStudio"></a>
### 1. AndroidStudioでの設定

<a id="1InstallSDK"></a>
#### 1-1. SDKのインストール
アプリのbuild.gradleに以下を追加してください(tokboxはChatCenterSDK内で使用しているボイス/ビデオチャットのライブラリです)。

	repositories {   
		・・・   
		↓ 追加   
		maven { url  "http://tokbox.bintray.com/maven" }   
	}   
   
	dependencies {   
		・・・   
		↓ 追加   
		compile 'ly.appsocial:chatcenter-android-sdk:1.0.+';   
	}   

***

<a id="2CopyService"></a>
#### 1-2. SampleGcmListenerServiceのコピー
Sampleの中にあるSampleGcmListenerService.javaを自分のプロジェクトの中にコピーしてください。こちらはプッシュ通知を受け取るためのServiceになります。

***

<a id="3EditManifest"></a>
#### 1-3. AndroidManifestの編集
ChatCenter SDKでは各ウィジェットの送信時に、ユーザーの情報を使用する場合があります。そのためAndroidManifestに許諾の設定をお願いします。
許諾の必要があるものは以下です。

	<uses-permission android:name="android.permission.INTERNET" />
	<uses-permission android:name="android.permission.WAKE_LOCK" />
	<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
	<uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
	<uses-permission android:name="ly.appsocial.chatcenter.sdksample.permission.C2D_MESSAGE" />

	<permission
		android:name="ly.appsocial.chatcenter.sdksample.permission.C2D_MESSAGE"
		android:protectionLevel="signature" />


続いて、Sampleの時と同じようにAppTokenを`<application></application>`内に記述します。

	<meta-data
			android:name="ly.appsocial.chatcenter.AppToken"
			android:value="[YOUR_APP_TOKEN_HERE]"/>


続いて、Google play serviceを`<application></application>`内に記述します。

    <meta-data
            android:name="com.google.android.geo.API_KEY"
           android:value="[YOUR_GOOGLE_API_KEY]"/>

次に、先ほどコピーしたServiceを含む必要な項目を`<application></application>`内に追加します。

		<receiver
			android:name="com.google.android.gms.gcm.GcmReceiver"
			android:exported="true"
			android:permission="com.google.android.c2dm.permission.SEND" >
			<intent-filter>
				<action android:name="com.google.android.c2dm.intent.RECEIVE" />
				<category android:name="com.codepath.gcmquickstart" />
			</intent-filter>
		</receiver>

		<service
			android:name=".SampleGcmListenerService"
			android:exported="false" >
			<intent-filter>
				<action android:name="com.google.android.c2dm.intent.RECEIVE" />
			</intent-filter>
		</service>

		<meta-data
			android:name="com.google.android.gms.version"
			android:value="@integer/google_play_services_version" />


***

<a id="DispalyChatView"></a>
## 2. チャットビューの呼び出し
チャットを表示するチャットビューを呼び出します。
<p align="center"><img src="InstallationImages/chatview.png" width="375" height="667"></p>

### 2-1. 認証ありの場合
以下のコードをActivityの任意の場所に挿入してください。

```
ChatCenter.showChat(this, orgUid, provider, providerToken, providerTokenCreatedAt, providerTokenExpiresDate, info);
```
以下がパラメータです。太字が必須です。他のパラメータで不要な場合はnilをご指定ください。
<table>
	<tr>
		<th>パラメータ名</th>
		<th>Facebook</th>
		<th>Twitter</th>
	</tr>
	<tr>
		<td>context(Activity)</td>
		<td colspan="2"><b>元のActivityをご指定ください</b></td>
	</tr>
	<tr>
		<td>orgUid(String)</td>
		<td colspan="2"><b>Chatに紐づく、チームIDを指定してください</b></td>
	</tr>
	<tr>
		<td>provider(String)</td>
		<td><b>"facebook"を指定してください</b></td>
		<td><b>"twitter"を指定してください</b></td>
	</tr>
	<tr>
		<td>providerToken(String)</td>
		<td colspan="2"><b>認証結果のtokenを指定してください</b></td>
	</tr>
	<tr>
		<td>providerCreatedAt(Date)</td>
		<td colspan="2">nullを指定してください</td>
	</tr>
	<tr>
		<td>providerExpiresAt(Date)</td>
		<td><b>認証結果のtokenの失効日(expirationDate)を指定してください</b></td>
		<td> nullを指定してください</td>
	</tr>
	<tr>
		<td>channelInformations(`Map<String, String>`)</td>
		<td colspan="2">生成するchannelに紐づくurlを以下のように指定してください		``
		例) 
		Map<String, String> info = new HashMap<>();   
		info.put("url", "https://app.asana.com");   
		``</td>
	</tr>
</table>



### 2-2. 認証なしの場合(Anonymousログイン)
**注意: 認証なしの場合は、ログインから30日後に自動ログアウトされます。また、後から認証処理を紐付けることは現在対応しておりません**  
以下のコードを任意の場所に挿入してください。  

```
ChatCenter.showChat(this, orgUid, firstName, familyName, email, info);
```

#### パラメータ
以下がパラメータです。太字が必須です。他のパラメータで不要な場合はnilをご指定ください。
<table>
	<tr>
		<th>パラメータ名</th>
		<th>値</th>
	</tr>
	<tr>
		<td>context(Activity)</td>
		<td><b>元のActivityをご指定ください</b></td>
	</tr>
	<tr>
		<td>orgUid(String)</td>
		<td><b>Chatに紐づく、チームIDを指定してください Organization Uid(法人/店舗ID)Iについて</b></td>
	</tr>
	<tr>
		<td>firstName(String)</td>
		<td>生成するユーザーのファーストネームを指定してください</td>
	</tr>
	<tr>
		<td>familyName(String)</td>
		<td>生成するユーザーのファミリーネームを指定してください</td>
	</tr>
	<tr>
		<td>email(String)</td>
		<td>生成するユーザーのEmailアドレスを指定してください</td>
	</tr>
	<tr>
		<td>channelInformations(`Map<String, String>`)</td>
		<td>生成するchannelに紐づくurlを以下のように指定してください  
		``
		例) 
		Map<String, String> info = new HashMap<>();
		info.put("url", "https://app.asana.com");
		``</td>
	</tr>
</table>

***

<a id="DispalyHistoryView"></a>
## 3. ヒストリービューの呼び出し
チャットの履歴一覧を表示するヒストリービューを呼び出します。  
<p align="center"><img src="InstallationImages/historyview.png" width="375" height="667"></p>

### 3-1. 認証ありの場合
以下のコードを任意の場所に挿入してください。

```
	ChatCenter.showMessages(context, provider, providerToken, providerTokenTimestamp);
```

以下がパラメータです。太字が必須です。他のパラメータで不要な場合はnilをご指定ください。
<table>
	<tr>
		<th>パラメータ名</th>
		<th>Facebook</th>
		<th>Twitter</th>
	</tr>
	<tr>
		<td> context(Activity)</td>
		<td colspan="2"><b>元のActivityをご指定ください</b></td>
	</tr>
	<tr>
		<td>provider(String)</td>
		<td><b>@"facebook"を指定してください</b></td>
		<td><b>@"twitter"を指定してください</b></td>
	</tr>
	<tr>
		<td>providerToken(String)</td>
		<td colspan="2"><b>認証結果のtokenを指定してください</b></td>
	</tr>
	<tr>
		<td> providerTokenTimestamp(Date)</td>
		<td><b>認証結果のtokenのタイムスタンプを指定してください</b></td>
		<td>nullを指定してください</td>
	</tr>
</table>


### 3-2. 認証なしの場合(Anonymousログイン)
**注意: 認証なしの場合は、ログインから30日後に自動ログアウトされます。また、後から認証処理を紐付けることは現在対応しておりません**    
以下のコードを任意の場所に挿入してください。  

```
ChatCenter.showMessages(context);
```

#### パラメータ
以下がパラメータです。太字が必須です。他のパラメータで不要な場合はnilをご指定ください。
<table>
	<tr>
		<th>パラメータ名</th>
		<th>値</th>
	</tr>
	<tr>
		<td>context(Activity)</td>
		<td><b>元のActivityをご指定ください</b></td>
	</tr>
</table>

***

<a id="LogoutUser"></a>
## 4. ユーザーのログアウト
端末に保存されているデータを削除し、ログアウトするときは以下を呼んでください。  
``ChatCenter.signOut(context, SignOutCallback)``
***

