package com.fhtrier.voiceDiary;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

@SuppressLint("NewApi") 
public class DialogPasswordPermission extends DialogFragment {

	EditText password;
	
    static boolean sSomeValue;
    private static ArrayList<PasswordDialogFragmentListener> listeners;


	public interface PasswordDialogFragmentListener {
        public void onReturnValue(String foo);
        public void onAbort();
    }

    public void init(boolean someValue)
    {
        sSomeValue = someValue;
        listeners = new ArrayList<PasswordDialogFragmentListener>();
    }
    
    public void addMyDialogFragmentListener(PasswordDialogFragmentListener l)
    {
        listeners.add(l);
    }

    public void removeMyDialogFragmentListener(PasswordDialogFragmentListener l)
    {
        listeners.remove(l);
    }
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		setCancelable(false);
		LayoutInflater li = LayoutInflater.from(getActivity());
		View promptsView = li.inflate(R.layout.password_dialog, null);
		builder.setView(promptsView);
		builder.setTitle(getActivity().getString(R.string.ask_password));
		
		this.password = (EditText) promptsView.findViewById(R.id.get_admin_password);
		
		builder.setPositiveButton(getActivity().getString(R.string.button_enter) , (new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				PasswordDialogFragmentListener activity = (PasswordDialogFragmentListener) getActivity();
				activity.onReturnValue(DialogPasswordPermission.this.password.getText().toString());
				DialogPasswordPermission.this.hideKeyboard();				
			}
		}));
		builder.setNegativeButton(getActivity().getString(R.string.button_cancel) , (new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				PasswordDialogFragmentListener activity = (PasswordDialogFragmentListener) getActivity();
				activity.onAbort();
			}
		}));
			return builder.create();
	}	
	
	public void hideKeyboard()
	{
		InputMethodManager imm = (InputMethodManager)this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.password.getWindowToken(),0);
	}

}