package com.example.earthquakeapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.earthquakeapp.MapsActivity;
import com.example.earthquakeapp.Model.EarthQuake;
import com.example.earthquakeapp.R;
import com.example.earthquakeapp.Util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EarthQuakesListActivity extends AppCompatActivity {
    private ArrayList<String> arrayList;
    private ListView listView;
    private RequestQueue requestQueue;
    private ArrayAdapter arrayAdapter;
    private List<EarthQuake> earthQuakeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earth_quakes_list);

        earthQuakeList = new ArrayList<>();
        listView = findViewById(R.id.id_listView);

        requestQueue = Volley.newRequestQueue(this);

        arrayList =  new ArrayList<>();

        getAllEartQuakes(Constants.URL_API);
    }

    private void getAllEartQuakes(String url) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    JSONArray jsonArrayFeature = response.getJSONArray("features");
                    for(int i=0;i<jsonArrayFeature.length();i++){
                        JSONObject jsonObjectProperties = jsonArrayFeature.getJSONObject(i).getJSONObject("properties");
                        String jsonObjectPLACE = jsonObjectProperties.getString("place");
                        arrayList.add(jsonObjectPLACE.toString());
                    }

                    arrayAdapter = new ArrayAdapter<>(EarthQuakesListActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1,arrayList);
                    listView.setAdapter(arrayAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                             Toast.makeText(getApplicationContext(), "You Clicked postition: "+i,Toast.LENGTH_SHORT).show();
                        }
                    });
                    arrayAdapter.notifyDataSetChanged();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }



            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}