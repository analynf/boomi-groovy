// Use for testing. Remove when adding to Data Process shape.
import com.boomi.execution.ContextCreator


// Place directory for multiple files and file name for single file
String pathFiles = "${System.getenv("PROJECT_DIR")}/input_files/paymentItemsCount.txt"
println pathFiles
dataContext = new ContextCreator()
dataContext.AddFiles(pathFiles)
ExecutionUtil ExecutionUtil = new ExecutionUtil()

/* Add any Dynamic Process Properties or Dynamic Document Properties. If
   setting DDPs for multiple files, then index needs to be set for each and
   the index range starts at 0. */
ExecutionUtil.setDynamicProcessProperty("DPP_name", "DPP_value", false)
dataContext.addDynamicDocumentPropertyValues(0, "DDP_name", "DDP_value")


// Place script after this line.
//----------------------------------------------------------------------------------------------------


import java.util.Properties
import java.io.InputStream
import java.text.DecimalFormat;
import com.boomi.execution.ExecutionUtil;

DELIMITER = "*";
DecimalFormat decimalFormat = new DecimalFormat("0.00");
logger = ExecutionUtil.getBaseLogger();
logger.info("Number of documents: " + dataContext.getDataCount())
Map<String, Integer> paymentItemsCount = new HashMap<>();
Map<String, Double> paymentAmounts = new HashMap<>();
for (int i = 0; i < dataContext.getDataCount(); i++) {
    InputStream is = dataContext.getStream(i)
    Properties props = dataContext.getProperties(i)
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

    while ((line = reader.readLine()) != null) {
        logger.info("line: " + line);
        String[] lineArr = line.split("\\*");

        // calculate total count of items per payment
        Integer count = paymentItemsCount.get(lineArr[0]);
        count = (count == null) ? 1 : count+1;
        paymentItemsCount.put(lineArr[0], count);

        // calculate total amount per payment
        Double amount = Double.parseDouble(lineArr[2]);
        Double totalAmount = paymentAmounts.get(lineArr[0]);
        totalAmount = (totalAmount == null) ? amount : totalAmount + amount;
        paymentAmounts.put(lineArr[0], totalAmount);

    }

    StringBuilder result = new StringBuilder();
    for (Map.Entry<String, Integer> entry : paymentItemsCount.entrySet()) {
        result.append(entry.getKey()).append(DELIMITER)
            .append("").append(DELIMITER)
            .append(decimalFormat.format(paymentAmounts.get(entry.getKey()))).append(DELIMITER)
            .append(entry.getValue()).append(System.lineSeparator());
    }
logger.info(result.toString());
    is = new ByteArrayInputStream(result.toString().getBytes());
    dataContext.storeStream(is, props)
}