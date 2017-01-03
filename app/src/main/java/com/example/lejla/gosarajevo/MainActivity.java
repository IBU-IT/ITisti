package com.example.lejla.gosarajevo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.AdapterView;
import android.widget.GridView;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;


import java.util.ArrayList;
import java.util.HashMap;

import static android.R.id.toggle;

public class MainActivity extends AppCompatActivity {
    GridView gridView;

    // Alert Dialog Manager
    private final AlertDialogWin alert = new AlertDialogWin();

    // Places List
    public static PlacesList nearPlaces;

    // GPS Location
    private GPSTracker gps;

    // Progress dialog
    private ProgressDialog pDialog;

    // Places Listview
    private ListView lv;

    // ListItems data
    private final ArrayList<HashMap<String, String>> placesListItems = new ArrayList<>();

    // KEY Strings
    private static final String KEY_REFERENCE = "reference"; // id of the place
    private static final String KEY_NAME = "name"; // name of the place

    //types of places to search
    public static String types;

    private LinearLayout atms, banks, bookstores, busstations, cafes, carwash,
            dentist, doctor, food, gasstation, grocery, gym, hospitals,
            temples, theater, park, pharmacy, police, restaurant, school, mall,
            spa, store, university;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(R.drawable.ic_hamburger_24dp);



        ConnectivityDetector cd = new ConnectivityDetector(getApplicationContext());
        // Check if Internet present
        Boolean isInternetPresent = cd.isConnectingToInternet();
        if (!isInternetPresent) {
            // Internet Connection is not present
            alert.showAlertDialog(MainActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection");
            // stop executing code by return
            return;
        }
        // creating GPS Class object
        gps = new GPSTracker(this);

        // check if GPS location can get
        if (gps.canGetLocation()) {
            Log.d("Your Location", "latitude:" + gps.getLatitude() + ", longitude: " + gps.getLongitude());
        } else {
            // Can't get user's current location
            alert.showAlertDialog(MainActivity.this, "GPS Status",
                    "Couldn't get location information. Please enable GPS"
            );
            // stop executing code by return
            return;
        }




        Button btnWhereToEat = (Button) findViewById(R.id.btnWhereToEat);
        Button btnWhereToSleep = (Button) findViewById(R.id.btnWhereToSleep);
        Button btnWhereToGo = (Button) findViewById(R.id.btnWhereToGo);

        btnWhereToEat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (query.isEmpty()) {
//                    Toast.makeText(getApplicationContext(), "Enter valid Input", Toast.LENGTH_SHORT);
//                } else {
                    SearchResultActivity.types = "restaurant";
                    Intent intent = new Intent(MainActivity.this,SearchResultActivity.class);
                    startActivity(intent);
//                }
            }
        });

        btnWhereToSleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (query.isEmpty()) {
//                    Toast.makeText(getApplicationContext(), "Enter valid Input", Toast.LENGTH_SHORT);
//                } else {
                SearchResultActivity.types = "hotel";
                Intent intent = new Intent(MainActivity.this,SearchResultActivity.class);
                startActivity(intent);
//                }
            }
        });

        btnWhereToGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (query.isEmpty()) {
//                    Toast.makeText(getApplicationContext(), "Enter valid Input", Toast.LENGTH_SHORT);
//                } else {
                SearchResultActivity.types = "park";
                Intent intent = new Intent(MainActivity.this,SearchResultActivity.class);
                startActivity(intent);
//                }
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation view item clicks here.
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_mylocation) {
                    Intent intent = new Intent(getApplicationContext(),MapsActivityCurrentPlaces.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_favorites) {
                    Intent intent = new Intent(getApplicationContext(), FavoritesActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_settings) {
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_aboutus) {
                    Intent intent = new Intent(getApplicationContext(), AboutUsActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_share) {
                    //Menu code here
                } else if (id == R.id.nav_send) {
                    //Menu code here
                }

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });


       }

    /**
     * Background Async Task to Load Google places
     * */
    private class LoadPlaces extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage(Html.fromHtml("<b>Search</b><br/>Loading Places..."));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting Places JSON
         * */
        protected String doInBackground(String... args) {
            // creating Places class object
            GooglePlaces googlePlaces = new GooglePlaces();

            try {
                // Separeate your place types by PIPE symbol "|"If you want all types places make it as null
                // Check list of types supported by google String types = "cafe|restaurant";
                // Listing places only cafes, restaurants Radius in meters - increase this value if you don't find any places

                double radius = 1000; // 1000 meters

                // get nearest places
                Log.d("Check ", "Before calling googlePlaces.search()");
                nearPlaces = googlePlaces.search(gps.getLatitude(),
                        gps.getLongitude(), radius, types);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * and show the data in UI
         * Always use runOnUiThread(new Runnable()) to update UI from background
         * thread, otherwise you will get error
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed Places into LISTVIEW
                     * */
                    // Get json response status
                    String status = nearPlaces.status;

                    // Check for all possible status
                    switch (status) {
                        case "OK":
                            // Successfully got places details
                            if (nearPlaces.results != null) {
                                // loop through each place
                                for (Place p : nearPlaces.results) {
                                    HashMap<String, String> map = new HashMap<>();

                                    // Place reference won't display in listview - it will be hidden
                                    // Place reference is used to get "place full details"
                                    map.put(KEY_REFERENCE, p.reference);

                                    // Place name
                                    map.put(KEY_NAME, p.name);


                                    // adding HashMap to ArrayList
                                    placesListItems.add(map);
                                }
                                // list adapter
                                ListAdapter adapter = new SimpleAdapter(MainActivity.this, placesListItems,
                                        R.layout.list_item,
                                        new String[]{KEY_REFERENCE, KEY_NAME}, new int[]{
                                        R.id.reference, R.id.name});

                                // Adding data into listview
                                lv.setAdapter(adapter);

                                //SnackBar
                                Snackbar snackbar = Snackbar
                                        .make(findViewById(R.id.coordLayout),
                                                "Found " + placesListItems.size() + " Results", Snackbar.LENGTH_SHORT);
                                snackbar.show();
                            }
                            break;
                        case "ZERO_RESULTS":
                            // Zero results found
                            alert.showAlertDialog(MainActivity.this, "Near Places",
                                    "Sorry no places found. Try to change the types of places"
                            );
                            break;
                        case "UNKNOWN_ERROR":
                            alert.showAlertDialog(MainActivity.this, "Places Error",
                                    "Sorry unknown error occured."
                            );
                            break;
                        case "OVER_QUERY_LIMIT":
                            alert.showAlertDialog(MainActivity.this, "Places Error",
                                    "Sorry query limit to google places is reached"
                            );
                            break;
                        case "REQUEST_DENIED":
                            alert.showAlertDialog(MainActivity.this, "Places Error",
                                    "Sorry error occured. Request is denied"
                            );
                            break;
                        case "INVALID_REQUEST":
                            alert.showAlertDialog(MainActivity.this, "Places Error",
                                    "Sorry error occured. Invalid Request"
                            );
                            break;
                        default:
                            alert.showAlertDialog(MainActivity.this, "Places Error",
                                    "Sorry error occured."
                            );
                            break;
                    }
                }
            });

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_about:
                Intent intent = new Intent(this, AboutActivity.class);
                this.startActivity(intent);
                break;
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
                break;
            default:
               // return super.onOptionsItemSelected(item);
        }

        return true;
    }


    public void sendMessage(View view)
    {
        Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
        startActivity(intent);
    }

   /* @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if(parent.getId() == R.id.app_bar_main_gridView){
            switch (position){
                case 0:{

                }
            }
        }
    }*/


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
 }
