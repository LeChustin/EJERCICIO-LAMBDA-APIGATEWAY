package com.chst.practica_aws_gateway;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText etCorredores, etDistancia;
    private TextView tvResultado;
    private RequestQueue requestQueue;
    private final String API_URL = "https://5m5ov8ryx0.execute-api.us-east-2.amazonaws.com/Produccion/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etCorredores = findViewById(R.id.etCorredores);
        etDistancia = findViewById(R.id.etDistancia);
        tvResultado = findViewById(R.id.tvResultado);
        Button btnIniciarCarrera = findViewById(R.id.btnIniciarCarrera);

        requestQueue = Volley.newRequestQueue(this);

        btnIniciarCarrera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarCarrera();
            }
        });
    }

    private void iniciarCarrera() {
        int numCorredores;
        double distancia;

        try {
            numCorredores = Integer.parseInt(etCorredores.getText().toString());
            distancia = Double.parseDouble(etDistancia.getText().toString());

            if (numCorredores < 2 || numCorredores > 5) {
                tvResultado.setText("Ingrese entre 2 y 5 corredores.");
                return;
            }

            obtenerDatosCarrera(numCorredores, distancia);

        } catch (NumberFormatException e) {
            tvResultado.setText("Ingrese valores válidos.");
        }
    }

    private void obtenerDatosCarrera(int numCorredores, double distancia) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, API_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        procesarCarrera(response, distancia);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        tvResultado.setText("Error al obtener datos del servidor.");
                        Log.e("API Error", error.toString());
                    }
                });

        requestQueue.add(request);
    }

    private void procesarCarrera(JSONObject response, double distancia) {
        try {
            JSONArray jsonArray = new JSONArray(response.getString("body"));
            JSONObject carrera = jsonArray.getJSONObject(0);
            JSONArray corredores = carrera.getJSONArray("corredores");

            StringBuilder resultado = new StringBuilder();
            double menorTiempo = Double.MAX_VALUE;
            String ganador = "";

            for (int i = 0; i < corredores.length(); i++) {
                JSONObject corredor = corredores.getJSONObject(i);
                String nombre = corredor.getString("nombre");
                double velocidad = corredor.getDouble("velocidad");

                if (i >= Integer.parseInt(etCorredores.getText().toString())) break; // Limitar a la cantidad ingresada

                double tiempo = distancia / velocidad;

                resultado.append(nombre)
                        .append(" - Velocidad: ").append(velocidad).append(" km/h")
                        .append(" - Tiempo: ").append(String.format("%.2f", tiempo)).append(" horas\n");

                if (tiempo < menorTiempo) {
                    menorTiempo = tiempo;
                    ganador = nombre;
                }
            }

            resultado.append("\n🏆 El ganador es: ").append(ganador);
            tvResultado.setText(resultado.toString());

        } catch (JSONException e) {
            tvResultado.setText("Error al procesar datos.");
            e.printStackTrace();
        }
    }
}
