// Use for testing. Remove when adding to Data Process shape.
import com.boomi.execution.ContextCreator


// Place directory for multiple files and file name for single file
String pathFiles = "${System.getenv("PROJECT_DIR")}/input_files/whoShipment.json"
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

logger = ExecutionUtil.getBaseLogger();
Set docList = new HashSet();

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

    StringBuilder output = new StringBuilder();
    while ((line = reader.readLine()) != null) {
        logger.info(line);
        String[] record = line.split("\\*");
        if (record.length > 0 && record[0]?.trim() && !docList.contains(record[0])) {
            docList.add(record[0]);
            output.append(line).append(System.lineSeparator());
        }
    }
    logger.info("output:");
    logger.info(output.toString());
    is = new ByteArrayInputStream(output.toString().getBytes());
    dataContext.storeStream(is, props);

}
