package me.saghm.itstwittertime;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import twitter4j.Twitter;
import twitter4j.Paging;
import twitter4j.TwitterException;

import java.util.List;

public class Connector extends AsyncTask<Connector.ActivityAndString, Void, Connector.ActivityAndStrings> {
    private Twitter twitter;

    public static class ActivityAndString {
        Activity activity;
        String string;

        ActivityAndString(Activity activity, String string) {
            this.activity = activity;
            this.string  = string;
        }
    }

    class ActivityAndStrings extends ActivityAndString {
        String otherString;

        ActivityAndStrings(ActivityAndString activityAndString, String otherString) {
            super(activityAndString.activity, activityAndString.string);
            this.otherString = otherString;
        }
    }

    GoogleApiClient mGoogleAppiClient;

    protected ActivityAndStrings doInBackground(ActivityAndString...activities) {
        try {
            Start start = (Start)activities[0].activity;
            twitter     = start.twitter;
            twitter.setOAuthAccessToken(start.accessToken);
            Log.i("token", start.accessToken.getToken());
        } catch (Exception e) {
            Log.i("mobile", ExceptionParser.parse(e));
            return new ActivityAndStrings(activities[0], "%%%___%%%");
        }

        List<twitter4j.Status> statuses;
        try {
            statuses = twitter.getHomeTimeline(new Paging(1, 100));
        } catch (TwitterException e) {
            Log.i("exception", ExceptionParser.parse(e));
            String s = e.getErrorCode() == 88 ?
                        "###___###" :
                        "^^^___^^^";
            return new ActivityAndStrings(activities[0], s);
        }

        String all = "";

        for (twitter4j.Status status: statuses) {
            Tweet tweet = new Tweet(status);
            all += String.format("%s:$$$___$$$%s$$$___$$$%d&&&___&&&",
                                 tweet.getUsername(), tweet.getText(), tweet.getStatusId());
        }

        return new ActivityAndStrings(activities[0], all.trim());
    }

    protected void onPostExecute(final ActivityAndStrings aas) {
        mGoogleAppiClient = new GoogleApiClient.Builder(aas.activity)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

                    @Override
                    public void onConnected(Bundle connectionHint) {
                        sendOnConnect(aas);
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d("crapcrap", "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d("crapcrap", "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();

        mGoogleAppiClient.connect();
    }

    private void sendOnConnect(ActivityAndStrings activityAndString) {
        Wearable.MessageApi.sendMessage(mGoogleAppiClient, activityAndString.string, "/stream",
                activityAndString.otherString.getBytes());
    }
}
