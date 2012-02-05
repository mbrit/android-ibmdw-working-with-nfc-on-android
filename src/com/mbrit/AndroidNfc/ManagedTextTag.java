package com.mbrit.AndroidNfc;

import java.io.UnsupportedEncodingException;

public class ManagedTextTag extends ManagedTag
{
	private String _encoding;
	private String _language;
	private String _text;
	
	public ManagedTextTag(byte[] payload) throws UnsupportedEncodingException
	{
		// what encoding are we using? if bit 7 of the zeroth byte is 0, it's UTF-8, 
		// otherwise UTF-16...
		_encoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
	
		// bits 5-7 of the zeroth byte is the length of the language code, which
		// follows immediately on from the zeroth byte... (and the code itself will
		// always be in US-ASCII, which we provide as the last param to the String
		// constructor)...
		int languageLength = payload[0] & 0077;
        _language = new String(payload, 1, languageLength, "US-ASCII");
	
        // the remainder of the string is therefore the data...
        _text = new String(payload, languageLength + 1, payload.length - (languageLength + 1), 
        		_encoding);
	}
	
	public String getLanguage()
	{
		return _language;
	}

	public String getEncoding()
	{
		return _encoding;
	}

	public String getText()
	{
		return _text;
	}
	
	@Override
	public String toString()
	{
		return String.format("Text: %s (language: %s, encoding: %s)", getText(), getLanguage(), getEncoding());
	}
}
