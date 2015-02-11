package com.example.twitprac1;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class TweetActivity extends FragmentActivity {
	
	Intent intent = getIntent();	
	private EditText mInputText;
	private Twitter mTwitter;
		
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tweet);
		
		mTwitter = TwitterUtils.getTwitterInstance(this);
		
		mInputText = (EditText) findViewById(R.id.input_text);
		
		// ToDo intentからデータを取得し、ちゃんと格納してEditTextの初期値に突っ込む
//		String replyName = intent.getExtras().toString();
//		System.out.println("tDebug1");
//			if(!replyName.equals(null)){
//				System.out.println("tDebug2");
//				mInputText.setText(replyName);
//				System.out.println("tDebug3");
//			}
		
		findViewById(R.id.action_tweet).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tweet();
			}
		});
	}
	
	private void tweet(){
		AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(String... params){
				try{
					mTwitter.updateStatus(params[0]);
					return true;
				}
				catch(TwitterException e){
					e.printStackTrace();
					return false;
				}
			}
			
			@Override
			protected void onPostExecute(Boolean result){
				if(result){
					showToast("ツイート完了！");
					finish();
				}
				else{
					showToast("ツイート失敗…");
				}
			}
		};
		task.execute(mInputText.getText().toString());
	}
	
	private void showToast(String text){
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

}
