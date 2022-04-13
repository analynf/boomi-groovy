import java.util.Properties;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import java.lang.StringBuffer;
import java.io.InputStream;
import com.boomi.execution.ExecutionUtil;
import java.util.logging.Logger;

Logger logger = ExecutionUtil.getBaseLogger();


for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    logger.info("- DataCount(" + i + ")");
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);



    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String line = br.readLine();

    //Note: The script will work if the operation of the Database connector is set to batch 1, as it will execute per database row.

    //remove the database line of START and END
    int start = line.indexOf("OUT_START|3|@|") + 14;
    int end = line.indexOf("|#||#|OUT_END|3");
    String lineString = line.substring(start,end);


    String tmpValue = "|^|XX_TMP";
    //if End has no value, lineSplit will not reach the required length.
    if (lineString.endsWith("|")){
        lineString = lineString + tmpValue;
    }

    //split the database row content using its delimiter
    logger.info("Line to Split : " + lineString);
    String[] fieldValue = lineString.split("\\|\\^\\|");

    //put each value of database content into fieldValue array string
    logger.info("fieldValue length: " + fieldValue.length);
    for (int j=0; j < fieldValue.length; j++){
        logger.info("Line(" + j + "):#" + fieldValue[j] + "#");
    }

    logger.info("Create flat message");

//Build the message structure
//TransactionID (New Employee) + CompanyNumber + EmployeeNumber + ColumnID , fromDate , toDate, spare, field value

    String outputline = createLine(fieldValue[2], fieldValue[3], fieldValue[1], "PERSONNR","","","", fieldValue[1] );            			  	//EmployeeNumber (SocialSecurityNumberSWEDEN)
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "GLONBEL",fieldValue[40],"","", fieldValue[6] );        	//Amount (ValidFrom date re-used the Compensation Effective Date)
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "KONTONR","","","", fieldValue[7] );        				//BankAccountNumber
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "AVT",fieldValue[35],"","", fieldValue[9] );            	//CBAReferenceID
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "POSTANST","","","", fieldValue[10] );       				//City
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "AFOR",fieldValue[33],"","", fieldValue[12] );          	//EmploymentType
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "ANSTDAT",fieldValue[32],"","", fieldValue[14] );       	//FirstDayOfEmployment
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "NAMNF","","","", fieldValue[15] );         				//FirstName
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "SEMRATT",fieldValue[35],"","", fieldValue[17] );       	//HolidayAllowanceCode
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "AVGDAT","","","", fieldValue[18] );        				//LastDayOfEmployment
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "NAMNE","","","", fieldValue[19] );         				//LastName
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "BEF",fieldValue[31],"","", fieldValue[20] );        	    //JobClassification
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "PKAT",fieldValue[34],"","", fieldValue[21] );          	//PaymentGroupCode (PaymentGroup and PersonnelGroup)
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "MAILADR","","","", fieldValue[22] );       				//PrivateEmail
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "KST",fieldValue[38],"","", fieldValue[24] );           	//ProfitCenter
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "GLONART",fieldValue[39],"","", fieldValue[25] );       	//SalaryTypeCode (ValidFrom date re-used the Compensation Effective Date)
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "TJST",fieldValue[37],"","",fieldValue[4] );            	//DepartmentNumber
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "POSTADR","","","", fieldValue[27] );       				//StreetAddress
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "AVORS","","","", fieldValue[28] );         				//TerminationReason
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "VATID",fieldValue[40],"","", fieldValue[29] );         	//WeeklyHours (ValidFrom date re-used the Compensation Effective Date)
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "POSTNR","","","", fieldValue[30] );        				//ZipCode
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "SEKEL","","","", fieldValue[5] );        				//BirthDate
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "MAILADRANST","","","", fieldValue[11] );        			//CompanyEmail
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "FSN16","","","", fieldValue[16] );        				//OriginalHireDate
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "FROMDATGL","","","", fieldValue[40] );                   //Compensation Effective Date
    outputline = outputline + createLine(fieldValue[2], fieldValue[3], fieldValue[1], "EGX102","","","", fieldValue[0] );                   	//EmployeeID

//print the flat message
    byte[] bytes = outputline.getBytes("UTF-8");
    dataContext.storeStream(new ByteArrayInputStream(bytes), props);


}

//declaration of string used in creation of message structure
public String createLine(String transactionID, String companyNo, String employeeNo, String columnID,  String fromDate, String toDate, String spare , String value){
    return transactionID+";"+companyNo+";"+employeeNo+";"+columnID+";"+fromDate+";"+toDate+";"+spare+";"+value+";;;\n";
}


