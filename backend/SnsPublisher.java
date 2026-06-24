import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public class SnsPublisher {
    private final SnsClient snsClient;
    private final Gson gson;
    // ARN
    private final String TOPIC_ARN = "arn:aws:sns:us-east-1:897913033260:TransaccionesAprobadas";

    public SnsPublisher() {
        this.snsClient = SnsClient.builder().region(Region.US_EAST_1).build();
        this.gson = new Gson();
    }

    public void publicarTransferencia(String source, String target, double amount) {
        try {
            Map<String, Object> tx = new HashMap<>();
            tx.put("sourceAccountId", source);
            tx.put("targetAccountId", target);
            tx.put("amount", amount);
            
            PublishRequest request = PublishRequest.builder()
                .message(gson.toJson(tx))
                .topicArn(TOPIC_ARN)
                .build();
            snsClient.publish(request);
        } catch (Exception e) { System.err.println("Error SNS: " + e.getMessage()); }
    }
}