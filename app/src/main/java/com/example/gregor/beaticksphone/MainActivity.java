package com.example.gregor.beaticksphone;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;



public class MainActivity extends AppCompatActivity  {

    TextView conn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        conn = findViewById(R.id.tvInfo);
        conn.setText("-");


        Thread btc = new BtConnector();
        btc.start();

        final Handler handler=new Handler();
        handler.post(new Runnable(){
            @Override
            public void run() {
                conn.setText(""+BtConnector.avrgValue);
                ((TextView) findViewById(R.id.tvAvrg)).setText("Last value: "+BtConnector.value);
                handler.postDelayed(this,500);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void SpotifyClick(View view) {
        //this.setVisible(false);

        startActivity(new Intent(MainActivity.this, LoginSpotify.class));
    }
}
