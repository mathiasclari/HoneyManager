package fun.mcbee.bungee.honeynetwork.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import fun.mcbee.bungee.honeynetwork.HoneyNetwork;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;

public class PData {
    private UUID uuid;

    private long[] days = new long[7];

    private Long totalTime;

    private Long joinTime;

    private Date lastJoinBefore;

    private int dayDiff;

    public PData(final UUID uuid) {
        this.uuid = uuid;
        this.joinTime = Long.valueOf(System.currentTimeMillis());
        BungeeCord.getInstance().getScheduler().runAsync((Plugin)HoneyNetwork.getInstance(), new Runnable() {
            public void run() {
                Statement stmt = null;
                String query = "SELECT *, DATEDIFF(now(), LastLogin) as diff FROM networkmanagerdatabase where UUID = '" + uuid + "';";
                try {
                    stmt = MYSQL.GetConnection().createStatement();
                    ResultSet rs = stmt.executeQuery(query);
                    while (rs.next()) {
                        PData.this.totalTime = Long.valueOf(rs.getLong("TotalTime"));
                        for (int i = 0; i < PData.this.days.length; i++)
                            PData.this.days[i] = rs.getLong("Day" + (i + 1));
                        PData.this.lastJoinBefore = rs.getDate("LastLogin");
                        PData.this.dayDiff = rs.getInt("diff");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    if (stmt != null)
                        try {
                            stmt.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                }
                if (PData.this.totalTime == null) {
                    PData.this.totalTime = Long.valueOf(0L);
                    stmt = null;
                    query = "INSERT INTO networkmanagerdatabase (UUID) VALUES ('" + uuid + "');";
                    try {
                        stmt = MYSQL.GetConnection().createStatement();
                        stmt.executeUpdate(query);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        if (stmt != null)
                            try {
                                stmt.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                    }
                } else if (PData.this.dayDiff == 0) {
                    stmt = null;
                    query = "UPDATE networkmanagerdatabase SET LastLogin = now() WHERE (`UUID` = '" + uuid + "');";
                    try {
                        stmt = MYSQL.GetConnection().createStatement();
                        stmt.executeUpdate(query);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        if (stmt != null)
                            try {
                                stmt.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                    }
                } else {
                    for (int i = 7; i > PData.this.dayDiff; i--) {
                        int index = i - PData.this.dayDiff;
                        if (index < 0) {
                            PData.this.days[i] = PData.this.days[index];
                        } else {
                            PData.this.days[i] = 0L;
                        }
                    }
                    stmt = null;
                    query = "UPDATE networkmanagerdatabase SET LastLogin = now(), Day1 = " + PData.this.days[0] + ", Day2 = " + PData.this.days[1] + ", Day3 = " + PData.this.days[2] + ", Day4 = " + PData.this.days[3] + ", Day5 = " + PData.this.days[4] + ", Day6 = " + PData.this.days[5] + ", Day7 = " + PData.this.days[6] + " WHERE (`UUID` = '" + uuid + "');";
                    try {
                        stmt = MYSQL.GetConnection().createStatement();
                        stmt.executeUpdate(query);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        if (stmt != null)
                            try {
                                stmt.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                    }
                }
            }
        });
    }

    public void Logout() {
        long timeDiffInSec = (System.currentTimeMillis() - this.joinTime.longValue()) / 1000L + this.totalTime.longValue();
        Statement stmt = null;
        String query = "UPDATE networkmanagerdatabase SET TotalTime = " + timeDiffInSec + ", Day1 = " + timeDiffInSec + " WHERE (UUID = '" + this.uuid + "');";
        try {
            stmt = MYSQL.GetConnection().createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }
    }

    public long GetSecondsTotalOnline() {
        return this.totalTime.longValue() + (System.currentTimeMillis() - this.joinTime.longValue()) / 1000L;
    }

    public long GetSecondsWeekOnline() {
        long time = (System.currentTimeMillis() - this.joinTime.longValue()) / 1000L;
        for (int i = 0; i < 7; i++)
            time += this.days[i];
        return time;
    }

    public static String GetTextTime(Long t) {
        List<Integer> times = new ArrayList<>();
        int years = (int)(t.longValue() / 3.1556926E7D);
        times.add(Integer.valueOf(years));
        t = Long.valueOf(t.longValue() - (long)(years * 3.1556926E7D));
        int days = (int)(t.longValue() / 86400.0D);
        times.add(Integer.valueOf(days));
        t = Long.valueOf(t.longValue() - (long)(days * 86400.0D));
        int hours = (int)(t.longValue() / 3600.0D);
        times.add(Integer.valueOf(hours));
        t = Long.valueOf(t.longValue() - (long)(hours * 3600.0D));
        int minutes = (int)(t.longValue() / 60.0D);
        times.add(Integer.valueOf(minutes));
        int seconds = t.intValue() - (int)(minutes * 60.0D);
        times.add(Integer.valueOf(seconds));
        String timeDispaly = "";
        String[] suffS = { "year", "day", "hour", "minute", "second" };
        for (int i = 0; i < times.size(); i++) {
            int time = ((Integer)times.get(i)).intValue();
            if (time != 0) {
                String suffix = suffS[i];
                if (!timeDispaly.isEmpty())
                    timeDispaly = timeDispaly + ", ";
                if (time == 1) {
                    timeDispaly = timeDispaly + time + " " + suffix;
                } else if (time > 1) {
                    timeDispaly = timeDispaly + time + " " + suffix + "s";
                }
            }
        }
        if (timeDispaly.isEmpty())
            timeDispaly = "0 seconds";
        return timeDispaly;
    }
}