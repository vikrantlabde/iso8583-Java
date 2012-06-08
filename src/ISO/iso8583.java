/**
 * @purpose ISO8583 Message Pack/Unpack 
 * @reference http://en.wikipedia.org/wiki/ISO_8583
 *
 * @author Vikrant <vikrant@cuelogic.co.in>
 * @author Sagar <sagar@cuelogic.co.in>
 * @version 1.0
 */

package ISO;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class iso8583 {
	
	public static HashMap<String, String> ISOSCHEMA = new HashMap<String, String>();
	public static HashMap<String, String> ISOMESSAGE = new HashMap<String, String>();
	public static HashMap<String, String> PARSEDISOMESSAGE = new HashMap<String, String>();
	public static HashMap<Integer, ArrayList<Integer>> SUBFIELDSMAPPING = new HashMap<Integer, ArrayList<Integer>>();
	public static boolean ISSUBFIELDPARSING = false;
	public static String SUBFIELDID = "";
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		/*
		 ==========
		 DATATYPES
		 ==========
		 CHAR -> Variable Length String
		 FCHAR -> Fixed Length String
		 NUMERIC -> Fixed Lenth Integer
		 NUM -> Varible Length Number
		
		*/
		
	   /* isoSchema.put("<field-id","<field-data-type>-<field-size>-<field-max-length>-<has-subfields_<has-bitmap>>-<Field Name>"); */
		
		ISOSCHEMA.put("1","BITMAP");
		ISOSCHEMA.put("4","NUM-2-10-0_0");
		ISOSCHEMA.put("9","NUMERIC-0-8-0_0-RADA");
		ISOSCHEMA.put("18","FCHAR-1-4-0_0");
		ISOSCHEMA.put("52","CHAR-2-64-0_0");
		ISOSCHEMA.put("57","CHAR-2-50-0_0");
		ISOSCHEMA.put("58","CHAR-2-99-0_0");
		ISOSCHEMA.put("65","NUMERIC-1-1-0_0");
		ISOSCHEMA.put("100","NUM-2-8-0_0");
		ISOSCHEMA.put("101","CHAR-2-99-0_0");
		ISOSCHEMA.put("114","CHAR-4-9999-1_1");
			ISOSCHEMA.put("114.1","BITMAP");
			ISOSCHEMA.put("114.7","CHAR-2-99-0_0");
			ISOSCHEMA.put("114.10","NUM-2-5-0_0");
			ISOSCHEMA.put("114.23","CHAR-2-20-0_0");
			ISOSCHEMA.put("114.24","CHAR-2-20-0_0");
			ISOSCHEMA.put("114.65","NUMERIC-0-1-0_0");
			ISOSCHEMA.put("114.117","CHAR-2-99-0_0");
			ISOSCHEMA.put("114.150","CHAR-2-99-0_0");

		ISOSCHEMA.put("120","CHAR-4-9999-1_1");
			ISOSCHEMA.put("120.1","BITMAP");
			ISOSCHEMA.put("120.64","CHAR-2-99-0_0");
			ISOSCHEMA.put("120.120","CHAR-2-99-0_0");

		ISOSCHEMA.put("130","CHAR-2-90-0_0");
		
			
		//Packing ISO MESSAGE
		HashMap <String, String> isofields = new HashMap<String, String>();
		isofields.put("4", "10");
		isofields.put("9", "99999999");
		isofields.put("18", "V");
		isofields.put("100", "3");
		isofields.put("101", "Cuelogic Technologies, India");
		isofields.put("130", "California");
		isofields.put("114.10", "99");
		isofields.put("114.117", "Steve Jobs");
		isofields.put("114.150", "Shravya Vikrant Labde");

		isofields.put("120.64", "Vikrant Labde is building ISO8583 message library for Sub Fields");
		isofields.put("120.120", "Here parsing one more subfield");
				
				
	    //TESTING: ISOMESSAGE PACKING
		String isoMessage = packIsoMsg("1001",isofields);
		System.out.println("Packed iso8583 Message: "+isoMessage+"\n");
		
		
		//Sends data to server
		//-------- // networkTransport(isoMessage);
		
		
		//TESTING: ISOMESSAGE UNPACKING
		unpackIsoMsg(isoMessage);
		
		System.out.println("Unpacked iso8583 Message" + PARSEDISOMESSAGE);
		
	}

    /**
     * This method packs the message into ISO8583 standards. 
     * This method requires HASHMAP representation as an input
     * 
     * @param MTI
     * @param isofields
     * @return String
     * @throws Exception
     */
	public static String packIsoMsg(String MTI, HashMap <String, String> isofields) throws Exception
	{
		Set<?> set = isofields.entrySet();
		Iterator<?> i = set.iterator(); 
		ArrayList<String> keys = new ArrayList<String>();
		HashMap<String, ArrayList<Integer>> subFiledBitMapHolder = new HashMap<String, ArrayList<Integer>>();  
		
		//Lets start building the ISO Message
		ISOMESSAGE.put("MTI", MTI);
		
		while(i.hasNext()) { 
			
		   @SuppressWarnings("rawtypes")
		   Map.Entry me = (Map.Entry)i.next();
				
		   String fields = me.getKey().toString();
		   
		   keys.add(fields);
	       
	       //Get Schema of fields
	       String dataType = null;
		   String fieldLenType = null;
		   String fieldMaxLen = null;
		   String subfieldIndicator = null;
		   String hasSubfield = null;
		   @SuppressWarnings("unused")
		   String hasSubfieldBitmap = null;
	       
		   String breakFieldForSubField[] = null;
	       String baseField = null;  
	       Integer subField = null;
	       
	       
	       try{
		       breakFieldForSubField = fields.split("[.]");
		       baseField = breakFieldForSubField[0].toString();
		       subField = Integer.parseInt(breakFieldForSubField[1]);
		       try{
		    	   subFiledBitMapHolder.get(baseField).add(subField);
		       }
		       catch(Exception e)
		       {
		    	   ArrayList<Integer> arrSubFields = new ArrayList<Integer>();
		    	   arrSubFields.add(subField);
		    	   subFiledBitMapHolder.put(baseField, arrSubFields);
		       }
		       
		   }catch(Exception e)
	       {
			   //Nothing to do here
	       }
	       
	       
	       String schema = ISOSCHEMA.get(fields);
	       String arrSchema[] = schema.split("-");
	       dataType = arrSchema[0];
	       fieldLenType = arrSchema[1];
	       fieldMaxLen = arrSchema[2];
	       subfieldIndicator = arrSchema[3];
	       String arrSubField[] = subfieldIndicator.split("_");
	       
	       hasSubfieldBitmap = arrSubField[1];
	       
	       if(dataType.equalsIgnoreCase("NUM") && fieldLenType.equalsIgnoreCase("1"))
	       {
	    	   throw new IOException("Field:" +fields + " has data type NUM is having field-size = 1 in ISOSCHEMA. Try assign NUMERIC data type");
	       }
	       
    	   String fieldVlaue = isofields.get(fields);
    	   Integer fieldLength = fieldVlaue.length();
    	   
    	   String strfieldLength = fieldLength.toString();
    	   
    	   if(dataType.equalsIgnoreCase("NUMERIC"))
    	   {   
    		   if(Integer.parseInt(fieldMaxLen) >= fieldLength) 
    		   {
    			   String newFieldValue = "";
    			   if (fieldVlaue.equals(""))
    			   {
    				   newFieldValue = String.format("%0"+ fieldMaxLen +"d", 0);
    			   }
    			   else if (fieldLength == Integer.parseInt(fieldMaxLen))
    			   {
    				   newFieldValue = fieldVlaue;
    			   }
    			   else
    			   {
    				   newFieldValue = String.format("%0"+ fieldMaxLen +"d", Long.parseLong(fieldVlaue));
    			   }
            	   
    			   ISOMESSAGE.put(fields, newFieldValue);
    		   }
    		   else
    		   {
    			   throw new IOException("Field:"+fields + " Has bigger value. Its set "+fieldMaxLen +" in ISOSCHEMA and you have entered" + fieldLength );
    		   }
    	   }
    	   else if (dataType.equalsIgnoreCase("CHAR") || dataType.equalsIgnoreCase("NUM"))
    	   {
    		   if(Integer.parseInt(fieldMaxLen) >= fieldLength)
    		   {
    			   String newFieldLen = String.format("%0"+ fieldLenType +"d", Long.parseLong(strfieldLength));
    			   
    			   /////
	    		   ISOMESSAGE.put(fields.toString(), newFieldLen + fieldVlaue);
	    		   ////
    		   }
    		   else
    		   {
    			   throw new IOException("Field:"+fields + " Has bigger value. Its set "+fieldMaxLen +" in ISOSCHEMA and you have entered" + fieldLength );
    		   }
    	   }
    	   else if (dataType.equalsIgnoreCase("FCHAR"))
    	   {
    		   if(Integer.parseInt(fieldMaxLen) >= fieldLength)
    		   {
    			   String newFieldValue = String.format("%-"+ fieldMaxLen +"s", fieldVlaue);
    			   
    			   /////
	    		   ISOMESSAGE.put(fields.toString(),  newFieldValue);
	    		   ////
    		   }
    		   else
    		   {
    			   throw new IOException("Field:"+fields + " Has bigger value. Its set "+fieldMaxLen +" in ISOSCHEMA and you have entered" + fieldLength );
    		   }
    	   }
    	   else if (dataType.equalsIgnoreCase("PCHAR"))
    	   {
    		   if(Integer.parseInt(fieldMaxLen) >= fieldLength)
    		   {
    			   String newFieldValue = String.format("%"+ fieldMaxLen +"s", fieldVlaue);
    			   newFieldValue = newFieldValue.replaceAll(" ", "0");
    			   /////
	    		   ISOMESSAGE.put(fields.toString(),  newFieldValue);
	    		   ////
    		   }
    		   else
    		   {
    			   throw new IOException("Field:"+fields + " Has bigger value. Its set "+fieldMaxLen +" in ISOSCHEMA and you have entered" + fieldLength );
    		   }
    	   }
	  }
	    
	  //Process Bitmap - Add 1 and 65 number Data Elements
	  ArrayList<Integer> finalFields = new ArrayList<Integer>();	
	  finalFields = processBitmap(parseFields(keys)); 
	  
	  //Process Subfield bitmap - Add 1 and 65 for each field that has subfields
	  processSubFieldBitmap(subFiledBitMapHolder);
	  
	  //Assemble the ISO8583 message
	  return buildISOMessage(finalFields);
	   
	}
	
	/**
	 * This method assembles the entire ISO Message in a String
	 * @param finalFields
	 * @return String
	 * @throws Exception
	 */
	public static String buildISOMessage(ArrayList<Integer> finalFields) throws Exception
	{
		
		String isoMessage = ISOMESSAGE.get("MTI");
		isoMessage = isoMessage + ISOMESSAGE.get("1");
		
		Iterator<?> j = finalFields.iterator();
		while(j.hasNext())
		{
			   String dataElement = j.next().toString();	
			   String schema = ISOSCHEMA.get(dataElement);
			   
			   try{
				   String arrSchema[] = schema.split("-");
				   
			       String fieldLenType = arrSchema[1];
				   
				   String subfieldIndicator = arrSchema[3];
				   String arrSubField[] = subfieldIndicator.split("_");
			       String hasSubfield = arrSubField[0];
		       		  
			       if(hasSubfield.equalsIgnoreCase("1"))
			       {
			    	   //Traverse subfields
			    	  ArrayList <Integer> subFields = new ArrayList<Integer>();
			    	  subFields = SUBFIELDSMAPPING.get(Integer.parseInt(dataElement));
			    	  
			    	  Iterator<?> i = subFields.iterator();
			    	  String isoSubMessage = "";
			    	 
			    	  if(!subFields.contains(1))
			    		  isoSubMessage = isoSubMessage + ISOMESSAGE.get(dataElement + ".1");
			    	  
			 		  while(i.hasNext())
			 		  {
			 			  String subDataElement = i.next().toString();
			 			  String mainDataElement = dataElement + "." + subDataElement;
			 			  isoSubMessage = isoSubMessage + ISOMESSAGE.get(mainDataElement);
			 		  }
			 		  
			 		
		    		  String subMessageLen = String.format("%0"+ fieldLenType +"d", isoSubMessage.length());
		      		  isoMessage = 	isoMessage + subMessageLen + isoSubMessage; 
			       }
			       else
			       {
			    	   
			    	   isoMessage = isoMessage + ISOMESSAGE.get(dataElement);
			       }
			       
			   }catch(Exception e)
			   {
				   //System.out.println(" has problem with schema");
			   }
		}
		
		return isoMessage;
	}
	
	/**
	 * Create array list of all mail Data Elements that are available. This method is used in method packIsoMsg
	 * @param fields
	 * @return ArrayList
	 * @throws Exception
	 */
	public static ArrayList<Integer> parseFields(ArrayList<String> fields) throws Exception
	{
		
		ArrayList<Integer> newFiledMap = new ArrayList<Integer>();
		Iterator<?> j = fields.iterator();
		  while(j.hasNext())
		  {
			  try{
				  String breakFieldForSubField[] = j.next().toString().split("[.]");
			      Integer baseField = Integer.parseInt(breakFieldForSubField[0].toString());
			      
			      if(!newFiledMap.contains(baseField))
			      {
			    	  newFiledMap.add(baseField);
			      }
			  }catch(Exception e)
			  {
				  Integer keys = Integer.parseInt(j.next().toString());
				  newFiledMap.add(keys);
			  }
		  }
		
		//Sort fields in assending order
		Collections.sort(newFiledMap);    
		return newFiledMap;
	}
	
	/**
	 * This is helper method used in the packIsoMsg method to process BITMAP
	 * @param fields
	 * @return Arraylist
	 * @throws Exception
	 */
	public static ArrayList<Integer> processBitmap(ArrayList<Integer> fields) throws Exception
	{
		String bitmapType = "primary";
		
		//Retrive heighest Data Element from the list
		Integer DE = fields.get(fields.size() - 1);
		
		//Know the type of bitmap (primary, secondary, tertiary
		if(DE > 65 && DE <= 128)
		{
			fields.add(1); //Add 1st Bit for secondary bitmap
			bitmapType = "secondary";
		}
		else if(DE > 128)
		{
			fields.add(1); //Add 1st Bit for secondary bitmap
			fields.add(65); //Add 65th Bit for tertiary bitmap
			ISOMESSAGE.put("65", "1");
			bitmapType = "tertiary";
		}
		else
		{
			bitmapType = "primary";
		}
		
		//Sort ArrayList again 
		Collections.sort(fields);
		
		//Append BITMAP to the message
		ISOMESSAGE.put("1", CalcBitMap(fields, bitmapType));
		
		return fields;
	}
	
	
	/**
	 * This is helper method used in the packIsoMsg method. To process SubFields
	 * @param subFieldMap
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static void processSubFieldBitmap(HashMap<String, ArrayList<Integer>> subFieldMap) throws Exception
	{
		Set<?> set = subFieldMap.entrySet();
		Iterator<?> i = set.iterator(); 
		while(i.hasNext()) {
			 
			@SuppressWarnings("rawtypes")
			Map.Entry me = (Map.Entry)i.next();
				
			ArrayList <Integer> fields = new ArrayList<Integer>(); 
			fields = (ArrayList<Integer>) me.getValue();
			String subFieldId = me.getKey().toString();
			
			Collections.sort(fields); //Sort the arrayList
		
			String bitmapType = "primary";
			
			//Retrive heighest Data Element from the list
			Integer DE = fields.get(fields.size() - 1);
			
			//Know the type of bitmap (primary, secondary, tertiary
			if(DE > 65 && DE <= 128)
			{
				fields.add(1); //Add 1st Bit for secondary bitmap
				bitmapType = "secondary";
			}
			else if(DE > 128)
			{
				fields.add(1); //Add 1st Bit for secondary bitmap
				fields.add(65); //Add 65th Bit for tertiary bitmap
				ISOMESSAGE.put(subFieldId + ".65", "1");
				bitmapType = "tertiary";
			}
			else
			{
				//fields.add(1); //Add 1st Bit for secondary bitmap
				bitmapType = "primary";
			}
			//Sort ArrayList again 
			Collections.sort(fields);
			
			//Maintain Subfield Mapping
			SUBFIELDSMAPPING.put(Integer.parseInt(subFieldId), fields);
			
			//Append BITMAP to the message
		 	ISOMESSAGE.put(subFieldId+".1", CalcBitMap(fields, bitmapType));
		}
	}
	
	/**
	 * This function uppack/Parse ISO8583 encoded message and stores the output in HASHMAP -> PARSEDISOMESSAGE
	 * @param isoMessage
	 * @throws Exception
	 */
	public static void unpackIsoMsg(String isoMessage) throws Exception
	{
		 String overallBitmap = null;
		 
		 String messageAfterBitMap = null;
		 
		 if(ISSUBFIELDPARSING == false)
			 PARSEDISOMESSAGE.put("MTI",isoMessage.substring(0, 4));
		 
		 String priMaryHexBitMap = "";
		 if(ISSUBFIELDPARSING == false)
			 priMaryHexBitMap = isoMessage.substring(4, 12);
		 else
			 priMaryHexBitMap = isoMessage.substring(0, 8);
		 
		 //Convert BITMAP to Binary
		 String primaryBitMap = GetBitMap(priMaryHexBitMap);
		 overallBitmap = primaryBitMap;
		 
		 //Check if Secondary bitMap is available or not
		 Integer firstBit = Integer.parseInt(primaryBitMap.substring(0, 1));
		 
		 //if firstBit = 1 it means secondary bitmap is available
		 String secondaryHexBitmap = null;
		 String secondaryBitMap = null;
		 String tertiaryHexBitmap = null;
		 String tertiaryBitMap = null;
		 int bitmaplength = 64; 
		 
		 if(firstBit > 0)
		 {
			bitmaplength = 128;
			if(ISSUBFIELDPARSING == false)
				secondaryHexBitmap = isoMessage.substring(12, 20);
			else
				secondaryHexBitmap = isoMessage.substring(8, 16);
			
			secondaryBitMap = GetBitMap(secondaryHexBitmap);
			overallBitmap = overallBitmap + secondaryBitMap;
			
			//if 65th field is binary then there is a tertiary bitmap
			Integer firstBitOfSecBitmap = Integer.parseInt(secondaryBitMap.substring(0, 1));
			if(firstBitOfSecBitmap > 0)
			{
				 bitmaplength = 192;
				 if(ISSUBFIELDPARSING == false)
					 tertiaryHexBitmap = isoMessage.substring(20, 28);
				 else
					 tertiaryHexBitmap = isoMessage.substring(16, 24);
				 
				 tertiaryBitMap = GetBitMap(tertiaryHexBitmap);
				 overallBitmap = overallBitmap + tertiaryBitMap;
				 if(ISSUBFIELDPARSING == false)
					 messageAfterBitMap = isoMessage.substring(28); //After MTI and Primary bitmap
				 else
					 messageAfterBitMap = isoMessage.substring(24); //After MTI and Primary bitmap
			}
			else
			{
				if(ISSUBFIELDPARSING == false)
					messageAfterBitMap = isoMessage.substring(20); //After MTI and Primary bitmap
				else
					messageAfterBitMap = isoMessage.substring(16); //After MTI and Primary bitmap
			}
		 }
		 else //Secondary bitmap is not available so remaining message is actual data
		 {
			 if(ISSUBFIELDPARSING == false)
				 messageAfterBitMap = isoMessage.substring(12); //After MTI and Primary bitmap
			 else
				 messageAfterBitMap = isoMessage.substring(8); //After MTI and Primary bitmap
		 }
		 
		 //Traverse the overall bitmap string
		 ArrayList<Integer> debugList = new ArrayList<Integer>(); //This is just for debugging purpose
		 
		 String remainingMessage = null;
		 for(int i=0;i<bitmaplength;i++)
		 {
			 //now figure out which fields are available
			 char bit =  overallBitmap.charAt(i);
			 if(bit == '1')
			 {
				 debugList.add(i+1); //This is just for debugging purpose.
				 Integer field = i+1;
				 
				 String dataType = null;
				 String fieldLenType = null;
				 String fieldMaxLen = null;
				 String subfieldIndicator = null;
				 String hasSubfield = null;
				 String hasSubfieldBitmap = null;
				 if(field > 1) //Exclude 1st Field which is reserve for bitmap
			     {
					   String schema = null;
					   try{
						   schema = ISOSCHEMA.get(SUBFIELDID+field.toString());
					   }catch(Exception e)
					   {
						   throw new IOException(field + " has problem with schema.");
					   }
					   
					   try{
					       String arrSchema[] = schema.split("-");
					       
					       dataType = arrSchema[0];
					       fieldLenType = arrSchema[1];
					       fieldMaxLen = arrSchema[2];
					       subfieldIndicator = arrSchema[3];
					       String arrSubField[] = subfieldIndicator.split("_");
					       hasSubfield = arrSubField[0];
					       hasSubfieldBitmap = arrSubField[1];
					   }catch(Exception e)
					   {
						   throw new IOException(field + " has problem with schema.");
					   }
				      
				       //chala todat todat javoo
				       if(dataType.equalsIgnoreCase("NUMERIC"))
			    	   {
				    	   if(remainingMessage == null)
				    	   {
				    		   String fieldValue = messageAfterBitMap.substring(0,Integer.parseInt(fieldMaxLen));
				    		   
				    		   if(fieldValue == null)
				    		   {
				    			   throw new IOException(field + " Has null or inappropriate value");
				    		   }
				    		   
				    		   if(hasSubfield.equalsIgnoreCase("1"))
						       {
						    	   ISSUBFIELDPARSING = true;
						    	   SUBFIELDID = field+".";
						    	   unpackIsoMsg(fieldValue);
						       }
						       else
						       {
						    	    PARSEDISOMESSAGE.put(SUBFIELDID+field.toString(), fieldValue); //Lets start pushing parsed fields into Hash Map
						       }
				    		   remainingMessage = messageAfterBitMap.substring(Integer.parseInt(fieldMaxLen));
				    	   }
				    	   else //operation on remainingMessage
				    	   {
				    		   String fieldValue = remainingMessage.substring(0,Integer.parseInt(fieldMaxLen));
				    		   
				    		   if(fieldValue == null)
				    		   {
				    			   throw new IOException(field + " Has null or inappropriate value");
				    		   }
				    		   
				    		   if(hasSubfield.equalsIgnoreCase("1"))
						       {
						    	   ISSUBFIELDPARSING = true;
						    	   SUBFIELDID = field+".";
						    	   unpackIsoMsg(fieldValue);
						       }
						       else
						       {
						    	    PARSEDISOMESSAGE.put(SUBFIELDID+field.toString(), fieldValue); //Lets start pushing parsed fields into Hash Map
						       }
				    		   remainingMessage = remainingMessage.substring(Integer.parseInt(fieldMaxLen));
				    	   }
			    	   }
				       else if(dataType.equalsIgnoreCase("FCHAR"))
			    	   {
				    	   if(remainingMessage == null)
				    	   {
				    		   String fieldValue = messageAfterBitMap.substring(0,Integer.parseInt(fieldMaxLen)).trim();
				    		   
				    		   if(fieldValue == null)
				    		   {
				    			   throw new IOException(field + " Has null or inappropriate value");
				    		   }
				    		   
				    		   if(hasSubfield.equalsIgnoreCase("1"))
						       {
						    	   ISSUBFIELDPARSING = true;
						    	   SUBFIELDID = field+".";
						    	   unpackIsoMsg(fieldValue);
						       }
						       else
						       {
						    	    PARSEDISOMESSAGE.put(SUBFIELDID+field.toString(), fieldValue); //Lets start pushing parsed fields into Hash Map
						       }
				    		   remainingMessage = messageAfterBitMap.substring(Integer.parseInt(fieldMaxLen));
				    	   }
				    	   else //operation on remainingMessage
				    	   {
				    		   String fieldValue = remainingMessage.substring(0,Integer.parseInt(fieldMaxLen)).trim();
				    		   
				    		   if(fieldValue == null)
				    		   {
				    			   throw new IOException(field + " Has null or inappropriate value");
				    		   }
				    		   
				    		   if(hasSubfield.equalsIgnoreCase("1"))
						       {
						    	   ISSUBFIELDPARSING = true;
						    	   SUBFIELDID = field+".";
						    	   unpackIsoMsg(fieldValue);
						       }
						       else
						       {
						    	    PARSEDISOMESSAGE.put(SUBFIELDID+field.toString(), fieldValue); //Lets start pushing parsed fields into Hash Map
						       }
				    		   remainingMessage = remainingMessage.substring(Integer.parseInt(fieldMaxLen));
				    	   }
			    	   }
				       else if(dataType.equalsIgnoreCase("PCHAR"))
			    	   {
				    	   if(remainingMessage == null)
				    	   {
				    		   String fieldValue = messageAfterBitMap.substring(0,Integer.parseInt(fieldMaxLen)).replaceFirst("^0+(?!$)", "");
				    		   
				    		   if(fieldValue == null)
				    		   {
				    			   throw new IOException(field + " Has null or inappropriate value");
				    		   }
				    		   
				    		   if(hasSubfield.equalsIgnoreCase("1"))
						       {
						    	   ISSUBFIELDPARSING = true;
						    	   SUBFIELDID = field+".";
						    	   unpackIsoMsg(fieldValue);
						       }
						       else
						       {
						    	    PARSEDISOMESSAGE.put(SUBFIELDID+field.toString(), fieldValue); //Lets start pushing parsed fields into Hash Map
						       }
				    		   remainingMessage = messageAfterBitMap.substring(Integer.parseInt(fieldMaxLen));
				    	   }
				    	   else //operation on remainingMessage
				    	   {
				    		   String fieldValue = remainingMessage.substring(0,Integer.parseInt(fieldMaxLen)).replaceFirst("^0+(?!$)", "");
				    		   
				    		   if(fieldValue == null)
				    		   {
				    			   throw new IOException(field + " Has null or inappropriate value");
				    		   }
				    		   
				    		   if(hasSubfield.equalsIgnoreCase("1"))
						       {
						    	   ISSUBFIELDPARSING = true;
						    	   SUBFIELDID = field+".";
						    	   unpackIsoMsg(fieldValue);
						       }
						       else
						       {
						    	    PARSEDISOMESSAGE.put(SUBFIELDID+field.toString(), fieldValue); //Lets start pushing parsed fields into Hash Map
						       }
				    		   remainingMessage = remainingMessage.substring(Integer.parseInt(fieldMaxLen));
				    	   }
			    	   }
				       else if(dataType.equalsIgnoreCase("CHAR") || dataType.equalsIgnoreCase("NUM"))
			    	   {
			    		   if(remainingMessage == null)
				    	   {
			    			   String fieldlength = messageAfterBitMap.substring(0,Integer.parseInt(fieldLenType));
			    			   remainingMessage = messageAfterBitMap.substring(Integer.parseInt(fieldLenType));
			    			   
			    			   String fieldValue = remainingMessage.substring(0,Integer.parseInt(fieldlength));
			    			   if(fieldValue == null)
				    		   {
				    			   throw new IOException(field + " Has null or inappropriate value");
				    		   }
			    			   
			    			   if(hasSubfield.equalsIgnoreCase("1"))
						       {
						    	   ISSUBFIELDPARSING = true;
						    	   SUBFIELDID = field+".";
						    	   unpackIsoMsg(fieldValue);
						       }
						       else
						       {
						    	    PARSEDISOMESSAGE.put(SUBFIELDID+field.toString(), fieldValue); //Lets start pushing parsed fields into Hash Map
						       }
						    	
			    			   remainingMessage = remainingMessage.substring(Integer.parseInt(fieldlength));
			    			   
				    	   }
			    		   else //operation on remaining message
			    		   {
			    			   String fieldlength = remainingMessage.substring(0,Integer.parseInt(fieldLenType));
			    		
			    			   remainingMessage = remainingMessage.substring(Integer.parseInt(fieldLenType));
			    			   
			    			   String fieldValue = remainingMessage.substring(0,Integer.parseInt(fieldlength));
			    			   if(fieldValue == null)
				    		   {
				    			   throw new IOException(field + " Has null or inappropriate value");
				    		   }
			    			   
			    			   if(hasSubfield.equalsIgnoreCase("1"))
						       {
						    	   ISSUBFIELDPARSING = true;
						    	   SUBFIELDID = field+".";
						    	   unpackIsoMsg(fieldValue);
						       }
						       else
						       {
						    	    PARSEDISOMESSAGE.put(SUBFIELDID+field.toString(), fieldValue); //Lets start pushing parsed fields into Hash Map
						       }
			    			   
			    			   remainingMessage = remainingMessage.substring(Integer.parseInt(fieldlength));
			    		   }
			    	   }
			       }
			 }
		 }
		 
		 ISSUBFIELDPARSING = false;
	     SUBFIELDID = "";
	}


	/**
	 * This Method generates the binary representation of the BITMAP.
	 * @input= Arraylist for the subfields present 
	 * @return String containing bitmap msg
	 * @throws Exception 
	 * 
	 */
	public static String CalcBitMap(ArrayList<Integer> list, String type) throws Exception
	{
		String strBitmap = "0000000000000000000000000000000000000000000000000000000000000000";
		String bitmap = null;
		if(type.equalsIgnoreCase("primary"))
			 bitmap=strBitmap;
		else if (type.equalsIgnoreCase("secondary"))
			 bitmap=strBitmap + strBitmap;
		else if((type.equalsIgnoreCase("tertiary")))
			bitmap=strBitmap + strBitmap + strBitmap;
		else
			bitmap=strBitmap;
		
		for(int i=0;i<list.size();i++)
		{
			int pos=0;
			char a = '1'; 
			pos=list.get(i);
			bitmap=replaceCharAt(bitmap, pos-1, a);
		}
		
		String fullHex="";
		String hexString = "";
		for(int y=0; y<bitmap.length();y=y+4)
		{
			 String toHex = bitmap.substring(y,y+4);
			 int i= Integer.parseInt(toHex,2);
			 hexString = Integer.toHexString(i);
			 fullHex = fullHex+hexString;
		}
		
		String fullHexAscii="";
		
		byte[] arrBytes = new byte[(fullHex.length()/2)];
		
	    for (int i = 0, j=0; i < fullHex.length(); i+=2, j++) {
	        String str = fullHex.substring(i, i+2);

	        arrBytes[j] = (byte)Integer.parseInt(str, 16);
	    	
	        fullHexAscii = fullHexAscii + (char)Integer.parseInt(str, 16);
	    }
	    
		return fullHexAscii;
	 }

	/**
	 * This method calculates the BITMAP of available fields in the message.
	 * It converts the binary representation into Hex representation
	 * and then Hex representation into ACSCII and binds to the ISO8583 message
	 * @param bin
	 * @return
	 * @throws Exception
	 */
	public static String GetBitMap(String bin) throws Exception  
	{  
		String[]hex={"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};  
		String[]binary={"0000","0001","0010","0011","0100","0101","0110","0111","1000","1001","1010","1011","1100","1101","1110","1111"};  
		  
		String originalMsg="";
	    for (int x = 0; x < bin.length(); x++) {
	    	int value = (int)bin.charAt(x);
	    	String originalValue = Integer.toHexString(value);
	
	    	if (originalValue.length() < 2)
	    		originalMsg = originalMsg + "0";
	    	
	    	if (originalValue.length() > 2)
	    		originalValue = originalValue.replace("ff", "");
	    	
	    	originalMsg = originalMsg + originalValue;
	    }
	    
	    
		String userInput= originalMsg;  
		String result="";  
		for(int i=0;i<userInput.length();i++)  
		{  
			char temp=userInput.charAt(i);  
			String temp2=""+temp+"";  
			for(int j=0;j<hex.length;j++)  
			{  
				if(temp2.equalsIgnoreCase(hex[j]))  
				{  
					result=result+binary[j];  
				}  
			}  
		}  
		return result; 
	}  



  /**
   * Method for Replacing the char in string at provided location
   * @input pos-Int position where to replace character
   *        char-char to be replace at provided pos
   *        String- s which is to be formated
   * 
   * @return Re-formated String       
   * 
   * */
	public static String replaceCharAt(String s, int pos, char c) throws Exception
	{
		   return s.substring(0,pos) + c + s.substring(pos+1);
	}
	
	/**
	 * This method sends ISO8583 message to server and accepts the response.
	 * 
	 * @param isoMessage
	 * @return String
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public static String networkTransport(String isoMessage) throws UnknownHostException, IOException
	{
		Socket connection = new Socket("127.1.1.1", 1223);
		
		BufferedOutputStream bos = new BufferedOutputStream(connection.getOutputStream());
		OutputStreamWriter osw = new OutputStreamWriter(bos);
		osw.write(isoMessage+"\n");
		osw.flush();
		
		byte[] arrOutut = new byte[4096];
		int count = connection.getInputStream().read(arrOutut, 0, 4096);
		
		String clientRequest = "";
		for (int outputCount = 0; outputCount < count; outputCount++)
		{
			char response = (char)arrOutut[outputCount];
			clientRequest = clientRequest + response;
		}
		
		connection.close();
			
		return clientRequest; 	
	}
}
