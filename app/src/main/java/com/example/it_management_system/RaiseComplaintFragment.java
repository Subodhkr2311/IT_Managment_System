package com.example.it_management_system;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.nl.translate.Translator;
import android.widget.RadioButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RaiseComplaintFragment extends Fragment {

    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_CAMERA = 2;

    private EditText descriptionInput;
    private Spinner locationSpinner, complaintTypeSpinner, complaintTitleSpinner;
    private Button submitButton, photoUploadButton;
    private TextView requiredTimeTextView, imageLinkTextView;
    private ImageView imagePreview;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private Uri selectedImageUri;

    public RaiseComplaintFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_raise_complaint, container, false);

        descriptionInput = view.findViewById(R.id.description_input);
        locationSpinner = view.findViewById(R.id.location_spinner);
        complaintTypeSpinner = view.findViewById(R.id.complaint_type_spinner);
        complaintTitleSpinner = view.findViewById(R.id.complaint_title_spinner);
        requiredTimeTextView = view.findViewById(R.id.required_time_text_view);
        submitButton = view.findViewById(R.id.submit_button);
        photoUploadButton = view.findViewById(R.id.photo_upload_button);
        imageLinkTextView = view.findViewById(R.id.image_link_text_view);
        imagePreview = view.findViewById(R.id.image_preview);

        populateSpinners();

        complaintTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                String selectedType = complaintTypeSpinner.getSelectedItem().toString();
                loadComplaintTitles(selectedType);
                requiredTimeTextView.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        complaintTitleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view12, int position, long id) {
                String selectedTitle = complaintTitleSpinner.getSelectedItem().toString();
                if (selectedTitle.contains("(")) {
                    String requiredTime = selectedTitle.split("\\(")[1].replace(")", "").trim();
                    requiredTimeTextView.setText("Required Time: " + requiredTime);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        submitButton.setOnClickListener(v -> submitComplaint());
        photoUploadButton.setOnClickListener(v -> showImagePickerOptions());

        return view;
    }

    private void populateSpinners() {
        String[] locations = {"Select Location", "Ward 1", "Ward 2", "Ward 3", "Main Road", "others"};
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, locations);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);

        String[] complaintTypes =  {"Select Complaint Type", "Garbage Collection Issue", "Road Maintenance Issue", "Water Supply Issue", "others"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, complaintTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        complaintTypeSpinner.setAdapter(typeAdapter);
    }

    private void loadComplaintTitles(String complaintType) {
        String[] titles;
        if (complaintType.equals("Garbage Collection Issue")) {
            titles = new String[]{"Select Title", "Garbage not collected for days (Required time: 1 day)", "Overflowing garbage bins (Required time: 2 hours)"};
        } else if (complaintType.equals("Road Maintenance Issue")) {
            titles = new String[]{"Select Title", "Potholes on road (Required time: 2 days)", "Road blocked due to construction (Required time: 3 days)"};
        } else if (complaintType.equals("Water Supply Issue")) {
            titles = new String[]{"Select Title", "No water supply in the area (Required time: 1 day)", "Water leakage in pipes (Required time: 4 hours)"};
        } else if (complaintType.equals("others")) {
            titles = new String[]{"Select Title", "Describe your complaint and location in the description section"};
        } else {
            titles = new String[]{"Select Title", "General municipality issue (Required time: 2 days)"};
        }
        ArrayAdapter<String> titleAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, titles);
        titleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        complaintTitleSpinner.setAdapter(titleAdapter);
    }

    private void showImagePickerOptions() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        } else {
            String[] options = {"Open Camera", "Open Gallery"};
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Select Image Source")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            openCamera();
                        } else {
                            openGallery();
                        }
                    })
                    .show();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK) {
                selectedImageUri = data.getData();
                imageLinkTextView.setText(selectedImageUri.toString());
                imageLinkTextView.setVisibility(View.VISIBLE);
                imagePreview.setImageURI(selectedImageUri);
                imagePreview.setVisibility(View.VISIBLE);
            } else if (requestCode == REQUEST_CAMERA) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imagePreview.setImageBitmap(imageBitmap);
                    imagePreview.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void submitComplaint() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String location = locationSpinner.getSelectedItem().toString();
        String complaintType = complaintTypeSpinner.getSelectedItem().toString();
        String title = complaintTitleSpinner.getSelectedItem().toString().split(" \\(")[0];
        String description = descriptionInput.getText().toString().trim();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());
        long currentTime = System.currentTimeMillis();
        if (TextUtils.isEmpty(title) || title.equals("Select Title")) {
            Toast.makeText(requireContext(), "Please select a complaint title", Toast.LENGTH_SHORT).show();
            return;
        }

        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(500);
        submitButton.startAnimation(animation);

        String complaintId = databaseReference.child("complaints").push().getKey();
        if (complaintId == null) {
            Toast.makeText(requireContext(), "Error generating complaint ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Complaints complaint = new Complaints(userId, location, complaintType, title, description, "Pending", currentTime, null, currentDate);

        if (selectedImageUri != null) {
            uploadImageAndSubmit(complaint, complaintId);
        } else {
            // If no image is selected, still submit the complaint without the photo URL
            complaint.setPhotoUrl(null); // Ensure the photo URL is null if no image is uploaded
            submitComplaintToDatabase(complaint, complaintId);
        }
    }

    private void uploadImageAndSubmit(Complaints complaint, String complaintId) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("complaint_images/" + complaintId + ".jpg");
        storageReference.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            complaint.setPhotoUrl(uri.toString());
                            submitComplaintToDatabase(complaint, complaintId);
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void submitComplaintToDatabase(Complaints complaint, String complaintId) {
        databaseReference.child("complaints").child(complaintId).setValue(complaint)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), "Complaint submitted successfully", Toast.LENGTH_SHORT).show();
                        resetForm();
                    } else {
                        Toast.makeText(requireContext(), "Failed to submit complaint: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetForm() {
        descriptionInput.setText("");
        locationSpinner.setSelection(0);
        complaintTypeSpinner.setSelection(0);
        complaintTitleSpinner.setSelection(0);
        requiredTimeTextView.setText("");
        imagePreview.setImageURI(null);
        imagePreview.setVisibility(View.GONE);
        imageLinkTextView.setText("");
        imageLinkTextView.setVisibility(View.GONE);
        selectedImageUri = null;
    }
    // Add these methods to your existing RaiseComplaintFragment class

   public void translateToHindi(Translator englishHindiTranslator) {
        // Translate static text
        englishHindiTranslator.translate("Submit")
                .addOnSuccessListener(translatedText -> submitButton.setText(translatedText));

        englishHindiTranslator.translate("Upload Photo")
                .addOnSuccessListener(translatedText -> photoUploadButton.setText(translatedText));

        // Translate spinner items
        translateSpinnerItems(locationSpinner, englishHindiTranslator);
        translateSpinnerItems(complaintTypeSpinner, englishHindiTranslator);
        translateSpinnerItems(complaintTitleSpinner, englishHindiTranslator);

        // Translate hint
        englishHindiTranslator.translate(descriptionInput.getHint().toString())
                .addOnSuccessListener(translatedText -> descriptionInput.setHint(translatedText));

        // Store original English text for reset
        if (requiredTimeTextView.getTag() == null) {
            requiredTimeTextView.setTag(requiredTimeTextView.getText().toString());
        }

        englishHindiTranslator.translate(requiredTimeTextView.getText().toString())
                .addOnSuccessListener(translatedText -> requiredTimeTextView.setText(translatedText));
    }
    public void translateSpinnerItems(Spinner spinner, Translator translator) {
        ArrayAdapter<String> currentAdapter = (ArrayAdapter<String>) spinner.getAdapter();
        if (currentAdapter == null) return;

        // Create a new list to hold translated items
        List<String> items = new ArrayList<>();
        for (int i = 0; i < currentAdapter.getCount(); i++) {
            items.add(currentAdapter.getItem(i));
        }

        // Create a new adapter for translated items
        ArrayAdapter<String> newAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Keep track of how many items have been translated
        final int[] translatedCount = {0};
        final int totalItems = items.size();

        // Translate each item
        for (String item : items) {
            translator.translate(item)
                    .addOnSuccessListener(translatedText -> {
                        newAdapter.add(translatedText);
                        translatedCount[0]++;

                        // When all items are translated, set the new adapter
                        if (translatedCount[0] == totalItems) {
                            spinner.setAdapter(newAdapter);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // In case of failure, add the original text
                        newAdapter.add(item);
                        translatedCount[0]++;

                        if (translatedCount[0] == totalItems) {
                            spinner.setAdapter(newAdapter);
                        }
                    });
        }
    }

    public void resetSpinnerToEnglish(Spinner spinner, List<String> originalItems) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                originalItems
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    public void resetsToEnglish() {
        // Logic to reset to English, e.g., updating UI or changing language settings
        Log.d("RaiseComplaintFragment", "Resetting language to English");
        // Add your reset logic here
    }
}
