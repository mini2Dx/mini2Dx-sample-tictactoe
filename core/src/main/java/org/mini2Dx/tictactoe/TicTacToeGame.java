/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 See AUTHORS file
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.mini2Dx.tictactoe;

import org.mini2Dx.core.game.BasicGame;
import org.mini2Dx.core.geom.Rectangle;
import org.mini2Dx.core.graphics.Graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;

/**
 * The main game logic
 */
public class TicTacToeGame extends BasicGame {
	public static final String GAME_IDENTIFIER = "org.mini2Dx.tictactoe";

	// Stores the spaces on the game board as a 2D array
	private final Space[][] board = new Space[3][3];
	// Stores the state of the game
	private GameState state = GameState.BEGIN;

	// Use GlyphLayout to calculate the size of rendered text
	private final GlyphLayout glyphLayout = new GlyphLayout();

	// Stores where the board should be rendered
	private int boardOffsetX, boardOffsetY;
	// Stores the size each space on the board should be rendered at
	private int spaceSize;

	// Timer for starting the game
	private float startTimer = 5f;
	// Store if the victory has been seen
	private boolean victoryAcknowledged = false;

	@Override
	public void initialise() {
		// Calculate the board position and size - we'll place it center screen
		spaceSize = Math.min(getWidth() / 4, getHeight() / 4);
		boardOffsetX = (getWidth() / 2) - ((spaceSize * 3) / 2);
		boardOffsetY = (getHeight() / 2) - ((spaceSize * 3) / 2);

		resetBoard();
	}

	@Override
	public void update(float delta) {
		switch (state) {
		case PLAYER_1_TURN:
		case PLAYER_2_TURN:
			boolean isPlayer1 = state == GameState.PLAYER_1_TURN;

			// Wait for the player to touch/click the screen
			if (!Gdx.input.isTouched()) {
				return;
			}
			// Check that the player touched/clicked the board
			if (!handleInput(isPlayer1, Gdx.input.getX(), Gdx.input.getY())) {
				return;
			}
			if (!isVictory(isPlayer1)) {
				if(isTied()) {
					state = GameState.TIED;
				} else {
					// Game is not won or tied, go to next turn
					if (isPlayer1) {
						state = GameState.PLAYER_2_TURN;
					} else {
						state = GameState.PLAYER_1_TURN;
					}
				}
				return;
			}
			if (isPlayer1) {
				state = GameState.PLAYER_1_VICTORY;
			} else {
				state = GameState.PLAYER_2_VICTORY;
			}
			break;
		case PLAYER_1_VICTORY:
		case PLAYER_2_VICTORY:
		case TIED:
			//As the game runs as 60FPS, the screen may still be touched from the previous frame
			if (!victoryAcknowledged) {
				if(!Gdx.input.isTouched()) {
					victoryAcknowledged = true;
				}
				return;
			}
			// Wait for a touch or click to start a new game
			if (Gdx.input.isTouched()) {
				state = GameState.BEGIN;
				resetBoard();
			}
			break;
		case BEGIN:
		default:
			startTimer -= delta;
			if (startTimer <= 0f) {
				// Reset the timer for the next game
				startTimer = 5f;
				// Start the game
				state = GameState.PLAYER_1_TURN;
			}
			break;
		}
	}

	@Override
	public void interpolate(float alpha) {
		// No moving sprites so this method does nothing
	}

	@Override
	public void render(Graphics g) {
		g.setBackgroundColor(Color.WHITE);

		renderBoard(g);
		switch (state) {
		case PLAYER_1_TURN:
			g.setColor(Color.BLUE);
			renderMessage(g, "Player 1's turn");
			break;
		case PLAYER_1_VICTORY:
			g.setColor(Color.GREEN);
			renderMessage(g, "Player 1 wins!");
			break;
		case PLAYER_2_TURN:
			g.setColor(Color.PURPLE);
			renderMessage(g, "Player 2's turn");
			break;
		case PLAYER_2_VICTORY:
			g.setColor(Color.GREEN);
			renderMessage(g, "Player 2 wins!");
			break;
		case TIED:
			g.setColor(Color.BLACK);
			renderMessage(g, "Game is tied :(");
			break;
		case BEGIN:
		default:
			// Render a countdown
			g.setColor(Color.BLACK);
			renderMessage(g, "Starting game in " + MathUtils.round(startTimer) + " seconds...");
			break;
		}
	}
	
