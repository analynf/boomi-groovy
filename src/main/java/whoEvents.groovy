// Use for testing. Remove when adding to Data Process shape.
import com.boomi.execution.ContextCreator


// Place directory for multiple files and file name for single file
String pathFiles = "${System.getenv("PROJECT_DIR")}/input_files/whoEvents.txt"
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

logger = ExecutionUtil.getBaseLogger();
Set docList = new HashSet();
Set missing = new HashSet();

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

    StringBuilder output = new StringBuilder();
    while ((line = reader.readLine()) != null) {
        logger.info(line);
        String[] record = line.split("\\,");
        if (record.length > 1 && record[1]?.trim() && !docList.contains(record[1])) {
            if (record.length < 8) {
                ExecutionUtil.setDynamicProcessProperty("dpp_missingExFactoryDate", "true", false)
                missing.add(record[1]);
            } else {
                docList.add(record[1]);
                output.append(line).append(System.lineSeparator());
            }
        }
    }
    logger.info("output:");
    logger.info(output.toString());
    logger.info("missing:");
    logger.info(missing.toString());
    if (docList.size() > 1) {
        logger.info("add to dataContext");
        is = new ByteArrayInputStream(output.toString().getBytes());
        dataContext.storeStream(is, props);
    } else {
        logger.info("do not add to dataContext");
    }
}
