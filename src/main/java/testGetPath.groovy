// Use for testing. Remove when adding to Data Process shape.
import com.boomi.execution.ContextCreator

// Place directory for multiple files and file name for single file
String pathFiles = "${System.getenv("PROJECT_DIR")}/input_files/path.txt"
println pathFiles
dataContext = new ContextCreator()
dataContext.AddFiles(pathFiles)
ExecutionUtil ExecutionUtil = new ExecutionUtil()

/* Add any Dynamic Process Properties or Dynamic Document Properties. If
   setting DDPs for multiple files, then index needs to be set for each and
   the index range starts at 0. */
ExecutionUtil.setDynamicProcessProperty("DPP_name", "DPP_value", false)
dataContext.addDynamicDocumentPropertyValues(0, "connector.track.ftp.filename", "test\\1\\file.txt")



// Place script after this line.
//----------------------------------------------------------------------------------------------------


import java.util.Properties;
import java.io.InputStream;
import com.boomi.execution.ExecutionUtil;

logger = ExecutionUtil.getBaseLogger();
//def regex = ~"([^\\]*$)"
for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);
    File f = new File(null);
    String contentFolder =  "test\\1\\file.txt" - f.getName();
    logger.info("Folder: " + contentFolder);

    props.setProperty("document.dynamic.userdefined.contentFolder", contentFolder);

    dataContext.storeStream(is, props);
}