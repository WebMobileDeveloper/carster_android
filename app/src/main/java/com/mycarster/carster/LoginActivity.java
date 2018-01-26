package com.mycarster.carster;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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


public class LoginActivity extends AppCompatActivity {

    EditText nameField;
    EditText passField;
    ProgressDialog pd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        nameField = (EditText) findViewById(R.id.userName);
        passField = (EditText) findViewById(R.id.password);

//        nameField.setText("carster");
//        passField.setText("CarsterLaunch2018!");
        pd = new ProgressDialog(this,R.style.MyTheme);


        //================ restore login info
        SharedPreferences sp1=this.getSharedPreferences("carster_login", MODE_PRIVATE);

        String unm=sp1.getString("Unm", null);
        String pass = sp1.getString("Psw", null);

        if (unm != null){
            nameField.setText(unm);
            passField.setText(pass);
            login(unm, pass);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pd.show();
        }


    }


    public void LoginClicked(View v){
        String name = nameField.getText().toString();
        String pass = passField.getText().toString();
        if(name.isEmpty()){
            Toast.makeText(LoginActivity.this, "Please input user name.", Toast.LENGTH_SHORT).show();
            nameField.requestFocus();
            return;
        }
        if(pass.isEmpty()){
            Toast.makeText(LoginActivity.this, "Please input password.", Toast.LENGTH_SHORT).show();
            passField.requestFocus();
            return;
        }

        login(name, pass);
        pd.setCancelable(false);
        pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        pd.show();
    }
    public void forgotPass(View v){
        Intent viewIntent =  new Intent("android.intent.action.VIEW", Uri.parse("https://mycarster.com/?page_id=27/"));
        startActivity(viewIntent);
    }

    public void visitWebsite(View v){
        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://mycarster.com"));
        startActivity(viewIntent);
    }
    public void gotoRegister(View v){
        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://mycarster.com/?page_id=18"));
        startActivity(viewIntent);
    }

    private void login(final String user_name, final String password){
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);

        String url = "https://mycarster.com/webservices/login.php";

        StringRequest MyStringRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pd.hide();
                        JSONObject jsobj;
                        try {
                            jsobj = new JSONObject(response);
                            String status = jsobj.get("Status").toString();
                            Log.d("status", status);
                            if(status.equals("200")){
                                Log.d("success status", status);
                                Toast.makeText(LoginActivity.this, "Login success!", Toast.LENGTH_SHORT).show();

                            // ===============Saving login info========================
                                SharedPreferences sp=getSharedPreferences("carster_login", MODE_PRIVATE);
                                SharedPreferences.Editor Ed=sp.edit();
                                Ed.putString("Unm",user_name );
                                Ed.putString("Psw",password);
                                Ed.commit();
                             // =============== end of Saving login info========================

                                launchActivity();
                            }else{
                                Log.d("fail status", status);
                                // ===============Saving login info========================
                                SharedPreferences sp=getSharedPreferences("carster_login", MODE_PRIVATE);
                                SharedPreferences.Editor Ed=sp.edit();
                                Ed.putString("Unm",null );
                                Ed.putString("Psw",null);
                                Ed.commit();
                                // =============== end of Saving login info========================
                                Toast.makeText(LoginActivity.this, "Login failed! \n Try again.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(LoginActivity.this, "Network error! \n Try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //This code is executed if there is an error.
                        pd.hide();
                        Toast.makeText(LoginActivity.this, "Login Failed. \n Please check your network connection.", Toast.LENGTH_SHORT).show();
                    }
                }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("user_login", user_name); //Add the data you'd like to send to the server.
                MyData.put("user_pass", password); //Add the data you'd like to send to the server.
                return MyData;
            }
        };

        MyRequestQueue.add(MyStringRequest);

    }


    private void launchActivity() {
        if (pd != null){
            pd.cancel();
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
