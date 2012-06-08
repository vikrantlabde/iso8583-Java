package ISO;

import java.io.IOException;
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
		//TODO Auto-generated method stub
		
		//isoSchema.put("<field-id","<field-data-type>-<field-size>-<field-max-length>-<has-subfields_<has-bitmap>>-<Field Name>");
		ISOSCHEMA.put("1","BITMAP");
		ISOSCHEMA.put("4","NUM-2-10-0_0");
		ISOSCHEMA.put("9","NUMERIC-0-8-0_0-RADA");
		ISOSCHEMA.put("18","CHAR-1-4-0_0");
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
		isofields.put("101", "Cuelogic Technologies PVT LTD.");
		isofields.put("130", "karvenagar");
		isofields.put("114.10", "99");
		isofields.put("114.117", "Shweta Labde");
		isofields.put("114.150", "Shravya Vikrant Labde");
		
		isofields.put("120.64", "Aaichya gavat ni barachya bhavat");
		isofields.put("120.120", "Guddu Sakhale");
		
		String isoMessage = packIsoMsg("1001",isofields);
		
		System.out.println(isoMessage);
		/// Packing ISO MESSAGE
		
		//UNPACKING ISO MESSAGE
		unpackIsoMsg(isoMessage);
		System.out.println(PARSEDISOMESSAGE);
		//UNPACKING ISO MESSAGE
	}
	
	
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
    			   String newFieldValue = String.format("%0"+ fieldMaxLen +"d", Integer.parseInt(fieldVlaue));
    			   ISOMESSAGE.put(fields, newFieldValue);
    		   }
    		   else
    		   {
    			   throw new IOException("Field:"+fields + " Has bigger value. Its set "+fieldMaxLen +" in ISOSCHEMA and you have entered" + fieldLength );
    		   }
    	   }
    	   else if (dataType.equalsIgnoreCase("CHAR") || dataType.equalsIgnoreCase("NUM"))
    	   {
    		   if(Integer.parseInt(fieldMaxLen) >= fieldLength) //?????????? fieldLength OR fieldMaxLen -- Discuss with Sagar
    		   {
    			   String newFieldLen = String.format("%0"+ fieldLenType +"d", Integer.parseInt(strfieldLength));
    			   
    			   /////
	    		   ISOMESSAGE.put(fields.toString(), newFieldLen + fieldVlaue);
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
	    
	   return buildISOMessage(finalFields);
	   
	}
	
	public static String buildISOMessage(ArrayList<Integer> finalFields) throws Exception
	{
		
		//System.out.println(finalFields);
		//System.out.println(ISOMESSAGE);
		//System.out.println(SUBFIELDSMAPPING);
		
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
		       		  
			       //System.out.println(dataElement + "-"  + hasSubfield + "-" + hasSubfieldBitmap);
			       
			       if(hasSubfield.equalsIgnoreCase("1"))
			       {
			    	   //Traverse subfields
			    	  ArrayList <Integer> subFields = new ArrayList<Integer>();
			    	  subFields = SUBFIELDSMAPPING.get(Integer.parseInt(dataElement));
			    	  
			    	  Iterator<?> i = subFields.iterator();
			    	  String isoSubMessage = "";
			 		  while(i.hasNext())
			 		  {
			 			  String subDataElement = i.next().toString();
			 			  String mainDataElement = dataElement + "." + subDataElement;
			 			  isoSubMessage = isoSubMessage + ISOMESSAGE.get(mainDataElement);
			 		  }
			 		  
			 		
		    		  String subMessageLen = String.format("%0"+ fieldLenType +"d", isoSubMessage.length());
		    		   
			 		 
			 		 // System.out.println(subMessageLen);
			 		 // System.out.println(isoSubMessage);
			 		  isoMessage = 	isoMessage + subMessageLen + isoSubMessage; 
			 		  
			 		  //vikratn is here --> Message length should be in perfect leng as per the schema
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
		
		//System.out.println(isoMessage);
		return isoMessage;
	}
	
	//Create array list of all mail Data Elements that are available.
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
				  //System.out.println(e + "these are subfields");
			  }
		  }
		
		//Sort fields in assending order
		Collections.sort(newFiledMap);    
		return newFiledMap;
	}
	
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
			//fields.add(1); //Add 1st Bit for secondary bitmap
			bitmapType = "primary";
		}
		
		//Sort ArrayList again 
		Collections.sort(fields);
		
		//Append BITMAP to the message
		ISOMESSAGE.put("1", CalcBitMap(fields, bitmapType));
		
		return fields;
	}
	
	
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
			//System.out.println(fields);
			
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
				fields.add(1); //Add 1st Bit for secondary bitmap
				bitmapType = "primary";
			}
			//Sort ArrayList again 
			Collections.sort(fields);
			
			//System.out.println(fields);
			
			//Maintain Subfield Mapping
			SUBFIELDSMAPPING.put(Integer.parseInt(subFieldId), fields);
			
			//Append BITMAP to the message
		 	ISOMESSAGE.put(subFieldId+".1", CalcBitMap(fields, bitmapType));
		}
	}
	
	
	
	public static void unpackIsoMsg(String isoMessage) throws Exception
	{
		 //System.out.println(isoMessage);
		 String overallBitmap = null;
		 
		 String messageAfterBitMap = null;
		 
		 if(ISSUBFIELDPARSING == false)
			 PARSEDISOMESSAGE.put("MTI",isoMessage.substring(0, 4));
		 
		 String priMaryHexBitMap = "";
		 if(ISSUBFIELDPARSING == false)
			 priMaryHexBitMap = isoMessage.substring(4, 20);
		 else
			 priMaryHexBitMap = isoMessage.substring(0, 16);
	
		 
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
				secondaryHexBitmap = isoMessage.substring(20, 36);
			else
				secondaryHexBitmap = isoMessage.substring(16, 32);
			
			secondaryBitMap = GetBitMap(secondaryHexBitmap);
			overallBitmap = overallBitmap + secondaryBitMap;
			//if 65th field is binary then there is a tertiary bitmap
			Integer firstBitOfSecBitmap = Integer.parseInt(secondaryBitMap.substring(0, 1));
			if(firstBitOfSecBitmap > 0)
			{
				 bitmaplength = 192;
				 if(ISSUBFIELDPARSING == false)
					 tertiaryHexBitmap = isoMessage.substring(36, 52);
				 else
					 tertiaryHexBitmap = isoMessage.substring(32, 48);
				 
				 tertiaryBitMap = GetBitMap(tertiaryHexBitmap);
				 overallBitmap = overallBitmap + tertiaryBitMap;
				 if(ISSUBFIELDPARSING == false)
					 messageAfterBitMap = isoMessage.substring(52); //After MTI and Primary bitmap
				 else
					 messageAfterBitMap = isoMessage.substring(48); //After MTI and Primary bitmap
			}
			else
			{
				if(ISSUBFIELDPARSING == false)
					messageAfterBitMap = isoMessage.substring(36); //After MTI and Primary bitmap
				else
					messageAfterBitMap = isoMessage.substring(32); //After MTI and Primary bitmap
			}
		 }
		 else //Secondary bitmap is not available so remaining message is actual data
		 {
			 if(ISSUBFIELDPARSING == false)
				 messageAfterBitMap = isoMessage.substring(20); //After MTI and Primary bitmap
			 else
				 messageAfterBitMap = isoMessage.substring(16); //After MTI and Primary bitmap
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
			    	   }else if(dataType.equalsIgnoreCase("CHAR") || dataType.equalsIgnoreCase("NUM"))
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
		 
		 //System.out.println("Parsed Message " + PARSEDISOMESSAGE); 
	}
	
	
	/**
	 * Method For Calculating the bitmap for fields
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
			//System.out.println(pos);
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
		return fullHex;
	 }
	
		
public static String GetBitMap(String bin) throws Exception  
{  
	String[]hex={"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};  
	String[]binary={"0000","0001","0010","0011","0100","0101","0110","0111","1000","1001","1010","1011","1100","1101","1110","1111"};  
	  
	String userInput= bin;  
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
}
