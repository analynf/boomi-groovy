import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

REMOTE_HOST = "sftp.scangl.com";
//USERNAME = "ShipShape";
//PASSWORD = "X8\$Mz57w7ZlL";
USERNAME = "ShipShapeTest";
PASSWORD = "=5p1+!JZ#wEk";
REMOTE_PORT = 22;
SESSION_TIMEOUT = 10000;
CHANNEL_TIMEOUT = 5000;
FILE_FILTER="(.*?).xml|(.*?).XML"

JSch jsch = new JSch();
//jsch.setKnownHosts(new ByteArrayInputStream("scangl.com".getBytes()));
Session jschSession = jsch.getSession(USERNAME, REMOTE_HOST, REMOTE_PORT);
jschSession.setConfig("StrictHostKeyChecking", "no");

// authenticate using private key
// jsch.addIdentity("/home/mkyong/.ssh/id_rsa");

// authenticate using password
jschSession.setPassword(PASSWORD);

// 10 seconds session timeout
jschSession.connect(SESSION_TIMEOUT);

Channel sftp = jschSession.openChannel("sftp");

// 5 seconds timeout
sftp.connect(CHANNEL_TIMEOUT);

ChannelSftp channelSftp = (ChannelSftp) sftp;

// transfer file from local to remote server
//channelSftp.put(localFile, remoteFile);

// download file from remote server to local
// channelSftp.get(remoteFile, localFile);

channelSftp.cd("/FromSGL")
println channelSftp.pwd();
Vector<ChannelSftp.LsEntry> fileList = channelSftp.ls(FILE_FILTER);
for (ChannelSftp.LsEntry entry : fileList) {
    println entry.getFilename();
}

channelSftp.exit();
jschSession.disconnect();
