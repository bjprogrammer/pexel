package com.cognitivecloud.pexel.main;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cognitivecloud.pexel.R;
import com.cognitivecloud.pexel.databinding.ActivityMainBinding;
import com.cognitivecloud.pexel.detail.DetailActivity;
import com.cognitivecloud.pexel.model.ImageListResponse;
import com.cognitivecloud.pexel.utils.ConnectivityReceiver;
import com.cognitivecloud.pexel.utils.Constants;
import com.cognitivecloud.pexel.utils.PaginationScrollListener;


import es.dmoral.toasty.Toasty;

import static com.cognitivecloud.pexel.utils.Constants.CONNECTIVITY_ACTION;

public class MainActivity extends AppCompatActivity implements MainContract.MainView, ConnectivityReceiver.ConnectivityReceiverListener{
    private ActivityMainBinding binding;
    private MainPresenter presenter;
    private boolean flag = true;
    private IntentFilter intentFilter;
    private ConnectivityReceiver receiver;

    private MainAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private String currentQuery= Constants.DEFAULT_SEARCH;
    private SearchView search;

    private TextView emptyList;

    private static final int PAGE_START = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = PAGE_START;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        presenter = new MainPresenter(getApplicationContext(),this);

        recyclerView = binding.recyclerView;
        emptyList = binding.tvEmpty;
        progressBar = binding.progressBar;
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);
        receiver = new ConnectivityReceiver();

        //Configuring customized Toast messages
        Toasty.Config.getInstance()
                .setErrorColor( getResources().getColor(R.color.colorPrimaryDark) )
                .setSuccessColor(getResources().getColor(R.color.colorPrimaryDark) )
                .setTextColor(Color.WHITE)
                .tintIcon(true)
                .setTextSize(18)
                .apply();

        adapter = new MainAdapter(imageUrl -> {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra(Constants.IMAGE_URL,imageUrl);
            startActivity(intent);
        });


        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,Constants.GRID_SIZE);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new PaginationScrollListener(gridLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;

                // mocking network delay for API call
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        presenter.getImageList(currentPage, currentQuery);
                    }
                }, 1000);
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(adapter.getItemViewType(position)){
                    case MainAdapter.LOADING:
                        return Constants.GRID_SIZE;
                    case MainAdapter.ITEM:
                        return 1; //number of columns of the grid
                    default:
                        return -1;
                }
            }
        });
        // mocking network delay for API call
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                presenter.getImageList(currentPage, currentQuery);
            }
        }, 1000);

    }


    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(receiver, intentFilter);
        ConnectivityReceiver.connectivityReceiverListener = this;
    }

    //Checking internet flag using broadcast receiver
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if(flag!=isConnected)
        {
            if(isConnected){
                Toasty.success(this, "Connected to internet", Toast.LENGTH_SHORT, true).show();
            }
            else
            {
                Toasty.error(getApplicationContext(), "Not connected to internet", Toast.LENGTH_LONG, true).show();
            }
        }
        flag= (isConnected);
    }

    @Override
    protected void onDestroy() {
        presenter.unSubscribe();
        presenter = null;
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (receiver != null) {
            try {
                unregisterReceiver(receiver);
            } catch (Exception e) { }
        }
    }

    @Override
    public void showWait() {
        progressBar.setVisibility(View.VISIBLE);

    }

    @Override
    public void removeWait() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onFailure(String appErrorMessage) {
        progressBar.setVisibility(View.GONE);
        Toasty.error(getApplicationContext(), appErrorMessage, Toast.LENGTH_LONG, true).show();
    }

    @Override
    public void onSuccess(ImageListResponse response) {
        adapter.removeLoadingFooter();
        isLoading = false;

        if(response!=null){
            if(!response.getImages().isEmpty()){
                adapter.addAll(response);
                adapter.addLoadingFooter();
                recyclerView.setVisibility(View.VISIBLE);
                emptyList.setVisibility(View.GONE);
            }
            else
            {
                showNoListView();
            }
        }
        else
        {
            showNoListView();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem menuitem=  menu.findItem(R.id.action_search);
        search = (SearchView)menuitem. getActionView () ;

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        search.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        if (!TextUtils.isEmpty(currentQuery)) {
            search.clearFocus();
        }

        if( search != null )
        {
            // Inserting user query in SQLite and searching it using server
            search.setOnQueryTextListener (new SearchView . OnQueryTextListener () {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    search.clearFocus();
                    currentQuery=query;
                    currentPage=1;
                    adapter.clear();
                    presenter.getImageList(currentPage, currentQuery);
                    return  true;
                }

                @Override
                public boolean onQueryTextChange(String query)
                {
                    return true ;
                }
            });

            //Registering broadcast receiver
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void showNoListView(){
        isLastPage = true;
        if(currentPage==1){
            recyclerView.setVisibility(View.GONE);
            emptyList.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyList.setVisibility(View.GONE);
        }
    }
}
