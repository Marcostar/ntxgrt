package com.sagycorp.greet.Fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.sagycorp.greet.MainActivity;
import com.sagycorp.greet.MySingleton;
import com.sagycorp.greet.PushStart;
import com.sagycorp.greet.R;
import com.sagycorp.greet.Startup;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class Story extends Fragment {

    private NetworkImageView StoryImage;
    private TextView StoryTitle, StoryDescription;
    private String Today, InShort;
    private String url = "http://fd.sagycorp.com/Stories/";
    private ImageLoader imageLoader;
    private ScrollView StoryViewLayout;
    private LinearLayout LoadingLayout, ErrorLayout;
    private SwipeRefreshLayout RefreshLayout;
    private Boolean visiblity = false;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private MainActivity activity = new MainActivity();
    private Tracker mTracker;
    public Story() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String Name = "SingleStory";
        // Obtain the shared Tracker instance.
        PushStart application = (PushStart) getActivity().getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName(Name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        // Notify the system to allow an options menu for this fragment.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_story, container, false);
        sharedPreferences = getActivity().getSharedPreferences(Startup.PreferenceSETTINGS, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        StoryImage = (NetworkImageView) rootView.findViewById(R.id.PlaceImage);
        StoryTitle = (TextView) rootView.findViewById(R.id.PlaceTitle);
        StoryDescription = (TextView) rootView.findViewById(R.id.PlaceDescription);
        StoryViewLayout = (ScrollView) rootView.findViewById(R.id.PlaceView);
        /*StoryViewLayout.setVisibility(View.GONE);*/
        LoadingLayout = (LinearLayout) rootView.findViewById(R.id.Loading);
        /*LoadingLayout.setVisibility(View.VISIBLE);*/
        ErrorLayout = (LinearLayout) rootView.findViewById(R.id.Error);
        RefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh);
        imageLoader = MySingleton.getInstance(this.getActivity()).getImageLoader();
        //Set Today's date
        Today = activity.TodayDate();
        RefreshLayout.setColorSchemeResources(
                R.color.swipe_color_1, R.color.swipe_color_2,
                R.color.swipe_color_3, R.color.swipe_color_4);


        return rootView;
    }

    private void initiateRequest() {
        //Request StoryPage
        RefreshLayout.setRefreshing(true);
        JsonObjectRequest request = new JsonObjectRequest(url+Today, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                /*LoadingLayout.setVisibility(View.GONE);*/
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
        MySingleton.getInstance(getActivity()).addToRequestQueue(request);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.story, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Refresh:
                // We make sure that the SwipeRefreshLayout is displaying it's refreshing indicator
                if(!visiblity) {
                    if (!RefreshLayout.isRefreshing()) {
                        ErrorLayout.setVisibility(View.GONE);
                        RefreshLayout.setRefreshing(true);
                    }

                    // Start our refresh background task
                    initiateRequest();
                }
                return true;

            case R.id.Share:
                //InShort = sharedPreferences.getString(Startup.InShort, null);

                if (InShort!= null && !InShort.isEmpty())
                {
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Action")
                            .setAction("Share").setLabel("SingleStory")
                            .build());

                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Read\n"+ InShort +"\nwith Greet."+"\n"+"http://goo.gl/T1AS5u");
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
