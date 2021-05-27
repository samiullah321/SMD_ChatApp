package com.example.chatapp;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.sql.SQLOutput;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;


    public GroupMessageAdapter (List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;


        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_messsage_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
        }
    }




    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, @SuppressLint("RecyclerView") final int position) {
        //System.out.println("i: "+  i + " " + userMessagesList.get(i).getMessageID());
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get( position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);


        if (fromMessageType.equals("text")) {
            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                String s = "You:" + "\n" + messages.getMessage() + "\n" + messages.getTime() + " - " + messages.getDate();
                SpannableString ss1 = new SpannableString(s);
                ss1.setSpan(new RelativeSizeSpan(0.8f), s.length() - 23, s.length(), 0); // set size
                ss1.setSpan(new ForegroundColorSpan(Color.rgb(179, 181, 181)), s.length() - 23, s.length(), 0);//
                messageViewHolder.senderMessageText.setText(ss1);
            } else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                String s = messages.getName()+":"+ "\n" + messages.getMessage() + "\n" + messages.getTime() + " - " + messages.getDate();
                SpannableString ss1 = new SpannableString(s);
                ss1.setSpan(new RelativeSizeSpan(0.8f), s.length() - 23, s.length(), 0);
                ss1.setSpan(new ForegroundColorSpan(Color.rgb(179, 181, 181)), s.length() - 23, s.length(), 0);// set color// set size
                messageViewHolder.receiverMessageText.setText(ss1);
            }
        } else if (fromMessageType.equals("image")) {
            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);

            } else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);
            }
        } else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {
            if (fromUserID.equals(messageSenderId)) {
                System.out.println("Hello" + userMessagesList.get(position).getMessageID());
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);

                if(fromMessageType.equals("docx")){
                    messageViewHolder.messageSenderPicture.setBackgroundResource(R.drawable.doc);
                }
                else if(fromMessageType.equals("pdf"))
                {
                    messageViewHolder.messageSenderPicture.setBackgroundResource(R.drawable.pdf);
                }

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get( position).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });
            } else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);

                if(fromMessageType.equals("docx")){
                    messageViewHolder.messageReceiverPicture.setBackgroundResource(R.drawable.doc);
                }
                else if(fromMessageType.equals("pdf"))
                {
                    messageViewHolder.messageReceiverPicture.setBackgroundResource(R.drawable.pdf);
                }
                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        System.out.println(userMessagesList.get(position).getMessage());
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get( position).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });
            }
        }

        if (fromUserID.equals(messageSenderId)) {
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (userMessagesList.get(position).getType().equals("image")){

                        Intent intent = new Intent(messageViewHolder.itemView.getContext(),ImageViewerActivity.class);
                        intent.putExtra("url",userMessagesList.get(position).getMessage());
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                    else if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx"))
                    {
                        System.out.println("URL" + userMessagesList.get(position).getMessage());
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                      
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }

                }
            });
            messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete Message",
                                       // "Delete For Everyone",
                                        "Cancel",
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Document");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    deleteSentMessage(position,messageViewHolder);



                                } else if (i == 1) {
                                    deleteMessageForEveryOne(position,messageViewHolder);
                                }
                            }
                        });
                        builder.show();
                    } else if (userMessagesList.get(position).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete Message",
                                        //"Delete For Everyone",
                                        "Cancel",
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    deleteSentMessage(position,messageViewHolder);
//

                                }else if (i == 1) {
                                    deleteMessageForEveryOne(position,messageViewHolder);
                                }
                            }
                        });
                        builder.show();
                    } else if (userMessagesList.get(position).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]
                                {


                                        "Delete Message",
                                        //"Delete For Everyone",
                                        "Cancel",

                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete image");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    deleteSentMessage(position,messageViewHolder);

                                } else if (i == 1) {
                                    deleteMessageForEveryOne(position,messageViewHolder);
                                }
                            }
                        });
                        builder.show();
                    }
                    return false;
                }
            });
        } else {
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (userMessagesList.get(position).getType().equals("image")){
                        Intent intent = new Intent(messageViewHolder.itemView.getContext(),ImageViewerActivity.class);
                        intent.putExtra("url",userMessagesList.get(position).getMessage());
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                    else if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx"))
                    {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }

                }
            });
//            messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")) {
//                        CharSequence options[] = new CharSequence[]
//                                {
//
//                                        "Delete For me",
//                                        "Cancel",
//                                };
//                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
//                        builder.setTitle("Delete Document");
//
//                        builder.setItems(options, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                if (i == 0) {
//                                    deleteReceiverMessage(position,messageViewHolder);
////
//
//                                }
//                            }
//                        });
//                        builder.show();
//                    } else if (userMessagesList.get(position).getType().equals("text")) {
//                        CharSequence options[] = new CharSequence[]
//                                {
//                                        "Delete For me",
//                                        "Cancel"
//                                };
//                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
//                        builder.setTitle("Delete Message");
//
//                        builder.setItems(options, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                if (i == 0) {
//                                    deleteReceiverMessage(position,messageViewHolder);
////
//                                }
//                            }
//                        });
//                        builder.show();
//                    } else if (userMessagesList.get(position).getType().equals("image")) {
//                        CharSequence options[] = new CharSequence[]
//                                {
//
//                                        "Delete For me",
//                                        "Cancel"
//                                };
//                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
//                        builder.setTitle("Delete Image");
//
//                        builder.setItems(options, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                if (i == 0) {
//                                    deleteReceiverMessage(position,messageViewHolder);
//                                }
//                            }
//                        });
//                        builder.show();
//                    }
//                    return false;
//                }
//            });

        }
    }








    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }


    private void deleteSentMessage(final int position, final MessageViewHolder holder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        Messages temp = userMessagesList.get(position);
        temp.setMessage("Message Deleted by " + userMessagesList.get(position).getName());
        temp.setType("text");
        System.out.println(userMessagesList.get(position).getFrom());
        System.out.println(userMessagesList.get(position).getTo());
        System.out.println(userMessagesList.get(position).getMessageID());
        rootRef.child("Groups")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .setValue(temp).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(),"Deleted Successfully.",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(),"Error Occurred.",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void deleteReceiverMessage(final int position, final MessageViewHolder holder)
    {
        
    }
    private void deleteMessageForEveryOne(final int position, final MessageViewHolder holder)
    {
        Messages temp = userMessagesList.get(position);
        temp.setMessage("Message Deleted by" + userMessagesList.get(position).getName());
        temp.setType("text");
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Groups")

                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .setValue(temp).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    rootRef.child("Groups")
                            .child(userMessagesList.get(position).getTo())

                            .child(userMessagesList.get(position).getMessageID())
                            .setValue(temp).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(holder.itemView.getContext(),"Message Deleted.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(),"Error Occurred.",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



}
