package BaseBallGame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class HitMap extends JFrame {
	JLabel userTeamName=new JLabel(); // 플레이어 팀 이름 레이블
	JLabel opponentTeamName=new JLabel(); // 적팀 이름 레이블
	JLabel userScore=new JLabel("0"); // 플레이어 득점 현황 레이블
	JLabel opponentScore=new JLabel("0"); // 적팀 득점 현황 레이블
	boolean swing = false; //베트를 휘둘렀는지 논리변수.
	int inningCount = 0; //이닝 카운트 
	int strikeCount = 0; //스트라이크 카운트
	int outCount = 0; //아웃 카운트
	ImageIcon normalBatIcon = new ImageIcon("images/bat.png"); // 기본 베트 이미지
	ImageIcon swingBatIcon = new ImageIcon("images/swingBat.gif");
	Field field_Frame;
	JLabel b_ball = new JLabel(new ImageIcon("images/b_ball.png"));
	JLabel[] countLight = new JLabel[4]; // 볼 스트라이크 아웃 카운트 레이블 배열
	JLabel hitZone = new JLabel(normalBatIcon);
	ballThread ballthread; // 공 날라오는 스레드
	CountDownThread countDownThread; // 공 던지기 제어 스레드. 3초간의 카운트다운 이후 투구.
	JLabel lbl; //  볼 -스트- 아웃 알려주는 레이블
	private JLabel countdownLabel=new JLabel(" "); // 투수가 공 던지기까지 대기하는 와인드업 타임. 즉 공이 투구되기까지의 카운트 다운 출력 레이블.
	private JLabel noticeLabel=new JLabel("투수가 준비중입니다..."); // 투수 와인드업 알림 레이블
	String[] hitTypeArray = new String[100]; // 타격 성공시, 타격의 종류를 나타내는 문자열 배열.
	//안타는 35%,플라이아웃은 25%, 그라운드아웃은 25% 2루타는 10%, 홈런은 5% 확률
	Boolean inning_end=false;
	Boolean write_end=false;
	Boolean countSync=true; // 카운트 다운 동기성을 맞추기위한 논리변수
	Boolean prevent_SwingError=false;  // 투수가 공 던지기전에 스윙하는 것을 방지하기위한 논리변수 => 기존, 투수가 공 던지기전에 스윙하면 스트라이크 판정 및 전체적인 투구 알고리즘에 에러를 유발시켰음
	String HitType;
	int last_inning=3; // 게임 회 수 설정. 몇 회까지 게임 진행할지 last_inning 변수로 결정가능.
	
	//상대팀 공격 이닝 자동화 알고리즘 메서드.
	//플레이어 이닝 종료 후 호출됨.0점: 30% 1점: 25% 2점: 20% 3점: 15% 4점: 7% 5점: 3%
	public void opponentScore_Algorithm() {
		
		int opponent_result=0;
		
		double randomNum = Math.random();

	    if (randomNum < 0.3) {
	        opponent_result = 0;
	    } 
	    else if (randomNum < 0.55) {
	        opponent_result = 1;
	    } 
	    else if (randomNum < 0.75) {
	        opponent_result = 2;
	    } 
	    else if (randomNum < 0.9) {
	        opponent_result = 3;
	    } 
	    else if (randomNum < 0.97) {
	        opponent_result = 4;
	    } 
	    else {
	        opponent_result = 5;
	    }
	    field_Frame.score[1][inningCount]=opponent_result;
	    
	    
	    if(inningCount+1 != last_inning)
	    	JOptionPane.showMessageDialog(null,(inningCount+1)+"회초 공격이 종료되었습니다\n이어서 상대팀의 공격이 이어집니다",(inningCount+1)+"회초 공격 종료",JOptionPane.PLAIN_MESSAGE);
	    else 
	    	JOptionPane.showMessageDialog(null,(inningCount+1)+"회초 마지막 공격이 종료되었습니다",(inningCount+1)+"회초 마지막 공격 종료",JOptionPane.PLAIN_MESSAGE);
	    
	    
		 field_Frame.clips.get(1).start();
		 field_Frame.clips.get(1).setFramePosition(0);
	    
	    
	    try {
			Thread.sleep(2500);
			field_Frame.printScore(inningCount);
			JOptionPane.showMessageDialog(null,"수비 결과 → ("+opponent_result+"점) 실점하였습니다","상대팀 공격 턴",JOptionPane.WARNING_MESSAGE);
			field_Frame.clips.get(1).start();
			field_Frame.clips.get(1).setFramePosition(0);
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO: handle exception
			return;
		}
	    
	    
	    if(inningCount+1 != last_inning) {
	    	try {
	    		JOptionPane.showMessageDialog(null, (inningCount+2)+"회초 경기가 시작됩니다.준비하세요","경기 시작",JOptionPane.WARNING_MESSAGE);
	    		field_Frame.clips.get(1).start();
	   		 	field_Frame.clips.get(1).setFramePosition(0);
				Thread.sleep(2500);
				field_Frame.clips.get(12).stop(); //이닝교체 오디오 무한루프 중지.
				field_Frame.clips.get(6).loop(Clip.LOOP_CONTINUOUSLY); // 관중 사운드 다시 시작.
			} catch (InterruptedException e) {
				// TODO: handle exception
				return;
			}
	    }	
	    else {
	    	JOptionPane.showMessageDialog(null,"경기 종료~!!!","경기 종료",JOptionPane.PLAIN_MESSAGE);
	    	field_Frame.clips.get(1).start();
	    	field_Frame.clips.get(1).setFramePosition(0);
	    	field_Frame.clips.get(12).stop();//이닝교체 오디오 무한루프 중지.
	    	//경기 승리 or 패배 or 무승부시 알고리즘 작성
	    	if(Integer.parseInt(field_Frame.scoreLabel[0][9].getText()) > Integer.parseInt(field_Frame.scoreLabel[1][9].getText())) {
	    		field_Frame.clips.get(18).loop(Clip.LOOP_CONTINUOUSLY);	
	    		JOptionPane.showMessageDialog(null,"최종 결과▶▶ "+field_Frame.team_name+": "+field_Frame.scoreLabel[0][9].getText()+"점    적팀: "+field_Frame.scoreLabel[1][9].getText()+"점\n"+
	    		field_Frame.team_name+" 승리~!!!!! 승리를 축하합니다","경기 승리",JOptionPane.PLAIN_MESSAGE);
	    	 		
	    	}
	    	else if(Integer.parseInt(field_Frame.scoreLabel[0][9].getText()) < Integer.parseInt(field_Frame.scoreLabel[1][9].getText())) {
	    		field_Frame.clips.get(20).loop(Clip.LOOP_CONTINUOUSLY);	
	    		JOptionPane.showMessageDialog(null,"최종 결과▶▶ "+field_Frame.team_name+": "+field_Frame.scoreLabel[0][9].getText()+"점    적팀: "+field_Frame.scoreLabel[1][9].getText()+"점\n"+
	    		field_Frame.team_name+" 패배. try again","경기 패배",JOptionPane.PLAIN_MESSAGE);
	    		
	    	}
	    	else {
	    		field_Frame.clips.get(19).loop(Clip.LOOP_CONTINUOUSLY);	
	    		JOptionPane.showMessageDialog(null,"최종 결과▶▶ "+field_Frame.team_name+": "+field_Frame.scoreLabel[0][9].getText()+"점    적팀: "+field_Frame.scoreLabel[1][9].getText()+"점\n"+
	    		"적팀과 치열한 접전끝에 무승부!!!","무승부",JOptionPane.PLAIN_MESSAGE);
	    		
	    	}
	    }
	    field_Frame.changeFrame();
	    lbl.setText("get ready");
	    
	    
	    	countDownThread.resumeThread();
	    	
	    
	}
	
	//이닝 처리 알고리즘 (5회까지)
	public void inningAlgorithm() {
		
		field_Frame.clips.get(6).stop();  // 관중 사운드 중단
		field_Frame.clips.get(12).loop(Clip.LOOP_CONTINUOUSLY);  // 이닝교체 오디오 무한루프.
		//관중 사운드 처음부터 다시 x. 멈춘부분부터 나오도록하기위해 아래구문 주석처리함.
		//field_Frame.clips.get(6).setFramePosition(0); // 관중 사운드 재사용을 위한 프레임 위치 조정
		
		inning_end=true;
		opponentScore_Algorithm();
		inningCount++;
		
		
		
		//상대 팀 공격 자동화. showmessageDialog() 로 상대팀 득점 결과 나오도록
		
		if(inningCount==last_inning) {
			System.exit(0);
		}
	}
	
	//스트라이크 처리 알고리즘
	public void strikeAlgorithm() {
	    strikeCount++; // 스트라이크 카운트 추가
	    ballthread.pauseThread();
	    b_ball.setLocation(500, 0);
	    
	    if (strikeCount <= 2) {
	        countLight[strikeCount - 1].setVisible(true); // 스트라이크 카운트 라이트 켜기
	        lbl.setText("STRIKE "+strikeCount);
	        if(strikeCount==1) { //스트라이크 1 오디오 재생
	    		field_Frame.clips.get(14).start();
	   		 	field_Frame.clips.get(14).setFramePosition(0);
	        }else if(strikeCount==2) {//스트라이크 2 오디오 재생
	    		field_Frame.clips.get(15).start();
	   		 	field_Frame.clips.get(15).setFramePosition(0);
	        }
	    } 
	    
	    else if (strikeCount == 3) { // 3 스트라이크 => 아웃 카운트 ++
	    	field_Frame.clips.get(16).start(); // 스트라이크 삼진 아웃 오디오 재생
   		 	field_Frame.clips.get(16).setFramePosition(0);
	        strikeCount = 0; // 스트라이크 카운트 초기화
	        countLight[0].setVisible(false);
	        countLight[1].setVisible(false);
	        outCount++;
	        field_Frame.output("삼진");
	        if (outCount <= 2) {
	            countLight[1 + outCount].setVisible(true); // 아웃 카운트 라이트 켜기
	            lbl.setText("OUT "+outCount);
	        } 
	        else if (outCount == 3) {
	            lbl.setText("strike out!!!");
	            outCount = 0; // 아웃 카운트 초기화
	            countLight[2].setVisible(false);
	            countLight[3].setVisible(false);
	            field_Frame.reset_Base(); // 이닝 종료. 베이스 비어있도록 구현
	            normal_changeFrame();
	            inningAlgorithm();
	            if(inningCount != last_inning)
	            field_Frame.output("\n "+" "+(inningCount+1)+"회 :");//삼진시, 파일 입력
	        }

	    }
	    hitZone.setIcon(normalBatIcon);
	}
	//아웃 처리 알고리즘
	public void outAlgorithm() {
			
	        outCount++; // 아웃 카운트 증가
	        strikeCount = 0; // 스트라이크 카운트 초기화
	        ballthread.pauseThread();
	        b_ball.setLocation(500, 0);
	        
	        if (outCount <= 2) {
	        	inning_end=false;
	            countLight[1 + outCount].setVisible(true); // 아웃 카운트 라이트 켜기
	            lbl.setText("OUT "+outCount);
	        } 
	        else if (outCount == 3) {
	            lbl.setText("inning end!!!");
	            outCount = 0; // 아웃 카운트 초기화
	            countLight[2].setVisible(false);
	            countLight[3].setVisible(false);
	            field_Frame.reset_Base();
	            
	            if(HitType.equals("fly-out"))
	            	field_Frame.output("뜬공");
	            else if(HitType.equals("ground-out"))
	            	field_Frame.output("땅볼");
	            if(inningCount+1 != last_inning)
				field_Frame.output("\n "+" "+(inningCount+2)+"회 :");//파일 입력
				
	            inningAlgorithm();
	        }
	        
	        for(int i = 0; i < 2; i++) {
	            countLight[i].setVisible(false); // 스트라이크 카운트 라이트 끄기
	        }
	        hitZone.setIcon(normalBatIcon);
	    }
	
	
	//베트를 휘두르지 않았을 경우, 즉 루킹 스트라이크 당했을경우 스트라이크 처리를 위한 메서드
	public void strikeTimestop_swingN(int second) {
		Timer timer = new Timer(second, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				strikeAlgorithm();
				countDownThread.resumeThread();
			}
		});

		timer.setRepeats(false);
		timer.start();

	}
	//베트를 휘둘렀을 경우, 즉 헛스윙한 경우 스트라이크 처리를 위한 메서드.
	//h 키를 눌러서 swing 값이 true 가 된다음, 다시 false 로 값을 바꿔줘야하기에.
	//h 키를 눌러 swing 값이 true가 된 후에 false 로 다시 안바꿔주면 swing 변수는 계속 true 값이되어
	//헛스윙 + 루킹 스트라이크 중복되어 strikeAlgorithm()가 동시에 두번 호출되기에 오류가 난다. 
	//so, 별도의 strikeTimestop_sw() 라는 헛스윙시의 별도의 메서드를 만들어준 것이다.
	public void strikeTimestop_swingY(int second) {
		Timer timer = new Timer(second, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				strikeAlgorithm();
				swing=false;
				countDownThread.resumeThread();
			}
		});
		
		timer.setRepeats(false);
		timer.start();
		
	}

	public String random_HitType() { // 랜덤한 타격 종류 선택하여 타격 종류의 문자열을 리턴해주는 메서드
		int randomNum = (int) (Math.random() * hitTypeArray.length);
		return hitTypeArray[randomNum];
	}
	

	public void timeStop_hit_changeFrame(int second, String s) {
		Timer timer = new Timer(second, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				 HitType = s;
				swing=false;
				hit_changeFrame();

				if (HitType.equals("single")) {
					inning_end=false;
					field_Frame.single_hit();
					strikeCount=0;
				}
				else if (HitType.equals("double")) {
					inning_end=false;
					field_Frame.double_hit();
					strikeCount=0;
				}
				else if (HitType.equals("homerun")) {

					inning_end=false;
					field_Frame.homerun();
					strikeCount=0;
				}
				else if (HitType.equals("fly-out")) {
					outAlgorithm();
					field_Frame.flyout();
					
				}
				else if (HitType.equals("ground-out")) {
					outAlgorithm();
					field_Frame.groundout();
					
				}

				/*
				 * else if(random_HitType().equals("fly_out")) field_Frame.fly_out(); else
				 * field_Frame.ground_out();
				 */

				// 이러면 2초 대기후 화면 전환 후 => field 에서 타격으로인한 베이스 진루 적용 후 4.5 대기 후 다시 이곳으로 전환됨.
				// 추후, random 으로 sigle or double or 홈런 or 땅볼아웃 플라이아웃으로.
				lbl.setText("get ready");
				hitZone.setBackground(new Color(150, 75, 0));
				b_ball.setLocation(500, 0);
				hitZone.setIcon(normalBatIcon);
				
			}
		});

		timer.setRepeats(false);
		timer.start();

	}

	public void hit_changeFrame() { // 탸격 성공시의 야구장 필드 프레임으로 화면 전환 메서드. 1.2 초 뒤에 전환됨.( 타격 결과 확인을 위한 텀)
		
		field_Frame.setVisible(true);
		dispose();
        countLight[0].setVisible(false);
        countLight[1].setVisible(false);
	}

	public void normal_changeFrame() { // 야구장 필드 프레임으로 화면 전환 메서드.

		field_Frame.setVisible(true);
		dispose();
	}

	class HitPanel extends JPanel {
		
		
		Image countboard = new ImageIcon("images/countboard.png").getImage();
		Image hitter = new ImageIcon("images/hitter.png").getImage();
		Image soil = new ImageIcon("images/soil.jpg").getImage();
		//Image grass = new ImageIcon("images/grass.png").getImage();
		
		public HitPanel() {
			// TODO Auto-generated constructor stub
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setLayout(null);
			//안타=> 30%, 플라이아웃=> 25%, 땅볼 아웃=> 25%, 2루타=>13%, 홈런=> 7%
			for (int i = 0; i < 100; i++) {
			    if (i < 30) {
			        hitTypeArray[i] = "single";
			    } else if (i < 55) {
			        hitTypeArray[i] = "fly-out";
			    } else if (i < 80) {
			        hitTypeArray[i] = "ground-out";
			    } else if (i < 93) {
			        hitTypeArray[i] = "double";
			    } else {
			        hitTypeArray[i] = "homerun";
			    }
			}
			
			userTeamName.setLocation(740, 0);
			userTeamName.setSize(100, 100);
			userTeamName.setForeground(Color.black);
			userTeamName.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
			this.add(userTeamName);
			
				
			opponentTeamName.setLocation(850, 0);
			opponentTeamName.setSize(100, 100);
			opponentTeamName.setForeground(new Color(102,102,102));
			opponentTeamName.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
			this.add(opponentTeamName);
						
			
			userScore.setLocation(760, 40);
			userScore.setSize(100, 100);
			userScore.setForeground(new Color(51,204,0));
			userScore.setFont(new Font("Malgun Gothic", Font.BOLD, 40));
			this.add(userScore);
			
				
			opponentScore.setLocation(860, 40);
			opponentScore.setSize(100, 100);
			opponentScore.setForeground(new Color(153,0,51));
			opponentScore.setFont(new Font("Malgun Gothic", Font.BOLD, 40));
			this.add(opponentScore);
					
			
			lbl = new JLabel("get ready");
			lbl.setLocation(700, 200);
			lbl.setSize(400, 200);
			lbl.setForeground(Color.RED);
			lbl.setFont(new Font("jokerman", Font.BOLD, 30));
			this.add(lbl);
			
			countdownLabel.setLocation(150, 180);
			countdownLabel.setSize(100, 50);
			countdownLabel.setForeground(Color.MAGENTA);
			countdownLabel.setFont(new Font("jokerman", Font.BOLD, 30));
			this.add(countdownLabel); 
			noticeLabel.setLocation(0, 250);
			noticeLabel.setSize(350, 100);
			noticeLabel.setForeground(Color.black);
			noticeLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 30));
			this.add(noticeLabel); 

			// 야구공 레이블
			b_ball.setSize(30, 30);
			b_ball.setLocation(500, 0);
			add(b_ball);
			// 히트존 레이블
			hitZone.setSize(300, 300);
			hitZone.setLocation(400, 530);
			// hitZone.setBackground(new Color(150, 75, 0));
			// hitZone.setOpaque(true);
			add(hitZone);

			/// 아웃 스트라이크 볼 카운트 레이블들 구성 및 패널에 부착
			for (int i = 0; i < 4; i++) {
				if (i <= 1) {
					countLight[i] = new JLabel(new ImageIcon("images/STRIKE.png")); // countLight 0,1,2 은 볼 카운트 라이트
					countLight[i].setLocation(49 + i * 47, 103);

				} else {
					countLight[i] = new JLabel(new ImageIcon("images/OUT.png"));// countLight 5,6 은 볼 카운트 라이트
					countLight[i].setLocation(185 + i * 44, 102);
				}
				countLight[i].setSize(30, 30);
				add(countLight[i]);
				countLight[i].setVisible(false); // 초기 카운트보드 라이트의 상태는 비어있음.
			}

			setFocusable(true); // HitPanel 에게 키포커스 주기.
			addKeyListener(new KeyAdapter() {

				@Override
				public void keyPressed(KeyEvent e) {

					if (!swing) { //h 키 연타를 방지하기위해 swing 변수 이용.
								  // 야구에서도 베트는 한번만 스윙가능하니깐.
								  //즉, 논리변수를 통해 베트는 한번만 스윙할 수 있도록 구현하였다.
					if (e.getKeyChar() == 'h' && prevent_SwingError == true) {
						swing=true; // h 키를 눌러서 베트를 스윙함.
						hitZone.setIcon(swingBatIcon);
						field_Frame.clips.get(5).start();
						field_Frame.clips.get(5).setFramePosition(0);
						/// **************** 이 아래 if 문이 바로 타격 성공시 !!! *****************//
						if (b_ball.getY() >= 680 - 80 && b_ball.getY() <= 680 + 5) { // 히트존
																						// 설정.어느정도 Y
																						// 축까지를
																						// 히트존으로
							
							// 즉, 타격 성공 구역으로 설정할지는 -50 +10 수정하여 조절가능.
							ballthread.pauseThread();// 타격 성공시 공이 멈춤.
							// 위에걸 지우고 공이 멈추는게 아니라 날아가도록 구현해보자 => 알고리즘 복잡해짐. 당장은 불가능
							field_Frame.clips.get(7).start();
							field_Frame.clips.get(7).setFramePosition(0);
							
							
							
							String hitType = random_HitType();
							lbl.setText(hitType);
							if(hitType.equals("homerun")) { //홈런이면 홈런 오디오 실행
								Timer timer = new Timer(800, new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										 field_Frame.clips.get(8).start();
										 field_Frame.clips.get(8).setFramePosition(0);
									}
								});

								timer.setRepeats(false);
								timer.start();

							} else if(hitType.equals("fly-out") || hitType.equals("ground-out")) {//뜬공&땅볼이면 관중야유 오디오 실행
								Timer timer = new Timer(700, new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										 field_Frame.clips.get(17).start();
										 field_Frame.clips.get(17).setFramePosition(0);									
									}
								});

								timer.setRepeats(false);
								timer.start();
								Timer timer2 = new Timer(1200, new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {								
										 field_Frame.clips.get(9).start();
										 field_Frame.clips.get(9).setFramePosition(0);
									}
								});

								timer2.setRepeats(false);
								timer2.start();
							}else {   //1루타 + 2루타
								Timer timer = new Timer(800, new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										 field_Frame.clips.get(10).start();
										 field_Frame.clips.get(10).setFramePosition(0);
									}
								});

								timer.setRepeats(false);
								timer.start();
								
							}
							timeStop_hit_changeFrame(2000, hitType); // 그리고 필드 화면으로 전환되어 필드 상태 확인.
							// 이제 추가로, 타격 결과를 설정하고, 그에 따라 카운트 보드 및 베이스 진루 설정해야지
							
						} else// 헛스윙. h 키를 눌러서 스윙했지만 타격은 실패한 경우.
						{
							strikeTimestop_swingY(2000);
						}
						
					}

				}
				}
			});
		}

		@Override
		protected void paintComponent(Graphics g) {
			// TODO Auto-generated method stub
			super.paintComponent(g);
			g.drawImage(soil, 0, 450, 1000, 450, this);
			g.drawImage(countboard, 0, 0, 400, 150, this);
			g.drawImage(hitter, 650, 550, 100, 200, this);
			
			//g.drawImage(grass, 650, 550, 100, 200, this);
			//히팅존. 
			g.setColor(Color.YELLOW);			
			g.drawRect(450, 610, 150, 75);
			//공 주변 가시성 향상을 위한 배경 그래픽
			g.setColor(Color.ORANGE);
			g.fillRoundRect(475, 0, 80, 60, 40, 60);
			//점수 현황 가시성 향상을 위한 배경 그래픽
			g.setColor(new Color(153,153,255));
			g.drawRect(670, 0, 330, 160);
		}
	}

	public HitMap(Field field_Frame) {
		// TODO Auto-generated constructor stub
		this.field_Frame = field_Frame;

		setContentPane(new HitPanel());

		this.setSize(1000, 900);
		this.setTitle("HITTING ZONE");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		double height = screenSize.getHeight();

		int x = (int) ((width - 1000) / 2);
		int y = (int) ((height - 900) / 2);

		this.setLocation(x, y);
		
	}
	
	
	
	/// 공 투구 자동화 카운트다운 스레드
	public class CountDownThread extends Thread {
		
		 boolean paused = true; // 스레드 일시 중지 여부를 체크하는 변수
	     int count=6;
	    
	    public void pauseThread() { // 공 멈추기 메서드
			paused = true; // 외부에서 호출하여 스레드를 일시 중지
		}

		public void resumeThread() { // 공 던지기 메서드
			field_Frame.clips.get(4).start();
			field_Frame.clips.get(4).setFramePosition(0);
			countdownLabel.setVisible(true);
			noticeLabel.setOpaque(false);
			noticeLabel.setText("투수가 준비중입니다...");
			synchronized (this) {
				paused = false;
				notify(); // 스레드 재개를 위해 일시 중지 상태 해제
			}
		}

		@Override
		public void run() {

		    while (true) {
		    	prevent_SwingError=false;
		        synchronized (this) {
		            if(paused) {
		                try {
		                    wait();
		                } catch (InterruptedException e) {
		                    return;
		                }
		            }
		        }
		        if(count==1) {
		        	if(field_Frame.audioStarted==true) {
		        		//field_Frame.clips.get(1).close(); //버튼클릭 오디오 클립 자원 반환시키기. 메모리 누수 방지차원에서 시스템 리소스를 회수.
		        		field_Frame.clips.get(3).close(); //버튼클릭 오디오 클립 자원 반환시키기. 메모리 누수 방지차원에서 시스템 리소스를 회수.
		        		field_Frame.audioStarted=false;
		        	}
		        	noticeLabel.setOpaque(true);
		        	noticeLabel.setBackground(Color.red);
		        	noticeLabel.setForeground(Color.blue);
		        	noticeLabel.setText("타격을 준비하세요!!!!");
		        }
		        if (count == 0) {
		        	prevent_SwingError=true;
		            ballthread.resumeThread();
		            this.pauseThread();
		            countdownLabel.setVisible(false);
		            if(countSync==false)
		            	count=7;
		            else {
		            	count = 7;  // 카운트 다운을 재시작할 수 있도록 count를 초기화
		            	countSync=false;
		            }
		            
		        }
		        
		        countdownLabel.setText(Integer.toString(count));
		        count--;

		        try {
		            Thread.sleep(1000);
		        } catch (InterruptedException e) {
		            return;
		        }
		    }
		}
	}
	/// 공 스레드
	class ballThread extends Thread {
		int ballX;
		int ballSpeed;
		Random rand = new Random(); // 랜덤 객체 생성
		int randomBall; 
		int hitZoneCenterX = hitZone.getX() + hitZone.getWidth() / 3; // hitZone의 중점 x 좌표
		// 진폭과 주파수를 랜덤하게 설정
		private boolean paused = true; // 스레드 일시 중지 여부를 체크하는 변수

		public void pauseThread() { // 공 멈추기 메서드
			paused = true; // 외부에서 호출하여 스레드를 일시 중지
		}

		public void resumeThread() { // 공 던지기 메서드
			synchronized (this) {
				paused = false;
				randomBall= rand.nextInt(2);
				notify(); // 스레드 재개를 위해 일시 중지 상태 해제
			}
		}

		@Override
		public void run() {
			while (true) {
				
				
				synchronized (this) {
					if (paused) { // 일시 중지 플래그가 true이면 실행을 대기
						try {
							wait();
						} catch (InterruptedException e) {
							return;
						}
					}
				}
				
				// 공의 y 좌표를 먼저 증가
				int ballY = b_ball.getY() + 10;
				b_ball.setLocation(b_ball.getX(), ballY);
				 
				if(ballY == 1020 && swing==false) { //looking 스트라이크 아웃. 
					strikeTimestop_swingN(2000);
				}
				
				double amplitude = hitZoneCenterX / 10.0; // 고정된 진폭 값으로 설정
				double frequency = 2 * Math.PI / getHeight() * 0.5; // 고정된 주파수 값으로 설정

				// 공의 x 좌표를 y 좌표의 사인 함수로 설정. hitZone의 중점을 중심으로 휘어져서 이동
				
				if(randomBall==0) {
					 ballX = hitZoneCenterX + (int) (amplitude * Math.sin(frequency * ballY)); // "+", "-" 이냐에 따라서 방향변경됨
				}else {
					 ballX = hitZoneCenterX - (int) (amplitude * Math.sin(frequency * ballY)); // "+", "-" 이냐에 따라서 방향변경됨
				}
				
				b_ball.setLocation(ballX, b_ball.getY());

				try {
					sleep(ballSpeed); // 한 10~ 1 정도?
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}