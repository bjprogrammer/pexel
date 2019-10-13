package com.cognitivecloud.pexel.detail;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import com.cognitivecloud.pexel.R;
import com.cognitivecloud.pexel.databinding.ActivityImageBinding;
import com.cognitivecloud.pexel.utils.Constants;

import java.io.File;

import es.dmoral.toasty.Toasty;

public class DetailActivity extends AppCompatActivity implements DetailContract.DetailView {
    private ActivityImageBinding binding;
    private ProgressDialog progressDialog;
    private String imageUrl;
    private DetailPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_image);
        presenter = new DetailPresenter(getApplicationContext(), this);

        imageUrl = getIntent().getStringExtra(Constants.IMAGE_URL);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.setImageUrl(imageUrl);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.download, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;

            case R.id.menu_item_download:
                presenter.downloadImage(imageUrl);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showWait() {
        progressDialog =new ProgressDialog(this);
        progressDialog.setMessage("Downloading...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    public void removeWait() {
        progressDialog.dismiss();
    }

    @Override
    public void onFailure(String appErrorMessage) {
        Toasty.error(getApplicationContext(), appErrorMessage, Toast.LENGTH_LONG, true).show();
    }

    @Override
    public void onSuccess(File file) {
        Uri uri = FileProvider.getUriForFile(this,"com.cognitivecloud.pexel.provider",file);
        if(uri!=null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setDataAndType(uri, getMimeType(file));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);

            try{
                PackageManager packageManager = getPackageManager();
                if(intent.resolveActivity(packageManager)!=null){
                    startActivity(intent);
                }
                else{
                    handleException("No available application to download image");
                }
            }catch (Exception e){
                handleException(e.getMessage());
            }
        }
        else {
            handleException("Could not download image");
        }
    }

    private void handleException(String error){
        AlertDialog alertDialog = new AlertDialog.Builder(this).setMessage(error).create();
        alertDialog.show();
        progressDialog.dismiss();
    }


    public String getMimeType(File file){
        String mimeType = "";
        String extension = getExtention(file.getName());
        if (MimeTypeMap.getSingleton().hasExtension(extension)) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        return mimeType;
    }

    private  String getExtention(String fileName){
        char[] arrayOfFilename = fileName.toCharArray();
        for(int i = arrayOfFilename.length-1; i > 0; i--){
            if(arrayOfFilename[i] == '.'){
                return fileName.substring(i+1, fileName.length());
            }
        }
        return "";
    }

    @Override
    protected void onDestroy() {
        presenter.unSubscribe();
        presenter =null;

        super.onDestroy();

    }
}
