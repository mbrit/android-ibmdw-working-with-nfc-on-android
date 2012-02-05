package com.mbrit.AndroidNfc;

import java.io.IOException;
import java.util.Arrays;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AndroidNfcActivity extends Activity implements OnClickListener 
{
	private NfcAdapter _adapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
   
        // get...
        getButtonGo().setOnClickListener(this);

    }
    
    @Override
    public void onResume() 
    {
        super.onResume();
        try
        {
			_adapter = NfcAdapter.getDefaultAdapter(this);
			if(_adapter == null)
				throw new Exception("No adapter was returned.");
			
			// create a pending indent..
			PendingIntent pending = PendingIntent.getActivity(this, 0,
	                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		    // Setup an intent filter for all MIME based dispatches
	        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
	        try {
	            ndef.addDataType("*/*");
	        } catch (MalformedMimeTypeException e) {
	            throw new RuntimeException("fail", e);
	        }
	        IntentFilter[] mFilters = new IntentFilter[] {
	                ndef,
	        };

	        // Setup a tech list for all NfcF tags
	        String[][] mTechLists = new String[][] { new String[] { NfcF.class.getName() } };
			
	        _adapter.enableForegroundDispatch(this, pending, mFilters, mTechLists);	   
	        
	        // say that we're waiting...
	        Log.i("NFC", "Now waiting for tags...");
        }
        catch(Exception ex)
        {
        	MessageBox.showError(this, ex);
        }  
    }
    
    @Override
    public void onNewIntent(Intent intent) 
    {
    	Ndef openTag = null;
    	try
    	{
	        Log.i("NFC", "Received notification of a new tag...");

	        // get...
	        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	        if(tag == null)
	        	throw new Exception("No tag was returned.");
	        
	        // decode the tag as an NDEF technology tag...
	        Ndef data = Ndef.get(tag);
	        if(data == null)
	        	throw new Exception("No data was returned.");
	        
	        // output header information...
	        Log.i("NFC", String.format("Tag type: %s", data.getType()));
	        Log.i("NFC", String.format("Tag isWritable: %b", data.isWritable()));

	        // next, connect to the tag, and flag that we've done do...
	        data.connect();
	        openTag = data;

	        // now the message... this is the current message on the tag...
	        NdefMessage message = data.getNdefMessage();
	        if(message == null)
	        	throw new Exception("No message was returned.");
	        
	        // get the records...
	        NdefRecord[] records = message.getRecords();
	        Log.i("NFC", String.format("Num records: %d", records.length));
	        for(int i = 0; i < records.length; i++)
	        {
	        	NdefRecord record = records[i];
	        	
	        	// build up a string containing the payload representation...
		        StringBuilder builder = new StringBuilder();
		        byte[] payload = record.getPayload();
		        for(int j = 0; j < payload.length; j++)
		        {
		        	if(j > 0 && j % 32 == 0)
		        		builder.append("\n");
		        	builder.append(String.format("%02x", payload[j])); // dump out the hex
		        	builder.append(" ");
		        }
		        
		        // dump out the bytes...
		        Log.i("NFC", String.format("Record #%d, payload %d bytes", i, payload.length));
		        Log.i("NFC", builder.toString());
		        
		        // "Type Name Format"...
		        short tnf = record.getTnf();  
		        Log.i("NFC", String.format("Record TNF: %d", tnf)); 
		        
		        // create our record...
		        ManagedTag result = null;
		        
		        // is it text?
		        if(tnf == NdefRecord.TNF_WELL_KNOWN)
		        {
		        	Log.i("NFC", "Is a well known record...");

			        // check the "RTD" (Record Type Definition)...
			        byte[] rtd = record.getType();
			        if(Arrays.equals(rtd, NdefRecord.RTD_TEXT))
	        		{
			        	// we have a text value...
			        	result = new ManagedTextTag(payload);
	        		}
			        else
			        	Log.i("NFC", "Is not a known type - ignoring.");
		        }
		        else
		        	Log.i("NFC", "Is not a well known record - ignoring.");
		        
		        // render out our results...
		        if(result != null)
		        	Log.i("NFC", String.format("DECODE RESULT: %s", result.toString()));
		        else
		        	Log.i("NFC", "DECODE RESULT: (null)");
	        }
    	}
    	catch(Exception ex)
    	{
    		MessageBox.showError(this, ex);
    	}
    	finally
    	{
    		// if we opened a tag, close it...
    		if(openTag != null)
    		{
				try 
				{
					openTag.close();
				} 
				catch (IOException ex)
				{
					// no-op - ignore problems closing...
				}
    		}
    	}
    }
    
    public Button getButtonGo()
    {
    	return (Button)findViewById(R.id.buttonGo);
    }

	public void onClick(View v) 
	{
		if(v.getId() == R.id.buttonGo)
			handleGo();
	}
	
	private void handleGo() 
	{
		try
		{
			if(_adapter != null)
			{
				_adapter.disableForegroundDispatch(this);
				_adapter = null;
				
				// log...
				Log.i("NFC", "Closed down NFC dispatch.");
			}
		}
		catch(Exception ex)
		{
			MessageBox.showError(this, ex);
		}
	}
}