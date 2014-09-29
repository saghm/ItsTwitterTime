package me.saghm.itstwittertime;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.support.wearable.view.CardFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;


public class Start extends Activity implements MessageApi.MessageListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GridViewPager pager;
    private GoogleApiClient client;
    private Context context = this;
    private TextView textView;

    interface Gridbox {}

    private static class Page implements Gridbox {
        String user;
        String tweet;
//      long id;

        public Page(String user, String tweet) {
            this.tweet = tweet;
            this.user  = user;
        }

//      public Page(String user, String tweet, long id) {
//          this.user  = user;
//          this.tweet = tweet;
//          this.id    = id;
//      }
    }

    private static class Link implements Gridbox {
        final String url;

        public Link(String url) {
            this.url = url;
        }
    }

    public class LinkFragment extends Fragment {
        public String url = "";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup containter,
                                        Bundle savedInstanceState) {
            ImageView view = new ImageView(context);
            view.setImageDrawable(getResources().getDrawable(R.drawable.open_phone));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (url.isEmpty()) return;

                    playOpenOnPhoneAnimation();

                    Wearable.NodeApi.getConnectedNodes(client)
                            .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                                @Override
                                public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                                    String temp = "";

                                    for (Node node: nodes.getNodes()) {
                                        temp = node.getId();
                                        break;
                                    }

                                    final String nodeId = temp;

                                    Wearable.NodeApi.getLocalNode(client).setResultCallback(
                                            new ResultCallback<NodeApi.GetLocalNodeResult>() {
                                                @Override

                                                public void onResult(NodeApi.GetLocalNodeResult localNode) {
                                                    Wearable.MessageApi.sendMessage(
                                                        client,
                                                        nodeId,
                                                        "/link",
                                                         url.getBytes()
                                                    );
                                                }
                                            }
                                    );
                                }
                            });
                }
            });

            return view;
        }
    }

    private class TwitterViewPagerAdapter extends FragmentGridPagerAdapter {
        private Gridbox[][] grid;

        public TwitterViewPagerAdapter(FragmentManager fm) {
            super(fm);

            grid = new Gridbox[100][];

            for (int i = 0; i < grid.length; i++) {
                grid[i] = new Gridbox[] { new Page("", ""), new Link("") };
            }
        }

        public void setPages(String tweetStream) {
            String[] tweets = tweetStream.split("&&&___&&&");
            //Log.i("length", "" + tweets.length);

            grid            = new Gridbox[tweets.length][];

            for (int i = 0; i < tweets.length; i++) {
                String[] whole = tweets[i].split("\\$\\$\\$___\\$\\$\\$");
                if (whole.length != 3) {
                    Log.i("tweet", tweets[i]);
                }

                String user    = whole[0].trim();
                String tweet   = whole[1].trim();
                String url     = String.format("https://twitter.com/%s/status/%s",
                                                whole[0].trim(),
                                                whole[2].trim()
                                              );
                grid[i]        = new Gridbox[]{ new Page(user, tweet), new Link(url) };
            }

            notifyDataSetChanged();
        }

        @Override
        public Fragment getFragment(int row, int col) {
            if (col == 0) {
                return CardFragment.create(((Page)grid[row][col]).user,
                                           ((Page)grid[row][col]).tweet);
            }

            LinkFragment lFragment = new LinkFragment();
            lFragment.url          = ((Link)grid[row][col]).url;

            return lFragment;
        }

        @Override
        public int getRowCount() {
            return grid.length;
        }

        @Override
        public int getColumnCount(int row) {
            return grid[row].length;
        }
    }

    private void setContentToText(String text) {
        pager.setVisibility(View.INVISIBLE);
        textView.setText(text);
        textView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        pager               = (GridViewPager)findViewById(R.id.container);
        pager.setAdapter(new TwitterViewPagerAdapter(getFragmentManager()));
        textView = (TextView)findViewById(R.id.text);
        setContentToText("Waiting for tweets...");

        client = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        client.connect();
    }

    private void playOpenOnPhoneAnimation() {
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                        ConfirmationActivity.OPEN_ON_PHONE_ANIMATION);
        startActivity(intent);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Wearable.MessageApi.addListener(client, this);

        Wearable.NodeApi.getConnectedNodes(client)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                        String temp = "";

                        for (Node node: nodes.getNodes()) {
                            temp = node.getId();
                            break;
                        }

                        final String nodeId = temp;

                        Wearable.NodeApi.getLocalNode(client).setResultCallback(
                            new ResultCallback<NodeApi.GetLocalNodeResult>() {
                                @Override

                                public void onResult(NodeApi.GetLocalNodeResult localNode) {
                                String s = localNode.getNode().getId();
                                Wearable.MessageApi.sendMessage(client, nodeId, "/stream", s.getBytes());
                                }
                            }
                        );
                    }
                });
    }

    @Override
    protected void onStop() {
        if (client != null && client.isConnected()) {
            Wearable.MessageApi.removeListener(client, this);
            client.disconnect();
        }

        super.onStop();
    }

    protected void addText(String text) {
        final String tweetStream = text;

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tweetStream.equals("%%%___%%%")) {
                    if (pager.getVisibility() == View.INVISIBLE) {
                        setContentToText("You are not logged in");
                    }

                    return;
                } else if (tweetStream.equals("^^^___^^^")) {
                    if (pager.getVisibility() == View.INVISIBLE) {
                        setContentToText("Error getting tweets");
                    }

                    return;
                } else if (tweetStream.equals("###___###")) {
                    if (pager.getVisibility() == View.INVISIBLE) {
                        setContentToText("Sorry, the Twitter API rate limit has been exceeded");
                    }

                    return;
                }

                textView.setVisibility(View.GONE);
                pager.setVisibility(View.VISIBLE);
                ((TwitterViewPagerAdapter)pager.getAdapter()).setPages(tweetStream);
            }
        });
    }

    @Override
    public void onMessageReceived(MessageEvent event) {
        if (!event.getPath().equals("/stream")) {
            Log.i("wear", "bad path: " + event.getPath());
            return;
        }

        addText(new String(event.getData()));
    }

    public void onConnectionSuspended(int i) {}
    public void onConnectionFailed(ConnectionResult c) {}
}

