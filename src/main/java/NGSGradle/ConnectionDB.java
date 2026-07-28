package NGSGradle;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
public class ConnectionDB {
    static Connection con;
    public static BufferedImage bgLoginMain;
    public static void connectDB(){
        try {
            
            bgLoginMain=ImageIO.read(new File(System.getProperty("user.dir").concat("/src/main/java/images/w11.jpg")));

            String urlDB = System.getProperty("user.dir").concat("/DB.accdb");
        System.out.println(urlDB);
            con = DriverManager.getConnection("jdbc:ucanaccess://" + urlDB + ";memory=true");
        } catch (SQLException ex) {
            Logger.getLogger(ConnectionDB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConnectionDB.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }
    public static void closeDB(){
        try {
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ConnectionDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
