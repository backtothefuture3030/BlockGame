import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;


public class BlockGame {
	
	static class MyFrame extends JFrame{
		
		// constant 상수
		static int BALL_WIDTH = 20;
		static int BALL_HEIGHT = 20;
		static int BLOCK_ROWS = 5;
		static int BLOCK_COLUMMS = 10;
		static int BLOCK_WIDTH = 40;
		static int BLOCK_HEIGHT = 20;
		static int BLOCK_GAP = 3;
		static int BAR_WIDTH = 80;
		static int BAR_HEIGHT = 20;
		static int CANVAS_WIDTH = 400+(BLOCK_GAP*BLOCK_COLUMMS)-BLOCK_GAP;  // 화면위치
		static int CANVAS_HEIGHT = 600;
		
		static int total =1;
		// variable 
		static MyPanel myPanel =null;
		static int score = 0;
		static Timer timer = null;
		static Block[][] blocks = new Block[BLOCK_ROWS][BLOCK_COLUMMS];		// 일단 공간만 만들어진것 객체를 만든것은아님
		static Bar bar = new Bar();
		static Ball ball = new Ball();
		static int barXTarget = bar.x; // Target Value - interporation 보간법 // 이걸쓰는이유가 이 변수를 만들어뒀던 이유가 뚝뚝끊기게 움직이는 바모양을 쓰지않고 유연한 움직임을 보기위해서이다
		static int dir =0;	//공 움직이는 방향  0: Up-Right(북동) 1:Down-Right(남동) 2:Up-Left(북서) 3:Down-Left(남서)
		static int ballSpeed =5;
		static boolean isGameFinish = false;
		
		static class Ball{
			int x=CANVAS_WIDTH/2 - BALL_WIDTH/2;	// 화면 센터에 그리기위해서
			int y=CANVAS_HEIGHT/2 - BALL_HEIGHT/2;
			int width=BALL_WIDTH;
			int height=BALL_HEIGHT;
			
			Point getCenter() {	//원의 중심좌표
				return new Point(x+(BALL_WIDTH/2),y+(BALL_HEIGHT/2));
			}
			Point getBottomCenter() {	// 원과 바가 만나는곳
				return new Point(x+(BALL_WIDTH/2),y+(BALL_HEIGHT));
			}
			Point getTopCenter() {	// 원의 젤 윗공간
				return new Point(x+(BALL_WIDTH/2),y);
			}
			Point getLeftCenter() {
				return new Point(x,y+(BALL_HEIGHT/2));
			}
			Point getRightCenter() {	
				return new Point(x+(BALL_WIDTH),y+(BALL_HEIGHT/2));
			}
		}
		
		
		static class Bar{
			int x=CANVAS_WIDTH/2 -BAR_WIDTH/2 ;	// x설정
			int y=CANVAS_HEIGHT-100;
			int width = BAR_WIDTH;
			int height=BAR_HEIGHT;
		}
		
		
		static class Block{	//블록모양이 랜덤으로 변함
			int x=0;
			int y=0;
			int width = BLOCK_WIDTH;
			int height = BLOCK_HEIGHT;
			int color = 0 ;  // 0:white , 1:yellow , 2:blue , 3:mazanta(주황), 4:red 	
			boolean isHidden = false; // 충돌후에, 블록이 사라지게 하는도구 불린값
		}
		
		
		static class MyPanel extends JPanel{	// UI를 디자인하는곳 //CANVAS FOR DRAW!
			public MyPanel() {
				this.setSize(CANVAS_WIDTH,CANVAS_HEIGHT);
				this.setBackground(Color.BLACK);
			}
			@Override	// JPanel 클래스 안쪽에있으므로 오버라이드 하는거야
			public void paint(Graphics g) {
				super.paint(g);
				Graphics2D g2d = (Graphics2D)g; // 형변환과정
				
				drawUI(g2d);
			}
			private void drawUI(Graphics2D g2d) {
				//블록들을 그린다
				for(int i = 0; i <BLOCK_ROWS; i++) {
					for(int j=0;j<BLOCK_COLUMMS;j++) {
						if(blocks[i][j].isHidden) {
							continue;	// 히든이면 블록을 그릴필요없다
						}
						if(blocks[i][j].color==0) {
							g2d.setColor(Color.WHITE);
						}
						else if(blocks[i][j].color==1) {
							g2d.setColor(Color.YELLOW);
						}
						else if(blocks[i][j].color==2) {
							g2d.setColor(Color.BLUE);
						}
						else if(blocks[i][j].color==3) {
							g2d.setColor(Color.MAGENTA);
						}
						else if(blocks[i][j].color==4) {
							g2d.setColor(Color.RED);
						}
						g2d.fillRect(blocks[i][j].x,blocks[i][j].y, blocks[i][j].width, blocks[i][j].height);
			
					}
					
					// 점수판
					g2d.setColor(Color.WHITE);
					g2d.setFont(new Font("TimesRoman",Font.BOLD,20));
					g2d.drawString("SCORE : "+score+"점", CANVAS_WIDTH/2-60, 20);
					g2d.drawString(total+"번째 도전", CANVAS_WIDTH/2-60, 40);
					if (isGameFinish) {
						g2d.setColor(Color.RED);
						g2d.drawString("Game Finish!", CANVAS_WIDTH/2-60, 60);
					}
						
					
					// 공을 그리자
					g2d.setColor(Color.WHITE);
					g2d.fillOval(ball.x, ball.y, BALL_WIDTH, BALL_HEIGHT );
					
					// 바를 그리자
					g2d.setColor(Color.WHITE);
					g2d.fillRect(bar.x, bar.y, BAR_WIDTH, BAR_HEIGHT );
				}
			}
			 
		}
		
		
		public MyFrame(String title) {
			super(title);
			this.setVisible(true); // 화면을 보이게한다	
			this.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);//this는 마이프레임을말함 MyFrame
			this.setLocation(400,300); // 가운데로 옮기기위해서
			this.setLayout(new BorderLayout());
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// 종료버튼
			
			
			initData(); // 초기화
			
