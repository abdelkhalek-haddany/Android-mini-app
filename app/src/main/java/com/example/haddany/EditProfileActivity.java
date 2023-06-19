package com.example.haddany;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "Profile";
    private RadioGroup sexe;
    private RadioButton rdM, rdF;
    private Button saveProfile;
    private EditText fnameTxt, lnameTxt, phoneTxt, emailTxt;
    private ImageView imageProfile;
    private FirebaseAuth auth;
    private FirebaseFirestore fstore;
    private DocumentSnapshot docuser;

    private String randomKey;
    private Uri imageUri, imageOldUri;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private SharedPreferences sharedPreferences;

    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 102;
    private static final int REQUEST_IMAGE_GALLERY = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        auth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        saveProfile = findViewById(R.id.idBtnSave);
        fnameTxt = findViewById(R.id.idRegisterfname);
        lnameTxt = findViewById(R.id.idRegisterlname);
        phoneTxt = findViewById(R.id.idRegisterPhone);
        emailTxt = findViewById(R.id.idRegisterEmail);
        emailTxt.setEnabled(false);
        sexe = findViewById(R.id.radioGroup2);
        rdM = findViewById(R.id.radioButton2);
        rdF = findViewById(R.id.radioButton3);
        imageProfile = findViewById(R.id.imageProfile);

        //Change image profile
        imageProfile.setOnClickListener(e -> {
            choosePicture();
        });

        //Save change profile
        saveProfile.setOnClickListener(view -> {
            int selected = sexe.getCheckedRadioButtonId();
            RadioButton gender = findViewById(selected);

            DocumentReference docref = fstore.collection("users").document(auth.getCurrentUser().getUid());
            HashMap<String, Object> user = new HashMap<>();
            user.put("firstname", fnameTxt.getText().toString());
            user.put("lastname", lnameTxt.getText().toString());
            user.put("email", auth.getCurrentUser().getEmail());
            user.put("profile", String.valueOf(imageUri));
            Log.d(TAG,"************************************************************************ Edit onCreate user image"+imageUri);
            user.put("phone", phoneTxt.getText().toString());
            String sexeUser = gender.getText().toString();
            user.put("sexe", gender.getText().toString());
            docref.update(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(EditProfileActivity.this, HomeActivity.class));
                }
            });
        });

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);

        // Load user profile
        loadUserProfile();
    }

    private void loadUserProfile() {
        String uid = auth.getCurrentUser().getUid();
        DocumentReference docref = fstore.collection("users").document(uid);

        // Check if user profile data is available in SharedPreferences
        if (sharedPreferences.contains(uid)) {
            String firstName = sharedPreferences.getString(uid + "_firstname", "");
            String lastName = sharedPreferences.getString(uid + "_lastname", "");
            String email = sharedPreferences.getString(uid + "_email", "");
            String phone = sharedPreferences.getString(uid + "_phone", "");
            String profileImageUrl = sharedPreferences.getString(uid + "_profileImageUrl" , "");
            String gender = sharedPreferences.getString(uid + "_gender", "");
            Log.d(TAG,"************************************************************************loadUserProfile 1 Edit user profileImageUrl"+profileImageUrl);

            fnameTxt.setText(firstName);
            lnameTxt.setText(lastName);
            emailTxt.setText(email);
            phoneTxt.setText(phone);

            if (gender.equals("Male")) {
                rdM.setChecked(true);
            } else if (gender.equals("Female")) {
                rdF.setChecked(true);
            }

            if (!TextUtils.isEmpty(profileImageUrl)) {
                Glide.with(EditProfileActivity.this)
                        .load(profileImageUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageProfile);
            }
        } else {
            docref.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        fnameTxt.setText(doc.get("firstname").toString());
                        lnameTxt.setText(doc.get("lastname").toString());
                        emailTxt.setText(doc.get("email").toString());
                        phoneTxt.setText(doc.get("phone").toString());

                        if (doc.get("sexe").equals("Male")) {
                            rdM.setChecked(true);
                        } else {
                            rdF.setChecked(true);
                        }

                        String profileImageUrl = doc.get("profile").toString();
                        if (!TextUtils.isEmpty(profileImageUrl)) {
                            Glide.with(EditProfileActivity.this)
                                    .load(profileImageUrl)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(imageProfile);
                        }

                        // Save user profile data in SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(uid + "_firstname", doc.get("firstname").toString());
                        editor.putString(uid + "_lastname", doc.get("lastname").toString());
                        editor.putString(uid + "_email", doc.get("email").toString());
                        editor.putString(uid + "_phone", doc.get("phone").toString());
                        editor.putString(uid + "_profileImageUrl", profileImageUrl);
                        Log.d(TAG,"************************************************************************ loadUserProfile 3 Edit user profileImageUrl"+profileImageUrl);
                        editor.putString(uid + "_gender", doc.get("sexe").toString());
                        editor.apply();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "User null", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "No data");
                    }
                }
            });
        }
    }
    // Image Profile
    private void choosePicture() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    if (checkCameraPermission()) {
                        dispatchTakePictureIntent();
                    } else {
                        requestCameraPermission();
                    }
                } else if (options[item].equals("Choose from Gallery")) {
                    if (checkStoragePermission()) {
                        openGallery();
                    } else {
                        requestStoragePermission();
                    }
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    private boolean checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_IMAGE_GALLERY);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_IMAGE_GALLERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    // Set the imageBitmap to your ImageView
                    imageProfile.setImageBitmap(imageBitmap);
                    // Save the image to Firebase Storage
                    saveImageToStorage(imageBitmap);
                    updateUserProfileImage(imageBitmap.toString());
                }
            } else if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                Uri selectedImageUri = data.getData();
                // Set the selectedImageUri to your ImageView
                imageProfile.setImageURI(selectedImageUri);
                // Save the image to Firebase Storage
                saveImageToStorage(selectedImageUri);
                // Update the profile picture immediately
                updateUserProfileImage(selectedImageUri.toString());
            }
        }
    }

    private void saveImageToStorage(Uri imageUri) {
        // Get the current user ID
        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        // Create a storage reference for the user's profile image
        StorageReference profileImageRef = FirebaseStorage.getInstance().getReference().child("images/" + uid );

        // Upload the image to Firebase Storage
        profileImageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the download URL of the uploaded image
                        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                // Save the download URL to Firestore or SharedPreferences as required
                                String imageUrl = downloadUri.toString();
                                // Update the user profile with the new image URL
                                updateUserProfileImage(imageUrl);
                                // Clear the image cache
                                Glide.with(EditProfileActivity.this).clear(imageProfile);
                                // Update the imageUri variable with the new image URI
                                EditProfileActivity.this.imageUri = downloadUri;
                                // Load the new image into the ImageView
                                Glide.with(EditProfileActivity.this).load(imageUri).into(imageProfile);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveImageToStorage(Bitmap imageBitmap) {
        // Get the current user ID
        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        // Create a storage reference for the user's profile image
        StorageReference profileImageRef = FirebaseStorage.getInstance().getReference().child("images/" + uid);

        // Convert the Bitmap to a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        // Upload the image to Firebase Storage
        profileImageRef.putBytes(imageData)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the download URL of the uploaded image
                        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                // Save the download URL to Firestore or SharedPreferences as required
                                String imageUrl = downloadUri.toString();
                                // Update the user profile with the new image URL
                                updateUserProfileImage(imageUrl);
                                // Convert the Bitmap to a Drawable
                                BitmapDrawable drawable = new BitmapDrawable(getResources(), imageBitmap);
                                // Clear the image cache
                                imageProfile.setImageDrawable(null);
                                // Update the imageUri variable with the new image URI
                                EditProfileActivity.this.imageUri = downloadUri;
                                // Load the new Bitmap into the ImageView
                                imageProfile.setImageDrawable(drawable);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

   private void updateUserProfileImage(String imageUrl) {
       // Update the user profile with the new image URL in Firestore or SharedPreferences as required
       String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
       DocumentReference userRef = fstore.collection("users").document(uid);
       userRef.update("profile", imageUrl)
               .addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void aVoid) {
                       Toast.makeText(EditProfileActivity.this, "Profile image updated successfully", Toast.LENGTH_SHORT).show();

                       // Update the imageUri variable with the new image URL
                       imageUri = Uri.parse(imageUrl);
                   }
               }).addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       Toast.makeText(EditProfileActivity.this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                   }
               });
   }

    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    //clicked menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch ((item.getItemId())){
            case R.id.action_profile:
                startActivity(new Intent(EditProfileActivity.this, EditProfileActivity.class));
                return true;
            case R.id.action_map:
                startActivity(new Intent(EditProfileActivity.this, LocationActivity.class));
                return true;
            case R.id.action_activities:
                startActivity(new Intent(EditProfileActivity.this, ActionActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Load user profile
        loadUserProfile();
    }
}