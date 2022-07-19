package com.example.noteswithsecurity;

import static com.example.noteswithsecurity.Constants.FIRESTORE_MY_NOTES;
import static com.example.noteswithsecurity.Constants.FIRESTORE_NOTES;
import static com.example.noteswithsecurity.Constants.FIRESTORE_NOTE_CONTENT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NoteEditorActivity extends AppCompatActivity {

    private static final String TAG = "NoteEditorActivity";

    private static final String EXTRA_NOTE_ID = "note_id";

    public static Intent newIntent(Context context, String noteId) {
        Intent starter = new Intent(context, NoteEditorActivity.class);
        starter.putExtra(EXTRA_NOTE_ID, noteId);
        return starter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        final EditText txtNote = findViewById(R.id.editText);
        final String editNoteId = getIntent().getStringExtra(EXTRA_NOTE_ID);

        if (editNoteId != null && !editNoteId.isEmpty()) {
            getSupportActionBar().setTitle("Edit Note");
            firestore.collection("notes")
                    .document(currentUser.getUid())
                    .collection("myNotes")
                    .document(editNoteId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isComplete()) {
                        txtNote.setText(task.getResult().getData().get("content").toString());
                    }
                }
            });
        } else {
            getSupportActionBar().setTitle("Add Note");
        }

        findViewById(R.id.savenote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String note = txtNote.getText().toString();
                if (note.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter data before saving", Toast.LENGTH_SHORT).show();
                } else {
                    DocumentReference newDocumentRef;
                    if (editNoteId != null && !editNoteId.isEmpty()) {
                        newDocumentRef = firestore
                                .collection(FIRESTORE_NOTES)
                                .document(currentUser.getUid())
                                .collection(FIRESTORE_MY_NOTES)
                                .document(editNoteId);
                    } else {
                        newDocumentRef = firestore
                                .collection(FIRESTORE_NOTES)
                                .document(currentUser.getUid())
                                .collection(FIRESTORE_MY_NOTES)
                                .document();
                    }

                    final Map<String, String> data = new HashMap<>();
                    data.put(FIRESTORE_NOTE_CONTENT, note);

                    newDocumentRef.set(data)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getApplicationContext(), "Note Saved", Toast.LENGTH_SHORT).show();
                                    setResult(Activity.RESULT_OK);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Failed to save note", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.close_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.closeButton) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}