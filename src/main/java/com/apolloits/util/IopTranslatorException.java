package com.apolloits.util;


/**
 * @author DK
 * Base Exception for the IOPTranslator
 */
public class IopTranslatorException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public IopTranslatorException()
	{
		super();
	}
	public IopTranslatorException(String message)
	{
		super(message);
	}
	public IopTranslatorException(String message, Throwable cause)
	{
		super(message,cause);
	}
	public IopTranslatorException (Throwable cause)
	{
		super(cause);
	}
	/*public IopTranslatorException(Exception exception,String processName, String attributeType, String attributeValue, DatabaseLogger databaseLogger) {
		if(exception!=null && exception.getMessage()!=null)
			exception.printStackTrace();
		//databaseLogger.addPMMSHUBAlert(exception, processName, attributeType, attributeValue);
	}*/

}
