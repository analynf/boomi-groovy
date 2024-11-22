// Use for testing. Remove when adding to Data Process shape.

import com.boomi.execution.ContextCreator
import com.boomi.execution.ExecutionUtil
import org.apache.commons.lang3.StringUtils
import org.json.JSONArray
import org.json.JSONObject


// Place directory for multiple files and file name for single file
String pathFiles = "${System.getenv("PROJECT_DIR")}/input_files/wm1InventoryReport.json"
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
    String data = is.getText("UTF-8");
    JSONObject jsonObject = new JSONObject(data);
    JSONArray lines = jsonObject.optJSONArray("lines");
    Map<BigInteger, JSONArray> jsonMap = new HashMap<>();
    for (int y = 0; y < lines.length(); y++) {
        JSONObject currentLine = lines.optJSONObject(y);
        BigInteger messageNumber = currentLine.opt("messageNumber") as BigInteger;
        JSONArray jsonArr = jsonMap.get(messageNumber);
        if (jsonArr == null) {
            JSONArray newArr = new JSONArray();
            newArr.put(currentLine);
            jsonMap.put(messageNumber, newArr);
        } else {
            JSONArray jsonArr2 = new JSONArray(jsonArr.toString());
            String eanNumber = currentLine.optString("eanNumber");
            String unitType = currentLine.optString("unitType");
            for (int z = 0; z < jsonArr2.length(); z++) {
                JSONObject obj = jsonArr2.optJSONObject(z);
                String eanNumber2 = obj.optString("eanNumber");
                String unitType2 = obj.optString("unitType");
                if (StringUtils.equals(eanNumber,eanNumber2) && StringUtils.equals(unitType,unitType2)) {
                    // add account
                    JSONArray accounts = obj.optJSONArray("accounts")
                    if (accounts == null) {
                        accounts = new JSONArray();
                        JSONObject account = new JSONObject();
                        account.put("availability", obj.optString("availability"));
                        account.put("quantity", obj.opt("quantity"));
                        accounts.put(account);
                    }
                    JSONObject account = new JSONObject();
                    account.put("availability", currentLine.optString("availability"));
                    account.put("quantity", currentLine.opt("quantity"));
                    accounts.put(account);
                    //obj.remove("accounts");
                    obj.put("accounts", accounts);
                    // sum the quantity
                    BigDecimal quantity = currentLine.optBigDecimal("quantity", BigDecimal.ZERO);
                    BigDecimal quantity2 = obj.optBigDecimal("quantity", BigDecimal.ZERO);
                    obj.put("quantity", quantity.add(quantity2));
                    jsonArr.put(z, obj);
                } else {
                    jsonArr.put(currentLine);
                }
            }

        }
    }
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