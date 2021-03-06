package virginia.hc.philips.com.virginiademoapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.gmariotti.cardslib.library.cards.material.MaterialLargeImageCard;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.recyclerview.internal.CardArrayRecyclerViewAdapter;
import it.gmariotti.cardslib.library.recyclerview.view.CardRecyclerView;
import it.gmariotti.cardslib.library.view.CardViewNative;

public class PatientActivity extends AppCompatActivity {

    private EditText txtSend;
    private EditText txtFirstName;
    private EditText txtLastName;
    private EditText txtAge;
    private EditText txtAddress;
    private RadioButton radMale;
    private RadioButton radFemale;
    private RadioButton radOthers;

    private SharedPreferences settings;
    private ProgressDialog waitCursor;
    private RequestQueue queue;
    private static final int REQUEST_CODE = 799;
    private static final int REQUEST_IMAGE_CAPTURE = 979;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        settings = getSharedPreferences("ServerSettings", 0);
        queue = Volley.newRequestQueue(this);
        waitCursor = new ProgressDialog(this);
        waitCursor.setMessage("Please wait...");
        waitCursor.setIndeterminate(true);
        waitCursor.setCancelable(false);

        txtFirstName = (EditText)findViewById(R.id.editText5);
        txtLastName = (EditText)findViewById(R.id.editText6);
        txtAge = (EditText)findViewById(R.id.editText7);
        txtAddress = (EditText)findViewById(R.id.editText8);

        radMale = (RadioButton)findViewById(R.id.radioButton4);
        radFemale = (RadioButton)findViewById(R.id.radioButton5);
        radOthers = (RadioButton)findViewById(R.id.radioButton6);

        final ImageButton btnVoice = (ImageButton)findViewById(R.id.btn_voice);
        btnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceRecognitionActivity();
            }
        });

        final ImageButton btnSend = (ImageButton)findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendMonologue(txtSend.getText().toString());
            }
        });

        txtSend = (EditText)findViewById(R.id.chat_text);
        txtSend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s == null || s.length()  == 0) {
                    btnVoice.setVisibility(View.VISIBLE);
                    btnSend.setVisibility(View.GONE);
                } else {
                    btnVoice.setVisibility(View.GONE);
                    btnSend.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard

            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            //Toast.makeText(this,matches.toString(),Toast.LENGTH_LONG).show();
            if(matches.size() > 0) {
                onSendMonologue(matches.get(0));
            }
//            wordsList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
//                    matches));
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            File file = new File(Environment.getExternalStorageDirectory()+File.separator + "virginiaocr.jpg");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap bitmap = bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),options);
            onSendOCR(resizeImage(bitmap));

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_patient, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_camera) {
            dispatchTakePictureIntent();
        }

        return super.onOptionsItemSelected(item);
    }

    private void startVoiceRecognitionActivity()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Virginia Demo");
        startActivityForResult(intent, REQUEST_CODE);
    }

    private String getPatientAge(JSONArray output) {
        String age = "";
        String age_unit = "";

        try {

            for (int i = 0; i < output.length(); i++) {
                JSONObject result = output.getJSONObject(i);
                if (result.getString("for").equalsIgnoreCase("age")) {
                    age = result.getString("match");
                } else if (result.getString("for").equalsIgnoreCase("age_unit")) {
                    age_unit = result.getString("match");
                    if (!age_unit.endsWith("s")) {
                        age_unit += "s";
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(age.length() > 0) {
            age += " " + age_unit;
        }
        return age;
    }

    private void onSendMonologue(String monologue) {
        waitCursor.show();
        //POST data to server
        String url = settings.getString("ServerURL","") +"/monologue";

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("monologue", monologue);
        params.put("seed", "pat_registration");

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // response
                        //Toast.makeText(PatientActivity.this, response.toString(),Toast.LENGTH_LONG).show();
                        JSONArray output = null;
                        try {
                            output = response.getJSONArray("out");
                            String age = PatientActivity.this.getPatientAge(output);
                            if(age.length() > 0) {
                                txtAge.setText(age);
                            }
                            for(int i = 0; i < output.length(); i++) {
                                try {
                                    JSONObject result = output.getJSONObject(i);
                                    if(result.getString("for").equalsIgnoreCase("first_name")){
                                        txtFirstName.setText(result.getString("match"));
                                    } else if(result.getString("for").equalsIgnoreCase("last_name")){
                                        txtLastName.setText(result.getString("match"));
                                    } else if(result.getString("for").equalsIgnoreCase("address")){
                                        txtAddress.setText(result.getString("match"));
                                    } else if(result.getString("for").equalsIgnoreCase("gender")){
                                        String gender = result.getString("match");
                                        if(!(gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("female"))) {
                                            JSONArray possibilities = result.getJSONArray("possiblities");
                                            if(possibilities.length() > 0) {
                                                if(possibilities.getString(0).equalsIgnoreCase("male")) {
                                                    radMale.setChecked(true);
                                                    radFemale.setChecked(false);
                                                    radOthers.setChecked(false);
                                                } else if(possibilities.getString(0).equalsIgnoreCase("female")) {
                                                    radFemale.setChecked(true);
                                                    radMale.setChecked(false);
                                                    radOthers.setChecked(false);
                                                } else {
                                                    radOthers.setChecked(true);
                                                    radFemale.setChecked(false);
                                                    radMale.setChecked(false);
                                                }
                                            } else {
                                                radOthers.setChecked(true);
                                                radFemale.setChecked(false);
                                                radMale.setChecked(false);
                                            }
                                        } else {
                                            if(gender.equalsIgnoreCase("male")) {
                                                radMale.setChecked(true);
                                                radFemale.setChecked(false);
                                                radOthers.setChecked(false);
                                            } else if(gender.equalsIgnoreCase("female")) {
                                                radFemale.setChecked(true);
                                                radMale.setChecked(false);
                                                radOthers.setChecked(false);
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        waitCursor.hide();
                        txtSend.setText("");
                        View view = PatientActivity.this.getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        waitCursor.hide();
                        Toast.makeText(PatientActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.d("Error.Response", error.toString());
                    }
                }
        );
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory()+File.separator + "virginiaocr.jpg");
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private Bitmap resizeImage(Bitmap orgImage) {
        Bitmap resizedImage =
                Bitmap.createScaledBitmap(orgImage,(int)(orgImage.getWidth()*0.5), (int)(orgImage.getHeight()*0.5), true);
        return resizedImage;
    }

    private byte[] getImage(Bitmap image){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        //String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        //return encodedImage;
        return imageBytes;
    }

    private void onSendOCR(final Bitmap image) {
        waitCursor.show();
        String url = settings.getString("ServerURL","") +"/ocr";
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                waitCursor.hide();
                String resultResponse = new String(response.data);
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    //Toast.makeText(PatientActivity.this, result.getString("text"),Toast.LENGTH_LONG).show();
                    txtSend.setText(result.getString("text"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                waitCursor.hide();
                Toast.makeText(PatientActivity.this, error.getMessage(),Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("file", new DataPart("image.jpg",getImage(image)));

                return params;
            }
        };

        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(multipartRequest);
    }
}
