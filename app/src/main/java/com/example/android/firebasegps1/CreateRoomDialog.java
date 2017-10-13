package com.example.android.firebasegps1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This may be entirely unnecessary
 */

public class CreateRoomDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Make the dialogBuilder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        //lets init the editText here 1st, then in the builder setup
        //add characteristics
        builder.setView(inflater.inflate(R.layout.dialog_layout, null))
                .setMessage("Create ChatRoom")
                .setTitle("Make a new room")
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //TODO:CREATE ROOM
                        Toast.makeText(getActivity(), "ChatRoomCreated", Toast.LENGTH_SHORT).show();

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //TODO:CANCEL
                        Toast.makeText(getActivity(), "Creation Canceled", Toast.LENGTH_SHORT).show();
                    }
                });
        return builder.create();
    }
}
