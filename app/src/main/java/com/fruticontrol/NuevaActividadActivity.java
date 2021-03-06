package com.fruticontrol;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;


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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NuevaActividadActivity extends AppCompatActivity {

    private Calendar calInicio;
    private Calendar calFin;
    private EditText txtFechaInicio;
    private EditText txtFechaFin;
    private EditText txtCostoMatActividad;
    private EditText txtCostoManoActividad;
    private DatePickerDialog datePInicio;
    private DatePickerDialog datePFin;
    private Spinner spinnerTipo;
    private Spinner spinnerSubtipo;
    private Button guardarNuevoProcesoButton;
    private Button seleccionarArbolesButton;
    private Token token;
    private ArrayList<Integer> listaArbolesSeleccionados;
    private int valorJornal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_actividad);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        token = (Token) getApplicationContext();
        txtCostoManoActividad=findViewById(R.id.editTextCostoManoActividad);
        txtCostoMatActividad=findViewById(R.id.editTextCostoMaterialesActividad);
        txtFechaInicio = findViewById(R.id.fechaInicio);
        txtFechaFin = findViewById(R.id.fechaFin);
        spinnerTipo = findViewById(R.id.spinnerTipoProceso);
        spinnerSubtipo = findViewById(R.id.spinnerSubtipo);
        seleccionarArbolesButton = findViewById(R.id.buttonSeleccionarArboles);
        guardarNuevoProcesoButton = findViewById(R.id.buttonGuardarNuevoProceso);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.TipoActividad, R.layout.spinner_item);
        spinnerTipo.setAdapter(spinnerAdapter);

        traerValorJornal();

        seleccionarArbolesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ListaArbolesSeleccionActivity.class);
                startActivityForResult(intent,100);
            }
        });
        guardarNuevoProcesoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    RequestQueue queue = Volley.newRequestQueue(NuevaActividadActivity.this);
                    String tipoActividad=spinnerTipo.getSelectedItem().toString();
                    String url = "https://app.fruticontrol.me/app/";
                    String sutTipo;
                    int costoMateriales=0;
                    if (tipoActividad.equals("Poda")) {
                        url = url + "prunings/";
                        sutTipo = traductorPodas(spinnerSubtipo.getSelectedItem().toString());
                    }
                    else if (tipoActividad.equals("Fumigación")) {
                        url = url + "fumigations/";
                        sutTipo = traductorFumigaciones(spinnerSubtipo.getSelectedItem().toString());
                    }
                    else if (tipoActividad.equals("Fertilización")) {
                        url = url + "fertilizations/";
                        sutTipo = traductorFertilizaciones(spinnerSubtipo.getSelectedItem().toString());
                    }
                    else if (tipoActividad.equals("Recolección")) {
                        url = url + "recollections/";
                        sutTipo = inicialTipo2(spinnerSubtipo.getSelectedItem().toString());
                    }else {
                        url = url + "waterings/";
                        sutTipo = traductorRiegos(spinnerSubtipo.getSelectedItem().toString());
                    }
                    String divide = txtFechaInicio.getText().toString();
                    String separated[] = divide.split(" ");
                    String aux = separated[3];
                    String data[] = aux.split("/");
                    String auxFecha = data[2] + "-" + data[1] + "-" + data[0];

                    String divide2 = txtFechaFin.getText().toString();
                    String separated2[] = divide2.split(" ");
                    String aux2 = separated2[3];
                    String data2[] = aux2.split("/");
                    String auxFecha2 = data2[2] + "-" + data2[1] + "-" + data2[0];

                    String arboles="\"[\""+17+"\",\""+18+"\"]\"";

                    ArrayList<Integer> lista = listaArbolesSeleccionados;
                    JSONArray care_type = new JSONArray();
                    for(int i=0; i < lista.size(); i++) {
                        care_type.put(lista.get(i));   // create array and add items into that
                    }


                    String body = "{\"start_date\":\"" + auxFecha + "\",\"end_date\":\"" + auxFecha2 + "\",\"farm\":\"" + token.getGranjaActual() + "\",\"trees\":" +care_type.toString()+ ",\"type\":\"" + sutTipo + "\",\"work_cost\":\"" + txtCostoManoActividad.getText().toString() + "\",\"tools_cost\":\"" + "0" + "\"}";

                    System.out.println("XXXXXXXXXXXXXXXXXX BODY ES "+body);
                    JSONObject newLastActivity = null;
                    try {
                        newLastActivity = new JSONObject(body);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JsonObjectRequest lastActivityRequest = new JsonObjectRequest(Request.Method.POST,
                            url/*TODO: cambiar a URL real para producción!!!!*/, newLastActivity,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.i("newActivityAPI", response.toString());

                                    if (response.has("error")) {
                                        try {
                                            Toast.makeText(NuevaActividadActivity.this, response.getString("error"), Toast.LENGTH_SHORT).show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Toast.makeText(NuevaActividadActivity.this, "Actividad creada", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("TreeAPI", "Error en la invocación a la API " + error.getCause());
                            Toast.makeText(NuevaActividadActivity.this, "Se presentó un error, por favor intente más tarde", Toast.LENGTH_SHORT).show();
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
                    queue.add(lastActivityRequest);

                    //SE HACE CONSUMO PARA AGREGAR NUEVOS GASTOS MANO DE OBRA
                    //SE TOMA LA FECHA DE INICIO Y SE CAMBIA EL FORMATO
                    String divide4 = txtFechaInicio.getText().toString();
                    String separated4[] = divide4.split(" ");
                    String aux4 = separated4[3];
                    String data4[] = aux4.split("/");
                    String auxFecha4 = data4[2] + "-" + data4[1] + "-" + data4[0];
                    String concepto2=spinnerTipo.getSelectedItem().toString()+": "+spinnerSubtipo.getSelectedItem().toString()+" "+separated4[3];
                    //VALOR
                    String valor=txtCostoManoActividad.getText().toString();
                    //INICIAL DE SUBTIPO DE ACTIVIDAD
                    int selectedItemOfMySpinner2 = spinnerSubtipo.getSelectedItemPosition();
                    String actualPositionOfMySpinner2 = (String) spinnerSubtipo.getItemAtPosition(selectedItemOfMySpinner2);
                    String tipoActividad2 = inicialTipo(actualPositionOfMySpinner2);
                    String body3 = "{\"concept\":\"" + concepto2 + "\",\"date\":\"" + auxFecha4 + "\",\"value\":\"" + valor + "\",\"recommended\":\"" + true + "\",\"type\":\"" + "O"+ "\",\"activity\":\"" + "H" + "\",\"act_type\":\"" + tipoActividad2 + "\"}";
                    Log.i("handOutcomeAPI", "Nuevo gasto: " + body3);
                    JSONObject newOutcome = null;
                    try {
                        newOutcome = new JSONObject(body3);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JsonObjectRequest newOutcomeRequest = new JsonObjectRequest(Request.Method.POST,
                            "https://app.fruticontrol.me/money/outcomes/", newOutcome,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.i("newOutcomeAPI", response.toString());

                                    if (response.has("error")) {
                                        try {
                                            Toast.makeText(NuevaActividadActivity.this, response.getString("error"), Toast.LENGTH_SHORT).show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        finish();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("TreeAPI", "Error en la invocación a la API " + error.getCause());
                            Toast.makeText(NuevaActividadActivity.this, "Se presentó un error, por favor intente más tarde", Toast.LENGTH_SHORT).show();
                        }
                    }) {    //this is the part, that adds the header to the request
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> params = new HashMap<String, String>();
                            //params.put("Content-Type", "application/json");
                            params.put("Authorization", "Token " + token.getToken());
                            System.out.println("XXXXXXXXX EL TOKEN ES " + token.getToken());
                            return params;
                        }
                    };
                    queue.add(newOutcomeRequest);


                    //GASTO MATERIAL
                    String valor5=txtCostoMatActividad.getText().toString();
                    String body4 = "{\"concept\":\"" + concepto2 + "\",\"date\":\"" + auxFecha4 + "\",\"value\":\"" + valor5 + "\",\"recommended\":\"" + true + "\",\"type\":\"" + "M"+ "\",\"activity\":\"" + "H" + "\",\"act_type\":\"" + tipoActividad2 + "\"}";
                    Log.i("materialOutcomeAPI", "Nuevo gasto: " + body4);
                    JSONObject newOutcome2 = null;
                    try {
                        newOutcome2 = new JSONObject(body3);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JsonObjectRequest newOutcomeRequest2 = new JsonObjectRequest(Request.Method.POST,
                            "https://app.fruticontrol.me/money/outcomes/", newOutcome2,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.i("newOutcomeAPI", response.toString());

                                    if (response.has("error")) {
                                        try {
                                            Toast.makeText(NuevaActividadActivity.this, response.getString("error"), Toast.LENGTH_SHORT).show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        finish();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("TreeAPI", "Error en la invocación a la API " + error.getCause());
                            Toast.makeText(NuevaActividadActivity.this, "Se presentó un error, por favor intente más tarde", Toast.LENGTH_SHORT).show();
                        }
                    }) {    //this is the part, that adds the header to the request
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> params = new HashMap<String, String>();
                            //params.put("Content-Type", "application/json");
                            params.put("Authorization", "Token " + token.getToken());
                            System.out.println("XXXXXXXXX EL TOKEN ES " + token.getToken());
                            return params;
                        }
                    };
                    queue.add(newOutcomeRequest2);

                }
            }
        });
        spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String tipo = spinnerTipo.getSelectedItem().toString();

                if (tipo.equals("Seleccione el tipo de proceso...")) {
                    setSpinnerError(spinnerTipo);
                }
                if (tipo.equals("Poda")) {
                    ArrayAdapter<CharSequence> spinnerAdapterPoda = ArrayAdapter.createFromResource(getApplicationContext(), R.array.TipoPoda, R.layout.spinner_item);
                    spinnerSubtipo.setAdapter(spinnerAdapterPoda);
                    txtCostoMatActividad.setText(Integer.toString(15000));
                }
                if (tipo.equals("Fumigación")) {
                    ArrayAdapter<CharSequence> spinnerAdapterFumigacion = ArrayAdapter.createFromResource(getApplicationContext(), R.array.TipoFumigacion, R.layout.spinner_item);
                    spinnerSubtipo.setAdapter(spinnerAdapterFumigacion);
                    txtCostoMatActividad.setText(Integer.toString(60000));
                }
                if (tipo.equals("Fertilización")) {
                    ArrayAdapter<CharSequence> spinnerAdapterFertilizacion = ArrayAdapter.createFromResource(getApplicationContext(), R.array.TipoFertilizacion, R.layout.spinner_item);
                    spinnerSubtipo.setAdapter(spinnerAdapterFertilizacion);
                    txtCostoMatActividad.setText(Integer.toString(60000));
                }
                if (tipo.equals("Riego")) {
                    ArrayAdapter<CharSequence> spinnerAdapterRiego = ArrayAdapter.createFromResource(getApplicationContext(), R.array.TipoRiego, R.layout.spinner_item);
                    spinnerSubtipo.setAdapter(spinnerAdapterRiego);
                    txtCostoMatActividad.setText(Integer.toString(10000));
                }
                if (tipo.equals("Recolección")) {
                    ArrayAdapter<CharSequence> spinnerAdapterRecoleccion = ArrayAdapter.createFromResource(getApplicationContext(), R.array.TipoArbolFrutal, R.layout.spinner_item);
                    spinnerSubtipo.setAdapter(spinnerAdapterRecoleccion);
                    txtCostoMatActividad.setText(Integer.toString(15000));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        txtFechaInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calInicio = Calendar.getInstance();
                int day = calInicio.get(Calendar.DAY_OF_MONTH);
                int month = calInicio.get(Calendar.MONTH);
                int year = calInicio.get(Calendar.YEAR);
                datePInicio = new DatePickerDialog(NuevaActividadActivity.this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int mYear, int mMonth, int mDayOfMonth) {
                        txtFechaInicio.setText("Fecha de inicio: " + String.format("%s/%s/%s", mDayOfMonth, mMonth + 1, mYear));
                        calcularManoObra();
                    }
                }, year, month, day);
                datePInicio.show();
            }
        });



        txtFechaFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calFin = Calendar.getInstance();
                int day = calFin.get(Calendar.DAY_OF_MONTH);
                int month = calFin.get(Calendar.MONTH);
                int year = calFin.get(Calendar.YEAR);
                datePFin = new DatePickerDialog(NuevaActividadActivity.this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int mYear, int mMonth, int mDayOfMonth) {
                        txtFechaFin.setText("Fecha de fin: " + String.format("%s/%s/%s", mDayOfMonth, mMonth + 1, mYear));
                        calcularManoObra();
                    }
                }, year, month, day);
                datePFin.show();
            }
        });
    }


    private String inicialTipo(String opcion) {
        if (opcion.equals("Mango tommy")) {
            return "S1";
        } else if (opcion.equals("Mango farchil")) {
            return "S2";
        } else if (opcion.equals("Naranja")) {
            return "S3";
        } else if (opcion.equals("Aguacate")) {
            return "S4";
        } else if (opcion.equals("Mandarina")) {
            return "S5";
        } else if (opcion.equals("Limon")) {
            return "S6";
        } else {
            return "S7";
        }
    }


    private String inicialTipo2(String opcion) {
        if (opcion.equals("Mango tommy")) {
            return "M";
        } else if (opcion.equals("Mango farchil")) {
            return "F";
        } else if (opcion.equals("Naranja")) {
            return "N";
        } else if (opcion.equals("Aguacate")) {
            return "A";
        } else if (opcion.equals("Mandarina")) {
            return "D";
        } else if (opcion.equals("Limon")) {
            return "L";
        } else {
            return "B";
        }
    }


    private void calcularManoObra(){
        if(!TextUtils.isEmpty(txtFechaInicio.getText().toString())){
            if(!TextUtils.isEmpty(txtFechaFin.getText().toString())){
                String divide = txtFechaInicio.getText().toString();
                String[] separated = divide.split(" ");
                String aux = separated[3];
                String[] data = aux.split("/");
                Calendar cal2 = Calendar.getInstance();
                cal2.set(Integer.parseInt(data[2]), Integer.parseInt(data[1]) - 1, Integer.parseInt(data[0]), 23, 59);
                String divide2 = txtFechaFin.getText().toString();
                String[] separated2 = divide2.split(" ");
                String aux2 = separated2[3];
                String[] data2 = aux2.split("/");
                Calendar cal4 = Calendar.getInstance();
                cal4.set(Integer.parseInt(data2[2]), Integer.parseInt(data2[1]) - 1, Integer.parseInt(data2[0]), 23, 59);
                long msDiff = cal4.getTimeInMillis() - cal2.getTimeInMillis();
                long daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff);
                daysDiff++;
                int total=valorJornal*Math.round(daysDiff);
                txtCostoManoActividad.setText(String.valueOf(total));
            }
        }
    }


    private void traerValorJornal(){
        RequestQueue queue = Volley.newRequestQueue(NuevaActividadActivity.this);
        JsonObjectRequest dayCostRequest = new JsonObjectRequest(Request.Method.GET,
                "https://app.fruticontrol.me/users/owner/", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("dayCost", response.toString());
                        try {
                            valorJornal=Integer.parseInt(response.getString("day_cost"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("dayCostAPI", "Error en la invocación a la API " + error.getCause());
                Toast.makeText(NuevaActividadActivity.this, "Se presentó un error, por favor intente más tarde", Toast.LENGTH_SHORT).show();
            }
        }){    //this is the part, that adds the header to the request
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Token " + token.getToken());
                System.out.println("XXXXXXXXX EL TOKEN ES "+token.getToken());
                return params;
            }
        };
        queue.add(dayCostRequest);
    }


    private void setSpinnerError(Spinner spinner) {
        View selectedView = spinner.getSelectedView();
        if (selectedView instanceof TextView) {
            spinner.requestFocus();
            TextView selectedTextView = (TextView) selectedView;
            selectedTextView.setError("Requerido");
        }
    }


    private boolean validateForm() {
        boolean valid = true;
        if(listaArbolesSeleccionados==null){
            valid=false;
            Toast.makeText(NuevaActividadActivity.this, "Debe seleccionar al menos un árbol al que se le va a aplicar la actividad", Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(txtCostoMatActividad.getText().toString())) {
            txtCostoMatActividad.setError("Requerido");
            valid = false;
        }else{
            txtCostoMatActividad.setError(null);
        }
        if (TextUtils.isEmpty(txtCostoManoActividad.getText().toString())) {
            txtCostoManoActividad.setError("Requerido");
            valid = false;
        }else{
            txtCostoManoActividad.setError(null);
        }
        int selectedItemOfMySpinner = spinnerTipo.getSelectedItemPosition();
        String actualPositionOfMySpinner = (String) spinnerTipo.getItemAtPosition(selectedItemOfMySpinner);
        if (actualPositionOfMySpinner.equals("Seleccione el tipo de actividad...")) {
            setSpinnerError(spinnerTipo);
            valid = false;
        } else {
            int selectedItemOfMySubSpinner = spinnerSubtipo.getSelectedItemPosition();
            String actualPositionOfMySpinnerSub = (String) spinnerSubtipo.getItemAtPosition(selectedItemOfMySubSpinner);
            if (actualPositionOfMySpinnerSub.equals("Seleccione el tipo de poda...")) {
                setSpinnerError(spinnerSubtipo);
                valid = false;
            }
            if (actualPositionOfMySpinnerSub.equals("Seleccione el tipo de fumigación...")) {
                setSpinnerError(spinnerSubtipo);
                valid = false;
            }
            if (actualPositionOfMySpinnerSub.equals("Seleccione el tipo de fertilización...")) {
                setSpinnerError(spinnerSubtipo);
                valid = false;
            }
            if (actualPositionOfMySpinnerSub.equals("Seleccione el tipo de riego...")) {
                setSpinnerError(spinnerSubtipo);
                valid = false;
            }
        }
        if (TextUtils.isEmpty(txtFechaInicio.getText().toString())) {
            txtFechaInicio.setError("Requerido");
            valid = false;
        } else {
            String divide = txtFechaInicio.getText().toString();
            String[] separated = divide.split(" ");
            String aux = separated[3];
            String[] data = aux.split("/");
            Calendar cal = Calendar.getInstance();
            cal.getTime();
            Calendar cal2 = Calendar.getInstance();
            cal2.set(Integer.parseInt(data[2]), Integer.parseInt(data[1]) - 1, Integer.parseInt(data[0]), 23, 59);
            if (cal.compareTo(cal2) > 0) {
                txtFechaInicio.setError("La fecha debe ser la actual o posterior a la actual");
                valid = false;
            } else {
                txtFechaInicio.setError(null);
            }
        }
        if (TextUtils.isEmpty(txtFechaFin.getText().toString())) {
            txtFechaFin.setError("Requerido");
            valid = false;
        } else {
            String divide = txtFechaInicio.getText().toString();
            String separated[] = divide.split(" ");
            String aux = separated[3];
            String data[] = aux.split("/");

            String divide2 = txtFechaFin.getText().toString();
            String separated2[] = divide2.split(" ");
            String aux2 = separated2[3];
            String data2[] = aux2.split("/");

            Calendar cal = Calendar.getInstance();
            cal.getTime();
            Calendar cal2 = Calendar.getInstance();
            cal2.set(Integer.parseInt(data[2]), Integer.parseInt(data[1]) - 1, Integer.parseInt(data[0]), 23, 59);

            Calendar cal3 = Calendar.getInstance();
            cal3.set(Integer.parseInt(data2[2]), Integer.parseInt(data2[1]) - 1, Integer.parseInt(data2[0]), 23, 59);
            cal2.set(Integer.parseInt(data[2]), Integer.parseInt(data[1]) - 1, Integer.parseInt(data[0]), 00, 00);
            if (cal3.compareTo(cal2) < 0) {
                txtFechaFin.setError("La fecha debe ser igual o superior a la actual");
                valid = false;
            } else {
                txtFechaFin.setError(null);
            }
        }

        return valid;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("XXXXXXXXXXXX ENTRA A ON ACTIVITY RESULT");
        if (requestCode == 100 && resultCode == RESULT_OK) {
            listaArbolesSeleccionados = data.getIntegerArrayListExtra("arbolesActividad");
            System.out.println("LOS IDS DE LOS ARBOLES SELECCIONADOS FUERON ");
            if(listaArbolesSeleccionados!=null){
                for(int i=0;i<listaArbolesSeleccionados.size();i++){
                    System.out.println("A "+listaArbolesSeleccionados.get(i).toString());
                }
            }
        }
    }


    private String traductorRiegos(String tipo) {
        if (tipo.equals("Sistema de riego")) {
            return "S";
        }
        if (tipo.equals("Manual")) {
            return "M";
        }
        else {
            return "N";
        }
    }


    private String traductorFertilizaciones(String tipo) {
        if (tipo.equals("Crecimiento")) {
            return "C";
        }
        if (tipo.equals("Produccion")) {
            return "P";
        } else {
            return "M";
        }
    }


    private String traductorFumigaciones(String tipo) {
        if (tipo.equals("Insectos")) {
            return "I";
        }
        if (tipo.equals("Hongos")) {
            return "F";
        }
        if (tipo.equals("Hierba")) {
            return "H";
        }
        if (tipo.equals("Ácaro")) {
            return "A";
        } else {
            return "P";
        }
    }


    private String traductorPodas(String tipo) {

        if (tipo.equals("Sanitaria")) {
            return "S";
        }
        if (tipo.equals("Formación")) {
            return "F";
        }
        if (tipo.equals("Mantenimiento")) {
            return "M";
        } else {
            return "L";
        }
    }


}

