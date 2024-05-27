// Use for testing. Remove when adding to Data Process shape.
import com.boomi.execution.ContextCreator


// Place directory for multiple files and file name for single file
String pathFiles = "${System.getenv("PROJECT_DIR")}/input_files/wm1DispatchedQty.txt"
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


import java.util.Properties;
import java.io.InputStream;
import com.boomi.execution.ExecutionUtil;
import org.apache.commons.lang3.StringUtils;

logger = ExecutionUtil.getBaseLogger();
Map<String, Integer> orderLines = new HashMap<>();

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

    while ((line = reader.readLine()) != null) {
        logger.info(line);
        String[] record = line.split("\\*");
        String key = StringUtils.join(record[0],"*",record[1]);
        if (orderLines.containsKey(key)) {
            Integer dispatchedQty = Integer.valueOf(orderLines.get(key));
            orderLines.put(key, dispatchedQty + Integer.valueOf(record[2]));
        } else {
            orderLines.put(key, record[2]); // orderNoLineId, dispatchedQty
        }
        logger.info("orderLines: " + orderLines);
    }
    StringBuilder output = new StringBuilder();
    for (Map.Entry<String, Integer> entry : orderLines.entrySet()) {
        output.append(entry.getKey()).append("*").append(entry.getValue()).append(System.lineSeparator()) ;
    }
    logger.info("output: ");
    logger.info(output.toString())
    is = new ByteArrayInputStream(output.toString().getBytes());
    dataContext.storeStream(is, props);

}
