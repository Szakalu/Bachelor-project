package math.uni.lodz.pl.jacek.traveler;

/**
 * Created by Jacek on 28.07.2017.
 */

public class DatabaseConnector {

    public final String url = "jdbc:mysql://192.168.0.101:3306/Traveler_New?useUnicode=true&characterEncoding=UTF-8";
    public final String username = "user";
    public final  String password = "qwerty";

    public DatabaseConnector() {
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
