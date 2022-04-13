// Use for testing. Remove when adding to Data Process shape.
import com.boomi.execution.ContextCreator


// Place directory for multiple files and file name for single file
String pathFiles = "${System.getenv("PROJECT_DIR")}/input_files/escapeString.txt"
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

for (int i = 0; i < dataContext.getDataCount(); i++) {
    InputStream is = dataContext.getStream(i)
    Properties props = dataContext.getProperties(i)
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

    String output = "";
    StringBuilder result = new StringBuilder();
    while ((line = reader.readLine()) != null) {
        output = StringUtils.replace(line, "&", "&amp;");
        output = StringUtils.replace(output, "'", "&quot;");
        output = StringUtils.replace(output, "\"", "&quot;");
        output = StringUtils.replace(output, "<", "&lt;");
        output = StringUtils.replace(output, ">", "&gt;");

        result.append(output).append(System.lineSeparator());
    }
    logger.info(result.toString());
    is = new ByteArrayInputStream(result.toString().getBytes());
    dataContext.storeStream(is, props)
}