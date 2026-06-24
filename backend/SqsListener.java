import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SqsListener implements Runnable {
    private final SqsClient sqsClient;
    private final Gson gson;
    private final String queueUrl;
    private final BancoCore bancoCore;

    public SqsListener(String nombreCola, BancoCore bancoCore) {
        this.sqsClient = SqsClient.builder().region(Region.US_EAST_1).build();
        this.gson = new Gson();
        this.bancoCore = bancoCore;
        this.queueUrl = this.sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(nombreCola).build()).queueUrl();
    }

    @Override
    public void run() {
        while (true) {
            try {
                ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder().queueUrl(queueUrl).maxNumberOfMessages(10).waitTimeSeconds(20).build();

                for (Message mensaje : sqsClient.receiveMessage(receiveRequest).messages()) {
                    JsonObject snsEnvelope = JsonParser.parseString(mensaje.body()).getAsJsonObject();
                    JsonObject tx = gson.fromJson(snsEnvelope.get("Message").getAsString(), JsonObject.class);

                    bancoCore.transferir(
                        tx.get("sourceAccountId").getAsString(), 
                        tx.get("targetAccountId").getAsString(), 
                        tx.get("amount").getAsDouble()
                    );
                    
                    sqsClient.deleteMessage(DeleteMessageRequest.builder().queueUrl(queueUrl).receiptHandle(mensaje.receiptHandle()).build());
                }
            } catch (Exception e) { try { Thread.sleep(5000); } catch (InterruptedException ignored) {} }
        }
    }
}
