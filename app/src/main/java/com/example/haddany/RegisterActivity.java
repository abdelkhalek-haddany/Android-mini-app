package com.example.haddany;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity" ;
    private Button btnRegister;
    private EditText emailTxt, passwordTxt, confirmPasswordTxt,fnameTxt, lnameTxt, phoneTxt;
    private TextView errorTxt;
    private RadioGroup sexe;

    private FirebaseAuth auth;
    private FirebaseFirestore fstore;
    private Uri imageUri, imageOldUri;
    private String userID;
    private String randomKey;

    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 102;
    private static final int REQUEST_IMAGE_GALLERY = 103;

    private ImageView imageProfile;
    private Bitmap imageBitmap;
    private Uri selectedImageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailTxt = findViewById(R.id.idRegisterEmail);
        passwordTxt = findViewById(R.id.idRegisterPassword);
        confirmPasswordTxt = findViewById(R.id.idRegisterConfirmPassword);
        fnameTxt = findViewById(R.id.idRegisterfname);
        lnameTxt = findViewById(R.id.idRegisterlname);
        phoneTxt = findViewById(R.id.idRegisterPhone);
        errorTxt = findViewById(R.id.idTextErrorView);
        btnRegister = findViewById(R.id.idBtnRegister);
//        imageProfile = findViewById(R.id.idImageProfile);
        sexe = findViewById(R.id.radioGroup);

        auth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        storage = FirebaseStorage.getInstance("gs://android-47d0f.appspot.com/");
        storageReference = storage.getReference();
        // save image
        imageProfile.setOnClickListener(e->{
            //optionChooseImage();
            choosePicture();
            Log.d(TAG,"****************************************************************choosePicture*********************************");
        });
        //Register info
        btnRegister.setOnClickListener(e->{
            errorTxt.setVisibility(View.GONE);

            String txtEmail = emailTxt.getText().toString();
            String txtPassword = passwordTxt.getText().toString();
            String txtconfirmPassword = confirmPasswordTxt.getText().toString();
            String txtfname = fnameTxt.getText().toString();
            String txtlname = lnameTxt.getText().toString();
            String txtphone = phoneTxt.getText().toString();
            int selected = sexe.getCheckedRadioButtonId();
            RadioButton gender=(RadioButton) findViewById(selected);
            String sexeUser = gender.getText().toString();

            if (TextUtils.isEmpty(txtEmail) || TextUtils.isEmpty(txtPassword)
                    || TextUtils.isEmpty(txtconfirmPassword) || TextUtils.isEmpty(txtfname) || TextUtils.isEmpty(sexeUser)
                    || TextUtils.isEmpty(txtlname) || TextUtils.isEmpty(txtphone)){
                Toast.makeText(RegisterActivity.this, "Fill all The inputs !!!", Toast.LENGTH_SHORT).show();
                errorTxt.setText("Fill all The inputs !!!");
                errorTxt.setVisibility(View.VISIBLE);
            } else if (txtPassword.length() < 6){
                Toast.makeText(RegisterActivity.this, "Password too short!", Toast.LENGTH_SHORT).show();
            } else if (!txtconfirmPassword.equals(txtPassword)){
                Toast.makeText(RegisterActivity.this, "Password and confirm password not matched", Toast.LENGTH_SHORT).show();

            }else {
                registerUser(txtEmail , txtPassword, txtfname, txtlname, txtphone, sexeUser);
            }
        });
    }

    private void pickFromCamera() {
        System.out.println("------------ intent---------------");
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        System.out.println("---------------end intent ----------------");
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private void choosePicture() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    requestCameraPermission();
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
        Log.d(TAG, "check camera permission********************************************* ");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
//        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        Log.d(TAG, "camera request****************************************** ");
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_CAMERA_PERMISSION);
        }
        else {
            pickFromCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFromCamera();
            }else {
                Toast.makeText(this, "Camera Permission is required to use camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
//        }

    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_IMAGE_GALLERY);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Log.d(TAG, "REQUEST_IMAGE_CAPTURE ---- bitmap ************************************************* ");
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Log.d(TAG, "extrat********************************************  ");
                    imageBitmap = (Bitmap) extras.get("data");
                    // Set the imageBitmap to your ImageView
                    imageProfile.setImageBitmap(imageBitmap);
                    // Save the image to Firebase Storage
                }
            } else if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                Log.d(TAG, "REQUEST_IMAGE_GALLERY****************************************");
                selectedImageUri = data.getData();
                // Set the selectedImageUri to your ImageView
                imageProfile.setImageURI(selectedImageUri);
            }
        }
    }

    private void saveImageToStorage(Uri imageUri, String uid) {
        Log.d(TAG, "imageUri********************************************************************************* URI " + imageUri);
        // Get the current user ID
        //String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        // Create a storage reference for the user's profile image
        StorageReference profileImageRef = FirebaseStorage.getInstance().getReference().child("images/" + uid);

        // Upload the image to Firebase Storage
        profileImageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the download URL of the uploaded image
                        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                Log.d(TAG, "imageUri********************************************************************************* " + downloadUri);
                                // Save the download URL to Firestore or SharedPreferences as required
                                String imageUrl = downloadUri.toString();
                                // Update the user profile with the new image URL
                                updateUserProfileImage(imageUrl);
                                // Clear the image cache
                                Glide.with(RegisterActivity.this).clear(imageProfile);
                                // Update the imageUri variable with the new image URI
                                RegisterActivity.this.imageUri = downloadUri;

                                // Load the new image into the ImageView
                                Glide.with(RegisterActivity.this).load(imageUri).into(imageProfile);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(RegisterActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveImageToStorage(Bitmap imageBitmap, String uid) {
        Log.d(TAG, "imageUri********************************************************************************* Bitmap " + imageBitmap);
        // Get the current user ID
        //String uid = auth.getCurrentUser().getUid();
        //Log.d(TAG, "imageUri********************************************************************************* uid " + uid);
        // Create a storage reference for the user's profile image
        StorageReference profileImageRef = FirebaseStorage.getInstance().getReference().child("images/" + uid);
        Log.d(TAG, "imageUri********************************************************************************* profileImageRef " + profileImageRef);

        // Convert the Bitmap to a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();
        Log.d(TAG, "imageUri********************************************************************************* imageData " + imageData);
        // Upload the image to Firebase Storage
        profileImageRef.putBytes(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "imageUri********************************************************************************* onSuccess 1 ");
                        // Get the download URL of the uploaded image
                        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                            @Override
                            public void onSuccess(Uri downloadUri) {
                                Log.d(TAG, "imageUri********************************************************************************* " + downloadUri);
                                // Save the download URL to Firestore or SharedPreferences as required
                                String imageUrl = downloadUri.toString();
                                // Update the user profile with the new image URL
                                updateUserProfileImage(imageUrl);
                                // Convert the Bitmap to a Drawable
                                BitmapDrawable drawable = new BitmapDrawable(getResources(), imageBitmap);
                                // Clear the image cache
                                imageProfile.setImageDrawable(null);
                                // Update the imageUri variable with the new image URI
                                RegisterActivity.this.imageUri = downloadUri;
                                // Load the new Bitmap into the ImageView
                                imageProfile.setImageDrawable(drawable);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(RegisterActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserProfileImage(String imageUrl) {
        // Update the user profile with the new image URL in Firestore or SharedPreferences as required
        String uid = auth.getCurrentUser().getUid();
        DocumentReference userRef = fstore.collection("Users").document(uid);
        userRef.update("profile", imageUrl)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RegisterActivity.this, "Profile image updated successfully", Toast.LENGTH_SHORT).show();

                        // Update the imageUri variable with the new image URL
                        imageUri = Uri.parse(imageUrl);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void registerUser(String email, String password, String fname, String lname, String phone, String sexe) {
        /*if (imageUri == null) {
            Toast.makeText(RegisterActivity.this, "Please choose a profile image", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "imageUri********************************************************************************* " + imageUri);
            return;
        }*/
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Registering user successfully", Toast.LENGTH_SHORT).show();
                    userID = auth.getCurrentUser().getUid();

                    if (imageBitmap != null) {
                        System.out.println("================ image bitmap ====================");
                        saveImageToStorage(imageBitmap, userID);
                        System.out.println("================ image bitmap saved ====================");
                    } else if (selectedImageUri != null) {
                        System.out.println("================ image uri ====================");
                        saveImageToStorage(selectedImageUri, userID);
                        System.out.println("================ image uri saved ====================");
                    }

                    DocumentReference docref = fstore.collection("Users").document(userID);

                    // Add data with empty
                    HashMap<String, String> user = new HashMap<>();
                    user.put("firstname", fname);
                    user.put("lastname", lname);
                    user.put("phone", phone);
                    user.put("email", email);
                    user.put("sexe", sexe);
                    user.put("profile", userID);

                    docref.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d(TAG, "User profile created with ID: " + userID);
                            //Log.d(TAG, "IMAGE **********************************************************************: " + imageUri.toString());
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegisterActivity.this, "Failed to create user profile", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, "Registering User failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}