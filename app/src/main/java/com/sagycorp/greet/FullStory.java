package com.sagycorp.greet;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.appodeal.ads.Appodeal;

import org.json.JSONException;
import org.json.JSONObject;

public class FullStory extends AppCompatActivity {

    private NetworkImageView StoryImage;
    private TextView StoryTitle, StoryDescription;
    private Toolbar toolbar;
    public String[] Horoscopes;
    private String sign, InShort, URL;
    private ImageLoader imageLoader;
    private ScrollView StoryViewLayout;
    private LinearLayout LoadingLayout, ErrorLayout;
    private SwipeRefreshLayout RefreshLayout;
    private Boolean visiblity = false;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_story);

        //ads
        Appodeal.show(this, Appodeal.BANNER_VIEW);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sharedPreferences = getSharedPreferences(Startup.PreferenceSETTINGS, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        StoryImage = (NetworkImageView) findViewById(R.id.PlaceImage);
        StoryTitle = (TextView) findViewById(R.id.PlaceTitle);
        StoryDescription = (TextView) findViewById(R.id.PlaceDescription);
        StoryViewLayout = (ScrollView) findViewById(R.id.PlaceView);
        LoadingLayout = (LinearLayout) findViewById(R.id.Loading);
        LoadingLayout.setVisibility(View.VISIBLE);
        ErrorLayout = (LinearLayout) findViewById(R.id.Error);
        RefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        imageLoader = MySingleton.getInstance(this).getImageLoader();

        URL = getIntent().getExtras().getString("URL");

        RefreshLayout.setColorSchemeResources(
                R.color.swipe_color_1, R.color.swipe_color_2,
                R.color.swipe_color_3, R.color.swipe_color_4);

        RefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!visiblity){
                    ErrorLayout.setVisibility(View.GONE);
                    initiateRequest();
                }
            }
        });
        initiateRequest();
    }

    private void initiateRequest() {
        //Request StoryPage
        RefreshLayout.setRefreshing(true);
        JsonObjectRequest request = new JsonObjectRequest(URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                LoadingLayout.setVisibility(View.GONE);
                StoryViewLayout.setVisibility(View.VISIBLE);
                try {
                    InShort = response.getString("InShort");
                    StoryTitle.setText(response.getString("Title"));
                    StoryDescription.setText(response.getString("Description"));
                    StoryImage.setImageUrl(response.getString("ImageURL"),imageLoader);

                    //set visiblity
                    visiblity = true;

                } catch (JSONException e) {
                    visiblity = false;
                    e.printStackTrace();
                }
                RefreshLayout.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                RefreshLayout.setRefreshing(false);
                LoadingLayout.setVisibility(View.GONE);
                ErrorLayout.setVisibility(View.VISIBLE);
                //System.out.println(error);
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(4000, 2, 2f));
        MySingleton.getInstance(this).addToRequestQueue(request);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.full_story, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.home)
        {
            super.onBackPressed();
            return true;
        }

        if (id == R.id.Refresh)
        {
            if(!visiblity) {
                if (!RefreshLayout.isRefreshing()) {
                    ErrorLayout.setVisibility(View.GONE);
                    RefreshLayout.setRefreshing(true);
                }

                // Start our refresh background task
                initiateRequest();
            }
            return true;
        }

        if (id == R.id.Share)
        {
            if (InShort!= null && !InShort.isEmpty())
            {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Read\n"+ InShort +"\nwith Greet."+"\nhttps://goo.gl/Sdc4w4");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
            return true;
        }

        if(id == R.id.suggestions)
        {
            Intent Email = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "suggestions@sagycorp.com", null));
            Email.putExtra(Intent.EXTRA_SUBJECT,"Advice for making this better app");
            Email.putExtra(Intent.EXTRA_TEXT, "**Your Demands/Suggestions here**");
            startActivity(Intent.createChooser(Email, "Share Your Advice with :"));
            return true;
        }
        if (id == R.id.tellFriend)
        {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "I found very interesting app!!\nGreet! Your storyteller, trip consultant, an astrological advisor and much more."+"\nDownload Greet\n"+ "https://goo.gl/Sdc4w4");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.change_horoscope) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();

            View dialogView = inflater.inflate(R.layout.horoscope_selector,null);
            builder.setView(dialogView);
            builder.setTitle(R.string.horo_title);
            Spinner spinner = (Spinner) dialogView.findViewById(R.id.edition_spinner);
            ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.horoscope_selector, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Horoscopes = getResources().getStringArray(R.array.horoscope_selector);

                    int positionID = position;
                    switch (positionID) {
                        case 0:
                            sign = "Aries";
                            break;
                        case 1:
                            sign = "Taurus";
                            break;
                        case 2:
                            sign = "Gemini";
                            break;
                        case 3:
                            sign = "Cancer";
                            break;
                        case 4:
                            sign = "Leo";
                            break;
                        case 5:
                            sign = "Virgo";
                            break;
                        case 6:
                            sign = "Libra";
                            break;
                        case 7:
                            sign = "Scorpio";
                            break;
                        case 8:
                            sign = "Sagittarius";
                            break;
                        case 9:
                            sign = "Capricorn";
                            break;
                        case 10:
                            sign = "Aquarius";
                            break;
                        case 11:
                            sign = "Pisces";
                            break;
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    editor.putString(Startup.HoroSign, sign);
                    editor.putBoolean(Startup.IsHoroSet, false);
                    editor.apply();
                    if (Build.VERSION.SDK_INT >= 11) {
                        recreate();
                    } else {
                        Intent intent = getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    }
                }
            }).setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            /*builder.setCancelable(false);*/
            builder.create().show();
            return true;


        }
        if(id == R.id.rate_app) {

            Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
            }
        }

        return super.onOptionsItemSelected(item);
    }

}