/*
 *	<iso8583 Message Pack & Unpack lib for Java. Works well with Android too. Supports:Tertiary Bitmap -> Subfields>
 *  Copyright (C) 2013  Vikrant Labde <vikrant@cuelogic.co.in>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
			ISOSCHEMA.put("9","NUMERIC-0-8-0_0");
			ISOSCHEMA.put("18","FCHAR-10-10-0_0");
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
	    		   if(Integer.parseInt(fieldMaxLen) >= fieldLength) //?????????? fieldLength OR fieldMaxLen -- Discuss with Sagar
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
	    	   else if (dataType.equalsIgnoreCase("BINARY"))
	    	   {
	    		   if(Integer.parseInt(fieldMaxLen) >= fieldLength) 
	    		   {
	    				int bytes = (fieldLength + 1) / 2; // The +1 is so it rounds up
	    				byte b;
	    				StringBuffer sb = new StringBuffer();
	    				String fPa;
	    				if (fieldLength % 2 == 0) // Even number of chars, so there's no padding at the end
	    				{
	    				  for (int iRe = 0; iRe < fieldLength; iRe += 2)
	    				  {    		
	    					  fPa = fieldVlaue.substring(iRe, iRe + 2);
	    					  sb.append(CtoX(fPa));
	    				  }
	    				}
	    				else // Odd number
	    				{
	    				  int iRe;

	    				  fPa = "";
	    				  fPa = fPa + fieldVlaue.charAt(0);

	    				  sb.append(CtoX(fPa)); // Get the first char from the second nibble
	    				  for (iRe = 1; iRe < fieldLength; iRe += 2)
	    				  {
	    					  fPa = fieldVlaue.substring(iRe, iRe + 2);
	    					  sb.append(CtoX(fPa));
	    				  }
	    				}
	    				
	    			   /////
		    		   ISOMESSAGE.put(fields.toString(),  sb.substring(0, sb.length()));
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
				    	  if(!subFields.contains(1)){
				    		  isoSubMessage = isoSubMessage + ISOMESSAGE.get(dataElement + ".1");
				    		  
				    	  }
				    	  
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
			char[] bitMap;
			
			//Retrive heighest Data Element from the list
			Integer DE = fields.get(fields.size() - 1);
			
			//Know the type of bitmap (primary, secondary, tertiary
			if(DE > 65 && DE <= 128)
			{
				fields.add(1); //Add 1st Bit for secondary bitmap
				bitmapType = "secondary";
				bitMap = new char[16];
			}
			else if(DE > 128)
			{
				fields.add(1); //Add 1st Bit for secondary bitmap
				fields.add(65); //Add 65th Bit for tertiary bitmap
				ISOMESSAGE.put("65", "1");
				bitmapType = "tertiary";
				bitMap = new char[24];
				
			}
			else
			{
				bitmapType = "primary";
				bitMap = new char[8];

			}
			
			//Sort ArrayList again 
			Collections.sort(fields);

			
			//Append BITMAP to the message
			CalcBitMap(bitMap, fields);
			ISOMESSAGE.put("1", String.valueOf(bitMap, 0, bitMap.length));
		
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
			char[] bitMap;
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
					bitMap = new char[16];
				}
				else if(DE > 128)
				{
					fields.add(1); //Add 1st Bit for secondary bitmap
					fields.add(65); //Add 65th Bit for tertiary bitmap
					ISOMESSAGE.put(subFieldId + ".65", "1");
					bitmapType = "tertiary";
					bitMap = new char[24];
				}
				else
				{
					bitmapType = "primary";
					bitMap = new char[8];
				}
				//Sort ArrayList again 
				Collections.sort(fields);
				
				//Maintain Subfield Mapping
				SUBFIELDSMAPPING.put(Integer.parseInt(subFieldId), fields);
				
				//Append BITMAP to the message
				CalcBitMap(bitMap, fields);
				ISOMESSAGE.put(subFieldId+".1", String.valueOf(bitMap, 0, bitMap.length));		 	
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
			 String primaryBitMap = GetBitMap(priMaryHexBitMap ,1);
			 
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
				
				secondaryBitMap = GetBitMap(secondaryHexBitmap, 1);
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
					 
					 tertiaryBitMap = GetBitMap(tertiaryHexBitmap, 1);
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
					       else if(dataType.equalsIgnoreCase("BINARY"))
				    	   {
					    	   if(remainingMessage == null)
					    	   {
					    		   String fieldValue = messageAfterBitMap.substring(0,(Integer.parseInt(fieldMaxLen)/2));
					    		   System.out.println(fieldValue);
					    		   if(fieldValue == null)
					    		   {
					    			   throw new IOException(field + " Has null or inappropriate value");
					    		   }
					    		   
					    		   String originalMsg="";
					    		   for (int x = 0; x < fieldValue.length(); x++) {
					    		    	int value = (int)fieldValue.charAt(x);
					    		    	String originalValue = Integer.toHexString(value);

					    		    	originalMsg = originalMsg + originalValue;
					    		    }
					    		   
					    		   fieldValue = Integer.toString(Integer.parseInt(originalMsg,16));
					    		   
					    		   PARSEDISOMESSAGE.put(SUBFIELDID+field.toString(), fieldValue); //Lets start pushing parsed fields into Hash Map
							       
					    		   remainingMessage = messageAfterBitMap.substring(Integer.parseInt(fieldMaxLen)/2);
					    	   }
					    	   else //operation on remainingMessage
					    	   {
					    		   String fieldValue = remainingMessage.substring(0,(Integer.parseInt(fieldMaxLen)/2));
					    		   
					    		   if(fieldValue == null)
					    		   {
					    			   throw new IOException(field + " Has null or inappropriate value");
					    		   }
					    		   
					    		   String originalMsg="";
					    		   for (int x = 0; x < fieldValue.length(); x++) {
					    		    	int value = (int)fieldValue.charAt(x);
					    		    	String originalValue = Integer.toHexString(value);

					    		    	originalMsg = originalMsg + originalValue;
					    		    }
					    		   
					    		   fieldValue = Integer.toString(Integer.parseInt(originalMsg,16));
					    		   
					    		   PARSEDISOMESSAGE.put(SUBFIELDID+field.toString(), fieldValue); //Lets start pushing parsed fields into Hash Map
					    		   remainingMessage = remainingMessage.substring(Integer.parseInt(fieldMaxLen)/2);
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
			 
			 //System.out.println("Parsed Message " + PARSEDISOMESSAGE); 
		}
	
	
		/**
		 * This Method generates the binary representation of the BITMAP.
		 * @input= Arraylist for the subfields present 
		 * @return String containing bitmap msg
		 * @throws Exception 
		 * 
		 */
		public static String CalcBitMap(char[] bitMap, ArrayList<Integer> list) throws Exception
		{
			for(int i=0;i<list.size();i++)
			{
				int iPos = (list.get(i) / 8);
				int iPosIn = list.get(i) - (iPos * 8);
				if(iPosIn == 0){
					iPos--;
					iPosIn = 8;
				}	
				if((65 <= list.get(i))  && (list.get(i) < 128))
					bitMap[ 0 ] |= 1 << 7;
				if((128 <= list.get(i))  && (list.get(i) < 192))
					bitMap[ 8 ] |= 1 << 7;
					
				bitMap[ iPos ] |= 1 << (8 - iPosIn);

			}
			return "OK";
		}
	
	
		/**
		 * This method calculates the BITMAP of available fields in the message.
		 * It converts the binary representation into Hex representation
		 * and then Hex representation into ACSCII and binds to the ISO8583 message
		 * @param bin
		 * @throws Exception
		 */
		public static String GetBitMap(String bin, int nouse) throws Exception  
		{  
			int iCo = 0;
			StringBuffer binRes = new StringBuffer(64);
			
			for(iCo = 0; iCo < 8; iCo++){
				if((bin.charAt(iCo) & 128) > 0) binRes.append('1'); else binRes.append('0');
				if((bin.charAt(iCo) & 64) > 0) binRes.append('1'); else binRes.append('0');
				if((bin.charAt(iCo) & 32) > 0) binRes.append('1'); else binRes.append('0');
				if((bin.charAt(iCo) & 16) > 0) binRes.append('1'); else binRes.append('0');
				if((bin.charAt(iCo) & 8) > 0) binRes.append('1'); else binRes.append('0');
				if((bin.charAt(iCo) & 4) > 0) binRes.append('1'); else binRes.append('0');
				if((bin.charAt(iCo) & 2) > 0) binRes.append('1'); else binRes.append('0');
				if((bin.charAt(iCo) & 1) > 0) binRes.append('1'); else binRes.append('0');
			}

			return binRes.toString();
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
		 * Type cast to char data type
		 * @param x
		 * @return
		 */
		public static char CtoX(String x)
		{
			int r = 0;
			r = Integer.parseInt(x,16);
			return (char)r;
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
