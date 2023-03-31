package com.example.librarian;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.librarian.Book;
import com.example.librarian.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import coil.Coil;
import coil.request.ImageRequest;

public class ReservedBooksAdapter extends RecyclerView.Adapter<ReservedBooksAdapter.ViewHolder> {
    private static List<Book> books;
    private Context context;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public ReservedBooksAdapter(List<Book> books, Context context) {
        this.books = books;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_display_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Book book = books.get(position);
        holder.title.setText(book.getTitle());
        holder.author.setText(book.getAuthor());
        ImageRequest request = new ImageRequest.Builder(holder.myImage.getContext()).data(book.getImageLink())
                .target(holder.myImage).build();
        Coil.imageLoader(holder.myImage.getContext()).enqueue(request);
        holder.confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date now = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");
                String timestamp = formatter.format(now);

                String bookId = books.get(position).getId();
                String bookedBy = books.get(position).getBookedBy();

                FirebaseFirestore.getInstance()
                        .collection("Books")
                        .whereEqualTo("id", bookId)
                        .whereEqualTo("bookedBy", bookedBy)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    DocumentSnapshot bookDocument = queryDocumentSnapshots.getDocuments().get(0);
                                    String bookDocumentId = bookDocument.getId();

                                    FirebaseFirestore.getInstance()
                                            .collection("Books")
                                            .document(bookDocumentId)
                                            .update("confirmedAt", timestamp)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(v.getContext(), "Confirmed", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(v.getContext(), "Error confirming: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    Toast.makeText(v.getContext(), "Error confirming: book not found or not booked by this user", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(v.getContext(), "Error confirming: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public void setBooks(List<Book> books) {
        this.books = books;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView title, author;
        private ImageView myImage;

        Button confirmButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.book_title);
            author = itemView.findViewById(R.id.book_author);
            myImage = itemView.findViewById(R.id.book_cover);

            confirmButton = itemView.findViewById(R.id.confirm);
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Clicked", Toast.LENGTH_SHORT).show();
                    long timestamp = System.currentTimeMillis();
                    String bookId = books.get(getAdapterPosition()).getId();

                    FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(bookId)
                            .update("lastClicked" + bookId, timestamp)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(itemView.getContext(), "Confirmed", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(itemView.getContext(), "Error confirming: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        }
    }
}