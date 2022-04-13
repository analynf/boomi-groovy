import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply;

REMOTE_HOST = "sftp.scangl.com";
USERNAME = "ShipShapeTest";
PASSWORD = "=5p1+!JZ#wEk";
REMOTE_PORT = 21;
REMOTE_DIRECTORY = "/FromSGL";
FILE_FILTER = "*.xml"

FTPClient ftpClient = new FTPClient();

ftpClient.connect(REMOTE_HOST, REMOTE_PORT);
showServerReply(ftpClient);
replyCode = ftpClient.getReplyCode();
if (!FTPReply.isPositiveCompletion(replyCode)) {
    throw new RuntimeException("Connect failed. Server reply code: " + replyCode);
}
ftpClient.enterLocalPassiveMode();
success = ftpClient.login(USERNAME, PASSWORD);
showServerReply(ftpClient);
if (!success) {
    throw new RuntimeException("Could not login to the server");
}

success = ftpClient.changeWorkingDirectory(REMOTE_DIRECTORY);
showServerReply(ftpClient);
if (!success) {
    throw new RuntimeException("Failed to change working directory");
}

println ftpClient.printWorkingDirectory();
println "listing files..."
String[] filenames = ftpClient.listNames();
String fileFilter = FILE_FILTER.replaceAll("\\*", "");
fileFilter = (fileFilter == ".") ? "" : fileFilter;
println "fileFilter: " + fileFilter;
// using retrieveFile(String, OutputStream)
//for (String filename : filenames) {
//    if (!fileFilter?.trim() || filename.toLowerCase().endsWith(fileFilter.toLowerCase())) {
//        println "download file: " + filename;
//        File downloadFile = new File("C:\\Boomi\\ftp\\" + filename);
//        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
//        boolean success = ftpClient.retrieveFile(filename, outputStream);
//        outputStream.close()
//        if (success) {
//            println filename + " has been downloaded successfully.";
//        }
//    }
//}

// using retrieveFileStream(String)
for (String filename : filenames) {
    if (!fileFilter?.trim() || filename.toLowerCase().endsWith(fileFilter.toLowerCase())) {
        println "download file: " + filename;
        File downloadFile = new File("C:\\Boomi\\ftp\\"+filename);
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
        InputStream inputStream = ftpClient.retrieveFileStream(filename);
        byte[] bytesArray = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(bytesArray)) != -1) {
            outputStream.write(bytesArray, 0, bytesRead);
        }
        if(!ftp.completePendingCommand()) {
            ftp.logout();
            ftp.disconnect();
            System.err.println("File transfer failed.");
            System.exit(1);
        }
        success = ftpClient.completePendingCommand();
        if (success) {
            println filename + " has been downloaded successfully.";
        }
        outputStream.close();
        inputStream.close();
    }
}

// logs out and disconnects from server
ftpClient.logout();
ftpClient.disconnect();

def showServerReply(FTPClient ftpClient) {
    String[] replies = ftpClient.getReplyStrings();
    if (replies != null && replies.length > 0) {
        for (String aReply : replies) {
            println "SERVER: " + aReply
        }
    }
}
