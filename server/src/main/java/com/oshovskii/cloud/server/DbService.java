package com.oshovskii.cloud.server;


import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DbService {
    private static Connection connection;
    private static Statement stmt;

    public static void connect() {
        System.out.println("Плдключаемся к базе");
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            stmt = connection.createStatement();
            System.out.println("База логинов подключена");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addUser(String login, String pass, String nick) {
        try {
        String query = "INSERT INTO main (login, password, nickname) VALUES (?, ?, ?);";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, login);
        ps.setInt(2, pass.hashCode());
        ps.setString(3, nick);
        ps.executeUpdate();
         } catch (SQLException e) {
            e.printStackTrace();
         }
    }

    public static void addUserToBlackListForSpecificUser(String nickWhoRequest, String nickToBlock) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM blacklist " +
                    "WHERE user_id = (SELECT id FROM main where nickname = ?) " +
                    "AND blocked_user_id = (SELECT if FROM main where nickname = ?)");
            ps.setString(1, nickWhoRequest);
            ps.setString(2, nickToBlock);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeUserToBlackListForSpecificUser(String nickWhoRequest, String nickToBlock) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM blacklist " +
                    "WHERE user_id = (SELECT id FROM main where nickname = ?) " +
                    "AND blocked_user_id = (SELECT id FROM main where nickname = ?)");
            ps.setString(1, nickWhoRequest);
            ps.setString(2, nickToBlock);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getBlacklist(String nickWhoRequest) {
        StringBuilder blackListString = new StringBuilder(getBlackList(nickWhoRequest).get(0));

        for (int i = 1; i < getBlackList(nickWhoRequest).size(); i++) {
            blackListString.append(", ").append(getBlackList(nickWhoRequest).get(i));
        }
        return blackListString.toString();
    }

    public static List<String> getBlackList(String nickWhoRequest) {
        List<String> blockedNicks = new ArrayList<>();
        try {
            ResultSet nickNamesFromDB = stmt.executeQuery("SELECT id, nickname from main");
            HashMap<Integer, String> nickNameMap = new HashMap();
            while (nickNamesFromDB.next()) {
                nickNameMap.put(nickNamesFromDB.getInt(1), nickNamesFromDB.getString(2));
            }
            ResultSet rs = stmt.executeQuery("SELECT blacklist.blocked_user_id FROM main " +
                    "INNER JOIN blacklist on main.id = blacklist.user_id " +
                    "where nickname='" + nickWhoRequest + "'");
            while (rs.next()) {
                int bNId = rs.getInt(1);
                for (Integer key : nickNameMap.keySet()) {
                    if (key == bNId)
                        blockedNicks.add(nickNameMap.get(bNId));
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return blockedNicks;
    }

    public static String getNickByLoginAndPass(String login, String pass) {
        try {
            ResultSet rs = stmt.executeQuery("SELECT nickname, password FROM main WHERE login = '" + login + "'");
            int myHash = Integer.parseInt(pass); // 137
            if (rs.next()) {
                String nick = rs.getString(1);
                int dbHash = rs.getInt(2);
                if (myHash == dbHash) {
                    return nick;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void log(LogEventType logEventType, String login) {
        int logId = getLoginId(login);
        if (logId != 0) {
            try {
                String query = "INSERT INTO logs (id, event_type, login) VALUES (NULL, " +
                        logEventType.eTypeId + ", ?);";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, String.valueOf(logId));
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // TODO не существующие логины в базе можно  обработать
        }
    }

    private static int getLoginId(String login) {
        int res = 0;
        try {
            ResultSet rs = stmt.executeQuery("SELECT id FROM main WHERE login = '" + login + "'");
            while (rs.next()){
                System.out.println(rs.getInt(1));
                res = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static boolean checkIfNickIsAvailableInDB(String nick) {
        try {
            ResultSet result = stmt.executeQuery("SELECT count(*) AS count FROM main where nickname='" + nick + "'");
            int count = result.getInt("count");
            if (count == 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkIfLoginIsAvailableInDB(String login) {
        try {
            ResultSet result = stmt.executeQuery("SELECT count(*) AS count FROM main where login='" + login + "'");
            int count = result.getInt("count");
            if (count == 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    enum LogEventType {
        LOGIN(0),
        LOGOUT(1),
        INCORRECT_LOGIN(2);

        private final int eTypeId;

        LogEventType(int eTypeId) {
            this.eTypeId = eTypeId;
        }
    }
}
