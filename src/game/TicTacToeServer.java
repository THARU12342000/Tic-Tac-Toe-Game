package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TicTacToeServer {

	private static Game game = new Game();
	private static int currentPlayer = 0; // 0 for Player X, 1 for Player O

	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(5999)) {
			System.out.println("Server started on port 5999");

			while (true) {
				Socket clientSocket = serverSocket.accept();
				new ClientHandler(clientSocket).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class ClientHandler extends Thread {
		private Socket clientSocket;
		private BufferedReader reader;
		private PrintWriter writer;

		ClientHandler(Socket socket) {
			this.clientSocket = socket;
			try {
				InputStreamReader inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
				reader = new BufferedReader(inputStreamReader);
				writer = new PrintWriter(clientSocket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				// Welcome message
				writer.println("Welcome to Tic-Tac-Toe! You are Player " + (currentPlayer == 0 ? "X" : "O"));
				writer.flush();

				// Game loop
				while (true) {
					String clientMove = reader.readLine();
					if (clientMove != null) {
						int markedPosition = Integer.parseInt(clientMove);
						String response = game.makeMove(currentPlayer == 0 ? Game.CROSS : Game.CIRCLE, markedPosition);

						// Send the response to the client
						writer.println(response);
						writer.flush();

						if (response.startsWith("$$$ Game Over!")) {
							break; // Game over, exit loop
						}

						// Change turn for the next player
						currentPlayer = (currentPlayer + 1) % 2;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					clientSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
