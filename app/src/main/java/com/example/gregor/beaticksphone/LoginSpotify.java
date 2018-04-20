package com.example.gregor.beaticksphone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlaybackBitrate;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginSpotify extends AppCompatActivity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    ListView songListView;
    Button b_startSong;
    RecyclerView.LayoutManager layoutManager;

    private Metadata mMetadata;

    public String[] items;
    public double bpm;

    private static final int REQUEST_CODE = 1337;

    private PlaybackState mCurrentPlaybackState;
    public ArrayAdapter adapter;

    private static final String TEST_SONG_URI = "spotify:track:6KywfgRqvgvfJc3JRwaZdZ";

    private Toast toast;

    private static final String TAG = "hallo";
    private static final String CLIENT_ID = "54a78dc7e5714bc49b07085a9675672c";
    private static final String REDIRECT_URI = "beaticks-callback://callback";

    private RequestQueue mRequestQueue;
    private JsonObjectRequest jsonPlaylistReq;
    private AuthenticationRequest request;
    private Player mPlayer;

    private Button b_loadTracks;

    private List<String> listTracks;
    private List<String> listTrackURI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.spotify_layout);

        b_startSong = findViewById(R.id.b_startSong);
        songListView = findViewById(R.id.songListView);


        new Thread(new Runnable() {
            @Override
            public void run() {
                adapter = new ArrayAdapter(LoginSpotify.this, android.R.layout.simple_list_item_1);

                AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                        AuthenticationResponse.Type.TOKEN,
                        REDIRECT_URI);
                builder.setScopes(new String[]{"user-read-private", "streaming", "user-library-read", "user-read-playback-state"});
                request = builder.build();


                AuthenticationClient.openLoginActivity(LoginSpotify.this, REQUEST_CODE, request);
            }
        }).start();


    }


    private final Player.OperationCallback mOperationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {

        }

        @Override
        public void onError(Error error) {

        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(LoginSpotify.this);
                        mPlayer.addNotificationCallback(LoginSpotify.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }


    // In dieser Methode werden die Playlists, Songs, und die BPM eines Songs über JSON Querys ausgelesen und in Lists gespeichert
    @Override
    public void onLoggedIn() { }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Error error) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        mCurrentPlaybackState = mPlayer.getPlaybackState();
        mMetadata = mPlayer.getMetadata();
        Log.i(TAG, "Player state: " + mCurrentPlaybackState);
        Log.i(TAG, "Metadata: " + mMetadata);
    }

    @Override
    public void onPlaybackError(Error error) {
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    public void onSongStartClicked(View view) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCurrentPlaybackState != null && mCurrentPlaybackState.isPlaying) {
                    mPlayer.pause(mOperationCallback);
                    b_startSong.setText("Play");

                } else {
                    //  mPlayer.resume(mOperationCallback);

                    b_startSong.setText("Pause");

                    // String uri = listTrackURI.get(2);

                    //mPlayer.playUri(mOperationCallback, uri, 0, 0);
                }
            }
        });
    }

    public void updateView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                });

                items = new String[10];
                for (int i = 1; i <= 2; i++) {
                    items[i] = "Track";
                }

                adapter.addAll(items);
                songListView.setAdapter(adapter);
            }
        }).start();

    }

    public void onLoadTracks(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mRequestQueue = Volley.newRequestQueue(LoginSpotify.this);


                jsonPlaylistReq = new JsonObjectRequest(Request.Method.GET,
                        "https://api.spotify.com/v1/users/cxsniperown/playlists", null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, response.toString());
                                JSONArray items = null;
                                try {

                                    //Toast.makeText(null,"READING PLAYLIST", Toast.LENGTH_SHORT);
                                    items = response.getJSONArray("items");
                                    //Toast.makeText(null,"Found Playlists: ", Toast.LENGTH_SHORT);
                                    for (int i = 0; i < items.length(); i++) {
                                        String playlistname = items.getJSONObject(i).getString("name");

                                        //Toast.makeText(null,"name: " + playlistname + "id: " + items.getJSONObject(i).getString("id"), Toast.LENGTH_LONG);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(null,"* * * * E R R O R - Get Playlists * * * *", Toast.LENGTH_SHORT);
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("Accept", "application/json");
                        params.put("Content-Type", "application/json");
                        params.put("Authorization", "Bearer BQCLho1hZYgssQ8mAzDWzaR-egUuQovAl67HWk5_43PhiTM82sZXPQKMgUr4eIu_OCb0nxFMf_j_cO6uTPZjhrnkqz_5zU84lG2gLv-VrdrfCDypyJ5ILMRnfpF8bQGT7CfT1xKWWDhZADz7WxONIXeJvmU");

                        return params;
                    }
                };
                mRequestQueue.add(jsonPlaylistReq);

            }
        }).start();

        updateView();

        /*
        JsonObjectRequest jsonTracksReq = new JsonObjectRequest(Request.Method.GET,
                "https://api.spotify.com/v1/users/cxsniperown/playlists/5jS80G2UyZ0fztAiUplK5B/tracks", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        JSONArray items = null;
                        try {
                            Toast.makeText(null,"READING PLAYLIST", Toast.LENGTH_SHORT);
                            items = response.getJSONArray("items");
                            Toast.makeText(null,"Found Tracks in Playlist: ", Toast.LENGTH_SHORT);
                            for (int i = 0; i < items.length(); i++) {

                                String trackname = items.getJSONObject(i).getJSONObject("track").getString("name");
                                listTrackURI.add(items.getJSONObject(i).getJSONObject("track").getJSONObject("track").getString("id"));
                                listTracks.add(trackname);
                                Toast.makeText(null,"track name: " + trackname + " track id: " + items.getJSONObject(i).getJSONObject("track").getString("id"), Toast.LENGTH_SHORT);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Accept", "application/json");
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer " + request);

                return params;
            }
        };
        mRequestQueue.add(jsonTracksReq);


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                "https://api.spotify.com/v1/audio-analysis/11dFghVXANMlKmJXsNCbNl", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            bpm = 0;
                            JSONObject track = response.getJSONObject("track");
                            bpm = track.getDouble("tempo");
                            Toast.makeText(null,"BPM: " + bpm, Toast.LENGTH_SHORT);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Accept", "application/json");
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer " + request);

                return params;
            }
        };
        mRequestQueue.add(jsonObjReq);
*/
    }
}


