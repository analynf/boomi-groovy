// Use for testing. Remove when adding to Data Process shape.
import com.boomi.execution.ContextCreator


// Place directory for multiple files and file name for single file
String pathFiles = "${System.getenv("PROJECT_DIR")}/input_files/whoContainer.json"
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
import org.json.JSONArray;
import org.apache.commons.lang3.StringUtils;

logger = ExecutionUtil.getBaseLogger();

logger.info("Number of documents: " + dataContext.getDataCount())
for (int i = 0; i < dataContext.getDataCount(); i++) {
    InputStream is = dataContext.getStream(i)
    Properties props = dataContext.getProperties(i)
    String data = is.text;
    JSONArray jsonArray = new JSONArray(data);
    String output = jsonArray.toString();
    output = StringUtils.replace(output, '"', '""');
    logger.info(output);

    ExecutionUtil.setDynamicProcessProperty("jsonContainers", "output", false)

    dataContext.storeStream(is, props)

}
