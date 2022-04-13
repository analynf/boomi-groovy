// Use for testing. Remove when adding to Data Process shape.
import com.boomi.execution.ContextCreator

// Place directory for multiple files and file name for single file
String pathFiles = "${System.getenv("PROJECT_DIR")}/input_files/test.txt"
println pathFiles
dataContext = new ContextCreator()
dataContext.AddFiles(pathFiles)
ExecutionUtil ExecutionUtil = new ExecutionUtil()

/* Add any Dynamic Process Properties or Dynamic Document Properties. If
   setting DDPs for multiple files, then index needs to be set for each and
   the index range starts at 0. */
ExecutionUtil.setDynamicProcessProperty("DPP_name", "DPP_value", false)
dataContext.addDynamicDocumentPropertyValues(0, "ddp_filename", "test.txt")


// Place script after this line.
//----------------------------------------------------------------------------------------------------

import com.boomi.execution.ExecutionUtil
import java.util.Properties;
import java.io.InputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply;

logger = ExecutionUtil.getBaseLogger();

REMOTE_HOST = "sftp.scangl.com";
USERNAME = "ShipShapeTest";
PASSWORD = "=5p1+!JZ#wEk";
REMOTE_PORT = 21;
REMOTE_DIRECTORY = "/ToSGL";

FTPClient ftpClient = new FTPClient();

ftpClient.connect(REMOTE_HOST, Integer.valueOf(REMOTE_PORT));
showServerReply(ftpClient);
replyCode = ftpClient.getReplyCode();
if (!FTPReply.isPositiveCompletion(replyCode)) {
    throw new RuntimeException("Connect failed. Server reply code: " + replyCode);
}
success = ftpClient.login(USERNAME, PASSWORD);
showServerReply(ftpClient);
if (!success) {
    throw new RuntimeException("Could not login to the server");
}
ftpClient.enterLocalPassiveMode();
ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

success = ftpClient.changeWorkingDirectory(REMOTE_DIRECTORY);
showServerReply(ftpClient);
if (!success) {
    throw new RuntimeException("Failed to change working directory");
}

logger.info("Current working directory: " + ftpClient.printWorkingDirectory());
for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);
    String filename = props.getProperty("document.dynamic.userdefined.ftp_filename");
    if (ftpClient.storeFile(filename, is)) {
        logger.info(filename + " has been uploaded successfully.");
    } else {
        throw new RuntimeException("Could not upload file: " + filename);
    }
    dataContext.storeStream(is, props);
}

// logs out and disconnects from server
ftpClient.logout();
ftpClient.disconnect();

def showServerReply(FTPClient ftpClient) {
    String[] replies = ftpClient.getReplyStrings();
    if (replies != null && replies.length > 0) {
        for (String aReply : replies) {
            logger.info("SERVER: " + aReply);
        }
    }
}