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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.safaorhan.reunion.FirestoreHelper;
import com.safaorhan.reunion.R;
import com.safaorhan.reunion.model.Conversation;
import com.safaorhan.reunion.model.Message;
import com.safaorhan.reunion.model.User;

public class ConversationAdapter extends FirestoreRecyclerAdapter<Conversation, ConversationAdapter.ConversationHolder> {
    private static final String TAG = ConversationAdapter.class.getSimpleName();
    private ConversationClickListener conversationClickListener;

    private ConversationAdapter(@NonNull FirestoreRecyclerOptions<Conversation> options) {
        super(options);
    }

    private ConversationClickListener getConversationClickListener() {
        if (conversationClickListener == null) {
            conversationClickListener = new ConversationClickListener() {
                @Override
                public void onConversationClick(DocumentReference documentReference) {
                    Log.e(TAG, "You need to call setConversationClickListener() to set the click listener of ConversationAdapter");
                }
            };
        }
        return conversationClickListener;
    }

    public void setConversationClickListener(ConversationClickListener conversationClickListener) {
        this.conversationClickListener = conversationClickListener;
    }

    public static ConversationAdapter get() {
        Query query = FirebaseFirestore.getInstance()
                .collection("conversations")
                //.orderBy()
                .whereEqualTo(FirestoreHelper.getMe().getId(), true)
                .limit(50);
        FirestoreRecyclerOptions<Conversation> options = new FirestoreRecyclerOptions.Builder<Conversation>()
                .setQuery(query, Conversation.class)
                .build();
        return new ConversationAdapter(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ConversationHolder holder, int position, @NonNull Conversation conversation) {
        conversation.setId(getSnapshots().getSnapshot(position).getId());
        holder.bind(conversation);
    }

    @NonNull
    @Override
    public ConversationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ConversationHolder(itemView);
    }

    class ConversationHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView opponentNameText;
        TextView lastMessageText;
        ImageView coloredCircleImageView;

        private User opponentUser = null;
        private Message lastMessage = null;
        String userFirstLetter;
        String userEmail;
        ColorGenerator colorGenerator = ColorGenerator.MATERIAL;
        TextDrawable coloredCircleDrawable;

        ConversationHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            opponentNameText = itemView.findViewById(R.id.opponentNameText);
            lastMessageText = itemView.findViewById(R.id.lastMessageText);
            coloredCircleImageView = itemView.findViewById(R.id.coloredCircleImageView);
        }

        private void bind(final Conversation conversation) {
            itemView.setVisibility(View.GONE);
            if (opponentUser == null) {
                conversation.getOpponent().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        opponentUser = documentSnapshot.toObject(User.class);
                        opponentNameText.setText(opponentUser != null ? opponentUser.getName() : null);
                        userFirstLetter = opponentUser.getName().substring(0, 1).toUpperCase();
                        userEmail = opponentUser.getEmail();
                        coloredCircleDrawable = TextDrawable.builder().buildRound(userFirstLetter, colorGenerator.getColor(userEmail));
                        coloredCircleImageView.setImageDrawable(coloredCircleDrawable);
                        itemView.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                opponentNameText.setText(opponentUser.getName());
                coloredCircleImageView.setImageDrawable(coloredCircleDrawable);
                itemView.setVisibility(View.VISIBLE);
            }
            if (lastMessage == null) {
                if (conversation.getLastMessage() != null) {
                    conversation.getLastMessage().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            lastMessage = documentSnapshot.toObject(Message.class);
                            lastMessageText.setText(lastMessage != null ? lastMessage.getText() : null);
                        }
                    });
                } else {
                    lastMessageText.setText(R.string.missingLastMessageReplacerText);
                }
            } else {
                lastMessageText.setText(lastMessage.getText());
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getConversationClickListener().onConversationClick(FirestoreHelper.getConversationRef(conversation));
                }
            });
        }
    }

    public interface ConversationClickListener {
        void onConversationClick(DocumentReference conversationRef);
    }
}