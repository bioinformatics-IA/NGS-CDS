package NGSGradle;




import NGSGradle.ConnectionDB;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.awt.Graphics;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class LOGINFrame extends javax.swing.JFrame {

    PreparedStatement stmt;
    ResultSet rs;
////////////////////////////////////////////////////////////////////////////////
////////////////////////      CONSTRUCTOR       ////////////////////////////////
////////////////////////////////////////////////////////////////////////////////    

    public LOGINFrame() {
        ConnectionDB.connectDB();
System.out.println(getClass().getResource(""));
        initComponents();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);

        //Component Setting
        //Panels Setting
        loginPanel.setVisible(true);
        forgotPanel.setVisible(false);
        signupPanel.setVisible(false);

    }//Constructor
////////////////////////////////////////////////////////////////////////////////
////////////////////////      FUNCTIONS       ////////////////////////////////
//////////////////////////////////////////////////////////////////////////////// 
// 1- Function check the valid email address 

    public static boolean isValidEmailAddress(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."
                + "[a-zA-Z0-9_+&*-]+)*@"
                + "(?:[a-zA-Z0-9-]+\\.)+[a-z"
                + "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null) {
            return false;
        }
        return pat.matcher(email).matches();
    }

////////////////////////////////////////////////////////////////////////////////
////////////////////////      FUNCTIONS END       ////////////////////////////////
////////////////////////////////////////////////////////////////////////////////     
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        MainPanel = new javax.swing.JPanel(){
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(ConnectionDB.bgLoginMain, 0, 0, null);
            }
        };
        loginPanel = new javax.swing.JPanel()
        ;
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        username = new javax.swing.JTextField();
        userpwd = new javax.swing.JPasswordField();
        signinButton = new javax.swing.JButton();
        forgotpasswordLabel = new javax.swing.JLabel();
        signinLabel = new javax.swing.JLabel();
        Logo = new javax.swing.JLabel();
        pwdWarning = new javax.swing.JLabel();
        userWarning = new javax.swing.JLabel();
        forgotPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        savePassbtn = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        userEmail = new javax.swing.JTextField();
        newPass1 = new javax.swing.JPasswordField();
        newPass2 = new javax.swing.JPasswordField();
        forgotPLabel = new javax.swing.JLabel();
        emailWarning = new javax.swing.JLabel();
        newpwdWarning = new javax.swing.JLabel();
        tickLabel = new javax.swing.JLabel();
        signupPanel = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        createUsername = new javax.swing.JTextField();
        createEmail = new javax.swing.JTextField();
        createPass1 = new javax.swing.JPasswordField();
        createPass2 = new javax.swing.JPasswordField();
        createaccountBtn = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        signupLabel = new javax.swing.JLabel();
        createEmailWarning = new javax.swing.JLabel();
        createUsernameWarning = new javax.swing.JLabel();
        createPwdWarning = new javax.swing.JLabel();
        createEmailTick = new javax.swing.JLabel();
        createUsernameTick = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        loginPanel.setOpaque(false);

        jLabel1.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/src/main/java/images/userLogin3.png"))); // NOI18N

        jLabel2.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/src/main/java/images/pass1.png"))); // NOI18N

        username.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        username.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                usernameFocusGained(evt);
            }
        });

        userpwd.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        userpwd.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                userpwdFocusGained(evt);
            }
        });

        signinButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        signinButton.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/src/main/java/images/SigninB3.png"))); // NOI18N
        signinButton.setText("Signin");
        signinButton.setPressedIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/src/main/java/images/SigninB2.png"))); // NOI18N
        signinButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                signinButtonActionPerformed(evt);
            }
        });

        forgotpasswordLabel.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        forgotpasswordLabel.setForeground(new java.awt.Color(255, 255, 255));
        forgotpasswordLabel.setText("Forgot Password?");
        forgotpasswordLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                forgotpasswordLabelMouseClicked(evt);
            }
        });

        signinLabel.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        signinLabel.setForeground(new java.awt.Color(255, 255, 255));
        signinLabel.setText("Signup");
        signinLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                signinLabelMouseClicked(evt);
            }
        });

        Logo.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 24)); // NOI18N
        Logo.setForeground(new java.awt.Color(255, 255, 255));
        Logo.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/src/main/java/images/loginMain4.png"))); // NOI18N
        Logo.setText("LOGIN");
        Logo.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        Logo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                LogoMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                LogoMouseExited(evt);
            }
        });

        pwdWarning.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 12)); // NOI18N
        pwdWarning.setForeground(new java.awt.Color(255, 255, 255));

        userWarning.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 12)); // NOI18N
        userWarning.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout loginPanelLayout = new javax.swing.GroupLayout(loginPanel);
        loginPanel.setLayout(loginPanelLayout);
        loginPanelLayout.setHorizontalGroup(
            loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginPanelLayout.createSequentialGroup()
                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, loginPanelLayout.createSequentialGroup()
                        .addGap(340, 340, 340)
                        .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(forgotpasswordLabel)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, loginPanelLayout.createSequentialGroup()
                                .addComponent(signinLabel)
                                .addGap(8, 8, 8))))
                    .addGroup(loginPanelLayout.createSequentialGroup()
                        .addGap(79, 79, 79)
                        .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(loginPanelLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(28, 28, 28)
                                .addComponent(username))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, loginPanelLayout.createSequentialGroup()
                                .addGap(76, 76, 76)
                                .addComponent(userWarning, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(loginPanelLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(33, 33, 33)
                                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(pwdWarning, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(userpwd, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE))))))
                .addGap(78, 78, 78))
            .addGroup(loginPanelLayout.createSequentialGroup()
                .addGap(194, 194, 194)
                .addComponent(Logo)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, loginPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(signinButton, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(206, 206, 206))
        );
        loginPanelLayout.setVerticalGroup(
            loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginPanelLayout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(Logo)
                .addGap(50, 50, 50)
                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(username, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(userWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(userpwd, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pwdWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addComponent(signinButton)
                .addGap(53, 53, 53)
                .addComponent(forgotpasswordLabel)
                .addGap(40, 40, 40)
                .addComponent(signinLabel)
                .addGap(95, 95, 95))
        );

        forgotPanel.setOpaque(false);

        jLabel5.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 16)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Enter your E-mail:");

        jLabel6.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 16)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Enter new password:");

        jLabel7.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 16)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Confirm Password:");

        savePassbtn.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        savePassbtn.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/src/main/java/images/save1.png"))); // NOI18N
        savePassbtn.setText("Save");
        savePassbtn.setPressedIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/src/main/java/images/save2.png"))); // NOI18N
        savePassbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savePassbtnActionPerformed(evt);
            }
        });

        jButton4.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        jButton4.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/src/main/java/images/cancle1.png"))); // NOI18N
        jButton4.setText("Cancel");
        jButton4.setPressedIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/src/main/java/images/cancle2.png"))); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        userEmail.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 18)); // NOI18N
        userEmail.setToolTipText("Write your registered email address and press enter");
        userEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userEmailActionPerformed(evt);
            }
        });

        newPass1.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 18)); // NOI18N

        newPass2.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 18)); // NOI18N

        forgotPLabel.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 24)); // NOI18N
        forgotPLabel.setForeground(new java.awt.Color(255, 255, 255));
        forgotPLabel.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/src/main/java/images/forgotP3.png"))); // NOI18N
        forgotPLabel.setText("Forgot Your Passoword?");
        forgotPLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        forgotPLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                forgotPLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                forgotPLabelMouseExited(evt);
            }
        });

        emailWarning.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        emailWarning.setForeground(new java.awt.Color(255, 255, 255));

        newpwdWarning.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        newpwdWarning.setForeground(new java.awt.Color(255, 255, 255));

        tickLabel.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/src/main/java/images/Tick1.png"))); // NOI18N

        javax.swing.GroupLayout forgotPanelLayout = new javax.swing.GroupLayout(forgotPanel);
        forgotPanel.setLayout(forgotPanelLayout);
        forgotPanelLayout.setHorizontalGroup(
            forgotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(forgotPanelLayout.createSequentialGroup()
                .addGroup(forgotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(forgotPanelLayout.createSequentialGroup()
                        .addGap(94, 94, 94)
                        .addComponent(forgotPLabel))
                    .addGroup(forgotPanelLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(forgotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(forgotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, forgotPanelLayout.createSequentialGroup()
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(savePassbtn)
                                    .addGap(49, 49, 49)
                                    .addComponent(jButton4)
                                    .addGap(75, 75, 75))
                                .addGroup(forgotPanelLayout.createSequentialGroup()
                                    .addGroup(forgotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel6)
                                        .addComponent(jLabel7))
                                    .addGap(18, 18, 18)
                                    .addGroup(forgotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(newPass1)
                                        .addComponent(newPass2)
                                        .addGroup(forgotPanelLayout.createSequentialGroup()
                                            .addGap(0, 0, Short.MAX_VALUE)
                                            .addComponent(newpwdWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addGroup(forgotPanelLayout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(18, 18, 18)
                                .addGroup(forgotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(userEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                                    .addComponent(emailWarning, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tickLabel)))
                .addContainerGap(26, Short.MAX_VALUE))
        );
        forgotPanelLayout.setVerticalGroup(
            forgotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, forgotPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(forgotPLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                .addGroup(forgotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(tickLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(emailWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(forgotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newPass1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(28, 28, 28)
                .addGroup(forgotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newPass2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newpwdWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                .addGroup(forgotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4)
                    .addComponent(savePassbtn))
                .addGap(58, 58, 58))
        );

        signupPanel.setOpaque(false);

        jLabel8.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Enter your E-mail:");

        jLabel9.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Enter Username:");

        jLabel10.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("Enter Password:");

        jLabel11.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Confirm Password");

        createUsername.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createUsernameActionPerformed(evt);
            }
        });

        createEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createEmailActionPerformed(evt);
            }
        });

        createaccountBtn.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        createaccountBtn.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/src/main/java/images/report1.png"))); // NOI18N
        createaccountBtn.setText("Create Account");
        createaccountBtn.setPressedIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/src/main/java/images/report2.png"))); // NOI18N
        createaccountBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createaccountBtnActionPerformed(evt);
            }
        });

        jButton5.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        jButton5.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/src/main/java/images/cancle1.png"))); // NOI18N
        jButton5.setText("Cancel");
        jButton5.setPressedIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/src/main/java/images/cancle2.png"))); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        signupLabel.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 24)); // NOI18N
        signupLabel.setForeground(new java.awt.Color(255, 255, 255));
        signupLabel.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/src/main/java/images/signupB1.png"))); // NOI18N
        signupLabel.setText("Sign Up");
        signupLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                signupLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                signupLabelMouseExited(evt);
            }
        });

        createEmailWarning.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 16)); // NOI18N
        createEmailWarning.setForeground(new java.awt.Color(255, 255, 255));

        createUsernameWarning.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 16)); // NOI18N
        createUsernameWarning.setForeground(new java.awt.Color(255, 255, 255));

        createPwdWarning.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 16)); // NOI18N
        createPwdWarning.setForeground(new java.awt.Color(255, 255, 255));

        createEmailTick.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/src/main/java/images/Tick1.png"))); // NOI18N

        createUsernameTick.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/src/main/java/images/Tick1.png"))); // NOI18N

        javax.swing.GroupLayout signupPanelLayout = new javax.swing.GroupLayout(signupPanel);
        signupPanel.setLayout(signupPanelLayout);
        signupPanelLayout.setHorizontalGroup(
            signupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, signupPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(createaccountBtn)
                .addGap(48, 48, 48)
                .addComponent(jButton5)
                .addGap(102, 102, 102))
            .addGroup(signupPanelLayout.createSequentialGroup()
                .addGroup(signupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(signupPanelLayout.createSequentialGroup()
                        .addGap(210, 210, 210)
                        .addComponent(signupLabel))
                    .addGroup(signupPanelLayout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addGroup(signupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel9)
                            .addComponent(jLabel8)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11))
                        .addGap(18, 18, 18)
                        .addGroup(signupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(createEmailWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(signupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(createEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
                                .addComponent(createUsername)
                                .addComponent(createPass2))
                            .addComponent(createUsernameWarning, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(createPwdWarning, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(createPass1, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(signupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(createEmailTick)
                            .addComponent(createUsernameTick))))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        signupPanelLayout.setVerticalGroup(
            signupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(signupPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(signupLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(signupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(createEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(createEmailTick))
                .addGap(1, 1, 1)
                .addComponent(createEmailWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(signupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(signupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(createUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel9))
                    .addComponent(createUsernameTick))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(createUsernameWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(signupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createPass1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addGap(18, 18, 18)
                .addGroup(signupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createPass2, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(createPwdWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 95, Short.MAX_VALUE)
                .addGroup(signupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createaccountBtn)
                    .addComponent(jButton5))
                .addGap(59, 59, 59))
        );

        javax.swing.GroupLayout MainPanelLayout = new javax.swing.GroupLayout(MainPanel);
        MainPanel.setLayout(MainPanelLayout);
        MainPanelLayout.setHorizontalGroup(
            MainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1072, Short.MAX_VALUE)
            .addGroup(MainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(MainPanelLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(loginPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(MainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MainPanelLayout.createSequentialGroup()
                    .addContainerGap(231, Short.MAX_VALUE)
                    .addComponent(forgotPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(235, Short.MAX_VALUE)))
            .addGroup(MainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MainPanelLayout.createSequentialGroup()
                    .addContainerGap(198, Short.MAX_VALUE)
                    .addComponent(signupPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(201, Short.MAX_VALUE)))
        );
        MainPanelLayout.setVerticalGroup(
            MainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 754, Short.MAX_VALUE)
            .addGroup(MainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(MainPanelLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(loginPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(MainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MainPanelLayout.createSequentialGroup()
                    .addContainerGap(91, Short.MAX_VALUE)
                    .addComponent(forgotPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(88, Short.MAX_VALUE)))
            .addGroup(MainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MainPanelLayout.createSequentialGroup()
                    .addContainerGap(55, Short.MAX_VALUE)
                    .addComponent(signupPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(78, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1023, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(MainPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 715, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(MainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void forgotpasswordLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_forgotpasswordLabelMouseClicked
        loginPanel.setVisible(false);
        signupPanel.setVisible(false);
        forgotPanel.setVisible(true);
        //Components ReSetting:
        userEmail.setText(null);
        userEmail.setEditable(true);
        newPass1.setText(null);
        newPass2.setText(null);
        newPass1.setEnabled(false);
        newPass2.setEnabled(false);

        emailWarning.setText(null);
        newpwdWarning.setText(null);

        savePassbtn.setEnabled(false);
        tickLabel.setVisible(false);
    }//GEN-LAST:event_forgotpasswordLabelMouseClicked

    private void signinLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_signinLabelMouseClicked
        loginPanel.setVisible(false);
        forgotPanel.setVisible(false);
        signupPanel.setVisible(true);

        //Components Resetting:
        createEmail.setText(null);
        createEmail.setEditable(true);
        createUsername.setText(null);
        createUsername.setEditable(false);
        createPass1.setText(null);
        createPass2.setText(null);
        createPass1.setEditable(false);
        createPass2.setEditable(false);
        createaccountBtn.setEnabled(false);

        createEmailWarning.setText(null);
        createUsernameWarning.setText(null);
        createPwdWarning.setText(null);

        createEmailTick.setVisible(false);
        createUsernameTick.setVisible(false);
    }//GEN-LAST:event_signinLabelMouseClicked

    private void LogoMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_LogoMouseEntered
        Logo.setIcon(new ImageIcon(System.getProperty("user.dir").concat("/src/images/loginMain5.png")));
    }//GEN-LAST:event_LogoMouseEntered

    private void LogoMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_LogoMouseExited
        Logo.setIcon(new ImageIcon(System.getProperty("user.dir").concat("/src/images/loginMain4.png")));

    }//GEN-LAST:event_LogoMouseExited

    private void forgotPLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_forgotPLabelMouseEntered
        forgotPLabel.setIcon(new ImageIcon(System.getProperty("user.dir").concat("/src/images/forgotP4.png")));

    }//GEN-LAST:event_forgotPLabelMouseEntered

    private void forgotPLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_forgotPLabelMouseExited
        forgotPLabel.setIcon(new ImageIcon(System.getProperty("user.dir").concat("/src/images/forgotP3.png")));
    }//GEN-LAST:event_forgotPLabelMouseExited

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        loginPanel.setVisible(true);
        forgotPanel.setVisible(false);
        signupPanel.setVisible(false);


    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        loginPanel.setVisible(true);
        forgotPanel.setVisible(false);
        signupPanel.setVisible(false);

    }//GEN-LAST:event_jButton5ActionPerformed

    private void signupLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_signupLabelMouseEntered
        signupLabel.setIcon(new ImageIcon(System.getProperty("user.dir").concat("/src/images/signupB2.png")));
    }//GEN-LAST:event_signupLabelMouseEntered

    private void signupLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_signupLabelMouseExited
        signupLabel.setIcon(new ImageIcon(System.getProperty("user.dir").concat("/src/images/signupB1.png")));
    }//GEN-LAST:event_signupLabelMouseExited

    private void signinButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_signinButtonActionPerformed
        try {
            stmt = ConnectionDB.con.prepareStatement("Select * from Login Where Username = ?");
            stmt.setString(1, username.getText());
            rs = stmt.executeQuery();
            if (rs.next()) {
                if ((username.getText().equals(rs.getString("Username"))) && (userpwd.getText().equals(rs.getString("Password")))) {
                    this.setVisible(false);
                    new App().setVisible(true);

                } else {
                    pwdWarning.setText("*Invalid password");
                }

            } else {
                userWarning.setText("*Username does not exist");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                rs.close();
                stmt.close();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Undefined error");
            }
        }


    }//GEN-LAST:event_signinButtonActionPerformed

    private void usernameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_usernameFocusGained
        username.setText(null);
        userWarning.setText(null);
    }//GEN-LAST:event_usernameFocusGained

    private void userpwdFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_userpwdFocusGained
        userpwd.setText(null);
        pwdWarning.setText(null);
    }//GEN-LAST:event_userpwdFocusGained

    private void userEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userEmailActionPerformed
        if (isValidEmailAddress(userEmail.getText())) {
            try {
                stmt = ConnectionDB.con.prepareStatement("Select * from Login Where Email = ?");
                stmt.setString(1, userEmail.getText());
                rs = stmt.executeQuery();
                if (rs.next()) {
                    tickLabel.setVisible(true);
                    newPass1.setEnabled(true);
                    newPass2.setEnabled(true);
                    emailWarning.setText(null);
                    savePassbtn.setEnabled(true);
                    userEmail.setEditable(false);
                } else {
                    emailWarning.setText("*This email is not registered");
                }

            } catch (SQLException ex) {
                Logger.getLogger(LOGINFrame.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            emailWarning.setText("*Incorrect Email address");
        }

    }//GEN-LAST:event_userEmailActionPerformed

    private void savePassbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_savePassbtnActionPerformed
        if ((newPass1.getText().isEmpty()) && (newPass2.getText().isEmpty())) {
            newpwdWarning.setText("*Fill all feilds");
        } else if (newPass1.getText().equals(newPass2.getText())) {

            try {
                stmt = ConnectionDB.con.prepareStatement("Update Login Set Password= ? Where Email= '" + userEmail.getText() + "'");

                stmt.setString(1, newPass1.getText());
                stmt.execute();
                stmt.close();
                forgotPanel.setVisible(false);
                loginPanel.setVisible(true);

            } catch (SQLException ex) {
                Logger.getLogger(LOGINFrame.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            newpwdWarning.setText("*Password does not match");
        }


    }//GEN-LAST:event_savePassbtnActionPerformed

    private void createEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createEmailActionPerformed
        if (isValidEmailAddress(createEmail.getText())) {

            try {
                stmt = ConnectionDB.con.prepareStatement("Select * from Login where Email=?");
                stmt.setString(1, createEmail.getText());
                rs = stmt.executeQuery();
                if (rs.next()) {
                    createEmailWarning.setText("*This email is already registered");
                } else {
                    createEmail.setEditable(false);
                    createEmailWarning.setText(null);
                    createEmailTick.setVisible(true);
                    createUsername.setEditable(true);

                }
            } catch (SQLException ex) {
                Logger.getLogger(LOGINFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }//Correct email 
        else {
            createEmailWarning.setText("*Enter a valid email address");
        }
    }//GEN-LAST:event_createEmailActionPerformed

    private void createUsernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createUsernameActionPerformed
        try {
            stmt = ConnectionDB.con.prepareStatement("Select * from Login where Username=?");
            stmt.setString(1, createUsername.getText());
            rs = stmt.executeQuery();
            if (rs.next()) {
                createUsernameWarning.setText("*Username already exists");
            } else {
                createUsername.setEditable(false);
                createUsernameWarning.setText(null);
                createUsernameTick.setVisible(true);
                createPass1.setEditable(true);
                createPass2.setEditable(true);
                createaccountBtn.setEnabled(true);

            }
        } catch (SQLException ex) {
            Logger.getLogger(LOGINFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_createUsernameActionPerformed

    private void createaccountBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createaccountBtnActionPerformed
        if ((createPass1.getText().isEmpty()) && (createPass2.getText().isEmpty())) {
            createPwdWarning.setText("*Fill all fields");
        } else {
            if (createPass1.getText().equals(createPass2.getText())) {

                try {
                    stmt = ConnectionDB.con.prepareStatement("INSERT INTO Login (Username,Email,Password) VALUES(?,?,?)");
                    stmt.setString(1, createUsername.getText());
                    stmt.setString(2, createEmail.getText());
                    stmt.setString(3, createPass1.getText());
                    stmt.execute();
                    stmt.close();

                    signupPanel.setVisible(false);
                    loginPanel.setVisible(true);

                } catch (SQLException ex) {
                    Logger.getLogger(LOGINFrame.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                createPwdWarning.setText("*Password does not match");
            }
        }


    }//GEN-LAST:event_createaccountBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LOGINFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LOGINFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LOGINFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LOGINFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LOGINFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Logo;
    private javax.swing.JPanel MainPanel;
    private javax.swing.JTextField createEmail;
    private javax.swing.JLabel createEmailTick;
    private javax.swing.JLabel createEmailWarning;
    private javax.swing.JPasswordField createPass1;
    private javax.swing.JPasswordField createPass2;
    private javax.swing.JLabel createPwdWarning;
    private javax.swing.JTextField createUsername;
    private javax.swing.JLabel createUsernameTick;
    private javax.swing.JLabel createUsernameWarning;
    private javax.swing.JButton createaccountBtn;
    private javax.swing.JLabel emailWarning;
    private javax.swing.JLabel forgotPLabel;
    private javax.swing.JPanel forgotPanel;
    private javax.swing.JLabel forgotpasswordLabel;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel loginPanel;
    private javax.swing.JPasswordField newPass1;
    private javax.swing.JPasswordField newPass2;
    private javax.swing.JLabel newpwdWarning;
    private javax.swing.JLabel pwdWarning;
    private javax.swing.JButton savePassbtn;
    private javax.swing.JButton signinButton;
    private javax.swing.JLabel signinLabel;
    private javax.swing.JLabel signupLabel;
    private javax.swing.JPanel signupPanel;
    private javax.swing.JLabel tickLabel;
    private javax.swing.JTextField userEmail;
    private javax.swing.JLabel userWarning;
    private javax.swing.JTextField username;
    private javax.swing.JPasswordField userpwd;
    // End of variables declaration//GEN-END:variables
}
