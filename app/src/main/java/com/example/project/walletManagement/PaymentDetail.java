package com.example.project.walletManagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.project.R;
import com.example.project.map.MapsActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class PaymentDetail extends AppCompatActivity {

    private static final String TAG = "PaymentDetail" ;
    private TextView tvId;
    private TextView tvAmount;
    private TextView tvStatus;
    private TextView tvCurrentWallet;
    private Button btGoToMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_detail);

        tvId= (TextView)findViewById(R.id.tvId);
        tvAmount= (TextView)findViewById(R.id.tvAmount);
        tvStatus= (TextView)findViewById(R.id.tvStatus);
        tvCurrentWallet= (TextView)findViewById(R.id.tvCurrentWallet);
        btGoToMap= (Button)findViewById(R.id.bt_go_to_map);

        Intent intent= getIntent();

        //estraggo dagli extra dell'intent le info su payment detail
        try {
            JSONObject jsonObject= new JSONObject(intent.getStringExtra("PaymentDetail"));
            /*All'interno del json object:
             * state-> identifica lo stato del pagamento
             * id-> è l'id del pagamento */
            Log.i(TAG, jsonObject.toString());
            //tvId.setText(jsonObject);
            tvId.setText("Codice transazione: "+jsonObject.getJSONObject("response").getString("id"));
            tvStatus.setText("Stato: "+jsonObject.getJSONObject("response").getString("state"));
            tvAmount.setText("Importo totale: €"+intent.getStringExtra("PaymentAmount"));
            tvCurrentWallet.setText("Credito residuo sul tuo wallet: €"+intent.getStringExtra("NewWallet"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void goToMap(View view) {
        Intent intent= new Intent(PaymentDetail.this, MapsActivity.class);
        startActivity(intent);
    }
}
