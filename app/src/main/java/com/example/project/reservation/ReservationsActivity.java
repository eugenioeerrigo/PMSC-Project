package com.example.project.reservation;


import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.media.session.PlaybackState;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ReservationsActivity extends AppCompatActivity {

    private static final String TAG= "ReservationActivity";
    private static final String URL_DB= "http://smartparkingpolito.altervista.org/GetReservationsByUserId.php";
    private List<Reservation> storico= new ArrayList<Reservation>();
    private ListView list_view;
    FirebaseAuth mAuth;
    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations);
        initUI();

        mAuth= FirebaseAuth.getInstance();
        //user= mAuth.getCurrentUser();

        list_view = (ListView) findViewById(android.R.id.list);

        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int pos, long l) {
                //onClick sugli elementi della ListView
                Intent intent= new Intent(getApplicationContext(), DetailedReservationActivity.class);
                Bundle reservationBundle =new Bundle();
                Reservation selected= (Reservation)adapter.getItemAtPosition(pos);
                //Log.i(TAG, "HAI CLICCATO SULLA PRENOTAZIONE DI: "+selected.getUser_id());
                reservationBundle.putParcelable("reservation", selected);
                intent.putExtra("reservation", reservationBundle);
                startActivity(intent);
            }
        });

        updateReservationList();
        Toast.makeText(this,"Seleziona una map_icona_panel_prenotazione per visualizzare maggiori dettagli", Toast.LENGTH_LONG);

    }

    private void initUI() {
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.reservation_description);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        getSupportActionBar().setHomeButtonEnabled(true);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) // Press Back Icon
        {
            Log.i(TAG, "Back, redirect a Maps");
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateReservationList() {
        DownloadReservationTask drt= new DownloadReservationTask();
        drt.execute(URL_DB);
    }

    //Async Task
    private class DownloadReservationTask extends AsyncTask<String, Void, List<Reservation>> {

        @Override
        protected List<Reservation> doInBackground(String... urls) {//ATTIVITA' CRITICA
            return loadReservationsFromDB(urls[0]);
        }

        @Override
        protected void onPostExecute(List<Reservation> storico) {//QUANDO FINSCO ATTIVITA' CRITICA

            CustomAdapter a= new CustomAdapter(ReservationsActivity.this, R.layout.reservation_simple_row_layout, storico);

            list_view.setAdapter(a);
        }
    }


    private List<Reservation> loadReservationsFromDB(String urlstring) {

        storico.clear();
        try {


            URL url = new URL(urlstring);
            //connessione
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            //concateno al POST l'user_id per filtrare la tabella
            String userId= mAuth.getCurrentUser().getUid();
            Log.i("current user", "Current user: "+userId);
            String params = "user_id=" + URLEncoder.encode(userId, "UTF-8");

            DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
            dos.writeBytes(params);
            dos.flush();
            dos.close();

            urlConnection.connect();
            InputStream is = urlConnection.getInputStream();

            //4c)leggo i contenuti della pagina
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();

            String result = sb.toString();//il risultato è una stringa Json

            Log.i("tag", "Risultato query:"+result);

            JSONArray jArray = new JSONArray(result); //decodifico la stringa Json
            //per decodificare uso la classe jsonArray-> collezione di oggetti JsonObject
            //su cui faccio un ciclo, ad ogni oggetto chiedo di darmi l'elemento corrispondente alle colonne della tabella
            Log.i("tag", "Lunghezza json Array "+jArray.length());
            String outputString = "";
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject json_data = jArray.getJSONObject(i);
               // Log.i(TAG, "\n ID_BOOKING =  " + json_data.getString(Reservation.ReservationMetaData.ID_USER));

                //4d) ottenuto l'array di classe Json posso creare un oggetto Reservation e inserirlo nell'array
                Reservation temp= new Reservation();
                temp.setId_booking(json_data.getString(Reservation.ReservationMetaData.ID));
                temp.setTime_start(json_data.getString(Reservation.ReservationMetaData.TIME_START));
                temp.setTime_end(json_data.getString(Reservation.ReservationMetaData.TIME_END));
                temp.setAddress_end(json_data.getString(Reservation.ReservationMetaData.END_ADDRESS));
                temp.setBonus(json_data.getInt(Reservation.ReservationMetaData.BONUS));
                temp.setParking_id(json_data.getInt(Reservation.ReservationMetaData.ID_PARKING));
                temp.setAmount(json_data.getString(Reservation.ReservationMetaData.AMOUNT));

                storico.add(temp);
                Log.i(TAG, "Utente inserito nell'array: "+ temp.toString());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error*** " + e.toString());
        }
        return storico;

    }


}