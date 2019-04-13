package com.adlubagusi.e_tronikv1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.adlubagusi.e_tronikv1.Prevalent.Prevalent;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AdminAddNewProductActivity extends AppCompatActivity {

    private String categoryName, Desc, Price, Pname, saveCurrentDate, saveCurrentTime;
    private Button addProductBtn;
    private ImageView inputProductImg;
    private EditText inputProductName, inputProductDesc, inputProductPrice;
    private ProgressDialog loadingBar;


    private static final int GalleryPick = 1;
    private Uri  ImageUri;
    private String productRandomKey, downloadImgUrl;
    private StorageReference ProductImgRef;
    private DatabaseReference ProductRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_new_product);

        categoryName = getIntent().getExtras().get("category").toString();
        ProductImgRef = FirebaseStorage.getInstance().getReference().child("Product Img");
        ProductRef  = FirebaseDatabase.getInstance().getReference().child("Products");

        addProductBtn = findViewById(R.id.add_product_btn);
        inputProductImg =findViewById(R.id.select_product_img);
        inputProductName = findViewById(R.id.product_name);
        inputProductDesc = findViewById(R.id.product_desc);
        inputProductPrice = findViewById(R.id.product_price);
        loadingBar = new ProgressDialog(this);


        inputProductImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateProductata();
            }
        });
    }

    private void validateProductata() {
        Desc = inputProductDesc.getText().toString();
        Price = inputProductPrice.getText().toString();
        Pname = inputProductName.getText().toString();

        if(ImageUri == null){
            Toast.makeText(this, "Gambar wajib diisi", Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(Desc)){
            Toast.makeText(this, "Deskripsi Produk wajib diisi...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(Price)){
            Toast.makeText(this, "Harga Produk wajib diisi...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(Pname)){
            Toast.makeText(this, "Nama Produk wajib diisi...", Toast.LENGTH_SHORT).show();
        }else{
            StoreProductInformation();
        }
    }

    private void StoreProductInformation() {
        loadingBar.setTitle("Menambahkan produk baru");
        loadingBar.setMessage("Harap tunggu, kami sedang menambahkan produk.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        productRandomKey = saveCurrentDate+saveCurrentTime;

        final StorageReference filePath = ProductImgRef.child(ImageUri.getLastPathSegment() + productRandomKey + ".jpg");
        final UploadTask uploadTask = filePath.putFile(ImageUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(AdminAddNewProductActivity.this, "Error :"+message, Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AdminAddNewProductActivity.this, "Gambar behasil diupload", Toast.LENGTH_SHORT).show();
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();

                        }

                        downloadImgUrl = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if(task.isSuccessful()){
                            downloadImgUrl = task.getResult().toString();
                            Toast.makeText(AdminAddNewProductActivity.this, "Berhasil mendapatkan URL gambar", Toast.LENGTH_SHORT).show();
                            saveProductInfoToDatabase();
                        }
                    }
                });
            }
        });
    }

    private void saveProductInfoToDatabase() {
        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("pid", productRandomKey);
        productMap.put("date", saveCurrentDate);
        productMap.put("time", saveCurrentTime);
        productMap.put("desc", Desc);
        productMap.put("image", downloadImgUrl);
        productMap.put("category", categoryName);
        productMap.put("price", Price);
        productMap.put("pname", Pname);

        ProductRef.child(productRandomKey).updateChildren(productMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Intent intent = new Intent(AdminAddNewProductActivity.this, AdminCategoryActivity.class);
                            startActivity(intent);
                            loadingBar.dismiss();
                            Toast.makeText(AdminAddNewProductActivity.this, "Produk berhasil ditambahkan...", Toast.LENGTH_SHORT).show();
                        }else{
                            loadingBar.dismiss();

                            String message = task.getException().toString();
                            Toast.makeText(AdminAddNewProductActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void OpenGallery(){
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GalleryPick && resultCode==RESULT_OK && data!=null){
            ImageUri = data.getData();
            inputProductImg.setImageURI(ImageUri);
        }
    }
}
