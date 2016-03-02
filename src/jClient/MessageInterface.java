package jClient;

/**
 * Interface for calling methods in the controller-class from the model-class without having a direct reference
 * to the controller-class from the model-class.
 *
 * These methods are implemented in the ClientController-class and then called from ClientModel.
 */

/**
 * Created by Jonas on 2016-02-25.
 */

interface MessageInterface {
    void appendOrangeBold();

    void appendRed();

    void appendGreen();

    void appendBlue();

    void appendPurpleBold();

    void appendRegular();

    void sendUserList();

    void changeUserName();
}
