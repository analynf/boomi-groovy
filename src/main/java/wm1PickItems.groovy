// Use for testing. Remove when adding to Data Process shape.
import com.boomi.execution.ContextCreator



// Place directory for multiple files and file name for single file
String pathFiles = "${System.getenv("PROJECT_DIR")}/input_files/wm1PickItems.json"
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
import org.json.JSONObject;
import org.json.JSONArray;

logger = ExecutionUtil.getBaseLogger();

//logger.info("Number of documents: " + dataContext.getDataCount())
for (int i = 0; i < dataContext.getDataCount(); i++) {
    InputStream is = dataContext.getStream(i)
    Properties props = dataContext.getProperties(i)
    String data = is.text;
    JSONObject jsonObject = new JSONObject(data);
    JSONArray orders = jsonObject.optJSONArray("outboundOrderResponse");
    for (int x = 0; x < orders.length(); x++) {
    JSONObject order = orders.optJSONObject(x);
        //logger.info("order: " + order);
        JSONArray orderLines = order.optJSONArray("orderLines");
        JSONArray newOrderLines = new JSONArray();
        //logger.info("orderLines: " + orderLines);
        Set existingLine = new HashSet();
        for (int y = 0; y < orderLines.length(); y++) {
            JSONObject line = orderLines.optJSONObject(y);
            BigInteger lineId = line.opt("lineId") as BigInteger;
            BigDecimal dispatchedQuantity = line.opt("dispatchedQuantity") as BigDecimal;
            if (!existingLine.contains(lineId)) {
                existingLine.add(lineId);
                newOrderLines.put(line);
            } else {
                if (BigDecimal.ZERO.compareTo(dispatchedQuantity) < 0) { // dispatchedQuantity is > 0
                    for (int z = 0; z < newOrderLines.length(); z++) {
                        JSONObject line2 = newOrderLines.optJSONObject(z);
                        BigInteger lineId2 = line2.opt("lineId") as BigInteger;
                        BigDecimal dispatchedQuantity2 = line2.opt("dispatchedQuantity") as BigDecimal;
                        if (lineId2 == lineId && BigDecimal.ZERO.compareTo(dispatchedQuantity2) == 0) {
                            line2.remove("dispatchedQuantity");
                            line2.put("dispatchedQuantity", dispatchedQuantity);
                        }
                    }
                }
            }
        }
        //logger.info("newOrderLines: " + newOrderLines);
        order.remove("orderLines");
        order.put("orderLines", newOrderLines);
    }
    String output = jsonObject.toString();
    //logger.info(output);

    is = new ByteArrayInputStream(output.getBytes());
    dataContext.storeStream(is, props)

}
