package com.fruticontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListaGranjasActivity extends AppCompatActivity {

    Button nuevaGranjaButton;

    ArrayList<ResumenGranjaDataModel> dataModels;
    ListView listView;
    private static ResumenGranjasAdapter adapter;
    private Token token;
    private ArrayList<String> nombresGranjas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_granjas);
        token=(Token)getApplicationContext();
        listView=(ListView)findViewById(R.id.listaGranjasList);
        dataModels= new ArrayList<>();
        nombresGranjas=new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(ListaGranjasActivity.this);
        JsonArrayRequest newFarmRequest = new JsonArrayRequest(Request.Method.GET,
                "http://10.0.2.2:8000/app/farms/"/*TODO: cambiar a URL real para producción!!!!*/, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i("farmsList", response.toString());
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject farmObject = response.getJSONObject(i);
                                String name = farmObject.getString("name");
                                nombresGranjas.add(name);
                            }
                            if(nombresGranjas.isEmpty()){
                                System.out.println("XXXXXXXXXXXXXXX ARREGLO VACIO");
                            }else {
                                //SE LLENA LA LISTA
                                System.out.println("XXXXXXXXXXXXXXXXX SIZE DE GRANJAS ES "+nombresGranjas.size());
                                for(int i=0;i<nombresGranjas.size();i++){
                                    dataModels.add(new ResumenGranjaDataModel(nombresGranjas.get(i).toString(),"Android 5.0","21"));
                                }
                                adapter= new ResumenGranjasAdapter(dataModels,getApplicationContext());
                                listView.setAdapter(adapter);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("usersAPI", "Error en la invocación a la API " + error.getCause());
                Toast.makeText(ListaGranjasActivity.this, "Se presentó un error, por favor intente más tarde", Toast.LENGTH_SHORT).show();
            }
        }){    //this is the part, that adds the header to the request
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Token "+token.getToken());
                System.out.println("XXXXXXXXX EL TOKEN ES "+token.getToken());
                return params;
            }
        };
        queue.add(newFarmRequest);


        nuevaGranjaButton = findViewById(R.id.buttonNuevaGranja);
        nuevaGranjaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), NuevaGranjaActivity.class);
                startActivity(intent);

            }
        });

    }

}
