package com.antwars.termiernator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This is the main class of the Termiernator program. Responsible for obtaining
 * and maintaining an http connection to the AntWars board. Receives and sends
 * game info for each turn. Console output is generated to inform the user on
 * game development.
 **/
public class Client {
	private static int MAX_SCORE = 0;

	// Use these variables to name your bot
	private static String PLAYER_NAME = "Anonymous";
	private static String BOT_NAME = "Termiernator";
	private static String BOT_VERSION = "0.0.0";

	// Store your player & game ID that AntWars assigned to you
	private static String PLAYER_ID;
	private static String GAME_ID;

	/**
	 * Set-up for the game.
	 * 
	 * @param args
	 * @throws org.apache.http.ParseException
	 * @throws IOException
	 */
	public static void main(String[] args) throws org.apache.http.ParseException, IOException {
		System.out.println("Game started!");

		JsonObject response = joinGame();
		setGame(response);
		playGame();

		System.out.println("Game Ended max points reached: " + MAX_SCORE);
	}

	/**
	 * Connect to the AntWars API and request to start a new game.
	 * 
	 * @return JsonObject: represents the current game state.
	 */
	public static JsonObject joinGame() {
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			HttpPost request = new HttpPost("http://antwars.azurewebsites.net/api/game/start");
			StringEntity params = new StringEntity("{\"playerName\": \"" + PLAYER_NAME + "\",\"botName\": \""
					+ PLAYER_NAME + "\",\"botVersion\": \"" + BOT_VERSION + "\"}");
			request.addHeader("content-type", "application/json");
			request.addHeader("Accept", "application/json");
			request.setEntity(params);
			HttpResponse response = httpClient.execute(request);
			String json = EntityUtils.toString(response.getEntity());
			JsonObject jsonGameState = (JsonObject) new JsonParser().parse(json);

			System.out.println("Successfully connected to http://antwars.azurewebsites.net/api/game/start");
			return jsonGameState;
		} catch (Exception ex) {
			System.out.println(ex);
			return null;
		}
	}

	/**
	 * Example method that can be used to pass the current game info to your
	 * game engine, so it can calculate your next moves.
	 * 
	 * @param jsonGameState
	 * @throws org.apache.http.ParseException
	 * @throws IOException
	 */
	public static void setGame(JsonObject jsonGameState) throws org.apache.http.ParseException, IOException {
		PLAYER_ID = jsonGameState.get("playerId").getAsString();
		GAME_ID = jsonGameState.get("id").getAsString();

		System.out.println("We are assigned the player id: " + PLAYER_ID + " for game: " + GAME_ID);

		/**
		 * I can't reveil my awesomeness, so you'll have to implement your own
		 * Game engine! but here is an example call that shows how to accesss
		 * the parameters in the gameState json Object.
		 **/

		// Game myGame = new Game(jsonGameState.get("playerId").getAsString(),
		// jsonGameState.get("id").getAsString(),
		// jsonGameState.get("state").getAsInt(),
		// jsonGameState.get("unitTypes").getAsJsonArray(),
		// jsonGameState.get("units").getAsJsonArray());
	}

	/**
	 * Method responsible for sending the moves for 1 round, for all your ants.
	 * 
	 * @throws org.apache.http.ParseException
	 * @throws IOException
	 */
	public static void playGame() throws org.apache.http.ParseException, IOException {
		// This is a dummy. You will want your Game object to determine if the
		// Game is over.
		// If you leave this dummy, you will be playing even though you already
		// lost. Sad.
		boolean gameOver = false;

		while (gameOver != true) {
			// Get your best next moves from your Game Engine.
			/// You can check the API for allowed moves and their format.
			StringEntity dummyMoves = formatEmptyMove();
			JsonObject newGameState = sendMove(dummyMoves);

			// Go to next turn 
			System.out.println("\nCURRENT TURN: " + newGameState.get("turn"));
			// use the new Game information to keep your Game up to date
			updateGame(newGameState);
		}
	}

	/**
	 * Send the moves for the current round and receive the changed game state.
	 * 
	 * @param moves
	 * @return JsonObject represents the new game state after your moves have
	 *         been processed.
	 */
	public static JsonObject sendMove(StringEntity moves) {
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			String url = ("http://antwars.azurewebsites.net/api/game/" + GAME_ID + "/move/" + PLAYER_ID);
			HttpPost move = new HttpPost(url);
			move.setHeader("content-type", "application/json");
			move.addHeader("Accept", "application/json");
			move.setEntity(moves);
			HttpResponse reply = httpClient.execute(move);

			System.out.println("Moves have been successfully sended!");
			String jReply = EntityUtils.toString(reply.getEntity());
			JsonObject j = (JsonObject) new JsonParser().parse(jReply);
			return j;
		} catch (Exception ex) {
			System.out.println(ex);
			return null;
		}
	}

	/**
	 * Dummy method to format an empty move that contains no commands.
	 * 
	 * @return StringEntity that represents an empty move
	 * @throws UnsupportedEncodingException
	 */
	public static StringEntity formatEmptyMove() throws UnsupportedEncodingException {
		StringBuilder parameter = new StringBuilder("");
		parameter.append("{");
		parameter.append("}");
		parameter.append("");
		String p = parameter.toString();

		StringEntity para = new StringEntity("[" + p + "]");
		return para;
	}

	/**
	 * Process the changes that came from your moves (and your opponent's moves)
	 * to update your game.
	 * 
	 * @param newGameState
	 */
	public static void updateGame(JsonObject newGameState) {
		System.out.println(newGameState.toString());
		
		// This is dummy behaviour to show how to access game state information.
		// You might want to incorporate this info in your Game object.
		int gameState = newGameState.get("state").getAsInt();
		JsonArray unitArray = newGameState.get("units").getAsJsonArray();
		int points = ((JsonObject) newGameState.get("playerState").getAsJsonObject()).get("points").getAsInt();
		if (points > MAX_SCORE)
			MAX_SCORE = points;
	}

}
