package com.example.noteswithsecurity;

import static com.example.noteswithsecurity.Constants.FIRESTORE_MY_NOTES;
import static com.example.noteswithsecurity.Constants.FIRESTORE_NOTES;
import static com.example.noteswithsecurity.Constants.FIRESTORE_NOTE_CONTENT;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseUser mCurrentUser;

    private ArrayAdapter<String> mNotesAdapter;
    private List<DocumentSnapshot> mNoteDocumentSnapshots;

    private ActivityResultLauncher<Intent> mStartForResult
            = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        updateNoteList();
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("Secured Notes");

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mNoteDocumentSnapshots = new ArrayList<>();
        mNotesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());

        final ListView listView = findViewById(R.id.listView);
        listView.setAdapter(mNotesAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final DocumentSnapshot document = mNoteDocumentSnapshots.get(i);
                final Intent newNoteIntent = NoteEditorActivity.newIntent(getApplicationContext(), document.getId());
                mStartForResult.launch(newNoteIntent);
            }
        });

        updateNoteList();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.add_note_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Type here to Search");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mNotesAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_note) {
            // Open NotesEditorActivity to create new note
            final Intent newNoteIntent = NoteEditorActivity.newIntent(this, null);
            mStartForResult.launch(newNoteIntent);
            return true;
        } else if (item.getItemId() == R.id.about) {
            Dialog about = new Dialog(MainActivity.this);
            about.requestWindowFeature(Window.FEATURE_NO_TITLE);
            about.setContentView(R.layout.about);
            about.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            about.show();
            return true;
        }else if (item.getItemId() == R.id.disclaimer){
            Dialog disclaimer = new Dialog(MainActivity.this);
            disclaimer.requestWindowFeature(Window.FEATURE_NO_TITLE);
            disclaimer.setContentView(R.layout.disclaimer);
            disclaimer.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            disclaimer.show();
            return true;
        } else if (item.getItemId() == R.id.location) {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(intent);
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    private void updateNoteList() {
        mFirebaseFirestore.collection(FIRESTORE_NOTES)
                .document(mCurrentUser.getUid())
                .collection(FIRESTORE_MY_NOTES)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isComplete()) {
                        final List<String> newNotes = new ArrayList<>();
                        final List<DocumentSnapshot> newDocuments = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            newDocuments.add(document);
                            newNotes.add(document.getData().get(FIRESTORE_NOTE_CONTENT).toString());
                        }
                        // Hold the document for later reference
                        mNoteDocumentSnapshots.clear();
                        mNoteDocumentSnapshots.addAll(newDocuments);
                        // Update list adapter
                        mNotesAdapter.clear();
                        mNotesAdapter.addAll(newNotes);
                    }
                });
    }
}
