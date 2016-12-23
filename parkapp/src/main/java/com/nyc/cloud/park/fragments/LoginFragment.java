package com.nyc.cloud.park.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nyc.cloud.park.IpConfig;
import com.nyc.cloud.park.MapsActivity;
import com.nyc.cloud.park.R;
import com.nyc.cloud.park.common.logger.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.nyc.cloud.park.StartActivity.progressBar;
import static com.nyc.cloud.park.StartActivity.spreference;

public class LoginFragment extends Fragment {

    Button btnLogin;
    AutoCompleteTextView txtpass, txtemail;
    TextInputLayout tilemail,tilepass;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view  = inflater.inflate(R.layout.fragment_login, container, false);
        setupViews(view);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtemail.getWindowToken(), 0);
        return view;
    }

    void setupViews(View view){

        txtemail = (AutoCompleteTextView) view.findViewById(R.id.txtemail);
        tilemail = (TextInputLayout) view.findViewById(R.id.tilemail);
        txtemail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                    txtpass.requestFocus();
                }
                return false;
            }
        });
        txtpass = (AutoCompleteTextView) view.findViewById(R.id.txtpass);
        tilepass= (TextInputLayout) view.findViewById(R.id.tilpass);
        txtpass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    btnLogin.performClick();
                }
                return false;
            }
        });

        btnLogin = (Button) view.findViewById(R.id.btnLogin);
        Typeface font = Typeface.createFromAsset( getActivity().getAssets(), "fontawesome-webfont.ttf" );
        btnLogin.setTypeface(font);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                final String email = txtemail.getText().toString();
                String pass = txtpass.getText().toString();
                if (email.trim().equals("")){
                    tilemail.setError("Please enter email Address");
                    txtemail.requestFocus();
                }else if (!email.trim().matches(emailPattern)) {
                    tilemail.setError("Please enter a valid email Address");
                    txtemail.requestFocus();
                }else if (pass.trim().equals("")) {
                    tilepass.setError("Please enter password");
                    txtpass.requestFocus();
                }else {
                    progressBar.setVisibility(View.VISIBLE);
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("email",email);
                        obj.put("password",pass);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d("Signup",obj.toString());
                    final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                            IpConfig.SERVER+"/login/",obj, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                //
                                progressBar.setVisibility(View.GONE);
                                String array=response.getString("success");
                                if(array.equals("true")){
                                    SharedPreferences.Editor editor = spreference.edit();
                                    editor.putString(getActivity().getString(R.string.token),response.getString("token"));
                                    editor.putString(getActivity().getString(R.string.fullName),response.getString("full_name"));
                                    editor.putString(getActivity().getString(R.string.email),email);
                                    editor.commit();
                                    Intent intent = new Intent(getActivity(),MapsActivity.class);
                                    intent.putExtra("token",response.getString("token"));
                                    startActivity(intent);
                                    getActivity().finish();
                                }else{
                                    JSONArray errorMessage=response.getJSONArray("error_list");
                                    Toast.makeText(getActivity(),errorMessage.get(0).toString(),Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                Log.v("response error:",""+e.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            progressBar.setVisibility(View.GONE);
                            try {
                                VolleyLog.d(volleyError.getMessage(), "Error: " + volleyError.getMessage());
                            }catch (Exception exc){

                            }
                        }
                    });
                    Volley.newRequestQueue(getActivity()).add(jsonObjReq);
                }

            }
        });
    }

}
