import com.boomi.execution.ExecutionUtil;
import java.util.Properties;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import groovy.json.JsonSlurper;

logger = ExecutionUtil.getBaseLogger();
logger.info("Start Get CRM Access Token");
getAccessToken();

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);

    dataContext.storeStream(is, props);
}

def getAccessToken() {
    def clientId = ExecutionUtil.getProcessProperty("d3448f83-8c7e-4e82-b315-b3710acb774e","03fe1ad1-1698-4a3d-997f-3bf880c5b54a");
    def clientSecret = ExecutionUtil.getProcessProperty("d3448f83-8c7e-4e82-b315-b3710acb774e","9aa69e72-c793-468b-8025-0513a7cb4a1a");
    def resource = ExecutionUtil.getProcessProperty("d3448f83-8c7e-4e82-b315-b3710acb774e","5528e706-9f7e-4337-8e97-a9690cfaeb08");
    def tokenUrl = ExecutionUtil.getProcessProperty("d3448f83-8c7e-4e82-b315-b3710acb774e","86f30410-0704-4db2-8772-bfb576e6322b");
    def tenantId = ExecutionUtil.getProcessProperty("d3448f83-8c7e-4e82-b315-b3710acb774e","595703fd-e1fb-4c73-9bd4-17df428e3e54");
    url = new URL(tokenUrl.replace("tenantId", tenantId));
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("POST");
    def boundary = "----------------------------3pcZfkT948";
    connection.setRequestProperty("Content-type", "multipart/form-data; boundary=" + boundary);
    connection.setRequestProperty("Accept", "application/json");
    connection.setDoOutput(true);
    StringBuilder builder = new StringBuilder();
    builder.append("--").append(boundary);
    appendRequest(builder,"grant_type", "client_credentials", boundary);
    appendRequest(builder,"client_id", clientId, boundary);
    appendRequest(builder,"client_secret", clientSecret, boundary);
    appendRequest(builder,"resource", resource, boundary);
    builder.append("--").append("\r\n");
    try {
        OutputStream os = connection.getOutputStream();
        byte[] input = builder.toString().getBytes("utf-8");
        os.write(input, 0, input.length);
        os.close();
    } finally {
        
    }
    int code = connection.getResponseCode();
    logger.info("Response code: " + code);
    try {
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
        StringBuilder response = new StringBuilder();
        String responseLine = null;
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }
        logger.info(response.toString());
        def jsonSlurper = new JsonSlurper();
        def responseJson = jsonSlurper.parseText(response.toString());
        logger.info(responseJson.access_token);
        ExecutionUtil.setDynamicProcessProperty("crmAuthorization",responseJson.access_token,false);
        
    } finally {
        
    }
}

def appendRequest(builder,key,value,boundary) {
    String lineSeparator = "\r\n";
    builder.append(lineSeparator);
    builder.append(String.format("Content-Disposition: form-data; name=\"%s\"",key)).append(lineSeparator);
    builder.append(lineSeparator);
    builder.append(value).append(lineSeparator);
    builder.append("--").append(boundary);
}

