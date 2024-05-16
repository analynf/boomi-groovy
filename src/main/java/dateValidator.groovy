import java.text.ParseException

try {
    Date.parse('yyyyMmdd', '2024-03-08')
    println "date is correct"
} catch (ParseException p) {
    println "date is invalid"
}
