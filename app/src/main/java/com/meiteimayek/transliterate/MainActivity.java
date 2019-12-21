package com.meiteimayek.transliterate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
  
  private AppBarConfiguration mAppBarConfiguration;
  
  @BindView(R.id.toolbar)
  Toolbar mToolbar;
  @BindView(R.id.drawer_layout)
  DrawerLayout mDrawer;
  @BindView(R.id.nav_view)
  NavigationView mNavView;
  
  private NavController mNavController;
  
  private long mLastPressed = 0;
  private boolean mIsHome = true;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    initViews();
  }
  
  private void initViews() {
    setSupportActionBar(mToolbar);
    mAppBarConfiguration = new AppBarConfiguration.Builder(
      R.id.nav_home, R.id.nav_privacy, R.id.nav_about)
      .setDrawerLayout(mDrawer)
      .build();
    
    mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
    NavigationUI.setupActionBarWithNavController(this, mNavController, mAppBarConfiguration);
    NavigationUI.setupWithNavController(mNavView, mNavController);
    
    mNavView.getMenu()
      .findItem(R.id.nav_store)
      .setOnMenuItemClickListener(item -> {
        Intent intent = new Intent(Intent.ACTION_VIEW,
          Uri.parse(getString(R.string.play_store_detail) + BuildConfig.APPLICATION_ID));
        startActivity(Intent.createChooser(intent, "Open with..."));
        return false;
      });
    
    mNavView.getMenu()
      .findItem(R.id.nav_share)
      .setOnMenuItemClickListener(item -> {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        
        share.putExtra(Intent.EXTRA_SUBJECT, "Check this out!");
        share.putExtra(Intent.EXTRA_TEXT,
          getString(R.string.play_store_detail) + BuildConfig.APPLICATION_ID);
        
        startActivity(Intent.createChooser(share, "Share link!"));
        return false;
      });
    
    mNavController.addOnDestinationChangedListener(
      (controller, destination, arguments) -> mIsHome = (destination.getId() == R.id.nav_home)
    );
  }
  
  @Override
  public boolean onSupportNavigateUp() {
    return NavigationUI.navigateUp(mNavController, mAppBarConfiguration)
      || super.onSupportNavigateUp();
  }
  
  @Override
  public void onBackPressed() {
    if(!mIsHome) {
      super.onBackPressed();
      return;
    }
    
    if(mLastPressed > (System.currentTimeMillis() - 2000)) {
      super.onBackPressed();
      return;
    }
    
    Toast.makeText(this, "Press again to EXIT!", Toast.LENGTH_SHORT).show();
    mLastPressed = System.currentTimeMillis();
  }
}
