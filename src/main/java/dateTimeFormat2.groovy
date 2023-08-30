import java.time.LocalDate
import java.time.format.DateTimeFormatter


LocalDate currentDate = LocalDate.now();
println currentDate;

DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
String formattedDate = currentDate.format(formatter);
println formattedDate;
//String strDateTime = "30-01-2023";
//def eventDateTime = null;
//if (strDateTime?.trim()) {
//    if (!strDateTime.contains(":")) {
//        strDateTime = strDateTime.trim() + " 00:02";
//    }
//    tempDateTime = LocalDateTime.parse(strDateTime, DATETIME_FORMATTER);
//    eventDateTime = tempDateTime.format(DATETIME_FORMATTER);
//}
//
//println eventDateTime;
//dd-MMM-yyyy
