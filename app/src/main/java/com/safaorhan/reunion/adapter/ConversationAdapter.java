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
import com.google.firebase.firestore.QuerySnapshot;
import com.safaorhan.reunion.FirestoreHelper;
import com.safaorhan.reunion.R;
import com.safaorhan.reunion.model.Conversation;
import com.safaorhan.reunion.model.Message;
import com.safaorhan.reunion.model.User;
public class ConversationAdapter extends FirestoreRecyclerAdapter<Conversation, ConversationAdapter.ConversationHolder> {
    private static final String TAG = ConversationAdapter.class.getSimpleName();
    private ConversationClickListener conversationClickListener;
    private DataChangedListener dataChangedListener;

    private ConversationAdapter(@NonNull FirestoreRecyclerOptions<Conversation> options, ConversationClickListener conversationClickListener, final DataChangedListener dataChangedListener) {
        super(options);
        setConversationClickListener(conversationClickListener);
        setDataChangedListener(dataChangedListener);
        FirebaseFirestore.getInstance()
                .collection("conversations")
                .whereEqualTo(FirestoreHelper.getMe().getId(), true)
                .limit(1)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                if (snapshots == null || snapshots.isEmpty() || snapshots.size() < 1)
                    getDataChangedListener().doWhenDataIsEmpty();
            }
        });
    }


    public static ConversationAdapter get(ConversationClickListener conversationClickListener, DataChangedListener dataChangedListener) {
        Query query = FirebaseFirestore.getInstance()
                .collection("conversations")
                .whereEqualTo(FirestoreHelper.getMe().getId(), true)
                .limit(50);
        FirestoreRecyclerOptions<Conversation> options = new FirestoreRecyclerOptions.Builder<Conversation>()
                .setQuery(query, Conversation.class)
                .build();
        return new ConversationAdapter(options, conversationClickListener, dataChangedListener);
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

    private void setConversationClickListener(ConversationClickListener conversationClickListener) {
        this.conversationClickListener = conversationClickListener;
    }

    private DataChangedListener getDataChangedListener() {
        if (dataChangedListener == null) {
            dataChangedListener = new DataChangedListener() {
                @Override
                public void doWhenDataIsViable() {
                    Log.e(TAG, "You need to call setDataChangedListener() to set the click listener of ConversationAdapter");
                }
                @Override
                public void doWhenDataIsEmpty() {
                }
            };
        }
        return dataChangedListener;
    }

    private void setDataChangedListener(DataChangedListener dataChangedListener) {
        this.dataChangedListener = dataChangedListener;
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
            // implement an image

        }
        private void bind(final Conversation conversation) {
            itemView.setVisibility(View.GONE);
            if (conversation == null || conversation.getLastMessage() == null) {
                if (conversation.getLastMessage() == null){
                    FirebaseFirestore.getInstance().collection("conversations").document(conversation.getId()).delete();
                }
                return;
            }
            conversation.getOpponent().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        opponentUser = documentSnapshot.toObject(User.class);
                        opponentUser.setId(documentSnapshot.getId());
                        if (opponentUser != null && opponentUser.getId() != null && !opponentUser.getId().isEmpty()) {
                            opponentNameText.setText(opponentUser.getName());
                            userFirstLetter = opponentUser.getName().substring(0, 1).toUpperCase();
                            userEmail = opponentUser.getEmail();
                            coloredCircleDrawable = TextDrawable.builder().buildRound(userFirstLetter, colorGenerator.getColor(userEmail));
                            coloredCircleImageView.setImageDrawable(coloredCircleDrawable);
                            conversation.getLastMessage().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    lastMessage = documentSnapshot.toObject(Message.class);
                                    if (lastMessageText.getText() != null) {
                                        lastMessageText.setText(lastMessage.getText());
                                        itemView.setVisibility(View.VISIBLE);
                                        getDataChangedListener().doWhenDataIsViable();

                                    }
                                }
                            });
                        }
                    }
                }
            });

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

    public interface DataChangedListener {
        void doWhenDataIsViable();
        void doWhenDataIsEmpty();
    }
}