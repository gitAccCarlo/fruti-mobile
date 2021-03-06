package com.fruticontrol;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NuevaGranjaActivity extends AppCompatActivity {

    static final int MY_PERMISSIONS_REQUEST_LOCATION = 100;
    static final int REQUEST_CHECK_SETTINGS = 200;
    private Button crearGranjaButton;
    private EditText nombraGranjaET;
    private Token token;
    private Button crearPoligono;
    private ArrayList<String> puntosPoligono;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_granja);
        token = (Token) getApplicationContext();
        requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, "Para ver ubicación", MY_PERMISSIONS_REQUEST_LOCATION);
        System.out.println("XXXXXXX EL TOKEN QUE SE RECIBE ES " + token.getToken());
        crearGranjaButton = findViewById(R.id.buttonCrearGranja);
        puntosPoligono=new ArrayList<>();
        crearPoligono=findViewById(R.id.buttonCrearPoligono);
        nombraGranjaET = findViewById(R.id.editTextNombreGranja);
        System.out.println("El token que se recibe es " + token.getToken());
        crearPoligono.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(), MapaPoligonoGranjaActivity.class), 100);
            }
        });
        crearGranjaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (validateForm()) {
                    Toast.makeText(NuevaGranjaActivity.this, "Espere un momento por favor", Toast.LENGTH_SHORT).show();
                    RequestQueue queue = Volley.newRequestQueue(NuevaGranjaActivity.this);
                    token.setPuntosPoligonoGranja(puntosPoligono);
                    String auxPuntosPoligono="POLYGON((";
                    for(int i=0;i<puntosPoligono.size();i=i+2){
                        if(i==0){
                            if(i==puntosPoligono.size()-2){
                                auxPuntosPoligono=auxPuntosPoligono+puntosPoligono.get(i)+" "+puntosPoligono.get(i+1)+",";
                                auxPuntosPoligono=auxPuntosPoligono+puntosPoligono.get(0)+" "+puntosPoligono.get(1);
                            }else{
                                auxPuntosPoligono=auxPuntosPoligono+puntosPoligono.get(i)+" "+puntosPoligono.get(i+1)+",";
                            }
                        }else{
                            if(i==puntosPoligono.size()-2){
                                auxPuntosPoligono=auxPuntosPoligono+" "+puntosPoligono.get(i)+" "+puntosPoligono.get(i+1)+",";
                                auxPuntosPoligono=auxPuntosPoligono+" "+puntosPoligono.get(0)+" "+puntosPoligono.get(1);
                            }else{
                                auxPuntosPoligono=auxPuntosPoligono+" "+puntosPoligono.get(i)+" "+puntosPoligono.get(i+1)+",";
                            }
                        }

                    }
                    auxPuntosPoligono=auxPuntosPoligono+"))";
                    String body = "{\"name\":\"" + nombraGranjaET.getText().toString() + "\",\"polygon\":\"" + auxPuntosPoligono + "\"}";
                    Log.i("newFarmAPI", "Nueva Granja: " + body);
                    JSONObject newFarm = null;
                    try {
                        newFarm = new JSONObject(body);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String aux;
                    JsonObjectRequest newFarmRequest = new JsonObjectRequest(Request.Method.POST,
                            "https://app.fruticontrol.me/app/farms/", newFarm,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.i("newFarmAPI", response.toString());

                                    if (response.has("error")) {
                                        try {
                                            Toast.makeText(NuevaGranjaActivity.this, response.getString("error"), Toast.LENGTH_SHORT).show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        try {
                                            token.setGranjaActual(response.getString("id"));

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        Toast.makeText(NuevaGranjaActivity.this, "Granja creada", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(view.getContext(), AccionesActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("usersAPI", "Error en la invocación a la API " + error.getCause());
                            Toast.makeText(NuevaGranjaActivity.this, "Se presentó un error, por favor intente más tarde", Toast.LENGTH_SHORT).show();
                        }
                    }) {    //this is the part, that adds the header to the request
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("Content-Type", "application/json");
                            params.put("Authorization", "Token " + token.getToken());
                            System.out.println("XXXXXXXXX EL TOKEN ES " + token.getToken());
                            return params;
                        }
                    };
                    queue.add(newFarmRequest);
                }
            }
        });
        /*TODO: para enviar la petición de creación el método es un POST, tienen que agregar un Header a la petición formado de la siguiente manera
            headers["Authorization"] = `Token ${token}`
            url = http://10.0.2.2:8000/farms/
            body = {name,polygon} para polygon seguir el formato de https://docs.djangoproject.com/en/3.0/ref/contrib/gis/geos/#django.contrib.gis.geos.Polygon
         */
    }

    private void requestPermission(Activity context, String permiso, String justificacion, int idCode) {
        if (ContextCompat.checkSelfPermission(context, permiso) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(context, justificacion, Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, idCode);
        }
    }

    private boolean validateForm() {
        boolean valid = true;
        if(puntosPoligono.isEmpty()){
            valid=false;
            Toast.makeText(NuevaGranjaActivity.this, "Debe ubicar la granja en el mapa para poder continuar", Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(nombraGranjaET.getText().toString())) {
            nombraGranjaET.setError("Requerido");
            valid = false;
        } else {
            nombraGranjaET.setError(null);
        }
        return valid;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                if (extras.containsKey("puntoPoligono")) {
                    System.out.println("VLAOR DERICIBO EN EL BUNDLE ");
                }else{
                    System.out.println("EL BUNDLE NO TIENE NADA");
                }
            }else{
                System.out.println("EXTRAS ES NULLL");
            }
            puntosPoligono=extras.getStringArrayList("puntoPoligono");
        }
    }
}

//Icons made by <a href="https://www.flaticon.com/authors/good-ware" title="Good Ware">Good Ware</a> from <a href="https://www.flaticon.com/" title="Flaticon"> www.flaticon.com</a>
