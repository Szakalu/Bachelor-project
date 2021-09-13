package math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase;

/**
 * Created by Jacek on 09.04.2017.
 */

public class SignInParameters {

    private String userName;
    private String password;

    public SignInParameters(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
