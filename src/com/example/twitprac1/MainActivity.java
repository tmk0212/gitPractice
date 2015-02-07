package com.example.twitprac1;

//import android.net.Uri;
import java.text.SimpleDateFormat;
import java.util.List;

import twitter4j.Status;
//import android.view.Menu;
//import twitter4j.AsyncTwitterFactory;
import twitter4j.Twitter;
//import twitter4j.TwitterAdapter;
//import twitter4j.TwitterListener;
//import twitter4j.AsyncTwitter;
//import twitter4j.auth.AccessToken;
//import twitter4j.auth.RequestToken;
import twitter4j.TwitterException;
import android.app.Activity;
//import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.image.SmartImageView;

public class MainActivity extends ListActivity {
	private TweetAdapter mAdapter;
	private Twitter mTwitter;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);

		if(!TwitterUtils.hasAccessToken(this)){
			Intent intent = new Intent(this, TwitterOAuthActivity.class);
			startActivity(intent);
			finish();
		}
		else{
			mAdapter = new TweetAdapter(this);
			setListAdapter(mAdapter);

			mTwitter = TwitterUtils.getTwitterInstance(this);
			reloadTimeLine("");
		}
	};

	private class TweetAdapter extends ArrayAdapter<twitter4j.Status>{

		private LayoutInflater mInflater;

		public TweetAdapter(Context context){
			super(context, android.R.layout.simple_list_item_1);
			mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.list_item_tweet, null);
			}
			Status item = getItem(position);
			TextView name = (TextView) convertView.findViewById(R.id.name);
			name.setText(item.getUser().getName());

			TextView screenName = (TextView) convertView.findViewById(R.id.screen_name);
			screenName.setText("@" + item.getUser().getScreenName());

			TextView text = (TextView) convertView.findViewById(R.id.text);
			String tweetText = item.getText();
			tweetText = tweetText.replaceAll("@[a-zA-Z0-9._-]* ", "<font color=\"#44aaff\">@[a-zA-Z0-9._-]* </font>").replaceAll("\n", "<br>");
			//ToDo ÉÅÉìÉVÉáÉìÇÃHTMLâª
			text.setText(Html.fromHtml(tweetText));

			SmartImageView icon = (SmartImageView) convertView.findViewById(R.id.icon);
			icon.setImageUrl(item.getUser().getProfileImageURL());

			TextView time = (TextView) convertView.findViewById(R.id.time);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			time.setText(sdf.format(item.getCreatedAt()));

			TextView via = (TextView) convertView.findViewById(R.id.viaSource);
			String viaText = item.getSource();
			viaText = viaText.replaceAll("</a>", "").replaceAll("<.+>", "");
			via.setText(viaText);

			TextView fav_count = (TextView) convertView.findViewById(R.id.fav_count);
			int favCount = item.getFavoriteCount();
			if(favCount > 0){
				fav_count.setText("Åö:" + favCount);
			}	else{
				fav_count.setText("");
			}

			TextView rt_count = (TextView) convertView.findViewById(R.id.rt_count);
			int rtCount = item.getRetweetCount();
			if(rtCount > 0){
				rt_count.setText("RT:" + rtCount);
			}	else{
				rt_count.setText("");
			}
			return convertView;
		}
	}

	private void reloadTimeLine(final String userScreenName){
		AsyncTask<Void, Void, List<twitter4j.Status>> task = new AsyncTask<Void, Void, List<twitter4j.Status>>(){

			@Override
			protected List<twitter4j.Status> doInBackground(Void... params){
				try{
					if(userScreenName != ""){
						if(userScreenName == getString(R.string.mention_tl_key)){
							return mTwitter.getMentionsTimeline();
						}	else{
							return mTwitter.getUserTimeline(userScreenName);
						}
					}	else{
						return mTwitter.getHomeTimeline();
					}
				}
				catch(TwitterException e){
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(List<twitter4j.Status> result){
				if(result != null){
					mAdapter.clear();
					for(twitter4j.Status status : result){
						mAdapter.add(status);
					}
					getListView().setSelection(0);
				}
				else{
					showToast("TLì«Ç›çûÇ›Ç…é∏îs");
				}
			}
		};
		task.execute();
	}


	private void showToast(String text){
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item){

		switch(item.getItemId()){
			case R.id.menu_refresh:
				reloadTimeLine("");
				return true;
			case R.id.menu_tweet:
				Intent intent = new Intent(this, TweetActivity.class);
				startActivity(intent);
				return true;
			case R.id.menu_mytweet:
				reloadTimeLine(getString(R.string.author_screen_name));
				return true;
			case R.id.menu_mention:
				reloadTimeLine(getString(R.string.mention_tl_key));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
