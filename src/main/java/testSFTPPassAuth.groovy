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


import com.boomi.execution.ExecutionUtil;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.util.Properties;
import java.io.InputStream;

logger = ExecutionUtil.getBaseLogger();

REMOTE_HOST = "sftp.transgroup.com";
USERNAME = "BI_SEA_CPH";
PASSWORD = "Globalshipment@#2020\$one";
REMOTE_PORT = "22";
FILE_FILTER = "*.txt";
SESSION_TIMEOUT = 10000;
CHANNEL_TIMEOUT = 5000;

JSch jsch = new JSch();
Session session = jsch.getSession(USERNAME, REMOTE_HOST, Integer.valueOf(REMOTE_PORT));
session.setConfig("StrictHostKeyChecking", "no");

// authenticate using password
session.setPassword(PASSWORD);

// 10 seconds session timeout
session.connect(SESSION_TIMEOUT);

Channel sftp = session.openChannel("sftp");

// 5 seconds timeout
sftp.connect(CHANNEL_TIMEOUT);

ChannelSftp channelSftp = (ChannelSftp) sftp;

// transfer file from local to remote server
//channelSftp.put(localFile, remoteFile);

// download file from remote server to local
// channelSftp.get(remoteFile, localFile);
println channelSftp.pwd(); // TODO delete line
Vector<ChannelSftp.LsEntry> fileList = channelSftp.ls(FILE_FILTER);
for (ChannelSftp.LsEntry entry : fileList) {
    String filename = entry.getFilename();
    println filename;
    Properties props = new Properties();
    InputStream is = channelSftp.get(filename);
    props.setProperty("document.dynamic.userdefined.ddp_filename", filename);
    dataContext.storeStream(is, props);
}
channelSftp.exit();
session.disconnect();