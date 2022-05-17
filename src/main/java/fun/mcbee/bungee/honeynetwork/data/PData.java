package fun.mcbee.bungee.honeynetwork.data;

import fun.mcbee.bungee.honeynetwork.HoneyNetwork;
import net.md_5.bungee.BungeeCord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.UUID;

public class PData {

    private UUID uuid;
    private long[] days = new long[7];
    private Long totalTime;
    private Long joinTime;
    private Date lastJoinBefore;

    public PData(UUID uuid) {
        this.uuid = uuid;
        this.joinTime = System.currentTimeMillis();
        BungeeCord.getInstance().getScheduler().runAsync(HoneyNetwork.getInstance(), new Runnable() {
            @Override
            public void run() {
                Statement stmt = null;
                String query = "SELECT * FROM networkmanagerdatabase where UUID = '" + uuid + "';";
                try {
                    stmt = MYSQL.GetConnection().createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    while (rs.next()) {
                        totalTime = rs.getLong("TotalTime");
                        for(int i = 0; i < days.length; i++) {
                            days[i] = rs.getLong("Day" + (i + 1));
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    if(stmt != null) {
                        try {
                            stmt.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if(totalTime == null) {//first join
                    totalTime = 0l;

                    stmt = null;
                    query = "INSERT INTO networkmanagerdatabase (UUID) VALUES ('" + uuid + "');";
                    try {
                        stmt = MYSQL.GetConnection().createStatement();
                        stmt.executeUpdate(query);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        if(stmt != null) {
                            try {
                                stmt.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                } else {

                    stmt = null;
                    query = "UPDATE networkmanagerdatabase SET LastLogin = now() WHERE (`UUID` = '" + uuid + "');";
                    try {
                        stmt = MYSQL.GetConnection().createStatement();
                        stmt.executeUpdate(query);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        if(stmt != null) {
                            try {
                                stmt.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }


            }
        });

    }

    public void Logout() {
        long timeDiffInSec = (System.currentTimeMillis() - this.joinTime) / 1000 + totalTime;

        Statement stmt = null;
        String query = "UPDATE networkmanagerdatabase SET TotalTime = " + timeDiffInSec + " WHERE (UUID = '" + uuid + "');";
        try {
            stmt = MYSQL.GetConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                totalTime = rs.getLong("TotalTime");
                for(int i = 0; i < days.length; i++) {
                    days[i] = rs.getLong("Day" + (i + 1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
