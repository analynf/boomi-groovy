// Use for testing. Remove when adding to Data Process shape.
import com.boomi.execution.ContextCreator

import java.time.LocalDate


// Place directory for multiple files and file name for single file
String pathFiles = "${System.getenv("PROJECT_DIR")}/input_files/wm1ShippingOrderItems.txt"
println pathFiles
dataContext = new ContextCreator()
dataContext.AddFiles(pathFiles)
ExecutionUtil ExecutionUtil = new ExecutionUtil()

/* Add any Dynamic Process Properties or Dynamic Document Properties. If
   setting DDPs for multiple files, then index needs to be set for each and
   the index range starts at 0. */
ExecutionUtil.setDynamicProcessProperty("DPP_name", "DPP_value", false)
dataContext.addDynamicDocumentPropertyValues(0, "DDP_name", "DDP_value")

ExecutionUtil.setDynamicProcessProperty("DPP_plannedGMDate", "20231017", false)

// Place script after this line.
//----------------------------------------------------------------------------------------------------


import java.util.Properties
import java.io.InputStream
import com.boomi.execution.ExecutionUtil;
import org.apache.commons.lang3.StringUtils;

logger = ExecutionUtil.getBaseLogger()

// Example of how to use logger.info() to print to console for testing and to logs within Boomi.
logger.info("Number of documents: " + dataContext.getDataCount())
def hasRelevantItems = false;
def zeroCapsuleCount = 0
def nosCapsuleCount = 0
def samCapsuleCount = 0
def capsuleHigh
def subOrderType
for (int i = 0; i < dataContext.getDataCount(); i++) {
    InputStream is = dataContext.getStream(i)
    Properties props = dataContext.getProperties(i)
    BufferedReader reader = new BufferedReader(new InputStreamReader(is))
    hasRelevantItems = true;

    while ((line = reader.readLine()) != null) {
        logger.info("line: " + line)
        String[] lineArr = line.split("\\*")

        logger.info("capsule: " + lineArr[9])
        if (StringUtils.equals(lineArr[9], "0CAPSULE")) {
            zeroCapsuleCount = zeroCapsuleCount + 1
        }
        if (StringUtils.contains(lineArr[9], "NOS")) {
            nosCapsuleCount = nosCapsuleCount + 1
        }
        if (StringUtils.contains(lineArr[9], "SAM")) {
            samCapsuleCount = samCapsuleCount + 1
        }
        capsuleHigh = lineArr[9] > capsuleHigh ? lineArr[9] : capsuleHigh

        is = new ByteArrayInputStream(line.getBytes())
        dataContext.storeStream(is, props)
    }
}
logger.info("capsuleHigh: " + capsuleHigh)
capsuleHigh = StringUtils.upperCase(capsuleHigh)
if (capsuleHigh?.trim()) {
    String capsHighDate = "";
    if (capsuleHigh == "NOS" || capsuleHigh == "SAM")
        capsHighDate = "20991231";
    else if (capsuleHigh == "0CAPSULE")
        capsHighDate = "19700101";
    else {
        def currentDate = LocalDate.now();
        logger.info("currentDate: " + currentDate)
        String dayMax = capsuleHigh.substring(7) == "A" ? "15" : "31";
        capsHighDate = currentDate.toString().substring(0, 2) + capsuleHigh.substring(0, 2) + capsuleHigh.substring(5, 7) + dayMax;
    }
    logger.info("capsHighDate: " + capsHighDate)
    def plannedGMDate = ExecutionUtil.getDynamicProcessProperty("DPP_plannedGMDate")
    logger.info("plannedGMDate: " + plannedGMDate)
    if (plannedGMDate <= capsHighDate) {
        subOrderType = "PRE";
    } else {
        subOrderType = "DAY";
    }
}

logger.info("zeroCapsuleCount: " + zeroCapsuleCount)
logger.info("nosCapsuleCount: " + nosCapsuleCount)
logger.info("samCapsuleCount: " + samCapsuleCount)
logger.info("subOrderType: " + subOrderType)
ExecutionUtil.setDynamicProcessProperty("DPP_hasRelevantItems", hasRelevantItems as String, false)
ExecutionUtil.setDynamicProcessProperty("DPP_0CapsuleCount", zeroCapsuleCount as String, false)
ExecutionUtil.setDynamicProcessProperty("DPP_NOSCapsuleCount", nosCapsuleCount as String, false)
ExecutionUtil.setDynamicProcessProperty("DPP_SAMCapsuleCount", samCapsuleCount as String, false)
ExecutionUtil.setDynamicProcessProperty("DPP_subOrderType", subOrderType, false)