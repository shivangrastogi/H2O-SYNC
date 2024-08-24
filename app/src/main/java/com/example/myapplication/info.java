package com.example.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class info extends AppCompatActivity {

    private EditText editTextName, editTextContact, editTextProblem, editTextEditableLocation, editTextLandmark;
    private Button buttonSubmit;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    private LatLng userLocation; // User's location
    private Marker submittedMarker; // Variable to hold the submitted marker

    private ActivityResultLauncher<Intent> filePickerLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        editTextName = findViewById(R.id.editTextName);
        editTextContact = findViewById(R.id.editTextContact);
        editTextProblem = findViewById(R.id.editTextProblem);
        editTextEditableLocation = findViewById(R.id.editTextEditableLocation);
        editTextLandmark = findViewById(R.id.editTextLandmark);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        db = FirebaseFirestore.getInstance();


        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        String fileName = getFileName(fileUri);

                        // Display the file name in the EditText
                        EditText editTextChooseFile = findViewById(R.id.editTextChooseFile);
                        editTextChooseFile.setText(fileName);

                        // Optionally, display an image preview if the file is an image
                        ImageView imageViewFilePreview = findViewById(R.id.imageViewFilePreview);
                        if (fileUri.toString().contains("image")) {
                            imageViewFilePreview.setImageURI(fileUri);
                        }
                    }
                }
        );

        Button btnChooseFile = findViewById(R.id.btn_choose_file);
        btnChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to open the file chooser
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*"); // You can change this to "image/*" for image files only
                filePickerLauncher.launch(intent);
            }
        });



        // Retrieve location coordinates from intent
        double latitude = getIntent().getDoubleExtra("LATITUDE", 0.0);
        double longitude = getIntent().getDoubleExtra("LONGITUDE", 0.0);
        userLocation = new LatLng(latitude, longitude);

        // You can use these coordinates as needed, for example, display them in the UI
        editTextEditableLocation.setText(String.format(Locale.getDefault(), "%f, %f", latitude, longitude));

        // Initialize map and set up the submitted marker
        // (Removed the map initialization code since it's not required in this activity)

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();
                String contact = editTextContact.getText().toString().trim();
                String problem = editTextProblem.getText().toString().trim();
                String location = editTextEditableLocation.getText().toString().trim();
                String landmark = editTextLandmark.getText().toString().trim();

                if (!name.isEmpty() && !contact.isEmpty() && !problem.isEmpty() && !location.isEmpty()) {
                    // Store data in Firestore
                    saveToFirestore(name, contact, problem, location, landmark);

                    // Show the submitted marker on the map
                    showSubmittedMarker();

                    showPrompt();
                }
            }
        });
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }




    private void saveToFirestore(String name, String contact, String problem, String location, String landmark) {
        // Create a new user document in Firestore
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("contact", contact);
        user.put("problem", problem);
        user.put("location", location);
        user.put("landmark", landmark);

        String userEmail = currentUser.getEmail(); // Replace this with the actual user's Gmail
        // Add a document with a generated ID
        db.collection("users")
                .document(userEmail)
                .collection("user_data")
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    // Handle success
                    clearInputFields(); // Clear the input fields
                    // You can add any additional actions after a successful submission
                })
                .addOnFailureListener(e -> {
                    // Handle failures
                    // You can add error handling code here
                });
    }

    private void showPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Submission Successful");
        builder.setMessage("Your details have been submitted successfully!");

        // Add a button to go back to MapsActivity
        builder.setPositiveButton("OK", (dialog, which) -> {
            // Close the current activity and go back to MapsActivity
            finish();
        });

        // Show the dialog
        builder.create().show();
    }

    private void clearInputFields() {
        editTextName.setText("");
        editTextContact.setText("");
        editTextProblem.setText("");
        editTextEditableLocation.setText("");
        editTextLandmark.setText("");
    }

    private void showSubmittedMarker() {
        // You may add code here to show the submitted marker on the map if needed
    }
}
