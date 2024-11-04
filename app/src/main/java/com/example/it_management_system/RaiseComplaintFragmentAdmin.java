package com.example.it_management_system;

import static android.app.Activity.RESULT_OK;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RaiseComplaintFragmentAdmin extends Fragment {
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private EditText descriptionInput;
    private Spinner locationSpinner, complaintTypeSpinner, complaintTitleSpinner;
    private Button submitButton, photoUploadButton;
    private TextView requiredTimeTextView, imageLinkTextView;
    private ImageView imagePreview;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private Uri selectedImageUri;
    private StorageReference storageReference;

    private ArrayAdapter<String> locationAdapter, typeAdapter, titleAdapter;
    private ArrayList<String> locations = new ArrayList<>();
    private ArrayList<String> complaintTypes = new ArrayList<>();
    private ArrayList<String> complaintTitles = new ArrayList<>();

    public RaiseComplaintFragmentAdmin() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        populateComplaintDataSet(); // Populate initial complaint data in Firebase
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_raise_complaint_admin, container, false);

        // Initialize UI elements
        descriptionInput = view.findViewById(R.id.description_inputAdmin);
        locationSpinner = view.findViewById(R.id.location_spinnerAdmin);
        complaintTypeSpinner = view.findViewById(R.id.complaint_type_spinnerAdmin);
        complaintTitleSpinner = view.findViewById(R.id.complaint_title_spinnerAdmin);
        requiredTimeTextView = view.findViewById(R.id.required_time_text_viewAdmin);
        submitButton = view.findViewById(R.id.submit_buttonAdmin);
        photoUploadButton = view.findViewById(R.id.photo_upload_buttonAdmin);
        imageLinkTextView = view.findViewById(R.id.image_link_text_viewAdmin);
        imagePreview = view.findViewById(R.id.image_previewAdmin);

        // Populate spinners
        loadSpinners();

        // Set listeners
        submitButton.setOnClickListener(v -> submitComplaint());
        photoUploadButton.setOnClickListener(v -> requestPhotoPermission());

        return view;
    }

    private void loadSpinners() {
        // Load locations
        databaseReference.child("ComplaintData/Locations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                locations.clear();
                locations.add("Select location"); // Add an empty string to show no selection
                for (DataSnapshot locationSnapshot : snapshot.getChildren()) {
                    locations.add(locationSnapshot.getKey());
                }
                locationAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, locations);
                locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                locationSpinner.setAdapter(locationAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Load complaint types
        databaseReference.child("ComplaintData/ComplaintTypes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                complaintTypes.clear();
                complaintTypes.add("Select complaint type"); // Add an empty string to show no selection
                for (DataSnapshot typeSnapshot : snapshot.getChildren()) {
                    complaintTypes.add(typeSnapshot.getKey());
                }
                typeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, complaintTypes);
                typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                complaintTypeSpinner.setAdapter(typeAdapter);

                complaintTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedType = complaintTypes.get(position);
                        if (!selectedType.isEmpty()) { // Only load titles if something is selected
                            loadComplaintTitles(selectedType);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void loadComplaintTitles(String selectedType) {
        databaseReference.child("ComplaintData/ComplaintTypes/" + selectedType).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                complaintTitles.clear(); // Clear previous titles
                complaintTitles.add("Select title");
                // Iterate over the keys of the snapshot to find the titles
                for (DataSnapshot titleSnapshot : snapshot.getChildren()) {
                    if (titleSnapshot.getValue(Boolean.class) != null) {
                       // Ensure it's a title with a boolean value
                        complaintTitles.add(titleSnapshot.getKey());
                    }
                }

                // Set up the adapter for complaint titles
                titleAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, complaintTitles);
                titleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                complaintTitleSpinner.setAdapter(titleAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });
    }
    private void submitComplaint() {
        String location = locationSpinner.getSelectedItem().toString();
        String complaintType = complaintTypeSpinner.getSelectedItem().toString();
        String complaintTitle = complaintTitleSpinner.getSelectedItem().toString();
        String description = descriptionInput.getText().toString();
        String userId = mAuth.getCurrentUser().getUid();

        if (location.isEmpty() || complaintType.isEmpty() || complaintTitle.isEmpty()) {
            Toast.makeText(getContext(), "Please select all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        Map<String, Object> complaintData = new HashMap<>();
        complaintData.put("location", location);
        complaintData.put("type", complaintType);
        complaintData.put("title", complaintTitle);
        complaintData.put("description", description);
        complaintData.put("userId", userId);
        complaintData.put("imageUrl", selectedImageUri != null ? selectedImageUri.toString() : "");
        complaintData.put("date", currentDate);

        databaseReference.child("complaints").push().setValue(complaintData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Complaint submitted successfully!", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to submit complaint.", Toast.LENGTH_SHORT).show());
    }

    private void clearFields() {
        descriptionInput.setText("");
        locationSpinner.setSelection(0);
        complaintTypeSpinner.setSelection(0);
        complaintTitleSpinner.setSelection(0);
        imagePreview.setImageURI(null);
        selectedImageUri = null;
        imageLinkTextView.setText("");
    }



    private void requestPhotoPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            showPhotoOptions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showPhotoOptions();
        } else {
            Toast.makeText(getContext(), "Permissions are required to upload a photo.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPhotoOptions() {
        String[] options = {"Open Camera", "Open Gallery"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }



    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        // Set the ImageView with the bitmap
                        imagePreview.setImageBitmap(imageBitmap);
                        // Optionally, save the bitmap to a file and set selectedImageUri
                        // selectedImageUri = saveBitmapToFile(imageBitmap);
                    }
                    uploadImageToFirebase();
                }
            });

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imageLinkTextView.setText(selectedImageUri.toString());
                    imageLinkTextView.setVisibility(View.VISIBLE);
                    imagePreview.setImageURI(selectedImageUri);
                    imagePreview.setVisibility(View.VISIBLE);
                    uploadImageToFirebase();
                }
            });

    private void uploadImageToFirebase() {
        if (selectedImageUri != null) {
            StorageReference fileReference = storageReference.child("complaint_images/" + System.currentTimeMillis() + ".jpg");
            fileReference.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot ->
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        selectedImageUri = uri;
                        imageLinkTextView.setText("Image uploaded successfully!");
                    })
            ).addOnFailureListener(e ->
                    Toast.makeText(getContext(), "Image upload failed!", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void populateComplaintDataSet() {
        // Define locations
        Map<String, Boolean> locations = new HashMap<>();
        locations.put("B007", true);
        locations.put("B013", true);
        locations.put("B012A", true);
        locations.put("B012B", true);
        locations.put("B101", true);
        locations.put("B102", true);
        locations.put("B108", true);
        locations.put("B109", true);
        locations.put("B110", true);
        locations.put("B114", true);
        locations.put("B115", true);

        // Define complaint types and titles
        Map<String, Boolean> itTitles = new HashMap<>();
        itTitles.put("Internet Issue", true);
        itTitles.put("Laptop Issue", true);

        Map<String, Boolean> maintenanceTitles = new HashMap<>();
        maintenanceTitles.put("AC Issue", true);
        maintenanceTitles.put("Plumbing", true);

        // Construct complaint types
        Map<String, Map<String, Boolean>> complaintTypes = new HashMap<>();
        complaintTypes.put("IT Issue", itTitles);
        complaintTypes.put("Maintenance", maintenanceTitles);

        // Prepare ComplaintData structure for Firebase
        Map<String, Object> complaintData = new HashMap<>();
        complaintData.put("Locations", locations);
        complaintData.put("ComplaintTypes", complaintTypes);

        // Push data to Firebase
        databaseReference.child("ComplaintData").setValue(complaintData)
                .addOnSuccessListener(aVoid -> {
                    // Data added successfully
                    // You can add an acknowledgment here if needed
                })
                .addOnFailureListener(e -> {
                    // Failed to add data
                    e.printStackTrace();
                });
    }
}
