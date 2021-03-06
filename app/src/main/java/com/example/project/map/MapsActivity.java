package com.example.project.map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.notification.AlarmBroadcastReceiver;
import com.example.project.notification.NotificationService;
import com.example.project.ParametersAsync.CloseReservationParamsAsync;
import com.example.project.ParametersAsync.LoadStationParamsAsync;
import com.example.project.ParametersAsync.OpenReservationParamsAsync;
import com.example.project.ParametersAsync.ServerTask;
import com.example.project.R;
import com.example.project.favourites.Favourite;
import com.example.project.favourites.FavouritesActivity;
import com.example.project.reservation.ReservationsActivity;
import com.example.project.userManagement.LoginActivity;
import com.example.project.userManagement.ProfileActivity;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shawnlin.numberpicker.NumberPicker;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import javax.net.ssl.HttpsURLConnection;

import static com.example.project.notification.AlarmBroadcastReceiver.ACTION_ALARM;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "Mappa";

    //LAYOUT

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private List<Marker> AllMarkers;
    private SlidingUpPanelLayout panel;
    private ConstraintLayout filterLayout;
    private ConstraintLayout stationLayout;

    private Station station_selected;
    private Place place_searched;
    private Marker marker_searched;

    // Keys for storing map activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";


    //GEOLOCALIZZAZIONE
    private Location mLastKnownLocation;
    private final LatLng mDefaultLocation = new LatLng(45.070841, 7.668552);
    private static final int DEFAULT_ZOOM = 15;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted = false;
    private boolean mLocationFound=false;


    //SCANNER
    private final static int NOTIFICATION_REQUESTCODE = 101;
    private final static int SCANNER_REQUEST_CODE = 2;
    private final static int PROFILE_REQUEST_CODE = 1;
    private final static int MY_CAMERA_REQUEST_CODE = 100;
    FloatingActionButton qrButton;

    //AUTENTICAZIONE
    FirebaseAuth mAuth;
    FirebaseUser user;

    //CURRENT RESERVATION INSTANCE
    private CurrentReservation currentReservation;

    //FILTRI
    private final static float RAGGIO_AREA= 1500;
    private String[] raggioPickerValues = { " 0.5 ", " 1 "," 1.5 ", " 2 "};
    private String[] tariffaPickerValues = { " 0.02 ", " 0.03 "," 0.05 "};

    private float raggioValue;
    private double tariffaValue;
    private Favourite areaValue;

    //TIMER
    private String[] timerPickerValues = {"15","20","25","30","35","40","45","50","60"};

    //PREFERITI
    private CheckBox checkBoxPreferiti;
    private Spinner spinnerFiltroPreferiti;

    //VARIE
    private DrawerLayout drawerLayout;

    //POPUP
    private String new_parking_rdrct;
    private AlertDialog popup;
    private AlertDialog popup_time;
    ConstraintLayout layoutReservation;

    //NAVIGATORE
    private LinkedList<LatLng> polylinesPoints;
    private Polyline polyline;
    private LocationManager locationManager;
    private LocationListener locationListener;

    //DIALOG PER INSERIRE IL NOME DELLO STALLO PREFERITO
    private Favourite newFavParking;
    private final static String FILE_PATH = "FavouritesMapFile.txt";

    //ALARM
    private static AlarmManager alarmManager;
    private static PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout_main);

        //SLIDER STAZIONI

        panel = findViewById(R.id.sliding_layout);
        panel.setPanelState(PanelState.HIDDEN);

        filterLayout = findViewById(R.id.panel_filter_layout_id);
        stationLayout = findViewById(R.id.panel_station_layout_id);

        //STAZIONI PREFERITE
        checkBoxPreferiti = findViewById(R.id.panel_station_favorite_id);
        spinnerFiltroPreferiti = findViewById(R.id.panel_filter_spinner_area_id);




        checkBoxPreferiti.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //controllo per distiguere il click dell'utente dal setChecked()
                if(!buttonView.isPressed())
                    return;

                if(isChecked){
                    //FLAVIA, MEMORIZZO LA STAZIONE SELEZIONATA NEL FILE
                    // usa station_seleceted per avere i dettagli sulla stazione
                    //dialog con edit text per inserire la label
                    AlertDialog dialog = createEd().create();
                    dialog.show();
                    newFavParking= new Favourite("noncaricata",
                            station_selected.getLatitude(),
                            station_selected.getLongitude());
                    //Dall'azione di conferma nella dialog completo newFavParking e aggiorno la mappa




                }else{
                    Log.i("flavia", "Ho deselezionato il preferito");
                    //FLAVIA, RIMUOVI LA STAZIONE SELEZIONATA DAL FILE
                    // usa station_seleceted per avere i dettagli sulla stazione
                    deleteFavouriteFromFile(new Favourite("-123", station_selected.getLatitude(), station_selected.getLongitude()));
                    buttonView.setChecked(false);
                    /*Map<String, Favourite> favourites= loadFavouriteFromFile();
                    for(Favourite f: favourites.values()){
                        if(f.getLat()== station_selected.getLatitude() && f.getLon()== station_selected.getLongitude()){
                            //lo tolgo dalla mappa
                            favourites.remove(f);
                            //ricarico la mappa sul file
                            saveMapOnFile(favourites);
                        }
                    }*/
                }
            }
        });


        //AUTENTICAZIONE
        mAuth = FirebaseAuth.getInstance();

        // SETTO LISTA MARKERS
        AllMarkers = new ArrayList<>();


        // TOOLBAR E NAVIGATION DRAWER
        Toolbar toolbar = findViewById(R.id.toolbar);
        setToolbar(toolbar);

        // MAPPA : Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (savedInstanceState != null) {  //Recupero informazioni sull'ultima posizione rilevata
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            CameraPosition mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        createMap();

        //IMPOSTO FILTRI
        pickers();


        // CONTROLLA CURRENT RESERVATION
        currentReservation = new CurrentReservation();// In realtà dovrebbe chiedere al server e se non c'è la crea, se c'è la setta
        String urlCurrRes = "https://smartparkingpolito.altervista.org/GetCurrentReservation.php";
        new CheckCurrentReservation().execute(urlCurrRes);


        // MESSAGE BROADCAST RECEIVER per catturare i messaggi provenienti dal Notification Service
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (intent.hasExtra("id_parking")) {
                            String id_parking = intent.getStringExtra("id_parking");
                            String distance = intent.getStringExtra("distance");
                            String address = intent.getStringExtra("address");
                            showRdrctPopup(id_parking, distance, address);
                            new_parking_rdrct = id_parking;
                        } else if(intent.hasExtra("time")){
                            showTimeExtensionPopup();
                        } else
                            showRdrctPopup(null, null, null);


                    }
                }, new IntentFilter(NotificationService.ACTION_MESSAGE_BROADCAST)
        );


        // BOTTONE PER QR CODE ACTIIVTY
        qrButton = findViewById(R.id.floatingQrButton);
        setQrButton(qrButton);
        //EVIDENZIA STATO PRENOTAZIONE IN CORSO
        layoutReservation = findViewById(R.id.linLayoutCurrRes);
        if (currentReservation.id_booking != null) {
            layoutReservation.setVisibility(View.VISIBLE);
        }


    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {

        super.onResume();
        Intent notificationIntent = getIntent();
        if (notificationIntent.getIntExtra("requestCode", 0) == NOTIFICATION_REQUESTCODE && currentReservation.id_booking!=null) {
            if (notificationIntent.hasExtra("id_parking")) {
                String id_park = (String) notificationIntent.getExtras().get("id_parking");
                String dist = (String) notificationIntent.getExtras().get("distance");
                String address = (String) notificationIntent.getExtras().get("address");
                Log.i("address", address);
                showRdrctPopup(id_park, dist, address);
            } else if(notificationIntent.hasExtra("time")){
                showTimeExtensionPopup();
            } else
                showRdrctPopup(null, null, null);
        }

    }


    //When initializing your Activity, check to see if the user is currently signed in.
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        user = mAuth.getCurrentUser();
        //mAuth.signOut();
        //updateUI(currentUser);
    }

    //ACTIVITY IN PAUSA
    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        //salvo lo stato della mappa quando l'activity è in pausa
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    //IMPOSTO LA TOOLBAR:---------------------------------------------------------------------

    //IMPOSTO LA TOOLBAR
    private void setToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); // for add back arrow in action bar
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.map_ic_menu_black_24dp);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("");
        setNavigationDrawer(toolbar);
        Drawable filterIcon = getDrawable(R.drawable.map_ic_filter_list_black_24dp);
        toolbar.setOverflowIcon(filterIcon);
        setAutocomplete();
    }

    //COLLEGO IL NAVIGATION LAYOUT ALLA TOOLBAR
    private void setNavigationDrawer(Toolbar toolbar) {
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.map_navigationDrawer_id);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);
                switch (menuItem.getItemId()) {
                    case R.id.show_profile_id:
                        if (mAuth.getCurrentUser() != null) {
                            startActivity(new Intent(MapsActivity.this, ProfileActivity.class));
                            Log.i(TAG, "Ho cliccato su Profilo nel menu. User:" + mAuth.getCurrentUser().getEmail());
                        } else {
                            mAuth.signOut();
                            Toast.makeText(MapsActivity.this, "Non sei autenticato!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MapsActivity.this, LoginActivity.class));
                        }
                        return true;
                    case R.id.logout_profile_id:
                        mAuth.signOut();
                        startActivity(new Intent(MapsActivity.this, LoginActivity.class));
                        return true;
                    case R.id.show_reservations:
                        startActivity(new Intent(MapsActivity.this, ReservationsActivity.class));
                        return true;
                    case R.id.favourite_management_id:
                        startActivity(new Intent(MapsActivity.this, FavouritesActivity.class));
                        return true;
                    default:
                        return true;
                }
            }
        });
        ActionBarDrawerToggle actionBarDrawerToggle =
                new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close) {
                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        super.onDrawerOpened(drawerView);
                    }
                };

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

    }

    //RICERCA LUOGO
    private void setAutocomplete(){
        //AUTOCOMPLETAMENTO INDIRIZZI

        String apiKey = getString(R.string.place_autocomplete_key);

        /*
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.
        final AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        ImageView searchIcon = (ImageView) ((LinearLayout) autocompleteFragment.getView()).getChildAt(0);
        searchIcon.setVisibility(View.GONE);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NotNull Place place) {
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + " - " + place.getLatLng());
                LatLng newPosition = new LatLng(place.getLatLng().latitude,place.getLatLng().longitude);
                place_searched=place;
                MarkerOptions markerSearch = new MarkerOptions().position(newPosition)
                        .title(place.getName());
                if (marker_searched!=null) marker_searched.remove();
                marker_searched=mMap.addMarker(markerSearch);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), DEFAULT_ZOOM));
                refresh();
                askStations(place.getLatLng().latitude,place.getLatLng().longitude);





            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

    }


    //IMPLEMENTO I FILTRI--------------------------------------------------------------------------

    //REDIRECT A PANNELLO FILTRI
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_filters_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                // User chose the "Settings" item, show the app settings UI...
                updateSpinner();
                Log.i(TAG,"Apro Filtri");
                stationLayout.setVisibility(View.GONE);
                filterLayout.setVisibility(View.VISIBLE);
                panel.setPanelState(PanelState.EXPANDED);

                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    //INIZIALIZZO FILTRI
    private void pickers(){

        NumberPicker pickerRaggio =  findViewById(R.id.panel_filter_npick_raggio_id);
        NumberPicker pickerTariffa = findViewById(R.id.panel_filter_npick_tariffa_id);
        NumberPicker pickerTimer = findViewById(R.id.panel_station_npick_tempo_id);
        setNumberPicker(pickerRaggio,raggioPickerValues);
        setNumberPicker(pickerTariffa,tariffaPickerValues);
        setNumberPicker(pickerTimer,timerPickerValues);
    }

    //IMPOSTO VALORI PICKER
    private void setNumberPicker(NumberPicker nubmerPicker, String [] numbers ){
        nubmerPicker.setMinValue(1);
        nubmerPicker.setMaxValue(numbers.length);
        nubmerPicker.setDisplayedValues(numbers);
        nubmerPicker.setValue(numbers.length/2+1);
    }

    //GESTIONE APPLICAZIONE FILTRI
    public void onApplyFilter(View view){
        NumberPicker raggioPicker = findViewById(R.id.panel_filter_npick_raggio_id);
        raggioValue = Float.parseFloat(raggioPickerValues[raggioPicker.getValue()-1]);

        Map<String,Favourite> favo = loadFavouriteFromFile();
        if(spinnerFiltroPreferiti.getSelectedItem()!=null) {
            areaValue = favo.get(spinnerFiltroPreferiti.getSelectedItem().toString());
        }
        else areaValue=null;

        NumberPicker tariffaPicker = findViewById(R.id.panel_filter_npick_tariffa_id);
        tariffaValue = Double.parseDouble(tariffaPickerValues[tariffaPicker.getValue()-1]);


        Log.i(TAG,"Filtro-1: ValoreArea-> "+areaValue+ "- spinn: -"+spinnerFiltroPreferiti.getSelectedItem()+"- -"+favo.size());
        Log.i(TAG,"Filtro-2: ValoreRaggio-> "+raggioValue);
        Log.i(TAG,"Filtro-3: ValoreTariffa-> "+tariffaValue);
        Log.i(TAG,"VALORI MAPPA");
        for (String string: favo.keySet()) {
            Log.i(TAG,"val:  -"+string+"-");
        }


        listenFilter();

        panel.setPanelState(PanelState.HIDDEN);

    }
    private void listenFilter(){
        boolean checkRay=false;
        boolean checkArea=false;
        boolean checkTariffa=false;
        Switch areaSwitch = findViewById(R.id.panel_filter_switch_area_id);
        Switch raggioSwitch = findViewById(R.id.panel_filter_switch_raggio_id);
        Switch tariffaSwitch = findViewById(R.id.panel_filter_switch_tariffa_id);

        if(areaSwitch.isChecked()){
            if(areaValue==null){
                Toast.makeText(MapsActivity.this,R.string.panel_filter_toast_no_selected_area,Toast.LENGTH_SHORT).show();
            }
            else{
                checkArea=true;
                LatLng posPref = new LatLng(areaValue.getLat(), areaValue.getLon());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posPref, DEFAULT_ZOOM));
            }

        }
        if(raggioSwitch.isChecked()) {
            if (place_searched == null) {
                Toast.makeText(MapsActivity.this, R.string.panel_filter_toast_no_selected_place, Toast.LENGTH_SHORT).show();
            }
            else checkRay=true;
        }
        if(tariffaSwitch.isChecked()){
            checkTariffa=true;
        }

            for (Marker marker: AllMarkers) {
                marker.setVisible(true);
                checkFilterMarker(marker,checkArea,checkRay,checkTariffa);
            }
    }

    //CHECKFILTERS
    private void checkFilterMarker(Marker marker,boolean checkArea, boolean checkRay, boolean checkTariffa){

        Station station = (Station) marker.getTag();
        boolean check=true;
        //FILTRO AREA
        if(checkArea&&check&&areaValue!=null){
            float[] results = new float[1];
            Location.distanceBetween(areaValue.getLat(), areaValue.getLon(), station.getLatitude(), station.getLongitude(), results);
            float distanceInMeters = results[0];
            check = distanceInMeters < RAGGIO_AREA;
        }
        //FILTRO RAGGIO
        if(checkRay&&check&&place_searched!=null){
            float[] results = new float[1];
            Location.distanceBetween(place_searched.getLatLng().latitude, place_searched.getLatLng().longitude, station.getLatitude(), station.getLongitude(), results);
            float distanceInMeters = results[0];
            check = distanceInMeters < raggioValue*1000;
        }
        //FILTRO TARIFFA
        if(checkTariffa&&check){
            check =  tariffaValue == station.getCost_minute();
        }

        if(check){
            marker.setVisible(true);
        }
        else{
            marker.setVisible(false);
        }

    }

    //IMPOSTO LA MAPPA + LOCALIZZAZIONE:---------------------------------------------------------------------

    private void createMap() {
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "mappa Pronta");
        mMap = googleMap;
        setMap();

        //CARICO LE STAZIONI
        askStations();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getTag()==null) return false;
                Log.i(TAG, "markerClick");

                station_selected = (Station) marker.getTag();
                TextView stationId = findViewById(R.id.panel_station_description_id);
                TextView tariffaTextId = findViewById(R.id.panel_station_tariffa_text_id);
                TextView distanceTextId = findViewById(R.id.panel_station_distance_text_id);

                Log.i(TAG, "Imposto panel ed apro");

                //verifico se la stazione è fra i preferiti e in caso positivo coloro la stellina
                if(isAFavStation(station_selected)){
                    //spunto la stellina
                    checkBoxPreferiti.setChecked(true);
                }else{
                    checkBoxPreferiti.setChecked(false);
                }

                String idPark = station_selected.getId_parking().toString()+" - "+station_selected.getStreet();
                stationId.setText(idPark);
                String tariff=station_selected.getCost_minute() +"€/minute";
                tariffaTextId.setText(tariff);


                if(mLocationPermissionGranted&&mLastKnownLocation!=null&&mLocationFound){
                    float[] results = new float[1];
                    Location.distanceBetween(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), station_selected.getLatitude(), station_selected.getLongitude(), results);
                    float distanceInKm = results[0]/1000;
                    String dst= String.format("%.2f", distanceInKm);
                    distanceTextId.setText(dst+" km");
                }

                filterLayout.setVisibility(View.GONE);
                stationLayout.setVisibility(View.VISIBLE);
                panel.setPanelState(PanelState.EXPANDED);
                Log.i(TAG, "marker aperto");

                return false;
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                panel.setPanelState(PanelState.HIDDEN);
            }
        });

        Log.i(TAG, "Stations charged");


    }

    //IMPOSTAZIONI MAPPA
    private void setMap() {
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);

        //Riposiziono il bottone di geolocalizzazione
        View locationButton = ((View) findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        rlp.setMargins(0, 240, 180, 0);

        // Posizione di default
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));

        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Attivo listener su cambiamenti di posizione
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {

                LatLng posizioneUtente = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posizioneUtente, 15));

                if(polylinesPoints != null && polylinesPoints.size()>0) {
                    /* Per maggiore precisione, si può ricalcolare il percorso ad ogni spostamento dell'utente
                    polyline.remove();
                    polylinesPoints.clear();
                    new DownloadTask().execute("https://maps.googleapis.com/maps/api/directions/json?origin=" +
                            posizioneUtente.latitude + "," + posizioneUtente.longitude +
                            "&destination=" +
                            destinazione.latitude + "," + destinazione.longitude +
                            "&key=AIzaSyBdcgZSbXkUcPAdylZgfAuK347e7J093WE");
                     */

                    polylinesPoints.remove(0);
                    polylinesPoints.add(0, posizioneUtente);
                    drawPolylines();
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) { }

            @Override
            public void onProviderEnabled(String s) { }

            @Override
            public void onProviderDisabled(String s) { }

        };

        //Acquisisco i permessi e  riposiziono la vista
        checkLocationPermission();
        //while(!mLocationPermissionGranted){ }
        setDeviceLocation();

        return;
    }





    //IMPOSTO POSIZIONE DISPOSITIVO
    private void setDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        Log.i(TAG, "GEOLOCALIZZAZIONE-3: Procedo ad impostare posizione device");
        try {
            if (mLocationPermissionGranted) {
                Log.i(TAG, "GEOLOCALIZZAZIONE-4: Permessi -->" + mLocationPermissionGranted);
                Log.i(TAG, "GEOLOCALIZZAZIONE-5: Imposto bottone geolocalizzazione");

                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        if (mLocationPermissionGranted)
                            askForGPS();
                        else
                            Toast.makeText(MapsActivity.this, "Permessi disattivati", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });

                Log.i(TAG, "GEOLOCALIZZAZIONE-6: Verifico attivazione GPS");
                askForGPS();

                Log.i(TAG, "GEOLOCALIZZAZIONE-7: Acquisisco ed imposto posizione");
                setMyLocation();

            } else {
                Log.i(TAG, "GEOLOCALIZZAZIONE-4: Permessi -->" + mLocationPermissionGranted + " , Imposto posizione default");
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", Objects.requireNonNull(e.getMessage()));
        }
    }

    //ACQUISISCO POSIZIONE DISPOSITIVO
    private void setMyLocation() {
        Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    // Set the map's camera position to the current location of the device.
                    mLastKnownLocation = task.getResult();
                    Log.i(TAG, "GEOLOCALIZZAZIONE-7a: Task terminato, Posizione Acquisita --> " + mLastKnownLocation);
                    if (mLastKnownLocation != null) {
                        Log.i(TAG, "GEOLOCALIZZAZIONE-7b: Imposto posizione nella mappa");
                        mLocationFound=true;
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                    } else {
                        Log.i(TAG, "GEOLOCALIZZAZIONE-7b: Posizione nulla, Imposto default");
                        Log.e(TAG, "Exception: %s", task.getException());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                    }

                } else {
                    Log.i(TAG, "GEOLOCALIZZAZIONE-7a: Task senza susccesso, Imposto default");
                    Log.e(TAG, "Exception: %s", task.getException());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));

                }
            }
        });


    }

    //VERIFICO GPS ATTIVO
    private void askForGPS() {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ApiException.class);
                    Log.i(TAG, "GEOLOCALIZZAZIONE-6a: GPS attivo, si procede all'attivazione");
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            Log.i(TAG, "GEOLOCALIZZAZIONE-6a: GPS non attivo, richiedo attivazione");
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(MapsActivity.this,
                                        LocationRequest.PRIORITY_HIGH_ACCURACY);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });

    }

    //RICHIESTA PERMESSI GEOLOCALIZZAZIONE
    private void checkLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        Log.i(TAG, "GEOLOCALIZZAZIONE-1: Verifico permessi");
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "GEOLOCALIZZAZIONE-2: permessi già accordati");
            mLocationPermissionGranted = true;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);

        } else {
            Log.i(TAG, "GEOLOCALIZZAZIONE-2: permessi NON accordati, richiedo...");
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

        }
    }


    //BOTTONI FLOATING + PERMESSI+ BOTTONE PRENOTAZIONE --------------------------------------
    private void setQrButton(View qrButton) {
        qrButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MapsActivity.this,   //controlla che il permesso sia garantito
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MapsActivity.this,   //richiesta di permesso all'utente
                            new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
                } else {
                    Intent intent = new Intent(MapsActivity.this, ScannerActivity.class);
                    boolean canCloseReservation = false;
                    if (currentReservation.getId_booking() != null) {
                        canCloseReservation = true;
                    }
                    intent.putExtra("canCloseReservation", canCloseReservation);
                    startActivityForResult(intent, SCANNER_REQUEST_CODE);

                }
            }
        });
    }

    private void askStations() {
        LatLng currentPosition = mMap.getCameraPosition().target;
        LoadStationParamsAsync parametersAsync = new LoadStationParamsAsync(currentPosition.latitude, currentPosition.longitude);
        new LoadStations().execute(parametersAsync);
    }
    private void askStations(Double latitude, Double longitude) {
        String city = "Torino";//fittizia
        LoadStationParamsAsync parametersAsync = new LoadStationParamsAsync(latitude, longitude);
        new LoadStations().execute(parametersAsync);
    }

    //Apre la reservation e nasconde panel
    public void onBookStation(View view) {
        if (currentReservation.getId_booking() == null) {
            OpenReservationParamsAsync paramsAsync = new OpenReservationParamsAsync(mAuth.getUid(), station_selected.getId_parking(),  0);
            new OpenReservation().execute(paramsAsync);
            panel.setPanelState(PanelState.HIDDEN);


        } else Toast.makeText(this, R.string.alreadybooked, Toast.LENGTH_LONG).show();


    }
    //refresh di all markers
    public void onRefreshClick(View view) {
        if (currentReservation.id_booking==null)refresh();
    }
    public void refresh(){
        for (Marker mLocationMarker : AllMarkers) {
            mLocationMarker.remove();
        }
        AllMarkers.clear();
        askStations();
        //controllo sui filtri

        listenFilter();
    }

    public void onCloseResButtonClick(View view) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alertCloseReser))
                .setMessage(getString(R.string.alertCloseMessage))

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String id_user = mAuth.getUid();
                        Integer id_parking = currentReservation.parking_id;
                        String id_booking = currentReservation.id_booking;// currentReservation.getId(); recupera id_booking da istanza currentReservation
                        CloseReservationParamsAsync paramsAsync = new CloseReservationParamsAsync(id_user, id_parking, id_booking, 1);
                        new CloseReservation().execute(paramsAsync);

                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    //TASK ASYNC:---------------------------------------------------------------------------------

    //1- LOAD STATION, permette di acquisire tutte le stazioni nell'area visualizzata durante il primo accesso alla mappa
    @SuppressLint("StaticFieldLeak")
    private class LoadStations extends AsyncTask<LoadStationParamsAsync, Void, ArrayList<Station>> {
        @Override
        protected ArrayList<Station> doInBackground(LoadStationParamsAsync... parametersAsyncs) {

            String url = "https://smartparkingpolito.altervista.org/AvailableParking.php";
            String params;
            ArrayList<Station> station = new ArrayList();

            //Encoding parametri:
            String lat_dest_string = String.valueOf(parametersAsyncs[0].latitude); //converto in stringhe i valori per inserirli nella richiesta
            String long_dest_string = String.valueOf(parametersAsyncs[0].longitude);

            try {
                params = "lat_destination=" + URLEncoder.encode(lat_dest_string, "UTF-8")
                        + "&long_destination=" + URLEncoder.encode(long_dest_string, "UTF-8");

                JSONArray jArray = ServerTask.askToServer(params, url);
                for (int i = 0; i < jArray.length(); i++) {         //Ciclo di estrazione oggetti
                    JSONObject json_data = jArray.getJSONObject(i);
                    String latitudine = json_data.getString("latitude");
                    String longitudine = json_data.getString("longitude");
                    String city = json_data.getString("city");
                    String street = json_data.getString("street");
                    Integer id_parking = Integer.parseInt(json_data.getString("id_parking"));
                    double cost_minute = Double.parseDouble(json_data.getString("cost_minute"));
                    double lat = Double.parseDouble(latitudine);
                    double lng = Double.parseDouble(longitudine);
                    Station stat = new Station(lat, lng, city, street, id_parking, cost_minute);// usare dati scaricati
                    station.add(stat);

                }


            } catch (Exception e) {
                Log.e("log_tag", "Error " + e.toString());
            }
            return station;
        }

        protected void onPostExecute(ArrayList<Station> stations) {
            Log.e(TAG, "PostExecute");
            for (int i = 0; i < stations.size(); i++) {         //Ciclo di estrazione oggetti
                Station stat = stations.get(i);
                LatLng position = new LatLng(stat.getLatitude(), stat.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions().position(position)
                        .title("Stazione N " + stat.getId_parking().toString())
                        .icon(bitmapDescriptorFromVector(getApplicationContext()));
                Marker marker = mMap.addMarker(markerOptions);//BitmapDescriptorFactory.fromResource(R.drawable.map_pin)
                AllMarkers.add(marker);
                marker.setTag(stat);

                Log.i(TAG, "marker aggiunto");
            }
        }
    }

    //2- OPEN RESERVATION, permette di aprire una map_icona_panel_prenotazione
    @SuppressLint("StaticFieldLeak")
    private class OpenReservation extends AsyncTask<OpenReservationParamsAsync, Void, String> {
        @Override
        protected String doInBackground(OpenReservationParamsAsync... parametersAsyncs) {
            String url = "https://smartparkingpolito.altervista.org/OpenReservation.php";
            String params;
            String control = null;

            //Encoding parametri:
            String bonus_string = String.valueOf(parametersAsyncs[0].bonus);
            String id_parking_string = String.valueOf(parametersAsyncs[0].id_parking);
            String id_user_string = parametersAsyncs[0].id_user;
            Log.i(TAG, "dettagli map_icona_panel_prenotazione: " + bonus_string + " " + id_parking_string + " " + id_user_string);
            try {
                params = "id_user=" + URLEncoder.encode(id_user_string, "UTF-8")
                        + "&bonus=" + URLEncoder.encode(bonus_string, "UTF-8")
                        + "&id_parking=" + URLEncoder.encode(id_parking_string, "UTF-8");

                JSONArray jsonArray = ServerTask.askToServer(params, url);
                //gestisci JsonArray
                JSONObject jsonObjectId = jsonArray.getJSONObject(0);
                JSONObject jsonObjectControl = jsonArray.getJSONObject(1);// index 0 booking_id, index 1 control_status
                control = jsonObjectControl.getString("control");

                Log.i("cntr0", control);
                if (control.equals("OK")) {   // non esegue l'if
                    currentReservation.id_booking = jsonObjectId.getString("booking_id");
                    currentReservation.parking_id = parametersAsyncs[0].id_parking;
                    currentReservation.latitude = Float.valueOf(jsonObjectId.getString("lat"));
                    currentReservation.longitude = Float.valueOf(jsonObjectId.getString("long"));
                }
                //avverti l'utente che il posto è stato occupato o non ha soldi

                //Mostrare apertura map_icona_panel_prenotazione
                //POP-UP CHE MOSTRA ALL'UTENTE IL RISULTATO;

            } catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
            }
            return control;

        }

        @Override
        protected void onPostExecute(String result) {
            switch (result) {
                case "OK":
                    Toast.makeText(MapsActivity.this, getString(R.string.reserv_success), Toast.LENGTH_LONG).show();
                    layoutReservation.setVisibility(View.VISIBLE);
                    //ALARM per gestione tempo di prenotazione
                    setAlarm();
                    //Percorso per NAVIGATORE
                    calcolaPercorso(new LatLng(currentReservation.latitude, currentReservation.longitude));
                    break;
                case "OCCUPIED":
                    Toast.makeText(MapsActivity.this, getString(R.string.occupied), Toast.LENGTH_LONG).show();
                    break;
                case "ZERO_WALLET":
                    Toast.makeText(MapsActivity.this, getString(R.string.zero_wallet), Toast.LENGTH_LONG).show();
                    break;
                case "CONN_ERROR":
                    Toast.makeText(MapsActivity.this, getString(R.string.error_close_res), Toast.LENGTH_LONG).show();
                    break;
            }

        }

    }

    //3- CLOSE RESERVATION, permette di chiudere una map_icona_panel_prenotazione
    @SuppressLint("StaticFieldLeak")
    private class CloseReservation extends AsyncTask<CloseReservationParamsAsync, Void, String> {
        @Override
        protected String doInBackground(CloseReservationParamsAsync... parametersAsyncs) {

            String url = "https://smartparkingpolito.altervista.org/CloseReservation.php";
            String params = null;
            String result;

            //Encoding parametri:
            String id_booking_string = parametersAsyncs[0].booking_id;
            String successful_string = String.valueOf(parametersAsyncs[0].successfull);
            String id_parking_string = String.valueOf(parametersAsyncs[0].id_parking);
            String id_user_string = parametersAsyncs[0].id_user;
            try {
                params = "id_user=" + URLEncoder.encode(id_user_string, "UTF-8")
                        + "&successful=" + URLEncoder.encode(successful_string, "UTF-8")
                        + "&id_booking=" + URLEncoder.encode(id_booking_string, "UTF-8")
                        + "&id_parking=" + URLEncoder.encode(id_parking_string, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            JSONArray jsonArray = ServerTask.askToServer(params, url);
            try {
                result = jsonArray.getJSONObject(0).getString("result");
            } catch (JSONException e) {
                e.printStackTrace();
                result = "ERROR";
            }

            return result;

        }

        @Override
        protected void onPostExecute(String result) {
            switch (result) {
                case "OK":
                    currentReservation.reset();
                    Toast.makeText(MapsActivity.this, getString(R.string.ok), Toast.LENGTH_LONG).show();
                    layoutReservation.setVisibility(View.INVISIBLE);
                    //Chiudi ALARM
                    if(alarmIntent!=null && alarmManager!=null)
                        closeAlarm();
                    //Chiudi Navigatore (Cancella percorso)
                    removeRoute();
                    break;
                case "WRONG_PARKING":
                    Toast.makeText(MapsActivity.this, getString(R.string.sorry), Toast.LENGTH_LONG).show();
                    break;
                case "ERROR":
                    Toast.makeText(MapsActivity.this, getString(R.string.error_close_res), Toast.LENGTH_LONG).show();
                    break;
            }

        }

    }


    //4-CHECK CURRENT RESERVATION, controlla se sul server è settata una map_icona_panel_prenotazione per l'user corrente, evita di perdere
    // la map_icona_panel_prenotazione se l'app viene chiusa
    @SuppressLint("StaticFieldLeak")
    private class CheckCurrentReservation extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                String params = "id_user=" + URLEncoder.encode(mAuth.getUid(), "UTF-8");
                JSONArray jArray = ServerTask.askToServer(params, strings[0]);
                try {
                    JSONObject json_result = jArray.getJSONObject(1);
                    if (json_result.getString("result").equals("ok")) {
                        JSONObject json_body = jArray.getJSONObject(0);
                        currentReservation.setId_booking(json_body.getString("id_booking"));
                        currentReservation.setParking_id(Integer.parseInt(json_body.getString("parking_id")));
                        currentReservation.setBonus(Integer.parseInt(json_body.getString("bonus")));
                        currentReservation.setSuccessful(Integer.parseInt(json_body.getString("successful")));
                    }

                } catch (JSONException ex) {
                    ex.printStackTrace();
                }

            } catch (Exception e) {
                Log.e("log_tag", "Error " + e.toString());
            }
            return currentReservation.id_booking != null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.i("icona", result.toString());
            if (result) layoutReservation.setVisibility(View.VISIBLE);

        }

    }

    // METODI CALLBACK:----------------------------------------------------------------------------

    //Callback method after startActivityForResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SCANNER_REQUEST_CODE:  //GESTIONE EVENTI SCANNER
                if (resultCode == Activity.RESULT_OK) {
                    int result = Integer.parseInt(Objects.requireNonNull(data.getStringExtra("parking_code")));
                    Toast.makeText(MapsActivity.this, Integer.toString(result), Toast.LENGTH_SHORT).show();
                    String id_user = mAuth.getUid();
                    Integer id_parking = result;
                    String id_booking = currentReservation.id_booking;// currentReservation.getId(); recupera id_booking da istanza currentReservation
                    CloseReservationParamsAsync paramsAsync = new CloseReservationParamsAsync(id_user, id_parking, id_booking, 1);
                    new CloseReservation().execute(paramsAsync);
                }
                break;
            case PROFILE_REQUEST_CODE:  //GESTIONE DATI PROFILO
                if (resultCode == Activity.RESULT_OK) {

                }
                break;
            case LocationRequest.PRIORITY_HIGH_ACCURACY:  //GESTIONE EVENTI LOCATION
                switch (resultCode) {
                    case Activity.RESULT_OK:    // BUG ANDROID, PROBLEMI CON HIGH ACCURACY
                        // All required changes were successfully made
                        Log.i(TAG, "GEOLOCALIZZAZIONE-5b: GPS attivato dall'utente");
                        setDeviceLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Log.i(TAG, "GEOLOCALIZZAZIONE-5b: Richiesta attivazione GPS respinta ");
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    //Callback dopo richiesta Permessi
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_CAMERA_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MapsActivity.this, ScannerActivity.class);
                    startActivityForResult(intent, SCANNER_REQUEST_CODE);

                } else {
                    Toast.makeText(MapsActivity.this, R.string.permcameradenied, Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
            // VERIFICO PERMESSI ED IMPOSTO POSIZIONE
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);
                    }
                }
                Log.i(TAG,"GEOLOCALIZZAZIONE-2a: esito richiesta --> "+mLocationPermissionGranted);
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    //Converte svg in bitmap (per icone mappa)-----------------------------------------------------
    private BitmapDescriptor bitmapDescriptorFromVector(Context context) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, R.drawable.map_ic_pin);
        assert vectorDrawable != null;
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    //-----POP UP REDIRECT----------------------------------------------------------------------

    // Aggiorna e mostra il pannello di redirect se arriva la notifica
    @SuppressLint("InflateParams")
    private void    showRdrctPopup(@Nullable String id_parking, @Nullable String distance, @Nullable String address) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this,R.style.CustomAlertDialog);
        LayoutInflater layoutInflater = getLayoutInflater();
        View customView;

        if (id_parking!=null){
            String text="";
            customView = layoutInflater.inflate(R.layout.map_redir_popup, null);
            TextView tv_panel_park= customView.findViewById(R.id.tv_park_id);
            tv_panel_park.setText(id_parking);
            TextView tv_panel_dist= customView.findViewById(R.id.tv_dist_id);
            text=distance+"m";
            tv_panel_dist.setText(text);
            TextView tv_panel_address= customView.findViewById(R.id.tv_address_id);
            tv_panel_address.setText(address);
        }
        else customView = layoutInflater.inflate(R.layout.map_no_redir_popup, null);
        builder.setView(customView);
        builder.setCancelable(false);

        if(popup==null){
        popup=builder.create();
        popup.show();
        }

    }

    //METODI DEI BOTTONI DEL POPUP DI REINDIRIZZAMENTO
    public void onEndReservationForFree(View view){
        String id_user=mAuth.getUid();
        Integer id_parking=currentReservation.parking_id;
        String id_booking=currentReservation.id_booking;// currentReservation.getId(); recupera id_booking da istanza currentReservation
        CloseReservationParamsAsync paramsAsync= new CloseReservationParamsAsync(id_user,id_parking,id_booking,0);
        new CloseReservation().execute(paramsAsync);
        popup.cancel();
        popup=null;

    }
    public void onRedirect(View view){
        String id_user=mAuth.getUid();
        Integer id_parking=currentReservation.parking_id;
        String id_booking=currentReservation.id_booking;
        CloseReservationParamsAsync paramsAsync= new CloseReservationParamsAsync(id_user,id_parking,id_booking,2);
        new CloseReservation().execute(paramsAsync);

        if (new_parking_rdrct!=null){
        Integer id_new_parking=Integer.valueOf(new_parking_rdrct);
        OpenReservationParamsAsync paramsAsyncOpen=new OpenReservationParamsAsync(mAuth.getUid(),id_new_parking,1);
        new OpenReservation().execute(paramsAsyncOpen);
        }
        popup.cancel();
        popup=null;

    }

    //--------NAVIGATORE---------------------------------------

    //CALCOLO PERCORSO
    public void calcolaPercorso(LatLng destinazione){
        if(mLocationPermissionGranted) {
            LatLng posizioneUtente = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
            polylinesPoints = new LinkedList<>();
            polylinesPoints.clear();
            polylinesPoints.add(posizioneUtente); //Parte dall'inizio

            new DownloadTask().execute("https://maps.googleapis.com/maps/api/directions/json?origin=" +
                    posizioneUtente.latitude + "," + posizioneUtente.longitude +
                    "&destination=" +
                    destinazione.latitude + "," + destinazione.longitude +
                    "&key=AIzaSyBdcgZSbXkUcPAdylZgfAuK347e7J093WE");

        }else{
            //Impossibile avviare navigatore senza permessi sulla location dell'utente
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.alertNoPermissionGranted))
                    .setMessage(getString(R.string.alertNoNavigator))

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setPositiveButton(android.R.string.yes, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

    }

    //Task asincrono di Download coordinate
    @SuppressLint("StaticFieldLeak")
    private class DownloadTask extends AsyncTask<String, Void, String>{

        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute(){
            progressDialog=new
                    ProgressDialog(MapsActivity.this);
            progressDialog.setTitle(R.string.progressDialogTitle);
            progressDialog.setMessage(getResources().getString(R.string.progressDialogText));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder result = new StringBuilder();
            URL url;
            HttpsURLConnection urlConnection;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpsURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();
                while(data != -1){
                    char cur = (char) data;
                    result.append(cur);
                    data = reader.read();
                }

                Log.i("mylog", result.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                String routes = jsonObject.getString("routes");
                JSONArray arrayRoutes = new JSONArray(routes);
                JSONObject primaRoute = arrayRoutes.getJSONObject(0);

                String legs = primaRoute.getString("legs");
                JSONArray arrayLegs = new JSONArray(legs);
                JSONObject primaLeg = arrayLegs.getJSONObject(0);

                String steps = primaLeg.getString("steps");
                JSONArray arraySteps = new JSONArray(steps);

                for(int i=0; i<arraySteps.length(); i++){
                    JSONObject step = arraySteps.getJSONObject(i);
                    String lat = step.getJSONObject("end_location").getString("lat");
                    String lon = step.getJSONObject("end_location").getString("lng");
                    Log.i("mylog", lat+" "+lon);

                    polylinesPoints.add(new LatLng(Double.parseDouble(lat), Double.parseDouble(lon)));

                }

                progressDialog.hide();
                drawPolylines();


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //Disegno linee su mappa
    public void drawPolylines(){
        if(polyline!=null){
            polyline.remove();
        }
        PolylineOptions plo = new PolylineOptions();
        for(LatLng point : polylinesPoints){
            plo.add(point);
            plo.color(R.color.bluMain);
            plo.width(20);
        }

        polyline = mMap.addPolyline(plo);

    }

    //Rimozione linee su mappa
    private void removeRoute() {
        if(polyline!=null) {
            polyline.remove();
            polylinesPoints.clear();
        }
    }
    //-----------GESTIONE PREFERITI FLAVIA-------------
    //Alert dialog con edit text per inserire label della stazione preferita
    private AlertDialog.Builder createEd() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.dialog_fav_title);
        builder.setMessage(R.string.dialog_fav_message);

        final EditText inputLabel = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        inputLabel.setLayoutParams(lp);
        builder.setView(inputLabel);

        builder.setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String labelNew= inputLabel.getText().toString();
                //controllo che il campo non sia vuoto
                if(labelNew.trim().equals("") || labelNew.isEmpty()){
                    Log.i("update", "Etichetta vuota");
                    Toast.makeText(getApplicationContext(), R.string.label_empty, Toast.LENGTH_LONG).show();
                    checkBoxPreferiti.setChecked(false);
                    return;
                }else {
                    Log.i("dialog", "Label inserita: " + labelNew);
                    newFavParking.setLabel(labelNew);
                    updateFavouriteFile(newFavParking);
                }
            }
        });
        builder.setNegativeButton(R.string.dialog_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                checkBoxPreferiti.setChecked(false);
                dialog.cancel();

            }
        });

        return builder;
    }

    //AGGIUNGE LA NUOVA PREFERENZA AL FILE
    private void updateFavouriteFile(Favourite temp) {

        Map<String,Favourite> preferitiMap= new HashMap(loadFavouriteFromFile());

        if(!preferitiMap.containsKey(temp.getLabel())){//se non è salvato nessun indirizzo per quell'etichetta, la salvo
            preferitiMap.put(temp.getLabel(),temp);
            saveMapOnFile(preferitiMap);
        }else{//altrimenti, mostro un messaggio, e deseleziono la checkbox
            Toast.makeText(this, R.string.favourite_repeated, Toast.LENGTH_LONG).show();
            checkBoxPreferiti.setChecked(false);
        }
    }
    //SCRIVERE MAPPA SU FILE
    private void saveMapOnFile(Map<String, Favourite> preferitiMap) {
        Log.i("flavia", "Sto caricado su file una mappa");
        try {
            FileOutputStream fos= openFileOutput(FILE_PATH, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(preferitiMap);
            oos.close();


        } catch (IOException e) {
            e.printStackTrace();
            //tvMessage.setText("Errore caricamento su file");
        }
    }
    //LEGGERE MAPPA DA FILE
    private Map<String,Favourite> loadFavouriteFromFile() {
        Map<String,Favourite> result= new HashMap<String, Favourite>();
        try {
            FileInputStream fis = openFileInput(FILE_PATH);
            ObjectInputStream objectInput= new ObjectInputStream(fis);
            result = new HashMap<>((HashMap<String, Favourite>) objectInput.readObject());
            objectInput.close();

        } catch (EOFException eof) {
            System.out.println("Reached end of file");
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        return result;
    }
    //cancellare un preferito da file
    private void deleteFavouriteFromFile(Favourite toDelete) {
        //carico preferiti
        Map<String,Favourite> preferiti= new HashMap(loadFavouriteFromFile());
        Log.i("flavia", "Dimensione vecchia mappa" + preferiti.size());
        //se passo solo le coordinate, cancello il preferito con quelle coordinate
            if(toDelete.getLabel().equals("-123")) {
                Log.i("flavia", "Rimozione da stellina");
                for (Favourite f : preferiti.values()) {
                    if ((f.getLat()== station_selected.getLatitude()) && (f.getLon() == station_selected.getLongitude())) {
                        Log.i("flavia", "Ho trovato il preferito su mappa");
                        //lo tolgo dalla mappa
                        preferiti.remove(f.getLabel(),f);
                        Log.i("flavia", "Dimensione nuova mappa" + preferiti.size());
                        //ricarico la mappa sul file
                        saveMapOnFile(preferiti);
                        Log.i("flavia", "File aggiornato, size: "+ loadFavouriteFromFile().size());
                        return;
                    }
                }
            }
            //se passo un preferito completo
            if(preferiti.containsKey(toDelete.getLabel())){
                preferiti.remove(toDelete.getLabel());
                //salvo il file aggiornato
                saveMapOnFile(preferiti);
            }
        }

    //controllo se una stazione è fra i preferiti per selezionare la checkbox
    private boolean isAFavStation(Station station_selected) {
        //recupero mappa preferiti
        Map<String, Favourite> favourite= loadFavouriteFromFile();
        for(Favourite f: favourite.values()){
            if(f.getLat()==station_selected.getLatitude()
                    && f.getLon()== station_selected.getLongitude()){
                return true;
            }
        }
        return false;
    }

    //Funzione per aggiornare lo spinner
    private void updateSpinner() {
        //Lista di etichette
        List<Favourite> fav= new ArrayList(loadFavouriteFromFile().values());
        if(fav.isEmpty()){
            spinnerFiltroPreferiti.setVisibility(View.GONE);
            findViewById(R.id.panel_filter_spinner_text_id).setVisibility(View.VISIBLE);
        }
        else{
            spinnerFiltroPreferiti.setVisibility(View.VISIBLE);
            findViewById(R.id.panel_filter_spinner_text_id).setVisibility(View.GONE);
        }
        ArrayAdapter <Favourite> a= new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fav);
        spinnerFiltroPreferiti.setAdapter(a);
    }

    //----------ALARM PRENOTAZIONE IN CORSO------
    private void setAlarm() {
        Intent intentToFire = new Intent(getApplicationContext(), AlarmBroadcastReceiver.class);
        intentToFire.setAction(ACTION_ALARM);

        alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intentToFire, 0);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        NumberPicker pickerTimer = findViewById(R.id.panel_station_npick_tempo_id);
        int reservMin = Integer.parseInt(timerPickerValues[pickerTimer.getValue()-1]);
        Log.i(TAG, "Minuti da picker: "+reservMin);
        //Alarm 10 min prima della scadenza
        long minInMillis = SystemClock.elapsedRealtime()+(reservMin-10)*60*1000; //Aggiungo al tempo attuale del dispositivo
        //long minInMillis = SystemClock.elapsedRealtime()+30*1000; //TEST con 30 secondi
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, minInMillis, alarmIntent);      //ELAPSED_REALTIME_WAKEUP è generato anche se dispositivo spento
        Log.i(TAG, "Alarm sent "+minInMillis);
        salvaInSharedPreferences(Long.toString(minInMillis));
    }

    private void closeAlarm(){
        alarmManager.cancel(alarmIntent);
    }

    //Salvo il tempo nelle preferenze per poter impostare un Alarm ad ogni avvio del dispositivo
    private void salvaInSharedPreferences(String millis) {
        SharedPreferences spref =  getSharedPreferences("alarms", MODE_PRIVATE);
        SharedPreferences.Editor editor = spref.edit();
        editor.putString("tempo", millis);
        editor.commit();

    }

    public static AlarmManager getAlarmManager() {
        return alarmManager;
    }

    public static PendingIntent getAlarmIntent() {
        return alarmIntent;
    }

    //-----POP UP PROROGA TEMPO PRENOTAZIONE--------------------------------------------------------------

    // Aggiorna e mostra il pannello di redirect se arriva la notifica
    private void showTimeExtensionPopup() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this,R.style.CustomAlertDialog);
        LayoutInflater layoutInflater = getLayoutInflater();
        View customView = layoutInflater.inflate(R.layout.map_time_popup, null);


        builder.setView(customView);
        builder.setCancelable(false);

        if(popup_time==null){
            popup_time=builder.create();
            popup_time.show();
        }

    }

    //METODI DEI BOTTONI DEL POPUP DI ESTENSIONE TEMPO
    public void onEndReservationTime(View view){
        onCloseResButtonClick(view);
        popup_time.cancel();
        popup_time=null;

    }

    public void onExtend(View view){
        //Rimuovo vecchio alarm
        alarmManager.cancel(alarmIntent);

        //Aggiungo 10 minuti al termine
        long pref = Long.valueOf(getSharedPreferences("alarms", Context.MODE_PRIVATE).getString("tempo", ""));
        Log.i(TAG, "Pre-progoga:"+pref);
        long minInMillis = pref + 10*60*1000;
        Log.i(TAG, "PROROGA:"+minInMillis);
        //Avvio nuovo alarm
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, minInMillis, alarmIntent);
        //Salvo le nuove preferenze
        salvaInSharedPreferences(Long.toString(minInMillis));
        popup_time.cancel();
        popup_time=null;

    }

    public void onContinueReservation(View view){
        popup_time.cancel();
        popup_time=null;
    }


}
