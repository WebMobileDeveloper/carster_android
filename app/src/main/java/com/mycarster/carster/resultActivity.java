package com.mycarster.carster;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class resultActivity extends AppCompatActivity {

    String vin_number;
    ProgressDialog pd;
    private ListView listView;
    String[] itemTitles = {"driven_wheels" ,"gross_weight","curb_weight","fuel_type","engine_name","vehicle_length","vehicle_height","vehicle_width",
            "vehicle_style","vehicle_type","year","make","model","trim","made_in",  "caution","hookup_alert","oilpan_alert","bumper_alert","bottom_row"};
    String[] itemValues = new String[itemTitles.length];
    int itemCount = 15 , alertCount = 3;
    MySimpleArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        listView = (ListView)findViewById(R.id.listResult);
        pd = new ProgressDialog(this,R.style.MyTheme);
        vin_number = getIntent().getStringExtra("vin_number");
        //vin_number = "1GB3CYC89GF120976";


        TextView result_top_title = (TextView) findViewById(R.id.result_top_title);
        result_top_title.setText("VIN NUMBER:\n " + vin_number);

        TextView table_top_label = (TextView) findViewById(R.id.table_top_title);
        table_top_label.setText("Result For:   " + vin_number);


        adapter = new MySimpleArrayAdapter(resultActivity.this, itemTitles, itemValues);
        listView.setAdapter(adapter);
        getDataFromApi(vin_number);
    }
    @Override
    public void onBackPressed() {
        if(pd.isShowing()){
            pd.dismiss();
        }
        if(pd != null){
            pd = null;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void getDataFromApi(final String vin_number){
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);

        String url = "https://mycarster.com/webservices/vin-search.php";

        pd.show();
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("response", response);
                        pd.hide();
                        JSONObject jsobj;
                        try {
                            jsobj = new JSONObject(response);

                            String status = jsobj.get("status").toString();
                            if(status.equals("200")){
                                try {

                                    JSONObject search_data = jsobj.getJSONObject("search_data");
                                    JSONObject alerts = jsobj.getJSONObject("alerts");
                                    for(int i=0; i<itemCount; i++){
                                        itemValues[i] = search_data.getString(itemTitles[i]);
                                    }
                                    for(int i=itemCount+1; i<itemTitles.length-1; i++){
                                        itemValues[i] = alerts.getString(itemTitles[i]);
                                    }
                                    adapter.notifyDataSetChanged();
                                } catch (JSONException e) {
//                                    Toast.makeText(resultActivity.this, "Wrong Data! \n Try again.", Toast.LENGTH_SHORT).show();
                                    showError("");
                                }
                            }else{
                                String err = jsobj.get("error").toString();
//                                Toast.makeText(resultActivity.this, err +"\n Try again.", Toast.LENGTH_SHORT).show();
                                showError("\n\n"+ err);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
//                            Toast.makeText(resultActivity.this, "Wrong Data! \n Try again.", Toast.LENGTH_SHORT).show();
//                            onBackPressed();
                            showError("");

                        }
                    }
                },
                new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //This code is executed if there is an error.
                        pd.hide();
//                        Toast.makeText(resultActivity.this, "Network Error! \n Try again.", Toast.LENGTH_SHORT).show();
                        showError("");
                    }
                }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("vin", vin_number); //Add the data you'd like to send to the server.
                return MyData;
            }
        };

        MyRequestQueue.add(MyStringRequest);

    }

    private void showError(String errorDetail ){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(resultActivity.this, R.style.AppCompatAlertDialogStyle);
        builder1.setTitle("Data Error!");
        builder1.setMessage("We couldn't details about barcode. \nPlease retry or enter manually." + errorDetail);
        builder1.setCancelable(false);

        builder1.setPositiveButton(
                "ENTER MANUALLY",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://mycarster.com/?page_id=29/"));
                        startActivity(viewIntent);
                    }
                });

        builder1.setNegativeButton(
                "RETRY",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        onBackPressed();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    public class MySimpleArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final String[] values;
        private final String[] titles;

        public MySimpleArrayAdapter(Context context, String[] titles, String[] values) {
            super(context, R.layout.append_row, titles);
            this.context = context;
            this.titles = titles;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = null;
            if(position<itemCount) {
                rowView = inflater.inflate(R.layout.append_row, parent, false);
                TextView title = (TextView) rowView.findViewById(R.id.itemTitle);
                TextView value = (TextView) rowView.findViewById(R.id.itemValue);

                title.setPadding(5, 5, 5, 5);
                value.setPadding(5, 5, 5, 5);

                title.setText(titles[position].replace("_", " "));
                value.setText(values[position]);
                if (position % 2 == 0) {
                    rowView.setBackgroundColor(Color.parseColor("#D7D7D7"));
                } else {
                    rowView.setBackgroundColor(Color.parseColor("#DFF1D9"));
                }
            }else if (position == itemCount){
                rowView = inflater.inflate(R.layout.caution_row, parent, false);
            }else if(position < (itemCount + 1 + alertCount)){
                rowView = inflater.inflate(R.layout.alert_row, parent, false);
                TextView title = (TextView) rowView.findViewById(R.id.alert_title);
                TextView value = (TextView) rowView.findViewById(R.id.alert_content);

                title.setPadding(5, 5, 5, 5);
                value.setPadding(5, 5, 5, 5);

                title.setText(titles[position].replace("_", " "));
                value.setText(values[position]);
            }else{
                rowView = inflater.inflate(R.layout.bottom_row, parent, false);

                final Button inspButton = (Button)rowView.findViewById(R.id.inspectionButton);
                final Button contactButton = (Button)rowView.findViewById(R.id.contactUsButton);
                final Button visitButton = (Button)rowView.findViewById(R.id.visitWebsiteButtonInList);


                inspButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://mycarster.com/?page_id=75/"));
                        startActivity(viewIntent);
                    }
                });
                contactButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://mycarster.com/?page_id=20/"));
                        startActivity(viewIntent);
                    }
                });
                visitButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://mycarster.com/"));
                        startActivity(viewIntent);
                    }
                });
            }
            return rowView;
        }
    }
}

