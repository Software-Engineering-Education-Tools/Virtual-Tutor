package de.ur.mi.roberts;

/**
 * Created by Jonas Roberts on 20.09.2017.
 */
public interface AssistantInteractionListener {


    void onMessageReceived(String message);
    void onMessageSent(String message);

}
