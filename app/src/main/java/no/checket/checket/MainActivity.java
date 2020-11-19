package no.checket.checket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        // Interface for communication with NewTaskFragment
        NewTaskFragment.NewTaskDialogListener {

    private IntroSlideManager mIntroSlideManager;

    BottomAppBar main_BottomAppBar;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Menu navigationMenu;

    private TextView txtV_email;
    private MenuItem MI_LoginReg;

    // Recycler view
    private LinkedList<no.checket.checket.Task> mTaskList = new LinkedList<>();
    private RecyclerView mRecyclerView;
    private TaskListAdapter mAdapter;

    // Firebase, declare instance
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase, initialize the instance
        mAuth = FirebaseAuth.getInstance();

        // Start by checking if this is the first launch, decides which view to show
        mIntroSlideManager = new IntroSlideManager(this);
        if(mIntroSlideManager.isFirstTime()) {
            // If first time, launch the intro slider

            // If user exits out of the app while active, assume this was by mistake, don't consider the intro finished
            //introSlideManager.setFirstTime(false);

            startActivity(new Intent(this, IntroSlideActivity.class));
            finish();
        } else {
            // This is not the first time the app has been launched, continue as normal
            setContentView(R.layout.activity_main);

            drawerLayout = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);
            navigationMenu = navigationView.getMenu();

            ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                    this,
                    drawerLayout,
                    main_BottomAppBar,
                    R.string.openNavDrawer,
                    R.string.closeNavDrawer
            );

            drawerLayout.addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();

            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId())
                    {
                        case R.id.nav_LoginReg:
                            // User has selected to login / logout
                            if(mAuth.getCurrentUser() != null) {
                                // User is currently logged in, show logout dialog
                                new MaterialAlertDialogBuilder(MainActivity.this).setTitle(R.string.dialog_logout_title).setMessage(R.string.dialog_logout_msg)
                                    .setNegativeButton(R.string.dialog_logout_neg, null)
                                    .setPositiveButton(R.string.dialog_logout_pos, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            // User has confirmed that they want to log out
                                            mAuth.signOut();
                                            // Reload the activity once we've signed out the user
                                            finish();
                                            startActivity(getIntent());
                                        }
                                    }).show();
                            } else {
                                // TODO
                                // Starts the LoginRegisterActivity, Switch case with putExtra to determine which layout we're showing?
                                Intent intent = new Intent(MainActivity.this, LoginRegisterActivity.class);
                                startActivity(intent);
                            }
                            break;
                        case R.id.nav_settings:
                            // Starts the SettingsActivity
                            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                            startActivity(intent);
                            break;

                        case R.id.nav_profile:
                            //Starts the ProfileActivity
                            Intent intentP = new Intent(MainActivity.this, ProfileActivity.class);
                            startActivity(intentP);
                            break;

                        case R.id.nav_achievements:
                            // Starts the AchievementsActivity
                            Intent intentAch = new Intent(MainActivity.this, AchievementsActivity.class);
                            startActivity(intentAch);
                            break;

                    }
                    return false;
                }
            });

            main_BottomAppBar = findViewById(R.id.main_BottomAppBar);

            main_BottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });

            main_BottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch(menuItem.getItemId()) {
                        case R.id.main_BottomAppBar_tasks:
                            // Open tasks
                            Intent intentTasks = new Intent(MainActivity.this, TasksActivity.class);
                            startActivity(intentTasks);
                            break;
                        case R.id.main_BottomAppBar_ach:
                            // Open achievements
                            Intent intentAch = new Intent(MainActivity.this, AchievementsActivity.class);
                            startActivity(intentAch);
                            break;
                    }
                    return true;
                }
            });
        }

        // Fill mTaskList
        fillTaskList();
        // Call the method to initialize and inflate the recycler
        recyclerView();
    }

    public void onStart() {
        super.onStart();

        // Check if a user is currently signed in, update UI
        // A function to update the UI accordingly, (Logout / Sign in / Register)
        FirebaseUser currentUser = mAuth.getCurrentUser();

        txtV_email = navigationView.getHeaderView(0).findViewById(R.id.nav_email);
        MI_LoginReg = navigationMenu.findItem(R.id.nav_LoginReg);

        if(currentUser != null) {
            txtV_email.setText(currentUser.getEmail());
            MI_LoginReg.setTitle("Logout");
        } else {
            MI_LoginReg.setTitle("Login / Register");
        }
    }

    public void fillTaskList () {
        // RecyclerView
        // Populate list
        // TODO: Get list from DB
        // NB! The year, month, etc. constructor is deprecated
        mTaskList.add(new no.checket.checket.Task("Social", "Drinks with colleagues", new Date(61565866200000L), "ic_misc"));
        mTaskList.add(new no.checket.checket.Task("TEST", "Vacuuming", new Date(61565866200000L), "ic_add"));
        mTaskList.add(new no.checket.checket.Task("Exercise", "30 minute cardio", new Date (2020, 11, 14, 21, 30), "ic_add"));
        mTaskList.add(new no.checket.checket.Task("Cleaning", "Vacuuming", new Date (2020, 11, 19, 21, 30), "ic_add"));
        mTaskList.add(new no.checket.checket.Task("Miscellaneous", "Pick dad up at the airport", new Date (2020, 12, 21, 20, 30), "ic_misc"));
        mTaskList.add(new no.checket.checket.Task("Cleaning", "Vacuuming", new Date (2020, 11, 5, 21, 30), "ic_add"));
        mTaskList.add(new no.checket.checket.Task("Sports", "Football in the park", new Date (2020, 10, 22, 20, 0), "ic_sports"));
        mTaskList.add(new no.checket.checket.Task("Cleaning", "Vacuuming", new Date (2020, 12, 28, 12, 30), "ic_add"));
        mTaskList.add(new no.checket.checket.Task("Cleaning", "Vacuuming", new Date (2020, 12, 28, 13, 30), "ic_add"));
        mTaskList.add(new no.checket.checket.Task("Cleaning", "Vacuuming", new Date (2020, 12, 28, 14, 30), "ic_add"));
        mTaskList.add(new no.checket.checket.Task("Cleaning", "Vacuuming. This is getting psychotic...", new Date (2020, 12, 28, 15, 30), "ic_add"));
        mTaskList.add(new no.checket.checket.Task("Cleaning", "Vacuuming", new Date (2020, 12, 28, 16, 30), "ic_add"));
        mTaskList.add(new no.checket.checket.Task("Cleaning", "Vacuuming", new Date (2020, 12, 28, 17, 30), "ic_add"));
        mTaskList.add(new no.checket.checket.Task("Cleaning", "Vacuuming", new Date (2020, 12, 28, 18, 30), "ic_add"));
        mTaskList.add(new no.checket.checket.Task("Cleaning", "Vacuuming. Apartment's REAAALLY clean now.", new Date (2020, 12, 28, 19, 30), "ic_add"));
        mTaskList.add(new no.checket.checket.Task("Cleaning", "Vacuuming. Here we go again.", new Date (2020, 12, 28, 21, 30), "ic_add"));
        mTaskList.add(new no.checket.checket.Task("Cleaning", "Vacuuming", new Date (2020, 12, 28, 21, 30), "ic_add"));

    }

    public void recyclerView() {
        // Sort list
        Collections.sort(mTaskList, new Comparator<no.checket.checket.Task>() {
            @Override
            public int compare(final no.checket.checket.Task object1, final no.checket.checket.Task object2) {
                return object1.getDate().compareTo(object2.getDate());
            }
        });
        // Get a handle to the RecyclerView.
        mRecyclerView = findViewById(R.id.coming_tasks);
        // Specify the length of the list for this activity
        // This lets us use the same TaskListAdapter class for multiple activities showing different lengths.
        int length = 6;
        // Create an adapter and supply the data to be displayed.
        mAdapter = new TaskListAdapter(this, mTaskList, length);
        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    
    public void newTask(View view) {
        // User has clicked the FAB
        DialogFragment dialog = new NewTaskFragment();
        dialog.show(getSupportFragmentManager(), "newTaskFragment");
    }

    // Listener for clicking of the save button
    // Had to create these from an error dialog when implementing the interface,
    // just to get the overrides right.
    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String header, String details, Date date, String icon) {
        Task task = new Task(header, details, date, icon);
        // Add the new task to the list
        int index = 0;
        if (!header.equals("")) {
            mTaskList.add(index, task);
            // TODO: Upload new Task to DB
            Log.i("Petter", header + ", " + details + ", " + icon);
            // Calling the function to refresh the RecyclerView
            recyclerView();
        } else {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_LONG).show();
            // TODO: Unsure whether this is the right view to give
            newTask(drawerLayout);
        }

    }

    // ... or the cancel button
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Log.i("Petter", "MainActivity.onNegativeDialogClick()");
    }

    // Used for accessing a time picker in the new task dialog
    public void showTimePickerFragment(View view) {
        DialogFragment time = new NewTaskFragment.TimePickerFragment();
        time.show(getSupportFragmentManager(), "timePickerFragment");
    }

    // Same as above, for a date picker
    public void showDatePickerFragment(View view) {
        DialogFragment newFragment = new NewTaskFragment.DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }




}