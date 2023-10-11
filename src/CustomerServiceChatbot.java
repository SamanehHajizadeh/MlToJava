import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import ai.verta.modeldb.Entities;
import ai.verta.modeldb.ModelDBConstants.Environment;
import ai.verta.modeldb.ModelDBConstants.ModelDBServiceResourceTypes;
import ai.verta.modeldb.ModelDBMessages;
import ai.verta.uac.UserInfo;
import ai.verta.uac.UserInfoServiceGrpc;
import ai.verta.uac.UserServiceGrpc;
import ai.verta.uac.Uac;
import ai.verta.uac.Uac.GetSelfProfile;
import com.google.rpc.Code;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ai.verta.modeldb.utils.ModelDBUtils;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.apache.commons.lang3.StringUtils;

public class CustomerServiceChatbot {

    private static final Logger LOGGER = LogManager.getLogger(CustomerServiceChatbot.class);

    private static final String KEY_FILE = "../../../credentials.json";

    public static void main(String[] args) {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    CustomerServiceChatbot.class.getResourceAsStream(KEY_FILE));

            ModelDBMessages.Credentials modelDBCreds = ModelDBUtils.getCredentialsFromGoogleCreds(credentials);

            // Initialize Vertex AI
            VertexAI.initialize(modelDBCreds, "your_project_id");

            ChatModel chatModel = ChatModel.fromPretrained("chat-bison@001");

            ChatModel.Parameters parameters = ChatModel.Parameters.newBuilder()
                    .setMaxOutputTokens(256)
                    .setTemperature(0.2)
                    .setTopP(0.8)
                    .setTopK(40)
                    .build();

            Chat chat = chatModel.startChat(
                    "You are a customer service representative of Cymbal.\n"
                            + "Here is Cymbal policy: How many days do I have to return my purchase?\n"
                            + "We offer free returns and exchanges within 30 days of your delivery, with exceptions as described in our Returns Policy. Certain items are designated as final sale and not eligible for returns or exchanges. All on-sale purchases are final.",
                    InputOutputTextPair.newBuilder()
                            .setInputText("I was in a car accident last month and couldn't return the item in the 30 days time window. Can you make an exception for me?")
                            .setOutputText("I understand that you had an emergency and were unable to return your order within the 30-day window. I would be happy to make an exception for you. Please contact our customer service department at 1-555-010-2667 and they will be able to assist you with your return.\n"
                                    + "Once your return is approved, you will be issued a return label. Please pack the item carefully and ship it back to us. We will process your refund within 3-5 business days of receiving the returned item.")
                            .build(),
                    InputOutputTextPair.newBuilder()
                            .setInputText("I forgot to return the item within 30 days. Can you make an exception for me?")
                            .setOutputText("I understand that you want to return the item, but we are unable to return your order since you have missed the 30-day window. Please let me know anything else I can assist you with.")
                            .build()
            );

            InputOutputText response = chat.sendMessage("I want to return my order. What's your return policy?", parameters);
            System.out.println("Response from Model: " + response.getText());

            response = chat.sendMessage("I ordered 30 days ago. Could you please help me with an exception? I was traveling abroad.", parameters);
            System.out.println("Response from Model: " + response.getText());
        } catch (Exception e) {
            LOGGER.error("Error: " + e.getMessage(), e);
        }
    }
}
