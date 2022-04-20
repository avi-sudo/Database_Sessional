package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sample.ReviewScreenController.Review;
import java.sql.*;

public class PGConnector {
    private final String url = "jdbc:postgresql://localhost/Restaurant Finder And Rating App Database";
    private final String user = "postgres";
    private final String password = "navid123";
    Connection connection;
    boolean isConnected = false;


    public Connection connect() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
            isConnected = true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return connection;
    }

    public ResultSet findRestaurant(String value) {
        if(!isConnected)
            connection = connect();
        ResultSet rs = null;
        String query = "select basic_search(?,1)";
        try{
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, value);
            rs = pst.executeQuery();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    public ResultSet findRestaurant(String value, int ordering, int prange,
                                    int visited, boolean liked, boolean listed,
                                    boolean lbpif, boolean wifi, boolean takeout,
                                    boolean delivery, boolean outdoorSeating,
                                    boolean reservation, boolean creditCard,
                                    boolean parking, String username) throws Exception{
        if(!isConnected)
            connection = connect();
        String query = "Select id from customer where username = ?";
        ResultSet rs1;
        PreparedStatement pst = connection.prepareStatement(query);
        pst.setString(1, username);
        rs1 = pst.executeQuery();
        rs1.next();
        int uid = rs1.getInt(1);
        query = "Select main_search(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        pst = connection.prepareStatement(query);
        pst.setString(1, value);
        pst.setInt(2, ordering);
        pst.setInt(3, prange);
        pst.setInt(4, visited);
        pst.setBoolean(5, liked);
        pst.setBoolean(6, listed);
        pst.setBoolean(7, lbpif);
        pst.setBoolean(8, wifi);
        pst.setBoolean(9, takeout);
        pst.setBoolean(10, delivery);
        pst.setBoolean(11, outdoorSeating);
        pst.setBoolean(12, reservation);
        pst.setBoolean(13, creditCard);
        pst.setBoolean(14, parking);
        pst.setInt(15, uid);
        ResultSet rs2 = pst.executeQuery();
        return rs2;
    }

    public boolean verifyLogin(String username, String password) throws Exception {
        if(!isConnected)
            connection = connect();
        ResultSet rs = null;
        Boolean flag = false;
        String query = "Select count(*) from customer where username = ? and " +
                "password = MD5(?)";
        try{
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, username);
            pst.setString(2, password);
            rs = pst.executeQuery();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        while(rs.next()) {
            int count = rs.getInt(1);
            if (count == 1)
                flag =  true;
            else flag = false;
            break;
        }
        return flag;
    }

    public boolean createAccount(String fullname, String username, String password) throws Exception {
        if(!isConnected)
            connection = connect();

        boolean flag = false;
        String query = "Select create_user(?,?,?)";
        PreparedStatement pst = connection.prepareStatement(query);
        pst.setString(1, username);
        pst.setString(2, fullname);
        pst.setString(3, password);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            String ret = rs.getString(1);
            System.out.println(ret);
            if(ret.equals("t"))
                flag = true;
            else flag = false;
        }

        return flag;
    }

    public ResultSet getRestaurantDetails(String rname) throws Exception{
        if(!isConnected)
            connection = connect();
        String query = "Select * from restaurant where name = ?";
        PreparedStatement pst = connection.prepareStatement(query);
        pst.setString(1, rname);
        ResultSet rs = pst.executeQuery();
        return rs;
    }

    public boolean doesLike(String rname, String username) throws Exception {
        if(!isConnected)
            connection = connect();
        String query = "Select count(*) from likes l, customer c, restaurant r where" +
                " l.restaurant_id = r.id and l.user_id = c.id and r.name = ? and c.username = ?";
        PreparedStatement pst = connection.prepareStatement(query);
        pst.setString(1, rname);
        pst.setString(2, username);
        ResultSet rs = pst.executeQuery();
        rs.next();
        if(rs.getInt(1) > 0)
            return true;
        else return false;
    }

    public void customInsert(String tname, String val1, String val2, int val3) throws Exception {
        if(!isConnected)
            connection = connect();
        String query = "Select custom_insert(?,?,?,?)";
        PreparedStatement pst = connection.prepareStatement(query);
        pst.setString(1, tname);
        pst.setString(2, val1);
        pst.setString(3, val2);
        pst.setInt(4, val3);
        pst.executeQuery();
    }

    public int getRatingGiven(String rname, String username) throws Exception{
        if(!isConnected)
            connection = connect();
        int rating = 1;
        String query = "Select r.rating from rates r, restaurant rt, customer c " +
                "where r.restaurant_id = rt.id and r.user_id = c.id and " +
                "rt.name = ? and c.username = ?";
        PreparedStatement pst = connection.prepareStatement(query);
        pst.setString(1, rname);
        pst.setString(2, username);
        ResultSet rs = pst.executeQuery();
        if(rs.next()) {
            rating = rs.getInt(1);
        }

       return rating;
    }

    public void addReview(String rname, String username, String comment) throws Exception{
        if(!isConnected)
            connection = connect();
        String query = "Select add_review(?,?,?)";
        PreparedStatement pst = connection.prepareStatement(query);
        pst.setString(1, rname);
        pst.setString(2, username);
        pst.setString(3, comment);
        pst.executeQuery();
    }

    public ObservableList<Review> getReviews(String rname) throws Exception{
        if(!isConnected)
            connection = connect();
        ObservableList<Review> data = FXCollections.observableArrayList();
        String query = "Select c.username, rv.comment, rv.date  from customer c,restaurant r, review rv " +
                "where r.id = rv.restaurant_id and r.name = ? and c.id = rv.user_id";
        PreparedStatement pst = connection.prepareStatement(query);
        pst.setString(1, rname);
        ResultSet rs = pst.executeQuery();
        while(rs.next()) {
            data.add(new Review(rs.getString(1),rs.getString(2),
                    rs.getString(3)));
        }
        return data;
    }
}
