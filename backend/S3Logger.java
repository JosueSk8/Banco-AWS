import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.sync.RequestBody;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;

public class S3Logger {
    private final S3Client s3Client;
    private final Gson gson;
    private final String BUCKET_NAME = "minibanco-logs-tx"; 

    public S3Logger() {
        this.s3Client = S3Client.builder().region(Region.US_EAST_1).build();
        this.gson = new Gson();
    }

    public void registrarTransaccion(String source, String target, double amount, long secuencia) {
        try {
            Map<String, Object> tx = new HashMap<>();
            tx.put("sourceAccountId", source);
            tx.put("targetAccountId", target);
            tx.put("amount", amount);

            String fileName = String.format("tx-%07d.json", secuencia);
            PutObjectRequest putOb = PutObjectRequest.builder().bucket(BUCKET_NAME).key(fileName).build();
            s3Client.putObject(putOb, RequestBody.fromString(gson.toJson(tx)));
        } catch (Exception e) {}
    }

    public void recuperarDesdeS3(long ultimaTxLocal, BancoCore bancoCore) {
        System.out.println("Buscando logs en S3 desde la tx: " + ultimaTxLocal);
        try {
            ListObjectsV2Request listReq = ListObjectsV2Request.builder().bucket(BUCKET_NAME).prefix("tx-").build();
            ListObjectsV2Response listRes;
            do {
                listRes = s3Client.listObjectsV2(listReq);
                for (S3Object obj : listRes.contents()) {
                    long secS3 = Long.parseLong(obj.key().substring(3, obj.key().indexOf(".json")));
                    if (secS3 > ultimaTxLocal) {
                        GetObjectRequest getReq = GetObjectRequest.builder().bucket(BUCKET_NAME).key(obj.key()).build();
                        String json = s3Client.getObjectAsBytes(getReq).asUtf8String();
                        JsonObject tx = gson.fromJson(json, JsonObject.class);
                        bancoCore.transferir(
                            tx.get("sourceAccountId").getAsString(), 
                            tx.get("targetAccountId").getAsString(), 
                            tx.get("amount").getAsDouble()
                        );
                        System.out.println("Recuperada TX: " + obj.key());
                    }
                }
                listReq = listReq.toBuilder().continuationToken(listRes.nextContinuationToken()).build();
            } while (listRes.isTruncated());
            System.out.println("Recuperación completada.");
        } catch (Exception e) { System.err.println("Fallo en S3: " + e.getMessage()); }
    }
}