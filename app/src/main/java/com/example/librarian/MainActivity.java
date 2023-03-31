package com.example.librarian;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final ArrayList<Book> books = new ArrayList<>();
    private final ArrayList<Book> booked = new ArrayList<>();
    private View loading;
    private View container;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference reference = FirebaseFirestore.getInstance().collection("Books");
    private ListenerRegistration dbListener;
    private ListenerRegistration userListener;
    private Button button;
    private RecyclerView recyclerView;
    private ReservedBooksAdapter adapter;
    private List<Book> reservedBooks;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loading = findViewById(R.id.booked_loading);
        container = findViewById(R.id.booked_container);
        recyclerView = findViewById(R.id.booked_recyclerview);
        reservedBooks = new ArrayList<>();
        adapter = new ReservedBooksAdapter(reservedBooks, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        setUpFirestore();
    }

    private void setUpFirestore() {
        loading.setVisibility(View.VISIBLE);
        dbListener = db.collection("Books")
                .whereEqualTo("isBooked", true)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error Fetching Books", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                        return;
                    }
                    if (snapshots != null) {
                        List<Book> bookedBooks = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Book book = doc.toObject(Book.class);
                            bookedBooks.add(book);
                        }
                        adapter.setBooks(bookedBooks);
                    }
                    loading.setVisibility(View.GONE);
                });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userListener = FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(user.getUid())
                    .addSnapshotListener((snapshots, error) -> {
                        if (error != null) {
                            error.printStackTrace();
                            return;
                        }

                        if (snapshots != null) {
                            List<String> reservedBookIds = (List<String>) snapshots.get("reserved");
                            db.collection("Books")
                                    .whereIn("id", reservedBookIds)
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            List<Book> reservedBooks = new ArrayList<>();
                                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                                Book book = doc.toObject(Book.class);
                                                reservedBooks.add(book);
                                            }
                                            adapter.setBooks(reservedBooks);
                                        } else {
                                            Log.d(TAG, "Error getting documents: ", task.getException());
                                        }
                                    });
                        }
                    });
        }
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(MainActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
        long timestamp = System.currentTimeMillis();
        String bookId = (String) view.getTag();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(userId)
                .update("reserved." + bookId, timestamp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Confirmed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error confirming: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}