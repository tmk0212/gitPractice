package com.example.twitprac1;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class TwitterOAuthActivity extends Activity {
	private String mCallbackURL;
	private Twitter mTwitter;
	private RequestToken mRequestToken;
	private final Activity mActivity = this;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_twitter_oauth);
		
		mCallbackURL = getString(R.string.twitter_callback_url);
		mTwitter = TwitterUtils.getTwitterInstance(this);
		
		findViewById(R.id.action_start_oauth).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				System.out.println("debug1");
				startAuthorize();
				System.out.println("debug2");
			}
		});
		findViewById(R.id.action_start_pin).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new OnOAuthButtonClickTask().execute();
				System.out.println("debug9");
			}
		});
	}
	
	private void startAuthorize(){
		AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void,String>(){
			@Override
			protected String doInBackground(Void... Params){
				try{
					mRequestToken = mTwitter.getOAuthRequestToken(mCallbackURL);
					System.out.println("debug3");
					return mRequestToken.getAuthorizationURL();
				}
				catch(TwitterException e){
					e.printStackTrace();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(String url){
				if(url != null){
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					System.out.println("debug4");
					startActivity(intent);
				}
				else{
					System.out.println("失敗");
					//失敗
				}
			}
		};
		task.execute();
	}
	
	@Override
	public void onNewIntent(Intent intent){
		if(intent == null || intent.getData() == null
				|| !intent.getData().toString().startsWith(mCallbackURL)){
			System.out.println("debug5");
			return;
		}
		String verifier = intent.getData().getQueryParameter("oauth_verifier");
		
		AsyncTask<String, Void, AccessToken> task = new AsyncTask<String, Void, AccessToken>(){
			
			@Override
			protected AccessToken doInBackground(String... params){
				try{
					System.out.println("debug6");
					return mTwitter.getOAuthAccessToken(mRequestToken, params[0]);
				}
				catch(TwitterException e){
					e.printStackTrace();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(AccessToken accessToken){
				if(accessToken != null){
//					認証成功
					System.out.println("debug7");
					showToast("認証成功！");
					successOAuth(accessToken);
				}
				else{
//					認証失敗
					showToast("認証失敗");
				}
			}
		};
		task.execute(verifier);
	}
	
	private void successOAuth(AccessToken accessToken){
		TwitterUtils.storeAccessToken(this, accessToken);
		System.out.println("debug8");
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
	
	private void showToast(String text){
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
	public class OnOAuthButtonClickTask extends AsyncTask<Void,Void,AccessToken>{
		@Override
		protected AccessToken doInBackground(Void... params) {
			EditText mEditText = (EditText)mActivity.findViewById(R.id.oauth_pin);
			try{
				//アクセストークンを取得
				return mTwitter.getOAuthAccessToken(mRequestToken,mEditText.getText().toString());
			}
			catch (TwitterException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(AccessToken accessToken) {
			super.onPostExecute(accessToken);
			if(accessToken == null){
				return;
			}
			//トークンの書き出し(取得に成功した場合)
			Toast.makeText(mActivity, "Success!", Toast.LENGTH_SHORT).show();
			Toast.makeText(mActivity, accessToken.getToken(), Toast.LENGTH_SHORT).show();
			System.out.println("AccessToken" + accessToken.getToken().toString());
			Toast.makeText(mActivity, accessToken.getTokenSecret(), Toast.LENGTH_SHORT).show();
			System.out.println("AccessTokenSecret" + accessToken.getTokenSecret().toString());
			//<a class="keyword" href="http://d.hatena.ne.jp/keyword/Twitter">Twitter</a>クラスにアクセストークンをセット（これで<a class="keyword" href="http://d.hatena.ne.jp/keyword/Twitter">Twitter</a>クラスが使えるようになる）
//			mTwitter.setOAuthAccessToken(accessToken);
			TwitterUtils.storeAccessToken(mActivity, accessToken);

			//Tweetの送信(通常は通信を伴うため別スレッド上で動かす
			//Android4.2以降ではメインスレッドでHTTP投げようとすると<a class="keyword" href="http://d.hatena.ne.jp/keyword/%CE%E3%B3%B0">例外</a>吐くようです．
			//<ex>
			//mTwitter.updateStatus("hogehoge");1660792
 
        }   
 
    }
}

