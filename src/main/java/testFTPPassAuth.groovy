// Use for testing. Remove when adding to Data Process shape.
import com.boomi.execution.ContextCreator

// Place directory for multiple files and file name for single file
String pathFiles = "${System.getenv("PROJECT_DIR")}/input_files/emptyfile.txt"
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

import com.boomi.execution.ExecutionUtil
import java.util.Properties;
import java.io.InputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply;

logger = ExecutionUtil.getBaseLogger();

REMOTE_HOST = ExecutionUtil.getDynamicProcessProperty("dpp_ftp_host");
USERNAME = ExecutionUtil.getDynamicProcessProperty("dpp_ftp_username");
PASSWORD = ExecutionUtil.getDynamicProcessProperty("dpp_ftp_password");
REMOTE_PORT = ExecutionUtil.getDynamicProcessProperty("dpp_ftp_port");
REMOTE_DIRECTORY = ExecutionUtil.getDynamicProcessProperty("dpp_ftp_directory");
FILE_FILTER = ExecutionUtil.getDynamicProcessProperty("dpp_ftp_file_filter");

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
String[] filenames = ftpClient.listNames();
String fileFilter = FILE_FILTER.replaceAll("\\*", "");
fileFilter = (fileFilter == ".") ? "" : fileFilter;
logger.info("fileFilter: " + fileFilter);
for (String filename : filenames) {
    if (!fileFilter?.trim() || filename.toLowerCase().endsWith(fileFilter.toLowerCase())) {
        Properties props = new Properties();
        InputStream is = ftpClient.retrieveFileStream(filename);
        props.setProperty("document.dynamic.userdefined.ddp_filename", filename);
        dataContext.storeStream(is, props);
        if (ftpClient.completePendingCommand()) {
            logger.info(filename + " has been downloaded successfully.");
        }
        is.close();
    }
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