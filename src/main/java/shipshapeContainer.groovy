// Use for testing. Remove when adding to Data Process shape.
import com.boomi.execution.ContextCreator


// Place directory for multiple files and file name for single file
String pathFiles = "${System.getenv("PROJECT_DIR")}/input_files/shipshapeContainer.txt"
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

// Example of how to use logger.info() to print to console for testing and to logs within Boomi.
logger.info("Number of documents: " + dataContext.getDataCount())
Map<String, Integer> containers = new HashMap<>();
for (int i = 0; i < dataContext.getDataCount(); i++) {
    InputStream is = dataContext.getStream(i)
    Properties props = dataContext.getProperties(i)
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

    StringBuilder output = new StringBuilder();
    while ((line = reader.readLine()) != null) {
        logger.info("line: " + line);
        String[] lineArr = line.split("\\*");
        if (StringUtils.isNotEmpty(lineArr[1])) {
            if (containers.containsKey(lineArr[1])) {
                Integer pkg = Integer.valueOf(containers.get(lineArr[1]));
                containers.put(lineArr[1], pkg + Integer.valueOf(lineArr[4]));
            } else {
                containers.put(lineArr[1], lineArr[4]); // containerNumber, noPackages
            }
            logger.info("packages: " + containers);

            if (StringUtils.isNotEmpty(lineArr[1]) && StringUtils.isNotEmpty(lineArr[2]) && !output.contains(lineArr[1])) {
                output.append(line).append(System.lineSeparator());
            }
        }
    }
    StringBuilder result = new StringBuilder();
    if (StringUtils.isNotEmpty(output)) {
        for (String lineOutput : new LinkedHashSet<String>(Arrays.asList(output.toString().split(System.lineSeparator())))) {
            String[] lineSplit = lineOutput.split("\\*");
            lineSplit[4] = containers.get(lineSplit[1]);
            String joinedStr = String.join("*", Arrays.asList(lineSplit));
            result.append(joinedStr).append(System.lineSeparator());
        }
    }
    is = new ByteArrayInputStream(result.toString().getBytes());
    dataContext.storeStream(is, props)
}
ExecutionUtil.setDynamicProcessProperty("DPP_containerCount", String.valueOf(containers.size()), false);