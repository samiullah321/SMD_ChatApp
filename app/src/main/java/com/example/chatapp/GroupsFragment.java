package com.example.chatapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment
{
    private FirebaseAuth mAuth;
    private View groupFragmentView;
    private ListView list_view;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_groups = new ArrayList<>();
    private List<String> memberList = new ArrayList<>();
    private DatabaseReference GroupRef;
    private String currentUserID;

    DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");






        RetrieveAndDisplayGroups();
        IntializeFields();



        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                String currentGroupName = adapterView.getItemAtPosition(position).toString();

                Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
                groupChatIntent.putExtra("groupName" , currentGroupName);
                startActivity(groupChatIntent);
            }
        });


        return groupFragmentView;
    }



    private void IntializeFields()
    {
        list_view = (ListView) groupFragmentView.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, list_of_groups);
        list_view.setAdapter(arrayAdapter);
    }




    private void RetrieveAndDisplayGroups()
    {
        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();

                while (iterator.hasNext())
                {
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }

                for(String name: set){
                    RootRef.child("GroupsMembers").child(name).child("members")
                            .addValueEventListener(new ValueEventListener()  {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot)
                                {
                                    System.out.println("data1"+dataSnapshot);
                                    if(dataSnapshot.exists())
                                    {
                                        GenericTypeIndicator<ArrayList<String>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<String>>() {};

                                        memberList = (ArrayList<String>)dataSnapshot.getValue(genericTypeIndicator);
                                        System.out.println("gname" + name + " " + memberList.toString());
                                        System.out.println("uid" + currentUserID + " " +set.toString());
                                        if(!memberList.contains(currentUserID)){
                                            System.out.println("here");
                                            set.remove(name);
                                            list_of_groups.clear();
                                            System.out.println("uid1" + currentUserID + " " +set.toString());
                                            list_of_groups.addAll(set);

                                            arrayAdapter.notifyDataSetChanged();
                                        }
                                        else{
                                            list_of_groups.clear();
                                            System.out.println("uid1" + currentUserID + " " +set.toString());
                                            list_of_groups.addAll(set);

                                            arrayAdapter.notifyDataSetChanged();
                                            System.out.println("uid" + currentUserID + " " +set.toString());
                                        }

                                    }



                                    //System.out.println("i: "+  messages.getType() + " " + messages.getMessageID());


                                }



                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }

                            });
                }




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
