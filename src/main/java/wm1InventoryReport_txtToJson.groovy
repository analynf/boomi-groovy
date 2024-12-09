// Use for testing. Remove when adding to Data Process shape.

import com.boomi.execution.ContextCreator
import com.boomi.execution.ExecutionUtil
import org.apache.commons.lang3.StringUtils
import org.json.JSONArray
import org.json.JSONObject


// Place directory for multiple files and file name for single file
String pathFiles = "${System.getenv("PROJECT_DIR")}/input_files/wm1InventoryReport.txt"
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

logger = ExecutionUtil.getBaseLogger();

logger.info("Before-Number of documents: " + dataContext.getDataCount())
for (int i = 0; i < dataContext.getDataCount(); i++) {
    InputStream is = dataContext.getStream(i)
    Properties props = dataContext.getProperties(i)
    BufferedReader reader = new BufferedReader(new InputStreamReader(is))
    Map<String, JSONArray> jsonMap = new HashMap<>();
    Map<String, Set> existingEanMap = new HashMap<>();
    while ((line = reader.readLine()) != null) {
        String[] lineArr = line.split("\\*")
        String messageNumber = lineArr[0];
        String eanNumber = lineArr[1];
        String quantity = lineArr[2];
        String unitType = null;
        if (lineArr.length > 3) {
            unitType = lineArr[3];
        }
        String availability = null;
        if (lineArr.length > 4) {
            availability = lineArr[4];
        }
        JSONArray jsonArr = jsonMap.get(messageNumber);
        if (jsonArr == null) {
            JSONArray newArr = new JSONArray();
            JSONObject newLine = new JSONObject();
            newLine.put("messageNumber", messageNumber);
            newLine.put("eanNumber", eanNumber);
            newLine.put("quantity", quantity);
            if (unitType != null) {
                newLine.put("unitType", unitType);
            }
            if (availability != null) {
                newLine.put("availability", availability);
            }
            newArr.put(newLine);
            jsonMap.put(messageNumber, newArr);
            Set existingEan = new HashSet();
            existingEan.add(eanNumber + unitType);
            existingEanMap.put(messageNumber, existingEan);
        } else {
            Set existingEan = existingEanMap.get(messageNumber);
            if (existingEan.contains(eanNumber + unitType)) {
                // add account
                for (int z = 0; z < jsonArr.length(); z++) {
                    JSONObject obj = jsonArr.optJSONObject(z);
                    String eanNumber2 = obj.optString("eanNumber");
                    String unitType2 = obj.optString("unitType");
                    if (StringUtils.equals(eanNumber, eanNumber2) && StringUtils.equals(unitType, unitType2)) {
                        JSONArray accounts = obj.optJSONArray("accounts")
                        if (accounts == null) {
                            accounts = new JSONArray();
                            JSONObject account = new JSONObject();
                            account.put("availability", obj.optString("availability"));
                            account.put("quantity", obj.opt("quantity"));
                            accounts.put(account);
                        }
                        JSONObject account = new JSONObject();
                        account.put("availability", availability);
                        account.put("quantity", quantity);
                        accounts.put(account);
                        //obj.remove("accounts");
                        obj.put("accounts", accounts);
                        // sum the quantity
                        BigDecimal quantityBd = new BigDecimal(quantity);
                        BigDecimal quantity2 = obj.opt("quantity") as BigDecimal;
                        obj.put("quantity", quantityBd.add(quantity2));
                        jsonArr.put(z, obj);
                        break;
                    }
                }
            } else {
                JSONObject newLine = new JSONObject();
                newLine.put("messageNumber", messageNumber);
                newLine.put("eanNumber", eanNumber);
                newLine.put("quantity", lineArr[2]);
                if (unitType != null) {
                    newLine.put("unitType", unitType);
                }
                if (lineArr.length > 4) {
                    newLine.put("availability", lineArr[4]);
                }
                jsonArr.put(newLine);
                existingEan.add(eanNumber + unitType);
            }
        }
    }

    JSONObject jsonObject = new JSONObject();
    JSONObject documentHeader = new JSONObject();
    documentHeader.put("receiver", props.getProperty("document.dynamic.userdefined.ddp_receiver"));
    jsonObject.put("documentHeader", documentHeader);
    logger.info("jsonMap: " + jsonMap.toString());
    jsonMap.forEach({ key, newLines ->
        //jsonObject.remove("lines");
        jsonObject.put("lines", newLines);
        logger.info("jsonObject: " + jsonObject.toString())
        ByteArrayInputStream bais = new ByteArrayInputStream(jsonObject.toString().getBytes("UTF-8"));
        dataContext.storeStream(bais, props);
    })
}
logger.info("After-Number of documents: " + dataContext.getDataCount())