			myPanel = new MyPanel();	//캠퍼스 역할을함
			this.add("Center", myPanel);
			
			setKeyListener();
			startTimer();
		}
		
		public void initData(){
			for(int i = 0; i <BLOCK_ROWS; i++) {
				for(int j=0;j<BLOCK_COLUMMS;j++) {
					blocks[i][j]=new Block();	// 그공간에 채운것
					blocks[i][j].x=BLOCK_WIDTH*j + BLOCK_GAP*j;
					blocks[i][j].y=100 /*상단공간을 둔다*/ +BLOCK_HEIGHT*i + BLOCK_GAP*i; // i는 세로 j는 가로
					blocks[i][j].width=BLOCK_WIDTH;
					blocks[i][j].height=BLOCK_HEIGHT;
					blocks[i][j].color = 4-i;	// 아래쪽부터 흰색으로 만든다 0:white , 1:yellow , 2:blue , 3:mazanta(주황), 4:red 	
					blocks[i][j].isHidden = false;
				}
			}
			
		}
		public void setKeyListener() {	//addKeyListener가 원래 있다 JFrame에
			this.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {	//KeyEvent 는 컴퓨터에 키보드를 치면 KeyEvent가 e라는 객체변수를 통해 들어옴
					if(e.getKeyCode() == KeyEvent.VK_LEFT) {
						//System.out.println("Pressed Left Key");
						barXTarget -= 20; 	// 이걸쓰는이유가 이 변수를 만들어뒀던 이유가 뚝뚝끊기게 움직이는 바모양을 쓰지않고 유연한 움직임을 보기위해서이다
						if(bar.x<barXTarget) {	// 바가 벽에 끝까지 갔는데 계속 누르는경우
							barXTarget=bar.x;
						}
					}
					else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
						//System.out.println("Pressed Right Key");
						barXTarget += 20; 	// 이걸쓰는이유가 이 변수를 만들어뒀던 이유가 뚝뚝끊기게 움직이는 바모양을 쓰지않고 유연한 움직임을 보기위해서이다
						if(bar.x>barXTarget) {	// 바가 벽에 끝까지 갔는데 계속 누르는경우
							barXTarget=bar.x;
						}
					}
				}
			});
			
		}
		public void startTimer() {
			 timer = new Timer(20, new ActionListener() {
				 @Override
				 public void actionPerformed(ActionEvent e) {	// ActionEvent는 타이머이벤트다
					 movement();	
					 checkCollsion();	// 벽과 바에 충돌처리
					 checkCollsionBlock();	// 50의블록에 충돌되도록.
					 myPanel.repaint();	// 다시그리게하는것
					 
					 //Game Success!
					 isGameFinish();
				 }
			 }); 
			 timer.start();	// 지금 타이머 스타트
				 	 
		}
		public void isGameFinish() {
			//Game Success!
			int count=0;
			for(int i=0; i<BLOCK_ROWS;i++) {
				for(int j=0; j<BLOCK_COLUMMS;j++) {
					Block block = blocks[i][j];
					if(block.isHidden) {
						count++;
					}
				}
			}
			if(count==BLOCK_ROWS*BLOCK_COLUMMS) {
				//Game Finished
				//timer.stop();
				isGameFinish = true;
				timer.stop();
			}
		}
		
		
		
		public void movement() {
			if(bar.x<barXTarget) {
				bar.x+=5;
			}else if(bar.x>barXTarget) {
				bar.x-=5;
			}
			if (dir==0) {	// 0: 북동쪽
				ball.x+=ballSpeed;
				ball.y-=ballSpeed;
			}else if(dir==1) {	// 1: 남동쪽
				ball.x+=ballSpeed;
				ball.y+=ballSpeed;
			}else if(dir==2) {	// 2: 북서쪽
				ball.x-=ballSpeed;
				ball.y-=ballSpeed;
			}else if(dir==3) {	// 3: 남서쪽
				ball.x-=ballSpeed;
				ball.y+=ballSpeed;
			}
			
		}
		public boolean duplRect(Rectangle rect1,Rectangle rect2) {
			return rect1.intersects(rect2);	//체크 하는 함수 두개의 사각형이 만나는것에 대한 (duplicate) // 자바 지원함수
		}
		
		public void checkCollsion() {
			
			if (dir==0) {	// 0: 북동쪽
				//벽에 충돌될경우
				if(ball.y<0) { // 천장벽에 충돌할경우
					dir=1;
				}
				if(ball.x>CANVAS_WIDTH-BALL_WIDTH) {	// 벽의 오른쪽방향에 충돌할경우
					dir=2;
				}
				// 바가 충돌 될 경우가 없다.
			}else if(dir==1) {	// 1: 남동쪽
				//벽에 충돌될경우
				if(ball.y > CANVAS_HEIGHT-BALL_HEIGHT-BALL_HEIGHT) {	// 아래쪽 벽에 충돌할 경우 , 이렇게 BALL_HEIGHT를 두번빼지않으면 공이 바닥에 잠기는거 처럼 보인다
					dir=0;
					
					//game reset 
					dir = 0;
					ball.x=CANVAS_WIDTH/2 - BALL_WIDTH/2;	
					ball.y=CANVAS_HEIGHT/2 - BALL_HEIGHT/2;
					score=0;
					initData();
					total++;
				}
				if(ball.x>CANVAS_WIDTH-BALL_WIDTH) {	// 아래쪽으로 내려오는 공이 벽의 오른쪽방향에 충돌할경우
					dir=3;
				}
				// 바와 충돌 될 경우.
				if(ball.getBottomCenter().y>=bar.y) {
					if(duplRect(new Rectangle(ball.x,ball.y,ball.width,ball.height),
							new Rectangle(bar.x,bar.y,bar.width,bar.height))) {
						dir=0;
					}
				}
				
			}else if(dir==2) {	// 2: 북서쪽	// 바와 충돌없음
				//벽에 충돌될경우
				if(ball.y<0) { // 위쪽벽에 충돌할경우
					dir=3;
				}
				if(ball.x<0) { //왼쪽벽에 충돌
					dir=0;
				}
			}else if(dir==3) {	// 3: 남서쪽
				//벽에 충돌될경우
				if(ball.y>CANVAS_HEIGHT-BALL_HEIGHT-BALL_HEIGHT){ //아래쪽 벽 , 이렇게 BALL_HEIGHT를 두번빼지않으면 공이 바닥에 잠기는거 처럼 보인다
					dir=2;
					
					//game reset 
					dir = 0;
					ball.x=CANVAS_WIDTH/2 - BALL_WIDTH/2;	
					ball.y=CANVAS_HEIGHT/2 - BALL_HEIGHT/2;
					score=0;
					initData();
					total++;
				}
				if(ball.x<0) { // 왼쪽벽
					dir=1;
				}
				// 바와 충돌할때
				if(ball.getBottomCenter().y>=bar.y) {
					if(duplRect(new Rectangle(ball.x,ball.y,ball.width,ball.height),
							new Rectangle(bar.x,bar.y,bar.width,bar.height))) {
						dir=2;
					}
				}
			}
		}
		public void checkCollsionBlock() {
			// 0: Up-Right(북동) 1:Down-Right(남동) 2:Up-Left(북서) 3:Down-Left(남서)
			for(int i=0; i<BLOCK_ROWS;i++) {
				for(int j=0; j<BLOCK_COLUMMS;j++) {
					Block block = blocks[i][j];
					if(block.isHidden==false) {	// 충돌하기전 
						if(dir==0) { // 0: Up-Right(북동)
							if(duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height),
									    new Rectangle(block.x, block.y, block.width, block.height))) {
								if(ball.x>block.x+2 && ball.getRightCenter().x<=block.x+block.width-2) { // 2라는 갭을 이용해 안쪽블록과 만날수있게 해준다 버그방지
									// 블록의 아래쪽에 부딫혔을때
									dir=1;
								}else {
									// 블록의 왼쪽 옆벽에 부딫히는경우
									dir =2;
								}
								block.isHidden=true;
								if(block.color==0) {
									score+=10;
								}else if(block.color==1) {
									score+=20;
								}else if(block.color==2) {
									score+=30;
								}else if(block.color==3) {
									score+=40;
								}else if(block.color==4) {
									score+=50;
								}
							}
						}
						else if(dir==1) { // 1:Down-Right(남동)
							if(duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height),
								    new Rectangle(block.x, block.y, block.width, block.height))) {
								if(ball.x>block.x+2 && ball.getRightCenter().x<=block.x+block.width-2) { // 2라는 갭을 이용해 안쪽블록과 만날수있게 해준다 버그방지
									// 블록의 위쪽에 부딫혔을때
									dir=0;
								}else {
									// 블록의 왼쪽 옆벽에 부딫히는경우
									dir =3;
								}
								block.isHidden=true;
								if(block.color==0) {
									score+=10;
								}else if(block.color==1) {
									score+=20;
								}else if(block.color==2) {
									score+=30;
								}else if(block.color==3) {
									score+=40;
								}else if(block.color==4) {
									score+=50;
								}
							}
						}
						else if(dir==2) { // 2:Up-Left(북서)
							if(duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height),
								    new Rectangle(block.x, block.y, block.width, block.height))) {
								if(ball.x>block.x+2 && ball.getRightCenter().x<=block.x+block.width-2) { // 2라는 갭을 이용해 안쪽블록과 만날수있게 해준다 버그방지
									// 블록의 아래쪽에 부딫혔을때
									dir=3;
								}else {
									// 블록의 오른쪽 옆벽에 부딫히는경우
									dir =0;
								}
								block.isHidden=true;
								if(block.color==0) {
									score+=10;
								}else if(block.color==1) {
									score+=20;
								}else if(block.color==2) {
									score+=30;
								}else if(block.color==3) {
									score+=40;
								}else if(block.color==4) {
									score+=50;
								}
							}
							
						}
						else if(dir==3) { // 3:Down-Left(남서)
							if(duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height),
								    new Rectangle(block.x, block.y, block.width, block.height))) {
								if(ball.x>block.x+2 && ball.getRightCenter().x<=block.x+block.width-2) { // 2라는 갭을 이용해 안쪽블록과 만날수있게 해준다 버그방지
									// 블록의 위쪽에 부딫혔을때
									dir=2;
								}else {
									// 블록의 오른쪽 옆벽에 부딫히는경우
									dir =1;
								}
								block.isHidden=true;
								if(block.color==0) {
									score+=10;
								}else if(block.color==1) {
									score+=20;
								}else if(block.color==2) {
									score+=30;
								}else if(block.color==3) {
									score+=40;
								}else if(block.color==4) {
									score+=50;
								}
							}
						}
						
						
					}
				}
			}
			
		}
	}

	public static void main(String[] args) {
		
		new MyFrame("Block Game");

	}

	
	
	
}
