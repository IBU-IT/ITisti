package com.example.lejla.gosarajevo;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener{
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
        setContentView(R.layout.activity_search_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        // Getting listview
        lv = (ListView) findViewById(R.id.list);

        // button show on map
        Button btnShowOnMap = (Button) findViewById(R.id.btn_show_map);

        // calling background Async task to load Google Places
        // After getting places from Google all the data is shown in listview
        new LoadPlaces().execute();

        /** Button click event for shown on map */
        btnShowOnMap.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                /*Intent i = new Intent(getApplicationContext(),
                        PlacesMapActivity.class);
                // Sending user current geo location
                i.putExtra("user_latitude", Double.toString(gps.getLatitude()));
                i.putExtra("user_longitude", Double.toString(gps.getLongitude()));

                // passing near places to map activity
                i.putExtra("near_places", nearPlaces);
                // staring activity
                startActivity(i);*/
                //alert.showAlertDialog(MainActivity.this,"Alert","Button Functionality not implemented !",false);
                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                startActivity(intent);
            }
        });

        /**
         * ListItem click event
         * On selecting a listitem SinglePlaceActivity is launched
         * */
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String reference = ((TextView) view.findViewById(R.id.reference)).getText().toString();

                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        SinglePlaceActivity.class);

                // Sending place refrence id to single place activity
                // place refrence id used to get "Place full details"
                in.putExtra(KEY_REFERENCE, reference);
                startActivity(in);
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


    public void sendMessage(View view)
    {
        Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if(parent.getId() == R.id.app_bar_main_gridView){
            switch (position){
                case 0:{

                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_mylocation) {
            Intent intent = new Intent(this, MapsActivityCurrentPlaces.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_favorites) {
            Intent intent = new Intent(this, FavoritesActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_aboutus) {
            Intent intent = new Intent(this, AboutUsActivity.class);
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


   /* /////////////////Main Menu
    private void iUI() {

        atms = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.atms);
        atms.setOnClickListener(this);

        banks = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.banks);
        banks.setOnClickListener(this);

        bookstores = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.bookstores);
        bookstores.setOnClickListener(this);

        busstations = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.busstations);
        busstations.setOnClickListener(this);

        cafes = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.cafes);
        cafes.setOnClickListener(this);

        carwash = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.carwash);
        carwash.setOnClickListener(this);

        dentist = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.dentist);
        dentist.setOnClickListener(this);

        doctor = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.doctor);
        doctor.setOnClickListener(this);

        food = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.food);
        food.setOnClickListener(this);

        gasstation = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.gasstation);
        gasstation.setOnClickListener(this);

        grocery = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.grocery);
        grocery.setOnClickListener(this);

        gym = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.gym);
        gym.setOnClickListener(this);

        hospitals = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.hospitals);
        hospitals.setOnClickListener(this);

        temples = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.temples);
        temples.setOnClickListener(this);

        theater = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.theater);
        theater.setOnClickListener(this);

        park = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.park);
        park.setOnClickListener(this);

        pharmacy = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.pharmacy);
        pharmacy.setOnClickListener(this);

        police = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.police);
        police.setOnClickListener(this);

        restaurant = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.restaurant);
        restaurant.setOnClickListener(this);

        school = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.school);
        school.setOnClickListener(this);

        mall = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.mall);
        mall.setOnClickListener(this);

        spa = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.spa);
        spa.setOnClickListener(this);

        store = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.store);
        store.setOnClickListener(this);

        university = (LinearLayout) findViewById(com.example.lejla.gosarajevo.R.id.university);
        university.setOnClickListener(this);

    }
    @Override
    public void onClick(View v) {

        //togglePeriodicLocationUpdates();

        switch (v.getId()) {

            case com.example.lejla.gosarajevo.R.id.atms:
                sConstants.topTitle = "ATMS LIST";
                sConstants.query = "atm";
                final Intent atm = new Intent(this, MainActivity.class);
                atm.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(atm);

                break;

            case com.example.lejla.gosarajevo.R.id.banks:
                sConstants.topTitle = "BANKS LIST";
                sConstants.query = "bank";
                final Intent bank = new Intent(this, MainActivity.class);
                bank.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(bank);

                break;

            case com.example.lejla.gosarajevo.R.id.bookstores:
                sConstants.topTitle = "BOOK STORES LIST";
                sConstants.query = "book_store";
                final Intent book_store = new Intent(this, MainActivity.class);
                book_store.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(book_store);

                break;
            case com.example.lejla.gosarajevo.R.id.busstations:
                sConstants.topTitle = "BUS STATION LIST";
                sConstants.query = "bus_station";
                final Intent bus_station = new Intent(this, MainActivity.class);
                bus_station.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(bus_station);

                break;
            case com.example.lejla.gosarajevo.R.id.cafes:
                sConstants.topTitle = "CAFES LIST";
                sConstants.query = "cafe";
                final Intent cafe = new Intent(this, MainActivity.class);
                cafe.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(cafe);

                break;

            case com.example.lejla.gosarajevo.R.id.carwash:
                sConstants.topTitle = "CAR WASH LIST";
                sConstants.query = "car_wash";
                final Intent car_wash = new Intent(this, MainActivity.class);
                car_wash.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(car_wash);

                break;

            case com.example.lejla.gosarajevo.R.id.dentist:
                sConstants.topTitle = "DENTIST LIST";
                sConstants.query = "dentist";
                final Intent dentist = new Intent(this, MainActivity.class);
                dentist.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(dentist);

                break;
            case com.example.lejla.gosarajevo.R.id.doctor:
                sConstants.topTitle = "DOCTOR LIST";
                sConstants.query = "doctor";
                final Intent doctor = new Intent(this, MainActivity.class);
                doctor.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(doctor);

                break;
            case com.example.lejla.gosarajevo.R.id.food:
                sConstants.topTitle = "FOOD LIST";
                sConstants.query = "food";
                final Intent food = new Intent(this, MainActivity.class);
                food.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(food);

                break;

            case com.example.lejla.gosarajevo.R.id.gasstation:
                sConstants.topTitle = "GAS STATION LIST";
                sConstants.query = "gas_station";
                final Intent gas_station = new Intent(this, MainActivity.class);
                gas_station.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(gas_station);

                break;

            case com.example.lejla.gosarajevo.R.id.grocery:
                sConstants.topTitle = "GROCERY LIST";
                sConstants.query = "grocery_or_supermarket";
                final Intent grocery_or_supermarket = new Intent(this,MainActivity.class);
                grocery_or_supermarket.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(grocery_or_supermarket);

                break;
            case com.example.lejla.gosarajevo.R.id.gym:
                sConstants.topTitle = "GYM LIST";
                sConstants.query = "gym";
                final Intent gym = new Intent(this, MainActivity.class);
                gym.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(gym);

                break;
            case com.example.lejla.gosarajevo.R.id.hospitals:
                sConstants.topTitle = "HOSPITALS LIST";
                sConstants.query = "hospital";
                final Intent hospital = new Intent(this, MainActivity.class);
                hospital.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(hospital);

                break;

            case com.example.lejla.gosarajevo.R.id.temples:
                sConstants.topTitle = "TEMPLES LIST";
                sConstants.query = "hindu_temple";
                final Intent temple = new Intent(this, MainActivity.class);
                temple.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(temple);

                break;

            case com.example.lejla.gosarajevo.R.id.theater:
                sConstants.topTitle = "THEATER LIST";
                sConstants.query = "movie_theater";
                final Intent movie_theater = new Intent(this, MainActivity.class);
                movie_theater.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(movie_theater);

                break;
            case com.example.lejla.gosarajevo.R.id.park:
                sConstants.topTitle = "PARK LIST";
                sConstants.query = "rv_park";
                final Intent rv_park = new Intent(this, MainActivity.class);
                rv_park.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(rv_park);

                break;
            case com.example.lejla.gosarajevo.R.id.pharmacy:
                sConstants.topTitle = "PHARMACY LIST";
                sConstants.query = "pharmacy";
                final Intent pharmacy = new Intent(this, MainActivity.class);
                pharmacy.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(pharmacy);

                break;

            case com.example.lejla.gosarajevo.R.id.police:
                sConstants.topTitle = "POLICE LIST";
                sConstants.query = "police";
                final Intent police = new Intent(this, MainActivity.class);
                police.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(police);

                break;

            case com.example.lejla.gosarajevo.R.id.restaurant:
                sConstants.topTitle = "RESTAURANT LIST";
                sConstants.query = "restaurant";
                final Intent restaurant = new Intent(this, MainActivity.class);
                restaurant.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(restaurant);

                break;
            case com.example.lejla.gosarajevo.R.id.school:
                sConstants.topTitle = "SCHOOL LIST";
                sConstants.query = "school";
                final Intent school = new Intent(this, MainActivity.class);
                school.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(school);

                break;
            case com.example.lejla.gosarajevo.R.id.mall:
                sConstants.topTitle = "SHOPPING MALL LIST";
                sConstants.query = "shopping_mall";
                final Intent shopping_mall = new Intent(this, MainActivity.class);
                shopping_mall.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(shopping_mall);

                break;

            case com.example.lejla.gosarajevo.R.id.spa:
                sConstants.topTitle = "SPA LIST";
                sConstants.query = "spa";
                final Intent spa = new Intent(this, MainActivity.class);
                spa.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(spa);

                break;

            case com.example.lejla.gosarajevo.R.id.store:
                sConstants.topTitle = "STORE LIST";
                sConstants.query = "store";
                final Intent store = new Intent(this, MainActivity.class);
                store.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(store);

                break;

            case com.example.lejla.gosarajevo.R.id.university:
                sConstants.topTitle = "UNIVERSITY LIST";
                sConstants.query = "university";
                final Intent university = new Intent(this, MainActivity.class);
                university.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(university);
        }
    }*/
}
