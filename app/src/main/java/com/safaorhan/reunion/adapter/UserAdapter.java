package com.safaorhan.reunion.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.safaorhan.reunion.FirestoreHelper;
import com.safaorhan.reunion.R;
import com.safaorhan.reunion.model.User;

public class UserAdapter extends FirestoreRecyclerAdapter<User, UserAdapter.UserHolder> {
    private static final String TAG = UserAdapter.class.getSimpleName();
    private UserClickListener userClickListener;

    private UserAdapter(@NonNull FirestoreRecyclerOptions<User> options) {
        super(options);
    }

    private UserClickListener getUserClickListener() {
        if (userClickListener == null) {
            userClickListener = new UserClickListener() {
                @Override
                public void onUserClick(DocumentReference userRef) {
                    Log.e(TAG, "You need to call setUserClickListener() to set the click listener of UserAdapter");
                }
            };
        }
        return userClickListener;
    }

    public void setUserClickListener(UserClickListener userClickListener) {
        this.userClickListener = userClickListener;
    }

    public static UserAdapter get() {
        Query query = FirestoreHelper.getUsers()
                //.orderBy("timestamp")
                .limit(50);
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();
        return new UserAdapter(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull UserHolder holder, int position, @NonNull User user) {
        user.setId(getSnapshots().getSnapshot(position).getId());
        holder.bind(user);
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserHolder(itemView);
    }

    class UserHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView nameText;
        TextView emailText;
        ImageView coloredCircleImageView;
        ColorGenerator colorGenerator = ColorGenerator.MATERIAL;
        TextDrawable coloredCircleDrawable;

        UserHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            nameText = itemView.findViewById(R.id.nameText);
            emailText = itemView.findViewById(R.id.emailText);
            coloredCircleImageView = itemView.findViewById(R.id.coloredCircleImageView);
        }

        void bind(final User user) {
            nameText.setText(String.format("%s %s", user.getName(), user.getSurname()));
            emailText.setText(user.getEmail());
            coloredCircleDrawable = TextDrawable.builder().buildRound(user.getName().substring(0, 1).toUpperCase(), colorGenerator.getColor(user.getEmail()));
            coloredCircleImageView.setImageDrawable(coloredCircleDrawable);
            if (user.getId().equals(FirebaseAuth.getInstance().getUid())) {
                itemView.setOnClickListener(null);
            } else {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getUserClickListener().onUserClick(FirestoreHelper.getUserRef(user));
                    }
                });
            }
        }
    }

    public interface UserClickListener {
        void onUserClick(DocumentReference userRef);
    }
}
