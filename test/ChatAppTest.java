import client.LoginWindow;
import server.ChatServer;

public class ChatAppTest {
	public static void main(String args[]) throws InterruptedException {
		ChatAppTest chatAppTest = new ChatAppTest();
		chatAppTest.testv1();
	}
	
	// 1st test
	public void testv1() {
		// starts server thread
		Thread server = new Thread(new ChatServer());
		server.start();
		
		// creates 4 login windows
		LoginWindow aoife = new LoginWindow();
		LoginWindow joseph = new LoginWindow();
		LoginWindow matthew = new LoginWindow();
		LoginWindow emily = new LoginWindow();
	}
}