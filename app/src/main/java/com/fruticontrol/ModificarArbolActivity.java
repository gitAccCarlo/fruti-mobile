package com.fruticontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ModificarArbolActivity extends AppCompatActivity {

    private String idArbol;
    private String tipo;
    private String fecha;
    private String localizacion;
    private String lat;
    private String lon;
    private TextView txtFechaSiembra;
    private Spinner spinnerTipoArbol;
    private Calendar cal;
    private DatePickerDialog dpd;
    private Button guardarCambiosButton;
    private Button modificarUbicacionButton;
    private Button eliminarArbolButton;
    private Token token;
    private String newLat;
    private String newLon;
    private ArrayList<String> localizacionesArboles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_arbol);
        token = (Token) getApplicationContext();
        txtFechaSiembra = findViewById(R.id.textFechaSiembra2);
        spinnerTipoArbol = findViewById(R.id.spinnerModificarTipoArbol);
        guardarCambiosButton = findViewById(R.id.buttonGuardarCambiosArbol);
        modificarUbicacionButton = findViewById(R.id.buttonModificarUbicacion);
        eliminarArbolButton = findViewById(R.id.buttonEliminarArbol);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.TipoArbolFrutal, R.layout.spinner_item);
        spinnerTipoArbol.setAdapter(spinnerAdapter);

        Intent intent = getIntent();
        idArbol = intent.getStringExtra("idArbolActual");
        tipo = intent.getStringExtra("tipo");
        fecha = intent.getStringExtra("fecha");
        localizacion = intent.getStringExtra("localizacion");
        localizacionesArboles=intent.getStringArrayListExtra("todosArboles");

        String divide = localizacion;
        String[] separated = divide.split("\\(");
        String[] anotherAux = separated[1].split(" ");
        lat = anotherAux[0];
        System.out.println("XXXXXXX ESTO ES MODIFICAR ARBOL LAT" + anotherAux[0]);
        String[] lonAux = anotherAux[1].split("\\)");
        lon = lonAux[0];
        System.out.println("XXXXXXX ESTO ES MODIFICAR ARBOL LON" + lonAux[0]);
        newLat = lat;
        newLon = lon;
        System.out.println("XXXXXXXXXXXXxx new lat es " + newLat);
        System.out.println("XXXXXXXXXXXXxx new lon es " + newLon);

        int setAux = valorPosicion(tipo);
        spinnerTipoArbol.setSelection(setAux);
        String divide2 = fecha;
        String[] separated2 = divide2.split("-");
        txtFechaSiembra.setText("Fecha de siembra: " + separated2[2] + "/" + separated2[1] + "/" + separated2[0]);

        eliminarArbolButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder=new AlertDialog.Builder(view.getContext());
                builder.setCancelable(true);
                builder.setTitle(Html.fromHtml("<font color='#964F2D'>Eliminar árbol</font>"));
                builder.setMessage(Html.fromHtml("<font color='#910C00'>¿Está seguro de que desea eliminar el árbol?</font>"));
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.setPositiveButton("Si, eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        RequestQueue queue = Volley.newRequestQueue(ModificarArbolActivity.this);
                        String auxUrl = "https://app.fruticontrol.me/app/trees/" + idArbol + "/";
                        StringRequest deleteTreeRequest = new StringRequest(Request.Method.DELETE,
                                auxUrl,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Toast.makeText(ModificarArbolActivity.this, "Árbol eliminado", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("TreeAPI", "Error en la invocación a la API " + Arrays.toString(error.getStackTrace()));
                                Toast.makeText(ModificarArbolActivity.this, "Se presentó un error, por favor intente más tarde", Toast.LENGTH_SHORT).show();
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
                        queue.add(deleteTreeRequest);
                    }
                });
                builder.show();
            }
        });
        guardarCambiosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateForm()) {
                    RequestQueue queue = Volley.newRequestQueue(ModificarArbolActivity.this);
                    //SE TOMA EL TIPO DE ARBOL Y SE AVERIGUA LA INICIAL
                    int selectedItemOfMySpinner = spinnerTipoArbol.getSelectedItemPosition();
                    String actualPositionOfMySpinner = (String) spinnerTipoArbol.getItemAtPosition(selectedItemOfMySpinner);
                    String inicial = inicialTipo(actualPositionOfMySpinner);
                    //SE TOMA LA FECHA DE SIEMBRA Y SE CAMBIA EL FORMATO
                    String divide = txtFechaSiembra.getText().toString();
                    String[] separated = divide.split(" ");
                    String aux = separated[3];
                    String[] data = aux.split("/");
                    String auxFecha = data[2] + "-" + data[1] + "-" + data[0];
                    //SE TOMAN LAS COORDENADAS X Y PARA LA POSICION
                    Intent intent = getIntent();
                    String auxUbicacion = "POINT (" + newLat + " " + newLon + ")";
                    //SE CREA EL BODY CON LOS DATOS ANTERIORES
                    String body = "{\"specie\":\"" + inicial + "\",\"seed_date\":\"" + auxFecha + "\",\"location\":\"" + auxUbicacion + "\",\"farm\":\"" + token.getGranjaActual() + "\"}";
                    Log.i("modificateTreeAPI", "Arbol modificado: " + body);
                    JSONObject newTree = null;
                    try {
                        newTree = new JSONObject(body);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String auxUrl = "https://app.fruticontrol.me/app/trees/" + idArbol + "/";
                    JsonObjectRequest newTreeRequest = new JsonObjectRequest(Request.Method.PUT,
                            auxUrl, newTree,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.i("modificateTreeAPI", response.toString());

                                    if (response.has("error")) {
                                        try {
                                            Toast.makeText(ModificarArbolActivity.this, response.getString("error"), Toast.LENGTH_SHORT).show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Toast.makeText(ModificarArbolActivity.this, "Cambios guardados", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("TreeAPI", "Error en la invocación a la API " + error.getCause());
                            Toast.makeText(ModificarArbolActivity.this, "Se presentó un error, por favor intente más tarde", Toast.LENGTH_SHORT).show();
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
                    queue.add(newTreeRequest);
                }
            }
        });
        modificarUbicacionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), MapaModificarArbolActivity.class);
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lon);
                intent.putExtra("todosArboles",(ArrayList<String>) localizacionesArboles);
                startActivityForResult(intent,100);
                //startActivity(intent);
            }
        });
        txtFechaSiembra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cal = Calendar.getInstance();
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int month = cal.get(Calendar.MONTH);
                int year = cal.get(Calendar.YEAR);
                dpd = new DatePickerDialog(ModificarArbolActivity.this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int mYear, int mMonth, int mDayOfMonth) {
                        txtFechaSiembra.setText("Fecha de siembra: " + String.format("%s/%s/%s", mDayOfMonth, mMonth + 1, mYear));
                    }
                }, year, month, day);
                dpd.show();
            }
        });
    }

    private int valorPosicion(String tipo) {
        switch (tipo) {
            case "Mango tommy":
                return 1;
            case "Mango farchil":
                return 2;
            case "Naranja":
                return 3;
            case "Mandarina":
                return 4;
            case "Limon":
                return 5;
            case "Aguacate":
                return 6;
            default:
                return 7;
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        int selectedItemOfMySpinner = spinnerTipoArbol.getSelectedItemPosition();
        String actualPositionOfMySpinner = (String) spinnerTipoArbol.getItemAtPosition(selectedItemOfMySpinner);

        if (actualPositionOfMySpinner.equals("Seleccione el tipo de árbol...")) {
            setSpinnerError(spinnerTipoArbol);
            valid = false;
        }
//VALIDACION FECHA DE SIEMBRA
        if (TextUtils.isEmpty(txtFechaSiembra.getText().toString())) {
            txtFechaSiembra.setError("Requerido");
            valid = false;
        } else {
            String divide = txtFechaSiembra.getText().toString();
            String[] separated = divide.split(" ");
            String aux = separated[3];
            String[] data = aux.split("/");
            Calendar cal = Calendar.getInstance();
            cal.getTime();
            Calendar cal2 = Calendar.getInstance();
            System.out.println("La seleccionada es " + data[2] + data[1] + data[0]);
            cal2.set(Integer.parseInt(data[2]), Integer.parseInt(data[1]) - 1, Integer.parseInt(data[0]), 00, 00);
            if (cal.compareTo(cal2) > 0) {
                txtFechaSiembra.setError(null);
            } else {
                txtFechaSiembra.setError("La fecha debe ser la actual o anterior a la actual");
                valid = false;
            }
        }
        return valid;
    }

    private void setSpinnerError(Spinner spinner) {
        View selectedView = spinner.getSelectedView();
        if (selectedView instanceof TextView) {
            spinner.requestFocus();
            TextView selectedTextView = (TextView) selectedView;
            selectedTextView.setError("Requerido"); // any name of the error will do
        }
    }

    private String inicialTipo(String opcion) {
        switch (opcion) {
            case "Mango tommy":
                return "M";
            case "Mango farchil":
                return "F";
            case "Naranja":
                return "N";
            case "Mandarina":
                return "D";
            case "Limon":
                return "L";
            case "Aguacate":
                return "A";
            default:
                return "B";
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            newLat = data.getStringExtra("latFinal");
            newLon = data.getStringExtra("longFinal");
        }
    }
}


