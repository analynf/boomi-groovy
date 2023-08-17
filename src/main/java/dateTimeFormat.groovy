import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
String strDateTime = "30-01-2023";
def eventDateTime = null;
if (strDateTime?.trim()) {
    if (!strDateTime.contains(":")) {
        strDateTime = strDateTime.trim() + " 00:02";
    }
    tempDateTime = LocalDateTime.parse(strDateTime, DATETIME_FORMATTER);
    eventDateTime = tempDateTime.format(DATETIME_FORMATTER);
}

println eventDateTime;

