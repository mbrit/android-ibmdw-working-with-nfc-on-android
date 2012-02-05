package com.mbrit.AndroidNfc;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;

public class MessageBox 
{
	public static void showInfo(Context context, String message) 
	{
		Builder builder = new Builder(context);
		builder.setMessage(message);
		builder.setPositiveButton("OK", null);
		builder.create().show();
	}

	public static void showError(Context context, Exception ex)
	{
		showInfo(context, ex.toString());
	}
}