	/**
	 * Resets the game board
	 */
	private void resetBoard() {
		victoryAcknowledged = false;
		
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				board[x][y] = Space.FREE;
			}
		}
	}

	/**
	 * Handles player input
	 * 
	 * @param isPlayer1
	 *            True if player 1 touched the screen
	 * @param touchX
	 *            The x coordinate that was touched
	 * @param touchY
	 *            The y coordinate that was touched
	 * @return True if the board changed
	 */
	private boolean handleInput(boolean isPlayer1, int touchX, int touchY) {
		Rectangle rectangle = new Rectangle();

		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				// Check to see if the board space was touched
				rectangle.set(boardOffsetX + (x * spaceSize), boardOffsetY + (y * spaceSize), spaceSize, spaceSize);
				if (!rectangle.contains(touchX, touchY)) {
					continue;
				}
				// Ensure it is a free space
				if (board[x][y] != Space.FREE) {
					continue;
				}
				if (isPlayer1) {
					board[x][y] = Space.X;
				} else {
					board[x][y] = Space.O;
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if a player has won the game
	 * @param isPlayer1 True if we're checking for player 1's victory
	 * @return True if the player won the game
	 */
	private boolean isVictory(boolean isPlayer1) {
		Space search = isPlayer1 ? Space.X : Space.O;

		// Check rows
		for (int y = 0; y < 3; y++) {
			if (board[0][y] != search) {
				continue;
			}
			if (board[1][y] != search) {
				continue;
			}
			if (board[2][y] != search) {
				continue;
			}
			return true;
		}

		// Check columns
		for (int x = 0; x < 3; x++) {
			if (board[x][0] != search) {
				continue;
			}
			if (board[x][1] != search) {
				continue;
			}
			if (board[x][2] != search) {
				continue;
			}
			return true;
		}

		// Check diagonally
		if (board[0][0] == search && board[1][1] == search && board[2][2] == search) {
			return true;
		}
		if (board[0][0] == search && board[1][1] == search && board[2][2] == search) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if the game is tied (i.e. the board is full)
	 * @return True if the game is tied
	 */
	private boolean isTied() {
		for(int x = 0; x < 3; x++) {
			for(int y = 0; y < 3; y++) {
				if(board[x][y] == Space.FREE) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Renders a message at the top center of the screen
	 * 
	 * @param g
	 *            The {@link Graphics} context
	 * @param message
	 *            The message to render
	 */
	private void renderMessage(Graphics g, String message) {
		glyphLayout.setText(g.getFont(), message);
		int renderX = MathUtils.round((getWidth() / 2) - (glyphLayout.width / 2f));
		int renderY = MathUtils.round(glyphLayout.height);
		g.drawString(message, renderX, renderY);
	}

	/**
	 * Renders the game board
	 * 
	 * @param g
	 *            The {@link Graphics} context
	 */
	private void renderBoard(Graphics g) {
		g.setLineHeight(4);
		g.setColor(Color.BLACK);

		int quarterSpaceSize = (spaceSize / 4);
		int halfSpaceSize = (spaceSize / 2);

		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				int renderX = boardOffsetX + (x * spaceSize);
				int renderY = boardOffsetY + (y * spaceSize);
				g.drawRect(renderX, renderY, spaceSize, spaceSize);

				switch (board[x][y]) {
				case O:
					g.drawCircle(renderX + halfSpaceSize, renderY + halfSpaceSize, quarterSpaceSize);
					break;
				case X:
					g.drawLineSegment(renderX + quarterSpaceSize, renderY + quarterSpaceSize,
							renderX + quarterSpaceSize + halfSpaceSize, renderY + quarterSpaceSize + halfSpaceSize);
					g.drawLineSegment(renderX + halfSpaceSize + quarterSpaceSize, renderY + quarterSpaceSize,
							renderX + quarterSpaceSize, renderY + quarterSpaceSize + halfSpaceSize);
					break;
				case FREE:
				default:
					break;
				}
			}
		}
	}
}
