package src;

public class Message {
    public int typeMessage;
    public int sizeNickname;
    public String nickname;
    public int sizeMessage;
    public String message;

    public Message(int typeMessage, int sizeNickname, String nickname, int sizeMessage, String message) {
        this.typeMessage = typeMessage;
        this.sizeNickname = sizeNickname;
        this.nickname = nickname;
        this.sizeMessage = sizeMessage;
        this.message = message;
    }
}
