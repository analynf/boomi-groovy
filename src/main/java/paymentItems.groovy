// Use for testing. Remove when adding to Data Process shape.
import com.boomi.execution.ContextCreator


// Place directory for multiple files and file name for single file
String pathFiles = "${System.getenv("PROJECT_DIR")}/input_files/paymentItems.txt"
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
import com.boomi.execution.ExecutionUtil;
import org.apache.commons.lang3.StringUtils;

logger = ExecutionUtil.getBaseLogger();
logger.info("Number of documents: " + dataContext.getDataCount())

List<String> otherPaymentFormats = new ArrayList<>(Arrays.asList("A2A", "BGI", "PGI", "CBP"));
List<String> structuredPaymentFormats = new ArrayList<>(Arrays.asList("BGO", "PGO", "KID", "FIK", "REF"));
Map<String, String> otherPayments = new HashMap<>();
Map<String, List<String>> creditVouchersMap = new HashMap<>();
StringBuilder output = new StringBuilder();
for (int i = 0; i < dataContext.getDataCount(); i++) {
    InputStream is = dataContext.getStream(i)
    Properties props = dataContext.getProperties(i)
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

    while ((line = reader.readLine()) != null) {
        //logger.info("line: " + line); // TODO comment this line
        String[] lineArr = line.split("\\*");
        if (otherPaymentFormats.contains(lineArr[1])) { // unstructured and cross border payments
            otherPayments.put(lineArr[0], line);

            // construct credit voucher list
            List<String> creditVoucherList = creditVouchersMap.get(lineArr[0]);
            creditVoucherList = (creditVoucherList == null) ? new ArrayList<>() : creditVoucherList;
            Integer listSize = creditVoucherList.size();
            //logger.info("listSize: " + listSize); // TODO comment this line
            String creditVoucher = (listSize > 0) ? creditVoucherList.get(listSize-1) : "";
            //logger.info("creditVoucher: " + creditVoucher); // TODO comment this line
            if ("A2A" == lineArr[1] && "SE" == lineArr[6]) {
                String paymentNumber =StringUtils.left(lineArr[0], 12);
                if (listSize < 1) {
                    creditVoucherList.add(paymentNumber);
                } else {
                    creditVoucherList.set(listSize-1, paymentNumber);
                }
            } else {
                String lineCreVoucher = (lineArr.length > 7) ? lineArr[7] : "";
                String newCreditVoucher = StringUtils.defaultString(creditVoucher) + " " + StringUtils.defaultString(lineCreVoucher);
                //logger.info("newCreditVoucher: " + newCreditVoucher); // TODO comment this line

                if (("BGI" == lineArr[1] || "PGI" == lineArr[1]) && "SE" == lineArr[6]) {
                    if (listSize > 3) {
                        if (newCreditVoucher.length() <= 96) {
                            creditVoucherList.set(listSize - 1, newCreditVoucher);
                        } else {
                            creditVoucherList.add(listSize, lineCreVoucher);
                        }
                    } else {
                        addCreditVoucher(newCreditVoucher, listSize, lineCreVoucher, creditVoucherList, 131);
                    }
                } else if ("A2A" == lineArr[1] && "NO" == lineArr[6]) {
                    addCreditVoucher(newCreditVoucher, listSize, lineCreVoucher, creditVoucherList, 111);

                } else if (("A2A" == lineArr[1] && "FI" == lineArr[6]) || "CBP" == lineArr[1]) {
                    addCreditVoucher(newCreditVoucher, listSize, lineCreVoucher, creditVoucherList, 131);

                } else if ("A2A" == lineArr[1] && "DK" == lineArr[6]) {
                    addCreditVoucher(newCreditVoucher, listSize, lineCreVoucher, creditVoucherList, 26);
                }
            }
            creditVouchersMap.put(lineArr[0], creditVoucherList);
        } else { // structured payments
            output.append(line).append(System.lineSeparator());
        }
    }
    logger.info(creditVouchersMap.toString()); // TODO comment this line
    logger.info("otherPayments: " + otherPayments.toString()); // TODO comment this line

    for (Map.Entry<String, List<String>> entry : creditVouchersMap.entrySet()) {
        String lineItem = otherPayments.get(entry.getKey());
        String[] lineSplit = lineItem.split("\\*");
        if ("A2A" == lineSplit[1] && "SE" == lineSplit[6]) {

            for (String creditVouchers : entry.getValue()) {
                appendOutput(output, lineSplit,  creditVouchers);
            }

        } else if (("BGI" == lineSplit[1] || "PGI" == lineSplit[1]) && "SE" == lineSplit[6]) {
            if (entry.getValue().size() > 4) {
                appendOutput(output, lineSplit, "Replace with AdviceTxt1");
            } else {
                for (String creditVouchers : entry.getValue()) {
                    appendOutput(output, lineSplit, "Invoice: " + creditVouchers);
                }
            }
        } else if ("A2A" == lineSplit[1] && "DK" == lineSplit[6]) {
            if (entry.getValue().size() > 41) {
                appendOutput(output, lineSplit, "Replace with AdviceTxt1");
            } else {
                for (String creditVouchers : entry.getValue()) {
                    appendOutput(output, lineSplit, "Invoice: " + creditVouchers);
                }
            }
        } else {
            if (entry.getValue().size() > 1) {
                appendOutput(output, lineSplit, "Replace with AdviceTxt1");
            } else {
                for (String creditVouchers : entry.getValue()) {
                    appendOutput(output, lineSplit, "Invoice: " + creditVouchers);
                }
            }
        }

    }

    logger.info(output.toString()); // TODO comment this line

    is = new ByteArrayInputStream(output.toString().getBytes());
    dataContext.storeStream(is, props)
}

def appendOutput(output, lineSplit, strValue) {
    ArrayList<String> lineList = Arrays.asList(lineSplit);
    if (lineList.size() > 7) {
        lineList.set(7, strValue);
    } else {
        lineList.add(StringUtils.trim(strValue));
    }
    String joinedStr = String.join("*", lineList);
    output.append(joinedStr).append(System.lineSeparator());
}

def addCreditVoucher(newCreditVoucher, listSize, lineCreVoucher, creditVoucherList, length) {
    if (newCreditVoucher.length() <= length) {
        if (listSize < 1) {
            creditVoucherList.add(lineCreVoucher);
        } else {
            creditVoucherList.set(listSize-1, newCreditVoucher);
        }

    } else {
        creditVoucherList.add(listSize, lineCreVoucher);
    }
}