package com.safaorhan.reunion.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.safaorhan.reunion.FirestoreHelper;
import com.safaorhan.reunion.R;
import com.safaorhan.reunion.model.Message;
import com.safaorhan.reunion.model.User;

public class MessageAdapter extends FirestoreRecyclerAdapter<Message, MessageAdapter.MessageHolder> {
    private static final String TAG = MessageAdapter.class.getSimpleName();
    private DataChangingListener dataChangingListener = null;

    private MessageAdapter(@NonNull FirestoreRecyclerOptions<Message> options) {
        super(options);
    }

    public static MessageAdapter get(DocumentReference conversationRef) {
        Query query = FirebaseFirestore.getInstance()
                .collection("messages")
                .whereEqualTo("conversation", conversationRef)
                .orderBy("sentAt");

        FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message.class)
                .build();
        return new MessageAdapter(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull MessageHolder holder, int position, @NonNull Message message) {
        holder.bind(message);
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageHolder(itemView);
    }

    public DataChangingListener getDataChangingListener() {
        if (dataChangingListener == null) {
            dataChangingListener = new DataChangingListener() {
                @Override
                public void doWhenDataChange() {
                    Log.e(TAG, "You have to set Listener before you can use it.");
                }
            };
        }
        return dataChangingListener;
    }

    public void setDataChangingListener(DataChangingListener dataChangingListener) {
        this.dataChangingListener = dataChangingListener;
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        getDataChangingListener().doWhenDataChange();
    }

    class MessageHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView senderTextView;
        TextView messageTextView;
        ImageView sentImageView;
        ImageView deliveredImageView;

        MessageHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            senderTextView = itemView.findViewById(R.id.senderTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            sentImageView = itemView.findViewById(R.id.sentImageView);
            deliveredImageView = itemView.findViewById(R.id.deliveredImageView);
        }

        void bind(final Message message) {
            itemView.setVisibility(View.GONE);
            if (message.getFrom() == null || message.getConversation() == null) {
                return;
            }
            messageTextView.setText(message.getText());
            if (message.getFrom().getId().equals(FirestoreHelper.getMe().getId())) {
                senderTextView.setText(R.string.meIsMessageSender);
            } else {
                FirestoreHelper.getUserByRef(message.getFrom(), new FirestoreHelper.GetUserByRefCallback() {
                    @Override
                    public void onCompleted(User user) {
                        senderTextView.setText(user.getName());
                    }
                });
            }
            if (!message.getFrom().getId().equals(FirestoreHelper.getMe().getId())){
                FirestoreHelper.messageDelivered(message, getSnapshots().getSnapshot(getAdapterPosition()).getId());
                deliveredImageView.setVisibility(View.GONE);
                sentImageView.setVisibility(View.GONE);
            }else if (message.getFrom().getId().equals(FirestoreHelper.getMe().getId())){
                if (message.getDelivered()){
                    deliveredImageView.setVisibility(View.VISIBLE);
                }else {
                    deliveredImageView.setVisibility(View.GONE);
                }
                sentImageView.setVisibility(View.VISIBLE);
            }
            itemView.setVisibility(View.VISIBLE);
        }
    }

    public interface DataChangingListener {
        void doWhenDataChange();
    }
}