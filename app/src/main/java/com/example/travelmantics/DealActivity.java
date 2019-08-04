package com.example.travelmantics;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;

public class DealActivity extends AppCompatActivity {
    private DatabaseReference mDatabaseReferance;
    private StorageReference mStorageReference;
    private EditText txtTile;
    private EditText txtPrice;
    private EditText txtDescription;
    private static final int PICTURE_RESULT = 42;
    TravelDeal deal;
    private Button btnImage;
    private RoundedImageView image;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FirebaseUtil.openFbReference("traveldeals", this);
        mDatabaseReferance = FirebaseUtil.mDatabaseReference;
        txtTile = (EditText) findViewById(R.id.txtTitle);
        txtPrice = (EditText) findViewById(R.id.txtPrice);
        txtDescription = (EditText) findViewById(R.id.txtDesc);
        btnImage = (Button) findViewById(R.id.btnImage);
        image = (RoundedImageView) findViewById(R.id.imageUp);
        progress = new ProgressDialog(this);
        displayDeal();
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent, "Insert Picture"), PICTURE_RESULT);
            }
        });
    }

    private void displayDeal() {
        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if (deal == null){
            deal = new TravelDeal();
        }
        this.deal = deal;
        txtTile.setText(deal.getTitle());
        txtPrice.setText(deal.getPrice());
        txtDescription.setText(deal.getDescription());
        Glide.with(DealActivity.this)
                .load(deal.getImageUrl())
                .apply(new RequestOptions()
                        .optionalFitCenter()
                        .placeholder(R.drawable.avata_default)
                        .centerCrop())
                .into(image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.save_menu:
                saveDeal();
                clean();
                backToList();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(this, "Deal Deleted", Toast.LENGTH_SHORT).show();
                backToList() ;
                return true;
            default:
                Toast.makeText(this, "Incorrect choice", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void clean() {
        txtTile.setText("");
        txtDescription.setText("");
        txtPrice.setText("");
        txtTile.requestFocus();
    }

    private void saveDeal() {
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        deal.setTitle(txtTile.getText().toString());
        deal.setPrice(txtPrice.getText().toString());
        deal.setDescription(txtDescription.getText().toString());
        if (deal.getId() == null) {
            mDatabaseReferance.push().setValue(deal);
        } else {
            mDatabaseReferance.child(deal.getId()).setValue(deal);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            Glide.with(DealActivity.this)
                    .load(imageUri)
                    .apply(new RequestOptions()
                            .optionalFitCenter()
                            .placeholder(R.drawable.avata_default)
                            .centerCrop())
                    .into(image);
            final StorageReference ref = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());
            progress.setMessage("Uploading Image...");
            progress.show();
            ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUrl = uri;
                            String fileUrl = downloadUrl.toString();
                            deal.setImageUrl(fileUrl);
                            progress.dismiss();
                        }
                    });
                }
            });
        }
    }

    private void deleteDeal(){
      if(deal == null){
          Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show();
          return;
      }
      mDatabaseReferance.child(deal.getId()).removeValue();
    }
    private void backToList(){
        startActivity(new Intent(this, ListActiivity.class));
    }

}
