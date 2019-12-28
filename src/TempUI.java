import java.awt.Color;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.bluetooth.RemoteDevice;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

public class TempUI extends JFrame implements ActionListener {
	
	
	JLabel temp = new JLabel("0", SwingConstants.CENTER), hu = new JLabel("0", SwingConstants.CENTER), stat = new JLabel("", SwingConstants.CENTER);;
	JButton timesendbtn = new JButton("시간 동기화"), sendmsgbtn = new JButton("전송");
	JTextField textfield = new JTextField();
	JComboBox<String> devicecombo = new JComboBox<>();
	
	public TempUI() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(400, 300);
		setLocationRelativeTo(null);
		setResizable(false);
		setLayout(null);
		
		setTitle("온습도 패널");
		getContentPane().setBackground(SystemColor.gray);
		
		stat.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
		stat.setBounds(0, 0, getWidth(), 20);
		stat.setForeground(Color.white);
		add(stat);
		
		TitledBorder T_tb = new TitledBorder("현재 온도");
		T_tb.setTitleColor(Color.white);
		T_tb.setTitleFont(new Font("맑은 고딕", Font.PLAIN, 16));
		
		
		temp.setFont(new Font("맑은 고딕", Font.BOLD, 64));
		temp.setBounds(10, 50, 180, 150);
		temp.setForeground(Color.white);
		temp.setBorder(T_tb);
		add(temp);
		
		TitledBorder H_tb = new TitledBorder("현재 습도");
		H_tb.setTitleColor(Color.white);
		H_tb.setTitleFont(new Font("맑은 고딕", Font.PLAIN, 16));
		
		hu.setFont(new Font("맑은 고딕", Font.BOLD, 64));
		hu.setBounds(210, 50, 180, 150);
		hu.setForeground(Color.white);
		hu.setBorder(H_tb);
		add(hu);

		timesendbtn.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
		timesendbtn.setBounds(10, 205, getWidth() - 20, 25);
		timesendbtn.setBorderPainted(false);
		timesendbtn.setContentAreaFilled(false);
		timesendbtn.setFocusable(false);
		timesendbtn.setOpaque(true);
		timesendbtn.setForeground(Color.white);
		timesendbtn.setBackground(SystemColor.textHighlight);
		timesendbtn.addActionListener(c -> {
			try {
				TempBlueTooth.os.write(new String("Date " + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date(System.currentTimeMillis()))).getBytes());
				TempBlueTooth.os.flush();
				stat.setText("현재 시간으로 동기화했습니다.");
			} catch (IOException e) {
				stat.setText("동기화하지 못했습니다.");
				e.printStackTrace();
			}
		});
		add(timesendbtn);

		textfield.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
		textfield.setBounds(10, 235, 310, 25);
		textfield.setToolTipText("띄울 메세지");
		textfield.setBackground(Color.LIGHT_GRAY);
		textfield.setBorder(null);
		
		textfield.addActionListener(this);
		add(textfield);

		sendmsgbtn.setFont(new Font("맑은 고딕", Font.PLAIN, 15));		
		sendmsgbtn.setBorderPainted(false);
		sendmsgbtn.setContentAreaFilled(false);
		sendmsgbtn.setFocusable(false);
		sendmsgbtn.setOpaque(true);
		sendmsgbtn.setBounds(320, 235, 70, 25);
		
		sendmsgbtn.addActionListener(this);
		
		add(sendmsgbtn);
		
		
		devicecombo.setBounds(10,20,380,30);
		devicecombo.setEnabled(false);
		add(devicecombo);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			if(textfield.getText().isEmpty() || textfield.getText() == null || textfield.getText().trim().isEmpty())
				return;
			String msg = textfield.getText().trim();
			textfield.setText("");
			TempBlueTooth.os.write(new String("msg " + msg).getBytes());
			TempBlueTooth.os.flush();
			stat.setText("메세지 전송 완료");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
